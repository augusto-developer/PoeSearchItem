package com.augustodeveloper.poe.app.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONObject;

public class PoeStatsAPI {
	
	public JSONObject fetchApiData(String url) throws Exception {
		   HttpClient client = HttpClient.newHttpClient();
		   HttpRequest request = HttpRequest.newBuilder()
		       .uri(new URI(url))
		       .build();
		   HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
		   String responseBody = response.body();

		   return new JSONObject(responseBody);
		}
	

}
