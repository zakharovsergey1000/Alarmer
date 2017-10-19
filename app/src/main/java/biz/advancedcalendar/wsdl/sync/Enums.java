package biz.advancedcalendar.wsdl.sync;

import java.util.HashMap;
import java.util.Map;

public class Enums {
	public enum GetEntityResult {
		SUCCESS(0), ACCESS_DENIED(1), ENTITY_IS_NOT_FOUND(2), ENTITY_IS_DELETED(3);
		private int code;

		GetEntityResult(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}

		public static GetEntityResult fromString(String str) {
			if (str.equals("SUCCESS")) {
				return SUCCESS;
			}
			if (str.equals("ACCESS_DENIED")) {
				return ACCESS_DENIED;
			}
			if (str.equals("ENTITY_IS_NOT_FOUND")) {
				return ENTITY_IS_NOT_FOUND;
			}
			if (str.equals("ENTITY_IS_DELETED")) {
				return ENTITY_IS_DELETED;
			}
			return null;
		}

		public static GetEntityResult fromInt(int x) {
			switch (x) {
			case 0:
				return SUCCESS;
			case 1:
				return ACCESS_DENIED;
			case 2:
				return ENTITY_IS_NOT_FOUND;
			case 3:
				return ENTITY_IS_DELETED;
			default:
				return null;
			}
		}
	}

	public enum TaskPriority {
		NORMAL(0), LOW(1), HIGH(2);
		private int code;

		TaskPriority(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}

		public static TaskPriority fromString(String str) {
			if (str.equals("NORMAL")) {
				return NORMAL;
			}
			if (str.equals("LOW")) {
				return LOW;
			}
			if (str.equals("HIGH")) {
				return HIGH;
			}
			return null;
		}

		public static TaskPriority fromInt(int x) {
			switch (x) {
			case 0:
				return NORMAL;
			case 1:
				return LOW;
			case 2:
				return HIGH;
			}
			return null;
		}
	}

	public enum EntityType {
		TASK((byte) 0), LABEL((byte) 1), CONTACT((byte) 2), FILE((byte) 3), DIARY_RECORD(
				(byte) 4), MESSAGE((byte) 5), REMINDER((byte) 6);
		private byte value;

		EntityType(byte code) {
			value = code;
		}

		public byte getCode() {
			return value;
		}

		private static Map<Byte, EntityType> map = new HashMap<Byte, EntityType>();
		private static Map<String, EntityType> stringToObjectMap = new HashMap<String, EntityType>();
		static {
			for (EntityType recurrenceInterval : EntityType.values()) {
				EntityType.map.put(recurrenceInterval.value, recurrenceInterval);
				EntityType.stringToObjectMap.put(recurrenceInterval.name(),
						recurrenceInterval);
			}
		}

		public static EntityType fromInt(byte value) {
			EntityType recurrenceInterval = EntityType.map.get(value);
			return recurrenceInterval;
		}

		public static EntityType fromString(String str) {
			EntityType recurrenceInterval = EntityType.stringToObjectMap.get(str);
			return recurrenceInterval;
		}
	}

	public enum SetEntityResult {
		SAVED(0), ENTITY_ON_THE_SERVER_IS_NEWER(1), ENTITY_HAS_INCORRECT_DATA(2), ENTITY_IS_NOT_FOUND(
				3), ENTITY_IS_DELETED(4), PARENT_ENTITY_IS_NOT_FOUND(5), PARENT_ENTITY_IS_DELETED(
				6), ACCESS_DENIED(7);
		private int value;

		SetEntityResult(int code) {
			value = code;
		}

		public int getCode() {
			return value;
		}

		public static SetEntityResult fromString(String str) {
			if (str.equals("SAVED")) {
				return SAVED;
			}
			if (str.equals("ENTITY_ON_THE_SERVER_IS_NEWER")) {
				return ENTITY_ON_THE_SERVER_IS_NEWER;
			}
			if (str.equals("ENTITY_HAS_INCORRECT_DATA")) {
				return ENTITY_HAS_INCORRECT_DATA;
			}
			if (str.equals("ENTITY_IS_NOT_FOUND")) {
				return ENTITY_IS_NOT_FOUND;
			}
			if (str.equals("ENTITY_IS_DELETED")) {
				return ENTITY_IS_DELETED;
			}
			if (str.equals("PARENT_ENTITY_IS_NOT_FOUND")) {
				return PARENT_ENTITY_IS_NOT_FOUND;
			}
			if (str.equals("PARENT_ENTITY_IS_DELETED")) {
				return PARENT_ENTITY_IS_DELETED;
			}
			if (str.equals("ACCESS_DENIED")) {
				return ACCESS_DENIED;
			}
			return null;
		}

