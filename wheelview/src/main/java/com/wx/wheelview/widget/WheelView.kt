/*
 * Copyright (C) 2016 venshine.cn@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.wx.wheelview.widget

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Rect
import android.os.Build
import android.os.Handler
import android.os.Message
import android.text.TextUtils
import android.util.AttributeSet
import android.util.TypedValue
import android.view.MotionEvent
import android.view.View
import android.view.ViewTreeObserver
import android.widget.AbsListView
import android.widget.AdapterView
import android.widget.ListAdapter
import android.widget.ListView
import android.widget.TextView
import com.wx.wheelview.adapter.ArrayWheelAdapter
import com.wx.wheelview.adapter.BaseWheelAdapter
import com.wx.wheelview.adapter.SimpleWheelAdapter
import com.wx.wheelview.common.WheelConstants
import com.wx.wheelview.common.WheelViewException
import com.wx.wheelview.graphics.DrawableFactory
import com.wx.wheelview.util.WheelUtils
import kotlin.math.abs
import kotlin.math.max
import kotlin.math.pow

/**
 * 滚轮控件
 *
 * @author venshine
 */
@JvmSuppressWildcards
open class WheelView<T> : ListView, IWheelView<T> {

    private var mItemH = 0 // 每一项高度
    private var mWheelSize = WHEEL_SIZE // 滚轮个数
    private var mLoop = LOOP // 是否循环滚动
    private var mList: List<T>? = null // 滚轮数据列表
    private var mCurrentPositon = -1 // 记录滚轮当前刻度
    private var mExtraText: String? = null // 添加滚轮选中位置附加文本
    private var mExtraTextColor = 0 // 附加文本颜色
    private var mExtraTextSize = 0 // 附加文本大小
    private var mExtraMargin = 0 // 附加文本外边距
    private var mExtraTextBold = false // 附加文本是否加粗
    private var mSelection = 0 // 选中位置
    private var mSelectionPending = false // 首次布局完成前设置的选中位置，待布局后生效
    private var mLayoutReady = false // 是否已按 item 高度完成首次布局
    private var mClickable = CLICKABLE // 是否可点击

    private val mTextPaint = Paint(Paint.ANTI_ALIAS_FLAG) // 附加文本画笔

    private var mSkin = Skin.None // 皮肤风格

    private var mStyle = WheelViewStyle() // 滚轮样式

    private var mJoinWheelView: WheelView<*>? = null // 副WheelView

    private var mJoinMap: HashMap<String, List<T>>? = null // 副滚轮数据列表

    private var mWheelAdapter: BaseWheelAdapter<T>? = null

    private var mOnWheelItemSelectedListener: OnWheelItemSelectedListener<T>? = null

    private var mOnWheelItemClickListener: OnWheelItemClickListener<T>? = null

    @Suppress("DEPRECATION")
    @SuppressLint("HandlerLeak")
    private val mHandler: Handler = object : Handler() {
        override fun handleMessage(msg: Message) {
            if (msg.what == WheelConstants.WHEEL_SCROLL_HANDLER_WHAT) {
                mOnWheelItemSelectedListener?.onItemSelected(currentPosition, selectionItemOrNull())
                val joinWheelView = mJoinWheelView
                if (joinWheelView != null) {
                    val joinMap = mJoinMap!!
                    if (joinMap.isNotEmpty()) {
                        // 联动数据的 key 是条目的显示文本（toString），条目不是 String 时也能找到对应数据
                        @Suppress("UNCHECKED_CAST")
                        (joinWheelView as WheelView<Any?>).resetDataFromTop(
                            joinMap[mList!![currentPosition].toString()]
                        )
                    } else {
                        throw WheelViewException("JoinList is error.")
                    }
                }
            }
        }
    }

    private val mOnItemClickListener = AdapterView.OnItemClickListener { _, _, _, _ ->
        mOnWheelItemClickListener?.onItemClick(currentPosition, selectionItemOrNull())
    }

    @SuppressLint("ClickableViewAccessibility")
    private val mTouchListener = OnTouchListener { v, _ ->
        v.parent.requestDisallowInterceptTouchEvent(true)
        false
    }

    private var mAdjusting = false // 正在发起对齐滚动，期间同步回调的 IDLE 不再触发对齐

    private val mAdjustRunnable = Runnable { adjustPosition() }

