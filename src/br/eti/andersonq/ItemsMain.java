package br.eti.andersonq;

import java.util.ArrayList;

import br.eti.andersonq.shoplist.R;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Application;
import android.app.FragmentManager;
import android.content.Intent;
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
        Application app = getApplication();
        Omniscient.setApp(app);
        setContentView(R.layout.items_list);
        //mDbHelper = new DbAdapter(this);
        DbAdapter.open(this);
        ActionBar ab = getActionBar();
        ab.setSubtitle(DbAdapter.getShopListName(DbAdapter.getCurrentShopListID()));

        //Fill data
    	TextView price = (TextView) this.findViewById(R.id.item_activity_price);
    	TextView pricetxt = (TextView) this.findViewById(R.id.item_activity_price_txt);
    	price.setText(String.format("%.2f",listCost()));
    	if(Omniscient.isShopping())
    	{
    		pricetxt = (TextView) this.findViewById(R.id.item_activity_price_txt);
    		pricetxt.setVisibility(View.VISIBLE);
    		price.setVisibility(View.VISIBLE);
    	}
    	else
    	{
    		pricetxt = (TextView) this.findViewById(R.id.item_activity_price_txt);
    		pricetxt.setVisibility(View.INVISIBLE);
    		price.setVisibility(View.INVISIBLE);	
    	}
    	
        ListView listView = (ListView) findViewById(R.id.items_list_view);
        MyAdapter adapter = new MyAdapter(this, 
        								R.layout.items_list, 
        								DbAdapter.getAllShopItems());
		listView.setAdapter(adapter);
		listView.setClickable(true);
		
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
     * My methods
     * ************************************************************************
     * ************************************************************************
     */
	
	@Override
	public void updateDisplayedData() 
	{
		if(Omniscient.isShopping())
			updateCost();
		fillData();
	}
	
	/**
	 * Fill the activity with the current items list
	 */
    private void fillData() 
    {
    	if(Omniscient.isShopping())
    	{
    		ArrayList<Item> items = DbAdapter.getAllReceiptItems();
	    	ListView listView = (ListView) findViewById(R.id.items_list_view);
	        MyAdapter adapter = new MyAdapter(this, 
	        								R.layout.items_list, 
	        								items);
			listView.setAdapter(adapter);
			listView.setClickable(true);
    	}
    	else
    	{
	    	ListView listView = (ListView) findViewById(R.id.items_list_view);
	        MyAdapter adapter = new MyAdapter(this, 
	        								R.layout.items_list, 
	        								DbAdapter.getAllShopItems());
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
		//Try to get RecipList associated with current shop list
		long receiptListId = DbAdapter.getReceiptListFromShopList();
		if(receiptListId == 0)//No associated receiptList
		{
			//Create receipt list from current shop list
			receiptListId = DbAdapter.createReceiptList();
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
		
		//Fill activity
		fillData();
		//Update total cost
		updateCost();
		
		mStartShopping.setVisible(false);
		mStopShopping.setVisible(true);
		
		/* 
		 * TODO
		 * auto-remove option
		 * */
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
}
