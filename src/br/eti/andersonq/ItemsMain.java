package br.eti.andersonq;

import br.eti.andersonq.shoplist.R;
import android.app.Activity;
import android.app.FragmentManager;
import android.app.ListActivity;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.widget.CheckBox;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ItemsMain extends Activity implements Update
{
	//Tag to debug
    private static final String TAG = "ItemsMain";
	
	private static final int ACTIVITY_ITEM_CREATE = 0;
    private static final int ACTIVITY_ITEM_EDIT = 1;
    private static final int ACTIVITY_LIST_CREATE = 3;
    private static final int ACTIVITY_LIST_MAIN = 4;

    private static final int ITEM_DELETE_ID = Menu.FIRST;
    private static final int ITEM_EDIT_ID = Menu.FIRST + 1;

    //private DbAdapter mDbHelper;

	
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.items_list);
        //mDbHelper = new DbAdapter(this);
        DbAdapter.open(this);
        //fillData();
        ListView listView = (ListView) findViewById(R.id.items_list_view);
        MyAdapter adapter = new MyAdapter(this, R.layout.items_list, DbAdapter.getAlltems());
		listView.setAdapter(adapter);
		listView.setClickable(true);
        registerForContextMenu(listView);
    }

    private void fillData() 
    {
        ListView listView = (ListView) findViewById(R.id.items_list_view);
        MyAdapter adapter = new MyAdapter(this, R.layout.items_list, DbAdapter.getAlltems());
		listView.setAdapter(adapter);
		listView.setClickable(true);
    	/*
        // Get all of the rows from the database and create the item list
    	Cursor itemsCursor = mDbHelper.fetchAllItem();
        startManagingCursor(itemsCursor);

        // Create an array to specify the fields to display in the item list
        String[] from = new String[]{DbAdapter.ITEM_NAME, DbAdapter.ITEM_QUANTITY, DbAdapter.ITEM_PRICE, DbAdapter.ITEM_PURCHASED};

        // and an array of the fields to bind those fields to
        int[] to = new int[]{R.id.item_name_row, R.id.item_quant_row, R.id.item_price_row, R.id.item_purchased_row};

        // Create a simple cursor adapter and set it to display
        SimpleCursorAdapter items = 
            new SimpleCursorAdapter(this, R.layout.items_row, itemsCursor, from, to);
        setListAdapter(items);*/
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu items for use in the action bar
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.item_action_bar, menu);
        
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
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
            ContextMenuInfo menuInfo) {
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
	protected void onDestroy() 
	{
		DbAdapter.close();
		super.onDestroy();
	}
    
	@Override
	public void onSaveState() 
	{
		fillData();
	}
    
    /*
     * Call Fragment to create or edit a item
     * If id == -1 it will create a item, otherwise will edit the item with
     * the specified id
     */
    private void editItem(long id)
    {
    	ItemEditFrag fire = new ItemEditFrag();
    	FragmentManager manager = getFragmentManager();
    	/* it is not working, using POG/KOP (Kludge Oriented Programming)
    	Bundle args = new Bundle();
    	args.putInt(ItemEditFrag.ITEM_ID, (int) id);
    	fire.setArguments(args);*/
    	Omniscient.setCurrentItemID(id);
    	fire.show(manager, "FRAGMENT");
    }

    private void chooseList() {
        Intent i = new Intent(this, ListsMain.class);
        startActivityForResult(i, ACTIVITY_LIST_MAIN);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) 
    {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }
}
