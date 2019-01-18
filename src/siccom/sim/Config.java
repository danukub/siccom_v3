package siccom.sim;

import java.awt.Color;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.lang.reflect.Field;
import java.text.DecimalFormat;
import java.text.DecimalFormatSymbols;
import java.util.Arrays;
import java.util.Locale;

import javax.swing.JTable;

import ec.util.MersenneTwisterFast;
import siccom.gui.FileParameterPanel;
import siccom.gui.SiccomUI;

public class Config 
{
	Siccom sim;
	
	// Format for output data 
	// -- always in the US format
	DecimalFormatSymbols usFS = new DecimalFormatSymbols(Locale.US);
	public DecimalFormat numform2 = new DecimalFormat("00", usFS);
	public DecimalFormat numform3 = new DecimalFormat("000", usFS);
	public DecimalFormat numform_2 = new DecimalFormat("##0.00", usFS);
	public DecimalFormat numform_3 = new DecimalFormat("#0.000", usFS);
	public DecimalFormat numform_8 = new DecimalFormat("0000.0000", usFS);
	public DecimalFormat numform_1 = new DecimalFormat("00.0", usFS);
	
	
	
	//CORAL --- general
	public int interactTurfAge = 1;
	
	// CORAL GROUPS --- variables to store respective parameters
	// see also CoralGroup
	static final String infoFileExtension = ".inf";
	public String name;
	public String colorString;
	public Color color;
	public double ci;
	public double maxIniRadius;
	public double maxRadius;
	public double coveredArea;
	public double diamAtMaturity;
	public double surfaceFactor;
	public double propagulesPerSqCm;
	public double retainFactor;
	public double growthRate;
	public double recRad;
	public int recFirst;
	public int recInterval;
	public int recNum;
	public double bleachProb;
	public double bleachDeathProb;
	public double minBleachProb;
	public double minDeathProb;
	public int numBranches;
	public double fragSize;
	public double fragRange;
	
	
	// ALGAE 
	// unlike corals algae are handled as a whole and so their 
	// variable are not stored in a Group class but directly in the main Program
	/**
	 * The number of algal individuals
	 */
	public int numAlgae = 0;
	
	public String aName;

	public Color algaColor;

	/**
	 * The growth rate for an alga
	 */
	public double aGrowthRate;// = 1.0;
	/**
	 * The maximum radius an alga can have. 
	 * -- described bigger than the actual radius of an alga in order to
	 * simulate the movement in the swell.
	 */
	public double aMaxRadius;	
	/**
	 * The maximum height that an alga can reach
	 */
	public double aMaxHeight;
	/**
	 * The number of algal recruits per recruitment event
	 */
	public int algaRecNum;
	/**
	 * The first month in the year when algal recruitment takes place
	 */
	public int algaRecFirst;
	/**
	 * The interval between algal recruitment events
	 */
	public int algaRecInterval;
	
	public double algaRecNumPerSqM;
	
	
	/**
	 * The radius of an algal recruit
	 */
	public double algaRecRad;
	/**
	 * The height of an alga at which fragmentation can happen
	 */
	public double aFragmentationHeight;
	/**
	 * The size of an algal fragment
	 */
	public double aFragmentSize;
	/**
	 * The radial range for an algal fragment where it settles
	 */
	public double aFragRange;
	/**
	 * The maximum age of an alga -- maximum duration of an alga in the simulation
	 */
	public int algaMaxAge;
	/**
	 * The fraction of the total area covered by algae 
	 */
	public double algalCoverPercent;				
	/**
	 * The absolute value of algal covered area
	 */
	public double algalCover;
	/**
	 * The maximal number of fragments an alga can produce
	 */
	public int aMaxFragNum;
	
	// Parameter files to read
	/**
	 * The parameter file from which to read the parameters
	 * for corals, algae, and the environment, respectively
	 */
	public ParameterFile currentParameterFile = null;

	Boolean gradualTempIncrease;

	double yearlyTempIncrease;

	int increaseYears;

