package it.cahung.research.callcenter.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cahung.research.callcenter.common.Statistics;

public class StatisticsTest {
	private static final Logger logger = LoggerFactory.getLogger(StatisticsTest.class.getCanonicalName());
	Statistics currentStatistics = new Statistics();

	@Test
	public void test() {
		currentStatistics.setDayOfTheWeek(1);
		assertEquals(1, currentStatistics.getDayOfTheWeek());
		currentStatistics.updateDayOfTheWeek();
		assertEquals(2, currentStatistics.getDayOfTheWeek());
		currentStatistics.setDayOfTheWeek(6);
		assertEquals(6, currentStatistics.getDayOfTheWeek());
		currentStatistics.updateDayOfTheWeek();
		assertEquals(0, currentStatistics.getDayOfTheWeek());

		assertEquals(0, currentStatistics.getTimeInDay());
		currentStatistics.increaseTimeInDay();
		assertEquals(1, currentStatistics.getTimeInDay());
		currentStatistics.resetTimeInDay();
		assertEquals(0, currentStatistics.getTimeInDay());

		currentStatistics.setCountOfPassedDays(10);
		assertEquals(10, currentStatistics.getCountOfPassedDays());
		currentStatistics.setCountOfPassedDays(0);
		assertEquals(1, currentStatistics.getCountOfPassedDays());

		currentStatistics.setCurrentDayCalls(100);
		assertEquals(100, currentStatistics.getCurrentDayCalls());
		currentStatistics.setDoneCallsInDay(90);
		assertEquals(90, currentStatistics.getDoneCallsInDay());

		currentStatistics.setTotalCustomersMovingInProgress(10);
		assertEquals(10, currentStatistics.getTotalCustomersMovingInProgress());
		currentStatistics.increaseTotalCustomersMovingInProgress();
		assertEquals(11, currentStatistics.getTotalCustomersMovingInProgress());

		currentStatistics.setTotalWaitingTime(100);
		assertEquals(100, currentStatistics.getTotalWaitingTime());
		currentStatistics.increaseTotalWaitingTime(100);
		assertEquals(200, currentStatistics.getTotalWaitingTime());

		assertEquals((200 / 11), currentStatistics.getLastPeriodAverageWaitingTime());

		// PROCESS DAY
		for (int j = 0; j < 8; ++j) {
			logger.debug("DAY: " + j);
			currentStatistics.setCurrentDayCalls(0);
			assertEquals(0, currentStatistics.getCurrentDayCalls());
			currentStatistics.setDoneCallsInDay(0);
			assertEquals(0, currentStatistics.getDoneCallsInDay());
			currentStatistics.resetTimeInDay();
			assertEquals(0, currentStatistics.getTimeInDay());
			// PROCESS PERIOD
			for (int i = 0; i < 24; ++i) { // 24 periods of 60 minutes
				logger.debug("HOUR: " + i);
				for (int k = 0; k < 60; ++k) {
					logger.debug("MINUTE: " + k);
					// PROCESS MINUTE BY MINUTE
					currentStatistics.setDoneCallsInDay(currentStatistics.getDoneCallsInDay() + 1); // 60 DONE CALL PER
																									// HOUR
					assertEquals(60 * i + (k + 1), currentStatistics.getDoneCallsInDay());
					currentStatistics.setCurrentDayCalls(currentStatistics.getCurrentDayCalls() + 1); // 60 NEW CALLS
																										// PER HOUR
					assertEquals(60 * i + (k + 1), currentStatistics.getCurrentDayCalls());
					currentStatistics.increaseTimeInDay();
					assertEquals(60 * i + (k + 1), currentStatistics.getTimeInDay());
				}
			}
			currentStatistics.computeAverages(10); // 10 operators
			currentStatistics.getLastWeekTotalCallsInDay().set(currentStatistics.getDayOfTheWeek(),
					currentStatistics.getCurrentDayCalls());
			currentStatistics.setCountOfPassedDays(currentStatistics.getCountOfPassedDays() + 1);
			currentStatistics.updateDayOfTheWeek();
		}
		assertEquals(9, currentStatistics.getCountOfPassedDays());
		assertEquals((Long) 2880L, currentStatistics.getTotalCallsInDay().get(0));
		assertEquals((Integer) 1440, currentStatistics.getAverageCallsInDay().get(0));
		assertEquals((Long) 1440L, currentStatistics.getTotalCallsInDay().get(1));
		assertEquals((Integer) 1440, currentStatistics.getAverageCallsInDay().get(1));
		assertEquals(1, currentStatistics.getDayOfTheWeek());

		assertEquals(144, currentStatistics.getCurrentAverageCallsPerOperator());
		assertEquals(1440, currentStatistics.getLastWeekAverageCallsPerDay());
		assertEquals((Integer) 1440, currentStatistics.getLastWeekTotalCallsInDay().get(0));
		assertEquals((Integer) 1440, currentStatistics.getLastWeekTotalCallsInDay().get(1));

		assertEquals((Integer) 60, currentStatistics.getTodaysIncrementalCallsPerHour().get(0));
		assertEquals((Integer) 120, currentStatistics.getTodaysIncrementalCallsPerHour().get(1));
		assertEquals((Integer) 180, currentStatistics.getTodaysIncrementalCallsPerHour().get(2));
		assertEquals((Integer) 1440, currentStatistics.getTodaysIncrementalCallsPerHour().get(23));

		assertEquals((Long) 60L, currentStatistics.getTotalCallsPerHourInDay().get(6).get(22));
		assertEquals((Long) 60L, currentStatistics.getTotalCallsPerHourInDay().get(3).get(5));
		assertEquals((Long) 120L, currentStatistics.getTotalCallsPerHourInDay().get(0).get(0));
		assertEquals((Long) 120L, currentStatistics.getTotalCallsPerHourInDay().get(0).get(7));

		assertEquals((Integer) 60, currentStatistics.getAverageCallsPerHourInDay().get(0).get(4));
		assertEquals((Integer) 60, currentStatistics.getAverageCallsPerHourInDay().get(1).get(8));
		assertEquals((Integer) 60, currentStatistics.getAverageCallsPerHourInDay().get(6).get(12));
	}
}
