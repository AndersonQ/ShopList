package br.eti.andersonq;

import java.util.ArrayList;

import br.eti.andersonq.shoplist.R;
import android.app.ActionBar;
import android.app.Activity;
import android.app.Application;
import android.app.FragmentManager;
import android.app.ListActivity;
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

    private static final int ITEM_DELETE_ID = Menu.FIRST;
    private static final int ITEM_EDIT_ID = Menu.FIRST + 1;
	
    
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
        ab.setSubtitle(DbAdapter.getListName(DbAdapter.getCurrentShopListID()));

        //Fill data
    	TextView price = (TextView) this.findViewById(R.id.item_activity_price);
    	price.setText(String.format("%.2f",listCost()));
    	
        ListView listView = (ListView) findViewById(R.id.items_list_view);
        MyAdapter adapter = new MyAdapter(this, 
        								R.layout.items_list, 
        								DbAdapter.getAllShopItems());
		listView.setAdapter(adapter);
		listView.setClickable(true);
		
        registerForContextMenu(listView);
        //AlertDialog alert;
        //alert.getListView().setOnCreateContextMenuListener(this);
        //listView.setOnCreateContextMenuListener(this);
        
        //Get default settings
        PreferenceManager.setDefaultValues(app, R.xml.preferences, false);        
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) 
    {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.item_action_bar, menu);
         
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
	 * Fill the activity with the current list items
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
		//Create receipt list from current shop list
		long receiptListId = DbAdapter.createReceiptList();
		DbAdapter.setCurrentReceiptListID((int) receiptListId);
		fillData();
		
		/* 
		 * TODO
		 * Use receipt list, it means go shopping
		 * track prices and purchased flag
		 * auto-remove option
		 * */

	}
	
    /**
	 * Stop shopping
	 */
	private void stopShopping()
	{
		//Set shopping true
		Omniscient.setShopping(false);
		fillData();
		//Create receipt list from current shop list
		//DbAdapter.createReceiptList();
		
		/* 
		 * TODO
		 * Use receipt list, it means go shopping
		 * track prices and purchased flag
		 * auto-remove option
		 * */

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
    	TextView price = (TextView) this.findViewById(R.id.item_activity_price);
    	float p = listCost();
    	price.setText(String.format("%.2f",p));
    }
}
