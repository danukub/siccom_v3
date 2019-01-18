/**
 * 	siccom -- Spatial Interaction in Coral Reef Communities 
 * 	Simulation of a virtual coral reef where corals and algae compete for space.
 *  In the beginning corals and algae are initialized and placed randomly on the simulation area.
 *	Both grow with distinct growth rates which can be altered in the course of interaction with other simulation objects.
 *  Both recruit at certain time intervals 	-- corals every 12 month, algae every 6 month.  
 *  Algae fragtate if they reach a certain height and can produce 0 to 2 fragments per cycle. 
 *  If they fragtate their height is reduced by 1. If they reach a certain age they die.
 *  Disturbance occurs in random time intervals with random sizes. Then all corals and algae within a certain distance of the disturbance center die at once.
 */


package siccom.sim;

import java.io.File;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.Locale;

import sim.engine.Schedule;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.field.continuous.Continuous2D;
import sim.field.grid.SparseGrid2D;
import sim.util.Bag;
import sim.util.Double2D;
import ec.util.MersenneTwisterFast;

public class Siccom extends SimState 
{
	/**
	 * This is the class which contains the main model 
	 * It can be run as it is or via {@link SiccomUI} with a GUI
	 * @author kubicek
	 * @version 1.0
	 */
	private static final long serialVersionUID = -3342324L;
	
	
	/*
	 * VARIABLES	
	 */
	
	Siccom sim = this;		
	
	
	static long startTime;

	
	/**
	 * The count for steps (month)
	 */
	private int steps;
	/**
	 * The configuration class in which parameters are read into the program and coral groups are set up
	 */
	Config conf;
	
	/**
	 * IF true --> the simulation is started from the GUI
	 */
	public static boolean gui = false;						

	// Main variables for the simulation field
	/**
	 * The discretization of the simulation 
	 */
	public double discretization = 10.0;
	/**
	 * The simulation area width in meters
	 */
	public static double areaWidth;	
	/**
	 * The simulation area height in meters
	 */
	public static double areaHeight;	
	/**
	 * The simulation area width in pixels
	 */
	public static double gridWidth;			
	/**
	 * The simulation area height in pixels
	 */
	public static double gridHeight;	
	/**
	 * Total area of the simulation field
	 */
	public static double totalArea;		
	/**
	 * The used resolution -- how many centimeters per pixel
	 */
	public static double resolution; 
	/**
	 * Converter from meters to pixels
	 */
	public static double dimensionConv_meters;	
	/**
	 * Converter from mm/year to pixels/month -- for coral growth rates
	 */
	public static double dimensionConv_milimeters;	
	
	// TEMPERATURE
	/**
	 * Evokes {@link Temperature}
	 */
	public Temperature temp;
	/**
	 * The daily exceeding temperature over the standard mean within 
	 * one temperature measurement 
	 */
	public double overTempPerDay;

	// ArrayLists for groups -- for initialisation
	/**
	 * List of {@link MassiveGroup}s
	 */
	public ArrayList<MassiveGroup> maCoGroups;
	/**
	 * List of {@link BranchingGroup}s
	 */
	public ArrayList<BranchingGroup> braCoGroups;

	
	// Hash tables for corals, algae
	/**
	 * The 2D-Layer for {@link MassiveCoral}s of all Massive Coral Groups
	 */
	public Continuous2D massiveCorals;
	/**
	 * The 2D-Layer for {@link BranchingCoral}s of all Branching Coral Groups
	 */
	public Continuous2D branchingCorals;
	/**
	 * The 2D-Layer for {@link Alga} 
	 */
	public Continuous2D algae;
	
	 
	// CORALS -- GENERAL
	/**
	 * The maximum radius at initialisation of a coral
	 */
	private final double cInitMaxR = 40.0;
	/**
	 * The maximum radius of a coral 
	 * -- initialized with the initial maximum radius
	 */
	public double maCoMaxLength = cInitMaxR;
	
	public double braCoMaxLength = cInitMaxR;
	
	/** 
	 * The number of {@link MassiveGroup}s
	 */
	public static int maCoGroupNum;
	/**
	 * The number of {@link BranchingGroup}s
	 */
	public static int braCoGroupNum;

	// CORAL GROUPS --- initialized from Config() variables to store respective parameters
	// see also CoralGroup

	// Parameter files to read
	/**
	 * The parameter file from which to read the parameters
	 * for corals, algae, and the environment, respectively
	 */
	public ParameterFile currentParameterFile = null;
	
	// ALGAE 
	// unlike corals algae are handled as a whole and so their 
	// variables are not stored in a Group class but directly in the main Program		-- maybe needs to be changed
	/**
	 * The number of algal individuals
	 */
	public int numAlgae = 0;
	/**
	 * The fraction of the total area covered by algae 
	 */
	public double algalCoverPercent;					