    private val mRefreshRunnable = Runnable { refreshCurrentPosition(false) }

    private val mOnScrollListener = object : OnScrollListener {
        override fun onScrollStateChanged(view: AbsListView, scrollState: Int) {
            removeCallbacks(mAdjustRunnable)
            // 部分 ROM 在 smoothScrollBy 内部会同步回调 IDLE，再次对齐会无限递归（issue #41）；
            // 放到回调外执行，在回调里直接 smoothScrollBy 在部分机型上不生效（issue #13）
            if (scrollState == OnScrollListener.SCROLL_STATE_IDLE && !mAdjusting) {
                post(mAdjustRunnable)
            }
        }

        override fun onScroll(view: AbsListView, firstVisibleItem: Int, visibleItemCount: Int, totalItemCount: Int) {
            if (visibleItemCount != 0) {
                refreshCurrentPosition(false)
            }
        }
    }

    constructor(context: Context) : super(context) {
        init()
    }

    constructor(context: Context, style: WheelViewStyle?) : super(context) {
        // 与原实现一致：style 为 null 时使用默认样式
        if (style != null) {
            this.style = style
        }
        init()
    }

    constructor(context: Context, attrs: AttributeSet?) : super(context, attrs) {
        init()
    }

    constructor(context: Context, attrs: AttributeSet?, defStyleAttr: Int) : super(context, attrs, defStyleAttr) {
        init()
    }

    /**
     * 设置滚轮滑动停止时事件，监听滚轮选中项
     *
     * @param onWheelItemSelectedListener
     */
    open fun setOnWheelItemSelectedListener(onWheelItemSelectedListener: OnWheelItemSelectedListener<T>?) {
        mOnWheelItemSelectedListener = onWheelItemSelectedListener
    }

    /**
     * 设置滚轮选中项点击事件
     *
     * @param onWheelItemClickListener
     */
    open fun setOnWheelItemClickListener(onWheelItemClickListener: OnWheelItemClickListener<T>?) {
        mOnWheelItemClickListener = onWheelItemClickListener
    }

