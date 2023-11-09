package com.augustodeveloper.poe.app.entities.itens;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.augustodeveloper.poe.app.services.PoeNinjaService;

public class Equipment {

	private String baseType;
	private String level;
	private String corrupted;
	private String synthesised;
	private List<String> gem;
	private List<String> explicitMods;
	private List<String> implicitMods;
	

	private PoeNinjaService poeNinjaService;

	public Equipment() {
		this.explicitMods = new ArrayList<>();
		this.implicitMods = new ArrayList<>();
		this.gem = new ArrayList<>();
		this.poeNinjaService = new PoeNinjaService();
	}

	public Map<String, List<Equipment>> getEquipmentInfo(String apiUrl) throws Exception {
		String apiContent = poeNinjaService.getStatsPoeNinja(apiUrl);
		JSONObject jsonResponse = new JSONObject(apiContent);
		JSONArray results = jsonResponse.getJSONArray("items");
		JSONArray jewelsResult = jsonResponse.getJSONArray("jewels");
				

		Map<String, List<Equipment>> infoMap = new HashMap<>();

		for (int i = 0; i < results.length(); i++) {
			JSONObject item = results.getJSONObject(i);
			if (item.has("itemData")) {
				JSONObject itemData = item.getJSONObject("itemData");
				String inventoryId = itemData.getString("inventoryId");
				Boolean corrupted = itemData.getBoolean("corrupted");
				Boolean synthesised = itemData.getBoolean("synthesised");
				if (itemData.has("baseType") && itemData.has("explicitMods") && itemData.has("implicitMods") && itemData.has("socketedItems")) {
					Equipment equipment = extractEquipmentInfo(itemData);
					equipment.setCorrupted(corrupted.toString());
                    equipment.setSynthesised(synthesised.toString());
                    infoMap.computeIfAbsent(inventoryId, k -> new ArrayList<>()).add(equipment);
                    infoMap.computeIfAbsent(corrupted.toString(), k -> new ArrayList<>()).add(equipment);
                    infoMap.computeIfAbsent(synthesised.toString(), k -> new ArrayList<>()).add(equipment);
				}
			}
		}
		
		addJewelInfoToList(jewelsResult, infoMap);

		return infoMap;
	}
	
	private void addJewelInfoToList(JSONArray jewelsResult, Map<String, List<Equipment>> infoMap) {
	    for(int j = 0; j < jewelsResult.length(); j++) {
	        JSONObject itemJewel = jewelsResult.getJSONObject(j);
	        if (itemJewel.has("itemData")) {
	            JSONObject itemData = itemJewel.getJSONObject("itemData");
	            String name = itemData.getString("name");
	            if(itemData.has("baseType") && itemData.has("explicitMods") && itemData.has("implicitMods")) {
	                Equipment jewelsInfo = extractJewelInfo(itemData);
	                infoMap.computeIfAbsent(name, k -> new ArrayList<>()).add(jewelsInfo);
	            }
	        }
	    }
	}
	
	private void extractModsInfo(JSONObject itemData, Equipment equipment) {
	    addModsToList(itemData, "explicitMods", equipment.getExplicitMods());
	    addModsToList(itemData, "implicitMods", equipment.getImplicitMods());
	    
	}
	
	private Equipment extractJewelInfo(JSONObject itemData) {
		Equipment equipment = new Equipment();	
		equipment.setBaseType(itemData.getString("baseType"));
		extractModsInfo(itemData, equipment);		
		return equipment;
	}

	private Equipment extractEquipmentInfo(JSONObject itemData) {
		Equipment equipment = new Equipment();
		equipment.setBaseType(itemData.getString("baseType"));
		extractModsInfo(itemData, equipment);
		addGemsToList(itemData, equipment.getGemName());
		return equipment;
	}

	private void addModsToList(JSONObject itemData, String modType, List<String> modList) {
		JSONArray mods = itemData.getJSONArray(modType);
		for (int i = 0; i < mods.length(); i++) {
			modList.add(mods.getString(i));
		}
	}

	private void addGemsToList(JSONObject itemData, List<String> gemList) {
		JSONArray socketedGems = itemData.getJSONArray("socketedItems");
		for (int l = 0; l < socketedGems.length(); l++) {
			JSONObject socketedItem = socketedGems.getJSONObject(l);
			String typeLine = socketedItem.getString("typeLine");
			gemList.add(extractGemDetails(socketedItem, typeLine));
		}
	}

	private String extractGemDetails(JSONObject socketedItem, String typeLine) {
		if (socketedItem.has("properties")) {
			JSONArray propertiesGems = socketedItem.getJSONArray("properties");
			for (int m = 0; m < propertiesGems.length(); m++) {
				JSONObject propertiesInfo = propertiesGems.getJSONObject(m);
				if (propertiesInfo.get("name").equals("Level")) {
					return extractGemLevel(propertiesInfo, typeLine);
				} else if (propertiesInfo.get("name").equals("Quality")) {
					return extractGemQuality(propertiesInfo, typeLine);
				}
			}
		}
		return typeLine;
	}

	private String extractGemLevel(JSONObject propertiesInfo, String typeLine) {
		JSONArray values = propertiesInfo.getJSONArray("values");
		if (values.length() > 0) {
			JSONArray valueArray = values.getJSONArray(0);
			if (valueArray.length() > 0) {
				String gemLevel = valueArray.getString(0);
				return typeLine + " gemLevel: " + gemLevel;
			}
		}
		return typeLine;
	}

	private String extractGemQuality(JSONObject propertiesInfo, String typeLine) {
		JSONArray values = propertiesInfo.getJSONArray("values");
		if (values.length() > 0) {
			JSONArray valueArray = values.getJSONArray(0);
			if (valueArray.length() > 0) {
				String gemQuality = valueArray.getString(0);
				return typeLine + ", Quality: " + gemQuality;
			}
		}
		return typeLine;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n Info {\n");
		sb.append("\tbaseType= '").append(baseType).append("'\n");
		sb.append("\tGems = ").append(gem).append("\n");
		sb.append("\texplicitMods = ").append(explicitMods).append("\n");
		sb.append("\timplicitMods = ").append(implicitMods).append("\n");
		sb.append("\tcorrupted = ").append(corrupted).append("\n");
		sb.append("\tsynthesised = ").append(synthesised).append("\n");
		sb.append("}");
		return sb.toString();
	}

	public String getBaseType() {
		return baseType;
	}

	public void setBaseType(String baseType) {
		this.baseType = baseType;
	}

	public List<String> getExplicitMods() {
		return explicitMods;
	}

	public void setExplicitMods(List<String> explicitMods) {
		this.explicitMods = explicitMods;
	}

	public List<String> getImplicitMods() {
		return implicitMods;
	}

	public void setImplicitMods(List<String> implicitMods) {
		this.implicitMods = implicitMods;
	}

	public List<String> getGemName() {
		return gem;
	}

	public void setGemName(List<String> typeLine) {
		this.gem = typeLine;
	}

	public String getLevel() {
		return level;
	}

	public String getCorrupted() {
		return corrupted;
	}

	public void setCorrupted(String corrupted) {
		this.corrupted = corrupted;
	}

	public String getSynthesised() {
		return synthesised;
	}

	public void setSynthesised(String synthesised) {
		this.synthesised = synthesised;
	}
	
	
}
