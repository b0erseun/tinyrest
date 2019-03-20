package com.tinyrest.core.server.parameters;

/**
 *
 */
public interface RequestReader {

    /**
     * Reads and serializes json data into expected object type
     *
     * @param encodingType expected class for json to serialized into
     * @param componentType used to serialized individual items when @code(encodingType) is a array or collection
     * @return serialized object of type encodingType
     */
    Object readParameterValue(Class encodingType, Class componentType);

}