	// GRAZING
	/**
	 * The initial probability for an alga to be grazed
	 */
	public double iniGrazingProb;
	/**
	 * To define the probability range for an alga to be grazed within one time step
	 * Mean grazing probability +- HalfRange
	 */
	public double grazingProbHalfRange=0.05;

	/**
	 * The actual probability for an alga to be grazed whithin one time step
	 * -- is computed for each time step in relation to algal cover --> see grazing()
	 */
	public double grazingProb;
	/**
	 * The maximum grazing probability
	 */
	public double maxGrazingProb;
	/**
	 * The minimum grazing probability
	 */
	public double minGrazingProb;
	/**
	 * The initial critical threshold of algal cover fraction
	 * -- can be used to adjust the fraction of algal cover
	 */
	public double iniAlgalThreshold;
	/**
	 * The actual critical threshold of algal cover fraction
	 * -- calculated in relation to available space
	 */
	public double algalThreshold = iniAlgalThreshold; 
	/**
	 * The slope of the reaction
	 */
	public double slope = 2.0;
	/** 
	 * A factor to couple the algal potential area with the total coral cover.
	 */
	public double grazingCouplingFactor=1;
	
	
	// TURF ALGAE
	/**
	 * The layer for turf algae
	 */
	public SparseGrid2D turf;
	/**
	 * The relative cover per turf cell
	 */
	double cover;
	/**
	 * The growth rate of turf in percent
	 */
	double turfGR = 5;
	/**
	 * The resolution -- how many pixels per rectangle
	 */
	public int turfResolution;// = 10;
	/**
	 * The number of cells along the width of the simulation area
	 */
	public int cellNumWidth;
	/**
	 * The number of cells along the height of the simulation area
	 */
	public int cellNumHeight;
	/**
	 * The width of a turf cell
	 */
	public int cellWidth; 
	/**
	 * The height of a turf cell
	 */
	public int cellHeight; 

	
	// ENVIRONMENTAL / BLEACHING
	/**
	 * The actual temperature within one month
	 */
	double temperature = 27;

	public boolean constantTemperature=false;
	
	public boolean bleachIsOn=false;
	
	/**
	 * The bleaching interval in months
	 */
	public int bleachInterval;
	public int firstBleach;
	

	
	
	/**
	 * The probability of a coral colony of being bleached
	 */
	public double bleachProb;
	/**
	 * The probability for a coral colony to die once it is bleached
	 */
	public double bleachDeathProb;
	/**
	 * The minimum bleaching probability
	 */
	public double minBleachProb=0; 
	/**
	 * The minimum bleach death probability
	 */
	public double minBleachDeathProb; 
	/**
	 * Storage for bleaching probabilities for respective coral species
	 */
	Hashtable<String, Double> bleachProbs;
	/**
	 * Storage for bleach death probabilities for respective coral species
	 */
	Hashtable<String, Double> bleachDeathProbs;
	/**
	 * The amount of years from which a long-term mean summer temperature is calculated
	 */
	public int longTermYears;
	/**
	 * The threshold of degree heating days from which bleach probs are calculated
	 */
	public double minDHD;
	/**
	 * The minimum temperature at which a coral can die once it is bleached
	 */
	public double minDeathTemp;
	/**
	 * The temperature at which all corals die once they are bleached
	 */
	public double maxDeathTemp;
	/**
	 * Determines how connected the simulated reef patch is
	 */
	public double coralRecImportFactor;
	/**
	 * The probability for a branching coral of being broken off due to surge
	 */
	public double breakageProb;
	/**
	 * The probability for a branching coral to fragment
	 */	
	public double fragProb;
	/**
	 * The range in meters in which a fragment settles
	 */	
	public Double fragRange;
	
	public double dieOvergrowthMas;
	public double dieOvergrowthBra;

	// Variables for Disturbance Events
	/**
	 * The radius of a disturbance
	 */
	double dRadius ;
	/**
	 * Determines if a respective disturbance takes place within a simulation run or not
	 */
	public boolean disser1 = true;
	/**
	 * The maximum diameter of a disturbed area
	 */
	public double disturbMaxSize1; 
	/**
	 * The minimum diameter of a disturbed area
	 */
	public double disturbMinSize1;
	/**
	 * The maximum radius of a disturbed area
	 */
	public double disturbMaxRadius1;
	/**
	 * The minimum radius of a disturbed area
	 */
	public double disturbMinRadius1; 
	/**
	 * The mean interval at which a respective disturbance takes place
	 */
	public int disturbMeanInterval1;
	/**
	 * The percentage standard deviation for the interval of a disturbance event
	 */
	public double disturbSDPercent1;
	/**
	 * The actual standard deviation for the interval of a disturbance event
	 */
	public int disturbSDInterval1;
	/**
	 * The number of synchronous disturbance events of the respective type
	 */
	int disturbMaxNumber1;
	/**
	 * The actual interval of a disturbance 	
	 */
	double dInterval; 
	// see above
	public boolean disser2 = true;
	public double disturbMaxSize2; 
	public double disturbMinSize2;
	public double disturbMaxRadius2;
	public double disturbMinRadius2; 
	public int disturbMeanInterval2;
	public double disturbSDPercent2;
	public int disturbSDInterval2;
	int disturbMaxNumber2;

