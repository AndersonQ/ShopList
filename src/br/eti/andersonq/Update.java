package br.eti.andersonq;

public interface Update 
{
	/**
	 * Called when it is necessary to make a update due to any change on Database
	 */
	public void updateDisplayedData();
	
	/**
	 * Call when it is necessary to update the displayed cost
	 */
	public void updateCost();

}
