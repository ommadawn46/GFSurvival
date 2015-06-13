package ommadawn46.gunForSurvival;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import ommadawn46.gunForSurvival.items.FlyingPotion;
import ommadawn46.gunForSurvival.items.GFSItem;
import ommadawn46.gunForSurvival.items.Gun;
import ommadawn46.gunForSurvival.items.JetBoots;
import ommadawn46.gunForSurvival.items.TeleportGun;
import ommadawn46.gunForSurvival.items.ThunderRod;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
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
    	}else if(cmd.getName().equalsIgnoreCase("gfslist")){
    		if(args.length != 0){
    			return false;
    		}

    		// アイテムのリストを表示
    		sender.sendMessage(ChatColor.YELLOW + "------GFS Items------");
    		for(String key: itemMap.keySet()){
        		sender.sendMessage("  " + key);
    		}
    		sender.sendMessage(ChatColor.YELLOW + "---------------------");

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
			// データフォルダの作成
			getDataFolder().mkdir();

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

    	for(String itemType: itemData.getKeys(false)){
    		List<Map<?, ?>> itemList = itemData.getMapList(itemType);
    		for(Map<?, ?> itemInfo: itemList){
    			String name = (String) itemInfo.get("Name");
    			try{
	    			if(itemType.equals("Gun")){
	    				// 銃
	    				itemMap.put(name, new Gun(this, itemInfo));
	    			}else if(itemType.equals("TeleportGun")){
	    				// テレポート銃
	    				itemMap.put(name, new TeleportGun(this, itemInfo));
	    			}else if(itemType.equals("ThunderRod")){
	    				// 雷の杖
	    				itemMap.put(name, new ThunderRod(this, itemInfo));
	    			}else if(itemType.equals("FlyingPotion")){
	    				// 飛行ポーション
	    				itemMap.put(name, new FlyingPotion(this, itemInfo));
	    			}else if(itemType.equals("JetBoots")){
	    				// ジェットブーツ
	    				itemMap.put(name, new JetBoots(this, itemInfo));
	    			}else{
	    				throw new Exception();
	    			}
    			}catch(Exception e){
    				System.out.println("!Load Error <" + itemType + ": " + name + ">");
    				continue;
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

    	// ItemStackの情報をもとにitemMapの中から該当するアイテムを探す
    	for(GFSItem item: itemMap.values()){
    		if(item.isThisItem(itemStack)){
    			return item;
    		}
    	}

    	return null;
    }

    private ItemStack getItemStack(String name) {
        if(!itemMap.containsKey(name)){
        	System.out.println(name + " is not exist.");
        	return null;
        }

        return itemMap.get(name).getItemStack();
    }

    public String getColorFromNumber(int num){
    	if(num >= 1000){
    		num = 999;
    	}
    	int hund = num / 100;
    	int teen = (num % 100) / 10;
    	int digit = num % 10;

    	String str = "" + ChatColor.UNDERLINE + getColorFromDigit(hund) + getColorFromDigit(teen) + getColorFromDigit(digit) + ChatColor.RESET;

    	return str;
    }

    public int getNumberFromColor(String str){
    	str = str.replaceAll(""+ChatColor.UNDERLINE, "").replaceAll(""+ChatColor.RESET, "");
    	if(str.length() != 6){
    		return -1;
    	}
    	String hund = str.substring(0, 2);
    	String teen = str.substring(2, 4);
    	String digit = str.substring(4, 6);

    	int num = getDigitFromColor(hund)*100 + getDigitFromColor(teen)*10 + getDigitFromColor(digit);

    	return num;
    }

    private ChatColor getColorFromDigit(int digit){
    	switch(digit){
    		case 0: return ChatColor.AQUA;
    		case 1: return ChatColor.BLACK;
    		case 2: return ChatColor.BLUE;
    		case 3: return ChatColor.DARK_AQUA;
    		case 4: return ChatColor.DARK_BLUE;
    		case 5: return ChatColor.DARK_GRAY;
    		case 6: return ChatColor.DARK_GREEN;
    		case 7: return ChatColor.DARK_PURPLE;
    		case 8: return ChatColor.DARK_RED;
    		case 9: return ChatColor.GOLD;
    		default : return null;
    	}
    }

    private int getDigitFromColor(String color){
    	if(color.equals(ChatColor.AQUA.toString())) return 0;
    	else if(color.equals(ChatColor.BLACK.toString())) return 1;
    	else if(color.equals(ChatColor.BLUE.toString())) return 2;
    	else if(color.equals(ChatColor.DARK_AQUA.toString())) return 3;
    	else if(color.equals(ChatColor.DARK_BLUE.toString())) return 4;
    	else if(color.equals(ChatColor.DARK_GRAY.toString())) return 5;
    	else if(color.equals(ChatColor.DARK_GREEN.toString())) return 6;
    	else if(color.equals(ChatColor.DARK_PURPLE.toString())) return 7;
    	else if(color.equals(ChatColor.DARK_RED.toString())) return 8;
    	else if(color.equals(ChatColor.GOLD.toString())) return 9;
    	else return 0;
    }
}