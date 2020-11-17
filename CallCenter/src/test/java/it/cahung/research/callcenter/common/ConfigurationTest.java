package it.cahung.research.callcenter.common;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import org.junit.Test;

import it.cahung.research.callcenter.common.Configuration;
import it.cahung.research.callcenter.customer.CustomerFactory;
import it.cahung.research.callcenter.customer.DefaultCustomerFactory;
import it.cahung.research.callcenter.operator.DefaultOperatorFactory;
import it.cahung.research.callcenter.operator.OperatorFactory;

public class ConfigurationTest {
	Configuration configuration = new Configuration();

	@Test
	public void testGettersAndSetters() throws Exception {
		CustomerFactory customerFactory = new DefaultCustomerFactory();
		configuration.setCustomerFactory(customerFactory);
		assertEquals(customerFactory, configuration.getCustomerFactory());
		OperatorFactory operatorFactory = new DefaultOperatorFactory();
		configuration.setOperatorFactory(operatorFactory);
		assertEquals(operatorFactory, configuration.getOperatorFactory());
		configuration.setOperatorFactoryClass(DefaultOperatorFactory.class.getCanonicalName());
		configuration.setCustomerFactoryClass(DefaultCustomerFactory.class.getCanonicalName());
		configuration.initializeFactories();

		configuration.setMaximumNumberOfOperators(100);
		assertEquals(100, configuration.getMaximumNumberOfOperators());
		configuration.setOutputFile("pippo");
		assertEquals("pippo", configuration.getOutputFile());
		configuration.setShowAvgWaitingTime(true);
		assertTrue(configuration.isShowAvgWaitingTime());
		configuration.setShowAvgWaitingTime(false);
		assertFalse(configuration.isShowAvgWaitingTime());
		configuration.setShowStatistics(true);
		assertTrue(configuration.isShowStatistics());
		configuration.setShowStatistics(false);
		assertFalse(configuration.isShowStatistics());
		configuration.setSortByProblemSize(true);
		assertTrue(configuration.isSortByProblemSize());
		configuration.setSortByProblemSize(false);
		assertFalse(configuration.isSortByProblemSize());
		configuration.setStatsPeriod(100);
		assertEquals(100, configuration.getStatsPeriod());
		configuration.setTotalCustomers(100);
		assertEquals(100, configuration.getTotalCustomers());
		configuration.setTotalNumberOfDays(100);
		assertEquals(100, configuration.getTotalNumberOfDays());
		configuration.setUseStatistics(true);
		assertTrue(configuration.isUseStatistics());
		configuration.setUseStatistics(false);
		assertFalse(configuration.isUseStatistics());
		configuration.setMaxProblemSize(100);
		assertEquals(100, configuration.getMaxProblemSize());
	}
}
