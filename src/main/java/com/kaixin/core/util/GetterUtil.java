package com.kaixin.core.util;

import java.text.DateFormat;
import java.util.Date;

public class GetterUtil {

	public static final boolean DEFAULT_BOOLEAN = false;

	public static final boolean[] DEFAULT_BOOLEAN_VALUES = new boolean[0];

	public static final double DEFAULT_DOUBLE = 0.0;

	public static final double[] DEFAULT_DOUBLE_VALUES = new double[0];

	public static final float DEFAULT_FLOAT = 0;

	public static final float[] DEFAULT_FLOAT_VALUES = new float[0];

	public static final int DEFAULT_INTEGER = 0;

	public static final int[] DEFAULT_INTEGER_VALUES = new int[0];

	public static final long DEFAULT_LONG = 0;

	public static final long[] DEFAULT_LONG_VALUES = new long[0];

	public static final short DEFAULT_SHORT = 0;

	public static final short[] DEFAULT_SHORT_VALUES = new short[0];

	public static final String DEFAULT_STRING = "";

	public static String[] BOOLEANS = {"true", "t", "y", "on", "1"};

	public static boolean getBoolean(Object value) {
		return getBoolean(value, DEFAULT_BOOLEAN);
	}

	public static boolean getBoolean(Object value, boolean defaultValue) {
		return get(value, defaultValue);
	}

	public static boolean[] getBooleanValues(Object[] values) {
		return getBooleanValues(values, DEFAULT_BOOLEAN_VALUES);
	}

	public static boolean[] getBooleanValues(Object[] values, boolean[] defaultValue) {

		if (values == null) {
			return defaultValue;
		}

		boolean[] booleanValues = new boolean[values.length];

		for (int i = 0; i < values.length; i++) {
			booleanValues[i] = getBoolean(values[i]);
		}

		return booleanValues;
	}

	public static Date getDate(Object value, DateFormat df) {
		return getDate(value, df, new Date());
	}

	public static Date getDate(Object value, DateFormat df, Date defaultValue) {
		return get(value, df, defaultValue);
	}

	public static double getDouble(Object value) {
		return getDouble(value, DEFAULT_DOUBLE);
	}

	public static double getDouble(Object value, double defaultValue) {
		return get(value, defaultValue);
	}

	public static double[] getDoubleValues(Object[] values) {
		return getDoubleValues(values, DEFAULT_DOUBLE_VALUES);
	}

	public static double[] getDoubleValues(Object[] values, double[] defaultValue) {

		if (values == null) {
			return defaultValue;
		}

		double[] doubleValues = new double[values.length];

		for (int i = 0; i < values.length; i++) {
			doubleValues[i] = getDouble(values[i]);
		}

		return doubleValues;
	}

	public static float getFloat(Object value) {
		return getFloat(value, DEFAULT_FLOAT);
	}

	public static float getFloat(Object value, float defaultValue) {
		return get(value, defaultValue);
	}

	public static float[] getFloatValues(Object[] values) {
		return getFloatValues(values, DEFAULT_FLOAT_VALUES);
	}

	public static float[] getFloatValues(Object[] values, float[] defaultValue) {

		if (values == null) {
			return defaultValue;
		}

		float[] floatValues = new float[values.length];

		for (int i = 0; i < values.length; i++) {
			floatValues[i] = getFloat(values[i]);
		}

		return floatValues;
	}

	public static int getInteger(Object value) {
		return getInteger(value, DEFAULT_INTEGER);
	}

	public static int getInteger(Object value, int defaultValue) {
		return get(value, defaultValue);
	}

	public static int[] getIntegerValues(Object[] values) {
		return getIntegerValues(values, DEFAULT_INTEGER_VALUES);
	}

	public static int[] getIntegerValues(Object[] values, int[] defaultValue) {
		if (values == null) {
			return defaultValue;
		}

		int[] intValues = new int[values.length];

		for (int i = 0; i < values.length; i++) {
			intValues[i] = getInteger(values[i]);
		}

		return intValues;
	}

