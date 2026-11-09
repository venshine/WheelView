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
import android.text.SpannableStringBuilder
import android.widget.TextView
import com.wx.wheelview.widget.WheelView

/**
 * 三级联动：省 → 市 → 区，主滚轮切换后副滚轮换数据并回到第一项
 *
 * @author venshine
 */
class LinkageActivity : BaseActivity() {

    private lateinit var provinceWheel: WheelView<String>
    private lateinit var cityWheel: WheelView<String>
    private lateinit var districtWheel: WheelView<String>
    private lateinit var confirmLabel: TextView
    private var confirmed = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_linkage)
        setToolbar(R.string.demo_linkage, "join()")

        // joinDatas 的 key 是主滚轮条目的显示文本
        val provinces = DATA.keys.toList()
        val cities = HashMap<String, List<String>>()
        val districts = HashMap<String, List<String>>()
        DATA.forEach { (province, cityMap) ->
            cities[province] = cityMap.keys.toList()
            districts.putAll(cityMap)
        }
        val firstCities = cities.getValue(provinces[INITIAL_PROVINCE])

        val style = DemoWheels.style(this, textSize = 17)
        provinceWheel = findViewById(R.id.province_wheel)
        cityWheel = findViewById(R.id.city_wheel)
        districtWheel = findViewById(R.id.district_wheel)
        DemoWheels.setup(provinceWheel, DemoWheelAdapter(this), provinces, style, selection = INITIAL_PROVINCE)
        DemoWheels.setup(cityWheel, DemoWheelAdapter(this), firstCities, style, selection = INITIAL_CITY)
        DemoWheels.setup(districtWheel, DemoWheelAdapter(this), districts.getValue(firstCities[INITIAL_CITY]), style)

        provinceWheel.join(cityWheel)
        provinceWheel.joinDatas(cities)
        cityWheel.join(districtWheel)
        cityWheel.joinDatas(districts)

        listOf(provinceWheel, cityWheel, districtWheel).forEach { wheel ->
            wheel.setOnWheelItemSelectedListener { _, _ ->
                confirmed = false
                render()
            }
        }

        findViewById<TextView>(R.id.code_text).text = CodeHighlighter.highlight(this,
            "// key 为主滚轮条目的显示文本\nprovinceWheel.join(cityWheel)\nprovinceWheel.joinDatas(CITIES)\n" +
                "cityWheel.join(districtWheel)\ncityWheel.joinDatas(DISTRICTS)",
            listOf("joinDatas", "join"))

        confirmLabel = findViewById(R.id.confirm_label)
        findViewById<android.view.View>(R.id.confirm_button).setOnClickListener {
            confirmed = true
            render()
        }
        render()
    }

    private fun render() {
        confirmLabel.setText(if (confirmed) R.string.linkage_confirmed else R.string.linkage_confirm)
        confirmLabel.setStartIcon(if (confirmed) tintedIcon(R.drawable.ic_check, colorOf(R.color.on_accent), 18f) else null)

        val wheels = listOf(provinceWheel, cityWheel, districtWheel)
        if (wheels.any { it.currentPosition < 0 }) return
        val names = wheels.map { it.selectionItem.orEmpty() }
        val separator = colorOf(R.color.chevron)
        findViewById<TextView>(R.id.result_text).text = SpannableStringBuilder()
            .append(names[0]).appendColored("  ›  ", separator)
            .append(names[1]).appendColored("  ›  ", separator)
            .appendColored(names[2], colorOf(R.color.accent_ink))
        findViewById<TextView>(R.id.result_index).text =
            "getCurrentPosition() → ${wheels.map { it.currentPosition }}"
    }

    private companion object {
        const val INITIAL_PROVINCE = 2
        const val INITIAL_CITY = 1

        val DATA: LinkedHashMap<String, LinkedHashMap<String, List<String>>> = linkedMapOf(
            "浙江" to linkedMapOf(
                "杭州" to listOf("西湖区", "上城区", "拱墅区", "滨江区", "余杭区"),
                "宁波" to listOf("海曙区", "江北区", "鄞州区"),
                "温州" to listOf("鹿城区", "龙湾区", "瓯海区"),
            ),
            "江苏" to linkedMapOf(
                "南京" to listOf("玄武区", "秦淮区", "鼓楼区", "建邺区"),
                "苏州" to listOf("姑苏区", "吴中区", "相城区", "吴江区"),
                "无锡" to listOf("梁溪区", "滨湖区", "新吴区"),
            ),
            "广东" to linkedMapOf(
                "广州" to listOf("天河区", "越秀区", "海珠区", "番禺区"),
                "深圳" to listOf("南山区", "福田区", "罗湖区", "宝安区"),
                "珠海" to listOf("香洲区", "斗门区", "金湾区"),
            ),
            "四川" to linkedMapOf(
                "成都" to listOf("锦江区", "青羊区", "武侯区", "成华区"),
                "绵阳" to listOf("涪城区", "游仙区"),
                "乐山" to listOf("市中区", "五通桥区"),
            ),
            "辽宁" to linkedMapOf(
                "沈阳" to listOf("和平区", "皇姑区", "大东区", "铁西区"),
                "大连" to listOf("中山区", "甘井子区", "金州区"),
            ),
        )
    }
}
