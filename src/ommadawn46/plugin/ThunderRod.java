package ommadawn46.plugin;

import java.util.List;
import java.util.regex.Pattern;

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

	public ThunderRod(GunForSurvival plugin, String rawName, Material material, List<String> lore, int cooltime, int range){
		super(plugin, rawName, material, lore);
		this.regex = "^"+ChatColor.GOLD;

		this.cooltime = cooltime;
		this.range = range;

		this.displayName = makeDisplayName(rawName);

		ItemMeta itemMeta = orgItemStack.getItemMeta();
		itemMeta.setDisplayName(this.displayName);
		orgItemStack.setItemMeta(itemMeta);
	}

	@Override
	public String makeDisplayName(String rawName) {
		// 表示名の設定
		return ChatColor.GOLD + rawName;
	}

	@Override
	public String getRawNameFromDisplayName(String name) {
		// 表示名から本来の名前を取得
		return name.split(ChatColor.GOLD +"")[1];
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