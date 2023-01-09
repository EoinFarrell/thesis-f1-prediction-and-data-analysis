package prediction;

import java.util.ArrayList;

import org.apache.commons.math3.linear.Array2DRowRealMatrix;
import org.apache.commons.math3.linear.LUDecomposition;
import org.apache.commons.math3.linear.RealMatrix;

public class LinearRegression 
{
	private int TrainingLength = 0;
	public int TestingLength = 0;
	public int RowCount;
	public int ColumnCount;
	
	public double[] m;
	private double[] b;
	public double b0 = 0;
	private double sumx = 0;
	private double sumy = 0;
	private double sumxy = 0;
	private double sumx2 = 0;
	private double sumy2 = 0;
	
	//this is used to tell if the start position is been used in the prediction so that 
	//if it equals to 0 then the prediction will be 0
	public boolean StartPos = false;
	
	public double[] correlation;
	public double kFoldError = 0;
	public double rootMeanSquaredError = 0;
	public double meanAbsoluteError = 0;
	public double[] predictionOutcome;
	public double[] predictionOutcomeRanked; 
	public double[] liklyHood; 
	
	private double[][] MatrixOutput;
	
	public void RunLinearRegression(double[][] trainingData,double[][] testingData)
	{
		getDataLength(trainingData,testingData);
		
		m = new double[ColumnCount];
		b = new double[ColumnCount];
		
		//CreateMatrix(trainingData);
		//System.out.println(MatrixOutput);
		
		//get correlation between explantory vars and response var
		Correlation pCorrelation = new Correlation();
		
		correlation = pCorrelation.PearsonCorrelationMatrix(trainingData);
		
		//possible to run k-fold validation here
		//Stopped developing this due to time constraints and limited improvement
		//if gone furthure would have needed more information so it could determine
		//at what stage in the dataset, it switched from 24 drivers per race to 
		//22 drivers
		//F1kFoldValidation(trainingData);
		
		kFoldValidation(trainingData);
		
		//need to recreate the model now using the full suite of training data not broken up
		getDataLength(trainingData, testingData);
		
		generateModel(trainingData);
		
		evaluateModel(testingData);
        
        //rank the results
        predictionOutcomeRanked = RankResults(predictionOutcome);
        
        getliklyHood(trainingData,testingData);
	}
	

	private double[] RankResults(double[] Rresults)
	{
		//*************************************************************************
        //Last Edited by: Eoin Farrell - Last Edit Date: 27/01/2014
        //The response variables from above are ranked for the output
        //*************************************************************************
        
		int i = 0;
		int j = 0;
		int count = 1;
		double[] ranking = new double[TestingLength];
		boolean checkStart = false;
		
        for(i = 0;i<TestingLength;i++)
        {
        	count = 1;
        	checkStart = false;
        	
        	for(j = 0;j<TestingLength;j++)
        	{
        		if(Rresults[i] != 0)
        		{
	        		if(Rresults[i] > Rresults[j] && Rresults[j] != 0)
	        		{
	        			count++;
	        		}
        		}
        		else
        		{
        			checkStart = true;
        		}
        	}
        	
        	if(!checkStart)
        	{
        		ranking[i] = count;
        	}
        	else
        	{
        		ranking[i] = 0;
        	}
        }
        
        return ranking;
	}
	
	private void CreateMatrix(double[][] trainingData)
	{
		double[][] trainingDataTranspose = new double[RowCount][ColumnCount];
		
		/* could not get transpose to work so using library for it
		for(i=0;i<RowCount;i++)
		{
			for(j=0;j<ColumnCount;j++)
			{
				XMatrixTranspose[i][j] = XMatrix[j][i];
			}
		}*/
		
		RealMatrix RealXMatrix = new Array2DRowRealMatrix(trainingData);
		
		RealMatrix RealMatrixTranspose = RealXMatrix.transpose();
		
		//Multiply XMatrix by its transpose matrix
		//No need to check if matrices have same column and row
		//length as transposed means always will be true
		//Cik = ForEach j AijBjk
		
		/*for(i=0;i<RowCount;i++)
		{
			for(j=0;j<RowCount;j++)
			{
				for(int k=0;k<ColumnCount;k++)
				{
					XMatrixByTranspose[i][j] +=  XMatrix[i][k] * XMatrixTranspose[k][j];
				}
			}
		}*/
		
		RealMatrix RealMatrixByTranspose = RealXMatrix.multiply(RealMatrixTranspose);
		
		//get the inverse of the matrix from the previous step
		//the sides should be equal so no need to check in this case
		//first need to get determinant using below formula
		//|A| = ForEach ( + ) A1qA2rA3s . . . Anz
		//I have used a library for this as matrix size is too large
		//to allow me to do it by hand
		
		//RealMatrix RealMatrixTranspose = new Array2DRowRealMatrix(XMatrixTranspose);
		//RealMatrix RealMatrixByTranspose = new Array2DRowRealMatrix(XMatrixByTranspose);
		
		MatrixOutput = RealMatrixByTranspose.getData();
		
		System.out.println(MatrixOutput);
		
		RealMatrix Inverse = new LUDecomposition(RealMatrixByTranspose).getSolver().getInverse();
		
		RealMatrix temp = Inverse.multiply(RealMatrixTranspose);
		RealMatrix temp1 = new Array2DRowRealMatrix(trainingData[ColumnCount]);
		
		RealMatrix Output = temp.multiply(temp1);
		
		MatrixOutput = Output.getData();
		
	}
	
