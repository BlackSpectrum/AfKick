package afkick;

import java.util.Random;

import org.bukkit.Location;

public class AfkInfo
{
	private long tagAfk;
	private long kickAfk;
	private boolean afk;
	private boolean isTagAfk;
	private boolean inactive;
	private Location lastLoc;
	private String capChar;
	private String capCharQ;
	
	private final Random rand = new Random();
	
	public AfkInfo(long tagAfk, long kickAfk, Location lastLoc)
	{
		this.tagAfk = tagAfk;
		this.kickAfk = kickAfk;
		this.lastLoc = lastLoc;
	}
	
	public Location getLastLoc(){
		return this.lastLoc;
	}
	
	public void setLastLoc(Location lastLoc){
		this.lastLoc = lastLoc;
	}
	
	public boolean isAfk(){
		return this.afk;
	}
	
	public void setAfk(boolean afk){
		this.afk = afk;
	}
	
	public boolean isTagAfk(){
		return this.isTagAfk;
	}
	
	public void setTagAfk(boolean isTagAfk){
		this.isTagAfk = isTagAfk;
	}
	
	public long getTagAfkTime(){
		return this.tagAfk;
	}
	
	public void setTagAfkTime(long softAfk){
		this.tagAfk = softAfk;
	}
	
	public long getKickAfk(){
		return this.kickAfk;
	}
	
	public void setKickAfk(long kickAfk){
		this.kickAfk = kickAfk;
	}
	
	public void generateCapChar()
	{
		int operation = rand.nextInt(3);
		
		int x,y,z = 0;
		char op = '+';
		
		switch (operation)
		{
			case 0: 
				x = rand.nextInt(21);
				y = rand.nextInt(21);
				
				z = x + y;
				op = '+';
				
				break;
			case 1: 
				 x = rand.nextInt(21);
				 y = rand.nextInt(21);
				 
				 z = x - y;
				 op = '-';
				 
				 break;
			case 2: 
				x = rand.nextInt(11);
				y = rand.nextInt(11);
				
				z = x * y;
				op = '*';
						
				break;
			default: 
				x = rand.nextInt(21);
				y = rand.nextInt(21);
				
				z = x + y;
				
				break;
		}
		this.capChar = "" + z;
		this.capCharQ = ("" + x + op + y);
	}
	
	public String getCapCharQ(){
		return this.capCharQ;
	}
	
	public boolean checkCapChar(String answer){
		return answer.equals(this.capChar);
	}
	
	public boolean isInactive(){
		return this.inactive;
	}
	
	public void setInactive(boolean inactive){
		this.inactive = inactive;
	}
}
