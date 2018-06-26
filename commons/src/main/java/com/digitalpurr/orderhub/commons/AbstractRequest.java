package com.digitalpurr.orderhub.commons;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import com.google.gson.annotations.SerializedName;

import java.lang.reflect.Type;

abstract class AbstractRequest {
    protected final static Gson GSON;

    static {
        GsonBuilder gsonBuilder = new GsonBuilder();
        gsonBuilder.registerTypeAdapter(RequestId.class, new ObjectTypeDeserializer());
        gsonBuilder.registerTypeAdapter(RequestId.class, new ObjectTypeSerializer());
        GSON = gsonBuilder.create();
    }

    @SerializedName("r") RequestId id;

    public String toJson() {
        return GSON.toJson(this);
    }

    private final static class ObjectTypeDeserializer implements JsonDeserializer<RequestId> {
        @Override
        public RequestId deserialize(JsonElement json, Type typeOfT, JsonDeserializationContext context) throws JsonParseException {
            int typeInt = json.getAsInt();
            return RequestId.values()[typeInt];
        }
    }

    private final static class ObjectTypeSerializer implements JsonSerializer<RequestId> {
        @Override
        public JsonElement serialize(RequestId src, Type typeOfSrc, JsonSerializationContext context) {
            return new JsonPrimitive(src.ordinal());
        }
    }
}
