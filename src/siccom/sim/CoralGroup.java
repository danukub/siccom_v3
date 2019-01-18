package siccom.sim;

import java.awt.Color;

/**
 * Defines the parameters for a {@link CoralGroup} which then can be 
 * used to instantiate the coral individuals.
 * <li>Parameters are directly read from the parameter files (*.inf)</li>
 *
 * @author kubicek
 * @version 1.0
 */

public abstract class CoralGroup
{
	/**
	 * The simulation in which the group is in
	 */
	Siccom sim;							// the simulation
	/**
	 * The name of the Coral Group (Species name)
	 */
	public String name;		
	
	public String colorString;
	/**
	 * The color to represent the colony in the GUI
	 */
	public Color color;						
	/**
	 * The competition index for individual corals of the group
	 */
	public double CI; 	
	/**
	 * The maximum radius the individual colony can have at initialisation
	 */
	public double maxIniRadius;
	/**
	 * The maximum radius the individual colony can reach
	 */
	public double	maxRadius;					
	/**
	 * The set growth rate of a coral colony
	 */
	public double iniGrowthRate;
	/**
	 * The momentary growth rate for a single coral colony	
	 */
	public double	growthRate;					
	/**
	 * The proportion of the whole area that is covered by the coral group
	 */
	public double coverPercent;

	// Reproduction
	public double diamAtMaturity;
	public double surfaceFactor;
	public double propagulesPerSqCm;
	public double retainFactor;
	
	
	/**
	 * The radius of a recruit at instantiation
	 */
	public double recRad; 					
	/**
	 * The month of a recruitment event as number
	 */
	public int	recFirst;
	/**
	 * The interval of recruitment events
	 */
	public int	recInterval;			
	/**
	 * The number of recruits per recruitment event
	 */
	public int	fixRecImport;					
	

	// TEMPERATURE
	public double minBleachProb;
	public double minDeathProb;
	
	/**
	 * The absolute size of the covered area	
	 */
	public double coveredArea;
	
	/**
	 * Constructor
	 * @param sim 			the simulation in which the group is set up
	 * @param name 			the name of the coral group
	 * @param color 		the color for graphical output
	 * @param CI 			the competition index of the coral group 
	 * @param maxIniRadius the maximum initial radius for an agent
	 * @param maxRadius		the maximum radius an agent can get
	 * @param growthRate	the growth rate for a coral
	 * @param coverPercent the percentage of covered area
	 * @param recRad		the radius of a coral recruit
	 * @param recFirst		the month of first recruitment in a year as integer value
	 * @param recInterval	the interval of recruitment
	 * @param recNumber		the number of recruits per recruitment event
	 */
	public CoralGroup(	Siccom sim,
						String name,
						String colorString,
						Color color,
						double CI,
						double maxIniRadius,
						double maxRadius,
						double growthRate,
						double coverPercent,
						double diamAtMaturity,
						double surfaceFactor,
						double propagulesPerSqCm,
						double retainFactor,
						double recRad,
						int recFirst,
						int recInterval,
						int recNumber,
						double minBleachProb,
						double minDeathProb)
	{
		this.sim 				= sim;
		this.name 				= name;
		this.colorString		= colorString;
		this.color 				= color;
		this.CI 				= CI;
		this.maxIniRadius 		= maxIniRadius;
		this.maxRadius 			= maxRadius;
		iniGrowthRate 			= growthRate;
		this.growthRate    		= iniGrowthRate;
		this.coverPercent 		= coverPercent;
		coveredArea 			= Siccom.totalArea/ 100 * coverPercent; 
		this.diamAtMaturity  	= diamAtMaturity;
		this.surfaceFactor		= surfaceFactor;
		this.propagulesPerSqCm 	= propagulesPerSqCm;
		this.retainFactor       = retainFactor;
		this.recRad 			= recRad;
		this.recFirst 			= recFirst;
		this.recInterval 		= recInterval;
		this.fixRecImport 			= recNumber;
		this.minBleachProb		= minBleachProb;
		this.minDeathProb 		= minDeathProb;
	}
	
			
}