	double totalTempIncrease;

	
	public Config(Siccom sim)
	{
		this.sim = sim;
	}

	
	public static String getLocalFileName(String fn) 
	{
		File f = new File (Siccom.class.getProtectionDomain().getCodeSource()
				.getLocation().getPath());
		String fileName = f.getParent()+"/species/"+ fn;
		return fileName;
	}
	
	
	public void readMainFile(String fn) 
	{
		File file;
		String fileName;
		InputStreamReader in = null;
		try {
			fileName = fn + infoFileExtension;
			file = new File(getLocalFileName(fileName));
			
			if (file.isFile()) {
				in = new InputStreamReader(new FileInputStream(file));
			} 
			else {
				System.out.println(fileName + "\tcould not be found or corrupt");
			}
			

			
			
			ParameterFile pf = new ParameterFile(in, fileName, true);
			this.currentParameterFile = pf;
			
			Siccom.areaWidth = readInt(pf, "areaWidth");			// the simulation area width in meters
			Siccom.areaHeight = readInt(pf, "areaHeight");			// the simulation area height in meters
			Siccom.resolution = readDouble(pf, "resolution"); 		// how many centimeters per pixel
			Siccom.maCoGroupNum = readInt(pf, "maCoNum");
			Siccom.braCoGroupNum = readInt(pf, "braCoNum");
			Siccom.indivOutInter = readInt(pf, "indivOutInter");
			
			Siccom.dimensionConv_meters = Siccom.resolution / 100.0;					// converter from meters to pixels
			Siccom.dimensionConv_milimeters = 1 / (Siccom.resolution*10) / 12;			// converter from mm/year to pixels/month
			Siccom.gridWidth = Siccom.areaWidth / Siccom.dimensionConv_meters;				// the simulation area width in pixels
			Siccom.gridHeight = Siccom.areaHeight / Siccom.dimensionConv_meters;			// the simulation area height in pixels
			
			Siccom.totalArea = Siccom.gridWidth * Siccom.gridHeight;
			
			
			sim.turfResolution = (int) (1 / Siccom.dimensionConv_meters);
			sim.cellNumWidth = (int)(Siccom.gridWidth / sim.turfResolution);
			sim.cellNumHeight = (int)(Siccom.gridHeight / sim.turfResolution);
			sim.cellWidth = sim.turfResolution;
			sim.cellHeight = sim.turfResolution;
		
		}
		catch (IOException e) 
		{
			System.out.println("Problem with reading " + fn + ".inf");
		}
	
	}

	
	/**
	 * Reads the environment parameter file
	 * @param fn the filename 
	 */
	public void readEnvironmentFile(String fn) 
	{
		File file;
		String fileName;
		InputStreamReader in = null;
		try {
			fileName = fn + infoFileExtension;
			file = new File(getLocalFileName(fileName));
			
			if (file.isFile()) {
				in = new InputStreamReader(new FileInputStream(file));
			} 
			else {
				System.out.println(fileName + "\tcould not be found or corrupt");
			}

			ParameterFile pf = new ParameterFile(in, fileName, true);
			this.currentParameterFile = pf;
			
			sim.disturbMaxSize1 = readDouble(pf, "disturbMaxSize1") / Siccom.dimensionConv_meters;
			sim.disturbMinSize1 = readDouble(pf, "disturbMinSize1") / Siccom.dimensionConv_meters;
			sim.disturbMaxRadius1 = sim.disturbMaxSize1 / 2;
			sim.disturbMinRadius1 = sim.disturbMinSize1 / 2;	
			sim.disturbMeanInterval1 = (int)readDouble(pf, "disturbMeanInterval1");
			sim.disturbSDPercent1 = readDouble(pf, "disturbSDInterval1");
			sim.disturbSDInterval1 = (int)(sim.disturbSDPercent1*sim.disturbMeanInterval1/100);
			sim.disturbMaxNumber1 = readInt(pf, "disturbMaxNumber1");
			
			if (sim.disturbMaxSize1==0 || sim.disturbMinSize1==0 || 
					sim.disturbMaxNumber1==0 || sim.disturbMeanInterval1==0 )
			{
				sim.disser1 = false;
			}
			else
			{

				MersenneTwisterFast random = new MersenneTwisterFast(sim.seed());

				// sets the time for the first disturbance event to occur
				sim.dInterval = Math.round(random.nextGaussian() * sim.disturbSDInterval1 + sim.disturbMeanInterval1 +1);
				while (sim.dInterval <= 0) {sim.dInterval = Math.round(random.nextGaussian() * sim.disturbSDInterval1 + sim.disturbMeanInterval1)+1;}
				
				sim.dist1 = new double[6];
				sim.dist1[0] = sim.disturbMaxRadius1;
				sim.dist1[1] = sim.disturbMinRadius1;
				sim.dist1[2] = (double)sim.disturbMeanInterval1;
				sim.dist1[3] = (double)sim.disturbSDInterval1;
				sim.dist1[4] = (double)sim.disturbMaxNumber1;
				sim.dist1[5] = (double)sim.dInterval;
			}
			
			
			sim.disturbMaxSize2 = readDouble(pf, "disturbMaxSize2") / Siccom.dimensionConv_meters;
			sim.disturbMinSize2 = readDouble(pf, "disturbMinSize2") / Siccom.dimensionConv_meters;
			sim.disturbMaxRadius2 = sim.disturbMaxSize2 / 2;
			sim.disturbMinRadius2 = sim.disturbMinSize2 / 2;	
			sim.disturbMeanInterval2 = (int)readDouble(pf, "disturbMeanInterval2");
			sim.disturbSDPercent2 = readDouble(pf, "disturbSDInterval2");
			sim.disturbSDInterval2 = (int)(sim.disturbSDPercent2*sim.disturbMeanInterval2/100);
			sim.disturbMaxNumber2 = readInt(pf, "disturbMaxNumber2");
			
			if (sim.disturbMaxSize2==0 || sim.disturbMinSize2==0 || 
					sim.disturbMaxNumber2==0 || sim.disturbMeanInterval2==0)
			{
				sim.disser2 = false;
			}
			else
			{
		
				MersenneTwisterFast random = new MersenneTwisterFast(sim.seed());
				// sets the time for the first disturbance event to occur
				sim.dInterval = Math.round(random.nextGaussian() * sim.disturbSDInterval2 + sim.disturbMeanInterval2)+1;
				while (sim.dInterval <= 0) {sim.dInterval = Math.round(random.nextGaussian() * sim.disturbSDInterval2 + sim.disturbMeanInterval2)+1;}
	
				sim.dist2 = new double[6];
				sim.dist2[0] = sim.disturbMaxRadius2;
				sim.dist2[1] = sim.disturbMinRadius2;
				sim.dist2[2] = (double)sim.disturbMeanInterval2;
				sim.dist2[3] = (double)sim.disturbSDInterval2;
				sim.dist2[4] = (double)sim.disturbMaxNumber2;
				sim.dist2[5] = (double)sim.dInterval;
			}
			
			sim.maxDisBreakProb = readDouble(pf, "maxDisBreakProb");
			sim.minDisBreakProb = readDouble(pf, "minDisBreakProb");
			
			sim.longTermYears = readInt(pf, "longTermYears");
			sim.minDHD = readDouble(pf, "minDHD");
			sim.maxHR = readDouble(pf, "maxHR");
			sim.minHR = readDouble(pf, "minHR");
			sim.bleachInterval = (int)readDouble(pf, "bleachInterval")*12; // input is in years --> calculated in month
			
			sim.coralRecImportFactor = readDouble(pf, "coralRecImportFactor");		
			sim.breakageProb = readDouble(pf, "breakageProb");
			
			sim.fragProb = readDouble(pf, "fragProb");
			sim.fragRange = readDouble(pf, "fragRange") / Siccom.dimensionConv_meters;
			
			sim.dieOvergrowthMas = readDouble(pf, "dieOvergrowthMas");
			sim.dieOvergrowthBra = readDouble(pf, "dieOvergrowthBra");

			sim.iniGrazingProb = readDouble(pf, "grazingProb");
			sim.grazingProb = sim.iniGrazingProb;
			sim.iniAlgalThreshold = readDouble(pf, "iniAlgalThreshold");
			
			// calculate the grazing prob min and max relative to the initial grazing prob
			sim.minGrazingProb = sim.grazingProb - sim.grazingProbHalfRange;
			if (sim.minGrazingProb < 0.0) sim.minGrazingProb = 0;
			sim.maxGrazingProb = sim.grazingProb + sim.grazingProbHalfRange;
			
			// SCALING TURF
			sim.turfResolution = (int) (readInt(pf, "turfResolution")/Siccom.dimensionConv_meters);
			sim.cellNumWidth = (int)(Siccom.gridWidth / sim.turfResolution);
			sim.cellNumHeight = (int)(Siccom.gridHeight / sim.turfResolution);
			
			// To determine if smaller bleaching events occur or not
			sim.constantTemperature = Boolean.valueOf(readString(pf, "constTemp"));

			// Gradual temperature increase over time
			gradualTempIncrease = Boolean.valueOf(readString(pf, "gradualTempIncrease"));
			totalTempIncrease = readDouble(pf, "totalTempIncrease");
			increaseYears = readInt(pf, "increaseYears");
			yearlyTempIncrease = totalTempIncrease/increaseYears;

			sim.temp = new Temperature("TemperatureData_Chumbe", 
					sim, 
					gradualTempIncrease,
					yearlyTempIncrease);
			
			// Strength of the Competition Index
			sim.ciFactor = readDouble(pf, "ciFactor");
			
		} 
		catch (IOException e) 
		{
			System.out.println("Problem with reading " + fn + ".inf");
		}
	}
	
	
	public void initOrganisms()
	{		
		readAlgaFile("algaParam");
		
		// read massive coral group parameters and setup the Massive Groups		
		for (int i=1; i<=Siccom.maCoGroupNum;i++)
		{
			String fileName = "maCoParam"+numform2.format(i);
			readParameterFile(fileName);
	
			MassiveGroup maco = new MassiveGroup(	sim,
													name,
													colorString,
													color,
													ci,
													maxIniRadius,
													maxRadius, 
													growthRate,
													coveredArea,
													diamAtMaturity,
													surfaceFactor,
													propagulesPerSqCm,
													retainFactor,
													recRad,
													recFirst,
													recInterval,
													recNum,
													minBleachProb,
													minDeathProb);

			sim.maCoGroups.add(maco);
		}
			
		// read branching coral group parameters and setup Branching Groups
		for (int i=1; i<=Siccom.braCoGroupNum;i++)
		{
			String fileName = "braCoParam"+numform2.format(i);
			readParameterFile(fileName);
		
			BranchingGroup braco = new BranchingGroup(	sim,
														name,
														colorString,
														color,
														ci,
														maxIniRadius,
														maxRadius, 
														growthRate,
														coveredArea,
														diamAtMaturity,
														surfaceFactor,
														propagulesPerSqCm,
														retainFactor,
														recRad,
														recFirst,
														recInterval,
														recNum,
														minBleachProb,
														minDeathProb,
														numBranches);
			sim.braCoGroups.add(braco);
		}
	}
	
