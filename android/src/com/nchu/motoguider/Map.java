package com.nchu.motoguider;

import java.io.IOException;

import android.os.Bundle;
import android.util.Log;
import android.widget.SimpleAdapter;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.os.AsyncTask;
import android.os.Handler;
import android.app.ListActivity;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.location.*;
import com.google.android.gms.maps.model.LatLng;

public class Map extends ListActivity
{
	ArrayList<HashMap<String,String>> list = new ArrayList<HashMap<String,String>>();
	private SimpleAdapter adapter;
	Handler handler;
	Intent intent;
	
	TextView showText;
	String[] instruction;
	LatLng startPoint[], endPoint[], dest;
	MyBroadcastReceiver receiver;
	
	JSONArray jRoutes = null, jLegs = null, jSteps = null;
	
	double startPointLat[], startPointLng[], endPointLat[], endPointLng[];
	double distance[], infoNowDistance;
	String htmlInstruction[], html_instructions;
	int infoNowIndex;
	
	private BluetoothAdapter btAdapter = BluetoothAdapter.getDefaultAdapter();
	static BTS mbts;

	@Override
	protected void onCreate(Bundle savedInstanceState) 
	{
		intent = new Intent(Map.this, InfoService.class);
		// ���I:�����j�Ǯժ��f 24.123488,120.675053
		// �n�����p7:24.122364,120.679621
		// ��߰ө�:24.121556,120.679208
		// �z�Ǥj�ӥ~�ਤ:24.121669,120.677923
		double dest_lat =  0; //�ਤ
		double dest_lng = 0;
		/* ���I:�����j�ǲz�Ǥj�Ӯ�
		double dest_lat = 24.12116; 
		double dest_lng = 120.677931;		  
		*/ 
		mbts = new BTS();
		handler = new Handler(){};
		Intent getIntent = this.getIntent();
		Bundle bundle = getIntent.getExtras();	
		dest_lat = bundle.getDouble("lat");
		dest_lng = bundle.getDouble("lng");
		Log.d("Map","��Geocoder���o�ت��a�g/�n�� = "+dest_lat+"/"+dest_lng);
		
		dest = new LatLng(dest_lat,dest_lng);
		LatLng origin = getOriginGPS();
		super.onCreate(savedInstanceState);
		   
		String url = getDirectionsUrl(origin, dest);
		DownloadTask downloadTask = new DownloadTask();
		downloadTask.execute(url);
		
		/* ���U�s�� */
		IntentFilter filter = new IntentFilter("InfoService");
		receiver = new MyBroadcastReceiver();
		registerReceiver(receiver, filter);
	 }
	
	@Override
	protected void onStart()
	{
		super.onStart();
	}
	
	@Override
	protected void onStop()
	{
		  super.onStop();
	}
	
	@Override
	protected void onDestroy()
	{
		unregisterReceiver(receiver);
		super.onDestroy();
	}
	
	private class MyBroadcastReceiver extends BroadcastReceiver
	{
		@Override
		public void onReceive(Context context, Intent intent) 
		{
			double infoNowLat, infoNowLng, infoNowSpeed, infoNowRemainTime;
			Bundle bundle = intent.getExtras();
			
			infoNowLat = bundle.getDouble("nowLat");
			infoNowLng = bundle.getDouble("nowLng");
			infoNowSpeed = bundle.getDouble("nowSpeed");
			infoNowDistance = bundle.getDouble("nowDistance");
			infoNowRemainTime = bundle.getDouble("nowRemainTime");
			infoNowIndex = bundle.getInt("nowIndex");
			
			if(infoNowIndex>htmlInstruction.length)
				Toast.makeText(context, "�w��F���I", Toast.LENGTH_SHORT);
			
			if(infoNowDistance > distance[infoNowIndex] + 10)
			{
				Toast.makeText(context, "���s���o���|...", Toast.LENGTH_SHORT).show();
				Log.d("Map","�W�L���q�Z��,���s���o��T...");
				stopService(intent);
				String url = getDirectionsUrl(new LatLng(infoNowLat,infoNowLng), dest);
				DownloadTask downloadTask = new DownloadTask();
				downloadTask.execute(url);
			}
			else
			{
				Log.d("Map", "����InfoService�e�Ӳ{�b���g�n�� :" + infoNowLat +"/"+ infoNowLng);
				Log.d("Map", "����{�b�t��:" + infoNowSpeed);
				Log.d("Map", "����{�b�P�Ӹ��q���I�Ѿl�Z��: " + infoNowDistance);
				Log.d("Map", "����{�b�P�Ӹ��q���I�Ѿl�ɶ�: " + infoNowRemainTime);
				Log.d("Map", "Index: " +  infoNowIndex);
				
				HashMap<String,String> item = new HashMap<String,String>();
				item.put( "Instruction", htmlInstruction[infoNowIndex]+"��V:"+JSONData.getTurn(htmlInstruction[infoNowIndex]));
				item.put( "startLatLng"," �_�I�g�n�סG " + startPoint[infoNowIndex].latitude + "/" + startPoint[infoNowIndex].longitude );
			    item.put( "endLatLng", "���I�g�n�סG "+ endPoint[infoNowIndex].latitude + "/" + endPoint[infoNowIndex].longitude);
				item.put( "nowLatLng", "�ثe�g�n��: "+infoNowLat+"/"+infoNowLng);
				item.put( "nowSpeed", "�ثe�t��: "+infoNowSpeed);
				item.put( "nowDistance", "�ثe�Ѿl�Z��: "+infoNowDistance);
				item.put( "RoadDistance", "���q�Z��: "+distance[infoNowIndex]);
				item.put( "remainTime", "�ثe�Ѿl�ɶ�: "+infoNowRemainTime);
				list.set(infoNowIndex, item);
				adapter.notifyDataSetChanged();
			}
		}
	}
		 
