package com.example.ui.screens

import android.content.res.Configuration
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColor
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.List
import androidx.compose.material.icons.filled.*
import androidx.compose.material.icons.outlined.StarBorder
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.SubcomposeAsyncImage
import com.example.model.Channel
import com.example.ui.components.VideoPlayer
import com.example.viewmodel.IPtvViewModel

@Composable
fun MainScreen(
    viewModel: IPtvViewModel = viewModel()
) {
    val m3uUrl by viewModel.m3uUrl.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val selectedCategory by viewModel.selectedCategory.collectAsStateWithLifecycle()
    val searchQuery by viewModel.searchQuery.collectAsStateWithLifecycle()
    val isLoading by viewModel.isLoading.collectAsStateWithLifecycle()
    val errorMessage by viewModel.errorMessage.collectAsStateWithLifecycle()
    val currentChannel by viewModel.currentPlayingChannel.collectAsStateWithLifecycle()
    val filteredChannels by viewModel.filteredChannels.collectAsStateWithLifecycle()
    val favorites by viewModel.favorites.collectAsStateWithLifecycle()
    val customChannels by viewModel.customChannels.collectAsStateWithLifecycle()

    val configuration = LocalConfiguration.current
    val isLandscape = configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var activeTab by remember { mutableStateOf(0) } // 0: Kanallar, 1: Favoriler, 2: Ayarlar

    Scaffold(
        topBar = {
            HeaderBar(
                title = "Dünya IPTV",
                isPlaying = currentChannel != null
            )
        }
    ) { paddingValues ->
        val contentModifier = Modifier
            .fillMaxSize()
            .padding(paddingValues)
            .background(MaterialTheme.colorScheme.background)

        if (isLandscape) {
            // Horizontal layout for Landscape or Tablets
            Row(modifier = contentModifier) {
                // Sidebar panel (left 40%)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.4f)
                        .background(MaterialTheme.colorScheme.surface)
                ) {
                    SidebarContent(
                        activeTab = activeTab,
                        onTabSelected = { activeTab = it },
                        m3uUrl = m3uUrl,
                        categories = categories,
                        selectedCategory = selectedCategory,
                        searchQuery = searchQuery,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        filteredChannels = filteredChannels,
                        favorites = favorites,
                        customChannels = customChannels,
                        currentChannel = currentChannel,
                        onCategorySelected = { viewModel.selectCategory(it) },
                        onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                        onChannelSelected = { viewModel.playChannel(it) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onUrlUpdated = { viewModel.loadPlaylist(it) },
                        onAddCustomChannel = { name, cat, url, logo -> viewModel.addCustomChannel(name, cat, url, logo) },
                        onDeleteCustomChannel = { viewModel.deleteCustomChannel(it) }
                    )
                }

                // Video player panel (right 60%)
                Box(
                    modifier = Modifier
                        .fillMaxHeight()
                        .weight(0.6f)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentChannel != null) {
                        VideoPlayerSection(channel = currentChannel!!)
                    } else {
                        EmptyPlayerState()
                    }
                }
            }
        } else {
            // Vertical layout for Portrait Mobile Devices
            Column(modifier = contentModifier) {
                // Player at top (fixed aspect ratio / height)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .aspectRatio(16f / 9f)
                        .background(Color.Black),
                    contentAlignment = Alignment.Center
                ) {
                    if (currentChannel != null) {
                        VideoPlayerSection(channel = currentChannel!!)
                    } else {
                        EmptyPlayerState()
                    }
                }

                // Interactive Content at bottom
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f)
                ) {
                    SidebarContent(
                        activeTab = activeTab,
                        onTabSelected = { activeTab = it },
                        m3uUrl = m3uUrl,
                        categories = categories,
                        selectedCategory = selectedCategory,
                        searchQuery = searchQuery,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        filteredChannels = filteredChannels,
                        favorites = favorites,
                        customChannels = customChannels,
                        currentChannel = currentChannel,
                        onCategorySelected = { viewModel.selectCategory(it) },
                        onSearchQueryChanged = { viewModel.setSearchQuery(it) },
                        onChannelSelected = { viewModel.playChannel(it) },
                        onToggleFavorite = { viewModel.toggleFavorite(it) },
                        onUrlUpdated = { viewModel.loadPlaylist(it) },
                        onAddCustomChannel = { name, cat, url, logo -> viewModel.addCustomChannel(name, cat, url, logo) },
                        onDeleteCustomChannel = { viewModel.deleteCustomChannel(it) }
                    )
                }
            }
        }
    }
}

