package siccom;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Frame;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import siccom.gui.GUIParameterFile;
import siccom.gui.SiccomUI;
import siccom.sim.Siccom;
import sim.display.Console;



public class Welcome extends JFrame implements ActionListener
{
	/**
	 * This class is used to setup the Welcome display where the user can set the main parameters which are then passed to {@link Siccom}
	 * 
	 * @author kubicek
	 * @version 1.0
	 */
	private static final long serialVersionUID = 4690645328021897162L;

	/**
	 * The simulation that will be started
	 */
	Siccom sim;
	/**
	 * The GUI
	 */
	SiccomUI siccomGUI;
	
	public JTextField widthField;			// user input for the width of the simulation area
	public JTextField heightField;			// user input for the height of the simulation area
	public JTextField resolutionField; 		// user input for the resolution 
	private JTextField maCoNumField;		// user input for the number of massive coral groups
	private JTextField braCoNumField;		// user input for the number of branching coral groups
	
	public int widthValue;
	public int heightValue;
	public int resolutionValue;
	public int maGroupNum;
	public int braGroupNum;

	private String infoFileExtension = ".inf";
	public GUIParameterFile currentParameterFile = null;
	Frame frame = new JFrame();
	
	
	private void readMainFile(String fn) 
	{
		File file;
		String fileName;
		InputStreamReader in = null;
		try {
			fileName = fn + infoFileExtension ;
			file = new File(Main.getLocalFileName(fileName));
			
			if (file.isFile()) {
				in = new InputStreamReader(new FileInputStream(file));
			} 
			else {
				in = Main.getArchiveFile(fileName);
			}

			GUIParameterFile pf = new GUIParameterFile(in, fileName, true);
			this.currentParameterFile = pf;
			widthValue = readInt(pf, "areaWidth");
			heightValue = readInt(pf, "areaHeight");
			resolutionValue = readInt(pf, "resolution"); 
			maGroupNum = readInt(pf, "maCoNum");
			braGroupNum = readInt(pf, "braCoNum");
			Siccom.indivOutInter = readInt(pf, "indivOutInter");
			

		}
		catch (IOException e) 
		{
			JOptionPane.showMessageDialog(frame, e, "Error",
					JOptionPane.WARNING_MESSAGE);
		}
	
	}
	
	public int readInt(GUIParameterFile pf, String key) {
		String str = pf.getValue(key);

		int val = 0;
		try {
			val = Integer.valueOf(str).intValue();
		} catch (NumberFormatException ex) {
			JOptionPane.showMessageDialog(frame, ex, "Error",
					JOptionPane.WARNING_MESSAGE);
		}
		return val;
	}

