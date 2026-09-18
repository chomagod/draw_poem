package com.example.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.data.model.CategoryEntity
import com.example.data.model.DrawHistoryEntity
import com.example.data.model.NoteEntity
import com.example.data.model.PoemCategoryCrossRef
import com.example.data.model.PoemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface PoemDao {

    // --- Poems ---
    @Query("SELECT * FROM poems WHERE isDeleted = 0 ORDER BY createdAt DESC")
    fun getAllActivePoemsFlow(): Flow<List<PoemEntity>>

    @Query("SELECT * FROM poems WHERE isDeleted = 0")
    suspend fun getAllActivePoems(): List<PoemEntity>

    @Query("SELECT * FROM poems WHERE isDeleted = 1 ORDER BY deletedAt DESC")
    fun getDeletedPoemsFlow(): Flow<List<PoemEntity>>

    @Query("SELECT * FROM poems WHERE id = :id LIMIT 1")
    suspend fun getPoemById(id: Long): PoemEntity?

    @Query("SELECT * FROM poems WHERE id = :id LIMIT 1")
    fun getPoemByIdFlow(id: Long): Flow<PoemEntity?>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoem(poem: PoemEntity): Long

    @Update
    suspend fun updatePoem(poem: PoemEntity)

    @Query("UPDATE poems SET isDeleted = 1, deletedAt = :deletedAt WHERE id = :id")
    suspend fun moveToTrash(id: Long, deletedAt: Long = System.currentTimeMillis())

    @Query("UPDATE poems SET isDeleted = 0, deletedAt = NULL WHERE id = :id")
    suspend fun restoreFromTrash(id: Long)

    @Query("DELETE FROM poems WHERE id = :id")
    suspend fun permanentlyDeletePoem(id: Long)

    @Query("DELETE FROM poems WHERE isDeleted = 1")
    suspend fun clearTrash()

    @Query("UPDATE poems SET isFavorite = :isFavorite WHERE id = :id")
    suspend fun setFavorite(id: Long, isFavorite: Boolean)

    // Distinct Authors and Collections for filter options
    @Query("SELECT DISTINCT author FROM poems WHERE isDeleted = 0 AND author != '' ORDER BY author ASC")
    fun getDistinctAuthorsFlow(): Flow<List<String>>

    @Query("SELECT DISTINCT collection FROM poems WHERE isDeleted = 0 AND collection != '' ORDER BY collection ASC")
    fun getDistinctCollectionsFlow(): Flow<List<String>>

    // --- Categories ---
    @Query("SELECT * FROM categories ORDER BY name ASC")
    fun getAllCategoriesFlow(): Flow<List<CategoryEntity>>

    @Query("SELECT * FROM categories ORDER BY name ASC")
    suspend fun getAllCategories(): List<CategoryEntity>

    @Query("SELECT * FROM categories WHERE name = :name LIMIT 1")
    suspend fun getCategoryByName(name: String): CategoryEntity?

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: CategoryEntity): Long

    @Update
    suspend fun updateCategory(category: CategoryEntity)

    @Delete
    suspend fun deleteCategory(category: CategoryEntity)

    @Query("DELETE FROM categories WHERE id = :id")
    suspend fun deleteCategoryById(id: Long)

    // --- Poem-Category relationships ---
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPoemCategoryCrossRef(crossRef: PoemCategoryCrossRef)

    @Query("DELETE FROM poem_categories WHERE poemId = :poemId")
    suspend fun deleteCategoriesForPoem(poemId: Long)

    @Query("""
        SELECT c.* FROM categories c
        INNER JOIN poem_categories pc ON c.id = pc.categoryId
        WHERE pc.poemId = :poemId
        ORDER BY c.name ASC
    """)
    fun getCategoriesForPoemFlow(poemId: Long): Flow<List<CategoryEntity>>

    @Query("""
        SELECT c.* FROM categories c
        INNER JOIN poem_categories pc ON c.id = pc.categoryId
        WHERE pc.poemId = :poemId
        ORDER BY c.name ASC
    """)
    suspend fun getCategoriesForPoem(poemId: Long): List<CategoryEntity>

    @Query("SELECT * FROM poem_categories")
    fun getAllPoemCategoryCrossRefsFlow(): Flow<List<PoemCategoryCrossRef>>

    @Query("SELECT * FROM poem_categories")
    suspend fun getAllPoemCategoryCrossRefs(): List<PoemCategoryCrossRef>

    // --- Notes ---
    @Query("SELECT * FROM notes WHERE poemId = :poemId ORDER BY createdAt DESC")
    fun getNotesForPoemFlow(poemId: Long): Flow<List<NoteEntity>>

    @Query("SELECT * FROM notes WHERE poemId = :poemId ORDER BY createdAt DESC")
    suspend fun getNotesForPoem(poemId: Long): List<NoteEntity>

    @Query("SELECT * FROM notes")
    fun getAllNotesFlow(): Flow<List<NoteEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertNote(note: NoteEntity): Long

    @Update
    suspend fun updateNote(note: NoteEntity)

    @Query("DELETE FROM notes WHERE id = :id")
    suspend fun deleteNoteById(id: Long)

    // --- Draw History for Recent Exclusion ---
    @Insert
    suspend fun insertDrawHistory(history: DrawHistoryEntity): Long

    @Query("SELECT DISTINCT poemId FROM draw_history ORDER BY id DESC LIMIT :limit")
    suspend fun getRecentDrawnPoemIds(limit: Int): List<Long>

    @Query("DELETE FROM draw_history")
    suspend fun clearDrawHistory()
}
