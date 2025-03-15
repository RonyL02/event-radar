package com.col.eventradar.auth

import android.content.Context
import android.content.Intent
import android.provider.Settings
import android.util.Log
import androidx.credentials.ClearCredentialStateRequest
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.GetCredentialResponse
import androidx.credentials.exceptions.NoCredentialException
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


    suspend fun signIn(callback: (signInResult: Pair<Boolean, String?>) -> Unit) {
        if (isSignedIn()) {
            callback(Pair(false, auth.currentUser?.uid))
            return
        }

        try {
            val credentialResponse = buildCredentialRequest()
            val authResult = handleSignIn(credentialResponse)
            Log.i(TAG, "Signed in successfully!")
            callback(
                Pair(
                    authResult?.additionalUserInfo?.isNewUser ?: false,
                    authResult?.user?.uid
                )
            )
        } catch (e: CancellationException) {
            throw e
        } catch (e: NoCredentialException) {
            val intent = Intent(Settings.ACTION_SYNC_SETTINGS).apply {
                putExtra(Settings.EXTRA_ACCOUNT_TYPES, arrayOf("com.google"))
            }
            context.startActivity(intent)
            Log.w(TAG, "Sign-in failed: ${e.message}. Going to Manage Accounts Settings", e)
        } catch (e: Exception) {
            Log.e(TAG, "Sign-in failed: ${e.message}", e)
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