package com.augustodeveloper.poe.app.controllers;

import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.client.RestTemplate;

public class PoeNinjaController {

	
	@Autowired
	public RestTemplate restTemplate;
	
	public Map<String, String> extractInfoFromUrl(String url) throws URISyntaxException, UnsupportedEncodingException {
	    Map<String, String> info = new HashMap<>();
	    URI uri = new URI(url);
	    String path = uri.getPath();
	    String[] parts = path.split("/");
	    info.put("overview", parts[2]);
	    info.put("account", parts[4]);
	    info.put("name", parts[5]);
	    String query = uri.getQuery();
	    if (query != null) {
	        String[] queryParts = query.split("&");
	        for (String part : queryParts) {
	            String[] keyValue = part.split("=");
	            if (keyValue.length > 1 && "time-machine".equals(keyValue[0])) {
	                info.put("timeMachine", URLDecoder.decode(keyValue[1], StandardCharsets.UTF_8.toString()));
	                break;
	            }
	        }
	    }
	    return info;
	}
	
	public String buildApiUrl(Map<String, String> info) {
		   String apiUrl = "https://poe.ninja/api/data/f1719be35c7b76f6f6f1850e7340247956/getcharacter?account=" + info.get("account") +
		                  "&name=" + info.get("name") + "&overview=" + info.get("overview");
		   if (info.containsKey("timeMachine")) {
		       apiUrl += "&timeMachine=" + info.get("timeMachine");
		   }
		   return apiUrl;
		}
	
	public String requestApi(String apiUrl) {
		   RestTemplate restTemplate = new RestTemplate();
		   String response = restTemplate.getForObject(apiUrl, String.class);
		   return response;
		}
	
	public String handleRequest(String url) throws UnsupportedEncodingException, URISyntaxException {
		   Map<String, String> info = extractInfoFromUrl(url);
		   String apiUrl = buildApiUrl(info);
		   return apiUrl;
		}
	
	public String requestPoeNinjaApi(String apiUrl) {
	    RestTemplate restTemplate = new RestTemplate();
	    String response = restTemplate.getForObject(apiUrl, String.class);
	    return response;
	}
	
	public String getStatsPoeNinja(String apiUrl) {
		   RestTemplate restTemplate = new RestTemplate();
	       ResponseEntity<String> response = restTemplate.getForEntity(apiUrl, String.class);
	       String result = response.getBody();
	       return result;
	   }
}
