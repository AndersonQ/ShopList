package br.eti.andersonq;

import java.util.ArrayList;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.os.Bundle;
import android.util.Log;

public class DbAdapter {

	//Database name
	private static final String DATABASE_NAME = "ShopList";
	/*
	 * Table Items
	 */
	public static final String TABLE_NAME = "SL_Items";
	//Primary key
	public static final String ITEM_ID = "_id";
	public static final String ITEM_NAME = "name";
	public static final String ITEM_QUANTITY = "quantitly";
	public static final String ITEM_PRICE = "price";
	public static final String ITEM_PURCHASED = "purchased";
	//Foreign key to table 'ShopLists'
	public static final String ITEM_LIST_ID = "listID";
	public static final int ITEM_DB_VERSON = 1;
	
	/*
	 * Table ShopLists
	 */
	public static final String LISTS_TABLE = "SL_Lists";
	//Primary key
	public static final String LIST_ID = "_id";
	public static final String LIST_NAME = "name";
	public static final String LIST_TIMESTAMP = "time_stamp";
	public static final int LIST_DB_VERSON = 1;
	
	/*
	 * SQL Create table statements
	 */
	private static final String DB_CREATE_ITEM = "create table " + TABLE_NAME + " (" +
			ITEM_ID + " integer primary key autoincrement, " +
			ITEM_NAME + " text not null, " +
			ITEM_QUANTITY + " integer not null, " +
			ITEM_PURCHASED + " integer not null, " +
			ITEM_PRICE + " decimal, " +
			ITEM_LIST_ID + " integer);";
	
	public static final String DB_CREATE_SHOPLIST = "create table " + LISTS_TABLE + " (" +
			LIST_ID + " integer primary key autoincrement, " +
			LIST_NAME + " text not null, " + 
			LIST_TIMESTAMP + " timestamp not null default current_timestamp);";
	
	private static final int DATABASE_VERSION = 2;
	
	//End of database definition
	
	//Keep track of the current shoplist being used
	private static int currentListID = 0;
    
	//Tag to debug
    private static final String TAG = "DbAdapter";
    
