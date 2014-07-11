package afkick;

import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Painting;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class AfKickListener implements Listener
{
	private AfKick plugin;
	
	public AfKickListener(AfKick instance)
	{
		this.plugin = instance;
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onWorldChange(PlayerChangedWorldEvent event)
	{
		Player player = event.getPlayer();
		
		AfkInfo afkInfo = this.plugin.getAfkInfo(player);
		
		if(afkInfo != null)
			afkInfo.setLastLoc(player.getLocation());
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerMove(PlayerMoveEvent event)
	{
		Player player = event.getPlayer();
		

		AfkInfo afkInfo = this.plugin.getAfkInfo(player);
		if (afkInfo == null || !afkInfo.isAfk())
			return;
		
		Block from = event.getFrom().getBlock();
		Block to = event.getTo().getBlock();
		
		if (from.equals(to)) 
			return;
		
		if (from.getY() != to.getY())
			return;
		
		if ( from.isLiquid() || to.isLiquid() )
		{
			afkInfo.setLastLoc(event.getTo());
			return;
		}
		
		if (player.isInsideVehicle())
			return;
		
		if ( (pushedByPlayer(player, from, to)) || (pushedByPlayer(player, from.getRelative(BlockFace.UP), to.getRelative(BlockFace.UP))) ) 
			return;
		
		if (!afkInfo.getLastLoc().getWorld().equals(event.getTo().getWorld()))
			afkInfo.setLastLoc(event.getTo());
		
		if (afkInfo.getLastLoc().distance(event.getTo()) < 2.0D) 
			return;
		
		afkInfo.setAfk(false);
		afkInfo.setLastLoc(event.getTo());
		
		if (afkInfo.isTagAfk())
		{
			String msg = this.plugin.config.getString("broadcast.nolongerAFK", "%p is no longer AFK.").replaceAll("%p", player.getName());
			this.plugin.getServer().broadcastMessage(msg);
			
			afkInfo.setTagAfk(false);
			afkInfo.setInactive(false);
			
			return;
		}
		
		if (afkInfo.isInactive())
		{
			player.sendMessage("You are no longer inactive.");
			afkInfo.setInactive(false);
		}
		
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerJoin(PlayerJoinEvent event)
	{		
		Player player = event.getPlayer();
		this.plugin.addPlayer(player);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		
		plugin.removePlayer(player);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onPlayerKick(PlayerKickEvent event)
	{
		Player player = event.getPlayer();
		
		plugin.removePlayer(player);
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onBlockBreak(BlockBreakEvent event)
	{
		if (event.isCancelled()) 
			return;
	
		checkPlayer(event.getPlayer());
	}
	
	@EventHandler(priority=EventPriority.MONITOR)
	public void onBlockPlace(BlockPlaceEvent event)
	{
		if (event.isCancelled()) 
			return;
		
		checkPlayer(event.getPlayer());
	}
	
	@EventHandler(priority=EventPriority.LOWEST)
	public void onChat(AsyncPlayerChatEvent event)
	{
		Player player = event.getPlayer();
		
		AfkInfo afkInfo = this.plugin.getAfkInfo(player);
		if (afkInfo != null && afkInfo.isInactive() && afkInfo.checkCapChar(event.getMessage()) )
		{
			event.setCancelled(true);
			event.setMessage(null);
			afkInfo.setAfk(false);
			afkInfo.setInactive(false);
			
			player.sendMessage("Correct! You are no longer inactive.");
			
			if (afkInfo.isTagAfk())
			{
				afkInfo.setTagAfk(false);
				String msg = this.plugin.config.getString("broadcast.nolongerAFK", "%p is no longer AFK.").replaceAll("%p", player.getName());
				this.plugin.getServer().broadcastMessage(msg);
			}
	
		}
	}
	@EventHandler(priority=EventPriority.NORMAL)
	public void onItemPickUp(PlayerPickupItemEvent event)
	{
		if (event.isCancelled()) 
			return;
	
		AfkInfo afkInfo = this.plugin.getAfkInfo(event.getPlayer());
		if(afkInfo == null)
			return;
		
		if ( this.plugin.config.getBoolean("denyItemPickup", true)  &&  afkInfo.isTagAfk() )	
			event.setCancelled(true);		
	}
	
	
	private void checkPlayer(Player player)
	{
		
		AfkInfo afkInfo = this.plugin.getAfkInfo(player);
		if(afkInfo == null)
			return;
		
		afkInfo.setAfk(false);

		if (afkInfo.isTagAfk())
		{
			afkInfo.setTagAfk(false);
			String msg = this.plugin.config.getString("broadcast.nolongerAFK", "%p is no longer AFK.").replaceAll("%p", player.getName());
			this.plugin.getServer().broadcastMessage(msg);
		}
		
	}
	
	private boolean pushedByPlayer(Player player, Block from, Block to)
	{
		for (Entity e : player.getNearbyEntities(2.0D, 2.0D, 2.0D)) {
			if ((!(e instanceof Painting)) && (
			(e.getLocation().getBlock().equals(from)) || (e.getLocation().getBlock().equals(to)))) 
				return true;
			
		}
		return false;
	}
}
