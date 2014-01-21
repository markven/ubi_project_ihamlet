package com.nchu.motoguider;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.util.UUID;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothSocket;
import android.os.Build;
import android.os.Handler;
import android.util.Log;

public class BTS
{
	private static final String TAG="inBTS";
	
	final int RECIEVE_MESSAGE = 1;
	//private static BluetoothAdapter btA;
	private static String address = "00:12:09:29:47:40";
	private static final UUID MY_UUID = UUID.fromString("00001101-0000-1000-8000-00805F9B34FB");
	private BluetoothSocket btSocket = null;   
	private ConnectedThread mConnectedThread;
	private StringBuilder sb = new StringBuilder();
	private BluetoothAdapter btAdapter = null;
	  
	Handler h;
	
	public BTS()
	{
		btAdapter = BluetoothAdapter.getDefaultAdapter();
		
		h = new Handler() 
		{
	        public void handleMessage(android.os.Message msg) 
	        {
				switch (msg.what)
				{
	            case RECIEVE_MESSAGE:   // if receive massage            	
	            	byte[] readBuf = (byte[]) msg.obj;
	                String strIncom = new String(readBuf, 0, msg.arg1);                 // create string from bytes array
	                sb.append(strIncom);                                                // append string
	                int endOfLineIndex = sb.indexOf("\r\n");                            // determine the end-of-line
	                int GetFromString = sb.indexOf("Get"); 
	                if (endOfLineIndex > 0) {                                            // if end-of-line,
	                    String sbprint = sb.substring(0, endOfLineIndex);               // extract string
	                    sb.delete(0, sb.length());                                      // and clear
	                    Log.i("Rec","Data from Arduino: " + sbprint);            // update TextView
	                    if(GetFromString>0) Log.i("BTState","Send & Receive Success");
	                    else{
		                    if(sbprint.length()>0)	Log.i("BTState","Arduino(sbprint): "+ sbprint.length() + sbprint);
		                    else Log.i("BTState","Arduino(sbprint)-length = 0 ");
		                    if(strIncom.length()>0)	Log.i("BTState","Arduino(strIncom): "+ strIncom.length() + strIncom);
		                    else Log.i("BTState","Arduino(strIncom)-length = 0 ");
	                    }
	                }
	                //Log.d("BTState", "...String:"+ sb.toString() +  "Byte:" + msg.arg1 + "...");
	                break;
	            }
	        };
	    };
	}
	
	public String BTCheck()
	{
		if (!btAdapter.isEnabled()) 
		{
	        return "Open";
	    } else {
	    	Log.i("BTState","...Bluetooth ON...");
	    	BTSConnect();
	    }
		return null;
	}
	
	public String BTSConnect()
	{
		Log.i("BTState","BTSConnect - try connect..");  
	        
	    BluetoothDevice device = btAdapter.getRemoteDevice(address);
	    
	    try 
	    {
	        btSocket = createBluetoothSocket(device);
	        Log.i("BTState","...Create Socket...");
	    } catch (IOException e) 
	    {
	        Log.e("BTState", "!!! socket create failed: " + e.getMessage() + ".");
	        return "Finish";
	    }
	    
	    btAdapter.cancelDiscovery();
	    
	    Log.i("BTState","...Connecting...");
		try 
		{
		    btSocket.connect();
		    Log.i("BTState","....Connection ok...");
		} catch (IOException e) 
		{
		    Log.e("BTState","!!! Connection failed...");
		    try 
		    {
		    	btSocket.close();
		    } catch (IOException e2) 
		    {
		    	Log.e("BTState", "!!! unable to close socket during connection failure" + e2.getMessage() + ".");
		    	return "Finish";
		    }
		}
	    
	    mConnectedThread = new ConnectedThread(btSocket);
	    mConnectedThread.start();
	    return "null";
	}
	
	public String BTStop()
	{
		try
		{
			btSocket.close();
		} catch (IOException e2)
		{
		    	Log.e("BTState", "In onPause() and failed to close socket." + e2.getMessage() + ".");
		    	return "Finish";
		}
		return "null";		
	}
	public String BTSend(Integer Time, Integer Direction)
	{
		if(!btSocket.isConnected())
		{
			Log.e("BTState","btSocket have been close! or without open!");
			BTSConnect();
		}
		String WriteStr="";
		WriteStr+=String.valueOf((char)(Direction+66));
		WriteStr+=String.valueOf((int)(Time));
		mConnectedThread.write(WriteStr);
		Log.i("BTState","Send:"+WriteStr);
		
		return "null";		
	}
	
	
	
	private BluetoothSocket createBluetoothSocket(BluetoothDevice device) throws IOException 
	{
	      if(Build.VERSION.SDK_INT >= 10)
	      {
	          try 
	          {
	              final Method  m = device.getClass().getMethod("createInsecureRfcommSocketToServiceRecord", new Class[] { UUID.class });
	              return (BluetoothSocket) m.invoke(device, MY_UUID);
	          } catch (Exception e) 
	          {
	              Log.e(TAG, "Could not create Insecure RFComm Connection",e);
	          }
	      }
	      return  device.createRfcommSocketToServiceRecord(MY_UUID);
	      
	 }
	 public class ConnectedThread extends Thread 
	 {
	        private final InputStream mmInStream;
	        private final OutputStream mmOutStream;        
	      
	        public ConnectedThread(BluetoothSocket socket) 
	        {
	            InputStream tmpIn = null;
	            OutputStream tmpOut = null;
	      
	            // Get the input and output streams, using temp objects because
	            // member streams are final
	            try 
	            {
	                tmpIn = socket.getInputStream();
	                tmpOut = socket.getOutputStream();
	            } catch (IOException e) { }
	      
	            mmInStream = tmpIn;
	            mmOutStream = tmpOut;
	        }
	      
	        public void run() 
	        {
	            byte[] buffer = new byte[256];  // buffer store for the stream
	            int bytes; // bytes returned from read()
	 
	            // Keep listening to the InputStream until an exception occurs
	            while (true)
	            {
	                try 
	                {
	                    // Read from the InputStream
	                    bytes = mmInStream.read(buffer);        // Get number of bytes and message in "buffer"
	                    h.obtainMessage(RECIEVE_MESSAGE, bytes, -1, buffer).sendToTarget();     // Send to message queue Handler
	                } catch (IOException e) 
	                {
	                    break;
	                }
	            }
	        }
	      
	        /* Call this from the main activity to send data to the remote device */
	        public void write(String message) 
	        {
	            Log.i("BTState","function Write: ...Data to send: " + message + "...");
	            byte[] msgBuffer = message.getBytes();
	            try 
	            {
	                mmOutStream.write(msgBuffer);
	            } catch (IOException e)
	            {
	                Log.e("BTState","...Error data send: " + e.getMessage() + "...");
	            }
	        }
	    }
}