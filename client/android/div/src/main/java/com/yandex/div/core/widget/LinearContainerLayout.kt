package com.yandex.div.core.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.drawable.Drawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.view.ViewGroup.LayoutParams.WRAP_CONTENT
import android.view.accessibility.AccessibilityEvent
import android.view.accessibility.AccessibilityNodeInfo
import androidx.appcompat.widget.LinearLayoutCompat
import androidx.appcompat.widget.LinearLayoutCompat.HORIZONTAL
import androidx.appcompat.widget.LinearLayoutCompat.VERTICAL
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import com.yandex.div.internal.widget.DivLayoutParams
import com.yandex.div.internal.widget.DivViewGroup
import kotlin.math.max
import kotlin.math.roundToInt

/** Class name may be obfuscated by Proguard. Hardcode the string for accessibility usage.  */
private const val ACCESSIBILITY_CLASS_NAME = "android.widget.LinearLayout"

@Suppress("unused", "MemberVisibilityCanBePrivate")
internal open class LinearContainerLayout @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : DivViewGroup(context, attrs, defStyleAttr), AspectView {

    /**
     * Maximally ascented child that is baseline-aligned.
     * This is used because we don't know child's top pre-layout.
     */
    private var maxBaselinedAscent = -1
    private var maxBaselinedDescent = -1

    @LinearLayoutCompat.OrientationMode
    var orientation = HORIZONTAL
        set(value) {
            if (field != value) {
                field = value
                requestLayout()
            }
        }

    private var _gravity = GravityCompat.START or Gravity.TOP

    /**
     * Describes how the child views are positioned. Defaults to GRAVITY_TOP. If
     * this layout has a VERTICAL orientation, this controls where all the child
     * views are placed if there is extra vertical space. If this layout has a
     * HORIZONTAL orientation, this controls the alignment of the children.
     */
    var gravity: Int
        get() = _gravity
        set(value) {
            if (_gravity == value) return

            var newGravity = value
            if ((newGravity and GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK) == 0) {
                newGravity = newGravity or GravityCompat.START
            }
            if ((newGravity and Gravity.VERTICAL_GRAVITY_MASK) == 0) {
                newGravity = newGravity or Gravity.TOP
            }
            _gravity = newGravity
            requestLayout()
        }

    private var totalLength = 0

    override var aspectRatio by dimensionAffecting(AspectView.DEFAULT_ASPECT_RATIO) { value ->
        value.coerceAtLeast(AspectView.DEFAULT_ASPECT_RATIO)
    }

    private var dividerWidth = 0
    private var dividerHeight = 0

    var dividerDrawable: Drawable? = null
        set(value) {
            if (field == value) return
            field = value
            dividerWidth = value?.intrinsicWidth ?: 0
            dividerHeight = value?.intrinsicHeight ?: 0
            setWillNotDraw(value == null)
            requestLayout()
        }

    @ShowSeparatorsMode
    var showDividers: Int = 0
        set(value) {
            if (field == value) return
            field = value
            requestLayout()
        }

    var dividerPadding = 0

    private val constrainedChildren = mutableListOf<View>()

    override fun shouldDelayChildPressedState(): Boolean {
        return false
    }

    override fun onDraw(canvas: Canvas) {
        dividerDrawable ?: return
        if (isVertical) {
            drawDividersVertical(canvas)
        } else {
            drawDividersHorizontal(canvas)
        }
    }

    private fun drawDividersVertical(canvas: Canvas) {
        forEachIndexed(significantOnly = true) { child, i ->
            if (hasDividerBeforeChildAt(i)) {
                val top = child.top - child.lp.topMargin - dividerHeight
                drawHorizontalDivider(canvas, top)
            }
        }
        if (hasDividerBeforeChildAt(childCount)) {
            val bottom = getChildAt(childCount - 1)?.let {
                it.bottom + it.lp.bottomMargin
            } ?: (height - paddingBottom - dividerHeight)
            drawHorizontalDivider(canvas, bottom)
        }
    }

