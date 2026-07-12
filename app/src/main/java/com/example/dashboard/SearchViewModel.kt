package com.example.dashboard

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.models.Asset
import com.example.repository.AssetRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.*

@HiltViewModel
class SearchViewModel @Inject constructor(
    private val assetRepository: AssetRepository
) : ViewModel() {

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    val searchResults: StateFlow<List<Asset>> = combine(
        assetRepository.assets,
        _searchQuery
    ) { assets, query ->
        if (query.isBlank()) {
            emptyList()
        } else {
            assets.filter { asset ->
                asset.id.contains(query, ignoreCase = true) ||
                asset.serialNumber.contains(query, ignoreCase = true) ||
                asset.location.contains(query, ignoreCase = true) ||
                asset.name.contains(query, ignoreCase = true) ||
                asset.type.contains(query, ignoreCase = true) ||
                asset.model.contains(query, ignoreCase = true) ||
                asset.manufacturer.contains(query, ignoreCase = true)
            }
        }
    }
    .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    val suggestions: StateFlow<List<String>> = combine(
        assetRepository.assets,
        _searchQuery
    ) { assets, query ->
        if (query.isBlank()) {
            emptyList()
        } else {
            val matchedList = mutableListOf<String>()
            for (asset in assets) {
                if (asset.id.contains(query, ignoreCase = true)) matchedList.add(asset.id)
                if (asset.serialNumber.contains(query, ignoreCase = true)) matchedList.add(asset.serialNumber)
                if (asset.location.contains(query, ignoreCase = true)) matchedList.add(asset.location)
                if (asset.name.contains(query, ignoreCase = true)) matchedList.add(asset.name)
            }
            matchedList.distinct().take(5)
        }
    }
    .stateIn(viewModelScope, SharingStarted.Lazily, emptyList())

    fun updateSearchQuery(query: String) {
        _searchQuery.value = query
    }
}
