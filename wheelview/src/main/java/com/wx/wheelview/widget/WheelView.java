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
package com.wx.wheelview.widget;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import com.wx.wheelview.adapter.ArrayWheelAdapter;
import com.wx.wheelview.adapter.BaseWheelAdapter;
import com.wx.wheelview.adapter.SimpleWheelAdapter;
import com.wx.wheelview.common.WheelConstants;
import com.wx.wheelview.common.WheelViewException;
import com.wx.wheelview.graphics.DrawableFactory;
import com.wx.wheelview.util.WheelUtils;

import java.util.HashMap;
import java.util.List;

/**
 * 滚轮控件
 *
 * @author venshine
 */
public class WheelView<T> extends ListView implements IWheelView<T> {

    private int mItemH = 0; // 每一项高度
    private int mWheelSize = WHEEL_SIZE;    // 滚轮个数
    private boolean mLoop = LOOP;   // 是否循环滚动
    private List<T> mList = null;   // 滚轮数据列表
    private int mCurrentPositon = -1;    // 记录滚轮当前刻度
    private String mExtraText;  // 添加滚轮选中位置附加文本
    private int mExtraTextColor;    // 附加文本颜色
    private int mExtraTextSize; // 附加文本大小
    private int mExtraMargin;   // 附加文本外边距
    private boolean mExtraTextBold; // 附加文本是否加粗
    private int mSelection = 0; // 选中位置
    private boolean mSelectionPending = false; // 首次布局完成前设置的选中位置，待布局后生效
    private boolean mLayoutReady = false; // 是否已按 item 高度完成首次布局
    private boolean mClickable = CLICKABLE; // 是否可点击

    private Paint mTextPaint;   // 附加文本画笔

    private Skin mSkin = Skin.None; // 皮肤风格

    private WheelViewStyle mStyle;  // 滚轮样式

    private WheelView mJoinWheelView;   // 副WheelView

    private HashMap<String, List<T>> mJoinMap;    // 副滚轮数据列表

    private BaseWheelAdapter<T> mWheelAdapter;

    private OnWheelItemSelectedListener<T> mOnWheelItemSelectedListener;

    private OnWheelItemClickListener<T> mOnWheelItemClickListener;

