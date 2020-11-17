package it.cahung.research.callcenter.operator;

import java.util.List;

import it.cahung.research.callcenter.common.Configuration;

public interface OperatorFactory {
	public List<Operator> generateRandomOperators(int max);
	
	public void setConfiguration(Configuration configuration);

	public Configuration getConfiguration();
}
