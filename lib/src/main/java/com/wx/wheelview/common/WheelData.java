package com.wx.wheelview.common;

import java.io.Serializable;

/**
 * 滚轮数据
 *
 * @author fengwx
 */
public class WheelData implements Serializable {

    private static final long serialVersionUID = 1L;

    /**
     * 资源id
     */
    private int id;

    /**
     * 数据名称
     */
    private String name;

    public WheelData() {
    }

    public WheelData(int id, String name) {
        this.id = id;
        this.name = name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        final StringBuilder sb = new StringBuilder("WheelData{");
        sb.append("id=").append(id);
        sb.append(", name='").append(name).append('\'');
        sb.append('}');
        return sb.toString();
    }
}
