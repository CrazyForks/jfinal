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

package com.jfinal.plugin.activerecord.sql;

import com.jfinal.template.source.ISource;

/**
 * 封装 sql 模板源
 */
class SqlSource {

	String file;
	ISource source;

	SqlSource(String file) {
		this.file = file;
		this.source = null;
	}

	SqlSource(ISource source) {
		this.file = null;
		this.source = source;
	}

	boolean isFile() {
		return file != null;
	}
}