	/**
	 * ArrayList to store disturbance parameter arrays in
	 */
	ArrayList<double[]> disturbances;
	/**
	 * Array to store disturbance parameters of disturbance type 1 in
	 */
	double[] dist1;
	/**
	 * Array to store disturbance parameters of disturbance type 2 in
	 */
	double[] dist2;
	// Steppables for disturbance events
	public Steppable disturber1;
	public Steppable disturber2;

	public double maxDisBreakProb;
	public double minDisBreakProb;

	

	// OUTPUT
	/**
	 * IF true -- output will be created
	 */
	public static boolean createOutput = true;
	/**
	 * The interval in which output shall be produced in time steps (month)
	 */
	public static int indivOutInter;// = 1;
	/**
	 * The relative path to the Main.class folder
	 */
	private File path = new File(Siccom.class.getProtectionDomain().getCodeSource()
					.getLocation().getPath());
	/**
	 * The relative path to the output folder 
	 */
	public String outputPath = path.getParent() +"/output" ; 
	/**
	 * The output writer
	 */
	public OutputWriter outW;



	private double totalMaCoCov;
	private double totalBraCoCov;


	public double availAreaPerc;




	private double actualTemp;

	// Gradual temperature increase over time
//	public boolean gradualTempIncrease;
//	public double totalTempIncrease;
//	public int increaseYears;
//	public double tempIncYear = totalTempIncrease/increaseYears;

	private boolean haveBeenBleaching=false;
	public double maxHR;
	public double minHR;
	public double ciFactor = 10;

	// VARIABLES FOR MASON INSPECTORS
	// MODEL
	/**
	 * Displays the checkbox for createOutput in the GUI's Model Tab
	 * @return the value of createOutput
	 */
	public boolean getCreateOutput() { return createOutput; }
	/**
	 * Allows to set the value of createOutput 
	 * @param b the value of createOutput -- true or false
	 */
	public void setCreateOutput(boolean b) 
	{ 
		createOutput = b;
		
		outW = new OutputWriter(this);
//		schedule.scheduleOnce(Schedule.EPOCH, outW);
//		setupOutput();
	}
	/**
	 * Displays the output interval in the GUI's Model Tab
	 * @return the value of outputInterval
	 */
	public int getOutputInterval() { return indivOutInter; }
	/**
	 * allows to set the output interval
	 * @param val the value for outputInterval
	 */
	public void setOutputInterval( int val ) {if (val>0) indivOutInter = val; }
	/**
	 * See the initial algal Threshold
	 * @return iniAlgalThreshold
	 */
	
	/**
	 * Contructor
	 * creates the CRPS main frame
	 * -- if started in its own main() --> direct initialisation and start of the program
	 * @param seed the random seed for the random generator
	 */
	public Siccom(long seed) 
	{
		super(new MersenneTwisterFast(seed), new Schedule());
		
		startTime = System.currentTimeMillis();
		
		conf = new Config(this);
		
		if(!gui) initMain();
	}
	
	/**
	 * Initializes the parameters for the different coral groups from parameter files (*.inf) in the specified folder
	 */
	public void initMain()
	{
		conf.readMainFile("mainParam");
		conf.readEnvironmentFile("environment");
	
		totalArea = gridWidth*gridHeight;
	}

