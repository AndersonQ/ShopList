package br.eti.andersonq;

import br.eti.andersonq.shoplist.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class ListsCopyFrag extends DialogFragment {
	//Tag to debug
    private static final String TAG = "ListsCopyFrag";

	final static String OLD_LIST_ID = "old_list_id";
	
    private EditText mNewListName;
    //private DbAdapter mDbHelper;
	private int mOldListID;
		
	private View myInflatedViewl;
	private Update mUpdate;
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) 
	{
        // Use the Builder class for convenient dialog construction
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        // Get the layout inflater
        LayoutInflater inflater = getActivity().getLayoutInflater();
        
        //Get layout view
        myInflatedViewl = inflater.inflate(R.layout.lists_copy_frag, null);
        //Set layout view
        builder.setView(myInflatedViewl);
        
        builder.setMessage(R.string.list_copy)
               .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
                   public void onClick(DialogInterface dialog, int id) 
                   {
                	   /*Log.d(TAG, "PositiveButton: ");
                	   Log.d(TAG, "DbAdapter.copyList(" + mOldListID + ", " + mNewListName.getText().toString());*/
                	   DbAdapter.copyList(mOldListID, mNewListName.getText().toString());
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
        //Get arguments
        /*Bundle args = getArguments();
        if (args != null) 
        	//Get Item id
        	mOldListID = args.getInt(OLD_LIST_ID);*/
        mOldListID = (int) Omniscient.getCurrentListID();
        /*Log.d(TAG, "onStart");
        Log.d(TAG, "Omniscient.getCurrentListID(): " + Omniscient.getCurrentListID());*/
        
        mNewListName = (EditText) myInflatedViewl.findViewById(R.id.list_copy_new_name);        
    }

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putSerializable(DbAdapter.LIST_ID, mOldListID);
	}

}
