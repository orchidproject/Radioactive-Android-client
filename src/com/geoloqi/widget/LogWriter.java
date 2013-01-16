package com.geoloqi.widget;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Date;
import android.util.Log;

public class LogWriter {
	File logFile;
	
	public LogWriter(){
		Date d= new Date();
		String file_name="sdcard/log-"+d.getSeconds()+"-"+d.getMinutes()+"-"+d.getHours()+"-"+d.getDate()+"-"+d.getMonth()+".txt";
		logFile= new File(file_name );
	}
	
	public void appendLog(String text)
	{    
		
	   if (!logFile.exists())
	   {
	      try
	      {
	         logFile.createNewFile();
	      } 
	      catch (IOException e)
	      {
	         // TODO Auto-generated catch block
	         e.printStackTrace();
	      }
	   }
	   try
	   {
		   Log.i("a", "writing logs");
	      //BufferedWriter for performance, true to set append to file flag
	      BufferedWriter buf = new BufferedWriter(new FileWriter(logFile, true)); 
	      buf.append(text);
	      buf.newLine();
	      buf.flush();
	      buf.close();
	   }
	   catch (IOException e)
	   {
	      // TODO Auto-generated catch block
	      e.printStackTrace();
	   }
	}

}
