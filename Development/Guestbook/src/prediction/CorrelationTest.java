package prediction;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class CorrelationTest 
{
	Correlation TestModel;
	
	@Before
	public void setUp() throws Exception 
	{
		TestModel = new Correlation();
	}

	@After
	public void tearDown() throws Exception 
	{
		TestModel = null;
	}

	@Test
	public void testPearsonCorreclation() 
	{
		double[] ExplanatoryVar = {1,2,5,4,10,15,7,8,6,3,11,18,9,12,17,16,13,14,22,21,19,20};
		double[] ResponseVar = {3,6,2,4,9,11,1,10,20,5,22,13,8,7,21,14,12,19,16,18,15,17};
		
		double Outcome = TestModel.PearsonCorreclation(ExplanatoryVar, ResponseVar);
		
		double OutcomeTest = 0.675889328;
		
		assertEquals(OutcomeTest,Outcome,0.000000001);
	}

}
