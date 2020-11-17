package it.cahung.research.callcenter.common;

public class Availability {
	private static final int MINUTES_IN_DAY = 1440;
	private int min = 0;
	private int max = MINUTES_IN_DAY;

	public int getMin() {
		return min;
	}

	public void setMin(int min) {
		this.min = min >= max ? max - 1 : (min < 0 ? 0 : min);
	}

	public int getMax() {
		return max;
	}

	public void setMax(int max) {
		this.max = max <= min ? min + 1 : (max > MINUTES_IN_DAY ? MINUTES_IN_DAY : max);
	}
}
