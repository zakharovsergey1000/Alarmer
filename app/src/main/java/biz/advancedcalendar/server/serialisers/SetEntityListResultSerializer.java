package biz.advancedcalendar.server.serialisers;

import java.lang.reflect.Type;
import biz.advancedcalendar.wsdl.sync.Enums.SetEntityListResult;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class SetEntityListResultSerializer implements
		JsonSerializer<SetEntityListResult>, JsonDeserializer<SetEntityListResult> {
	@Override
	public JsonElement serialize(SetEntityListResult setEntityListResult, Type type,
			JsonSerializationContext jsc) {
		return new JsonPrimitive(setEntityListResult.name());
	}

	@Override
	public SetEntityListResult deserialize(JsonElement jsonElement, Type type,
			JsonDeserializationContext jdsc) throws JsonParseException {
		try {
			return SetEntityListResult.fromInt(jsonElement.getAsInt());
		} catch (Exception e) {
			return SetEntityListResult.fromString(jsonElement.getAsString());
		}
	}
}