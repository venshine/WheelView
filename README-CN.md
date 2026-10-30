# WheelView  

[English](README.md) | 简体中文

[![JitPack](https://jitpack.io/v/venshine/WheelView.svg)](https://jitpack.io/#venshine/WheelView)  [![CI](https://github.com/venshine/WheelView/actions/workflows/ci.yml/badge.svg)](https://github.com/venshine/WheelView/actions/workflows/ci.yml)  [![API](https://img.shields.io/badge/API-14%2B-brightgreen.svg?style=flat)](https://developer.android.com/guide/topics/manifest/uses-sdk-element#ApiLevels)

Android滚轮控件，基于ListView实现，可以自定义样式。

Features
--
* 支持自定义滚轮样式
* 支持common和holo两种皮肤
* 支持文本和图文混排两中数据模版
* 支持循环显示数据
* 支持选中项添加附加文本
* 支持设置滚轮刻度
* 支持联动功能
* 支持嵌入滚动控件中([NestedScrollView](https://github.com/venshine/WheelView/blob/master/wheelview/src/main/java/com/wx/wheelview/widget/NestedScrollView.java))
* 支持滚轮对话框
* 支持滚轮选中项点击事件

ScreenShot
--
![](https://github.com/venshine/WheelView/blob/master/screenshot/screenshot.gif)
![](https://github.com/venshine/WheelView/blob/master/screenshot/screenshot1.png)
![](https://github.com/venshine/WheelView/blob/master/screenshot/screenshot2.png)

Usage
--
##### Gradle:
1. 在 `settings.gradle` 中添加 JitPack 仓库：
```groovy
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven { url = 'https://jitpack.io' }
    }
}
```
2. 在模块的 `build.gradle` 中添加依赖：
```groovy
implementation 'com.github.venshine:WheelView:2.1.0'
```

##### Gradle Kotlin DSL:
```kotlin
// settings.gradle.kts
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://jitpack.io")
    }
}

// build.gradle.kts
implementation("com.github.venshine:WheelView:2.1.0")
```

> 2.0.0 起改为通过 JitPack 发布，旧坐标 `com.wx.wheelview:wheelview` 已无法下载。
> `WheelViewStyle` 的颜色字段（`backgroundColor`、`holoBorderColor`、`textColor`、`selectedTextColor`）改为 `Integer`，`null` 表示使用默认颜色，现在可以正常设置 `Color.WHITE`。
> 2.0.2 起，联动时主滚轮切换导致副滚轮数据变化，副滚轮会回到第一项（之前保持原位置）。

Demo
--
库本身用 Java 编写，Java 和 Kotlin 都可以直接调用，不会给项目额外引入 Kotlin 依赖。

##### Kotlin:
```kotlin
class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val wheelView = findViewById<WheelView<String>>(R.id.wheelview)
        wheelView.setWheelAdapter(ArrayWheelAdapter(this)) // 文本数据源
        wheelView.skin = WheelView.Skin.Holo // holo皮肤
        wheelView.setWheelData(listOf("item0", "item1", "item2", "item3", "item4")) // 数据集合
        wheelView.style = WheelView.WheelViewStyle().apply {
            selectedTextColor = Color.BLACK
            holoBorderColor = Color.LTGRAY // 分割线颜色
            selectedBackgroundColor = Color.parseColor("#f2f2f2") // 选中项背景
            textScale = 0.85f // 未选中项逐级缩小
        }
        wheelView.setOnWheelItemSelectedListener { position, item ->
            Log.d("WheelView", "selected $position: $item")
        }
    }
}
```

联动（省市选择）：
```kotlin
val cities = hashMapOf(
    "浙江" to listOf("杭州", "宁波", "温州"),
    "江苏" to listOf("南京", "苏州", "无锡"),
)
val provinceWheel = findViewById<WheelView<String>>(R.id.province_wheelview)
provinceWheel.setWheelAdapter(ArrayWheelAdapter(this))
provinceWheel.setWheelData(listOf("浙江", "江苏"))
val cityWheel = findViewById<WheelView<String>>(R.id.city_wheelview)
cityWheel.setWheelAdapter(ArrayWheelAdapter(this))
cityWheel.setWheelData(cities.getValue("浙江"))
provinceWheel.join(cityWheel)
provinceWheel.joinDatas(cities) // key 为主滚轮条目的显示文本
```

滚轮对话框：
```kotlin
WheelViewDialog<String>(this)
    .setTitle("请选择")
    .setItems(listOf("item0", "item1", "item2", "item3", "item4"))
    .setButtonText("确定")
    .setOnDialogItemClickListener { position, item -> Log.d("WheelView", "$position: $item") }
    .show()
```

> 在代码里用 `WheelView(context)` 创建的滚轮，需要先 `addView` 到界面中才会显示。
> 滚轮高度固定为「条目高度 × 滚轮个数」（`setWheelSize`），`layout_height` 不生效；需要其他高度时，可以继承 `BaseWheelAdapter` 自定义条目布局。

##### Java:
```Java
public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        WheelView<String> wheelView = findViewById(R.id.wheelview);
        wheelView.setWheelAdapter(new ArrayWheelAdapter<>(this)); // 文本数据源
        wheelView.setSkin(WheelView.Skin.Common); // common皮肤
        wheelView.setWheelData(Arrays.asList("item0", "item1", "item2", "item3", "item4")); // 数据集合
    }
}
```

##### XML:
```xml
<com.wx.wheelview.widget.WheelView
    android:id="@+id/wheelview"
    android:layout_width="match_parent"
    android:layout_height="wrap_content" />
```

##### Methods:
| method 方法          | description 描述 |
|:---				 |:---|
| void **setWheelAdapter**(BaseWheelAdapter<T> adapter)  	     | 设置滚轮数据源适配器（required） |
| void **setWheelData**(List<T> list)  	     | 设置滚轮数据（required） |
| void **setLoop**(boolean loop)  	     | 设置滚轮是否循环滚动 |
| void **setWheelSize**(int wheelSize) 	     | 设置滚轮个数 |
| void **setSkin**(Skin skin) 	     | 设置皮肤风格 |
| Skin **getSkin**()  	     | 获得皮肤风格 |
| void **setStyle**(WheelViewStyle style)  	     | 设置滚轮样式 |
| WheelViewStyle **getStyle**()  	     | 获得滚轮样式 |
| void **setWheelClickable**(boolean clickable)  	     | 设置滚轮选中项是否可点击 |
| void **setSelection**(final int selection) 	     | 设置滚轮位置 |
| int **getSelection**() 	     | 获取滚轮位置 |
| void **join**(WheelView wheelView)  	     | 连接副WheelView（联动设置） |
| void **joinDatas**(HashMap<String, List<T>> map)	     | 副WheelView数据（联动设置），key 为主滚轮条目的显示文本（toString()） |
| int **getCurrentPosition**()  	     | 获取当前滚轮位置 |
| T **getSelectionItem**()  	     | 获取当前滚轮位置的数据 |
| void **setExtraText**(String text, int textColor, int textSize, int margin)     	     | 设置选中行附加文本 |
| int **getWheelCount**() 	     | 获得滚轮数据总数 |
| void **setOnWheelItemSelectedListener**(OnWheelItemSelectedListener<T> onWheelItemSelectedListener) | 设置滚轮滑动停止时事件，监听滚轮选中项 |
| void **setOnWheelItemClickListener**(OnWheelItemClickListener<T> onWheelItemClickListener) | 设置滚轮选中项点击事件 |
| WheelViewDialog **setDialogStyle**(int color) | 设置Dialog外观颜色 |

##### WheelViewStyle:
| field 字段          | description 描述 |
|:---				 |:---|
| Integer **backgroundColor** | 背景颜色，`null` 使用皮肤默认颜色 |
| Integer **holoBorderColor** | Holo 皮肤选中框分割线颜色 |
| int **holoBorderWidth** | Holo 皮肤选中框分割线宽度，单位 px，默认 3 |
| Integer **textColor** | 文本颜色 |
| Integer **selectedTextColor** | 选中文本颜色 |
| Integer **selectedBackgroundColor** | 选中项背景颜色，Common 和 Holo 皮肤生效 |
| int **textSize** | 文本大小，单位 dp，默认 16 |
| int **selectedTextSize** | 选中文本大小，单位 dp |
| float **textAlpha** | 未选中文本透明度（0f ~ 1f），离选中项越远越透明，默认 0.7 |
| float **textScale** | 未选中文本缩放比例（0f ~ 1f），离选中项每远一项再缩小一次，默认不缩放 |
| float **selectedTextZoom** | 选中文本放大倍数，未设置 selectedTextSize 时生效 |
| boolean **selectedTextBold** | 选中文本是否加粗 |

History
--
* 1.0.0(2016-03-24)
    - 完成滚轮控件
* 1.1.0(2016-03-28)
    - 支持联动功能
* 1.2.3(2016-04-14)
    - 支持嵌入滚动控件
* 1.3.0(2016-04-15)
    - 支持滚轮对话框
* 1.3.1(2016-04-18)
    - 增加滚轮选中项点击事件
* 2.0.0 及之后的版本见 [Releases](https://github.com/venshine/WheelView/releases)

About
--
* Email：venshine.cn@gmail.com

License
--
    Copyright (C) 2016 venshine.cn@gmail.com

    Licensed under the Apache License, Version 2.0 (the "License");
    you may not use this file except in compliance with the License.
    You may obtain a copy of the License at
    
    http://www.apache.org/licenses/LICENSE-2.0
    
    Unless required by applicable law or agreed to in writing, software
    distributed under the License is distributed on an "AS IS" BASIS,
    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
    See the License for the specific language governing permissions and
    limitations under the License.

