package siccom.sim;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.geom.Rectangle2D;
import sim.engine.SimState;
import sim.engine.Steppable;
import sim.portrayal.DrawInfo2D;
import sim.portrayal.simple.RectanglePortrayal2D;
import sim.util.Bag;
import sim.util.gui.SimpleColorMap;

/**
 * One cell of turf algae
 * @author andreas
 *
 */


public class TurfCell extends RectanglePortrayal2D implements Steppable
{
	/**
	 */
	private static final long serialVersionUID = 4334605420891738448L;

	//VARIABLES
	/**
	 * The percentage cover
	 */
	double 	cover;
	/**
	 * The maximal cover
	 */
	double	maxCover;
	/**
	 * The size of a cell
	 */
	double	size;
	/**
	 * The initial growth rate of turf
	 */
	double 	iniGrowthRate;
	/**
	 * The actual growth rate
	 */
	double	growthRate;
	
	private final double iniMaxCover = 100;
	
	/**
	 * The position of the upper left corner of the cell
	 */
	double x, y;
	
	/**
	 * Cell width and height
	 */
	int	 cellWidth,
		 cellHeight;
	
	/**
	 * The color of a cell
	 */
	Color turfColor = new Color( 0, 100, 0);
	Color color;
	/**
	 * Rectangle object that represents the cell
	 */
	Rectangle2D.Double cell;
	
	Siccom sim;
	
	
	//CONSTRUCTOR
	public TurfCell(	Siccom sim,
						double x2,
						double y2,
						int cellWidth,
						int cellHeight,						
						double cover,
						double growthRate)
	{
		this.sim = sim;
			
		this.cellWidth = cellWidth;
		this.cellHeight = cellHeight;
		this.x = x2;
		this.y = y2;
		this.cover = cover;
		iniGrowthRate = growthRate;
		this.growthRate = iniGrowthRate;
		size = cellWidth*cellHeight;
		maxCover = iniMaxCover;
		
	}
	
	

	@Override
	/**
	 * The step routine
	 */
	public void step(SimState state) 
	{

		maxCover = iniMaxCover;
		growthRate = iniGrowthRate;
		
		interactMaCo();
		interactBraCo();
		
		if (cover >= maxCover) cover = maxCover;
		else cover += growthRate;
		
		reschedule(1.0);	
		
	}
	
	/**
	 * Reschedule after dTime
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
	 * Interaction with massive corals
	 */
	public void interactMaCo()
	{
		
		Bag mB = sim.massiveCorals.getAllObjects();
		for (int i=0; i<mB.size(); i++)
		{
			MassiveCoral mC = (MassiveCoral) mB.objs[i];
			if (mC.sendShape().intersects(sendShape()))
			{
				
			if (mC.sendShape().contains(this.sendShape()))
			{	
				cover = 0;
				growthRate = 0;
			}
			else if (mC.size > size)
				growthRate = growthRate * 0.9;
				
			}
		}
	}
	
	/**
	 * Interaction with branching corals
	 */
	public void interactBraCo()
	{
		Bag bB = sim.branchingCorals.getAllObjects();
		for (int i=0; i<bB.size(); i++)
		{
			BranchingCoral bC = (BranchingCoral) bB.objs[i];
			
			if (bC.sendShape().intersects(sendShape()))
			{
				
				if (bC.sendShape().contains(this.sendShape()))
				{	
					cover = 0;
					growthRate = 0;
				}
				else if (bC.size > size)
					growthRate = growthRate * 0.9;
			}
		}
	}
	
	/**
	 * Draws a turf cell
	 */
	public void draw(Object object, Graphics2D graphics, DrawInfo2D info)
	{
		final double width = info.draw.width * cellWidth;
		final double height = info.draw.height * cellHeight;
	    	
		SimpleColorMap colorM = new SimpleColorMap( 0.0, 100.0, Color.black, turfColor);		
		color = colorM.getColor(cover);
		graphics.setColor(color);
	    	
		final int x = (int) (info.draw.x - (cellWidth*0.5));
		final int y = (int) (info.draw.y - (cellHeight*0.5));
		final int w = (int)(width);
		final int h = (int)(height);

		graphics.fillRect( x, y, w, h);
	}  
		
	public Rectangle2D.Double sendShape()
	{
		return new Rectangle2D.Double( x,y, cellWidth, cellHeight );
	}
	
	public double sendCover() { return cover; }
	
	
}
