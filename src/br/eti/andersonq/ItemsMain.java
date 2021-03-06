package br.eti.andersonq;

import java.util.ArrayList;

import br.eti.andersonq.shoplist.R;
import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.FragmentManager;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.ListView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.TextView;

/**
 * 
 * @author	Anderson de Franca Queiroz
 * @email	contato@andersonq.eti.br
 */
public class ItemsMain extends Activity implements Update
{
	//Tag to debug
    private static final String TAG = "ItemsMain";
	
    private static final int ACTIVITY_LIST_MAIN = 4;
    
    private static boolean started = false;
    
    private static MenuItem mStartShopping, mStopShopping;
    
    private Activity mActivity;
    
    public static final String about = "This software was developed as a academic work by Anderson de França Queiroz\n" +
			"for the class CS551 Mobile Software And Applications at University of Strathclyde\n" +
			"Copyright (C) 2014  Anderson de França Queirozs\n\n" + 

			"This program is free software: you can redistribute it and/or modify\n" +
			"it under the terms of the GNU General Public License as published by\n" +
			"the Free Software Foundation, either version 3 of the License, or\n" +
			"(at your option) any later version.\n\n" + 

    		"This program is distributed in the hope that it will be useful,\n" + 
    		"but WITHOUT ANY WARRANTY; without even the implied warranty of\n" +
    		"MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the\n" + 
    		"GNU General Public License for more details.\n\n" +

    		"You should have received a copy of the GNU General Public License\n" +
    		"along with this program.  If not, see <http://www.gnu.org/licenses/>.\n\n"

    		+ "A copy of this program and its source code is available in\n"
    		+ "http://github.com/AndersonQ/ShopList";
    
    /*
     * ************************************************************************
     * ************************************************************************
     * Android override methods
     * ************************************************************************
     * ************************************************************************
     */
    @Override
    public void onCreate(Bundle savedInstanceState) 
    {
    	long currentShopListId;
        super.onCreate(savedInstanceState);
        MyAdapter adapter;
        Application app = getApplication();
        Omniscient.setApp(app);
        setContentView(R.layout.items_list);
        //mDbHelper = new DbAdapter(this);
        DbAdapter.open(this);
        ActionBar ab = getActionBar();
        
        mActivity = this;
        
        //Get a shopList when app starts
        if(started == false)
        {
        	started = true;
        	currentShopListId = DbAdapter.getFirstShopList();
        	DbAdapter.setCurrentShopListID((int) currentShopListId);
        }

        ab.setSubtitle(DbAdapter.getShopListName(DbAdapter.getCurrentShopListID()));

        //Fill data
    	ListView listView = (ListView) findViewById(R.id.items_list_view);
    	fillData();
		
        registerForContextMenu(listView);
        
        //Get default settings
        PreferenceManager.setDefaultValues(app, R.xml.preferences, false);        
    }