@Composable
fun HeaderBar(title: String, isPlaying: Boolean) {
    Surface(
        tonalElevation = 8.dp,
        color = MaterialTheme.colorScheme.surface
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .statusBarsPadding()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = Icons.Default.Tv,
                contentDescription = "App Logo",
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(28.dp)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.weight(1f))

            // Real-time blinking streaming light indicator
            if (isPlaying) {
                val infiniteTransition = rememberInfiniteTransition(label = "pulse")
                val pulseColor by infiniteTransition.animateColor(
                    initialValue = Color(0xFF00E5FF),
                    targetValue = Color(0xFF00E5FF).copy(alpha = 0.2f),
                    animationSpec = infiniteRepeatable(
                        animation = tween(1000),
                        repeatMode = RepeatMode.Reverse
                    ),
                    label = "pulse_color"
                )

                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    Box(
                        modifier = Modifier
                            .size(8.dp)
                            .clip(CircleShape)
                            .background(pulseColor)
                    )
                    Text(
                        text = "YAYINDA",
                        style = MaterialTheme.typography.labelSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
    }
}

@Composable
fun SidebarContent(
    activeTab: Int,
    onTabSelected: (Int) -> Unit,
    m3uUrl: String,
    categories: Set<String>,
    selectedCategory: String,
    searchQuery: String,
    isLoading: Boolean,
    errorMessage: String?,
    filteredChannels: List<Channel>,
    favorites: List<com.example.data.FavoriteChannel>,
    customChannels: List<com.example.data.CustomChannel>,
    currentChannel: Channel?,
    onCategorySelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onChannelSelected: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
    onUrlUpdated: (String) -> Unit,
    onAddCustomChannel: (String, String, String, String) -> Unit,
    onDeleteCustomChannel: (String) -> Unit
) {
    Column(modifier = Modifier.fillMaxSize()) {
        TabRow(
            selectedTabIndex = activeTab,
            containerColor = MaterialTheme.colorScheme.surface,
            contentColor = MaterialTheme.colorScheme.primary
        ) {
            Tab(
                selected = activeTab == 0,
                onClick = { onTabSelected(0) },
                icon = { Icon(Icons.AutoMirrored.Filled.List, contentDescription = "Kanallar") },
                text = { Text("Kanallar") }
            )
            Tab(
                selected = activeTab == 1,
                onClick = { onTabSelected(1) },
                icon = { Icon(Icons.Default.Star, contentDescription = "Favoriler") },
                text = { Text("Favoriler") }
            )
            Tab(
                selected = activeTab == 2,
                onClick = { onTabSelected(2) },
                icon = { Icon(Icons.Default.Settings, contentDescription = "Ayarlar & Özel") },
                text = { Text("Ayarlar") }
            )
        }

        Box(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
                .padding(12.dp)
        ) {
            when (activeTab) {
                0 -> {
                    ChannelsTab(
                        categories = categories,
                        selectedCategory = selectedCategory,
                        searchQuery = searchQuery,
                        isLoading = isLoading,
                        errorMessage = errorMessage,
                        channels = filteredChannels,
                        favorites = favorites,
                        customChannelUrls = customChannels.map { it.url }.toSet(),
                        currentChannel = currentChannel,
                        onCategorySelected = onCategorySelected,
                        onSearchQueryChanged = onSearchQueryChanged,
                        onChannelSelected = onChannelSelected,
                        onToggleFavorite = onToggleFavorite,
                        onDeleteCustomChannel = onDeleteCustomChannel
                    )
                }
                1 -> {
                    FavoritesTab(
                        favorites = favorites,
                        currentChannel = currentChannel,
                        onChannelSelected = onChannelSelected,
                        onToggleFavorite = onToggleFavorite
                    )
                }
                2 -> {
                    SettingsTab(
                        currentUrl = m3uUrl,
                        onUrlUpdated = onUrlUpdated,
                        isLoading = isLoading,
                        customChannels = customChannels,
                        onAddCustomChannel = onAddCustomChannel,
                        onDeleteCustomChannel = onDeleteCustomChannel
                    )
                }
            }
        }
    }
}

@Composable
fun ChannelsTab(
    categories: Set<String>,
    selectedCategory: String,
    searchQuery: String,
    isLoading: Boolean,
    errorMessage: String?,
    channels: List<Channel>,
    favorites: List<com.example.data.FavoriteChannel>,
    customChannelUrls: Set<String>,
    currentChannel: Channel?,
    onCategorySelected: (String) -> Unit,
    onSearchQueryChanged: (String) -> Unit,
    onChannelSelected: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit,
    onDeleteCustomChannel: (String) -> Unit
) {
    val focusManager = LocalFocusManager.current

    Column(modifier = Modifier.fillMaxSize()) {
        // Search Box
        OutlinedTextField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            modifier = Modifier
                .fillMaxWidth()
                .testTag("search_box")
                .padding(bottom = 8.dp),
            placeholder = { Text("Kanal ara...") },
            leadingIcon = { Icon(Icons.Default.Search, contentDescription = null) },
            trailingIcon = {
                if (searchQuery.isNotEmpty()) {
                    IconButton(onClick = { onSearchQueryChanged("") }) {
                        Icon(Icons.Default.Clear, contentDescription = "Aramayı Temizle")
                    }
                }
            },
            singleLine = true,
            keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
            keyboardActions = KeyboardActions(onSearch = { focusManager.clearFocus() }),
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        // Categories List
        LazyRow(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            items(categories.toList()) { category ->
                val isSelected = category == selectedCategory
                FilterChip(
                    selected = isSelected,
                    onClick = { onCategorySelected(category) },
                    label = { Text(category) },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primary,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimary
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // State displays
        if (isLoading) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        } else if (errorMessage != null) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(16.dp),
                contentAlignment = Alignment.Center
            ) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Icon(
                        Icons.Default.Error,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.error,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        text = errorMessage,
                        color = MaterialTheme.colorScheme.error,
                        textAlign = TextAlign.Center
                    )
                }
            }
        } else if (channels.isEmpty()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "Aranan kriterlere uygun kanal bulunamadı.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            }
        } else {
            // Channels list
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(channels, key = { it.url }) { channel ->
                    val isFav = favorites.any { it.url == channel.url }
                    val isPlaying = currentChannel?.url == channel.url
                    val isCustom = channel.url in customChannelUrls
                    ChannelItem(
                        channel = channel,
                        isFavorite = isFav,
                        isPlaying = isPlaying,
                        isCustom = isCustom,
                        onChannelClick = { onChannelSelected(channel) },
                        onFavClick = { onToggleFavorite(channel) },
                        onDeleteClick = { onDeleteCustomChannel(channel.url) }
                    )
                }
            }
        }
    }
}

