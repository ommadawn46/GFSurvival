package ommadawn46.gunForSurvival;

import ommadawn46.gunForSurvival.items.GFSItem;
import ommadawn46.gunForSurvival.items.Gun;
import ommadawn46.gunForSurvival.items.JetBoots;
import ommadawn46.gunForSurvival.items.TeleportGun;
import ommadawn46.gunForSurvival.items.ThunderRod;

import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityDamageEvent.DamageCause;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerToggleSneakEvent;
import org.bukkit.event.player.PlayerToggleSprintEvent;
import org.bukkit.inventory.ItemStack;

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
				item.playerAction(player, itemStack, click);
			}
		}
	}

	@EventHandler
	public void onPlayerToggleSprintEvent(PlayerToggleSprintEvent e){
		Player player = e.getPlayer();
		ItemStack boots = player.getInventory().getArmorContents()[0];
		GFSItem item = this.plugin.getItem(boots);
		if(item != null && e.isSprinting()){
			item.playerAction(player, boots, "SPLINT");
		}
	}

	@EventHandler
	public void onPlayerToggleSneakEvent(PlayerToggleSneakEvent e){
		Player player = e.getPlayer();
		ItemStack itemStack = player.getItemInHand();
		GFSItem item = this.plugin.getItem(itemStack);
		if(item != null && e.isSneaking()){
			item.playerAction(player, itemStack, "SNEAK");
		}

		ItemStack boots = player.getInventory().getBoots();
		item = this.plugin.getItem(boots);
		if(item != null && e.isSneaking()){
			item.playerAction(player, boots, "SNEAK");
		}
	}

	@EventHandler
	public void onPlayerItemConsumeEvent(PlayerItemConsumeEvent e){
		ItemStack itemStack = e.getItem();
		GFSItem item = this.plugin.getItem(itemStack);
		if(item != null){
			item.playerAction(e.getPlayer(), itemStack, "CONSUME");
		}
	}

	@EventHandler
	public void onPlayerItemHeldEvent(PlayerItemHeldEvent e){
		ItemStack itemStack = e.getPlayer().getInventory().getItem(e.getPreviousSlot());
		deleteStatus(itemStack);
	}

	@EventHandler
	public void onPlayerDropItemEvent(PlayerDropItemEvent e){
		ItemStack itemStack = e.getItemDrop().getItemStack();
		deleteStatus(itemStack);
	}

	@EventHandler
	public void onInventoryClickEvent(InventoryClickEvent e){
		ItemStack itemStack = e.getCurrentItem();
		deleteStatus(itemStack);
	}

	@EventHandler
	public void onEntityDeathEvent(EntityDeathEvent e){
		Entity entity = e.getEntity();
		if(entity instanceof Player){
			ItemStack itemStack = ((Player)entity).getItemInHand();
			deleteStatus(itemStack);
			ItemStack boots = ((Player)entity).getInventory().getBoots();
			deleteStatus(boots);
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
					((Gun)item).hit(proj, e);
				}
			}
		}else if(e.getDamager().getType().equals(EntityType.LIGHTNING)){
			if(e.getEntity() instanceof Player){
				Player player = (Player) e.getEntity();
				ItemStack itemStack = player.getItemInHand();
				if(itemStack == null){
					return;
				}
				GFSItem item = this.plugin.getItem(itemStack);
				if(item instanceof ThunderRod){
					// プレイヤーが雷の杖を持っているときはダメージを無効化
					e.setCancelled(true);
				}
			}
		}
	}

	@EventHandler
	public void onEntityDamageEvent(EntityDamageEvent e){
		if(e.getCause().equals(DamageCause.FALL)){
			if(e.getEntity() instanceof Player){
				Player player = (Player) e.getEntity();
				ItemStack boots = player.getInventory().getBoots();
				GFSItem item = this.plugin.getItem(boots);
				if(item instanceof JetBoots && ((JetBoots)item).isNoFallingDamage()){
					e.setCancelled(true);
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

	public void deleteStatus(ItemStack itemStack){
		GFSItem item = this.plugin.getItem(itemStack);
		if(item != null && (item instanceof Gun || item instanceof TeleportGun || item instanceof JetBoots)){
			item.playerAction(null, itemStack, "DELETESTATUS");
		}
	}
}