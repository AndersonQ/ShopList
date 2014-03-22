package br.eti.andersonq;

import br.eti.andersonq.shoplist.R;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.database.Cursor;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public class ListsEditFrag extends DialogFragment {
	//Tag to debug
    private static final String TAG = "ListsEditFrag";

	final static String LIST_ID = "lists_id";
	
    private EditText mNameText;
    //private DbAdapter mDbHelper;
	private int mId;
		
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
        myInflatedViewl = inflater.inflate(R.layout.lists_edit_frag, null);
        //Set layout view
        builder.setView(myInflatedViewl);
        
        builder.setMessage(R.string.list_create)
               .setPositiveButton(R.string.confirm, new DialogInterface.OnClickListener() {
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
        //Get arguments
        Bundle args = getArguments();
        if (args != null) 
        	//Get Item id
        	mId = args.getInt(LIST_ID);
        else
        	//Set -1 so it know that a new item is being created
        	mId = -1;
        
        mNameText = (EditText) myInflatedViewl.findViewById(R.id.list_edit_title);
        
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
			Cursor note = DbAdapter.fetchList(mId);
			getActivity().startManagingCursor(note);
			
			//Set activity title
			getDialog().setTitle(R.string.list_edit);
			
			//Fill the field with list information
			mNameText.setText(
					note.getString(note.getColumnIndexOrThrow(DbAdapter.LIST_NAME)));
		}
	}
	
	private void saveState() 
	{
		String listName = mNameText.getText().toString();
		
		if (mId == -1)
		{
			long id = DbAdapter.createList(listName);
			if(id > 0)
				mId = (int) id;
		}
		else
		{
			DbAdapter.updateList(mId, listName);
		}
	}

}
