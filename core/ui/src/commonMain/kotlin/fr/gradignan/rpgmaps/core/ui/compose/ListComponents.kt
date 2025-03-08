package fr.gradignan.rpgmaps.core.ui.compose

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.rounded.DragHandle
import androidx.compose.material3.Card
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.Text
import androidx.compose.material3.minimumInteractiveComponentSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import sh.calvin.reorderable.ReorderableItem
import sh.calvin.reorderable.rememberReorderableLazyListState


@Composable
fun ReorderableList(
    hasFullControl: Boolean,
    items: List<Item>,
    onOrderChange: (List<Int>) -> Unit,
    onDelete: (Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val lazyListState = rememberLazyListState()
    var list by remember(items) {
        mutableStateOf(items)
    }
    val reorderableLazyColumnState = rememberReorderableLazyListState(lazyListState) { from, to ->
        list = list.toMutableList().apply {
            add(to.index, removeAt(from.index))
        }
    }

    LazyColumn(
        modifier = modifier.fillMaxSize()
            .border(1.dp, MaterialTheme.colorScheme.onSurface, MaterialTheme.shapes.small),
        state = lazyListState,
        contentPadding = PaddingValues(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        itemsIndexed(list, key = { _, item -> item.index }) { _, item ->
            ReorderableItem(reorderableLazyColumnState, item.index) {
                Card(
                    onClick = {},
                    modifier = Modifier.fillMaxSize()
                        .then(if (hasFullControl) Modifier.draggableHandle(
                            onDragStopped = {
                                if (list != items) {
                                    onOrderChange(list.map { it.index })
                                }
                            }
                        ) else Modifier
                        )

                ){
                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier.minimumInteractiveComponentSize(),
                    ) {
                        if (hasFullControl) {
                            Icon(
                                imageVector = Icons.Rounded.DragHandle,
                                contentDescription = "Reorder",
                            )
                        }
                        val label = if (item.optionalId == null) item.name else "${item.name} - ${item.optionalId}"
                        Text(label, Modifier.weight(1f))
                        if (hasFullControl) {
                            IconButton(
                                onClick = { onDelete(item.index) }
                            ) {
                                Icon(Icons.Filled.Close, contentDescription = "Delete")
                            }
                        }
                    }
                }
            }
        }
    }
}