	public static long getLong(Object value) {
		return getLong(value, DEFAULT_LONG);
	}

	public static long getLong(Object value, long defaultValue) {
		return get(value, defaultValue);
	}

	public static long[] getLongValues(Object[] values) {
		return getLongValues(values, DEFAULT_LONG_VALUES);
	}

	public static long[] getLongValues(Object[] values, long[] defaultValue) {
		if (values == null) {
			return defaultValue;
		}

		long[] longValues = new long[values.length];

		for (int i = 0; i < values.length; i++) {
			longValues[i] = getLong(values[i]);
		}

		return longValues;
	}

	public static short getShort(Object value) {
		return getShort(value, DEFAULT_SHORT);
	}

	public static short getShort(Object value, short defaultValue) {
		return get(value, defaultValue);
	}

	public static short[] getShortValues(String[] values) {
		return getShortValues(values, DEFAULT_SHORT_VALUES);
	}

	public static short[] getShortValues(String[] values, short[] defaultValue) {

		if (values == null) {
			return defaultValue;
		}

		short[] shortValues = new short[values.length];

		for (int i = 0; i < values.length; i++) {
			shortValues[i] = getShort(values[i]);
		}

		return shortValues;
	}

	public static String getString(Object value) {
		return getString(value, DEFAULT_STRING);
	}

	public static String getString(Object value, String defaultValue) {
		return get(value, defaultValue);
	}

	public static boolean get(Object value, boolean defaultValue) {
		if (value != null) {
			try {
				String svalue = value.toString().trim();

				if (svalue.equalsIgnoreCase(BOOLEANS[0]) ||
						svalue.equalsIgnoreCase(BOOLEANS[1]) ||
						svalue.equalsIgnoreCase(BOOLEANS[2]) ||
						svalue.equalsIgnoreCase(BOOLEANS[3]) ||
						svalue.equalsIgnoreCase(BOOLEANS[4])) {

					return true;
				}
				else {
					return false;
				}
			}
			catch (Exception e) {
			}
		}

		return defaultValue;
	}

	public static Date get(Object value, DateFormat df, Date defaultValue) {
		try {
			Date date = df.parse(value.toString().trim());

			if (date != null) {
				return date;
			}
		}
		catch (Exception e) {
		}

		return defaultValue;
	}

	public static double get(Object value, double defaultValue) {
		try {
			return Double.parseDouble(_trim(value.toString()));
		}
		catch (Exception e) {
		}

		return defaultValue;
	}

	public static float get(Object value, float defaultValue) {
		try {
			return Float.parseFloat(_trim(value.toString()));
		}
		catch (Exception e) {
		}

		return defaultValue;
	}

	public static int get(Object value, int defaultValue) {
		try {
			return Integer.parseInt(_trim(value.toString()));
		}
		catch (Exception e) {
		}

		return defaultValue;
	}

	public static long get(Object value, long defaultValue) {
		try {
			return Long.parseLong(_trim(value.toString()));
		}
		catch (Exception e) {
		}

		return defaultValue;
	}

	public static short get(Object value, short defaultValue) {
		try {
			return Short.parseShort(_trim(value.toString()));
		}
		catch (Exception e) {
		}

		return defaultValue;
	}

	public static String get(Object value, String defaultValue) {
		try {
			if (value != null) {
				return value.toString().trim();
			}
		}catch(Exception e) {			
		}

		return defaultValue;
	}
	
	public static Object emptyToNull(Object value) {
		if (value == null)
			return null;
		
		if (value instanceof String && _trim((String)value).length() == 0) {
			return null;
		}
		return value;
	}
	
	public static boolean isEmpty(String value) {
		if (value == null || _trim(value).length() == 0)
			return true;
		else
			return false;
	}
	
	private static String _trim(String value) {
		if (value != null) {
			value = value.trim();
		}
		return value;
	}

}