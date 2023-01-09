package prediction;

//*************************************************************************
//Last Edited by: Eoin Farrell - Last Edit Date: 03/03/2014
//This class is used to connect to the MySQL (development)/Google
//Cloud SQL (production) RDBMS. This will be used to pull
//information when needed for F1 prediction and for driver and circuit
//queries
//*************************************************************************

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Logger;

import com.google.appengine.api.utils.SystemProperty;

import dataStorage.StoreDriverOutput;
import dataStorage.StoreLeagueData;
import dataStorage.StoreNextRace;
import dataStorage.StoreTrackOutput;

public class DatabaseConnection
{
	//query set up
	private String url = null;
	public String query;
	public int ColumnCount;
	public int RowCount = 0;
	
	private Connection connect;
	private Statement statement;
	private String countQuery;
	private ResultSet resultSet;
	private ResultSetMetaData resultSetMData;
	private static final Logger log = Logger.getLogger(DatabaseConnection.class.getName());
	
	public DatabaseConnection()
	{
		if (SystemProperty.environment.value() == SystemProperty.Environment.Value.Production) 
		{
			// Local Google Cloud SQL instance to use for production.
			try 
			{
				//Class.forName("com.mysql.jdbc.GoogleDriver");
				Class.forName("com.google.cloud.sql.Driver");
			} 
			catch (ClassNotFoundException e) 
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  //url = "jdbc:mysql://173.194.110.127:3306/F1Prediction?user=root&password=centre";
			  url = "jdbc:google:rdbms://f1prediction:db/F1Prediction?user=root";
			  System.out.println("URL to production RDBMS created");
		} 
		
		else 
		{
			// Local MySQL instance to use during development.
			try 
			{
				Class.forName("com.mysql.jdbc.Driver");
			} 
			catch (ClassNotFoundException e)
			{
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			  url = "jdbc:mysql://127.0.0.1:3306/F1Prediction?user=root&password=s^6d^%25$7cx4F!%256248";
			  
			  System.out.println("URL to development RDBMS created");
		}
	}
	
	public double[][] getTrainingData(boolean PRace3, boolean PRace2, boolean PRace1, boolean FP3Pos, boolean SPos)
	{
		//Query the DB and place results in arrays
		querySetUp();
		
		double[][] trainingMatrix = new double[ColumnCount][RowCount];
		
		int i = 0;
		int j = 1;
		
		try
		{
			while(resultSet.next())
			{
				if(PRace3)
				{
					trainingMatrix[j-1][i] = resultSet.getInt(j);
					j++;
				}
				if(PRace2)
				{
					trainingMatrix[j-1][i] = resultSet.getInt(j);
					j++;
				}
				if(PRace1)
				{
					trainingMatrix[j-1][i] = resultSet.getInt(j);
					j++;
				}
				if(FP3Pos)
				{
					trainingMatrix[j-1][i] = resultSet.getInt(j);
					j++;
				}
				if(SPos)
				{
					trainingMatrix[j-1][i] = resultSet.getInt(j);
					j++;
				}
				
				trainingMatrix[j-1][i] = resultSet.getInt(j);
				j = 1;
				i++;
			}
		}
			
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		return trainingMatrix;
	}
	
	public double[][] getTestData(boolean PRace3, boolean PRace2, boolean PRace1, boolean FP3Pos, boolean SPos)
	{	
		querySetUp();
		
		double[][] testingMatrix = new double[ColumnCount][RowCount];
		
		int i = 0;
		int j = 1;
		
		try 
		{
			while(resultSet.next())
			{
				if(PRace3)
				{
					testingMatrix[j-1][i] = resultSet.getInt(j);
					j++;
				}
				if(PRace2)
				{
					testingMatrix[j-1][i] = resultSet.getInt(j);
					j++;
				}
				if(PRace1)
				{
					testingMatrix[j-1][i] = resultSet.getInt(j);
					j++;
				}
				if(FP3Pos)
				{
					testingMatrix[j-1][i] = resultSet.getInt(j);
					j++;
				}
				if(SPos)
				{
					testingMatrix[j-1][i] = resultSet.getInt(j);
				}
				j = 1;
				i++;
			}
		} 
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		closeConnection();
		
		return testingMatrix;
	}
	
	public String[] runStringQuery()
	{
		querySetUp();
		
		String[] returnedString = new String[RowCount];
		int i = 0;
		
		try 
		{
			while(resultSet.next())
			{
				returnedString[i] = resultSet.getString(1);
				i++;
			}
		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		closeConnection();
		return returnedString;		
	}
	
	public double[] runDoubleQuery()
	{
		querySetUp();
		
		double[] returnedDouble = new double[RowCount];
		int i = 0;
		
		try 
		{
			while(resultSet.next())
			{
				returnedDouble[i] = resultSet.getInt(1);
				i++;
			}
		} 
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		closeConnection();
		return returnedDouble;
	}
	
	//this functions just pushes update functions to the server
	public void runUpdateQuery()
	{
		try
		{
			connect = DriverManager.getConnection(url);
			statement = connect.createStatement();
			statement.executeUpdate(query);
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	public StoreTrackOutput[] getTrackData()
	{
		StoreTrackOutput[] trackOutput;
		
		querySetUp();
			
		trackOutput = new StoreTrackOutput[RowCount];
			
		int i;
			
		for(i = 0;i<RowCount;i++)
		{
			trackOutput[i] = new StoreTrackOutput();
		}
			
		i = 0;
			
		try 
		{
			while(resultSet.next())
			{
				trackOutput[i].CircuitName = resultSet.getString(1);
				trackOutput[i].FirstYear = resultSet.getString(2); 
				trackOutput[i].LastYear = resultSet.getString(3);
				trackOutput[i].Region = resultSet.getString(4);
				trackOutput[i].Country = resultSet.getString(5);
				trackOutput[i].GrandsPrixHeld = resultSet.getInt(6);
				trackOutput[i].TrackLength = resultSet.getFloat(7);
				trackOutput[i].Turns = resultSet.getInt(8);
				trackOutput[i].LapRecord = resultSet.getString(9);
				trackOutput[i].LapRecordHolder = resultSet.getString(10);
				trackOutput[i].Description = resultSet.getString(11);
				trackOutput[i].ImageLink = resultSet.getString(12);
				i++;
			}
		}
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
			log.severe("Problem while updating XXX record : " + e.getLocalizedMessage());
		}
		
		closeConnection();
		return trackOutput;
	}
	
	public StoreDriverOutput[] getDriverData()
	{		
		StoreDriverOutput[] DriverOutput;
		
		querySetUp();
			
		DriverOutput = new StoreDriverOutput[RowCount];
			
		int i;
			
		for(i = 0;i<RowCount;i++)
		{
			DriverOutput[i] = new StoreDriverOutput();
		}
			
		i = 0;
			
		try 
		{
			while(resultSet.next())
			{
				
				DriverOutput[i].FirstName = resultSet.getString(1);
				DriverOutput[i].MiddleName = resultSet.getString(2); 
				DriverOutput[i].LastName = resultSet.getString(3);
				DriverOutput[i].AbbreviatedName = resultSet.getString(4);
				DriverOutput[i].DateofBirth = resultSet.getString(5);
				DriverOutput[i].HomeCountry = resultSet.getString(6);
				DriverOutput[i].Region = resultSet.getString(7);
				DriverOutput[i].Height = resultSet.getInt(8);
				DriverOutput[i].Weight = resultSet.getInt(9);
				DriverOutput[i].SeasonWins = resultSet.getInt(10);
				DriverOutput[i].RaceStarts = resultSet.getInt(11);
				DriverOutput[i].RaceWins = resultSet.getInt(12);
				DriverOutput[i].Podiums = resultSet.getInt(13);
				DriverOutput[i].PolePositions = resultSet.getInt(14);
				DriverOutput[i].LapsRaced = resultSet.getInt(15);
				DriverOutput[i].LapsLed = resultSet.getInt(16);
				DriverOutput[i].Imagelink = resultSet.getString(17);
				i++;
			}
		}
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		closeConnection();
		return DriverOutput;
	}
	
	public StoreNextRace[] getNextRace()
	{
		StoreNextRace[] RaceOutput;
		
		//need to run one query to find out what the next race is then use that in here
		
		DatabaseConnection checkSelf = new DatabaseConnection();
		checkSelf.query = "select RaceCalenderPrimKey from F1Prediction.RaceCalender where RaceDate >= CURDATE() ORDER BY RaceDate LIMIT 1;";
		
		String[] nextRaceId = new String[1];
		nextRaceId = checkSelf.runStringQuery();
		
		query = "select CircuitName, GrandPrix, RaceDate, AbbreviatedName, StartPos, DriverPrimKey, RaceCalenderPrimKey from F1Prediction.RaceCalender inner join circuits on circuits.CircuitPrimKey = CircuitID inner join DriverRaceWeekend on DriverRaceWeekend.RaceCalenderID = RaceCalenderPrimKey inner join Drivers on Drivers.DriverPrimKey = DriverRaceWeekend.DriversID where RaceCalenderPrimKey = " + nextRaceId[0] + " order by DriverRaceWeekend.CarNumber";
		
		querySetUp();
		
		RaceOutput = new StoreNextRace[RowCount];
		
		int i;
		
		for(i=0;i<RowCount;i++)
		{
			RaceOutput[i] = new StoreNextRace();
		}
		
		i = 0;
		
		try 
		{
			while(resultSet.next())
			{
				RaceOutput[i].circuitName = resultSet.getString(1);
				RaceOutput[i].GPName = resultSet.getString(2);
				RaceOutput[i].date = resultSet.getString(3);
				RaceOutput[i].driverName = resultSet.getString(4);
				RaceOutput[i].startPos = resultSet.getInt(5);
				RaceOutput[i].driverID = resultSet.getInt(6);
				RaceOutput[i].raceWeekendID = resultSet.getInt(7);
				RaceOutput[i].prediction = i + 1;
				i++;
			}
		}
		catch (SQLException e)
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		closeConnection();
		return RaceOutput;
	}
	
	public StoreNextRace[] getNextRaceWithPredictions()
	{
		StoreNextRace[] RaceOutput;
		
		querySetUp();
		
		RaceOutput = new StoreNextRace[RowCount];
		
		int i;
		
		for(i=0;i<RowCount;i++)
		{
			RaceOutput[i] = new StoreNextRace();
		}
		
		i = 0;
		
		try 
		{
			while(resultSet.next())
			{
				RaceOutput[i].circuitName = resultSet.getString(1);
				RaceOutput[i].GPName = resultSet.getString(2);
				RaceOutput[i].date = resultSet.getString(3);
				RaceOutput[i].driverName = resultSet.getString(4);
				RaceOutput[i].startPos = resultSet.getInt(5);
				RaceOutput[i].driverID = resultSet.getInt(6);
				RaceOutput[i].raceWeekendID = resultSet.getInt(7);
				
				if(RaceOutput[i].driverID == resultSet.getInt(8))
				{
					RaceOutput[i].prediction = 1;
				}
				else if(RaceOutput[i].driverID == resultSet.getInt(9))
				{
					RaceOutput[i].prediction = 2;
				}
				else if(RaceOutput[i].driverID == resultSet.getInt(10))
				{
					RaceOutput[i].prediction = 3;
				}
				else if(RaceOutput[i].driverID == resultSet.getInt(11))
				{
					RaceOutput[i].prediction = 4;
				}
				else if(RaceOutput[i].driverID == resultSet.getInt(12))
				{
					RaceOutput[i].prediction = 5;
				}
				else if(RaceOutput[i].driverID == resultSet.getInt(13))
				{
					RaceOutput[i].prediction = 6;
				}
				else if(RaceOutput[i].driverID == resultSet.getInt(14))
				{
					RaceOutput[i].prediction = 7;
				}
				else if(RaceOutput[i].driverID == resultSet.getInt(15))
				{
					RaceOutput[i].prediction = 8;
				}
				else if(RaceOutput[i].driverID == resultSet.getInt(16))
				{
					RaceOutput[i].prediction = 9;
				}
				else if(RaceOutput[i].driverID == resultSet.getInt(17))
				{
					RaceOutput[i].prediction = 10;
				}
				else if(RaceOutput[i].driverID == resultSet.getInt(18))
				{
					RaceOutput[i].prediction = 11;
				}
				else if(RaceOutput[i].driverID == resultSet.getInt(19))
				{
					RaceOutput[i].prediction = 12;
				}
				else if(RaceOutput[i].driverID == resultSet.getInt(20))
				{
					RaceOutput[i].prediction = 13;
				}
				else if(RaceOutput[i].driverID == resultSet.getInt(21))
				{
					RaceOutput[i].prediction = 14;
				}
				else if(RaceOutput[i].driverID == resultSet.getInt(22))
				{
					RaceOutput[i].prediction = 15;
				}
				else if(RaceOutput[i].driverID == resultSet.getInt(23))
				{
					RaceOutput[i].prediction = 16;
				}
				else if(RaceOutput[i].driverID == resultSet.getInt(24))
				{
					RaceOutput[i].prediction = 17;
				}
				else if(RaceOutput[i].driverID == resultSet.getInt(25))
				{
					RaceOutput[i].prediction = 18;
				}
				else if(RaceOutput[i].driverID == resultSet.getInt(26))
				{
					RaceOutput[i].prediction = 19;
				}
				else if(RaceOutput[i].driverID == resultSet.getInt(27))
				{
					RaceOutput[i].prediction = 20;
				}
				else if(RaceOutput[i].driverID == resultSet.getInt(28))
				{
					RaceOutput[i].prediction = 21;
				}
				else if(RaceOutput[i].driverID == resultSet.getInt(29))
				{
					RaceOutput[i].prediction = 22;
				}
				i++;
			}
		}
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		closeConnection();
		return RaceOutput;
	}
	
	public StoreLeagueData[] getLeagueData()
	{
		StoreLeagueData[] LeagueOutput;
		
		
		query = "select NickName, LeaguePoints from F1Prediction.PredictionsLeague order by LeaguePoints";
		querySetUp();
		
		LeagueOutput = new StoreLeagueData[RowCount];
		
		int i;
		
		for(i=0;i<RowCount;i++)
		{
			LeagueOutput[i] = new StoreLeagueData();
		}
		
		i = 0;
		
		try 
		{
			while(resultSet.next())
			{
				LeagueOutput[i].userNickName = resultSet.getString(1);
				LeagueOutput[i].points = resultSet.getInt(2);
				i++;
			}
		}
		catch (SQLException e) 
		{
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		closeConnection();
		return LeagueOutput;
	}
	
	private void querySetUp()
	{
		try 
		{
			connect = DriverManager.getConnection(url);
			//connect = DriverManager.getConnection("jdbc:google:mysql://f1prediction:db/F1Prediction","root", "centre");
			statement = connect.createStatement();
			
			countQuery = query.substring(0, 6) + " Count(*) AS C," + query.substring(6, query.length());
			
			resultSet = statement.executeQuery(countQuery);
			
			while(resultSet.next())
			{
				RowCount = resultSet.getInt("C");
				System.out.println(RowCount);
				String temp = String.valueOf(RowCount);
			}
			
			resultSet = statement.executeQuery(query);
			
			resultSetMData = resultSet.getMetaData();
			
			ColumnCount = resultSetMData.getColumnCount();
		}
		catch(SQLException e)
		{
			e.printStackTrace();
		}
	}
	
	private void closeConnection()
	{    
	    try 
	    { 
	    	resultSet.close();
	    	statement.close();
	    	connect.close();
	    } 
	    catch (Exception e) 
	    {
	    	e.printStackTrace();
	    }
	    
	}
}
