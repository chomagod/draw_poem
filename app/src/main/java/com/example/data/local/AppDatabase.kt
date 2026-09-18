package com.example.data.local

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.data.model.CategoryEntity
import com.example.data.model.DrawHistoryEntity
import com.example.data.model.NoteEntity
import com.example.data.model.PoemCategoryCrossRef
import com.example.data.model.PoemEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        PoemEntity::class,
        CategoryEntity::class,
        PoemCategoryCrossRef::class,
        NoteEntity::class,
        DrawHistoryEntity::class
    ],
    version = 1,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {

    abstract fun poemDao(): PoemDao

    companion object {
        @Volatile
        private var INSTANCE: AppDatabase? = null

        fun getDatabase(context: Context, scope: CoroutineScope): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "poem_picker_database"
                )
                    .addCallback(DatabaseCallback(scope))
                    .fallbackToDestructiveMigration()
                    .build()
                INSTANCE = instance
                instance
            }
        }

        private class DatabaseCallback(
            private val scope: CoroutineScope
        ) : RoomDatabase.Callback() {
            override fun onCreate(db: SupportSQLiteDatabase) {
                super.onCreate(db)
                INSTANCE?.let { database ->
                    scope.launch(Dispatchers.IO) {
                        populateInitialPoems(database.poemDao())
                    }
                }
            }
        }

        private suspend fun populateInitialPoems(dao: PoemDao) {
            // Initial categories
            val catDawnId = dao.insertCategory(CategoryEntity(name = "새벽"))
            val catComfortId = dao.insertCategory(CategoryEntity(name = "위로"))
            val catSpringId = dao.insertCategory(CategoryEntity(name = "봄"))
            val catLongingId = dao.insertCategory(CategoryEntity(name = "그리움"))
            val catNatureId = dao.insertCategory(CategoryEntity(name = "자연"))

            // Poem 1: 윤동주 - 서시
            val poem1Id = dao.insertPoem(
                PoemEntity(
                    title = "서시",
                    author = "윤동주",
                    content = """죽는 날까지 하늘을 우러러
한 점 부끄럼이 없기를,
잎새에 이는 바람에도
나는 괴로워했다.
별을 노래하는 마음으로
모든 죽어가는 것을 사랑해야지
그리고 나한테 주어진 길을
걸어가야겠다.

오늘 밤에도 별이 바람에 스치운다.""".trimIndent(),
                    collection = "하늘과 바람과 별과 시",
                    isFavorite = true
                )
            )
            dao.insertPoemCategoryCrossRef(PoemCategoryCrossRef(poem1Id, catDawnId))
            dao.insertPoemCategoryCrossRef(PoemCategoryCrossRef(poem1Id, catComfortId))
            dao.insertNote(
                NoteEntity(
                    poemId = poem1Id,
                    content = "언제 읽어도 마음을 정갈하게 가다듬게 해주는 소중한 시."
                )
            )

            // Poem 2: 김소월 - 진달래꽃
            val poem2Id = dao.insertPoem(
                PoemEntity(
                    title = "진달래꽃",
                    author = "김소월",
                    content = """나 보기가 역겨워
가실 때에는
말없이 고이 보내 드리우리다.

영변에 약산
진달래꽃
아름 따다 가실 길에 뿌리우리다.

가시는 걸음 걸음
놓인 그 꽃을
사뿐히 즈려밟고 가시옵소서.

나 보기가 역겨워
가실 때에는
죽어도 아니 눈물 흘리우리다.""".trimIndent(),
                    collection = "진달래꽃",
                    isFavorite = true
                )
            )
            dao.insertPoemCategoryCrossRef(PoemCategoryCrossRef(poem2Id, catSpringId))
            dao.insertPoemCategoryCrossRef(PoemCategoryCrossRef(poem2Id, catLongingId))

            // Poem 3: 정지용 - 향수
            val poem3Id = dao.insertPoem(
                PoemEntity(
                    title = "향수",
                    author = "정지용",
                    content = """넓은 벌 동쪽 끝으로
옛이야기 지줄대는 실개천이 회돌아 나가고,
얼룩백이 황소가
해설피 금빛 게으른 울음을 우는 곳,
— 그곳이 차마 꿈엔들 잊힐 리야.

질화로에 재가 식어지면
비인 밭에 밤바람 소리 말을 달리고,
엷은 졸음에 겨운 늙으신 아버지가
짚베개를 돋아 고이시는 곳,
— 그곳이 차마 꿈엔들 잊힐 리야.""".trimIndent(),
                    collection = "정지용 시집",
                    isFavorite = false
                )
            )
            dao.insertPoemCategoryCrossRef(PoemCategoryCrossRef(poem3Id, catLongingId))

            // Poem 4: 박목월 - 나그네
            val poem4Id = dao.insertPoem(
                PoemEntity(
                    title = "나그네",
                    author = "박목월",
                    content = """강나루 건너서
밀밭 길을

구름에 달 가듯이
가는 나그네

길은 외줄기
남도 삼백 리

술 익는 마을마다
타는 저녁놀

구름에 달 가듯이
가는 나그네""".trimIndent(),
                    collection = "청록집",
                    isFavorite = false
                )
            )
            dao.insertPoemCategoryCrossRef(PoemCategoryCrossRef(poem4Id, catNatureId))

            // Poem 5: 윤동주 - 별 헤는 밤
            val poem5Id = dao.insertPoem(
                PoemEntity(
                    title = "별 헤는 밤",
                    author = "윤동주",
                    content = """계절이 지나가는 하늘에는
가을로 가득 차 있습니다.

나는 아무 걱정도 없이
가을 속의 별들을 다 헤일 듯합니다.

가슴 속에 하나 둘 새겨지는 별을
이제 다 못 헤는 것은
쉬이 아침이 오는 까닭이요,
내일 밤이 남은 까닭이요,
아직 나의 청춘이 다하지 않은 까닭입니다.

별 하나에 추억과
별 하나에 사랑과
별 하나에 쓸쓸함과
별 하나에 동경과
별 하나에 시와
별 하나에 어머니, 어머니""".trimIndent(),
                    collection = "하늘과 바람과 별과 시",
                    isFavorite = true
                )
            )
            dao.insertPoemCategoryCrossRef(PoemCategoryCrossRef(poem5Id, catDawnId))
            dao.insertPoemCategoryCrossRef(PoemCategoryCrossRef(poem5Id, catLongingId))
        }
    }
}