	private void readAlgaFile(String fn) {
		File file;
		String fileName;
		InputStreamReader in = null;
		try {
			fileName = fn + infoFileExtension;
			file = new File(getLocalFileName(fileName));
			
			if (file.isFile()) {
				in = new InputStreamReader(new FileInputStream(file));
			} 
			else {
				System.out.println(fileName + "\tcould not be found or corrupt");
			}

			ParameterFile pf = new ParameterFile(in, fileName, true);
			this.currentParameterFile = pf;
			aName = readString(pf, "name");
			colorString = readString(pf, "color");
			algaColor = getRGBColor(colorString);
			aMaxRadius = readDouble(pf, "aMaxRadius") / Siccom.resolution;
			aMaxHeight = readDouble(pf, "aMaxHeight") / Siccom.resolution;
			algaMaxAge = readInt(pf, "algaMaxAge");
			aGrowthRate = readDouble(pf, "aGrowthRate") / (Siccom.resolution*10);
			aFragmentationHeight = readDouble(pf, "aFragmentationHeight") / Siccom.resolution; 
			aMaxFragNum = readInt(pf, "aMaxFragNum");
			aFragmentSize = readDouble(pf, "aFragmentSize") / Siccom.resolution;
			aFragRange	  = readDouble(pf, "aFragRange") / Siccom.dimensionConv_meters;
			algalCoverPercent = readDouble(pf, "algalCoverPercent");
			algaRecRad = readDouble(pf, "algaRecRad") / Siccom.resolution;
			algaRecFirst = readInt(pf, "algaRecFirst");
			algaRecInterval = readInt(pf, "algaRecInterval");
			algaRecNumPerSqM = readDouble(pf, "algaRecNumPerSqM");
			algaRecNum = (int)( algaRecNumPerSqM * Siccom.areaWidth * Siccom.areaHeight );
			algalCover = Siccom.totalArea / 100 * algalCoverPercent;
			
			
		
		} 
		catch (IOException e) 
		{
			System.out.println("Problem with reading " + fn + ".inf");
		}
	}


