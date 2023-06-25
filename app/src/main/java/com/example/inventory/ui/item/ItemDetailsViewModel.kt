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

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.inventory.data.TasksRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

/**
 * ViewModel to retrieve, update and delete an Task from the [TasksRepository]'s data source.
 */
class TaskDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val tasksRepository: TasksRepository,
) : ViewModel() {

    private val taskId: Int = checkNotNull(savedStateHandle[TaskDetailsDestination.taskIdArg])

    /**
     * Holds the Task details ui state. The data is retrieved from [TasksRepository] and mapped to
     * the UI state.
     */
    val uiState: StateFlow<TaskDetailsUiState> =
        tasksRepository.getTaskStream(taskId)
            .filterNotNull()
            .map {
                TaskDetailsUiState(outOfStock = it.quantity <= 0, taskDetails = it.toTaskDetails())
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(TIMEOUT_MILLIS),
                initialValue = TaskDetailsUiState()
            )

    /**
     * Reduces the Task quantity by one and update the [TasksRepository]'s data source.
     */
    fun reduceQuantityByOne() {
        viewModelScope.launch {
            val currentTask = uiState.value.taskDetails.toTask()
            if (currentTask.quantity > 0) {
                tasksRepository.updateTask(currentTask.copy(quantity = currentTask.quantity - 1))
            }
        }
    }

    /**
     * Deletes the Task from the [TasksRepository]'s data source.
     */
    suspend fun deleteTask() {
        tasksRepository.deleteTask(uiState.value.taskDetails.toTask())
    }

    companion object {
        private const val TIMEOUT_MILLIS = 5_000L
    }
}

/**
 * UI state for TaskDetailsScreen
 */
data class TaskDetailsUiState(
    val outOfStock: Boolean = true,
    val taskDetails: TaskDetails = TaskDetails()
)
