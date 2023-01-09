package prediction;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;
import org.junit.runners.Suite.SuiteClasses;

@RunWith(Suite.class)
@SuiteClasses(
	{ 
		CorrelationTest.class, 
		JsonLinearRegressionTest.class,
		LinearRegressionTest.class,
		DatabaseConnectionTest.class
	})
public class AllTests 
{

}
