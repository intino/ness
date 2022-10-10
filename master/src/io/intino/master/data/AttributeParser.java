package io.intino.master.data;

public class AttributeParser {

	public static Boolean asBoolean(String value) {
		try {
			return value == null ? null : Boolean.parseBoolean(value);
		} catch (Exception e) {
			throw new AttributeParseException(e.getMessage());
		}
	}

	public static boolean isBoolean(String value) {
		try {
			asBoolean(value);
			return true;
		} catch (AttributeParseException e) {
			return false;
		}
	}

	public static Integer asInteger(String value) {
		try {
			return value == null ? null : Integer.parseInt(value);
		} catch (Exception e) {
			throw new AttributeParseException(e.getMessage());
		}
	}

	public static boolean isInteger(String value) {
		try {
			asInteger(value);
			return true;
		} catch (AttributeParseException e) {
			return false;
		}
	}

	public static Double asDouble(String value) {
		try {
			return value == null ? null : Double.parseDouble(value);
		} catch (Exception e) {
			throw new AttributeParseException(e.getMessage());
		}
	}

	public static boolean isDouble(String value) {
		try {
			asDouble(value);
			return true;
		} catch (AttributeParseException e) {
			return false;
		}
	}

	public static Float asFloat(String value) {
		try {
			return value == null ? null : Float.parseFloat(value);
		} catch (Exception e) {
			throw new AttributeParseException(e.getMessage());
		}
	}

	public static boolean isFloat(String value) {
		try {
			asFloat(value);
			return true;
		} catch (AttributeParseException e) {
			return false;
		}
	}

	public static Long asLong(String value) {
		try {
			return value == null ? null : Long.parseLong(value);
		} catch (Exception e) {
			throw new AttributeParseException(e.getMessage());
		}
	}

	public static boolean isLong(String value) {
		try {
			asLong(value);
			return true;
		} catch (AttributeParseException e) {
			return false;
		}
	}

	public static class AttributeParseException extends RuntimeException {

		public AttributeParseException(String message) {
			super(message);
		}
	}
}
