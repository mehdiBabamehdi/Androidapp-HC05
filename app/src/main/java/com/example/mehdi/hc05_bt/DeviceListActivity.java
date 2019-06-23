package com.example.mehdi.hc05_bt;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;

import android.app.ProgressDialog;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ListView;
import android.widget.Toast;

public class DeviceListActivity extends MainActivity {

	protected static final int CONNECT_SUCCESS = 1;

	private ListView 			mListView;
	private DeviceListAdapter 	mAdapter;
	private ArrayList<BluetoothDevice> mDeviceList;
	private BluetoothSocket 	mBSocket;
	private ProgressDialog 		progress;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.activity_paired_devices);

		mDeviceList = getIntent().getExtras().getParcelableArrayList("device.list");

		mListView = (ListView) findViewById(R.id.lv_paired);

		mAdapter = new DeviceListAdapter(this);

		mAdapter.setData(mDeviceList);
		mAdapter.setListener(new DeviceListAdapter.OnConnectButtonClickListener() {
			@Override
			public void onConnectButtonClick(int position) {
				BluetoothDevice mDevice = mDeviceList.get(position);

				/* If the device already connected, do nothing,
				   and if the device is currently-paired, make a connection only, if
				   not, first pair the device and then make a connection. */
				if (!CONNECTION_STATUS) {

					if (mDevice.getBondState() != BluetoothDevice.BOND_BONDED) {
						Toast.makeText(getApplicationContext(), "Pairing...", Toast.LENGTH_SHORT).show();
						try {
							Thread mPairThread = new Thread(new PairThread());
							mPairThread.start();
						} catch (Exception e) {
							e.printStackTrace();
						}

					}
					MakeConnection(mDevice);

				} else if (CONNECTION_STATUS) {
					Toast.makeText(getApplicationContext(), "Already Connected!", Toast.LENGTH_SHORT).show();
				}
			}
		});

		mListView.setAdapter(mAdapter);

		registerReceiver(mPairReceiver, new IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED));
	}

	@Override
	public void onDestroy() {
		unregisterReceiver(mPairReceiver);

		super.onDestroy();
	}

	/**
	 * @name 	unpairDevice
	 * @brief 	The method is used to unpaired the selected already-paired device
	 * @author	Mehdi
	 * */
	private void unpairDevice(BluetoothDevice mDevice) {
		try {
			Method method = mDevice.getClass().getMethod("removeBond", (Class[]) null);
			method.invoke(mDevice, (Object[]) null);

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @name	MakeConnection
	 * @brief	The method is used to make connection with the selected device by
	 * 			manipulating AsyncTask class
	 * @author	Mehdi
	 */
	private void MakeConnection(BluetoothDevice mDevice) {

		try {
			new ConnectAsyncTask().execute(mDevice);
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * @name 	Disconnect
	 * @brief	The method is used to disconnect the already-connected device
	 * @author	Mehdi
	 */
	private void Disconnect() {
		if (mBSocket != null) // If the Socket is busy
		{
			try {
				mBSocket.close(); // Close connection
			} catch (IOException e) {
				Toast.makeText(getApplicationContext(), "Error in Disconnecting!", Toast.LENGTH_SHORT).show();
			}
		}
	}

	private final BroadcastReceiver mPairReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			if (BluetoothDevice.ACTION_BOND_STATE_CHANGED.equals(action)) {
				final int state = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR);
				final int prevState = intent.getIntExtra(BluetoothDevice.EXTRA_PREVIOUS_BOND_STATE,
						BluetoothDevice.ERROR);

				if (state == BluetoothDevice.BOND_BONDED && prevState == BluetoothDevice.BOND_BONDING) {
					Toast.makeText(getApplicationContext(), "Paired!", Toast.LENGTH_SHORT).show();
				} else if (state == BluetoothDevice.BOND_NONE && prevState == BluetoothDevice.BOND_BONDED) {
					Toast.makeText(getApplicationContext(), "Unpaired!", Toast.LENGTH_SHORT).show();
				}

				mAdapter.notifyDataSetChanged();
			}
		}
	};


	/**
	 * @name 	ConnectAsyncTask
	 * @brief	This class creates new thread used for connecting Bluetooth devices and
	 * 			then connect to the selected device. This work should take place in
	 * 			separate thread. This is because forming a connection can block a thread
	 * 			for a significant amount of time.
	 *
	 * @author 	Mehdi
	 *
	 */

	private class ConnectAsyncTask extends AsyncTask<BluetoothDevice, Void, Void> {
		private BluetoothDevice mTDevice;
		Boolean ConnectionSuccess = true;

		@Override
		protected void onPreExecute() {
			progress = ProgressDialog.show(DeviceListActivity.this, "Connecting...", "Please Wait!!!");
		}

		@Override
		protected Void doInBackground(BluetoothDevice... params) {
			BluetoothDevice mTDevice = params[0];
			BluetoothSocket tmp = null;

			try {
				/* Open bluetooth socket for communication with selected device */
				tmp = mTDevice.createRfcommSocketToServiceRecord(MY_UUID);
			} catch (IOException e) {
			}

			mBSocket = tmp;
			mBluetoothAdapter.cancelDiscovery();

			try {
				/* Connect the device through the socket. This will block until it succeeds or throws an exception */
				mBSocket.connect();
			} catch (IOException connectException) {
				/* Unable to connect; close the socket and get out */
				try {
					mBSocket.close();
				} catch (IOException closeException) {
					ConnectionSuccess = false;
				}
			}
			return null;
		}

		protected void onPostExecute(Void result) {
			super.onPostExecute(result);
			progress.dismiss();

			if (!ConnectionSuccess) {
				Toast.makeText(getApplicationContext(), "Connection Failed! Is it a SPP Bluetooth? Try again.",
						Toast.LENGTH_LONG).show();
				CONNECTION_STATUS = false;
				finish();

			} else {
				CONNECTION_STATUS = true;
				Toast.makeText(getApplicationContext(), "Connection Was Made Successfully.", Toast.LENGTH_LONG).show();
				SocketHandler.setSocket(mBSocket);
				Intent myIntent = new Intent();
				setResult(RESULT_OK, null);
				finish();
			}

		}

	}

}