		public static SetEntityResult fromInt(int x) {
			switch (x) {
			case 0:
				return SAVED;
			case 1:
				return ENTITY_ON_THE_SERVER_IS_NEWER;
			case 2:
				return ENTITY_HAS_INCORRECT_DATA;
			case 3:
				return ENTITY_IS_NOT_FOUND;
			case 4:
				return ENTITY_IS_DELETED;
			case 5:
				return PARENT_ENTITY_IS_NOT_FOUND;
			case 6:
				return PARENT_ENTITY_IS_DELETED;
			case 7:
				return ACCESS_DENIED;
			}
			return null;
		}
	}

	public enum SetEntityListResult {
		SUCCESS(0), ACCESS_DENIED(1);
		private int code;

		SetEntityListResult(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}

		public static SetEntityListResult fromString(String str) {
			if (str.equals("SUCCESS")) {
				return SUCCESS;
			}
			if (str.equals("ACCESS_DENIED")) {
				return ACCESS_DENIED;
			}
			return null;
		}

		public static SetEntityListResult fromInt(int x) {
			switch (x) {
			case 0:
				return SUCCESS;
			case 1:
				return ACCESS_DENIED;
			default:
				return null;
			}
		}
	}

	public enum AuthenticateResult {
		SUCCESS(0), USERNAME_AND_PASSWORD_DO_NOT_MATCH(1), USERNAME_DOES_NOT_EXIST(2), GOOGLE_SIGN_IN_ERROR_UNKNOWN(
				3);
		private int code;

		AuthenticateResult(int code) {
			this.code = code;
		}

		public int getCode() {
			return code;
		}

		public static AuthenticateResult fromString(String str) {
			if (str.equals("SUCCESS")) {
				return SUCCESS;
			}
			if (str.equals("USERNAME_AND_PASSWORD_DO_NOT_MATCH")) {
				return USERNAME_AND_PASSWORD_DO_NOT_MATCH;
			}
			if (str.equals("USERNAME_DOES_NOT_EXIST")) {
				return USERNAME_DOES_NOT_EXIST;
			}
			if (str.equals("GOOGLE_SIGN_IN_ERROR_UNKNOWN")) {
				return GOOGLE_SIGN_IN_ERROR_UNKNOWN;
			}
			return null;
		}

		public static AuthenticateResult fromInt(int x) {
			switch (x) {
			case 0:
				return SUCCESS;
			case 1:
				return USERNAME_AND_PASSWORD_DO_NOT_MATCH;
			case 2:
				return USERNAME_DOES_NOT_EXIST;
			case 3:
				return GOOGLE_SIGN_IN_ERROR_UNKNOWN;
			default:
				return null;
			}
		}
	}

	public enum CreateUserResult {
		SUCCESS((byte) 0), THE_USER_WITH_REQUESTED_USERNAME_ALREADY_EXISTS((byte) 1), ERROR_UNKNOWN(
				(byte) 2);
		private byte value;

		CreateUserResult(byte value) {
			this.value = value;
		}

		public byte getCode() {
			return value;
		}

		private static Map<Byte, CreateUserResult> map = new HashMap<Byte, CreateUserResult>();
		private static Map<String, CreateUserResult> stringToObjectMap = new HashMap<String, CreateUserResult>();
		static {
			for (CreateUserResult recurrenceInterval : CreateUserResult.values()) {
				CreateUserResult.map.put(recurrenceInterval.value, recurrenceInterval);
				CreateUserResult.stringToObjectMap.put(recurrenceInterval.name(),
						recurrenceInterval);
			}
		}

		public static CreateUserResult fromInt(byte value) {
			CreateUserResult recurrenceInterval = CreateUserResult.map.get(value);
			return recurrenceInterval;
		}

		public static CreateUserResult fromString(String str) {
			CreateUserResult recurrenceInterval = CreateUserResult.stringToObjectMap
					.get(str);
			return recurrenceInterval;
		}
	}

	public enum GetEntityListResult {
		SUCCESS((byte) 0), ACCESS_DENIED((byte) 1), NOT_ALL_ENTITIES_ARE_FOUND((byte) 2);
		private byte value;

		GetEntityListResult(byte code) {
			value = code;
		}

		public byte getCode() {
			return value;
		}

		private static Map<Byte, GetEntityListResult> map = new HashMap<Byte, GetEntityListResult>();
		private static Map<String, GetEntityListResult> stringToObjectMap = new HashMap<String, GetEntityListResult>();
		static {
			for (GetEntityListResult recurrenceInterval : GetEntityListResult.values()) {
				GetEntityListResult.map.put(recurrenceInterval.value, recurrenceInterval);
				GetEntityListResult.stringToObjectMap.put(recurrenceInterval.name(),
						recurrenceInterval);
			}
		}

		public static GetEntityListResult fromInt(byte value) {
			GetEntityListResult recurrenceInterval = GetEntityListResult.map.get(value);
			return recurrenceInterval;
		}

		public static GetEntityListResult fromString(String str) {
			GetEntityListResult recurrenceInterval = GetEntityListResult.stringToObjectMap
					.get(str);
			return recurrenceInterval;
		}
	}
}