package com.wx.wheelview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
import android.text.TextUtils;
import android.util.AttributeSet;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.AbsListView;
import android.widget.ListAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.List;

/**
 * 滚轮控件
 *
 * @author fengwx
 */
public class WheelView<T> extends ListView implements IWheelView<T> {

    private static final int[] SHADOWS_COLORS =
            {
                    0xFF111111,
                    0x00AAAAAA,
                    0x00AAAAAA
            };  // 阴影色值
    private int mItemH = 0; // 每一项高度
    private int mWheelSize = WHEEL_SIZE;    // 滚轮个数
    private boolean mLoop = LOOP;   // 是否循环滚动
    private List<T> mList = null;   // 滚轮数据列表
    private int mCurrentPositon = -1;    // 记录滚轮当前刻度
    private String mExtraText;  // 添加滚轮选中位置附加文本
    private int mExtraTextColor;    // 附加文本颜色
    private int mExtraTextSize; // 附加文本大小
    private int mExtraMargin;   // 附加文本外边距

    private Paint mCommonBgPaint, mHoloBgPaint, mHoloPaint, mCommonPaint, mCommonDividerPaint, mTextPaint, mCommonBorderPaint;

    private GradientDrawable mTopShadow = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            SHADOWS_COLORS);    // 顶部阴影

    private GradientDrawable mBottomShadow = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
            SHADOWS_COLORS);    // 底部阴影

    private Skin mSkin = Skin.None; // 皮肤风格

    private WheelViewStyle mStyle;  // 滚轮样式

    private BaseWheelAdapter<T> mWheelAdapter;

    private OnWheelItemSelectedListener<T> mOnWheelItemSelectedListener;

    private OnScrollListener onScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == SCROLL_STATE_IDLE) {
                View itemView = getChildAt(0);
                if (itemView != null) {
                    float deltaY = itemView.getY();
                    if (deltaY == 0 || mItemH == 0) {
                        return;
                    }
                    if (Math.abs(deltaY) < mItemH / 2) {
                        int d = getSmoothDistance(deltaY);
                        smoothScrollBy(d, WheelConstants.WHEEL_SMOOTH_SCROLL_DURATION);
                    } else {
                        int d = getSmoothDistance(mItemH + deltaY);
                        smoothScrollBy(d, WheelConstants.WHEEL_SMOOTH_SCROLL_DURATION);
                    }
                }
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            if (visibleItemCount != 0) {
                refreshCurrentPosition();
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

    public void setOnWheelItemSelectedListener(OnWheelItemSelectedListener<T> onWheelItemSelectedListener) {
        mOnWheelItemSelectedListener = onWheelItemSelectedListener;
    }

    /**
     * 初始化
     */
    private void init() {
        if (mStyle == null) {
            mStyle = new WheelViewStyle();
        }

        initPaint();

        setVerticalScrollBarEnabled(false);
        setScrollingCacheEnabled(false);
        setCacheColorHint(Color.TRANSPARENT);
        setFadingEdgeLength(0);
        setOverScrollMode(OVER_SCROLL_NEVER);
        setDividerHeight(0);
        setOnScrollListener(onScrollListener);

        setBackground();

        getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
            @Override
            public void onGlobalLayout() {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
                    getViewTreeObserver().removeOnGlobalLayoutListener(this);
                } else {
                    getViewTreeObserver().removeGlobalOnLayoutListener(this);
                }
                if (getChildCount() > 0 && mItemH == 0) {
                    mItemH = getChildAt(0).getHeight();
                    if (mItemH != 0) {
                        ViewGroup.LayoutParams params = getLayoutParams();
                        params.height = mItemH * mWheelSize;
                        refreshVisibleItems(getFirstVisiblePosition(), getCurrentPosition() + mWheelSize / 2,
                                mWheelSize / 2);
                    } else {
                        throw new WheelViewException("wheel item is error.");
                    }
                }
            }
        });
    }

    private void initPaint() {
        mCommonBgPaint = new Paint();
        mCommonBgPaint.setColor(mStyle.backgroundColor != -1 ? mStyle.backgroundColor : WheelConstants
                .WHEEL_SKIN_COMMON_BG);

        mHoloBgPaint = new Paint();
        mHoloBgPaint.setColor(mStyle.backgroundColor != -1 ? mStyle.backgroundColor : WheelConstants
                .WHEEL_SKIN_HOLO_BG);

        mHoloPaint = new Paint();
        mHoloPaint.setColor(mStyle.holoBorderColor != -1 ? mStyle.holoBorderColor : WheelConstants
                .WHEEL_SKIN_HOLO_BORDER_COLOR);
        mHoloPaint.setStrokeWidth(3);

        mCommonPaint = new Paint();
        mCommonPaint.setColor(WheelConstants.WHEEL_SKIN_COMMON_COLOR);

        mCommonDividerPaint = new Paint();
        mCommonDividerPaint.setColor(WheelConstants.WHEEL_SKIN_COMMON_DIVIDER_COLOR);
        mCommonDividerPaint.setStrokeWidth(2);

        mTextPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

        mCommonBorderPaint = new Paint();
        mCommonBorderPaint.setStrokeWidth(6);
        mCommonBorderPaint.setColor(WheelConstants.WHEEL_SKIN_COMMON_BORDER_COLOR);
    }

    /**
     * 获得滚轮样式
     *
     * @return
     */
    public WheelViewStyle getStyle() {
        return mStyle;
    }

    /**
     * 设置滚轮样式
     *
     * @param style
     */
    public void setStyle(WheelViewStyle style) {
        mStyle = style;
    }

    /**
     * 设置背景
     */
    private void setBackground() {
        if (mSkin.equals(Skin.Common)) {
            drawCommonBg();
        } else if (mSkin.equals(Skin.Holo)) {
            drawHoloBg();
        } else if (mSkin.equals(Skin.None)) {
            drawBg();
        }
    }

    /**
     * 背景
     */
    private void drawBg() {
        setBackgroundColor(mStyle.backgroundColor != -1 ? mStyle.backgroundColor : WheelConstants.WHEEL_BG);
    }

    /**
     * holo背景
     */
    private void drawHoloBg() {
        Drawable drawable = new Drawable() {
            @Override
            public void draw(Canvas canvas) {
                int width = getWidth();
                // draw background
                canvas.drawRect(0, 0, width, getHeight(), mHoloBgPaint);

                // draw select border
                if (mItemH != 0) {
                    canvas.drawLine(0, mItemH * (mWheelSize / 2), width, mItemH
                            * (mWheelSize / 2), mHoloPaint);
                    canvas.drawLine(0, mItemH * (mWheelSize / 2 + 1), width, mItemH
                            * (mWheelSize / 2 + 1), mHoloPaint);
                }
            }

            @Override
            public void setAlpha(int alpha) {
            }

            @Override
            public void setColorFilter(ColorFilter colorFilter) {
            }

            @Override
            public int getOpacity() {
                return 0;
            }
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            setBackground(drawable);
        } else {
            setBackgroundDrawable(drawable);
        }
    }

    /**
     * common背景
     */
    private void drawCommonBg() {
        Drawable drawable = new Drawable() {
            @Override
            public void draw(Canvas canvas) {
                int width = getWidth();
                // draw background
                canvas.drawRect(0, 0, width, getHeight(), mCommonBgPaint);

                // draw select border
                if (mItemH != 0) {
                    canvas.drawRect(0, mItemH * (mWheelSize / 2), width, mItemH
                            * (mWheelSize / 2 + 1), mCommonPaint);
                    canvas.drawLine(0, mItemH * (mWheelSize / 2), width, mItemH
                            * (mWheelSize / 2), mCommonDividerPaint);
                    canvas.drawLine(0, mItemH * (mWheelSize / 2 + 1), width, mItemH
                            * (mWheelSize / 2 + 1), mCommonDividerPaint);
                }
            }

            @Override
            public void setAlpha(int alpha) {
            }

            @Override
            public void setColorFilter(ColorFilter colorFilter) {
            }

            @Override
            public int getOpacity() {
                return 0;
            }
        };
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
    public Skin getSkin() {
        return mSkin;
    }

    /**
     * 设置皮肤风格
     *
     * @param skin
     */
    public void setSkin(Skin skin) {
        mSkin = skin;
        setBackground();
    }

    /**
     * 设置滚轮个数
     *
     * @param wheelSize
     */
    @Override
    public void setWheelSize(int wheelSize) {
        if (wheelSize < 3 || ((wheelSize & 1) == 0)) {
            throw new WheelViewException("wheel size must be an odd number.");
        }
        if (mList != null && wheelSize > mList.size()) {
            throw new WheelViewException("wheel datas cannot be smaller than wheel size.");
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
     * 设置滚轮位置
     *
     * @param selection
     */
    @Override
    public void setSelection(final int selection) {
        setVisibility(View.INVISIBLE);
        WheelView.this.postDelayed(new Runnable() {
            @Override
            public void run() {
                WheelView.super.setSelection(getRealPosition(selection));
                refreshCurrentPosition();
                setVisibility(View.VISIBLE);
            }
        }, 200);
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
    public T getSelectionItem() {
        int position = getCurrentPosition();
        position = position < 0 ? 0 : position;
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
        if (adapter != null && adapter instanceof BaseWheelAdapter) {
            setWheelAdapter((BaseWheelAdapter) adapter);
        } else {
            throw new WheelViewException("please invoke setWheelAdapter method.");
        }
    }

    /**
     * 设置滚轮数据源适配器
     *
     * @param adapter
     */
    @Override
    public void setWheelAdapter(BaseWheelAdapter<T> adapter) {
        super.setAdapter(adapter);
        mWheelAdapter = adapter;
        mWheelAdapter.setData(mList).setLoop(mLoop).setWheelSize(mWheelSize);
    }

    /**
     * 设置滚轮数据
     *
     * @param list
     */
    @Override
    public void setWheelData(List<T> list) {
        if (list == null || (list != null && list.size() < mWheelSize)) {
            throw new WheelViewException("wheel datas cannot be smaller than wheel size.");
        }
        mList = list;
        if (mWheelAdapter != null) {
            mWheelAdapter.setData(list);
        }
    }

    /**
     * 设置选中行附加文本
     *
     * @param text
     * @param textColor
     * @param textSize
     * @param margin
     */
    public void setExtraText(String text, int textColor, int textSize, int margin) {
        mExtraText = text;
        mExtraTextColor = textColor;
        mExtraTextSize = textSize;
        mExtraMargin = margin;
    }

    /**
     * 获得wheel数据总数
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
     * 刷新当前位置
     */
    private void refreshCurrentPosition() {
        if (getChildAt(0) == null || mItemH == 0) {
            return;
        }
        int firstPosition = getFirstVisiblePosition();
        if (mLoop && firstPosition == 0) {
            return;
        }
        int position = 0;
        if (Math.abs(getChildAt(0).getY()) <= mItemH / 2) {
            position = firstPosition;
        } else {
            position = firstPosition + 1;
        }
        refreshVisibleItems(firstPosition, position + mWheelSize / 2, mWheelSize / 2);
        if (mLoop) {
            position = (position + mWheelSize / 2) % getWheelCount();
        }
        if (position == mCurrentPositon) {
            return;
        }
        mCurrentPositon = position;
        if (mOnWheelItemSelectedListener != null) {
            mOnWheelItemSelectedListener.onItemSelected(getCurrentPosition(), getSelectionItem());
        }
    }

    /**
     * 刷新可见滚动列表
     *
     * @param firstPosition
     * @param curPosition
     * @param offset
     */
    private void refreshVisibleItems(int firstPosition, int curPosition, int offset) {
        for (int i = curPosition - offset; i <= curPosition + offset; i++) {
            View itemView = getChildAt(i - firstPosition);
            if (itemView == null) {
                continue;
            }
            if (mWheelAdapter instanceof ArrayWheelAdapter || mWheelAdapter instanceof SimpleWheelAdapter) {
                TextView textView = (TextView) itemView.findViewWithTag(WheelConstants.WHEEL_ITEM_TEXT_TAG);
                if (curPosition == i) { // 选中
                    textView.setTextColor(mStyle.selectedTextColor != -1 ? mStyle.selectedTextColor : (mStyle.textColor !=
                            -1 ? mStyle.textColor : WheelConstants.WHEEL_TEXT_COLOR));
                    itemView.setAlpha(1f);
                } else {    // 未选中
                    textView.setTextColor(mStyle.textColor != -1 ? mStyle.textColor : WheelConstants.WHEEL_TEXT_COLOR);
                    int delta = Math.abs(i - curPosition);
                    float alpha = (float) Math.pow(0.7f, delta);
                    itemView.setAlpha(alpha);
                }
            } else {    // 自定义类型
                TextView textView = WheelUtils.findTextView(itemView);
                if (textView != null) {
                    if (curPosition == i) { // 选中
                        textView.setTextColor(mStyle.selectedTextColor != -1 ? mStyle.selectedTextColor : (mStyle.textColor !=
                                -1 ? mStyle.textColor : WheelConstants.WHEEL_TEXT_COLOR));
                        itemView.setAlpha(1f);
                    } else {    // 未选中
                        textView.setTextColor(mStyle.textColor != -1 ? mStyle.textColor : WheelConstants.WHEEL_TEXT_COLOR);
                        int delta = Math.abs(i - curPosition);
                        float alpha = (float) Math.pow(0.7f, delta);
                        itemView.setAlpha(alpha);
                    }
                }
            }
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        int width = getWidth();
        int height = getHeight();

        if (!TextUtils.isEmpty(mExtraText)) {
            Rect targetRect = new Rect(0, mItemH * (mWheelSize / 2), width, mItemH * (mWheelSize / 2 + 1));
            mTextPaint.setTextSize(mExtraTextSize);
            mTextPaint.setColor(mExtraTextColor);
            Paint.FontMetricsInt fontMetrics = mTextPaint.getFontMetricsInt();
            int baseline = (targetRect.bottom + targetRect.top - fontMetrics.bottom - fontMetrics.top) / 2;
            mTextPaint.setTextAlign(Paint.Align.CENTER);
            canvas.drawText(mExtraText, targetRect.centerX() + mExtraMargin, baseline, mTextPaint);
        }

        if (mSkin.equals(Skin.Common)) {
            if (mItemH == 0) {
                return;
            }
            int h = mItemH;
            mTopShadow.setBounds(0, 0, width, h);
            mTopShadow.draw(canvas);

            mBottomShadow.setBounds(0, height - h, width, height);
            mBottomShadow.draw(canvas);

            canvas.drawLine(0, 0, 0, height, mCommonBorderPaint);
            canvas.drawLine(width, 0, width, height, mCommonBorderPaint);
        }
    }

    public enum Skin { // 滚轮皮肤
        Common, Holo, None
    }

    public interface OnWheelItemSelectedListener<T> {
        void onItemSelected(int position, T t);
    }

    public static class WheelViewStyle {

        public int backgroundColor = -1; // 背景颜色
        public int holoBorderColor = -1;   // holo样式边框颜色
        public int textColor = -1; // 文本颜色
        public int selectedTextColor = -1; // 选中文本颜色

        public WheelViewStyle() {
        }

        public WheelViewStyle(WheelViewStyle style) {
            this.backgroundColor = style.backgroundColor;
            this.holoBorderColor = style.holoBorderColor;
            this.textColor = style.textColor;
            this.selectedTextColor = style.selectedTextColor;
        }

    }

}
