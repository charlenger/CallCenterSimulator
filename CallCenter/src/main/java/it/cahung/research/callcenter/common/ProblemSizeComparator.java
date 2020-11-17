package it.cahung.research.callcenter.common;

import java.util.Comparator;

import it.cahung.research.callcenter.customer.Customer;

public class ProblemSizeComparator implements Comparator<Customer> {

	@Override
	public int compare(Customer c1, Customer c2) {
		return Integer.compare(c1.getProblemSize(), c2.getProblemSize());
	}
}
