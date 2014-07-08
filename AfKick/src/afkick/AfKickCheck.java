package afkick;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class AfKickCheck implements BukkitTask, Runnable
{
	private final AfKick plugin;
	private boolean run = false;
	
	public AfKickCheck(AfKick instance)
	{
		plugin = instance;
		run = true;
	}
	
	@Override
	public void run()
	{
		if(run)
			plugin.checkAfks();	
	}
	
	@Override
	public Plugin getOwner() {
		return plugin;
	}
	
	@Override
	public int getTaskId() {
		return 0;
	}
	
	@Override
	public boolean isSync() {
		return false;
	}
	
	@Override
	public void cancel() {
		run = false;		
	}
}
