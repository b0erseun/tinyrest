/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server.parameters;

import java.math.BigDecimal;

/**
 *
 * @author Sam
 */
public class ValueReader {

    public static Object readValue(String value, Class<?> clazz) {
        if (value == null) {
            return null;
        }
        if (boolean.class == clazz) {
            return Boolean.parseBoolean(value);
        }
        if (byte.class == clazz) {
            return Byte.parseByte(value);
        }
        if (short.class == clazz) {
            return Short.parseShort(value);
        }
        if (int.class == clazz) {
            return Integer.parseInt(value);
        }
        if (long.class == clazz) {
            return Long.parseLong(value);
        }
        if (float.class == clazz) {
            return Float.parseFloat(value);
        }
        if (double.class == clazz) {
            return Double.parseDouble(value);
        }
        if (BigDecimal.class == clazz) {
            return new BigDecimal(value);
        }
        return value;
    }
}
