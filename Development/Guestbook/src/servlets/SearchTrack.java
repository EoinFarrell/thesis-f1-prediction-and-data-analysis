package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import prediction.DatabaseConnection;
import prediction.JsonLinearRegression;

import com.google.gson.Gson;

import dataStorage.StoreTrackOutput;

public class SearchTrack extends HttpServlet {
	
	String ReturnedjsonStr = "";
	private static final Logger log = Logger.getLogger(JsonLinearRegression.class.getName());
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException
	{
		System.out.println(req);
		System.out.println("^^^^^");
		
    	Gson JsonParser  = new Gson();
    	
    	StringBuilder sb = new StringBuilder();
        BufferedReader reader = req.getReader();
        
        try 
        {
            String line;
            while ((line = reader.readLine()) != null) 
            {
            	sb.append(line);
            }
        } 
        finally 
        {
            reader.close();
        }
        
        System.out.println(sb);
        
        String RequestType = req.getHeader("Type");
        
        DatabaseConnection dbConnection = new DatabaseConnection();
        
        if("trackDataUpdate".equals(RequestType))
        {
        	String temp = sb.toString();
        	StoreTrackOutput inputData = JsonParser.fromJson(temp, StoreTrackOutput.class);
        	
        	//need to now update the database with the new info
        	
        	dbConnection.query = "update F1Prediction.Circuits SET CircuitName = '" + inputData.CircuitName + "', Firstyear = " + inputData.FirstYear + ", Lastyear = " 
        			+ inputData.LastYear + ", Region = '" + inputData.Region + "', Country = '" + inputData.Country + "', GrandsPrixHeld = " + inputData.GrandsPrixHeld 
        			+ ", Length = " + inputData.TrackLength + ", Turns = " + inputData.Turns + ", LapRecord = '" + inputData.LapRecord + "', LapRecordHolder = '" 
        			+ inputData.LapRecordHolder + "', Description = '" + inputData.Description + "' where CircuitName = '" + inputData.CircuitName + "'";
        	
        	dbConnection.runUpdateQuery();
        }
        else
        {	
			dbConnection.query = "select CircuitName, Firstyear, Lastyear, Region, Country, GrandsPrixHeld, Length, Turns, LapRecord, LapRecordHolder, Description, ImageLink from F1Prediction.circuits";
			
			StoreTrackOutput[] dataOutput = dbConnection.getTrackData();
			
			Gson ReturnedJson = new Gson();
	        
	        //The string is initialised to )]}',\n This is a specific string 
	        //that is used to protect against a Json security vulnerability 
	        
	        ReturnedjsonStr = ")]}',\n";
	        
	        ReturnedjsonStr += ReturnedJson.toJson(dataOutput);
        }
        
        try 
    	{
    		resp.setContentType("application/json");  
        	resp.setCharacterEncoding("UTF-8");
    		resp.getWriter().write(ReturnedjsonStr);
		} 
    	catch (IOException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

}