    /**
     * Create action bar menu
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.item_action_bar, menu);
        
        //Keep a reference to start/stop shopping
        mStartShopping = menu.findItem(R.id.item_action_go_shopping);
        mStopShopping = menu.findItem(R.id.item_action_stop_shopping);
        
        if(Omniscient.isShopping())
        {
    		mStartShopping.setVisible(false);
    		mStopShopping.setVisible(true);
        }
        else
        {
    		mStartShopping.setVisible(true);
    		mStopShopping.setVisible(false);
        }
        
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) 
    {
    	AdapterContextMenuInfo info;
    	info = (AdapterContextMenuInfo) item.getMenuInfo();
    	
        switch(item.getItemId()) 
        {
        	case R.id.item_action_add:
        		//Create item 
        		editItem(-1);
                return true;
        	case R.id.item_action_choose_list:
        		stopShopping();
        		chooseList();
            	return true;
        	case R.id.item_action_go_shopping:
        		startShopping();
        		return true;
        	case R.id.item_action_stop_shopping:
        		stopShopping();
        		return false;
        	case R.id.item_action_settings:
        		startActivity(new Intent(this, SettingsActivity.class));
        		return true;
        	case R.id.item_contextmenu_edit:
        		//Log.d(TAG, "onMenuItemSelected: editItem, id: " + info.id);
        		editItem(info.id);
            	return true;
        	case R.id.item_contextmenu_delete:
            	if(Omniscient.isShopping())
            	{
	            	boolean ret = DbAdapter.deleteReceiptItem(info.id);
	            	if(!ret)
	            		Log.e(TAG, "onContextItemSelected(): "
	            				+ "Error: receipt item wasn't"
	            				+ " deleted from DB");
	                fillData();
            	}
            	else
            	{
	            	boolean ret = DbAdapter.deleteShopItem(info.id);
	            	if(!ret)
	            		Log.e(TAG, "onContextItemSelected(): "
	            				+ "Error: shop item wasn't"
	            				+ " deleted from DB");
	                fillData();
            	}
            	return true;
        	case R.id.item_action_about:
        		//msgBox(ItemsMain.about, "About");
        		AlertDialog.Builder builder = new AlertDialog.Builder(mActivity);
        		builder.setTitle(R.string.about);
        		builder.setMessage(ItemsMain.about);
        		builder.setNegativeButton(R.string.got_ti_msg, null);
        		builder.create().show();
        		return true;
        }
        return false;
    }

    /**
     * Create context menu
     */
	@Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) 
    {
        // Inflate the menu items for use in context menu
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.item_context_menu, menu);
    }
    
    @Override
    protected void onActivityResult(int requestCode, 
    								int resultCode, 
    								Intent intent) 
    {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }
    
	@Override
	protected void onDestroy() 
	{
		DbAdapter.close();
		super.onDestroy();
	}
	
    /*
     * ************************************************************************
     * ************************************************************************
     * My methods, Update interface methods
     * ************************************************************************
     * ************************************************************************
     */
	
    @Override
    public void updateCost()
    {
    	//Get textViews
    	TextView price = (TextView) this.findViewById(R.id.item_activity_price);
    	TextView pricetxt = (TextView) this.findViewById(R.id.item_activity_price_txt);
    	//Set new total cost
    	price.setText(String.format("%.2f", listCost()));
    	//Make text views visible
		pricetxt.setVisibility(View.VISIBLE);
		price.setVisibility(View.VISIBLE);
    }
	
	@Override
	public void updateDisplayedData() 
	{
		//Log.d(TAG, "updateDisplayedData()");
		if(Omniscient.isShopping())
			updateCost();
		fillData();
	}
	
    /*
     * ************************************************************************
     * ************************************************************************
     * My methods
     * ************************************************************************
     * ************************************************************************
     */
	
	/**
	 * Fill the activity with the current items list
	 */
    private void fillData() 
    {
    	ArrayList<Item> items = null;
    	ListView listView = (ListView) findViewById(R.id.items_list_view);
    	TextView price = (TextView) this.findViewById(R.id.item_activity_price);
    	TextView pricetxt = (TextView) this.findViewById(R.id.item_activity_price_txt);
    	
    	//Log.d(TAG, "fillData():");
    	if(Omniscient.isShopping())
    	{
    		ArrayList<Item> ditems = new ArrayList<Item>();
        	//Load auto-remove preferences
        	SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this);
        	boolean autoRemove = sharedPref.getBoolean(SettingsActivity.KEY_AUTO_REMOVE, false);
        	
        	//Get items to display
    		items = DbAdapter.getAllReceiptItems();
    		
    		price.setText(String.format("%.2f",listCost()));
    		pricetxt.setVisibility(View.VISIBLE);
    		price.setVisibility(View.VISIBLE);
    		
        	/*Log.d(TAG, "currentReceiptList: " + DbAdapter.getCurrentReceiptListID());
    		Log.d(TAG, "autoRemove: " + autoRemove);*/
    		
    		if(autoRemove)
    		{
    			for(Item item : items)
    			{
    				if(!item.isPurchased())
    					ditems.add(item);
    			}
    			
    			/*Log.d(TAG, "Items do display:");
    			for(Item i : ditems)
    				Log.d(TAG, "Item: " + i.getName() + " price: " + i.getPrice());*/
    			
    	    	listView = (ListView) findViewById(R.id.items_list_view);
    	        MyAdapter adapter = new MyAdapter(this, 
    	        								R.layout.items_list, 
    	        								ditems);
    			listView.setAdapter(adapter);
    			listView.setClickable(true);
    		}
    		else
    		{
    			items = DbAdapter.getAllReceiptItems();
    			
    			/*Log.d(TAG, "Items do display:");
    			for(Item i : items)
    				Log.d(TAG, "Item: " + i.getName() + " price: " + i.getPrice());*/
    			
		    	listView = (ListView) findViewById(R.id.items_list_view);
		        MyAdapter adapter = new MyAdapter(this, 
		        								R.layout.items_list, 
		        								items);
				listView.setAdapter(adapter);
				listView.setClickable(true);
    		}
    	}
    	else
    	{
    		items = DbAdapter.getAllShopItems();
    		
    		pricetxt.setVisibility(View.INVISIBLE);
    		price.setVisibility(View.INVISIBLE);
    		
    		/*Log.d(TAG, "currentShopList: " + DbAdapter.getCurrentShopListID());
			Log.d(TAG, "Items do display:");
			for(Item i : items)
				Log.d(TAG, "Item: " + i.getName() + " price: " + i.getPrice());*/
			
	    	listView = (ListView) findViewById(R.id.items_list_view);
	        MyAdapter adapter = new MyAdapter(this, 
	        								R.layout.items_list, 
	        								items);
			listView.setAdapter(adapter);
			listView.setClickable(true);
    	}
    }
    
    /**
     * Call Fragment to create or edit a item
     * If id == -1 it will create a item, otherwise will edit the item with
     * the specified id
     * @param id -1 to create a item or the of of the item to be edited
     */
    private void editItem(long id)
    {
    	ItemEditFrag fire = new ItemEditFrag();
    	FragmentManager manager = getFragmentManager();
    	Omniscient.setCurrentItemID(id);
    	/*Log.d(TAG, "editItem");
    	Log.d(TAG, "item id: " + id);*/
    	
    	/*if(Omniscient.isShopping())
    		DbAdapter.setCurrentReceiptListID((int)id);
    	else
    		DbAdapter.setCurrentShopListID((int)id);*/
    	//Log.d(TAG, "firing Fragment");
    	fire.show(manager, "FRAGMENT");
    }

    private void chooseList() 
    {
        Intent i = new Intent(this, ListsMain.class);
        startActivityForResult(i, ACTIVITY_LIST_MAIN);
    }
    
    /**
     * Calculate current shoplist total cost
     * @return current shoplist total cost
     */
    private float listCost()
    {
    	//Get list items
    	ArrayList<Item> items = DbAdapter.getAllReceiptItems();
    	float totalCost = 0;
    	
    	for(Item item : items)
    	{
    		totalCost += 	item.getQuantity() * 
    						item.getPrice() * 
    						item.getPurchased();
    	}
    	return totalCost;
    }
    
    /**
	 * Start shopping
	 */
	private void startShopping()
	{
		//Set shopping true
		Omniscient.setShopping(true);
		
    	//Get textViews
    	TextView price = (TextView) this.findViewById(R.id.item_activity_price);
    	TextView pricetxt = (TextView) this.findViewById(R.id.item_activity_price_txt);
    	//Make text related to cost views invisible
		pricetxt.setVisibility(View.VISIBLE);
		price.setVisibility(View.VISIBLE);
		
		//Try to get RecipList associated with current shop list
		long receiptListId = DbAdapter.getReceiptListFromShopList();
		
		/*Log.d(TAG,  "startShopping");
		Log.d(TAG, "currentShopList: " + DbAdapter.getCurrentShopListID());
		Log.d(TAG, "DbAdapter.getReceiptListFromShopList(): " + receiptListId);*/
		
		if(receiptListId == 0)//No associated receiptList
		{
			//Create receipt list from current shop list
			receiptListId = DbAdapter.createReceiptList();
			//Log.d(TAG, "new receipt list id: " + receiptListId);
			//Set shopList foreign key to new receipt list
			DbAdapter.setShopListReceiptList(
							DbAdapter.getCurrentShopListID(), 
							receiptListId);
		}
		else if(receiptListId == -1)
		{
			Log.e(TAG, "startShopping(): Error: "
					+ "DbAdapter.getReceiptListFromShopList()"
					+ "returned -1, error in sql query");
		}
		
		//Set current receiptList	
		DbAdapter.setCurrentReceiptListID((int) receiptListId);
		//Log.d(TAG, "DbAdapter.setCurrentReceiptListID: " + receiptListId);
		//Log.d(TAG, "DbAdapter.getCurrentReceiptListID(): " + DbAdapter.getCurrentReceiptListID());
		
		mStartShopping.setVisible(false);
		mStopShopping.setVisible(true);
		
		//Update total cost
		updateCost();
		//Fill activity
		fillData();
	}
	
    /**
	 * Stop shopping
	 */
	private void stopShopping()
	{
		//Set shopping false
		Omniscient.setShopping(false);
		
    	//Get textViews
    	TextView price = (TextView) this.findViewById(R.id.item_activity_price);
    	TextView pricetxt = (TextView) this.findViewById(R.id.item_activity_price_txt);
    	//Make text related to cost views invisible
		pricetxt.setVisibility(View.INVISIBLE);
		price.setVisibility(View.INVISIBLE);
		
		mStartShopping.setVisible(true);
		mStopShopping.setVisible(false);
		
		//Fill activity
		fillData();
	}
}
