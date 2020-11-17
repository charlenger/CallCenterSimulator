package it.cahung.research.callcenter.customer;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cahung.research.callcenter.common.Availability;
import it.cahung.research.callcenter.common.Statistics;
import it.cahung.research.callcenter.common.WeekDays;
import it.cahung.research.callcenter.customer.Customer;

public class CustomerTest {
	private static final Logger logger = LoggerFactory.getLogger(CustomerTest.class.getCanonicalName());
	Customer customer = new Customer();

	@Test
	public void testGetterAndSetters() {
		customer.setDeadlineSinceCall(10);
		assertEquals(10, customer.getDeadlineSinceCall());
		customer.setDropProbability(0.7f);
		assertEquals((Float) 0.7f, (Float) customer.getDropProbability());
		customer.setMaxWaitingTimeBeforeNewAttempt(100);
		assertEquals(100, customer.getMaxWaitingTimeBeforeNewAttempt());
		customer.setMinimumWaitingTime(10);
		assertEquals(10, customer.getMinimumWaitingTime());
		customer.setProblemSize(100);
		assertEquals(100, customer.getProblemSize());
		customer.setRetry(10);
		assertEquals(10, customer.getRetry());
		customer.setTimeInCall(100);
		assertEquals(100, customer.getTimeInCall());
		customer.setWaitingTimeBeforeNewAttempt(100);
		assertEquals(100, customer.getWaitingTimeBeforeNewAttempt());
	}

	@Test
	public void testAvailabilities() {
		Availability range0 = new Availability();
		range0.setMin(100);
		range0.setMax(150);
		Availability range1 = new Availability();
		range1.setMin(200);
		range1.setMax(300);
		Availability range2 = new Availability();
		range2.setMin(500);
		range2.setMax(1000);
		Map<WeekDays, List<Availability>> availabilityMap = new HashMap<>();
		List<Availability> availabilitiesForDayMonday = new ArrayList<>();
		availabilitiesForDayMonday.add(range0);
		availabilitiesForDayMonday.add(range1);
		List<Availability> availabilitiesForDayTuesday = new ArrayList<>();
		availabilitiesForDayTuesday.add(range1);
		availabilitiesForDayTuesday.add(range2);
		List<Availability> availabilitiesForDayFriday = new ArrayList<>();
		availabilitiesForDayFriday.add(range2);
		availabilityMap.put(WeekDays.MONDAY, availabilitiesForDayMonday);
		availabilityMap.put(WeekDays.TUESDAY, availabilitiesForDayTuesday);
		availabilityMap.put(WeekDays.FRIDAY, availabilitiesForDayFriday);

		customer.setAvailabilities(availabilityMap);

		assertFalse(customer.isAvailable(WeekDays.THURSDAY, 100));

		assertTrue(customer.isAvailable(WeekDays.MONDAY, 101));
		assertFalse(customer.isAvailable(WeekDays.MONDAY, 155));
		assertTrue(customer.isAvailable(WeekDays.MONDAY, 220));

		assertTrue(customer.isAvailable(WeekDays.TUESDAY, 220));
		assertFalse(customer.isAvailable(WeekDays.TUESDAY, 120));
		assertTrue(customer.isAvailable(WeekDays.TUESDAY, 500));
	}

