package com.example.ui.screens.library

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
import androidx.compose.foundation.shape.RoundedCornerShape
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
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CategoryEntity

data class LibraryFilter(
    val author: String = "",
    val collection: String = "",
    val categoryNames: Set<String> = emptySet(),
    val favoriteOnly: Boolean = false,
    val notesOnly: Boolean = false
) {
    val isDefault: Boolean
        get() = author.isEmpty() &&
                collection.isEmpty() &&
                categoryNames.isEmpty() &&
                !favoriteOnly &&
                !notesOnly

    val activeFilterCount: Int
        get() {
            var count = 0
            if (author.isNotEmpty()) count++
            if (collection.isNotEmpty()) count++
            if (categoryNames.isNotEmpty()) count += categoryNames.size
            if (favoriteOnly) count++
            if (notesOnly) count++
            return count
        }
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun LibraryFilterSheet(
    sheetState: SheetState,
    currentFilter: LibraryFilter,
    allCategories: List<CategoryEntity>,
    authors: List<String>,
    collections: List<String>,
    onApply: (LibraryFilter) -> Unit,
    onReset: () -> Unit,
    onDismiss: () -> Unit
) {
    var selectedAuthor by remember(currentFilter) { mutableStateOf(currentFilter.author) }
    var selectedCollection by remember(currentFilter) { mutableStateOf(currentFilter.collection) }
    var selectedCategories by remember(currentFilter) { mutableStateOf(currentFilter.categoryNames) }
    var favoriteOnly by remember(currentFilter) { mutableStateOf(currentFilter.favoriteOnly) }
    var notesOnly by remember(currentFilter) { mutableStateOf(currentFilter.notesOnly) }

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
                    text = "내 시 다중 필터",
                    style = MaterialTheme.typography.titleLarge,
                    fontWeight = FontWeight.Bold
                )

                IconButton(onClick = onDismiss) {
                    Icon(imageVector = Icons.Default.Close, contentDescription = "닫기")
                }
            }

            Text(
                text = "원하는 조건을 조합하여 보관된 시를 찾아보세요.",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(18.dp))

            // Status Filter
            Text(
                text = "상태",
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                FilterChip(
                    selected = favoriteOnly,
                    onClick = { favoriteOnly = !favoriteOnly },
                    label = { Text("❤️ 좋아요") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Favorite,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 2.dp)
                        )
                    }
                )

                FilterChip(
                    selected = notesOnly,
                    onClick = { notesOnly = !notesOnly },
                    label = { Text("📝 메모 있음") },
                    leadingIcon = {
                        Icon(
                            imageVector = Icons.Default.Notes,
                            contentDescription = null,
                            modifier = Modifier.padding(end = 2.dp)
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(18.dp))

            // Categories
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

            Spacer(modifier = Modifier.height(18.dp))

            // Author
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
                Spacer(modifier = Modifier.height(18.dp))
            }

            // Collection
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
                    collections.forEach { col ->
                        val isSelected = selectedCollection == col
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCollection = if (isSelected) "" else col
                            },
                            label = { Text(col) }
                        )
                    }
                }
                Spacer(modifier = Modifier.height(18.dp))
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Buttons
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
                    modifier = Modifier.weight(1f)
                ) {
                    Icon(imageVector = Icons.Default.RestartAlt, contentDescription = null)
                    Spacer(modifier = Modifier.width(6.dp))
                    Text("초기화")
                }

                Button(
                    onClick = {
                        onApply(
                            LibraryFilter(
                                author = selectedAuthor,
                                collection = selectedCollection,
                                categoryNames = selectedCategories,
                                favoriteOnly = favoriteOnly,
                                notesOnly = notesOnly
                            )
                        )
                    },
                    modifier = Modifier.weight(1.3f)
                ) {
                    Text("필터 적용하기")
                }
            }
        }
    }
}
