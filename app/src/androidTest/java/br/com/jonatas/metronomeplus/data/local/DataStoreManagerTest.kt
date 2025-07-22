package br.com.jonatas.metronomeplus.data.local

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

private val Context.testDataStore: DataStore<Preferences> by preferencesDataStore(name = "tests_preferences")

@RunWith(AndroidJUnit4::class)
class DataStoreManagerTest {

    private lateinit var context: Context
    private lateinit var dataStoreManager: DataStoreManager

    companion object {
        private val TEST_STRING_KEY = stringPreferencesKey("TEST_STRING_KEY")
        private val TEST_WITHOUT_KEY = stringPreferencesKey("TEST_WITHOUT_kEY")
    }

    @Before
    fun setup() {
        context = ApplicationProvider.getApplicationContext()

        dataStoreManager = DataStoreManager.getInstance(context)
    }

    @After
    fun tearDown() = runTest {
        context.testDataStore.edit { it.clear() }
    }

    @Test
    fun shouldReturnSameInstanceWhenGetInstanceIsCalled() {
        val instance1 = DataStoreManager.getInstance(context)
        val instance2 = DataStoreManager.getInstance(context)

        assertEquals(instance1, instance2)
    }

    @Test
    fun shouldReturnEmptyStringWhenGetDataDoesNotFindTheKey() = runTest {
        val result = dataStoreManager.getData(TEST_WITHOUT_KEY).first()

        assertEquals("", result)
    }

    @Test
    fun shouldPersistAndReturnValuesWhenCallingSetAndGetData() = runTest {
        val testString = "Hello DataStore Test"

        dataStoreManager.setData(TEST_STRING_KEY, testString)

        val actualString = dataStoreManager.getData(TEST_STRING_KEY).first()

        assertEquals(testString, actualString)
    }

    @Test
    fun shouldOverrideAnExistingValueWhenSetDataIsCalledWithNewValue() = runTest {
        val testString = "Hello DataStore Test"
        val testString2 = "GoodBye DataStore Test"

        dataStoreManager.setData(TEST_STRING_KEY, testString)
        val actualString = dataStoreManager.getData(TEST_STRING_KEY).first()
        assertEquals(testString, actualString)

        dataStoreManager.setData(TEST_STRING_KEY, testString2)
        val actualString2 = dataStoreManager.getData(TEST_STRING_KEY).first()
        assertEquals(testString2, actualString2)
    }
}