	private void generateModel(double[][] trainingData)
	{
		//*************************************************************************
        //Last Edited by: Eoin Farrell - Last Edit Date: 11/03/2014
        //This section is used to calculate the slope and the intercept of 
        //the line for Linear Regresssion.
        //Slope = m
        //Intercept = b
        //*************************************************************************
		
		//reset to zero
		for(int i =0;i<ColumnCount;i++)
		{
			m[i] = 0;
			b[i] = 0;
		}
		
		b0 = 0;
		
		for(int i = 0;i<ColumnCount;i++)
		{
			
			double averagex = 0;
			double averagey = 0;
			
			double DifferenceToMeanx = 0;
			double DifferenceToMeany = 0;
			double DifferenceToMeanx2 = 0;
			double DifferenceXByY = 0;
			
			sumx = 0;
			sumy = 0;
			sumxy = 0;
			sumx2 = 0;
			sumy2 = 0;
			
	        for(int j = 0;j<TrainingLength;j++)
	        {
	        	sumx += trainingData[i][j];
	        	sumy += trainingData[ColumnCount][j];
	        	sumxy += trainingData[i][j] * trainingData[ColumnCount][j];
	        	sumx2 += trainingData[i][j] * trainingData[i][j];
	        	sumy2 += trainingData[ColumnCount][j] * trainingData[ColumnCount][j];
	        }
	        
			averagex = sumx / TrainingLength;
	        averagey = sumy / TrainingLength;
	        
	        for(int j = 0;j<TrainingLength;j++)
	        {
	        	DifferenceToMeanx2 += (trainingData[i][j] - averagex) * (trainingData[i][j] - averagex);
	        	DifferenceXByY += (trainingData[i][j] - averagex) * (trainingData[ColumnCount][j] - averagey);
	        }
	        
	        //this is a previous method I was using
	        //this method is using the prince goerge college method
	        //a pdf i found online
	        //it worked but decided to change, since other method seems to be
	        //the exact formula in most papers found online
	        //m[i] = ((TrainingLength * sumxy) - (sumx * sumy)) / ((TrainingLength * sumx2) - (sumx * sumx));
	        //b[i] = ((sumy * sumx2) - (sumx * sumxy)) / ((TrainingLength * sumx2) - (sumx * sumx));
	        
	        //this method is from simple linear regression http://www.ccsr.ac.uk/publications/teaching/mlr.pdf
	        m[i] = DifferenceXByY/DifferenceToMeanx2;
	        b[i] = averagey - (m[i] * averagex);
	        
	        b0 += b[i];
		}
	}
	
	private void getDataLength(double[][] trainingData,double[][] testingData)
	{
    	TrainingLength = trainingData[0].length;
    	TestingLength = testingData[0].length;
    	ColumnCount = testingData.length;
	}
	
