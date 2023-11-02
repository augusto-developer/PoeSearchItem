package com.augustodeveloper.poe.app.entities;

import org.json.JSONObject;

public class Type {
	
	private String type;
	private String status;
	

    public Type(String type, String status) {
        this.type = type;
        this.status = status;
    }
   
    public JSONObject toJson() {
        JSONObject json = new JSONObject();
        json.put("type", this.type);
        json.put("status", this.status);
        return json;
    }

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getStatus() {
		return status;
	}

	public void setStatus(String status) {
		this.status = status;
	}
    
    
}
