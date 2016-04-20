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
import android.view.View;

import com.wx.wheelview.adapter.ArrayWheelAdapter;
import com.wx.wheelview.adapter.SimpleWheelAdapter;
import com.wx.wheelview.common.WheelData;
import com.wx.wheelview.util.WheelUtils;
import com.wx.wheelview.widget.WheelView;
import com.wx.wheelview.widget.WheelViewDialog;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

/**
 * Demo
 *
 * @author venshine
 */
public class MainActivity extends Activity {

    private WheelView mainWheelView, subWheelView, childWheelView, hourWheelView, minuteWheelView, secondWheelView,
            commonWheelView,
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
     * 联动WheelView
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
        subWheelView.setWheelData(createSubDatas().get(createMainDatas().get(mainWheelView.getSelection())));
        subWheelView.setStyle(style);
        mainWheelView.join(subWheelView);
        mainWheelView.joinDatas(createSubDatas());

        childWheelView = (WheelView) findViewById(R.id.child_wheelview);
        childWheelView.setWheelAdapter(new ArrayWheelAdapter(this));
        childWheelView.setSkin(WheelView.Skin.Holo);
        childWheelView.setWheelData(createChildDatas().get(createSubDatas().get(createMainDatas().get(mainWheelView
                .getSelection())).get(subWheelView.getSelection())));
        childWheelView.setStyle(style);
        subWheelView.join(childWheelView);
        subWheelView.joinDatas(createChildDatas());
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
        style.selectedTextColor = Color.parseColor("#0288ce");
        style.textColor = Color.GRAY;
        style.selectedTextSize = 20;
        hourWheelView.setStyle(style);
        hourWheelView.setExtraText("时", Color.parseColor("#0288ce"), 40, 70);

        minuteWheelView = (WheelView) findViewById(R.id.minute_wheelview);
        minuteWheelView.setWheelAdapter(new ArrayWheelAdapter(this));
        minuteWheelView.setSkin(WheelView.Skin.Holo);
        minuteWheelView.setWheelData(createMinutes());
        minuteWheelView.setStyle(style);
        minuteWheelView.setExtraText("分", Color.parseColor("#0288ce"), 40, 70);

        secondWheelView = (WheelView) findViewById(R.id.second_wheelview);
        secondWheelView.setWheelAdapter(new ArrayWheelAdapter(this));
        secondWheelView.setSkin(WheelView.Skin.Holo);
        secondWheelView.setWheelData(createMinutes());
        secondWheelView.setStyle(style);
        secondWheelView.setExtraText("秒", Color.parseColor("#0288ce"), 40, 70);
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
        simpleWheelView.setWheelClickable(true);
        simpleWheelView.setOnWheelItemClickListener(new WheelView.OnWheelItemClickListener() {
            @Override
            public void onItemClick(int position, Object o) {
                WheelUtils.log("click:" + position);
            }
        });
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

    public void showDialog(View view) {
        WheelViewDialog dialog = new WheelViewDialog(this);
        dialog.setTitle("wheelview dialog").setItems(createArrays()).setButtonText("确定").setDialogStyle(Color
                .parseColor("#6699ff")).setCount(5).show();
    }

    private List<String> createMainDatas() {
        String[] strings = {"黑龙江", "吉林", "辽宁"};
        return Arrays.asList(strings);
    }

    private HashMap<String, List<String>> createSubDatas() {
        HashMap<String, List<String>> map = new HashMap<String, List<String>>();
        String[] strings = {"黑龙江", "吉林", "辽宁"};
        String[] s1 = {"哈尔滨", "齐齐哈尔", "大庆"};
        String[] s2 = {"长春", "吉林"};
        String[] s3 = {"沈阳", "大连", "鞍山", "抚顺"};
        String[][] ss = {s1, s2, s3};
        for (int i = 0; i < strings.length; i++) {
            map.put(strings[i], Arrays.asList(ss[i]));
        }
        return map;
    }

    private HashMap<String, List<String>> createChildDatas() {
        HashMap<String, List<String>> map = new HashMap<String, List<String>>();
        String[] strings = {"哈尔滨", "齐齐哈尔", "大庆", "长春", "吉林", "沈阳", "大连", "鞍山", "抚顺"};
        String[] s1 = {"道里区", "道外区", "南岗区", "香坊区"};
        String[] s2 = {"龙沙区", "建华区", "铁锋区"};
        String[] s3 = {"红岗区", "大同区"};
        String[] s11 = {"南关区", "朝阳区"};
        String[] s12 = {"龙潭区"};
        String[] s21 = {"和平区", "皇姑区", "大东区", "铁西区"};
        String[] s22 = {"中山区", "金州区"};
        String[] s23 = {"铁东区", "铁西区"};
        String[] s24 = {"新抚区", "望花区", "顺城区"};
        String[][] ss = {s1, s2, s3, s11, s12, s21, s22, s23, s24};
        for (int i = 0; i < strings.length; i++) {
            map.put(strings[i], Arrays.asList(ss[i]));
        }
        return map;
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
