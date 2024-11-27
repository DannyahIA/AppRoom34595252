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

package br.edu.up.rgm34595252.ui.item

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import br.edu.up.rgm34595252.data.Item
import br.edu.up.rgm34595252.data.ItemsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * ViewModel to retrieve, update and delete an item from the [ItemsRepository]'s data source.
 */
class ItemDetailsViewModel(
    savedStateHandle: SavedStateHandle,
    private val itemsRepository: ItemsRepository
) : ViewModel() {

    private val itemId: Int = checkNotNull(savedStateHandle[ItemDetailsDestination.itemIdArg])

    val uiState: StateFlow<ItemDetailsUiState> =
        itemsRepository.getItemStream(itemId)
            .filterNotNull() // Ignorar valores nulos
            .map { item ->
                ItemDetailsUiState(
                    outOfStock = item.quantity == 0,
                    itemDetails = ItemDetails(
                        id = item.id,
                        name = item.name,
                        price = item.formatedPrice(),
                        quantity = item.quantity.toString()
                    )
                )
            }.stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5000),
                initialValue = ItemDetailsUiState()
            )

    suspend fun deleteItem() {
        itemsRepository.deleteItem(Item(itemId, "", 0.0, 0)) // Dados genéricos para excluir pelo ID
    }

    suspend fun sellItem() {
        val currentItem = itemsRepository.getItemStream(itemId).firstOrNull() ?: return
        if (currentItem.quantity > 0) {
            itemsRepository.updateItem(currentItem.copy(quantity = currentItem.quantity - 1))
        }
    }
}

/**
 * UI state for ItemDetailsScreen
 */
data class ItemDetailsUiState(
    val outOfStock: Boolean = true,
    val itemDetails: ItemDetails = ItemDetails()
)

