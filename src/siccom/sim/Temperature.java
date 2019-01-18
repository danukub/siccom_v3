package siccom.sim;
import java.util.List;
import java.io.File;
import java.io.FileNotFoundException;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Scanner;

import sim.engine.SimState;
import sim.engine.Steppable;

/**
 * This class handles the temperature data
 * The different years of the data set are accessed randomly and the 
 * one extreme year (1998) is accessed when the bleaching interval is reached 
 * @author andreas
 *
 */

public class Temperature implements Steppable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 434562251L;

	/**
	 * The main simulation
	 */
	Siccom sim;
	/**
	 * The name of the input file
	 */
	String fileName;
	/**
	 * Scanner to read in the data
	 */
	Scanner scan;
	/**
	 * Scanner to read in the lines separately
	 */
	Scanner lineScan;
	/**
	 * Daily temperature data for a year
	 */
	ArrayList<Double> yearData;
	/**
	 * Storage for all yearly temperature data sets
	 */
	Hashtable<String, ArrayList<Double>> tempData = new Hashtable<String, ArrayList<Double>>();
	/**
	 * The year name
	 */
	private String yearString;
	/**
	 * Determines if this is the first year
	 */
	boolean firstYear=true;
	/**
	 * The lowest value for year of the data set
	 */
	int minYear;
	/**
	 * The highest value for year of the data set
	 */
	int maxYear;
	/**
	 * The days each month of the year has
	 */
	final int[] daysPerMonth = {31, 28, 31, 30, 31, 30, 31, 31, 30, 31, 30, 31};
	/**
	 * The glider which is used to calculate temperature sums
	 */
	List<Double> glider = new ArrayList<Double>();
	/**
	 * The number of days in a year, already over
	 */
	int oldDays;
	/**
	 * All old days including the ones of the focal month
	 */
	int allDays = 0;
	/**
	 * The temperature sum of the last day of the last measurement
	 */
	double oldTemp=28;
	/**
	 * Temperature threshold from which on over-temperatures are summed up
	 */
	public double longTermSummerMeanTemp;
	/**
	 * The over temperature
	 */
//	double degreeHeatingDays = 0;
	/**
	 * Temperatures of the consecutive hot days
	 */
	ArrayList<Double> consecList = new ArrayList<Double>();
	/**
	 * The iterator for the glider
	 */
	int tempIterator = 0;
	/**
	 * The actual year the data is taken from
	 */
	private int year;
	/**
	 * The total sum of exceeding temperature 
	 */
	private double totalTemp = 0;
	/**
	 * The total sum of temperatures divided by the days
	 */
	public double meanTemp;
	/**
	 * A yearly data set 
	 */
	private ArrayList<Double> tempList;
	/**
	 * Stores the temperature values for the calculation
	 */
	private ArrayList<Double> movingWindow = new ArrayList<Double>();
	/**
	 * Determines how many days are taken for the calculation
	 */
	private int movWindowValues = 120;

