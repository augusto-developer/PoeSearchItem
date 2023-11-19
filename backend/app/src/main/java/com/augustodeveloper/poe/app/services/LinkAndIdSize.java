package com.augustodeveloper.poe.app.services;

public class LinkAndIdSize {
	private String link;
    private int idSize;

    public LinkAndIdSize(String link, int idSize) {
        this.link = link;
        this.idSize = idSize;
    }

    public String getLink() {
        return link;
    }

    public int getIdSize() {
        return idSize;
    }
}
