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
package com.wx.wheelview.adapter

import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import com.wx.wheelview.util.WheelUtils
import com.wx.wheelview.widget.IWheelView

/**
 * 滚轮抽象数据适配器
 *
 * @author venshine
 */
@JvmSuppressWildcards
abstract class BaseWheelAdapter<T> : BaseAdapter() {

    /**
     * 滚轮数据，子类在 [bindView] 中按 position 取数据
     *
     * 声明为非空，便于 Kotlin 子类直接 mList[position]；未设置数据时为空列表。
     * Java 子类直接赋值 null 时与原实现一样，在取数据时抛出 NullPointerException
     */
    @JvmField
    protected var mList: List<T> = emptyList()

    private var mLoop = IWheelView.LOOP

    private var mWheelSize = IWheelView.WHEEL_SIZE

    private var mClickable = IWheelView.CLICKABLE

    private var mCurrentPositon = -1

    private var mOnClickListener: OnClickListener? = null

    protected abstract fun bindView(position: Int, convertView: View?, parent: ViewGroup): View?

    /**
     * 设置当前刻度
     *
     * @param position
     */
    fun setCurrentPosition(position: Int) {
        mCurrentPositon = position
    }

    open fun setOnClickListener(onClickListener: OnClickListener?) {
        mOnClickListener = onClickListener
    }

    final override fun getCount(): Int {
        if (mLoop) {
            return Int.MAX_VALUE
        }
        return if (!WheelUtils.isEmpty(mList)) mList.size + mWheelSize - 1 else 0
    }

    final override fun getItemId(position: Int): Long =
        if (!WheelUtils.isEmpty(mList)) (position % mList.size).toLong() else position.toLong()

    final override fun getItem(position: Int): T? =
        if (!WheelUtils.isEmpty(mList)) mList[position % mList.size] else null

    override fun areAllItemsEnabled(): Boolean = !mClickable

    override fun isEnabled(position: Int): Boolean {
        if (mClickable) {
            if (mLoop) {
                if (position % mList.size == mCurrentPositon) {
                    return true
                }
            } else {
                if (position == mCurrentPositon + mWheelSize / 2) {
                    return true
                }
            }
        }
        return false
    }

    final override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
        val realPosition = if (mLoop) {
            position % mList.size
        } else if (position < mWheelSize / 2 || position >= mWheelSize / 2 + mList.size) {
            -1
        } else {
            position - mWheelSize / 2
        }
        val view = bindView(if (realPosition == -1) 0 else realPosition, convertView, parent)!!
        // 循环模式也要显式设为可见：运行时从非循环切到循环后，被隐藏的首尾占位行会被复用到循环位置
        view.visibility = if (realPosition == -1) View.INVISIBLE else View.VISIBLE
        val listener = mOnClickListener
        if (listener != null) {
            view.setOnClickListener { listener.onPositionClick(realPosition) }
        }
        return view
    }

    fun setClickable(clickable: Boolean): BaseWheelAdapter<T> {
        if (clickable != mClickable) {
            mClickable = clickable
            super.notifyDataSetChanged()
        }
        return this
    }

    fun setLoop(loop: Boolean): BaseWheelAdapter<T> {
        if (loop != mLoop) {
            mLoop = loop
            super.notifyDataSetChanged()
        }
        return this
    }

    fun setWheelSize(wheelSize: Int): BaseWheelAdapter<T> {
        mWheelSize = wheelSize
        super.notifyDataSetChanged()
        return this
    }

    fun setData(list: List<T>?): BaseWheelAdapter<T> {
        mList = list ?: emptyList()
        super.notifyDataSetChanged()
        return this
    }

    /**
     * 数据已改变，重绘可见区域
     */
    @Deprecated("Use setData / setLoop / setWheelSize / setClickable instead")
    final override fun notifyDataSetChanged() {
        super.notifyDataSetChanged()
    }

    /**
     * 数据失效，重绘控件
     */
    @Deprecated("Use setData instead")
    final override fun notifyDataSetInvalidated() {
        super.notifyDataSetInvalidated()
    }

    fun interface OnClickListener {
        fun onPositionClick(position: Int)
    }
}
