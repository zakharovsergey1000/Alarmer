package biz.advancedcalendar.server.serialisers;

import java.lang.reflect.Type;
import biz.advancedcalendar.wsdl.sync.Enums.AuthenticateResult;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class AuthenticateResultSerializer implements JsonSerializer<AuthenticateResult>,
		JsonDeserializer<AuthenticateResult> {
	@Override
	public JsonElement serialize(AuthenticateResult authenticateResult, Type type,
			JsonSerializationContext jsc) {
		return new JsonPrimitive(authenticateResult.name());
	}

	@Override
	public AuthenticateResult deserialize(JsonElement jsonElement, Type type,
			JsonDeserializationContext jdsc) throws JsonParseException {
		try {
			return AuthenticateResult.fromInt(jsonElement.getAsInt());
		} catch (Exception e) {
			return AuthenticateResult.fromString(jsonElement.getAsString());
		}
	}
}