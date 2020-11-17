package it.cahung.research.callcenter.operator;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import it.cahung.research.callcenter.common.Configuration;
import it.cahung.research.callcenter.common.ProblemSizeComparator;
import it.cahung.research.callcenter.common.Statistics;
import it.cahung.research.callcenter.common.WeekDays;
import it.cahung.research.callcenter.customer.Customer;

public class Operations {
	private static final Logger logger = LoggerFactory.getLogger(Operations.class);

	// Configuration variables
	private Configuration configuration = new Configuration();

	// Working Centers Variables
	private LinkedList<Operator> busyOperators = new LinkedList<>();
	private LinkedList<Operator> onDutyOperators = new LinkedList<>();
	private LinkedList<Operator> availableOperators = new LinkedList<>();

	// Customer Queues a.k.a. workflow steps
	private LinkedList<Customer> futureCalls = new LinkedList<>();
	private LinkedList<Customer> ivr = new LinkedList<>(); // Customer is deciding whether to wait or not
	private LinkedList<Customer> waiting = new LinkedList<>(); // WIP starts here // WASTE
	private LinkedList<Customer> inProgress = new LinkedList<>(); // WIP call is going on // EFFECTIVE WORK
	private LinkedList<Customer> dropped = new LinkedList<>(); // Dropped calls
	private LinkedList<Customer> done = new LinkedList<>(); // Completed calls
	private Map<Customer, Operator> calls = new HashMap<>();

	// Timeline variables
	private Statistics currentStatistics = new Statistics();
	private int lastAverageWaitingTime = 0;

	// Operations methods
	public void run() throws Exception {
		configuration.initializeFactories();
		logger.info("Generating operators");
		generateOperators(); // Fills the onDutyOperators stack
		logger.info("Operators generation done");
		logger.info("Starting processing");
		processDemand(); // iterates throughout the total number of days and process demand
		logger.info("Processing completed");
	}

	private void generateOperators() {
		Collections.addAll(onDutyOperators, configuration.getOperatorFactory()
				.generateRandomOperators(configuration.getMaximumNumberOfOperators()).toArray(new Operator[0]));
	}

	private void generateDemand() {
		Collections.addAll(futureCalls, configuration.getCustomerFactory()
				.generateRandomCustomers(configuration.getTotalCustomers()).toArray(new Customer[0]));
	}

	private void processDemand() {
		try (FileWriter writer = new FileWriter(new File(configuration.getOutputFile()))) {
			int numberOfPeriods = Statistics.MINUTES_IN_DAY / configuration.getStatsPeriod();
			int remainingMinutes = Statistics.MINUTES_IN_DAY % configuration.getStatsPeriod();
			for (int i = 0; i < configuration.getTotalNumberOfDays(); ++i) {
				processSingleDayWithStats(writer, numberOfPeriods, remainingMinutes);
			}
		} catch (IOException e) {
			logger.error(e.getMessage(), e);
		}
	}

	private void processSingleDayWithStats(FileWriter writer, int numberOfPeriods, int remainingMinutes)
			throws IOException {
		currentStatistics.setCurrentDayCalls(0);
		currentStatistics.setDoneCallsInDay(0);
		currentStatistics.resetTimeInDay();
		processSingleDay(writer, numberOfPeriods, remainingMinutes);
		currentStatistics.computeAverages(onDutyOperators.size() + busyOperators.size());
		currentStatistics.getLastWeekTotalCallsInDay().set(currentStatistics.getDayOfTheWeek(),
				currentStatistics.getCurrentDayCalls());
		currentStatistics.setCountOfPassedDays(currentStatistics.getCountOfPassedDays() + 1);
		currentStatistics.updateDayOfTheWeek();
	}

	private void writeStats(FileWriter writer) throws IOException {
		int currentHour = currentStatistics.getTimeInDay() / 60;
		writer.write(String.format("%d,%d,%d,%d,%d,%d,%d,%d,%d,%s,%d,%d,%d,%d%n", busyOperators.size(),
				onDutyOperators.size(), availableOperators.size(), done.size(), dropped.size(), inProgress.size(),
				waiting.size(), ivr.size(), futureCalls.size(),
				WeekDays.values()[currentStatistics.getDayOfTheWeek()].toString(), currentHour,
				currentStatistics.getCountOfPassedDays(), currentStatistics.getLastPeriodAverageWaitingTime(),
				currentStatistics.getLastWeekAverageCallsPerDay()));
	}

