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

	private int coolTime;
	private double hopUp;
	private double horizontalAccel;
	private double upAccel;
	private double downAccel;
	private double jump;
	private boolean noFallingDamage;
	private Sound jetSound;
	private float jetSoundPitch;

	public JetBoots(GunForSurvival plugin, Map<?, ?> itemInfo) {
		super(plugin, itemInfo);
		this.identifier = ChatColor.YELLOW + "" + ChatColor.RESET;

		this.coolTime = Integer.parseInt((String) itemInfo.get("Cooltime"));

		this.hopUp = Double.parseDouble((String) itemInfo.get("HopUp"));
		this.horizontalAccel = Double.parseDouble((String) itemInfo.get("HorizontalAccel"));
		this.upAccel = Double.parseDouble((String) itemInfo.get("UpAccel"));
		this.downAccel = Double.parseDouble((String) itemInfo.get("DownAccel"));
		this.jump = Double.parseDouble((String) itemInfo.get("Jump"));

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
	public void playerAction(Player player, ItemStack itemStack, String action) {
		if(action.equals("SPLINT")){
			accel(player, itemStack);
		}else if(action.equals("SNEAK")){
			jump(player, itemStack);
		}
	}

	private void accel(Player player, ItemStack itemStack){
		ItemMeta itemMeta = itemStack.getItemMeta();
		String name = itemMeta.getDisplayName();

		if(Pattern.compile(cooltimeID).matcher(name).find()){
			new BootsCoolTimer(cooltimeID, itemStack, player).runTaskLater(this.plugin, coolTime);
			return;
		}

		// プレイヤーを加速させる
		Vector vector = player.getLocation().getDirection();
		vector.setX(vector.getX() * horizontalAccel);
		vector.setZ(vector.getZ() * horizontalAccel);
		if(vector.getY() > 0){
			vector.setY(hopUp + vector.getY() * upAccel);
		}else{
			vector.setY(hopUp + vector.getY() * downAccel);
		}
		player.setFallDistance(0);
		player.setVelocity(vector);

		player.getWorld().playSound(player.getLocation(), jetSound, 0.8f, jetSoundPitch);

		itemMeta.setDisplayName(name + cooltimeID);
		itemStack.setItemMeta(itemMeta);

		new BootsCoolTimer(cooltimeID, itemStack, player).runTaskLater(this.plugin, coolTime);
	}

	private void jump(Player player, ItemStack itemStack){
		ItemMeta itemMeta = itemStack.getItemMeta();
		String name = itemMeta.getDisplayName();

		if(Pattern.compile(cooltimeID).matcher(name).find()){
			new BootsCoolTimer(cooltimeID, itemStack, player).runTaskLater(this.plugin, coolTime);
			return;
		}

		if(jump <= 0){
			return;
		}

		// プレイヤーを上方向に加速させる
		Vector vector = player.getVelocity();
		vector.setY(jump);

		player.setFallDistance(0);
		player.setVelocity(vector);

		player.getWorld().playSound(player.getLocation(), jetSound, 0.8f, jetSoundPitch);

		itemMeta.setDisplayName(name + cooltimeID);
		itemStack.setItemMeta(itemMeta);

		new BootsCoolTimer(cooltimeID, itemStack, player).runTaskLater(this.plugin, coolTime);
	}

	public class BootsCoolTimer extends BukkitRunnable {
		String cooltimeID;
	    ItemStack itemStack;
	    Player player;

	    public BootsCoolTimer(String cooltimeID, ItemStack itemStack, Player player) {
	    	this.cooltimeID = cooltimeID;
	        this.itemStack = itemStack;
	        this.player = player;
	    }

	    @Override
	    public void run() {
	    	ItemStack playerBoots = player.getInventory().getBoots();
	    	if(!playerBoots.hasItemMeta()){
	    		return;
	    	}
	    	ItemMeta itemMeta = itemStack.getItemMeta();
	    	String name = itemMeta.getDisplayName();
	    	if(playerBoots.getItemMeta().getDisplayName().equals(itemMeta.getDisplayName())){
	    		if(Pattern.compile(cooltimeID).matcher(name).find()){
	    			name = name.split(cooltimeID)[0];
	    			itemMeta.setDisplayName(name);;
	    			itemStack.setItemMeta(itemMeta);

	    			PlayerInventory pi = player.getInventory();
	    			pi.setBoots(itemStack);
	    			player.updateInventory();
	    		}
	    	}
	    }
	}
}
