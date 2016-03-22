package com.wx.wheelview.demo;

import android.app.Activity;
import android.os.Bundle;

import com.wx.wheelview.ArrayWheelAdapter;
import com.wx.wheelview.WheelData;
import com.wx.wheelview.WheelView;

import java.util.ArrayList;

/**
 * @author fengwx
 */
public class MainActivity extends Activity {

    private WheelView wheelView;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        wheelView = (WheelView) findViewById(R.id.wheelview);
        wheelView.setWheelData(createLists1());
        wheelView.setWheelSize(5);
        wheelView.setLoop(false);
        wheelView.setSkin(WheelView.Skin.None);
//        SimpleWheelAdapter adapter = new SimpleWheelAdapter(this);
        ArrayWheelAdapter adapter = new ArrayWheelAdapter(this);
        wheelView.setWheelAdapter(adapter);
        wheelView.setSelection(0);
//        wheelView.setOnWheelItemSelectedListener(new WheelView.OnWheelItemSelectedListener<WheelData>() {
//            @Override
//            public void onItemSelected(int position, WheelData t) {
//                Log.d("fengwx", "position:" + position + ", t:" + t.toString());
//            }
//        });
    }

    private ArrayList<String> createLists1() {
        ArrayList<String> list = new ArrayList<String>();
        for (int i = 0; i < 10; i++) {
            list.add("item:" + i);
        }
        return list;
    }

    private ArrayList<WheelData> createLists() {
        ArrayList<WheelData> list = new ArrayList<WheelData>();
        WheelData item;
        for (int i = 0; i < 10; i++) {
            item = new WheelData();
            item.setId(R.mipmap.ic_launcher);
            item.setName("item:" + i);
            list.add(item);
        }
        return list;
    }

}
