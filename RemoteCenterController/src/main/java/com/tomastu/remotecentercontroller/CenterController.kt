package com.tomastu.remotecentercontroller

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.graphics.Rect
import android.graphics.RectF
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.view.View
import kotlin.math.atan
import kotlin.math.sin


/**
 * 使用前请注意将宽高设置为相同大小，否则无法正常使用
 */
class CenterController : View {
    constructor(ctx: Context) : super(ctx)
    constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs)
    constructor(ctx: Context, attrs: AttributeSet, defStyleAtr: Int) : super(
        ctx,
        attrs,
        defStyleAtr
    )

    constructor(ctx: Context, attrs: AttributeSet, defStyleAtr: Int, defStyleRes: Int) : super(
        ctx,
        attrs,
        defStyleAtr,
        defStyleRes
    )

    companion object {
        const val STATE_IDLE = 0
        const val STATE_OK = 1
        const val STATE_LEFT = 2
        const val STATE_UP = 3
        const val STATE_RIGHT = 4
        const val STATE_DOWN = 5
    }

    private val mPaint = Paint()
    private var mState = STATE_IDLE
    private val mRect = Rect()
    private val mPath = Path()
    private val mSmallCircleRectF = RectF()
    private val mBigCircleRectF = RectF()

    private var mOnDirBtnTouchListener: OnDirBtnTouchListener? = null

    // ratioStrokeWidth 表示外边的圆占整个圆的比例
    private val mRatioStrokeWidth = 26 / 41f

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        val widthAndHeight = width.toFloat()

        Log.e("controller", "$widthAndHeight")

        // STEP 1 : 画最大的圆
        val strokeWidth = (mRatioStrokeWidth / 2) * widthAndHeight
        val bigCircleRadius = (widthAndHeight / 2) - (strokeWidth / 2)
        mPaint.style = Paint.Style.STROKE
        mPaint.strokeWidth = strokeWidth
        mPaint.color = Color.WHITE

        canvas.drawCircle(widthAndHeight / 2, widthAndHeight / 2, bigCircleRadius, mPaint)

        // STEP 2 : 根据组件是否被按下绘制深色背景
        mPaint.color = Color.rgb(246, 246, 246)
        mPaint.style = Paint.Style.FILL
        mPaint.strokeWidth = 1f
        val sin45 = sin(Math.toRadians(45.0))
        val lineLength = (strokeWidth * sin45).toFloat()
        mSmallCircleRectF.set(
            strokeWidth,
            strokeWidth,
            widthAndHeight - strokeWidth,
            widthAndHeight - strokeWidth
        )
        mBigCircleRectF.set(0f, 0f, widthAndHeight, widthAndHeight)
        when (mState) {
            STATE_UP -> {
                mPath.reset()
                mPath.arcTo(mSmallCircleRectF, -135f, 90f, true)
                mPath.rLineTo(lineLength, -lineLength)
                mPath.arcTo(mBigCircleRectF, -45f, -90f, true)
                mPath.rLineTo(lineLength, lineLength)
                canvas.drawPath(mPath, mPaint)
            }

            STATE_RIGHT -> {
                mPath.reset()
                mPath.arcTo(mSmallCircleRectF, -45f, 90f, true)
                mPath.rLineTo(lineLength, lineLength)
                mPath.arcTo(mBigCircleRectF, 45f, -90f, true)
                mPath.rLineTo(-lineLength, lineLength)
                canvas.drawPath(mPath, mPaint)
            }

            STATE_DOWN -> {
                mPath.reset()
                mPath.arcTo(mSmallCircleRectF, 45f, 90f, true)
                mPath.rLineTo(-lineLength, lineLength)
                mPath.arcTo(mBigCircleRectF, -225f, -90f, true)
                mPath.rLineTo(-lineLength, -lineLength)
                canvas.drawPath(mPath, mPaint)
            }

            STATE_LEFT -> {
                mPath.reset()
                mPath.arcTo(mSmallCircleRectF, 135f, 90f, true)
                mPath.rLineTo(-lineLength, -lineLength)
                mPath.arcTo(mBigCircleRectF, -135f, -90f, true)
                mPath.rLineTo(lineLength, -lineLength)
                canvas.drawPath(mPath, mPaint)
            }
        }

        // STEP 3 : 画 左上右下 四个小点
        val pointCircleRadius = widthAndHeight * 12 / 410 / 2f
        mPaint.color = Color.argb(100, 204, 204, 204)
        mPaint.style = Paint.Style.FILL
        // 左
        canvas.drawCircle(strokeWidth / 2, widthAndHeight / 2, pointCircleRadius, mPaint)
        // 右
        canvas.drawCircle(
            widthAndHeight / 2 + bigCircleRadius,
            widthAndHeight / 2,
            pointCircleRadius,
            mPaint
        )
        // 上
        canvas.drawCircle(widthAndHeight / 2, strokeWidth / 2, pointCircleRadius, mPaint)
        // 下
        canvas.drawCircle(
            widthAndHeight / 2,
            widthAndHeight - strokeWidth / 2,
            pointCircleRadius,
            mPaint
        )

        // STEP 4 : 写 OK 字符
        mPaint.color = Color.BLACK
        mPaint.textSize = (20f * widthAndHeight / 825).sp2px(context)
        mPaint.getTextBounds("OK", 0, 2, mRect)
        canvas.drawText(
            "OK",
            ((widthAndHeight / 2) - (mRect.width() / 2)),
            (widthAndHeight / 2) + (mRect.height() / 2),
            mPaint
        )

        mState = STATE_IDLE
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
//        return super.onTouchEvent(event)
        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                // 判断手指按下的范围，先看在小圆还是大圆
                val x = event.x
                val y = event.y
                if (inBigCircle(x, y)) {
                    if (inSmallCircle(x, y)) {
                        mState = STATE_OK
                        mOnDirBtnTouchListener?.onOkTouch()
                    } else {
                        when (inWhichState(x, y)) {
                            STATE_LEFT -> {
                                mState = STATE_LEFT
                                mOnDirBtnTouchListener?.onLeftTouch()
                            }

                            STATE_UP -> {
                                mState = STATE_UP
                                mOnDirBtnTouchListener?.onUpTouch()
                            }

                            STATE_RIGHT -> {
                                mState = STATE_RIGHT
                                mOnDirBtnTouchListener?.onRightTouch()
                            }

                            STATE_DOWN -> {
                                mState = STATE_DOWN
                                mOnDirBtnTouchListener?.onDownTouch()
                            }
                        }
                    }
                }
            }

            MotionEvent.ACTION_UP -> {
                mState = STATE_IDLE
            }
        }

        refreshDrawableState()
        invalidate()
        return true
    }

    private fun inSmallCircle(x: Float, y: Float): Boolean {
        val strokeWidth = (mRatioStrokeWidth / 2) * width
        val smallCircleRadius = (width / 2) - (strokeWidth)
        return (x - width / 2) * (x - width / 2) + (y - width / 2) * (y - width / 2) < smallCircleRadius * smallCircleRadius
    }

    private fun inBigCircle(x: Float, y: Float): Boolean =
        (x - width / 2) * (x - width / 2) + (y - width / 2) * (y - width / 2) < (width / 2) * (width / 2)

    // 主要通过与水平线角度判断方向
    private fun inWhichState(x: Float, y: Float): Int {
        // 按下的点 与 View 最中心的点 组成的直线的斜率
        val gradient = -(y - width / 2) / (x - width / 2)
        val angle = Math.toDegrees(atan(gradient.toDouble())).toInt()

        // 判断是在 上半部分 还是 下半部分
        val inUpperHalf = (y <= width / 2)
        val inLeftHalf = (x <= width / 2)
        if (inUpperHalf) {
            // 如果是中间的部分
            if ((angle in -45 downTo -90) or (angle in 45..90)) {
                return STATE_UP
            }
            return if (inLeftHalf) STATE_LEFT else STATE_RIGHT
        } else {
            if ((angle in -45 downTo -90) or (angle in 45..90)) {
                return STATE_DOWN
            }
            return if (inLeftHalf) STATE_LEFT else STATE_RIGHT
        }
    }

    fun setOnDirBtnTouchListener(l: OnDirBtnTouchListener) {
        mOnDirBtnTouchListener = l
    }
}

/**
 * 用于 方向 / OK 按键的触摸事件
 */
interface OnDirBtnTouchListener {
    fun onLeftTouch()
    fun onUpTouch()
    fun onRightTouch()
    fun onDownTouch()
    fun onOkTouch()
}