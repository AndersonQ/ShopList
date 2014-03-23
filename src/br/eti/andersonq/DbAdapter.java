/**
 * DbAdapter.java is part of ShopList.
 *
 * ShopList is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * ShopList is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with ShopList.  If not, see <http://www.gnu.org/licenses/>.
 */
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

/**
 * 
 * @author	Anderson de Franca Queiroz
 * @email	contato@andersonq.eti.br
 */
public class DbAdapter 
{
	//Tag to debug
    private static final String TAG = "DbAdapter";

	/*
	 * Database name
	 */
	private static final String DATABASE_NAME = "ShopList";
	
	/*
	 * Table Items
	 * Store all items from shop lists
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
	
	/*
	 * Table Receipt Items
	 * Store all items from receipt lists
	 */
	public static final String RECEIPT_ITEMS_TABLE = "SL_Receipt_Items";
	//Primary key
	public static final String RECEIPT_ITEM_ID = "_id";
	public static final String RECEIPT_ITEM_NAME = "name";
	public static final String RECEIPT_ITEM_QUANTITY = "quantitly";
	public static final String RECEIPT_ITEM_PRICE = "price";
	public static final String RECEIPT_ITEM_PURCHASED = "purchased";
	//Foreign key to table 'ShopLists'
	public static final String RECEIPT_ITEM_LIST_ID = "receipt_listID";
	
	/*
	 * Table ShopLists
	 * A shop list is a list of item to be bought,
	 * there is no need to neither price of flag 'purchased'
	 */
	public static final String LISTS_TABLE = "SL_Lists";
	//Primary key
	public static final String LIST_ID = "_id";
	public static final String LIST_NAME = "name";
	//Foreign key to Receipt list table
	public static final String LIST_RECEIPT_LIST_ID = "receipt_list_id";
	public static final String LIST_TIMESTAMP = "time_stamp";
	
	/*
	 * Table Receipt
	 * A shop list used meanwhile buying stuff, so it keeps
	 * price and flag 'purchased'
	 * When 'start shopping' a 'shop list' is copied to become
	 * a 'receipt'
	 */
	public static final String RECEIPT_LIST_TABLE = "SL_Receipts";
	//Primary key
	public static final String RECEIPT_LIST_ID = "_id";
	public static final String RECEIPT_LIST_NAME = "name";
	public static final String RECEIPT_LIST_TIMESTAMP = "time_stamp";
	
	/*
	 * SQL Create table statements
	 */
	private static final String DB_CREATE_ITEM_TABLE = "create table " + 
			ITEMS_TABLE + " (" +
			ITEM_ID + " integer primary key autoincrement, " +
			ITEM_NAME + " text not null, " +
			ITEM_QUANTITY + " integer not null, " +
			ITEM_PURCHASED + " integer not null, " +
			ITEM_PRICE + " decimal, " +
			ITEM_LIST_ID + " integer);";
	
	private static final String DB_CREATE_RECEIPT_ITEMS_TABLE = "create table "+ 
			RECEIPT_ITEMS_TABLE + " (" +
			RECEIPT_ITEM_ID + " integer primary key autoincrement, " +
			RECEIPT_ITEM_NAME + " text not null, " +
			RECEIPT_ITEM_QUANTITY + " integer not null, " +
			RECEIPT_ITEM_PURCHASED + " integer not null, " +
			RECEIPT_ITEM_PRICE + " decimal, " +
			RECEIPT_ITEM_LIST_ID + " integer);";
	
	public static final String DB_CREATE_SHOPLIST_TABLE = "create table " + 
			LISTS_TABLE + " (" +
			LIST_ID + " integer primary key autoincrement, " +
			LIST_NAME + " text not null, " + 
			LIST_RECEIPT_LIST_ID + " integer, " +
			LIST_TIMESTAMP + " timestamp not null default current_timestamp);";
	
	public static final String DB_CREATE_RECEIPT_TABLE = "create table " + 
			RECEIPT_LIST_TABLE + " (" +
			RECEIPT_LIST_ID + " integer primary key autoincrement, " +
			RECEIPT_LIST_NAME + " text not null, " + 
			RECEIPT_LIST_TIMESTAMP + " timestamp not null default current_timestamp);";
		
	private static final int DATABASE_VERSION = 1;
	
