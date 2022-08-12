package com.yandex.div.view

import android.graphics.Rect
import android.view.View
import androidx.annotation.Px
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.recyclerview.widget.StaggeredGridLayoutManager
import com.yandex.div.core.util.KAssert

class PaddingItemDecoration @JvmOverloads constructor(
    @param:Px @field:Px private val paddingLeft: Int = 0,
    @param:Px @field:Px private val midItemPadding: Int = 0,
    @param:Px @field:Px private val paddingRight: Int = 0,
    @param:Px @field:Px private val paddingTop: Int = 0,
    @param:Px @field:Px private val paddingBottom: Int = 0,
    private val orientation: Int = RecyclerView.HORIZONTAL
) : RecyclerView.ItemDecoration() {

    override fun getItemOffsets(outRect: Rect, view: View, parent: RecyclerView, state: RecyclerView.State) {
        val spanCount = when (val layoutManager = parent.layoutManager) {
            is StaggeredGridLayoutManager -> layoutManager.spanCount
            is LinearLayoutManager -> 1
            else -> {
                KAssert.fail { "Unsupported layoutManger: $layoutManager" }

                1
            }
        }

        if (spanCount == 1) {
            val itemCount = parent.adapter?.itemCount ?: return
            val position = parent.layoutManager?.getPosition(view) ?: return

            val isFirst = position == 0
            val isLast = position == itemCount - 1

            when (orientation) {
                RecyclerView.HORIZONTAL -> outRect.set(
                    if (isFirst) paddingLeft else 0,
                    paddingTop,
                    if (isLast) paddingRight else midItemPadding,
                    paddingBottom
                )
                RecyclerView.VERTICAL -> outRect.set(
                    paddingLeft,
                    if (isFirst) paddingTop else 0,
                    paddingRight,
                    if (isLast) paddingBottom else midItemPadding
                )
                else -> KAssert.fail { "Unsupported orientation: $orientation" }
            }
        } else {
            val halfMidItemPadding = midItemPadding / 2

            outRect.set(
                halfMidItemPadding,
                halfMidItemPadding,
                halfMidItemPadding,
                halfMidItemPadding
            )
        }
    }
}
