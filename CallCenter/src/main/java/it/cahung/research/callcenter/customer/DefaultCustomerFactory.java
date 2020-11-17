package it.cahung.research.callcenter.customer;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import it.cahung.research.callcenter.common.WorkerProfile;
import it.cahung.research.callcenter.common.Availability;
import it.cahung.research.callcenter.common.AvailabilityProfile;
import it.cahung.research.callcenter.common.Configuration;
import it.cahung.research.callcenter.common.Statistics;
import it.cahung.research.callcenter.common.WeekDays;

public class DefaultCustomerFactory implements CustomerFactory {
	private static final float JUMPDAY_PROBABILITY = 0.4f;
	private static final float RANDOMDAY_PROBABILITY = 0.5f;
	private static final int DEFAULT_PROBLEM_SIZE = 500;
	private final SecureRandom random = new SecureRandom();
	private Configuration configuration;

	public List<Customer> generateRandomCustomers(int max) {
		int total = random.nextInt(max);
		List<Customer> list = new ArrayList<>();
		for (int i = 0; i < total; ++i) {
			Customer customer = new Customer();
			customer.setAvailabilities(generateRandomAvailability());
			customer.setDeadlineSinceCall(random.nextInt(Statistics.MINUTES_IN_DAY * 30) + 120);
			customer.setDropProbability(random.nextFloat() / 2);
			customer.setMaxWaitingTimeBeforeNewAttempt(random.nextInt(20));
			customer.setMinimumWaitingTime(random.nextInt(60));
			if (configuration != null) {
				customer.setProblemSize(random.nextInt(configuration.getMaxProblemSize()));
			} else {
				customer.setProblemSize(random.nextInt(DEFAULT_PROBLEM_SIZE));
			}
			list.add(customer);
		}
		return list;
	}

	@Override
	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	@Override
	public Configuration getConfiguration() {
		return configuration;
	}

	public Map<WeekDays, List<Availability>> generateRandomAvailability() {
		AvailabilityProfile availabilityProfile = AvailabilityProfile.values()[random
				.nextInt(AvailabilityProfile.values().length)];
		WorkerProfile workerProfile = WorkerProfile.values()[random.nextInt(WorkerProfile.values().length)];
		return generateAvailability(availabilityProfile, workerProfile, random.nextInt(120), random.nextFloat(),
				random.nextInt(3));
	}

	public Map<WeekDays, List<Availability>> generateAvailability(AvailabilityProfile profile,
			WorkerProfile workerProfile, int variability, float workingDaysStability, int shiftRange) {
		HashMap<WeekDays, List<Availability>> availabilityMap = new HashMap<>();
		int jumps = 0;
		for (int i = 0; i < 7; ++i) {
			switch (workerProfile) {
			case MONDAY_FRIDAY:
				if (i < 5 && random.nextFloat() < workingDaysStability) {
					availabilityMap.put(WeekDays.values()[i],
							generateAvailabilityRanges(profile, variability, shiftRange));
				}
				break;
			case SHIFTER_5DAYS:
				if (availabilityMap.size() < 5 && (jumps > 1 || random.nextFloat() < JUMPDAY_PROBABILITY)) {
					availabilityMap.put(WeekDays.values()[i],
							generateAvailabilityRanges(profile, variability, shiftRange));
				} else {
					jumps++;
				}
				break;
			case SHORTWEEK_3DAYS:
				if (availabilityMap.size() < 3 && (jumps > 3 || random.nextFloat() < JUMPDAY_PROBABILITY)) {
					availabilityMap.put(WeekDays.values()[i],
							generateAvailabilityRanges(profile, variability, shiftRange));
				} else {
					jumps++;
				}
				break;
			case RANDOMDAYS:
				if (availabilityMap.size() == 0 || random.nextFloat() < RANDOMDAY_PROBABILITY) {
					availabilityMap.put(WeekDays.values()[i],
							generateAvailabilityRanges(profile, variability, shiftRange));
				}
			}
		}
		return availabilityMap;
	}

