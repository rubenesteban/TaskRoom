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

package com.example.inventory.ui.Task

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import com.example.inventory.data.Task
import com.example.inventory.data.TasksRepository
import java.text.NumberFormat

/**
 * View Model to validate and insert Tasks in the Room database.
 */
class TaskEntryViewModel(private val tasksRepository: TasksRepository) : ViewModel() {

    /**
     * Holds current Task ui state
     */
    var taskUiState by mutableStateOf(TaskUiState())
        private set

    /**
     * Updates the [TaskUiState] with the value provided in the argument. This method also triggers
     * a validation for input values.
     */
    fun updateUiState(taskDetails: TaskDetails) {
        taskUiState =
            TaskUiState(taskDetails = taskDetails, isEntryValid = validateInput(taskDetails))
    }

    /**
     * Inserts an [Task] in the Room database
     */
    suspend fun saveTask() {
        if (validateInput()) {
            tasksRepository.insertTask(taskUiState.taskDetails.toTask())
        }
    }

    private fun validateInput(uiState: TaskDetails = taskUiState.taskDetails): Boolean {
        return with(uiState) {
            name.isNotBlank() && price.isNotBlank() && quantity.isNotBlank() && key.isNotBlank() && prico.isNotBlank() && user.isNotBlank()&& login.isNotBlank()
        }
    }
}

/**
 * Represents Ui State for an Task.
 */
data class TaskUiState(
    val taskDetails: TaskDetails = TaskDetails(),
    val isEntryValid: Boolean = false
)

data class TaskDetails(
    val id: Int = 0,
    val name: String = "",
    val price: String = "",
    val prico: String = "",
    val key: String = "",
    val user: String = "",
    val login: String = "",
    val quantity: String = "",
)

/**
 * Extension function to convert [TaskUiState] to [Task]. If the value of [TaskDetails.price] is
 * not a valid [Double], then the price will be set to 0.0. Similarly if the value of
 * [TaskUiState] is not a valid [Int], then the quantity will be set to 0
 */
fun TaskDetails.toTask(): Task = Task(
    id = id,
    name = name,
    price = price.toDoubleOrNull() ?: 0.0,
    prico = prico.toDoubleOrNull() ?: 0.0,
    key = key,
    user = user.toIntOrNull() ?: 0,
    login = login.toIntOrNull() ?: 0,
    quantity = quantity.toIntOrNull() ?: 0
)

fun Task.formatedPrice(): String {
    return NumberFormat.getCurrencyInstance().format(price)
}



/**
 * Extension function to convert [Task] to [TaskUiState]
 */
fun Task.toTaskUiState(isEntryValid: Boolean = false): TaskUiState = TaskUiState(
    taskDetails = this.toTaskDetails(),
    isEntryValid = isEntryValid
)

/**
 * Extension function to convert [Task] to [TaskDetails]
 */
fun Task.toTaskDetails(): TaskDetails = TaskDetails(
    id = id,
    name = name,
    price = price.toString(),
    prico = prico.toString(),
    key = key,
    user = user.toString(),
    login = login.toString(),
    quantity = quantity.toString()
)