	@Test
	public void testDynamicLogics() {
		// incrementTimeInCall();
		customer.incrementTimeInCall();
		assertEquals(1, customer.getTimeInCall());
		// resetTimeInCall();
		customer.resetTimeInCall();
		assertEquals(0, customer.getTimeInCall());

		// incrementRetry();
		customer.incrementRetry();
		assertEquals(1, customer.getRetry());

		// getId();
		UUID.fromString(customer.getId());

		// reduceProblemSize(int ability);
		customer.setProblemSize(100);
		assertEquals(100, customer.getProblemSize());
		customer.reduceProblemSize(10);
		assertEquals(90, customer.getProblemSize());

		// incrementWaitingTimeBeforeNewAttempt();
		int currentWT = customer.getWaitingTimeBeforeNewAttempt();
		customer.incrementWaitingTimeBeforeNewAttempt();
		assertEquals(currentWT + 1, customer.getWaitingTimeBeforeNewAttempt());
		customer.resetWaitingTimeBeforeNewAttempt();
		assertTrue(customer.getWaitingTimeBeforeNewAttempt() < customer.getMaxWaitingTimeBeforeNewAttempt());
		assertTrue(customer.getWaitingTimeBeforeNewAttempt() > 0);

		customer.setDropProbability(0.5f);
		customer.setDeadlineSinceCall(1441);
		customer.setMaxWaitingTimeBeforeNewAttempt(2000);
		customer.updateTimeline();
		assertEquals(720, customer.getMaxWaitingTimeBeforeNewAttempt());
		assertEquals(1440, customer.getDeadlineSinceCall());
		assertEquals((Float) (0.5f * 1440f / 14400f), (Float) customer.getDropProbability());

		customer.setMaxWaitingTimeBeforeNewAttempt(100);
		customer.setWaitingTimeBeforeNewAttempt(100);
		assertTrue(customer.wantsToRetry());
		customer.incrementWaitingTimeBeforeNewAttempt();
		assertFalse(customer.wantsToRetry());

		Availability range0 = new Availability();
		range0.setMin(100);
		range0.setMax(150);
		Map<WeekDays, List<Availability>> availabilityMap = new HashMap<>();
		List<Availability> availabilitiesForDayMonday = new ArrayList<>();
		availabilitiesForDayMonday.add(range0);
		availabilityMap.put(WeekDays.MONDAY, availabilitiesForDayMonday);
		customer.setAvailabilities(availabilityMap);
		assertTrue(customer.wantsToCall(0, 101));
		assertFalse(customer.wantsToCall(2, 100));
		assertFalse(customer.wantsToCall(0, 151));

		customer.setDeadlineSinceCall(0);
		assertTrue(customer.wantsToDrop());
		customer.setDeadlineSinceCall(120);
		customer.setDropProbability(1.0f);
		customer.setMinimumWaitingTime(10);
		customer.setTimeInCall(11);
		assertTrue(customer.wantsToDrop());
		customer.setTimeInCall(9);
		assertFalse(customer.wantsToDrop());
		customer.setDropProbability(0.0f);
		customer.setTimeInCall(11);
		assertFalse(customer.wantsToDrop());
		customer.setTimeInCall(9);
		assertFalse(customer.wantsToDrop());

		Statistics currentStatistics = new Statistics();
		currentStatistics.setTotalCustomersMovingInProgress(10);
		currentStatistics.setTotalWaitingTime(200);
		assertTrue(customer.wantsToStayInCall(null, true, true));
		assertTrue(customer.wantsToStayInCall(new Statistics(), false, false));
		logger.debug("Avg Waiting Time: " + currentStatistics.getLastPeriodAverageWaitingTime());
		customer.setMinimumWaitingTime(25);
		customer.setDropProbability(0f);
		assertTrue(customer.wantsToStayInCall(currentStatistics, true, false));
		customer.setMinimumWaitingTime(15);
		customer.setDropProbability(1f);
		assertFalse(customer.wantsToStayInCall(currentStatistics, true, false));
		customer.setMinimumWaitingTime(15);
		customer.setDropProbability(0f);
		assertTrue(customer.wantsToStayInCall(currentStatistics, true, false));

		// SET FAKE HOURLY STATISTICS
		List<List<Integer>> fakeStatistics = new ArrayList<>();
		for (int i = 0; i < 7; ++i) {
			List<Integer> fakeHourlyStatsForDay = new ArrayList<>();
			for (int j = 0; j <= 12; ++j) {
				fakeHourlyStatsForDay.add(10 * j);
			}
			for (int j = 12; j > 0; --j) {
				fakeHourlyStatsForDay.add(10 * j);
			}
			fakeStatistics.add(fakeHourlyStatsForDay);
		}
		currentStatistics.setAverageCallsPerHourInDay(fakeStatistics);
		currentStatistics.setDayOfTheWeek(4);
		currentStatistics.setTimeInDay(540);
		assertEquals((Integer) 90, currentStatistics.getAverageCallsPerHourInDay()
				.get(currentStatistics.getDayOfTheWeek()).get(currentStatistics.getTimeInDay() / 60));
		assertEquals((Integer) 100, currentStatistics.getAverageCallsPerHourInDay()
				.get(currentStatistics.getDayOfTheWeek()).get(1 + currentStatistics.getTimeInDay() / 60));
		assertEquals((Integer) 70,
				currentStatistics.getAverageCallsPerHourInDay().get(currentStatistics.getDayOfTheWeek()).get(18));
		// SET CUSTOMER AVAILABILITIES SO THAT HE CAN MOVE FURTHER
		Availability officeRange = new Availability();
		officeRange.setMin(540);
		officeRange.setMax(1140);
		Map<WeekDays, List<Availability>> fakeAvailabilityMap = new HashMap<>();
		List<Availability> fakeAvailabilities = new ArrayList<>();
		fakeAvailabilities.add(officeRange);
		fakeAvailabilityMap.put(WeekDays.FRIDAY, fakeAvailabilities);
		fakeAvailabilityMap.put(WeekDays.SATURDAY, fakeAvailabilities);
		fakeAvailabilityMap.put(WeekDays.SUNDAY, fakeAvailabilities);
		customer.setAvailabilities(fakeAvailabilityMap);
		customer.setDeadlineSinceCall(14400);
		customer.setDropProbability(1f);
		// CHECK IF THE CUSTOMER WANTS TO WAIT OR DELAY THE CALL
		assertFalse(customer.wantsToStayInCall(currentStatistics, false, true));
		customer.setDropProbability(0f);
		assertTrue(customer.wantsToStayInCall(currentStatistics, false, true));
		customer.setDropProbability(1f);
		customer.setDeadlineSinceCall(1440);
		assertTrue(customer.wantsToStayInCall(currentStatistics, false, true));
		customer.setDeadlineSinceCall(300);
		assertTrue(customer.wantsToStayInCall(currentStatistics, false, true));

		// SET FAKE HOURLY STATISTICS
		fakeStatistics = new ArrayList<>();
		for (int i = 0; i < 7; ++i) {
			List<Integer> fakeHourlyStatsForDay = new ArrayList<>();
			for (int j = 0; j < 24; ++j) {
				fakeHourlyStatsForDay.add((10 - i) * j);
			}
			fakeStatistics.add(fakeHourlyStatsForDay);
		}
		currentStatistics.setAverageCallsPerHourInDay(fakeStatistics);
		currentStatistics.setDayOfTheWeek(5);
		currentStatistics.setTimeInDay(540);
		assertEquals((Integer) 90,
				currentStatistics.getAverageCallsPerHourInDay().get(0).get(currentStatistics.getTimeInDay() / 60));
		assertTrue(currentStatistics.getAverageCallsPerHourInDay().get(1)
				.get(currentStatistics.getTimeInDay() / 60) < currentStatistics.getAverageCallsPerHourInDay().get(0)
						.get(currentStatistics.getTimeInDay() / 60));
		assertEquals((Integer) 100,
				currentStatistics.getAverageCallsPerHourInDay().get(0).get(1 + currentStatistics.getTimeInDay() / 60));
		assertEquals((Integer) 180, currentStatistics.getAverageCallsPerHourInDay().get(0).get(18));
		customer.setDeadlineSinceCall(2880);
		assertTrue(customer.wantsToStayInCall(currentStatistics, false, true));
		customer.setDeadlineSinceCall(4320);
		assertFalse(customer.wantsToStayInCall(currentStatistics, false, true));
		
		customer.getAvailabilities().remove(WeekDays.SATURDAY);
		customer.getAvailabilities().remove(WeekDays.SUNDAY);
		assertTrue(customer.wantsToStayInCall(currentStatistics, false, true));
		
		currentStatistics.setDayOfTheWeek(4);
		Availability fakeOfficeRange = new Availability();
		fakeOfficeRange.setMin(1300);
		fakeOfficeRange.setMax(1440);
		List<Availability> fakeNewAvailabilities = new ArrayList<>();
		fakeNewAvailabilities.add(fakeOfficeRange);
		customer.getAvailabilities().put(WeekDays.SATURDAY, fakeNewAvailabilities);
		customer.setDropProbability(1f);
		assertTrue(customer.wantsToStayInCall(currentStatistics, false, true));
	}
}
