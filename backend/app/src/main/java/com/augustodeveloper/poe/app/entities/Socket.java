package com.augustodeveloper.poe.app.entities;

import org.json.JSONObject;

public class Socket {
	
	 	private int minSockets;
	    private int minLinks;

	    public Socket(int minSockets, int minLinks) {
	        this.minSockets = minSockets;
	        this.minLinks = minLinks;
	    }

	    public JSONObject toJson() {
	        JSONObject json = new JSONObject();
	        JSONObject sockets = new JSONObject();
	        sockets.put("min", this.minSockets);
	        json.put("sockets", sockets);
	        
	        JSONObject links = new JSONObject();
	        links.put("min", this.minLinks);
	        json.put("links", links);

	        JSONObject socketFilters = new JSONObject();
	        socketFilters.put("filters", json);
	        
	        

	        return socketFilters;
	    }

		public int getMinSockets() {
			return minSockets;
		}

		public void setMinSockets(int minSockets) {
			this.minSockets = minSockets;
		}

		public int getMinLinks() {
			return minLinks;
		}

		public void setMinLinks(int minLinks) {
			this.minLinks = minLinks;
		}

	    
}
