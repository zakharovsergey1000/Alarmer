package biz.advancedcalendar.server.serialisers;

import java.lang.reflect.Type;
import biz.advancedcalendar.greendao.Task.RecurrenceInterval;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class RecurrenceIntervalSerializer implements JsonSerializer<RecurrenceInterval>,
		JsonDeserializer<RecurrenceInterval> {
	@Override
	public JsonElement serialize(RecurrenceInterval recurrenceInterval, Type type,
			JsonSerializationContext jsc) {
		return new JsonPrimitive(recurrenceInterval.name());
	}

	@Override
	public RecurrenceInterval deserialize(JsonElement jsonElement, Type type,
			JsonDeserializationContext jdsc) throws JsonParseException {
		try {
			return RecurrenceInterval.fromInt(jsonElement.getAsShort());
		} catch (Exception e) {
			return RecurrenceInterval.fromString(jsonElement.getAsString());
		}
	}
}