	/**
	 * Sets the simulation time to zero, starts the simulation and repeats a specified Steppable().
	 */
	public void start()
	{
		super.start();
		
		System.out.println("Random Seed: " + sim.seed());

		
		algalThreshold = iniAlgalThreshold;
		availAreaPerc = 100;
		totalMaCoCov = 0;
		totalBraCoCov = 0;
	
		
		maCoGroups = new ArrayList<MassiveGroup>();
		braCoGroups = new ArrayList<BranchingGroup>();		
	
		if(!gui)	conf.initOrganisms();
		else		conf.initGUI();			
		
		

		
		//SET UP THE HASH TABLES
		turf = new SparseGrid2D(cellNumWidth,cellNumHeight);
		massiveCorals = new Continuous2D(discretization, gridWidth, gridHeight);
		branchingCorals = new Continuous2D(discretization, gridWidth, gridHeight);
		algae  = new Continuous2D(discretization, gridWidth, gridHeight);

		initTurf();
		initMassiveCorals();
		initBranchingCorals();
		initAlgae();
		
		// setup and initialize the hashtable where the bleaching 
		// probabilities for the different CoralGroups are stored in
		bleachProbs = new Hashtable<String, Double>();
		initBleachProbs();
		bleachDeathProbs = new Hashtable<String, Double>();
		initBleachDeathProbs();
		

			// to randomize the first bleaching event
		if(bleachInterval!=0) firstBleach = random.nextInt((bleachInterval/12));
		else firstBleach=99999;	
		
//		temp = new Temperature("TemperatureData_Chumbe", sim);
//		temp.readTempFile();
		schedule.scheduleOnce(Schedule.EPOCH, temp);
		
		// SETUP OUTPUT WRITER: 
		// at first just the parameters are written to the screen
		outW = new OutputWriter(this);
	
		/**
		 *  MONTHLIES
		 *  -- methods that are repeated for each month 
		 */
		Steppable monthlies = new Steppable()
		{
			private static final long serialVersionUID = 3564764645L;

			@Override
			public void step(SimState state) 
			{
				if (!createOutput) steps = (int) schedule.getSteps();
				calculateBleachProbs();
				getMax();
				grazing();
			}
		};
		schedule.scheduleRepeating(Schedule.EPOCH, 2, monthlies, 1);

		if(createOutput == true) 
		{
			outW.initOutput(this);
			
			Steppable output = new Steppable()
			{
				private static final long serialVersionUID = 1L;
	
				@Override
				public void step(SimState state) 
				{
					steps = (int) schedule.getSteps();
					
					outW.groupedOutput(steps);
					outW.temperatureOutput(steps, temp.meanTemp, temp.longTermSummerMeanTemp, minHR, temp.heatRate, haveBeenBleaching);
//					if (steps!=0 && steps%indivOutInter == 0)  outW.individualOutput(steps);
				}
			};
			schedule.scheduleRepeating(Schedule.EPOCH, 2, output, 1);
		}

		/**
		 * GET THE DATA
		 */
		Steppable dataGetter = new Steppable()
		{
			private static final long serialVersionUID = 3875019604046987874L;

			public void step(SimState state)
			{
				massiveCoralCover();
				massiveCoralCounter();
				branchingCoralCover();
				branchingCoralCounter();
				algaeCover();
				algCounter();
			}
		};
		schedule.scheduleRepeating(Schedule.EPOCH, 1, dataGetter, 1);
		
		/**
		 * 	DISTURBANCE 
		 */
		if (disser1)
		{
			disturber1 = new Steppable()
			{
				private static final long serialVersionUID = 3564764645L;

				@Override
				public void step(SimState state) 
				{
					disturbance1(dist1);
				}
			};
			schedule.scheduleOnce(Schedule.EPOCH+dist1[5], disturber1);
		}
		
		if (disser2)
		{
			disturber2 = new Steppable()
			{
				private static final long serialVersionUID = 3564764645L;

				@Override
				public void step(SimState state) 
				{
					 disturbance2(dist2);
				}
			};
			schedule.scheduleOnce(Schedule.EPOCH+dist2[5], disturber2);
		}
		
		/**
		 * 	CORAL RECRUITMENT
		 */
		for (final BranchingGroup bG : braCoGroups )
		{
			Steppable braCoRecruitment = new Steppable()
			{
				private static final long serialVersionUID = 3432L;

				@Override
				public void step(SimState state) 
				{
					bG.recruitBranchingCorals();
				}
				
			};
			schedule.scheduleRepeating(Schedule.EPOCH+bG.recFirst, 1, braCoRecruitment, bG.recInterval);
		}
		
		for (final MassiveGroup mG : maCoGroups )
		{
			Steppable maCoRecruitment = new Steppable()
			{
				private static final long serialVersionUID = 3432L;

				@Override
				public void step(SimState state) 
				{
					mG.recruitMassiveCorals();
				}
			};
			schedule.scheduleRepeating(Schedule.EPOCH + mG.recFirst, 1, maCoRecruitment, mG.recInterval);
		}
		
		/**
		 * 	ALGAL RECRUITMENT
		 */
		Steppable aRecruitment = new Steppable()
		{
			private static final long serialVersionUID = 43095843L;

			@Override
			public void step(SimState state) 
			{
				recruitAlgae();
			}
		};
		schedule.scheduleRepeating(Schedule.EPOCH + conf.algaRecFirst, 1, aRecruitment, conf.algaRecInterval);	
	}

