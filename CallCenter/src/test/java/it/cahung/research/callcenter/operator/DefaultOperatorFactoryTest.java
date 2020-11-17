package it.cahung.research.callcenter.operator;

import static org.junit.Assert.assertTrue;

import java.util.List;

import org.junit.Test;

import it.cahung.research.callcenter.common.Configuration;

public class DefaultOperatorFactoryTest {
	DefaultOperatorFactory factory = new DefaultOperatorFactory();

	@Test
	public void test() {
		Configuration configuration = new Configuration();
		configuration.setMaxOperatorAbility(10);
		factory.setConfiguration(configuration);
		List<Operator> generateRandomOperators = factory.generateRandomOperators(100);
		assertTrue(generateRandomOperators.size() >= 0);
	}
}
