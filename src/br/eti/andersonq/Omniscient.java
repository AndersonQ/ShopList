package br.eti.andersonq;

/**
 * Keep information to be passed by other classes
 * Yes, I know that Android provide ways to make things (activities, fragments and so on) 
 * communicate, but them are not working and it WILL!
 * @author ainsoph
 *
 */
public class Omniscient 
{
	/** ID of the item being used to whatever*/
	private static long currentItemID;

	public static long getCurrentItemID() {
		return currentItemID;
	}

	public static void setCurrentItemID(long currentItemID) {
		Omniscient.currentItemID = currentItemID;
	}
}
