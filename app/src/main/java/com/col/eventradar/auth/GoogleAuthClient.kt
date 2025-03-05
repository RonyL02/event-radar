package com.col.eventradar.auth

import android.content.Context
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import com.google.android.libraries.identity.googleid.GetGoogleIdOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential
import com.google.android.libraries.identity.googleid.GoogleIdTokenParsingException
import com.google.firebase.auth.AuthResult
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.GoogleAuthProvider
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.tasks.await

class GoogleAuthClient(
    private val context: Context,
) {
    private val auth = FirebaseAuth.getInstance()
    private val credentialManager = CredentialManager.create(context)

    suspend fun isSignedIn(): Boolean = auth.currentUser != null

    private var _userId: String? = null
    val userId = _userId
    private var _isNewUser: Boolean = false
    val isNewUser = _isNewUser

    fun getCurrentUser() = auth.currentUser


    suspend fun signIn(): Pair<Boolean, String?>? {
        println(auth.currentUser?.displayName)
        if (isSignedIn()) return null

        return try {
            val credentialResponse = buildCredentialRequest()
            val authResult = handleSignIn(credentialResponse)
            _userId = authResult?.user?.uid
            _isNewUser = authResult?.additionalUserInfo?.isNewUser ?: false
            Log.i(TAG, "Signed in successfully!")
            Pair(_isNewUser, _userId)
        } catch (e: CancellationException) {
            throw e
        } catch (e: Exception) {
            Log.e(TAG, "Sign-in failed: ${e.message}", e)
            null
        }
    }

    private suspend fun buildCredentialRequest(): GetCredentialResponse {
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(
                GetGoogleIdOption.Builder().setFilterByAuthorizedAccounts(false)
                    .setAutoSelectEnabled(false)
                    .setServerClientId(CLIENT_ID)
                    .build()
            ).build()

        return credentialManager.getCredential(context, request)
    }

    private suspend fun handleSignIn(result: GetCredentialResponse): AuthResult? {
        val credential = result.credential

        if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {
            return try {
                val tokenCredential = GoogleIdTokenCredential.createFrom(credential.data)
                Log.d(TAG, "Signing in with Google: ${tokenCredential.displayName}")

                val authCredential = GoogleAuthProvider.getCredential(tokenCredential.idToken, null)

                val authResult = auth.signInWithCredential(authCredential).await()

                authResult
            } catch (e: GoogleIdTokenParsingException) {
                Log.e(TAG, "Google ID Token Parsing failed: ${e.message}", e)
                null
            }
        } else {
            Log.e(TAG, "Credential is not a valid Google ID Token")
            return null
        }
    }

    suspend fun signOut() {
        try {
            credentialManager.clearCredentialState(ClearCredentialStateRequest())
            auth.signOut()
            Log.d(TAG, "Successfully signed out")
        } catch (e: Exception) {
            Log.e(TAG, "Sign-out failed: ${e.message}", e)
        }
    }

    companion object {
        private const val TAG = "GoogleAuthClient"
        private const val CLIENT_ID =
            "799091358025-uipjcncengnellt4f1qlgk1esm9mmgts.apps.googleusercontent.com"
    }
}