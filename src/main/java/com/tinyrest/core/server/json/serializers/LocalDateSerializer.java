/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server.json.serializers;

import com.google.gson.JsonElement;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

import java.lang.reflect.Type;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

/**
 *
 * @author Sam
 */
public class LocalDateSerializer implements  JsonSerializer<LocalDate>{

    @Override
    public JsonElement serialize(LocalDate src, Type typeOfSrc, JsonSerializationContext context) {
        
        DateTimeFormatter format = DateTimeFormatter.ISO_LOCAL_DATE;
        
        return new JsonPrimitive(src.format(format));
        
    }
    
    
    
}
