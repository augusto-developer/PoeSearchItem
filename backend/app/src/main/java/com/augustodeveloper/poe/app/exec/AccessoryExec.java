package com.augustodeveloper.poe.app.exec;

import java.util.HashMap;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.augustodeveloper.poe.app.entities.Filter;
import com.augustodeveloper.poe.app.entities.Miscellaneous;
import com.augustodeveloper.poe.app.entities.Type;
import com.augustodeveloper.poe.app.entities.Value;
import com.augustodeveloper.poe.app.services.PoeTradeService;

public class AccessoryExec {

	
	private JSONObject json;

	private PoeTradeService poeTradeService;

    public AccessoryExec() {
        this.poeTradeService = new PoeTradeService();
    }

	public String run() throws Exception {
		JSONArray filters = new JSONArray();
		JSONObject json = new JSONObject();
		Filter filter = new Filter();
		Miscellaneous misc = new Miscellaneous();
		
		Type typeFilter = new Type("Astral Plate", "online");

		String responseBody = poeTradeService.getStats();

		JSONObject jsonResponse = new JSONObject(responseBody);
		JSONArray results = jsonResponse.getJSONArray("result");

		
		json.put("query", typeFilter.toJson());
		JSONObject queryJson = json.getJSONObject("query");
		queryJson.put("status", new JSONObject().put("option", "online"));
		
		misc.setSynthesised_item(new Value(true));
		misc.setCorrupted(new Value(true));
		misc.setDisabled(false);
		
		
		queryJson.put("filters", new JSONObject().put("misc_filters", new JSONObject().put("filters", misc.toJson())));
		
		
		
		filter.setId("explicit.stat_691932474");
		filter.setValue(new Value(Math.round(20)));
		filter.setDisabled(false);
		filters.put(filter.toJson());

		queryJson.put("stats", new JSONArray().put(new JSONObject().put("type", "and").put("filters", filters)));

		this.json = json;

		System.out.println(json.toString());
		
		return json.toString();

	}

	public Map<String, String> generateLocalNamesMap(JSONArray results) {
		Map<String, String> localNames = new HashMap<>();

		for (int i = 0; i < results.length(); i++) {
			JSONObject item = results.getJSONObject(i);

			if (item.getString("label").contains("Explicit")) {
				if (item.has("entries")) {
					JSONArray entries = item.getJSONArray("entries");

					for (int j = 0; j < entries.length(); j++) {
						JSONObject entry = entries.getJSONObject(j);

						if (entry.has("text")) {
							String text = entry.getString("text");

							if (text.contains("(Local)")) {
								String nameWithoutLocal = text.replace("(Local)", "").trim();
								localNames.put(nameWithoutLocal, text);
							}
						}
					}
				}
			}
		}

		return localNames;
	}

	public String addLocalIfNeeded(String input, Map<String, String> localNames) {
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

	public void setJson(JSONObject json) {
		this.json = json;
	}
	
	

}
