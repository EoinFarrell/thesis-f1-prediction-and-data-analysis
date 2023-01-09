package servlets;

import java.io.IOException;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import prediction.DatabaseConnection;

import com.google.gson.Gson;

import dataStorage.StoreDriverOutput;

public class SearchDriver extends HttpServlet {
	
	public StoreDriverOutput[] dataOutput;
	
	public void doPost(HttpServletRequest req, HttpServletResponse resp)
	{
		DatabaseConnection dbConnection = new DatabaseConnection();
		
		dbConnection.query = "select FirstName, MiddleName, LastName, AbbreviatedName, DateofBirth, HomeCountry, Region, Height, Weight, SeasonWins, RaceStarts, RaceWins, Podiums, PolePositions, LapsRaced, LapsLed, Imagelink from F1Prediction.Drivers";
		dbConnection.ColumnCount = 1;
		
		dataOutput = dbConnection.getDriverData();
		
		Gson ReturnedJson = new Gson();
        
        //The string is initialised to )]}',\n This is a specific string 
        //that is used to protect against a Json security vulnerability 
        
        String ReturnedjsonStr = ")]}',\n";
        
        ReturnedjsonStr += ReturnedJson.toJson(dataOutput);
        
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
