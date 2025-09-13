package io.github.birajrai.betterstats.medals;

/**
 * @author Biraj Rai
 * 2025 - 2028
 */
public enum MLevel {
	
	// index, sound level
	I(1, 1, "&a"), II(2, 15, "&9"), III(3, 30, "&6"), GOD(4, 50, "");
	
	private int number, soundLevel;
	private String color;
	
	MLevel(int number, int soundLevel, String color) {
		this.number = number;
		this.soundLevel = soundLevel;
		this.color = color;
	}
	
	public int getNumber() {
		return number;
	}
	
	public int getSoudLevel() {
		return soundLevel;
	}
	
	public String getLevelColor() {
		return color;
	}
	
}
