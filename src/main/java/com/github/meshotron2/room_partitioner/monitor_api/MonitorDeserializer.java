package com.github.meshotron2.room_partitioner.monitor_api;

import com.google.gson.*;

import java.lang.reflect.Type;

public class MonitorDeserializer implements JsonDeserializer<MonitorData> {
    @Override
    public MonitorData deserialize(JsonElement jsonElement, Type type, JsonDeserializationContext jsonDeserializationContext) throws JsonParseException {
        final JsonObject jsonObject = jsonElement.getAsJsonObject();

        if (jsonObject.has("pid"))
            return jsonDeserializationContext.deserialize(jsonObject, Process.class);

        return jsonDeserializationContext.deserialize(jsonObject, Node.class);
    }
}