    private fun drawDividersHorizontal(canvas: Canvas) {
        val isLayoutRtl = isLayoutRtl()
        forEachIndexed(significantOnly = true) { child, i ->
            if (hasDividerBeforeChildAt(i)) {
                val position = if (isLayoutRtl) {
                    child.right + child.lp.rightMargin
                } else {
                    child.left - child.lp.leftMargin - dividerWidth
                }
                drawVerticalDivider(canvas, position)
            }
        }

        if (hasDividerBeforeChildAt(childCount)) {
            val child = getChildAt(childCount - 1)
            val position = when {
                child == null && isLayoutRtl -> paddingLeft
                child == null -> width - paddingRight - dividerWidth
                isLayoutRtl -> child.left - child.lp.leftMargin - dividerWidth
                else -> child.right + child.lp.rightMargin
            }
            drawVerticalDivider(canvas, position)
        }
    }

    private fun drawHorizontalDivider(canvas: Canvas, top: Int) = drawDivider(
        canvas,
        paddingLeft + dividerPadding,
        top,
        width - paddingRight - dividerPadding,
        top + dividerHeight
    )

    private fun drawVerticalDivider(canvas: Canvas, left: Int) = drawDivider(
        canvas,
        left,
        paddingTop + dividerPadding,
        left + dividerWidth,
        height - paddingBottom - dividerPadding
    )

    private fun drawDivider(canvas: Canvas, left: Int, top: Int, right: Int, bottom: Int) =
        dividerDrawable?.run {
            val centerHorizontal = (left + right) / 2f
            val centerVertical = (top + bottom) / 2f
            val halfWidth = dividerWidth / 2f
            val halfHeight = dividerHeight / 2f
            setBounds((centerHorizontal - halfWidth).toInt(), (centerVertical - halfHeight).toInt(),
                (centerHorizontal + halfWidth).toInt(), (centerVertical + halfHeight).toInt())
            draw(canvas)
        }

    override fun getBaseline(): Int {
        if (isVertical) {
            val child = getChildAt(0) ?: return super.getBaseline()
            return child.baseline + child.lp.topMargin + paddingTop
        }
        if (maxBaselinedAscent != -1) {
            return maxBaselinedAscent + paddingTop
        }
        return super.getBaseline()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        if (isVertical) {
            measureVertical(widthMeasureSpec, heightMeasureSpec)
        } else {
            measureHorizontal(widthMeasureSpec, heightMeasureSpec)
        }
        constrainedChildren.clear()
    }

    private fun hasDividerBeforeChildAt(childIndex: Int): Boolean {
        return when {
            childIndex == 0 -> showDividers and ShowSeparatorsMode.SHOW_AT_START != 0
            childIndex == childCount -> showDividers and ShowSeparatorsMode.SHOW_AT_END != 0
            showDividers and ShowSeparatorsMode.SHOW_BETWEEN != 0 -> {
                for (i in childIndex - 1 downTo 0) {
                    if (getChildAt(i).visibility != GONE) {
                        return true
                    }
                }
                false
            }
            else -> false
        }
    }

