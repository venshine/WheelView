# WheelView
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

ScreenShot
--
![](https://github.com/venshine/WheelView/blob/master/screenshot/screenshot.gif)

Usage
--
##### Gradle:
```groovy
compile 'com.wx.wheelview:wheelview:1.2.0'
```

##### Maven:
```xml
<dependency>
  <groupId>com.wx.wheelview</groupId>
  <artifactId>wheelview</artifactId>
  <version>1.2.0</version>
  <type>pom</type>
</dependency>
```

##### 导入Eclipse:
[下载jar包](https://github.com/ifwx/WheelView/blob/master/wheelview/wheelview.jar)

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

History
--
* 1.0.0(2016-03-24)
    - 完成滚轮控件
* 1.1.0(2016-03-28)
    - 支持联动功能

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

