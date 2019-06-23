package com.example.mehdi.hc05_bt;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Set;
import java.util.UUID;

import android.app.Activity;
import android.app.ProgressDialog;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

public class MainActivity extends Activity implements View.OnClickListener {

	private static final int Request_Code_Connected = 100;
	public static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	public boolean CONNECTION_STATUS = false;
	public static boolean SEND_SUCCESS = true;

	Button 		Room_Temp, Light_1_on, Light_1_off, Light_2_on,Light_2_off;
	EditText 	RoomTempText;
	ListView 	listView;
	private Button 		mScanBtn;
	private BTSendRec 	btsendrec;

	private ProgressDialog 	mProgressDlg;

	public BluetoothAdapter mBluetoothAdapter;
	private BluetoothSocket mBSocket;
	private ArrayList<BluetoothDevice> mDeviceList = new ArrayList<BluetoothDevice>();

	/* Define a Handler to transfer data from the thread in which send and
	 * receive is done. and then perform some action on received data, in this case the room temperature.
	 */
	Handler mHandler = new Handler() {
		@Override
		public void handleMessage(Message msg) {

			switch (msg.what) {

			case BTSendRec.READ_SUCCESS:
				
				byte[] writeBuf = (byte[]) msg.obj;
				int begin = (int) msg.arg1;
				int end = (int) msg.arg2;

				String RoomTemp = new String(writeBuf);
				RoomTemp = RoomTemp.substring(begin, end);

				RoomTempText.setText(RoomTemp + " C");
				break;
			}
		}
	};

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);

		/* Assign Buttons & Edit View */
		RoomTempText = (EditText) findViewById(R.id.RoomTempEditView);
		Room_Temp 	 = (Button) findViewById(R.id.TempButton);
		Light_1_on 	 = (Button) findViewById(R.id.Light1ON);
		Light_1_off  = (Button) findViewById(R.id.Light1OFF);
		Light_2_on 	 = (Button) findViewById(R.id.Light2ON);
		Light_2_off  = (Button) findViewById(R.id.Light2OFF);
		mScanBtn 	 = (Button) findViewById(R.id.btn_scan);

		/* Set button listener */
		Room_Temp.setOnClickListener(this);
		Light_1_on.setOnClickListener(this);
		Light_1_off.setOnClickListener(this);
		Light_2_on.setOnClickListener(this);
		Light_2_off.setOnClickListener(this);

		/* Get a BluetoothAdapter representing the local Bluetooth adapter */
		mBluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

		mProgressDlg = new ProgressDialog(this);
		mProgressDlg.setMessage("Scanning...");
		mProgressDlg.setCancelable(false);

		mProgressDlg.setButton(DialogInterface.BUTTON_NEGATIVE, "Cancel", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				dialog.dismiss();

				mBluetoothAdapter.cancelDiscovery();
			}
		});

		if (mBluetoothAdapter == null) {
			Toast.makeText(getApplicationContext(), "No Bluetooth Detected!", Toast.LENGTH_LONG).show();
			finish();
		} else {
			if (!mBluetoothAdapter.isEnabled()) {
				turnOnBT();
			}
		}

		mScanBtn.setOnClickListener(new View.OnClickListener() {

			@Override
			public void onClick(View arg0) {
				mBluetoothAdapter.startDiscovery();
			}
		});

		/* Defining intentfilter, adding needed actions to it,
		   and then register dynamically a broadcastreciever
		*/
		IntentFilter filter = new IntentFilter();

		filter.addAction(BluetoothAdapter.ACTION_STATE_CHANGED);
		filter.addAction(BluetoothDevice.ACTION_FOUND);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED);
		filter.addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED);

		registerReceiver(mReceiver, filter);
	}

	/**
	 * @name turnOnBT
	 * @brief The routine turns on Bluetooth adapter of the device.
	 */
	private void turnOnBT() {
		Intent intent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
		startActivityForResult(intent, 1);
	}

	/**
	 * @name TurnOffBT
	 * @brief The routine turns off Bluetooth adapter of the device.
	 */
	public void TurnOffBT() {
		mBluetoothAdapter.disable();
	}

	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		if (resultCode == RESULT_CANCELED) {
			Toast.makeText(getApplicationContext(), "Bluetooth must be enabled to continue", Toast.LENGTH_SHORT).show();
			finish();
		}
		if (requestCode == Request_Code_Connected && resultCode == RESULT_OK) {
			Room_Temp.setEnabled(true);
			Light_1_on.setEnabled(true);
			Light_1_off.setEnabled(true);
			Light_2_on.setEnabled(true);
			Light_2_off.setEnabled(true);

			mBSocket = SocketHandler.getSocket();

			btsendrec = new BTSendRec(mBSocket, mHandler);
			btsendrec.start();
		}
	}

	/**
	 * @name onClick
	 * @brief Get the pin number from the button that was clicked and then send it via
	 * 		  Bluetooth to MC
	 */
	@Override
	public void onClick(View view) {

		switch (view.getId()) {
		case R.id.TempButton:
			btsendrec.Send("RT"); 		// Send a data to uC via Bluetooth
			break;
		case R.id.Light1ON:
			btsendrec.Send("L1ON"); 	// Send a data to uC via Bluetooth
			break;
		case R.id.Light1OFF:
			btsendrec.Send("L1OFF"); 	// Send a data to uC via Bluetooth
			break;
		case R.id.Light2ON:
			btsendrec.Send("L2ON"); 	// Send a data to uC via Bluetooth
			break;
		case R.id.Light2OFF:
			btsendrec.Send("L2OFF"); 	// Send a data to uC via Bluetooth
			break;
		}
	}

	/**
	 * @name onPause
	 * @brief The routine performs actions defined by user when app is on Pause.
	 */
	@Override
	public void onPause() {
		if (mBluetoothAdapter != null) {
			if (mBluetoothAdapter.isDiscovering()) {
				mBluetoothAdapter.cancelDiscovery();
			}
		}
		super.onPause();
	}

	/**
	 * @name onDestroy
	 * @brief The routine performs actions defined by user before destoying the app.
	 */
	@Override
	public void onDestroy() {
		unregisterReceiver(mReceiver);
		if (mBSocket != null) // If the btSocket is not busy
		{
			try {
				mBSocket.close(); // Close the connection
			} catch (IOException e) {
			}
		}
		super.onDestroy();
	}

	/**
	 * The BroadcastReciever notifies the android system of actions added above in the filter,
	 * and according to the event, android performs appropriate reactions
	 */

	private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			/* Intent used to broadcast the change in connection state of the local Bluetooth adapter to a profile of the remote device. */
			if (BluetoothAdapter.ACTION_STATE_CHANGED.equals(action)) {
				final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);

				if (state == BluetoothAdapter.STATE_ON) {
					Toast.makeText(getApplicationContext(), "Blutooth is Enable.", Toast.LENGTH_SHORT).show();
				}
				/* The local Bluetooth adapter has started the remote device discovery process.
				* At first the paired devices are added to the list of bluetooth array list.
				* then when discovery process finishes, the app navigates to DeviceListActivity class.
				*/
			} else if (BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals(action))
			{
				mDeviceList = new ArrayList<BluetoothDevice>();
				Set<BluetoothDevice> pairedDevices = mBluetoothAdapter.getBondedDevices();
				mDeviceList.addAll(pairedDevices);

				mProgressDlg.show();
			} else if (BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals(action)) {
				mProgressDlg.dismiss();

				Intent newIntent = new Intent(MainActivity.this, DeviceListActivity.class);
				newIntent.putParcelableArrayListExtra("device.list", mDeviceList);
				startActivityForResult(newIntent, Request_Code_Connected);

			} /* If another device which is not already paired found, it will be added to bluetooth device list.*/
			else if (BluetoothDevice.ACTION_FOUND.equals(action)) {
				BluetoothDevice device = (BluetoothDevice) intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE);

				// If the device is not currently-paired, add it into the list
				// of device
				if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
					mDeviceList.add(device);
				}
				Toast.makeText(getApplicationContext(), "Found device " + device.getName(), Toast.LENGTH_SHORT).show();
			}
		}
	};
}
