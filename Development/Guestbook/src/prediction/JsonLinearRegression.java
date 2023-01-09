package prediction;

import com.google.appengine.api.datastore.DatastoreService;
import com.google.appengine.api.datastore.DatastoreServiceFactory;
import com.google.appengine.api.datastore.Entity;
import com.google.appengine.api.datastore.Key;
import com.google.appengine.api.datastore.KeyFactory;
import com.google.appengine.api.users.User;
import com.google.appengine.api.users.UserService;
import com.google.appengine.api.users.UserServiceFactory;
import com.google.gson.Gson;

import dataStorage.StoreModelOutput;

import java.io.BufferedReader;
import java.io.IOException;
import java.text.DecimalFormat;
import java.util.Date;
import java.util.List;
import java.util.logging.Logger;
	


import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.servlet.http.HttpServletRequest;

public class JsonLinearRegression
{
	//data store variables
	private static final Logger log = Logger.getLogger(JsonLinearRegression.class.getName());
	private Entity greeting = null;
	private User user = null;
	private String guestbookName;
	boolean dataCorrect;
	String errorMsg = "No Error";
	
	
	//Store Json Data
	boolean PreviousRace3 = false;
	boolean PreviousRace2 = false;
	boolean PreviousRace1 = false;
	boolean FP3Rank = false;
	boolean StartPos = false;
	int Season;
	String GrandPrixSearch;
	
	//SQL Query Variables
	private DatabaseConnection dbConnection;
	private String SqlSelect = "Select ";
	private String SqlTrainingColumns;
	private String SqlTestingColumns;
	private String SqlFrom = " from F1Prediction.DriverRaceWeekend";
	private String SqlInnerJoin = " inner join RaceCalender on RaceCalenderID = RaceCalender.RaceCalenderPrimKey inner join circuits on CircuitID = circuits.CircuitPrimKey";
	
	private String trainingQuery;
	private String testingQuery;
	private String resultsQuery;
	private String driverQuery;
	private String pointsQuery;
	
	//data storing variables
	public String TrainingVar1;
	public String TrainingVar2;
	public String TrainingVar3;
	public String TrainingVar4;
	
	public String TrainingResp = null;
	
	public String TestingVar1 = null;
	public String TestingVar2 = null;
	public String TestingVar3 = null;
	public String TestingVar4 = null;
	
	//general returned storage
	//store strings as 2D double arrays
	private double[][] trainingData;
	private double[][] testingData;
    
	//Data info
	private int TrainingLength = 0;
	private int TestingLength = 0;
	private int columnCount = 0;
	
	//can not be a global varaible as it messes with the GSON parsing
	//private DecimalFormat formatter;
	
	public LinearRegression model;
	
	//return model data
	public StoreModelOutput[] storeOutputData;
    
