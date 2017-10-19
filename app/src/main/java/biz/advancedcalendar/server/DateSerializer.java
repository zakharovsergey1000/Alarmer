package biz.advancedcalendar.server;

import com.google.gson.JsonDeserializationContext;
import com.google.gson.JsonDeserializer;
import com.google.gson.JsonElement;
import com.google.gson.JsonParseException;
import com.google.gson.JsonSerializationContext;
import com.google.gson.JsonSerializer;
import java.lang.reflect.Type;
import java.util.Date;

public class DateSerializer implements JsonSerializer<Date>, JsonDeserializer<Date> {
	@Override
	public JsonElement serialize(Date date, Type type, JsonSerializationContext jsc) {
		// TimeZone tz = TimeZone.getTimeZone("UTC");
		// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
		// sdf.setTimeZone(tz);
		// return new JsonPrimitive(sdf.format(date));
		return null;
		// new JsonPrimitive(new org.joda.time.DateTime(date).toDateTime(
		// org.joda.time.DateTimeZone.UTC).toString(
		// org.joda.time.format.ISODateTimeFormat.dateTime()));
	}

	@Override
	public Date deserialize(JsonElement arg0, Type type, JsonDeserializationContext jdsc)
			throws JsonParseException {
		return null;
		// new org.joda.time.DateTime(arg0.getAsString()).toDate();
	}
}