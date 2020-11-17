package it.cahung.research.callcenter.customer;

import java.util.List;

import it.cahung.research.callcenter.common.Configuration;

public interface CustomerFactory {
	public List<Customer> generateRandomCustomers(int max);

	public void setConfiguration(Configuration configuration);

	public Configuration getConfiguration();
}