    @SuppressLint("HandlerLeak")
    private Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            if (msg.what == WheelConstants.WHEEL_SCROLL_HANDLER_WHAT) {
                if (mOnWheelItemSelectedListener != null) {
                    mOnWheelItemSelectedListener.onItemSelected
                            (getCurrentPosition(), getSelectionItem());
                }
                if (mJoinWheelView != null) {
                    if (!mJoinMap.isEmpty()) {
                        // 联动数据的 key 是条目的显示文本（toString），条目不是 String 时也能找到对应数据
                        mJoinWheelView.resetDataFromTop(mJoinMap.get(String.valueOf(mList.get
                                (getCurrentPosition())))
                        );
                    } else {
                        throw new WheelViewException("JoinList is error.");
                    }
                }
            }
        }
    };

    private OnItemClickListener mOnItemClickListener = new OnItemClickListener() {
        @Override
        public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
            if (mOnWheelItemClickListener != null) {
                mOnWheelItemClickListener.onItemClick(getCurrentPosition(), getSelectionItem());
            }
        }
    };

    private OnTouchListener mTouchListener = (v, event) -> {
        v.getParent().requestDisallowInterceptTouchEvent(true);
        return false;
    };

    private boolean mAdjusting = false; // 正在发起对齐滚动，期间同步回调的 IDLE 不再触发对齐

    private final Runnable mAdjustRunnable = this::adjustPosition;

    private final Runnable mRefreshRunnable = () -> refreshCurrentPosition(false);

    private OnScrollListener mOnScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            removeCallbacks(mAdjustRunnable);
            // 部分 ROM 在 smoothScrollBy 内部会同步回调 IDLE，再次对齐会无限递归（issue #41）；
            // 放到回调外执行，在回调里直接 smoothScrollBy 在部分机型上不生效（issue #13）
            if (scrollState == SCROLL_STATE_IDLE && !mAdjusting) {
                post(mAdjustRunnable);
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int
                visibleItemCount, int totalItemCount) {
            if (visibleItemCount != 0) {
                refreshCurrentPosition(false);
            }
        }
    };

    public WheelView(Context context) {
        super(context);
        init();
    }

    public WheelView(Context context, WheelViewStyle style) {
        super(context);
        setStyle(style);
        init();
    }

    public WheelView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public WheelView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        init();
    }

    /**
     * 设置滚轮滑动停止时事件，监听滚轮选中项
     *
     * @param onWheelItemSelectedListener
     */
    public void setOnWheelItemSelectedListener(@Nullable OnWheelItemSelectedListener<T>
                                                       onWheelItemSelectedListener) {
        mOnWheelItemSelectedListener = onWheelItemSelectedListener;
    }

    /**
     * 设置滚轮选中项点击事件
     *
     * @param onWheelItemClickListener
     */
    public void setOnWheelItemClickListener(@Nullable OnWheelItemClickListener<T> onWheelItemClickListener) {
        mOnWheelItemClickListener = onWheelItemClickListener;
    }

    /**
     * 初始化
     */
    private void init() {
        if (mStyle == null) {
            mStyle = new WheelViewStyle();
        }

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        setTag(WheelConstants.TAG);
        setVerticalScrollBarEnabled(false);
        setScrollingCacheEnabled(false);
        setCacheColorHint(Color.TRANSPARENT);
        setFadingEdgeLength(0);
        setOverScrollMode(OVER_SCROLL_NEVER);
        setDividerHeight(0);
        setOnItemClickListener(mOnItemClickListener);
        setOnScrollListener(mOnScrollListener);
        setOnTouchListener(mTouchListener);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            setNestedScrollingEnabled(true);
        }
        addOnGlobalLayoutListener();
    }

    private void addOnGlobalLayoutListener() {
        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver
                .OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                // 控件或父容器不可见、数据未设置时还没有 item，继续等待下一次布局，拿到 item 高度后才移除监听
                if (getChildCount() == 0 || getChildAt(0).getHeight() == 0) {
                    return;
                }
                if (mItemH == 0) {
                    mItemH = getChildAt(0).getHeight();
                    ViewGroup.LayoutParams params = getLayoutParams();
                    params.height = mItemH * mWheelSize;
                    setLayoutParams(params);
                    // 高度变化会触发重新布局，在新高度下完成布局后（下一次回调）再定位，否则会按旧高度定位而错位
                    return;
                }
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                mLayoutReady = true;
                if (mSelectionPending) {
                    mSelectionPending = false;
                    WheelView.super.setSelection(getRealPosition(mSelection));
                }
                refreshVisibleItems(getFirstVisiblePosition(),
                        getCurrentPosition() + mWheelSize / 2,
                        mWheelSize / 2);
                setBackground();
            }
        });
    }

    /**
     * 获得滚轮样式
     *
     * @return
     */
    @NonNull
    public WheelViewStyle getStyle() {
        return mStyle;
    }

    /**
     * 设置滚轮样式
     *
     * @param style
     */
    public void setStyle(@NonNull WheelViewStyle style) {
        mStyle = style;
        // 已完成布局时立即刷新，否则要等到下次滚动才生效
        if (mLayoutReady) {
            refreshCurrentPosition(false);
            setBackground();
        }
    }

    /**
     * 设置背景
     */
    private void setBackground() {
        Drawable drawable = DrawableFactory.createDrawable(mSkin, getWidth(),
                mItemH * mWheelSize, mStyle, mWheelSize, mItemH);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(drawable);
        } else {
            setBackgroundDrawable(drawable);
        }
    }

    /**
     * 获得皮肤风格
     *
     * @return
     */
    @NonNull
    public Skin getSkin() {
        return mSkin;
    }

    /**
     * 设置皮肤风格
     *
     * @param skin
     */
    public void setSkin(@NonNull Skin skin) {
        mSkin = skin;
        if (mLayoutReady) {
            setBackground();
        }
    }

    /**
     * 设置滚轮个数
     *
     * @param wheelSize
     */
    @Override
    public void setWheelSize(int wheelSize) {
        if ((wheelSize & 1) == 0) {
            throw new WheelViewException("wheel size must be an odd number.");
        }
        mWheelSize = wheelSize;
        if (mWheelAdapter != null) {
            mWheelAdapter.setWheelSize(wheelSize);
        }
    }

    /**
     * 设置滚轮是否循环滚动
     *
     * @param loop
     */
    @Override
    public void setLoop(boolean loop) {
        if (loop != mLoop) {
            mLoop = loop;
            setSelection(0);
            if (mWheelAdapter != null) {
                mWheelAdapter.setLoop(loop);
            }
        }
    }

    /**
     * 设置滚轮选中项是否可点击
     *
     * @param clickable
     */
    @Override
    public void setWheelClickable(boolean clickable) {
        if (clickable != mClickable) {
            mClickable = clickable;
            if (mWheelAdapter != null) {
                mWheelAdapter.setClickable(clickable);
            }
        }
    }

    /**
     * 设置是否支持点击滚轮进行选择
     *
     * @param clickToPosition 是否支持点击滚轮进行选择
     */
    public void setClickToPosition(boolean clickToPosition) {
        if (clickToPosition) {
            mWheelAdapter.setOnClickListener(new BaseWheelAdapter.OnClickListener() {
                @Override
                public void onPositionClick(int position) {
                    int deltaPosition = position - getCurrentPosition();
                    if (mLoop) {
                        if (deltaPosition > mWheelSize / 2) {
                            deltaPosition -= getWheelCount();
                        } else if (deltaPosition < -mWheelSize / 2) {
                            deltaPosition += getWheelCount();
                        }
                    }
                    smoothScrollBy(mItemH * deltaPosition, 400);
                }
            });
        } else {
            mWheelAdapter.setOnClickListener(null);
        }
    }

    /**
     * 重置数据
     *
     * @param list
     */
    public void resetDataFromTop(@NonNull final List<T> list) {
        if (WheelUtils.isEmpty(list)) {
            throw new WheelViewException("join map data is error.");
        }
        WheelView.this.postDelayed(new Runnable() {
            @Override
            public void run() {
                // 主滚轮切换导致数据变化时副滚轮回到第一项（issue #69）；数据不变时保留位置，
                // 否则初始化时恢复的副滚轮选中项会被主滚轮首次联动回调重置
                boolean changed = !list.equals(mList);
                setWheelData(list);
                if (changed) {
                    setSelection(0);
                }
                refreshCurrentPosition(true);
            }
        }, 10);
    }

    /**
     * 获取滚轮位置
     *
     * @return
     */
    public int getSelection() {
        return mSelection;
    }

    /**
     * 设置滚轮位置
     *
     * @param selection
     */
    @Override
    public void setSelection(final int selection) {
        mSelection = selection;
        if (!mLayoutReady) {
            // 尚未完成首次布局，等拿到 item 高度后再定位
            mSelectionPending = true;
            return;
        }
        // 定位后的布局过程会回调 onScroll，由 refreshCurrentPosition 更新当前位置和样式
        super.setSelection(getRealPosition(selection));
    }

    /**
     * 连接副WheelView
     *
     * @param wheelView
     */
    @Override
    public void join(@NonNull WheelView wheelView) {
        if (wheelView == null) {
            throw new WheelViewException("wheelview cannot be null.");
        }
        mJoinWheelView = wheelView;
    }

    /**
     * 副WheelView数据
     *
     * @param map key 为主滚轮条目的显示文本（即 toString() 的结果），value 为副滚轮的数据
     */
    public void joinDatas(@NonNull HashMap<String, List<T>> map) {
        mJoinMap = map;
    }

    /**
     * 获得滚轮当前真实位置
     *
     * @param positon
     * @return
     */
    private int getRealPosition(int positon) {
        if (WheelUtils.isEmpty(mList)) {
            return 0;
        }
        if (mLoop) {
            int d = Integer.MAX_VALUE / 2 / mList.size();
            return positon + d * mList.size() - mWheelSize / 2;
        }
        return positon;
    }

    /**
     * 获取当前滚轮位置
     *
     * @return
     */
    public int getCurrentPosition() {
        return mCurrentPositon;
    }

    /**
     * 获取当前滚轮位置的数据
     *
     * @return
     */
    @Nullable
    public T getSelectionItem() {
        int position = getCurrentPosition();
        position = Math.max(position, 0);
        if (mList != null && mList.size() > position) {
            return mList.get(position);
        }
        return null;
    }

    /**
     * 设置滚轮数据适配器，已弃用，具体使用{@link #setWheelAdapter(BaseWheelAdapter)}
     *
     * @param adapter
     */
    @Override
    @Deprecated
    public void setAdapter(ListAdapter adapter) {
        if (adapter instanceof BaseWheelAdapter) {
            setWheelAdapter((BaseWheelAdapter) adapter);
        } else {
            throw new WheelViewException("please invoke setWheelAdapter " +
                    "method.");
        }
    }

    /**
     * 设置滚轮数据源适配器
     *
     * @param adapter
     */
    @Override
    public void setWheelAdapter(@NonNull BaseWheelAdapter<T> adapter) {
        super.setAdapter(adapter);
        mWheelAdapter = adapter;
        mWheelAdapter.setData(mList).setWheelSize(mWheelSize).setLoop(mLoop).setClickable(mClickable);
    }

    /**
     * 设置滚轮数据
     *
     * @param list
     */
    @Override
    public void setWheelData(@NonNull List<T> list) {
        if (WheelUtils.isEmpty(list)) {
            throw new WheelViewException("wheel datas are error.");
        }
        mList = list;
        if (mWheelAdapter != null) {
            mWheelAdapter.setData(list);
        }
    }

    /**
     * 设置选中行附加文本
     *
     * @param text      附加文本
     * @param textColor 文本颜色
     * @param textSize  文本大小
     * @param margin    距中心水平方向的距离
     */
    public void setExtraText(@Nullable String text, int textColor, int textSize, int
            margin) {
        setExtraText(text, textColor, textSize, margin, false);
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
    public void setExtraText(@Nullable String text, int textColor, int textSize, int
            margin, boolean textBold) {
        mExtraText = text;
        mExtraTextColor = textColor;
        mExtraTextSize = textSize;
        mExtraMargin = margin;
        mExtraTextBold = textBold;
    }

    /**
     * 获得滚轮数据总数
     *
     * @return
     */
    public int getWheelCount() {
        return !WheelUtils.isEmpty(mList) ? mList.size() : 0;
    }

    /**
     * 平滑的滚动距离
     *
     * @param scrollDistance
     * @return
     */
    private int getSmoothDistance(float scrollDistance) {
        if (Math.abs(scrollDistance) <= 2) {
            return (int) scrollDistance;
        } else if (Math.abs(scrollDistance) < 12) {
            return scrollDistance > 0 ? 2 : -2;
        } else {
            return (int) (scrollDistance / 6);  // 减缓平滑滑动速率
        }
    }

    /**
     * 把最靠近中间的条目对齐到选中框
     */
    private void adjustPosition() {
        View itemView = getChildAt(0);
        if (itemView == null || mItemH == 0) {
            return;
        }
        float deltaY = itemView.getY();
        int d = Math.abs(deltaY) < mItemH >> 1 ? getSmoothDistance(deltaY)
                : getSmoothDistance(mItemH + deltaY);
        // 已对齐：距离为 0 时 smoothScrollBy 会直接 endFling 并再次回调 IDLE
        if (d == 0) {
            return;
        }
        mAdjusting = true;
        try {
            smoothScrollBy(d, WheelConstants.WHEEL_SMOOTH_SCROLL_DURATION);
        } finally {
            mAdjusting = false;
        }
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        // 高度固定为 条目高 × 滚轮个数：ViewPager 等容器会忽略 LayoutParams 强制撑满，
        // 数据一屏放得下时无法滚动，setSelection 等都失效（issue #63）
        if (mItemH != 0) {
            heightMeasureSpec = MeasureSpec.makeMeasureSpec(mItemH * mWheelSize, MeasureSpec.EXACTLY);
        }
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        boolean handled = super.onGenericMotionEvent(event);
        // 鼠标滚轮直接移动列表，不会回调 SCROLL_STATE_IDLE，停止滚动后主动对齐（issue #66）
        if (event.getAction() == MotionEvent.ACTION_SCROLL) {
            removeCallbacks(mAdjustRunnable);
            postDelayed(mAdjustRunnable, WheelConstants.WHEEL_ADJUST_DELAY_DURATION);
        }
        return handled;
    }

    /**
     * 刷新当前位置
     *
     * @param join
     */
    private void refreshCurrentPosition(boolean join) {
        if (getChildAt(0) == null || mItemH == 0) {
            return;
        }
        int firstPosition = getFirstVisiblePosition();
        if (mLoop && firstPosition == 0) {
            return;
        }
        int position = 0;
        if (Math.abs(getChildAt(0).getY()) <= mItemH >> 1) {
            position = firstPosition;
        } else {
            position = firstPosition + 1;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && isInLayout()) {
            // 布局过程中修改文字大小会再次 requestLayout 并产生警告（issue #53），推迟到布局结束后刷新样式
            removeCallbacks(mRefreshRunnable);
            post(mRefreshRunnable);
        } else {
            refreshVisibleItems(firstPosition, position + mWheelSize / 2,
                    mWheelSize / 2);
        }
        if (mLoop) {
            position = (position + mWheelSize / 2) % getWheelCount();
        }
        if (position == mCurrentPositon && !join) {
            return;
        }
        mCurrentPositon = position;
        mWheelAdapter.setCurrentPosition(position);
        mHandler.removeMessages(WheelConstants.WHEEL_SCROLL_HANDLER_WHAT);
        mHandler.sendEmptyMessageDelayed(WheelConstants
                .WHEEL_SCROLL_HANDLER_WHAT, WheelConstants
                .WHEEL_SCROLL_DELAY_DURATION);
    }

    /**
     * 刷新可见滚动列表
     *
     * @param firstPosition
     * @param curPosition
     * @param offset
     */
    private void refreshVisibleItems(int firstPosition, int curPosition, int
            offset) {
        for (int i = curPosition - offset; i <= curPosition + offset; i++) {
            View itemView = getChildAt(i - firstPosition);
            if (itemView == null) {
                continue;
            }
            if (mWheelAdapter instanceof ArrayWheelAdapter || mWheelAdapter
                    instanceof SimpleWheelAdapter) {
                TextView textView = itemView.findViewWithTag
                        (WheelConstants.WHEEL_ITEM_TEXT_TAG);
                refreshTextView(i, curPosition, itemView, textView);
            } else {    // 自定义类型
                TextView textView = WheelUtils.findTextView(itemView);
                if (textView != null) {
                    refreshTextView(i, curPosition, itemView, textView);
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
    private void refreshTextView(int position, int curPosition, View
            itemView, TextView textView) {
        if (curPosition == position) { // 选中
            int textColor = mStyle.selectedTextColor != null ? mStyle
                    .selectedTextColor : (mStyle.textColor != null ? mStyle
                    .textColor : WheelConstants.WHEEL_TEXT_COLOR);
            float defTextSize = mStyle.textSize != -1 ? mStyle.textSize :
                    WheelConstants.WHEEL_TEXT_SIZE;
            float textSize = mStyle.selectedTextSize != -1 ? mStyle
                    .selectedTextSize : (mStyle.selectedTextZoom != -1 ?
                    (defTextSize * mStyle.selectedTextZoom) :
                    defTextSize);
            boolean textBold = mStyle.selectedTextBold;
            setTextView(itemView, textView, textColor, textSize, 1.0f, 1.0f, textBold);
        } else {    // 未选中
            int textColor = mStyle.textColor != null ? mStyle.textColor :
                    WheelConstants.WHEEL_TEXT_COLOR;
            float textSize = mStyle.textSize != -1 ? mStyle.textSize :
                    WheelConstants.WHEEL_TEXT_SIZE;
            int delta = Math.abs(position - curPosition);
            float alpha = (float) Math.pow(mStyle.textAlpha != -1 ? mStyle.textAlpha :
                    WheelConstants
                            .WHEEL_TEXT_ALPHA, delta);
            // 用缩放实现字号递减：直接改字号会在每帧滚动时触发重新布局
            float scale = mStyle.textScale != -1 ? (float) Math.pow(mStyle.textScale, delta) : 1.0f;
            setTextView(itemView, textView, textColor, textSize, alpha, scale, false);
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
    private void setTextView(View itemView, TextView textView, int textColor, float textSize,
                             float textAlpha, float textScale, boolean textBold) {
        textView.setTextColor(textColor);
        textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, textSize);
        textView.setScaleX(textScale);
        textView.setScaleY(textScale);
        itemView.setAlpha(textAlpha);
        try {
            textView.getPaint().setFakeBoldText(textBold);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);
        if (!TextUtils.isEmpty(mExtraText)) {
            Rect targetRect = new Rect(0, mItemH * (mWheelSize / 2), getWidth
                    (), mItemH * (mWheelSize / 2 + 1));
            mTextPaint.setTextSize(mExtraTextSize);
            mTextPaint.setColor(mExtraTextColor);
            Paint.FontMetricsInt fontMetrics = mTextPaint.getFontMetricsInt();
            int baseline = (targetRect.bottom + targetRect.top - fontMetrics
                    .bottom - fontMetrics.top) / 2;
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            try {
                mTextPaint.setFakeBoldText(mExtraTextBold);
            } catch (Exception e) {
                e.printStackTrace();
            }
            canvas.drawText(mExtraText, targetRect.centerX() + mExtraMargin,
                    baseline, mTextPaint);
        }
    }

    public enum Skin { // 滚轮皮肤
        Common, Holo, None
    }

    public interface OnWheelItemSelectedListener<T> {
        void onItemSelected(int position, T t);
    }

    public interface OnWheelItemClickListener<T> {
        void onItemClick(int position, T t);
    }

    public static class WheelViewStyle {

        // 颜色用 null 表示未设置：任何 int 都是合法颜色，用 -1 作标记会让 Color.WHITE 无法生效
        @Nullable
        public Integer backgroundColor; // 背景颜色
        @Nullable
        public Integer holoBorderColor;   // holo样式边框颜色
        public int holoBorderWidth = -1;//holo样式边框宽度
        @Nullable
        public Integer textColor; // 文本颜色
        @Nullable
        public Integer selectedTextColor; // 选中文本颜色
        @Nullable
        public Integer selectedBackgroundColor; // 选中项背景颜色，Common 和 Holo 皮肤生效
        public int textSize = -1;// 文本大小
        public int selectedTextSize = -1;   // 选中文本大小
        public float textAlpha = -1;  // 文本透明度(0f ~ 1f)
        public float textScale = -1;  // 未选中文本缩放比例(0f ~ 1f)，离选中项每远一项再缩放一次
        public float selectedTextZoom = -1; // 选中文本放大倍数
        public boolean selectedTextBold; // 选中文本是否加粗

        public WheelViewStyle() {
        }

        public WheelViewStyle(@NonNull WheelViewStyle style) {
            this.backgroundColor = style.backgroundColor;
            this.holoBorderColor = style.holoBorderColor;
            this.holoBorderWidth = style.holoBorderWidth;
            this.textColor = style.textColor;
            this.selectedTextColor = style.selectedTextColor;
            this.selectedBackgroundColor = style.selectedBackgroundColor;
            this.textSize = style.textSize;
            this.selectedTextSize = style.selectedTextSize;
            this.textAlpha = style.textAlpha;
            this.textScale = style.textScale;
            this.selectedTextZoom = style.selectedTextZoom;
            this.selectedTextBold = style.selectedTextBold;
        }

    }

}
