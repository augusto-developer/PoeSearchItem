package com.augustodeveloper.poe.app.controllers;

import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;


public class PoeTradeController {
	
	
	@Autowired
	public RestTemplate restTemplate;
	
	// Request com a API do site para retornar o link de pesquisa
	public String makeRequest(String jsonString) throws Exception {
		Thread.sleep(6000);
		
		RestTemplate restTemplate = new RestTemplate();
		HttpHeaders headers = new HttpHeaders();
		headers.setContentType(MediaType.APPLICATION_JSON);
		HttpEntity<String> entity = new HttpEntity<>(jsonString, headers);
		ResponseEntity<String> response = restTemplate.postForEntity("https://www.pathofexile.com/api/trade/search/Ancestor", entity, String.class);

		String responseBody = response.getBody();
		JSONObject responseJson = new JSONObject(responseBody);
		String id = responseJson.getString("id");

		String searchUrl = "https://pathofexile.com/trade/search/Ancestor/" + id;
		return searchUrl;

	}
	
	public String getStats() {
		   RestTemplate restTemplate = new RestTemplate();
	       ResponseEntity<String> response = restTemplate.getForEntity("https://www.pathofexile.com/api/trade/data/stats", String.class);
	       String result = response.getBody();
	       return result;
	   }
}
