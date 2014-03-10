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

public class ListsMain extends ListActivity 
{
	//Tag to debug
	private static final String TAG = "ListsMain";
	
	private static final int ACTIVITY_CREATE=0;
    private static final int ACTIVITY_EDIT=1;

    private static final int DELETE_ID 	= Menu.FIRST;
    private static final int EDIT_ID 	= Menu.FIRST + 2;
    private static final int COPY_ID 	= Menu.FIRST + 3;

    private DbAdapter mDbHelper;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lists_list);
        mDbHelper = new DbAdapter(this);
        mDbHelper.open();
        fillData();
        registerForContextMenu(getListView());
    }

    private void fillData() {
        // Get all of the rows from the database and create the item list
    	Cursor listCursor = mDbHelper.fetchAllLists();
        startManagingCursor(listCursor);

        // Create an array to specify the fields we want to display in the list
        String[] from = new String[]{DbAdapter.LIST_NAME, DbAdapter.LIST_TIMESTAMP};

        // and an array of the fields we want to bind those fields to
        int[] to = new int[]{R.id.list_name_row, R.id.list_timestamp_row};

        // Now create a simple cursor adapter and set it to display
        SimpleCursorAdapter items = 
            new SimpleCursorAdapter(this, R.layout.lists_row, listCursor, from, to);
        setListAdapter(items);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.list_action_bar, menu);

        //menu.add(0, INSERT_ID, 0, R.string.menu_list_insert);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch(item.getItemId()) 
        {
        case R.id.list_action_add:
        	createList();
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v,
            ContextMenuInfo menuInfo) {
        super.onCreateContextMenu(menu, v, menuInfo);
        menu.add(0, DELETE_ID, 0, R.string.menu_list_delete);
        menu.add(0, EDIT_ID, 0, R.string.menu_list_edit);
        menu.add(0, COPY_ID, 0, R.string.menu_list_copy);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) 
    {
    	AdapterContextMenuInfo info = (AdapterContextMenuInfo) item.getMenuInfo();
    	long id = info.id;
        switch(item.getItemId()) 
        {
            case DELETE_ID:
                mDbHelper.deleteList(id);
                fillData();
                return true;
            case EDIT_ID:
                Intent i = new Intent(this, ListsEdit.class);
                i.putExtra(DbAdapter.LIST_ID, id);
                
                int tmp = (int) id;
                DbAdapter.setCurrentListID(tmp);
                
                startActivityForResult(i, ACTIVITY_EDIT);
                return true;
        }
        return super.onContextItemSelected(item);
    }

    private void createList() {
        Intent i = new Intent(this, ListsEdit.class);
        startActivityForResult(i, ACTIVITY_CREATE);
    }

    @Override
    protected void onListItemClick(ListView l, View v, int position, long id) {
        super.onListItemClick(l, v, position, id);
        Intent i = new Intent(this, ItemsMain.class);
        i.putExtra(DbAdapter.LIST_ID, id);
        
        int tmp = (int) id;
        DbAdapter.setCurrentListID(tmp);
        
        startActivityForResult(i, ACTIVITY_EDIT);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);
        fillData();
    }
}
