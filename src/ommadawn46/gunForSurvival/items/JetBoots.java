package ommadawn46.gunForSurvival.items;

import java.util.List;
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
		List<String> lore = itemStack.getItemMeta().getLore();

		if(lore.size() > 0 && Pattern.compile("CoolTime").matcher(lore.get(lore.size()-1)).find()){
			new BootsCoolTimer(itemStack, player).runTaskLater(this.plugin, coolTime);
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

		player.getWorld().playSound(player.getLocation(), jetSound, 3, jetSoundPitch);

		// loreの最後の行にステータスを記述する
		lore.add("<CoolTime>");
		itemMeta.setLore(lore);

		itemStack.setItemMeta(itemMeta);

		new BootsCoolTimer(itemStack, player).runTaskLater(this.plugin, coolTime);
	}

	private void jump(Player player, ItemStack itemStack){
		ItemMeta itemMeta = itemStack.getItemMeta();
		List<String> lore = itemStack.getItemMeta().getLore();

		if(lore.size() > 0 && Pattern.compile("CoolTime").matcher(lore.get(lore.size()-1)).find()){
			new BootsCoolTimer(itemStack, player).runTaskLater(this.plugin, coolTime);
			return;
		}

		// プレイヤーを上方向に加速させる
		Vector vector = player.getVelocity();
		vector.setY(jump);

		player.setFallDistance(0);
		player.setVelocity(vector);

		player.getWorld().playSound(player.getLocation(), jetSound, 3, jetSoundPitch);

		// loreの最後の行にステータスを記述する
		lore.add("<CoolTime>");
		itemMeta.setLore(lore);

		itemStack.setItemMeta(itemMeta);

		new BootsCoolTimer(itemStack, player).runTaskLater(this.plugin, coolTime);
	}

	public class BootsCoolTimer extends BukkitRunnable {
	    ItemStack itemStack;
	    Player player;

	    public BootsCoolTimer(ItemStack itemStack, Player player) {
	        this.itemStack = itemStack;
	        this.player = player;
	    }

	    @Override
	    public void run() {
	    	ItemStack playerBoots = player.getInventory().getBoots();
	    	if(playerBoots == null){
	    		return;
	    	}
	    	if(!playerBoots.hasItemMeta()){
	    		return;
	    	}
	    	ItemMeta itemMeta = itemStack.getItemMeta();
	    	if(playerBoots.getItemMeta().getDisplayName().equals(itemMeta.getDisplayName())){
	    		List<String> lore = itemMeta.getLore();
	    		if(lore.size() > 0 && Pattern.compile("CoolTime").matcher(lore.get(lore.size()-1)).find()){
	    			lore.remove(lore.size()-1);
	    			itemMeta.setLore(lore);
	    			itemStack.setItemMeta(itemMeta);

	    			PlayerInventory pi = player.getInventory();
	    			pi.setBoots(itemStack);
	    			player.updateInventory();
	    		}
	    	}
	    }
	}
}
