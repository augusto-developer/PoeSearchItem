package com.augustodeveloper.poe.app.entities;

public class Miscellaneous {

	private Value synthesised_item;
	private Value corrupted;
	private Value quality;
	private Value gem_level;
	private boolean disabled;
	

	public Miscellaneous() {
	}

	public Miscellaneous(Value corrupted, Value synthesised_item, Value quality, Value gem_level) {
		this.corrupted = corrupted;
		this.synthesised_item = synthesised_item;
		this.quality = quality;
		this.gem_level = gem_level;
	}

	public Value getSynthesised_item() {
		return synthesised_item;
	}

	public void setSynthesised_item(Value synthesised_item) {
		this.synthesised_item = synthesised_item;
	}

	public Value getCorrupted() {
		return corrupted;
	}

	public void setCorrupted(Value corrupted) {
		this.corrupted = corrupted;
	}

	public boolean isDisabled() {
		return disabled;
	}

	public void setDisabled(boolean disabled) {
		this.disabled = disabled;
	}

	public Value getQuality() {
		return quality;
	}

	public void setQuality(Value quality) {
		this.quality = quality;
	}

	public Value getGem_level() {
		return gem_level;
	}

	public void setGem_level(Value gem_level) {
		this.gem_level = gem_level;
	}

	

}
