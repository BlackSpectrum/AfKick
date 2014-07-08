package afkick;


import java.util.ArrayList;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPistonExtendEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerKickEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.player.PlayerQuitEvent;


public class AfKickListener implements Listener{
	
	public AfKick plugin;
	public ArrayList<Player> pushedPlayers = new ArrayList<Player>();

	
	public AfKickListener(AfKick instance)
	{
		plugin = instance;
	}
	
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerMove(PlayerMoveEvent event)
	{
		
		Block from = event.getFrom().getBlock();
		Block to = event.getTo().getBlock();
		
		//only if you really moved
		if(from.equals(to ) )
			return;
				
		//no vertical movement to stop jumping
		if(from.getY() != to.getY())
			return;
		

		
		Player player = event.getPlayer();
		
		//vehicle check
		if(player.isInsideVehicle())
			return;
		
		//Piston check
		if(pushedPlayers.contains(player))
		{
			pushedPlayers.remove(player);
			return;
		}
		
		
		if(plugin.afkPlayers.containsKey(player))
			plugin.afkPlayers.remove(player);
		if(plugin.softAfkPlayers.contains(player))
		{
			plugin.softAfkPlayers.remove(player);
			String msg = plugin.config.getString("broadcast.nolongerAFK", "%p is no longer AFK.").replaceAll("%p", player.getName());
			plugin.getServer().broadcastMessage(msg);
		}
		
			
	}
	
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPistonExtend(BlockPistonExtendEvent event)
	{
		BlockFace direction = event.getDirection();
		
		Location loc;
		
		if(event.getBlocks().isEmpty())
		{
			loc = event.getBlock().getRelative(direction).getLocation();
		}
		else
		{
			loc = event.getBlocks().get(event.getLength()-1).getRelative(direction).getLocation();
		}
		
		for(Entity e : loc.getChunk().getEntities())
		{
			if(e instanceof Player)
			{
					if(e.getLocation().getBlock().getLocation().equals(loc) 
							|| e.getLocation().getBlock().getRelative(BlockFace.UP).getLocation().equals(loc))
							pushedPlayers.add((Player) e);
			}
		}
		

	}

	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit(PlayerQuitEvent event)
	{
		Player player = event.getPlayer();
		if(plugin.afkPlayers.containsKey(player))
			plugin.afkPlayers.remove(player);
		if(plugin.softAfkPlayers.contains(player))
			plugin.softAfkPlayers.remove(player);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerKick(PlayerKickEvent event)
	{
		Player player = event.getPlayer();
		if(plugin.afkPlayers.containsKey(player))
			plugin.afkPlayers.remove(player);
		if(plugin.softAfkPlayers.contains(player))
			plugin.softAfkPlayers.remove(player);
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockBreak(BlockBreakEvent event)
	{
		Player player = event.getPlayer();
		if(plugin.afkPlayers.containsKey(player))
			plugin.afkPlayers.remove(player);
		if(plugin.softAfkPlayers.contains(player))
		{
			plugin.softAfkPlayers.remove(player);
			String msg = plugin.config.getString("broadcast.nolongerAFK", "%p is no longer AFK.").replaceAll("%p", player.getName());
			plugin.getServer().broadcastMessage(msg);
		}
	}
	
	@EventHandler(priority = EventPriority.MONITOR)
	public void onChat(AsyncPlayerChatEvent event)
	{
		Player player = event.getPlayer();
		if(plugin.afkPlayers.containsKey(player))
			plugin.afkPlayers.remove(player);
		if(plugin.softAfkPlayers.contains(player))
		{
			plugin.softAfkPlayers.remove(player);
			String msg = plugin.config.getString("broadcast.nolongerAFK", "%p is no longer AFK.").replaceAll("%p", player.getName());
			plugin.getServer().broadcastMessage(msg);
		}
	}
	
	@EventHandler(priority = EventPriority.NORMAL)
	public void onItemPickUp(PlayerPickupItemEvent event)
	{
		if(event.isCancelled())
			return;
		
		if(plugin.config.getBoolean("denyItemPickup", true) && plugin.softAfkPlayers.contains(event.getPlayer()))
			event.setCancelled(true);
	}
	
	

}
