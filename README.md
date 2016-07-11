# WheelView  

[![Android Arsenal](https://img.shields.io/badge/Android%20Arsenal-WheelView-green.svg?style=true)](https://android-arsenal.com/details/1/3853)  [![API](https://img.shields.io/badge/API-14%2B-brightgreen.svg?style=flat)](https://android-arsenal.com/api?level=14)

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
```groovy
compile 'com.wx.wheelview:wheelview:1.3.3'
```

##### 导入Eclipse:
[下载jar包](https://github.com/ifwx/WheelView/raw/master/wheelview/wheelview_1.3.3.jar)

Demo
--
Use the WheelView as a View, Java and XML are both supported.

##### Java:
```Java
    public class MainActivity extends Activity {
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setContentView(R.layout.activity_main)

            WheelView wheelView = (WheelView) findViewById(R.id.wheelview);
            wheelView.setWheelAdapter(new ArrayWheelAdapter(this)); // 文本数据源
            wheelView.setSkin(WheelView.Skin.Common); // common皮肤
            wheelView.setWheelData( ?);  // 数据集合

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
| void **joinDatas**(HashMap<String, List<T>> map)	     | 副WheelView数据（联动设置） |
| int **getCurrentPosition**()  	     | 获取当前滚轮位置 |
| T **getSelectionItem**()  	     | 获取当前滚轮位置的数据 |
| void **setExtraText**(String text, int textColor, int textSize, int margin)     	     | 设置选中行附加文本 |
| int **getWheelCount**() 	     | 获得滚轮数据总数 |
| void **setOnWheelItemSelectedListener**(OnWheelItemSelectedListener<T> onWheelItemSelectedListener) | 设置滚轮滑动停止时事件，监听滚轮选中项 |
| void **setOnWheelItemClickListener**(OnWheelItemClickListener<T> onWheelItemClickListener) | 设置滚轮选中项点击事件 |
| WheelViewDialog **setDialogStyle**(int color) | 设置Dialog外观颜色 |

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

