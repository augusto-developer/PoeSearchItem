package com.augustodeveloper.poe.app.controllers;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import com.augustodeveloper.poe.app.services.PoeTradeService;

@RestController
public class PoeTradeController {
	
	  @Autowired	
	  private RestTemplate restTemplate;
	  
	  @Autowired
	  private PoeTradeService poeTradeService;

	  public PoeTradeController(RestTemplate restTemplate, PoeTradeService poeTradeService) {
	      this.restTemplate = restTemplate;
	      this.poeTradeService = poeTradeService;
	  }

	  @PostMapping("/trade")
	  public @ResponseBody String makeRequest(@RequestBody String jsonString) throws Exception {
	      return poeTradeService.makeRequest(jsonString);
	  }
	  
}
