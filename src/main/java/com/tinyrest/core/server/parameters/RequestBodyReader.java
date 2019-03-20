/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.tinyrest.core.server.parameters;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.google.gson.annotations.SerializedName;
import com.tinyrest.core.server.json.GsonFactory;
import com.tinyrest.core.server.http.request.HttpRequest;
import java.io.IOException;
import java.lang.reflect.Field;
import java.lang.reflect.ParameterizedType;
import java.util.*;
import java.util.stream.Collectors;

import org.apache.commons.io.IOUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;


/**
 *
 * @author Sam
 */
public class RequestBodyReader implements RequestReader{

    private HttpRequest request;
    private final static Logger LOGGER = LoggerFactory.getLogger(RequestBodyReader.class);

    public RequestBodyReader(HttpRequest request) {
        this.request = request;
    }

    @Override
    public Object readParameterValue(Class encodingType, Class componentType) {
        Gson gson = GsonFactory.createGson();
        try {
            String myString = IOUtils.toString(request.getRequest().getInputStream(), "UTF-8");
            LOGGER.debug("Received " + encodingType.getCanonicalName() + " as " + myString);
            StringBuilder message = new StringBuilder();

            //Check if receiver is expecting array or collection
            if(encodingType.isArray() || Collection.class.isAssignableFrom(encodingType)){
                JsonArray jsonArray = gson.fromJson(myString, JsonArray.class);

                //each item in array is checked against componentType
                for (int i=0;i<jsonArray.size();i++) {
                    JsonElement jsonElem = jsonArray.get(i);
                    Map<String, List<Object>> map = getRequiredMap("["+i+"]", componentType, jsonElem);
                    message.append(print(map));
                }
            }else{
                Map<String, List<Object>> map = getRequiredMap("", encodingType, gson.fromJson(myString, JsonObject.class));
                message.append(print(map));
            }

            //If any fields are missing message will have text
            if(message.toString().isEmpty()){
                return gson.fromJson(myString, encodingType);
            }else {
                throw new RuntimeException("The following required fields are missing:\n" + message.toString());
            }

        } catch (IOException ioex) {
            return null;
        }
    }

    /**
     * Gathers and returns missing fields in a human readable fashion
     *
     * @param map missing field map
     * @return human readable text
     */
    private static String print(Map<String, List<Object>> map){
        StringBuilder stringBuilder = new StringBuilder();
        map.entrySet().stream()
                .filter(e->!e.getValue().isEmpty())//skips empty lists
                .forEach(e->
                    e.getValue().forEach(li->{
                        if(li instanceof String){
                            String missingField = "";
                            if(!e.getKey().isEmpty()){
                                missingField += e.getKey() + "=>";
                            }
                            missingField += li;
                            stringBuilder.append(missingField);
                        }else{
                            //Where a field is a sub object a new map containing that objects missing fields is returned
                            stringBuilder.append(print((Map<String, List<Object>>)li));
                        }
                    }
                )
        );
        return stringBuilder.toString();
    }

    /**
     * Gets all the required fields for a class and any ancestor classes
     *
     * @param type class to pull fields from
     * @return list of fields
     */
    private static List<Field> getRequiredFields(Class type){
        return new ArrayList<>();
    }

