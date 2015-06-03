package ommadawn46.gunForSurvival.items;

import java.util.Map;
import java.util.regex.Pattern;

import ommadawn46.gunForSurvival.CoolTimer;
import ommadawn46.gunForSurvival.GunForSurvival;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.util.BlockIterator;

public class ThunderRod extends GFSItem{
	private final String cooltimeID = "" + ChatColor.YELLOW + ChatColor.WHITE + ChatColor.RESET;

	private int cooltime;
	private int range;

	public ThunderRod(GunForSurvival plugin, Map<?, ?> itemInfo){
		super(plugin, itemInfo);
		this.identifier = ChatColor.GOLD + "" + ChatColor.RESET;

		this.cooltime = Integer.parseInt((String) itemInfo.get("Cooltime"));
		this.range = Integer.parseInt((String) itemInfo.get("Range"));

		this.displayName = makeDisplayName(rawName);

		ItemMeta itemMeta = orgItemStack.getItemMeta();
		itemMeta.setDisplayName(this.displayName);
		orgItemStack.setItemMeta(itemMeta);
	}

	@Override
	public void playerAction(Player player, ItemStack itemStack, String action) {
		if(action.equals("LEFT_CLICK")){
			shot(player, itemStack);
		}
	}

	private void shot(Player player, ItemStack itemStack){
		ItemMeta itemMeta = itemStack.getItemMeta();
		String name = itemMeta.getDisplayName();

		if(Pattern.compile(cooltimeID).matcher(name).find()){
			new CoolTimer(cooltimeID, itemStack, player).runTaskLater(this.plugin, cooltime);
			return;
		}

		// 視線上のブロックへ雷を落とす
		Block target = getTargetBlock(player, range);

		if(target == null){
			return;
		}

		Location loc = target.getLocation();

		player.getWorld().strikeLightning(loc);

		itemMeta.setDisplayName(name + cooltimeID);
		itemStack.setItemMeta(itemMeta);

		new CoolTimer(cooltimeID, itemStack, player).runTaskLater(this.plugin, cooltime);
	}

	private Block getTargetBlock(Player player, int range) {
	    // 視線上のブロックを取得
	    BlockIterator it = new BlockIterator(player, range);

	    while (it.hasNext()) {
	    	Block block = it.next();

	        if (block.getType() != Material.AIR) {
	            // ブロックが見つかった
	            return block;
	        }
	    }

	    // 最後までブロックがみつからなかった
	    return null;
	}
}