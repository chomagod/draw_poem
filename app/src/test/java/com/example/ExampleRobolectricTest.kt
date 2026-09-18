package com.example

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.example.data.local.AppDatabase
import com.example.data.local.SavedDrawFilter
import com.example.data.model.PoemCategoryCrossRef
import com.example.data.model.PoemEntity
import com.example.data.repository.PoemRepository
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [36])
class ExampleRobolectricTest {

  private lateinit var db: AppDatabase
  private lateinit var repository: PoemRepository

  @Before
  fun setUp() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
      .allowMainThreadQueries()
      .build()
    repository = PoemRepository(db.poemDao())
  }

  @After
  fun tearDown() {
    db.close()
  }

  @Test
  fun `read string from context`() {
    val context = ApplicationProvider.getApplicationContext<Context>()
    val appName = context.getString(R.string.app_name)
    assertEquals("시 뽑기", appName)
  }

  @Test
  fun `test poem insertion and random draw with filter`() = runBlocking {
    val poem1Id = repository.savePoem(
      title = "서시",
      author = "윤동주",
      content = "죽는 날까지 하늘을 우러러...",
      collection = "하늘과 바람과 별과 시",
      categoryIds = emptyList()
    )

    val poem2Id = repository.savePoem(
      title = "진달래꽃",
      author = "김소월",
      content = "나 보기가 역겨워 가실 때에는...",
      collection = "진달래꽃",
      categoryIds = emptyList()
    )

    val allPoems = repository.allActivePoemsWithDetails.first()
    assertEquals(2, allPoems.size)

    // Draw only 윤동주
    val drawn = repository.drawRandomPoem(
      filter = SavedDrawFilter(author = "윤동주"),
      exclusionCount = 0,
      allPoems = allPoems
    )
    assertNotNull(drawn)
    assertEquals("윤동주", drawn?.poem?.author)
    assertEquals("서시", drawn?.poem?.title)
  }

  @Test
  fun `test recent exclusion logic`() = runBlocking {
    val poem1Id = repository.savePoem(
      title = "서시",
      author = "윤동주",
      content = "죽는 날까지...",
      collection = "",
      categoryIds = emptyList()
    )
    val poem2Id = repository.savePoem(
      title = "자화상",
      author = "윤동주",
      content = "산모퉁이를 돌아...",
      collection = "",
      categoryIds = emptyList()
    )

    val allPoems = repository.allActivePoemsWithDetails.first()

    // Draw 1st poem
    val firstDraw = repository.drawRandomPoem(
      filter = SavedDrawFilter(),
      exclusionCount = 1,
      allPoems = allPoems
    )
    assertNotNull(firstDraw)

    // With exclusionCount = 1, next draw should pick the other poem!
    val secondDraw = repository.drawRandomPoem(
      filter = SavedDrawFilter(),
      exclusionCount = 1,
      allPoems = allPoems
    )
    assertNotNull(secondDraw)
    assertTrue(firstDraw?.poem?.id != secondDraw?.poem?.id)
  }
}