	public StoreModelOutput[] RunLinearRegression() 
	{
        //DataStoreSetUp(req);
		
		DecimalFormat formatter = new DecimalFormat("#.###");
        
        dataCorrect = ConvertData();
        
        if(dataCorrect)
        {
	        
	        model = new LinearRegression();
	        
	        model.RunLinearRegression(trainingData,testingData);
	        
	        storeOutputData = new StoreModelOutput[model.TestingLength];
	        
	        for(int i=0;i<model.TestingLength;i++)
	        {
	        	storeOutputData[i] = new StoreModelOutput();
	        }
	        
	        
	        int j = 0;
	        
	        for(int i  = 0;i<model.TestingLength;i++)
	        {
	        	System.out.println(model.predictionOutcome[i]);
	        	if(TrainingVar1 != null)
				{
	        		storeOutputData[i].PreviousRace3 = testingData[j][i];
					j++;
				}
				if(TrainingVar2 != null)
				{
					storeOutputData[i].PreviousRace2 = testingData[j][i];
					j++;
				}
				if(TrainingVar3 != null)
				{
					storeOutputData[i].PreviousRace1 = testingData[j][i];
					j++;
				}
				if(TrainingVar4 != null)
				{
					storeOutputData[i].FP3Rank = testingData[j][i];
					j++;
				}
				
				storeOutputData[i].PredictedFinishPos = formatter.format(model.predictionOutcome[i]);
				storeOutputData[i].PredictedFinishPosRanked = model.predictionOutcomeRanked[i];
				j = 0;
	        }
	        
	        storeOutputData[0].Correlation = new String[model.ColumnCount];
	        for(int i =0;i<model.ColumnCount;i++)
	        {
	        	storeOutputData[0].Correlation[i] = formatter.format(model.correlation[i]);
	        }
	        storeOutputData[0].MeanAbsoluteError = formatter.format(model.meanAbsoluteError);
	        storeOutputData[0].RootMeanSquare =formatter.format(model.rootMeanSquaredError);
	        storeOutputData[0].error = errorMsg;
		
	        //log.info("Slope m = " + m +" Intercept b = " + b + " Outcome = " + bar3);
	        	
	        ///greeting.setProperty("content", total);
	        
	        //greeting.setProperty("Slope", m);
	        //greeting.setProperty("Intercept", b);
	        //greeting.setProperty("Outcome", FinalOutcome);
	        
	        
	        //DatastoreService datastore = DatastoreServiceFactory.getDatastoreService();
	        //datastore.put(greeting);
        }
        else
        {
        	//should probably have an error message here that can be returned to the user
        	System.out.println("The user entered incorrect data");
        	storeOutputData = new StoreModelOutput[1];
        	storeOutputData[0] = new StoreModelOutput();
        	storeOutputData[0].error = errorMsg;
        }
        
        return storeOutputData;
    }
	
	public StoreModelOutput[] runRacePrediction()
	{
		//create sql query based on json inputs
		
		DecimalFormat formatter = new DecimalFormat("#.###");
		
		BuildSql();
		
		//Connect to the database
		
		dbConnection = new DatabaseConnection();
		
		dbConnection.query = trainingQuery;
		
		double[][] trainingInputData = dbConnection.getTrainingData(PreviousRace3,PreviousRace2,PreviousRace1,FP3Rank,StartPos);

		dbConnection.query = testingQuery;
        
        double[][] testingInputData = dbConnection.getTestData(PreviousRace3,PreviousRace2,PreviousRace1,FP3Rank,StartPos);
        
        model = new LinearRegression();
        
        if(StartPos == true)
        {
        	model.StartPos = true;
        }
        
        model.RunLinearRegression(trainingInputData,testingInputData);
        
        dbConnection.query = driverQuery;
        String[] drivers = dbConnection.runStringQuery();
        dbConnection.query = resultsQuery;
        double[] results = dbConnection.runDoubleQuery();
        dbConnection.query = pointsQuery;
        double[] points = dbConnection.runDoubleQuery();
        
        storeOutputData = new StoreModelOutput[model.TestingLength];
        
        for(int i=0;i<model.TestingLength;i++)
        {
        	storeOutputData[i] = new StoreModelOutput();
        }
        
        
        int j = 0;
        
        for(int i  = 0;i<model.TestingLength;i++)
        {
        	System.out.println(model.predictionOutcome[i]);
        	if(PreviousRace3)
			{
        		storeOutputData[i].PreviousRace3 = testingInputData[j][i];
				j++;
			}
			if(PreviousRace2)
			{
				storeOutputData[i].PreviousRace2 = testingInputData[j][i];
				j++;
			}
			if(PreviousRace1)
			{
				storeOutputData[i].PreviousRace1 = testingInputData[j][i];
				j++;
			}
			if(FP3Rank)
			{
				storeOutputData[i].FP3Rank = testingInputData[j][i];
				j++;
			}
			if(StartPos)
			{
				storeOutputData[i].StartPos = testingInputData[j][i];
				j++;
			}
			
			storeOutputData[i].ActualFinishPos = results[i];
			storeOutputData[i].PredictedFinishPos = formatter.format(model.predictionOutcome[i]);
			storeOutputData[i].PredictedFinishPosRanked = model.predictionOutcomeRanked[i];
			storeOutputData[i].PredictedFinishPosRankedTable = model.predictionOutcomeRanked[i];
			storeOutputData[i].DriverName = drivers[i];
			storeOutputData[i].CurrentPoints = points[i];
			j = 0;
        }
        
        storeOutputData[0].Correlation = new String[model.ColumnCount];
        for(int i =0;i<model.ColumnCount;i++)
        {
        	storeOutputData[0].Correlation[i] = formatter.format(model.correlation[i]);
        }
        storeOutputData[0].MeanAbsoluteError = formatter.format(model.meanAbsoluteError);
        storeOutputData[0].RootMeanSquare =formatter.format(model.rootMeanSquaredError);
		
		return storeOutputData;
	}
	