	private void processSingleDay(FileWriter writer, int numberOfPeriods, int remainingMinutes) throws IOException {
		if (configuration.isUseStatistics() && currentStatistics.getCountOfPassedDays() > 7) {
			adaptOperators();
		}
		generateDemand();
		for (int i = 0; i < numberOfPeriods; ++i) {
			processPeriod(writer, configuration.getStatsPeriod()); // process the current range of minutes
		}
		processPeriod(writer, remainingMinutes); // complete the day and update stats
	}

	private void processPeriod(FileWriter writer, int totalMinutes) throws IOException {
		currentStatistics.setTotalCustomersMovingInProgress(totalMinutes / 2);
		currentStatistics.setTotalWaitingTime((totalMinutes / 2) * lastAverageWaitingTime);
		for (int i = 0; i < totalMinutes; ++i) {
			currentStatistics.setDoneCallsInDay(currentStatistics.getDoneCallsInDay() + fromInProgress());
			fromWaiting();
			fromIvr();
			currentStatistics.setCurrentDayCalls(currentStatistics.getCurrentDayCalls() + fromFutureCalls());
			fromDropped();
			currentStatistics.increaseTimeInDay();
		}
		if (totalMinutes > 0) {
			writeStats(writer);
		}
	}

	private void adaptOperators() {
		int currentAverageCallsPerOperator = currentStatistics.getCurrentAverageCallsPerOperator();
		if (currentAverageCallsPerOperator > 0) {
			int averageCallInThatDay = currentStatistics.getAverageCallsInDay()
					.get(currentStatistics.getDayOfTheWeek());
			if (currentStatistics.getLastWeekAverageCallsPerDay() / averageCallInThatDay >= 2) {
				averageCallInThatDay = currentStatistics.getLastWeekAverageCallsPerDay();
			}
			int neededOperators = averageCallInThatDay / currentAverageCallsPerOperator;
			int currentOnDuty = onDutyOperators.size() + busyOperators.size();
			if (neededOperators > currentOnDuty) {
				reduceOperators(currentOnDuty - neededOperators);
			} else if (neededOperators < currentOnDuty) {
				increaseOperators(neededOperators - currentOnDuty);
			}
		}
	}

	private void increaseOperators(int maxDifference) {
		for (int i = 0; i < maxDifference && !availableOperators.isEmpty(); ++i) {
			Operator operator = availableOperators.removeFirst();
			onDutyOperators.add(operator);
		}
	}

	private void reduceOperators(int maxDifference) {
		for (int i = 0; i < maxDifference && !onDutyOperators.isEmpty(); ++i) {
			Operator operator = onDutyOperators.removeFirst();
			availableOperators.add(operator);
		}
	}

	private void fromDropped() {
		for (int i = 0; i < dropped.size(); ++i) {
			Customer currentCustomer = dropped.removeFirst();
			currentCustomer.incrementWaitingTimeBeforeNewAttempt();
			if (currentCustomer.wantsToRetry()) {
				customerRetries(currentCustomer);
				futureCalls.add(currentCustomer);
			} else {
				dropped.add(currentCustomer);
			}
			currentCustomer.updateTimeline();
		}
	}

	private void customerRetries(Customer currentCustomer) {
		currentCustomer.resetWaitingTimeBeforeNewAttempt();
		currentCustomer.resetTimeInCall();
		currentCustomer.setRetry(currentCustomer.getRetry() + 1);
	}

	private int fromInProgress() {
		int doneCustomers = 0;
		for (int i = 0; i < inProgress.size(); ++i) {
			Customer currentCustomer = inProgress.removeFirst();
			Operator currentOperator = calls.get(currentCustomer);
			int ability = currentOperator.getAbility();
			currentCustomer.reduceProblemSize(ability);
			if (currentCustomer.getProblemSize() <= 0) {
				done.add(currentCustomer);
				busyOperators.remove(currentOperator);
				onDutyOperators.add(currentOperator);
				calls.remove(currentCustomer);
			} else {
				inProgress.add(currentCustomer);
			}
			currentCustomer.updateTimeline();
		}
		return doneCustomers;
	}

