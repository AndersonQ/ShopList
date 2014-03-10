package br.eti.andersonq;

import br.eti.andersonq.shoplist.R;
import android.app.Activity;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

public class ItemEdit extends Activity {
	//Tag to debug
    private static final String TAG = "ItemEdit";
    
    private EditText mNameText;
    private EditText mQuantText;
    private EditText mPurchasesText;
    private Long mId;
    private DbAdapter mDbHelper;
    
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.item_edit);
        setTitle(R.string.edit_item);

        mDbHelper = new DbAdapter(this);
        mDbHelper.open();
        
        mNameText = (EditText) findViewById(R.id.item_name);
        mQuantText = (EditText) findViewById(R.id.item_quant);
        mPurchasesText = (EditText) findViewById(R.id.item_purchased);

        Button confirmButton = (Button) findViewById(R.id.confirm);

        mId = (savedInstanceState == null) ? null :
            (Long) savedInstanceState.getSerializable(DbAdapter.ITEM_ID);
        if (mId == null) {
            Bundle extras = getIntent().getExtras();
            mId = extras != null ? extras.getLong(DbAdapter.ITEM_ID)
                                    : null;
        }
       
        populateFields();

        confirmButton.setOnClickListener(new View.OnClickListener() {

            public void onClick(View view) {
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
        outState.putSerializable(DbAdapter.ITEM_ID, mId);
    }

	private void populateFields() {
		if(mId != null)
		{
			Cursor note = mDbHelper.fetchItem(mId);
			startManagingCursor(note);
			mNameText.setText(note.getString(
					note.getColumnIndexOrThrow(DbAdapter.ITEM_NAME)));
			mQuantText.setText(
					note.getString(note.getColumnIndexOrThrow(DbAdapter.ITEM_QUANTITY)));
			mPurchasesText.setText(
					note.getString(note.getColumnIndexOrThrow(DbAdapter.ITEM_PURCHASED)));
		}
	}
	
	private void saveState() {
		String name = mNameText.getText().toString();
		String quant = mQuantText.getText().toString();
		String purchases = mPurchasesText.getText().toString();
		
		if (mId == null)
		{
			long id = mDbHelper.createItem(name, quant, purchases);
			if(id > 0)
				mId = id;
			
		}
		else
		{
			mDbHelper.updateItem(mId, name, quant, purchases);
		}
	}
}
