package afkick;

import java.util.HashMap;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.Server;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AfKick extends JavaPlugin
{
	private final AfKickListener listener = new AfKickListener(this);
	
	public final Server server = Bukkit.getServer();
	
	public HashMap<UUID, AfkInfo> afkPlayers = new HashMap<UUID, AfkInfo>();
	public FileConfiguration config;
	
	private int taskId = -1;
	
	@Override
	public void onEnable()
	{
		setConfig();
		afkPlayers.clear();
		
		long now = System.currentTimeMillis();
		
		for (Player p : server.getOnlinePlayers()) {
			
			this.afkPlayers.put(p.getUniqueId(), new AfkInfo(now + this.config.getLong("timeUntil.tagAFK", 300L) * 1000L, 
			now + this.config.getLong("timeUntil.kick", 540L) * 1000L, p.getLocation()));
		}
		
		PluginManager pm = server.getPluginManager();
		pm.registerEvents(this.listener, this);
		taskId = server.getScheduler().scheduleSyncRepeatingTask(this, new Runnable(){
													public void run(){
														checkAfks();
													}
												}, 
					this.config.getInt("checkperiodInTicks", 100), this.config.getInt("checkperiodInTicks", 100));
		
	}
	
	@Override
	public void onDisable()
	{
		this.afkPlayers.clear();
		server.getScheduler().cancelTask(taskId);
	}
	
	public void checkAfks()
	{
		long now = System.currentTimeMillis();
		for (UUID uid : afkPlayers.keySet()) 
		{
			
			Player p = server.getPlayer(uid);

			AfkInfo afkInfo = this.afkPlayers.get(uid);
			if (afkInfo.isAfk())
			{
				if ((!p.hasPermission("afkick.nokick")) && (afkInfo.isAfk()) && (now >= afkInfo.getKickAfk()))
				{
					p.kickPlayer(this.config.getString("kickMessage", "You got kicked for being AFK."));
					String msg = this.config.getString("broadcast.kick", "%p got kicked for being AFK.").replaceAll("%p", p.getName());
					server.broadcastMessage(msg);
				}
				else
				{
					if ((!p.hasPermission("afkick.notag")) && (!afkInfo.isInactive()) && (now >= afkInfo.getTagAfkTime() - 30000L))
					{
						afkInfo.generateCapChar();
						p.sendMessage("You did not walk for too long and got marked as inactive - please move or answer " + afkInfo.getCapCharQ() + "=? in chat.");
						afkInfo.setInactive(true);
					}
					if ((!p.hasPermission("afkick.notag")) && (!afkInfo.isTagAfk()) && (now >= afkInfo.getTagAfkTime()))
					{
						String msg = this.config.getString("broadcast.tagAFK", "%p is now AFK.").replaceAll("%p", p.getName());
						server.broadcastMessage(msg);
						afkInfo.setTagAfk(true);
					}
				}
			}
			else
			{
				afkInfo.setAfk(true);
				afkInfo.setLastLoc(p.getLocation());
				afkInfo.setTagAfkTime(now + this.config.getLong("timeUntil.tagAFK", 300L) * 1000L);
				afkInfo.setKickAfk(now + this.config.getLong("timeUntil.kick", 540L) * 1000L);
			}
		
		}
	}
	
	@Override
	public boolean onCommand(CommandSender sender, Command cmd, String commandLabel, String[] args)
	{
		if (((sender instanceof Player)) && (cmd.getName().equalsIgnoreCase("afk")))
		{
			Player player = (Player)sender;
			if (this.afkPlayers.containsKey(player))
			{
				AfkInfo afkInfo = this.afkPlayers.get(player);
				if (afkInfo.isTagAfk())
				{
					if (afkInfo.isInactive()) 
						return true;
				
					afkInfo.setTagAfk(false);
					
					String msg = this.config.getString("broadcast.nolongerAFK", "%p is no longer AFK.").replaceAll("%p", player.getName());
					server.broadcastMessage(msg);
					
					return true;
				}
				
				afkInfo.setAfk(true);
				afkInfo.setTagAfk(true);
				String msg = this.config.getString("broadcast.tagAFK", "%p is now AFK.").replaceAll("%p", player.getName());
				
				server.broadcastMessage(msg);
				return true;
			}
		
			long now = System.currentTimeMillis();
			this.afkPlayers.put(player.getUniqueId(), new AfkInfo(now, 
													now + this.config.getLong("timeUntil.kick", 540L) * 1000L,
													player.getLocation())	);
			
			String msg = this.config.getString("broadcast.tagAFK", "%p is now AFK.").replaceAll("%p", player.getName());
			server.broadcastMessage(msg);
			
			return true;
		}
		
		return false;
	}
	
	public void addPlayer(Player p)
	{
		if (p.hasPermission("afkick.noafk"))
			return;
		
		long now = System.currentTimeMillis();
		this.afkPlayers.put(p.getUniqueId(), new AfkInfo(now + this.config.getLong("timeUntil.tagAFK", 300L) * 1000L, 
				now + (this.config.getLong("timeUntil.kick", 540L) - this.config.getLong("timeUntil.tagAFK", 300L) * 1000L), p.getLocation()));
	}
	
	public void removePlayer(Player p)
	{
		if (afkPlayers.containsKey(p.getUniqueId())) 
			afkPlayers.remove(p.getUniqueId());
	}
	
	public AfkInfo getAfkInfo(Player p)
	{
		return afkPlayers.get(p.getUniqueId());
	}
	
	private void setConfig()
	{
		this.config = getConfig();
		
		this.config.set("timeUntil.tagAFK", Long.valueOf(this.config.getLong("timeUntil.tagAFK", 300L)));
		this.config.set("timeUntil.kick", Long.valueOf(this.config.getLong("timeUntil.kick", 540L)));
		this.config.set("denyItemPickup", Boolean.valueOf(this.config.getBoolean("denyItemPickup", true)));
		this.config.set("broadcast.tagAFK", this.config.getString("broadcast.tagAFK", "%p is now AFK."));
		this.config.set("broadcast.nologerAFK", this.config.getString("broadcast.nolongerAFK", "%p is no longer AFK."));
		this.config.set("broadcast.kick", this.config.getString("broadcast.kick", "%p got kicked for being AFK."));
		this.config.set("kickMessage", this.config.getString("kickMessage", "You got kicked for being AFK."));
		this.config.set("checkperiodInTicks", Integer.valueOf(this.config.getInt("checkperiodInTicks", 100)));
		
		saveConfig();
	}
}
