package it.cahung.research.callcenter.customer;

import java.security.SecureRandom;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import it.cahung.research.callcenter.common.Availability;
import it.cahung.research.callcenter.common.Statistics;
import it.cahung.research.callcenter.common.WeekDays;

public class Customer {
	// Configuration Variables
	private int problemSize = 100;
	private int deadlineSinceCall = 7200; // Default is 5 days
	private int minimumWaitingTime = 10;
	private float dropProbability = 0.5f;
	private int maxWaitingTimeBeforeNewAttempt = 240;
	private Map<WeekDays, List<Availability>> availabilities;

	// State Variables
	private int waitingTimeBeforeNewAttempt = 0;
	private int timeInCall = 0;
	private int retry = 0;
	// Process Variables
	private String id = UUID.randomUUID().toString();
	private SecureRandom random = new SecureRandom();

	public int getProblemSize() {
		return problemSize;
	}

	public void setProblemSize(int problemSize) {
		this.problemSize = problemSize;
	}

	public int getDeadlineSinceCall() {
		return deadlineSinceCall;
	}

	public void setDeadlineSinceCall(int deadlineSinceCall) {
		this.deadlineSinceCall = deadlineSinceCall;
	}

	public int getMinimumWaitingTime() {
		return minimumWaitingTime;
	}

	public void setMinimumWaitingTime(int minimumWaitingTime) {
		this.minimumWaitingTime = minimumWaitingTime;
	}

	public float getDropProbability() {
		return dropProbability;
	}

	public void setDropProbability(float dropProbability) {
		this.dropProbability = dropProbability;
	}

	public int getWaitingTimeBeforeNewAttempt() {
		return waitingTimeBeforeNewAttempt;
	}

	public void setWaitingTimeBeforeNewAttempt(int waitingTimeBeforeNewAttempt) {
		this.waitingTimeBeforeNewAttempt = waitingTimeBeforeNewAttempt;
	}

	public Map<WeekDays, List<Availability>> getAvailabilities() {
		return availabilities;
	}

	public void setAvailabilities(Map<WeekDays, List<Availability>> availabilities) {
		this.availabilities = availabilities;
	}

	public int getTimeInCall() {
		return timeInCall;
	}

	public void setTimeInCall(int timeInCall) {
		this.timeInCall = timeInCall;
	}

	public void resetTimeInCall() {
		this.timeInCall = 0;
	}

	public void incrementTimeInCall() {
		this.timeInCall++;
	}

	public void resetWaitingTimeBeforeNewAttempt() {
		this.waitingTimeBeforeNewAttempt = random.nextInt(maxWaitingTimeBeforeNewAttempt);
	}

	public void incrementWaitingTimeBeforeNewAttempt() {
		this.waitingTimeBeforeNewAttempt++;
	}

	public int getMaxWaitingTimeBeforeNewAttempt() {
		return maxWaitingTimeBeforeNewAttempt;
	}

	public void setMaxWaitingTimeBeforeNewAttempt(int maxWaitingTimeBeforeNewAttempt) {
		this.maxWaitingTimeBeforeNewAttempt = maxWaitingTimeBeforeNewAttempt;
	}

	public void incrementRetry() {
		this.setRetry(this.getRetry() + 1);
	}

	public int getRetry() {
		return retry;
	}

	public void setRetry(int retry) {
		this.retry = retry;
	}

	public String getId() {
		return id;
	}

	public void reduceProblemSize(int ability) {
		problemSize -= ability;
	}

	public boolean isAvailable(WeekDays dayOfWeek, int timeOfDay) {
		List<Availability> availabilitiesRanges = availabilities.get(dayOfWeek);
		if (availabilitiesRanges == null) {
			return false;
		}
		for (Availability currentAvailability : availabilitiesRanges) {
			if (timeOfDay >= currentAvailability.getMin() && timeOfDay <= currentAvailability.getMax()) {
				return true;
			}
		}
		return false;
	}

	public void updateTimeline() {
		deadlineSinceCall--;
		if (maxWaitingTimeBeforeNewAttempt > deadlineSinceCall / 2) {
			maxWaitingTimeBeforeNewAttempt = deadlineSinceCall / 2;
		}
		reduceDropabilityWithRemainingDays();
	}