	private void fromWaiting() {
		if (configuration.isSortByProblemSize()) {
			Collections.sort(waiting, new ProblemSizeComparator());
		}
		for (int i = 0; i < waiting.size(); ++i) {
			Customer currentCustomer = waiting.removeFirst();
			Operator currentOperator = onDutyOperators.isEmpty() ? null : onDutyOperators.removeFirst();
			if (currentOperator != null) {
				busyOperators.add(currentOperator);
				inProgress.add(currentCustomer);
				calls.put(currentCustomer, currentOperator);
				currentStatistics.increaseTotalWaitingTime(currentCustomer.getTimeInCall());
				currentStatistics.increaseTotalCustomersMovingInProgress();
			} else if (currentCustomer.wantsToDrop()) {
				dropped.add(currentCustomer);
			} else {
				waiting.add(currentCustomer);
				currentCustomer.incrementTimeInCall();
			}
			currentCustomer.updateTimeline();
		}
	}

	private void fromIvr() {
		for (int i = 0; i < ivr.size(); ++i) {
			Customer currentCustomer = ivr.removeFirst();
			if (currentCustomer.wantsToStayInCall(currentStatistics, configuration.isShowAvgWaitingTime(),
					configuration.isShowStatistics())) {
				waiting.add(currentCustomer);
				currentCustomer.resetTimeInCall();
			} else {
				dropped.add(currentCustomer);
			}
			currentCustomer.updateTimeline();
		}
	}

	private int fromFutureCalls() {
		int callingCustomers = 0;
		for (int i = 0; i < futureCalls.size(); ++i) {
			Customer currentCustomer = futureCalls.removeFirst();
			if (currentCustomer.wantsToCall(currentStatistics.getDayOfTheWeek(), currentStatistics.getTimeInDay())) {
				ivr.add(currentCustomer);
				callingCustomers++;
			} else if (currentCustomer.wantsToDrop()) {
				dropped.add(currentCustomer);
			} else {
				futureCalls.add(currentCustomer);
			}
			currentCustomer.updateTimeline();
		}
		return callingCustomers;
	}

	public void setConfiguration(Configuration configuration) {
		this.configuration = configuration;
	}

	public Configuration getConfiguration() {
		return configuration;
	}

	// Running methods
	public static void main(String[] args) {
		Operations currentRun = new Operations();
		for (int i = 0; i < args.length; ++i) {
			switch (args[i]) {
			case "totalCustomers":
				currentRun.getConfiguration().setTotalCustomers(Integer.parseInt(args[i + 1]));
				break;
			case "totalNumberOfDays":
				currentRun.getConfiguration().setTotalNumberOfDays(Integer.parseInt(args[i + 1]));
				break;
			case "maximumNumberOfOperators":
				currentRun.getConfiguration().setMaximumNumberOfOperators(Integer.parseInt(args[i + 1]));
				break;
			case "showAvgWaitingTime":
				currentRun.getConfiguration().setShowAvgWaitingTime(Boolean.parseBoolean(args[i + 1]));
				break;
			case "showStatistics":
				currentRun.getConfiguration().setShowStatistics(Boolean.parseBoolean(args[i + 1]));
				break;
			case "sortByProblemSize":
				currentRun.getConfiguration().setSortByProblemSize(Boolean.parseBoolean(args[i + 1]));
				break;
			case "useStatistics":
				currentRun.getConfiguration().setUseStatistics(Boolean.parseBoolean(args[i + 1]));
				break;
			case "outputFile":
				currentRun.getConfiguration().setOutputFile(args[i + 1]);
				break;
			case "statsPeriod":
				currentRun.getConfiguration().setStatsPeriod(Integer.parseInt(args[i + 1]));
				break;
			case "customerFactoryClass":
				currentRun.getConfiguration().setCustomerFactoryClass(args[i + 1]);
				break;
			case "operatorFactoryClass":
				currentRun.getConfiguration().setOperatorFactoryClass(args[i + 1]);
				break;
			case "maxProblemSize":
				currentRun.getConfiguration().setMaxProblemSize(Integer.parseInt(args[i + 1]));
				break;
			case "maxOperatorAbility":
				currentRun.getConfiguration().setMaxOperatorAbility(Integer.parseInt(args[i + 1]));
				break;
			}
		}
		try {
			currentRun.run();
		} catch (Exception e) {
			logger.error("Unable to run", e);
		}
	}
}