	//End of database definition
	
	//Keep track of the current shop list being used
	private static int currentShopListID = 1;
	//Keep track of the current receipt list being used
	private static int currentReceiptListID = -2;

	private static DatabaseHelper mDbHelper;
    private static SQLiteDatabase mDb;
    private static Context mCtx;

    private static class DatabaseHelper extends SQLiteOpenHelper {

        DatabaseHelper(Context context) {
            super(context, DATABASE_NAME, null, DATABASE_VERSION);
        }

        @Override
        public void onCreate(SQLiteDatabase db)
        {
        	//Create tables
            db.execSQL(DB_CREATE_ITEM_TABLE);
            db.execSQL(DB_CREATE_RECEIPT_ITEMS_TABLE);
            db.execSQL(DB_CREATE_SHOPLIST_TABLE);
            db.execSQL(DB_CREATE_RECEIPT_TABLE);
            
            //Insert default list on database
            db.execSQL("insert into " + 
            		LISTS_TABLE + 
            		"(" + LIST_NAME + ")" +
            		"values ('default');");

            setCurrentShopListID(1);
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
     * Create a shop item
     * @param itemName item name
     * @param itemQuant item quantity
     * @param price item price
     * @param purchased purchased if the item was purchased (0-false, 1-true)
     * @param listId if of the item's list
     * @return new item ID
     */
    public static long createShopItem(	String itemName, 
    									int itemQuant, 
    									float price, 
    									int purchased, 
    									int listId) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(ITEM_NAME, itemName);
        initialValues.put(ITEM_QUANTITY, itemQuant);
        initialValues.put(ITEM_PRICE, price);
        initialValues.put(ITEM_PURCHASED, purchased);
        initialValues.put(ITEM_LIST_ID, listId);
        
        return mDb.insert(ITEMS_TABLE, null, initialValues);
    }
    
    /**
     * Create a receipt item
     * @param item an Item object
     * @return the receipt item id
     */
    public static long createReceiptItem(Item item)
    {
        ContentValues args = new ContentValues();
        args.put(RECEIPT_ITEM_NAME, item.getName());
        args.put(RECEIPT_ITEM_QUANTITY, item.getQuantity());
        args.put(RECEIPT_ITEM_PRICE, item.getPrice());
        args.put(RECEIPT_ITEM_PURCHASED, item.getPurchased());
        args.put(RECEIPT_ITEM_LIST_ID, (int) item.getId());
        
    	return mDb.insert(RECEIPT_ITEMS_TABLE, null, args);
    }
    
    /**
     * 
     * Create a receipt item
     * @param itemName item name
     * @param itemQuant item quantity
     * @param price item price
     * @param purchased purchased if the item was purchased (0-false, 1-true)
     * @param receiptListId if of the item's list
     * @return new item ID
     */
    public static long createReceiptItem(	String itemName, 
    										int itemQuant, 
    										float price, 
    										int purchased, 
    										long receiptListId) 
    {
        ContentValues initialValues = new ContentValues();
        initialValues.put(RECEIPT_ITEM_NAME, itemName);
        initialValues.put(RECEIPT_ITEM_QUANTITY, itemQuant);
        initialValues.put(RECEIPT_ITEM_PRICE, price);
        initialValues.put(RECEIPT_ITEM_PURCHASED, purchased);
        initialValues.put(RECEIPT_ITEM_LIST_ID, (int) receiptListId);
        
        return mDb.insert(RECEIPT_ITEMS_TABLE, null, initialValues);
    }

    /**
     * Delete a specified shop item
     * @param id, item id
     * @return true if one row was deleted, false otherwise
     */
    public static boolean deleteShopItem(long id) 
    {
    	int affectedRows = mDb.delete(ITEMS_TABLE, ITEM_ID + "=" + (int)id, null);
        return  affectedRows == 1;
    }
    
    /**
     * Delete a specified receipt item
     * @param id, item id
     * @return true if one row was deleted, false otherwise
     */
    public static boolean deleteReceiptItem(long id) 
    {
        return mDb.delete(RECEIPT_ITEMS_TABLE, RECEIPT_ITEM_ID + "=" + (int)id, null) == 1;
    }