	/**
	 * Initialization of bleaching probabilities for each coral species
	 */
	private void initBleachProbs()
	 {
		 for(MassiveGroup mG : maCoGroups)
		 {
			 bleachProbs.put(mG.name, 0.0);
		 }
		 for(BranchingGroup bG : braCoGroups)
		 {
			 bleachProbs.put(bG.name, 0.0);
		 }
	 }
	/**
	 * Determination of bleaching probs within a month
	 */
	 public void calculateBleachProbs()
	 {
		 if(temp.degreeHeatingDays > minDHD && temp.heatRate > minHR)
		 {
			 haveBeenBleaching=true;
			 
			 for(MassiveGroup mG : maCoGroups)
			 {
				 bleachProb = ((1 - mG.minBleachProb) / (maxHR-minHR) * (temp.heatRate-minHR)) + mG.minBleachProb;	
				 if (bleachProb>1) bleachProb = 1;
				 if (bleachProb<0) bleachProb = 0;
				 bleachProbs.put(mG.name, bleachProb);
				 
				 bleachDeathProb = ((1 - mG.minDeathProb) / (maxHR-minHR) * (temp.heatRate-minHR)) + mG.minDeathProb;	
				 if (bleachDeathProb>1) bleachDeathProb = 1;
				 if (bleachDeathProb<0) bleachDeathProb = 0;
				 bleachDeathProbs.put(mG.name, bleachDeathProb);
			 }
			 
			 for(BranchingGroup bG : braCoGroups)
			 {
				 bleachProb = ((1 - bG.minBleachProb) / (maxHR-minHR) * (temp.heatRate-minHR)) + bG.minBleachProb;
				 if (bleachProb>1) bleachProb = 1;
				 if (bleachProb<0) bleachProb = 0;
				 bleachProbs.put(bG.name, bleachProb);
				 
				 bleachDeathProb = ((1 - bG.minDeathProb) / (maxHR-minHR) * (temp.heatRate-minHR)) + bG.minDeathProb;
				 if (bleachDeathProb>1) bleachDeathProb = 1;
				 if (bleachDeathProb<0) bleachDeathProb = 0;				 
				 bleachDeathProbs.put(bG.name, bleachDeathProb);
			 }
//			 System.out.println(bleachProbs);
		 }
		 else if(haveBeenBleaching) 
		 {
			 initBleachProbs();
			 haveBeenBleaching=false;
		 }
	 }
	
	 /**
	  * Initialization of bleach death probabilities
	  */
	 private void initBleachDeathProbs()
	 {
		 for(MassiveGroup mG : maCoGroups)
		 {
			 bleachDeathProbs.put(mG.name, 0.0);
		 }
		 for(BranchingGroup bG : braCoGroups)
		 {
			 bleachDeathProbs.put(bG.name, 0.0);
		 }
	 }

	/* 
	 * 	CORALS	
	 */
		
	/**
	 *  Initializes massive coral agents from the massive groups and stores them into the massiveCorals-Continuous2D
	 */
 	public synchronized void initMassiveCorals()
	{
 		for (MassiveGroup mG : maCoGroups )
 		{	
 			mG.initMassiveCorals();
		}
	}

	/**
	 *  Initializes branching coral agents from the branching groups and stores them into the branchingCorals-Continuous2D
	 */
	public synchronized void initBranchingCorals()
	{
		for (BranchingGroup bG : braCoGroups )
 		{	
 			bG.initBranchingCorals();
		}
	}
	
 
  	/*
	 * 	ALGAE
	 */
	
	/**
	 *  Initializes alga agents into the algae-Continuous2D
	 */
	public synchronized void initAlgae()
	{
		double sumSize = 0;
		
		while (sumSize <= conf.algalCover)
		{
			double xPos = random.nextDouble()*gridWidth;
			double yPos = random.nextDouble()*gridHeight;
			
			Alga a = new Alga(	sim,									// the simulation, the agent acts in
								xPos, 									// x position
								yPos,									// y position
								random.nextDouble()*conf.aMaxRadius,			// radius
								random.nextInt(conf.algaMaxAge),				// age
								random.nextDouble()*conf.aMaxHeight);		// height
			
			sim.algae.setObjectLocation(a, 
					new Double2D(xPos, yPos));	
			schedule.scheduleOnce(Schedule.EPOCH, a);
			sumSize = sumSize + a.getSize();
		}
	}
	
	
	/**
	 * Creates new alga agents and stores them into the algae-Continuous2D
	 */
 	public void recruitAlgae()
  	{
   		for (int i=0; i<conf.algaRecNum; i++)
  		{
  			double xPos = random.nextDouble()*gridWidth;
  			double yPos = random.nextDouble()*gridHeight;
  				
  			Alga a = new Alga(		sim,							// the simulation, the agent acts in
  									xPos,							// x position
  									yPos,							// y position
									conf.algaRecRad,				// radius
									0,								// age
									conf.algaRecRad);				// height of the recruit equals the radius
  				
  			sim.algae.setObjectLocation(a, new Double2D(xPos, yPos));
  			schedule.scheduleOnce(a);
   		}
  	}
	
