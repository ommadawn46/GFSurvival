package ommadawn46.gunForSurvival.items;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import ommadawn46.gunForSurvival.CoolTimer;
import ommadawn46.gunForSurvival.GunForSurvival;
import ommadawn46.gunForSurvival.ReloadTimer;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

public class Gun extends GFSItem{
	private int ammoSize;
	private int coolTime;
	private int reloadTime;

	private EntityType bulletType;
	private double bulletDamage;
	private double bulletSpeed;

	private Sound shotSound;
	private float shotSoundPitch;
	private Sound reloadSound;
	private float reloadSoundPitch;
	private Sound finishReloadSound;
	private float finishReloadSoundPitch;

	public Gun(GunForSurvival plugin, Map<?, ?> itemInfo){
		super(plugin, itemInfo);
		this.identifier = ChatColor.GRAY  + "" + ChatColor.RESET;

		this.ammoSize = Integer.parseInt((String) itemInfo.get("AmmoSize"));
		this.coolTime = Integer.parseInt((String) itemInfo.get("Cooltime"));
		this.reloadTime = Integer.parseInt((String) itemInfo.get("Reloadtime"));

		this.bulletType = EntityType.valueOf((String)itemInfo.get("BulletType"));
		this.bulletDamage = Double.parseDouble((String) itemInfo.get("BulletDamage"));
		this.bulletSpeed = Double.parseDouble((String) itemInfo.get("BulletSpeed"));

		this.shotSound = Sound.valueOf((String)itemInfo.get("ShotSound"));
		this.shotSoundPitch = Float.parseFloat((String)itemInfo.get("ShotSoundPitch"));
		this.reloadSound = Sound.valueOf((String)itemInfo.get("ReloadSound"));
		this.reloadSoundPitch = Float.parseFloat((String)itemInfo.get("ReloadSoundPitch"));
		this.finishReloadSound = Sound.valueOf((String)itemInfo.get("FinishReloadSound"));
		this.finishReloadSoundPitch = Float.parseFloat((String)itemInfo.get("FinishReloadSoundPitch"));

		this.displayName = makeDisplayName(rawName);

		ItemMeta itemMeta = orgItemStack.getItemMeta();
		itemMeta.setDisplayName(this.displayName);
		orgItemStack.setItemMeta(itemMeta);
	}

	@Override
	protected String makeDisplayName(String rawName) {
		// 表示名の設定
		return identifier + rawName + " <"+ammoSize+"/"+ammoSize+">";
	}

	@Override
	protected String getRawNameFromDisplayName(String name) {
		// 表示名から本来の名前を取得
		return name.split(identifier)[1].split(" <")[0];
	}

	private int getAmmoRemain(String name){
		// 表示名から残弾数を取得
		return Integer.parseInt(name.split("<")[1].split("/")[0]);
	}

	@Override
	public void playerAction(Player player, ItemStack itemStack, String action){
		if(action.equals("LEFT_CLICK")){
			zoom(player);
		}else if(action.equals("RIGHT_CLICK")){
			shot(player, itemStack);
		}else if(action.equals("SNEAK")){
			reload(player, itemStack);
		}
	}

	public void hit(Projectile proj, EntityDamageByEntityEvent e){
		if(!proj.getType().equals(bulletType)){
			// 銃弾と異なるEntityTypeのとき
			return;
		}
		// ダメージをセットする
		e.setDamage(bulletDamage);
	}

	public void hit(Location loc){
		// 着弾場所に何かしたいならここに書く
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
			new CoolTimer(itemStack, player).runTaskLater(this.plugin, coolTime);
			return;
		}

		if(ammoRemain > 0){
			// 銃弾を発射する
			Location loc = player.getEyeLocation();
			Vector vec = new Vector(loc.getDirection().getX()*bulletSpeed ,loc.getDirection().getY()*bulletSpeed ,loc.getDirection().getZ()*bulletSpeed);
			Entity bullet = player.getWorld().spawnEntity(loc.add(loc.getDirection().getX()*1.5, loc.getDirection().getY()*1.5, loc.getDirection().getZ()*1.5), bulletType);
			bullet.setVelocity(vec);
			if(bullet instanceof Projectile){
				((Projectile)bullet).setShooter(player);
			}
			loc.getWorld().playSound(loc, shotSound, 3, shotSoundPitch);

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

			new CoolTimer(itemStack, player).runTaskLater(this.plugin, coolTime);
		}else if(ammoRemain == 0){
			// 弾切れ
			reload(player, itemStack);
		}
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
			new CoolTimer(itemStack, player).runTaskLater(this.plugin, coolTime);
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
				finishReloadSound, finishReloadSoundPitch).runTaskLater(this.plugin, reloadTime);
	}
}