	private Color getRGBColor(String cS) {
		java.util.List<String> colorVals = Arrays.asList(cS.split(","));
		// For RGB colors
		int r = Integer.parseInt(colorVals.get(0));
		int g = Integer.parseInt(colorVals.get(1));
		int b = Integer.parseInt(colorVals.get(2));
		Color c = new Color(r,g,b);
		
		return c;
	}
	
	
	/**
	 * Before starting the simulation the parameters are read from the *.inf-files
	 */
	public void readParameterFile( String fn ) {
		File file;
		String fileName;
		InputStreamReader in = null;
		try {
			fileName = fn + infoFileExtension;
			file = new File(getLocalFileName(fileName));
			
			if (file.isFile()) {
				in = new InputStreamReader(new FileInputStream(file));
			} 
			ParameterFile pf = new ParameterFile(in, fileName, true);
			this.currentParameterFile = pf;

			name = readString(pf, "name");
			colorString = readString(pf, "color");
			color = getRGBColor(colorString);
			
			ci = readDouble(pf, "CI");
			maxIniRadius = readDouble(pf, "maxIniRadius") / Siccom.resolution;
			maxRadius = readDouble(pf, "maxRadius") / Siccom.resolution;
			growthRate = readDouble(pf, "growthRate") * Siccom.dimensionConv_milimeters;
			coveredArea = readDouble(pf, "coveredArea");
			diamAtMaturity = readDouble(pf, "diamAtMaturity") / Siccom.resolution;
			surfaceFactor = readDouble(pf, "surfaceFactor");
			propagulesPerSqCm = readDouble(pf, "propagulesPerSqCm");
			retainFactor = readDouble(pf, "retainFactor");
			recRad = readDouble(pf, "recRad") / Siccom.resolution;
			recFirst = readInt(pf, "recFirst");
			recInterval = readInt(pf, "recInterval");
			recNum = (int)(readDouble(pf, "recNumberPerSqM")*(Siccom.areaWidth*Siccom.areaHeight));
			minBleachProb = readDouble(pf, "minBleachProb");
			minDeathProb = readDouble(pf, "minDeathProb");
			
			if (fileName.startsWith("braCo"))
			numBranches = readInt(pf, "numBranches");

		} catch (IOException e) {
//			JOptionPane.showMessageDialog(frame, e, "Error",
//					JOptionPane.WARNING_MESSAGE);
		}
	}
	