    private DatabaseHelper mDbHelper;
    private SQLiteDatabase mDb;
    private final Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db) {

        	//Create tables
            db.execSQL(DB_CREATE_ITEM);
            db.execSQL(DB_CREATE_SHOPLIST);
            
            //Insert default list on database
            db.execSQL("insert into " + 
            		LISTS_TABLE + 
            		"(" + LIST_NAME + ")" +
            		"values ('default');");
            setCurrentListID(1);
        }

        @Override
        public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
            Log.w(TAG, "Upgrading database from version " + oldVersion + " to "
                    + newVersion + ", which will destroy all old data");
            db.execSQL("DROP TABLE IF EXISTS " + TABLE_NAME);
            onCreate(db);
        }
    }

    public DbAdapter(Context ctx) {
        this.mCtx = ctx;
    }

    public DbAdapter open() throws SQLException {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
        return this;
    }

    public void close() {
        mDbHelper.close();
    }

    /*
     * Methods to deal with Items in a list
     */
    public long createItem(String itemName, int itemQuant, float itemPrice, int purchased, int id) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(ITEM_NAME, itemName);
        initialValues.put(ITEM_QUANTITY, itemQuant);
        initialValues.put(ITEM_PRICE, itemPrice);
        initialValues.put(ITEM_PURCHASED, purchased);
        initialValues.put(ITEM_LIST_ID, id);
        
        Log.v(TAG + " - createItem", "Item: " + itemName + ", listID: " + getCurrentListID());

        return mDb.insert(TABLE_NAME, null, initialValues);
    }

    public boolean deleteItem(long rowId) {

        return mDb.delete(TABLE_NAME, ITEM_ID + "=" + rowId, null) > 0;
    }

    public Cursor fetchAllItem() {

        return mDb.query(TABLE_NAME, new String[] {ITEM_ID, ITEM_NAME,
                ITEM_QUANTITY, ITEM_PRICE, ITEM_PURCHASED}, ITEM_LIST_ID + "=" + getCurrentListID(), null, null, null, null);
    }
    

    public Cursor fetchItem(long rowId) throws SQLException {

        Cursor mCursor =
            mDb.query(true, TABLE_NAME, new String[] {ITEM_ID,
                    ITEM_NAME, ITEM_QUANTITY, ITEM_PRICE, ITEM_PURCHASED}, ITEM_ID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }

    public boolean updateItem(long id, String name, int quant, float price, int purchased) {
        ContentValues args = new ContentValues();
        args.put(ITEM_NAME, name);
        args.put(ITEM_QUANTITY, quant);
        args.put(ITEM_PRICE, price);
        args.put(ITEM_PURCHASED, purchased);

        return mDb.update(TABLE_NAME, args, ITEM_ID + "=" + id, null) > 0;
    }
    /*
     * End of methods to deal with Items in a list
     */
    
    
    /*
     * Methods to deal with Shop lists
     */
    public long createList(String listName) {
		ContentValues initVals = new ContentValues();
		
		initVals.put(this.LIST_NAME, listName);
		
		return mDb.insert(this.LISTS_TABLE, null, initVals);
    }
    
    public long copyList(long oldListID, String newListName)
    {
    	int idxITEM_NAME, idxITEM_QUANTITY, idxITEM_PRICE, idxITEM_PURCHASED;
    	//Cursor to old list
    	Cursor oldList = mDb.query(TABLE_NAME, new String[] {ITEM_ID, ITEM_NAME,
                ITEM_QUANTITY, ITEM_PURCHASED}, ITEM_LIST_ID + "=" + (int) oldListID, null, null, null, null);

    	//Get column index to each column
    	idxITEM_NAME = oldList.getColumnIndex(ITEM_NAME);
    	idxITEM_QUANTITY = oldList.getColumnIndex(ITEM_QUANTITY);
    	idxITEM_PRICE = oldList.getColumnIndex(ITEM_PRICE);
    	idxITEM_PURCHASED = oldList.getColumnIndex(ITEM_PURCHASED);
    	
    	//Create new list
    	long newListID = createList(newListName);
    	
    	//Go to first item
    	oldList.moveToFirst();
    	do{
    		createItem(oldList.getString(idxITEM_NAME), 
    				oldList.getInt(idxITEM_QUANTITY), 
    				oldList.getFloat(idxITEM_PRICE),
    				oldList.getInt(idxITEM_PURCHASED), 
    				(int)newListID );
    	}while (oldList.moveToNext());
    	
    	return newListID;
    }

    public boolean deleteList(long id) {

    	return mDb.delete(LISTS_TABLE, LIST_ID + "=" + id, null) > 0;
    }

    public Cursor fetchAllLists() {

		return mDb.query(LISTS_TABLE, 
				new String [] {LIST_ID, LIST_NAME, LIST_TIMESTAMP},
				null, null, null, null, null);
    }

    public Cursor fetchList(long id) throws SQLException {

        Cursor mCursor = mDb.query(true, LISTS_TABLE, new String[] {LIST_ID,
            				LIST_NAME, LIST_TIMESTAMP}, LIST_ID + "=" + id, null,
            				null, null, null, null);
        
        if (mCursor != null)
            mCursor.moveToFirst();
        
        return mCursor;
    }

    public boolean updateList(long id, String listName) {
        ContentValues args = new ContentValues();
        args.put(LIST_NAME, listName);

        return mDb.update(LISTS_TABLE, args, LIST_ID + "=" + id, null) > 0;
    }
    
	public static int getCurrentListID() {
		return currentListID;
	}

	public static void setCurrentListID(int listID) {
		currentListID = listID;
	}
	
	/**
	 * Get name of a list
	 * @param id of the list to to get the name
	 * @return
	 *
	//TODO make it to work!!
	public String getListName(int id)
	{
		String sql = "select " + LIST_NAME + 
				" from " + LISTS_TABLE +
				" where " + LIST_ID + "=" + id;
		Cursor mCursor = mDb.query(LISTS_TABLE, new String[] {LIST_ID, LIST_NAME}, 
				LIST_ID + "=" + id, null,
				null, null, null);
		mCursor.moveToFirst();
		
		int idx = mCursor.getColumnIndex(LIST_NAME);
		String res = mCursor.getString(idx);
		return res;
	}*/
    
	/*
	 * End of methods to deal with Shop lists
	 */
}
