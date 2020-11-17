package it.cahung.research.callcenter.operator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;

import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cahung.research.callcenter.common.Configuration;
import it.cahung.research.callcenter.customer.Customer;

public class OperationsTest {
	private static final Logger logger = LoggerFactory.getLogger(OperationsTest.class.getCanonicalName());

	public Configuration setup() {
		Configuration configuration = new Configuration();

		configuration.setMaximumNumberOfOperators(20);
		configuration.setMaxOperatorAbility(10);
		configuration.setMaxProblemSize(600);

		configuration.setTotalCustomers(500);
		configuration.setTotalNumberOfDays(365);

		configuration.setShowAvgWaitingTime(false);
		configuration.setShowStatistics(false);
		configuration.setSortByProblemSize(false);
		configuration.setUseStatistics(false);
		return configuration;
	}

	@Test
	public void testCalls() throws Exception {
		Customer customer = new Customer();
		Operator operator = new Operator();
		HashMap<Customer, Operator> map = new HashMap<>();
		map.put(customer, operator);
		assertNotNull(map.get(customer));
		assertEquals(operator.getId(), map.get(customer).getId());

		testWithoutInternalStats();
		testWithInternalStats();
	}

	private void testWithInternalStats() throws Exception {
		logger.info("TESTING CASE USE STATISTICS NO SORTING");
		testAllStatsCases(true, false);
		logger.info("TESTING CASE USE STATISTICS WITH SORTING");
		testAllStatsCases(true, true);
	}

	private void testWithoutInternalStats() throws Exception {
		logger.info("TESTING CASE NO INTERNAL STATISTICS NO SORTING");
		testAllStatsCases(false, false);
		logger.info("TESTING CASE NO INTERNAL STATISTICS WITH SORTING");
		testAllStatsCases(false, true);
	}

	private void testAllStatsCases(boolean useStatistics, boolean sortByProblemSize) throws Exception {
		// Test without showing any stat
		logger.info("FF");
		testSingleCase(sortByProblemSize, useStatistics, false, false, "output_" + String.valueOf(useStatistics) + "FF"
				+ String.valueOf(sortByProblemSize) + System.currentTimeMillis() + ".csv");

		// Test showing avg time
		logger.info("TF");
		testSingleCase(sortByProblemSize, useStatistics, true, false, "output_" + String.valueOf(useStatistics) + "TF"
				+ String.valueOf(sortByProblemSize) + System.currentTimeMillis() + ".csv");

		// Test showing only statistics
		logger.info("FT");
		testSingleCase(sortByProblemSize, useStatistics, false, true, "output_" + String.valueOf(useStatistics) + "FT"
				+ String.valueOf(sortByProblemSize) + System.currentTimeMillis() + ".csv");

		// Test showing all stats
		logger.info("TT");
		testSingleCase(sortByProblemSize, useStatistics, true, true, "output_" + String.valueOf(useStatistics) + "TT"
				+ String.valueOf(sortByProblemSize) + System.currentTimeMillis() + ".csv");
	}

	private void testSingleCase(boolean sortByProblemSize, boolean useStatistics, boolean showAvgWaitingTime,
			boolean showStatistics, String outputFile) throws Exception {
		Configuration configuration = setup();
		configuration.setSortByProblemSize(sortByProblemSize);
		configuration.setOutputFile(outputFile);
		configuration.setUseStatistics(useStatistics);
		configuration.setShowAvgWaitingTime(showAvgWaitingTime);
		configuration.setShowStatistics(showStatistics);
		Operations operations = new Operations();
		operations.setConfiguration(configuration);
		operations.run();
	}

}
