package com.yandex.div.view

import android.content.Context
import android.util.AttributeSet
import android.view.View
import androidx.core.view.doOnNextLayout
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.yandex.div.core.util.Assert

// From: http://stackoverflow.com/a/37816976
open class SnappyRecyclerView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : RecyclerView(context, attrs, defStyleAttr) {

    val savedItemPosition: Int
        get() = _savedItemPosition
    private var _savedItemPosition = DEFAULT_ITEM_POSITION
    private val itemCount: Int
        get() = adapter?.itemCount ?: 0
    public var orientation = HORIZONTAL


    override fun fling(velocityX: Int, velocityY: Int): Boolean {
        Assert.assertTrue(layoutManager is androidx.recyclerview.widget.LinearLayoutManager)

        val linearLayoutManager = layoutManager as androidx.recyclerview.widget.LinearLayoutManager

        // views on the screen
        var lastVisibleItemPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition()
        if (lastVisibleItemPosition == NO_POSITION) {
            lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition()
        }
        val lastView = linearLayoutManager.findViewByPosition(lastVisibleItemPosition)
        var firstVisibleItemPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
        if (firstVisibleItemPosition == NO_POSITION) {
            firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition()
        }
        val firstView = linearLayoutManager.findViewByPosition(firstVisibleItemPosition)

        if (firstView == null || lastView == null) {
            return false
        }

        val screenSize = choosePropertyDependOnOrientation(width, height)
        val params = ScrollParams(screenSize, firstView, lastView)

        var delta: Int
        if (Math.abs(choosePropertyDependOnOrientation(velocityX, velocityY)) < FLING_VELOCITY_FOR_PAGE_CHANGE) {
            // The fling is slow -> stay at the current page if we are less than half through,
            // or go to the next page if more than half through
            if (params.startEdge > screenSize / 2) {
                // go to left page
                delta = params.distanceEnd
            } else if (params.endEdge < screenSize / 2) {
                // go to right page
                delta = params.distanceStart
            } else {
                // stay at current page
                if (isRightSwipe(velocityX)) {
                    delta = params.distanceEnd
                } else {
                    delta = params.distanceStart
                }
            }
        } else {
            // The fling is fast -> go to next page
            if (isRightSwipe(velocityX)) {
                delta = params.distanceStart
            } else {
                delta = params.distanceEnd
            }
        }
        if (delta == 0) {
            /* Hack for first and last items.
            If we scroll last item to the right side, there is a scroll with 0px, as result there isn't SCROLL_STATE_IDLE. But our
            onScrollStateChanges listeners work only with SCROLL_STATE_IDLE state (this state creates after scroll is finished).
            We make fictive scroll with 1px to bound of RecyclerView. In fact RecyclerView will not be scrolled (because hasn't scroll out of bounds),
            but we will have full correct scroll with SCROLL_STATE_IDLE. */
            if (isRightSwipe(velocityX)) {
                delta = 1
            } else {
                delta = -1
            }
        }
        if (orientation == HORIZONTAL) {
            smoothScrollBy(delta, 0)
        } else {
            smoothScrollBy(0, delta)
        }
        return true
    }

    override fun onScrollStateChanged(state: Int) {
        super.onScrollStateChanged(state)

        // If you tap on the phone while the RecyclerView is scrolling it will stop in the middle.
        // This code fixes this. This code is not strictly necessary but it improves the behaviour.

        if (state == SCROLL_STATE_IDLE) {
            val linearLayoutManager = layoutManager as androidx.recyclerview.widget.LinearLayoutManager
            // views on the screen
            var firstVisibleItemPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
            if (firstVisibleItemPosition == 0) {
                return
            }
            var lastVisibleItemPosition = linearLayoutManager.findLastCompletelyVisibleItemPosition()
            if (lastVisibleItemPosition == itemCount - 1) {
                return
            }

            if (firstVisibleItemPosition == NO_POSITION) {
                firstVisibleItemPosition = linearLayoutManager.findFirstVisibleItemPosition()
            }
            if (lastVisibleItemPosition == NO_POSITION) {
                lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition()
            }
            val firstView = linearLayoutManager.findViewByPosition(firstVisibleItemPosition)
            val lastView = linearLayoutManager.findViewByPosition(lastVisibleItemPosition)

            if (firstView == null || lastView == null) {
                return
            }

            val screenSize = choosePropertyDependOnOrientation(width, height)
            val params = ScrollParams(screenSize, firstView, lastView)

            if (firstVisibleItemPosition == lastVisibleItemPosition) {
                // Special case: there is no difference between distanceStart and distanceEnd.
                // We just need to perform smoothScrollBy().
                if (orientation == HORIZONTAL) {
                    smoothScrollBy(params.distanceStart, 0)
                } else {
                    smoothScrollBy(0, params.distanceStart)
                }
            } else if (params.startEdge > screenSize / 4) {
                if (orientation == HORIZONTAL) {
                    smoothScrollBy(params.distanceEnd, 0)
                } else {
                    smoothScrollBy(0, params.distanceEnd)
                }
            } else if (params.endEdge < screenSize / 4) {
                if (orientation == HORIZONTAL) {
                    smoothScrollBy(params.distanceStart, 0)
                } else {
                    smoothScrollBy(0, params.distanceStart)
                }
            }
        }
    }

