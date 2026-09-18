package com.example.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.data.local.AppDatabase
import com.example.data.local.SavedDrawFilter
import com.example.data.local.UserPreferences
import com.example.data.model.CategoryEntity
import com.example.data.model.PoemWithCategoriesAndNotes
import com.example.data.repository.PoemRepository
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

sealed class Screen {
    data object Draw : Screen()
    data object Library : Screen()
    data class Detail(val poemId: Long, val fromScreen: Screen = Draw) : Screen()
    data class Edit(val poemId: Long? = null) : Screen()
    data object Settings : Screen()
    data object Trash : Screen()
    data object Onboarding : Screen()
}

class PoemViewModel(application: Application) : AndroidViewModel(application) {

    private val database = AppDatabase.getDatabase(application, viewModelScope)
    private val repository = PoemRepository(database.poemDao())
    private val prefs = UserPreferences(application)

    // User preferences states
    val onboardingCompleted = prefs.onboardingCompleted
    val exclusionCount = prefs.exclusionCount
    val fontSizeScale = prefs.fontSizeScale
    val themeMode = prefs.themeMode
    val savedDrawFilter = prefs.savedFilter

    // Data streams
    val activePoems: StateFlow<List<PoemWithCategoriesAndNotes>> =
        repository.allActivePoemsWithDetails.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val trashPoems: StateFlow<List<PoemWithCategoriesAndNotes>> =
        repository.deletedPoemsWithDetails.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val categories: StateFlow<List<CategoryEntity>> =
        repository.allCategories.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val authors: StateFlow<List<String>> =
        repository.distinctAuthors.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    val collections: StateFlow<List<String>> =
        repository.distinctCollections.stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(5000),
            initialValue = emptyList()
        )

    // Navigation and screen state:
    // ALWAYS start on Draw screen (Requirement 3: "앱을 실행하면 항상 🎲 뽑기 화면에서 시작한다")
    // If onboarding not completed, start with Onboarding screen
    private val _currentScreen = MutableStateFlow<Screen>(
        if (prefs.onboardingCompleted.value) Screen.Draw else Screen.Onboarding
    )
    val currentScreen: StateFlow<Screen> = _currentScreen.asStateFlow()

    // Draw animation state
    private val _isDrawingAnimation = MutableStateFlow(false)
    val isDrawingAnimation: StateFlow<Boolean> = _isDrawingAnimation.asStateFlow()

    // Currently selected / viewed poem
    private val _currentPoemId = MutableStateFlow<Long?>(null)
    val currentPoemId: StateFlow<Long?> = _currentPoemId.asStateFlow()

    fun navigateTo(screen: Screen) {
        _currentScreen.value = screen
        if (screen is Screen.Detail) {
            _currentPoemId.value = screen.poemId
        }
    }

    fun completeOnboarding(andNavigateToLibrary: Boolean = false) {
        prefs.setOnboardingCompleted(true)
        if (andNavigateToLibrary) {
            _currentScreen.value = Screen.Library
        } else {
            _currentScreen.value = Screen.Draw
        }
    }

    // --- Drawing action ---
    fun drawPoem(
        filter: SavedDrawFilter = savedDrawFilter.value,
        onSuccess: (PoemWithCategoriesAndNotes) -> Unit,
        onEmpty: () -> Unit
    ) {
        viewModelScope.launch {
            _isDrawingAnimation.value = true
            // Anticipation animation duration: ~1.4 seconds
            val delayJob = launch { delay(1400) }

            val all = activePoems.value
            val picked = repository.drawRandomPoem(
                filter = filter,
                exclusionCount = exclusionCount.value,
                allPoems = all
            )

            delayJob.join()
            _isDrawingAnimation.value = false

            if (picked != null) {
                _currentPoemId.value = picked.poem.id
                onSuccess(picked)
            } else {
                onEmpty()
            }
        }
    }

    fun reDraw(filter: SavedDrawFilter = savedDrawFilter.value, onResult: (Boolean) -> Unit) {
        drawPoem(
            filter = filter,
            onSuccess = { onResult(true) },
            onEmpty = { onResult(false) }
        )
    }

    // Filter configuration
    fun updateSavedFilter(filter: SavedDrawFilter) {
        prefs.saveDrawFilter(filter)
    }

    fun resetSavedFilter() {
        prefs.resetDrawFilter()
    }

    // Preferences configuration
    fun setExclusionCount(count: Int) {
        prefs.setExclusionCount(count)
    }

    fun setFontSizeScale(scale: Float) {
        prefs.setFontSizeScale(scale)
    }

    fun setThemeMode(mode: String) {
        prefs.setThemeMode(mode)
    }

    // Favorite toggle
    fun toggleFavorite(poemId: Long) {
        viewModelScope.launch {
            val poemItem = activePoems.value.find { it.poem.id == poemId }
                ?: trashPoems.value.find { it.poem.id == poemId }
            val currentFav = poemItem?.poem?.isFavorite ?: false
            repository.toggleFavorite(poemId, currentFav)
        }
    }

    // Poem CRUD
    fun savePoem(
        id: Long = 0,
        title: String,
        author: String,
        content: String,
        collection: String,
        categoryIds: List<Long>,
        isFavorite: Boolean = false,
        onComplete: (Long) -> Unit
    ) {
        viewModelScope.launch {
            val poemId = repository.savePoem(
                id = id,
                title = title,
                author = author,
                content = content,
                collection = collection,
                categoryIds = categoryIds,
                isFavorite = isFavorite
            )
            onComplete(poemId)
        }
    }

    fun moveToTrash(poemId: Long, onComplete: () -> Unit = {}) {
        viewModelScope.launch {
            repository.moveToTrash(poemId)
            onComplete()
        }
    }

    fun restoreFromTrash(poemId: Long) {
        viewModelScope.launch {
            repository.restoreFromTrash(poemId)
        }
    }

    fun permanentlyDeletePoem(poemId: Long) {
        viewModelScope.launch {
            repository.permanentlyDeletePoem(poemId)
        }
    }

    fun clearTrash() {
        viewModelScope.launch {
            repository.clearTrash()
        }
    }

    // Category CRUD
    fun createCategory(name: String, onCreated: (Long) -> Unit = {}) {
        viewModelScope.launch {
            val id = repository.createCategory(name)
            onCreated(id)
        }
    }

    fun updateCategory(id: Long, newName: String) {
        viewModelScope.launch {
            repository.updateCategory(id, newName)
        }
    }

    fun deleteCategory(id: Long) {
        viewModelScope.launch {
            repository.deleteCategory(id)
        }
    }

    // Note CRUD
    fun addNote(poemId: Long, content: String) {
        viewModelScope.launch {
            repository.addNote(poemId, content)
        }
    }

    fun updateNote(noteId: Long, poemId: Long, content: String) {
        viewModelScope.launch {
            repository.updateNote(noteId, poemId, content)
        }
    }

    fun deleteNote(noteId: Long) {
        viewModelScope.launch {
            repository.deleteNote(noteId)
        }
    }
}