	public StoreModelOutput[] updateRacePrediction(StoreModelOutput[] inputData)
	{
		DecimalFormat formatter = new DecimalFormat("#.###");
		
        int j = 0;
        
        //need to check what fields are been used by this predictino
        //loop through data and check if all fields of a given row
        //are equal to 0, otherwise they are been used
        
        TestingLength = inputData.length;
        
        for(int i = 0;i<TestingLength;i++)
        {
        	if(inputData[i].PreviousRace3 != 0 && PreviousRace3 != true)
        	{
        		PreviousRace3 = true;
        		columnCount++;
        	}
        	if(inputData[i].PreviousRace2 != 0 && PreviousRace2 != true)
        	{
        		PreviousRace2 = true;
        		columnCount++;
        	}
        	if(inputData[i].PreviousRace1 != 0 && PreviousRace1 != true)
        	{
        		PreviousRace1 = true;
        		columnCount++;
        	}
        	if(inputData[i].FP3Rank != 0 && FP3Rank != true) 
        	{
        		FP3Rank = true;
        		columnCount++;
        	}
        	if(inputData[i].StartPos != 0 && StartPos != true)
        	{
        		StartPos = true;
        		columnCount++;
        	}
        }
        
        testingData = new double[columnCount][TestingLength];
        
        //if need be for efficiency could use multiple for statements for each if
        
        for(int i  = 0;i<inputData.length;i++)
        {
        	if(PreviousRace3 == true)
			{
        		testingData[j][i] = inputData[i].PreviousRace3;
				j++;
			}
			if(PreviousRace2 == true)
			{
				testingData[j][i] = inputData[i].PreviousRace2;
				j++;
			}
			if(PreviousRace1 == true)
			{
				testingData[j][i] = inputData[i].PreviousRace1;
				j++;
			}
			if(FP3Rank == true)
			{
				testingData[j][i] = inputData[i].FP3Rank;
				j++;
			}
			if(StartPos == true)
			{
				testingData[j][i] = inputData[i].StartPos;
				j++;
			}
			j = 0;
        }
        
        //need to call somesort of SQL statement now to get the data.
        
        BuildSql();
        
        testingQuery = null;
        driverQuery = null;
        resultsQuery = null;
        
        runSQL();
		
        model = new LinearRegression();
        
        if(StartPos == true)
        {
        	model.StartPos = true;
        }
        
        model.RunLinearRegression(trainingData,testingData);
        
        storeOutputData = new StoreModelOutput[model.TestingLength];
        
        for(int i=0;i<model.TestingLength;i++)
        {
        	storeOutputData[i] = new StoreModelOutput();
        }
        
        
        j = 0;
        
        for(int i  = 0;i<model.TestingLength;i++)
        {
        	System.out.println(model.predictionOutcome[i]);
        	if(PreviousRace3)
			{
        		storeOutputData[i].PreviousRace3 = inputData[i].PreviousRace3;
				j++;
			}
			if(PreviousRace2)
			{
				storeOutputData[i].PreviousRace2 = inputData[i].PreviousRace2;
				j++;
			}
			if(PreviousRace1)
			{
				storeOutputData[i].PreviousRace1 = inputData[i].PreviousRace1;
				j++;
			}
			if(FP3Rank)
			{
				storeOutputData[i].FP3Rank = inputData[i].FP3Rank;
				j++;
			}
			if(StartPos)
			{
				storeOutputData[i].StartPos = inputData[i].StartPos;
				j++;
			}
			
			storeOutputData[i].ActualFinishPos = inputData[i].ActualFinishPos;
			storeOutputData[i].PredictedFinishPos = formatter.format(model.predictionOutcome[i]);
			storeOutputData[i].PredictedFinishPosRanked = model.predictionOutcomeRanked[i];
			storeOutputData[i].PredictedFinishPosRankedTable = model.predictionOutcomeRanked[i];
			storeOutputData[i].DriverName = inputData[i].DriverName;
			storeOutputData[i].CurrentPoints = inputData[i].CurrentPoints;	
			j = 0;
        }
        
        storeOutputData[0].Correlation = new String[model.ColumnCount];
        for(int i =0;i<model.ColumnCount;i++)
        {
        	storeOutputData[0].Correlation[i] = formatter.format(model.correlation[i]);
        }
        storeOutputData[0].MeanAbsoluteError = formatter.format(model.meanAbsoluteError);
        storeOutputData[0].RootMeanSquare =formatter.format(model.rootMeanSquaredError);
		
		return storeOutputData;
	}
	
