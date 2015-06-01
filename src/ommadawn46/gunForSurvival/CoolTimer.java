package ommadawn46.gunForSurvival;

import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class CoolTimer extends BukkitRunnable {
    ItemStack itemStack;
    Player player;

    public CoolTimer(ItemStack itemStack, Player player) {
        this.itemStack = itemStack;
        this.player = player;
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
    		List<String> lore = itemMeta.getLore();
    		if(lore.size() > 0 && Pattern.compile("CoolTime").matcher(lore.get(lore.size()-1)).find()){
    			lore.remove(lore.size()-1);
    			itemMeta.setLore(lore);
    			itemStack.setItemMeta(itemMeta);
    			itemStack.setAmount(1);
    			player.setItemInHand(itemStack);
    		}
    	}
    }
}