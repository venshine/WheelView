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

import android.app.AlertDialog
import android.content.Context
import android.graphics.Color
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import com.wx.wheelview.adapter.ArrayWheelAdapter
import com.wx.wheelview.common.WheelConstants
import com.wx.wheelview.util.WheelUtils

/**
 * 滚轮对话框
 *
 * @author venshine
 */
@JvmSuppressWildcards
open class WheelViewDialog<T>(private val mContext: Context) : View.OnClickListener {

    private val mTitle: TextView

    private val mLine1: View
    private val mLine2: View

    private val mWheelView: WheelView<T>

    private val mStyle: WheelView.WheelViewStyle

    private val mButton: TextView

    private val mDialog: AlertDialog

    private var mOnDialogItemClickListener: OnDialogItemClickListener<in T>? = null

    private var mSelectedPos = 0

    private var mSelectedText: T? = null

    init {
        val layout = LinearLayout(mContext)
        layout.orientation = LinearLayout.VERTICAL
        layout.setPadding(WheelUtils.dip2px(mContext, 20f), 0, WheelUtils.dip2px(mContext, 20f), 0)

        mTitle = TextView(mContext)
        mTitle.setTextColor(WheelConstants.DIALOG_WHEEL_COLOR)
        mTitle.setTextSize(TypedValue.COMPLEX_UNIT_SP, 16f)
        mTitle.gravity = Gravity.CENTER
        val titleParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            WheelUtils.dip2px(mContext, 50f))
        layout.addView(mTitle, titleParams)

        mLine1 = View(mContext)
        mLine1.setBackgroundColor(WheelConstants.DIALOG_WHEEL_COLOR)
        val lineParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            WheelUtils.dip2px(mContext, 2f))
        layout.addView(mLine1, lineParams)

        mWheelView = WheelView(mContext)
        mWheelView.skin = WheelView.Skin.Holo
        mWheelView.setWheelAdapter(ArrayWheelAdapter(mContext))
        mStyle = WheelView.WheelViewStyle()
        mStyle.textColor = Color.GRAY
        mStyle.selectedTextZoom = 1.2f
        mWheelView.style = mStyle

        mWheelView.setOnWheelItemSelectedListener { position, text ->
            mSelectedPos = position
            mSelectedText = text
        }
        val wheelParams = ViewGroup.MarginLayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            LinearLayout.LayoutParams.WRAP_CONTENT)
        layout.addView(mWheelView, wheelParams)

        mLine2 = View(mContext)
        mLine2.setBackgroundColor(WheelConstants.DIALOG_WHEEL_COLOR)
        val line2Params = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            WheelUtils.dip2px(mContext, 1f))
        layout.addView(mLine2, line2Params)

        mButton = TextView(mContext)
        mButton.setTextColor(WheelConstants.DIALOG_WHEEL_COLOR)
        mButton.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
        mButton.gravity = Gravity.CENTER
        mButton.isClickable = true
        mButton.setOnClickListener(this)
        mButton.text = "OK"
        val buttonParams = LinearLayout.LayoutParams(LinearLayout.LayoutParams.MATCH_PARENT,
            WheelUtils.dip2px(mContext, 45f))
        layout.addView(mButton, buttonParams)

        mDialog = AlertDialog.Builder(mContext).create()
        mDialog.setView(layout)
        mDialog.setCanceledOnTouchOutside(false)
    }

    /**
     * 点击事件
     *
     * 参数为 OnDialogItemClickListener<? super T>：原 Java 签名是原始类型，Java 中传原始类型或 lambda 都能编译
     *
     * @param onDialogItemClickListener
     * @return
     */
    open fun setOnDialogItemClickListener(onDialogItemClickListener: OnDialogItemClickListener<in T>?): WheelViewDialog<T> {
        mOnDialogItemClickListener = onDialogItemClickListener
        return this
    }

    /**
     * 设置dialog外观颜色
     *
     * @param color
     * @return
     */
    open fun setDialogStyle(color: Int): WheelViewDialog<T> {
        mTitle.setTextColor(color)
        mLine1.setBackgroundColor(color)
        mLine2.setBackgroundColor(color)
        mButton.setTextColor(color)
        mStyle.selectedTextColor = color
        mStyle.holoBorderColor = color
        return this
    }

    /**
     * 设置标题
     *
     * @param title
     * @return
     */
    open fun setTitle(title: String?): WheelViewDialog<T> {
        mTitle.text = title
        return this
    }

    /**
     * 设置标题颜色
     *
     * @param color
     * @return
     */
    open fun setTextColor(color: Int): WheelViewDialog<T> {
        mTitle.setTextColor(color)
        return this
    }

    /**
     * 设置标题大小
     *
     * @param size
     * @return
     */
    open fun setTextSize(size: Int): WheelViewDialog<T> {
        mTitle.textSize = size.toFloat()
        return this
    }

    /**
     * 设置按钮文本
     *
     * @param text
     * @return
     */
    open fun setButtonText(text: String?): WheelViewDialog<T> {
        mButton.text = text
        return this
    }

    /**
     * 设置按钮文本颜色
     *
     * @param color
     * @return
     */
    open fun setButtonColor(color: Int): WheelViewDialog<T> {
        mButton.setTextColor(color)
        return this
    }

    /**
     * 设置按钮文本尺寸
     *
     * @param size
     * @return
     */
    open fun setButtonSize(size: Int): WheelViewDialog<T> {
        mButton.textSize = size.toFloat()
        return this
    }

    /**
     * 设置数据项显示个数
     *
     * @param count
     */
    open fun setCount(count: Int): WheelViewDialog<T> {
        mWheelView.setWheelSize(count)
        return this
    }

    /**
     * 数据项是否循环显示
     *
     * @param loop
     */
    open fun setLoop(loop: Boolean): WheelViewDialog<T> {
        mWheelView.setLoop(loop)
        return this
    }

    /**
     * 设置数据项显示位置
     *
     * @param selection
     */
    open fun setSelection(selection: Int): WheelViewDialog<T> {
        mWheelView.setSelection(selection)
        return this
    }

    /**
     * 设置数据项
     *
     * @param arrays
     */
    open fun setItems(arrays: Array<T>): WheelViewDialog<T> = setItems(arrays.asList())

    /**
     * 设置数据项
     *
     * @param list
     */
    open fun setItems(list: List<T>?): WheelViewDialog<T> {
        mWheelView.setWheelData(list)
        return this
    }

    /**
     * 显示
     */
    open fun show(): WheelViewDialog<T> {
        if (!mDialog.isShowing) {
            mDialog.show()
        }
        return this
    }

    /**
     * 隐藏
     */
    open fun dismiss(): WheelViewDialog<T> {
        if (mDialog.isShowing) {
            mDialog.dismiss()
        }
        return this
    }

    override fun onClick(v: View?) {
        dismiss()
        // 与原实现一致：未滚动选择时回调 null
        @Suppress("UNCHECKED_CAST")
        mOnDialogItemClickListener?.onItemClick(mSelectedPos, mSelectedText as T)
    }

    fun interface OnDialogItemClickListener<T> {
        fun onItemClick(position: Int, s: T)
    }
}
