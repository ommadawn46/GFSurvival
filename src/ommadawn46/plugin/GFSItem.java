package ommadawn46.plugin;

import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public abstract class GFSItem{
	protected GunForSurvival plugin;
	protected String regex;
	protected String rawName;
	protected String displayName;
	protected Material material;
	protected ItemStack orgItemStack;
	protected List<String> orgLore;

	public GFSItem(GunForSurvival plugin, String rawName, Material material, List<String> lore){
		this.plugin = plugin;
		this.regex = null; // 各アイテムクラスで設定すること
		this.rawName = rawName;
		this.displayName = null; // 各アイテムクラスでmakeDisplayName()を呼び出すこと
		this.material = material;
		this.orgLore = lore;

		ItemStack itemStack = new ItemStack(material, 1);
		ItemMeta itemMeta = itemStack.getItemMeta();
		itemMeta.setLore(this.orgLore);
		itemStack.setItemMeta(itemMeta);
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
		if(Pattern.compile(regex).matcher(name).find() && this.material.equals(material)){
			String rawName = getRawNameFromDisplayName(name);
			if(this.rawName.equals(rawName)){
				return true;
			}
		}
		return false;
	}

	// アイテムの表示名を作成
	public abstract String makeDisplayName(String rawName);

	// アイテムの本来の名前を取得
	public abstract String getRawNameFromDisplayName(String name);

	// アクションの指定
	public abstract void playerAction(Player player, ItemStack itemStack, String action);
}