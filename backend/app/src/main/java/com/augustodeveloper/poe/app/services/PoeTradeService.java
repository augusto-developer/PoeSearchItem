package com.augustodeveloper.poe.app.services;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.json.JSONArray;
import org.json.JSONObject;

import com.augustodeveloper.poe.app.controllers.PoeNinjaController;
import com.augustodeveloper.poe.app.controllers.PoeTradeController;
import com.augustodeveloper.poe.app.entities.Type;

public class PoeTradeService {

	private JSONObject json;
	private PoeNinjaService equipments;
	private String apiUrlPoeNinja;
	private List<String> equipmentTypes = Arrays.asList("BodyArmour", "Amulet", "Boots", "Gloves");
//	, "Amulet", "Boots", "Gloves", "Belt", "Ring",
//	"Ring2", "Offhand", "Helm", "Weapon"
	private List<String> tradeLinks = new ArrayList<>();
	

	public PoeTradeService(String apiUrlPoeNinja) {
		this.apiUrlPoeNinja = apiUrlPoeNinja;
		new PoeTradeController();
		this.equipments = new PoeNinjaService(new PoeNinjaController());
	}

	public void run(Consumer<String> linkCallback) throws Exception {
		PoeTradeController poeTradeController = new PoeTradeController();

		// Resgatar o JSON do PoeNinjaService pra acesso
		equipments.setApiUrl(apiUrlPoeNinja);
		Map<String, List<PoeNinjaService>> equipmentInfo = equipments.getEquipmentInfo();

		// Converta o equipamentoInfo para um objeto JSON
		JSONObject equipmentInfoJson = convertEquipmentInfoToJson(equipmentInfo);

		Pattern valuePattern = Pattern.compile("[\\d\\.]+");

		JSONArray filters = new JSONArray();

		// Acesse a API do PoeTrade
		String poeTradeAPI = poeTradeController.getStats();
		JSONObject jsonResponse = new JSONObject(poeTradeAPI);
		JSONArray results = jsonResponse.getJSONArray("result");

		// Percorra o objeto JSON
		for (String key : equipmentInfoJson.keySet()) {

			if (equipmentTypes.contains(key)) {
				JSONArray valueJson = equipmentInfoJson.getJSONArray(key);

				for (int i = 0; i < valueJson.length(); i++) {
					JSONObject poeNinjaServiceJson = valueJson.getJSONObject(i);

					// Crie um novo JSON para cada tipo de equipamento
					JSONObject json = new JSONObject();
					String baseType = poeNinjaServiceJson.getString("baseType");
					Type typeFilter = new Type(baseType, "online");
					json.put("query", typeFilter.toJson());
					JSONObject queryJson = json.getJSONObject("query");
					queryJson.put("status", new JSONObject().put("option", "online"));

					// Limpe a variável filters
					filters = new JSONArray();

					if (poeNinjaServiceJson.has("explicitMods")) {
						JSONArray explicitMods = poeNinjaServiceJson.getJSONArray("explicitMods");
						// Crie um Map para armazenar os nomes dos IDs e os IDs
						Map<String, String> idMap = new HashMap<>();

						// Gere o mapa de nomes locais
						Map<String, String> localNames = generateLocalNamesMap(results);

						for (int j = 0; j < explicitMods.length(); j++) {

							String text = explicitMods.getString(j);

							// Substitua os valores numéricos por "#"
							String cleanedText = text.replaceAll("[\\d\\.]+", "#");

							// Adicione "(Local)" ao início do texto se ele contiver "(Local)"
							cleanedText = addLocalIfNeeded(cleanedText, localNames);

							// Extraia o valor da linha
							Matcher valueMatcher = valuePattern.matcher(text);
							String value = valueMatcher.find() ? valueMatcher.group() : "0"; // valor padrao caso nao
																								// ache o valor.

							for (int k = 0; k < results.length(); k++) {
								JSONObject item = results.getJSONObject(k);

								if (item.getString("label").contains("Explicit")) {
									if (item.has("entries")) {
										JSONArray entries = item.getJSONArray("entries");

										for (int l = 0; l < entries.length(); l++) {
											JSONObject entry = entries.getJSONObject(l);

											if (entry.has("text")) {
												String entryText = entry.getString("text");
												String cleanedEntryText = entryText.replaceAll("[\\d\\.]+", "#");

												// Adicione "(Local)" ao início do entryText se ele contiver "(Local)"
												cleanedEntryText = addLocalIfNeeded(cleanedEntryText, localNames);

												if (cleanedText.equals(cleanedEntryText)) {
													if (entry.has("id")) {
														String id = entry.getString("id");

														// Verifique se o ID já foi adicionado
														if (!idMap.containsKey(cleanedText)) {
															// System.out.println("ID para " + cleanedText + ": " + id);

															// Adicione o nome do ID e o ID ao Map
															idMap.put(cleanedText, id);

															// Crie um novo objeto JSONObject para o ID e adicione ao
															// array de filtros
															JSONObject filter = new JSONObject();
															filter.put("id", id);
															filter.put("value", new JSONObject().put("min", value));
															filter.put("disabled", false);
															filters.put(filter);
														}
													}
												}
											}
										}
									}
									queryJson.put("stats", new JSONArray()
											.put(new JSONObject().put("type", "and").put("filters", filters)));
								}
							}

						}
						if (poeNinjaServiceJson.has("implicitMods")) {
							JSONArray implicitMods = poeNinjaServiceJson.getJSONArray("implicitMods");

							for (int j = 0; j < implicitMods.length(); j++) {

								String text = implicitMods.getString(j);

								// Substitua os valores numéricos por "#"
								String cleanedText = text.replaceAll("[\\d\\.]+", "#");

								// Adicione "(Local)" ao início do texto se ele contiver "(Local)"
								cleanedText = addLocalIfNeeded(cleanedText, localNames);

								for (int k = 0; k < results.length(); k++) {
									JSONObject item = results.getJSONObject(k);

									if (item.getString("label").contains("Implicit")) {
										if (item.has("entries")) {
											JSONArray entries = item.getJSONArray("entries");

											for (int l = 0; l < entries.length(); l++) {
												JSONObject entry = entries.getJSONObject(l);

												if (entry.has("text")) {
													String entryText = entry.getString("text");
													String cleanedEntryText = entryText.replaceAll("[\\d\\.]+", "#");

													// Adicione "(Local)" ao início do entryText se ele contiver
													// "(Local)"
													cleanedEntryText = addLocalIfNeeded(cleanedEntryText, localNames);

													if (cleanedText.equals(cleanedEntryText)) {
														if (entry.has("id")) {
															String id = entry.getString("id");

															// Verifique se o ID já foi adicionado
															if (!idMap.containsKey(cleanedText)) {
																// System.out.println("ID para " + cleanedText + ": " +
																// id);

																// Adicione o nome do ID e o ID ao Map
																idMap.put(cleanedText, id);

																// Crie um novo objeto JSONObject para o ID e adicione
																// ao array de filtros
																JSONObject filter = new JSONObject();
																filter.put("id", id);
																filter.put("value", new JSONObject().put("min", 0));
																filter.put("disabled", false);
																filters.put(filter);

															}
														}
													}
												}
											}
										}
									}
								}
							}
							queryJson.put("stats",
									new JSONArray().put(new JSONObject().put("type", "and").put("filters", filters)));

						}
					}
					JSONObject miscFilters = new JSONObject();

					if (poeNinjaServiceJson.has("corrupted")) {
						String corrupted = poeNinjaServiceJson.getString("corrupted");
						miscFilters.put("corrupted", new JSONObject().put("option", corrupted.equals("true")));
						queryJson.put("filters",
								new JSONObject().put("misc_filters", new JSONObject().put("filters", miscFilters)));

					}

					if (poeNinjaServiceJson.has("synthesised")) {
						String synthesised = poeNinjaServiceJson.getString("synthesised");
						miscFilters.put("synthesised_item", new JSONObject().put("option", synthesised.equals("true")));
						queryJson.put("filters",
								new JSONObject().put("misc_filters", new JSONObject().put("filters", miscFilters)));

					}

		
					
					String link = poeTradeController.makeRequest(json.toString());
		            tradeLinks.add(link);
		            // Modifique aqui: inclua o nome do equipamento no link
	                String linkWithEquipmentName = key + " - " + link;
		          
	                linkCallback.accept(linkWithEquipmentName);

					json = new JSONObject();
				}

			}

		}

	}

