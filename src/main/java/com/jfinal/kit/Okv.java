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

package com.jfinal.kit;

import java.math.BigDecimal;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.function.Function;
import com.jfinal.json.Json;

/**
 * Okv (Ordered Key Value)
 *
 * Okv 与 Kv 的唯一区别在于 Okv 继承自 LinkedHashMap，而 Kv 继承自 HashMap
 * 所以对 Okv 中的数据进行迭代输出的次序与数据插入的先后次序一致
 *
 * Example：
 *    Okv para = Okv.of("id", 123);
 *    User user = user.findFirst(getSqlPara("find", para));
 */
@SuppressWarnings({"rawtypes", "unchecked"})
public class Okv extends LinkedHashMap {

	private static final long serialVersionUID = -6517132544791494383L;

	public Okv() {
	}

	public static Okv of(Object key, Object value) {
		return new Okv().set(key, value);
	}

	public static Okv by(Object key, Object value) {
		return new Okv().set(key, value);
	}

	public static Okv create() {
		return new Okv();
	}

	public Okv set(Object key, Object value) {
		super.put(key, value);
		return this;
	}

	public Okv setIfNotBlank(Object key, String value) {
		if (StrKit.notBlank(value)) {
			set(key, value);
		}
		return this;
	}

	public Okv setIfNotNull(Object key, Object value) {
		if (value != null) {
			set(key, value);
		}
		return this;
	}

	public Okv set(Map map) {
		super.putAll(map);
		return this;
	}

	public Okv set(Okv okv) {
		super.putAll(okv);
		return this;
	}

	public Okv remove(Object key) {
		super.remove(key);
		return this;
	}

	/**
	 * remove 方法已取代 delete
	 */
	@Deprecated
	public Okv delete(Object key) {
		super.remove(key);
		return this;
	}

	public <T> T getAs(Object key) {
		return (T)get(key);
	}

	public <T> T getAs(Object key, T defaultValue) {
		Object ret = get(key);
		return ret != null ? (T) ret : defaultValue;
	}

	public <T> T getAs(Object key, Function<Object, T> converter) {
		Object ret = get(key);
		return ret != null ? converter.apply(ret) : null;
	}

	public <T> T getAs(Object key, T defaultValue, Function<Object, T> converter) {
		Object ret = get(key);
		return ret != null ? converter.apply(ret) : defaultValue;
	}

	public String getStr(Object key) {
		Object s = get(key);
		return s != null ? s.toString() : null;
	}

	public Integer getInt(Object key) {
		return TypeKit.toInt(get(key));
	}

	public Long getLong(Object key) {
		return TypeKit.toLong(get(key));
	}

	public BigDecimal getBigDecimal(Object key) {
		return TypeKit.toBigDecimal(get(key));
	}

	public Double getDouble(Object key) {
		return TypeKit.toDouble(get(key));
	}

	public Float getFloat(Object key) {
		return TypeKit.toFloat(get(key));
	}

	public Number getNumber(Object key) {
		return TypeKit.toNumber(get(key));
	}

	public Boolean getBoolean(Object key) {
		return TypeKit.toBoolean(get(key));
	}

	public java.util.Date getDate(Object key) {
		return TypeKit.toDate(get(key));
	}

	public java.time.LocalDateTime getLocalDateTime(Object key) {
		return TypeKit.toLocalDateTime(get(key));
	}

	public String getStr(Object key, String defaultValue) {
		Object s = get(key);
		return s != null ? s.toString() : defaultValue;
	}

	public Integer getInt(Object key, Integer defaultValue) {
		Object value = get(key);
		return value != null ? TypeKit.toInt(value) : defaultValue;
	}

	public Long getLong(Object key, Long defaultValue) {
		Object value = get(key);
		return value != null ? TypeKit.toLong(value) : defaultValue;
	}

	public java.math.BigDecimal getBigDecimal(Object key, java.math.BigDecimal defaultValue) {
		Object value = get(key);
		return value != null ? TypeKit.toBigDecimal(value) : defaultValue;
	}

	public Double getDouble(Object key, Double defaultValue) {
		Object value = get(key);
		return value != null ? TypeKit.toDouble(value) : defaultValue;
	}

	public Float getFloat(Object key, Float defaultValue) {
		Object value = get(key);
		return value != null ? TypeKit.toFloat(value) : defaultValue;
	}

	public Number getNumber(Object key, Number defaultValue) {
		Object value = get(key);
		return value != null ? TypeKit.toNumber(value) : defaultValue;
	}

	public Boolean getBoolean(Object key, Boolean defaultValue) {
		Object value = get(key);
		return value != null ? TypeKit.toBoolean(value) : defaultValue;
	}

	public java.util.Date getDate(Object key, java.util.Date defaultValue) {
		Object value = get(key);
		return value != null ? TypeKit.toDate(value) : defaultValue;
	}

	public java.time.LocalDateTime getLocalDateTime(Object key, java.time.LocalDateTime defaultValue) {
		Object value = get(key);
		return value != null ? TypeKit.toLocalDateTime(value) : defaultValue;
	}

	/**
	 * key 存在，并且 value 不为 null
	 */
	public boolean notNull(Object key) {
		return get(key) != null;
	}

	/**
	 * key 不存在，或者 key 存在但 value 为null
	 */
	public boolean isNull(Object key) {
		return get(key) == null;
	}

	/**
	 * key 所对应的 value 值不为空白字符串
	 */
	public boolean notBlank(Object key) {
		return StrKit.notBlank(getStr(key));
	}

	/**
	 * key 所对应的 value 值为空白字符串
	 */
	public boolean isBlank(Object key) {
		return StrKit.isBlank(getStr(key));
	}

	/**
	 * key 存在，并且 value 为 true，则返回 true
	 */
	public boolean isTrue(Object key) {
		Object value = get(key);
		return value != null && TypeKit.toBoolean(value);
	}

	/**
	 * key 存在，并且 value 为 false，则返回 true
	 */
	public boolean isFalse(Object key) {
		Object value = get(key);
		return value != null && !TypeKit.toBoolean(value);
	}

	public String toJson() {
		return Json.getJson().toJson(this);
	}

	public boolean equals(Object okv) {
		return okv instanceof Okv && super.equals(okv);
	}

	public Okv keep(String... keys) {
		if (keys != null && keys.length > 0) {
			Okv newOkv = Okv.create();
			for (String k : keys) {
				if (containsKey(k)) {	// 避免将并不存在的变量存为 null
					newOkv.put(k, get(k));
				}
			}

			clear();
			putAll(newOkv);
		} else {
			clear();
		}

		return this;
	}

	public <K, V>Map<K, V> toMap() {
		return this;
	}
}


