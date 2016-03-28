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
 * @author fengwx
 */
public class MainActivity extends Activity {

    private WheelView mainWheelView, subWheelView, wheelView1, wheelView2, wheelView3, wheelView4;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        initWheel();
//        initWheel1();
//        initWheel2();
//        initWheel3();
//        initWheel4();
    }

    private void initWheel() {
        mainWheelView = (WheelView) findViewById(R.id.main_wheelview);
        mainWheelView.setWheelAdapter(new ArrayWheelAdapter(this));
        mainWheelView.setSkin(WheelView.Skin.Holo);
        mainWheelView.setWheelData(createMainDatas());

        subWheelView = (WheelView) findViewById(R.id.sub_wheelview);
        subWheelView.setWheelAdapter(new ArrayWheelAdapter(this));
        subWheelView.setSkin(WheelView.Skin.Holo);
        subWheelView.setWheelData(createSubDatas().get(mainWheelView.getSelection()));
        mainWheelView.join(subWheelView);
        mainWheelView.joinDatas(createSubDatas());
    }

    /**
     * common皮肤
     */
    private void initWheel1() {
        wheelView1 = (WheelView) findViewById(R.id.wheelview1);
        wheelView1.setWheelAdapter(new ArrayWheelAdapter(this));
        wheelView1.setSkin(WheelView.Skin.Common);
        wheelView1.setWheelData(createArrays());
    }

    /**
     * holo皮肤
     */
    private void initWheel2() {
        wheelView2 = (WheelView) findViewById(R.id.wheelview2);
        wheelView2.setWheelAdapter(new ArrayWheelAdapter(this));
        wheelView2.setWheelSize(5);
        wheelView2.setWheelData(createArrays());
        wheelView2.setLoop(true);
        wheelView2.setExtraText("文本", Color.RED, 40, 120);
        wheelView2.setSkin(WheelView.Skin.Holo);
        WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();
        style.textColor = Color.BLACK;
        style.selectedTextColor = Color.BLUE;
        wheelView2.setStyle(style);
    }

    /**
     * 图文混排，无皮肤
     */
    private void initWheel3() {
        wheelView3 = (WheelView) findViewById(R.id.wheelview3);
        wheelView3.setWheelAdapter(new SimpleWheelAdapter(this));
        wheelView3.setWheelSize(5);
        wheelView3.setWheelData(createDatas());
        wheelView3.setSkin(WheelView.Skin.None);
        wheelView3.setSelection(2);
        wheelView3.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener<WheelData>() {
            @Override
            public void onItemSelected(int position, WheelData data) {
                WheelUtils.log("selected:" + position);
            }
        });
    }

    /**
     * 自定义布局
     */
    private void initWheel4() {
        wheelView4 = (WheelView) findViewById(R.id.wheelview4);
        wheelView4.setWheelAdapter(new MyWheelAdapter(this));
        wheelView4.setWheelSize(5);
        wheelView4.setSkin(WheelView.Skin.Holo);
        wheelView4.setWheelData(createArrays());
        WheelView.WheelViewStyle style = new WheelView.WheelViewStyle();
        style.backgroundColor = Color.YELLOW;
        style.textColor = Color.DKGRAY;
        style.selectedTextColor = Color.GREEN;
        wheelView4.setStyle(style);
        wheelView4.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener<String>() {
            @Override
            public void onItemSelected(int position, String s) {
            }
        });
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
            item.setName("item" + i);
            list.add(item);
        }
        return list;
    }

}
