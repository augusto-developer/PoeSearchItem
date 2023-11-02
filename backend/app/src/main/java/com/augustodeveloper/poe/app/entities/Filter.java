package com.augustodeveloper.poe.app.entities;

import org.json.JSONObject;

public class Filter {
	
	private String id;
    private Value value;
    private boolean disabled;
    
    public Filter() {}

	public Filter(String id, Value value, boolean disabled) {
		super();
		this.id = id;
		this.value = value;
		this.disabled = disabled;
	}
	
	public JSONObject toJson() {
	    JSONObject json = new JSONObject();
	    json.put("id", this.id);
	    json.put("value", new JSONObject().put("min", this.value.getMin()));
	    json.put("disabled", this.disabled);
	    return json;
	}

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public Value getValue() {
		return value;
	}

	public void setValue(Value value) {
		this.value = value;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}
    
    
}