	/**
	 * Read a double value from a {@link ParameterFile} object.
	 * 
	 * @param pf
	 *            The parameter file that has been read.
	 * @param key
	 *            The key string to the value
	 * @return the value
	 */
	public double readDouble(ParameterFile pf, String key) {
		String s = pf.getValue(key);

		double value = 0;
		try {
			value = Double.valueOf(s).doubleValue();
		} catch (NumberFormatException ex) {
//			JOptionPane.showMessageDialog(frame, ex, "Error",
//					JOptionPane.WARNING_MESSAGE);
		}
		return value;
	}

	/**
	 * Read a integer value from a {@link ParameterFile} object.
	 * 
	 * @param pf
	 *            The parameter file that has been read.
	 * @param key
	 *            The key string to the value
	 * @return the value
	 */
	public int readInt(ParameterFile pf, String key) {
		String str = pf.getValue(key);

		int val = 0;
		try {
			val = Integer.valueOf(str).intValue();
		} catch (NumberFormatException ex) {
//			JOptionPane.showMessageDialog(frame, ex, "Error",
//					JOptionPane.WARNING_MESSAGE);
		}
		return val;
	}

	/**
	 * Read a String from a {@link ParameterFile} object.
	 * 
	 * @param pf
	 *            The parameter file that has been read.
	 * @param key
	 *            The key string to the value
	 * @return the String
	 */
	public String readString(ParameterFile pf, String key)
	{
		String s = pf.getValue(key);
		
		String value = "";
		try{
			value = String.valueOf(s);
		} catch (NumberFormatException ex) {
//			JOptionPane.showMessageDialog(frame, ex, "Error",
//					JOptionPane.WARNING_MESSAGE);
		}
		return value;
	}
	
	public boolean readBoolean(ParameterFile pf, String key) {
		String str = pf.getValue(key);
	
		boolean val = true;
		try {
			val = Boolean.valueOf(str);
		} catch (NumberFormatException ex) {
	//		JOptionPane.showMessageDialog(frame, ex, "Error",
	//				JOptionPane.WARNING_MESSAGE);
		}
		return val;
	}
	
