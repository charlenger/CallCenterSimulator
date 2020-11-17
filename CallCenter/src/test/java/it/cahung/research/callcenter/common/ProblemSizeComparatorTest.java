package it.cahung.research.callcenter.common;

import static org.junit.Assert.assertEquals;

import java.util.Collections;
import java.util.LinkedList;

import org.junit.Test;

import it.cahung.research.callcenter.common.ProblemSizeComparator;
import it.cahung.research.callcenter.customer.Customer;

public class ProblemSizeComparatorTest {

	@Test
	public void compareTest() {
		Customer c1 = new Customer();
		c1.setProblemSize(10);
		Customer c2 = new Customer();
		c2.setProblemSize(1);
		Customer c3 = new Customer();
		c3.setProblemSize(100);

		LinkedList<Customer> cs = new LinkedList<>();
		cs.add(c1);
		cs.add(c3);
		cs.add(c2);

		Collections.sort(cs, new ProblemSizeComparator());
		assertEquals(c2.getId(), cs.removeFirst().getId());
		assertEquals(c3.getId(), cs.removeLast().getId());
	}
}