	private void DataStoreSetUp(HttpServletRequest req)
	{
		UserService userService = UserServiceFactory.getUserService();
        user = userService.getCurrentUser();

        // We have one entity group per Guestbook with all Greetings residing
        // in the same entity group as the Guestbook to which they belong.
        // This lets us run a transactional ancestor query to retrieve all
        // Greetings for a given Guestbook.  However, the write rate to each
        // Guestbook should be limited to ~1/second.
		
        guestbookName = ("default");;
        Key guestbookKey = KeyFactory.createKey("Guestbook", guestbookName);
		
		Date date = new Date();
        greeting = new Entity("Greeting", guestbookKey);
        greeting.setProperty("user", user);
        greeting.setProperty("date", date);
	}
    
    private boolean ConvertData()
    {
    	//*************************************************************************
        //Last Edited by: Eoin Farrell - Last Edit Date: 06/02/2014
        //This section is used to take the string parameters from the user
    	//prediction and convert them them into 2 2D arrays
        //*************************************************************************
        
    	//check patterns against a regular expression
    	//that will make sure only numbers and symbols
    	// , . are found in the text    	
    	
    	//declare local variables
        int i = 0;
        int j = 0;
        
        //declare variables for regular expression comparison
        Pattern pattern = Pattern.compile("^[0-9,.]*$");
        
        String[] TrainingVar1Array = null;
        String[] TrainingVar2Array = null;
        String[] TrainingVar3Array = null;
        String[] TrainingVar4Array = null;
        String[] TrainingRespArray = null;
        
        String[] TestingVar1Array = null;
        String[] TestingVar2Array = null;
        String[] TestingVar3Array = null;
        String[] TestingVar4Array = null;

        //need to reset any empty strings to nulls so they can be found in the below if statements
        
        if(TrainingVar1 == "")
        {
        	TrainingVar1 = null;
        }
        if(TrainingVar2 == "")
        {
        	TrainingVar2 = null;
        }
        if(TrainingVar3 == "")
        {
        	TrainingVar3 = null;
        }
        if(TrainingVar4 == "")
        {
        	TrainingVar4 = null;
        }
        
        //if statements to count number of columns,
        //make sure all explanatory and response variables
        //are the same length and that where an explanatory
        //variable is found a response variable must be found
        if(TrainingVar1 != null)
        {
        	TrainingVar1Array = TrainingVar1.split(",");
        	TrainingLength = TrainingVar1Array.length;
        	if(TestingVar1 == null || false == pattern.matcher(TrainingVar1).matches() || false == pattern.matcher(TestingVar1).matches())
        	{
        		errorMsg = "Please recheck the 1st training and testing data inputs";
        		return false;
        	}
        	else
        	{
        		columnCount++;
        		
        		TestingVar1Array = TestingVar1.split(",");
                TestingLength = TestingVar1Array.length;
        	}
        }
        else
        {
        	errorMsg = "Please enter data for the 1st testing variable";
        	return false;
        }
        if(TrainingVar2 != null)
        {
        	TrainingVar2Array = TrainingVar2.split(",");
        	if(TestingVar2 == null || TrainingVar2Array.length != TrainingLength || false == pattern.matcher(TrainingVar2).matches())
        	{
        		errorMsg = "Please recheck the 2nd training and testing data inputs";
        		return false;
        	}
        	else
        	{
        		columnCount++;
        		
        		TestingVar2Array = TestingVar2.split(",");
                
                if(TestingVar2Array.length != TestingLength || false == pattern.matcher(TestingVar2).matches())
                {
                	errorMsg = "Please recheck the 2nd testing data inputs";
                	return false;
                }
        	}
        }
        if(TrainingVar3 != null)
        {
        	TrainingVar3Array = TrainingVar3.split(",");
        	if(TestingVar3 == null || TrainingVar3Array.length != TrainingLength || false == pattern.matcher(TrainingVar3).matches())
        	{
        		errorMsg = "Please recheck the 3rd training and testing data inputs";
        		return false;
        	}
        	else
        	{
        		columnCount++;
        		
        		TestingVar3Array = TestingVar3.split(",");
                
                if(TestingVar3Array.length != TestingLength || false == pattern.matcher(TestingVar3).matches())
                {
                	errorMsg = "Please recheck the 3rd testing data inputs";
                	return false;
                }
        	}
        }
        if(TrainingVar4 != null)
        {
        	TrainingVar4Array = TrainingVar4.split(",");
        	if(TestingVar4 == null || TrainingVar4Array.length != TrainingLength || false == pattern.matcher(TrainingVar4).matches())
        	{
        		errorMsg = "Please recheck the 4th training and testing data inputs";
        		return false;
        	}
        	else
        	{
        		columnCount++;
        		
        		TestingVar4Array = TestingVar4.split(",");
                
                if(TestingVar4Array.length != TestingLength || false == pattern.matcher(TestingVar4).matches())
                {
                	errorMsg = "Please recheck the 4th testing data inputs";
                	return false;
                }
        	}
        }
        if(TrainingResp != null)
        {
        	columnCount++;
        	
        	TrainingRespArray = TrainingResp.split(",");
            
            if(TrainingRespArray.length != TrainingLength || false == pattern.matcher(TrainingResp).matches())
            {
            	errorMsg = "Please recheck training response data inputs";
            	return false;
            }
        }
        else
        {
        	errorMsg = "Please recheck training response data inputs";
        	return false;
        }
        
        trainingData = new double[columnCount][TrainingLength];
        testingData = new double[columnCount-1][TestingLength];
        
        
        //place data into 2d array, if this needs to be fast could
        //use multiple for loops so each if only gets called once
        for(i=0;i<TrainingLength;i++)
        {
        	j=0;
        	trainingData[j][i] = Double.parseDouble(TrainingVar1Array[i]);
        	j++;
        	if(TrainingVar2Array != null)
        	{
        		trainingData[j][i] = Double.parseDouble(TrainingVar2Array[i]);
        		j++;
        	}
        	if(TrainingVar3Array != null)
        	{
        		trainingData[j][i] = Double.parseDouble(TrainingVar3Array[i]);
        		j++;
        	}
        	if(TrainingVar4Array != null)
        	{
	        	trainingData[j][i] = Double.parseDouble(TrainingVar4Array[i]);
	        	j++;
        	}
        	trainingData[j][i] = Double.parseDouble(TrainingRespArray[i]);
        }
        
        for(i=0;i<TestingLength;i++)
        {
        	j=0;
        	testingData[j][i] = Double.parseDouble(TestingVar1Array[i]);
        	j++;
        	if(TrainingVar2Array != null)
        	{
        		testingData[j][i] = Double.parseDouble(TestingVar2Array[i]);
        		j++;
        	}
        	if(TrainingVar3Array != null)
        	{
        		testingData[j][i] = Double.parseDouble(TestingVar3Array[i]);
        		j++;
        	}
        	if(TrainingVar4Array != null)
        	{
	        	testingData[j][i] = Double.parseDouble(TestingVar4Array[i]);
	        	j++;
        	}
        }
        return true;
    }
    
