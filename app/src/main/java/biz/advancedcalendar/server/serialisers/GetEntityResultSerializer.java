package biz.advancedcalendar.server.serialisers;

import java.lang.reflect.Type;
import biz.advancedcalendar.wsdl.sync.Enums.GetEntityResult;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class GetEntityResultSerializer implements JsonSerializer<GetEntityResult>,
		JsonDeserializer<GetEntityResult> {
	@Override
	public JsonElement serialize(GetEntityResult getEntityResult, Type type,
			JsonSerializationContext jsc) {
		return new JsonPrimitive(getEntityResult.name());
	}

	@Override
	public GetEntityResult deserialize(JsonElement jsonElement, Type type,
			JsonDeserializationContext jdsc) throws JsonParseException {
		try {
			return GetEntityResult.fromInt(jsonElement.getAsInt());
		} catch (Exception e) {
			return GetEntityResult.fromString(jsonElement.getAsString());
		}
	}
}