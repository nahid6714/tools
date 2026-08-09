package com.example.util

import androidx.compose.foundation.gestures.Orientation
import androidx.compose.foundation.gestures.detectDragGesturesAfterLongPress
import androidx.compose.runtime.Stable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateMapOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.layout.boundsInWindow
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.zIndex
import androidx.compose.runtime.Composable

/**
 * Generic long-press drag & drop reorder state for both vertical (Column) and
 * horizontal (Row) item lists, Lazy or not. Each item registers its on-screen
 * bounds via [dragReorderVisuals]; while the user drags (started with a long
 * press via [dragReorderHandle]), items are swapped live the moment the
 * dragged item's center crosses another item's bounds. The caller supplies
 * [onMove] to actually reorder (and persist) the backing list.
 */
@Stable
class DragDropListState(
    val orientation: Orientation,
    private val onMove: (from: Int, to: Int) -> Unit
) {
    var draggingIndex by mutableStateOf(-1)
        private set

    var dragOffset by mutableStateOf(0f)
        private set

    private val itemBounds = mutableStateMapOf<Int, Rect>()

    fun registerItemBounds(index: Int, bounds: Rect) {
        itemBounds[index] = bounds
    }

    fun isDragging(index: Int): Boolean = draggingIndex == index

    fun offsetForItem(index: Int): Float = if (draggingIndex == index) dragOffset else 0f

    fun onDragStart(index: Int) {
        if (itemBounds.containsKey(index)) {
            draggingIndex = index
            dragOffset = 0f
        }
    }

    fun onDrag(delta: Float) {
        val current = draggingIndex
        if (current < 0) return
        dragOffset += delta

        val currentBounds = itemBounds[current] ?: return
        val draggedCenter = if (orientation == Orientation.Vertical) {
            currentBounds.top + currentBounds.height / 2f + dragOffset
        } else {
            currentBounds.left + currentBounds.width / 2f + dragOffset
        }

        val target = itemBounds.entries.firstOrNull { (idx, bounds) ->
            idx != current && if (orientation == Orientation.Vertical) {
                draggedCenter in bounds.top..bounds.bottom
            } else {
                draggedCenter in bounds.left..bounds.right
            }
        }

        if (target != null) {
            val (targetIndex, targetBounds) = target
            val shift = if (orientation == Orientation.Vertical) {
                targetBounds.top - currentBounds.top
            } else {
                targetBounds.left - currentBounds.left
            }
            onMove(current, targetIndex)
            draggingIndex = targetIndex
            dragOffset -= shift
        }
    }

    fun onDragEnd() {
        draggingIndex = -1
        dragOffset = 0f
    }
}

@Composable
fun rememberDragDropListState(
    orientation: Orientation = Orientation.Vertical,
    onMove: (from: Int, to: Int) -> Unit
): DragDropListState {
    val latestOnMove by rememberUpdatedState(onMove)
    return remember(orientation) {
        DragDropListState(orientation = orientation, onMove = { from, to -> latestOnMove(from, to) })
    }
}

/**
 * Registers this item's current on-screen bounds with [state] and applies the
 * live drag visual feedback (follows the finger, lifts with a shadow while dragging).
 */
fun Modifier.dragReorderVisuals(state: DragDropListState, index: Int): Modifier = composed {
    val currentIndex by rememberUpdatedState(index)
    this
        .onGloballyPositioned { coordinates ->
            state.registerItemBounds(currentIndex, coordinates.boundsInWindow())
        }
        .graphicsLayer {
            val offset = state.offsetForItem(currentIndex)
            if (state.orientation == Orientation.Vertical) {
                translationY = offset
            } else {
                translationX = offset
            }
            val dragging = state.isDragging(currentIndex)
            shadowElevation = if (dragging) 10f else 0f
            scaleX = if (dragging) 1.03f else 1f
            scaleY = if (dragging) 1.03f else 1f
            alpha = if (dragging) 0.96f else 1f
        }
        .zIndex(if (state.isDragging(currentIndex)) 1f else 0f)
}

/**
 * Detects the long-press-then-drag gesture that drives [state]. Apply this to
 * either the whole item (if it has no competing gestures like text input) or
 * to a small dedicated drag-handle icon.
 */
fun Modifier.dragReorderHandle(state: DragDropListState, index: Int): Modifier = composed {
    val currentIndex by rememberUpdatedState(index)
    this.pointerInput(state) {
        detectDragGesturesAfterLongPress(
            onDragStart = { state.onDragStart(currentIndex) },
            onDragEnd = { state.onDragEnd() },
            onDragCancel = { state.onDragEnd() },
            onDrag = { change, dragAmount ->
                change.consume()
                val delta = if (state.orientation == Orientation.Vertical) dragAmount.y else dragAmount.x
                state.onDrag(delta)
            }
        )
    }
}

/** Convenience: visuals + long-press drag gesture combined on the same element. */
fun Modifier.dragToReorder(state: DragDropListState, index: Int): Modifier =
    this
        .dragReorderVisuals(state, index)
        .dragReorderHandle(state, index)
