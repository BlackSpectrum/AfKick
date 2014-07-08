package afkick;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.logging.Logger;


import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AfKick extends JavaPlugin{
	
	private final AfKickListener listener = new AfKickListener(this);
	
	public HashMap<Player, AfkInfo> afkPlayers = new HashMap<Player, AfkInfo>();
	public ArrayList<Player> softAfkPlayers = new ArrayList<Player>();
	
	public Logger log = Logger.getLogger("Minecraft");
	
	public FileConfiguration config;
	
	public void onEnable()
	{
		
		this.setConfig();
		
		this.saveConfig();
		
		PluginManager pm = this.getServer().getPluginManager();
		pm.registerEvents(listener, this);
		this.getServer().getScheduler().scheduleSyncRepeatingTask(this, new AfKickCheck(this), 1, 100);
	}
	
	public void onDisable() {
		
	}
	
	public void checkAfks()
	{
		long now = System.currentTimeMillis();
		
		for(Player p : this.getServer().getOnlinePlayers())
		{
			if(this.afkPlayers.containsKey(p))
			{
				if(now >= this.afkPlayers.get(p).getKickAfk() )
				{
					p.kickPlayer(config.getString("kickMessage", "You got kicked for being AFK."));
					String msg = config.getString("broadcast.kick", "%p got kicked for bein AFK.").replaceAll("%p", p.getName());
					this.getServer().broadcastMessage(msg);
				}
				else
				{
					if(!softAfkPlayers.contains(p) && now >= this.afkPlayers.get(p).getSoftAfk())
					{
						String msg = config.getString("broadcast.softAFK", "%p is now AFK.").replaceAll("%p", p.getName());
						this.getServer().broadcastMessage(msg);
						softAfkPlayers.add(p);
					}
				}
			}
			else
			{
				afkPlayers.put(p, new AfkInfo(now + (config.getLong("timeUntil.softAFK", 300) * 1000), now + (config.getLong("timeUntil.kick", 420) * 1000)));
			}
		}
	}
	
	private void setConfig()
	{
		config = this.getConfig();
		
		config.set("timeUntil.softAFK", config.getLong("timeUntil.softAFK", 300));
		config.set("timeUntil.Kick",config.getLong("timeUntil.kick", 420));
		config.set("denyItemPickup",config.getBoolean("denyItemPickup", true));
		config.set("broadcast.softAFK",config.getString("broadcast.softAFK", "%p is now AFK."));
		config.set("broadcast.nologerAFK",config.getString("broadcast.nolongerAFK", "%p is no longer AFK."));
		config.set("broadcast.kick", config.getString("broadcast.kick", "%p got kicked for bein AFK."));
		config.set("kickMessage",config.getString("kickMessage", "You got kicked for being AFK."));
	}
	
	
	

}
