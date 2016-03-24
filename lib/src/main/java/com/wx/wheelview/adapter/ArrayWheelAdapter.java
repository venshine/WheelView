package com.wx.wheelview.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.wx.wheelview.widget.WheelItem;

/**
 * 滚轮文本适配器
 *
 * @author fengwx
 */
public class ArrayWheelAdapter extends BaseWheelAdapter<String> {

    private Context mContext;

    public ArrayWheelAdapter(Context context) {
        mContext = context;
    }

    @Override
    public View bindView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new WheelItem(mContext);
        }
        WheelItem item = (WheelItem) convertView;
        item.setText(mList.get(position));
        return convertView;
    }

}
