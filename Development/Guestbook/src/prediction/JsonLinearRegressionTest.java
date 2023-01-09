package prediction;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class JsonLinearRegressionTest 
{

	JsonLinearRegression TestModel;
	
	@Before
	public void setUp() throws Exception 
	{
		TestModel = new JsonLinearRegression();
	}

	@After
	public void tearDown() throws Exception 
	{
		TestModel = null;
	}

	@Test
	public void testRunLinearRegression() 
	{
		TestModel.TrainingVar1Str = "1,2,3,4,5";
		TestModel.TrainingReStr= "10,40,70,100,130";
		TestModel.TestingVar1Str="6,7,8,9";
		
		String Outcome = TestModel.RunLinearRegression();
		
		String OutcomeTest = "160.0,190.0,220.0,250.0,Training Length: 5,Testing Length: 4,Slope: 30.0,Intercept: -20.0";
		
		assertEquals(OutcomeTest,Outcome);
		
		//fail("Not yet implemented"); // TODO
	}

}