	private List<Availability> generateAvailabilityRanges(AvailabilityProfile profile, int variability,
			int shiftRange) {
		List<Availability> currentAvailabilities = new ArrayList<>();
		int currentShift = (random.nextBoolean() ? 1 : -1) * (shiftRange > 0 ? random.nextInt(shiftRange) : 0);
		int currentMinVariability = variability > 0 ? random.nextInt(variability) : 0;
		int currentMaxVariability = variability > 0 ? random.nextInt(variability) : 0;
		switch (profile) {
		case DAILY_PERMANENT:
			addThreePeriodsAvailabilities(variability, currentAvailabilities, currentShift, currentMinVariability,
					currentMaxVariability, 7, 9, 13, 14, 18, 24);
			break;
		case DAILY_PARTTIME:
			int shift = random.nextInt(3);
			addPartimeShift(shift, variability, currentShift, currentAvailabilities, currentMinVariability,
					currentMaxVariability);
			break;
		case DAILY_FREELANCE:
			addThreePeriodsAvailabilities(variability, currentAvailabilities, currentShift, currentMinVariability,
					currentMaxVariability, 0, 10, 13, 14, 16, 24);
			break;
		case FREE_DAY:
			addSinglePeriodAvailable(8, 23, currentAvailabilities, currentShift, currentMinVariability,
					currentMaxVariability);
			break;
		case FREE_NIGHT:
			Availability wholeNight = new Availability();
			Availability afterDailySleep = new Availability();
			wholeNight.setMin(0 + currentShift - currentMinVariability);
			wholeNight.setMax(9 * 60 + currentShift - currentMaxVariability);
			afterDailySleep.setMin(17 * 60 + currentShift - currentMinVariability);
			afterDailySleep.setMax(24 * 60 + currentShift - currentMaxVariability);
			currentAvailabilities.add(wholeNight);
			currentAvailabilities.add(afterDailySleep);
			break;
		case NIGHTSHIFTER_PERMANENT:
			Availability nightShift = new Availability();
			Availability mealBreak = new Availability();
			nightShift.setMin(4 * 60 + currentShift - currentMinVariability);
			nightShift.setMax(19 * 60 + currentShift - currentMaxVariability);
			mealBreak.setMin(23 * 60 + currentShift - currentMinVariability);
			mealBreak.setMax(24 * 60 + currentShift - currentMaxVariability);
			currentAvailabilities.add(nightShift);
			currentAvailabilities.add(mealBreak);
			break;
		case NIGHTSHIFTER_PARTTIME:
			addSinglePeriodAvailable(4, 23, currentAvailabilities, currentShift, currentMinVariability,
					currentMaxVariability);
			break;
		}
		return currentAvailabilities;
	}

	private void addSinglePeriodAvailable(int start, int end, List<Availability> currentAvailabilities,
			int currentShift, int currentMinVariability, int currentMaxVariability) {
		Availability singlePeriod = new Availability();
		singlePeriod.setMin(start * 60 + currentShift - currentMinVariability);
		singlePeriod.setMax(end * 60 + currentShift - currentMaxVariability);
		currentAvailabilities.add(singlePeriod);
	}

	private void addPartimeShift(int shift, int variability, int currentShift, List<Availability> currentAvailabilities,
			int currentMinVariability, int currentMaxVariability) {
		Availability beforeWork = new Availability();
		Availability afterWork = new Availability();
		switch (shift) {
		case 0:
			beforeWork.setMin(0 * 60 + currentShift - currentMinVariability);
			beforeWork.setMax(9 * 60 + currentShift - currentMaxVariability);
			afterWork.setMin(14 * 60 + currentShift - currentMinVariability);
			afterWork.setMax(24 * 60 + currentShift - currentMaxVariability);
			break;
		case 1:
			beforeWork.setMin(0 * 60 + currentShift - currentMinVariability);
			beforeWork.setMax(13 * 60 + currentShift - currentMaxVariability);
			afterWork.setMin(18 * 60 + currentShift - currentMinVariability);
			afterWork.setMax(24 * 60 + currentShift - currentMaxVariability);
			break;
		default:
			beforeWork.setMin(0 * 60 + currentShift - currentMinVariability);
			beforeWork.setMax(17 * 60 + currentShift - currentMaxVariability);
			afterWork.setMin(22 * 60 + currentShift - currentMinVariability);
			afterWork.setMax(24 * 60 + currentShift - currentMaxVariability);
		}
		currentAvailabilities.add(beforeWork);
		currentAvailabilities.add(afterWork);
	}

	private void addThreePeriodsAvailabilities(int variability, List<Availability> currentAvailabilities,
			int currentShift, int currentMinVariability, int currentMaxVariability, int startBefore, int endBefore,
			int startLunch, int endLunch, int startAfter, int endAfter) {
		Availability beforeWork = new Availability();
		beforeWork.setMin(startBefore * 60 + currentShift - currentMinVariability);
		beforeWork.setMax(endBefore * 60 + currentShift - currentMaxVariability);
		Availability lunchBreak = new Availability();
		currentMinVariability = variability > 0 ? random.nextInt(variability) : 0;
		currentMaxVariability = variability > 0 ? random.nextInt(variability) : 0;
		lunchBreak.setMin(startLunch * 60 + currentShift - currentMinVariability);
		lunchBreak.setMax(endLunch * 60 + currentShift - currentMaxVariability);
		Availability afterWork = new Availability();
		currentMinVariability = variability > 0 ? random.nextInt(variability) : 0;
		currentMaxVariability = variability > 0 ? random.nextInt(variability) : 0;
		afterWork.setMin(startAfter * 60 + currentShift - currentMinVariability);
		afterWork.setMax(endAfter * 60 + currentShift - currentMaxVariability);
		currentAvailabilities.add(beforeWork);
		currentAvailabilities.add(lunchBreak);
		currentAvailabilities.add(afterWork);
	}
}