    private void BuildSql()
	{
		if(PreviousRace3)
		{
			if(SqlTrainingColumns == null)
			{
				SqlTrainingColumns = "PreviousRace3";
			}
			else
			{
				SqlTrainingColumns += ", PreviousRace3";
			}
		}
		
		if(PreviousRace2)
		{
			if(SqlTrainingColumns == null)
			{
				SqlTrainingColumns = "PreviousRace2";
			}
			else
			{
				SqlTrainingColumns += ", PreviousRace2";
			}
		}
		
		if(PreviousRace1)
		{
			if(SqlTrainingColumns == null)
			{
				SqlTrainingColumns = "PreviousRace1";
			}
			else
			{
				SqlTrainingColumns += ", PreviousRace1";
			}
		}
		
		if(FP3Rank)
		{
			if(SqlTrainingColumns == null)
			{
				SqlTrainingColumns = "FP3Rank";
			}
			else
			{
				SqlTrainingColumns += ", FP3Rank";
			}
		}
		
		if(StartPos)
		{
			if(SqlTrainingColumns == null)
			{
				SqlTrainingColumns = "StartPos";
			}
			else
			{
				SqlTrainingColumns += ", StartPos";
			}
		}
		
		SqlTestingColumns = SqlTrainingColumns;
		SqlTrainingColumns += ", FinishPos";
		
		trainingQuery = SqlSelect + SqlTrainingColumns + SqlFrom + SqlInnerJoin + " where RaceCalender.Season <> " + Season + " AND circuits.CircuitName <> '" + GrandPrixSearch + "' AND PreviousRace1 <> -1 AND PreviousRace2 <> -1 AND PreviousRace3 <> -1 AND FinishPos <> -1";	
		testingQuery = SqlSelect + SqlTestingColumns + SqlFrom + SqlInnerJoin + " where RaceCalender.Season = " + Season + " AND circuits.CircuitName = '" + GrandPrixSearch + "'";
		resultsQuery = SqlSelect + " FinishPos" + SqlFrom + SqlInnerJoin + " where RaceCalender.Season = " + Season + " AND circuits.CircuitName = '" + GrandPrixSearch + "'";
		driverQuery = SqlSelect + " AbbreviatedName" + SqlFrom + SqlInnerJoin + " inner join Drivers on DriversID = Drivers.DriverPrimKey where RaceCalender.Season = " + Season + " AND circuits.CircuitName = '" + GrandPrixSearch + "'";
		pointsQuery = SqlSelect + " StartingPoints" + SqlFrom + SqlInnerJoin + " where RaceCalender.Season = " + Season + " AND circuits.CircuitName = '" + GrandPrixSearch + "'";
	}
    
    private void runSQL()
    {
    	//connect to the database
    	
    	dbConnection = new DatabaseConnection();
    	
    	
    	if(trainingQuery != null)
    	{
    		dbConnection.query = trainingQuery;
    	
    		trainingData = dbConnection.getTrainingData(PreviousRace3,PreviousRace2,PreviousRace1, FP3Rank,StartPos);
    	}
    	
    	if(testingQuery != null)
    	{
    		dbConnection.query = testingQuery;
    		testingData = dbConnection.getTestData(PreviousRace3,PreviousRace2,PreviousRace1,FP3Rank,StartPos);
    	}
    	
    	if(resultsQuery != null)
    	{
    		dbConnection.query = resultsQuery;
            double[] results = dbConnection.runDoubleQuery();
    	}
    	
    	if(driverQuery != null)
    	{
    		dbConnection.query = driverQuery;
            String[] drivers = dbConnection.runStringQuery();
    	}
    }
}