package com.view.splashanimation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.View
import android.view.animation.LinearInterpolator
import android.view.animation.OvershootInterpolator

/**
 * Copyright (c), 2018-2019
 * @author: lixin
 * Date: 2019/3/28
 * Description: 启动页动画
 */
class SplashView: View {

    constructor(context: Context, attributeSet: AttributeSet?): super(context, attributeSet) {
        init()
    }

    /**
     * 旋转圆的画笔
     */
    private lateinit var mPaint: Paint

    /**
     * 扩散圆的画笔
     */
    private lateinit var mHolePaint: Paint

    /**
     * 属性动画
     */
    private lateinit var mValueAnimator: ValueAnimator

    /**
     * 背景色
     */
    private var mBackgroundColor = Color.WHITE

    /**
     * 旋转圆的颜色数组
     */
    private lateinit var mCircleColors: IntArray

    /**
     * 旋转圆的中心坐标
     */
    private var mCenterX: Float = 0f
    private var mCenterY: Float = 0f

    /**
     * 斜对角线的一半，扩散圆的最大半径
     */
    private var mDistance: Float = 0f

    /**
     * 6个小球的半径
     */
    private var mCircleRadius: Float = 18f

    /**
     * 旋转大圆的半径
     */
    private var mRotateRadius: Float = 90f

    /**
     * 当前大圆的旋转角度
     */
    private var mCurrentRotateAngle: Float = 0f

    /**
     * 当前大圆的半径
     */
    private var mCurrentRotateRadius: Float = mRotateRadius

    /**
     * 扩散圆的半径
     */
    private var mCurrentHoleRadius: Float = 0f

    /**
     * 动画的时长
     */
    private var mAnimationDuration: Long = 1200

    private fun init() {
        mPaint = Paint(Paint.ANTI_ALIAS_FLAG)

        mHolePaint = Paint()
        mHolePaint.apply {
            isAntiAlias = true
            style = Paint.Style.STROKE
            color = mBackgroundColor
        }

        mCircleColors = resources.getIntArray(R.array.splash_circle_colors)
    }

    /**
     * 在控件大小发生改变时调用。所以这里初始化会被调用一次
     */
    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)
        mCenterX = (w / 2).toFloat()
        mCenterY = (h /2).toFloat()
        mDistance = (Math.hypot(w.toDouble(), h.toDouble()) / 2).toFloat()
    }

    override fun onDraw(canvas: Canvas?) {
        super.onDraw(canvas)
        if (mState == null) {
            mState = RotateState()
        }
        mState?.drawState(canvas)
    }

    /**
     * 绘制背景
     */
    private fun drawBackground(canvas: Canvas?) {

        if (mCurrentHoleRadius > 0) {
            //绘制空心圆
            val strokeWidth: Float = mDistance - mCurrentHoleRadius
            val radius: Float = strokeWidth / 2 + mCurrentHoleRadius
            mHolePaint.strokeWidth = strokeWidth
            canvas?.drawCircle(mCenterX, mCenterY, radius, mHolePaint)
        } else {
            canvas?.drawColor(mBackgroundColor)
        }
    }

    /**
     * 绘制6个小球
     */
    private fun drawCircles(canvas: Canvas?) {
        val rotateAngle: Float = (Math.PI * 2 / mCircleColors.size).toFloat()
        for (i in 0 until mCircleColors.size) {
            // x = r * cos(α) + centerX
            // y = r * sin(α) + centerY
            val angle: Double = (i * rotateAngle).toDouble() + mCurrentRotateAngle
            val cx: Float = (Math.cos(angle) * mCurrentRotateRadius + mCenterX).toFloat()
            val cy: Float = (Math.sin(angle) * mCurrentRotateRadius + mCenterY).toFloat()
            mPaint.color = mCircleColors[i]
            canvas?.drawCircle(cx, cy, mCircleRadius, mPaint)
        }
    }

    /**
     * 动画状态
     */
    private var mState: SplashState? = null

    private abstract class SplashState {
        abstract fun drawState(canvas: Canvas?)
    }

    /**
     * 旋转动画
     */
    private inner class RotateState: SplashState() {

        init {
            //旋转一周360度
            mValueAnimator = ValueAnimator.ofFloat(0f, (Math.PI * 2).toFloat())
            mValueAnimator.apply {
                repeatCount = 2
                duration = mAnimationDuration
                interpolator = LinearInterpolator()

                addUpdateListener {
                    mCurrentRotateAngle = it.animatedValue as Float
                    invalidate()
                }

                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        mState = MergingState()
                    }
                })
            }.start()
        }

        override fun drawState(canvas: Canvas?) {
            //绘制北京
            drawBackground(canvas)
            //绘制6个小球
            drawCircles(canvas)
        }
    }

    /**
     * 扩散聚合动画
     */
    private inner class MergingState: SplashState() {

        init {
            mValueAnimator = ValueAnimator.ofFloat(mCircleRadius, mRotateRadius)
            mValueAnimator.apply {
                duration = mAnimationDuration
                interpolator = OvershootInterpolator(10f)

                addUpdateListener {
                    mCurrentRotateRadius = it.animatedValue as Float
                    invalidate()
                }

                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator?) {
                        super.onAnimationEnd(animation)
                        mState = ExpandState()
                    }
                })
            }.reverse()
        }

        override fun drawState(canvas: Canvas?) {
            drawBackground(canvas)
            drawCircles(canvas)
        }
    }

    /**
     * 水波纹
     */
    private inner class ExpandState: SplashState() {

        init {
            mValueAnimator = ValueAnimator.ofFloat(mCircleRadius, mDistance)
            mValueAnimator.apply {
                duration = mAnimationDuration
                interpolator = LinearInterpolator()

                addUpdateListener {
                    mCurrentHoleRadius = it.animatedValue as Float
                    invalidate()
                }
            }.start()
        }

        override fun drawState(canvas: Canvas?) {
            drawBackground(canvas)
        }
    }
}