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

package com.jfinal.template.expr.ast;

import java.math.BigDecimal;
import java.math.BigInteger;
import com.jfinal.template.TemplateException;
import com.jfinal.template.expr.Sym;
import com.jfinal.template.stat.Location;
import com.jfinal.template.stat.ParseException;
import com.jfinal.template.stat.Scope;

/**
 * Compare
 *
 * 1：支持 byte short int long float double BigInteger BigDecimal 的 == != > >= < <= 操作
 * 2：== != 作用于 string，调用其 equals 方法进行比较
 * 3：> >= < <= 可以比较实现了 Comparable 接口的对象
 *
 * 注意：float double 浮点型数据在比较操作时，具有精度上的局限性，不建议对浮点数进行比较
 */
public class Compare extends Expr {

    private Sym op;
    private Expr left;
    private Expr right;

    public Compare(Sym op, Expr left, Expr right, Location location) {
        if (left == null || right == null) {
            throw new ParseException("The target of \"" + op.value() + "\" operator can not be blank", location);
        }
        this.op = op;
        this.left = left;
        this.right = right;
        this.location = location;
    }

    public Object eval(Scope scope) {
        Object leftValue = left.eval(scope);
        Object rightValue = right.eval(scope);

        switch(op) {
        case EQUAL:
            return equal(leftValue, rightValue);
        case NOTEQUAL:
            return equal(leftValue, rightValue) ? Boolean.FALSE : Boolean.TRUE;
        case GT:
            return gt(leftValue, rightValue);
        case GE:
            return ge(leftValue, rightValue);
        case LT:
            return lt(leftValue, rightValue);
        case LE:
            return le(leftValue, rightValue);
        default:
            String l = leftValue  != null ? leftValue.getClass().getSimpleName()  : "null";
            String r = rightValue != null ? rightValue.getClass().getSimpleName() : "null";
            throw new TemplateException("Unsupported operation: " + l + " \"" + op.value() + "\" " + r, location);
        }
    }

