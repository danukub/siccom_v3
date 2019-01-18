package siccom.gui;

import org.jfree.chart.axis.ValueAxis;
import org.jfree.chart.plot.XYPlot;

import sim.util.media.chart.TimeSeriesChartGenerator;

/**
 * Once 10 years of simulation time are exceeded the charts are set from 
 * static to dynamic 
 * 
 * @author kubicek
 * @version 1.0
 */

public class DynamicCharter extends TimeSeriesChartGenerator
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 7899793031369189378L;
	private ValueAxis axis;
	private ValueAxis axis2;
	
	public XYPlot plot = chart.getXYPlot();
	
	public void setXDynamic(String n)
    {
		axis = ((XYPlot)(chart.getPlot())).getDomainAxis();
    	axis.setRange(0, 10);
    	axis.setAutoRange(true);
    	axis.setFixedAutoRange(10.0);
    	axis2 = ((XYPlot)(chart.getPlot())).getRangeAxis();
    	if(n=="temp")
    	{
    		axis2.setAutoRange(true);
    		axis2.setFixedAutoRange(7.0);
    	}
    	else 
    		axis2.setAutoRangeMinimumSize(10);
    }
	
	public void setYDynamic()
	{
	   	axis2 = ((XYPlot)(chart.getPlot())).getRangeAxis();
		axis2.setAutoRange(true);
		axis2.setFixedAutoRange(7.0);
	}
	
	
}
