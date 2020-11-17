package it.cahung.research.callcenter.operator;

import static org.junit.Assert.assertEquals;

import java.util.UUID;

import org.junit.Test;

import it.cahung.research.callcenter.operator.Operator;

public class OperatorTest {
	Operator operator = new Operator();

	@Test
	public void test() {
		operator.setAbility(100);
		assertEquals(100, operator.getAbility());
		UUID.fromString(operator.getId());
	}
}
