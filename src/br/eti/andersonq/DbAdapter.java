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
	public static final String ITEMS_TABLE = "SL_Items";
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
	private static final String DB_CREATE_ITEM = "create table " + ITEMS_TABLE + " (" +
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
	private static int currentListID = 1;
    
	//Tag to debug
    private static final String TAG = "DbAdapter";
    
    private static DatabaseHelper mDbHelper;
    private static SQLiteDatabase mDb;
    private static Context mCtx;

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
            db.execSQL("DROP TABLE IF EXISTS " + ITEMS_TABLE);
            onCreate(db);
        }
    }

    public DbAdapter(Context ctx) 
    {
        //DbAdapter.mCtx = ctx;
    }

    public static void open(Context ctx) throws SQLException 
    {
    	DbAdapter.mCtx = ctx;
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
    }
    
    public static void open() throws SQLException 
    {
        mDbHelper = new DatabaseHelper(mCtx);
        mDb = mDbHelper.getWritableDatabase();
    }

    public static void close() 
    {
        mDbHelper.close();
    }


    /*
     * ************************************************************************
     * ************************************************************************
     * Methods to deal with Items in a list
     * ************************************************************************
     * ************************************************************************
     */
    /**
     * Create an item
     * @param itemName item name
     * @param itemQuant item quantity
     * @param price item price
     * @param purchased purchased if the item was purchased (0-false, 1-true)
     * @param listId if of the item's list
     * @return new item ID
     */
    public static long createItem(String itemName, int itemQuant, float price, int purchased, int listId) {
        ContentValues initialValues = new ContentValues();
        initialValues.put(ITEM_NAME, itemName);
        initialValues.put(ITEM_QUANTITY, itemQuant);
        initialValues.put(ITEM_PRICE, price);
        initialValues.put(ITEM_PURCHASED, purchased);
        initialValues.put(ITEM_LIST_ID, listId);
        
        return mDb.insert(ITEMS_TABLE, null, initialValues);
    }

    public static boolean deleteItem(long rowId) {

        return mDb.delete(ITEMS_TABLE, ITEM_ID + "=" + rowId, null) == 1;
    }

    public static Cursor fetchAllItem() {

        return mDb.query(ITEMS_TABLE, new String[] {ITEM_ID, ITEM_NAME,
                ITEM_QUANTITY, ITEM_PRICE, ITEM_PURCHASED}, ITEM_LIST_ID + "=" + getCurrentListID(), null, null, null, null);
    }

    /**
     * Get all item of current shop list
     * @return A array of Item
     */
    public static ArrayList<Item> getAlltems()
    {
    	//Id of each correspondent column 
    	int idxITEM_ID, idxITEM_NAME, idxITEM_QUANTITY, idxITEM_PRICE, idxITEM_PURCHASED, idxITEM_LIST_ID;
    	//Array to store all items of this list
    	ArrayList<Item> items = new ArrayList<Item>();
    	//Cursor to old list
    	Cursor itemsCursor = mDb.query(ITEMS_TABLE, null, ITEM_LIST_ID + "=" + getCurrentListID(), null, null, null, null);
    	//Go to first item, if there isen't a first so there is no items at all
    	if(itemsCursor.moveToFirst())
    	{
	    	//Get column index to each column
	    	idxITEM_ID = itemsCursor.getColumnIndexOrThrow(ITEM_ID);
	    	idxITEM_NAME = itemsCursor.getColumnIndex(ITEM_NAME);
	    	idxITEM_QUANTITY = itemsCursor.getColumnIndex(ITEM_QUANTITY);
	    	idxITEM_PRICE = itemsCursor.getColumnIndex(ITEM_PRICE);
	    	idxITEM_PURCHASED = itemsCursor.getColumnIndex(ITEM_PURCHASED);
	    	idxITEM_LIST_ID = itemsCursor.getColumnIndex(ITEM_LIST_ID);
    	
	    	//Store all fetched items on 'items' array
	    	do{
	    		int id = itemsCursor.getInt(idxITEM_ID);
	    		String name = itemsCursor.getString(idxITEM_NAME);
	    		int quantity = itemsCursor.getInt(idxITEM_QUANTITY);
	    		float price = itemsCursor.getFloat(idxITEM_PRICE);
	    		int purchased = itemsCursor.getInt(idxITEM_PURCHASED);
	    		int listId = itemsCursor.getInt(idxITEM_LIST_ID);
	    		//Add fetched item in the array
	    		items.add(new Item(id, listId, name, quantity, price, purchased));
	    	}while (itemsCursor.moveToNext());
    	}
    	
    	return items;
    }

    public static Cursor fetchItem(long rowId) throws SQLException {

        Cursor mCursor =

            mDb.query(true, ITEMS_TABLE, new String[] {ITEM_ID,
                    ITEM_NAME, ITEM_QUANTITY, ITEM_PRICE, ITEM_PURCHASED}, ITEM_ID + "=" + rowId, null,
                    null, null, null, null);
        if (mCursor != null) {
            mCursor.moveToFirst();
        }
        return mCursor;
    }
    
    /**
     * Get a item by its ID
     * @param id item's id
     * @return an object Item
     */
    public static Item getItem(long id)
    {
    	//Id of each correspondent column 
    	int /*idxITEM_ID,*/ idxITEM_NAME, idxITEM_QUANTITY, idxITEM_PRICE, idxITEM_PURCHASED, idxITEM_LIST_ID;
    	//Array to store all items of this list
    	Item item = null;
    	//Cursor to old list
    	Cursor itemsCursor = mDb.query(true, ITEMS_TABLE, new String[] {ITEM_ID,
                ITEM_NAME, ITEM_QUANTITY, ITEM_PRICE, ITEM_PURCHASED, ITEM_LIST_ID}, ITEM_ID + "=" + id, null,
                null, null, null, null);
    	//Go to first item, if there isen't a first so there is no items at all
    	if(itemsCursor.moveToFirst())
    	{
	    	//Get column index to each column
	    	idxITEM_NAME = itemsCursor.getColumnIndex(ITEM_NAME);
	    	idxITEM_QUANTITY = itemsCursor.getColumnIndex(ITEM_QUANTITY);
	    	idxITEM_PRICE = itemsCursor.getColumnIndex(ITEM_PRICE);
	    	idxITEM_PURCHASED = itemsCursor.getColumnIndex(ITEM_PURCHASED);
	    	idxITEM_LIST_ID = itemsCursor.getColumnIndex(ITEM_LIST_ID);
    	
    		String name = itemsCursor.getString(idxITEM_NAME);
    		int quantity = itemsCursor.getInt(idxITEM_QUANTITY);
    		float price = itemsCursor.getFloat(idxITEM_PRICE);
    		int purchased = itemsCursor.getInt(idxITEM_PURCHASED);
    		int listId = itemsCursor.getInt(idxITEM_LIST_ID);
    		//Add fetched item in the array
	    	item =  new Item(id, listId, name, quantity, price, purchased);
    	}
    	
    	return item;
    }

    /**
     * Update informations of a item
     * @param id item ID
     * @param name item Name
     * @param quant item quantity
     * @param price item price
     * @param purchased if the item was purchased (0-false, 1-true)
     * @return true success, false fail
     */
    public static boolean updateItem(long id, String name, int quant, float price, int purchased) {
        ContentValues args = new ContentValues();
        args.put(ITEM_NAME, name);
        args.put(ITEM_QUANTITY, quant);
        args.put(ITEM_PRICE, price);
        args.put(ITEM_PURCHASED, purchased);

        return mDb.update(ITEMS_TABLE, args, ITEM_ID + "=" + id, null) == 1;
    }
    
    /**
     * 
     * Update informations of a item
     * @param id item ID
     * @param name item Name
     * @param quant item quantity
     * @param price item price
     * @return true success, false fail
     */
    public static boolean updateItem(long id, String name, int quant, float price) 
    {
        ContentValues args = new ContentValues();
        args.put(ITEM_NAME, name);
        args.put(ITEM_QUANTITY, quant);
        args.put(ITEM_PRICE, price);

        return mDb.update(ITEMS_TABLE, args, ITEM_ID + "=" + id, null) == 1;
    }
    
    /**
     * Update informations of a item
     * @param item object Item
     * @return true if one row was updated, false otherwise
     */
    public static boolean updateItem(Item item)
    {
        ContentValues args = new ContentValues();
        args.put(ITEM_NAME, item.getName());
        args.put(ITEM_QUANTITY, item.getQuantity());
        args.put(ITEM_PRICE, item.getPrice());
        args.put(ITEM_PURCHASED, item.getPurchased());

        return mDb.update(ITEMS_TABLE, args, ITEM_ID + "=" + item.getId(), null) == 1;
    }
    /*
     * ************************************************************************
     * ************************************************************************
     * End of methods to deal with Items in a list
     * ************************************************************************
     * ************************************************************************

     */
    
    
    /*
     * ************************************************************************
     * ************************************************************************
     * Methods to deal with Shop lists
     * ************************************************************************
     * ************************************************************************
     */
    /**
     * Create a new list
     * @param listName name of new list
     * @return
     */
    public static long createList(String listName) {
		ContentValues initVals = new ContentValues();
		
		initVals.put(LIST_NAME, listName);
		
		return mDb.insert(LISTS_TABLE, null, initVals);
    }
    
    public static long copyList(long oldListID, String newListName)
    {
    	int idxITEM_NAME, idxITEM_QUANTITY, idxITEM_PRICE, idxITEM_PURCHASED;
    	//Cursor to old list
    	Cursor oldList = mDb.query(ITEMS_TABLE, new String[] {ITEM_ID, ITEM_NAME,
                ITEM_QUANTITY, ITEM_PRICE, ITEM_PURCHASED}, ITEM_LIST_ID + "=" + (int) oldListID, null, null, null, null);

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

    public static boolean deleteList(long id) 
    {
    	boolean listRet = mDb.delete(LISTS_TABLE, LIST_ID + "=" + id, null) == 1;
    	mDb.delete(ITEMS_TABLE, ITEM_LIST_ID + "=" + id, null);
    	
    	return listRet;
    }

    public static Cursor fetchAllLists() {

		return mDb.query(LISTS_TABLE, 
				new String [] {LIST_ID, LIST_NAME, LIST_TIMESTAMP},
				null, null, null, null, null);
    }

    public static Cursor fetchList(long id) throws SQLException {

        Cursor mCursor = mDb.query(true, LISTS_TABLE, new String[] {LIST_ID,
            				LIST_NAME, LIST_TIMESTAMP}, LIST_ID + "=" + id, null,
            				null, null, null, null);
        
        if (mCursor != null)
            mCursor.moveToFirst();
        
        return mCursor;
    }

    public static boolean updateList(long id, String listName) {
        ContentValues args = new ContentValues();
        args.put(LIST_NAME, listName);

        return mDb.update(LISTS_TABLE, args, LIST_ID + "=" + id, null) > 0;
    }
    
    public static String getListName(long id)
    {
    	Cursor cursor = mDb.query(true, LISTS_TABLE, new String[] {LIST_ID,
				LIST_NAME}, LIST_ID + "=" + id, null,
				null, null, null, null);
    	    	
    	return cursor.moveToFirst() == true ? cursor.getString(cursor.getColumnIndex(LIST_NAME)) : null;
    }
    
	public static int getCurrentListID() {
		return currentListID;
	}

	public static void setCurrentListID(int listID) {
		currentListID = listID;
	}
    
	/*
     * ************************************************************************
     * ************************************************************************
	 * End of methods to deal with Shop lists
     * ************************************************************************
     * ************************************************************************
	 */
}
