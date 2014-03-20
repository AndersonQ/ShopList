package br.eti.andersonq;

import java.util.ArrayList;

import br.eti.andersonq.shoplist.R;
import android.annotation.SuppressLint;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.graphics.Paint;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
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
	Context mContext;
	View mView;
	
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
	    Switch purchasedSwitch;
	    View view = mView = convertView;
	    
		if(view == null)
		{
			view = mView = viewinf.inflate(R.layout.items_row, null);
		}
        view.setLongClickable(true);
        view.setClickable(true);
		
		//Get current item
		Item item = items.get(position);
        nameText = (TextView) view.findViewById(R.id.item_name_row);
        quantText = (TextView) view.findViewById(R.id.item_quant_row);
        priceText = (TextView) view.findViewById(R.id.item_price_row);
        purchasedSwitch = (Switch) view.findViewById(R.id.item_purchased_switch_row);
        
        //Fill fields
        nameText.setText(item.getName());
        quantText.setText(String.valueOf(item.getQuantity()));
        priceText.setText(String.valueOf((item.getPrice())));
        purchasedSwitch.setChecked(item.getPurchasedBool());
        
        //Cross out item name if purchased
        /*if(purchasedSwitch.isChecked())
        	nameText.setPaintFlags(nameText.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
        else
        	nameText.setPaintFlags(nameText.getPaintFlags() | ~Paint.STRIKE_THRU_TEXT_FLAG);*/
        
        view.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) 
            {
            	Item item = items.get(position);
            	CharSequence text = "Item " + item.getName() + "\n" + "Price " + item.getPrice();
            	int duration = Toast.LENGTH_SHORT;
            	Toast toast = Toast.makeText(mContext, text, duration);
            	toast.show(); 
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
