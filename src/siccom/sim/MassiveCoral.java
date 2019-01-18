package siccom.sim;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.util.ArrayList;

import org.jhotdraw.geom.Polygon2D;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.gui.SimpleColorMap;

public class MassiveCoral extends SimplePortrayal2D implements Steppable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -3129930872986577803L;

	 /**
	 * This class defines {@link MassiveCoral}
	 * --> constructor, methods for Coral
	 * 
	 * @author kubicek
	 * @version 1.0
	 * 
	 */
	
	/*
	 * VARIABLES
	 */
	/*
	 *  Generals
	 */
	/**
	 * The simulation where the coral is in
	 */
	public Siccom sim;
	/**
	 * Name of the coral group
	 */
	public String name;
	/**
	 * A bag to collect all neighbors
	 */
	public Bag neighbours;
	/**
	 * The graphical information for {@link SimplePortrayal2D}
	 */
	public Graphics2D graph;
	
	/**
	 * The drawing information for {@link SimplePortrayal2D}
	 */
	public DrawInfo2D inf;
	
	
	/*
	 *  Colony
	 */
	Double2D me;
	/**
	 * The x-Position of the colony center
	 */
	public double	xPos;
	/**
	 * The y-Position of the colony center
	 */
	public double 	yPos;
	/**
	 * The set growth rate of the colony
	 */
	public double 	initGrowthRate;
	/**
	 * The momentary growth rate of the colony. Can vary due to interaction.
	 */
	public double 	growthRate;			
	/**
	 * The radius of a colony. 
	 */
	public double radius;						
	/**
	 * The maximum radius a massive colony can reach	
	 */
	public double maximumRadius;
	/**
	 * The diameter of the colony
	 */
	public double diameter;										
	/**
	 * The size of the colony
	 */
	public double size;											
	/**
	 * The range in which the individual looks for neighbours
	 */
	public double range;							
	/**
	 * The desired initial color for representation in the GUI 
	 */
	Color initColor;												
	/**
	 * The actual momentary color for representation in the GUI 
	 */
	Color color;													
	/** 
	 * The life status of the coral colony
	 */
	private boolean alive;	
	
	private boolean bleachDead=false;
	
	/**
	 * Measure to determine for how long is the colony in the simulation
	 */
	public int age;
	
	public double CI;
	
	public MassiveGroup mG;
	
	/*
	 *  Bleaching
	 */
	/** 
	 *  The bleach status
	 */
	boolean bleached;
	/**
	 * The grade of bleaching
	 */
	double bleachGrade;

	/**
	 * The recovery rate from a bleaching event
	 */
	double recoveryRate = 17;
	/**
	 * The percentage of a bleached coral that is covered by algae.
	 */
	public double algCoverage;

	/*
	 *  CORAL POLYGON SHAPE	
	 */
	/**
	 * The shape of the coral
	 */
	 Polygon2D.Double p;
	
	/**
	 * The number branches a massive coral has -- determines the number of corners a polygon has
	 */
	private int numBranches = 24;
	/**
	 * List of all branches of the coral
	 */
	public ArrayList<Branch> branches = new ArrayList<Branch>(numBranches);
	
	/**
	 * The actual maximum branch length
	 */
	public double maxLength;
	/**
	 * The maximum allowed branch length
	 */
	public double maximumBranchLength;
	
	private double interArea;
	
	// Different getters and setters for the console
	public String getName() { return name; }
	public double getCI() { return CI; }
	public double sendXPos() { return xPos; }
	public double sendYPos() { return yPos; }
	public int getAge() { return age; }
	public double sendSize() { return size; }
	public double getGrowthRate() { return initGrowthRate/Siccom.dimensionConv_milimeters; }
	public double getRadius() { return radius; }
	public boolean getBleached() { return bleached; }
	public double getMinBleachProb() { return mG.minBleachProb; }
	public double getMinDeathProb() { return mG.minDeathProb; }
	
	
	/**
	   * Constructor for massive coral
	   * @param sim the simulation where coral is situated in 
	   * @param name the name of the coral group
	   * @param color the color of the coral group
	   * @param maxRadius the maximum radius a colony can reach
	   * @param xPos x-position
	   * @param yPos y-position
	   * @param radius initial radius
	   * @param growthRate the growth rate of the coral
	   * @param age the time the coral is in the simulation
	   * 
	   */
	
	public MassiveCoral(	Siccom sim, 
							String name,
							Color color,
							double CI,
							double maxRadius,
							double xPos, 
							double yPos, 
							double radius,
							double growthRate, 
							int age,
							MassiveGroup mG) 
	{
		this.sim = sim;
		this.name = name;
		this.xPos = xPos;						//xPos;
		this.yPos = yPos;
		this.radius = radius;
		initGrowthRate = growthRate;
		this.growthRate = initGrowthRate;
		initColor = color;
		this.CI = CI;
		this.age = age;
		this.mG = mG;
		diameter = 2*radius;
		me = new Double2D(xPos, yPos);		// get my own location
		size = Math.PI * Math.pow(radius, 2);
		alive = true;	
		this.maximumRadius = maxRadius;
		
		// create axes of the coral
		for (int i=1; i <= numBranches ; i++)
		{
			Branch b = new Branch(xPos, yPos, radius, numBranches, i, growthRate);
			branches.add(b);
		}
		
		// set up the polygon shape
		p = new Polygon2D.Double();
		for (int i=0; i < branches.size(); i++)
		{
			Branch b =  branches.get(i);
			int length = (int)b.getBranchLength();
			int rad = length;
			int div = (int)b.multiplier;
			double endX = b.startX + (rad*Math.cos( (2*Math.PI/numBranches )*div ));
	        double endY = b.startY - (rad*Math.sin( (2*Math.PI/numBranches)*div ));

	        p.addPoint(endX, endY);
		}
		
	}
	
	/** 
	 * Step routine of a massive coral colony
	 */
	public void step(SimState state)
	{
			if (alive)
			{
			
				checkBleaching();
				if (bleached){
					 growthRate = initGrowthRate * (1 - (this.bleachGrade / 100)); 
					recover();
				}
				else
					// reset the growth rate at every step 
					growthRate = initGrowthRate;
				
				if (!(radius >= maximumRadius))
				{
					if (alive) getAvRadius();
					
					if (alive) interactMassiveCorals();
					if (alive) interactBranchingCorals();
					if (radius<=sim.conf.aMaxRadius || bleached) interactAlgae();
					if (age <= sim.conf.interactTurfAge) interactTurf();
			
					double percentOvergrowth = 100/size*interArea;
					if (percentOvergrowth>=sim.dieOvergrowthMas) die(); 	
					interArea = 0.0;
					
					if (alive)
					{
						grow();
					}
				}				
				reschedule(1.0);
				age += 1;
			}	
		}

	
	/**
	 * Interaction with neighboring macroalgae
	 */
	private void interactAlgae() {
		neighbours = sim.algae.getObjectsExactlyWithinDistance(me, maximumBranchLength+sim.conf.aMaxRadius+1);

		if (neighbours.numObjs > 0)
		{
			//algae can grow over corals if these are bleached. 
			//--> calculate the area algae cover of the coral colony
			algCoverage=0;
			for (int j=0; j<neighbours.numObjs; j++)
			{
				algCoverage = algCoverage + ((Alga) neighbours.objs[j]).getSize();
			}
				
			for (int i=0; i< neighbours.numObjs; i++)	
			{	
				if(alive)
				{
					Alga otherA =  (Alga) neighbours.objs[i];
					double dist = me.distance(otherA.sendXPos(), otherA.sendYPos());
			    
					if ( dist <= this.getRadius() + otherA.getRadius())
					{
						Double r=0.0;
						Double R=0.0;
						if (otherA.getRadius()> this.getRadius()+ dist) die();
						else if (otherA.getRadius() > this.getRadius()){
							r = this.getRadius();
							R = otherA.getRadius();
						}
						else {	
							r = otherA.getRadius();
							R = this.getRadius();
						}
						
						Double part1 = r*r*Math.acos((dist*dist + r*r - R*R)/(2*dist*r));
						Double part2 = R*R*Math.acos((dist*dist + R*R - r*r)/(2*dist*R));
						Double part3 = 0.5*Math.sqrt((-dist+r+R)*(dist+r-R)*(dist-r+R)*(dist+r+R));

						Double intersectionArea = part1 + part2 - part3;	
						
						if (intersectionArea>0)
							interArea += intersectionArea;	
						
						for (Branch b : branches)
						{
							if (otherA.sendShape().contains(b.getEndX(), b.getEndY()))
							{
								b.reduceGeneralGrowth();
							}
						}
					}
				}
			}
		}
		neighbours.clear();
	}
	
	/**
	 * Interaction with neighboring massive corals
	 */
	public void interactMassiveCorals() 
	{
		neighbours = sim.massiveCorals.getObjectsExactlyWithinDistance(me, maximumBranchLength+sim.maCoMaxLength+10);
		if (neighbours.numObjs > 1)
		{
			for (int i=0; i< neighbours.numObjs; i++)	
			{
				if (alive)
				{
					MassiveCoral otherC = (MassiveCoral) neighbours.objs[i];	
					if(otherC.me!=this.me)
					{
						double ciCalc = 1;
						if (this.getCI() <= otherC.getCI()) ciCalc = this.getCI()/otherC.getCI() / sim.ciFactor;
						
						double gReducer = ciCalc;
						if (gReducer > 1) gReducer = 1;
						
						double dist = me.distance(otherC.sendXPos(), otherC.sendYPos());
						if (dist<this.getRadius()+otherC.getRadius() && otherC.bleached == false)
						{
							Double r=0.0;
							Double R=0.0;
							if (otherC.getRadius()> this.getRadius()+dist) die();
							 if (otherC.getRadius() > this.getRadius()){
								r = this.getRadius();
								R = otherC.getRadius();
							}
							else {	
								r = otherC.getRadius();
								R = this.getRadius();
							}
							
							Double part1 = r*r*Math.acos((dist*dist + r*r - R*R)/(2*dist*r));
							Double part2 = R*R*Math.acos((dist*dist + R*R - r*r)/(2*dist*R));
							Double part3 = 0.5*Math.sqrt((-dist+r+R)*(dist+r-R)*(dist-r+R)*(dist+r+R));
	
							Double intersectionArea = part1 + part2 - part3;	
							if (intersectionArea>0)
								interArea += intersectionArea;
	
							for (Branch b : branches)
							{
								if ( otherC.sendShape().contains(b.getEndX(), b.getEndY()) )
								{
									b.growthRate = initGrowthRate*gReducer;
								}
							}
						}
					}
				}
			}
		}
		neighbours.clear();
	}
	
	/**
	 * Interaction with neighboring branching corals
	 */
	public void interactBranchingCorals() 
	{
		
		growthRate = initGrowthRate;
		neighbours = sim.branchingCorals.getObjectsExactlyWithinDistance(me, maximumBranchLength+sim.braCoMaxLength+10);
		if (neighbours.numObjs > 1)
		{
			for (int i=0; i< neighbours.numObjs; i++)	
			{
				if (alive)
				{
					BranchingCoral otherC = (BranchingCoral) neighbours.objs[i];

					double ciCalc = 1;
					if (this.getCI() <= otherC.getCI()) ciCalc = this.getCI()/otherC.getCI() / sim.ciFactor;

					double gReducer = ciCalc;
					if (gReducer > 1) gReducer = 1;
					
					double dist = me.distance(otherC.xPos, otherC.yPos);
					if (dist<this.getRadius()+otherC.getRadius() && otherC.bleached == false)
					{
						Double r=0.0;
						Double R=0.0;
						if (otherC.getRadius()> this.getRadius()+dist) die();
						else if (otherC.getRadius() > this.getRadius()){
							r = this.getRadius();
							R = otherC.getRadius();
						}
						else {	
							r = otherC.getRadius();
							R = this.getRadius();
						}
						
						Double part1 = r*r*Math.acos((dist*dist + r*r - R*R)/(2*dist*r));
						Double part2 = R*R*Math.acos((dist*dist + R*R - r*r)/(2*dist*R));
						Double part3 = 0.5*Math.sqrt((-dist+r+R)*(dist+r-R)*(dist-r+R)*(dist+r+R));

						Double intersectionArea = part1 + part2 - part3;	
						if (intersectionArea>0)
							interArea += intersectionArea;						
						
						for (Branch b : branches)
						{
							if ( otherC.sendShape().contains(b.getEndX(), b.getEndY()) )
							{
								b.growthRate = initGrowthRate*gReducer;

							}
						}
					}
				}
			}
		}
		neighbours.clear();
	}

	/**
	 * Interaction with turf algae
	 */
	private void interactTurf()
	{
		Bag t = sim.turf.getAllObjects();
			
		for (int i=0; i<t.size(); i++)
		{
			TurfCell tC = (TurfCell) t.objs[i];
			if (tC.sendShape().contains(me.x, me.y))
			{
				if (tC.sendCover() > 100) tC.cover = 100;
				if (sim.random.nextBoolean(tC.sendCover()/100/2)) die(); 
			}
		}
	}
	
	
	
	/**
	 * reschedules coral 
	 * @param dTime time interval
	 */
	public final void reschedule(double dTime) {
		if (dTime <= 0.0) {
			step(sim);
			return;
		}
		sim.schedule.scheduleOnceIn(dTime, this);
	}

	/**
	 * Determines the average radius of the colony
	 */
	public void getAvRadius()
	{
		double averageLength = 0;
		maxLength = 0;
		for (Branch b : branches)
		{
			/** 
			 * The following part is to minimize edge effects of the simulation. 
			 * If branching corals do not have any neighbours on one side they would grow to infinity and thereby 
			 * skew the value for the average radius
			 */
			
			if (b.getEndX() < Siccom.gridWidth  || b.getEndX() > Siccom.gridWidth ||
				b.getEndY() < Siccom.gridHeight || b.getEndY() > Siccom.gridHeight ) maxLength = radius;

			// if they are on the simulation area.
			if (b.getBranchLength() >= maxLength) maxLength = b.getBranchLength();
			averageLength += b.getBranchLength();
		}
		radius = averageLength / numBranches;
		diameter = 2*radius;
		size = Math.PI * Math.pow(radius, 2);
		
		maximumBranchLength = maxLength;
	}
	
	/**
	 * Draws the colony
	 */
	public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
    {
    	graph = graphics;
    	inf = info;
    	
		
		double x = inf.draw.x;
    	double y = inf.draw.y;

    	
       	if (bleachDead) graph.setColor(Color.white);
    	else
    	{
    		SimpleColorMap colorM = new SimpleColorMap(0.0, 100.0, initColor, Color.WHITE); 	// color is chosen 
    		color = colorM.getColor(bleachGrade);												// depending on the bleachGrade of the coral		

    		graph.setColor(color);
    	}
   		
   		drawPoly(inf, x, y);
   		graph.setColor(Color.white);
    }
	/**
	 * Draws the polygon shape of the colony
	 * @param inf the drawing information
	 * @param x	the x coordinate
	 * @param y the y coordinate
	 */
	private void drawPoly(DrawInfo2D inf, double x, double y) 
	{
		Polygon2D.Double drawP = new Polygon2D.Double();
		for (int i=0; i < branches.size(); i++)
		{
			Branch b =  branches.get(i);
			double length = b.getBranchLength();
			
			double rad = (inf.draw.width*length);
			
			int div = (int)b.multiplier;
			double endX = x + (rad*Math.cos( (2*Math.PI/numBranches)*div ));
	        double endY = y - (rad*Math.sin( (2*Math.PI/numBranches)*div ));

	        drawP.addPoint(endX, endY);
		}
		graph.fill(drawP);
		
		if (alive)
		{
			graph.setColor(Color.black);
		
			graph.draw(drawP);
		}
	}
	
	/**
	 * for the inspector of Coral objects
	 */
	public boolean hitObject(Object object, DrawInfo2D range)
    {
		final double SLOP = 1.0;	// need a little extra diameter to hit circles
    	final double width = range.draw.width * diameter;
    	final double height = range.draw.height * diameter;

       	Ellipse2D.Double ellipse = new Ellipse2D.Double( 
    			range.draw.x-width/2-SLOP,
    			range.draw.y-height/2-SLOP, 
                width+SLOP*2,
                height+SLOP*2 );
    	
    	return (ellipse.intersects( range.clip.x, range.clip.y, range.clip.width, range.clip.height ));
    }
	
	/**
	 * Calculates the hemispherical surface area of the coral
	 * @return
	 */
	public double calculateHemisphere()
	{
		return 2*Math.PI*Math.pow((radius*Siccom.resolution), 2);
	}
	
	
	/**
	 *  sets the life-status to false
	 *  -- removes object from hash table 
	 */
	public void die() 
	{
		alive = false;
		sim.massiveCorals.remove(this);
	}
		
	
	
	
	/**
	 * Passes the actual shape of the coral.
	 * @return Polygon p
	 */
	public Polygon2D.Double sendShape()
	{
		return 	p;	
	}

	/**
	 * Lets the coral grow. If the branch is not touching another, bigger coral, 
	 * it will grow with the growth rate defined in the interaction part.
	 * Afterwards it calculates the new size and diameter.
	 */
	public void grow()
	{
		
		p = new Polygon2D.Double();

			
		for (int i=0; i < branches.size(); i++)
		{
			Branch b =  branches.get(i);
			
			if (b.branchLength < maximumRadius && b.stop == false)
				b.growBranch();

			b.stop = false;
			growthRate = initGrowthRate;
			
			
			double length = b.getBranchLength();
			double rad = length;
			double div = b.multiplier;

			double endX = b.startX + (rad*Math.cos( (2*Math.PI/numBranches )*div ));
	        double endY = b.startY - (rad*Math.sin( (2*Math.PI/numBranches)*div ));

	        p.addPoint(endX, endY);
	
		}
		
		size = Math.PI * Math.pow(radius, 2);
		diameter = 2*radius;		
	}
	
	/**
	 * reduces the growth rate of a coral as soon as it touches a macroalga for each macroalga it touches
	 * @return the new growth rate
	 */
	 public double reduceGeneralGrowth()
	 {
		 growthRate = growthRate * 0.9;
		 return growthRate;
	 }

	 /*
	  * BLEACHING AND RECOVERY
	  */
	 /**
	  * Check if the colony will bleach within this time step
	  */
	 public void checkBleaching()
	 {
		 double bleachProb = sim.bleachProbs.get(name);

		 if (bleachProb > 0)
		 {
			 if (sim.random.nextBoolean(bleachProb)) bleach();
		 }
	 }

	 /**
	  * Lets a coral bleach, and die with a certain probability
	  */
	 public void bleach()
	 {
		 if (sim.random.nextBoolean( sim.bleachDeathProbs.get(name) )) 
		 {
			 die();
//			 bleachDead=true;
		 }
		 
		 bleachGrade = 100;
		 
		 // reduce growth rate to nearly 0;
		 growthRate = growthRate / bleachGrade;
		 // set boolean bleached to true;
		 bleached = true; 
	 }
	 
	 /**
	  * Recovery of a bleached coral
	  */
	 public void recover()
	 {
		 bleachGrade = bleachGrade - recoveryRate;
//		 growthRate = growthRate / bleachGrade;
		 if (bleachGrade<= 0)
		 {
			 bleached = false;
			 bleachGrade = 0;
		 }
	 }
}