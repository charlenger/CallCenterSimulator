package it.cahung.research.callcenter.operator;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;

import it.cahung.research.callcenter.common.Configuration;

public class DefaultOperatorFactory implements OperatorFactory {
	private final SecureRandom random = new SecureRandom();
	private Configuration configuration;
	private boolean isRandom = false;

	public List<Operator> generateRandomOperators(int max) {
		int total = isRandom ? random.nextInt(max) : max;
		List<Operator> list = new ArrayList<>();
		for (int i = 0; i < total; ++i) {
			Operator operator = new Operator();
			operator.setAbility(random.nextInt(configuration.getMaxOperatorAbility()));
			list.add(operator);
		}
		return list;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public boolean isRandom() {
		return isRandom;
	}

	public void setRandom(boolean isRandom) {
		this.isRandom = isRandom;
	}
}
