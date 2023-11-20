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
	private JSONObject equipmentInfoJson;
	private PoeNinjaService equipments;
	private String apiUrlPoeNinja;
	private List<String> equipmentTypes = Arrays.asList("Helm", "BodyArmour", "Gloves", "Boots", "Belt", "Ring",
			"Ring2", "Amulet", "Weapon", "Offhand");
	private List<String> equipmentGemsTypes = Arrays.asList("Helm", "BodyArmour", "Gloves", "Boots", "Weapon",
			"Offhand");
	private List<String> tradeLinks = new ArrayList<>();
	private static final String DEFAULT_VALUE = null;
	private Map<String, String> gemLinks = new HashMap<>();
	private Map<String, Integer> processedItems = new HashMap<>();
	private PoeTradeController poeTradeController;

	public PoeTradeService(String apiUrlPoeNinja) {
		this.apiUrlPoeNinja = apiUrlPoeNinja;
		new PoeTradeController();
		this.equipments = new PoeNinjaService(new PoeNinjaController());
		this.poeTradeController = new PoeTradeController();
	}

	public void equipmentsTrade(Consumer<String> linkCallback) throws Exception {
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
		for (String key : equipmentTypes) {

			if (equipmentInfoJson.has(key)) {
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
							String value = valueMatcher.find() ? valueMatcher.group() : DEFAULT_VALUE; // valor padrao
																										// caso nao
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

	public void flaskTrade(Consumer<LinkAndIdSize> linkAndIdSizeCallback) throws Exception {
		// Converta o equipamentoInfo para um objeto JSON
		JSONObject equipmentInfoJson = prepareEquipmentInfoJson();

		Pattern valuePattern = Pattern.compile("[\\d\\.]+");

		JSONArray filters = new JSONArray();

		// Access API PoeTrade
		JSONArray results = getAccessApiPoeTrade();

		String baseTypeName = null;

		// Percorra o objeto JSON
		for (String key : equipmentInfoJson.keySet()) {

			if (key.contains("Flask")) {
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

					baseTypeName = poeNinjaServiceJson.getString("baseType");
					Map<String, String> idMap = new HashMap<>();
					if (poeNinjaServiceJson.has("explicitMods")) {
						JSONArray explicitMods = poeNinjaServiceJson.getJSONArray("explicitMods");
//								// Crie um Map para armazenar os nomes dos IDs e os IDs
//								Map<String, String> idMap = new HashMap<>();

						for (int j = 0; j < explicitMods.length(); j++) {

							String text = explicitMods.getString(j);

							String cleanedText = null;

							if (text.equals("(.*?) reduced Duration")) {
								cleanedText = text.replaceAll("(.*?) reduced Duration", "(.*?) increased Duration");
								;
							} else {
								// Substitua os valores numéricos por "#"
								cleanedText = text.replaceAll("[\\d\\.]+", "#");
							}

							Matcher valueMatcher = valuePattern.matcher(text);
							String value = valueMatcher.find() ? valueMatcher.group() : DEFAULT_VALUE;

							for (int k = 0; k < results.length(); k++) {
								JSONObject item = results.getJSONObject(k);
								JSONObject filter = new JSONObject();
								if (item.getString("label").contains("Explicit")) {
									if (item.has("entries")) {
										JSONArray entries = item.getJSONArray("entries");

										for (int l = 0; l < entries.length(); l++) {
											JSONObject entry = entries.getJSONObject(l);

											if (cleanedText.equals(entry.getString("text"))) {
												if (entry.has("text") && entry.has("id")) {
													String id = entry.getString("id");
													if (!idMap.containsKey(cleanedText)) {
														idMap.put(cleanedText, id);

														filter.put("id", id);
														filter.put("value",
																new JSONObject().put("min", value).put("max", value));
														filter.put("disabled", false);
														filters.put(filter);

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

					int idSize = countExplicitModsInPassiveJewels("Flask");
					System.out.println(json.toString());

					String link = poeTradeController.makeRequest(json.toString());
					tradeLinks.add(link);
					// Modifique aqui: inclua o nome do equipamento no link
					String linkWithEquipmentName = baseTypeName + " - " + link;

					linkAndIdSizeCallback.accept(new LinkAndIdSize(linkWithEquipmentName, idSize));

					// System.out.println(equipmentInfoJson);

					json = new JSONObject();
				}
			}
		}
	}

	public void jewelTrade(Consumer<LinkAndIdSize> linkAndIdSizeCallback) throws Exception {

		// Converta o equipamentoInfo para um objeto JSON
		JSONObject equipmentInfoJson = prepareEquipmentInfoJson();

		Pattern valuePattern = Pattern.compile("[\\d\\.]+");
		Pattern patternForbidden = Pattern.compile("Allocates (.*?) if");
		Pattern patternCluster = Pattern.compile("Added Small Passive Skills grant:(.*?)");

		JSONArray filters = new JSONArray();

		// Access API PoeTrade
		JSONArray results = getAccessApiPoeTrade();

		String baseTypeName = null;

		// Percorra o objeto JSON
		for (String key : equipmentInfoJson.keySet()) {

			if (key.contains("PassiveJewels")) {
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

					baseTypeName = poeNinjaServiceJson.getString("baseType");
					Map<String, String> idMap = new HashMap<>();
					if (poeNinjaServiceJson.has("explicitMods")) {
						JSONArray explicitMods = poeNinjaServiceJson.getJSONArray("explicitMods");

						for (int j = 0; j < explicitMods.length(); j++) {

							String textProfile = explicitMods.getString(j);

							String cleanedText = null;
							String allocatedName = null;

							Matcher matcher = patternForbidden.matcher(textProfile);
							if (matcher.find()) {
								allocatedName = matcher.group(1);
								cleanedText = textProfile.replaceAll("Allocates(.*?)if", "Allocates # if");
							} else {
								cleanedText = textProfile.replaceAll("\\d+(\\.\\d+)?", "#");
							}

							if (textProfile.matches("^(\\d+\\s)?Added Passive Skill.*")) {
								cleanedText = textProfile;
							}

							// Se o texto é "Historic", continue para a próxima iteração
							if (textProfile.equals("Historic")) {
								continue;
							}

							if (textProfile.contains("Commanded leadership over")) {

								// Substitui o número entre "over" e "warriors" por "#"
								cleanedText = textProfile.replaceFirst("over\\s+\\d+\\s+warriors", "over # warriors");

								// Ignora tudo após o "\n"
								int index = cleanedText.indexOf("\n");
								if (index != -1) {
									cleanedText = cleanedText.substring(0, index);
								}
							}

							Matcher valueMatcher = valuePattern.matcher(textProfile);
							String value = valueMatcher.find() ? valueMatcher.group() : DEFAULT_VALUE;

							if (cleanedText.equals("Allocates # if you have the matching modifier on Forbidden Flame")
									|| cleanedText.equals(
											"Allocates # if you have the matching modifier on Forbidden Flesh")) {
								filters = forbiddenJewels(cleanedText, allocatedName);
							} else {

								for (int k = 0; k < results.length(); k++) {
									JSONObject item = results.getJSONObject(k);
									JSONObject filter = new JSONObject();
									if (item.getString("label").contains("Explicit")) {
										if (item.has("entries")) {
											JSONArray entries = item.getJSONArray("entries");

											for (int l = 0; l < entries.length(); l++) {
												JSONObject entry = entries.getJSONObject(l);

												if (cleanedText.equals(entry.getString("text"))) {
													if (entry.has("text") && entry.has("id")) {
														String id = entry.getString("id");
														if (!idMap.containsKey(cleanedText)) {
															idMap.put(cleanedText, id);

															filter.put("id", id);
															filter.put("value", new JSONObject().put("min", value)
																	.put("max", value));
															filter.put("disabled", false);
															filters.put(filter);

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
						}
						if (poeNinjaServiceJson.has("enchantMods")) {
							JSONArray implicitMods = poeNinjaServiceJson.getJSONArray("enchantMods");
							for (int j = 0; j < implicitMods.length(); j++) {
								String text = implicitMods.getString(j);

								String allocatedName = null;
								String cleanedText = null;
								Integer idOpt = null;

								Pattern pattern = Pattern.compile("grant:(.*)", Pattern.DOTALL);
								Matcher matcher = pattern.matcher(text);
								if (matcher.find()) {
									allocatedName = matcher.group(1);
									allocatedName = allocatedName.replaceAll("Added Small Passive Skills grant:", "");
									//allocatedName = allocatedName.replace("\n ", "\\n");
									allocatedName = allocatedName.trim();
									cleanedText = "Added Small Passive Skills grant: #";
									System.out.println(allocatedName);
									System.out.println("CT:" + cleanedText);

									idOpt = clusterJewels(cleanedText, allocatedName);
								} else {
									cleanedText = text.replaceAll("[\\d\\.]+", "#");
								}

								Matcher valueMatcher = valuePattern.matcher(text);
								String value = valueMatcher.find() ? valueMatcher.group() : DEFAULT_VALUE;

								for (int k = 0; k < results.length(); k++) {
									JSONObject item = results.getJSONObject(k);

									if (item.getString("label").contains("Enchant")) {
										if (item.has("entries")) {
											JSONArray entries = item.getJSONArray("entries");

											for (int l = 0; l < entries.length(); l++) {
												JSONObject entry = entries.getJSONObject(l);

												if (entry.has("text")) {
													String entryText = entry.getString("text");
													String cleanedEntryText = entryText.replaceAll("[\\d\\.]+", "#");

													if (cleanedText.equals(cleanedEntryText)) {
														if (entry.has("id")) {
															String id = entry.getString("id");
															JSONObject filter = new JSONObject();
															if (id.equals("enchant.stat_3948993189")) {
																filter.put("id", id);
																filter.put("value",
																		new JSONObject().put("option", idOpt));

																filter.put("disabled", false);
																filters.put(filter);
															} else if (!idMap.containsKey(cleanedText)) {
																// System.out.println("ID para " + cleanedText + ":
																// " +
																// id);

																// Adicione o nome do ID e o ID ao Map
																idMap.put(cleanedText, id);

																// Crie um novo objeto JSONObject para o ID e
																// adicione
																// ao array de filtros

																filter.put("id", id);
																filter.put("value", new JSONObject().put("min", value)
																		.put("max", value));
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
					if (poeNinjaServiceJson.has("implicitMods")) {
						JSONArray implicitMods = poeNinjaServiceJson.getJSONArray("implicitMods");

						for (int j = 0; j < implicitMods.length(); j++) {

							String text = implicitMods.getString(j);

							// Substitua os valores numéricos por "#"
							String cleanedText = text.replaceAll("[\\d\\.]+", "#");

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
															filter.put("value", new JSONObject().put("min", "0"));
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
				int idSize = countExplicitModsInPassiveJewels("PassiveJewels");
				System.out.println(json.toString());

				String link = poeTradeController.makeRequest(json.toString());
				tradeLinks.add(link);
				// Modifique aqui: inclua o nome do equipamento no link
				String linkWithEquipmentName = baseTypeName + " - " + link;

				linkAndIdSizeCallback.accept(new LinkAndIdSize(linkWithEquipmentName, idSize));

				// System.out.println(equipmentInfoJson);

				json = new JSONObject();
			}
		}
	}

	}

	public int countExplicitModsInPassiveJewels(String typeEquipment) {
		int count = 0;
		JSONArray infoCount = equipmentInfoJson.getJSONArray(typeEquipment);
		for (int i = 0; i < infoCount.length(); i++) {
			JSONObject typeEquip = infoCount.getJSONObject(i);
			if (typeEquip.has("explicitMods")) {
				count++;
			}
		}
		return count;
	}

	public JSONObject prepareEquipmentInfoJson() throws Exception {
		equipments.setApiUrl(apiUrlPoeNinja);
		Map<String, List<PoeNinjaService>> equipmentInfo = equipments.getEquipmentInfo();
		return equipmentInfoJson = convertEquipmentInfoToJson(equipmentInfo);
	}

	private JSONArray getAccessApiPoeTrade() {
		String poeTradeAPI = poeTradeController.getStats();
		JSONObject jsonResponse = new JSONObject(poeTradeAPI);
		JSONArray results = jsonResponse.getJSONArray("result");
		return results;
	}

	public Integer clusterJewels(String cleanedText, String allocatedName) {
		// Access API PoeTrade
		JSONArray results = getAccessApiPoeTrade();

		Integer idOpt = null;
		for (int k = 0; k < results.length(); k++) {
			JSONObject item = results.getJSONObject(k);

			if (item.getString("label").contains("Enchant")) {
				if (item.has("entries")) {
					JSONArray entries = item.getJSONArray("entries");
					for (int l = 0; l < entries.length(); l++) {
						JSONObject entry = entries.getJSONObject(l);

						if (entry.has("text") && entry.has("type") && entry.has("option")) {
							if (cleanedText.equals(entry.getString("text"))) {
								JSONObject option = entry.getJSONObject("option");
								if (option.has("options")) {
									JSONArray options = option.getJSONArray("options");
									for (int opt = 0; opt < options.length(); opt++) {
										JSONObject entryOption = options.getJSONObject(opt);
										if (entryOption.has("text") && entryOption.has("id")) {
											String textOpt = entryOption.getString("text");
											if (textOpt.equals(allocatedName)) {
												idOpt = entryOption.getInt("id");
												System.out.println("idOpt:" + idOpt);

											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return idOpt;
	}

	public JSONArray forbiddenJewels(String cleanedText, String allocatedName) {
		// Access API PoeTrade
		JSONArray results = getAccessApiPoeTrade();

		JSONArray filters = new JSONArray();
		Map<String, String> idMap = new HashMap<>();

		for (int k = 0; k < results.length(); k++) {
			JSONObject item = results.getJSONObject(k);
			JSONObject filter = new JSONObject();
			String id = null;
			if (cleanedText.equals("Allocates # if you have the matching modifier on Forbidden Flame")) {
				id = "explicit.stat_2460506030";
				filter.put("id", "explicit.stat_2460506030");
			} else if (cleanedText.equals("Allocates # if you have the matching modifier on Forbidden Flesh")) {
				id = "explicit.stat_1190333629";
				filter.put("id", "explicit.stat_1190333629");
			}
			if (item.getString("label").contains("Explicit")) {
				if (item.has("entries")) {
					JSONArray entries = item.getJSONArray("entries");

					for (int l = 0; l < entries.length(); l++) {
						JSONObject entry = entries.getJSONObject(l);

						if (entry.has("text") && entry.has("id") && entry.has("option")) {
							JSONObject option = entry.getJSONObject("option");
							if (option.has("options")) {
								JSONArray options = option.getJSONArray("options");
								for (int opt = 0; opt < options.length(); opt++) {
									JSONObject entryOption = options.getJSONObject(opt);
									if (entryOption.has("text") && entryOption.has("id")) {
										String textOpt = entryOption.getString("text");
										if (textOpt.equals(allocatedName)) {
											int idOpt = entryOption.getInt("id");
											if (!idMap.containsKey(cleanedText)) {
												idMap.put(cleanedText, id);
												filter.put("option", idOpt);
												filters.put(new JSONObject(filter.toString()));
											}
										}
									}
								}
							}
						}
					}
				}
			}
		}
		return filters;
	}

	public void gemTrade(Consumer<LinkAndIdSize> linkAndIdSizeCallback) throws Exception {
		PoeTradeController poeTradeController = new PoeTradeController();
		// Resgatar o JSON do PoeNinjaService pra acesso
		equipments.setApiUrl(apiUrlPoeNinja);
		Map<String, List<PoeNinjaService>> equipmentInfo = equipments.getEquipmentInfo();

		// Converta o equipamentoInfo para um objeto JSON
		JSONObject equipmentInfoJson = convertEquipmentInfoToJson(equipmentInfo);

		for (String key : equipmentInfoJson.keySet()) {
			if (equipmentGemsTypes.contains(key)) {
				JSONArray valueJson = equipmentInfoJson.getJSONArray(key);

				for (int i = 0; i < valueJson.length(); i++) {
					JSONObject poeNinjaServiceJson = valueJson.getJSONObject(i);

					// Obtenha o array de gemas
					JSONArray gemArray = poeNinjaServiceJson.getJSONArray("gem");

					// Se o array de gemas estiver vazio, continue para a próxima iteração
					if (gemArray.length() == 0) {
						continue;
					}

					for (int j = 0; j < gemArray.length(); j++) {

						JSONObject gemJson = gemArray.getJSONObject(j);

						// Crie um novo JSON para cada gema
						JSONObject json = new JSONObject();
						String gemName = gemJson.getString("typeLine");

						Type typeFilter = new Type(gemName, "online");
						json.put("query", typeFilter.toJson());

						JSONObject queryJson = json.getJSONObject("query");
						queryJson.put("status", new JSONObject().put("option", "online"));

						String typeLine = gemJson.getString("typeLine");

						// Verifica se o nome da gema já foi processado
						if (processedItems.containsKey(gemName)) {
							int count = processedItems.get(gemName);
							gemName += " (" + (count + 1) + ")";
							processedItems.put(gemName, count + 1);
						} else {
							processedItems.put(gemName, 1);
						}

						String discriminator = "";

						if (typeLine.contains("Divergent")) {
							typeLine = typeLine.replace("Divergent", "").trim();
							discriminator = "divergent";
						} else if (typeLine.contains("Anomalous")) {
							typeLine = typeLine.replace("Anomalous", "").trim();
							discriminator = "anomalous";
						} else if (typeLine.contains("Phantasmal")) {
							typeLine = typeLine.replace("Phantasmal", "").trim();
							discriminator = "phantasmal";
						}
						queryJson.put("type",
								new JSONObject().put("option", typeLine).put("discriminator", discriminator));
						// Adicione os filtros
						JSONArray filters = new JSONArray();
						queryJson.put("stats",
								new JSONArray().put(new JSONObject().put("type", "and").put("filters", filters)));

						// Adicione os filtros misc
						JSONObject miscFilters = new JSONObject();
						String gemLevel = gemJson.optString("gemLevel", "").replace(" (Max)", "");
						miscFilters.put("gem_level", new JSONObject().put("min", gemLevel.isEmpty() ? "0" : gemLevel));
						String quality = gemJson.has("quality")
								? gemJson.getString("quality").replace("+", "").replace("%", "")
								: "0";
						miscFilters.put("quality", new JSONObject().put("min", quality));

						queryJson.put("filters",
								new JSONObject().put("misc_filters", new JSONObject().put("filters", miscFilters)));

						// Imprime o JSON
						System.out.println(json.toString());

						int idSize = countGems();
						// Verifica se já existe um link para a gema
						String link = gemLinks.get(gemName);
						if (link == null) {
							// Se não existir um link, faça uma nova solicitação para obter o link
							link = poeTradeController.makeRequest(json.toString());
							gemLinks.put(gemName, link); // Armazene o link no mapa
						}

						String linkWithGemName = gemName + " - " + link;
						linkAndIdSizeCallback.accept(new LinkAndIdSize(linkWithGemName, idSize));

						// System.out.println(link);

						json = new JSONObject();

					}
				}
			}
		}

	}

	public int countGems() throws Exception {
		// Resgatar o JSON do PoeNinjaService pra acesso
		equipments.setApiUrl(apiUrlPoeNinja);
		Map<String, List<PoeNinjaService>> equipmentInfo = equipments.getEquipmentInfo();

		// Converta o equipamentoInfo para um objeto JSON
		JSONObject equipmentInfoJson = convertEquipmentInfoToJson(equipmentInfo);

		int gemCount = 0;

		for (String key : equipmentInfoJson.keySet()) {
			if (equipmentGemsTypes.contains(key)) {
				JSONArray valueJson = equipmentInfoJson.getJSONArray(key);

				for (int i = 0; i < valueJson.length(); i++) {
					JSONObject poeNinjaServiceJson = valueJson.getJSONObject(i);

					// Obtenha o array de gemas
					JSONArray gemArray = poeNinjaServiceJson.getJSONArray("gem");

					// Se o array de gemas estiver vazio, continue para a próxima iteração
					if (gemArray.length() == 0) {
						continue;
					}

					// Incrementa o contador de gemas pelo número de gemas na gemaArray
					gemCount += gemArray.length();
				}
			}
		}

		return gemCount;
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
