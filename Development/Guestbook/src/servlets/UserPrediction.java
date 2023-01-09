package servlets;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.logging.Logger;

import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import prediction.DatabaseConnection;
import prediction.JsonLinearRegression;
import prediction.PredictionInputs;

import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.*;

import dataStorage.StoreLeagueData;
import dataStorage.StoreModelOutput;
import dataStorage.StoreNextRace;


//*************************************************************************
//Last Edited by: Eoin Farrell - Last Edit Date: 08/03/2014
//This class is used to take in the user Ajax prediction request
//It first finds out where on the site the request came from using 
//the request type header.
//Following this, it then moves the request data into an input class
//before then instante an instance of jsonLinearRegression
//to handle this data and run the linear regreession model
//*************************************************************************

public class UserPrediction extends HttpServlet
{
	private StoreModelOutput[] modelData;
	private static final Logger log = Logger.getLogger(JsonLinearRegression.class.getName());
	
	@Override
	public void doPost(HttpServletRequest req, HttpServletResponse resp) throws IOException 
	{  	
		System.out.println(req);
		System.out.println("^^^^^");
		
		UserService userService = UserServiceFactory.getUserService();
        User user = userService.getCurrentUser();
		
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
        
        //possible to put an if statement in here to figure out what algorithm the user wants in the future
        //also possible to add if statement in case the user is not using json & ajax
        //looking at the input from the req this should be possible
        
        
        String RequestType = req.getHeader("Type");
        System.out.println(RequestType);
        
        Gson ReturnedJson = new Gson();
        
		//The string is initialised to )]}',\n This is a specific string 	
        //that is used to protect against a Json security vulnerability 
		String ReturnedjsonStr = ")]}',\n";
        
        if("updateRacePrediction".equals(RequestType))
        {
        	StoreModelOutput[] inputData = JsonParser.fromJson(sb.toString(),StoreModelOutput[].class);
        	
        	JsonLinearRegression RegressionPrediction = new JsonLinearRegression();
        	
        	modelData = RegressionPrediction.updateRacePrediction(inputData);
        	
        	ReturnedjsonStr += ReturnedJson.toJson(modelData);
        }
        else if("racePrediction".equals(RequestType))
        {
        	JsonLinearRegression RegressionPrediction = JsonParser.fromJson(sb.toString(),JsonLinearRegression.class);
            
            modelData = RegressionPrediction.runRacePrediction();

            ReturnedjsonStr += ReturnedJson.toJson(modelData);
        }
        else if("userPrediction".equals(RequestType))
        {
        	JsonLinearRegression RegressionPrediction = JsonParser.fromJson(sb.toString(),JsonLinearRegression.class);
		
        	modelData = RegressionPrediction.RunLinearRegression();
        	
        	ReturnedjsonStr += ReturnedJson.toJson(modelData);
        }
        else if("nextRace".equals(RequestType))
        {
        	//query database to get next race
        	DatabaseConnection dbConnection = new DatabaseConnection();
        	StoreNextRace[] raceData;
        	
        	if(user != null)
        	{
	        	dbConnection.query = "Select PredictionsLeaguePrimKey from F1Prediction.PredictionsLeague where UserEamil = '" + user.getEmail() + "';";
				String[] userLeagueCheck = dbConnection.runStringQuery();
				raceData = dbConnection.getNextRace();
				String[] userPredictionCheck = null;
				
				//need to first check if the user is in the league
				if(userLeagueCheck.length != 0)
				{
					dbConnection.query = "Select PredictionsPrimKey from F1Prediction.Predictions where PredictionLeagueID = " + userLeagueCheck[0] + " AND RaceCalenderID = " + raceData[0].raceWeekendID;
					userPredictionCheck = dbConnection.runStringQuery();
					
					if(userPredictionCheck.length == 0)
					{
						userPredictionCheck = null;
					}
				}
				
				//if they are in the league then we must see if they have made a prediction on this race, if not just return next race
				if(userPredictionCheck == null)
				{
					raceData = dbConnection.getNextRace();
	        	
					ReturnedjsonStr += ReturnedJson.toJson(raceData);
				}
				else
				{
					String values = "FirstPos,SecondPos,ThirdPos,FourthPos,FifthPos,SixthPos,SeventhPos,EightPos,NinethPos,TenthPos,EleventhPos,TwelftPos,ThirteenthPos,FourteenthPos,FifteenthPos,SixteenthPos,SeventeenthPos,EighteenthPos,NineteenthPos,TwentiethPos,TwentiethFirstPos,TwentiethSecondPos";
					dbConnection.query = "select CircuitName, GrandPrix, RaceDate, AbbreviatedName, StartPos, DriverPrimKey, RaceCalenderPrimKey, " + values + " from F1Prediction.RaceCalender inner join circuits on circuits.CircuitPrimKey = CircuitID inner join DriverRaceWeekend on DriverRaceWeekend.RaceCalenderID = RaceCalenderPrimKey inner join Drivers on Drivers.DriverPrimKey = DriverRaceWeekend.DriversID inner join Predictions on RaceCalenderPrimKey = Predictions.RaceCalenderID inner join PredictionsLeague on Predictions.PredictionLeagueID = PredictionsLeague.PredictionsLeaguePrimKey " + "where RaceCalenderPrimKey = " + raceData[0].raceWeekendID + " AND UserEamil = '" + user.getEmail() + "' order by DriverRaceWeekend.CarNumber";
					raceData = dbConnection.getNextRaceWithPredictions();
					
					ReturnedjsonStr += ReturnedJson.toJson(raceData);
				}
        	}
        	else
        	{
        		raceData = dbConnection.getNextRace();
	        	
				ReturnedjsonStr += ReturnedJson.toJson(raceData);
        	}
        }
        else if("getUserPredictionTable".equals(RequestType))
        {
        	DatabaseConnection dbConnection = new DatabaseConnection();
        	dbConnection.query = "select NickName, LeaguePoints from F1Prediction.PredictionsLeague order by LeaguePoints";
        	
        	//need a function within dbConnection to do the league points so i can easily just return the data here
        	StoreLeagueData[] leagueData = dbConnection.getLeagueData();
        	
        	ReturnedjsonStr += ReturnedJson.toJson(leagueData);
        	
        	//need to query the database now and get the league data
        }
        else if("insertPrediction".equals(RequestType))
        {
        	//this statement is used to predict a race result when a user has altered the explanatory variables of the race testing set
        	//first of all we need to push the inserted json into a class
        	
        	StoreNextRace[] inputData =  JsonParser.fromJson(sb.toString(),StoreNextRace[].class);

        	PredictionInputs storeInputData = new PredictionInputs();
        	
        	storeInputData.insertPrediction(inputData, user);
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
