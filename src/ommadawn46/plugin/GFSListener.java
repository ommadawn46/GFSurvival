package ommadawn46.plugin;

import java.util.regex.Pattern;

import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

public class GFSListener implements Listener{
	private GunForSurvival plugin;

	GFSListener(GunForSurvival plugin) {
		this.plugin = plugin;
	}

	@EventHandler
	public void onPlayerInteractEvent(PlayerInteractEvent e) {
		String click = "";
		Action action = e.getAction();
		if (action == Action.LEFT_CLICK_AIR || action == Action.LEFT_CLICK_BLOCK){
			click = "LEFT_CLICK";
		}else if (action == Action.RIGHT_CLICK_AIR || action == Action.RIGHT_CLICK_BLOCK){
			click = "RIGHT_CLICK";
		}

		if(!click.isEmpty()){
			Player player = e.getPlayer();
			ItemStack itemStack = player.getItemInHand();
			GFSItem item = this.plugin.getItem(itemStack);
			if(item != null){
				item.playerAction(player, click);
			}
		}
	}

	@EventHandler
	public void onPlayerToggleSneakEvent(PlayerToggleSneakEvent e){
		Player player = e.getPlayer();
		ItemStack itemStack = player.getItemInHand();
		GFSItem item = this.plugin.getItem(itemStack);
		if(item != null && item instanceof Gun && e.isSneaking()){
			// スニークボタンでリロード
			((Gun)item).playerAction(player, "SNEAK");
		}
	}

	@EventHandler
	public void onPlayerItemHeldEvent(PlayerItemHeldEvent e){
		ItemStack itemStack = e.getPlayer().getInventory().getItem(e.getPreviousSlot());
		GFSItem item = this.plugin.getItem(itemStack);
		if(item != null && item instanceof Gun){
			ItemMeta itemMeta = itemStack.getItemMeta();
			String name = itemMeta.getDisplayName();
			if(Pattern.compile("Reload").matcher(name).find()){
				// リロード中のアイテムを持ち替えた場合，リロードの表示を消す
				itemMeta.setDisplayName(name.substring(0, name.indexOf('>')+1));
				itemStack.setItemMeta(itemMeta);
			}
		}
	}

	@EventHandler
	public void onPlayerDropItemEvent(PlayerDropItemEvent e){
		ItemStack itemStack = e.getItemDrop().getItemStack();
		GFSItem item = this.plugin.getItem(itemStack);
		if(item != null && item instanceof Gun){
			ItemMeta itemMeta = itemStack.getItemMeta();
			String name = itemMeta.getDisplayName();
			if(Pattern.compile("Reload").matcher(name).find()){
				// リロード中のアイテムを捨てた場合，リロードの表示を消す
				itemMeta.setDisplayName(name.substring(0, name.indexOf('>')+1));
				itemStack.setItemMeta(itemMeta);
			}
		}
	}

	@EventHandler
	public void onEntityDamageByEntityEvent(EntityDamageByEntityEvent e){
		if(e.getDamager() instanceof Projectile){
			Projectile proj = (Projectile) e.getDamager();
			// 発射したのはプレイヤーか
			if(proj.getShooter() instanceof Player){
				Player player = (Player) proj.getShooter();
				ItemStack itemStack = player.getItemInHand();
				if(itemStack == null){
					return;
				}
				GFSItem item = this.plugin.getItem(itemStack);
				// プレイヤーが持っているのは銃か
				if(item instanceof Gun){
					((Gun)item).hit(proj, e.getEntity());
				}
			}
		}
	}

	@EventHandler
	public void onProjectileHitEvent(ProjectileHitEvent e){
		Projectile proj = (Projectile) e.getEntity();
		// 発射したのはプレイヤーか
		if(proj.getShooter() instanceof Player){
			Player player = (Player) proj.getShooter();
			ItemStack itemStack = player.getItemInHand();
			if(itemStack == null){
				return;
			}
			GFSItem item = this.plugin.getItem(itemStack);
			// プレイヤーが持っているのは銃か
			if(item instanceof Gun){
				((Gun)item).hit(proj.getLocation());
			}
		}
	}
}