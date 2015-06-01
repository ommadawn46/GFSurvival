package ommadawn46.gunForSurvival.items;

import java.util.List;
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
		List<String> lore = itemStack.getItemMeta().getLore();

		if(lore.size() > 0 && Pattern.compile("CoolTime").matcher(lore.get(lore.size()-1)).find()){
			new CoolTimer(itemStack, player).runTaskLater(this.plugin, cooltime);
			return;
		}
		// 視線上のブロックへ雷を落とす
		Block target = getTargetBlock(player, range);

		if(target == null){
			return;
		}

		Location loc = target.getLocation();

		player.getWorld().strikeLightning(loc);

		ItemMeta itemMeta = itemStack.getItemMeta();

		// loreの最後の行にステータスを記述する
		if(Pattern.compile("Reloaded").matcher(lore.get(lore.size()-1)).find()){
			lore.set(lore.size()-1, "<CoolTime>");
		}else{
			lore.add("<CoolTime>");
		}
		itemMeta.setLore(lore);

		itemStack.setItemMeta(itemMeta);

		new CoolTimer(itemStack, player).runTaskLater(this.plugin, cooltime);
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