@Composable
fun FavoritesTab(
    favorites: List<com.example.data.FavoriteChannel>,
    currentChannel: Channel?,
    onChannelSelected: (Channel) -> Unit,
    onToggleFavorite: (Channel) -> Unit
) {
    if (favorites.isEmpty()) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center
            ) {
                Icon(
                    imageVector = Icons.Default.Star,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.tertiary,
                    modifier = Modifier.size(64.dp)
                )
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = "Favorileriniz Boş",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Kanal listesinden yıldız simgesine dokunarak favori kanallarınızı buraya ekleyebilirsiniz.",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                    textAlign = TextAlign.Center
                )
            }
        }
    } else {
        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(favorites, key = { it.url }) { fav ->
                val channel = Channel(
                    name = fav.name,
                    category = fav.category,
                    url = fav.url,
                    logoUrl = fav.logoUrl
                )
                val isPlaying = currentChannel?.url == fav.url
                ChannelItem(
                    channel = channel,
                    isFavorite = true,
                    isPlaying = isPlaying,
                    isCustom = false,
                    onChannelClick = { onChannelSelected(channel) },
                    onFavClick = { onToggleFavorite(channel) }
                )
            }
        }
    }
}

@Composable
fun SettingsTab(
    currentUrl: String,
    onUrlUpdated: (String) -> Unit,
    isLoading: Boolean,
    customChannels: List<com.example.data.CustomChannel>,
    onAddCustomChannel: (String, String, String, String) -> Unit,
    onDeleteCustomChannel: (String) -> Unit
) {
    var urlInput by remember { mutableStateOf(currentUrl) }
    val focusManager = LocalFocusManager.current

    // Add Custom Channel form states
    var customName by remember { mutableStateOf("") }
    var customUrl by remember { mutableStateOf("") }
    var customCategory by remember { mutableStateOf("") }
    var customLogoUrl by remember { mutableStateOf("") }

    val presets = listOf(
        "Varsayılan" to "https://iptv-org.github.io/iptv/index.m3u",
        "TR - Genel" to "https://iptv-org.github.io/iptv/countries/tr.m3u",
        "TR - Haber" to "https://iptv-org.github.io/iptv/categories/news.m3u",
        "Uluslararası Filmler" to "https://iptv-org.github.io/iptv/categories/movies.m3u"
    )

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(8.dp)
    ) {
        // --- IPTV Source Playlist URL Section ---
        Text(
            text = "IPTV Liste Kaynağı",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        OutlinedTextField(
            value = urlInput,
            onValueChange = { urlInput = it },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("m3u_url_input"),
            label = { Text("M3U Playlist URL") },
            placeholder = { Text("https://...") },
            singleLine = false,
            maxLines = 3,
            colors = OutlinedTextFieldDefaults.colors(
                focusedBorderColor = MaterialTheme.colorScheme.primary,
                unfocusedBorderColor = MaterialTheme.colorScheme.outline
            )
        )

        Spacer(modifier = Modifier.height(12.dp))

        Button(
            onClick = {
                focusManager.clearFocus()
                if (urlInput.isNotBlank()) {
                    onUrlUpdated(urlInput.trim())
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .testTag("update_list_button")
                .height(48.dp),
            enabled = !isLoading,
            shape = RoundedCornerShape(8.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary,
                    strokeWidth = 2.dp
                )
            } else {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    Icon(Icons.Default.Refresh, contentDescription = null)
                    Text("Listeyi Güncelle", fontWeight = FontWeight.SemiBold)
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Custom Channel Adding Section ---
        Text(
            text = "Özel Kanal Ekle",
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 12.dp)
        )

        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
            )
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                OutlinedTextField(
                    value = customName,
                    onValueChange = { customName = it },
                    label = { Text("Kanal Adı *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("custom_channel_name")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customUrl,
                    onValueChange = { customUrl = it },
                    label = { Text("Yayın URL'si (m3u8, mp4 vb.) *") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("custom_channel_url")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customCategory,
                    onValueChange = { customCategory = it },
                    label = { Text("Kategori (Örn: Haber, Spor)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("custom_channel_category")
                )
                Spacer(modifier = Modifier.height(8.dp))
                OutlinedTextField(
                    value = customLogoUrl,
                    onValueChange = { customLogoUrl = it },
                    label = { Text("Kanal Logo URL'si (İsteğe bağlı)") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth().testTag("custom_channel_logo")
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = {
                        if (customName.isNotBlank() && customUrl.isNotBlank()) {
                            onAddCustomChannel(customName, customCategory, customUrl, customLogoUrl)
                            // Clear inputs
                            customName = ""
                            customUrl = ""
                            customCategory = ""
                            customLogoUrl = ""
                            focusManager.clearFocus()
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(48.dp).testTag("save_custom_channel_btn"),
                    enabled = customName.isNotBlank() && customUrl.isNotBlank()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Özel Kanal Kaydet")
                }
            }
        }

        Spacer(modifier = Modifier.height(24.dp))

        // --- Custom Channels Display List Section ---
        if (customChannels.isNotEmpty()) {
            Text(
                text = "Kayıtlı Özel Kanallar",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.padding(bottom = 12.dp)
            )

            customChannels.forEach { cc ->
                Card(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    shape = RoundedCornerShape(8.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.3f)
                    )
                ) {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(12.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Icon(
                            Icons.Default.Tv,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(12.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(
                                text = cc.name,
                                style = MaterialTheme.typography.bodyMedium,
                                fontWeight = FontWeight.Bold
                            )
                            Text(
                                text = cc.category,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        }
                        IconButton(onClick = { onDeleteCustomChannel(cc.url) }) {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = "Sil",
                                tint = MaterialTheme.colorScheme.error
                            )
                        }
                    }
                }
            }
            Spacer(modifier = Modifier.height(24.dp))
        }

        // --- Quick Preset Lists Section ---
        Text(
            text = "Hızlı Listeler",
            style = MaterialTheme.typography.titleSmall,
            fontWeight = FontWeight.Bold,
            modifier = Modifier.padding(bottom = 8.dp)
        )

        presets.forEach { (name, url) ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 4.dp)
                    .clickable {
                        urlInput = url
                        onUrlUpdated(url)
                    },
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                )
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Default.PlayArrow,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(18.dp)
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Column {
                        Text(
                            text = name,
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            text = url,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                            maxLines = 1,
                            overflow = TextOverflow.Ellipsis
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun ChannelItem(
    channel: Channel,
    isFavorite: Boolean,
    isPlaying: Boolean,
    isCustom: Boolean = false,
    onChannelClick: () -> Unit,
    onFavClick: () -> Unit,
    onDeleteClick: () -> Unit = {}
) {
    val containerColor = if (isPlaying) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    } else {
        MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f)
    }

    val outlineColor = if (isPlaying) {
        MaterialTheme.colorScheme.primary
    } else {
        Color.Transparent
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .testTag("channel_item_${channel.name}")
            .clickable { onChannelClick() },
        shape = RoundedCornerShape(12.dp),
        colors = CardDefaults.cardColors(
            containerColor = containerColor
        ),
        border = if (isPlaying) CardDefaults.outlinedCardBorder().copy(
            brush = androidx.compose.ui.graphics.SolidColor(outlineColor)
        ) else null
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Channel Logo / Icon
            if (channel.logoUrl.isNotEmpty()) {
                SubcomposeAsyncImage(
                    model = channel.logoUrl,
                    contentDescription = "${channel.name} Logo",
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color.White.copy(alpha = 0.1f)),
                    contentScale = ContentScale.Fit,
                    error = {
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                Icons.Default.Tv,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(24.dp)
                            )
                        }
                    }
                )
            } else {
                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(RoundedCornerShape(8.dp))
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        Icons.Default.Tv,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(24.dp)
                    )
                }
            }

            Spacer(modifier = Modifier.width(12.dp))

            // Channel Name & Category Info
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = channel.name,
                    style = MaterialTheme.typography.bodyMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (isPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = channel.category,
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Delete Button for Custom Channels
                if (isCustom) {
                    IconButton(
                        onClick = onDeleteClick,
                        modifier = Modifier.testTag("delete_button_${channel.name}")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Delete,
                            contentDescription = "Kanalı Sil",
                            tint = MaterialTheme.colorScheme.error.copy(alpha = 0.8f)
                        )
                    }
                }

                // Favorite Star Button
                IconButton(
                    onClick = onFavClick,
                    modifier = Modifier.testTag("fav_button_${channel.name}")
                ) {
                    Icon(
                        imageVector = if (isFavorite) Icons.Default.Star else Icons.Outlined.StarBorder,
                        contentDescription = if (isFavorite) "Favorilerden Çıkar" else "Favorilere Ekle",
                        tint = if (isFavorite) MaterialTheme.colorScheme.tertiary else MaterialTheme.colorScheme.onSurface.copy(
                            alpha = 0.4f
                        )
                    )
                }
            }
        }
    }
}

@Composable
fun VideoPlayerSection(channel: Channel) {
    Column(modifier = Modifier.fillMaxSize()) {
        VideoPlayer(
            videoUrl = channel.url,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f)
        )
        // Sub-title bar beneath the player in landscape or full-screen
        Surface(
            tonalElevation = 2.dp,
            color = MaterialTheme.colorScheme.surface.copy(alpha = 0.9f)
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = channel.name,
                        style = MaterialTheme.typography.bodyMedium,
                        fontWeight = FontWeight.Bold,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                    Text(
                        text = "Yayın formatı algılanıyor • ${channel.category}",
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f),
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
        }
    }
}

@Composable
fun EmptyPlayerState() {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color.Black)
            .padding(16.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Icon(
                imageVector = Icons.Default.Tv,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f),
                modifier = Modifier.size(64.dp)
            )
            Spacer(modifier = Modifier.height(16.dp))
            Text(
                text = "Yayını Başlatmak İçin Bir Kanal Seçin",
                color = Color.Gray,
                textAlign = TextAlign.Center,
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