	private void F1kFoldValidation(double[][] trainingData)
	{
		//need to first divide up the data
		
		int kFoldLengthTest = 24;
		int kFoldLengthTrain = trainingData[0].length - kFoldLengthTest;
		
		int lowerCount = 0;
		int higherCount = kFoldLengthTest;
		
		double error = 0;
		
		double[][] tempTrainingData = new double[trainingData.length][kFoldLengthTrain];
		double[][] tempTestingData = new double[trainingData.length-1][kFoldLengthTest];
		double[] testOriginalResults = new double[kFoldLengthTest];
		
		//maybe possibly generate some sql query here that can count out 65
		//and also figure out when it changes to 22 drivers a season
		//which at the moment happens at race 49
		
		for(int k = 0;k<65;k++)
		{
			
			for(int i = 0;i<lowerCount;i++)
			{
				for(int j=0;j<trainingData.length;j++)
				{
					tempTrainingData[j][i] = trainingData[j][i];
				}
			}
			
			for(int i = lowerCount;i < higherCount ;i++)
			{
				for(int j=0;j < trainingData.length;j++)
				{
					if(j != trainingData.length - 1)
					{
						tempTestingData[j][i-lowerCount] = trainingData[j][i];
					}
					else
					{
						testOriginalResults[i-lowerCount] = trainingData[j][i];
					}
				}
			}
			
			for(int i = higherCount;i<trainingData[0].length;i++)
			{
				for(int j=0;j<trainingData.length;j++)
				{
					tempTrainingData[j][i-higherCount] = trainingData[j][i];
				}
			}
			
			getDataLength(tempTrainingData, tempTestingData);
			
			generateModel(tempTrainingData);
			
			evaluateModel(tempTestingData);
			
	        predictionOutcomeRanked = RankResults(predictionOutcome);
			
			//now need to compare predicted results against real results
			
			for(int i=0;i<kFoldLengthTest;i++)
			{
				error += (testOriginalResults[i] - predictionOutcomeRanked[i]) * (testOriginalResults[i] - predictionOutcomeRanked[i]);
				meanAbsoluteError += Math.abs(testOriginalResults[i] - predictionOutcomeRanked[i]);
			}
			
			kFoldError += error/kFoldLengthTest;
			rootMeanSquaredError = Math.sqrt(error/kFoldLengthTest);
			meanAbsoluteError = meanAbsoluteError / kFoldLengthTest;
			
			System.out.println("Stage Error " + k + " : " + error/kFoldLengthTest + " RMSE : " + rootMeanSquaredError + " MAE : " + meanAbsoluteError);
			//loop through that process for k
			
			if(k == 48)
			{
				kFoldLengthTest = 22;
				kFoldLengthTrain = trainingData[0].length - kFoldLengthTest;
				tempTrainingData = new double[trainingData.length][kFoldLengthTrain];
				tempTestingData = new double[trainingData.length-1][kFoldLengthTest];
				testOriginalResults = new double[kFoldLengthTest];
				lowerCount += 24;
				higherCount += 22;
			}
			else
			{
				lowerCount += kFoldLengthTest;
				higherCount += kFoldLengthTest;
			}
			error = 0;
		}
		
		kFoldError = kFoldError / 10;
		
		System.out.println("Overall Error : " + kFoldError + " RMSE : " + rootMeanSquaredError + " MAE : " + meanAbsoluteError);
		
		//then average up the results
	}
	
	private void kFoldValidation(double[][] trainingData)
	{
		//need to first divide up the data
		
		int kFoldLengthTest = trainingData[0].length / 10;
		int kFoldLengthTrain = trainingData[0].length - kFoldLengthTest;
		
		int lowerCount = 0;
		int higherCount = kFoldLengthTest;
		
		double error = 0;
		double meanAbsoluteErrortemp = 0;
		
		double[][] tempTrainingData = new double[trainingData.length][kFoldLengthTrain];
		double[][] tempTestingData = new double[trainingData.length-1][kFoldLengthTest];
		double[] testOriginalResults = new double[kFoldLengthTest];
		
		//runs xvalidation 10 times
		for(int k = 0;k<10;k++)
		{	
			//chooses training data that is before starting point of testing data
			for(int i = 0;i<lowerCount;i++)
			{
				for(int j=0;j<trainingData.length;j++)
				{
					tempTrainingData[j][i] = trainingData[j][i];
				}
			}
			
			//gets the testing fold
			for(int i = lowerCount;i < higherCount ;i++)
			{
				for(int j=0;j < trainingData.length;j++)
				{
					//generates testing fold, while saving its results
					if(j != trainingData.length -1)
					{
						tempTestingData[j][i-lowerCount] = trainingData[j][i];
					}
					else
					{
						testOriginalResults[i-lowerCount] = trainingData[j][i];
					}
				}
			}
			
			//chooses training data that is after the finish point of testing data
			for(int i = higherCount;i<trainingData[0].length;i++)
			{
				for(int j=0;j<trainingData.length;j++)
				{
					tempTrainingData[j][i-higherCount] = trainingData[j][i];
				}
			}
			
			getDataLength(tempTrainingData, tempTestingData);
			
			generateModel(tempTrainingData);
			
			evaluateModel(tempTestingData);
			
	        predictionOutcomeRanked = RankResults(predictionOutcome);
			
			//now need to compare predicted results against real results
			//Formulas for various error measurements 
			for(int i=0;i<kFoldLengthTest;i++)
			{
				error += (testOriginalResults[i] - predictionOutcome[i]) * (testOriginalResults[i] - predictionOutcome[i]);
				meanAbsoluteErrortemp += Math.abs(testOriginalResults[i] - predictionOutcome[i]);
			}
			
			kFoldError += error/kFoldLengthTest;
			rootMeanSquaredError += Math.sqrt(error/kFoldLengthTest);
			meanAbsoluteError += meanAbsoluteErrortemp / kFoldLengthTest;
			
			System.out.println("Stage Error " + k + " : " + error/kFoldLengthTest + " RMSE : " + Math.sqrt(error/kFoldLengthTest) + " MAE : " + meanAbsoluteErrortemp / kFoldLengthTest);
			//loop through that process for k
			lowerCount += kFoldLengthTest;
			higherCount += kFoldLengthTest;
			error = 0;
			meanAbsoluteErrortemp = 0;
		}
		
		//get average error results
		kFoldError = kFoldError / 10;
		rootMeanSquaredError = rootMeanSquaredError / 10;
		meanAbsoluteError = meanAbsoluteError / 10;
		
		System.out.println("Overall Error : " + kFoldError + " RMSE : " + rootMeanSquaredError + " MAE : " + meanAbsoluteError);
	}
	
