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
package com.wx.wheelview.widget;

import com.wx.wheelview.adapter.BaseWheelAdapter;

import java.util.HashMap;
import java.util.List;

/**
 * 滚轮抽象接口
 *
 * @author venshine
 */
public interface IWheelView<T> {

    /**
     * 滚轮数据是否循环，默认不循环
     */
    boolean LOOP = false;

    /**
     * 滚轮默认数量
     */
    int WHEEL_SIZE = 3;

    /**
     * 滚轮选中刻度是否可点击
     */
    boolean CLICKABLE = false;

    /**
     * 设置滚轮选中刻度是否可点击
     *
     * @param clickable
     */
    void setWheelClickable(boolean clickable);

    /**
     * 设置滚轮是否循环滚动
     *
     * @param loop
     */
    void setLoop(boolean loop);

    /**
     * 设置滚轮个数
     *
     * @param wheelSize
     */
    void setWheelSize(int wheelSize);

    /**
     * 设置滚轮数据
     *
     * @param list
     */
    void setWheelData(List<T> list);

    /**
     * 设置滚轮数据源适配器
     *
     * @param adapter
     */
    void setWheelAdapter(BaseWheelAdapter<T> adapter);

    /**
     * 连接副WheelView
     *
     * @param wheelView
     */
    void join(WheelView wheelView);

    /**
     * 副WheelView数据
     *
     * @param map
     */
    void joinDatas(HashMap<String, List<T>> map);


}
