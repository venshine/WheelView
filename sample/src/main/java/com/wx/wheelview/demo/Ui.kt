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

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.graphics.Typeface
import android.graphics.drawable.Drawable
import android.text.SpannableStringBuilder
import android.text.Spanned
import android.text.TextPaint
import android.text.style.ForegroundColorSpan
import android.text.style.MetricAffectingSpan
import android.util.TypedValue
import android.widget.TextView
import androidx.annotation.ColorInt
import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import androidx.annotation.FontRes
import androidx.annotation.StringRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.DrawableCompat

/*
 * 示例页共用的小工具：取色、单位换算、着色图标、代码高亮、复制
 */

@ColorInt
fun Context.colorOf(@ColorRes id: Int): Int = ContextCompat.getColor(this, id)

fun Context.dp(value: Float): Int = (value * resources.displayMetrics.density + 0.5f).toInt()

fun Context.sp(value: Float): Int =
    TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, value, resources.displayMetrics).toInt()

fun Context.font(@FontRes id: Int): Typeface? = ResourcesCompat.getFont(this, id)

@ColorInt
fun Int.withAlpha(alpha: Int): Int = (this and 0x00FFFFFF) or (alpha shl 24)

/** 着色后的线性图标，用作 TextView 的 compound drawable */
fun Context.tintedIcon(@DrawableRes id: Int, @ColorInt color: Int, sizeDp: Float): Drawable {
    val drawable = DrawableCompat.wrap(ContextCompat.getDrawable(this, id)!!.mutate())
    DrawableCompat.setTint(drawable, color)
    val size = dp(sizeDp)
    drawable.setBounds(0, 0, size, size)
    return drawable
}

fun TextView.setStartIcon(icon: Drawable?) {
    setCompoundDrawablesRelative(icon, null, null, null)
}

fun Context.copyToClipboard(text: String) {
    getSystemService(ClipboardManager::class.java)?.setPrimaryClip(ClipData.newPlainText("WheelView", text))
}

/** 复制后把按钮文字短暂换成「已复制」 */
fun TextView.flashCopied(@StringRes copied: Int, @StringRes normal: Int) {
    setText(copied)
    removeCallbacks(tag as? Runnable)
    val restore = Runnable { setText(normal) }
    tag = restore
    postDelayed(restore, 1600)
}

/** 给一段文字指定字体，Android 9 以下的 TypefaceSpan 不支持传 Typeface */
class FontSpan(private val typeface: Typeface) : MetricAffectingSpan() {
    override fun updateMeasureState(paint: TextPaint) {
        paint.typeface = typeface
    }

    override fun updateDrawState(paint: TextPaint) {
        paint.typeface = typeface
    }
}

fun SpannableStringBuilder.appendColored(text: CharSequence, @ColorInt color: Int): SpannableStringBuilder {
    val start = length
    append(text)
    setSpan(ForegroundColorSpan(color), start, length, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    return this
}

/**
 * 示例代码的简易高亮：关键字用强调色，引号内字符串和整行注释各用一种颜色
 */
object CodeHighlighter {

    private val STRING = Regex("\"[^\"\\n]*\"|'[^'\\n]*'")

    // 只认整行注释，避免把 'https://…' 里的 // 当成注释
    private val COMMENT = Regex("(?m)^\\s*//.*$")

    fun highlight(context: Context, code: String, keywords: List<String>): CharSequence {
        val builder = SpannableStringBuilder(code)
        val keywordColor = context.colorOf(R.color.accent_ink)
        for (keyword in keywords) {
            // 只匹配完整的单词，避免 join 命中 joinDatas 的前半段
            Regex("(?<![A-Za-z])" + Regex.escape(keyword) + "(?![A-Za-z])").findAll(code)
                .forEach { builder.color(keywordColor, it.range.first, it.range.last + 1) }
        }
        STRING.findAll(code).forEach { builder.color(context.colorOf(R.color.code_string), it.range.first, it.range.last + 1) }
        COMMENT.findAll(code).forEach { builder.color(context.colorOf(R.color.code_comment), it.range.first, it.range.last + 1) }
        return builder
    }

    private fun SpannableStringBuilder.color(@ColorInt color: Int, start: Int, end: Int) {
        setSpan(ForegroundColorSpan(color), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE)
    }
}
