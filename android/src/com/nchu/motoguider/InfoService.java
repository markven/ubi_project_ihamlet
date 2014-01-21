package com.nchu.motoguider;

import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.IBinder;
import android.util.Log;

public class InfoService extends Service implements LocationListener
{
	private LocationManager locationManager;
	private LocationListener locationListener;
	private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();

	static BTS mbts;
	double startPointLat[], startPointLng[];
	double endPointLat[], endPointLng[];
	double lastLat = 0.0, lastLon = 0.0;
	
	String htmlInstruction[], bestProvider;

	Intent intentToMap = new Intent("InfoService");
	Bundle bundle = new Bundle();
	JSONData jsonData = null;
	
	@Override
	public void onCreate()
	{
		super.onCreate();
	}
	
	@Override
	public void onStart(Intent intent,int startId)
	{
		mbts = new BTS();
		Log.d("InfoService","啟動 InfoService");		
		getJSONfromMap(intent);
	    if(btAdapter == null) 
	    {
	    	Log.e("BTSError", "Bluetooth not support");
	    }
	    else if (!btAdapter.isEnabled()) 
	    {
	         Intent enableIntent = new Intent(BluetoothAdapter.ACTION_REQUEST_ENABLE);
	    }
	     else Log.i("BTState","...Bluetooth ON...");
	      
	     if(mbts != null)
	     {
	    	 if(mbts.BTSConnect() == "Finish")
	    		 ;
	     }
	     else Log.e("BTState","mbts is NULL");
		//intentToMap.putExtras(bundle);
		//sendBroadcast(intentToMap);
		UpdateLocation();
		Log.d("InfoService","完成定位:"+lastLat+"/"+lastLon);
		mbts.BTSend(0,1);

	}
	
	/* 從Map取得JSON Data */
	public void getJSONfromMap(Intent intent)
	{
		bundle = intent.getExtras();
		jsonData = (JSONData) bundle.getSerializable("allData");
		startPointLat = jsonData.getAllRoadStartPointLat();
		startPointLng = jsonData.getAllRoadStartPointLng();
		endPointLat = jsonData.getAllRoadEndPointLat();
		endPointLng = jsonData.getAllRoadEndPointLng();
		htmlInstruction = jsonData.getAllRoadInstruction();
	}
	
	/* 啟動Location listener */
	public void UpdateLocation()
	{
		locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE); 
		Criteria criteria = new Criteria();
	    bestProvider = locationManager.getBestProvider(criteria, false);
		
