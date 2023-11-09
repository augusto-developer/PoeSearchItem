package com.augustodeveloper.poe.app.entities;

import org.json.JSONObject;

public class Miscellaneous {

	private Value synthesised_item;
	private Value corrupted;
	private boolean disabled;
	

	public Miscellaneous() {
	}

	public Miscellaneous(Value corrupted, Value synthesised_item) {
		this.corrupted = corrupted;
		this.synthesised_item = synthesised_item;
	}

	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("synthesised_item",  new JSONObject().put("option", this.synthesised_item.getOption()));
		json.put("corrupted",  new JSONObject().put("option", this.corrupted.getOption()));

		return json;
	}

	public Value getSynthesised_item() {
		return synthesised_item;
	}

	public void setSynthesised_item(Value synthesised_item) {
		this.synthesised_item = synthesised_item;
	}

	public Value getCorrupted() {
		return corrupted;
	}

	public void setCorrupted(Value corrupted) {
		this.corrupted = corrupted;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	

}
