package it.cahung.research.callcenter.common;

import java.lang.reflect.InvocationTargetException;

import it.cahung.research.callcenter.customer.CustomerFactory;
import it.cahung.research.callcenter.operator.OperatorFactory;

public class Configuration {
	private int totalNumberOfDays = 3650; // Ten years default
	private int maximumNumberOfOperators = 50;
	private int totalCustomers = maximumNumberOfOperators * 50;
	private boolean showAvgWaitingTime = false;
	private boolean showStatistics = false;
	private boolean sortByProblemSize = false;
	private boolean useStatistics = false;
	private int statsPeriod = 60; // Default is 1 hour
	private String outputFile = "output" + System.currentTimeMillis() + ".csv";
	private String customerFactoryClass = "it.cahung.research.callcenter.customer.DefaultCustomerFactory";
	private String operatorFactoryClass = "it.cahung.research.callcenter.operator.DefaultOperatorFactory";
	private CustomerFactory customerFactory;
	private OperatorFactory operatorFactory;
	private int maxProblemSize = 120;
	private int maxOperatorAbility = 10;

	public int getTotalNumberOfDays() {
		return totalNumberOfDays;
	}

	public int getMaximumNumberOfOperators() {
		return maximumNumberOfOperators;
	}

	public int getTotalCustomers() {
		return totalCustomers;
	}

	public boolean isShowAvgWaitingTime() {
		return showAvgWaitingTime;
	}

	public boolean isShowStatistics() {
		return showStatistics;
	}

	public boolean isUseStatistics() {
		return useStatistics;
	}

	public int getStatsPeriod() {
		return statsPeriod;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

	public void setTotalCustomers(int totalCustomers) {
		this.totalCustomers = totalCustomers;
	}

	public void setTotalNumberOfDays(int totalNumberOfDays) {
		this.totalNumberOfDays = totalNumberOfDays;
	}

	public void setMaximumNumberOfOperators(int maximumNumberOfOperators) {
		this.maximumNumberOfOperators = maximumNumberOfOperators;
	}

	public void setShowAvgWaitingTime(boolean showAvgWaitingTime) {
		this.showAvgWaitingTime = showAvgWaitingTime;
	}

	public void setShowStatistics(boolean showStatistics) {
		this.showStatistics = showStatistics;
	}

	public void setUseStatistics(boolean useStatistics) {
		this.useStatistics = useStatistics;
	}

	public void setStatsPeriod(int statsPeriod) {
		this.statsPeriod = statsPeriod;
	}

	public boolean isSortByProblemSize() {
		return sortByProblemSize;
	}

	public void setSortByProblemSize(boolean sortByProblemSize) {
		this.sortByProblemSize = sortByProblemSize;
	}

	public String getCustomerFactoryClass() {
		return customerFactoryClass;
	}

	public void setCustomerFactoryClass(String customerFactoryClass) {
		this.customerFactoryClass = customerFactoryClass;
	}

	public String getOperatorFactoryClass() {
		return operatorFactoryClass;
	}

	public void setOperatorFactoryClass(String operatorFactoryClass) {
		this.operatorFactoryClass = operatorFactoryClass;
	}

	public void initializeFactories() throws InstantiationException, IllegalAccessException, IllegalArgumentException,
			InvocationTargetException, NoSuchMethodException, SecurityException, ClassNotFoundException {
		this.customerFactory = (CustomerFactory) Class.forName(customerFactoryClass).getConstructor().newInstance();
		this.customerFactory.setConfiguration(this);
		this.operatorFactory = (OperatorFactory) Class.forName(operatorFactoryClass).getConstructor().newInstance();
		this.operatorFactory.setConfiguration(this);
	}

	public CustomerFactory getCustomerFactory() {
		return customerFactory;
	}

	public void setCustomerFactory(CustomerFactory customerFactory) {
		this.customerFactory = customerFactory;
	}

	public OperatorFactory getOperatorFactory() {
		return operatorFactory;
	}

	public void setOperatorFactory(OperatorFactory operatorFactory) {
		this.operatorFactory = operatorFactory;
	}

	public int getMaxProblemSize() {
		return maxProblemSize;
	}

	public void setMaxProblemSize(int maxProblemSize) {
		this.maxProblemSize = maxProblemSize;
	}

	public int getMaxOperatorAbility() {
		return maxOperatorAbility;
	}

	public void setMaxOperatorAbility(int maxOperatorAbility) {
		this.maxOperatorAbility = maxOperatorAbility;
	}
}