    public static Cursor fetchAllItem() {

        return mDb.query(ITEMS_TABLE, new String[] {ITEM_ID, ITEM_NAME,
                ITEM_QUANTITY, ITEM_PRICE, ITEM_PURCHASED}, ITEM_LIST_ID + "=" +
                		currentShopListID, null, null, null, null);
    }

    /**
     * Get all item of current shop list
     * @return A array of Item
     */
    public static ArrayList<Item> getAllShopItems()
    {
    	//Id of each correspondent column 
    	int idxITEM_ID, 
    		idxITEM_NAME, 
    		idxITEM_QUANTITY, 
    		idxITEM_PRICE, 
    		idxITEM_PURCHASED, 
    		idxITEM_LIST_ID;
    	
    	//Array to store all items of this list
    	ArrayList<Item> items = new ArrayList<Item>();
    	//Cursor to old list
    	Cursor itemsCursor = mDb.query(ITEMS_TABLE, 
    									new String[] 	{ITEM_ID, 
    													ITEM_NAME,
    													ITEM_QUANTITY, 
    													ITEM_PRICE, 
    													ITEM_PURCHASED,
    													ITEM_LIST_ID}, 
    									ITEM_LIST_ID + "=" + currentShopListID, 
    									null, null, null, null);
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
    
    /**
     * Get all item of current receipt list
     * @return A array of Item
     */
    public static ArrayList<Item> getAllReceiptItems()
    {
    	//Id of each correspondent column 
    	int idxITEM_ID, idxITEM_NAME, idxITEM_QUANTITY, idxITEM_PRICE, idxITEM_PURCHASED, idxITEM_LIST_ID;
    	//Array to store all items of this list
    	ArrayList<Item> items = new ArrayList<Item>();
    	//Cursor to receipt list
    	Cursor itemsCursor = mDb.query(RECEIPT_ITEMS_TABLE, null, RECEIPT_ITEM_LIST_ID + " = " + currentReceiptListID, null, null, null, null);
    	//Go to first item, if there isen't a first so there is no items at all
    	if(itemsCursor.moveToFirst())
    	{
	    	//Get column index to each column
	    	idxITEM_ID = itemsCursor.getColumnIndexOrThrow(RECEIPT_ITEM_ID);
	    	idxITEM_NAME = itemsCursor.getColumnIndex(RECEIPT_ITEM_NAME);
	    	idxITEM_QUANTITY = itemsCursor.getColumnIndex(RECEIPT_ITEM_QUANTITY);
	    	idxITEM_PRICE = itemsCursor.getColumnIndex(RECEIPT_ITEM_PRICE);
	    	idxITEM_PURCHASED = itemsCursor.getColumnIndex(RECEIPT_ITEM_PURCHASED);
	    	idxITEM_LIST_ID = itemsCursor.getColumnIndex(RECEIPT_ITEM_LIST_ID);
    	
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
                    ITEM_NAME, ITEM_QUANTITY, ITEM_PRICE, ITEM_PURCHASED}, ITEM_ID + "=" + (int)rowId, null,
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
    public static Item getShopItem(long id)
    {
    	//Id of each correspondent column 
    	int /*idxITEM_ID,*/ idxITEM_NAME, idxITEM_QUANTITY, idxITEM_PRICE, idxITEM_PURCHASED, idxITEM_LIST_ID;
    	//Array to store all items of this list
    	Item item = null;
    	//Cursor to old list
    	Cursor itemsCursor = mDb.query(true, ITEMS_TABLE, new String[] {ITEM_ID,
                ITEM_NAME, ITEM_QUANTITY, ITEM_PRICE, ITEM_PURCHASED, ITEM_LIST_ID}, ITEM_ID + "=" + (int)id, null,
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
     * Get a receipt item by its ID
     * @param id, receipt item's id
     * @return an Item object
     */
    public static Item getReceiptItem(long id)
    {
    	//Id of each correspondent column 
    	int /*idxITEM_ID,*/ idxRECEIPT_ITEM_NAME, idxRECEIPT_ITEM_QUANTITY, idxRECEIPT_ITEM_PRICE, idxRECEIPT_ITEM_PURCHASED, idxRECEIPT_ITEM_LIST_ID;
    	//Array to store all items of this list
    	Item item = null;
    	//Cursor to old list
    	Cursor itemsCursor = mDb.query(true, RECEIPT_ITEMS_TABLE, 
    			new String[] {	RECEIPT_ITEM_ID,
    							RECEIPT_ITEM_NAME, 
    							RECEIPT_ITEM_QUANTITY, 
    							RECEIPT_ITEM_PRICE, 
    							RECEIPT_ITEM_PURCHASED, 
    							RECEIPT_ITEM_LIST_ID}, 
    							RECEIPT_ITEM_ID + "=" + (int)id, 
    							null, null, null, null, null);
    	//Go to first item, if there isen't a first so there is no items at all
    	if(itemsCursor.moveToFirst())
    	{
	    	//Get column index to each column
	    	idxRECEIPT_ITEM_NAME = itemsCursor.getColumnIndex(RECEIPT_ITEM_NAME);
	    	idxRECEIPT_ITEM_QUANTITY = itemsCursor.getColumnIndex(RECEIPT_ITEM_QUANTITY);
	    	idxRECEIPT_ITEM_PRICE = itemsCursor.getColumnIndex(RECEIPT_ITEM_PRICE);
	    	idxRECEIPT_ITEM_PURCHASED = itemsCursor.getColumnIndex(RECEIPT_ITEM_PURCHASED);
	    	idxRECEIPT_ITEM_LIST_ID = itemsCursor.getColumnIndex(RECEIPT_ITEM_LIST_ID);
    	
    		String name = itemsCursor.getString(idxRECEIPT_ITEM_NAME);
    		int quantity = itemsCursor.getInt(idxRECEIPT_ITEM_QUANTITY);
    		float price = itemsCursor.getFloat(idxRECEIPT_ITEM_PRICE);
    		int purchased = itemsCursor.getInt(idxRECEIPT_ITEM_PURCHASED);
    		int receipt_listId = itemsCursor.getInt(idxRECEIPT_ITEM_LIST_ID);
    		//Add fetched item in the array
	    	item =  new Item(id, receipt_listId, name, quantity, price, purchased);
    	}
    	return item;
    }

    /**
     * Update informations of a shop item
     * @param id item ID
     * @param name item Name
     * @param quant item quantity
     * @param price item price
     * @param purchased if the item was purchased (0-false, 1-true)
     * @return true success, false fail
     */
    public static boolean updateShopItem(	long id, 
    										String name, 
    										int quant, 
    										float price, 
    										int purchased)
    {
        ContentValues args = new ContentValues();
        args.put(ITEM_NAME, name);
        args.put(ITEM_QUANTITY, quant);
        args.put(ITEM_PRICE, price);
        args.put(ITEM_PURCHASED, purchased);

        return mDb.update(ITEMS_TABLE, args, ITEM_ID + "=" + (int)id, null) == 1;
    }
    
    /**
     * 
     * Update informations of a shop item
     * @param id item ID
     * @param name item Name
     * @param quant item quantity
     * @param price item price
     * @return true success, false fail
     */
    public static boolean updateShopItem(	long id, 
    										String name, 
    										int quant, 
    										float price) 
    {
        ContentValues args = new ContentValues();
        args.put(ITEM_NAME, name);
        args.put(ITEM_QUANTITY, quant);
        args.put(ITEM_PRICE, price);

        return mDb.update(ITEMS_TABLE, args, ITEM_ID + "=" + (int)id, null) == 1;
    }
    
    /**
     * Update informations of a shop item
     * @param item object Item
     * @return true if one row was updated, false otherwise
     */
    public static boolean updateShopItem(Item item)
    {
        ContentValues args = new ContentValues();
        args.put(ITEM_NAME, item.getName());
        args.put(ITEM_QUANTITY, item.getQuantity());
        args.put(ITEM_PRICE, item.getPrice());
        args.put(ITEM_PURCHASED, item.getPurchased());

        return mDb.update(ITEMS_TABLE, args, ITEM_ID + "=" + (int)item.getId(), null) == 1;
    }
    
    /**
     * Update informations of a receipt item
     * @param item object Item
     * @return true if one row was updated, false otherwise
     */
    public static boolean updateReceiptItem(Item item)
    {
        ContentValues args = new ContentValues();
        args.put(RECEIPT_ITEM_NAME, item.getName());
        args.put(RECEIPT_ITEM_QUANTITY, item.getQuantity());
        args.put(RECEIPT_ITEM_PRICE, item.getPrice());
        args.put(RECEIPT_ITEM_PURCHASED, item.getPurchased());

        return mDb.update(RECEIPT_ITEMS_TABLE, args, 
        				RECEIPT_ITEM_ID + " = " + (int)item.getId(), null) == 1;
    }
    
	/**
	 * Get the lowest price of a item by its name
	 * @return the lowest price or -1
	 */
	public static float getLowestPrice(String itemName) 
	{
		Cursor c;
		String query = "select min(" + RECEIPT_ITEM_PRICE + "), " + 
								RECEIPT_ITEM_NAME +
								" from " + RECEIPT_ITEMS_TABLE +
								" where " + RECEIPT_ITEM_NAME + 
								" like '" + itemName + "';";
		c = mDb.rawQuery(query, null);
		if (c.moveToFirst())
			return c.getFloat(0);
		else
			return -1;
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
     * Create a new shop list
     * @param listName name of new shop list
     * @return
     */
    public static long createShopList(String listName) {
		ContentValues initVals = new ContentValues();
		
		initVals.put(LIST_NAME, listName);
		initVals.put(LIST_RECEIPT_LIST_ID, 0);
		
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
    	long newListID = createShopList(newListName);
    	
    	//Go to first item
    	oldList.moveToFirst();
    	do{
    		createShopItem(oldList.getString(idxITEM_NAME), 
    				oldList.getInt(idxITEM_QUANTITY),
    				oldList.getFloat(idxITEM_PRICE),
    				oldList.getInt(idxITEM_PURCHASED), 
    				(int)newListID );
    	}while (oldList.moveToNext());
    	
    	return newListID;
    }
    
    /**
     * Create a receipt list from a shop list
     * @param shopListId of the shop list
     * @return receipt list id
     */
    public static long createReceiptList(long shopListId)
    {
    	int idxITEM_NAME, idxITEM_QUANTITY, idxITEM_PRICE, idxITEM_PURCHASED;
    	String shopListName = getShopListName(shopListId);
		ContentValues value = new ContentValues();
		
		//Create new receipt list with the same name as shop list
		value.put(RECEIPT_LIST_NAME, shopListName);
		long reciptListID =  mDb.insert(RECEIPT_LIST_TABLE, null, value);
		
		if(reciptListID != -1)//Receipt list was successfully created
		{
	    	//Cursor to shop list
	    	Cursor shopList = mDb.query(ITEMS_TABLE, 
	    			new String[] {	ITEM_ID, ITEM_NAME,
	                				ITEM_QUANTITY, 
	                				ITEM_PRICE, 
	                				ITEM_PURCHASED,
	                				ITEM_LIST_ID}, 
	                ITEM_LIST_ID + "=" + (int) shopListId, 
	                null, null, null, null);
	
	    	//Get column index to each column
	    	idxITEM_NAME = shopList.getColumnIndex(ITEM_NAME);
	    	idxITEM_QUANTITY = shopList.getColumnIndex(ITEM_QUANTITY);
	    	idxITEM_PRICE = shopList.getColumnIndex(ITEM_PRICE);
	    	idxITEM_PURCHASED = shopList.getColumnIndex(ITEM_PURCHASED);
	    	    	
	    	//Go to first shop item
	    	shopList.moveToFirst();
	    	do{//create receipt items
	    		createReceiptItem(shopList.getString(idxITEM_NAME), 
	    				shopList.getInt(idxITEM_QUANTITY),
	    				shopList.getFloat(idxITEM_PRICE),
	    				shopList.getInt(idxITEM_PURCHASED), 
	    				reciptListID );
	    	}while (shopList.moveToNext());
		}
    	
    	return reciptListID;
    }

    /**
     * Create receipt list from current shop list
     * @return receipt list id
     */
    public static long createReceiptList()
    {
    	return createReceiptList(currentShopListID);
    }
    
    /**
     * Delete a list
     * @param id of the list to be deleted
     * @return true success, false otherwise
     */
    public static boolean deleteList(long id) 
    {
    	boolean listRet = mDb.delete(LISTS_TABLE, LIST_ID + "=" + id, null) == 1;
    	mDb.delete(ITEMS_TABLE, ITEM_LIST_ID + "=" + (int)id, null);
    	
    	return listRet;
    }

    /**
     * Fetch all shop lists
     * @return A Cursor to all shop lists in the first position
     */
    public static Cursor fetchAllShopLists()
    {
    	Cursor cursor = mDb.query(LISTS_TABLE, 
				new String [] {LIST_ID, LIST_NAME, LIST_TIMESTAMP},
				null, null, null, null, null);
    	
    	if (cursor != null)
            cursor.moveToFirst();
		
    	return cursor;
    }

    /**
     * Fetch a specific list
     * @param id of the list to be fetched
     * @return A Cursor to the fetched list in the first (and only) position
     * @throws SQLException
     */
    public static Cursor fetchShopList(long id) throws SQLException 
    {
        Cursor cursor = mDb.query(true, LISTS_TABLE, new String[] {LIST_ID,
            				LIST_NAME, LIST_TIMESTAMP}, LIST_ID + "=" + (int)id, null,
            				null, null, null, null);
        
        if (cursor != null)
            cursor.moveToFirst();
        
        return cursor;
    }

    /**
     * Update the name of a shop list
     * @param id of the list
     * @param listName new name
     * @return true if one row was affected, false otherwise
     */
    public static boolean updateShopList(long id, String listName) {
        ContentValues args = new ContentValues();
        args.put(LIST_NAME, listName);

        return mDb.update(LISTS_TABLE, args, LIST_ID + "=" + (int)id, null) == 0;
    }
    
    /**
     * Get the name of a shop list
     * @param id of the list
     * @return The list name or null
     */
    public static String getShopListName(long id)
    {
    	Cursor cursor = mDb.query(true, LISTS_TABLE, new String[] {LIST_ID,
				LIST_NAME}, LIST_ID + "=" + (int)id, null,
				null, null, null, null);
    	    	
    	return cursor.moveToFirst() == true ? cursor.getString(cursor.getColumnIndex(LIST_NAME)) : null;
    }
    
    /**
     * Set shopList foreign key to Receipt list table
     * @param shopId, ShopList id
     * @param receiptId, ReceiptList id
     * @return true if one row was affected, false otherwise
     */
    public static boolean setShopListReceiptList(long shopId, long receiptId)
    {
        ContentValues args = new ContentValues();
        args.put(LIST_RECEIPT_LIST_ID, receiptId);

        return mDb.update(LISTS_TABLE, args, LIST_ID + "=" + (int)shopId, null) == 0;
    }
    
    /**
     * Get the id of receipt list associated to a shopList
     * @param shopListId, id of shop list
     * @return id of associated receipt,
     *          0 if there is no associated receipt list,
     *         -1 if it was not found
     */
    public static long getReceiptListFromShopList(long shopListId)
    {
    	Cursor cursor = mDb.query(true, LISTS_TABLE, 
    			new String[] {LIST_RECEIPT_LIST_ID},
				LIST_ID + "=" + (int)shopListId, null,
				null, null, null, null);
    	    	
    	return cursor.moveToFirst() == true ? 
    		cursor.getLong(cursor.getColumnIndex(LIST_RECEIPT_LIST_ID)) : -1;
    }
    
    /**
     * Get the id of receipt list associated to current shopList
     * @return id of associated receipt,
     *          0 if there is no associated receipt list,
     *         -1 if it was not found
     */
    public static long getReceiptListFromShopList()
    {
    	return getReceiptListFromShopList(currentShopListID);
    }
        
    /**
     * Get current shop list ID
     * @return current shop list ID
     */
	public static int getCurrentShopListID() {
		return currentShopListID;
	}

	/**
	 * Set current shop list ID
	 * @param listID new current shop list ID
	 */
	public static void setCurrentShopListID(int listID) {
		currentShopListID = listID;
	}
	
    /**
     * Get current receipt list ID
     * @return current receipt list ID
     */
    public static int getCurrentReceiptListID() {
		return currentReceiptListID;
	}

	/**
	 * Set current receipt list ID
	 * @param listID new current receipt list ID
	 */
	public static void setCurrentReceiptListID(int currentReceiptListID) {
		DbAdapter.currentReceiptListID = currentReceiptListID;
	}
    
	/*
     * ************************************************************************
     * ************************************************************************
	 * End of methods to deal with Shop lists
     * ************************************************************************
     * ************************************************************************
	 */
}
