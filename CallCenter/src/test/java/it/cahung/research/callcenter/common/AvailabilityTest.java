package it.cahung.research.callcenter.common;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import it.cahung.research.callcenter.common.Availability;

public class AvailabilityTest {

	@Test
	public void testGetAndSet() {
		Availability a = new Availability();
		a.setMin(10);
		assertEquals(10, a.getMin());
		a.setMax(100);
		assertEquals(100, a.getMax());
		a.setMin(-10);
		assertEquals(0, a.getMin());
		a.setMax(2000);
		assertEquals(1440, a.getMax());
		a.setMax(1000);
		a.setMin(1001);
		assertEquals(999, a.getMin());
		a.setMax(100);
		assertEquals(1000, a.getMax());
	}
}
