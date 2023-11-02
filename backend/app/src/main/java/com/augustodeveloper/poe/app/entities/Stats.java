package com.augustodeveloper.poe.app.entities;

import java.util.List;

import org.json.JSONObject;

public class Stats {
	
	private String type;
	private List<String> filters;
	
	public Stats() {}
	
	public Stats(String stats, List<String> filters) {
		super();
		this.type = stats;
		this.filters = filters;
	}
	
	public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("type", this.type);
        json.put("filters", this.filters);
        return json;
    }

	public String getStats() {
		return type;
	}

	public void setType(String stats) {
		this.type = stats;
	}

	public List<String> getFilters() {
		return filters;
	}

	public void setFilters(List<String> filters) {
		this.filters = filters;
	}
	
	
	
}