	    locationListener = new LocationListener()
		{
			int numOfInstruction = htmlInstruction.length; // 導航路段總數
			int numOfNowInstruction = 0; // 使用者從第一個html_instruction開始
			
			public void onLocationChanged(Location newLocation)
			{
				if(numOfNowInstruction > numOfInstruction)
				{
					Log.d("InfoService", "導航結束");
					mbts.BTSend(0, 0);
				}
				else
				{
					double remainTime = -1.0;
					double distance = -1;
					double startDistance = -1;
					double roadDistance = -1;
					float speed = newLocation.getSpeed();
					Double nowLng = newLocation.getLongitude();
					Double nowLat = newLocation.getLatitude();
					Log.d("InfoService", "使用者目前經/緯度 = " + nowLng + ", /" + nowLat);

					// 求目前位置與目前路段終點的距離
					distance = 
							getDistance(nowLat, nowLng,
									endPointLat[numOfNowInstruction],endPointLng[numOfNowInstruction]);
					
					roadDistance =
							getDistance(startPointLat[numOfNowInstruction], startPointLng[numOfNowInstruction],
									endPointLat[numOfNowInstruction], endPointLng[numOfNowInstruction]);
					
					startDistance = getDistance(nowLat, nowLng, startPointLat[numOfNowInstruction], startPointLng[numOfNowInstruction]);
					
					remainTime = distance/speed;
					Log.d("InfoService","距離下個路口: "+ distance + " 時間: "+remainTime);
					
					// 判斷送給 arduino 的信號
					
					if(distance > roadDistance & numOfNowInstruction == 0)
					{
						mbts.BTSend(7, getTurn(htmlInstruction[numOfNowInstruction]));
						// 剩餘距離不亮, 給方向
					}
					else
					{
						if(distance < 34 & distance > 10)
						{
							if(numOfNowInstruction < numOfInstruction)
							{
								mbts.BTSend((int)((distance-10)/4)-1, getTurn(htmlInstruction[numOfNowInstruction+1]));
							}
							else if(numOfNowInstruction == numOfInstruction)
							{
								mbts.BTSend((int)((distance-10)/4)-1, -1); // 倒數距離, 執行
							}
						}	
						else if(distance < 10)
						{
							if((numOfNowInstruction+1)>numOfInstruction)
							{
								;
							}
							else
							{
								numOfNowInstruction++;
								mbts.BTSend(0, getTurn(htmlInstruction[numOfNowInstruction]));
							}
						}
						else if(distance > 34)
						{
							mbts.BTSend(7, -1);
						}
					}
					if(numOfNowInstruction > numOfInstruction)
					{
						// 抵達目的地,送出全亮信號
						Log.d("InfoService", "導航結束");
						mbts.BTSend(0, 0);
						stopSelf();
					}
					
					// 通知Map改變ListView, 送出所需更動的資料
					bundle.putDouble("nowLng", nowLng);
					bundle.putDouble("nowLat", nowLat);
					bundle.putDouble("nowSpeed", speed);
					bundle.putDouble("nowDistance", distance);
					bundle.putDouble("nowRemainTime", remainTime);
					bundle.putInt("nowIndex", numOfNowInstruction);
					intentToMap.putExtras(bundle);
					sendBroadcast(intentToMap);
					Log.d("InfoService", "InfoService 已送出現在經緯度");
				}
			}
			/* 取得兩點經緯度的距離 */
			public double getDistance(double lat1, double lon1, double lat2, double lon2)
			{
				float[] results = new float[1];
				Location.distanceBetween(lat1, lon1, lat2, lon2, results);
				return results[0];
			}
			
		@Override
		public void onProviderDisabled(String provider) 
		{
			// TODO Auto-generated method stub
		}
		
		@Override
		public void onProviderEnabled(String provider) 
		{
			// TODO Auto-generated method stub
		
		}
		@Override
		public void onStatusChanged(String provider, int status, Bundle extras) 
		{
			// TODO Auto-generated method stub
		} 
	};
	locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 1000 , 0, locationListener);
	}
	
	@Override
	public void onLocationChanged(Location location)
	{
		// TODO Auto-generated method stub
	}
	@Override
	public void onProviderDisabled(String provider)
	{
		// TODO Auto-generated method stub
	}
	@Override
	public void onProviderEnabled(String provider)
	{
		// TODO Auto-generated method stub
	}
	@Override
	public void onStatusChanged(String provider, int status, Bundle extras)
	{
		// TODO Auto-generated method stub
	}
	@Override
	public IBinder onBind(Intent arg0)
	{
		return null;
	}
/* parse html's instruction direction*/
	
	public int getTurn(String s)
	 {
		 int turnResult = -1;
		 
		 if(s.contains("north"))
			 turnResult=0;
		 else if(s.contains("northeast"))
			 turnResult=1;
		 else if(s.contains("east"))
			 turnResult=2;
		 else if(s.contains("southeast"))
			 turnResult=3;
		 else if(s.contains("south"))
			 turnResult=4;
		 else if(s.contains("southwest"))
			 turnResult=5;
		 else if(s.contains("west"))
			 turnResult=6;
		 else if(s.contains("northwest"))
			 turnResult=7;
		 if(s.contains("Turn")&s.contains("left"))
			 turnResult=8;
		 if(s.contains("Turn")&s.contains("right"))
			 turnResult=9;
		 if(s.contains("Slight")&s.contains("left"))
			 turnResult=10;
		 if(s.contains("Slight")&s.contains("right"))
			 turnResult=11;
		 if(s.contains("Sharp")&s.contains("left"))
			 turnResult=12;
		 if(s.contains("Sharp")&s.contains("right"))
			 turnResult=13;
		 if(s.contains("U-turn"))
			 turnResult=14;
		 return turnResult; 
	}
	
	
}