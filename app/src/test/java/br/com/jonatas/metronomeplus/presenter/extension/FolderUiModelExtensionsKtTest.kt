package br.com.jonatas.metronomeplus.presenter.extension

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import br.com.jonatas.metronomeplus.R
import br.com.jonatas.metronomeplus.presenter.model.FolderUiModel
import com.ibm.icu.util.TimeZone
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner

@RunWith(RobolectricTestRunner::class)
class FolderUiModelExtensionsKtTest {

    private lateinit var context: Context

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        TimeZone.setDefault(TimeZone.getTimeZone("UTC"))
    }

    @After
    fun tearDown() {
        TimeZone.setDefault(null)
    }

    @Test
    fun `should return the correctly formatted date when it has a date`() {
        val expectDate = "01/01/2025 00:00"
        val folderUi = FolderUiModel(id = "1", name = "Folder", musics = 0, date = 1735689600000)

        assertEquals(expectDate, folderUi.getDateString())
    }

    @Test
    fun `should return the correctly formatted date when it doesn't have a date`() {
        val expectDate = "01/01/1970 00:00"
        val folderUi = FolderUiModel(id = "1", name = "Folder", musics = 0, date = 0)

        assertEquals(expectDate, folderUi.getDateString())
    }

    @Test
    fun `should return the correctly music count string when the number of musics is one or less`() {
        val expectedMusicStringList = listOf("0 Music", "1 Music")
        val folderUiList = listOf(
            FolderUiModel(id = "1", name = "Folder", musics = 0, date = 0),
            FolderUiModel(id = "2", name = "Folder 2", musics = 1, date = 0)
        )

        for (index in folderUiList.indices) {
            assertEquals(
                expectedMusicStringList[index],
                folderUiList[index].getMusicCountString(context)
            )
        }
    }

    @Test
    fun `should return the correctly music count string when the number of musics is two or more`() {
        val expectedMusicStringList = listOf("2 Musics", "10 Musics")
        val folderUiList = listOf(
            FolderUiModel(id = "1", name = "Folder", musics = 2, date = 0),
            FolderUiModel(id = "2", name = "Folder 2", musics = 10, date = 0)
        )

        for (index in folderUiList.indices) {
            assertEquals(
                expectedMusicStringList[index],
                folderUiList[index].getMusicCountString(context)
            )
        }
    }

    @Test
    fun `should return an empty folder drawable when the number of musics is zero`() {
        val expectedResId = R.drawable.ic_folder_empty_white
        val folderUi = FolderUiModel(id = "1", name = "Folder", musics = 0, date = 0)

        assertEquals(expectedResId, folderUi.getDrawableResId())
    }

    @Test
    fun `should return a folder drawable whit a file when the number of musics is between one and three`() {
        val expectedResId = R.drawable.ic_folder_file_white
        val testFolders = listOf(
            FolderUiModel(id = "1", name = "Folder", musics = 1, date = 0),
            FolderUiModel(id = "2", name = "Folder 2", musics = 2, date = 0),
            FolderUiModel(id = "3", name = "Folder 3", musics = 3, date = 0)
        )

        for (folderUi in testFolders) {
            assertEquals(expectedResId, folderUi.getDrawableResId())
        }
    }

    @Test
    fun `should return a folder drawable whit many files when the number of musics is more than three`() {
        val expectedResId = R.drawable.ic_folder_files_white
        val testFolders = listOf(
            FolderUiModel(id = "1", name = "Folder", musics = 4, date = 0),
            FolderUiModel(id = "2", name = "Folder", musics = 5, date = 0),
            FolderUiModel(id = "3", name = "Folder", musics = 20, date = 0)
        )

        for (folderUi in testFolders) {
            assertEquals(expectedResId, folderUi.getDrawableResId())
        }
    }
}