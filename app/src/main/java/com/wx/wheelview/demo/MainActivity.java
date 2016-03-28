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
package com.wx.wheelview.demo;

import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;

import com.wx.wheelview.adapter.ArrayWheelAdapter;
import com.wx.wheelview.adapter.SimpleWheelAdapter;
import com.wx.wheelview.common.WheelData;
import com.wx.wheelview.util.WheelUtils;
import com.wx.wheelview.widget.WheelView;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * Demo
 *
 * @author venshine
 */
public class MainActivity extends Activity {

    private WheelView mainWheelView, subWheelView, hourWheelView, minuteWheelView, secondWheelView, commonWheelView,
            simpleWheelView, myWheelView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWheel1();
        initWheel2();
        initWheel3();
    }

    /**
     * 级联WheelView
     */
    private void initWheel1() {
        mainWheelView = (WheelView) findViewById(R.id.main_wheelview);
        mainWheelView.setWheelAdapter(new ArrayWheelAdapter(this));
        mainWheelView.setSkin(WheelView.Skin.Holo);
        mainWheelView.setWheelData(createMainDatas());
        WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();
        style.selectedTextSize = 20;
        style.textSize = 16;
        mainWheelView.setStyle(style);

        subWheelView = (WheelView) findViewById(R.id.sub_wheelview);
        subWheelView.setWheelAdapter(new ArrayWheelAdapter(this));
        subWheelView.setSkin(WheelView.Skin.Holo);
        subWheelView.setWheelData(createSubDatas().get(mainWheelView.getSelection()));
        subWheelView.setStyle(style);
        mainWheelView.join(subWheelView);
        mainWheelView.joinDatas(createSubDatas());
    }

    /**
     * holo皮肤
     */
    private void initWheel2() {
        hourWheelView = (WheelView) findViewById(R.id.hour_wheelview);
        hourWheelView.setWheelAdapter(new ArrayWheelAdapter(this));
        hourWheelView.setSkin(WheelView.Skin.Holo);
        hourWheelView.setWheelData(createHours());
        WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();
        style.selectedTextColor = Color.BLUE;
        style.selectedTextSize = 20;
        hourWheelView.setStyle(style);
        hourWheelView.setExtraText("时", Color.BLUE, 40, 70);

        minuteWheelView = (WheelView) findViewById(R.id.minute_wheelview);
        minuteWheelView.setWheelAdapter(new ArrayWheelAdapter(this));
        minuteWheelView.setSkin(WheelView.Skin.Holo);
        minuteWheelView.setWheelData(createMinutes());
        minuteWheelView.setStyle(style);
        minuteWheelView.setExtraText("分", Color.BLUE, 40, 70);

        secondWheelView = (WheelView) findViewById(R.id.second_wheelview);
        secondWheelView.setWheelAdapter(new ArrayWheelAdapter(this));
        secondWheelView.setSkin(WheelView.Skin.Holo);
        secondWheelView.setWheelData(createMinutes());
        secondWheelView.setStyle(style);
        secondWheelView.setExtraText("秒", Color.BLUE, 40, 70);
    }

    /**
     * common皮肤、图文混排无皮肤、自定义布局
     */
    private void initWheel3() {
        commonWheelView = (WheelView) findViewById(R.id.common_wheelview);
        commonWheelView.setWheelAdapter(new ArrayWheelAdapter(this));
        commonWheelView.setSkin(WheelView.Skin.Common);
        commonWheelView.setWheelData(createArrays());

        simpleWheelView = (WheelView) findViewById(R.id.simple_wheelview);
        simpleWheelView.setWheelAdapter(new SimpleWheelAdapter(this));
        simpleWheelView.setWheelSize(5);
        simpleWheelView.setWheelData(createDatas());
        simpleWheelView.setSkin(WheelView.Skin.None);
        simpleWheelView.setLoop(true);
        simpleWheelView.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener<WheelData>() {
            @Override
            public void onItemSelected(int position, WheelData data) {
                WheelUtils.log("selected:" + position);
            }
        });

        myWheelView = (WheelView) findViewById(R.id.my_wheelview);
        myWheelView.setWheelAdapter(new MyWheelAdapter(this));
        myWheelView.setWheelSize(5);
        myWheelView.setSkin(WheelView.Skin.Holo);
        myWheelView.setWheelData(createArrays());
        myWheelView.setSelection(2);
        WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();
        style.backgroundColor = Color.YELLOW;
        style.textColor = Color.DKGRAY;
        style.selectedTextColor = Color.GREEN;
        myWheelView.setStyle(style);
    }

    private List<String> createMainDatas() {
        String[] strings = {"北京", "河北", "山东", "江苏", "河南", "山西", "湖北", "湖南", "浙江", "辽宁"};
        return Arrays.asList(strings);
    }

    private List<List<String>> createSubDatas() {
        String[] s1 = {"海淀区", "朝阳区", "昌平区", "西城区", "东城区", "大兴区", "房山区", "顺义区", "石景山区"};
        String[] s2 = {"石家庄", "唐山", "保定", "秦皇岛", "承德", "张家口", "廊坊", "衡水", "沧州", "邯郸"};
        String[] s3 = {"济南", "潍坊", "淄博", "威海", "临沂", "枣庄", "烟台", "日照", "德州", "聊城", "莱芜"};
        String[] s4 = {"南京", "无锡", "常州", "扬州", "苏州", "连云港", "盐城", "淮安", "宿迁", "南通"};
        String[] s5 = {"郑州", "洛阳", "焦作", "商丘", "安阳", "开封", "新乡", "信阳"};
        String[] s6 = {"太原", "大同", "阳泉", "长治", "运城", "吕梁"};
        String[] s7 = {"武汉", "襄樊", "宜昌", "孝感", "黄冈", "咸宁", "黄石"};
        String[] s8 = {"长沙", "衡阳", "湘潭", "岳阳", "常德", "张家界", "怀化", "常德", "株洲"};
        String[] s9 = {"杭州", "绍兴", "温州", "嘉兴", "金华", "舟山", "台州"};
        String[] s10 = {"沈阳", "鞍山", "本溪", "铁岭", "锦州", "辽阳", "营口", "抚顺", "丹东", "葫芦岛"};
        List<List<String>> lists = new ArrayList<List<String>>();
        lists.add(Arrays.asList(s1));
        lists.add(Arrays.asList(s2));
        lists.add(Arrays.asList(s3));
        lists.add(Arrays.asList(s4));
        lists.add(Arrays.asList(s5));
        lists.add(Arrays.asList(s6));
        lists.add(Arrays.asList(s7));
        lists.add(Arrays.asList(s8));
        lists.add(Arrays.asList(s9));
        lists.add(Arrays.asList(s10));
        return lists;
    }

    private ArrayList<String> createHours() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < 24; i++) {
            if (i < 10) {
                list.add("0" + i);
            } else {
                list.add("" + i);
            }
        }
        return list;
    }

    private ArrayList<String> createMinutes() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < 60; i++) {
            if (i < 10) {
                list.add("0" + i);
            } else {
                list.add("" + i);
            }
        }
        return list;
    }

    private ArrayList<String> createArrays() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < 20; i++) {
            list.add("item" + i);
        }
        return list;
    }

    private ArrayList<WheelData> createDatas() {
        ArrayList<WheelData> list = new ArrayList<WheelData>();
        WheelData item;
        for (int i = 0; i < 20; i++) {
            item = new WheelData();
            item.setId(R.mipmap.ic_launcher);
            item.setName((i < 10) ? ("0" + i) : ("" + i));
            list.add(item);
        }
        return list;
    }

}