//	private double yearlyTempIncrease = sim.totalTempIncrease/sim.increaseYears;
	private int incCount=0;

	private Boolean gradualTempIncrease;

	double yearlyTempIncrease;
	
	double minTempDataValue=0;

	private List<Double> longTermMeans;
	private List<ArrayList<Double>> longTermData = new ArrayList<ArrayList<Double>>(10);

	public int daysOverLMST;
	public double degreeHeatingDays;
	public double heatRate;

	public Temperature(String fileName, Siccom sim, Boolean gradualTempIncrease, double yearlyTempIncrease)
	{
		this.fileName = fileName;
		File file = new File(Config.getLocalFileName(fileName));
		try {
			scan = new Scanner(file);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}

		this.sim = sim;
		
		this.gradualTempIncrease = gradualTempIncrease;
		this.yearlyTempIncrease = yearlyTempIncrease;
		
		readTempFile();
		setupFirstLongTerm();
		
		for (int i=0; i<movWindowValues; i++)
				movingWindow.add(i, 28.0);
		
		for (int i=0; i<120; i++)
			glider.add(i, 28.0);
		
		year = (int)(sim.random.nextDouble() * (maxYear - minYear +1) + minYear);
		
		longTermSummerMeanTemp = 29;
	}
	
	/**
	 * Read the temperature file
	 */
	void readTempFile()
	{
		while (scan.hasNextLine())
		{
			String line = scan.nextLine();
			lineScan = new Scanner(line);
			if (!(line.startsWith("#")))  
			{
				yearData = new ArrayList<Double>();
				yearString = lineScan.next()+"";
				if (firstYear) 
				{
					minYear = Integer.parseInt(yearString);
					firstYear = false;
				}
				tempData.put(yearString, yearData);

				for (int i=0; i<365; i++)
				{
					double t = Double.parseDouble(lineScan.next());
					yearData.add(t);
				}					
				
			}
		}
		maxYear = Integer.parseInt(yearString);
	}
	
	void setupFirstLongTerm()
	{
		for(int i=1; i<=sim.longTermYears; i++)
		{
			int y =  (int)(sim.random.nextDouble() * (maxYear - minYear +1) + minYear);
			while (y == 1998) y = (int)(sim.random.nextDouble() * (maxYear - minYear +1) + minYear);
			ArrayList<Double> ltd = tempData.get(Integer.toString(y));
			longTermData.add(ltd);
			
		}
	}
	
	private double getLongTermMeanMax()
	{
		longTermMeans= new ArrayList<Double>();
		double tempSum=0;
		int div=0;
		double mean=0;
		double max=0;
		for(int j=0; j<365; j++)
		{
			for(int i=0; i<longTermData.size(); i++)
			{
				double v = longTermData.get(i).get(j);
				if(v > 0)
				{
					tempSum += v;
					div++;
				}
			}
			mean = tempSum/div;
			tempSum=0;
			div=0;
			if(mean>max) max=mean;
			longTermMeans.add(j, mean);
		}
		/* 
		 *  because summer on the Southern Hemisphere is from December
		 *  to May, the first five and the last month are used for 
		 *  calculating the long term summer mean temperature. 
		 */
		for(int j=0; j<151; j++)
		{
			double v = longTermMeans.get(j);
			if(v > 0)
			{
				tempSum += v;
				div++;
			}	
		}
		
		for(int j=334; j<365; j++)
		{
			double v = longTermMeans.get(j);
			if(v > 0)
			{
				tempSum += v;
				div++;
			}	
		}
		mean = tempSum/div;
		return mean;
		
	}
	
	private void adjustLongTermData()
	{
		longTermData.remove(0);
		if(gradualTempIncrease)
		{
			ArrayList<Double> newYearData = new ArrayList<Double>(365);
			
			for(int i=0; i<yearData.size(); i++)
			{
				double v = yearData.get(i) + (yearlyTempIncrease*incCount);
				newYearData.add(i, v); 
			}
			yearData=newYearData;
		
		}
		longTermData.add(yearData);		
	}
	
	/**
	 * Choose a yearly data set
	 */
	public ArrayList<Double> randomize()
	{
		year = (int)(sim.random.nextDouble() * (maxYear - minYear +1) + minYear);
		return tempData.get(year+"");
	}
	/**
	 * Choose the extreme temperature data set
	 */
	public ArrayList<Double> elNino()
	{
		return tempData.get("1998");
	}
	
	
	/**
	 * adjust the glider for the next calculation
	 * @param days
	 * @param yD
	 */
	public void glide(int days, ArrayList<Double> yD)
	{
		oldDays = allDays;
		allDays += days;
		
		for (int i=oldDays; i<allDays; i++) 
		{
			glider.add(yD.get(i));
			glider.remove(0);
		}
		
		daysOverLMST=0;
		degreeHeatingDays=0;
		heatRate=0;
		if(!sim.constantTemperature || sim.bleachIsOn)
		{
			for (double t : glider)
			{
	
//				if (t< (24.5 + (yearlyTempIncrease*incCount))) t=24.5 + (yearlyTempIncrease*incCount);
				if (gradualTempIncrease == true) t = t + (yearlyTempIncrease*incCount);
				if (t > longTermSummerMeanTemp)
				{
					degreeHeatingDays += (t - longTermSummerMeanTemp);
					daysOverLMST++;
				}
	//			if (overTemp > maxOverTemp) maxOverTemp = overTemp;
				
			}
		}
		
		heatRate = degreeHeatingDays/daysOverLMST;
		if (Double.isNaN(heatRate)) heatRate = 0;
		sim.overTempPerDay = degreeHeatingDays / glider.size();
					
	}

	
	@Override
	/**
	 * The step routine for the Temperature object
	 * @param state
	 */
	public void step(SimState state) 
	{

		int yearly = (int)(sim.schedule.getSteps()%12);
		int bleacho;
		if(sim.bleachInterval!=0) bleacho = (int)(sim.schedule.getSteps()%sim.bleachInterval);
		else bleacho = 99999;
		if ( yearly == 0 ) 
		{ 
			incCount++;
			oldDays = 0;
			allDays = 0;
			// at the beginning of each year we choose a year data file
			// --> here we choose between all files but the one with elNino data
			// just if 'bleacho' is true the elNino year is chosen

			{
				if (!(sim.schedule.getSteps() == 0) && bleacho == (sim.firstBleach*12))
				{
					sim.bleachIsOn=true;
					tempList = tempData.get("1998");
					yearData = tempList;
	//				System.out.print("\n############################   NEW YEAR  " + incCount + " ###########################\n");
					System.out.println("1998 The Bleacho\t");
					
				}
				else
				{			
					sim.bleachIsOn=false;
					int tempDataYear = (int)(sim.random.nextDouble() * (maxYear - minYear +1) + minYear);
	
					// to make sure that 1998 is not chosen outside the bleaching interval
					while (tempDataYear == 1998) tempDataYear = (int)(sim.random.nextDouble() * (maxYear - minYear +1) + minYear);
					
					tempList = tempData.get(tempDataYear+""); 
					yearData = tempList;
	
					System.out.println(tempDataYear+"\t");
				}	
			}
			adjustLongTermData();
			longTermSummerMeanTemp = getLongTermMeanMax();
			
		}
		
		int daysOfMonth = daysPerMonth[yearly];
		glide(daysOfMonth, tempList);

		// Calculate the mean temperature for the month
		for(int l=glider.size()-daysOfMonth; l<glider.size(); l++)
		{
			double t = glider.get(l);
			if (t<24.5) t=24.5;
			if (gradualTempIncrease == true) t = t + yearlyTempIncrease*incCount;
			totalTemp += t;
		}
		if(!sim.constantTemperature || sim.bleachIsOn) meanTemp = totalTemp/(daysOfMonth);
		else meanTemp = 27;
		totalTemp=0;
		allDays = oldDays + daysOfMonth;
		reschedule(1.0);
	}
	
	/**
	 * Reschedule for the next time step (month)
	 * @param dTime
	 */
	public final void reschedule(double dTime) 
	{
		if (dTime <= 0.0) {
			step(sim);
		}
		sim.schedule.scheduleOnceIn(dTime, this);
	}
	
}
	
	
