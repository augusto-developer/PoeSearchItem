package com.augustodeveloper.poe.app.entities.itens;

import java.util.ArrayList;
import java.util.List;

import org.json.JSONArray;
import org.json.JSONObject;

import com.augustodeveloper.poe.app.services.PoeNinjaService;

public class Boots {
	
	private PoeNinjaService poeNinjaService;

    public Boots() {
        this.poeNinjaService = new PoeNinjaService();
    }
	
    public List<String> getBootsInfo(String apiUrl) throws Exception {
    	
    	String apiContent = poeNinjaService.getStatsPoeNinja(apiUrl);
        
        
        
        JSONObject jsonResponse = new JSONObject(apiContent);
        JSONArray results = jsonResponse.getJSONArray("items");
        
        // Inicializar um ArrayList para armazenar as informações
        List<String> infoList = new ArrayList<>();
        
		for (int i = 0; i < results.length(); i++) {
			JSONObject item = results.getJSONObject(i);
			if (item.has("itemData")) {
				 JSONObject itemData = item.getJSONObject("itemData");
				if (itemData.getString("inventoryId").contains("Boots")) {
					infoList.add(itemData.getString("baseType"));
				}
			}
		}
        	
        System.out.println(infoList);
        // Retornar o array de strings.
        return infoList;
    }
}
