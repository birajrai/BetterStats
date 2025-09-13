package io.github.birajrai.betterstats.stats;

/**
 * @author Biraj Rai
 * 2025 - 2028
 */
public class Stat {
	
	private Stats stat;
	private Object variable, complex;
	
	public Stat(Stats stat, Object variable) {
		this.stat = stat;
		this.variable = variable;
	}
	
	public Stat(Stats stat, Object variable, Object complex) {
		this.stat = stat;
		this.variable = variable;
		this.complex = complex;
	}
	
	public Stats getStat() {
		return stat;
	}
	
	public Object getVariable() {
		return variable;
	}
	
	public Object getComplex() {
		return complex;
	}

}
