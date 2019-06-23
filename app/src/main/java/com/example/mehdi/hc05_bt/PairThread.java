package com.example.mehdi.hc05_bt;

import android.bluetooth.BluetoothDevice;
import java.lang.reflect.Method;

/**
 * @name	PairThread
 * @brief	The Class used to paired the selected device. It should be done before
 * 			starting making connection. So, since the process may take time to be
 * 			done, it is carried out by a new thread and a delay is imposed for these
 * 			purpose.
 *
 * @author  Mehdi
 */
public class PairThread implements Runnable {

    private BluetoothDevice myDevice;

    public void pairMethod(BluetoothDevice device) {
        myDevice = device;
    }

    @Override
    public void run() {

        try {
            Method method = myDevice.getClass().getMethod("createBond", (Class[]) null);
            method.invoke(myDevice, (Object[]) null);
            Thread.sleep(5000);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}