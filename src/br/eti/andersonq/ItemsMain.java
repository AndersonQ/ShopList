package br.eti.andersonq;

import java.util.ArrayList;

import br.eti.andersonq.shoplist.R;
import android.app.ActionBar;
import android.app.Activity;
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
    
    private static MenuItem mStartShopping, mStopShopping;
    
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
        super.onCreate(savedInstanceState);
        MyAdapter adapter;
        Application app = getApplication();
        Omniscient.setApp(app);
        setContentView(R.layout.items_list);
        //mDbHelper = new DbAdapter(this);
        DbAdapter.open(this);
        ActionBar ab = getActionBar();
        ab.setSubtitle(DbAdapter.getShopListName(DbAdapter.getCurrentShopListID()));

        //Fill data
        /*
    	TextView price = (TextView) this.findViewById(R.id.item_activity_price);
    	TextView pricetxt = (TextView) this.findViewById(R.id.item_activity_price_txt);
    	price.setText(String.format("%.2f",listCost()));*/
    	
    	ListView listView = (ListView) findViewById(R.id.items_list_view);
    	/*ArrayList<Item> items = null;
    	if(Omniscient.isShopping())
    	{
    		items = DbAdapter.getAllReceiptItems();
    		Log.d(TAG, "onCreate");
    		Log.d(TAG, "current RECEIPT list:" + DbAdapter.getCurrentReceiptListID());
    		for(Item i : items)
    			Log.d(TAG, "item: " + i.getName());
            adapter = new MyAdapter(this, 
					R.layout.items_list, 
					items);
    		pricetxt.setVisibility(View.VISIBLE);
    		price.setVisibility(View.VISIBLE);
    	}
    	else
    	{
    		items = DbAdapter.getAllShopItems();
    		Log.d(TAG, "onCreate");
    		Log.d(TAG, "current SHOP list: " + DbAdapter.getCurrentShopListID());
    		for(Item i : items)
    			Log.d(TAG, "item: " + i.getName());
            adapter = new MyAdapter(this, 
					R.layout.items_list, 
					items);
    		pricetxt.setVisibility(View.INVISIBLE);
    		price.setVisibility(View.INVISIBLE);	
    	}
    	
		listView.setAdapter(adapter);
		listView.setClickable(true);*/
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
