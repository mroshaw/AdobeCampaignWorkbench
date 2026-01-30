package com.campaignworkbench.util;

import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;


import java.io.IOException;
import java.nio.file.Path;

/**
 * JSON utility class using full Jackson ObjectMapper.
 * Supports reading and writing objects to/from JSON files.
 */
public final class JsonUtil {

    // Singleton ObjectMapper instance
    private static final ObjectMapper mapper = JsonMapper.builder()
            // You can register modules here if needed
            .build();

    private JsonUtil() {} // private constructor for utility class

    /**
     * Writes an object to a JSON file.
     *
     * @param filePath Full path to the JSON file
     * @param value    Object to serialize
     * @param <T>      Type of object
     * @throws IOException if writing fails
     */
    public static <T> void writeToJson(Path filePath, T value) throws IOException {
            mapper.writerWithDefaultPrettyPrinter().writeValue(filePath.toFile(), value);
    }

    /**
     * Reads an object from a JSON file.
     *
     * @param filePath Full path to the JSON file
     * @param type     Class type to deserialize
     * @param <T>      Type of object
     * @return Deserialized object
     * @throws IOException if reading fails
     */
    public static <T> T readFromJson(Path filePath, Class<T> type) throws IOException {
            return mapper.readValue(filePath.toFile(), type);
    }
}
