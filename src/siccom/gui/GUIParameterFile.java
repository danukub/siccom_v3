package siccom.gui;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Hashtable;
import java.util.ListIterator;
import java.util.StringTokenizer;
import java.util.Vector;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

/**
 * This class takes a {@link File} or {@link InputStreamReader} object as a
 * reference and opens and read the existing parameter file:
 * <ul>
 * <li>It reads all lines and creates a {@link ParmEntry} object for every to
 * store the information.</li>
 * <li>It stores the read values by the name of the entry. If multiple entries
 * with the same name exists a warning dialog is displayed.</li>
 * <li>The first line is, usually the header, is read and processed like every
 * line.</li>
 * </ul>
 * The read information can be retrieved by invoking {@link #getValue(String)}
 * and {@link #getComment(String)}�where the read value or the comment are
 * returned for the provided key. If the key does not exist null is usually
 * returned.
 * 
 * @author hoehne
 * 
 */
public class GUIParameterFile {
	String fileName;
	/**
	 * The table with the entries.
	 */
	Hashtable<String, ParmEntry> values = new Hashtable<String, ParmEntry>();

	boolean header = false;
	ParmEntry headerLine = null;

	JFrame frame = new JFrame();

	public GUIParameterFile(File file, boolean header) {
		this.header = header;
		this.fileName = file.getName();
		InputStreamReader inputStreamReader = null;
		try {
			inputStreamReader = new InputStreamReader(new FileInputStream(file));
		} catch (IOException e) {
			JOptionPane.showMessageDialog(frame, e, "Error",
					JOptionPane.WARNING_MESSAGE);
		}

		read(inputStreamReader);
	}

	public GUIParameterFile(InputStreamReader inputStreamReader, String fileName,
			boolean header) {
		this.header = header;
		this.fileName = fileName;
		read(inputStreamReader);
	}
	
	
	public void write(){
		
	}

	/**
	 * Read the parameter file and gather all entries.
	 */
	private void read(InputStreamReader inputStreamReader) {
		boolean readHeader = this.header;
		BufferedReader br = new BufferedReader(inputStreamReader);
		int order = 0;

		if (br != null) {
			String line = null;
			ParmEntry entry;

			do {
				line = readLine(br);
				if (line != null) {
					entry = readElement(order, line);
					order++;

					if (readHeader) {
						readHeader = false;
						headerLine = entry;
					} else {
						ParmEntry oldValue = values.put(entry.getName(), entry);

						if (oldValue != null)
							JOptionPane.showMessageDialog(frame, "Value "
									+ entry.getName() + " multiple defined.",
									"Warning", JOptionPane.WARNING_MESSAGE);
					}
				}

			} while (line != null);
		}

		br = null;
		inputStreamReader = null;
	}

	/**
	 * Read a single line from the file.
	 * 
	 * @param br
	 * @return
	 */
	private final String readLine(BufferedReader br) {
		String line = null;
		try {
			line = br.readLine();
		} catch (IOException e) {
			JOptionPane.showMessageDialog(frame, e, "Error",
					JOptionPane.WARNING_MESSAGE);
		}

		return line;
	}

	/**
	 * Read a line from the parameter file and return the first token from that
	 * line.
	 * 
	 * @param br
	 * @return
	 */
	private ParmEntry readElement(int order, String line) {
		StringTokenizer lineElement = new StringTokenizer(line, "|");
		String value = lineElement.nextToken();
		String name = lineElement.nextToken();
		String comment = lineElement.nextToken();

		value = value.trim();
		name = name.trim();
		comment = comment.trim();

		return new ParmEntry(order, name, value, comment);
	}

	/**
	 * Return the file name where the parameters have been read from.
	 * 
	 * @return fileName 
	 */
	public String getFileName() {
		return fileName;
	}

	/**
	 * Return a list with the ordered keys of the stored entries. The list of
	 * keys returned is in the order same as the keys has been added.
	 * 
	 * @return a list of keys
	 */
	public Vector<String> getOrderedKeys() {
		Vector<ParmEntry> elements = new Vector<ParmEntry>(values.values());
		Collections.sort(elements);

		ListIterator<ParmEntry> li = elements.listIterator();
		Vector<String> keys = new Vector<String>(elements.size());
		while (li.hasNext()) {
			ParmEntry entry = li.next();
			keys.add(entry.getName());
		}

		return keys;
	}

	/**
	 * This method returns the value as a string for a given parameter entry.
	 * The parameter entry is defined by its name.
	 * 
	 * @param key
	 * @return the value as a String
	 */
	public String getValue(String key) {
		ParmEntry entry = values.get(key);
		if (entry != null) {
			return entry.getValue();
		}

		return null;
	}

	/**
	 * This method returns the comment as a string for a given parameter entry.
	 * The parameter entry is defined by its name.
	 * 
	 * @param key
	 * @return the entry as String
	 */
	public String getComment(String key) {
		ParmEntry entry = values.get(key);
		if (entry != null) {
			return entry.getComment();
		}

		return null;
	}

	/**
	 * Return the value of the string that names the column for the values.
	 * 
	 * @return column name as String
	 */
	public String getHeaderValue() {
		return headerLine.getValue();
	}

	/**
	 * Return the value of the string that names the column for the names.
	 * 
	 * @return column name as String
	 */
	public String getHeaderName() {
		return headerLine.getName();
	}

	/**
	 * Return the value of the string that names the column for the comments.
	 * 
	 * @return the header as String
	 */
	public String getHeaderComment() {
		return headerLine.getComment();
	}

}

/**
 * An object of this class holds information about a parameter entry.
 * 
 * @author hoehne
 * 
 */
class ParmEntry implements Comparable<ParmEntry> {
	int order;
	String name;
	String value;
	String comment;

	public ParmEntry(int order, String name, String value, String comment) {
		this.order = order;
		this.name = name;
		this.value = value;
		this.comment = comment;
	}

	/*
	 * The methods required by the interfaces
	 */

	public int compareTo(ParmEntry pv) {
		int val1 = getOrder();
		int val2 = pv.getOrder();

		return Integer.signum(val1 - val2);
	}

	public int getOrder() {
		return order;
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public String getComment() {
		return comment;
	}
}
