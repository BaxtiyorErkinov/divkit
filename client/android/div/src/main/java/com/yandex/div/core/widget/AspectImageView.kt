package com.yandex.div.core.widget

import android.content.Context
import android.graphics.Canvas
import android.graphics.Matrix
import android.util.AttributeSet
import android.view.Gravity
import android.view.View
import androidx.appcompat.widget.AppCompatImageView
import androidx.core.view.GravityCompat
import androidx.core.view.ViewCompat
import com.yandex.div.R
import kotlin.math.max
import kotlin.math.min
import kotlin.math.roundToInt

open class AspectImageView @JvmOverloads constructor(
    context: Context,
    attrs: AttributeSet? = null,
    defStyleAttr: Int = 0
) : AppCompatImageView(context, attrs, defStyleAttr) {

    var gravity by appearanceAffecting(Gravity.NO_GRAVITY)

    var aspectRatio by dimensionAffecting(ASPECT_RATIO_OF_IMAGE) { value ->
        value.coerceAtLeast(ASPECT_RATIO_OF_IMAGE)
    }

    var imageScale by dimensionAffecting(Scale.NO_SCALE)

    private val transformMatrix = Matrix()

    private var isMatrixInvalidated = true

    init {
        super.setScaleType(ScaleType.MATRIX)

        if (isInEditMode) {
            val array = context.obtainStyledAttributes(attrs, R.styleable.AspectImageView, defStyleAttr, 0)
            try {
                gravity = array.getInt(R.styleable.AspectImageView_android_gravity, Gravity.NO_GRAVITY)
                aspectRatio = array.getFloat(R.styleable.AspectImageView_aspectRatio, ASPECT_RATIO_OF_IMAGE)
                imageScale = Scale.values()[array.getInteger(R.styleable.AspectImageView_imageScale, 0)]
            } finally {
                array.recycle()
            }
        }
    }

    override fun setScaleType(scaleType: ScaleType?) = Unit

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec)
        applyAspectRatio(widthMeasureSpec, heightMeasureSpec)
    }

    private fun applyAspectRatio(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val aspectRatio = aspectRatio
        if (aspectRatio == ASPECT_RATIO_OF_IMAGE) {
            return
        }

        val widthMode = View.MeasureSpec.getMode(widthMeasureSpec)
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        val resizeWidth = canResizeWidth(widthMeasureSpec)
        val resizeHeight = canResizeHeight(heightMeasureSpec)

        var width = measuredWidth
        var height = measuredHeight
        when {
            !resizeWidth && !resizeHeight -> height = (width / aspectRatio).roundToInt()
            !resizeWidth && resizeHeight -> height = (width / aspectRatio).roundToInt()
            resizeWidth && !resizeHeight -> width = (height * aspectRatio).roundToInt()
            resizeWidth && resizeHeight -> height = (width / aspectRatio).roundToInt()
        }

        setMeasuredDimension(
            MeasureSpec.makeMeasureSpec(width, widthMode),
            MeasureSpec.makeMeasureSpec(height, heightMode)
        )
    }

    protected open fun canResizeWidth(widthMeasureSpec: Int): Boolean {
        val widthMode = MeasureSpec.getMode(widthMeasureSpec)
        return widthMode != MeasureSpec.EXACTLY
    }

    protected open fun canResizeHeight(heightMeasureSpec: Int): Boolean {
        val heightMode = MeasureSpec.getMode(heightMeasureSpec)
        return heightMode != MeasureSpec.EXACTLY
    }

    override fun invalidate() {
        super.invalidate()
        isMatrixInvalidated = true
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        isMatrixInvalidated = true
    }

    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        super.onSizeChanged(width, height, oldWidth, oldHeight)
        isMatrixInvalidated = true
    }

    override fun onDraw(canvas: Canvas) {
        if ((imageMatrix == null || imageMatrix == transformMatrix)
            && isMatrixInvalidated
            && width > 0
            && height > 0
        ) {
            updateMatrix(width, height)
            isMatrixInvalidated = false
        }
        super.onDraw(canvas)
    }

    private fun updateMatrix(width: Int, height: Int) {
        val drawable = drawable ?: return

        val availableWidth = (width - paddingLeft - paddingRight).toFloat()
        val availableHeight = (height - paddingTop - paddingBottom).toFloat()
        val imageWidth = drawable.intrinsicWidth.toFloat()
        val imageHeight = drawable.intrinsicHeight.toFloat()
        val absoluteGravity = GravityCompat.getAbsoluteGravity(gravity, ViewCompat.getLayoutDirection(this))

        val scale = when (imageScale) {
            Scale.NO_SCALE -> 1.0f
            Scale.FIT -> min(availableWidth / imageWidth, availableHeight / imageHeight)
            Scale.FILL -> max(availableWidth / imageWidth, availableHeight / imageHeight)
        }
        val translationX = when (absoluteGravity and Gravity.HORIZONTAL_GRAVITY_MASK) {
            Gravity.CENTER_HORIZONTAL -> (availableWidth - imageWidth * scale) / 2
            Gravity.RIGHT -> availableWidth - imageWidth * scale
            else -> 0.0f
        }
        val translationY = when (absoluteGravity and Gravity.VERTICAL_GRAVITY_MASK) {
            Gravity.CENTER_VERTICAL -> (availableHeight - imageHeight * scale) / 2
            Gravity.BOTTOM -> availableHeight - imageHeight * scale
            else -> 0.0f
        }

        transformMatrix.apply {
            reset()
            postScale(scale, scale)
            postTranslate(translationX, translationY)
        }
        imageMatrix = transformMatrix
    }

    enum class Scale {
        NO_SCALE,
        FIT,
        FILL
    }

    companion object {
        const val ASPECT_RATIO_OF_IMAGE = 0.0f
    }
}
