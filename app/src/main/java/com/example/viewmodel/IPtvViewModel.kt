package com.example.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.AppDatabase
import com.example.data.CustomChannel
import com.example.data.FavoriteChannel
import com.example.data.FavoriteRepository
import com.example.model.Channel
import com.example.network.M3uParser
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class IPtvViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getDatabase(application)
    private val repository = FavoriteRepository(database.favoriteDao())

    private val _m3uUrl = MutableStateFlow("https://iptv-org.github.io/iptv/index.m3u")
    val m3uUrl: StateFlow<String> = _m3uUrl.asStateFlow()

    private val _allChannels = MutableStateFlow<List<Channel>>(emptyList())
    val allChannels: StateFlow<List<Channel>> = _allChannels.asStateFlow()

    val customChannels: StateFlow<List<CustomChannel>> = database.customChannelDao().getAllCustomChannels()
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val categories: StateFlow<Set<String>> = combine(
        _allChannels,
        customChannels
    ) { parsed, custom ->
        val cats = parsed.map { it.category }.toSet() + custom.map { it.category }.toSet()
        setOf("Tümü") + cats
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = setOf("Tümü")
    )

    private val _selectedCategory = MutableStateFlow("Tümü")
    val selectedCategory: StateFlow<String> = _selectedCategory.asStateFlow()

    private val _searchQuery = MutableStateFlow("")
    val searchQuery: StateFlow<String> = _searchQuery.asStateFlow()

    private val _isLoading = MutableStateFlow(false)
    val isLoading: StateFlow<Boolean> = _isLoading.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage.asStateFlow()

    private val _currentPlayingChannel = MutableStateFlow<Channel?>(null)
    val currentPlayingChannel: StateFlow<Channel?> = _currentPlayingChannel.asStateFlow()

    val favorites: StateFlow<List<FavoriteChannel>> = repository.allFavorites
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val filteredChannels: StateFlow<List<Channel>> = combine(
        _allChannels,
        customChannels,
        _selectedCategory,
        _searchQuery
    ) { parsed, custom, category, query ->
        val customAsChannels = custom.map {
            Channel(name = it.name, category = it.category, url = it.url, logoUrl = it.logoUrl)
        }
        val combined = customAsChannels + parsed
        combined.filter { channel ->
            val matchesCategory = category == "Tümü" || channel.category == category
            val matchesSearch = channel.name.contains(query, ignoreCase = true)
            matchesCategory && matchesSearch
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(5000),
        initialValue = emptyList()
    )

    init {
        loadPlaylist(_m3uUrl.value)
    }

    fun loadPlaylist(url: String) {
        viewModelScope.launch {
            _isLoading.value = true
            _errorMessage.value = null
            _m3uUrl.value = url
            try {
                val parsed = M3uParser.parseFromUrl(url)
                if (parsed.isEmpty()) {
                    _errorMessage.value = "Kanal listesi yüklenemedi veya boş. Lütfen URL'yi kontrol edin."
                } else {
                    _allChannels.value = parsed
                    _selectedCategory.value = "Tümü"
                    
                    // Auto play first channel if none is playing
                    if (_currentPlayingChannel.value == null && parsed.isNotEmpty()) {
                        _currentPlayingChannel.value = parsed.first()
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Hata oluştu: ${e.localizedMessage}"
            } finally {
                _isLoading.value = false
            }
        }
    }

    fun selectCategory(category: String) {
        _selectedCategory.value = category
    }

    fun setSearchQuery(query: String) {
        _searchQuery.value = query
    }

    fun playChannel(channel: Channel) {
        _currentPlayingChannel.value = channel
    }

    fun toggleFavorite(channel: Channel) {
        viewModelScope.launch {
            val isFav = favorites.value.any { it.url == channel.url }
            if (isFav) {
                repository.removeFavoriteByUrl(channel.url)
            } else {
                repository.addFavorite(
                    FavoriteChannel(
                        url = channel.url,
                        name = channel.name,
                        category = channel.category,
                        logoUrl = channel.logoUrl
                    )
                )
            }
        }
    }

    fun addCustomChannel(name: String, category: String, url: String, logoUrl: String = "") {
        viewModelScope.launch {
            database.customChannelDao().insertCustomChannel(
                CustomChannel(
                    url = url.trim(),
                    name = name.trim(),
                    category = if (category.isBlank()) "Özel" else category.trim(),
                    logoUrl = logoUrl.trim()
                )
            )
        }
    }

    fun deleteCustomChannel(url: String) {
        viewModelScope.launch {
            database.customChannelDao().deleteCustomChannelByUrl(url)
            // Also remove from favorite if it was favorited
            repository.removeFavoriteByUrl(url)
            if (_currentPlayingChannel.value?.url == url) {
                _currentPlayingChannel.value = null
            }
        }
    }
}
