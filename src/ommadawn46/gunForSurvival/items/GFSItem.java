package ommadawn46.gunForSurvival.items;

import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import ommadawn46.gunForSurvival.GunForSurvival;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class GFSItem{
	protected GunForSurvival plugin;
	protected String identifier;
	protected String rawName;
	protected String displayName;
	protected Material material;
	protected ItemStack orgItemStack;
	protected List<String> orgLore;

	@SuppressWarnings("unchecked")
	public GFSItem(GunForSurvival plugin, Map<?, ?> itemInfo){
		this.plugin = plugin;
		this.identifier = null; // chatColorでクラスの識別をする。各アイテムクラスで設定すること
		this.rawName = (String) itemInfo.get("Name");
		this.displayName = null; // 各アイテムクラスでmakeDisplayName()を呼び出すこと
		this.material = Material.valueOf((String)itemInfo.get("Material"));

		ItemStack itemStack = new ItemStack(material, 1);
		if(itemInfo.containsKey("Lore")){
			this.orgLore = (List<String>) itemInfo.get("Lore");
			ItemMeta itemMeta = itemStack.getItemMeta();
			itemMeta.setLore(this.orgLore);
			itemStack.setItemMeta(itemMeta);
		}
		this.orgItemStack = itemStack;
	}

	public ItemStack getItemStack(){
		return orgItemStack.clone();
	}

	public String getRawName(){
		return rawName;
	}

	// このインスタンスから生成されたアイテムかどうかを調べる
	public boolean isThisItem(ItemStack itemStack){
		String name = itemStack.getItemMeta().getDisplayName();
		Material material = itemStack.getType();
		if(Pattern.compile(identifier + rawName).matcher(name).find() && this.material.equals(material)){
			return true;
		}
		return false;
	}

	// アイテムの表示名を作成
	protected String makeDisplayName(String rawName){
		// 表示名の設定
		return identifier + rawName;
	}

	// アイテムの本来の名前を取得
	protected String getRawNameFromDisplayName(String name) {
		// 表示名から本来の名前を取得
		return name.split(identifier)[1];
	}

	// アクションの指定
	public abstract void playerAction(Player player, ItemStack itemStack, String action);
}