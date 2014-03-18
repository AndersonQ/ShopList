package br.eti.andersonq;

import br.eti.andersonq.shoplist.R;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ListsEdit extends Activity {
	//Tag to debug
    private static final String TAG = "ListsEdit";
    
	private Long mListID;
    private EditText mListName;
    
    private DbAdapter mDbHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.lists_edit);
        setTitle(R.string.list_create);

        mDbHelper = new DbAdapter(this);
        mDbHelper.open();
       
        mListName = (EditText) findViewById(R.id.list_edit_title);
        
        mListID = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(DbAdapter.LIST_ID);
        if (mListID == null) {
            Bundle extras = getIntent().getExtras();
            mListID = extras != null ? extras.getLong(DbAdapter.LIST_ID)
                                    : null;
        }
        
        //Associate methods to activity buttons
        createButtons();
        //Populate field
        populateFields();
    }

    @Override
    protected void onPause() {
        super.onPause();
        //TODO Save state, but not on the database
        //saveState();
    }

	@Override
	protected void onResume() {
		super.onResume();
		populateFields();
	}

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        //TODO Save state, but not on the database
        //saveState();
        outState.putSerializable(DbAdapter.LIST_ID, mListID);
    }
    
	private void populateFields()
	{
		//If it is creating a new item there is no information to populate fields
		if(mListID != null)
		{
			Cursor note = mDbHelper.fetchList(mListID);
			startManagingCursor(note);
			
			//Set activity title
			setTitle(R.string.list_edit);
			
			//Fill the field with list information
			mListName.setText(
					note.getString(note.getColumnIndexOrThrow(DbAdapter.LIST_NAME)));
		}
	}
	
	private void saveState() {
		String listName = mListName.getText().toString();
		
		if (mListID == null)
		{
			long id = mDbHelper.createList(listName);
			if(id > 0)
				mListID = id;
		}
		else
		{
			mDbHelper.updateList(mListID, listName);
		}
	}
	
	/*
	 * Associate a OnClickListener to each button
	 */
	private void createButtons()
	{
        Button confirmButton = (Button) findViewById(R.id.confirm);
        Button cancelButton = (Button) findViewById(R.id.cancel);
        
        //Confirm button
        confirmButton.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View view) 
            {
            	saveState();
                setResult(RESULT_OK);
                finish();
            }
        });
        //Cancel button
        cancelButton.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View view) 
            {
                setResult(RESULT_OK);
                finish();
            }
        });
	}
}