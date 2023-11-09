package com.augustodeveloper.poe.app.entities;

public class Value {
	private long min;
	private Boolean option;
	
	public Value() {}
	
	public Value(long min) {
		this.min = min;	
	}
	
	public Value(Boolean option) {
		this.option = option;
	}

	public long getMin() {
		return min;
	}

	public void setMin(long min) {
		this.min = min;
	}

	public Boolean getOption() {
		return option;
	}

	public void setOption(Boolean option) {
		this.option = option;
	}
	
	
}
