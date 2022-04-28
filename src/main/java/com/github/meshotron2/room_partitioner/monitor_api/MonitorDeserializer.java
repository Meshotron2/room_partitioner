package com.github.meshotron2.room_partitioner.monitor_api;

import com.google.gson.*;

import java.lang.reflect.Type;

/**
 * Deserializes data coming from the monitor.
 * <p>
 * All data coming from the monitor will be of a class that implements {@link MonitorData}.
 * <p>
 * For now, this it is implemented by {@link Process} (data refering to individual processes)
 * and {@link Node} (data referring to the whole node).
 */
public class MonitorDeserializer implements JsonDeserializer<MonitorData> {

    /**
     * The way this method implements the separation between implementations of {@link MonitorData} is very naive,
     * if more classes should implement it this method has to be changed.
     * <p>
     * See {@link JsonDeserializer#deserialize(JsonElement, Type, JsonDeserializationContext)}
     * for the full documentation of this method.
     */
    @Override
    public MonitorData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        final JsonObject jsonObject = jsonElement.getAsJsonObject();

        if (jsonObject.has("pid"))
            return jsonDeserializationContext.deserialize(jsonObject, Process.class);

        return jsonDeserializationContext.deserialize(jsonObject, Node.class);
    }
}
