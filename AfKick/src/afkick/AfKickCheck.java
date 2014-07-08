package afkick;

import org.bukkit.plugin.Plugin;
import org.bukkit.scheduler.BukkitTask;

public class AfKickCheck implements BukkitTask, Runnable{

	public AfKick plugin;
	
	public AfKickCheck(AfKick instance)
	{
		plugin = instance;
	}
	
	
	public void run()
	{
		 plugin.checkAfks();	
	}
	
	@Override
	public Plugin getOwner() {
		return plugin;
	}

	@Override
	public int getTaskId() {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public boolean isSync() {
		// TODO Auto-generated method stub
		return false;
	}


	@Override
	public void cancel() {
		// TODO Auto-generated method stub
		
	}

}
