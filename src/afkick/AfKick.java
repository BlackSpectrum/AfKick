package afkick;

import java.util.HashMap;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.PluginManager;
import org.bukkit.plugin.java.JavaPlugin;

public class AfKick extends JavaPlugin
{
	private final AfKickListener listener = new AfKickListener(this);
	public HashMap<Player, AfkInfo> afkPlayers = new HashMap<Player, AfkInfo>();
	public FileConfiguration config;
	
	@Override
	public void onEnable()
	{
		setConfig();
		
		long now = System.currentTimeMillis();
		for (Player p : getServer().getOnlinePlayers()) {
		this.afkPlayers.put(p, new AfkInfo(now + this.config.getLong("timeUntil.tagAFK", 300L) * 1000L, 
		now + this.config.getLong("timeUntil.kick", 540L) * 1000L, p.getLocation()));
		}
		PluginManager pm = getServer().getPluginManager();
		pm.registerEvents(this.listener, this);
		getServer().getScheduler().scheduleSyncRepeatingTask(this, new AfKickCheck(this), 
		this.config.getInt("checkperiodInTicks", 100), this.config.getInt("checkperiodInTicks", 100));
	}
	
	@Override
	public void onDisable()
	{
		this.afkPlayers.clear();
	}
	
	public void checkAfks()
	{
		long now = System.currentTimeMillis();
		for (Player p : getServer().getOnlinePlayers()) 
		{
			if (p.hasPermission("afkick.noafk"))
				continue;
			
			if (this.afkPlayers.containsKey(p))
			{
				AfkInfo afkInfo = this.afkPlayers.get(p);
				if (afkInfo.isAfk())
				{
					if ((!p.hasPermission("afkick.nokick")) && (afkInfo.isAfk()) && (now >= afkInfo.getKickAfk()))
					{
						p.kickPlayer(this.config.getString("kickMessage", "You got kicked for being AFK."));
						String msg = this.config.getString("broadcast.kick", "%p got kicked for being AFK.").replaceAll("%p", p.getName());
						getServer().broadcastMessage(msg);
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
							getServer().broadcastMessage(msg);
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
			else
			{
				this.afkPlayers.put(p, new AfkInfo(now + this.config.getLong("timeUntil.tagAFK", 300L) * 1000L, 
						now + (this.config.getLong("timeUntil.kick", 540L) - this.config.getLong("timeUntil.tagAFK", 300L) * 1000L), p.getLocation()));
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
					getServer().broadcastMessage(msg);
					
					return true;
				}
				
				afkInfo.setAfk(true);
				afkInfo.setTagAfk(true);
				String msg = this.config.getString("broadcast.tagAFK", "%p is now AFK.").replaceAll("%p", player.getName());
				
				getServer().broadcastMessage(msg);
				return true;
			}
		
			long now = System.currentTimeMillis();
			this.afkPlayers.put(player, new AfkInfo(now, 
													now + this.config.getLong("timeUntil.kick", 540L) * 1000L,
													player.getLocation())	);
			
			String msg = this.config.getString("broadcast.tagAFK", "%p is now AFK.").replaceAll("%p", player.getName());
			getServer().broadcastMessage(msg);
			
			return true;
		}
		
		return false;
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
