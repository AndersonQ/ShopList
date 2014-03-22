package br.eti.andersonq;

import java.util.ArrayList;

import br.eti.andersonq.shoplist.R;
import android.app.ActionBar;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.SimpleCursorAdapter;
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
        setContentView(R.layout.items_list);
        //mDbHelper = new DbAdapter(this);
        DbAdapter.open(this);
        ActionBar ab = getActionBar();
        ab.setSubtitle(DbAdapter.getListName(DbAdapter.getCurrentListID()));

        //Fill data
    	TextView price = (TextView) this.findViewById(R.id.item_activity_price);
    	price.setText(String.format("%.2f",listCost()));
    	
        ListView listView = (ListView) findViewById(R.id.items_list_view);
        MyAdapter adapter = new MyAdapter(this, R.layout.items_list, DbAdapter.getAlltems());
		listView.setAdapter(adapter);
		listView.setClickable(true);
		
        registerForContextMenu(listView);
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
        switch(item.getItemId()) 
        {
        	case R.id.item_action_add:
        		//Create item 
        		editItem(-1);
                return true;
        	case R.id.item_action_choose_list:
        		chooseList();
            	return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) 
    {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, ITEM_DELETE_ID, 0, R.string.menu_item_delete);
        menu.add(0, ITEM_EDIT_ID, 0, R.string.menu_item_edit);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) 
    {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	
        switch(item.getItemId()) 
        {
            case ITEM_DELETE_ID:
            	DbAdapter.deleteItem(info.id);
                fillData();
                return true;
            case ITEM_EDIT_ID:
            	editItem(info.id);
            	return true;
        }
        return super.onContextItemSelected(item);
    }
    
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) 
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
		updateCost();
		fillData();
	}
	
	/**
	 * Fill the activity with the current list items
	 */
    private void fillData() 
    {
    	ListView listView = (ListView) findViewById(R.id.items_list_view);
        MyAdapter adapter = new MyAdapter(this, R.layout.items_list, DbAdapter.getAlltems());
		listView.setAdapter(adapter);
		listView.setClickable(true);
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
    	ArrayList<Item> items = DbAdapter.getAlltems();
    	float totalCost = 0;
    	
    	for(Item item : items)
    	{
    		totalCost += item.getQuantity() * item.getPrice() * item.getPurchased();
    	}

    	return totalCost;
    }
    
    @Override
    public void updateCost()
    {
    	TextView price = (TextView) this.findViewById(R.id.item_activity_price);
    	price.setText(String.format("%.2f",listCost()));
    }
}
