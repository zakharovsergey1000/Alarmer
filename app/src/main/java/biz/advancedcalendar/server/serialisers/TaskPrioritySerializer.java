package biz.advancedcalendar.server.serialisers;

import java.lang.reflect.Type;
import biz.advancedcalendar.wsdl.sync.Enums.TaskPriority;
import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonPrimitive;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;

public class TaskPrioritySerializer implements JsonSerializer<TaskPriority>,
		JsonDeserializer<TaskPriority> {
	@Override
	public JsonElement serialize(TaskPriority setEntityResult, Type type,
			JsonSerializationContext jsc) {
		return new JsonPrimitive(setEntityResult.name());
	}

	@Override
	public TaskPriority deserialize(JsonElement jsonElement, Type type,
			JsonDeserializationContext jdsc) throws JsonParseException {
		try {
			return TaskPriority.fromInt(jsonElement.getAsInt());
		} catch (Exception e) {
			return TaskPriority.fromString(jsonElement.getAsString());
		}
	}
}