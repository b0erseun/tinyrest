/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server.json;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.tinyrest.core.server.json.deserializers.LocalDateDeserializer;
import com.tinyrest.core.server.json.deserializers.LocalDateTimeDeserializer;
import com.tinyrest.core.server.json.deserializers.LocalTimeDeserializer;
import com.tinyrest.core.server.json.deserializers.ZonedDateTimeDeserializer;
import com.tinyrest.core.server.json.serializers.LocalDateSerializer;
import com.tinyrest.core.server.json.serializers.LocalDateTimeSerializer;
import com.tinyrest.core.server.json.serializers.LocalTimeSerializer;
import com.tinyrest.core.server.json.serializers.ZonedDateTimeSerializer;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZonedDateTime;

/**
 *
 * @author Sam
 */
public class GsonFactory {

    public static Gson createGson() {
        return new GsonBuilder()
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeDeserializer())
                .registerTypeAdapter(LocalDateTime.class, new LocalDateTimeSerializer())
                .registerTypeAdapter(LocalDate.class, new LocalDateDeserializer())
                .registerTypeAdapter(LocalDate.class, new LocalDateSerializer())
                .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeDeserializer())
                .registerTypeAdapter(ZonedDateTime.class, new ZonedDateTimeSerializer())
                .registerTypeAdapter(LocalTime.class, new LocalTimeDeserializer())
                .registerTypeAdapter(LocalTime.class, new LocalTimeSerializer())
                .create();
    }

}
