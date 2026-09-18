package com.example.data.local

import android.content.Context
import android.content.SharedPreferences
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

data class SavedDrawFilter(
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

    fun summary(): String {
        if (isDefault) return "전체"
        val parts = mutableListOf<String>()
        if (author.isNotEmpty()) parts.add("작가: $author")
        if (collection.isNotEmpty()) parts.add("시집: $collection")
        if (categoryNames.isNotEmpty()) parts.add("카테고리: ${categoryNames.joinToString(", ")}")
        if (favoriteOnly) parts.add("❤️ 좋아요")
        if (notesOnly) parts.add("📝 메모 있음")
        return parts.joinToString(" · ")
    }
}

class UserPreferences(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("poem_picker_prefs", Context.MODE_PRIVATE)

    companion object {
        private const val KEY_ONBOARDING_COMPLETED = "onboarding_completed"
        private const val KEY_EXCLUSION_COUNT = "recent_exclusion_count"
        private const val KEY_FONT_SIZE_SCALE = "font_size_scale"
        private const val KEY_THEME_MODE = "theme_mode" // SYSTEM, LIGHT, DARK

        private const val KEY_FILTER_AUTHOR = "filter_author"
        private const val KEY_FILTER_COLLECTION = "filter_collection"
        private const val KEY_FILTER_CATEGORIES = "filter_categories"
        private const val KEY_FILTER_FAVORITE = "filter_favorite"
        private const val KEY_FILTER_NOTES = "filter_notes"
    }

    private val _onboardingCompleted =
        MutableStateFlow(prefs.getBoolean(KEY_ONBOARDING_COMPLETED, false))
    val onboardingCompleted: StateFlow<Boolean> = _onboardingCompleted.asStateFlow()

    private val _exclusionCount =
        MutableStateFlow(prefs.getInt(KEY_EXCLUSION_COUNT, 10))
    val exclusionCount: StateFlow<Int> = _exclusionCount.asStateFlow()

    private val _fontSizeScale =
        MutableStateFlow(prefs.getFloat(KEY_FONT_SIZE_SCALE, 1.0f))
    val fontSizeScale: StateFlow<Float> = _fontSizeScale.asStateFlow()

    private val _themeMode =
        MutableStateFlow(prefs.getString(KEY_THEME_MODE, "SYSTEM") ?: "SYSTEM")
    val themeMode: StateFlow<String> = _themeMode.asStateFlow()

    private val _savedFilter = MutableStateFlow(loadSavedFilter())
    val savedFilter: StateFlow<SavedDrawFilter> = _savedFilter.asStateFlow()

    private fun loadSavedFilter(): SavedDrawFilter {
        return SavedDrawFilter(
            author = prefs.getString(KEY_FILTER_AUTHOR, "") ?: "",
            collection = prefs.getString(KEY_FILTER_COLLECTION, "") ?: "",
            categoryNames = prefs.getStringSet(KEY_FILTER_CATEGORIES, emptySet()) ?: emptySet(),
            favoriteOnly = prefs.getBoolean(KEY_FILTER_FAVORITE, false),
            notesOnly = prefs.getBoolean(KEY_FILTER_NOTES, false)
        )
    }

    fun setOnboardingCompleted(completed: Boolean) {
        prefs.edit().putBoolean(KEY_ONBOARDING_COMPLETED, completed).apply()
        _onboardingCompleted.value = completed
    }

    fun setExclusionCount(count: Int) {
        val safeCount = count.coerceAtLeast(0)
        prefs.edit().putInt(KEY_EXCLUSION_COUNT, safeCount).apply()
        _exclusionCount.value = safeCount
    }

    fun setFontSizeScale(scale: Float) {
        prefs.edit().putFloat(KEY_FONT_SIZE_SCALE, scale).apply()
        _fontSizeScale.value = scale
    }

    fun setThemeMode(mode: String) {
        prefs.edit().putString(KEY_THEME_MODE, mode).apply()
        _themeMode.value = mode
    }

    fun saveDrawFilter(filter: SavedDrawFilter) {
        prefs.edit()
            .putString(KEY_FILTER_AUTHOR, filter.author)
            .putString(KEY_FILTER_COLLECTION, filter.collection)
            .putStringSet(KEY_FILTER_CATEGORIES, filter.categoryNames)
            .putBoolean(KEY_FILTER_FAVORITE, filter.favoriteOnly)
            .putBoolean(KEY_FILTER_NOTES, filter.notesOnly)
            .apply()
        _savedFilter.value = filter
    }

    fun resetDrawFilter() {
        saveDrawFilter(SavedDrawFilter())
    }
}
