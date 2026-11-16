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

import android.content.Context
import android.view.View
import android.view.ViewGroup
import com.wx.wheelview.common.WheelData
import com.wx.wheelview.widget.WheelItem

/**
 * 滚轮图片和文本适配器
 *
 * @author venshine
 */
open class SimpleWheelAdapter(private val mContext: Context) : BaseWheelAdapter<WheelData>() {

    public override fun bindView(position: Int, convertView: View?, parent: ViewGroup): View? {
        val item = (convertView ?: WheelItem(mContext)) as WheelItem
        item.setImage(mList[position].id)
        item.setText(mList[position].name)
        return item
    }
}
