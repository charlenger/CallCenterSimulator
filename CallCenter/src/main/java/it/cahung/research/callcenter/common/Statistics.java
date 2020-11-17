package it.cahung.research.callcenter.common;

import java.util.ArrayList;
import java.util.List;

public class Statistics {
	public static final int MINUTES_IN_DAY = 1440;
	public static final int HOURS_IN_DAY = 24;
	public static final int DAYS_IN_WEEK = 7;
	private int timeInDay = 0; // in minutes
	private int dayOfTheWeek = 0; // 0: SUNDAY, 6: SATURDAY
	private int countOfPassedDays = 1;

	// Stats for Operations
	private int currentDayCalls = 0;
	private int doneCallsInDay = 0;
	private int currentAverageCallsPerOperator = 1;
	private int lastWeekAverageCallsPerDay = 0;
	private List<Long> totalCallsInDay = new ArrayList<>(DAYS_IN_WEEK); // the index corresponds to the day
	private List<Integer> averageCallsInDay = new ArrayList<>(DAYS_IN_WEEK); // the index corresponds to the day
	private List<Integer> lastWeekTotalCallsInDay = new ArrayList<>(DAYS_IN_WEEK); // the index corresponds to the day

	// Stats for Customer
	private List<Integer> todaysIncrementalCallsPerHour = new ArrayList<>(HOURS_IN_DAY);
	private List<List<Long>> totalCallsPerHourInDay = new ArrayList<>(DAYS_IN_WEEK);
	private List<List<Integer>> averageCallsPerHourInDay = new ArrayList<>(DAYS_IN_WEEK);
	private long totalCustomersMovingInProgress = 0;
	private long totalWaitingTime = 0;

	public Statistics() {
		for (int i = 0; i < HOURS_IN_DAY; ++i) {
			getTodaysIncrementalCallsPerHour().add(0);
		}
		for (int i = 0; i < DAYS_IN_WEEK; ++i) {
			totalCallsInDay.add(0L);
			averageCallsInDay.add(0);
			lastWeekTotalCallsInDay.add(0);
			List<Long> list1 = new ArrayList<>();
			for (int j = 0; j < HOURS_IN_DAY; ++j) {
				list1.add(0L);
			}
			List<Integer> list2 = new ArrayList<>();
			for (int j = 0; j < HOURS_IN_DAY; ++j) {
				list2.add(0);
			}
			getTotalCallsPerHourInDay().add(list1);
			averageCallsPerHourInDay.add(list2);
		}
	}

	public int getDayOfTheWeek() {
		return dayOfTheWeek;
	}

	public void setDayOfTheWeek(int dayOfTheWeek) {
		this.dayOfTheWeek = dayOfTheWeek;
	}

	public int getCountOfPassedDays() {
		return countOfPassedDays;
	}

	public void setCountOfPassedDays(int countOfPassedDays) {
		this.countOfPassedDays = countOfPassedDays > 0 ? countOfPassedDays : 1;
	}

	public List<Long> getTotalCallsInDay() {
		return totalCallsInDay;
	}

	public void setTotalCallsInDay(List<Long> totalCallsInDay) {
		this.totalCallsInDay = totalCallsInDay;
	}

	public List<Integer> getAverageCallsInDay() {
		return averageCallsInDay;
	}

	public void setAverageCallsInDay(List<Integer> averageCallsInDay) {
		this.averageCallsInDay = averageCallsInDay;
	}

	public int getCurrentDayCalls() {
		return currentDayCalls;
	}

	public void setCurrentDayCalls(int currentDayCalls) {
		this.currentDayCalls = currentDayCalls;
	}

	public int getDoneCallsInDay() {
		return doneCallsInDay;
	}

	public void setDoneCallsInDay(int doneCallsInDay) {
		this.doneCallsInDay = doneCallsInDay;
	}

	public int getCurrentAverageCallsPerOperator() {
		return currentAverageCallsPerOperator;
	}

	public void setCurrentAverageCallsPerOperator(int currentAverageCallsPerOperator) {
		this.currentAverageCallsPerOperator = currentAverageCallsPerOperator;
	}

	public int getLastWeekAverageCallsPerDay() {
		return lastWeekAverageCallsPerDay;
	}

	public List<Integer> getLastWeekTotalCallsInDay() {
		return lastWeekTotalCallsInDay;
	}

	public void updateDayOfTheWeek() {
		dayOfTheWeek = dayOfTheWeek == 6 ? 0 : dayOfTheWeek + 1;
	}

