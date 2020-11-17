package it.cahung.research.callcenter.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import it.cahung.research.callcenter.common.WeekDays;

public class WeekDaysTest {
	
	@Test
	public void testValues() {
		WeekDays[] values = WeekDays.values();
		assertEquals(WeekDays.MONDAY, values[0]);
		assertEquals(WeekDays.TUESDAY, values[1]);
		assertEquals(WeekDays.WEDNESDAY, values[2]);
		assertEquals(WeekDays.THURSDAY, values[3]);
		assertEquals(WeekDays.FRIDAY, values[4]);
		assertEquals(WeekDays.SATURDAY, values[5]);
		assertEquals(WeekDays.SUNDAY, values[6]);
	}
}
