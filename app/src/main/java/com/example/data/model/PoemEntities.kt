package com.example.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "poems")
data class PoemEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val title: String,
    val author: String,
    val content: String,
    val collection: String = "", // 시집 제목 (선택)
    val isFavorite: Boolean = false,
    val isDeleted: Boolean = false,
    val deletedAt: Long? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "categories",
    indices = [Index(value = ["name"], unique = true)]
)
data class CategoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val name: String,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "poem_categories",
    primaryKeys = ["poemId", "categoryId"],
    indices = [Index("poemId"), Index("categoryId")],
    foreignKeys = [
        ForeignKey(
            entity = PoemEntity::class,
            parentColumns = ["id"],
            childColumns = ["poemId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class PoemCategoryCrossRef(
    val poemId: Long,
    val categoryId: Long
)

@Entity(
    tableName = "notes",
    indices = [Index("poemId")],
    foreignKeys = [
        ForeignKey(
            entity = PoemEntity::class,
            parentColumns = ["id"],
            childColumns = ["poemId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class NoteEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val poemId: Long,
    val content: String,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "draw_history",
    indices = [Index("poemId")]
)
data class DrawHistoryEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val poemId: Long,
    val drawnAt: Long = System.currentTimeMillis()
)

data class PoemWithCategoriesAndNotes(
    val poem: PoemEntity,
    val categories: List<CategoryEntity> = emptyList(),
    val notes: List<NoteEntity> = emptyList()
)
