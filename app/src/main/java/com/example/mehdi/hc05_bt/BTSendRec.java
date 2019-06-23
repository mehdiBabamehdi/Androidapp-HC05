package com.example.mehdi.hc05_bt;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import android.bluetooth.BluetoothSocket;
import android.os.Handler;
import android.util.Log;

/**
 * @name 	BTSendRec
 * @brief	The class used to send and receive data from a device (uC) via Bluetooth. The
 * 			connection between device and MC was created before.
 * 	@author Mehdi
 */

public class BTSendRec extends Thread {
	
	private BluetoothSocket mmSocket;
	private InputStream mmInStream;
	private OutputStream mmOutStream;
	protected static final int READ_SUCCESS = 1;

	private Handler handler;

	/**
	 * @name   BTSendRec
	 * @brief  The construct gets bluetoothsocket and handler from mainactivity
	 */

	public BTSendRec(BluetoothSocket socket, Handler handler) {

		this.handler = handler;

		mmSocket = socket;
		InputStream tmpIn = null;
		OutputStream tmpOut = null;

		/* Get the input and output streams, using "tmp" objects because member streams are final */
		try {
			tmpIn = socket.getInputStream();
			tmpOut = socket.getOutputStream();
		} catch (IOException e) {

		}

		mmInStream = tmpIn;
		mmOutStream = tmpOut;
	}

	public void run() {

		byte[] buffer = new byte[1024]; // buffer store for the stream
		int begin = 0;
		int bytes = 0;

		/* Keep listening to the InputStream until an exception occurs. */
		while (true) {
			try {
				/* Read number of array from input buffer */
				bytes = mmInStream.read(buffer);
				for (int i = begin; i <= bytes; i++) {

					/* The data sent from uC ends with # sign */
					if (buffer[i] == "#".getBytes()[0] || i == bytes) {

						/* Send the obtained bytes to the UI activity */
						handler.obtainMessage(READ_SUCCESS, begin, i, buffer).sendToTarget();
						begin = i + 1;
						if (i == bytes - 1) {
							bytes = 0;
							begin = 0;
						}
					}
				}
			} catch (IOException e) {
				break;
			}
		}
	}

	/**
	 * @name	Send
	 * @brief 	Call this from the main activity to send string data to the remote device.
	 */
	public void Send(String income) {

		try {
			mmOutStream.write(income.getBytes());
			for (int i = 0; i < income.getBytes().length; i++)
				Log.v("outStream" + Integer.toString(i),
						Character.toString((char) (Integer.parseInt(Byte.toString(income.getBytes()[i])))));
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {

		}
	}

	/**
	 * @name 	Send
	 * @brief 	Call this from the main activity to send byte to the remote device.
	 */
	public void Send(byte[] bytes) {
		try {
			mmOutStream.write(bytes);
			try {
				Thread.sleep(20);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		} catch (IOException e) {
		}
	}

	/**
	* @name 	cancel
	* @brief	Call this from the main activity to shutdown the connection.
	*/
	public void cancel() {
		try {
			mmSocket.close();
		} catch (IOException e) {
		}
	}
}
