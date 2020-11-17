package it.cahung.research.callcenter.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import it.cahung.research.callcenter.common.WorkerProfile;

public class WorkerProfileTest {
	@Test
	public void testValues() {
		WorkerProfile[] values = WorkerProfile.values();
		assertEquals(WorkerProfile.MONDAY_FRIDAY, values[0]);
		assertEquals(WorkerProfile.SHIFTER_5DAYS, values[1]);
		assertEquals(WorkerProfile.SHORTWEEK_3DAYS, values[2]);
		assertEquals(WorkerProfile.RANDOMDAYS, values[3]);
	}
}
