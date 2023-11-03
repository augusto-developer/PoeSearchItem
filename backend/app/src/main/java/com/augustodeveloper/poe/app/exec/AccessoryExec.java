package com.augustodeveloper.poe.app.exec;

import java.util.HashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		  Map<String, String> mapping = initMapping();
		    String[] lines = input.split("\n");
		    Type typeFilter = new Type(lines[2], "online");

		    JSONArray filters = new JSONArray();

		    JSONObject json = new JSONObject();
		    json.put("query", typeFilter.toJson());

		    JSONObject queryJson = json.getJSONObject("query");
		    queryJson.put("status", new JSONObject().put("option", "online"));

		    for (String line : lines) {
		        for (String key : mapping.keySet()) {
		            if (line.contains(key)) {
		                Filter filter = new Filter();
		                filter.setId(mapping.get(key));
		                filter.setValue(new Value(20));
		                filter.setDisabled(false);
		                filters.put(filter.toJson());
		            }
		        }
		    }

		    // Adiciona o array "filters" ao objeto "query" fora do loop
		    queryJson.put("stats", new JSONArray().put(new JSONObject().put("type", "and").put("filters", filters)));

		    this.json = json;
		 
		 System.out.println(json.toString());
    }
	
//	private String extractTextAfterTo(String text) {
//	    Pattern pattern = Pattern.compile("(\\d+)(?:\\s+to\\s+(.*)|\\s+%.*)");
//	    Matcher matcher = pattern.matcher(text);
//	    if (matcher.find()) {
//	        if (matcher.group(2) != null) {
//	            return matcher.group(2);
//	        }
//	    }
//	    return text;
//	}
//
//	private Integer extractNumberBeforeTo(String text) {
//	    Pattern pattern = Pattern.compile("(\\d+)(?:\\s+to\\s+(.*)|\\s+%.*)");
//	    Matcher matcher = pattern.matcher(text);
//	    if (matcher.find()) {
//	        if (matcher.group(1) != null) {
//	            return Integer.parseInt(matcher.group(1));
//	        }
//	    }
//	    return null;
//	}
	
	
	private Map<String, String> initMapping() {
	    Map<String, String> mapping = new HashMap<>();
	    mapping.put("to Dexterity", "explicit.stat_3261801346");
	    mapping.put("increased Armour and Evasion", "explicit.stat_2511217560");
	    mapping.put("increased Stun And Block Recovery", "explicit.stat_2511217560");
	    mapping.put("to maximum Life", "explicit.stat_3299347043");
	    mapping.put("to Chaos Resistance", "explicit.stat_2923486259");
	    mapping.put("increased Movement Speed", "explicit.stat_2250533757");
	    return mapping;
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

