package br.eti.andersonq;

import br.eti.andersonq.shoplist.R;
import br.eti.andersonq.shoplist.R.layout;
import br.eti.andersonq.shoplist.R.menu;
import android.os.Bundle;
import android.app.Activity;
import android.view.Menu;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ListsCopy extends Activity {
	//Tag to debug
    private static final String TAG = "ListsCopy";
    
	private Long mOldListID;
	//Text field with new list name
	private EditText mNewListName;
	//Access database
	private DbAdapter mDbHelper;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.lists_copy);
        setTitle(R.string.list_copy);

        //Create object to deal with DB
        mDbHelper = new DbAdapter(this);
        mDbHelper.open();
        
        mNewListName = (EditText) findViewById(R.id.list_copy_new_name);
        
        //Get id of the list
        mOldListID = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(DbAdapter.LIST_ID);
        if (mOldListID == null) {
            Bundle extras = getIntent().getExtras();
            mOldListID = extras != null ? extras.getLong(DbAdapter.LIST_ID)
                                    : null;
        }
        
        createButtons();
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.list_copy, menu);
		return true;
	}

	/*
	 * Associate a OnClickListener to each button
	 */
	private void createButtons()
	{
        Button confirmButton = (Button) findViewById(R.id.lists_copy_confirm);
        Button cancelButton = (Button) findViewById(R.id.lists_copy_cancel);
        
        //Confirm button
        confirmButton.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View view) 
            {
            	mDbHelper.copyList(mOldListID, mNewListName.getText().toString());
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
