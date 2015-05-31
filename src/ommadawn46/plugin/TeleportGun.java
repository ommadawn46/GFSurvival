package ommadawn46.plugin;

import java.util.Collection;
import java.util.List;
import java.util.regex.Pattern;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.BlockIterator;
import org.bukkit.util.Vector;

public class TeleportGun extends GFSItem{
	private int ammoSize;
	private int cooltime;
	private int reloadTime;
	private int range;

	private Sound shotSound;
	private float shotSoundPitch;
	private Sound reloadSound;
	private float reloadSoundPitch;
	private Sound finishReloadSound;
	private float finishReloadPitch;

	public TeleportGun(GunForSurvival plugin, String rawName, Material material, List<String> lore, int ammoSize, int cooltime, int reloadTime, int range,
			Sound shotSound, float shotSoundPitch, Sound reloadSound, float reloadSoundPitch, Sound finishReloadSound, float finishReloadPitch){
		super(plugin, rawName, material, lore);
		this.regex = "^"+ChatColor.DARK_AQUA+".*<[0-9]+/[0-9]+>";

		this.ammoSize = ammoSize;
		this.cooltime = cooltime;
		this.reloadTime = reloadTime;
		this.range = range;

		this.shotSound = shotSound;
		this.shotSoundPitch = shotSoundPitch;
		this.reloadSound = reloadSound;
		this.reloadSoundPitch = reloadSoundPitch;
		this.finishReloadSound = finishReloadSound;
		this.finishReloadPitch = finishReloadPitch;

		this.displayName = makeDisplayName(rawName);

		ItemMeta itemMeta = orgItemStack.getItemMeta();
		itemMeta.setDisplayName(this.displayName);
		orgItemStack.setItemMeta(itemMeta);
	}

	@Override
	public String makeDisplayName(String rawName) {
		// 表示名の設定
		return ChatColor.DARK_AQUA + rawName + " <"+ammoSize+"/"+ammoSize+">";
	}

	@Override
	public String getRawNameFromDisplayName(String name) {
		// 表示名から本来の名前を取得
		return name.split(ChatColor.DARK_AQUA +"")[1].split(" <")[0];
	}

	public int getAmmoRemain(String name){
		// 表示名から残弾数を取得
		return Integer.parseInt(name.split("<")[1].split("/")[0]);
	}

	@Override
	public void playerAction(Player player, ItemStack itemStack, String action) {
		if(action.equals("LEFT_CLICK")){
			zoom(player);
		}else if(action.equals("RIGHT_CLICK")){
			shot(player, itemStack);
		}else if(action.equals("SNEAK")){
			reload(player, itemStack);
		}
	}

	private void shot(Player player, ItemStack itemStack){
		ItemMeta itemMeta = itemStack.getItemMeta();
		String name = itemMeta.getDisplayName();
		List<String> lore = itemStack.getItemMeta().getLore();
		int ammoRemain = getAmmoRemain(name);

		if(Pattern.compile("Reload").matcher(name).find()){
			reload(player, itemStack);
			return;
		}

		if(lore.size() > 0 && Pattern.compile("CoolTime").matcher(lore.get(lore.size()-1)).find()){
			new CoolTimer(itemStack, player).runTaskLater(this.plugin, cooltime);
			return;
		}

		if(ammoRemain > 0){
			// 視線上のブロックへワープする
			Block target = getTargetBlock(player, range);

			if(target == null){
				return;
			}

			Vector dic = player.getLocation().getDirection();
			Location loc = target.getLocation().setDirection(dic);

			player.teleport(loc);
			player.setVelocity(dic.multiply(0.5));
			player.getWorld().playSound(loc, shotSound, 3, shotSoundPitch);

			ammoRemain--;
			itemMeta.setDisplayName(itemStack.getItemMeta().getDisplayName().split(" <")[0] + " <"+ammoRemain+"/"+ammoSize+">");

			// loreの最後の行にステータスを記述する
			if(Pattern.compile("Reloaded").matcher(lore.get(lore.size()-1)).find()){
				lore.set(lore.size()-1, "<CoolTime>");
			}else{
				lore.add("<CoolTime>");
			}
			itemMeta.setLore(lore);

			itemStack.setItemMeta(itemMeta);

			new CoolTimer(itemStack, player).runTaskLater(this.plugin, cooltime);
		}else if(ammoRemain == 0){
			// 弾切れ
			reload(player, itemStack);
		}
	}

	private Block getTargetBlock(Player player, int range) {
	    // 視線上のブロックを取得
	    BlockIterator it = new BlockIterator(player, range);
	    Block[] bufferBlock = new Block[3];

	    while (it.hasNext()) {
	    	bufferBlock[0] = bufferBlock[1];
	    	bufferBlock[1] = bufferBlock[2];
	    	bufferBlock[2] = it.next();

	        if (bufferBlock[2].getType() != Material.AIR) {
	            // ブロックが見つかった
	            return bufferBlock[0];
	        }
	    }

	    // 最後までブロックがみつからなかった
	    return null;
	}

	private void zoom(Player player){
		// スローのポーションエフェクトをズーム代わりにする
		Collection<PotionEffect> peCollection = player.getActivePotionEffects();
		for(PotionEffect pe: peCollection){
			if(pe.getType().equals(PotionEffectType.SLOW)){
				// プレイヤーのポーションエフェクトを取り除く
				player.removePotionEffect(PotionEffectType.SLOW);
				return;
			}
		}
		// プレイヤーにスローのポーションエフェクトを与える
		player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, 24000, 4));
	}

	private void reload(Player player, ItemStack itemStack){
		ItemMeta itemMeta = itemStack.getItemMeta();
		String name = itemMeta.getDisplayName();
		List<String> lore = itemStack.getItemMeta().getLore();
		int ammoRemain = getAmmoRemain(name);

		if(lore.size() > 0 && Pattern.compile("CoolTime").matcher(lore.get(lore.size()-1)).find()){
			new CoolTimer(itemStack, player).runTaskLater(this.plugin, cooltime);
			return;
		}

		if(ammoRemain == ammoSize){
			return;
		}
		ammoRemain = ammoSize;
		if(!Pattern.compile("Reload").matcher(name).find()){
			itemMeta.setDisplayName(itemStack.getItemMeta().getDisplayName() + " [Reload]");
			itemStack.setItemMeta(itemMeta);
			player.getWorld().playSound(player.getLocation(), reloadSound, 2, reloadSoundPitch);
		}
		new ReloadTimer(itemStack, player, itemStack.getItemMeta().getDisplayName().split(" <")[0] + " <"+ammoRemain+"/"+ammoSize+">",
				finishReloadSound, finishReloadPitch).runTaskLater(this.plugin, reloadTime);
	}
}
