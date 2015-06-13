package ommadawn46.gunForSurvival.items;

import java.util.Map;
import java.util.regex.Pattern;

import ommadawn46.gunForSurvival.GunForSurvival;

import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.Vector;

public class JetBoots extends GFSItem{
	private final String cooltimeID = "" + ChatColor.YELLOW + ChatColor.WHITE + ChatColor.RESET;

	private int jetAmmo;
	private int jetInterval;
	private int coolTime;
	private double hopUp;
	private double horizontalAccel;
	private double upAccel;
	private double downAccel;
	private boolean setAccel;
	private boolean noFallingDamage;
	private Sound jetSound;
	private float jetSoundPitch;

	public JetBoots(GunForSurvival plugin, Map<?, ?> itemInfo) {
		super(plugin, itemInfo);
		this.identifier = ChatColor.YELLOW + "" + ChatColor.RESET;

		this.jetAmmo = Integer.parseInt((String) itemInfo.get("JetAmmo"));
		this.jetInterval = Integer.parseInt((String) itemInfo.get("JetInterval"));
		this.coolTime = Integer.parseInt((String) itemInfo.get("Cooltime"));

		this.hopUp = Double.parseDouble((String) itemInfo.get("HopUp"));
		this.horizontalAccel = Double.parseDouble((String) itemInfo.get("HorizontalAccel"));
		this.upAccel = Double.parseDouble((String) itemInfo.get("UpAccel"));
		this.downAccel = Double.parseDouble((String) itemInfo.get("DownAccel"));
		this.setAccel = (boolean)itemInfo.get("SetAccel");

		this.noFallingDamage = (boolean)itemInfo.get("NoFallingDamage");

		this.jetSound = Sound.valueOf((String)itemInfo.get("JetSound"));
		this.jetSoundPitch = Float.parseFloat((String)itemInfo.get("JetSoundPitch"));

		this.displayName = makeDisplayName(rawName);

		ItemMeta itemMeta = orgItemStack.getItemMeta();
		itemMeta.setDisplayName(this.displayName);
		orgItemStack.setItemMeta(itemMeta);
	}

	public boolean isNoFallingDamage(){
		return noFallingDamage;
	}

	@Override
	protected String makeDisplayName(String rawName) {
		// 表示名の設定
		return identifier + rawName + plugin.getColorFromNumber(jetAmmo);
	}

	@Override
	protected String getRawNameFromDisplayName(String name) {
		// 表示名から本来の名前を取得
		return name.split(identifier)[1].split(ChatColor.UNDERLINE.toString())[0];
	}

	private int getAmmoRemain(String name){
		// 表示名から残弾数を取得
		return plugin.getNumberFromColor(name.split(ChatColor.UNDERLINE.toString())[1].split(ChatColor.RESET.toString())[0]);
	}

	@Override
	public void playerAction(Player player, ItemStack itemStack, String action) {
		if(action.equals("SPLINT")){
			accel(player, itemStack);
		}else if(action.equals("DELETESTATUS")){
			deleteStatus(itemStack);
		}
	}

	private void accel(Player player, ItemStack itemStack){
		ItemMeta itemMeta = itemStack.getItemMeta();
		String name = itemMeta.getDisplayName();
		int ammoRemain = getAmmoRemain(name);

		if(ammoRemain > 0){
			new AccelTimer(itemStack, player).runTaskLater(plugin, 0);
		}
	}

	private void deleteStatus(ItemStack itemStack){
		ItemMeta itemMeta = itemStack.getItemMeta();
		String name = itemMeta.getDisplayName();
		if(Pattern.compile(cooltimeID).matcher(name).find()){
			// リロード中のアイテムの場合，リロードの表示を消す
			itemMeta.setDisplayName(name.split(ChatColor.UNDERLINE.toString())[0] + plugin.getColorFromNumber(jetAmmo));
			itemStack.setItemMeta(itemMeta);
		}
	}

	public class AccelTimer extends BukkitRunnable{
		ItemStack itemStack;
		Player player;

		public AccelTimer(ItemStack itemStack, Player player) {
			this.itemStack = itemStack;
			this.player = player;
		}

		@Override
		public void run() {
			ItemStack playerBoots = player.getInventory().getBoots();
			int ammoRemain = getAmmoRemain(playerBoots.getItemMeta().getDisplayName());
	    	if(!playerBoots.hasItemMeta()){
	    		return;
	    	}

	    	ItemMeta itemMeta = itemStack.getItemMeta();
			if(ammoRemain > 0 &&player.isSprinting() && playerBoots.equals(itemStack)){
				// プレイヤーを加速させる
				Vector vector = player.getLocation().getDirection();
				vector.setX(vector.getX() * horizontalAccel);
				vector.setZ(vector.getZ() * horizontalAccel);
				if(vector.getY() > 0){
					vector.setY(hopUp + vector.getY() * upAccel);
				}else{
					vector.setY(hopUp + vector.getY() * downAccel);
				}

				if(!setAccel){
					vector = player.getVelocity().add(vector);
				}

				player.setFallDistance(0);
				player.setVelocity(vector);

				player.getWorld().playSound(player.getLocation(), jetSound, 0.8f, jetSoundPitch);

				itemMeta.setDisplayName(itemStack.getItemMeta().getDisplayName().split(ChatColor.UNDERLINE.toString())[0] + plugin.getColorFromNumber(--ammoRemain) + cooltimeID);
				itemStack.setItemMeta(itemMeta);
				PlayerInventory pi = player.getInventory();
				pi.setBoots(itemStack);
				player.updateInventory();
				if(ammoRemain > 0){
					new AccelTimer(itemStack, player).runTaskLater(plugin, jetInterval);
				}
				new BootsCoolTimer(cooltimeID, itemMeta.getDisplayName(), player).runTaskLater(plugin, coolTime);
	    	}
		}
	}

	public class BootsCoolTimer extends BukkitRunnable {
		String cooltimeID;
	    String dispName;
	    Player player;

	    public BootsCoolTimer(String cooltimeID, String dispName, Player player) {
	    	this.cooltimeID = cooltimeID;
	        this.dispName = dispName;
	        this.player = player;
	    }

	    @Override
	    public void run() {
	    	ItemStack playerBoots = player.getInventory().getBoots();
	    	if(playerBoots == null || !playerBoots.hasItemMeta()){
	    		return;
	    	}
	    	if(Pattern.compile(cooltimeID).matcher(dispName).find() &&
    			playerBoots.getItemMeta().getDisplayName().equals(dispName)){
    			ItemMeta itemMeta = playerBoots.getItemMeta();
    			String newName = dispName.split(ChatColor.UNDERLINE.toString())[0] + plugin.getColorFromNumber(jetAmmo);
    			itemMeta.setDisplayName(newName);
    			playerBoots.setItemMeta(itemMeta);

    			PlayerInventory pi = player.getInventory();
    			pi.setBoots(playerBoots);
    			player.updateInventory();
	    	}
	    }
	}
}