 	/*
 	 *  TURF
 	 */
 	/**
 	 * Initializes turf algae and stores them into the turf-SparseGrid2D
 	 */
	private void initTurf() {
		for (int i = 0; i < cellNumWidth; i++)
		{
			double x = (i * cellWidth);
			for (int j = 0; j < cellNumHeight; j++)
			{
				double y = (j * cellHeight);
				cover = random.nextDouble()*100.0;
				
				TurfCell tC = new TurfCell( 	sim,
												x, 
												y,
												cellWidth,
												cellHeight,
												cover,
												turfGR			);
				
				schedule.scheduleOnce(Schedule.EPOCH, tC);
				turf.setObjectLocation(tC, i, j);
			}
		}
	}
 
 	/* GRAZING */
	/**
	 *  At first the grazing probability is computed in relation to the algal density.
	 *  Then it checks the whole algae-Continuous2D and removes an alga with a certain probability
	 */
	public void grazing()
	{

		// calculate available area for algal dispersal 
		//-- the total area of massive and branching corals is substracted from the total area
		availAreaPerc =	(totalArea - ((totalMaCoCov + (totalBraCoCov))*grazingCouplingFactor))/totalArea * 100; // branching corals leave some space for settling between their branches
		algalThreshold = (iniAlgalThreshold / 100) * availAreaPerc;

		// calculate the probability for grazing depending on the algal density
		grazingProb = (maxGrazingProb-minGrazingProb) * ( 1 - ( 1 / Math.pow((algalCoverPercent / algalThreshold), slope))) + minGrazingProb; 

		if (grazingProb < 0) grazingProb = 0; 
		else if (grazingProb > 1) grazingProb = 1;
		
		Bag a = algae.getAllObjects();
		for (int i=0; i<a.numObjs; i++)
		{
			if (random.nextBoolean(grazingProb)) ((Alga) a.objs[i]).die();
		}
		
		Bag t = turf.getAllObjects();
		for (int j=0; j<t.numObjs; j++)
		{
			if(random.nextBoolean(grazingProb)) ((TurfCell) t.objs[j]).cover = ((TurfCell) t.objs[j]).cover - 50;
			if (((TurfCell) t.objs[j]).cover < 0) ((TurfCell) t.objs[j]).cover = 0;
		}
		
	}
	
	

  	/* FIND THE MAXIMUM RADIUS OF CORALS */
  	/**
	 * 	Finds the maximum radius of corals.	
	 */
	public void getMax()
	{
		maCoMaxLength = 25;						// in case that the larger coral dies, maxR is reset to the initial value
		braCoMaxLength = 10;
		Bag m = massiveCorals.getAllObjects();
		for (int i=0; i<m.numObjs; i++)
		{
			 if ((!((MassiveCoral) m.objs[i]).bleached) && (((MassiveCoral) m.objs[i]).maximumBranchLength > maCoMaxLength))
			 {
				maCoMaxLength = ((MassiveCoral) m.objs[i]).getRadius(); 
			 }
		}
		
		Bag b = branchingCorals.getAllObjects();
		for (int i=0; i<b.numObjs; i++)
		{
			 if ((!((BranchingCoral) b.objs[i]).bleached) && (((BranchingCoral) b.objs[i]).maximumBranchLength > braCoMaxLength))
			 {
				braCoMaxLength = ((BranchingCoral) b.objs[i]).getRadius(); 
			 }
		}
	}
	
	/**
	 * sends maxR
	 * @return maxR the maximum radius
	 */
	public double sendMaxR()
	{
		return maCoMaxLength;
	}


	/* DISTURBANCE */
	
	// Disturbance No. 1
	/**
	 * Clears a field of the simulation area of all objects
	 */
	public void disturbance1(double[] disList)
	{
		double disturbMaxRadius = disList[0];
		double disturbMinRadius = disList[1];
		int disturbMeanInterval = (int) disList[2];
		int disturbSDInterval = (int) disList[3];
		int disturbPerEvent = (int) disList[4]; 					// random.nextInt(disturbMaxNumber1);
		
		if (disturbPerEvent != 0)
		{

			for (int j=0; j<disturbPerEvent; j++)
			{
				dRadius = random.nextDouble()* (disturbMaxRadius - disturbMinRadius +1) + disturbMinRadius;
				if (dRadius<0.5) dRadius = 0;
				
				
				double dX = random.nextDouble() *  ((gridWidth+dRadius) - (0-dRadius) + 1)  + (0-dRadius);
				double dY = random.nextDouble() *  ((gridWidth+dRadius) - (0-dRadius) + 1)  + (0-dRadius);	
				Double2D dCentre = new Double2D(	dX, dY );
				
				if (createOutput) outW.disturbanceOutput(steps, "small", dX, dY, dRadius);
				Bag m = massiveCorals.getObjectsExactlyWithinDistance(dCentre, dRadius);
				for (int i=0; i<m.numObjs; i++)
				{
					MassiveCoral maco = ((MassiveCoral) m.objs[i]);
					double sf = maco.mG.surfaceFactor;
					double prob = 1 - ((maxDisBreakProb-minDisBreakProb)/sf);
					
					if(random.nextBoolean(prob))
					{
						maco.die();
					}
				}
					
				Bag b = branchingCorals.getObjectsExactlyWithinDistance(dCentre, dRadius);
				for (int i=0; i<b.numObjs; i++)
				{
					BranchingCoral braco = ((BranchingCoral) b.objs[i]);
					double sf = braco.bG.surfaceFactor;
					double prob = 1 - ((maxDisBreakProb-minDisBreakProb)/sf);
				
					if(random.nextBoolean(prob))
					{
						if (sim.random.nextBoolean(braco.fragProb)) braco.fragtate();
						braco.die();
					}
				}
				
				Bag a = algae.getObjectsExactlyWithinDistance(dCentre, dRadius);
				for (int i=0; i<a.numObjs; i++) ((Alga) a.objs[i]).die();
			}
		}
			// sets the time for the first disturbance event to occur
			if (disturbMeanInterval<=1) 
			{
				dInterval = 1;
				schedule.scheduleOnce(disturber1);
			}
			else
			{
				dInterval = Math.round(random.nextGaussian() * disturbSDInterval + disturbMeanInterval) + 1;
				schedule.scheduleOnce(steps+dInterval, disturber1);			// reschedule the disturbance event after the time interval
			}
														
	}
	
