package biz.advancedcalendar.server.serialisers;

import java.lang.reflect.Type;
import biz.advancedcalendar.greendao.Reminder.ReminderTimeMode;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class ReminderTimeModeSerializer implements JsonSerializer<ReminderTimeMode>,
		JsonDeserializer<ReminderTimeMode> {
	@Override
	public JsonElement serialize(ReminderTimeMode reminderTimeMode, Type type,
			JsonSerializationContext jsc) {
		return new JsonPrimitive(reminderTimeMode.name());
	}

	@Override
	public ReminderTimeMode deserialize(JsonElement jsonElement, Type type,
			JsonDeserializationContext jdsc) throws JsonParseException {
		try {
			return ReminderTimeMode.fromInt(jsonElement.getAsByte());
		} catch (Exception e) {
			return ReminderTimeMode.fromString(jsonElement.getAsString());
		}
	}
}