	public void updateAverageCallsPerOperator(int todaysAverage) {
		currentAverageCallsPerOperator = currentAverageCallsPerOperator == 1 ? todaysAverage
				: ((currentAverageCallsPerOperator + todaysAverage) / 2);
	}

	public void computeAverages(int totalOperators) {
		computeAveragesPerCallsOfDay();
		computeAverageCallsPerOperator(totalOperators);
		computeLastWeekAverageCallsPerDay();
	}

	private void computeLastWeekAverageCallsPerDay() {
		int totalOfLastWeek = lastWeekTotalCallsInDay.stream().mapToInt(Integer::intValue).sum();
		lastWeekAverageCallsPerDay = totalOfLastWeek / DAYS_IN_WEEK;
	}

	private void computeAverageCallsPerOperator(int totalOperators) {
		if (totalOperators > 0) {
			int todaysAverage = doneCallsInDay / totalOperators;
			updateAverageCallsPerOperator(todaysAverage);
		}
	}

	private void computeAveragesPerCallsOfDay() {
		Long currentTotalForDay = totalCallsInDay.get(dayOfTheWeek);
		if (currentTotalForDay == null) {
			currentTotalForDay = 0L;
		}
		currentTotalForDay += currentDayCalls;
		totalCallsInDay.set(dayOfTheWeek, currentTotalForDay);
		int spare = (countOfPassedDays % DAYS_IN_WEEK) > dayOfTheWeek ? 1 : 0;
		averageCallsInDay.set(dayOfTheWeek,
				Integer.valueOf((int) (currentTotalForDay / ((countOfPassedDays / DAYS_IN_WEEK) + spare))));
	}

	public void increaseTimeInDay() {
		timeInDay++;
		if (timeInDay % 60 == 0) {
			updateHourlyStats();
		}
	}

	public void resetTimeInDay() {
		timeInDay = 0;
	}

	public int getTimeInDay() {
		return timeInDay;
	}

	private void updateHourlyStats() {
		int currentHourIndex = (timeInDay / 60) - 1;
		getTodaysIncrementalCallsPerHour().set(currentHourIndex, currentDayCalls);
		int previousCalls = currentHourIndex == 0 ? 0 : getTodaysIncrementalCallsPerHour().get(currentHourIndex - 1);
		int callsForCurrentHour = currentDayCalls - previousCalls;
		Long currentTotal = getTotalCallsPerHourInDay().get(dayOfTheWeek).get(currentHourIndex);
		if (currentTotal == null) {
			currentTotal = 0L;
		}
		currentTotal += callsForCurrentHour;
		getTotalCallsPerHourInDay().get(dayOfTheWeek).set(currentHourIndex, currentTotal);
		int spare = (countOfPassedDays % DAYS_IN_WEEK) > dayOfTheWeek ? 1 : 0;
		averageCallsPerHourInDay.get(dayOfTheWeek).set(currentHourIndex,
				(int) (currentTotal / ((countOfPassedDays / DAYS_IN_WEEK) + spare)));
	}

	public List<List<Integer>> getAverageCallsPerHourInDay() {
		return averageCallsPerHourInDay;
	}

	public void setAverageCallsPerHourInDay(List<List<Integer>> averageCallsPerHourInDay) {
		this.averageCallsPerHourInDay = averageCallsPerHourInDay;
	}

	public int getLastPeriodAverageWaitingTime() {
		if (totalCustomersMovingInProgress == 0) {
			totalCustomersMovingInProgress = 1;
		}
		return (int) (totalWaitingTime / totalCustomersMovingInProgress);
	}

	public List<Integer> getTodaysIncrementalCallsPerHour() {
		return todaysIncrementalCallsPerHour;
	}

	public List<List<Long>> getTotalCallsPerHourInDay() {
		return totalCallsPerHourInDay;
	}

	public long getTotalCustomersMovingInProgress() {
		return totalCustomersMovingInProgress;
	}

	public void setTotalCustomersMovingInProgress(long totalCustomersMovingInProgress) {
		this.totalCustomersMovingInProgress = totalCustomersMovingInProgress;
	}

	public long getTotalWaitingTime() {
		return totalWaitingTime;
	}

	public void setTotalWaitingTime(long totalWaitingTime) {
		this.totalWaitingTime = totalWaitingTime;
	}

	public void increaseTotalWaitingTime(int timeInCall) {
		this.totalWaitingTime += timeInCall;
	}

	public void increaseTotalCustomersMovingInProgress() {
		this.totalCustomersMovingInProgress++;
	}

	public void setTimeInDay(int timeInDay) {
		this.timeInDay = timeInDay;
	}
}