	private void evaluateModel(double[][] testingData)
	{
		//*************************************************************************
        //Last Edited by: Eoin Farrell - Last Edit Date: 11/03/2014
        //Now that we have the slope and intercept, next step is to pass 
        //the testing data through the formula y=mx+b where m and b are equal
        //to the results from above and x is the passed in testing data to
        //give a response variable = y
        //*************************************************************************
		
        double RegressionModel;
        predictionOutcome = new double[TestingLength];
        
        b0 = b0/ColumnCount;
        
        //this is used to multiply each testing data point by each explanatory
        //variable slope, and then it divides by the number of explanatoy slopes.
        //Thus it essentially gets the average slope of the five pieces of data
        for(int i=0;i<TestingLength;i++)
        {
        	RegressionModel = 0;
        	
        	for(int j = 0;j<ColumnCount;j++)
        	{
        		if(StartPos == false)
        		{
        			RegressionModel += testingData[j][i] * m[j];
        		}
        		else
        		{
        			if(j != ColumnCount-1 || testingData[j][i] != 0)
        			{
        				RegressionModel += testingData[j][i] * m[j];
        			}
        			else
        			{
        				RegressionModel = 0;
        			}
        		}
        		
        	}
        	
        	if(RegressionModel != 0)
        	{
        		RegressionModel = RegressionModel/ColumnCount;
        		predictionOutcome[i] = b0 + RegressionModel;
        	}
        }
	}
	
	private void getliklyHood(double[][] trainingData, double[][] testingData)
	{
		double[] compareTesting = new double[ColumnCount];
		double[] compareTraining = new double[ColumnCount];
		ArrayList<Double> trainingResults = new ArrayList<Double>();
		boolean check = true;

		float correctCount = 0;
		liklyHood = new double[TestingLength];
		
		for(int i = 0;i<TestingLength;i++)
		{
			//first need to get the first drivers test results
			for(int j = 0;j<ColumnCount;j++)
			{
				compareTesting[j] = testingData[j][i];
			}
			
			//loop through trainingData
			for(int j = 0;j<TrainingLength;j++)
			{
				check = true;
				
				//get first row of TrainingData
				for(int k = 0;k<ColumnCount;k++)
				{
					compareTraining[k] = trainingData[k][j];
				}
				
				//compare rows of training and test data
				for(int k=0;k<ColumnCount;k++)
				{
					if(compareTraining[k] != compareTesting[k])
					{
						k = ColumnCount;
						check = false;
					}
				}
				
				if(check == true)
				{
					trainingResults.add(new Double(trainingData[ColumnCount][j]));
				}
			}
			
			//this is where the percentage calculation should go
			
			for(int j=0;j<trainingResults.size();j++)
			{
				if(trainingResults.get(j) == testingData[ColumnCount-1][i])
				{
					correctCount++;
				}
			}
			
			if(trainingResults.size() == 0)
			{
				liklyHood[i] = 0;
			}
			else
			{
				liklyHood[i] = (correctCount/trainingResults.size()) * 100;
			}
			trainingResults.clear();
			correctCount = 0;
		}
		
		for(int i =0;i<TestingLength;i++)
		{
			System.out.println("LiklyHood: " + liklyHood[i]);
		}
	}
}
