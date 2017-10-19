package biz.advancedcalendar.server.serialisers;

import java.lang.reflect.Type;
import biz.advancedcalendar.wsdl.sync.Enums.CreateUserResult;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class CreateUserResultSerializer implements JsonSerializer<CreateUserResult>,
		JsonDeserializer<CreateUserResult> {
	@Override
	public JsonElement serialize(CreateUserResult createUserResult, Type type,
			JsonSerializationContext jsc) {
		return new JsonPrimitive(createUserResult.name());
	}

	@Override
	public CreateUserResult deserialize(JsonElement jsonElement, Type type,
			JsonDeserializationContext jdsc) throws JsonParseException {
		try {
			return CreateUserResult.fromInt(jsonElement.getAsByte());
		} catch (Exception e) {
			return CreateUserResult.fromString(jsonElement.getAsString());
		}
	}
}