	private void reduceDropabilityWithRemainingDays() {
		float timeLeft = (float) deadlineSinceCall / 14400f;
		dropProbability *= timeLeft;
	}

	public boolean wantsToRetry() {
		return deadlineSinceCall > 0 && getWaitingTimeBeforeNewAttempt() <= getMaxWaitingTimeBeforeNewAttempt();
	}

	public boolean wantsToCall(int dayOfWeek, int timeInDay) {
		return isAvailable(WeekDays.values()[dayOfWeek], timeInDay);
	}

	public boolean wantsToDrop() {
		return deadlineSinceCall <= 0 || evaluateProbabilityOfDropping();
	}

	private boolean evaluateProbabilityOfDropping() {
		return timeInCall > minimumWaitingTime && dropProbability >= random.nextFloat();
	}

	public boolean wantsToStayInCall(Statistics currentStatistics, boolean showAvgWaitingTime, boolean showStatistics) {
		return currentStatistics == null || checkStatistics(currentStatistics, showAvgWaitingTime, showStatistics);
	}

	private boolean checkStatistics(Statistics currentStatistics, boolean showAvgWaitingTime, boolean showStatistics) {
		return checkAvgWaitingTime(currentStatistics.getLastPeriodAverageWaitingTime(), showAvgWaitingTime)
				&& checkAvailabilities(currentStatistics, showStatistics);
	}

	private boolean checkAvgWaitingTime(int lastPeriodAverageWaitingTime, boolean showAvgWaitingTime) {
		return !showAvgWaitingTime || minimumWaitingTime >= lastPeriodAverageWaitingTime
				|| random.nextFloat() > dropProbability;
	}

	private boolean checkAvailabilities(Statistics currentStatistics, boolean showStatistics) {
		return !showStatistics || checkAvailabilitiesWithStats(currentStatistics)
				|| random.nextFloat() > dropProbability;
	}

	private boolean checkAvailabilitiesWithStats(Statistics currentStatistics) {
		int availableDays = (deadlineSinceCall / Statistics.MINUTES_IN_DAY) - 1;
		if (availableDays <= 0) {
			return true;
		}
		int currentAvg = currentStatistics.getAverageCallsPerHourInDay().get(currentStatistics.getDayOfTheWeek())
				.get(currentStatistics.getTimeInDay() / 60);
		int currentHour = currentStatistics.getTimeInDay() / 60;
		for (int i = 0; i < availableDays; ++i) {
			if (!evaluateNextDay(currentStatistics, currentAvg, currentHour, i)) {
				return false;
			}
		}
		return true;
	}

	private boolean evaluateNextDay(Statistics currentStatistics, int currentAvg, int currentHour, int i) {
		int currentDayIndex = (i + currentStatistics.getDayOfTheWeek()) % 7;
		WeekDays currentDay = WeekDays.values()[currentDayIndex];
		List<Availability> currentDayAvailabilities = availabilities.get(currentDay);
		if (currentDayAvailabilities != null && !currentDayAvailabilities.isEmpty()) {
			List<Integer> todaysAverages = currentStatistics.getAverageCallsPerHourInDay().get(currentDayIndex);
			for (int k = 0; k < 24; ++k) {
				if ((currentDayIndex != currentStatistics.getDayOfTheWeek() || k > currentHour)
						&& todaysAverages.get(k) < currentAvg
						&& checkOverlapWithAvailabilities(currentDayAvailabilities, k)) {
					return false;
				}
			}
		}
		return true;
	}

	private boolean checkOverlapWithAvailabilities(List<Availability> currentDayAvailabilities, int k) {
		int kMin = k * 60;
		int kMax = kMin + 60;
		for (Availability availability : currentDayAvailabilities) {
			if ((availability.getMin() > kMin && availability.getMin() < kMax)
					|| (availability.getMax() > kMin && availability.getMax() < kMax)
					|| (availability.getMin() <= kMin && availability.getMax() >= kMax)) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Customer && ((Customer) obj).getId().equals(this.id);
	}

	@Override
	public int hashCode() {
		return this.id.hashCode();
	}
}
