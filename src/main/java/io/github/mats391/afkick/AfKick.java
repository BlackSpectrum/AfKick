package io.github.mats391.afkick;

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

	private final AfKickListener	listener	= new AfKickListener( this );

	private final Server			server		= Bukkit.getServer();

	public HashMap<UUID, AfkInfo>	afkPlayers	= new HashMap<UUID, AfkInfo>();
	public FileConfiguration		config;

	private int						taskId		= -1;

	public void addPlayer( final Player p ) {
		final long now = System.currentTimeMillis();
		this.afkPlayers.put(
				p.getUniqueId(),
				new AfkInfo( now + this.config.getLong( "timeUntil.tagAFK", 300L ) * 1000L, now
						+ this.config.getLong( "timeUntil.kick", 540L ) - this.config.getLong( "timeUntil.tagAFK", 300L ) * 1000L, p
						.getLocation() ) );
	}

	public void checkAfks() {
		final long now = System.currentTimeMillis();
		for ( final UUID uid : this.afkPlayers.keySet() )
		{

			final Player p = this.server.getPlayer( uid );

			final AfkInfo afkInfo = this.afkPlayers.get( uid );
			if ( afkInfo.isAfk() )
			{
				if ( afkInfo.isAfk() && now >= afkInfo.getKickAfk() )
				{
					p.kickPlayer( this.config.getString( "kickMessage", "You got kicked for being AFK." ) );
					final String msg = this.config.getString( "broadcast.kick", "%p got kicked for being AFK." ).replaceAll( "%p",
							p.getName() );
					this.server.broadcastMessage( msg );
				}
				else
				{
					if ( !afkInfo.isInactive() && now >= afkInfo.getTagAfkTime() - 30000L )
					{
						afkInfo.generateCapChar();
						p.sendMessage( "You did not walk for too long and got marked as inactive - please move or answer "
								+ afkInfo.getCapCharQ() + "=? in chat." );
						afkInfo.setInactive( true );
					}
					if ( !afkInfo.isTagAfk() && now >= afkInfo.getTagAfkTime() )
					{
						final String msg = this.config.getString( "broadcast.tagAFK", "%p is now AFK." ).replaceAll( "%p", p.getName() );
						this.server.broadcastMessage( msg );
						afkInfo.setTagAfk( true );
					}
				}
			}
			else
			{
				afkInfo.setAfk( true );
				afkInfo.setLastLoc( p.getLocation() );
				afkInfo.setTagAfkTime( now + this.config.getLong( "timeUntil.tagAFK", 300L ) * 1000L );
				afkInfo.setKickAfk( now + this.config.getLong( "timeUntil.kick", 540L ) * 1000L );
			}

		}
	}

	public AfkInfo getAfkInfo( final Player p ) {
		return this.afkPlayers.get( p.getUniqueId() );
	}

	@Override
	public boolean onCommand( final CommandSender sender, final Command cmd, final String commandLabel, final String[] args ) {
		if ( sender instanceof Player && cmd.getName().equalsIgnoreCase( "afk" ) )
		{
			final Player player = (Player) sender;
			if ( this.afkPlayers.containsKey( player ) )
			{
				final AfkInfo afkInfo = this.afkPlayers.get( player );
				if ( afkInfo.isTagAfk() )
				{
					if ( afkInfo.isInactive() )
						return true;

					afkInfo.setTagAfk( false );

					final String msg = this.config.getString( "broadcast.nolongerAFK", "%p is no longer AFK." ).replaceAll( "%p",
							player.getName() );
					this.server.broadcastMessage( msg );

					return true;
				}

				afkInfo.setAfk( true );
				afkInfo.setTagAfk( true );
				final String msg = this.config.getString( "broadcast.tagAFK", "%p is now AFK." ).replaceAll( "%p", player.getName() );

				this.server.broadcastMessage( msg );
				return true;
			}

			final long now = System.currentTimeMillis();
			this.afkPlayers.put( player.getUniqueId(), new AfkInfo( now, now + this.config.getLong( "timeUntil.kick", 540L ) * 1000L,
					player.getLocation() ) );

			final String msg = this.config.getString( "broadcast.tagAFK", "%p is now AFK." ).replaceAll( "%p", player.getName() );
			this.server.broadcastMessage( msg );

			return true;
		}

		return false;
	}

	@Override
	public void onDisable() {
		this.afkPlayers.clear();
		this.server.getScheduler().cancelTask( this.taskId );
	}

	@Override
	public void onEnable() {
		this.setConfig();
		this.afkPlayers.clear();

		final long now = System.currentTimeMillis();

		for ( final Player p : this.server.getOnlinePlayers() )
			this.afkPlayers.put( p.getUniqueId(), new AfkInfo( now + this.config.getLong( "timeUntil.tagAFK", 300L ) * 1000L, now
					+ this.config.getLong( "timeUntil.kick", 540L ) * 1000L, p.getLocation() ) );

		final PluginManager pm = this.server.getPluginManager();
		pm.registerEvents( this.listener, this );
		this.taskId = this.server.getScheduler().scheduleSyncRepeatingTask( this, new Runnable() {

			@Override
			public void run() {
				AfKick.this.checkAfks();
			}
		}, this.config.getInt( "checkperiodInTicks", 100 ), this.config.getInt( "checkperiodInTicks", 100 ) );

	}

	public void removePlayer( final Player p ) {
		if ( this.afkPlayers.containsKey( p.getUniqueId() ) )
			this.afkPlayers.remove( p.getUniqueId() );
	}

	private void setConfig() {
		this.config = this.getConfig();

		this.config.set( "timeUntil.tagAFK", Long.valueOf( this.config.getLong( "timeUntil.tagAFK", 300L ) ) );
		this.config.set( "timeUntil.kick", Long.valueOf( this.config.getLong( "timeUntil.kick", 540L ) ) );
		this.config.set( "denyItemPickup", Boolean.valueOf( this.config.getBoolean( "denyItemPickup", true ) ) );
		this.config.set( "broadcast.tagAFK", this.config.getString( "broadcast.tagAFK", "%p is now AFK." ) );
		this.config.set( "broadcast.nologerAFK", this.config.getString( "broadcast.nolongerAFK", "%p is no longer AFK." ) );
		this.config.set( "broadcast.kick", this.config.getString( "broadcast.kick", "%p got kicked for being AFK." ) );
		this.config.set( "kickMessage", this.config.getString( "kickMessage", "You got kicked for being AFK." ) );
		this.config.set( "checkperiodInTicks", Integer.valueOf( this.config.getInt( "checkperiodInTicks", 100 ) ) );

		this.saveConfig();
	}
}
