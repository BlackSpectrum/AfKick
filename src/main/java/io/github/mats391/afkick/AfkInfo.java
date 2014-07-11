package io.github.mats391.afkick;

import java.util.Random;

import org.bukkit.Location;

public class AfkInfo
{

	private long			tagAfk;
	private long			kickAfk;
	private boolean			afk;
	private boolean			isTagAfk;
	private boolean			inactive;
	private Location		lastLoc;
	private String			capChar;
	private String			capCharQ;

	private final Random	rand	= new Random();

	public AfkInfo(final long tagAfk, final long kickAfk, final Location lastLoc) {
		this.tagAfk = tagAfk;
		this.kickAfk = kickAfk;
		this.lastLoc = lastLoc;
	}

	public boolean checkCapChar( final String answer ) {
		return answer.equals( this.capChar );
	}

	public void generateCapChar() {
		final int operation = this.rand.nextInt( 3 );

		int x, y, z = 0;
		char op = '+';

		switch ( operation ) {
		case 0:
			x = this.rand.nextInt( 21 );
			y = this.rand.nextInt( 21 );

			z = x + y;
			op = '+';

			break;
		case 1:
			x = this.rand.nextInt( 21 );
			y = this.rand.nextInt( 21 );

			z = x - y;
			op = '-';

			break;
		case 2:
			x = this.rand.nextInt( 11 );
			y = this.rand.nextInt( 11 );

			z = x * y;
			op = '*';

			break;
		default:
			x = this.rand.nextInt( 21 );
			y = this.rand.nextInt( 21 );

			z = x + y;

			break;
		}
		this.capChar = "" + z;
		this.capCharQ = "" + x + op + y;
	}

	public String getCapCharQ() {
		return this.capCharQ;
	}

	public long getKickAfk() {
		return this.kickAfk;
	}

	public Location getLastLoc() {
		return this.lastLoc;
	}

	public long getTagAfkTime() {
		return this.tagAfk;
	}

	public boolean isAfk() {
		return this.afk;
	}

	public boolean isInactive() {
		return this.inactive;
	}

	public boolean isTagAfk() {
		return this.isTagAfk;
	}

	public void setAfk( final boolean afk ) {
		this.afk = afk;
	}

	public void setInactive( final boolean inactive ) {
		this.inactive = inactive;
	}

	public void setKickAfk( final long kickAfk ) {
		this.kickAfk = kickAfk;
	}

	public void setLastLoc( final Location lastLoc ) {
		this.lastLoc = lastLoc;
	}

	public void setTagAfk( final boolean isTagAfk ) {
		this.isTagAfk = isTagAfk;
	}

	public void setTagAfkTime( final long softAfk ) {
		this.tagAfk = softAfk;
	}
}
