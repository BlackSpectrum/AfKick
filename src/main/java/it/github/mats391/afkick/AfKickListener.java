package it.github.mats391.afkick;

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

	private final AfKick plugin;

	public AfKickListener(final AfKick instance) {
		this.plugin = instance;
	}

	private void checkPlayer( final Player player ) {

		final AfkInfo afkInfo = this.plugin.getAfkInfo( player );
		if ( afkInfo == null )
			return;

		afkInfo.setAfk( false );

		if ( afkInfo.isTagAfk() )
		{
			afkInfo.setTagAfk( false );
			final String msg = this.plugin.config.getString( "broadcast.nolongerAFK", "%p is no longer AFK." ).replaceAll( "%p",
					player.getName() );
			this.plugin.getServer().broadcastMessage( msg );
		}

	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockBreak( final BlockBreakEvent event ) {
		if ( event.isCancelled() )
			return;

		this.checkPlayer( event.getPlayer() );
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onBlockPlace( final BlockPlaceEvent event ) {
		if ( event.isCancelled() )
			return;

		this.checkPlayer( event.getPlayer() );
	}

	@EventHandler(priority = EventPriority.LOWEST)
	public void onChat( final AsyncPlayerChatEvent event ) {
		final Player player = event.getPlayer();

		final AfkInfo afkInfo = this.plugin.getAfkInfo( player );
		if ( afkInfo != null && afkInfo.isInactive() && afkInfo.checkCapChar( event.getMessage() ) )
		{
			event.setCancelled( true );
			event.setMessage( null );
			afkInfo.setAfk( false );
			afkInfo.setInactive( false );

			player.sendMessage( "Correct! You are no longer inactive." );

			if ( afkInfo.isTagAfk() )
			{
				afkInfo.setTagAfk( false );
				final String msg = this.plugin.config.getString( "broadcast.nolongerAFK", "%p is no longer AFK." ).replaceAll( "%p",
						player.getName() );
				this.plugin.getServer().broadcastMessage( msg );
			}

		}
	}

	@EventHandler(priority = EventPriority.NORMAL)
	public void onItemPickUp( final PlayerPickupItemEvent event ) {
		if ( event.isCancelled() )
			return;

		final AfkInfo afkInfo = this.plugin.getAfkInfo( event.getPlayer() );
		if ( afkInfo == null )
			return;

		if ( this.plugin.config.getBoolean( "denyItemPickup", true ) && afkInfo.isTagAfk() )
			event.setCancelled( true );
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerJoin( final PlayerJoinEvent event ) {
		final Player player = event.getPlayer();
		this.plugin.addPlayer( player );
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerKick( final PlayerKickEvent event ) {
		final Player player = event.getPlayer();

		this.plugin.removePlayer( player );
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerMove( final PlayerMoveEvent event ) {
		final Player player = event.getPlayer();

		final AfkInfo afkInfo = this.plugin.getAfkInfo( player );
		if ( afkInfo == null || !afkInfo.isAfk() )
			return;

		final Block from = event.getFrom().getBlock();
		final Block to = event.getTo().getBlock();

		if ( from.equals( to ) )
			return;

		if ( from.getY() != to.getY() )
			return;

		if ( from.isLiquid() || to.isLiquid() )
		{
			afkInfo.setLastLoc( event.getTo() );
			return;
		}

		if ( player.isInsideVehicle() )
			return;

		if ( this.pushedByPlayer( player, from, to )
				|| this.pushedByPlayer( player, from.getRelative( BlockFace.UP ), to.getRelative( BlockFace.UP ) ) )
			return;

		if ( !afkInfo.getLastLoc().getWorld().equals( event.getTo().getWorld() ) )
			afkInfo.setLastLoc( event.getTo() );

		if ( afkInfo.getLastLoc().distance( event.getTo() ) < 2.0D )
			return;

		afkInfo.setAfk( false );
		afkInfo.setLastLoc( event.getTo() );

		if ( afkInfo.isTagAfk() )
		{
			final String msg = this.plugin.config.getString( "broadcast.nolongerAFK", "%p is no longer AFK." ).replaceAll( "%p",
					player.getName() );
			this.plugin.getServer().broadcastMessage( msg );

			afkInfo.setTagAfk( false );
			afkInfo.setInactive( false );

			return;
		}

		if ( afkInfo.isInactive() )
		{
			player.sendMessage( "You are no longer inactive." );
			afkInfo.setInactive( false );
		}

	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onPlayerQuit( final PlayerQuitEvent event ) {
		final Player player = event.getPlayer();

		this.plugin.removePlayer( player );
	}

	@EventHandler(priority = EventPriority.MONITOR)
	public void onWorldChange( final PlayerChangedWorldEvent event ) {
		final Player player = event.getPlayer();

		final AfkInfo afkInfo = this.plugin.getAfkInfo( player );

		if ( afkInfo != null )
			afkInfo.setLastLoc( player.getLocation() );
	}

	private boolean pushedByPlayer( final Player player, final Block from, final Block to ) {
		for ( final Entity e : player.getNearbyEntities( 2.0D, 2.0D, 2.0D ) )
			if ( !( e instanceof Painting ) && ( e.getLocation().getBlock().equals( from ) || e.getLocation().getBlock().equals( to ) ) )
				return true;
		return false;
	}
}
