package it.cahung.research.callcenter.customer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.util.List;
import java.util.Map;

import org.junit.Test;

import it.cahung.research.callcenter.common.Availability;
import it.cahung.research.callcenter.common.AvailabilityProfile;
import it.cahung.research.callcenter.common.WeekDays;
import it.cahung.research.callcenter.common.WorkerProfile;

public class DefaultCustomerFactoryTest {
	DefaultCustomerFactory factory = new DefaultCustomerFactory();

	@Test
	public void test() {
		List<Customer> generateRandomCustomers = factory.generateRandomCustomers(100);
		assertTrue(generateRandomCustomers.size() >= 0);
	}

	@Test
	public void generateAvailabilities() {
		int variability = 0;
		int shiftRange = 0;
		float workingDaysStability = 1.0f;
		testAvailabilityProfiles(variability, shiftRange, workingDaysStability, AvailabilityProfile.DAILY_PERMANENT);
		testAvailabilityProfiles(variability, shiftRange, workingDaysStability, AvailabilityProfile.DAILY_PARTTIME);
		testAvailabilityProfiles(variability, shiftRange, workingDaysStability, AvailabilityProfile.DAILY_FREELANCE);
		testAvailabilityProfiles(variability, shiftRange, workingDaysStability, AvailabilityProfile.FREE_DAY);
		testAvailabilityProfiles(variability, shiftRange, workingDaysStability, AvailabilityProfile.FREE_NIGHT);
		testAvailabilityProfiles(variability, shiftRange, workingDaysStability,
				AvailabilityProfile.NIGHTSHIFTER_PARTTIME);
		testAvailabilityProfiles(variability, shiftRange, workingDaysStability,
				AvailabilityProfile.NIGHTSHIFTER_PERMANENT);
	}

	private void testAvailabilityProfiles(int variability, int shiftRange, float workingDaysStability,
			AvailabilityProfile availabilityProfile) {
		Map<WeekDays, List<Availability>> officeWeek = factory.generateAvailability(availabilityProfile,
				WorkerProfile.MONDAY_FRIDAY, variability, workingDaysStability, shiftRange);
		for (WeekDays weekday : WeekDays.values()) {
			if (weekday == WeekDays.SATURDAY || weekday == WeekDays.SUNDAY) {
				assertNull(officeWeek.get(weekday));
			} else {
				List<Availability> currentList = officeWeek.get(weekday);
				testCurrentDayList(currentList, availabilityProfile);
			}

		}

		Map<WeekDays, List<Availability>> officeWeekShifter = factory.generateAvailability(availabilityProfile,
				WorkerProfile.SHIFTER_5DAYS, variability, workingDaysStability, shiftRange);
		int count = 0;
		for (WeekDays weekday : WeekDays.values()) {
			List<Availability> currentList = officeWeekShifter.get(weekday);
			if (currentList != null) {
				count++;
				testCurrentDayList(currentList, availabilityProfile);
			}
		}
		assertEquals(5, count);

		Map<WeekDays, List<Availability>> officeWeekShortWeeker = factory.generateAvailability(availabilityProfile,
				WorkerProfile.SHORTWEEK_3DAYS, variability, workingDaysStability, shiftRange);
		count = 0;
		for (WeekDays weekday : WeekDays.values()) {
			List<Availability> currentList = officeWeekShortWeeker.get(weekday);
			if (currentList != null) {
				count++;
				testCurrentDayList(currentList, availabilityProfile);
			}
		}
		assertEquals(3, count);

		Map<WeekDays, List<Availability>> officeWeekRandomer = factory.generateAvailability(availabilityProfile,
				WorkerProfile.RANDOMDAYS, variability, workingDaysStability, shiftRange);
		count = 0;
		for (WeekDays weekday : WeekDays.values()) {
			List<Availability> currentList = officeWeekRandomer.get(weekday);
			if (currentList != null) {
				count++;
				testCurrentDayList(currentList, availabilityProfile);
			}
		}
		assertTrue(count > 0);
	}

	private void testCurrentDayList(List<Availability> currentList, AvailabilityProfile profile) {
		switch (profile) {
		case DAILY_PERMANENT:
			assertTrue(currentList.get(0).getMin() >= 0);
			assertTrue(currentList.get(0).getMax() <= 9 * 60);
			assertTrue(currentList.get(1).getMin() >= 13 * 60);
			assertTrue(currentList.get(1).getMax() <= 14 * 60);
			assertTrue(currentList.get(2).getMin() >= 18 * 60);
			assertTrue(currentList.get(2).getMax() <= 24 * 60);
			break;
		case DAILY_PARTTIME:
			boolean morningBefore = currentList.get(0).getMin() >= 0 && currentList.get(0).getMax() <= 9 * 60;
			boolean morningAfter = currentList.get(1).getMin() >= 14 * 60 && currentList.get(1).getMax() <= 24 * 60;
			boolean afternoonBefore = currentList.get(0).getMin() >= 0 && currentList.get(0).getMax() <= 13 * 60;
			boolean afternoonAfter = currentList.get(1).getMin() >= 18 * 60 && currentList.get(1).getMax() <= 24 * 60;
			boolean eveningBefore = currentList.get(0).getMin() >= 0 && currentList.get(0).getMax() <= 17 * 60;
			boolean eveningAfter = currentList.get(1).getMin() >= 22 * 60 && currentList.get(1).getMax() <= 24 * 60;
			assertTrue((morningBefore && morningAfter) || (afternoonBefore && afternoonAfter)
					|| (eveningBefore && eveningAfter));
			break;
		case DAILY_FREELANCE:
			assertTrue(currentList.get(0).getMin() >= 0);
			assertTrue(currentList.get(0).getMax() <= 10 * 60);
			assertTrue(currentList.get(1).getMin() >= 13 * 60);
			assertTrue(currentList.get(1).getMax() <= 14 * 60);
			assertTrue(currentList.get(2).getMin() >= 16 * 60);
			assertTrue(currentList.get(2).getMax() <= 24 * 60);
			break;
		case FREE_DAY:
			assertTrue(currentList.get(0).getMin() >= 8 * 60);
			assertTrue(currentList.get(0).getMax() <= 23 * 60);
			break;
		case FREE_NIGHT:
			assertTrue(currentList.get(0).getMin() >= 0);
			assertTrue(currentList.get(0).getMax() <= 9 * 60);
			assertTrue(currentList.get(1).getMin() >= 17 * 60);
			assertTrue(currentList.get(1).getMax() <= 24 * 60);
			break;
		case NIGHTSHIFTER_PERMANENT:
			assertTrue(currentList.get(0).getMin() >= 4 * 60);
			assertTrue(currentList.get(0).getMax() <= 19 * 60);
			assertTrue(currentList.get(1).getMin() >= 23 * 60);
			assertTrue(currentList.get(1).getMax() <= 24 * 60);
			break;
		case NIGHTSHIFTER_PARTTIME:
			assertTrue(currentList.get(0).getMin() >= 4 * 60);
			assertTrue(currentList.get(0).getMax() <= 23 * 60);
			break;
		}
	}
}
