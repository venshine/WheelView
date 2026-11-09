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
package com.wx.wheelview.demo

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.annotation.IdRes
import com.wx.wheelview.widget.WheelViewDialog

/**
 * 弹窗选择：前三项用底部弹窗（WheelView 放进 Dialog），「血型」用库自带的 WheelViewDialog
 *
 * @author venshine
 */
class DialogActivity : BaseActivity() {

    private class Field(
        @IdRes val row: Int,
        val title: String,
        val subtitle: String,
        val items: List<String>,
        val unit: String,
        val unitOffsetDp: Float,
        val loop: Boolean,
        var selection: Int,
    ) {
        val display get() = if (unit.isEmpty()) items[selection] else "${items[selection]} $unit"
    }

    private val fields = listOf(
        Field(R.id.row_height, "身高", "150 – 200 cm", (150..200).map { it.toString() }, "cm", 30f, false, 25),
        Field(R.id.row_sign, "星座", "循环滚动", SIGNS, "", 0f, true, 6),
        Field(R.id.row_steps, "每日步数目标", "每档 1000 步", (4000..20000 step 1000).map { it.toString() }, "步", 40f,
            false, 4),
    )
    private var blood = 0
    private val hideToast = Runnable { findViewById<View>(R.id.toast_text).visibility = View.GONE }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dialog)
        findViewById<TextView>(R.id.toolbar_title).setText(R.string.dialog_title)

        fields.forEach { field ->
            val row = findViewById<View>(field.row)
            row.findViewById<TextView>(R.id.row_label).text = field.title
            row.setOnClickListener {
                BottomWheelDialog(this, field.title, field.subtitle, field.items, field.unit, field.unitOffsetDp,
                    field.loop, field.selection) { position ->
                    field.selection = position
                    render()
                    showToast(getString(R.string.dialog_updated_format, field.title, field.display))
                }.show()
            }
        }

        val bloodRow = findViewById<View>(R.id.row_blood)
        bloodRow.findViewById<TextView>(R.id.row_label).text = "血型"
        bloodRow.setOnClickListener { showBloodDialog() }
        render()
    }

    /** 库自带的居中对话框，setDialogStyle 统一标题、分割线、按钮和选中文字的颜色 */
    private fun showBloodDialog() {
        val dialog = WheelViewDialog<String>(this)
        dialog.setTitle("血型")
        dialog.setItems(BLOOD_TYPES)
        dialog.setButtonText(getString(R.string.confirm))
        dialog.setDialogStyle(colorOf(R.color.dialog_accent))
        dialog.setSelection(blood)
        dialog.setOnDialogItemClickListener(WheelViewDialog.OnDialogItemClickListener<String> { position, item ->
            blood = position
            render()
            showToast(getString(R.string.dialog_updated_format, "血型", item))
        })
        dialog.show()
    }

    private fun render() {
        fields.forEach { field ->
            findViewById<View>(field.row).findViewById<TextView>(R.id.row_value).text = field.display
        }
        findViewById<View>(R.id.row_blood).findViewById<TextView>(R.id.row_value).text = BLOOD_TYPES[blood]
    }

    private fun showToast(message: String) {
        findViewById<TextView>(R.id.toast_text).apply {
            text = message
            setStartIcon(tintedIcon(R.drawable.ic_check, colorOf(R.color.accent), 16f))
            removeCallbacks(hideToast)
            visibility = View.VISIBLE
            alpha = 0f
            animate().alpha(1f).setDuration(160).start()
            postDelayed(hideToast, 1800)
        }
    }

    private companion object {
        val SIGNS = listOf("白羊座", "金牛座", "双子座", "巨蟹座", "狮子座", "处女座",
            "天秤座", "天蝎座", "射手座", "摩羯座", "水瓶座", "双鱼座")
        val BLOOD_TYPES = listOf("A 型", "B 型", "AB 型", "O 型", "其他")
    }
}
