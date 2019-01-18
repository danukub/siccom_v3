package siccom.sim;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Ellipse2D;
import java.util.ArrayList;

import sim.engine.SimState;
import sim.engine.Steppable;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;
import sim.util.Bag;
import sim.util.Double2D;
import sim.util.gui.SimpleColorMap;


/**
 * 
 * 
 * @author kubicek
 * @author version 1.0
 *
 */
public class BranchingCoral extends SimplePortrayal2D implements Steppable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = -7344285892340782073L;
	
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

	/**
	 * The number of branches the colony has. 
	 */
	public int numBranches = 34;
	
	/**
	 * The maximum momentary branch length
	 */
	public double maxLength;
	
	/**
	 * ArrayList for the single branches of the colony
	 */
	public ArrayList<Branch> branches = new ArrayList<Branch>(numBranches);

	// Colony
	public Double2D me;
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
	 * The radius of a colony. Used to determine the initial branch length.
	 * Later the radius is calculated as mean branch length in {@link #getAvRadius()}
	 */
	public double radius;											
	/**
	 * The maximum branch length of a colony
	 */
	public double maxBranchLength;		
	/**
	 * The diameter of the colony
	 */
	public double diameter;										
	/**
	 * The size of the colony
	 */
	public double size;		
	/**
	 * The maximumSize of a colony
	 */
	public double maxSize;
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
	/**
	 * Measure to determine for how long is the colony in the simulation
	 */
	public int age;	
	
	public double CI;
	
	public BranchingGroup bG;
	
	
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
	/**
	 * The number of neighboring coral colonies
	 */
	private int numNeighbours = 0;
	/**
	 * The maximum branch Length of a 
	 */
	public double maximumBranchLength;
	
	private double interArea;
	
	// FRAGMENTING
	double extensionBuffer = 100/Siccom.resolution;
	public double fragSize;
	public double fragRange;
	double fragProb;

	private double surfaceFactor;

	private int nn;
		

	// Different getters and setters for the console
	public String getName() { return name; }
	public double getCI() { return CI; }
	public double getSurfaceFactor() { return surfaceFactor; }
	public double sendXPos() { return xPos; }
	public double sendYPos() { return yPos; }
	public int getAge() { return age; }
	public boolean getAlive() { return alive; }
	public double sendSize() { return size; }
	public double getGrowthRate() { return initGrowthRate/Siccom.dimensionConv_milimeters; }
	public double getRadius() { return radius; }
	public boolean getBleached() { return bleached; }
	public double getMinBleachProb() { return bG.minBleachProb; }
	public double getMinDeathProb() { return bG.minDeathProb; }
	public int getNumNeighbours() { return nn; }
	
	
	

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
	 * @param growthRate2 
	   * @param numBranches the number of branches per colony
	   * @param age the time the colony is in the simulation
	   * 
	   */
	
	public BranchingCoral(	Siccom sim, 
							String name,
							Color color,
							double CI,
							double surfaceFactor,
							double maxRadius,
							double xPos, 
							double yPos, 
							double radius,
							double growthRate,
							int numBranches,
							int age,
							BranchingGroup bG) 
	{
		this.sim = sim;
		this.name = name;
		this.xPos = xPos;						
		this.yPos = yPos;
		this.radius = radius;
		initGrowthRate = growthRate;
		this.growthRate = initGrowthRate;
		this.CI = CI;
		this.surfaceFactor = surfaceFactor;
		this.age = age;
		this.bG = bG;
		diameter = 2*radius;
		me = new Double2D(xPos, yPos);		
		size = Math.PI * Math.pow(radius, 2);
		alive = true;	  
		maxBranchLength = maxRadius;
	

		
		maxSize = Math.pow(maxRadius, 2) * Math.PI;	
		
	
		initColor = color;
		this.numBranches = numBranches;
		
		for (int i=1; i <= numBranches ; i++)
		{
			Branch b = new Branch(xPos, yPos, radius, numBranches, i, growthRate);
			branches.add(b);
		}

		fragSize = 10 / Siccom.resolution;
		fragRange = sim.fragRange;
		fragProb = sim.fragProb*bG.surfaceFactor;
	}


	
	/** 
	 * Step routine of a branching coral colony
	 */
	public void step(SimState state)
	{
		if (xPos < -extensionBuffer || xPos > Siccom.gridWidth+extensionBuffer 
				|| yPos < -extensionBuffer || yPos > Siccom.gridHeight+extensionBuffer)
	  			die();
		
		if (alive)
		{
			checkBleaching();
			if (bleached){
				 growthRate = initGrowthRate * (1 - (bleachGrade / 100)); 
				 recover();
			}
			else
			// reset the growth rate at every step 
			growthRate = initGrowthRate;

	     	if (radius>=maxBranchLength && sim.random.nextBoolean(fragProb)) fragtate();
			
			// find neighbours
			getAvRadius();
			if(alive) interactMassiveCorals();
			if(alive) interactBranchingCorals();
			if (radius<=sim.conf.aMaxRadius || bleached) interactAlgae();
			if (age <= sim.conf.interactTurfAge ) interactTurf();
			
			double percentOvergrowth = 100/size*interArea;
			if (percentOvergrowth>=sim.dieOvergrowthBra) die(); 
			interArea=0;
	
			if (alive && radius<=maxBranchLength)
			{
				grow();
			}
			
			
			if (numNeighbours < 1)			
			{
				if (sim.random.nextBoolean(sim.breakageProb	* surfaceFactor ))
				{
					if (sim.random.nextBoolean(fragProb)) fragtate();
					die();
				}
			}
			

			reschedule(1.0);
			age += 1;
			numNeighbours = 0;
		}			
	}
	
	private void interactAlgae() {
		neighbours = sim.algae.getObjectsWithinDistance(me, maximumBranchLength+sim.conf.aMaxRadius+1);

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
	 * Interaction with neighboring branching corals
	 */
	public void interactBranchingCorals() {
		neighbours = sim.branchingCorals.getObjectsWithinDistance(me, maximumBranchLength+sim.braCoMaxLength+1);
		if (neighbours.numObjs > 1)
		{
			for (int i=0; i< neighbours.numObjs; i++)	
			{
				if (alive)
				{
					BranchingCoral otherC = (BranchingCoral) neighbours.objs[i];
			
					if(otherC.me!=this.me)
					{
						double ciCalc = 1;
						if (this.getCI() <= otherC.getCI()) ciCalc = this.getCI()/otherC.getCI() / sim.ciFactor;
						
						double gReducer = ciCalc;
						if (gReducer > 1) gReducer = 1;
						
						double dist = me.distance(otherC.xPos, otherC.yPos);
						if (dist<=this.getRadius()+otherC.getRadius() && otherC.size >= size*0.5) numNeighbours+=1;
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
		}
		neighbours.clear();
	}					


	
	/**
	 * Interaction with neighboring massive corals
	 */
	public void interactMassiveCorals()
	{
		neighbours = sim.massiveCorals.getObjectsWithinDistance(me, (maximumBranchLength+sim.maCoMaxLength+1));
		if (neighbours.numObjs > 1)
		{			
			for (int i=0; i< neighbours.numObjs; i++)	
			{
				if(alive)
					{
					MassiveCoral otherC = (MassiveCoral) neighbours.objs[i];
					
					/*
					 * in competition with massive corals the growth rate of a branching coral 
					 * is not only reduced due to the CI of the competitor but also because of 
					 * the massive structure, which is why the size term also is multiplied by 2
					 */
					double ciCalc = 1;
					if (this.getCI() <= otherC.getCI()) ciCalc = this.getCI()/otherC.getCI() / sim.ciFactor;
					
					double gReducer = ciCalc;
					if (gReducer > 1) gReducer = 1;
					

					double dist = me.distance(otherC.sendXPos(), otherC.sendYPos());
					if (dist<=this.getRadius()+otherC.getRadius() && otherC.size >= size*0.5) numNeighbours+=1;
					
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
						if (otherC.getRadius()>this.getRadius() && intersectionArea>0)
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
				if (sim.random.nextBoolean(tC.sendCover()/100/2)) die(); //probability to max 50%
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
	public double getAvRadius()
	{
		double averageLength = 0;
		maxLength = 0;
		for (Branch b : branches)
		{
			/** 
			 * The following part is to minimize edge effects of the simulation. 
			 * If branching corals do not have any neighbors on one side they would grow to infinity and thereby 
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
		
		
		return radius;
	}

		
	/**
	 * Growth of the colony
	 */
	public void grow()
	{
		for (Branch b : branches)
		{
			if (b.branchLength < maxBranchLength && b.stop == false)
							b.growBranch();
			if (b.branchLength > maxBranchLength) b.branchLength=maxBranchLength;  // ATTENTION
			
			b.stop = false;
			b.growthRate = initGrowthRate;
		}
		
		diameter = 2*radius;
		size = Math.PI * Math.pow(radius, 2);
				
	}
	
	/**
	 * Draw the coral
	 * Draw info is for scaling issues of the portrayals
	 */
	public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
    {
    	graph = graphics;
    	inf = info;
    	
		
		int x = (int)(inf.draw.x);
    	int y = (int)(inf.draw.y);
     	
    	SimpleColorMap colorM = new SimpleColorMap(0.0, 100.0, initColor, Color.WHITE); 	// color is chosen 
    	color = colorM.getColor(bleachGrade);												// depending on the bleachGrade of the coral	
    	
   		graph.setColor(color);
   		
   		drawCoral(inf, x, y);

    }

	private void drawCoral(DrawInfo2D inf, int x, int y) 
	{
		for (Branch b : branches)
		{
			drawArm(x,y, (int)b.getBranchLength(), (int)b.multiplier, inf);
		}
	}

	private void drawArm(int x, int y, int length, int div, DrawInfo2D inf)
	{
		int rad = (int)(inf.draw.width*length);
		
		int endX = (int)(x + (rad*Math.cos( (2*Math.PI/numBranches)*div )));
        int endY = (int)(y - (rad*Math.sin( (2*Math.PI/numBranches)*div )));

   		graph.drawLine(x,y,endX,endY);
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
		sim.branchingCorals.remove(this);
  	}

	public Ellipse2D.Double sendShape()
	{
		  	double ulX = xPos - radius;
		  	double ulY = yPos - radius;
			return new Ellipse2D.Double( ulX, ulY, diameter, diameter );
	}
	

	
	 /*
	  * BLEACHING AND RECOVERY
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
		 if (sim.random.nextBoolean( sim.bleachDeathProbs.get(name) )) die();

		 
		 bleachGrade = 100;
		 
		 // reduce growth rate to nearly 0;
		 growthRate = growthRate / bleachGrade;
		 // set boolean bleached to true;
		 bleached = true; 
	 }
	 
	 public void recover()
	 {
		 bleachGrade = bleachGrade - recoveryRate;
//		 growthRate = growthRate / bleachGrade;
		 if (bleachGrade<=0){
			 bleached = false;
			 bleachGrade = 0;
		 }
	 }

		public void fragtate()
		{
			double x = this.sendXPos();
			double y = this.sendYPos();
			double halfRange = sim.conf.aFragRange;
			
			int fragNum = 	1; //	sim.random.nextInt(sim.conf.aMaxFragNum);
			
			 
			if (fragNum != 0)
			{
				for (int i=0; i<=fragNum; i++)
				{
					double xPos = sim.random.nextDouble() *  ((x+halfRange) - (x-halfRange) + 1)  + (x-halfRange);
					double yPos = sim.random.nextDouble() *  ((y+halfRange) - (y-halfRange) + 1)  + (y-halfRange);	
			  					
					BranchingCoral bC = new BranchingCoral(	sim,										// the simulation, the agent acts in
								name,
								bG.color,
								bG.CI,
								bG.surfaceFactor,
								bG.maxRadius,
								xPos, 										
								yPos,										
								fragSize,									
								growthRate,								
								numBranches,
								0,
								bG);																			

					sim.branchingCorals.setObjectLocation(bC, new Double2D(xPos, yPos));
					sim.schedule.scheduleOnce(bC);
					
				}
			}
		}
 
}

