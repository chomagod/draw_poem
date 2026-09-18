package com.example.ui.screens.draw

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.Notes
import androidx.compose.material.icons.filled.RestartAlt
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.SheetState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.local.SavedDrawFilter
import com.example.data.model.CategoryEntity
import com.example.data.model.PoemWithCategoriesAndNotes

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun DrawFilterSheet(
    sheetState: SheetState,
    currentFilter: SavedDrawFilter,
    allPoems: List<PoemWithCategoriesAndNotes>,
    allCategories: List<CategoryEntity>,
    authors: List<String>,
    collections: List<String>,
    onApply: (SavedDrawFilter) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedAuthor by remember(currentFilter) { mutableStateOf(currentFilter.author) }
    var selectedCollection by remember(currentFilter) { mutableStateOf(currentFilter.collection) }
    var selectedCategories by remember(currentFilter) { mutableStateOf(currentFilter.categoryNames) }
    var favoriteOnly by remember(currentFilter) { mutableStateOf(currentFilter.favoriteOnly) }
    var notesOnly by remember(currentFilter) { mutableStateOf(currentFilter.notesOnly) }

    // Calculate matching count preview
    val matchingCount by remember(
        allPoems,
        selectedAuthor,
        selectedCollection,
        selectedCategories,
        favoriteOnly,
        notesOnly
    ) {
        derivedStateOf {
            allPoems.count { item ->
                val poem = item.poem
                if (selectedAuthor.isNotEmpty() && !poem.author.equals(selectedAuthor, ignoreCase = true)) {
                    return@count false
                }
                if (selectedCollection.isNotEmpty() && !poem.collection.equals(selectedCollection, ignoreCase = true)) {
                    return@count false
                }
                if (selectedCategories.isNotEmpty()) {
                    val poemCats = item.categories.map { it.name }
                    if (!selectedCategories.any { it in poemCats }) {
                        return@count false
                    }
                }
                if (favoriteOnly && !poem.isFavorite) {
                    return@count false
                }
                if (notesOnly && item.notes.isEmpty()) {
                    return@count false
                }
                true
            }
        }
    }

    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 32.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "랜덤 뽑기 필터 설정",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )
                IconButton(
                    onClick = onDismiss,
                    modifier = Modifier.testTag("close_filter_sheet_button")
                ) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "닫기")
                }
            }

            Text(
                text = "원하는 조건을 선택하면 해당하는 시 중에서만 랜덤으로 뽑습니다.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Quick Status Options: Favorite / Notes
            Text(
                text = "상태 필터",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                FilterChip(
                    selected = favoriteOnly,
                    onClick = { favoriteOnly = !favoriteOnly },
                    label = { Text("❤️ 좋아요한 시만") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            tint = if (favoriteOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.testTag("filter_favorite_chip")
                )

                FilterChip(
                    selected = notesOnly,
                    onClick = { notesOnly = !notesOnly },
                    label = { Text("📝 메모가 있는 시만") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = null,
                            tint = if (notesOnly) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.testTag("filter_notes_chip")
                )
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Category Filter
            Text(
                text = "카테고리 (다중 선택 가능)",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            if (allCategories.isEmpty()) {
                Text(
                    text = "등록된 카테고리가 없습니다.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allCategories.forEach { cat ->
                        val isSelected = selectedCategories.contains(cat.name)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategories = if (isSelected) {
                                    selectedCategories - cat.name
                                } else {
                                    selectedCategories + cat.name
                                }
                            },
                            label = { Text("#${cat.name}") },
                            colors = FilterChipDefaults.filterChipColors(
                                selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // Author Filter
            if (authors.isNotEmpty()) {
                Text(
                    text = "작가",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedAuthor.isEmpty(),
                        onClick = { selectedAuthor = "" },
                        label = { Text("전체 작가") }
                    )
                    authors.forEach { author ->
                        val isSelected = selectedAuthor == author
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedAuthor = if (isSelected) "" else author
                            },
                            label = { Text(author) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Collection Filter
            if (collections.isNotEmpty()) {
                Text(
                    text = "시집",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Spacer(modifier = Modifier.height(8.dp))
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    FilterChip(
                        selected = selectedCollection.isEmpty(),
                        onClick = { selectedCollection = "" },
                        label = { Text("전체 시집") }
                    )
                    collections.forEach { collection ->
                        val isSelected = selectedCollection == collection
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCollection = if (isSelected) "" else collection
                            },
                            label = { Text(collection) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(20.dp))
            }

            // Candidate Preview
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(vertical = 8.dp),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "조건에 해당하는 시",
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = "${matchingCount}편",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold,
                    color = if (matchingCount > 0) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Action Buttons: Reset & Apply
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                OutlinedButton(
                    onClick = {
                        selectedAuthor = ""
                        selectedCollection = ""
                        selectedCategories = emptySet()
                        favoriteOnly = false
                        notesOnly = false
                        onReset()
                    },
                    modifier = Modifier
                        .weight(1f)
                        .testTag("reset_draw_filter_button")
                ) {
                    Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("전체 초기화")
                }

                Button(
                    onClick = {
                        onApply(
                            SavedDrawFilter(
                                author = selectedAuthor,
                                collection = selectedCollection,
                                categoryNames = selectedCategories,
                                favoriteOnly = favoriteOnly,
                                notesOnly = notesOnly
                            )
                        )
                    },
                    modifier = Modifier
                        .weight(1.4f)
                        .testTag("apply_draw_filter_button")
                ) {
                    Text("조건 적용하기 (${matchingCount}편)")
                }
            }
        }
    }
}
