package ommadawn46.plugin;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class GFSItem{
	protected GunForSurvival plugin;
	protected String rawName;
	protected String name;
	protected Material material;
	protected ItemStack itemStack;
	protected List<String> lore;

	public GFSItem(GunForSurvival plugin, String rawName, Material material, List<String> lore){
		this.plugin = plugin;
		this.rawName = rawName;
		this.name = null; // 各アイテムクラスでmakeDisplayName()を呼び出すこと
		this.material = material;
		this.lore = lore;

		ItemStack itemStack = new ItemStack(material, 1);
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.setLore(this.lore);
		itemStack.setItemMeta(itemMeta);
		this.itemStack = itemStack;
	}

	public GFSItem(GunForSurvival plugin, ItemStack itemStack){
		this.plugin = plugin;
		this.name = itemStack.getItemMeta().getDisplayName();
		this.rawName = getRawNameFromDisplayName(name);
		this.material = itemStack.getType();
		this.itemStack = itemStack;

		if(itemStack.getItemMeta().hasLore()){
			this.lore = itemStack.getItemMeta().getLore();
		}else{
			this.lore = new ArrayList<String>();
		}
	}

	public ItemStack getItemStack(){
		return itemStack.clone();
	}

	public String getRawName(){
		return rawName;
	}

	// アイテムの表示名を作成
	public abstract String makeDisplayName(String rawName);

	// アイテムの本来の名前を取得
	public abstract String getRawNameFromDisplayName(String name);

	// アクションの指定
	public abstract void playerAction(Player player, String action);
}