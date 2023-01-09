/**
 * 
 */
package prediction;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

/**
 * @author Eoin Farrell
 *
 */
public class LinearRegressionTest {

	LinearRegression TestModel;
	
	/**
	 * @throws java.lang.Exception
	 */
	@Before
	public void setUp() throws Exception {
		
		TestModel = new LinearRegression();
	}

	/**
	 * @throws java.lang.Exception
	 */
	@After
	public void tearDown() throws Exception {
		
		TestModel = null;
	}

	/**
	 * Test method for {@link prediction.LinearRegression#RunLinearRegression(float[], float[], float[])}.
	 */
	@Test
	public void testRunLinearRegression() 
	{	
		double[] TrainingVar = {1,2,3};
		double[] TrainingExp = {10,20,30};
		double[] TestingVar = {5,6,7};
		String[] Outcome = TestModel.RunLinearRegression(TrainingVar, TrainingExp, TestingVar, false);
		
		String[] OutcomeTest = {"50.0","60.0","70.0","Training Length: 3","Testing Length: 3","Slope: 10.0","Intercept: 0.0"};
		
		assertArrayEquals(OutcomeTest,Outcome);
	}
	
	@Test
	public void testRankResults()
	{
		double[] TrainingVar = {1,2,3};
		double[] TrainingExp = {10,20,30};
		double[] TestingVar = {5,7,3};
		String[] Outcome = TestModel.RunLinearRegression(TrainingVar, TrainingExp, TestingVar, true);
		
		String[] OutcomeTest = {"2.0","3.0","1.0","Training Length: 3","Testing Length: 3","Slope: 10.0","Intercept: 0.0"};
		
		assertArrayEquals(OutcomeTest,Outcome);
	}

}
