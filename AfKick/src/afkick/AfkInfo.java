package afkick;

public class AfkInfo {
	
	private long softAfk;
	private long kickAfk;

	
	public AfkInfo(long softAfk, long kickAfk)
	{
		this.softAfk = softAfk;
		this.kickAfk = kickAfk;
	}

	public long getSoftAfk() {
		return softAfk;
	}

	public void setSoftAfk(long softAfk) {
		this.softAfk = softAfk;
	}

	public long getKickAfk() {
		return kickAfk;
	}

	public void setKickAfk(long kickAfk) {
		this.kickAfk = kickAfk;
	}

}
