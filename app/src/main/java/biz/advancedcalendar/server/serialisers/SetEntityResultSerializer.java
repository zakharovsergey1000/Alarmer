package biz.advancedcalendar.server.serialisers;

import java.lang.reflect.Type;
import biz.advancedcalendar.wsdl.sync.Enums.SetEntityResult;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class SetEntityResultSerializer implements JsonSerializer<SetEntityResult>,
		JsonDeserializer<SetEntityResult> {
	@Override
	public JsonElement serialize(SetEntityResult setEntityResult, Type type,
			JsonSerializationContext jsc) {
		return new JsonPrimitive(setEntityResult.name());
	}

	@Override
	public SetEntityResult deserialize(JsonElement jsonElement, Type type,
			JsonDeserializationContext jdsc) throws JsonParseException {
		try {
			return SetEntityResult.fromInt(jsonElement.getAsInt());
		} catch (Exception e) {
			return SetEntityResult.fromString(jsonElement.getAsString());
		}
	}
}