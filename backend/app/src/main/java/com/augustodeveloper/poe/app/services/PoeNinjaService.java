package com.augustodeveloper.poe.app.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.json.JSONArray;
import org.json.JSONObject;

import com.augustodeveloper.poe.app.controllers.PoeNinjaController;

public class PoeNinjaService {

	private String baseType;
	private String level;
	private String corrupted;
	private String synthesised;
	private String apiUrl;
	private List<Map<String, String>> gem;
	private List<String> explicitMods;
	private List<String> implicitMods;
	private List<String> enchantMods;

	private PoeNinjaController poeNinjaController;

	public PoeNinjaService() {

		this.gem = new ArrayList<>();
		this.explicitMods = new ArrayList<>();
		this.implicitMods = new ArrayList<>();
		this.enchantMods = new ArrayList<>();

	}

	public PoeNinjaService(PoeNinjaController poeNinjaController) {
		this.poeNinjaController = new PoeNinjaController();
		this.explicitMods = new ArrayList<>();
		this.implicitMods = new ArrayList<>();
		this.enchantMods = new ArrayList<>();
		this.gem = new ArrayList<>();
	}

	public Map<String, List<PoeNinjaService>> getEquipmentInfo() throws Exception {
		String apiContent = poeNinjaController.getStatsPoeNinja(apiUrl);
		JSONObject jsonResponse = new JSONObject(apiContent);
		JSONArray results = jsonResponse.getJSONArray("items");
		JSONArray jewelsResult = jsonResponse.getJSONArray("jewels");
		JSONArray flaskResult = jsonResponse.getJSONArray("flasks");

		Map<String, List<PoeNinjaService>> infoMap = new HashMap<>();

		for (int i = 0; i < results.length(); i++) {
			JSONObject item = results.getJSONObject(i);
			if (item.has("itemData")) {
				JSONObject itemData = item.getJSONObject("itemData");
				String inventoryId = itemData.getString("inventoryId");
				Boolean corrupted = itemData.getBoolean("corrupted");
				Boolean synthesised = itemData.getBoolean("synthesised");
				if (itemData.has("baseType") && itemData.has("explicitMods") && itemData.has("implicitMods")
						&& itemData.has("socketedItems")) {
					PoeNinjaService equipment = extractEquipmentInfo(itemData);
					equipment.setCorrupted(corrupted.toString());
					equipment.setSynthesised(synthesised.toString());
					String key = inventoryId;
					infoMap.computeIfAbsent(key, k -> new ArrayList<>()).add(equipment);
				}
			}
		}

		addJewelInfoToList(jewelsResult, infoMap);
		addFlasksInfoToList(flaskResult, infoMap);

		
		
		return infoMap;
	}

	private void addFlasksInfoToList(JSONArray flaskResult, Map<String, List<PoeNinjaService>> infoMap) {
		for (int i = 0; i < flaskResult.length(); i++) {
			JSONObject itemFlask = flaskResult.getJSONObject(i);
			if (itemFlask.has("itemData")) {
				JSONObject itemData = itemFlask.getJSONObject("itemData");
				String inventoryId = itemData.getString("inventoryId");
				if (itemData.has("baseType") && itemData.has("explicitMods")) {
					PoeNinjaService jewelsInfo = extractJewelInfo(itemData);
					infoMap.computeIfAbsent(inventoryId, k -> new ArrayList<>()).add(jewelsInfo);
				}
			}
		}
	}

	private void addJewelInfoToList(JSONArray jewelsResult, Map<String, List<PoeNinjaService>> infoMap) {
		for (int j = 0; j < jewelsResult.length(); j++) {
			JSONObject itemJewel = jewelsResult.getJSONObject(j);
			if (itemJewel.has("itemData")) {
				JSONObject itemData = itemJewel.getJSONObject("itemData");
				String name = itemData.getString("inventoryId");
				if (itemData.has("baseType") && itemData.has("explicitMods")) {
					PoeNinjaService jewelsInfo = extractJewelInfo(itemData);
					infoMap.computeIfAbsent(name, k -> new ArrayList<>()).add(jewelsInfo);
				}
			}
		}
	}

	private void extractModsInfo(JSONObject itemData, PoeNinjaService equipment) {
		addModsToList(itemData, "explicitMods", equipment.getExplicitMods());
		addModsToList(itemData, "implicitMods", equipment.getImplicitMods());
		addModsToList(itemData, "enchantMods", equipment.getEnchantMods());

	}

	private PoeNinjaService extractJewelInfo(JSONObject itemData) {
		PoeNinjaService equipment = new PoeNinjaService(this.poeNinjaController);
		equipment.setBaseType(itemData.getString("baseType"));
		extractModsInfo(itemData, equipment);
		return equipment;
	}

