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
import com.augustodeveloper.poe.app.services.PoeTradeService;


public class AccessoryExec {
	   private String[] args;
	   private JSONObject json;
	   
	  
	   public PoeTradeService poeTradeService;

	   public AccessoryExec(String[] args) {
	       this.args = args;
	       this.poeTradeService = new PoeTradeService();
	   }

	   public void run(String input) throws Exception {
		   	
		   	String[] lines = input.split("\n");
		    Type typeFilter = new Type(lines[2], "online");

		    
		    String responseBody = poeTradeService.getStats();

		    JSONObject jsonResponse = new JSONObject(responseBody);
		    JSONArray results = jsonResponse.getJSONArray("result");
		    
		    
		    JSONArray filters = new JSONArray();
		    
		    JSONObject json = new JSONObject();
		    json.put("query", typeFilter.toJson());

		    JSONObject queryJson = json.getJSONObject("query");
		    queryJson.put("status", new JSONObject().put("option", "online"));
		    
 
		    
		    Pattern pattern = Pattern.compile("(\\+\\d+\\.?\\d*%?|\\d+\\.?\\d*%?|\\+\\d+\\.?\\d*)");
		    Filter filter = new Filter();

		    for (String line : lines) {
		       String cleanedLine = line.replaceAll("[\\d\\.]+", "#");
		       cleanedLine = addLocalIfNeeded(cleanedLine);
		       Matcher matcher = pattern.matcher(line);

		       for (int i = 0; i < results.length(); i++) {
		           JSONObject item = results.getJSONObject(i);

		           if (item.getString("label").contains("Explicit")) {
		               if (item.has("entries")) {
		                   JSONArray entries = item.getJSONArray("entries");

		                   for (int j = 0; j < entries.length(); j++) {
		                      JSONObject entry = entries.getJSONObject(j);

		                      if (entry.has("text")) {
		                          String text = entry.getString("text");
		                          String cleanedText = text.replaceAll("[\\d\\.]+", "#");

		                          if (cleanedLine.equals(cleanedText)) {
		                              if (entry.has("id")) {
		                                  filter.setId(entry.getString("id"));

		                                  while (matcher.find()) {
		                                     String valueString = matcher.group();
		                                     double value = Double.parseDouble(valueString.replaceAll("[^\\d.]", ""));
		                                     filter.setValue(new Value(Math.round(value)));
		                                  }

		                                  filter.setDisabled(false);
		                                  filters.put(filter.toJson());
		                                  System.out.println("ID para " + text + ": " + entry.getString("id"));
		                              }
		                          }
		                      }
		                   }
		               }
		           }
		       }
		    }
		    
		    
		    queryJson.put("stats", new JSONArray().put(new JSONObject().put("type", "and").put("filters", filters)));

		    this.json = json;

		    System.out.println(json.toString());
		   
	   }
	   
	   public String addLocalIfNeeded(String input) {
		   Map<String, String> localNames = new HashMap<>();
		   localNames.put("increased Armour and Evasion", "increased Armour and Evasion (Local)");
		   
		   for (String name : localNames.keySet()) {
		       if (input.contains(name)) {
		           return input.replace(name, localNames.get(name));
		       }
		   }
		   return input;
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

