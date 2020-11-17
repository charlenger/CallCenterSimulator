package it.cahung.research.callcenter.operator;

import java.util.UUID;

public class Operator {
	private String id = UUID.randomUUID().toString();
	private int ability;

	public int getAbility() {
		return ability;
	}

	public void setAbility(int ability) {
		this.ability = ability;
	}

	public String getId() {
		return id;
	}

	@Override
	public boolean equals(Object obj) {
		return obj instanceof Operator && ((Operator) obj).getId().equals(this.id);
	}

	@Override
	public int hashCode() {
		return this.id.hashCode() + this.ability;
	}
}