	private PoeNinjaService extractEquipmentInfo(JSONObject itemData) {
		PoeNinjaService equipment = new PoeNinjaService(this.poeNinjaController);
		equipment.setBaseType(itemData.getString("baseType"));
		extractModsInfo(itemData, equipment);

		List<Map<String, String>> gemList = new ArrayList<>();
		addGemsToList(itemData, gemList);
		equipment.setGem(gemList);

		return equipment;
	}

	private void addModsToList(JSONObject itemData, String modType, List<String> modList) {
		JSONArray mods = itemData.getJSONArray(modType);
		for (int i = 0; i < mods.length(); i++) {
			modList.add(mods.getString(i));
		}
	}

	private void addGemsToList(JSONObject itemData, List<Map<String, String>> gemList) {
		JSONArray socketedGems = itemData.getJSONArray("socketedItems");
		for (int l = 0; l < socketedGems.length(); l++) {
			JSONObject socketedItem = socketedGems.getJSONObject(l);
			String typeLine = socketedItem.getString("typeLine");
			Map<String, String> gemDetails = extractGemDetails(socketedItem, typeLine);
			gemList.add(gemDetails);
		}
	}

	private Map<String, String> extractGemDetails(JSONObject socketedItem, String typeLine) {
		Map<String, String> gemDetails = new HashMap<>();
		gemDetails.put("typeLine", typeLine);

		if (socketedItem.has("properties")) {
			JSONArray propertiesGems = socketedItem.getJSONArray("properties");
			for (int m = 0; m < propertiesGems.length(); m++) {
				JSONObject propertiesInfo = propertiesGems.getJSONObject(m);
				if (propertiesInfo.get("name").equals("Level")) {
					gemDetails.put("gemLevel", extractGemLevel(propertiesInfo));
				} else if (propertiesInfo.get("name").equals("Quality")) {
					gemDetails.put("quality", extractGemQuality(propertiesInfo));
				}
			}
		}

		return gemDetails;
	}

	private String extractGemLevel(JSONObject propertiesInfo) {
		JSONArray values = propertiesInfo.getJSONArray("values");
		if (values.length() > 0) {
			JSONArray valueArray = values.getJSONArray(0);
			if (valueArray.length() > 0) {
				return valueArray.getString(0);
			}
		}
		return null;
	}

	private String extractGemQuality(JSONObject propertiesInfo) {
		JSONArray values = propertiesInfo.getJSONArray("values");
		if (values.length() > 0) {
			JSONArray valueArray = values.getJSONArray(0);
			if (valueArray.length() > 0) {
				return valueArray.getString(0);
			}
		}
		return null;
	}

	public JSONObject toJson() {
		JSONObject json = new JSONObject();
		json.put("baseType", this.baseType);
		json.put("level", this.level);
		json.put("corrupted", this.corrupted);
		json.put("synthesised", this.synthesised);

		JSONArray gemJson = new JSONArray();
		for (Map<String, String> gem : this.gem) {
			gemJson.put(gem);
		}
		json.put("gem", gemJson);

		JSONArray explicitModsJson = new JSONArray();
		for (String explicitMod : this.explicitMods) {
			explicitModsJson.put(explicitMod);
		}
		json.put("explicitMods", explicitModsJson);

		JSONArray implicitModsJson = new JSONArray();
		for (String implicitMod : this.implicitMods) {
			implicitModsJson.put(implicitMod);
		}
		json.put("implicitMods", implicitModsJson);

		JSONArray enchantModsJson = new JSONArray();
		for (String enchantMod : this.enchantMods) {
			enchantModsJson.put(enchantMod);
		}
		json.put("enchantMods", enchantModsJson);

		return json;
	}

	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		sb.append("\n Info {\n");
		sb.append("\tbaseType= '").append(baseType).append("'\n");
		sb.append("\tGems = ").append(gem).append("\n");
		sb.append("\texplicitMods = ").append(explicitMods).append("\n");
		sb.append("\timplicitMods = ").append(implicitMods).append("\n");
		sb.append("\tenchantMods = ").append(enchantMods).append("\n");
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

	public List<Map<String, String>> getGem() {
        return gem;
    }

    public void setGem(List<Map<String, String>> gem) {
        this.gem = gem;
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

	public List<String> getEnchantMods() {
		return enchantMods;
	}

	public void setEnchantMods(List<String> enchantMods) {
		this.enchantMods = enchantMods;
	}

	public String getApiUrl() {
		return apiUrl;
	}

	public void setApiUrl(String apiUrl) {
		this.apiUrl = apiUrl;
	}

}
