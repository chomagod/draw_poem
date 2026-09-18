package com.example

import android.os.Bundle
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Casino
import androidx.compose.material.icons.filled.CollectionsBookmark
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.ui.screens.detail.PoemDetailScreen
import com.example.ui.screens.draw.DrawAnimationOverlay
import com.example.ui.screens.draw.DrawScreen
import com.example.ui.screens.edit.PoemEditScreen
import com.example.ui.screens.library.MyPoemsScreen
import com.example.ui.screens.onboarding.OnboardingScreen
import com.example.ui.screens.settings.SettingsScreen
import com.example.ui.screens.trash.TrashScreen
import com.example.ui.theme.MyApplicationTheme
import com.example.ui.viewmodel.PoemViewModel
import com.example.ui.viewmodel.Screen
import kotlinx.coroutines.launch

class MainActivity : ComponentActivity() {

    private val viewModel: PoemViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            PoemPickerApp(viewModel = viewModel)
        }
    }
}

@Composable
fun PoemPickerApp(viewModel: PoemViewModel) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }

    // States from ViewModel
    val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
    val fontSizeScale by viewModel.fontSizeScale.collectAsStateWithLifecycle()
    val currentScreen by viewModel.currentScreen.collectAsStateWithLifecycle()
    val isDrawingAnimation by viewModel.isDrawingAnimation.collectAsStateWithLifecycle()

    val activePoems by viewModel.activePoems.collectAsStateWithLifecycle()
    val trashPoems by viewModel.trashPoems.collectAsStateWithLifecycle()
    val categories by viewModel.categories.collectAsStateWithLifecycle()
    val authors by viewModel.authors.collectAsStateWithLifecycle()
    val collections by viewModel.collections.collectAsStateWithLifecycle()
    val savedFilter by viewModel.savedDrawFilter.collectAsStateWithLifecycle()
    val exclusionCount by viewModel.exclusionCount.collectAsStateWithLifecycle()
    val currentPoemId by viewModel.currentPoemId.collectAsStateWithLifecycle()

    // Back button handling
    BackHandler(enabled = currentScreen !is Screen.Draw && currentScreen !is Screen.Onboarding) {
        when (val screen = currentScreen) {
            is Screen.Detail -> viewModel.navigateTo(screen.fromScreen)
            is Screen.Edit -> {
                if (currentPoemId != null) {
                    viewModel.navigateTo(Screen.Detail(currentPoemId!!, fromScreen = Screen.Library))
                } else {
                    viewModel.navigateTo(Screen.Library)
                }
            }
            is Screen.Settings -> viewModel.navigateTo(Screen.Draw)
            is Screen.Trash -> viewModel.navigateTo(Screen.Settings)
            is Screen.Library -> viewModel.navigateTo(Screen.Draw)
            else -> {}
        }
    }

    MyApplicationTheme(themeMode = themeMode) {
        val showBottomNav = currentScreen is Screen.Draw || currentScreen is Screen.Library

        Box(modifier = Modifier.fillMaxSize()) {
            Scaffold(
                modifier = Modifier.fillMaxSize(),
                snackbarHost = { SnackbarHost(snackbarHostState) },
                bottomBar = {
                    if (showBottomNav) {
                        NavigationBar(
                            containerColor = MaterialTheme.colorScheme.surface,
                            tonalElevation = 4.dp,
                            modifier = Modifier.testTag("main_bottom_navigation")
                        ) {
                            // 🎲 뽑기 Tab
                            NavigationBarItem(
                                selected = currentScreen is Screen.Draw,
                                onClick = { viewModel.navigateTo(Screen.Draw) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.Casino,
                                        contentDescription = "뽑기",
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = "뽑기",
                                        fontWeight = if (currentScreen is Screen.Draw) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("tab_draw")
                            )

                            // 📚 내 시 Tab
                            NavigationBarItem(
                                selected = currentScreen is Screen.Library,
                                onClick = { viewModel.navigateTo(Screen.Library) },
                                icon = {
                                    Icon(
                                        imageVector = Icons.Default.MenuBook,
                                        contentDescription = "내 시",
                                        modifier = Modifier.size(24.dp)
                                    )
                                },
                                label = {
                                    Text(
                                        text = "내 시",
                                        fontWeight = if (currentScreen is Screen.Library) FontWeight.Bold else FontWeight.Normal
                                    )
                                },
                                colors = NavigationBarItemDefaults.colors(
                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                    selectedTextColor = MaterialTheme.colorScheme.primary,
                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer
                                ),
                                modifier = Modifier.testTag("tab_library")
                            )
                        }
                    }
                }
            ) { innerPadding ->
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(innerPadding)
                ) {
                    Crossfade(
                        targetState = currentScreen,
                        label = "screen_transition"
                    ) { screen ->
                        when (screen) {
                            is Screen.Onboarding -> {
                                OnboardingScreen(
                                    onStartClick = { viewModel.completeOnboarding(andNavigateToLibrary = false) },
                                    onRegisterPoemClick = {
                                        viewModel.completeOnboarding(andNavigateToLibrary = true)
                                        viewModel.navigateTo(Screen.Edit())
                                    }
                                )
                            }

                            is Screen.Draw -> {
                                DrawScreen(
                                    currentFilter = savedFilter,
                                    allPoems = activePoems,
                                    allCategories = categories,
                                    authors = authors,
                                    collections = collections,
                                    exclusionCount = exclusionCount,
                                    onDrawClick = {
                                        viewModel.drawPoem(
                                            onSuccess = { picked ->
                                                viewModel.navigateTo(Screen.Detail(picked.poem.id, fromScreen = Screen.Draw))
                                            },
                                            onEmpty = {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar(
                                                        "조건에 맞는 시가 없습니다. 필터를 변경하거나 해제해 보세요."
                                                    )
                                                }
                                            }
                                        )
                                    },
                                    onApplyFilter = { newFilter ->
                                        viewModel.updateSavedFilter(newFilter)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("뽑기 필터가 적용되었습니다.")
                                        }
                                    },
                                    onResetFilter = {
                                        viewModel.resetSavedFilter()
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("뽑기 필터가 전체로 초기화되었습니다.")
                                        }
                                    },
                                    onNavigateToSettings = { viewModel.navigateTo(Screen.Settings) }
                                )
                            }

                            is Screen.Library -> {
                                MyPoemsScreen(
                                    poems = activePoems,
                                    categories = categories,
                                    authors = authors,
                                    collections = collections,
                                    onPoemClick = { poemId ->
                                        viewModel.navigateTo(Screen.Detail(poemId, fromScreen = Screen.Library))
                                    },
                                    onAddPoemClick = { viewModel.navigateTo(Screen.Edit()) },
                                    onToggleFavorite = { poemId -> viewModel.toggleFavorite(poemId) },
                                    onNavigateToSettings = { viewModel.navigateTo(Screen.Settings) }
                                )
                            }

                            is Screen.Detail -> {
                                val poemDetail = activePoems.find { it.poem.id == screen.poemId }
                                    ?: trashPoems.find { it.poem.id == screen.poemId }

                                PoemDetailScreen(
                                    poemWithDetails = poemDetail,
                                    fontSizeScale = fontSizeScale,
                                    onBack = { viewModel.navigateTo(screen.fromScreen) },
                                    onToggleFavorite = { poemId -> viewModel.toggleFavorite(poemId) },
                                    onRedraw = {
                                        viewModel.reDraw { success ->
                                            if (success) {
                                                viewModel.currentPoemId.value?.let { newId ->
                                                    viewModel.navigateTo(Screen.Detail(newId, fromScreen = screen.fromScreen))
                                                }
                                            } else {
                                                coroutineScope.launch {
                                                    snackbarHostState.showSnackbar("조건에 맞는 다른 시가 없습니다.")
                                                }
                                            }
                                        }
                                    },
                                    onEdit = { poemId -> viewModel.navigateTo(Screen.Edit(poemId)) },
                                    onDelete = { poemId ->
                                        viewModel.moveToTrash(poemId) {
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar("시가 휴지통으로 이동되었습니다.")
                                            }
                                        }
                                    },
                                    onAddNote = { poemId, note ->
                                        viewModel.addNote(poemId, note)
                                    },
                                    onUpdateNote = { noteId, poemId, note ->
                                        viewModel.updateNote(noteId, poemId, note)
                                    },
                                    onDeleteNote = { noteId ->
                                        viewModel.deleteNote(noteId)
                                    }
                                )
                            }

                            is Screen.Edit -> {
                                val poemToEdit = screen.poemId?.let { id ->
                                    activePoems.find { it.poem.id == id }
                                }

                                PoemEditScreen(
                                    poemToEdit = poemToEdit,
                                    allCategories = categories,
                                    onSave = { title, author, content, collection, categoryIds ->
                                        viewModel.savePoem(
                                            id = screen.poemId ?: 0L,
                                            title = title,
                                            author = author,
                                            content = content,
                                            collection = collection,
                                            categoryIds = categoryIds,
                                            isFavorite = poemToEdit?.poem?.isFavorite ?: false
                                        ) { savedId ->
                                            coroutineScope.launch {
                                                snackbarHostState.showSnackbar(
                                                    if (screen.poemId != null) "시가 수정되었습니다." else "새로운 시가 등록되었습니다."
                                                )
                                            }
                                            viewModel.navigateTo(Screen.Detail(savedId, fromScreen = Screen.Library))
                                        }
                                    },
                                    onCreateCategory = { name, onCreated ->
                                        viewModel.createCategory(name, onCreated)
                                    },
                                    onBack = {
                                        if (screen.poemId != null) {
                                            viewModel.navigateTo(Screen.Detail(screen.poemId, fromScreen = Screen.Library))
                                        } else {
                                            viewModel.navigateTo(Screen.Library)
                                        }
                                    }
                                )
                            }

                            is Screen.Settings -> {
                                SettingsScreen(
                                    exclusionCount = exclusionCount,
                                    fontSizeScale = fontSizeScale,
                                    themeMode = themeMode,
                                    trashPoems = trashPoems,
                                    categories = categories,
                                    onUpdateExclusionCount = { count -> viewModel.setExclusionCount(count) },
                                    onUpdateFontSizeScale = { scale -> viewModel.setFontSizeScale(scale) },
                                    onUpdateThemeMode = { mode -> viewModel.setThemeMode(mode) },
                                    onCreateCategory = { name -> viewModel.createCategory(name) },
                                    onUpdateCategory = { id, name -> viewModel.updateCategory(id, name) },
                                    onDeleteCategory = { id -> viewModel.deleteCategory(id) },
                                    onNavigateToTrash = { viewModel.navigateTo(Screen.Trash) },
                                    onBack = { viewModel.navigateTo(Screen.Draw) }
                                )
                            }

                            is Screen.Trash -> {
                                TrashScreen(
                                    trashPoems = trashPoems,
                                    onRestore = { poemId ->
                                        viewModel.restoreFromTrash(poemId)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("시가 복원되었습니다.")
                                        }
                                    },
                                    onPermanentDelete = { poemId ->
                                        viewModel.permanentlyDeletePoem(poemId)
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("시가 영구 삭제되었습니다.")
                                        }
                                    },
                                    onClearTrash = {
                                        viewModel.clearTrash()
                                        coroutineScope.launch {
                                            snackbarHostState.showSnackbar("휴지통을 모두 비웠습니다.")
                                        }
                                    },
                                    onBack = { viewModel.navigateTo(Screen.Settings) }
                                )
                            }
                        }
                    }
                }
            }

            // Short anticipation animation overlay during draw
            if (isDrawingAnimation) {
                DrawAnimationOverlay()
            }
        }
    }
}
