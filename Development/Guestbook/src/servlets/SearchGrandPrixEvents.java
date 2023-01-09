package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import prediction.DatabaseConnection;

import com.google.gson.Gson;

public class SearchGrandPrixEvents extends HttpServlet
{
	public void doPost(HttpServletRequest req, HttpServletResponse resp )
	{
		//this class is used just to get the list of circuits for a particular season
		
		System.out.println(req);
		
    	Gson JsonParser  = new Gson();
    	
    	StringBuilder sb = new StringBuilder();
        BufferedReader reader;
		
        try 
        {
        	reader = req.getReader();
            String line;
            while ((line = reader.readLine()) != null) 
            {
            	sb.append(line);
            }
            
            reader.close();
        }
        
        catch (IOException e) 
        {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
        
        String Season = sb.substring(11, 15);
        
        System.out.println(Season);
        
        //now need to query the database to get all events from that season
        //attmpted to use distinct but it returned an invalid query
        //this is why dataOutput2 as it does not contain duplicates
        
        DatabaseConnection dbConnection = new DatabaseConnection();
		
		dbConnection.query = "select CircuitName from F1Prediction.circuits inner join RaceCalender on CircuitPrimKey =  RaceCalender.CircuitID inner join DriverRaceWeekend on RaceCalenderPrimKey = DriverRaceWeekend.RaceCalenderID where PreviousRace1 <> -1 AND PreviousRace2 <> -1 AND PreviousRace3 <> -1 AND RaceCalender.Season = " + Season;
		
		//need to get rid of the duplicate values
		
		String[] dataOutput = dbConnection.runStringQuery();
		
		//a set is a collection with no duplicates
		Set<String> set = new HashSet<String>();
		
		for(int i = 0;i<dataOutput.length;i++)
		{
			set.add(dataOutput[i]);
		}
		
		int i=0;
		int setSize = set.size();
		String[] dataOutput2 = new String[setSize];
		Iterator it = set.iterator();
		while(it.hasNext())
		{
			dataOutput2[i] = (String) it.next();
			i++;
		}
		
        Gson ReturnedJson = new Gson();
        
        //The string is initialised to )]}',\n This is a specific string 
        //that is used to protect against a Json security vulnerability 
        
        String ReturnedjsonStr = ")]}',\n";
        
        ReturnedjsonStr += ReturnedJson.toJson(dataOutput2);
        
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