    override fun scrollToPosition(position: Int) {
        if (isLayoutFrozen) {
            return
        }
        stopScroll()

        if (layoutManager == null) {
            return
        }

        if (layoutManager !is LinearLayoutManager) {
            layoutManager!!.scrollToPosition(position)
            awakenScrollBars()
            return
        }

        if (itemCount <= 0) {
            return
        }

        val linearLayoutManager = layoutManager as LinearLayoutManager
        var firstViewPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
        if (firstViewPosition == NO_POSITION) {
            firstViewPosition = linearLayoutManager.findFirstVisibleItemPosition()
        }

        val firstView = linearLayoutManager.findViewByPosition(firstViewPosition)
        if (firstView == null) {
            linearLayoutManager.scrollToPositionWithOffset(position, 0)
            doOnNextLayout { post { scrollToPosition(position) } }
            return
        }

        val screenSize = choosePropertyDependOnOrientation(width, height)
        val viewSize = choosePropertyDependOnOrientation(firstView.width, firstView.height)

        linearLayoutManager.scrollToPositionWithOffset(position, (screenSize - viewSize) / 2)

        awakenScrollBars()
    }

    @JvmOverloads
    fun findSelectedItemPosition(direction: Int = DIRECTION_NONE): Int {
        val linearLayoutManager = layoutManager as androidx.recyclerview.widget.LinearLayoutManager

        val firstCompletelyVisibleItemPosition = linearLayoutManager.findFirstCompletelyVisibleItemPosition()
        if (firstCompletelyVisibleItemPosition != NO_POSITION) {
            return firstCompletelyVisibleItemPosition
        }
        val lastVisibleItemPosition = linearLayoutManager.findLastVisibleItemPosition()
        //workaround for first/last position
        if (lastVisibleItemPosition == linearLayoutManager.findFirstVisibleItemPosition()) {
            return if (lastVisibleItemPosition != NO_POSITION) lastVisibleItemPosition else DEFAULT_ITEM_POSITION
        }
        //have 2 items on screen and choose by direction
        return if (direction >= 0) {
            lastVisibleItemPosition
        } else {
            lastVisibleItemPosition - 1
        }
    }

    fun savePosition(position: Int) {
        _savedItemPosition = position
    }

    private fun<T> choosePropertyDependOnOrientation(horizontal: T, vertical: T) = if (orientation == HORIZONTAL) horizontal else vertical

    inner class ScrollParams internal constructor(screenSize: Int, firstView: View, lastView: View) {

        val distanceStart: Int
        val distanceEnd: Int
        val startEdge: Int
        val endEdge: Int

        init {
            val startMargin = (screenSize - choosePropertyDependOnOrientation(lastView.width, lastView.height)) / 2
            val endMargin = (screenSize - choosePropertyDependOnOrientation(firstView.width, firstView.height)) / 2 +
                    choosePropertyDependOnOrientation(firstView.width, firstView.height)
            startEdge = choosePropertyDependOnOrientation(lastView.left, lastView.top)
            endEdge = choosePropertyDependOnOrientation(firstView.right, firstView.bottom)
            distanceStart = startEdge - startMargin
            distanceEnd = endEdge - endMargin
        }
    }

    companion object {

        // Use it with a horizontal LinearLayoutManager
        // Based on http://stackoverflow.com/a/29171652/4034572

        private const val FLING_VELOCITY_FOR_PAGE_CHANGE = 1000
        private const val DEFAULT_ITEM_POSITION = 0
        private const val DIRECTION_NONE = 0

        private fun isRightSwipe(velocityX: Int): Boolean {
            return velocityX > 0
        }
    }
}
