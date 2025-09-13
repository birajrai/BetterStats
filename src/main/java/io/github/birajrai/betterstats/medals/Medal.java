package io.github.birajrai.betterstats.medals;

import org.bson.Document;
import org.bukkit.Effect;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

/**
 * @author Biraj Rai
 * 2025 - 2028
 */
public class Medal {
	
	private Medals medal;
	private MLevel level;
	
	public Medal(Medals medal) {
		this.medal = medal;
		this.level = medal.getMedalLevel();
	}
	
	public Medal(Medals medal, MLevel level) {
		this.medal = medal;
		this.level = level;
	}
	
	public MLevel getMedalLevel() {
		return level;
	}
	
	public Medals getMedal() {
		return medal;
	}
	
	public int getIndex() {
		return medal.getIndex();
	}
	
	public boolean checkLevelTransition(long stat) {
		
		double multiplyer = Math.pow(medal.getMultiplyer(), level.getNumber());
		long transition = (long) ((int) medal.getTransition() * multiplyer);

		if(stat >= transition && level!=MLevel.GOD) {
			levelUp(level);
			checkLevelTransition(stat);
			return true;
		}
		
		return false;
	}
	
	public void levelUp(MLevel level) {
		switch(level) {
		case I:
			this.level = MLevel.II;
			break;
		case II:
			this.level = MLevel.III;
			break;
		case III:
			this.level = MLevel.GOD;
			break;
		default:
		}
	}
	
	public void newMedalEffect(Player player) {
		Location location = player.getLocation();
		player.playSound(location, Sound.ENTITY_PLAYER_LEVELUP, level.getSoudLevel(), 2);
		location.getWorld().playEffect(location, Effect.MOBSPAWNER_FLAMES, 2020);	
	}
	
	public Document createMedalDoc() {
		return new Document("medalName", medal.toString())
							.append("medalLevel", level.toString());
	}
	
}