    Boolean equal(Object leftValue, Object rightValue) {
        if (leftValue == rightValue) {
            return Boolean.TRUE;
        }
        if (leftValue == null || rightValue == null) {
            return Boolean.FALSE;
        }
        if (leftValue.equals(rightValue)) {
            return Boolean.TRUE;
        }
        if (leftValue instanceof Number && rightValue instanceof Number) {
            Number l = (Number)leftValue;
            Number r = (Number)rightValue;
            int maxType = Arith.getMaxType(l, r);
            switch (maxType) {
            case Arith.INT:
                return l.intValue() == r.intValue();
            case Arith.LONG:
                return l.longValue() == r.longValue();
            case Arith.FLOAT:
                // 此法仅适用于两个对象类型相同的情况，升级为 BigDecimal 后精度会再高几个数量级
                // return Float.floatToIntBits(l.floatValue()) == Float.floatToIntBits(r.floatValue());
                return l.floatValue() == r.floatValue();
            case Arith.DOUBLE:
                // 此法仅适用于两个对象类型相同的情况，升级为 BigDecimal 后精度会再高几个数量级
                // return Double.doubleToLongBits(l.doubleValue()) == Double.doubleToLongBits(r.doubleValue());
                return l.doubleValue() == r.doubleValue();
            case Arith.BIGDECIMAL:
                BigDecimal[] bd = Arith.toBigDecimals(l, r);
                return (bd[0]).compareTo(bd[1]) == 0;
            case Arith.BIGINTEGER:
                BigInteger[] bi = Arith.toBigIntegers(l, r);
                return (bi[0]).compareTo(bi[1]) == 0;
            case Arith.UNKNOWN:
                throw Arith.unsupportedTypeException(l, r, location);
            }
            throw new TemplateException("Equal comparison support types of int long float double BigInteger and BigDeciaml", location);
        }

        return Boolean.FALSE;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    Boolean gt(Object leftValue, Object rightValue) {
        if (leftValue instanceof Number && rightValue instanceof Number) {
            Number l = (Number)leftValue;
            Number r = (Number)rightValue;
            int maxType = Arith.getMaxType(l, r);
            switch (maxType) {
            case Arith.INT:
                return l.intValue() > r.intValue();
            case Arith.LONG:
                return l.longValue() > r.longValue();
            case Arith.FLOAT:
                // return Float.floatToIntBits(l.floatValue()) > Float.floatToIntBits(r.floatValue());
                return l.floatValue() > r.floatValue();
            case Arith.DOUBLE:
                // return Double.doubleToLongBits(l.doubleValue()) > Double.doubleToLongBits(r.doubleValue());
                return l.doubleValue() > r.doubleValue();
            case Arith.BIGDECIMAL:
                BigDecimal[] bd = Arith.toBigDecimals(l, r);
                return (bd[0]).compareTo(bd[1]) > 0;
            case Arith.BIGINTEGER:
                BigInteger[] bi = Arith.toBigIntegers(l, r);
                return (bi[0]).compareTo(bi[1]) > 0;
            case Arith.UNKNOWN:
                throw Arith.unsupportedTypeException(l, r, location);
            }
            throw new TemplateException("Unsupported operation: " + l.getClass().getSimpleName() + " \">\" " + r.getClass().getSimpleName(), location);
        }

        if (leftValue instanceof Comparable &&
            rightValue != null &&
            leftValue.getClass() == rightValue.getClass()) {
            return ((Comparable)leftValue).compareTo((Comparable)rightValue) > 0;
        }

        return checkComparisonValue(leftValue, rightValue);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    Boolean ge(Object leftValue, Object rightValue) {
        if (leftValue instanceof Number && rightValue instanceof Number) {
            Number l = (Number)leftValue;
            Number r = (Number)rightValue;
            int maxType = Arith.getMaxType(l, r);
            switch (maxType) {
            case Arith.INT:
                return l.intValue() >= r.intValue();
            case Arith.LONG:
                return l.longValue() >= r.longValue();
            case Arith.FLOAT:
                // return Float.floatToIntBits(l.floatValue()) >= Float.floatToIntBits(r.floatValue());
                return l.floatValue() >= r.floatValue();
            case Arith.DOUBLE:
                // return Double.doubleToLongBits(l.doubleValue()) >= Double.doubleToLongBits(r.doubleValue());
                return l.doubleValue() >= r.doubleValue();
            case Arith.BIGDECIMAL:
                BigDecimal[] bd = Arith.toBigDecimals(l, r);
                return (bd[0]).compareTo(bd[1]) >= 0;
            case Arith.BIGINTEGER:
                BigInteger[] bi = Arith.toBigIntegers(l, r);
                return (bi[0]).compareTo(bi[1]) >= 0;
            case Arith.UNKNOWN:
                throw Arith.unsupportedTypeException(l, r, location);
            }
            throw new TemplateException("Unsupported operation: " + l.getClass().getSimpleName() + " \">=\" " + r.getClass().getSimpleName(), location);
        }

        if (leftValue instanceof Comparable &&
            rightValue != null &&
            leftValue.getClass() == rightValue.getClass()) {
            return ((Comparable)leftValue).compareTo((Comparable)rightValue) >= 0;
        }

        return checkComparisonValue(leftValue, rightValue);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    Boolean lt(Object leftValue, Object rightValue) {
        if (leftValue instanceof Number && rightValue instanceof Number) {
            Number l = (Number)leftValue;
            Number r = (Number)rightValue;
            int maxType = Arith.getMaxType(l, r);
            switch (maxType) {
            case Arith.INT:
                return l.intValue() < r.intValue();
            case Arith.LONG:
                return l.longValue() < r.longValue();
            case Arith.FLOAT:
                // return Float.floatToIntBits(l.floatValue()) < Float.floatToIntBits(r.floatValue());
                return l.floatValue() < r.floatValue();
            case Arith.DOUBLE:
                // return Double.doubleToLongBits(l.doubleValue()) < Double.doubleToLongBits(r.doubleValue());
                return l.doubleValue() < r.doubleValue();
            case Arith.BIGDECIMAL:
                BigDecimal[] bd = Arith.toBigDecimals(l, r);
                return (bd[0]).compareTo(bd[1]) < 0;
            case Arith.BIGINTEGER:
                BigInteger[] bi = Arith.toBigIntegers(l, r);
                return (bi[0]).compareTo(bi[1]) < 0;
            case Arith.UNKNOWN:
                throw Arith.unsupportedTypeException(l, r, location);
            }
            throw new TemplateException("Unsupported operation: " + l.getClass().getSimpleName() + " \"<\" " + r.getClass().getSimpleName(), location);
        }

        if (leftValue instanceof Comparable &&
            rightValue != null &&
            leftValue.getClass() == rightValue.getClass()) {
            return ((Comparable)leftValue).compareTo((Comparable)rightValue) < 0;
        }

        return checkComparisonValue(leftValue, rightValue);
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    Boolean le(Object leftValue, Object rightValue) {
        if (leftValue instanceof Number && rightValue instanceof Number) {
            Number l = (Number)leftValue;
            Number r = (Number)rightValue;
            int maxType = Arith.getMaxType(l, r);
            switch (maxType) {
            case Arith.INT:
                return l.intValue() <= r.intValue();
            case Arith.LONG:
                return l.longValue() <= r.longValue();
            case Arith.FLOAT:
                // return Float.floatToIntBits(l.floatValue()) <= Float.floatToIntBits(r.floatValue());
                return l.floatValue() <= r.floatValue();
            case Arith.DOUBLE:
                // return Double.doubleToLongBits(l.doubleValue()) <= Double.doubleToLongBits(r.doubleValue());
                return l.doubleValue() <= r.doubleValue();
            case Arith.BIGDECIMAL:
                BigDecimal[] bd = Arith.toBigDecimals(l, r);
                return (bd[0]).compareTo(bd[1]) <= 0;
            case Arith.BIGINTEGER:
                BigInteger[] bi = Arith.toBigIntegers(l, r);
                return (bi[0]).compareTo(bi[1]) <= 0;
            case Arith.UNKNOWN:
                throw Arith.unsupportedTypeException(l, r, location);
            }
            throw new TemplateException("Unsupported operation: " + l.getClass().getSimpleName() + " \"<=\" " + r.getClass().getSimpleName(), location);
        }

        if (leftValue instanceof Comparable &&
            rightValue != null &&
            leftValue.getClass() == rightValue.getClass()) {
            return ((Comparable)leftValue).compareTo((Comparable)rightValue) <= 0;
        }

        return checkComparisonValue(leftValue, rightValue);
    }

    private Boolean checkComparisonValue(Object leftValue, Object rightValue) {
        if (leftValue == null) {
            throw new TemplateException("The operation target on the left side of \"" + op.value() + "\" can not be null", location);
        }
        if (rightValue == null) {
            throw new TemplateException("The operation target on the right side of \"" + op.value() + "\" can not be null", location);
        }

        throw new TemplateException(
            "Unsupported operation: " +
            leftValue.getClass().getSimpleName() +
            " \"" + op.value() + "\" " +
            rightValue.getClass().getSimpleName(),
            location
        );
    }
}









