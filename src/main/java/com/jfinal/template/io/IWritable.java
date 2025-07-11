/**
 * Copyright (c) 2011-2023, James Zhan 詹波 (jfinal@126.com).
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

package com.jfinal.template.io;

/**
 * IWritable 支持 OutputStream、Writer 双模式动态切换输出
 *
 * 详见 com.jfinal.template.stat.ast.Text 中的用法
 */
public interface IWritable {

    /**
     * 供 OutputStream 模式下的 ByteWrite 使用
     */
    public byte[] getBytes();

    /**
     * 供 Writer 模式下的 CharWrite 使用
     */
    public char[] getChars();
}



