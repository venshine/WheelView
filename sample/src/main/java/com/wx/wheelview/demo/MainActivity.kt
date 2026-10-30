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

import android.app.Activity
import android.graphics.Color
import android.os.Bundle
import android.view.View
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import com.wx.wheelview.adapter.ArrayWheelAdapter
import com.wx.wheelview.adapter.SimpleWheelAdapter
import com.wx.wheelview.common.WheelData
import com.wx.wheelview.util.WheelUtils
import com.wx.wheelview.widget.WheelView
import com.wx.wheelview.widget.WheelViewDialog

/**
 * Demo
 *
 * @author venshine
 */
class MainActivity : Activity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        // targetSdk 35 起，Android 15+ 强制全面屏，内容需要避开状态栏和导航栏
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars() or WindowInsetsCompat.Type.displayCutout())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }
        // 浅色背景上使用深色状态栏图标
        WindowCompat.getInsetsController(window, window.decorView).isAppearanceLightStatusBars = true

        initWheel1()
        initWheel2()
        initWheel3()
    }

    /**
     * 联动WheelView
     */
    private fun initWheel1() {
        val style = WheelView.WheelViewStyle().apply {
            selectedTextSize = 20
            textSize = 16
        }

        val mainWheelView = findViewById<WheelView<String>>(R.id.main_wheelview).apply {
            setWheelAdapter(ArrayWheelAdapter(this@MainActivity))
            skin = WheelView.Skin.Holo
            setWheelData(MAIN_DATAS)
            this.style = style
        }

        val subWheelView = findViewById<WheelView<String>>(R.id.sub_wheelview).apply {
            setWheelAdapter(ArrayWheelAdapter(this@MainActivity))
            skin = WheelView.Skin.Holo
            setWheelData(SUB_DATAS.getValue(MAIN_DATAS[mainWheelView.selection]))
            this.style = style
        }
        mainWheelView.join(subWheelView)
        mainWheelView.joinDatas(SUB_DATAS)

        val childWheelView = findViewById<WheelView<String>>(R.id.child_wheelview).apply {
            setWheelAdapter(ArrayWheelAdapter(this@MainActivity))
            skin = WheelView.Skin.Holo
            val city = SUB_DATAS.getValue(MAIN_DATAS[mainWheelView.selection])[subWheelView.selection]
            setWheelData(CHILD_DATAS.getValue(city))
            this.style = style
        }
        subWheelView.join(childWheelView)
        subWheelView.joinDatas(CHILD_DATAS)
    }

    /**
     * holo皮肤
     */
    private fun initWheel2() {
        val style = WheelView.WheelViewStyle().apply {
            selectedTextColor = Color.parseColor("#0288ce")
            textColor = Color.GRAY
            selectedTextSize = 20
            selectedTextBold = true
        }
        val wheels = listOf(
            Triple(R.id.hour_wheelview, createNumbers(24), "时"),
            Triple(R.id.minute_wheelview, createNumbers(60), "分"),
            Triple(R.id.second_wheelview, createNumbers(60), "秒"),
        )
        for ((id, data, unit) in wheels) {
            findViewById<WheelView<String>>(id).apply {
                setWheelAdapter(ArrayWheelAdapter(this@MainActivity))
                skin = WheelView.Skin.Holo
                setWheelData(data)
                this.style = style
                setExtraText(unit, Color.parseColor("#0288ce"), 40, 70, true)
            }
        }
    }

    /**
     * common皮肤、图文混排无皮肤、自定义布局
     */
    private fun initWheel3() {
        findViewById<WheelView<String>>(R.id.common_wheelview).apply {
            setWheelAdapter(ArrayWheelAdapter(this@MainActivity))
            skin = WheelView.Skin.Common
            setWheelData(createArrays())
        }

        findViewById<WheelView<WheelData>>(R.id.simple_wheelview).apply {
            setWheelAdapter(SimpleWheelAdapter(this@MainActivity))
            setWheelSize(5)
            setWheelData(createDatas())
            skin = WheelView.Skin.None
            setLoop(true)
            setWheelClickable(true)
            setOnWheelItemClickListener { position, _ -> WheelUtils.log("click:$position") }
            setOnWheelItemSelectedListener { position, _ -> WheelUtils.log("selected:$position") }
        }

        findViewById<WheelView<String>>(R.id.my_wheelview).apply {
            setWheelAdapter(MyWheelAdapter(this@MainActivity))
            setWheelSize(5)
            skin = WheelView.Skin.Holo
            setWheelData(createArrays())
            setSelection(2)
            setClickToPosition(true)
            style = WheelView.WheelViewStyle().apply {
                backgroundColor = Color.YELLOW
                textColor = Color.DKGRAY
                selectedTextColor = Color.GREEN
            }
        }
    }

    fun showDialog(view: View) {
        WheelViewDialog<String>(this)
            .setTitle("wheelview dialog")
            .setItems(createArrays())
            .setButtonText("确定")
            .setDialogStyle(Color.parseColor("#6699ff"))
            .setCount(5)
            .show()
    }

    private fun createNumbers(count: Int): List<String> = (0 until count).map { "%02d".format(it) }

    private fun createArrays(): List<String> = (0 until 20).map { "item$it" }

    private fun createDatas(): List<WheelData> =
        (0 until 20).map { WheelData(R.mipmap.ic_launcher, "%02d".format(it)) }

    private companion object {
        val MAIN_DATAS = listOf("黑龙江", "吉林", "辽宁")

        val SUB_DATAS: HashMap<String, List<String>> = hashMapOf(
            "黑龙江" to listOf("哈尔滨", "齐齐哈尔", "大庆"),
            "吉林" to listOf("长春", "吉林"),
            "辽宁" to listOf("沈阳", "大连", "鞍山", "抚顺"),
        )

        val CHILD_DATAS: HashMap<String, List<String>> = hashMapOf(
            "哈尔滨" to listOf("道里区", "道外区", "南岗区", "香坊区"),
            "齐齐哈尔" to listOf("龙沙区", "建华区", "铁锋区"),
            "大庆" to listOf("红岗区", "大同区"),
            "长春" to listOf("南关区", "朝阳区"),
            "吉林" to listOf("龙潭区"),
            "沈阳" to listOf("和平区", "皇姑区", "大东区", "铁西区"),
            "大连" to listOf("中山区", "金州区"),
            "鞍山" to listOf("铁东区", "铁西区"),
            "抚顺" to listOf("新抚区", "望花区", "顺城区"),
        )
    }
}
