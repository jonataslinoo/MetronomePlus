package br.com.jonatas.metronomeplus.data.local

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.test.ext.junit.runners.AndroidJUnit4
import dagger.hilt.android.testing.HiltAndroidRule
import dagger.hilt.android.testing.HiltAndroidTest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.runner.RunWith
import javax.inject.Inject

@HiltAndroidTest
@RunWith(AndroidJUnit4::class)
class DataStoreManagerTest {

    @get:Rule
    val hiltRule = HiltAndroidRule(this)

    @Inject
    lateinit var testDataStore: DataStore<Preferences>

    @Inject
    lateinit var testDataStoreManager: DataStoreManager

    companion object {
        private val TEST_STRING_KEY = stringPreferencesKey("TEST_STRING_KEY")
        private val TEST_WITHOUT_KEY = stringPreferencesKey("TEST_WITHOUT_kEY")
    }

    @Before
    fun setup() {
        hiltRule.inject()
    }

    @After
    fun tearDown() = runTest {
        testDataStore.edit { it.clear() }
    }

    @Test
    fun shouldReturnEmptyStringWhenGetDataDoesNotFindTheKey() = runTest {
        val result = testDataStoreManager.getData(TEST_WITHOUT_KEY).first()

        assertEquals("", result)
    }

    @Test
    fun shouldPersistAndReturnValuesWhenCallingSetAndGetData() = runTest {
        val testString = "Hello DataStore Test"

        testDataStoreManager.setData(TEST_STRING_KEY, testString)

        val actualString = testDataStoreManager.getData(TEST_STRING_KEY).first()

        assertEquals(testString, actualString)
    }

    @Test
    fun shouldOverrideAnExistingValueWhenSetDataIsCalledWithNewValue() = runTest {
        val testString = "Hello DataStore Test"
        val testString2 = "GoodBye DataStore Test"

        testDataStoreManager.setData(TEST_STRING_KEY, testString)
        val actualString = testDataStoreManager.getData(TEST_STRING_KEY).first()
        assertEquals(testString, actualString)

        testDataStoreManager.setData(TEST_STRING_KEY, testString2)
        val actualString2 = testDataStoreManager.getData(TEST_STRING_KEY).first()
        assertEquals(testString2, actualString2)
    }
}