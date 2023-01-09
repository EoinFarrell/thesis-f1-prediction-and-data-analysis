package prediction;

import com.google.appengine.api.users.User;

import dataStorage.StoreDriverOutput;
import dataStorage.StoreNextRace;

public class PredictionInputs
{
	private String predictionFields = "(RaceCalenderID,PredictionLeagueID,FirstPos,SecondPos,ThirdPos,FourthPos,FifthPos,SixthPos,SeventhPos,EightPos,NinethPos,TenthPos,EleventhPos,TwelftPos,ThirteenthPos,FourteenthPos,FifteenthPos,SixteenthPos,SeventeenthPos,EighteenthPos,NineteenthPos,TwentiethPos,TwentiethFirstPos,TwentiethSecondPos)";
	private String values = "";
	
	public void insertPrediction(StoreNextRace[] inputData, User user)
	{
		DatabaseConnection dbConnection = new DatabaseConnection();
		
		//check if there is a user
		if(user != null)
		{
			//check if user is in the league
			dbConnection.query = "Select PredictionsLeaguePrimKey from F1Prediction.PredictionsLeague where UserEamil = '" + user.getEmail() + "';";
			String[] userLeagueCheck = dbConnection.runStringQuery();
			
			if(userLeagueCheck.length == 0)
			{
				//create new row for the user
				dbConnection.query = "insert into F1Prediction.PredictionsLeague (UserEamil, NickName, LeaguePoints) values ('" + user.getEmail() + "', '" + user.getNickname() + "', 0)";
				dbConnection.runUpdateQuery();
				
				//select the user id
				dbConnection.query = "Select PredictionsLeaguePrimKey from F1Prediction.PredictionsLeague where UserEamil = '" + user.getEmail() + "';";
				userLeagueCheck = dbConnection.runStringQuery();
			}
			
			int[] driverIDPrediction = new int[inputData.length];
			
			//this is used to sort the driver ID by their predicted finish pos
			for(int i = 0;i<inputData.length;i++)
			{
				for(int j = 0;j<inputData.length;j++)
				{
					if(inputData[j].prediction == i+1)
					{
						driverIDPrediction[i] = inputData[j].driverID;
						j = inputData.length;
					}
				}
			}
			
			//add in ' ' to each string so that don't have to do it for each entry in the query;
			
			for(int i =0;i<inputData.length;i++)
			{
				values += ", " + driverIDPrediction[i] + "";
			}
			
			//check if ther user has already made a prediction on this race
			dbConnection.query = "select PredictionsPrimKey from F1Prediction.Predictions where PredictionLeagueID = " + userLeagueCheck[0] + " AND RaceCalenderID = " + inputData[0].raceWeekendID;
			String[] userPredictionCheck = dbConnection.runStringQuery();
			
			if(userPredictionCheck.length == 0)
			{
				//insert user prediction into table
				dbConnection.query = "insert into F1Prediction.Predictions " + predictionFields + " values (" + inputData[0].raceWeekendID + ", " + userLeagueCheck[0] + values + ");";
				dbConnection.runUpdateQuery();
			}
			else
			{
				//update the users row
				//if i was to do this again, I would create a child table that would 
				//reference the Predictions table, with a row for each prediction,
				//instead of a so many columns
				dbConnection.query = "update F1Prediction.Predictions Set FirstPos = " + driverIDPrediction[0] + " ,SecondPos = " + driverIDPrediction[1] + " ,ThirdPos = " + driverIDPrediction[2] + " ,FourthPos = " + driverIDPrediction[3] + " ,FifthPos = " + driverIDPrediction[4] + " ,SixthPos = " + driverIDPrediction[5] + " ,SeventhPos = " + driverIDPrediction[6] + " ,EightPos = " + driverIDPrediction[7] + " ,NinethPos = " + driverIDPrediction[8] + " ,TenthPos = " + driverIDPrediction[9] + " ,EleventhPos = " + driverIDPrediction[10] + " ,TwelftPos = " + driverIDPrediction[11] + " ,ThirteenthPos = " + driverIDPrediction[12] + " ,FourteenthPos = " + driverIDPrediction[13] + " ,FifteenthPos = " + driverIDPrediction[14] + " ,SixteenthPos = " + driverIDPrediction[15] + " ,SeventeenthPos = " + driverIDPrediction[16] + " ,EighteenthPos = " + driverIDPrediction[17] + " ,NineteenthPos = " + driverIDPrediction[18] + " ,TwentiethPos = " + driverIDPrediction[19] + " ,TwentiethFirstPos = " + driverIDPrediction[20] + " ,TwentiethSecondPos  = " + driverIDPrediction[21] + " where PredictionsPrimKey = " + userPredictionCheck[0];
				dbConnection.runUpdateQuery();
			}
			
			
			
		}
	}
}
