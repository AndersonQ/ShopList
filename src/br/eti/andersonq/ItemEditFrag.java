package br.eti.andersonq;

import br.eti.andersonq.shoplist.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;

public class ItemEditFrag extends DialogFragment {
	//Tag to debug
    private static final String TAG = "ItemEditFrag";

	final static String ITEM_ID = "item_id";
	
    private AutoCompleteTextView mNameText;
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
        /*Log.d(TAG, "onCreateDialog");
        Log.d(TAG, "getCurrentItemID:" + mId);*/
        
        if(mId == -1)//Create Item
        {
	        //Inflate layout
	        myInflatedViewl = inflater.inflate(R.layout.items_create_frag, null);
	        //Set layout view
	        builder.setView(myInflatedViewl);
	        //Set title to Create Item or Edit Item
	        builder.setTitle(R.string.item_create);
        }
        else //Edit Item
        {
	        //Inflate layout
	        myInflatedViewl = inflater.inflate(R.layout.item_edit_frag, null);
	        //Set layout view
	        builder.setView(myInflatedViewl);
	        //Set title to Create Item or Edit Item
	        builder.setTitle(R.string.item_edit);
        }
        
        builder.setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) {
                   	saveState();
                   	/*Log.d(TAG, "ConfirmButton()");
                   	Log.d(TAG, "calling mUpdate.updateDisplayedData");*/
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
	}

	@Override
    public void onStart() 
    {
		String [] itemsNames;
		ArrayAdapter<String> autoCompletNames = null;
    	//Load auto-complete preferences
    	SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(this.getActivity());
    	boolean autoComplete = sharedPref.getBoolean(SettingsActivity.KEY_AUTO_COMPLETE, false);
		
    	if(autoComplete)
    	{
			//Get names from DB
	        itemsNames = DbAdapter.getNames();
	        if(itemsNames == null)
	        	autoComplete = false;
	        else
	        {
	        	autoCompletNames = 
	        		new ArrayAdapter<String>(this.getActivity(), 
	        				android.R.layout.simple_dropdown_item_1line, 
	        				itemsNames);
	        }
    	}
        
        if(mId == -1)//Create Item
        {
	        mNameText = (AutoCompleteTextView) myInflatedViewl.findViewById(R.id.item_name);
	        if(autoComplete)
	        	mNameText.setAdapter(autoCompletNames);
	        mQuantText = (EditText) myInflatedViewl.findViewById(R.id.item_quant);
        }
        else//Edit Item
        {
	        mNameText = (AutoCompleteTextView) myInflatedViewl.findViewById(R.id.item_name);
	        if(autoComplete)
	        	mNameText.setAdapter(autoCompletNames);
	        mQuantText = (EditText) myInflatedViewl.findViewById(R.id.item_quant);
	        mPriceText = (EditText) myInflatedViewl.findViewById(R.id.item_price);
        }
        
        populateFields();
        super.onStart();
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
			/*Log.d(TAG, "populateFields");
			Log.d(TAG, "mId: " + mId);*/
			Item item;
			//Get shop Item or receipt item depending if isShopping or not
			item = Omniscient.isShopping() 	?	DbAdapter.getReceiptItem(mId) :
												DbAdapter.getShopItem(mId);
			mNameText.setText(item.getName());
			mQuantText.setText(String.valueOf(item.getQuantity()));
			mPriceText.setText(String.valueOf(item.getPrice()));
		}
	}

	private void saveState() 
	{
		/*Log.d(TAG, "saveState");
		Log.d(TAG, "itemID: " + mId);*/
		//Get name
		String name = mNameText.getText().toString();
		//Validate field
		if(name.length() == 0)
		{
			alerttMsg();
			return;
		}
		//Get quantity
		String squant = mQuantText.getText().toString();
		//Validate field
		if(squant.length() == 0)
		{
			alerttMsg();
			return;
		}
		int quant = Integer.parseInt(squant);

		if (mId == -1)//Creating Item
		{
			if(Omniscient.isShopping())//Create item in both lists
			{
				DbAdapter.createReceiptItem(name, quant, 0, 0,
						DbAdapter.getCurrentReceiptListID());
				DbAdapter.createShopItem(name, quant, 0, 0, 
						DbAdapter.getCurrentShopListID());
			}
			else
			{
				DbAdapter.createShopItem(name, quant, 0, 0, 
						DbAdapter.getCurrentShopListID());
			}
			/*
				long id = Omniscient.isShopping() ? 
						DbAdapter.createReceiptItem(name, quant, 0, 0, 
								DbAdapter.getCurrentReceiptListID()) :
						DbAdapter.createShopItem(name, quant, 0, 0, 
								DbAdapter.getCurrentShopListID());
			if(id > 0)
				mId = (int) id;*/
		}
		else//Editing Item
		{
			if(Omniscient.isShopping())
			{
				//Get item
				Item item = DbAdapter.getReceiptItem(mId);
				//Set name
				item.setName(name);
				//Set quantity
				item.setQuantity(quant);
				String sprice = mPriceText.getText().toString();
				//Log.d(TAG, "itemPrice: " + Float.parseFloat(sprice));
				if(sprice.length() == 0)
				{
					alerttMsg();
					return;
				}
				//Set price
				item.setPrice(Float.parseFloat(sprice));
				//Save on DB
				boolean ret = DbAdapter.updateReceiptItem(item);
				if(!ret) //If there was a problem, log it
					Log.e(TAG, "saveState(): Error: Receipt item wasn't saved in DB");
			}
			else
			{
				//Get item
				Item item = DbAdapter.getShopItem(mId);
				//Set name
				item.setName(name);
				//Set quantity
				item.setQuantity(quant);
				//Set new price
				item.setPrice(Float.parseFloat(mPriceText.getText().toString()));
				//Save on DB
				boolean ret = DbAdapter.updateShopItem(item);
				if(!ret) //If there was a problem, log it
					Log.e(TAG, "saveState(): Error: Shop item wasn't saved in DB");
			}
		}
	}
	
	private void alerttMsg()
	{
		Activity act = getActivity();
		AlertDialog.Builder builder = new AlertDialog.Builder(act);
		builder.setTitle(R.string.error_title);
		builder.setMessage(R.string.error_empty_msg);
		builder.setNegativeButton(R.string.got_ti_msg, null);
		builder.create().show();
	}
}
