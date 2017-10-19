package biz.advancedcalendar.fragments;

public class ValueBounds<T> {
	private T defaultValue;
	private T minValue;
	private T maxValue;

	public ValueBounds(T defaultValue, T minValue, T maxValue) {
		this.defaultValue = defaultValue;
		this.minValue = minValue;
		this.maxValue = maxValue;
	}

	public T getDefaultValue() {
		return defaultValue;
	}

	public T getMinValue() {
		return minValue;
	}

	public T getMaxValue() {
		return maxValue;
	}
}