	 public LatLng getOriginGPS()
	 {
		 LocationManager locationManager = null;
	     Criteria criteria = null;
	     String bestProvider = null;
	     Location location = null;
		 
		 locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
	     criteria = new Criteria();
	     bestProvider = locationManager.getBestProvider(criteria, true);
	     location = locationManager.getLastKnownLocation(bestProvider);
	     
	     Double lat = 0.0, lon= 0.0;
	     
	     try 
	     {
	    	 lat = location.getLatitude();
		     lon = location.getLongitude();
	     }
	     catch (NullPointerException e)
	     {
	    	 Log.d("Map","nullpointer");
	         e.printStackTrace();
	     }
	     if(lat == 0 & lon == 0)
	    	 Log.d("Map","�ثe�L�k�ϥ�GPS���o�z����m");
	     else
	    	 Log.d("gps","�z�ثe��m��GPS�g�n�� = " + lat + "/ lon = "+lon);
	     return new LatLng(lat,lon);
	 }

	 private String getDirectionsUrl(LatLng origin, LatLng dest) 
	 {
		 // Origin of route
		 String str_origin = "origin=" + origin.latitude + ","
	    + origin.longitude;
	
		 // Destination of route
		 String str_dest = "destination=" + dest.latitude + "," + dest.longitude;
	
		 // Sensor enabled
		 String sensor = "sensor=false";
	
		 // Building the parameters to the web service
		 String parameters = str_origin + "&" + str_dest + "&" + sensor +"&mode=walking";
	
		 // Output format
		 String output = "json";
		 // Building the url to the web service
		 String url = "https://maps.googleapis.com/maps/api/directions/"
		    + output + "?" + parameters;
	
		 return url;
	 }
	 /* ���ohtml_instruction������Vcode */
	
	 /**�qURL�U��JSON��ƪ���k**/
	 private String downloadUrl(String strUrl) throws IOException 
	 {
		  String data = "";
		  InputStream iStream = null;
		  HttpURLConnection urlConnection = null;
		  try 
		  {
		   URL url = new URL(strUrl);
		
		   // Creating an http connection to communicate with url
		   urlConnection = (HttpURLConnection) url.openConnection();
		
		   // Connecting to url
		   urlConnection.connect();
		
		   // Reading data from url
		   iStream = urlConnection.getInputStream();
		   BufferedReader br = new BufferedReader(new InputStreamReader(iStream));
		   StringBuffer sb = new StringBuffer();
		   String line = "";
		  
		   while ((line = br.readLine()) != null) 
		   {
			   sb.append(line);
		   }
		   data = sb.toString();
		
		   br.close();
		}
		catch (Exception e) 
		{
		   Log.d("Exception while downloading url", e.toString());
		}
		finally 
		{
		   iStream.close();
		   urlConnection.disconnect();
		}
	  return data;
	 }
	
	 private class DownloadTask extends AsyncTask<String, Void, String>
	 {
		 @Override
		  protected String doInBackground(String... url)
		  {
			   String data = "";
			   try 
			   {
				   data = downloadUrl(url[0]);
			   }
			   catch (Exception e)
			   {
				   Log.d("Map", "Background Task"+e.toString());
			   }
			   return data;
	  }
	  
		 @Override
	  protected void onPostExecute(String result)
	  {
		  super.onPostExecute(result);
		  ParserTask parserTask = new ParserTask();
		  parserTask.execute(result);
		  Log.d("getJSON",result);
	  }
	 }
	
	 /** �ѪRJSON�榡 **/
	 private class ParserTask extends AsyncTask<String, Integer, JSONObject> 
	 {
		  @Override
		  protected JSONObject doInBackground(String... jsonData)
		 {
		   JSONObject jObject = null;
		   try 
		   {
			   jObject = new JSONObject(jsonData[0]);
			   return jObject;
			} catch (JSONException e) 
			{
				e.printStackTrace();
			}
		   return jObject;
		  }
		  
