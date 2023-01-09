package prediction;

//*************************************************************************
//Last Edited by: Eoin Farrell - Last Edit Date: 09/02/2014
//This class is used to calculate the correlation between
//two data lists
//*************************************************************************

public class Correlation {
	
	public double PearsonCorrelation(double[] ExplanatoryVar, double[] ResponseVar)
	{
		double Outcome = 0;
		
		//*************************************************************************
        //Last Edited by: Eoin Farrell - Last Edit Date: 12/03/2014
        //This function is used to generate Pearsons Correlation Coefficient
		//r value for two lists of numbers
        //*************************************************************************
        
		int i = 0;
		
		int VarCount = ExplanatoryVar.length;
		
        double sumx = 0;
        double sumy = 0;
        double sumxy = 0;
        double sumx2 = 0;
        double sumy2 = 0;
        
        for(i=0;i<VarCount;i++)
        {
        	sumx += ExplanatoryVar[i];
        	sumy += ResponseVar[i];
        	sumxy += ExplanatoryVar[i] * ResponseVar[i];
        	sumx2 += ExplanatoryVar[i] * ExplanatoryVar[i];
        	sumy2 += ResponseVar[i] * ResponseVar[i];
        }
        
        Outcome = ((VarCount*sumxy)-(sumx*sumy))/(Math.sqrt(((VarCount*sumx2)-(sumx*sumx))*((VarCount * sumy2)-(sumy*sumy))));
		
		return Outcome;
	}
	
	public double[] PearsonCorrelationMatrix(double[][] Matrix)
	{
		double[] ExplanatoryVar = new double[Matrix[0].length];
		double[] ResponseVar = Matrix[Matrix.length-1];
		double[] outcome = new double[Matrix.length];
		
		System.out.println(Matrix.length);
		
		for(int i = 0; i < Matrix.length-1;i++)
		{
			ExplanatoryVar = Matrix[i];
			
			outcome[i] = PearsonCorrelation(ExplanatoryVar,ResponseVar);
			
			ExplanatoryVar = null;
			
			System.out.println("Correlation Var : " + i + " : " + outcome[i]);
		}
		
		return outcome;
	}

}
