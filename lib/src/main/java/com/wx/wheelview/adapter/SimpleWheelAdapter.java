package com.wx.wheelview.adapter;

import android.content.Context;
import android.view.View;
import android.view.ViewGroup;

import com.wx.wheelview.common.WheelData;
import com.wx.wheelview.widget.WheelItem;

/**
 * 滚轮图片和文本适配器
 *
 * @author fengwx
 */
public class SimpleWheelAdapter extends BaseWheelAdapter<WheelData> {

    private Context mContext;

    public SimpleWheelAdapter(Context context) {
        mContext = context;
    }

    @Override
    public View bindView(int position, View convertView, ViewGroup parent) {
        if (convertView == null) {
            convertView = new WheelItem(mContext);
        }
        WheelItem item = (WheelItem) convertView;
        item.setImage(mList.get(position).getId());
        item.setText(mList.get(position).getName());
        return convertView;
    }

}