		  @Override
		  protected void onPostExecute(JSONObject result)
		  {
			  try 
			    {
			    	jRoutes = result.getJSONArray("routes");
			    	for(int i = 0; i<jRoutes.length(); i++)
			    	{
			    		jLegs = ( (JSONObject)jRoutes.get(i)).getJSONArray("legs");
				  	    /** Traversing all legs */
			
				  	    for(int j=0; j<jLegs.length(); j++)
				  	    {
				  	    	 jSteps = ( (JSONObject)jLegs.get(j)).getJSONArray("steps");
				  	    	 int len = jSteps.length();
			  	    		 startPoint = new LatLng[len];
			  	    		 endPoint = new LatLng[len];
				  	    	 startPointLat = new double[len];
			  	    		 startPointLng = new double[len];
			  	    		 endPointLat = new double[len];
			  	    		 endPointLng = new double[len];
			  	    		 distance = new double[len];
			  	    		 htmlInstruction = new String[len];
				  	    	
			  	    		 /** Traversing all steps */
			
					  	     for(int k = 0; k < jSteps.length(); k++)
					  	     {
					  	    	 endPointLat[k] = Double.parseDouble((((JSONObject)((JSONObject)jSteps.get(k)).get("end_location"))).get("lat").toString()) ;   	 
					  	    	 endPointLng[k] = Double.parseDouble((((JSONObject)((JSONObject)jSteps.get(k)).get("end_location"))).get("lng").toString()) ;
					  	    	 
					  	    	 startPointLat[k] = Double.parseDouble((((JSONObject)((JSONObject)jSteps.get(k)).get("start_location"))).get("lat").toString()) ;
					  	    	 startPointLng[k] = Double.parseDouble((((JSONObject)((JSONObject)jSteps.get(k)).get("start_location"))).get("lng").toString()) ;
					  	    	 
					  	    	 distance[k] = Double.parseDouble((((JSONObject)((JSONObject)jSteps.get(k)).get("distance"))).get("value").toString()) ;
					  	    	 
					  	    	 html_instructions = ((((JSONObject)jSteps.get(k)).getString("html_instructions")).toString());
					  	    	 htmlInstruction[k] = html_instructions.replaceAll("\\<.*?>","");
					  	    	 
					  	    	 startPoint[k] = new LatLng(startPointLat[k], startPointLng[k]);
								 endPoint[k] = new LatLng(endPointLat[k], endPointLng[k]);
					  	     }
				  	    }
			    	}
			    } catch (JSONException e)
			    { 
			    	e.printStackTrace();
			    }
			    catch (Exception e){}
			  // �إ߭n�ǰe��jsonData
			  JSONData allData = null;
			  allData = new JSONData(startPointLat,startPointLng,endPointLat,endPointLng,htmlInstruction);
			  			  
			  //���ƥ[�JArrayList��
			  list.clear();
			  if(htmlInstruction.length != 0)
			  {
				  for(int index = 0; index < htmlInstruction.length; index++)
				  {
					    HashMap<String,String> item = new HashMap<String,String>();
					    Log.d("Map",htmlInstruction[index]);
					    item.put( "Instruction", htmlInstruction[index]+"��V:"+JSONData.getTurn(htmlInstruction[index]));
						item.put( "startLatLng"," �_�I�g�n�סG " + startPoint[index].latitude + "/" + startPoint[index].longitude );
					    item.put( "endLatLng", "���I�g�n�סG "+ endPoint[index].latitude + "/" + endPoint[index].longitude);
						item.put( "nowLatLng", "�ثe�g�n��: unknown");
						item.put( "nowSpeed", "�ثe�t��: unknown");
						item.put( "nowDistance", "�ثe�Ѿl�Z��: unknown");
						item.put( "RoadDistance", "���q�Z��: unknown");
						item.put( "remainTime", "�ثe�Ѿl�ɶ�: unknown");
						list.add(item);
				  }
			  }
			  //�s�WSimpleAdapter
			  adapter = new SimpleAdapter( 
			  Map.this, 
			  list,
			  R.layout.mylistview1, 
			  new String[] { "Instruction","startLatLng","endLatLng","nowLatLng","nowSpeed","nowDistance", "RoadDistance","remainTime" },
			  new int[] { R.id.textView1, R.id.textView2, R.id.textView3, R.id.textView4, R.id.textView5, R.id.textView6, R.id.textView7, R.id.textView8 } );
			  
			  setListAdapter(adapter);
			  
			  Bundle bundle = new Bundle();
			  bundle.putSerializable("allData",allData);
			  intent.putExtras(bundle);
			  
			  startService(intent);
		  }
	 }
}