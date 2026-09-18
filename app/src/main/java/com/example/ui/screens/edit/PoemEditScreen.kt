package com.example.ui.screens.edit

import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.data.model.CategoryEntity
import com.example.data.model.PoemWithCategoriesAndNotes
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun PoemEditScreen(
    poemToEdit: PoemWithCategoriesAndNotes?,
    allCategories: List<CategoryEntity>,
    onSave: (title: String, author: String, content: String, collection: String, categoryIds: List<Long>) -> Unit,
    onCreateCategory: (String, (Long) -> Unit) -> Unit,
    onBack: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isEditMode = poemToEdit != null

    var title by remember { mutableStateOf(poemToEdit?.poem?.title ?: "") }
    var author by remember { mutableStateOf(poemToEdit?.poem?.author ?: "") }
    var content by remember { mutableStateOf(poemToEdit?.poem?.content ?: "") }
    var collection by remember { mutableStateOf(poemToEdit?.poem?.collection ?: "") }
    var selectedCategoryIds by remember {
        mutableStateOf(poemToEdit?.categories?.map { it.id }?.toSet() ?: emptySet())
    }

    var titleError by remember { mutableStateOf(false) }
    var authorError by remember { mutableStateOf(false) }
    var contentError by remember { mutableStateOf(false) }

    var showAddCategoryDialog by remember { mutableStateOf(false) }
    var newCategoryName by remember { mutableStateOf("") }

    val snackbarHostState = remember { SnackbarHostState() }
    val coroutineScope = rememberCoroutineScope()

    Scaffold(
        modifier = modifier.fillMaxSize(),
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        text = if (isEditMode) "시 수정" else "새로운 시 등록",
                        fontWeight = FontWeight.Bold,
                        fontFamily = FontFamily.Serif
                    )
                },
                navigationIcon = {
                    IconButton(
                        onClick = onBack,
                        modifier = Modifier.testTag("edit_back_button")
                    ) {
                        Icon(
                            imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = "뒤로가기"
                        )
                    }
                },
                actions = {
                    IconButton(
                        onClick = {
                            var hasError = false
                            if (title.isBlank()) {
                                titleError = true
                                hasError = true
                            } else {
                                titleError = false
                            }
                            if (author.isBlank()) {
                                authorError = true
                                hasError = true
                            } else {
                                authorError = false
                            }
                            if (content.isBlank()) {
                                contentError = true
                                hasError = true
                            } else {
                                contentError = false
                            }

                            if (hasError) {
                                coroutineScope.launch {
                                    snackbarHostState.showSnackbar("제목, 작가, 시 내용은 필수 입력 항목입니다.")
                                }
                            } else {
                                onSave(
                                    title.trim(),
                                    author.trim(),
                                    content.trim(),
                                    collection.trim(),
                                    selectedCategoryIds.toList()
                                )
                            }
                        },
                        modifier = Modifier.testTag("save_poem_button")
                    ) {
                        Icon(
                            imageVector = Icons.Default.Check,
                            contentDescription = "저장",
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        },
        snackbarHost = { SnackbarHost(snackbarHostState) },
        containerColor = MaterialTheme.colorScheme.background
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Title Field (Required)
            OutlinedTextField(
                value = title,
                onValueChange = {
                    title = it
                    if (it.isNotBlank()) titleError = false
                },
                label = { Text("시 제목 *") },
                placeholder = { Text("예: 서시") },
                isError = titleError,
                supportingText = if (titleError) {
                    { Text("시 제목을 입력해 주세요.") }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_title_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Author Field (Required)
            OutlinedTextField(
                value = author,
                onValueChange = {
                    author = it
                    if (it.isNotBlank()) authorError = false
                },
                label = { Text("작가 *") },
                placeholder = { Text("예: 윤동주") },
                isError = authorError,
                supportingText = if (authorError) {
                    { Text("작가를 입력해 주세요.") }
                } else null,
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_author_input")
            )

            Spacer(modifier = Modifier.height(14.dp))

            // Collection Field (Optional)
            OutlinedTextField(
                value = collection,
                onValueChange = { collection = it },
                label = { Text("시집 제목 (선택)") },
                placeholder = { Text("예: 하늘과 바람과 별과 시") },
                singleLine = true,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_collection_input")
            )

            Spacer(modifier = Modifier.height(20.dp))

            // Categories (Optional, Multi-select, Create inline)
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "카테고리 (선택)",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )

                OutlinedButton(
                    onClick = { showAddCategoryDialog = true },
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.testTag("add_new_category_button")
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
                        contentDescription = null,
                        modifier = Modifier.size(16.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("새 카테고리")
                }
            }

            Spacer(modifier = Modifier.height(8.dp))

            if (allCategories.isEmpty()) {
                Text(
                    text = "아직 생성된 카테고리가 없습니다. '새 카테고리' 버튼을 눌러 자유롭게 만들어보세요.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    allCategories.forEach { cat ->
                        val isSelected = selectedCategoryIds.contains(cat.id)
                        FilterChip(
                            selected = isSelected,
                            onClick = {
                                selectedCategoryIds = if (isSelected) {
                                    selectedCategoryIds - cat.id
                                } else {
                                    selectedCategoryIds + cat.id
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

            // Full Content Field (Required)
            OutlinedTextField(
                value = content,
                onValueChange = {
                    content = it
                    if (it.isNotBlank()) contentError = false
                },
                label = { Text("시 내용 전체 *") },
                placeholder = { Text("작품의 전체 구절을 입력해 주세요.") },
                isError = contentError,
                supportingText = if (contentError) {
                    { Text("시 내용을 입력해 주세요.") }
                } else null,
                minLines = 8,
                maxLines = 20,
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .testTag("edit_content_input")
            )

            Spacer(modifier = Modifier.height(28.dp))

            // Submit Button
            Button(
                onClick = {
                    var hasError = false
                    if (title.isBlank()) {
                        titleError = true
                        hasError = true
                    } else {
                        titleError = false
                    }
                    if (author.isBlank()) {
                        authorError = true
                        hasError = true
                    } else {
                        authorError = false
                    }
                    if (content.isBlank()) {
                        contentError = true
                        hasError = true
                    } else {
                        contentError = false
                    }

                    if (hasError) {
                        coroutineScope.launch {
                            snackbarHostState.showSnackbar("필수 항목을 모두 입력해 주세요.")
                        }
                    } else {
                        onSave(
                            title.trim(),
                            author.trim(),
                            content.trim(),
                            collection.trim(),
                            selectedCategoryIds.toList()
                        )
                    }
                },
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(52.dp)
                    .testTag("save_poem_submit_button")
            ) {
                Text(
                    text = if (isEditMode) "수정 완료" else "시 등록하기",
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.Bold
                )
            }

            Spacer(modifier = Modifier.height(40.dp))
        }
    }

    // Add Category Inline Dialog
    if (showAddCategoryDialog) {
        AlertDialog(
            onDismissRequest = {
                showAddCategoryDialog = false
                newCategoryName = ""
            },
            title = { Text("새 카테고리 만들기") },
            text = {
                OutlinedTextField(
                    value = newCategoryName,
                    onValueChange = { newCategoryName = it },
                    label = { Text("카테고리 이름") },
                    placeholder = { Text("예: 비 오는 날, 위로, 새벽") },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            },
            confirmButton = {
                Button(
                    onClick = {
                        val trimmed = newCategoryName.trim()
                        if (trimmed.isNotBlank()) {
                            onCreateCategory(trimmed) { createdId ->
                                selectedCategoryIds = selectedCategoryIds + createdId
                            }
                            showAddCategoryDialog = false
                            newCategoryName = ""
                        }
                    }
                ) {
                    Text("생성 및 선택")
                }
            },
            dismissButton = {
                TextButton(
                    onClick = {
                        showAddCategoryDialog = false
                        newCategoryName = ""
                    }
                ) {
                    Text("취소")
                }
            }
        )
    }
}
