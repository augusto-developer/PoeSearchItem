package com.augustodeveloper.poe.app.exec;

import org.json.JSONArray;
import org.json.JSONObject;

import com.augustodeveloper.poe.app.entities.Filter;
import com.augustodeveloper.poe.app.entities.Type;
import com.augustodeveloper.poe.app.entities.Value;

public class AccessoryExec {
	private String[] args;
	private JSONObject json;
	
	public AccessoryExec(String[] args) {
        this.args = args;
    }
	
	public void run(String input) {
		
		
		String[] lines = input.split("\n");

		
		Type typeFilter = new Type(lines[2], "online");
		
		Filter filter = new Filter();
		filter.setId("explicit.stat_4220027924");
		filter.setValue(new Value(20));
		filter.setDisabled(false);
		
		JSONObject json = new JSONObject();
		json.put("query", typeFilter.toJson());

		json.getJSONObject("query").put("status", new JSONObject().put("option", "online"));
		json.getJSONObject("query").put("stats", new JSONArray().put(new JSONObject().put("type", "and").put("filters", new JSONArray().put(filter.toJson()))));
		
		this.json = json;;
    }

	public JSONObject getJson() {
        return this.json;
    }

	public String[] getArgs() {
		return args;
	}

	public void setArgs(String[] args) {
		this.args = args;
	}

	public void setJson(JSONObject json) {
		this.json = json;
	}
	
	
}

