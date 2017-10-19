package biz.advancedcalendar.server.serialisers;

import java.lang.reflect.Type;
import biz.advancedcalendar.wsdl.sync.Enums.EntityType;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class EntityTypeSerializer implements JsonSerializer<EntityType>,
		JsonDeserializer<EntityType> {
	@Override
	public JsonElement serialize(EntityType entityType, Type type,
			JsonSerializationContext jsc) {
		return new JsonPrimitive(entityType.name());
	}

	@Override
	public EntityType deserialize(JsonElement jsonElement, Type type,
			JsonDeserializationContext jdsc) throws JsonParseException {
		try {
			return EntityType.fromInt(jsonElement.getAsByte());
		} catch (Exception e) {
			return EntityType.fromString(jsonElement.getAsString());
		}
	}
}