	/**
	 * Read a Color from a {@link ParameterFile} object.
	 * --> convert it to java color
	 * 
	 * @param colorName the name of the color
	 */	
	public Color getColor(String colorName) {
	    try {
	        // Find the field and value of colorName
	        Field field = Class.forName("java.awt.Color").getField(colorName);
	        return (Color)field.get(null);
	    } catch (Exception e) {
	        return null;
	    }
	}
	
	
	/**
	 * Initializes the parameters for the different coral groups from the parameter panels of the GUI console.
	 */
	public void initGUI()												
	{
		for (FileParameterPanel fpp : SiccomUI.panels)
			readGuiParam(fpp.table, fpp.fileName);
	}
	
	/**
	 * Reads the parameters from the GUI parameter panels
	 * @param tab the parameter table
	 * @param fN the file name
	 */
	public void readGuiParam(JTable tab, String fN)
	{
		if(fN.equals("environment.inf"))
		{
			sim.disturbMaxSize1 = Double.valueOf((String)tab.getValueAt(0, 0)) / Siccom.dimensionConv_meters;
			sim.disturbMinSize1 = Double.valueOf((String)tab.getValueAt(1, 0)) / Siccom.dimensionConv_meters;
			sim.disturbMaxRadius1 = sim.disturbMaxSize1 / 2;
			sim.disturbMinRadius1 = sim.disturbMinSize1 / 2;	
			sim.disturbMeanInterval1 = (int)(Double.valueOf((String)tab.getValueAt(2, 0))*1.0);
			sim.disturbSDPercent1 = Double.valueOf((String)tab.getValueAt(3, 0));
			sim.disturbSDInterval1 = (int)(sim.disturbSDPercent1*sim.disturbMeanInterval1/100);
			sim.disturbMaxNumber1 = Integer.valueOf((String)tab.getValueAt(4, 0));
			
			if (sim.disturbMaxSize1==0 || sim.disturbMinSize1==0 || 
					sim.disturbMaxNumber1==0 || sim.disturbMeanInterval1==0 )
			{
				sim.disser1 = false;
			}
			else
			{
				MersenneTwisterFast random = new MersenneTwisterFast(sim.seed());

				// sets the time for the first disturbance event to occur
				sim.dInterval = Math.round(random.nextGaussian() * sim.disturbSDInterval1 + sim.disturbMeanInterval1+1);
				while (sim.dInterval <= 0) {sim.dInterval = Math.round(random.nextGaussian() * sim.disturbSDInterval1 + sim.disturbMeanInterval1)+1;}
				
				sim.dist1 = new double[6];
				sim.dist1[0] = sim.disturbMaxRadius1;
				sim.dist1[1] = sim.disturbMinRadius1;
				sim.dist1[2] = (double)sim.disturbMeanInterval1;
				sim.dist1[3] = (double)sim.disturbSDInterval1;
				sim.dist1[4] = (double)sim.disturbMaxNumber1;
				sim.dist1[5] = (double)sim.dInterval;
			}
			
			
			sim.disturbMaxSize2 = Double.valueOf((String)tab.getValueAt(5, 0)) / Siccom.dimensionConv_meters;
			sim.disturbMinSize2 = Double.valueOf((String)tab.getValueAt(6, 0)) / Siccom.dimensionConv_meters;
			sim.disturbMaxRadius2 = sim.disturbMaxSize2 / 2;
			sim.disturbMinRadius2 = sim.disturbMinSize2 / 2;	
			sim.disturbMeanInterval2 = (int) (Double.valueOf((String)tab.getValueAt(7, 0))*1.0);
			sim.disturbSDPercent2 = Double.valueOf((String)tab.getValueAt(8, 0));
			sim.disturbSDInterval2 = (int)(sim.disturbSDPercent2*sim.disturbMeanInterval2/100);
			sim.disturbMaxNumber2 = Integer.valueOf((String)tab.getValueAt(9, 0));
			
			if (sim.disturbMaxSize2==0 || sim.disturbMinSize2==0 || 
					sim.disturbMaxNumber2==0 || sim.disturbMeanInterval2==0)
			{
				sim.disser2 = false;
			}
			else
			{
				MersenneTwisterFast random = new MersenneTwisterFast(sim.seed());
				// sets the time for the first disturbance event to occur
				sim.dInterval = Math.round(random.nextGaussian() * sim.disturbSDInterval2 + sim.disturbMeanInterval2+1);
				while (sim.dInterval <= 0) {sim.dInterval = Math.round(random.nextGaussian() * sim.disturbSDInterval2 + sim.disturbMeanInterval2)+1;}
				
				sim.dist2 = new double[6];
				sim.dist2[0] = sim.disturbMaxRadius2;
				sim.dist2[1] = sim.disturbMinRadius2;
				sim.dist2[2] = (double)sim.disturbMeanInterval2;
				sim.dist2[3] = (double)sim.disturbSDInterval2;
				sim.dist2[4] = (double)sim.disturbMaxNumber2;
				sim.dist2[5] = (double)sim.dInterval;
			}
			
			sim.maxDisBreakProb = Double.valueOf((String)tab.getValueAt(10, 0));
			sim.minDisBreakProb = Double.valueOf((String)tab.getValueAt(11, 0));
			
			sim.longTermYears = Integer.valueOf((String)tab.getValueAt(12, 0) );
			sim.minDHD = Double.valueOf((String)tab.getValueAt(13, 0));
			sim.maxHR = Double.valueOf((String)tab.getValueAt(14, 0));
			sim.minHR = Double.valueOf((String)tab.getValueAt(15, 0));
			sim.bleachInterval = (int)(Double.valueOf((String)tab.getValueAt(16, 0)) * 12);// input is in years --> calculated in month
			
			sim.coralRecImportFactor = Double.valueOf((String)tab.getValueAt(17, 0));
			
			sim.breakageProb = Double.valueOf((String)tab.getValueAt(18, 0));
			sim.fragProb = Double.valueOf((String)tab.getValueAt(19, 0));
			sim.fragRange = Double.valueOf((String)tab.getValueAt(20, 0)) / Siccom.dimensionConv_meters;
			
			sim.dieOvergrowthMas = Double.valueOf((String)tab.getValueAt(21, 0));
			sim.dieOvergrowthBra = Double.valueOf((String)tab.getValueAt(22, 0));

			sim.iniGrazingProb = Double.valueOf((String)tab.getValueAt(23, 0));
			sim.grazingProb = sim.iniGrazingProb;
			sim.iniAlgalThreshold = Double.valueOf((String)tab.getValueAt(24, 0));
			// calculate the grazing prob min and max relative to the initial grazing prob
			sim.minGrazingProb = sim.iniGrazingProb - sim.grazingProbHalfRange;
			if (sim.minGrazingProb < 0.0) sim.minGrazingProb = 0;
			sim.maxGrazingProb = sim.iniGrazingProb + sim.grazingProbHalfRange;
			
			// SCALING TURF
			sim.turfResolution = (int) (Double.valueOf((String)tab.getValueAt(25, 0))/Siccom.dimensionConv_meters);
			sim.cellNumWidth = (int)(Siccom.gridWidth / sim.turfResolution);
			sim.cellNumHeight = (int)(Siccom.gridHeight / sim.turfResolution);
			sim.cellWidth = sim.turfResolution;
			sim.cellHeight = sim.turfResolution;
			
			// To determine if smaller bleaching events occur or not
			sim.constantTemperature = Boolean.valueOf((String)tab.getValueAt(26, 0));
			
			// Gradual temperature increase over time
			gradualTempIncrease = Boolean.valueOf((String)tab.getValueAt(27, 0));
			totalTempIncrease = Double.valueOf((String)tab.getValueAt(28, 0));
			increaseYears = Integer.valueOf((String)tab.getValueAt(29, 0));
			yearlyTempIncrease = totalTempIncrease/increaseYears;
	
			
			sim.temp = new Temperature("TemperatureData_Chumbe", 
					sim, 
					gradualTempIncrease,
					yearlyTempIncrease);

			// Strength of the Competition Index
			sim.ciFactor = Double.valueOf((String)tab.getValueAt(30, 0));
			

			
		}
		else if (fN.equals("algaParam.inf"))
		{
			aName = 				(String) tab.getValueAt(0, 0);
			colorString = 			(String) tab.getValueAt(1, 0);
			algaColor = 			getRGBColor(colorString);
			aMaxRadius = 			Double.valueOf((String)tab.getValueAt(2, 0)) / Siccom.resolution;
			aMaxHeight = 			Double.valueOf((String)tab.getValueAt(3, 0)) / Siccom.resolution;
			algaMaxAge = 			Integer.valueOf((String)tab.getValueAt(4, 0));
			aGrowthRate = 			Double.valueOf((String)tab.getValueAt(5, 0)) / (Siccom.resolution*10);
			aFragmentationHeight = 	Double.valueOf((String)tab.getValueAt(6, 0)) / Siccom.resolution;
			aMaxFragNum	=			Integer.valueOf((String)tab.getValueAt(7, 0));
			aFragmentSize = 		Double.valueOf((String)tab.getValueAt(8, 0)) / Siccom.resolution;
			aFragRange = 			Double.valueOf((String)tab.getValueAt(9, 0)) / Siccom.dimensionConv_meters;
			algalCoverPercent = 	Double.valueOf((String)tab.getValueAt(10, 0));
			algaRecRad = 			Double.valueOf((String)tab.getValueAt(11, 0)) / Siccom.resolution;
			algaRecFirst = 			Integer.valueOf((String)tab.getValueAt(12, 0));
			algaRecInterval = 		Integer.valueOf((String)tab.getValueAt(13, 0));
			algaRecNumPerSqM = 		Double.valueOf((String)tab.getValueAt(14, 0));
			algaRecNum = 			(int)( algaRecNumPerSqM * Siccom.areaWidth * Siccom.areaHeight );
			algalCover = 			Siccom.totalArea / 100 * algalCoverPercent;
		}
		
		else if (fN.startsWith("maCoParam") || fN.startsWith("braCoParam"))
		{
		
			name = (String) tab.getValueAt(0, 0);
			colorString = (String) tab.getValueAt(1, 0);	
			color = getRGBColor(colorString);
			ci = Double.valueOf((String) tab.getValueAt(2, 0));
			maxIniRadius = Double.valueOf((String)tab.getValueAt(3, 0)) / Siccom.resolution;
			maxRadius = Double.valueOf((String)tab.getValueAt(4, 0)) / Siccom.resolution;
			growthRate = Double.valueOf((String)tab.getValueAt(5, 0)) * Siccom.dimensionConv_milimeters; // / (resolution * 10) / 12;
			coveredArea = Double.valueOf((String)tab.getValueAt(6, 0));
			diamAtMaturity = Double.valueOf((String)tab.getValueAt(7, 0)) / Siccom.resolution;
			surfaceFactor = Double.valueOf((String)tab.getValueAt(8, 0));
			propagulesPerSqCm = Double.valueOf((String)tab.getValueAt(9, 0));
			retainFactor = Double.valueOf((String)tab.getValueAt(10, 0));
			recRad = Double.valueOf((String)tab.getValueAt(11, 0)) / Siccom.resolution;
			recFirst = Integer.valueOf((String)tab.getValueAt(12, 0));
			recInterval = Integer.valueOf((String)tab.getValueAt(13, 0));
			recNum = (int)(Double.valueOf((String)tab.getValueAt(14, 0))*(Siccom.areaWidth*Siccom.areaHeight));
			minBleachProb = Double.valueOf((String)tab.getValueAt(15, 0));
			minDeathProb = Double.valueOf((String)tab.getValueAt(16, 0));
			
			if (fN.startsWith("braCo"))
			{
				numBranches = Integer.valueOf((String)tab.getValueAt(17, 0));
				
				BranchingGroup braco = new BranchingGroup(	sim,
															name,
															colorString,
															color,
															ci,
															maxIniRadius,
															maxRadius, 
															growthRate,
															coveredArea,
															diamAtMaturity,
															surfaceFactor,
															propagulesPerSqCm,
															retainFactor,
															recRad,
															recFirst,
															recInterval,
															recNum,
															minBleachProb,
															minDeathProb,
															numBranches);
	
				sim.braCoGroups.add(braco);
			}
			else
			{
				MassiveGroup maco = new MassiveGroup(	sim,
														name,
														colorString,
														color,
														ci,
														maxIniRadius,
														maxRadius, 
														growthRate,
														coveredArea,
														diamAtMaturity,
														surfaceFactor,
														propagulesPerSqCm,
														retainFactor,
														recRad,
														recFirst,
														recInterval,
														recNum,
														minBleachProb,
														minDeathProb);
	
				sim.maCoGroups.add(maco);
			}
		}
	}
	
	

	
	
}
