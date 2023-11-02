package com.augustodeveloper.poe.app.services;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import org.json.JSONObject;

public class PoeTradeService {
	
		public String makeRequest(String jsonString) throws Exception {
        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(new URI("https://www.pathofexile.com/api/trade/search/Ancestor"))
                .header("Content-Type", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(jsonString))
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        JSONObject responseJson = new JSONObject(response.body());
        String id = responseJson.getString("id");

        String searchUrl = "https://pathofexile.com/trade/search/Ancestor/" + id;

        return searchUrl;
        
    }
}
