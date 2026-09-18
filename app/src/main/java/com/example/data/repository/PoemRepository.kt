package com.example.data.repository

import com.example.data.local.PoemDao
import com.example.data.local.SavedDrawFilter
import com.example.data.model.CategoryEntity
import com.example.data.model.DrawHistoryEntity
import com.example.data.model.NoteEntity
import com.example.data.model.PoemCategoryCrossRef
import com.example.data.model.PoemEntity
import com.example.data.model.PoemWithCategoriesAndNotes
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.withContext
import kotlin.random.Random

class PoemRepository(private val poemDao: PoemDao) {

    // Reactive flow combining active poems, cross refs, categories, and notes
    val allActivePoemsWithDetails: Flow<List<PoemWithCategoriesAndNotes>> = combine(
        poemDao.getAllActivePoemsFlow(),
        poemDao.getAllCategoriesFlow(),
        poemDao.getAllPoemCategoryCrossRefsFlow(),
        poemDao.getAllNotesFlow()
    ) { poems, categories, crossRefs, notes ->
        val catMap = categories.associateBy { it.id }
        val poemToCatIds = crossRefs.groupBy({ it.poemId }, { it.categoryId })
        val notesByPoem = notes.groupBy { it.poemId }

        poems.map { poem ->
            val poemCats = (poemToCatIds[poem.id] ?: emptyList())
                .mapNotNull { catMap[it] }
            val poemNotes = notesByPoem[poem.id] ?: emptyList()
            PoemWithCategoriesAndNotes(
                poem = poem,
                categories = poemCats,
                notes = poemNotes
            )
        }
    }

    val deletedPoemsWithDetails: Flow<List<PoemWithCategoriesAndNotes>> = combine(
        poemDao.getDeletedPoemsFlow(),
        poemDao.getAllCategoriesFlow(),
        poemDao.getAllPoemCategoryCrossRefsFlow(),
        poemDao.getAllNotesFlow()
    ) { poems, categories, crossRefs, notes ->
        val catMap = categories.associateBy { it.id }
        val poemToCatIds = crossRefs.groupBy({ it.poemId }, { it.categoryId })
        val notesByPoem = notes.groupBy { it.poemId }

        poems.map { poem ->
            val poemCats = (poemToCatIds[poem.id] ?: emptyList())
                .mapNotNull { catMap[it] }
            val poemNotes = notesByPoem[poem.id] ?: emptyList()
            PoemWithCategoriesAndNotes(
                poem = poem,
                categories = poemCats,
                notes = poemNotes
            )
        }
    }

    val allCategories: Flow<List<CategoryEntity>> = poemDao.getAllCategoriesFlow()
    val distinctAuthors: Flow<List<String>> = poemDao.getDistinctAuthorsFlow()
    val distinctCollections: Flow<List<String>> = poemDao.getDistinctCollectionsFlow()

    suspend fun getPoemWithDetails(poemId: Long): PoemWithCategoriesAndNotes? =
        withContext(Dispatchers.IO) {
            val poem = poemDao.getPoemById(poemId) ?: return@withContext null
            val categories = poemDao.getCategoriesForPoem(poemId)
            val notes = poemDao.getNotesForPoem(poemId)
            PoemWithCategoriesAndNotes(poem, categories, notes)
        }

    suspend fun savePoem(
        id: Long = 0,
        title: String,
        author: String,
        content: String,
        collection: String,
        categoryIds: List<Long>,
        isFavorite: Boolean = false
    ): Long = withContext(Dispatchers.IO) {
        val now = System.currentTimeMillis()
        val poem = PoemEntity(
            id = id,
            title = title.trim(),
            author = author.trim(),
            content = content.trim(),
            collection = collection.trim(),
            isFavorite = isFavorite,
            isDeleted = false,
            createdAt = if (id == 0L) now else (poemDao.getPoemById(id)?.createdAt ?: now),
            updatedAt = now
        )

        val poemId = if (id == 0L) {
            poemDao.insertPoem(poem)
        } else {
            poemDao.updatePoem(poem)
            id
        }

        // Update categories
        poemDao.deleteCategoriesForPoem(poemId)
        categoryIds.distinct().forEach { catId ->
            poemDao.insertPoemCategoryCrossRef(PoemCategoryCrossRef(poemId, catId))
        }

        poemId
    }

