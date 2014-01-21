package com.nchu.motoguider;
/*
 * Direction JSON 取回來的資料儲存為此型態
 * */
import java.io.Serializable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import com.google.android.gms.maps.model.LatLng;

public class JSONData implements Serializable
{

	private static final long serialVersionUID = 1L;
	double startPointLat[];
	double startPointLng[];
	double endPointLat[];
	double endPointLng[];
	String htmlInstruction[];
	
    int[] testValue={1,2,3,4};
    
	public JSONData(double startLat[], double[] startLng, double[] endLat, double[] endLng, String[] html)
	{
		startPointLat = startLat;
		startPointLng = startLng;
		endPointLat = endLat;
		endPointLng = endLng;
		htmlInstruction = html;
	}
	
	/* get JSON data */

	public double[] getAllRoadStartPointLat()
	{
		return startPointLat;
	}
	
	public double[] getAllRoadStartPointLng()
	{
		return startPointLng;
	}
	
	public double[] getAllRoadEndPointLat()
	{
		   return endPointLat;
	}
	
	public double[] getAllRoadEndPointLng()
	{
		   return endPointLng;
	}
	
	public String[] getAllRoadInstruction()
	{
		return htmlInstruction;
	}
	
	 public void setTestValue(int arg) 
	 { 
	 } 
	 public int[] getTestValue()
	 { 
		 return testValue; 
	 }
	
	/* parse html's instruction direction*/
	
	public static int getTurn(String s)
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