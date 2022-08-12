package com.yandex.div.core.view2.items

import android.view.View
import androidx.annotation.VisibleForTesting
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView
import com.yandex.div.core.util.KAssert
import com.yandex.div.core.view.layout.TabsLayout
import com.yandex.div.core.view2.divs.widgets.DivPagerView
import com.yandex.div.core.view2.divs.widgets.DivRecyclerView
import com.yandex.div.core.view2.divs.widgets.DivSnappyRecyclerView

/**
 * Abstract view having items.
 */
internal sealed class DivViewWithItems {

    /**
     * Current item.
     */
    abstract var currentItem: Int

    /**
     * Total item count.
     */
    abstract val itemCount: Int

    /**
     * Implementation of [DivViewWithItems] specific for div gallery with `scroll_mode` "paging"
     */
    internal class PagingGallery(
        private val view: DivSnappyRecyclerView,
        private val direction: Direction
    ) : DivViewWithItems() {
        override var currentItem: Int
            get() = view.currentItem(direction)
            set(value) = checkItem(value, itemCount) { view.smoothScrollToPosition(value) }

        override val itemCount: Int
            get() = view.itemCount
    }

    /**
     * Implementation of [DivViewWithItems] specific for div gallery with `scroll_mode` "default".
     */
    internal class Gallery(private val view: DivRecyclerView, private val direction: Direction) : DivViewWithItems() {
        override var currentItem: Int
            get() = view.currentItem(direction)
            set(value) {
                checkItem(value, itemCount) {
                    val smoothScroller = object : LinearSmoothScroller(view.context) {
                        override fun getHorizontalSnapPreference() = SNAP_TO_START
                        override fun getVerticalSnapPreference() = SNAP_TO_START
                    }
                    smoothScroller.targetPosition = value
                    view.layoutManager?.startSmoothScroll(smoothScroller)
                }
            }

        override val itemCount: Int
            get() = view.itemCount
    }

    /**
     * Implementation of [DivViewWithItems] specific for div pager.
     */
    internal class Pager(private val view: DivPagerView) : DivViewWithItems() {
        override var currentItem: Int
            get() = view.viewPager.currentItem
            set(value) = checkItem(value, itemCount) { view.viewPager.setCurrentItem(value, true) }

        override val itemCount: Int
            get() = view.viewPager.adapter?.itemCount ?: 0
    }

    /**
     * Implementation of [DivViewWithItems] specific for div tabs.
     */
    internal class Tabs(private val view: TabsLayout) : DivViewWithItems() {
        override var currentItem: Int
            get() = view.viewPager.currentItem
            set(value) = checkItem(value, itemCount) { view.viewPager.setCurrentItem(value, true) }

        override val itemCount: Int
            get() = view.viewPager.adapter?.count ?: 0
    }

    companion object {
        @set:VisibleForTesting(otherwise = VisibleForTesting.NONE)
        internal var viewForTests: DivViewWithItems? = null

        internal inline fun create(view: View, direction: () -> Direction): DivViewWithItems? {
            return viewForTests ?: when (view) {
                is DivSnappyRecyclerView -> PagingGallery(view, direction())
                is DivRecyclerView -> Gallery(view, direction())
                is DivPagerView -> Pager(view)
                is TabsLayout -> Tabs(view)
                else -> null
            }
        }
    }
}

/**
 * Direction of items navigation.
 */
internal enum class Direction {
    NEXT, PREVIOUS
}

private val RecyclerView.itemCount: Int
    get() = layoutManager?.itemCount ?: 0

private fun <T : RecyclerView> T.currentItem(direction: Direction): Int {
    return completelyVisibleItemPosition(direction).ifNoPosition {
        linearLayoutManager?.visibleItemPosition(direction) ?: RecyclerView.NO_POSITION
    }
}

private fun <T : RecyclerView> T.completelyVisibleItemPosition(direction: Direction): Int {
    val llm = linearLayoutManager ?: return RecyclerView.NO_POSITION
    return when (direction) {
        Direction.PREVIOUS -> llm.findFirstCompletelyVisibleItemPosition()
        Direction.NEXT -> if (canScroll()) {
            llm.findFirstCompletelyVisibleItemPosition()
        } else {
            llm.findLastCompletelyVisibleItemPosition()
        }
    }
}

private fun <T : RecyclerView> T.canScroll(): Boolean {
    return when (linearLayoutManager?.orientation) {
        RecyclerView.HORIZONTAL -> canScrollHorizontally(1)
        RecyclerView.VERTICAL -> canScrollVertically(1)
        else -> false
    }
}

private fun LinearLayoutManager.visibleItemPosition(direction: Direction): Int {
    return when (direction) {
        Direction.NEXT -> findFirstVisibleItemPosition()
        Direction.PREVIOUS -> findLastVisibleItemPosition()
    }
}

private val <T : RecyclerView> T.linearLayoutManager: LinearLayoutManager?
    get() = layoutManager as? LinearLayoutManager

private inline fun Int.ifNoPosition(fallback: () -> Int): Int {
    return takeIf { it != RecyclerView.NO_POSITION } ?: fallback()
}

private inline fun checkItem(item: Int, itemCount: Int, block: () -> Unit) {
    if (0 > item || item >= itemCount) {
        KAssert.fail { "$item is not in range [0, $itemCount)" }
    } else {
        block()
    }
}
