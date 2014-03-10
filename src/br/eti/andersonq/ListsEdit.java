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
        setTitle(R.string.edit_list);

        mDbHelper = new DbAdapter(this);
        mDbHelper.open();
        
        mListName = (EditText) findViewById(R.id.list_edit_title);
        
        Button confirmButton = (Button) findViewById(R.id.confirm);

        mListID = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(DbAdapter.LIST_ID);
        if (mListID == null) {
            Bundle extras = getIntent().getExtras();
            mListID = extras != null ? extras.getLong(DbAdapter.LIST_ID)
                                    : null;
        }
       
        populateFields();

        confirmButton.setOnClickListener(new View.OnClickListener() 
        {
            public void onClick(View view) 
            {
                setResult(RESULT_OK);
                finish();
            }
        });
    }

    @Override
    protected void onPause() {
        super.onPause();
        saveState();
    }

	@Override
	protected void onResume() {
		super.onResume();
		populateFields();
	}

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        saveState();
        outState.putSerializable(DbAdapter.LIST_ID, mListID);
    }

	private void populateFields() {
		if(mListID != null)
		{
			Cursor note = mDbHelper.fetchList(mListID);
			startManagingCursor(note);
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
}