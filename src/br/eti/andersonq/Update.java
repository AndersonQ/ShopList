package br.eti.andersonq;

public interface Update {
	
	/*
	 * Called when it is necessary to make a update due to any change on Database
	 */
	public void onSaveState();

}
