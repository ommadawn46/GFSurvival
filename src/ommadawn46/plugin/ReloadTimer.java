package ommadawn46.plugin;

import java.util.List;
import java.util.regex.Pattern;

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
    	if(playerItem == null){
    		return;
    	}
    	if(!playerItem.hasItemMeta()){
    		return;
    	}
    	ItemMeta itemMeta = itemStack.getItemMeta();
    	if(playerItem.getItemMeta().getDisplayName().equals(itemMeta.getDisplayName())){
    		List<String> playerLore = playerItem.getItemMeta().getLore();
    		if(playerLore.size() <= 0 || !Pattern.compile("Reloaded").matcher(playerLore.get(playerLore.size()-1)).find()){
    			itemMeta.setDisplayName(newName);

    			List<String> lore = itemMeta.getLore();

    			// loreの最後の行にステータスを記述する
				if(Pattern.compile("CoolTime").matcher(lore.get(lore.size()-1)).find()){
					lore.set(lore.size()-1, "<Reloaded>");
				}else{
					lore.add("<Reloaded>");
				}
    			itemMeta.setLore(lore);

    			itemStack.setItemMeta(itemMeta);
    			player.setItemInHand(itemStack);

    			player.getWorld().playSound(player.getLocation(), finishReloadSound, 2, finishReloadSoundPitch);
    		}
    	}
    }
}