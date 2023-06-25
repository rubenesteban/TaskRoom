/*
 * Copyright (C) 2023 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.example.inventory

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.example.inventory.data.InventoryDatabase
import com.example.inventory.data.Task
import com.example.inventory.data.taskDao
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class taskDaoTest {

    private lateinit var taskDao: taskDao
    private lateinit var inventoryDatabase: InventoryDatabase
    private val Task1 = Task(1, "Apples", 10.0, 20)
    private val Task2 = Task(2, "Bananas", 15.0, 97)

    @Before
    fun createDb() {
        val context: Context = ApplicationProvider.getApplicationContext()
        // Using an in-memory database because the information stored here disappears when the
        // process is killed.
        inventoryDatabase = Room.inMemoryDatabaseBuilder(context, InventoryDatabase::class.java)
            // Allowing main thread queries, just for testing.
            .allowMainThreadQueries()
            .build()
        taskDao = inventoryDatabase.taskDao()
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        inventoryDatabase.close()
    }

    @Test
    @Throws(Exception::class)
    fun daoInsert_insertsTaskIntoDB() = runBlocking {
        addOneTaskToDb()
        val allTasks = taskDao.getAllTasks().first()
        assertEquals(allTasks[0], Task1)
    }

    @Test
    @Throws(Exception::class)
    fun daoGetAllTasks_returnsAllTasksFromDB() = runBlocking {
        addTwoTasksToDb()
        val allTasks = taskDao.getAllTasks().first()
        assertEquals(allTasks[0], Task1)
        assertEquals(allTasks[1], Task2)
    }


    @Test
    @Throws(Exception::class)
    fun daoGetTask_returnsTaskFromDB() = runBlocking {
        addOneTaskToDb()
        val Task = taskDao.getTask(1)
        assertEquals(Task.first(), Task1)
    }

    @Test
    @Throws(Exception::class)
    fun daoDeleteTasks_deletesAllTasksFromDB() = runBlocking {
        addTwoTasksToDb()
        taskDao.delete(Task1)
        taskDao.delete(Task2)
        val allTasks = taskDao.getAllTasks().first()
        assertTrue(allTasks.isEmpty())
    }

    @Test
    @Throws(Exception::class)
    fun daoUpdateTasks_updatesTasksInDB() = runBlocking {
        addTwoTasksToDb()
        taskDao.update(Task(1, "Apples", 15.0, 25))
        taskDao.update(Task(2, "Bananas", 5.0, 50))

        val allTasks = taskDao.getAllTasks().first()
        assertEquals(allTasks[0], Task(1, "Apples", 15.0, 25))
        assertEquals(allTasks[1], Task(2, "Bananas", 5.0, 50))
    }

    private suspend fun addOneTaskToDb() {
        taskDao.insert(Task1)
    }

    private suspend fun addTwoTasksToDb() {
        taskDao.insert(Task1)
        taskDao.insert(Task2)
    }
}