	// Disturbance No. 2
	/**
	 * Clears a field of the simulation area of all objects
	 */
	public void disturbance2(double[] disList)
	{
		double disturbMaxRadius = disList[0];
		double disturbMinRadius = disList[1];
		int disturbMeanInterval = (int) disList[2];
		int disturbSDInterval = (int) disList[3];
		int disturbPerEvent = (int) disList[4]; 					// random.nextInt(disturbMaxNumber1);

		if (disturbPerEvent != 0)
		{

			for (int j=0; j<disturbPerEvent; j++)
			{
				dRadius = random.nextDouble()* (disturbMaxRadius - disturbMinRadius +1) + disturbMinRadius;
				double dX = random.nextDouble() *  ((gridWidth+dRadius) - (0-dRadius) + 1)  + (0-dRadius);
				double dY = random.nextDouble() *  ((gridWidth+dRadius) - (0-dRadius) + 1)  + (0-dRadius);	
				Double2D dCentre = new Double2D(	dX, dY );
				
				if (createOutput) outW.disturbanceOutput(steps, "large", dX, dY, dRadius);
				Bag m = massiveCorals.getObjectsExactlyWithinDistance(dCentre, dRadius);
				for (int i=0; i<m.numObjs; i++)
				{
					MassiveCoral maco = ((MassiveCoral) m.objs[i]);
					double sf = maco.mG.surfaceFactor;
					double prob = 1-((maxDisBreakProb-minDisBreakProb)/sf);
					
					if(random.nextBoolean(prob))
					{
						maco.die();
					}
				}
				
				Bag b = branchingCorals.getObjectsExactlyWithinDistance(dCentre, dRadius);
				for (int i=0; i<b.numObjs; i++)
				{
					BranchingCoral braco = ((BranchingCoral) b.objs[i]);
					double sf = braco.bG.surfaceFactor;
					double prob = 1-((maxDisBreakProb-minDisBreakProb)/sf);
					
					if(random.nextBoolean(prob))
					{
						braco.die();
					}
				}
				
				Bag a = algae.getObjectsExactlyWithinDistance(dCentre, dRadius);
				for (int i=0; i<a.numObjs; i++) ((Alga) a.objs[i]).die();
			}
		}
		
			if (disturbMeanInterval<=1) 
			{
				dInterval = 1;
				schedule.scheduleOnce(disturber2);
			}
			else
			{
				dInterval = Math.round(random.nextGaussian() * disturbSDInterval + disturbMeanInterval) + 1;
				schedule.scheduleOnce(steps+dInterval, disturber2);			// reschedule the disturbance event after the time interval
			}													
	}
	
	/**
	 * Counts massive coral agents of each group and stores them in separate lists for each group
	 */
	public void massiveCoralCounter()
	{
		for (MassiveGroup mG : maCoGroups )
		{
			int num = 0;
			Bag m = massiveCorals.getAllObjects();
			
			for (int i=0; i<m.numObjs; i++)
			{
				MassiveCoral mC = (MassiveCoral)m.objs[i];
				if (mC.getName().equals(mG.name))
				num++;
			}
			mG.numMaCo = num;
		}
	}
	