    /**
     * 初始化
     */
    private fun init() {
        tag = WheelConstants.TAG
        isVerticalScrollBarEnabled = false
        isScrollingCacheEnabled = false
        cacheColorHint = Color.TRANSPARENT
        setFadingEdgeLength(0)
        overScrollMode = OVER_SCROLL_NEVER
        dividerHeight = 0
        onItemClickListener = mOnItemClickListener
        setOnScrollListener(mOnScrollListener)
        setOnTouchListener(mTouchListener)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            isNestedScrollingEnabled = true
        }
        addOnGlobalLayoutListener()
    }

    private fun addOnGlobalLayoutListener() {
        viewTreeObserver.addOnGlobalLayoutListener(object : ViewTreeObserver.OnGlobalLayoutListener {
            override fun onGlobalLayout() {
                // 控件或父容器不可见、数据未设置时还没有 item，继续等待下一次布局，拿到 item 高度后才移除监听
                if (childCount == 0 || getChildAt(0).height == 0) {
                    return
                }
                if (mItemH == 0) {
                    mItemH = getChildAt(0).height
                    val params = layoutParams
                    params.height = mItemH * mWheelSize
                    layoutParams = params
                    // 高度变化会触发重新布局，在新高度下完成布局后（下一次回调）再定位，否则会按旧高度定位而错位
                    return
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    viewTreeObserver.removeOnGlobalLayoutListener(this)
                } else {
                    @Suppress("DEPRECATION")
                    viewTreeObserver.removeGlobalOnLayoutListener(this)
                }
                mLayoutReady = true
                if (mSelectionPending) {
                    mSelectionPending = false
                    superSetSelection(getRealPosition(mSelection))
                }
                refreshVisibleItems(firstVisiblePosition, currentPosition + mWheelSize / 2, mWheelSize / 2)
                applyBackground()
            }
        })
    }

    private fun superSetSelection(position: Int) {
        super.setSelection(position)
    }

    /**
     * 滚轮样式
     */
    open var style: WheelViewStyle
        get() = mStyle
        set(style) {
            mStyle = style
            // 已完成布局时立即刷新，否则要等到下次滚动才生效
            if (mLayoutReady) {
                refreshCurrentPosition(false)
                applyBackground()
            }
        }

    /**
     * 设置背景
     */
    private fun applyBackground() {
        val drawable = DrawableFactory.createDrawable(mSkin, width, mItemH * mWheelSize, mStyle, mWheelSize, mItemH)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            background = drawable
        } else {
            @Suppress("DEPRECATION")
            setBackgroundDrawable(drawable)
        }
    }

    /**
     * 皮肤风格
     */
    open var skin: Skin
        get() = mSkin
        set(skin) {
            mSkin = skin
            if (mLayoutReady) {
                applyBackground()
            }
        }

    /**
     * 设置滚轮个数
     *
     * @param wheelSize
     */
    override fun setWheelSize(wheelSize: Int) {
        if (wheelSize and 1 == 0) {
            throw WheelViewException("wheel size must be an odd number.")
        }
        mWheelSize = wheelSize
        mWheelAdapter?.setWheelSize(wheelSize)
    }

    /**
     * 设置滚轮是否循环滚动
     *
     * @param loop
     */
    override fun setLoop(loop: Boolean) {
        if (loop != mLoop) {
            mLoop = loop
            setSelection(0)
            mWheelAdapter?.setLoop(loop)
        }
    }

    /**
     * 设置滚轮选中项是否可点击
     *
     * @param clickable
     */
    override fun setWheelClickable(clickable: Boolean) {
        if (clickable != mClickable) {
            mClickable = clickable
            mWheelAdapter?.setClickable(clickable)
        }
    }

    /**
     * 设置是否支持点击滚轮进行选择
     *
     * @param clickToPosition 是否支持点击滚轮进行选择
     */
    open fun setClickToPosition(clickToPosition: Boolean) {
        val adapter = mWheelAdapter!!
        if (clickToPosition) {
            adapter.setOnClickListener { position ->
                var deltaPosition = position - currentPosition
                if (mLoop) {
                    if (deltaPosition > mWheelSize / 2) {
                        deltaPosition -= wheelCount
                    } else if (deltaPosition < -mWheelSize / 2) {
                        deltaPosition += wheelCount
                    }
                }
                smoothScrollBy(mItemH * deltaPosition, 400)
            }
        } else {
            adapter.setOnClickListener(null)
        }
    }

    /**
     * 重置数据
     *
     * @param list
     */
    open fun resetDataFromTop(list: List<T>?) {
        if (WheelUtils.isEmpty(list)) {
            throw WheelViewException("join map data is error.")
        }
        postDelayed({
            // 主滚轮切换导致数据变化时副滚轮回到第一项（issue #69）；数据不变时保留位置，
            // 否则初始化时恢复的副滚轮选中项会被主滚轮首次联动回调重置
            val changed = list != mList
            setWheelData(list)
            if (changed) {
                setSelection(0)
            }
            refreshCurrentPosition(true)
        }, 10)
    }

    /**
     * 获取滚轮位置
     *
     * @return
     */
    open fun getSelection(): Int = mSelection

    /**
     * 设置滚轮位置
     *
     * @param selection
     */
    override fun setSelection(selection: Int) {
        mSelection = selection
        if (!mLayoutReady) {
            // 尚未完成首次布局，等拿到 item 高度后再定位
            mSelectionPending = true
            return
        }
        // 定位后的布局过程会回调 onScroll，由 refreshCurrentPosition 更新当前位置和样式
        super.setSelection(getRealPosition(selection))
    }

    /**
     * 连接副WheelView
     *
     * @param wheelView
     */
    override fun join(wheelView: WheelView<*>?) {
        if (wheelView == null) {
            throw WheelViewException("wheelview cannot be null.")
        }
        mJoinWheelView = wheelView
    }

    /**
     * 副WheelView数据
     *
     * @param map key 为主滚轮条目的显示文本（即 toString() 的结果），value 为副滚轮的数据
     */
    override fun joinDatas(map: HashMap<String, List<T>>?) {
        mJoinMap = map
    }

    /**
     * 获得滚轮当前真实位置
     *
     * @param positon
     * @return
     */
    private fun getRealPosition(positon: Int): Int {
        val list = mList
        if (list.isNullOrEmpty()) {
            return 0
        }
        if (mLoop) {
            val d = Int.MAX_VALUE / 2 / list.size
            return positon + d * list.size - mWheelSize / 2
        }
        return positon
    }

    /**
     * 当前滚轮位置
     */
    open val currentPosition: Int
        get() = mCurrentPositon

    /**
     * 当前滚轮位置的数据
     */
    open val selectionItem: T?
        get() {
            val position = max(currentPosition, 0)
            val list = mList
            if (list != null && list.size > position) {
                return list[position]
            }
            return null
        }

    /**
     * 回调给监听器的条目：监听器参数声明为 T，数据为空时与原 Java 实现一样传 null
     */
    @Suppress("UNCHECKED_CAST")
    private fun selectionItemOrNull(): T = selectionItem as T

    /**
     * 设置滚轮数据适配器，已弃用，具体使用[setWheelAdapter]
     *
     * @param adapter
     */
    @Deprecated("Use setWheelAdapter instead", ReplaceWith("setWheelAdapter(adapter)"))
    override fun setAdapter(adapter: ListAdapter?) {
        if (adapter is BaseWheelAdapter<*>) {
            @Suppress("UNCHECKED_CAST")
            setWheelAdapter(adapter as BaseWheelAdapter<T>)
        } else {
            throw WheelViewException("please invoke setWheelAdapter method.")
        }
    }

    /**
     * 设置滚轮数据源适配器
     *
     * @param adapter
     */
    override fun setWheelAdapter(adapter: BaseWheelAdapter<T>) {
        super.setAdapter(adapter)
        mWheelAdapter = adapter
        adapter.setData(mList).setWheelSize(mWheelSize).setLoop(mLoop).setClickable(mClickable)
    }

    /**
     * 设置滚轮数据
     *
     * @param list
     */
    override fun setWheelData(list: List<T>?) {
        if (list.isNullOrEmpty()) {
            throw WheelViewException("wheel datas are error.")
        }
        mList = list
        mWheelAdapter?.setData(list)
    }

    /**
     * 设置选中行附加文本
     *
     * @param text      附加文本
     * @param textColor 文本颜色
     * @param textSize  文本大小
     * @param margin    距中心水平方向的距离
     */
    open fun setExtraText(text: String?, textColor: Int, textSize: Int, margin: Int) {
        setExtraText(text, textColor, textSize, margin, false)
    }

    /**
     * 设置选中行附加文本
     *
     * @param text      附加文本
     * @param textColor 文本颜色
     * @param textSize  文本大小
     * @param margin    距中心水平方向的距离
     * @param textBold  文本是否加粗
     */
    open fun setExtraText(text: String?, textColor: Int, textSize: Int, margin: Int, textBold: Boolean) {
        mExtraText = text
        mExtraTextColor = textColor
        mExtraTextSize = textSize
        mExtraMargin = margin
        mExtraTextBold = textBold
    }

    /**
     * 滚轮数据总数
     */
    open val wheelCount: Int
        get() = mList?.size ?: 0

    /**
     * 平滑的滚动距离
     *
     * @param scrollDistance
     * @return
     */
    private fun getSmoothDistance(scrollDistance: Float): Int = when {
        abs(scrollDistance) <= 2 -> scrollDistance.toInt()
        abs(scrollDistance) < 12 -> if (scrollDistance > 0) 2 else -2
        else -> (scrollDistance / 6).toInt() // 减缓平滑滑动速率
    }

    /**
     * 把最靠近中间的条目对齐到选中框
     */
    private fun adjustPosition() {
        val itemView = getChildAt(0)
        if (itemView == null || mItemH == 0) {
            return
        }
        val deltaY = itemView.y
        val d = if (abs(deltaY) < (mItemH shr 1)) getSmoothDistance(deltaY) else getSmoothDistance(mItemH + deltaY)
        // 已对齐：距离为 0 时 smoothScrollBy 会直接 endFling 并再次回调 IDLE
        if (d == 0) {
            return
        }
        mAdjusting = true
        try {
            smoothScrollBy(d, WheelConstants.WHEEL_SMOOTH_SCROLL_DURATION)
        } finally {
            mAdjusting = false
        }
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // 高度固定为 条目高 × 滚轮个数：ViewPager 等容器会忽略 LayoutParams 强制撑满，
        // 数据一屏放得下时无法滚动，setSelection 等都失效（issue #63）
        val heightSpec = if (mItemH != 0) {
            MeasureSpec.makeMeasureSpec(mItemH * mWheelSize, MeasureSpec.EXACTLY)
        } else {
            heightMeasureSpec
        }
        super.onMeasure(widthMeasureSpec, heightSpec)
    }

    override fun onGenericMotionEvent(event: MotionEvent): Boolean {
        val handled = super.onGenericMotionEvent(event)
        // 鼠标滚轮直接移动列表，不会回调 SCROLL_STATE_IDLE，停止滚动后主动对齐（issue #66）
        if (event.action == MotionEvent.ACTION_SCROLL) {
            removeCallbacks(mAdjustRunnable)
            postDelayed(mAdjustRunnable, WheelConstants.WHEEL_ADJUST_DELAY_DURATION.toLong())
        }
        return handled
    }

    /**
     * 刷新当前位置
     *
     * @param join
     */
    private fun refreshCurrentPosition(join: Boolean) {
        val firstChild = getChildAt(0)
        if (firstChild == null || mItemH == 0) {
            return
        }
        val firstPosition = firstVisiblePosition
        if (mLoop && firstPosition == 0) {
            return
        }
        var position = if (abs(firstChild.y) <= (mItemH shr 1)) firstPosition else firstPosition + 1
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && isInLayout) {
            // 布局过程中修改文字大小会再次 requestLayout 并产生警告（issue #53），推迟到布局结束后刷新样式
            removeCallbacks(mRefreshRunnable)
            post(mRefreshRunnable)
        } else {
            refreshVisibleItems(firstPosition, position + mWheelSize / 2, mWheelSize / 2)
        }
        if (mLoop) {
            position = (position + mWheelSize / 2) % wheelCount
        }
        if (position == mCurrentPositon && !join) {
            return
        }
        mCurrentPositon = position
        mWheelAdapter!!.setCurrentPosition(position)
        mHandler.removeMessages(WheelConstants.WHEEL_SCROLL_HANDLER_WHAT)
        mHandler.sendEmptyMessageDelayed(WheelConstants.WHEEL_SCROLL_HANDLER_WHAT,
            WheelConstants.WHEEL_SCROLL_DELAY_DURATION.toLong())
    }

    /**
     * 刷新可见滚动列表
     *
     * @param firstPosition
     * @param curPosition
     * @param offset
     */
    private fun refreshVisibleItems(firstPosition: Int, curPosition: Int, offset: Int) {
        for (i in curPosition - offset..curPosition + offset) {
            val itemView = getChildAt(i - firstPosition) ?: continue
            if (mWheelAdapter is ArrayWheelAdapter<*> || mWheelAdapter is SimpleWheelAdapter) {
                val textView = itemView.findViewWithTag<TextView>(WheelConstants.WHEEL_ITEM_TEXT_TAG)
                refreshTextView(i, curPosition, itemView, textView)
            } else { // 自定义类型
                val textView = WheelUtils.findTextView(itemView)
                if (textView != null) {
                    refreshTextView(i, curPosition, itemView, textView)
                }
            }
        }
    }

    /**
     * 刷新文本
     *
     * @param position
     * @param curPosition
     * @param itemView
     * @param textView
     */
    private fun refreshTextView(position: Int, curPosition: Int, itemView: View, textView: TextView) {
        val style = mStyle
        if (curPosition == position) { // 选中
            val textColor = style.selectedTextColor ?: style.textColor ?: WheelConstants.WHEEL_TEXT_COLOR
            val defTextSize = (if (style.textSize != -1) style.textSize else WheelConstants.WHEEL_TEXT_SIZE).toFloat()
            val textSize = when {
                style.selectedTextSize != -1 -> style.selectedTextSize.toFloat()
                style.selectedTextZoom != -1f -> defTextSize * style.selectedTextZoom
                else -> defTextSize
            }
            setTextView(itemView, textView, textColor, textSize, 1.0f, 1.0f, style.selectedTextBold)
        } else { // 未选中
            val textColor = style.textColor ?: WheelConstants.WHEEL_TEXT_COLOR
            val textSize = (if (style.textSize != -1) style.textSize else WheelConstants.WHEEL_TEXT_SIZE).toFloat()
            val delta = abs(position - curPosition)
            val alpha = (if (style.textAlpha != -1f) style.textAlpha else WheelConstants.WHEEL_TEXT_ALPHA)
                .toDouble().pow(delta).toFloat()
            // 用缩放实现字号递减：直接改字号会在每帧滚动时触发重新布局
            val scale = if (style.textScale != -1f) style.textScale.toDouble().pow(delta).toFloat() else 1.0f
            setTextView(itemView, textView, textColor, textSize, alpha, scale, false)
        }
    }

    /**
     * 设置TextView
     *
     * @param itemView
     * @param textView
     * @param textColor
     * @param textSize
     * @param textAlpha
     * @param textScale
     */
    private fun setTextView(
        itemView: View, textView: TextView, textColor: Int, textSize: Float,
        textAlpha: Float, textScale: Float, textBold: Boolean,
    ) {
        textView.setTextColor(textColor)
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize)
        textView.scaleX = textScale
        textView.scaleY = textScale
        itemView.alpha = textAlpha
        try {
            textView.paint.isFakeBoldText = textBold
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun dispatchDraw(canvas: Canvas) {
        super.dispatchDraw(canvas)
        val extraText = mExtraText
        if (!TextUtils.isEmpty(extraText)) {
            val targetRect = Rect(0, mItemH * (mWheelSize / 2), width, mItemH * (mWheelSize / 2 + 1))
            mTextPaint.textSize = mExtraTextSize.toFloat()
            mTextPaint.color = mExtraTextColor
            val fontMetrics = mTextPaint.fontMetricsInt
            val baseline = (targetRect.bottom + targetRect.top - fontMetrics.bottom - fontMetrics.top) / 2
            mTextPaint.textAlign = Paint.Align.CENTER
            try {
                mTextPaint.isFakeBoldText = mExtraTextBold
            } catch (e: Exception) {
                e.printStackTrace()
            }
            canvas.drawText(extraText!!, (targetRect.centerX() + mExtraMargin).toFloat(), baseline.toFloat(), mTextPaint)
        }
    }

    enum class Skin { // 滚轮皮肤
        Common, Holo, None
    }

    fun interface OnWheelItemSelectedListener<T> {
        fun onItemSelected(position: Int, t: T)
    }

    fun interface OnWheelItemClickListener<T> {
        fun onItemClick(position: Int, t: T)
    }

    open class WheelViewStyle {

        // 颜色用 null 表示未设置：任何 int 都是合法颜色，用 -1 作标记会让 Color.WHITE 无法生效
        @JvmField
        var backgroundColor: Int? = null // 背景颜色

        @JvmField
        var holoBorderColor: Int? = null // holo样式边框颜色

        @JvmField
        var holoBorderWidth = -1 // holo样式边框宽度

        @JvmField
        var textColor: Int? = null // 文本颜色

        @JvmField
        var selectedTextColor: Int? = null // 选中文本颜色

        @JvmField
        var selectedBackgroundColor: Int? = null // 选中项背景颜色，Common 和 Holo 皮肤生效

        @JvmField
        var textSize = -1 // 文本大小

        @JvmField
        var selectedTextSize = -1 // 选中文本大小

        @JvmField
        var textAlpha = -1f // 文本透明度(0f ~ 1f)

        @JvmField
        var textScale = -1f // 未选中文本缩放比例(0f ~ 1f)，离选中项每远一项再缩放一次

        @JvmField
        var selectedTextZoom = -1f // 选中文本放大倍数

        @JvmField
        var selectedTextBold = false // 选中文本是否加粗

        constructor()

        constructor(style: WheelViewStyle) {
            backgroundColor = style.backgroundColor
            holoBorderColor = style.holoBorderColor
            holoBorderWidth = style.holoBorderWidth
            textColor = style.textColor
            selectedTextColor = style.selectedTextColor
            selectedBackgroundColor = style.selectedBackgroundColor
            textSize = style.textSize
            selectedTextSize = style.selectedTextSize
            textAlpha = style.textAlpha
            textScale = style.textScale
            selectedTextZoom = style.selectedTextZoom
            selectedTextBold = style.selectedTextBold
        }
    }

    companion object {
        // 与 IWheelView 中的常量相同。Kotlin 不能通过实现类访问接口 companion 的常量，
        // 在这里再声明一份，保留 Kotlin 使用方 WheelView.WHEEL_SIZE 的写法
        const val LOOP = IWheelView.LOOP
        const val WHEEL_SIZE = IWheelView.WHEEL_SIZE
        const val CLICKABLE = IWheelView.CLICKABLE
    }
}
