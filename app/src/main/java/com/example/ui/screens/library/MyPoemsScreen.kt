package com.example.ui.screens.library

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.FavoriteBorder
import androidx.compose.material.icons.filled.FilterAlt
import androidx.compose.material.icons.filled.FilterList
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Badge
import androidx.compose.material3.BadgedBox
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SuggestionChip
import androidx.compose.material3.SuggestionChipDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.data.model.CategoryEntity
import com.example.data.model.PoemWithCategoriesAndNotes
import com.example.ui.theme.LoveRed
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun MyPoemsScreen(
    poems: List<PoemWithCategoriesAndNotes>,
    categories: List<CategoryEntity>,
    authors: List<String>,
    collections: List<String>,
    onPoemClick: (Long) -> Unit,
    onAddPoemClick: () -> Unit,
    onToggleFavorite: (Long) -> Unit,
    onNavigateToSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    var searchQuery by remember { mutableStateOf("") }
    var multiFilter by remember { mutableStateOf(LibraryFilter()) }
    var isFilterSheetOpen by remember { mutableStateOf(false) }
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    val coroutineScope = rememberCoroutineScope()

    // Quick filter selection (e.g. ALL, FAVORITE, HAS_NOTES, or specific category name)
    var quickFilterCategory by remember { mutableStateOf<String?>(null) }

    // Filter logic
    val filteredPoems by remember(poems, searchQuery, multiFilter, quickFilterCategory) {
        derivedStateOf {
            poems.filter { item ->
                val poem = item.poem

                // 1. Text search
                if (searchQuery.isNotBlank()) {
                    val query = searchQuery.trim()
                    val matchTitle = poem.title.contains(query, ignoreCase = true)
                    val matchAuthor = poem.author.contains(query, ignoreCase = true)
                    val matchCollection = poem.collection.contains(query, ignoreCase = true)
                    val matchContent = poem.content.contains(query, ignoreCase = true)
                    if (!matchTitle && !matchAuthor && !matchCollection && !matchContent) {
                        return@filter false
                    }
                }

                // 2. Quick category filter
                if (quickFilterCategory != null) {
                    val poemCatNames = item.categories.map { it.name }
                    if (!poemCatNames.contains(quickFilterCategory)) {
                        return@filter false
                    }
                }

                // 3. Multi-filter conditions
                if (multiFilter.author.isNotEmpty() && !poem.author.equals(multiFilter.author, ignoreCase = true)) {
                    return@filter false
                }
                if (multiFilter.collection.isNotEmpty() && !poem.collection.equals(multiFilter.collection, ignoreCase = true)) {
                    return@filter false
                }
                if (multiFilter.categoryNames.isNotEmpty()) {
                    val poemCats = item.categories.map { it.name }
                    if (!multiFilter.categoryNames.any { it in poemCats }) {
                        return@filter false
                    }
                }
                if (multiFilter.favoriteOnly && !poem.isFavorite) {
                    return@filter false
                }
                if (multiFilter.notesOnly && item.notes.isEmpty()) {
                    return@filter false
                }

                true
            }
        }
    }

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.primary,
                            modifier = Modifier.size(24.dp)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "내 시 보관함",
                            fontWeight = FontWeight.Bold,
                            fontFamily = FontFamily.Serif
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = onNavigateToSettings,
                        modifier = Modifier.testTag("my_poems_settings_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = "설정",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        floatingActionButton = {
            ExtendedFloatingActionButton(
                onClick = onAddPoemClick,
                containerColor = MaterialTheme.colorScheme.primary,
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.testTag("add_poem_fab")
            ) {
                Icon(imageVector = Icons.Default.Add, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text("시 등록", fontWeight = FontWeight.SemiBold)
            }
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            // Search Bar & Multi-Filter Button
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("시 제목, 작가, 시집, 내용 검색...") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Search,
                            contentDescription = "검색",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
                                Icon(
                                    imageVector = Icons.Default.Clear,
                                    contentDescription = "검색어 지우기"
                                )
                            }
                        }
                    },
                    singleLine = true,
                    shape = RoundedCornerShape(16.dp),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = MaterialTheme.colorScheme.surface,
                        unfocusedContainerColor = MaterialTheme.colorScheme.surface
                    ),
                    modifier = Modifier
                        .weight(1f)
                        .testTag("search_poem_input")
                )

                Spacer(modifier = Modifier.width(8.dp))

                BadgedBox(
                    badge = {
                        if (!multiFilter.isDefault) {
                            Badge(
                                containerColor = MaterialTheme.colorScheme.primary
                            ) {
                                Text("${multiFilter.activeFilterCount}")
                            }
                        }
                    }
                ) {
                    IconButton(
                        onClick = { isFilterSheetOpen = true },
                        modifier = Modifier
                            .clip(CircleShape)
                            .background(
                                if (!multiFilter.isDefault) {
                                    MaterialTheme.colorScheme.primaryContainer
                                } else {
                                    MaterialTheme.colorScheme.surfaceVariant
                                }
                            )
                            .testTag("open_multi_filter_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.FilterList,
                            contentDescription = "다중 필터",
                            tint = if (!multiFilter.isDefault) {
                                MaterialTheme.colorScheme.onPrimaryContainer
                            } else {
                                MaterialTheme.colorScheme.onSurfaceVariant
                            }
                        )
                    }
                }
            }

            // Quick Category / Status Chips
            LazyRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                // All
                item {
                    FilterChip(
                        selected = quickFilterCategory == null && multiFilter.isDefault,
                        onClick = {
                            quickFilterCategory = null
                            multiFilter = LibraryFilter()
                        },
                        label = { Text("전체 (${poems.size})") }
                    )
                }

                // Quick Favorite
                item {
                    FilterChip(
                        selected = multiFilter.favoriteOnly,
                        onClick = {
                            multiFilter = multiFilter.copy(favoriteOnly = !multiFilter.favoriteOnly)
                        },
                        label = { Text("❤️ 좋아요") }
                    )
                }

                // Quick Notes
                item {
                    FilterChip(
                        selected = multiFilter.notesOnly,
                        onClick = {
                            multiFilter = multiFilter.copy(notesOnly = !multiFilter.notesOnly)
                        },
                        label = { Text("📝 메모 있음") }
                    )
                }

                // Categories
                items(categories, key = { it.id }) { cat ->
                    val isSelected = quickFilterCategory == cat.name
                    FilterChip(
                        selected = isSelected,
                        onClick = {
                            quickFilterCategory = if (isSelected) null else cat.name
                        },
                        label = { Text("#${cat.name}") }
                    )
                }
            }

            // Results count
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 6.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "총 ${filteredPoems.size}편의 시",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )

                if (!multiFilter.isDefault || quickFilterCategory != null || searchQuery.isNotEmpty()) {
                    Text(
                        text = "필터 해제",
                        style = MaterialTheme.typography.labelMedium,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier
                            .clickable {
                                searchQuery = ""
                                quickFilterCategory = null
                                multiFilter = LibraryFilter()
                            }
                            .padding(4.dp)
                    )
                }
            }

            // Poem List or Empty State
            if (filteredPoems.isEmpty()) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Column(horizontalAlignment = Alignment.CenterHorizontally) {
                        Icon(
                            imageVector = Icons.Default.Book,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.outline.copy(alpha = 0.5f),
                            modifier = Modifier.size(56.dp)
                        )
                        Spacer(modifier = Modifier.height(16.dp))
                        Text(
                            text = if (poems.isEmpty()) {
                                "보관함에 등록된 시가 없습니다.\n우측 하단의 '+' 버튼을 눌러 시를 등록해 보세요."
                            } else {
                                "조건에 해당하는 시가 없습니다.\n필터나 검색어를 변경해 보세요."
                            },
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant,
                            textAlign = androidx.compose.ui.text.style.TextAlign.Center
                        )
                    }
                }
            } else {
                LazyColumn(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = 16.dp),
                    verticalArrangement = Arrangement.spacedBy(10.dp)
                ) {
                    items(filteredPoems, key = { it.poem.id }) { item ->
                        val poem = item.poem
                        Card(
                            shape = RoundedCornerShape(16.dp),
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surface
                            ),
                            elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onPoemClick(poem.id) }
                                .testTag("poem_card_${poem.id}")
                        ) {
                            Column(modifier = Modifier.padding(16.dp)) {
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.Top
                                ) {
                                    Column(modifier = Modifier.weight(1f)) {
                                        // Title
                                        Text(
                                            text = poem.title,
                                            style = MaterialTheme.typography.titleMedium,
                                            fontWeight = FontWeight.Bold,
                                            fontFamily = FontFamily.Serif,
                                            color = MaterialTheme.colorScheme.onSurface
                                        )

                                        Spacer(modifier = Modifier.height(4.dp))

                                        // Author & Collection
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(
                                                text = poem.author,
                                                style = MaterialTheme.typography.bodyMedium,
                                                color = MaterialTheme.colorScheme.primary,
                                                fontWeight = FontWeight.Medium
                                            )
                                            if (poem.collection.isNotBlank()) {
                                                Text(
                                                    text = " · ${poem.collection}",
                                                    style = MaterialTheme.typography.bodySmall,
                                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }

                                    // Favorite toggle button
                                    IconButton(
                                        onClick = { onToggleFavorite(poem.id) },
                                        modifier = Modifier.size(36.dp)
                                    ) {
                                        Icon(
                                            imageVector = if (poem.isFavorite) Icons.Default.Favorite else Icons.Default.FavoriteBorder,
                                            contentDescription = if (poem.isFavorite) "좋아요 취소" else "좋아요",
                                            tint = if (poem.isFavorite) LoveRed else MaterialTheme.colorScheme.outline,
                                            modifier = Modifier.size(22.dp)
                                        )
                                    }
                                }

                                Spacer(modifier = Modifier.height(8.dp))

                                // Excerpt of poem content
                                Text(
                                    text = poem.content,
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                                    maxLines = 2,
                                    overflow = TextOverflow.Ellipsis
                                )

                                Spacer(modifier = Modifier.height(10.dp))

                                // Footer: Category tags and Note indicator
                                Row(
                                    modifier = Modifier.fillMaxWidth(),
                                    horizontalArrangement = Arrangement.SpaceBetween,
                                    verticalAlignment = Alignment.CenterVertically
                                ) {
                                    // Category tags
                                    if (item.categories.isNotEmpty()) {
                                        FlowRow(
                                            horizontalArrangement = Arrangement.spacedBy(6.dp),
                                            modifier = Modifier.weight(1f)
                                        ) {
                                            item.categories.take(3).forEach { cat ->
                                                Text(
                                                    text = "#${cat.name}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary,
                                                    modifier = Modifier
                                                        .clip(RoundedCornerShape(6.dp))
                                                        .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f))
                                                        .padding(horizontal = 6.dp, vertical = 2.dp)
                                                )
                                            }
                                            if (item.categories.size > 3) {
                                                Text(
                                                    text = "+${item.categories.size - 3}",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.outline
                                                )
                                            }
                                        }
                                    } else {
                                        Spacer(modifier = Modifier.width(1.dp))
                                    }

                                    // Note indicator
                                    if (item.notes.isNotEmpty()) {
                                        Row(
                                            verticalAlignment = Alignment.CenterVertically,
                                            modifier = Modifier.padding(start = 8.dp)
                                        ) {
                                            Icon(
                                                imageVector = Icons.Default.Notes,
                                                contentDescription = null,
                                                tint = MaterialTheme.colorScheme.secondary,
                                                modifier = Modifier.size(16.dp)
                                            )
                                            Spacer(modifier = Modifier.width(4.dp))
                                            Text(
                                                text = "${item.notes.size}",
                                                style = MaterialTheme.typography.labelSmall,
                                                color = MaterialTheme.colorScheme.secondary,
                                                fontWeight = FontWeight.Medium
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }

                    item {
                        // Extra bottom spacing so FAB doesn't obscure the last item
                        Spacer(modifier = Modifier.height(80.dp))
                    }
                }
            }
        }
    }

    if (isFilterSheetOpen) {
        LibraryFilterSheet(
            sheetState = sheetState,
            currentFilter = multiFilter,
            allCategories = categories,
            authors = authors,
            collections = collections,
            onApply = { newFilter ->
                multiFilter = newFilter
                coroutineScope.launch {
                    sheetState.hide()
                    isFilterSheetOpen = false
                }
            },
            onReset = {
                multiFilter = LibraryFilter()
                coroutineScope.launch {
                    sheetState.hide()
                    isFilterSheetOpen = false
                }
            },
            onDismiss = {
                coroutineScope.launch {
                    sheetState.hide()
                    isFilterSheetOpen = false
                }
            }
        )
    }
}
