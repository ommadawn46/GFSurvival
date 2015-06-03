package ommadawn46.gunForSurvival;

import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class ReloadTimer extends BukkitRunnable {
    ItemStack itemStack;
    Player player;
    String newName;
    Sound finishReloadSound;
    float finishReloadSoundPitch;

    public ReloadTimer(ItemStack itemStack , Player player, String newName, Sound finishReloadSound, float finishReloadSoundPitch) {
        this.itemStack = itemStack;
        this.player = player;
        this.newName = newName;
        this.finishReloadSound = finishReloadSound;
        this.finishReloadSoundPitch = finishReloadSoundPitch;
    }

    @Override
    public void run() {
    	ItemStack playerItem = player.getItemInHand();
    	if(!playerItem.hasItemMeta()){
    		return;
    	}
    	ItemMeta itemMeta = itemStack.getItemMeta();
    	if(playerItem.getItemMeta().getDisplayName().equals(itemMeta.getDisplayName())){
			itemMeta.setDisplayName(newName);

			itemStack.setItemMeta(itemMeta);
			itemStack.setAmount(1);
			player.setItemInHand(itemStack);

			player.getWorld().playSound(player.getLocation(), finishReloadSound, 0.8f, finishReloadSoundPitch);
    	}
    }
}