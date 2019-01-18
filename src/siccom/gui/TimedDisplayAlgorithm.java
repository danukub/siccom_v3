package siccom.gui;

import sim.display.*;
import sim.engine.*;

/**
 * This class implements an algorithm that will compute a periodically visual
 * update during simulation time instead after every or an amount of steps.<br>
 * The {@link Schedule} class simplifies the process to implement this simple
 * extension because the method {@link #visualUpdate(SimState)} is only called
 * once for a time on the time scale: several event with the same time are
 * processed in a single step. <b>This class depends on this behaviour, so it
 * should not be modified in later version of MASON.</b><br>
 * An instance of this class can be configured to force a periodically update
 * after a defined amount of simulation time or when a given time has elapsed
 * (possibly skipping time). Forcing the periodically behaviour this class
 * creates an event that will be scheduled after each invocation of
 * {@link #visualUpdate(SimState)}.
 * 
 * @author hoehne
 * 
 */
public class TimedDisplayAlgorithm {
	class UpdateEvent implements Steppable {
		/**
		 * 
		 */
		private static final long serialVersionUID = 4661446292735252588L;
		/**
		 * Storing the information if an instance of this class is verbose.
		 * Usually used in the {@link #step(SimState)} method.
		 */
		boolean verbose = false;

		UpdateEvent(boolean verbose) {
			this.verbose = verbose;
		}

		/**
		 * Getting the verbose state of this event.
		 * 
		 * @return the verbose
		 */
		public final boolean isVerbose() {
			return verbose;
		}

		/**
		 * Set the verbose state of this event.
		 * 
		 * @param verbose
		 *            the verbose to set
		 */
		public final void setVerbose(boolean verbose) {
			this.verbose = verbose;
		}

		/**
		 * The implemented method of the interface {@link Steppable}.
		 */
		public void step(SimState sim) {
			if (verbose)
				System.out.println("visual update event occured by "
						+ sim.schedule.getTime());
		}
	}

	/**
	 * An event needed if this instance will create its own events.
	 */
	private UpdateEvent updateEvent = new UpdateEvent(false);

	/**
	 * The first time a visual update should occur.
	 */
	double firstUpdate = 0.0d;

	/**
	 * The time of the recent visual update.
	 */
	double recentUpdate = 0.0d;
	/**
	 * The time of the next visual update. The actual time may be greater than
	 * the value stored in this property.
	 */
	double nextUpdate = 0.0d;

	/**
	 * The time between two successive updates.
	 */
	double dTime = 0.0d;

	/**
	 * Shall this instance create its own events to force a continuous visual
	 * update even if no progress in simulation occurred.
	 */
	boolean createOwnEvents = true;

	/**
	 * The constructor of this visual timer.
	 * 
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
	public TimedDisplayAlgorithm(GUIState simulation, double firstUpdate,
			double dTime, boolean createOwnEvents) {
		this.firstUpdate = firstUpdate;
		this.dTime = dTime;
		this.createOwnEvents = createOwnEvents;

		// create the first update event
		if (createOwnEvents)
			simulation.state.schedule.scheduleOnce(firstUpdate, updateEvent);
	}

	/**
	 * Get the status if an instance of this class creates its own events to
	 * ensure a visual update every {@link #dTime} elapsed simulation time.
	 * 
	 * @return the createOwnEvents
	 */
	public final boolean isCreateOwnEvents() {
		return createOwnEvents;
	}

	/**
	 * Set the status if an instance of this class creates its own events to
	 * ensure a visual update every {@link #dTime} elapsed simulation time.
	 * 
	 * @param value
	 */
	public void setCreateOwnEvents(boolean value) {
		this.createOwnEvents = value;
	}

	/**
	 * Get the simulation time the first visual update should be performed.
	 * 
	 * @return the firstUpdate
	 */
	public final double getFirstUpdate() {
		return firstUpdate;
	}

	/**
	 * Set the simulation time the first visual update should be performed. This
	 * call is only effective before the simulation started.
	 * 
	 * @param firstUpdate
	 *            the firstUpdate to set
	 */
	public final void setFirstUpdate(double firstUpdate) {
		this.firstUpdate = firstUpdate;
	}

	/**
	 * Get the simulation time of the next visual update.
	 * 
	 * @return the nextUpdate
	 */
	public final double getNextUpdate() {
		return nextUpdate;
	}

	/**
	 * Return the time of the periodic interval.
	 * 
	 * @return the dTime
	 */
	public final double getDTime() {
		return dTime;
	}

	/**
	 * Set the time for the periodic interval. A change of this value will be
	 * effective when the method {@link #visualUpdate(SimState)} is invoked.
	 * 
	 * @param time
	 *            the dTime to set
	 */
	public final void setDTime(double time) {
		dTime = time;
	}

	/**
	 * Return the simulation when the recent visual update occurred.
	 * @return the recentUpdate
	 */
	public final double getRecentUpdate() {
		return recentUpdate;
	}

	/**
	 * The method that will return true if a visual update should be performed
	 * for the invoking display; false otherwise. This method tests if the
	 * elapsed time since the recent visual update is equal or greater than
	 * defined in {@link #dTime}. If this condition is met:
	 * <ul>
	 * <li>The time for the next visual update is calculated.</li>
	 * <li>If necessary an event to ensure the visual update is created.</li>
	 * <li>True will be returned.</li>
	 * </ul>
	 * Return false otherwise.
	 */
	public boolean visualUpdate(final SimState state) {
		double currentTime = state.schedule.getTime();

		if (currentTime >= nextUpdate) {
			recentUpdate = currentTime;
			nextUpdate = recentUpdate + dTime;
			if (createOwnEvents) {
				state.schedule.scheduleOnce(nextUpdate, updateEvent);
			}

			return true;
		}

		return false;
	}
}