    /**
     * Measures the children when the orientation of this LinearLayout is set
     * to [VERTICAL].
     *
     * @param widthMeasureSpec Horizontal space requirements as imposed by the parent.
     * @param heightMeasureSpec Vertical space requirements as imposed by the parent.
     *
     * @see orientation
     * @see onMeasure
     */
    private fun measureVertical(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        totalLength = 0
        var maxWidth = 0
        var childState = 0
        var alternativeMaxWidth = 0
        var weightedMaxWidth = 0
        var allFillParent = true
        var totalWeight = 0f
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var matchWidth = false
        var skippedMeasure = false
        var totalConstrainedHeight = 0

        // See how tall everyone is. Also remember max width.
        forEachIndexed(significantOnly = true) { child, i ->
            if (hasDividerBeforeChildAt(i)) {
                totalLength += dividerHeight
            }
            val lp = child.lp
            totalWeight += lp.fixedVerticalWeight
            if (heightMode == MeasureSpec.EXACTLY && lp.height == MATCH_PARENT) {
                // Optimization: don't bother measuring children who are going to use
                // leftover space. These views will get measured again down below if
                // there is any leftover space.
                totalLength = max(totalLength, totalLength + lp.topMargin + lp.bottomMargin)
                skippedMeasure = true
            } else {
                val oldHeight = lp.height
                if (oldHeight == MATCH_PARENT || oldHeight == DivLayoutParams.WRAP_CONTENT_CONSTRAINED) {
                    lp.height = WRAP_CONTENT
                }

                // Determine how big this child would like to be. If this or
                // previous children have given a weight, then we allow it to
                // use all available space (and we will shrink things later
                // if needed).
                measureChildWithMargins(child, widthMeasureSpec, 0, heightMeasureSpec,
                    if (totalWeight == 0f) totalLength else 0)
                lp.height = oldHeight
                val childHeight = child.measuredHeight + lp.topMargin + lp.bottomMargin
                totalLength = max(totalLength, totalLength + childHeight)
                if (lp.height == DivLayoutParams.WRAP_CONTENT_CONSTRAINED) {
                    constrainedChildren.add(child)
                    totalConstrainedHeight = max(totalConstrainedHeight, totalConstrainedHeight + childHeight)
                }
            }
            var matchWidthLocally = false
            if (widthMode != MeasureSpec.EXACTLY && lp.width == MATCH_PARENT) {
                // The width of the linear layout will scale, and at least one
                // child said it wanted to match our width. Set a flag
                // indicating that we need to remeasure at least that view when
                // we know our width.
                matchWidth = true
                matchWidthLocally = true
            }
            val margin = lp.leftMargin + lp.rightMargin
            val measuredWidth = child.measuredWidth + margin
            maxWidth = max(maxWidth, measuredWidth)
            childState = combineMeasuredStates(childState, child.measuredState)
            allFillParent = allFillParent && lp.width == MATCH_PARENT
            if (lp.fixedVerticalWeight > 0) {
                /*
                 * Widths of weighted Views are bogus if we end up
                 * remeasuring, so keep them separate.
                 */
                weightedMaxWidth = max(weightedMaxWidth,
                    if (matchWidthLocally) margin else measuredWidth)
            } else {
                alternativeMaxWidth = max(alternativeMaxWidth,
                    if (matchWidthLocally) margin else measuredWidth)
            }
        }
        if (totalLength > 0 && hasDividerBeforeChildAt(childCount)) {
            totalLength += dividerHeight
        }

        // Add in our padding
        totalLength += paddingTop + paddingBottom
        var heightSize = totalLength

        // Check against our minimum height
        heightSize = max(heightSize, suggestedMinimumHeight)

        // Reconcile our calculated size with the heightMeasureSpec
        val heightSizeAndState = resolveSizeAndState(heightSize, heightMeasureSpec, 0)
        heightSize = heightSizeAndState and MEASURED_SIZE_MASK

        // Either expand children with weight to take up available space or
        // shrink them if they extend beyond our current bounds. If we skipped
        // measurement on any children, we need to measure them now.
        var delta = heightSize - totalLength
        if (skippedMeasure || delta != 0 && (totalWeight > 0.0f || totalConstrainedHeight > 0)) {
            var weightSum = totalWeight
            totalLength = 0

            if (delta < 0) {
                constrainedChildren.sortByDescending { it.minimumHeight / it.measuredHeight.toFloat() }
                constrainedChildren.forEach { child ->
                    val lp = child.lp
                    val oldHeight = child.measuredHeight
                    val oldHeightWithMargins = oldHeight + lp.topMargin + lp.bottomMargin
                    val share = (oldHeightWithMargins / totalConstrainedHeight.toFloat() * delta).roundToInt()
                    val childHeight = (oldHeight + share).coerceAtLeast(child.minimumHeight)

                    childState = remeasureChildVertical(child, widthMeasureSpec, childHeight, childState)

                    totalConstrainedHeight -= oldHeightWithMargins
                    delta -= child.measuredHeight - oldHeight
                }
            }

            forEach(significantOnly = true) { child ->
                val lp = child.lp
                val childExtra = lp.fixedVerticalWeight
                if (childExtra > 0) {
                    val share = (childExtra * delta / weightSum).toInt()
                    weightSum -= childExtra
                    delta -= share
                    val childHeight = if (lp.height != MATCH_PARENT || heightMode != MeasureSpec.EXACTLY) {
                        child.measuredHeight + share
                    } else {
                        share
                    }.coerceAtLeast(0)

                    // Child may now not fit in vertical dimension.
                    childState = remeasureChildVertical(child, widthMeasureSpec, childHeight, childState)
                }
                val margin = lp.leftMargin + lp.rightMargin
                val measuredWidth = child.measuredWidth + margin
                maxWidth = max(maxWidth, measuredWidth)
                val matchWidthLocally = widthMode != MeasureSpec.EXACTLY && lp.width == MATCH_PARENT
                alternativeMaxWidth = max(alternativeMaxWidth,
                    if (matchWidthLocally) margin else measuredWidth)
                allFillParent = allFillParent && lp.width == MATCH_PARENT
                totalLength = max(totalLength,
                    totalLength + child.measuredHeight + lp.topMargin + lp.bottomMargin)
            }

            // Add in our padding
            totalLength += paddingTop + paddingBottom
        } else {
            alternativeMaxWidth = max(alternativeMaxWidth, weightedMaxWidth)
        }
        if (!allFillParent && widthMode != MeasureSpec.EXACTLY) {
            maxWidth = alternativeMaxWidth
        }
        maxWidth += paddingLeft + paddingRight

        // Check against our minimum width
        maxWidth = max(maxWidth, suggestedMinimumWidth)
        setMeasuredDimension(resolveSizeAndState(maxWidth, widthMeasureSpec, childState),
            heightSizeAndState)
        if (matchWidth) {
            forceUniformWidth(heightMeasureSpec)
        }
    }

