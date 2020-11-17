package it.cahung.research.callcenter.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import it.cahung.research.callcenter.common.AvailabilityProfile;

public class AvailabilityProfileTest {
	@Test
	public void testValues() {
		AvailabilityProfile[] values = AvailabilityProfile.values();
		assertEquals(AvailabilityProfile.DAILY_PERMANENT, values[0]);
		assertEquals(AvailabilityProfile.DAILY_PARTTIME, values[1]);
		assertEquals(AvailabilityProfile.DAILY_FREELANCE, values[2]);
		assertEquals(AvailabilityProfile.NIGHTSHIFTER_PERMANENT, values[3]);
		assertEquals(AvailabilityProfile.NIGHTSHIFTER_PARTTIME, values[4]);
		assertEquals(AvailabilityProfile.FREE_DAY, values[5]);
		assertEquals(AvailabilityProfile.FREE_NIGHT, values[6]);
	}
}
