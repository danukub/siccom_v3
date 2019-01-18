package siccom.gui;


import sim.display.*;
import sim.display3d.*;
import sim.engine.*;

/**
 * This class implements a {@link Display2D} that will invoke a visual update
 * periodically during simulation time instead after every or an amount of
 * steps. This behaviour is advantageous when coupling the simulation time with
 * real time, e.g. when a simulation shall simulate a real time process and
 * every second of the real time process a visual update should occur.<br>
 * For implementing this behaviour this class uses an instance of the class
 * {@link TimedDisplayAlgorithm} that is configured and will be called every
 * step of the simulation. The method {@link #step(SimState)} will be
 * overwritten to invoke the method
 * {@link TimedDisplayAlgorithm#visualUpdate(SimState)} that will return true if
 * a visual update has to be performed; in that case the method
 * {@link #step(SimState)} of the superclass will be called (doing everything
 * necessary for the visual update, this class has no idea how to perform it
 * itself so we rely on the superclass). An instance of the class
 * {@link TimedDisplayAlgorithm} can be configured in various ways so refer to
 * {@link TimedDisplayAlgorithm}.<br>
 * The reason an instance of the class {@link TimedDisplayAlgorithm} is used
 * lies in the single inheritance scheme of JAVA since no multiple inheritance
 * is available. The very same algorithm for performing the periodic behaviour
 * is used for the subclasses of {@link Display2D} and {@link Display3D} so the
 * code has to maintained twice. Using a different class simplifies the
 * implementation efforts for {@link TimedDisplay2D} and {@link TimedDisplay3D}.
 * 
 * @author hoehne
 * 
 */
public class TimedDisplay2D extends Display2D {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1301580127902747658L;
	/**
	 * The instance of class {@link TimedDisplayAlgorithm} that will be created
	 * and configured in the constructor of this class.
	 */
	private TimedDisplayAlgorithm timedDisplayAlgorithm = null;

	/**
	 * The constructor of this display.
	 * 
	 * @param width
	 *            The visual width, see {@link Display2D}.
	 * @param height
	 *            The visual height, see {@link Display2D}.
	 * @param simulation
	 *            The simulation, see {@link Display2D}.
	 * @param firstUpdate
	 *            The time of the first visual update. This simulation time has
	 *            to elapsed first before a visual update will occur.
	 * @param dTime
	 *            The (minimum) time between two successive visual updates.
	 * @param createOwnEvents
	 *            Force a visual update even if no simulation progress occurred
	 *            during two successive visual updates.
	 */
	public TimedDisplay2D(double width, double height, GUIState simulation,
			double firstUpdate, double dTime, boolean createOwnEvents) {
		super(width, height, simulation, 1L);

		this.timedDisplayAlgorithm = new TimedDisplayAlgorithm(simulation,
				firstUpdate, dTime, createOwnEvents);
	}

	/**
	 * The overwritten method of {@link Display2D#step(SimState)}. The instance
	 * of {@link TimedDisplayAlgorithm} is asked if a visual update should
	 * occur.
	 */
	public void step(final SimState state) {
		if (timedDisplayAlgorithm.visualUpdate(state))
			super.step(state);
	}

	/**
	 * Return the reference to the instance of the {@link TimedDisplayAlgorithm}
	 * so the behaviour can be changed by accessing the object.
	 * 
	 * @return the timedDisplayAlgorithm
	 */
	public final TimedDisplayAlgorithm getTimedDisplayAlgorithm() {
		return timedDisplayAlgorithm;
	}

	/**
	 * Set the reference to the instance of the {@link TimedDisplayAlgorithm}.
	 * 
	 * @param timedDisplayAlgorithm
	 *            the timedDisplayAlgorithm to set
	 */
	public final void setTimedDisplayAlgorithm(
			TimedDisplayAlgorithm timedDisplayAlgorithm) {
		this.timedDisplayAlgorithm = timedDisplayAlgorithm;
	}
}