	/**
	 * Constructor
	 * Set up the Welcome frame and the different panels with text fields as well as the start button
	 * 
	 */
	public Welcome()
	{
		super();
		setTitle("Welcome to the Reef Simulator");
		setSize(450, 700);
		setResizable(false);

		
		
		Siccom.gui = true;
		readMainFile("mainParam");
		
		File directory = new File(Main.class.getProtectionDomain().getCodeSource()
				.getLocation().getPath());

		directory = new File(directory.getParent() +"/species");
		
		
		/* NORTH PANEL*/
		// title
		
		ImageIcon siccomImage = new ImageIcon(directory.getPath() + "/siccom.png");
		JLabel siccomLabel = new JLabel();
		siccomLabel.setHorizontalAlignment(JLabel.CENTER);
		siccomLabel.setIcon(siccomImage);

		
		//subtitle
		JPanel subTitleP = new JPanel();
		JLabel subTitle = new JLabel("Spatial Interaction in Coral Reef Communities");
		subTitle.setFont(new Font(null, 1, 16));
		subTitleP.add(subTitle);
		
		// put together the north panel
		JPanel northPanel = new JPanel(new BorderLayout());
//		northPanel.setBackground(Color.WHITE);
		northPanel.add(siccomLabel, BorderLayout.CENTER);
		northPanel.add(subTitleP, BorderLayout.SOUTH);

		
		/* CENTER PANEL*/
		// image
		JPanel iconPanel = new JPanel();
		
		ImageIcon image = new ImageIcon(directory.getPath() + "/UkombeReef.JPG");
//		System.out.println(directory.getPath());
		
		JLabel iconLabel = new JLabel();
		iconLabel.setIcon(image);
		iconPanel.add(iconLabel);
		
						
		// parameters
		JLabel widthText = new JLabel		("Width of Simulation Area [m]: ");
		widthField = new JTextField(5);
		widthField.setHorizontalAlignment(JTextField.RIGHT);
		widthField.setText(String.valueOf(widthValue));
		
			
		JLabel heightText = new JLabel		("Height of Simulation Area [m]: ");
		heightField = new JTextField(5);
		heightField.setHorizontalAlignment(JTextField.RIGHT);
		heightField.setText(String.valueOf(heightValue));
		
		JLabel resolutionText = new JLabel	("Resolution [cm/pixel]: ");
		resolutionField = new JTextField(5);
		resolutionField.setHorizontalAlignment(JTextField.RIGHT);
		resolutionField.setText(String.valueOf(resolutionValue));
		
		JLabel maCoNumText = new JLabel	("Number of massive coral groups: ");
		maCoNumField = new JTextField(5);
		maCoNumField.setHorizontalAlignment(JTextField.RIGHT);
		maCoNumField.setText(String.valueOf(maGroupNum));
		
		JLabel braCoNumText = new JLabel("Number of branching coral groups: ");
		braCoNumField = new JTextField(5);
		braCoNumField.setHorizontalAlignment(JTextField.RIGHT);
		braCoNumField.setText(String.valueOf(braGroupNum));
	
		JPanel parmP = new JPanel();
		
		JPanel textP = new JPanel(new GridLayout(5,1, 12, 16));
		textP.setSize(150, 350);
		
		textP.add(widthText);
		textP.add(heightText);
		textP.add(resolutionText);
		textP.add(maCoNumText);
		textP.add(braCoNumText);		

		JPanel boxP = new JPanel(new GridLayout(5,1,12, 12));
		textP.setSize(150, 150);

		boxP.add(widthField);
		boxP.add(heightField);
		boxP.add(resolutionField);
		boxP.add(maCoNumField);
		boxP.add(braCoNumField);
		
		
		parmP.add(textP);
		parmP.add(boxP);
		
		// put together the center panel
		JPanel centerPanel = new JPanel(new BorderLayout());
		centerPanel.add(iconPanel, BorderLayout.NORTH);
		centerPanel.add(parmP, BorderLayout.CENTER);
		
		
		/* START BUTTON */
		JButton start = new JButton("Start");
		start.addActionListener(this);
		
		
		/* MAIN PANEL */
		// put together the main panel
		JPanel welcomePnl = new JPanel();
		welcomePnl.setLayout(new BorderLayout());
		welcomePnl.add(northPanel, BorderLayout.NORTH);
		welcomePnl.add(centerPanel, BorderLayout.CENTER);
		welcomePnl.add(start, BorderLayout.SOUTH);
		
		add(welcomePnl);
		setVisible(true);			
	}

	/**
	 * When the Start-Button is pressed the parameters set in the text fields are passed to {@link Siccom}
	 * and the GUI with console and display is set up.
	 */
	@Override
	public void actionPerformed(ActionEvent evt) 
	{
		// get parameters
		Siccom.areaWidth = Integer.valueOf(widthField.getText().trim()).intValue();
		Siccom.areaHeight = Integer.valueOf(heightField.getText().trim()).intValue();

		Siccom.resolution = (Integer.valueOf(resolutionField.getText().trim()).intValue());
		Siccom.dimensionConv_meters = Siccom.resolution / 100.0;
		Siccom.dimensionConv_milimeters = 1 / (Siccom.resolution*10) /12;
		
		Siccom.gridWidth  = Siccom.areaWidth / Siccom.dimensionConv_meters;
		Siccom.gridHeight = Siccom.areaHeight / Siccom.dimensionConv_meters;
				
		Siccom.totalArea = Siccom.gridWidth * Siccom.gridHeight;
		SiccomUI.maCoGroupNum = Integer.valueOf(maCoNumField.getText().trim()).intValue();
		SiccomUI.braCoGroupNum = Integer.valueOf(braCoNumField.getText().trim()).intValue();
		Siccom.maCoGroupNum = Integer.valueOf(maCoNumField.getText().trim()).intValue();
		Siccom.braCoGroupNum = Integer.valueOf(braCoNumField.getText().trim()).intValue();

		// setup the GUI
		siccomGUI = new SiccomUI();
		siccomGUI.initParameters();
		SiccomUI.cons = new Console(siccomGUI);
		SiccomUI.cons.setSize(SiccomUI.consWidth, SiccomUI.consHeight);
		SiccomUI.cons.setVisible(true);
		SiccomUI.cons.setLocation((int)Siccom.gridWidth+80, 0);
		
		// remove the welcome panel
		this.dispose();
	}
}