	/**
	 * Computes the relative cover of massive coral groups and stores them separately for each group
	 */
	public void massiveCoralCover()
	{
		totalMaCoCov = 0;
		
		
		for ( MassiveGroup mG : maCoGroups ) 
		{
			double cSize = 0;
			Bag m = massiveCorals.getAllObjects();
		
			for (int i=0; i<m.numObjs; i++) 
			{
				MassiveCoral mC = (MassiveCoral)m.objs[i];
				if (mC.getName().equals(mG.name))
					cSize += ((MassiveCoral) m.objs[i]).size;
			}		
			mG.maCoPercentCov = cSize/totalArea*100;
			
			
			totalMaCoCov += cSize;
		}
		
		
	}
	
	/**
	 * Counts branching coral agents of each group and stores them in separate lists for each group
	 */
	public void branchingCoralCounter()
	{
		for (BranchingGroup bG : braCoGroups)
		{
			int num = 0;
			Bag m = branchingCorals.getAllObjects();
			
			for (int i=0; i<m.numObjs; i++)
			{
				BranchingCoral bC = (BranchingCoral)m.objs[i];
				if (bC.getName().equals(bG.name) && (bC.xPos > 0 || bC.xPos < Siccom.gridWidth || bC.yPos > 0 || bC.yPos < Siccom.gridHeight))
				num++;
			}
			bG.numBraCo = num;
		}
	}
	
	/**
	 * Computes the relative cover of branching coral groups and stores them separately for each group
	 */
	public void branchingCoralCover()
	{
		totalBraCoCov = 0;
		
		for ( BranchingGroup bG : braCoGroups ) 
		{
			double cSize = 0;
			Bag m = branchingCorals.getAllObjects();
		
			for (int i=0; i<m.numObjs; i++) 
			{
				BranchingCoral bC = (BranchingCoral)m.objs[i];
				if (bC.getName().equals(bG.name) && (bC.xPos > 0 || bC.xPos < Siccom.gridWidth || bC.yPos > 0 || bC.yPos < Siccom.gridHeight))	
					cSize += ((BranchingCoral) m.objs[i]).size;
			}
			bG.braCoPercentCov = cSize/totalArea*100;
			
			totalBraCoCov += cSize;
		}
	}
	
	/**
	 * Counts macroalgal agents
	 */
	public void algCounter()
	{
		numAlgae = 0;
		Bag a = algae.getAllObjects();
		for (int i=0; i<a.numObjs; i++)
		{
			Alga alg = (Alga) a.objs[i];
			if (alg.alive && (alg.xPos > 0 || alg.xPos < Siccom.gridWidth || alg.yPos > 0 || alg.yPos < Siccom.gridHeight)) numAlgae++;
		}
	}

	/**
	 * Computes the relative cover of macroalgae
	 */
	public double algaeCover()
	{
		double aSize = 0;
		Bag a = algae.getAllObjects();
		for (int i=0; i<a.numObjs; i++)
		{
			Alga alg = (Alga) a.objs[i];
			
			if (alg.alive && alg.age>2 && (alg.xPos > 0 || alg.xPos < Siccom.gridWidth || alg.yPos > 0 || alg.yPos < Siccom.gridHeight)) 
				aSize = aSize + alg.getSize();
			
		}
		algalCoverPercent = aSize/totalArea*100;

		return algalCoverPercent;
		
	}
	

	/**
	 * The main method
	 * @param args
		-repeat R Long value > 0: Runs the job R times. The random seed for
		each job is the provided -seed plus the job# (starting at 0).
		Default: runs once only: job number is 0.
		
		-checkpoint C String: loads the simulation from file C for
		job# 0. Further jobs are started new using -seed as normal.
		Default: starts a new simulation rather than loading one.

		-until U Double value >= 0: the simulation must stop when the
		simulation time U has been reached or exceeded.
		Default: don't stop.

		-for N Long value >= 0: the simulation must stop when N
		simulation steps have transpired.
		Default: don't stop.
		
		-seed S Long value not 0: the random number generator seed.
		Default: the system time in milliseconds.

		-time T Long value >= 0: print a timestamp every T simulation steps.
		If 0, nothing is printed.
		Default: auto-chooses number of steps based on how many
		appear to fit in one second of wall clock time. Rounds to
		one of 1, 2, 5, 10, 25, 50, 100, 250, 500, 1000, 2500, etc.

		-docheckpoint D Long value > 0: checkpoint every D simulation steps.
		Default: never.
		Checkpoints files named
		<steps>.<job#>.Siccom.checkpoint
	 */
	public static void main (String[] args)
	{
		doLoop (Siccom.class, args);
		
		DecimalFormatSymbols usFS = new DecimalFormatSymbols(Locale.US);
		DecimalFormat numform2 = new DecimalFormat("00", usFS);
		
		long endTime = System.currentTimeMillis();
		long extTime = endTime - startTime;
		
		double mins = (int)(extTime / 60000);
		double secs = (int)((extTime -(mins*60000)) /1000);
		
		System.out.println("Execution Time: " + numform2.format(mins) + " min " + numform2.format(secs) + " sec");
		
		System.exit(0);
		
	}
}
