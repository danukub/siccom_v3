package siccom.sim;

import java.awt.Shape;
import java.awt.geom.Line2D;
import java.awt.geom.Point2D;
import java.io.Serializable;

/**
 * Describes a branch of a branching coral colony or an axial ray of a massive
 * one. Evoked by the {@link MassiveCoral} or {@link BranchingCoral}
 * 
 * @author andreas
 *
 */

class Branch implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 2087139366528515537L;

	/**
	 * Coordinates for the center of the colony
	 */
	double startX, startY;
	/**
	 * Coordinates for the branch end
	 */
	double endX, endY;
	/**
	 * Length of a branch
	 */
	double branchLength;
	/**
	 * Point for the branch end location
	 */
	Point2D.Double end;
	/**
	 * Line object which represents the branch
	 */
	Line2D.Double arm;
	/**
	 * For the calculation of the radial position of a single branch
	 */
	double multiplier;
	/**
	 * The initial growth rate of the coral colony
	 */
	private double initGrowthRate;
	/**
	 * The actual growth rate of the respective coral colony
	 */
	double growthRate;
	/**
	 * Determines if a branch stops growing
	 */
	boolean stop = false;
	/**
	 * How many branches are there
	 */
	int numArms;
	/**
	 * The shape object of a branch -- to communicate the position to other
	 * objects
	 */
	Shape branch;
	/**
	 * The main simulation
	 */
	Siccom sim;

	double shrinkRate = 20 * Siccom.dimensionConv_milimeters;

	public Branch(double startX, double startY, double length, int numArms, double multiplier, double growthRate) {

		this.startX = startX;
		this.startY = startY;
		branchLength = length;
		this.numArms = numArms;
		this.multiplier = multiplier;
		this.initGrowthRate = growthRate;
		this.growthRate = initGrowthRate;

		endX = startX + (branchLength * Math.cos((2 * Math.PI / numArms) * multiplier));
		endY = startY - (branchLength * Math.sin((2 * Math.PI / numArms) * multiplier));
		end = new Point2D.Double(endX, endY);
		arm = new Line2D.Double(startX, startY, endX, endY);
	}

	/**
	 * Growth of a branch object
	 */
	public void growBranch() {
		branchLength += growthRate;
		endX = startX + (branchLength * Math.cos((2 * Math.PI / numArms) * multiplier));
		endY = startY - (branchLength * Math.sin((2 * Math.PI / numArms) * multiplier));
		end = new Point2D.Double(endX, endY);
		arm = new Line2D.Double(startX, startY, endX, endY);
		growthRate = initGrowthRate;
	}

	public void shrinkBranch(double sf) {
		branchLength -= (shrinkRate * sf);
		endX = startX + (branchLength * Math.cos((2 * Math.PI / numArms) * multiplier));
		endY = startY - (branchLength * Math.sin((2 * Math.PI / numArms) * multiplier));
		end = new Point2D.Double(endX, endY);
		arm = new Line2D.Double(startX, startY, endX, endY);
		// growthRate = initGrowthRate;
	}

	/**
	 * reduces the growth rate of a branch as soon as it touches another branch
	 * 
	 * @return growthRate
	 */
	public double reduceGeneralGrowth() {
		growthRate = growthRate * 0.7;
		return growthRate;
	}

	/**
	 * Getter for branch length
	 * 
	 * @return branchLength
	 */
	public double getBranchLength() {
		return branchLength;
	}

	/**
	 * Getter for x position of the branch end
	 * 
	 * @return endX
	 */
	public double getEndX() {
		return endX;
	}

	/**
	 * Getter for y position of the branch end
	 * 
	 * @return endY
	 */
	public double getEndY() {
		return endY;
	}

}
