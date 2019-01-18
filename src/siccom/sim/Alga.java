/**
 * This class defines the single Alga 
 */

package siccom.sim;

import java.awt.*;
import java.awt.geom.Ellipse2D;

import sim.engine.*;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.SimplePortrayal2D;
import sim.util.*;
import sim.util.gui.SimpleColorMap;


public class Alga extends SimplePortrayal2D implements Steppable
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 2513272544690847577L;

	/**
	 * The position of the alga
	 */
	public double xPos, yPos; 	
	/**
	 * The growth rate of an alga
	 */
	public double growthRate;
	/**
	 * The radius of the alga
	 */
	public double radius;	
	/**
	 * The diameter of the alga
	 */
	public double diameter;
	/**
	 * The range within which the alga identifies neighbors for interaction
	 */
	public double range;
	/**
	 * The height of the alga
	 */
	public double algaHeight;	
	/**
	 * The area the alga occupies
	 */
	public double size;				
	/**
	 * The alga's color
	 */
	Color color;									
	Color darkGreen = new Color(100, 200, 100);
	Color mediumGreen = new Color(50, 220, 50);
	Color lightGreen = new Color(0, 255, 0);
	
	/**
	 * The arrays in which competitors are stored
	 */
	Bag aB, mB, bB;
	/**
	 * The alga's position
	 */
	Double2D me;
	/**
	 * The main simulation
	 */
	Siccom sim;
	/**
	 * The configuration class through which parameters are determined
	 */
	Config conf;
	
	/**
	 * Determines the alga's life status
	 */
	public boolean alive;
	/**
	 * The time the alga is in the simulation
	 */
	public int age;
	/**
	 * The range outside the experimental field at which algal fragments can still settle
	 * in order to minimize edge effects. 
	 */
	private double algalExtensionBuffer = 100 / Siccom.resolution;
	
	// Different outputs for the console
	public double sendXPos() { return xPos; }
	public double sendYPos() { return yPos; }
	public double getRadius() { return radius; }
	public double getDiameter() { return diameter; }
	public double getGrowthRate() { return growthRate; }
	public int getAge() { return age; }
	public boolean getAlive() { return alive; }
	public double getHeight() { return algaHeight; }
	public double getSize() { return size; }
	
	/**
	 * Constructor
	 * @param sim the simulation where alga is situated in 
	 * @param xPos x-position
	 * @param yPos y-position
	 * @param radius initial radius
	 * @param age initial age of the alga
	 * @param aH alga height at instantiation
	 */
	  public Alga( 	Siccom sim,
			  		double 	xPos, 			
			  		double 	yPos, 
			  		double 	radius,
			  		int 	age,
			  		double  aH)
	  {
		  this.sim = sim;
		  this.color = sim.conf.algaColor;
		  this.xPos = xPos;
		  this.yPos = yPos;
		  this.radius = radius;
		  this.growthRate = sim.conf.aGrowthRate;
		  this.age = age;
		  diameter = 2*radius;
		  this.me = new Double2D(xPos, yPos);		// get my own location
		  alive = true;
		  size = Math.pow(radius, 2)*Math.PI;
		  if (radius <= sim.conf.aMaxRadius)
		  {
			  algaHeight = radius;
		  }
		  else
		  {
			  radius = sim.conf.aMaxRadius;
			  algaHeight = aH;
		  }
	  }
	 
	  @Override
	  public void step(SimState state) 
	  {
		  /**
		   * 	Life loop of an alga 
		   */
		  if (alive)
		  {
				if (xPos < -algalExtensionBuffer || xPos > Siccom.gridWidth+algalExtensionBuffer 
						|| yPos < -algalExtensionBuffer || yPos > Siccom.gridHeight+algalExtensionBuffer)
			  			die();
			  growthRate = sim.conf.aGrowthRate;		// sim.aGrowthRate;
			  
			  if (getHeight()> sim.conf.aFragmentationHeight) 
			  { 
				  fragtate(); 
			  }			 
			  
			  // find neighbours
			  if (alive) interactAlgae();
			  if (alive) interactMassiveCorals();
			  if (alive) interactBranchingCorals();
		  
		  
			  if (age > sim.conf.algaMaxAge) die();
			  else  
			  {
				  grow();
				  reschedule(1.0);	
			  }
		  }
	  }
	 
	  /**
	   * Interaction with massive corals
	   */
	  private void interactMassiveCorals() 
	  {
		  mB = sim.massiveCorals.getObjectsExactlyWithinDistance(me, radius+sim.maCoMaxLength);
		  if (mB.numObjs > 0)
		  {
			  for (int i=0; i< mB.numObjs; i++)	
			  {
				  if (alive)
				  {
					  MassiveCoral otherC = (MassiveCoral) mB.objs[i];
	
					  if ((otherC.size > size) && otherC.sendShape().contains(me.x, me.y))  
					  {
						  if (otherC.bleached)
						  {
							  if (sim.random.nextBoolean(1-(otherC.bleachGrade/100))) die(); // bleach grade is used to define the probability for 
							  																 // algal survival if situated on a coral
							  																 // --> the more the coral recovers, the higher the probability for an alga to die
						  }
						  else die();
						  
					  }
				  }
			  }
		  }
		  mB.clear();
	  }
	
	  /**
	   * Interaction with branching corals
	   */
	  private void interactBranchingCorals() 
	  {
		bB = sim.branchingCorals.getObjectsExactlyWithinDistance(me, radius+sim.braCoMaxLength+10);
		  if (bB.numObjs > 0 )
		  {
			  for (int i=0; i< bB.numObjs; i++)	
			  {
				  if(alive)
				  {
					  BranchingCoral otherC = (BranchingCoral) bB.objs[i];
					  double dist = me.distance(otherC.sendXPos(), otherC.sendYPos());
					  
					  if (otherC.radius > this.radius + dist)
					  {
					  	  if (otherC.bleached)
						  {
							  if (sim.random.nextBoolean(1-(otherC.bleachGrade/100))) die(); 
						  }																 	
						  else die();																 	
					  }
				  }
			  }
		  }
		  mB.clear();
	  }
	  
	  /**
	   * Interaction with other algae
	   */
	  private void interactAlgae() 
	  {
		  aB = sim.algae.getObjectsExactlyWithinDistance(me, radius+3);
		  if (aB.numObjs > 1)
		  {
				  for (int i=0; i< aB.numObjs; i++)	
				  {
					  if(alive)
					  {
						  Alga otherA = (Alga) aB.objs[i];
					
						  double dist = me.distance(otherA.me);
						  
						  if (dist < (this.radius+otherA.radius) && otherA.getHeight() > this.getHeight())
						  {
							  die();
						  }
					  }
				  }
		  }
		  aB.clear();
	  }
		
	  /**
	   * Reschedules the alga within the given time
	   * @param dTime
	   */
	  public final void reschedule(double dTime) 
	  {
		  if (dTime <= 0.0) {
				step(sim);
		  }
		  sim.schedule.scheduleOnceIn(dTime, this);
	  }
	
	  /**
	   * Draws the alga
	   */
	  public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
	  {
		  final double width = info.draw.width * diameter;
		  final double height = info.draw.height * diameter;
	    	
		  SimpleColorMap colorM = new SimpleColorMap(0.0, 6.0, darkGreen, lightGreen);
		  color = colorM.getColor(algaHeight);
		  graphics.setColor(color);
	    	
		  final int x = (int)(info.draw.x - width / 2.0);
		  final int y = (int)(info.draw.y - height / 2.0);
		  final int w = (int)(width);
		  final int h = (int)(height);

		  // draw centered on the origin
		  graphics.fillOval(x, y, w, h);
	  }  
		

		/**
		 *  information for the Inspector
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
		  *  Death of an alga - sets the life-status to false
		  */
		 public void die()
		 {
			 alive = false;
			 sim.algae.remove(this);
		 }
		
		
		/**
		 * Fragmenting of an alga
		 */
		public void fragtate()
		{
   			double x = this.sendXPos();
  			double y = this.sendYPos();
  			double halfRange = sim.conf.aFragRange;
  			
  			int fragNum = sim.random.nextInt(sim.conf.aMaxFragNum);
  			
  			
  			if (fragNum !=0)
  			{
  				for (int i=0; i<=fragNum; i++)
  				{
  					double xPos = sim.random.nextDouble() *  ((x+halfRange) - (x-halfRange) + 1)  + (x-halfRange);
  					double yPos = sim.random.nextDouble() *  ((y+halfRange) - (y-halfRange) + 1)  + (y-halfRange);	
  			  					
  					Alga a = new Alga(	sim,											
  										xPos, 											
  										yPos,											
  										sim.conf.aFragmentSize,							
  										0,																	
  										sim.conf.algaRecRad);							
  					sim.algae.setObjectLocation(a, new Double2D(xPos, yPos));
  					sim.schedule.scheduleOnce(a);
 					
  				}
  			}
  			algaHeight = algaHeight - (fragNum * sim.conf.aFragmentSize) ;
  			
		}
  		
		/**
		 * Algal growth
		 */
		public void grow()
		{
			if (radius<sim.conf.aMaxRadius)
			{
			  radius = radius + growthRate;
			  algaHeight = radius;
			} 
			else 
			{
				radius = sim.conf.aMaxRadius;
				algaHeight = algaHeight + growthRate;
			}
			age += 1;
			
			diameter = 2*radius;
			
			size = ((Math.pow(radius,2))*Math.PI);
		}
		
		/**
		 * To retrieve the shape object of an alga
		 * @return
		 */
		 public Ellipse2D.Double sendShape()
			{
				  	double ulX = xPos - radius;
				  	double ulY = yPos - radius;
					return new Ellipse2D.Double( ulX, ulY, diameter, diameter );
			}
		 
		 
		 
}
