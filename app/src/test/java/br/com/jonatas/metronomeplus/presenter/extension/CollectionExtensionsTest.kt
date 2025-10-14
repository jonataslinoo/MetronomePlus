package br.com.jonatas.metronomeplus.presenter.extension

import org.junit.Assert.assertEquals
import org.junit.Test
import java.util.Collections

class CollectionExtensionsTest {

    val items: List<String> = listOf("item1", "item2", "item3", "item4", "item5")

    @Test
    fun `should swap the position of items when receiving two different positions`() {
        val fromPosition = 0
        val toPosition = 2
        val expectedItems = items.toMutableList().apply {
            Collections.swap(this, fromPosition, toPosition)
        }

        val swappedItems = items.swapItems(fromPosition, toPosition)

        assertEquals(expectedItems, swappedItems)
    }

    @Test
    fun `should not swap the position of items when receiving the same position`() {
        val swappedItems = items.swapItems(3, 3)

        assertEquals(items, swappedItems)
    }

    @Test
    fun `should not swap the position of items when receiving an invalid position`() {
        val swappedItems = items.swapItems(4, -1)

        assertEquals(items, swappedItems)
    }

    @Test
    fun `should add the item to the list if it does not already exist`() {
        val item = "item6"
        val expectedItems = items.toSet() + item

        val resultList = items.toSet().addOrRemove(item)

        assertEquals(expectedItems, resultList)
    }

    @Test
    fun `should remove the item to the list if it already exist`() {
        val position = 0
        val item = items[position]
        val expectedItems = items.toMutableSet().apply {
            remove(item)
        }

        val resultList = items.toSet().addOrRemove(item)

        assertEquals(expectedItems, resultList)
    }

    @Test
    fun `should not add the item to the list if it is empty`() {
        val item = ""

        val resultList = items.toSet().addOrRemove(item)

        assertEquals(items.toSet(), resultList)
    }

    @Test
    fun `should not add the item to the list if it is blank`() {
        val item = " "

        val resultList = items.toSet().addOrRemove(item)

        assertEquals(items.toSet(), resultList)
    }
}