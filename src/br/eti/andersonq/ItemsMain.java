package br.eti.andersonq;

import br.eti.andersonq.shoplist.R;
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
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.AdapterView.AdapterContextMenuInfo;

public class ItemsMain extends ListActivity 
{
	//Tag to debug
    private static final String TAG = "ItemsMain";
	
	private static final int ACTIVITY_ITEM_CREATE = 0;
    private static final int ACTIVITY_ITEM_EDIT = 1;
    private static final int ACTIVITY_LIST_CREATE = 3;
    private static final int ACTIVITY_LIST_MAIN = 4;

    private static final int ITEM_DELETE_ID = Menu.FIRST;

    private DbAdapter mDbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.items_list);
        mDbHelper = new DbAdapter(this);
        mDbHelper.open();
        fillData();
        registerForContextMenu(getListView());
    }

    private void fillData() {
        // Get all of the rows from the database and create the item list
    	Cursor itemsCursor = mDbHelper.fetchAllItem();
        startManagingCursor(itemsCursor);

        // Create an array to specify the fields to display in the item list
        String[] from = new String[]{DbAdapter.ITEM_NAME, DbAdapter.ITEM_QUANTITY, DbAdapter.ITEM_PURCHASED};

        // and an array of the fields to bind those fields to
        int[] to = new int[]{R.id.item_name_row, R.id.item_quant_row, R.id.item_purchased_row};

        // Create a simple cursor adapter and set it to display
        SimpleCursorAdapter items = 
            new SimpleCursorAdapter(this, R.layout.items_row, itemsCursor, from, to);
        setListAdapter(items);
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
        		createItem();
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
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch(item.getItemId()) 
        {
            case ITEM_DELETE_ID:
                AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
                mDbHelper.deleteItem(info.id);
                fillData();
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void createItem() {
        Intent i = new Intent(this, ItemEdit.class);
        startActivityForResult(i, ACTIVITY_ITEM_CREATE);
    }
    
    private void createList() {
        Intent i = new Intent(this, ListsEdit.class);
        startActivityForResult(i, ACTIVITY_LIST_CREATE);
    }

    private void chooseList() {
        Intent i = new Intent(this, ListsMain.class);
        startActivityForResult(i, ACTIVITY_LIST_MAIN);
    }
    
    @Override
    /*
     * Called to edit a item
     */
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, ItemEdit.class);
        i.putExtra(DbAdapter.ITEM_ID, id);
        startActivityForResult(i, ACTIVITY_ITEM_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) 
    {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }
}