    /**
     * Gets the map of missing fields
     *
     * @param parent the direct parent tag name possibilities(and index if applicable), left blank for highest ancestor if it is an object
     * @param type expected class type to be met for json to be valid
     * @param jsonElem data to check
     * @return Map of missing fields
     */
    private Map<String, List<Object>> getRequiredMap(String parent, Class type, JsonElement jsonElem){
        Map<String, List<Object>> map = new HashMap<>();
        //Get a list of required fields
        List<Field> fields = getRequiredFields(type);
        //holds any missing fields or missing objects
        List<Object> missingFields = new ArrayList<>();

        for(Field f: fields){
            String fieldName = getFieldName(f);//Get human readable expected tag name possibilities

            if(!f.getType().isPrimitive() && f.getType() != String.class){
                //field is expecting a complex object

                //check expected type is array or collection
                if(f.getType().isArray() || Collection.class.isAssignableFrom(f.getType())){

                    if(isFieldPresent(jsonElem.getAsJsonObject(), f)) {
                        //array found and must now check each item in this array for missing fields
                        JsonArray jsonArray = getJsonElement(jsonElem.getAsJsonObject(), f).getAsJsonArray();

                        //The item class type
                        Class genericTypeClass;

                        if(Collection.class.isAssignableFrom(f.getType())) {
                            //get item class from collection
                            genericTypeClass = (Class<?>) ((ParameterizedType) f.getGenericType()).getActualTypeArguments()[0];
                        }else{
                            //get item class for an array
                            genericTypeClass = f.getType().getComponentType();
                        }

                        //Recursively calls this method to check each item in array against expected class fields
                        for (int i = 0; i < jsonArray.size(); i++) {
                            missingFields.add(getRequiredMap(fieldName + "[" + i + "]", genericTypeClass, jsonArray.get(i)));
                        }
                    }else {
                        //array missing from json, marked and returned
                        missingFields.add(fieldName + "[]");
                    }

                }else{//expected type is an object

                    if(isFieldPresent(jsonElem.getAsJsonObject(), f)){
                        //if object is found in json, recursively calls to check objects fields for missing field
                        missingFields.add(getRequiredMap(fieldName, f.getType(), getJsonElement(jsonElem.getAsJsonObject(), f)));
                    }else{
                        //object not found, mark as missing object
                        missingFields.add(fieldName + "{}");
                    }

                }

            } else {//if expected field type is primitive or String, does a simple check
                if(!isFieldPresent(jsonElem.getAsJsonObject(), f)){
                    missingFields.add(fieldName);
                }
            }

        }

        map.put(parent, missingFields);//Add missing fields and objects to response
        return map;
    }

    /**
     * Gets the expected field name possibilities from annotations in human readable format
     *
     * @param field to check
     * @return text in human readable fashion of tag names
     */
    private String getFieldName(Field field){
        String fieldName = "";
        if(field.isAnnotationPresent(SerializedName.class)){//checks for gson annotation and returns these values
            SerializedName annotationInstance =field.getAnnotation(SerializedName.class);
            fieldName += annotationInstance.value();
            List<String> alternateNames = Arrays.asList(annotationInstance.alternate());
            if(!alternateNames.isEmpty()){
                fieldName += "(";
                fieldName += alternateNames.parallelStream().collect(Collectors.joining("/"));
                fieldName += ")";
            }
        }else {
            fieldName = field.getName();
        }
        return fieldName;
    }

    /**
     * Checks if field tag is present in a json object
     *
     * @param jsonObject data to check
     * @param field to check
     * @return true if found, false if not
     */
    private boolean isFieldPresent(JsonObject jsonObject, Field field){
        if(field.isAnnotationPresent(SerializedName.class)){
            SerializedName annotationInstance =field.getAnnotation(SerializedName.class);
            //Check required value tag
            return jsonObject.has(annotationInstance.value())
                    //Checks for presence of any alternative tags
                    || Arrays.asList(annotationInstance.alternate()).parallelStream().anyMatch(jsonObject::has);
        }else {
            //If gson not used checks field name
            return jsonObject.has(field.getName());
        }
    }

    /**
     * Gets a json element for a specific field
     *
     * @param jsonObject parent object
     * @param field field to fetch value for
     * @return resulting jsonElement
     */
    private JsonElement getJsonElement(JsonObject jsonObject, Field field){
        if(field.isAnnotationPresent(SerializedName.class)){
            SerializedName annotationInstance =field.getAnnotation(SerializedName.class);
            if(jsonObject.has(annotationInstance.value())){
                //If field value is present fetch result
                return jsonObject.get(annotationInstance.value());
            }else {
                //if value not found try to find value for each tag alternative
                return Arrays.asList(annotationInstance.alternate())
                        .parallelStream()
                        .filter(jsonObject::has)
                        .map(jsonObject::get)
                        .findFirst()
                        .orElseThrow(RuntimeException::new);
            }
        }else {
            return jsonObject.get(field.getName());
        }
    }

}
