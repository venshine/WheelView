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
package com.wx.wheelview.common

import java.io.Serializable

/**
 * 滚轮数据
 *
 * @author venshine
 */
open class WheelData : Serializable {

    /**
     * 资源id
     */
    open var id: Int = 0

    /**
     * 数据名称
     */
    open var name: String? = null

    constructor()

    constructor(id: Int, name: String?) {
        this.id = id
        this.name = name
    }

    override fun toString(): String = "WheelData{id=$id, name='$name'}"

    private companion object {
        // 与原 Java 类保持一致，旧版本序列化的数据仍可反序列化
        private const val serialVersionUID = 1L
    }
}
