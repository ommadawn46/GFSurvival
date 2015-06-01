package ommadawn46.gunForSurvival.items;

import java.util.Map;

import ommadawn46.gunForSurvival.GunForSurvival;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class FlyingPotion extends GFSItem {
	private int duration;
	private Sound consumeSound;
	private float consumeSoundPitch;

	public FlyingPotion(GunForSurvival plugin, Map<?, ?> itemInfo) {
		super(plugin, itemInfo);
		this.identifier = ChatColor.BLUE + "" + ChatColor.RESET;

		this.duration = Integer.parseInt((String) itemInfo.get("Duration"));
		this.consumeSound = Sound.valueOf((String)itemInfo.get("ConsumeSound"));
		this.consumeSoundPitch = Float.parseFloat((String)itemInfo.get("ConsumeSoundPitch"));

		this.displayName = makeDisplayName(rawName);

		ItemMeta itemMeta = orgItemStack.getItemMeta();
		itemMeta.setDisplayName(this.displayName);
		orgItemStack.setItemMeta(itemMeta);
	}

	@Override
	public void playerAction(Player player, ItemStack itemStack, String action) {
		if(action.equals("CONSUME")){
			consume(player, itemStack);
		}
	}

	private void consume(Player player, ItemStack itemStack) {
		player.setAllowFlight(true);
		player.getWorld().playSound(player.getLocation(), consumeSound, 1, consumeSoundPitch);
		player.setVelocity(new Vector(0, 1, 0));
		player.setFlying(true);

		new FlyingPotionTimer(player).runTaskLater(plugin, duration);
	}

	private class FlyingPotionTimer extends BukkitRunnable {
		Player player;

		public FlyingPotionTimer(Player player){
			this.player = player;
		}

		@Override
		public void run() {
			player.setFlying(false);
			player.setAllowFlight(false);
			player.setFallDistance(0);
		}
	}
}
