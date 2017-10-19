package biz.advancedcalendar.server.serialisers;

import biz.advancedcalendar.greendao.Task.InformationUnitSelector;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;

public class InformationUnitSelectorSerializer implements
		JsonSerializer<InformationUnitSelector>,
		JsonDeserializer<InformationUnitSelector> {
	@Override
	public JsonElement serialize(InformationUnitSelector informationUnitSelector, Type type,
			JsonSerializationContext jsc) {
		return new JsonPrimitive(informationUnitSelector.name());
	}

	@Override
	public InformationUnitSelector deserialize(JsonElement jsonElement, Type type,
			JsonDeserializationContext jdsc) throws JsonParseException {
		try {
			return InformationUnitSelector.fromInt(jsonElement.getAsByte());
		} catch (Exception e) {
			return InformationUnitSelector.fromString(jsonElement.getAsString());
		}
	}
}
