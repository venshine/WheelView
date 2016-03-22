package com.wx.wheelview;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.Paint;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.os.Build;
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
public class WheelView<T> extends ListView implements IWheelView {

    private int mItemH = 0; // 每一项高度

    private int mWheelSize = WHEEL_SIZE;    // 滚轮个数

    private boolean mLoop = LOOP;   // 是否循环滚动

    private List<T> mList = null;   // 滚轮数据列表

    private int mCurrentPositon = 0;    // 记录滚轮当前刻度

    private GradientDrawable mTopShadow = new GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,
            SHADOWS_COLORS);    // 顶部阴影
    ;
    private GradientDrawable mBottomShadow = new GradientDrawable(GradientDrawable.Orientation.BOTTOM_TOP,
            SHADOWS_COLORS);    // 底部阴影
    ;
    private static final int[] SHADOWS_COLORS =
            {
                    0xFF111111,
                    0x00AAAAAA,
                    0x00AAAAAA
            };  // 阴影色值

    private Skin mSkin = Skin.None; // 皮肤风格

    private WheelViewStyle mStyle;  // 滚轮样式

    private BaseWheelAdapter<T> mBaseWheelAdapter;

    private OnWheelItemSelectedListener<T> mOnWheelItemSelectedListener;

    public void setOnWheelItemSelectedListener(OnWheelItemSelectedListener<T> onWheelItemSelectedListener) {
        mOnWheelItemSelectedListener = onWheelItemSelectedListener;
    }

    public enum Skin { // 滚轮皮肤
        Common, Holo, None
    }

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
     * 初始化
     */
    private void init() {
        if (mStyle == null) {
            mStyle = new WheelViewStyle();
        }
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
                getViewTreeObserver().removeGlobalOnLayoutListener(this);
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

    /**
     * 设置滚轮样式
     *
     * @param style
     */
    public void setStyle(WheelViewStyle style) {
        mStyle = style;
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
     * 设置背景
     */
    private void setBackground() {
        if (mSkin.equals(Skin.Common)) {
            drawCommonBg();
        } else if (mSkin.equals(Skin.Holo)) {
            drawHoloBg();
        }
    }

    /**
     * holo背景
     */
    private void drawHoloBg() {
        Drawable drawable = new Drawable() {
            @Override
            public void draw(Canvas canvas) {
                // draw background
                Paint paint = new Paint();
                paint.setColor(mStyle.backgroundColor != -1 ? mStyle.backgroundColor : WheelConstants
                        .WHEEL_SKIN_HOLO_BG);
                canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

                // draw select border
                int viewWidth = getWidth();
                Paint dividerPaint = new Paint();
                dividerPaint.setColor(mStyle.holoBorderColor != -1 ? mStyle.holoBorderColor : WheelConstants
                        .WHEEL_SKIN_HOLO_BORDER_COLOR);
                dividerPaint.setStrokeWidth(3);
                canvas.drawLine(0, mItemH * (mWheelSize / 2), viewWidth, mItemH
                        * (mWheelSize / 2), dividerPaint);
                canvas.drawLine(0, mItemH * (mWheelSize / 2 + 1), viewWidth, mItemH
                        * (mWheelSize / 2 + 1), dividerPaint);
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
                // draw background
                Paint paint = new Paint();
                paint.setColor(mStyle.backgroundColor != -1 ? mStyle.backgroundColor : WheelConstants
                        .WHEEL_SKIN_COMMON_BG);
                canvas.drawRect(0, 0, getWidth(), getHeight(), paint);

                // draw select border
                int viewWidth = getWidth();
                Paint dividerPaint = new Paint();
                dividerPaint.setColor(Color.parseColor("#b5b5b5"));
                dividerPaint.setStrokeWidth(2);
                Paint seletedSolidPaint = new Paint();
                seletedSolidPaint.setColor(Color.parseColor("#f0cfcfcf"));
                canvas.drawRect(0, mItemH * (mWheelSize / 2), viewWidth, mItemH
                        * (mWheelSize / 2 + 1), seletedSolidPaint);
                canvas.drawLine(0, mItemH * (mWheelSize / 2), viewWidth, mItemH
                        * (mWheelSize / 2), dividerPaint);
                canvas.drawLine(0, mItemH * (mWheelSize / 2 + 1), viewWidth, mItemH
                        * (mWheelSize / 2 + 1), dividerPaint);
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
     * 设置皮肤风格
     *
     * @param skin
     */
    public void setSkin(Skin skin) {
        mSkin = skin;
        setBackground();
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
     * 设置滚轮个数
     *
     * @param wheelSize
     */
    public void setWheelSize(int wheelSize) {
        if (wheelSize < 3 || ((wheelSize & 1) == 0)) {
            throw new WheelViewException("wheel size must be an odd number.");
        }
        if (mList != null && wheelSize > mList.size()) {
            throw new WheelViewException("wheel datas cannot be smaller than wheel size.");
        }
        mWheelSize = wheelSize;
        if (mBaseWheelAdapter != null) {
            mBaseWheelAdapter.setWheelSize(wheelSize);
        }
    }

    /**
     * 设置滚轮是否循环滚动
     *
     * @param loop
     */
    public void setLoop(boolean loop) {
        if (loop != mLoop) {
            mLoop = loop;
            if (mBaseWheelAdapter != null) {
                mBaseWheelAdapter.setLoop(loop);
            }
        }
    }

    /**
     * 设置滚轮位置
     *
     * @param position
     */
    @Override
    public void setSelection(final int position) {
        post(new Runnable() {
            @Override
            public void run() {
                mCurrentPositon = getRealPosition(position);
                WheelView.super.setSelection(mCurrentPositon);
            }
        });
    }

    /**
     * @param positon
     * @return
     */
    private int getRealPosition(int positon) {
        if (mList == null || mList.size() == 0) {
            return 0;
        }
        if (mLoop) {
            int d = Integer.MAX_VALUE / 2 / mList.size();
            return positon + d * mList.size();
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
     * 设置滚轮数据适配器
     *
     * @param adapter
     */
    public void setWheelAdapter(BaseWheelAdapter<T> adapter) {
        super.setAdapter(adapter);
        mBaseWheelAdapter = adapter;
        mBaseWheelAdapter.setData(mList).setLoop(mLoop).setWheelSize(mWheelSize);
    }

    /**
     * 设置滚轮数据
     *
     * @param list
     */
    public void setWheelData(List<T> list) {
        if (list == null || (list != null && list.size() < mWheelSize)) {
            throw new WheelViewException("wheel datas cannot be smaller than wheel size.");
        }
        mList = list;
        if (mBaseWheelAdapter != null) {
            mBaseWheelAdapter.setData(list);
        }
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
     * 滚动时刷新当前位置
     */
    private void refreshCurrentPosition() {
        int firstPosition = getFirstVisiblePosition();
        int position = 0;
        if (getChildAt(0) == null) {
            return;
        }
        if (Math.abs(getChildAt(0).getY()) <= mItemH / 2) {
            position = firstPosition;
        } else {
            position = firstPosition + 1;
        }
        if (position == mCurrentPositon) {
            return;
        }
        mCurrentPositon = position;
        if (mOnWheelItemSelectedListener != null) {
            mOnWheelItemSelectedListener.onItemSelected(getCurrentPosition(), getSelectionItem());
        }
        refreshVisibleItems(firstPosition, position + mWheelSize / 2, mWheelSize / 2);
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
            TextView labelTv = (TextView) itemView.findViewWithTag(WheelConstants.WHEEL_ITEM_TEXT_TAG);
            if (curPosition == i) { // 选中
                labelTv.setTextColor(mStyle.selectedTextColor != -1 ? mStyle.selectedTextColor : (mStyle.textColor !=
                        -1 ? mStyle.textColor : WheelConstants.WHEEL_ITEM_TEXT_TAG));
                itemView.setAlpha(1f);
            } else {    // 未选中
                labelTv.setTextColor(mStyle.textColor != -1 ? mStyle.textColor : WheelConstants.WHEEL_ITEM_TEXT_TAG);
                int delta = Math.abs(i - curPosition);
                double alpha = Math.pow(0.7f, delta);
                itemView.setAlpha((float) alpha);
            }
        }
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        super.dispatchDraw(canvas);

        if (mSkin.equals(Skin.Common)) {
            int height = mItemH;
            mTopShadow.setBounds(0, 0, getWidth(), height);
            mTopShadow.draw(canvas);

            mBottomShadow.setBounds(0, getHeight() - height, getWidth(), getHeight());
            mBottomShadow.draw(canvas);

            Paint paint = new Paint();
            paint.setStrokeWidth(6);
            paint.setColor(Color.parseColor("#666666"));
            canvas.drawLine(0, 0, 0, getHeight(), paint);
            canvas.drawLine(getWidth(), 0, getWidth(), getHeight(), paint);
        }
    }

    private OnScrollListener onScrollListener = new OnScrollListener() {
        @Override
        public void onScrollStateChanged(AbsListView view, int scrollState) {
            if (scrollState == SCROLL_STATE_IDLE) {
                View itemView = getChildAt(0);
                if (itemView != null) {
                    float deltaY = itemView.getY();
                    if (deltaY == 0) {
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
            } else if (scrollState == SCROLL_STATE_TOUCH_SCROLL) {
            } else if (scrollState == SCROLL_STATE_FLING) {
            }
        }

        @Override
        public void onScroll(AbsListView view, int firstVisibleItem, int visibleItemCount, int totalItemCount) {
            refreshCurrentPosition();
        }
    };

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
