package siccom.sim;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;

import sim.util.Bag;

/**
 * Writes the output files to the output folder
 * @author andreas
 *
 */

public class OutputWriter
{

	public static final long serialVersionUID = 6948161854909261168L;

	Siccom sim;
	

	/**
	 * File for individual output
	 */
	private File indOutFile;
	/**
	 * The print writer for the individual output file
	 */
	private PrintWriter indWriter;
	/**
	 * File for grouped output
	 */
	private File groupOutFile;

	/**
	 * The print writer for the grouped output file
	 */
	private PrintWriter groupWriter;
	/**
	 * File for temperature output
	 */
	private File tempOutFile;
	/**
	 * The print writer for the temperature output file
	 */
	private PrintWriter tempWriter;
	/**
	 * The month as name
	 */
	private String month;
	/**
	 * The count for years
	 */
	private double year;
	/**
	 * The file for disturbance output data
	 */
	private File disOutFile;
	/**
	 * The print writer for disturbance data
	 */
	private PrintWriter disWriter;

	/**
	 * This class
	 */
	Config conf;

	public OutputWriter(Siccom sim)
	{
		this.sim = sim;
		
		this.conf = sim.conf;

		writeParameters();
	}

	/**
	 * setup of output folder and output files
	 * @param sim
	 */
	public void initOutput(Siccom sim) {
		File f = new File(sim.outputPath);
		f.mkdir();
		
		indOutFile = new File(sim.outputPath + "/individualOutput.dat");
		groupOutFile = new File(sim.outputPath + "/groupedOutput.dat");
		
		tempOutFile = new File(sim.outputPath + "/temperatureOutput.dat");
		
		disOutFile = new File(sim.outputPath + "/disturbance.dat");
		

		try 
		{
			indWriter = new PrintWriter( new BufferedWriter(new FileWriter(indOutFile)) );
			indWriter.write("Step\tMonth\tYear\tName\tColor\txPos\tyPos\tRadius\tDiameter\tSize\tAge\n");

			groupWriter = new PrintWriter( new BufferedWriter(new FileWriter(groupOutFile)));
			groupWriter.write("Step\tMonth\tYear\tName\tColor\tAbundance\tRelativeCover\n");			
			
			disWriter = new PrintWriter( new BufferedWriter(new FileWriter(disOutFile)));
			disWriter.write("Step\tMode\txLoc\tyLoc\tDiameter\n");
	
			
			tempWriter = new PrintWriter(new BufferedWriter(new FileWriter(tempOutFile)));
			tempWriter.write("Step\tMonthMean\tLTSM\tthreshHeatRate\theatRate\tbleached\n");
			tempWriter.flush();
			
			
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
	
	/**
	 * write disturbance output
	 * @param steps
	 * @param mode
	 * @param xLoc
	 * @param yLoc
	 * @param size
	 */
	public void disturbanceOutput(int steps, String mode, double xLoc, double yLoc, double size)
	{
		disWriter.append( steps + "\t"  );  
		disWriter.append( mode + "\t"  );  
		disWriter.append( conf.numform_2.format(xLoc*Siccom.dimensionConv_meters) + "\t");
		disWriter.append( conf.numform_2.format(yLoc*Siccom.dimensionConv_meters) + "\t");
		disWriter.append( conf.numform_2.format(size*2*Siccom.dimensionConv_meters) + "\n");
		disWriter.flush();
	}
	
	public void temperatureOutput(int steps, double monthMean, double ltsm, double minHeatRate, double heatRate, boolean bleachEvent)
	{
		tempWriter.append( steps + "\t");
		tempWriter.append( conf.numform_2.format(monthMean) + "\t");
		tempWriter.append( conf.numform_2.format(ltsm) + "\t");
		tempWriter.append( conf.numform_3.format(minHeatRate) + "\t");
		tempWriter.append( conf.numform_3.format(heatRate) + "\t");
		tempWriter.append( bleachEvent + "\n");
		tempWriter.flush();
	}
	
	
	/**
	 * write individual output
	 * @param steps
	 */
	public void individualOutput(int steps)
	{
		Bag m = sim.massiveCorals.getAllObjects();
		for (int i=0; i<m.size(); i++ )
		{	
//			steps = sim.steps;
			MassiveCoral mc = (MassiveCoral) m.objs[i];
			indWriter.append( steps + "\t"  );  
			indWriter.append( getMonth(steps) +"\t"  );  
			indWriter.append(  getYear(steps) + "\t" );
			indWriter.append( mc.getName() + "\t");
			indWriter.append( conf.numform3.format(mc.color.getRed()) + "," 
							  + conf.numform3.format(mc.color.getGreen()) + "," 
							  + conf.numform3.format(mc.color.getBlue())+ "\t");
			indWriter.append( conf.numform_2.format(mc.xPos) + "\t");
			indWriter.append( conf.numform_2.format(mc.yPos) + "\t");
			indWriter.append( conf.numform_2.format(mc.radius*Siccom.resolution) + "\t");
			indWriter.append( conf.numform_2.format(mc.diameter*Siccom.resolution) + "\t");
			indWriter.append( conf.numform_8.format(mc.sendSize()*Math.pow(Siccom.resolution, 2.0)) + "\t");
			indWriter.append( mc.getAge() + "\n");
		}
		
		Bag b = sim.branchingCorals.getAllObjects();
		for (int i=0; i<b.size(); i++ )
		{	
			BranchingCoral bc = (BranchingCoral) b.objs[i];
			indWriter.append( steps + "\t"  );  
			indWriter.append( getMonth(steps) +"\t"  );  
			indWriter.append(  getYear(steps) + "\t" );
			indWriter.append( bc.getName() + "\t");
			indWriter.append( conf.numform3.format(bc.color.getRed()) + "," 
					  + conf.numform3.format(bc.color.getGreen()) + "," 
					  + conf.numform3.format(bc.color.getBlue())+ "\t");
			indWriter.append( conf.numform_2.format(bc.xPos) + "\t");
			indWriter.append( conf.numform_2.format(bc.yPos) + "\t");
			indWriter.append( conf.numform_2.format(bc.radius*Siccom.resolution) + "\t");
			indWriter.append( conf.numform_2.format(bc.diameter*Siccom.resolution) + "\t");
			indWriter.append( conf.numform_8.format(bc.sendSize()*Math.pow(Siccom.resolution, 2.0)) + "\t");
			indWriter.append( bc.getAge() + "\n");
			
		}
		indWriter.flush();
	}
	/**
	 * Determine the right month
	 * @param steps
	 * @return
	 */
	private String getMonth(int steps) 
	{
		if (steps%12  == 0) month = "January  ";
		if (steps%12  == 1) month = "February ";
		if (steps%12  == 2) month = "March    ";
		if (steps%12  == 3) month = "April    ";
		if (steps%12  == 4) month = "May      ";
		if (steps%12  == 5) month = "June     ";
		if (steps%12  == 6) month = "July     ";
		if (steps%12  == 7) month = "August   ";
		if (steps%12  == 8) month = "September";
		if (steps%12  == 9) month = "October  ";
		if (steps%12  == 10) month = "November";
		if (steps%12  == 11) month = "December";
		return month;
	}
	
	private double getYear(int steps) 
	{
		year = steps/12.0;
		return year;
	}
	
	/**
	 * Writes the grouped output file
	 */
	public void groupedOutput(int steps)
	{
		for (MassiveGroup mG : sim.maCoGroups)
		{
			groupWriter.append( steps + "\t"  );  
			groupWriter.append( getMonth(steps) +"\t"  );  
			groupWriter.append(  conf.numform_3.format(getYear(steps)) + "\t" );
			if (mG.name.length() >= 8) groupWriter.append( mG.name + "\t");
			else 	groupWriter.append( mG.name + "\t");
			groupWriter.append( conf.numform3.format(mG.color.getRed()) + "," 
							  + conf.numform3.format(mG.color.getGreen()) + "," 
							  + conf.numform3.format(mG.color.getBlue())+ "\t");
			groupWriter.append(mG.getNum() + "\t");
			groupWriter.append(conf.numform_1.format(mG.getCover()));
			groupWriter.append("\n");
		}
		
		for (BranchingGroup bG : sim.braCoGroups)
		{
			groupWriter.append( steps + "\t"  );  
			groupWriter.append( getMonth(steps) +"\t"  );  
			groupWriter.append(  conf.numform_3.format(getYear(steps)) + "\t" );
			if (bG.name.length() >= 8) groupWriter.append( bG.name + "\t");
			else 	groupWriter.append( bG.name + "\t");
			groupWriter.append( conf.numform3.format(bG.color.getRed()) + "," 
					  + conf.numform3.format(bG.color.getGreen()) + "," 
					  + conf.numform3.format(bG.color.getBlue())+ "\t");
			groupWriter.append(bG.getNum() + "\t");
			groupWriter.append(conf.numform_1.format(bG.getCover()));
			groupWriter.append("\n");
		}
		groupWriter.append( steps + "\t"  );  
		groupWriter.append( getMonth(steps) +"\t"  );  
		groupWriter.append(  conf.numform_3.format(getYear(steps)) + "\t" );
		groupWriter.append( "Algae" + "\t");
		groupWriter.append( conf.numform3.format(sim.conf.algaColor.getRed()) + "," 
				  + conf.numform3.format(sim.conf.algaColor.getGreen()) + "," 
				  + conf.numform3.format(sim.conf.algaColor.getBlue())+ "\t");
		groupWriter.append( sim.numAlgae + "\t");
		groupWriter.append(conf.numform_1.format(sim.algalCoverPercent) + "\n");
		groupWriter.flush();
	}
	
	/**
	 * Write out the parameter settings to the simulation output file
	 */
	private void writeParameters() 
	{
		System.out.println();
		System.out.println("---------------  PARAMETERS  ---------------");
		System.out.println();
		System.out.println("Main Parameters");
		System.out.println( (int)(Siccom.gridWidth* Siccom.dimensionConv_meters) + "\t|\t" + "areaWidth" + "\t|\t ---" ) ;
		System.out.println( (int)(Siccom.gridHeight* Siccom.dimensionConv_meters) + "\t|\t" + "areaHeight" + "\t|\t ---");
		System.out.println(Siccom.resolution + "\t|\t" + "resolution" + "\t|\t ---");
//		System.out.println(conf.numform2.format(Siccom.totalArea*Math.pow(Siccom.meterConv, 2)) + "\t|\t" + "totalArea");
		System.out.println(Siccom.maCoGroupNum + "\t|\t" + "maCoNum " + "\t|\t ---");
		System.out.println(Siccom.braCoGroupNum + "\t|\t" + "braCoNum" + "\t|\t ---");
		System.out.println(Siccom.indivOutInter + "\t|\t" + "indivOutInter" + "\t|\t ---");
		System.out.println();
		
		System.out.println("Environment Parameters");
//		System.out.println("disser1\t\t\t" + sim.disser1);
		System.out.println( sim.disturbMaxSize1*Siccom.dimensionConv_meters + "\t|\t" + "disturbMaxSize1\t" + "\t|\t ---" );
		System.out.println( sim.disturbMinSize1*Siccom.dimensionConv_meters + "\t|\t" + "disturbMinSize1\t" + "\t|\t ---" );
		System.out.println( sim.disturbMeanInterval1 + "\t|\t" + "disturbMeanInterval1" + "\t|\t ---" );
		System.out.println( sim.disturbSDPercent1 + "\t|\t" + "disturbSDInterval1" + "\t|\t ---" );
		System.out.println( sim.disturbMaxNumber1 + "\t|\t" + "disturbMaxNumber1" + "\t|\t ---" );
//		System.out.println("disser1\t\t\t" + sim.disser2);
		System.out.println( sim.disturbMaxSize2*Siccom.dimensionConv_meters + "\t|\t" + "disturbMaxSize2\t" + "\t|\t ---" );
		System.out.println( sim.disturbMinSize2*Siccom.dimensionConv_meters + "\t|\t" + "disturbMinSize2\t" + "\t|\t ---" );
		System.out.println( sim.disturbMeanInterval2 + "\t|\t" + "disturbMeanInterval2" + "\t|\t ---" );
		System.out.println( sim.disturbSDPercent2 + "\t|\t" + "disturbSDInterval2" + "\t|\t ---" );
		System.out.println( sim.disturbMaxNumber2 + "\t|\t" + "disturbMaxNumber2" + "\t|\t ---" );
		System.out.println( sim.minDHD + "\t|\t" + "minDHD" + "\t\t\t|\t ---" );
		System.out.println( sim.maxHR + "\t|\t" + "maxHR" + "\t\t\t|\t ---" );
		System.out.println( sim.bleachInterval / 12 + "\t|\t" + "bleachInterval\t" + "\t|\t ---" );
		System.out.println( sim.coralRecImportFactor + "\t|\t" + "coralRecImportFactor" + "\t|\t ---" );

		System.out.println( sim.breakageProb + "\t|\t" + "breakageProb\t" + "\t|\t ---" );
		
		System.out.println( sim.dieOvergrowthMas + "\t|\t" + "dieOvergrowthMas" + "\t|\t ---" );
		System.out.println( sim.dieOvergrowthBra + "\t|\t" + "dieOvergrowthBra" + "\t|\t ---" );
		
		System.out.println( sim.grazingProb + "\t|\t" + "grazingProb\t" + "\t|\t ---" );
		System.out.println( sim.iniAlgalThreshold + "\t|\t" + "iniAlgalThreshold" + "\t|\t ---" );
		System.out.println( (int)(sim.turfResolution*Siccom.dimensionConv_meters) + "\t|\t" + "turfResolution\t" + "\t|\t ---" );
		System.out.println( sim.constantTemperature + "\t|\t" + "constTemp\t" + "\t|\t ---" );
		System.out.println( conf.gradualTempIncrease + "\t|\t" + "gradualTempIncrease" + "\t|\t ---" );
		System.out.println( conf.totalTempIncrease + "\t|\t" + "totalTempIncrease" + "\t|\t ---" );
		System.out.println( conf.increaseYears + "\t|\t" + "increaseYears\t" + "\t|\t ---" );
		System.out.println( sim.ciFactor + "\t|\t" + "ciFactor\t" + "\t|\t ---" );
		
		
		System.out.println();
		
		System.out.println("Algae Parameters");
		System.out.println("Alga\t\t|\tname\t\t" + "\t|\t ---" );
		System.out.println( conf.numform3.format(sim.conf.algaColor.getRed()) + "," 
				  + conf.numform3.format(sim.conf.algaColor.getGreen()) + "," 
				  + conf.numform3.format(sim.conf.algaColor.getBlue()) + "\t|\t" + "color\t\t" + "\t|\t ---" );
		System.out.println( conf.aMaxRadius*Siccom.resolution + "\t\t|\t" + "aMaxRadius\t" + "\t|\t ---" );
		System.out.println( conf.aMaxHeight*Siccom.resolution + "\t\t|\t" + "aMaxHeight\t" + "\t|\t ---" );
		System.out.println( conf.algaMaxAge + "\t\t|\t" + "algaMaxAge\t" + "\t|\t ---" );
		System.out.println( conf.aGrowthRate*Siccom.resolution*10 + "\t\t|\t" + "aGrowthRate\t" + "\t|\t ---" );
		System.out.println( conf.aFragmentationHeight*Siccom.resolution + "\t\t|\t" + "aFragmentationHeight" + "\t|\t ---" );
		System.out.println( conf.aMaxFragNum + "\t\t|\t" + "aMaxFragNum" + "\t\t|\t ---" );
		System.out.println( conf.aFragmentSize*Siccom.resolution + "\t\t|\t" + "aFragmentSize\t" + "\t|\t ---" );
		System.out.println( conf.aFragRange*Siccom.dimensionConv_meters + "\t\t|\t" + "aFragRange\t" + "\t|\t ---" );
		System.out.println( conf.algalCoverPercent + "\t\t|\t" + "algalCoverPercent" + "\t|\t ---" );
		System.out.println( conf.algaRecRad*Siccom.resolution + "\t\t|\t" + "algaRecRad\t" + "\t|\t ---" );
		System.out.println( conf.algaRecFirst + "\t\t|\t" + "algaRecFirst\t" + "\t|\t ---" );
		System.out.println( conf.algaRecInterval + "\t\t|\t" + "algaRecInterval\t" + "\t|\t ---" );
		System.out.println( conf.algaRecNumPerSqM + "\t\t|\t" + "algaRecNumPerSqM" + "\t|\t ---" );
		System.out.println();
		
		for(MassiveGroup mg : sim.maCoGroups)
		{
			System.out.println("Coral Parameters " + mg.name);
			System.out.println( mg.name + "\t\t\t|\t" + "name\t\t" + "\t|\t ---" );
			System.out.println( conf.numform3.format(mg.color.getRed()) + "," 
					  + conf.numform3.format(mg.color.getGreen()) + "," 
					  + conf.numform3.format(mg.color.getBlue()) + "\t\t|\t" + "color\t\t" + "\t|\t ---" );
			System.out.println( mg.CI + "\t\t\t|\t" + "CI\t\t" + "\t|\t ---" );
			System.out.println( conf.numform_2.format(mg.maxIniRadius*Siccom.resolution) + "\t\t\t|\t" + "maxIniRadius\t" + "\t|\t ---" );
			System.out.println( conf.numform_2.format(mg.maxRadius*Siccom.resolution) + "\t\t\t|\t" + "maxRadius\t" + "\t|\t ---" );
			System.out.println( conf.numform_2.format(mg.growthRate/Siccom.dimensionConv_milimeters) + "\t\t\t|\t" + "growthRate\t" + "\t|\t ---" );
			System.out.println( conf.numform_2.format(mg.coverPercent) + "\t\t\t|\t" + "coveredArea\t" + "\t|\t ---" );
			System.out.println( mg.diamAtMaturity*Siccom.resolution + "\t\t\t|\t" + "diamAtMaturity\t" + "\t|\t ---" );
			System.out.println( mg.surfaceFactor + "\t\t\t|\t" + "surfaceFactor\t" + "\t|\t ---" );
			System.out.println( conf.numform_2.format(mg.propagulesPerSqCm) + "\t\t\t|\t" + "propagulesPerSqCm" + "\t|\t ---" );
			System.out.println( mg.retainFactor + "\t\t\t|\t" + "retainFactor\t" + "\t|\t ---" );
			System.out.println( conf.numform_2.format(mg.recRad*Siccom.resolution) + "\t\t\t|\t" + "recRad\t\t" + "\t|\t ---" );
			System.out.println( mg.recFirst + "\t\t\t|\t" + "recFirst\t" + "\t|\t ---" );
			System.out.println( mg.recInterval + "\t\t\t|\t" + "recInterval\t" + "\t|\t ---" );
			System.out.println( mg.fixRecImport/(Siccom.areaWidth*Siccom.areaHeight) + "\t\t\t|\t" + "recNumberPerSqM\t" + "\t|\t ---" );
			System.out.println( mg.minBleachProb + "\t\t\t|\t" + "minBleachProb\t" + "\t|\t ---" );
			System.out.println( mg.minDeathProb + "\t\t\t|\t" + "minDeathProb\t" + "\t|\t ---" );
			System.out.println();
		}
		
		for(BranchingGroup mg : sim.braCoGroups)
		{
			System.out.println("Coral Parameters " + mg.name);
			System.out.println( mg.name + "\t\t\t|\t" + "name\t\t" + "\t|\t ---" );
			System.out.println( conf.numform3.format(mg.color.getRed()) + "," 
					  + conf.numform3.format(mg.color.getGreen()) + "," 
					  + conf.numform3.format(mg.color.getBlue()) + "\t\t|\t" + "color\t\t" + "\t|\t ---" );
			System.out.println( mg.CI + "\t\t\t|\t" + "CI\t\t" + "\t|\t ---" );
			System.out.println( conf.numform_2.format(mg.maxIniRadius*Siccom.resolution) + "\t\t\t|\t" + "maxIniRadius\t" + "\t|\t ---" );
			System.out.println( conf.numform_2.format(mg.maxRadius*Siccom.resolution) + "\t\t\t|\t" + "maxRadius\t" + "\t|\t ---" );
			System.out.println( conf.numform_2.format(mg.growthRate/Siccom.dimensionConv_milimeters) + "\t\t\t|\t" + "growthRate\t" + "\t|\t ---" );
			System.out.println( conf.numform_2.format(mg.coverPercent) + "\t\t\t|\t" + "coveredArea\t" + "\t|\t ---" );
			System.out.println( mg.diamAtMaturity*Siccom.resolution + "\t\t\t|\t" + "diamAtMaturity\t" + "\t|\t ---" );
			System.out.println( mg.surfaceFactor + "\t\t\t|\t" + "surfaceFactor\t" + "\t|\t ---" );
			System.out.println( conf.numform_2.format(mg.propagulesPerSqCm) + "\t\t\t|\t" + "propagulesPerSqCm" + "\t|\t ---" );
			System.out.println( mg.retainFactor + "\t\t\t|\t" + "retainFactor\t" + "\t|\t ---" );
			System.out.println( conf.numform_2.format(mg.recRad*Siccom.resolution) + "\t\t\t|\t" + "recRad\t\t" + "\t|\t ---" );
			System.out.println( mg.recFirst + "\t\t\t|\t" + "recFirst\t" + "\t|\t ---" );
			System.out.println( mg.recInterval + "\t\t\t|\t" + "recInterval\t" + "\t|\t ---" );
			System.out.println( mg.fixRecImport/(Siccom.areaWidth*Siccom.areaHeight) + "\t\t\t|\t" + "recNumberPerSqM\t" + "\t|\t ---" );
			System.out.println( mg.minBleachProb + "\t\t\t|\t" + "minBleachProb\t" + "\t|\t ---" );
			System.out.println( mg.minDeathProb + "\t\t\t|\t" + "minDeathProb\t" + "\t|\t ---" );
			System.out.println( mg.numBranches + "\t\t\t|\t" + "numBranches\t" + "\t|\t ---" );
			System.out.println();
		}
		System.out.println();
		System.out.println("--------------- --------- ---------------");
		System.out.println();
	}
	
	
	
}
