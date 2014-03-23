package br.eti.andersonq;

import android.app.Application;

/**
 * Keep information to be passed by other classes
 * Yes, I know that Android provide ways to make things (activities, fragments and so on) 
 * communicate, but them are not working and it WILL!
 * 
 * @author	Anderson de Franca Queiroz
 * @email	contato@andersonq.eti.br
 *
 */
public class Omniscient 
{
	/** Application */
	private static Application app;
	/** ID of the item being used to whatever*/
	private static long currentItemID;
	/** Shopping mode on/off*/
	private static boolean shopping = false;
	
	public static Application getApp() {
		return app;
	}

	public static void setApp(Application app) {
		Omniscient.app = app;
	}

	public static long getCurrentItemID() {
		return currentItemID;
	}

	public static void setCurrentItemID(long currentItemID) {
		Omniscient.currentItemID = currentItemID;
	}
	
	public static boolean isShopping() 
	{
		return shopping;
	}
	public static void setShopping(boolean shopping) 
	{
		Omniscient.shopping = shopping;
	}
}
