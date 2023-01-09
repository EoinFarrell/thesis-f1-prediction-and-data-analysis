package prediction;

import static org.junit.Assert.*;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

public class DatabaseConnectionTest
{
	DatabaseConnection TestModel;
	
	@Before
	public void setUp() throws Exception 
	{
		TestModel = new DatabaseConnection();
	}

	@After
	public void tearDown() throws Exception 
	{
		TestModel = null;
	}

	@Test
	public void testDoPostHttpServletRequestHttpServletResponse() 
	{
		
		fail("Not yet implemented"); // TODO
	}
	
	@Test
	public void testConnectToDatabase() 
	{
		TestModel.ConnectToDatabase();
		fail("Not yet implemented"); // TODO
	}

}
