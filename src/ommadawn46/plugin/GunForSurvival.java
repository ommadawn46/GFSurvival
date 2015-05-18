package ommadawn46.plugin;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

public class GunForSurvival extends JavaPlugin{
	HashMap<String, GFSItem> itemMap;

	@Override
    public void onEnable() {
		itemMap = loadItems();
		getServer().getPluginManager().registerEvents(new GFSListener(this), this);
    }

    @Override
    public void onDisable() {
    	getServer().resetRecipes();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args){
    	if(cmd.getName().equalsIgnoreCase("gfs")){
    		if(args.length != 2){
    			return false;
    		}

    		Player player = getPlayer(args[0]);
    		if(player == null){
    			sender.sendMessage("存在しないプレイヤー: " + args[0]);
    			return false;
    		}

    		ItemStack item = getItemStack(args[1]);
    		if(item == null){
    			sender.sendMessage("存在しないアイテム: " + args[1]);
    			return false;
    		}

    		// プレイヤーのインベントリにアイテムを追加
    		if(player.getInventory().contains(item)){
    			sender.sendMessage("既にアイテムを持っています");
    		}else{
    			player.getInventory().addItem(item);
    		}

    		return true;
    	}
    	return false;
    }

    // アイテムの読み込み
    private HashMap<String, GFSItem> loadItems(){
    	itemMap = new HashMap<String, GFSItem>();

    	String filePath = getDataFolder() + File.separator + "items.yml";
    	FileConfiguration itemData = new YamlConfiguration();

    	// ファイルの読み込み
    	try {
			itemData.load(filePath);
		} catch (IOException | InvalidConfigurationException e1) {
			// ファイルが存在しない時はデフォルトファイルをコピーする
			InputStream inputStream = this.getClass().getResourceAsStream("/items.yml");
			try {
				OutputStream outputStream = new FileOutputStream(filePath);
				int DEFAULT_BUFFER_SIZE = 1024 * 4;
				byte[] buffer = new byte[DEFAULT_BUFFER_SIZE];
				int size = -1;
				while ((size = inputStream.read(buffer)) != -1) {
					outputStream.write(buffer, 0, size);
				}
				inputStream.close();
				outputStream.close();

				itemData.load(filePath);
			} catch (IOException | InvalidConfigurationException e2) {
				e2.printStackTrace();
			}

		}

    	// アイテムの種類を設定する
    	List<String> itemTypes = new ArrayList<String>();
    	itemTypes.add("Gun");
    	itemTypes.add("TeleportGun");

    	for(String itemType: itemTypes){
    		List<Map<?, ?>> itemList = itemData.getMapList(itemType);
    		for(Map<?, ?> itemInfo: itemList){
    			String name = (String) itemInfo.get("Name");
    			Material material = Material.valueOf((String)itemInfo.get("Material"));
    			@SuppressWarnings("unchecked")
    			List<String> lore = (List<String>) itemInfo.get("Lore");
    			if(itemType.equals("Gun")){
    				int ammoSize = Integer.parseInt((String) itemInfo.get("AmmoSize"));
    				int cooltime = Integer.parseInt((String) itemInfo.get("Cooltime"));
    				int reloadtime = Integer.parseInt((String) itemInfo.get("Reloadtime"));

    				EntityType bulletType = EntityType.valueOf((String)itemInfo.get("BulletType"));
    				double bulletDamage = Double.parseDouble((String) itemInfo.get("BulletDamage"));
    				double bulletSpeed = Double.parseDouble((String) itemInfo.get("BulletSpeed"));

    				Sound shotSound = Sound.valueOf((String)itemInfo.get("ShotSound"));
    				float shotSoundPitch = Float.parseFloat((String)itemInfo.get("ShotSoundPitch"));
    				Sound reloadSound = Sound.valueOf((String)itemInfo.get("ReloadSound"));
    				float reloadSoundPitch = Float.parseFloat((String)itemInfo.get("ReloadSoundPitch"));
    				Sound finishReloadSound = Sound.valueOf((String)itemInfo.get("FinishReloadSound"));
    				float finishReloadSoundPitch = Float.parseFloat((String)itemInfo.get("FinishReloadSoundPitch"));

    				itemMap.put(name, new Gun(this, name, material, lore, ammoSize, cooltime, reloadtime,
    						bulletType, bulletDamage, bulletSpeed,
    						shotSound, shotSoundPitch, reloadSound, reloadSoundPitch, finishReloadSound, finishReloadSoundPitch));
    			}else if(itemType.equals("TeleportGun")){
    				int ammoSize = Integer.parseInt((String) itemInfo.get("AmmoSize"));
    				int cooltime = Integer.parseInt((String) itemInfo.get("Cooltime"));
    				int reloadtime = Integer.parseInt((String) itemInfo.get("Reloadtime"));
    				int range = Integer.parseInt((String) itemInfo.get("Range"));

    				Sound shotSound = Sound.valueOf((String)itemInfo.get("ShotSound"));
    				float shotSoundPitch = Float.parseFloat((String)itemInfo.get("ShotSoundPitch"));
    				Sound reloadSound = Sound.valueOf((String)itemInfo.get("ReloadSound"));
    				float reloadSoundPitch = Float.parseFloat((String)itemInfo.get("ReloadSoundPitch"));
    				Sound finishReloadSound = Sound.valueOf((String)itemInfo.get("FinishReloadSound"));
    				float finishReloadSoundPitch = Float.parseFloat((String)itemInfo.get("FinishReloadSoundPitch"));

    				itemMap.put(name, new TeleportGun(this, name, material, lore, ammoSize, cooltime, reloadtime, range,
    						shotSound, shotSoundPitch, reloadSound, reloadSoundPitch, finishReloadSound, finishReloadSoundPitch));
    			}
    			System.out.println(itemType + ": " + name + " is Loaded");
    		}
    	}

    	return itemMap;
    }

    private Player getPlayer(String name) {
        for ( Player player : Bukkit.getOnlinePlayers() ) {
            if ( player.getName().equals(name) ) {
                return player;
            }
        }
        return null;
    }

    // アイテムの種類を調べる
    public GFSItem getItem(ItemStack itemStack) {
    	if(itemStack == null){
    		return null;
    	}
    	if(!itemStack.hasItemMeta()){
    		return null;
    	}
    	ItemMeta itemMeta = itemStack.getItemMeta();
    	String name = itemMeta.getDisplayName();
    	if(name == null){
    		return null;
    	}

    	if(Pattern.compile("^"+ChatColor.GRAY+".*<[0-9]+/[0-9]+>").matcher(name).find()){
    		return new Gun(this, itemStack);
    	}else if(Pattern.compile("^"+ChatColor.DARK_AQUA+".*<[0-9]+/[0-9]+>").matcher(name).find()){
    		return new TeleportGun(this, itemStack);
    	}else{
    		return null;
    	}
    }

    private ItemStack getItemStack(String name) {
        if(!itemMap.containsKey(name)){
        	System.out.println(name + " is not exist.");
        	return null;
        }

        return itemMap.get(name).getItemStack();
    }
}