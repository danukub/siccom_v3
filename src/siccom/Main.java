package siccom;
    
import java.io.File;
import java.io.InputStreamReader;

/**
 * Main Class creates the Welcome frame to start the simulation with the GUI
 * 
 * @author kubicek
 * @version 1.0
 */
public class Main 								//implements ActionListener
{
	static Welcome welcome;



	/**
	 * Looks for parameter files in the inf-folder
	 * 
	 * @param fn the file name 
	 */
	public static String getLocalFileName(String fn) {
		
		File f = new File (Main.class.getProtectionDomain().getCodeSource()
				.getLocation().getPath());
		
		String fileName = f.getParent()+"/species/"+ fn;

		return fileName;
	}

	/** 
	 * Looks for the next file name in the same folder as Main.class
	 * 
	 * @return fileName the file name
	 */
	public static String getNextLocalFileName()
	{
		String fileName = Main.class.getProtectionDomain().getCodeSource()
		.getLocation().getPath();
		
		return fileName;
	}
	
	/**
	 * Sets up the reading tool for the chosen file
	 * 
	 * @param fileName the name of the file that is read
	 * @return in the input reader
	 */
	public static InputStreamReader getArchiveFile(String fileName) {
		InputStreamReader in = null;
		in = new InputStreamReader(Main.class.getResourceAsStream(fileName));
		return in;
	}

	/**
	 * Creates the Welcome frame on which the main parameters can be set
	 */
	public static void createWelcome()
	{
		welcome = new Welcome();
	}
	
	/**
	 * The main Method
	 * Instantiates the Welcome frame --> starts the GUI 
	 * 
	 * @param args not used
	 */
	public static void main(String[] args)
	{
		createWelcome();
	}
}