	public void addFilterIfExists(JSONObject poeNinjaServiceJson, String fieldName, String id) {
		if (poeNinjaServiceJson.has(fieldName)) {
			JSONArray filters = new JSONArray();
			String value = poeNinjaServiceJson.getString(fieldName);

			JSONObject filter = new JSONObject();
			filter.put("id", id);
			filter.put("value", new JSONObject().put("option", value.equals("true")));
			filter.put("disabled", false);

			filters.put(filter);
		}
	}

	private JSONObject convertEquipmentInfoToJson(Map<String, List<PoeNinjaService>> equipmentInfo) {

		JSONObject equipmentInfoJson = new JSONObject();
		for (Map.Entry<String, List<PoeNinjaService>> entry : equipmentInfo.entrySet()) {
			String key = entry.getKey();
			List<PoeNinjaService> value = entry.getValue();

			JSONArray valueJson = new JSONArray();
			for (PoeNinjaService poeNinjaService : value) {
				valueJson.put(poeNinjaService.toJson());
			}

			equipmentInfoJson.put(key, valueJson);

		}

		return equipmentInfoJson;
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
				return localNames.get(name);
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

	public List<String> getEquipmentTypes() {
		return equipmentTypes;
	}

	public List<String> getTradeLinks() {
		return tradeLinks;
	}

	public void setTradeLinks(List<String> tradeLinks) {
		this.tradeLinks = tradeLinks;
	}

}
