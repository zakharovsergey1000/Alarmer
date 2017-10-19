package biz.advancedcalendar.server.serialisers;

import java.lang.reflect.Type;
import biz.advancedcalendar.wsdl.sync.Enums.GetEntityListResult;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class GetEntityListResultSerializer implements
		JsonSerializer<GetEntityListResult>, JsonDeserializer<GetEntityListResult> {
	@Override
	public JsonElement serialize(GetEntityListResult getEntityListResult, Type type,
			JsonSerializationContext jsc) {
		return new JsonPrimitive(getEntityListResult.name());
	}

	@Override
	public GetEntityListResult deserialize(JsonElement jsonElement, Type type,
			JsonDeserializationContext jdsc) throws JsonParseException {
		try {
			return GetEntityListResult.fromInt(jsonElement.getAsByte());
		} catch (Exception e) {
			return GetEntityListResult.fromString(jsonElement.getAsString());
		}
	}
}