    suspend fun toggleFavorite(poemId: Long, currentFavorite: Boolean) =
        withContext(Dispatchers.IO) {
            poemDao.setFavorite(poemId, !currentFavorite)
        }

    suspend fun moveToTrash(poemId: Long) = withContext(Dispatchers.IO) {
        poemDao.moveToTrash(poemId)
    }

    suspend fun restoreFromTrash(poemId: Long) = withContext(Dispatchers.IO) {
        poemDao.restoreFromTrash(poemId)
    }

    suspend fun permanentlyDeletePoem(poemId: Long) = withContext(Dispatchers.IO) {
        poemDao.deleteCategoriesForPoem(poemId)
        poemDao.permanentlyDeletePoem(poemId)
    }

    suspend fun clearTrash() = withContext(Dispatchers.IO) {
        poemDao.clearTrash()
    }

    // Category CRUD
    suspend fun createCategory(name: String): Long = withContext(Dispatchers.IO) {
        val trimmed = name.trim()
        val existing = poemDao.getCategoryByName(trimmed)
        if (existing != null) return@withContext existing.id
        poemDao.insertCategory(CategoryEntity(name = trimmed))
    }

    suspend fun updateCategory(id: Long, newName: String) = withContext(Dispatchers.IO) {
        val trimmed = newName.trim()
        poemDao.updateCategory(CategoryEntity(id = id, name = trimmed))
    }

    suspend fun deleteCategory(id: Long) = withContext(Dispatchers.IO) {
        poemDao.deleteCategoryById(id)
    }

    // Note CRUD
    suspend fun addNote(poemId: Long, content: String): Long = withContext(Dispatchers.IO) {
        poemDao.insertNote(NoteEntity(poemId = poemId, content = content.trim()))
    }

    suspend fun updateNote(noteId: Long, poemId: Long, content: String) =
        withContext(Dispatchers.IO) {
            poemDao.updateNote(
                NoteEntity(
                    id = noteId,
                    poemId = poemId,
                    content = content.trim(),
                    updatedAt = System.currentTimeMillis()
                )
            )
        }

    suspend fun deleteNote(noteId: Long) = withContext(Dispatchers.IO) {
        poemDao.deleteNoteById(noteId)
    }

    // Random drawing logic
    suspend fun drawRandomPoem(
        filter: SavedDrawFilter,
        exclusionCount: Int,
        allPoems: List<PoemWithCategoriesAndNotes>
    ): PoemWithCategoriesAndNotes? = withContext(Dispatchers.IO) {
        // 1. Filter poems according to filter criteria
        val candidates = allPoems.filter { poemItem ->
            val poem = poemItem.poem

            // Author filter
            if (filter.author.isNotEmpty() && !poem.author.equals(filter.author, ignoreCase = true)) {
                return@filter false
            }

            // Collection filter
            if (filter.collection.isNotEmpty() && !poem.collection.equals(filter.collection, ignoreCase = true)) {
                return@filter false
            }

            // Category filter: poem must match any of selected categories (or all if specified)
            if (filter.categoryNames.isNotEmpty()) {
                val poemCatNames = poemItem.categories.map { it.name }
                val hasMatch = filter.categoryNames.any { it in poemCatNames }
                if (!hasMatch) return@filter false
            }

            // Favorite filter
            if (filter.favoriteOnly && !poem.isFavorite) {
                return@filter false
            }

            // Notes filter
            if (filter.notesOnly && poemItem.notes.isEmpty()) {
                return@filter false
            }

            true
        }

        if (candidates.isEmpty()) return@withContext null

        // 2. Duplicate exclusion
        val finalCandidate = if (exclusionCount > 0) {
            val recentExcludedIds = poemDao.getRecentDrawnPoemIds(exclusionCount)
            val nonExcluded = candidates.filter { it.poem.id !in recentExcludedIds }
            if (nonExcluded.isNotEmpty()) {
                nonExcluded[Random.nextInt(nonExcluded.size)]
            } else {
                // If all matching poems have been recently drawn, pick randomly from candidates
                candidates[Random.nextInt(candidates.size)]
            }
        } else {
            // Full random (exclusionCount == 0)
            candidates[Random.nextInt(candidates.size)]
        }

        // 3. Record in draw history
        poemDao.insertDrawHistory(DrawHistoryEntity(poemId = finalCandidate.poem.id))

        finalCandidate
    }
}