    private fun remeasureChildVertical(
        child: View,
        widthMeasureSpec: Int,
        height: Int,
        childState: Int
    ): Int {
        val lp = child.lp
        val childWidthMeasureSpec = getChildMeasureSpec(
            widthMeasureSpec,
            paddingLeft + paddingRight + lp.leftMargin + lp.rightMargin,
            lp.width,
            child.minimumWidth,
            lp.maxWidth
        )

        child.measure(childWidthMeasureSpec, MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY))
        return combineMeasuredStates(childState, child.measuredState and
            ((MEASURED_STATE_MASK shr MEASURED_HEIGHT_STATE_SHIFT)))
    }

    private fun forceUniformWidth(heightMeasureSpec: Int) {
        // Pretend that the linear layout has an exact size.
        val uniformMeasureSpec = MeasureSpec.makeMeasureSpec(measuredWidth, MeasureSpec.EXACTLY)
        forEach(significantOnly = true) { child ->
            val lp = child.lp
            if (lp.width != MATCH_PARENT) return@forEach

            // Temporarily force children to reuse their old measured height
            // FIXME: this may not be right for something like wrapping text?
            val oldHeight = lp.height
            lp.height = child.measuredHeight

            // Remeasure with new dimensions
            measureChildWithMargins(child, uniformMeasureSpec, 0, heightMeasureSpec, 0)
            lp.height = oldHeight
        }
    }

    /**
     * Measures the children when the orientation of this LinearLayout is set
     * to [HORIZONTAL].
     *
     * @param widthMeasureSpec Horizontal space requirements as imposed by the parent.
     * @param heightMeasureSpec Vertical space requirements as imposed by the parent.
     *
     * @see orientation
     * @see .onMeasure
     */
    open fun measureHorizontal(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        totalLength = 0
        var maxHeight = 0
        var childState = 0
        var alternativeMaxHeight = 0
        var weightedMaxHeight = 0
        var allFillParent = true
        var totalWeight = 0f
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        var matchHeight = false
        var skippedMeasure = false
        maxBaselinedAscent = -1
        maxBaselinedDescent = -1
        val isExactly = widthMode == MeasureSpec.EXACTLY
        var totalConstrainedWidth = 0

        // See how wide everyone is. Also remember max height.
        forEachIndexed(significantOnly = true) { child, i ->
            if (hasDividerBeforeChildAt(i)) {
                totalLength += dividerWidth
            }
            val lp = child.lp
            totalWeight += lp.fixedHorizontalWeight
            if (widthMode == MeasureSpec.EXACTLY && lp.width == MATCH_PARENT) {
                // Optimization: don't bother measuring children who are going to use
                // leftover space. These views will get measured again down below if
                // there is any leftover space.
                totalLength += lp.leftMargin + lp.rightMargin

                // Baseline alignment requires to measure widgets to obtain the
                // baseline offset (in particular for TextViews). The following
                // defeats the optimization mentioned above. Allow the child to
                // use as much space as it wants because we can shrink things
                // later (and re-measure).
                if (lp.isBaselineAligned) {
                    val freeSpec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
                    child.measure(freeSpec, freeSpec)
                } else {
                    skippedMeasure = true
                }
            } else {
                val oldWidth = lp.width
                if (oldWidth == MATCH_PARENT || oldWidth == DivLayoutParams.WRAP_CONTENT_CONSTRAINED) {
                    lp.width = WRAP_CONTENT
                }

                // Determine how big this child would like to be. If this or
                // previous children have given a weight, then we allow it to
                // use all available space (and we will shrink things later
                // if needed).
                measureChildWithMargins(child, widthMeasureSpec,
                    if (totalWeight == 0f) totalLength else 0,
                    heightMeasureSpec, 0)
                lp.width = oldWidth
                val childWidth = child.measuredWidth + lp.leftMargin + lp.rightMargin
                if (isExactly) {
                    totalLength += childWidth
                } else {
                    totalLength = max(totalLength, totalLength + childWidth)
                }
                if (lp.width == DivLayoutParams.WRAP_CONTENT_CONSTRAINED) {
                    constrainedChildren.add(child)
                    totalConstrainedWidth = max(totalConstrainedWidth, totalConstrainedWidth + childWidth)
                }
            }
            var matchHeightLocally = false
            if (heightMode != MeasureSpec.EXACTLY && lp.height == MATCH_PARENT) {
                // The height of the linear layout will scale, and at least one
                // child said it wanted to match our height. Set a flag indicating that
                // we need to remeasure at least that view when we know our height.
                matchHeight = true
                matchHeightLocally = true
            }
            val margin = lp.topMargin + lp.bottomMargin
            val childHeight = child.measuredHeight + margin
            childState = combineMeasuredStates(childState, child.measuredState)
            if (lp.isBaselineAligned) {
                val childBaseline = child.baseline
                if (childBaseline != -1) {
                    maxBaselinedAscent = max(maxBaselinedAscent, childBaseline + lp.topMargin)
                    maxBaselinedDescent = max(maxBaselinedDescent, childHeight - childBaseline - lp.topMargin)
                }
            }
            maxHeight = max(maxHeight, childHeight)
            allFillParent = allFillParent && lp.height == MATCH_PARENT
            if (lp.fixedHorizontalWeight > 0) {
                /*
                 * Heights of weighted Views are bogus if we end up
                 * remeasuring, so keep them separate.
                 */
                weightedMaxHeight = max(weightedMaxHeight,
                    if (matchHeightLocally) margin else childHeight)
            } else {
                alternativeMaxHeight = max(alternativeMaxHeight,
                    if (matchHeightLocally) margin else childHeight)
            }
        }
        if (totalLength > 0 && hasDividerBeforeChildAt(childCount)) {
            totalLength += dividerWidth
        }

        // Add in our padding
        totalLength += paddingLeft + paddingRight
        var widthSize = totalLength

        // Check against our minimum width
        widthSize = max(widthSize, suggestedMinimumWidth)

        // Reconcile our calculated size with the widthMeasureSpec
        val widthSizeAndState = resolveSizeAndState(widthSize, widthMeasureSpec, 0)
        widthSize = widthSizeAndState and MEASURED_SIZE_MASK

        // Either expand children with weight to take up available space or
        // shrink them if they extend beyond our current bounds. If we skipped
        // measurement on any children, we need to measure them now.
        var delta = widthSize - totalLength
        if (skippedMeasure || (delta != 0 && (totalWeight > 0.0f || totalConstrainedWidth > 0))) {
            var weightSum = totalWeight
            maxBaselinedAscent = -1
            maxBaselinedDescent = -1
            maxHeight = -1
            totalLength = 0

            if (delta < 0) {
                constrainedChildren.sortByDescending { it.minimumWidth / it.measuredWidth.toFloat() }
                constrainedChildren.forEach { child ->
                    val lp = child.lp
                    val oldWidth = child.measuredWidth
                    val oldWidthWithMargins = oldWidth + lp.leftMargin + lp.rightMargin
                    val share = (oldWidthWithMargins / totalConstrainedWidth.toFloat() * delta).roundToInt()
                    val childWidth = (oldWidth + share).coerceAtLeast(child.minimumWidth)

                    childState = remeasureChildHorizontal(child, heightMeasureSpec, childWidth, childState)

                    totalConstrainedWidth -= oldWidthWithMargins
                    delta -= child.measuredWidth - oldWidth
                }
            }

            forEach(significantOnly = true) { child ->
                val lp = child.lp
                val childExtra = lp.fixedHorizontalWeight
                if (childExtra > 0) {
                    val share = (childExtra * delta / weightSum).toInt()
                    weightSum -= childExtra
                    delta -= share

                    val childWidth = if (lp.width != MATCH_PARENT || widthMode != MeasureSpec.EXACTLY) {
                        child.measuredWidth + share
                    } else {
                        share
                    }.coerceAtLeast(0)

                    // Child may now not fit in vertical dimension.
                    childState = remeasureChildHorizontal(child, heightMeasureSpec, childWidth, childState)
                }
                if (isExactly) {
                    totalLength += child.measuredWidth + lp.leftMargin + lp.rightMargin
                } else {
                    totalLength = max(totalLength,
                        totalLength + child.measuredWidth + lp.leftMargin + lp.rightMargin)
                }
                val matchHeightLocally = heightMode != MeasureSpec.EXACTLY && lp.height == MATCH_PARENT
                val margin = lp.topMargin + lp.bottomMargin
                val childHeight = child.measuredHeight + margin
                maxHeight = max(maxHeight, childHeight)
                alternativeMaxHeight = max(alternativeMaxHeight,
                    if (matchHeightLocally) margin else childHeight)
                allFillParent = allFillParent && lp.height == MATCH_PARENT
                if (lp.isBaselineAligned) {
                    val childBaseline = child.baseline
                    if (childBaseline != -1) {
                        maxBaselinedAscent = max(maxBaselinedAscent, childBaseline + lp.topMargin)
                        maxBaselinedDescent = max(maxBaselinedDescent, childHeight - childBaseline - lp.topMargin)
                    }
                }
            }

            // Add in our padding
            totalLength += paddingLeft + paddingRight
        } else {
            alternativeMaxHeight = max(alternativeMaxHeight, weightedMaxHeight)
        }
        if (!allFillParent && heightMode != MeasureSpec.EXACTLY) {
            maxHeight = alternativeMaxHeight
        }
        if (maxBaselinedAscent != -1) {
            maxHeight = max(maxHeight, maxBaselinedAscent + maxBaselinedDescent)
        }
        maxHeight += paddingTop + paddingBottom
        // Check against our minimum height
        maxHeight = max(maxHeight, suggestedMinimumHeight)
        setMeasuredDimension(widthSizeAndState or (childState and MEASURED_STATE_MASK),
            resolveSizeAndState(maxHeight, heightMeasureSpec,
                (childState shl MEASURED_HEIGHT_STATE_SHIFT)))
        if (matchHeight) {
            forceUniformHeight(widthMeasureSpec)
        }
    }

    private fun remeasureChildHorizontal(
        child: View,
        heightMeasureSpec: Int,
        width: Int,
        childState: Int
    ): Int {
        val lp = child.lp
        val childHeightMeasureSpec = getChildMeasureSpec(
            heightMeasureSpec,
            paddingTop + paddingBottom + lp.topMargin + lp.bottomMargin,
            lp.height,
            child.minimumHeight,
            lp.maxHeight
        )

        child.measure(MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY), childHeightMeasureSpec)
        return combineMeasuredStates(childState, child.measuredState and MEASURED_STATE_MASK)
    }

    private fun forceUniformHeight(widthMeasureSpec: Int) {
        // Pretend that the linear layout has an exact size. This is the measured height of
        // ourselves. The measured height should be the max height of the children, changed
        // to accommodate the heightMeasureSpec from the parent
        val uniformMeasureSpec = MeasureSpec.makeMeasureSpec(measuredHeight, MeasureSpec.EXACTLY)
        forEach(significantOnly = true) { child ->
            val lp = child.lp
            if (lp.height != MATCH_PARENT) return@forEach

            // Temporarily force children to reuse their old measured width
            // FIXME: this may not be right for something like wrapping text?
            val oldWidth = lp.width
            lp.width = child.measuredWidth

            // Remeasure with new dimensions
            measureChildWithMargins(child, widthMeasureSpec, 0, uniformMeasureSpec, 0)
            lp.width = oldWidth
        }
    }

    override fun onLayout(changed: Boolean, l: Int, t: Int, r: Int, b: Int) {
        if (isVertical) {
            layoutVertical(l, t, r, b)
        } else {
            layoutHorizontal(l, t, r, b)
        }
    }

    /**
     * Position the children during a layout pass if the orientation of this
     * LinearLayout is set to [VERTICAL].
     *
     * @see orientation
     * @see onLayout
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    open fun layoutVertical(left: Int, top: Int, right: Int, bottom: Int) {

        // Where right end of child should go
        val width = right - left
        val childRight = width - paddingRight

        // Space available for child
        val childSpace = width - paddingLeft - paddingRight
        val majorGravity = gravity and Gravity.VERTICAL_GRAVITY_MASK
        val minorGravity = gravity and GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK
        var childTop = when (majorGravity) {
            Gravity.BOTTOM -> paddingTop + bottom - top - totalLength
            Gravity.CENTER_VERTICAL -> paddingTop + (bottom - top - totalLength) / 2
            Gravity.TOP -> paddingTop
            else -> paddingTop
        }
        forEachIndexed(significantOnly = true) { child, i ->
            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight
            val lp = child.lp
            var gravity = lp.gravity
            if (gravity < 0) {
                gravity = minorGravity
            }
            val layoutDirection = ViewCompat.getLayoutDirection(this)
            val absoluteGravity = GravityCompat.getAbsoluteGravity(gravity, layoutDirection)
            val childLeft = when (absoluteGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
                Gravity.CENTER_HORIZONTAL ->
                    paddingLeft + (childSpace - childWidth) / 2 + lp.leftMargin - lp.rightMargin
                Gravity.RIGHT -> childRight - childWidth - lp.rightMargin
                Gravity.LEFT -> paddingLeft + lp.leftMargin
                else -> paddingLeft + lp.leftMargin
            }
            if (hasDividerBeforeChildAt(i)) {
                childTop += dividerHeight
            }
            childTop += lp.topMargin
            setChildFrame(child, childLeft, childTop, childWidth, childHeight)
            childTop += childHeight + lp.bottomMargin
        }
    }

    /**
     * Position the children during a layout pass if the orientation of this
     * LinearLayout is set to [HORIZONTAL].
     *
     * @see orientation
     * @see onLayout
     * @param left
     * @param top
     * @param right
     * @param bottom
     */
    open fun layoutHorizontal(left: Int, top: Int, right: Int, bottom: Int) {
        val isLayoutRtl = isLayoutRtl()
        var childTop: Int

        // Where bottom of child should go
        val height = bottom - top
        val childBottom = height - paddingBottom

        // Space available for child
        val childSpace = height - paddingTop - paddingBottom
        val majorGravity = gravity and GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK
        val minorGravity = gravity and Gravity.VERTICAL_GRAVITY_MASK
        val layoutDirection = ViewCompat.getLayoutDirection(this)
        var childLeft = when (GravityCompat.getAbsoluteGravity(majorGravity, layoutDirection)) {
            Gravity.RIGHT -> paddingLeft + right - left - totalLength
            Gravity.CENTER_HORIZONTAL -> paddingLeft + (right - left - totalLength) / 2
            Gravity.LEFT -> paddingLeft
            else -> paddingLeft
        }
        var start = 0
        var dir = 1
        //In case of RTL, start drawing from the last child.
        if (isLayoutRtl) {
            start = childCount - 1
            dir = -1
        }
        for (i in 0 until childCount) {
            val childIndex = start + dir * i
            val child = getChildAt(childIndex)
            if (child == null || child.visibility == GONE) continue

            val childWidth = child.measuredWidth
            val childHeight = child.measuredHeight
            var childBaseline = -1
            val lp = child.lp
            if (lp.isBaselineAligned && lp.height != MATCH_PARENT) {
                childBaseline = child.baseline
            }
            var gravity = lp.gravity
            if (gravity < 0) {
                gravity = minorGravity
            }
            when (gravity and Gravity.VERTICAL_GRAVITY_MASK) {
                Gravity.TOP -> {
                    childTop = paddingTop + lp.topMargin
                    if (childBaseline != -1) {
                        childTop += maxBaselinedAscent - childBaseline - lp.topMargin
                    }
                }
                Gravity.CENTER_VERTICAL ->
                    // Removed support for baseline alignment when layout_gravity or
                    // gravity == center_vertical. See bug #1038483.
                    // Keep the code around if we need to re-enable this feature
                    // if (childBaseline != -1) {
                    //     // Align baselines vertically only if the child is smaller than us
                    //     if (childSpace - childHeight > 0) {
                    //         childTop = paddingTop + (childSpace / 2) - childBaseline;
                    //     } else {
                    //         childTop = paddingTop + (childSpace - childHeight) / 2;
                    //     }
                    // } else {
                    childTop = paddingTop + (childSpace - childHeight) / 2 +
                        lp.topMargin - lp.bottomMargin
                Gravity.BOTTOM -> {
                    childTop = childBottom - childHeight - lp.bottomMargin
                }
                else -> childTop = paddingTop
            }
            if (hasDividerBeforeChildAt(childIndex)) {
                childLeft += dividerWidth
            }
            childLeft += lp.leftMargin
            setChildFrame(child, childLeft, childTop, childWidth, childHeight)
            childLeft += childWidth + lp.rightMargin
        }
    }

    private fun setChildFrame(child: View, left: Int, top: Int, width: Int, height: Int) =
        child.layout(left, top, left + width, top + height)

    fun setHorizontalGravity(horizontalGravity: Int) {
        val newGravity = horizontalGravity and GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK
        if ((gravity and GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK) == newGravity) return
        _gravity = (gravity and GravityCompat.RELATIVE_HORIZONTAL_GRAVITY_MASK.inv()) or newGravity
        requestLayout()
    }

    fun setVerticalGravity(verticalGravity: Int) {
        val newGravity = verticalGravity and Gravity.VERTICAL_GRAVITY_MASK
        if ((gravity and Gravity.VERTICAL_GRAVITY_MASK) == newGravity) return
        _gravity = (gravity and Gravity.VERTICAL_GRAVITY_MASK.inv()) or newGravity
        requestLayout()
    }

    /**
     * Returns a set of layout parameters with a width of [MATCH_PARENT] and a height of [WRAP_CONTENT]
     * when the layout's orientation is [VERTICAL]. When the orientation is
     * [HORIZONTAL], the width is set to [WRAP_CONTENT] and the height to [WRAP_CONTENT].
     */
    override fun generateDefaultLayoutParams() =
        if (isVertical) DivLayoutParams(MATCH_PARENT, WRAP_CONTENT) else DivLayoutParams(WRAP_CONTENT, WRAP_CONTENT)

    override fun onInitializeAccessibilityEvent(event: AccessibilityEvent) {
        super.onInitializeAccessibilityEvent(event)
        event.className = ACCESSIBILITY_CLASS_NAME
    }

    override fun onInitializeAccessibilityNodeInfo(info: AccessibilityNodeInfo) {
        super.onInitializeAccessibilityNodeInfo(info)
        info.className = ACCESSIBILITY_CLASS_NAME
    }

    private val isVertical get() = orientation == VERTICAL

    private fun isLayoutRtl() =
        ViewCompat.getLayoutDirection(this) == ViewCompat.LAYOUT_DIRECTION_RTL

    private val DivLayoutParams.fixedHorizontalWeight get() = getFixedWeight(horizontalWeight, width)

    private val DivLayoutParams.fixedVerticalWeight get() = getFixedWeight(verticalWeight, height)

    private fun getFixedWeight(weight: Float, size: Int) = when {
        weight > 0 -> weight
        size == MATCH_PARENT -> 1f
        else -> 0f
    }
}
