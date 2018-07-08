package com.digitalpurr.orderhub.commons;

import java.lang.reflect.Type;

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

public class BaseRequest {
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
    
    public final static RequestId deserializeRequestId(String message) {
    	return GSON.fromJson(message, BaseRequest.class).id;
    }
    
	public final static <T> T deserializeRequest(String input, Class<T> outputClass) {
    	return (T) GSON.fromJson(input, outputClass);
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
