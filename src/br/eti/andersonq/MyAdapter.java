package br.eti.andersonq;

import java.util.ArrayList;

import br.eti.andersonq.shoplist.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Paint;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnCreateContextMenuListener;
import android.view.View.OnTouchListener;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;

@SuppressLint("NewApi")
public class MyAdapter extends ArrayAdapter<Item>
{
	//Tag to debug
    private static final String TAG = "MyAdapter";

	private ArrayList<Item> items;
	private Context mContext;
	
	LayoutInflater viewinf;
	
	public MyAdapter(Context context, int textViewResourceId, ArrayList<Item> items) 
	{
		super(context, textViewResourceId, items);
		this.items = items;
		mContext = context;
		viewinf = (LayoutInflater) mContext.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	@Override
	public View getView(final int position, View convertView, ViewGroup parent) 
	{
		TextView nameText;
		TextView quantText;
		TextView priceText;
		TextView priceCompText;
		CheckBox purchasedChk;
	    View view = convertView;
		
        if(Omniscient.isShopping())
        {
        	float oldPrice;
        	//Load preferences
        	SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
        	boolean compPrice = sharedPref.getBoolean(SettingsActivity.KEY_PRICE_COMPARISON, false);

    		if(view == null)
    		{
    			view = viewinf.inflate(R.layout.items_receipt_row, null);
    		}
            view.setLongClickable(true);
            view.setClickable(true);
            view.setFocusableInTouchMode(true);
            view.setHapticFeedbackEnabled(true);
        	
			//Get current item
			Item item = items.get(position);
	        nameText = (TextView) view.findViewById(R.id.item_name_row);
	        quantText = (TextView) view.findViewById(R.id.item_quant_row);
	        priceText = (TextView) view.findViewById(R.id.item_price_row);
	        purchasedChk = (CheckBox) view.findViewById(R.id.item_purchased_checkbox_row);
	        if(compPrice)
	        {
	        	priceCompText = (TextView) view.findViewById(R.id.item_price_comp_row);
	        	oldPrice = DbAdapter.getLowestPrice(item.getName());
	        	if(oldPrice >= 0)
	        	{
	        		priceCompText.setText(" / " + 
	        				String.format("£%.2f", oldPrice));
	        		if(Float.compare(item.getPrice(), oldPrice) > 0)
	        			priceCompText.setTextColor(Color.GREEN);
	        		else
	        			priceCompText.setTextColor(Color.RED);
	        	}
	        }
	        
	        //Fill fields
	        nameText.setText(item.getName());
	        quantText.setText(String.valueOf(item.getQuantity()));
	        priceText.setText(String.format("£%.2f",item.getPrice()));
	        purchasedChk.setChecked(item.isPurchased());
	        
	        purchasedChk.setOnClickListener(new View.OnClickListener()
	        {
				@Override
				public void onClick(View v)
		        {
					boolean isChecked = ((CheckBox) v).isChecked();
					//Get selected item
					Item item = items.get(position);

	       		   	if(item.isPurchased() != isChecked)
	       		   	{
	       		   		item.setPurchased(isChecked == true ? 1 : 0);
	       		   		boolean ret = DbAdapter.updateReceiptItem(item);
	       		   		if(!ret)//If there was a problem, log it
	       		   			Log.e(TAG, "CheckBox: receipt item wasn't updated on DB! Name " + item.getName() + " Id " +item.getId());
	       		   	}
					//Update total cost
					((ItemsMain) mContext).updateCost();
		        }
	        });
        }
        else
        {
    		if(view == null)
    		{
    			view = viewinf.inflate(R.layout.items_shop_row, null);
    		}
            view.setLongClickable(true);
            view.setClickable(true);
            view.setFocusableInTouchMode(true);
            view.setHapticFeedbackEnabled(true);
            
			//Get current item
			Item item = items.get(position);
	        nameText = (TextView) view.findViewById(R.id.item_name_row);
	        quantText = (TextView) view.findViewById(R.id.item_quant_row);
	        
	        //Fill fields
	        nameText.setText(item.getName());
	        quantText.setText(String.valueOf(item.getQuantity()));
        }

        view.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) 
            {
            	//Get clicked item
            	Item item = items.get(position);
            	//Get preference 'auto remove'
            	SharedPreferences sharedPref = PreferenceManager.getDefaultSharedPreferences(mContext);
            	//TODO auto remove
            	boolean autoRemove = sharedPref.getBoolean(SettingsActivity.KEY_AUTO_REMOVE, false);
            	boolean compPrice = sharedPref.getBoolean(SettingsActivity.KEY_PRICE_COMPARISON, false);
            	
            	CharSequence text = "Auto remove pref: " + autoRemove + 
            			"\ncompPrice = " + compPrice;
            	
            	if(autoRemove)
            	{
            		//TODO auto remove
            		//v.setVisibility(View.INVISIBLE);//DbAdapter.deleteReceiptItem(item.getId());
            		//text = text + " INVISIBLE";
            	}
            	
            	int duration = Toast.LENGTH_SHORT;
            	Toast toast = Toast.makeText(mContext, text, duration);
            	toast.show();
            	
            	//Update displayed data
            	((Update) mContext).updateDisplayedData();
            }
        });

		return view;
	}

	@Override
	public long getItemId(int position) 
	{		
		return items.get(position).getId();
	}
}
