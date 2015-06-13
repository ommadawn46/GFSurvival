package ommadawn46.gunForSurvival.items;

import java.util.Collection;
import java.util.Map;
import java.util.regex.Pattern;

import ommadawn46.gunForSurvival.CoolTimer;
import ommadawn46.gunForSurvival.GunForSurvival;
import ommadawn46.gunForSurvival.ReloadTimer;

import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.EnderDragon;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class Gun extends GFSItem{
	private final String cooltimeID = "" + ChatColor.YELLOW + ChatColor.WHITE + ChatColor.RESET;

	private int ammoSize;
	private int coolTime;
	private int reloadTime;

	private EntityType bulletType;
	private double bulletDamage;
	private double bulletSpeed;

	private double recoil;
	private double dispersion;

	private double knockBack;
	private double knockUp;

	private int burstShot;
	private int burstInterval;

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

		this.recoil = Double.parseDouble((String) itemInfo.get("Recoil"));
		this.dispersion = Double.parseDouble((String) itemInfo.get("Dispersion"));

		this.knockBack = Double.parseDouble((String) itemInfo.get("KnockBack"));
		this.knockUp = Double.parseDouble((String) itemInfo.get("KnockUp"));

		this.burstShot =  Integer.parseInt((String) itemInfo.get("BurstShot"));
		this.burstInterval = Integer.parseInt((String) itemInfo.get("BurstInterval"));

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
			// 右クリック長押し中は0.2秒毎に呼ばれる
			shot(player, itemStack);
		}else if(action.equals("SNEAK")){
			reload(player, itemStack);
		}else if(action.equals("DELETESTATUS")){
			deleteStatus(itemStack);
		}
	}

	public void hit(Projectile proj, EntityDamageByEntityEvent e){
		if(!proj.getType().equals(bulletType)){
			// 銃弾と異なるEntityTypeのとき
			return;
		}

		Entity entity = e.getEntity();
		if(proj.getShooter().equals(entity)){
			// イベントをキャンセルしない場合
			e.setDamage(bulletDamage);
		}else{
			// イベントをキャンセルする場合
			e.setCancelled(true);
			if(entity instanceof LivingEntity){
				// ダメージのセット
				if(!(entity instanceof EnderDragon)){
					((LivingEntity)entity).damage(bulletDamage);
				}else{
					// エンダードラゴンはdamageでダメージを与えられないので直接Healthを引く
					double health = ((LivingEntity)entity).getHealth();
					((LivingEntity)entity).setHealth(health - bulletDamage);
				}
				((LivingEntity)entity).setNoDamageTicks(0);

				// ノックバックのセット
				Vector vec = entity.getVelocity().add(entity.getLocation().toVector().subtract(((Player)proj.getShooter()).getLocation().toVector())).normalize().multiply(knockBack);
				vec.setY(vec.getY() + knockUp);
				entity.setVelocity(vec);
			}
		}
	}

	public void hit(Location loc){
		// 着弾場所に何かしたいならここに書く
	}

	private void shot(Player player, ItemStack itemStack){
		ItemMeta itemMeta = itemStack.getItemMeta();
		String name = itemMeta.getDisplayName();
		int ammoRemain = getAmmoRemain(name);

		if(Pattern.compile("Reload").matcher(name).find()){
			return;
		}

		if(Pattern.compile(cooltimeID).matcher(name).find()){
			new CoolTimer(cooltimeID, itemStack, player).runTaskLater(this.plugin, Math.max(coolTime, burstInterval));
			return;
		}

		if(ammoRemain > 0){
			int useBullet = burstShot < ammoRemain ? burstShot : ammoRemain;

			// ショットガンの場合は弾を1発だけ消費する
			boolean isShotGun = false;
			if(burstInterval <= 0 && 1 < burstShot){
				isShotGun = true;
				useBullet = burstShot;
				ammoRemain--;
				itemMeta.setDisplayName(itemStack.getItemMeta().getDisplayName().split(" <")[0] + " <"+ammoRemain+"/"+ammoSize+">");
				itemStack.setItemMeta(itemMeta);
			}

			// ShotTimerのセット
			for(int i = 0; i < useBullet; i++){
				if(!isShotGun){
					ammoRemain--;
				}
				new ShotTimer(itemStack, player,  ammoRemain, useBullet-1 == i).runTaskLater(plugin, burstInterval*i);
			}

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
		int ammoRemain = getAmmoRemain(name);

		if(Pattern.compile(cooltimeID).matcher(name).find()){
			new CoolTimer(cooltimeID, itemStack, player).runTaskLater(this.plugin, coolTime);
			return;
		}

		if(ammoRemain == ammoSize){
			return;
		}
		ammoRemain = ammoSize;

		if(Pattern.compile("Reload").matcher(name).find()){
			return;
		}

		itemMeta.setDisplayName(itemStack.getItemMeta().getDisplayName() + " [Reload]");
		itemStack.setItemMeta(itemMeta);
		player.getWorld().playSound(player.getLocation(), reloadSound, 0.8f, reloadSoundPitch);

		new ReloadTimer(itemStack, player, itemStack.getItemMeta().getDisplayName().split(" <")[0] + " <"+ammoRemain+"/"+ammoSize+">",
				finishReloadSound, finishReloadSoundPitch).runTaskLater(this.plugin, reloadTime);
	}

	private void deleteStatus(ItemStack itemStack){
		ItemMeta itemMeta = itemStack.getItemMeta();
		String name = itemMeta.getDisplayName();
		if(Pattern.compile("Reload").matcher(name).find()){
			// リロード中のアイテムの場合，リロードの表示を消す
			itemMeta.setDisplayName(name.substring(0, name.indexOf('>')+1));
			itemStack.setItemMeta(itemMeta);
		}
	}

	private class ShotTimer extends BukkitRunnable{
		private ItemStack itemStack;
		private Player player;
		private int ammoRemain;
		private boolean isLast;
		private boolean bulletIsProjectile;

		public ShotTimer(ItemStack itemStack, Player player, int ammoRemain, boolean isLast){
			this.itemStack = itemStack;
			this.player = player;
			this.ammoRemain = ammoRemain;
			this.isLast = isLast;
			this.bulletIsProjectile = Projectile.class.isAssignableFrom(bulletType.getEntityClass()); // 弾がProjectileのサブクラスかどうか
		}

		@Override
		public void run(){
			ItemStack playerItem = player.getItemInHand();
	    	if(!playerItem.hasItemMeta()){
	    		return;
	    	}
		    if(playerItem.equals(itemStack)){
		    	if(recoil != 0){
		    		// リコイルのセット
		    		Vector recoilVec = new Vector(Math.random()-0.5,Math.random()-0.5,Math.random()-0.5).normalize().multiply(recoil);
		    		Vector playerDir = player.getLocation().getDirection().add(recoilVec).normalize();
		    		player.teleport(player.getLocation().setDirection(playerDir));
		    	}

		    	// dispersionに応じて弾をばらけさせる
		    	Vector dispersionVec = new Vector(Math.random()-0.5,Math.random()-0.5,Math.random()-0.5).normalize().multiply(dispersion);
		    	Location eyeloc = player.getEyeLocation();
				Vector vec = eyeloc.getDirection().add(dispersionVec).normalize().multiply(bulletSpeed);

				// 弾の発射
				if(bulletIsProjectile){
					Projectile proj = player.launchProjectile(bulletType.getEntityClass().asSubclass(Projectile.class));
					proj.setVelocity(vec);
				}else{
					Entity bullet = player.getWorld().spawnEntity(eyeloc.add(eyeloc.getDirection().getX()*1.5, eyeloc.getDirection().getY()*1.5, eyeloc.getDirection().getZ()*1.5), bulletType);
					bullet.setVelocity(vec);
				}
				eyeloc.getWorld().playSound(eyeloc, shotSound, 0.8f, shotSoundPitch);

				ItemMeta itemMeta = itemStack.getItemMeta();
				itemMeta.setDisplayName(itemStack.getItemMeta().getDisplayName().split(" <")[0] + " <"+ammoRemain+"/"+ammoSize+">" + cooltimeID);
				itemStack.setItemMeta(itemMeta);
				player.setItemInHand(itemStack);

				if(isLast){
					new CoolTimer(cooltimeID, itemStack, player).runTaskLater(plugin, coolTime);
				}
	    	}
		}
	}
}