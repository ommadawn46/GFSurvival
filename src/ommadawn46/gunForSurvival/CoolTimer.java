package ommadawn46.gunForSurvival;

import java.util.regex.Pattern;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;

public class CoolTimer extends BukkitRunnable {
	String cooltimeID;
    ItemStack itemStack;
    Player player;

    public CoolTimer(String cooltimeID, ItemStack itemStack, Player player) {
    	this.cooltimeID = cooltimeID;
        this.itemStack = itemStack;
        this.player = player;
    }

    @Override
    public void run() {
    	ItemStack playerItem = player.getItemInHand();
    	if(!playerItem.hasItemMeta()){
    		return;
    	}
    	ItemMeta itemMeta = itemStack.getItemMeta();
    	String name = itemMeta.getDisplayName();
    	if(playerItem.getItemMeta().getDisplayName().equals(name)){
    		if(Pattern.compile(cooltimeID).matcher(name).find()){
    			name = name.split(cooltimeID)[0];
    			itemMeta.setDisplayName(name);
    			itemStack.setItemMeta(itemMeta);
    			itemStack.setAmount(1);
    			player.setItemInHand(itemStack);
    		}
    	}
    }
}