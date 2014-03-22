package br.eti.andersonq;

import br.eti.andersonq.shoplist.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class ItemEditFrag extends DialogFragment {
	//Tag to debug
    private static final String TAG = "ItemEditFrag";

	final static String ITEM_ID = "item_id";
	
    private EditText mNameText;
    private EditText mQuantText;
    private EditText mPriceText;
    //private EditText mPurchasesText;
    
    //private DbAdapter mDbHelper;
	private long mId;
		
	private View myInflatedViewl;
	private Update mUpdate;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) 
	{
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        
        mId = Omniscient.getCurrentItemID();
        if(mId == -1)//Create Item
        {
	        //Inflate layout
	        myInflatedViewl = inflater.inflate(R.layout.items_create_frag, null);
	        //Set layout view
	        builder.setView(myInflatedViewl);
	        //Set title to Create Item or Edit Item
	        builder.setMessage(R.string.item_create);
        }
        else //Edit Item
        {
	        //Inflate layout
	        myInflatedViewl = inflater.inflate(R.layout.item_edit_frag, null);
	        //Set layout view
	        builder.setView(myInflatedViewl);
	        //Set title to Create Item or Edit Item
	        builder.setMessage(R.string.item_edit);
        }
        
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                   	saveState();
                   	mUpdate.updateDisplayedData();
                   }
               })
               .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                       // User cancelled the dialog
                   }
               });
        // Create the AlertDialog object and return it
        return builder.create();
    }
	
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
	    
		//Verify if parent activity implements Update
		try 
		{
	    	mUpdate = (Update) activity;
	    } catch (ClassCastException e) 
	    {
	        throw new ClassCastException(activity.toString() + " must implement Update");
	    }
		
		/*
		//Create DbAdapter to deal with database
        mDbHelper = new DbAdapter(activity);
        //Open database
        mDbHelper.open();*/
	}

	@Override
    public void onStart() 
    {
        super.onStart();
        /* it is not working, using POG/KOP (Kludge Oriented Programming)
        //Get arguments
        Bundle args = getArguments();
        if (args != null) 
        	//Get Item id
        	mId = args.getInt(ITEM_ID);
        else
        	//Set -1 so it know that a new item is being created
        	mId = -1;*/
        ////Get arguments - working
        
        if(mId == -1)//Create Item
        {
	        mNameText = (EditText) myInflatedViewl.findViewById(R.id.item_name);
	        mQuantText = (EditText) myInflatedViewl.findViewById(R.id.item_quant);
        }
        else//Edit Item
        {
	        mNameText = (EditText) myInflatedViewl.findViewById(R.id.item_name);
	        mQuantText = (EditText) myInflatedViewl.findViewById(R.id.item_quant);
	        mPriceText = (EditText) myInflatedViewl.findViewById(R.id.item_price);
        }
        
        populateFields();
    }

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(DbAdapter.ITEM_ID, mId);
	}
	
	private void populateFields() 
	{		
		//If it is creating a new item there is no information to populate fields
		if(mId != -1)
		{
			Cursor note = DbAdapter.fetchItem(mId);
			getActivity().startManagingCursor(note);
			
			//Set activity title
			getDialog().setTitle(R.string.item_edit);
						
			//Fill the fields with item information
			mNameText.setText(note.getString(
					note.getColumnIndexOrThrow(DbAdapter.ITEM_NAME)));
			mQuantText.setText(String.valueOf(
					note.getInt(note.getColumnIndexOrThrow(DbAdapter.ITEM_QUANTITY))));
			mPriceText.setText(String.valueOf(
					note.getFloat(note.getColumnIndexOrThrow(DbAdapter.ITEM_PRICE))));
		}
	}
    /**
     * Create an item
     * @param itemName item name
     * @param itemQuant item quantity
     * @param price item price
     * @param purchased purchased if the item was purchased (0-false, 1-true)
     * @param listId if of the item's list
     * @return new item ID
     */
	private void saveState() 
	{
		String name = mNameText.getText().toString();
		int quant = Integer.parseInt(mQuantText.getText().toString());
		
		if (mId == -1)//Creating Item
		{
			long id = DbAdapter.createShopItem(name, quant, 0, 0, DbAdapter.getCurrentShopListID());
			if(id > 0)
				mId = (int) id;
		}
		else//Editing Item
		{
			//Get item
			Item item = DbAdapter.getItem(mId);
			//Set new price
			item.setPrice(Float.parseFloat(mPriceText.getText().toString()));
			//Save on DB
			boolean ret = DbAdapter.updateShopItem(item);
			if(!ret) //If there was a problem, log it
				Log.e(TAG, "saveState(): Error: item wasn't saved in DB");
			item = DbAdapter.getItem(mId);
			//DbAdapter.updateItem(mId, name, quant, price);
		}
	}
}
