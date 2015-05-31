package ommadawn46.plugin;

import java.util.List;

import org.bukkit.ChatColor;
import org.bukkit.Material;
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

	public FlyingPotion(GunForSurvival plugin, String rawName,
			Material material, List<String> lore, int duration, Sound consumeSound, float consumeSoundPitch) {
		super(plugin, rawName, material, lore);
		this.regex = "^"+ChatColor.BLUE;

		this.duration = duration;
		this.consumeSound = consumeSound;
		this.consumeSoundPitch = consumeSoundPitch;

		this.displayName = makeDisplayName(rawName);

		ItemMeta itemMeta = orgItemStack.getItemMeta();
		itemMeta.setDisplayName(this.displayName);
		orgItemStack.setItemMeta(itemMeta);
	}

	@Override
	public String makeDisplayName(String rawName) {
		// 表示名の設定
		return ChatColor.BLUE + rawName;
	}

	@Override
	public String getRawNameFromDisplayName(String name) {
		// 表示名から本来の名前を取得
		return name.split(ChatColor.BLUE +"")[1];
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
