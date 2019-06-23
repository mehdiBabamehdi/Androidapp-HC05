package com.example.mehdi.hc05_bt;

import java.util.List;

import android.bluetooth.BluetoothDevice;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.TextView;

public class DeviceListAdapter extends BaseAdapter {
	private LayoutInflater 					mInflater;
	private List<BluetoothDevice> 			mData;
	private OnConnectButtonClickListener 	mListener;
	    
	public DeviceListAdapter(Context context) { 
        mInflater = LayoutInflater.from(context);        
    }
	
	public void setData(List<BluetoothDevice> data) {
		mData = data;
	}
	
	public void setListener(OnConnectButtonClickListener listener) {
		mListener = listener;
	}
	
	public int getCount() {
		return (mData == null) ? 0 : mData.size();
	}

	public Object getItem(int position) {
		return null;
	}

	public long getItemId(int position) {
		return position;
	}

	public View getView(final int position, View convertView, ViewGroup parent) {
		ViewHolder holder;
		
		if (convertView == null) {
			/* Inflate the layout */
			convertView			=  mInflater.inflate(R.layout.list_item_device, null);
			
			holder 				= new ViewHolder();

			/* Set up the ViewHolder */
			holder.nameTv		= (TextView) convertView.findViewById(R.id.tv_name);
			holder.addressTv 	= (TextView) convertView.findViewById(R.id.tv_address);
			holder.pair_status	= (TextView) convertView.findViewById(R.id.pair_status);
			holder.ConnectBtn	= (Button) 	 convertView.findViewById(R.id.btn_connect);

			/* Store the holder with the view. */
			convertView.setTag(holder);
		} else {

			/* We've just avoided calling findViewById() on resource every time just use the viewHolder */
			holder = (ViewHolder) convertView.getTag();
		}

		/* Object item based on the position */
		BluetoothDevice device	= mData.get(position);

		/* Get the TextView from the ViewHolder and then set the text (item name) and tag (item ID) value */
		holder.nameTv.setText(device.getName());
		holder.addressTv.setText(device.getAddress());
		holder.pair_status.setText((device.getBondState() == BluetoothDevice.BOND_BONDED) ? "Paired" : "UnPaired");
		holder.ConnectBtn.setText("Connect");
		
		holder.ConnectBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (mListener != null) {
					mListener.onConnectButtonClick(position);
				}
			}
		});
		
        return convertView;
	}

	static class ViewHolder {
		TextView nameTv;
		TextView addressTv;
		TextView pair_status;
		TextView ConnectBtn;
	}
	
	public interface OnConnectButtonClickListener {
		public abstract void onConnectButtonClick(int position);
	}
}