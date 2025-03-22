package com.col.eventradar.ui.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.col.eventradar.data.local.AreasOfInterestRepository
import com.col.eventradar.models.toFeature
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.launch
import org.maplibre.geojson.FeatureCollection

class AreasViewModel(
    private val repository: AreasOfInterestRepository,
) : ViewModel() {
    private val _featuresLiveData = MutableLiveData<FeatureCollection>()
    val featuresLiveData: LiveData<FeatureCollection> = _featuresLiveData

    init {
        viewModelScope.launch {
            repository.storedFeaturesFlow.distinctUntilChanged().collectLatest { featureEntities ->
                if (featureEntities.isEmpty() &&
                    _featuresLiveData.value
                        ?.features()
                        ?.isNotEmpty() == true
                ) {
                    return@collectLatest
                }

                val features = featureEntities.map { it.toFeature() }
                _featuresLiveData.postValue(FeatureCollection.fromFeatures(features))
            }
        }
    }

    fun refresh() {
        viewModelScope.launch {
            val featureEntities = repository.getStoredFeatures()
            val features = featureEntities.map { it.toFeature() }
            _featuresLiveData.postValue(FeatureCollection.fromFeatures(features))
        }
    }
}
