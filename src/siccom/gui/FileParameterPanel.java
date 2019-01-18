package siccom.gui;

import java.awt.BorderLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ListIterator;
import java.util.Vector;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;

import siccom.Main;
import siccom.sim.Siccom;

/**
 * Creates panels of the parameter files in the inf-folder
 * 
 * @author kubicek
 * @version 1.0
 */


public class FileParameterPanel extends JPanel {
	private static final long serialVersionUID = 1L;
	public JTable table;
	private DefaultTableModel model;
	BufferedReader br;
	Vector<String> columnNames;
	Vector<Vector<String>> data;
	BufferedWriter output;
	FileOutputStream file = null;
	boolean reset = false;
	JFrame pFrame = new JFrame();
	public GUIParameterFile parameterFile;
	public String fileName;
	
	// references to sim with ui, sim state
	SiccomUI crpsUI;
	Siccom crps;
	public JButton btnReset;
	public JButton btnSave;
	

	public FileParameterPanel(SiccomUI crpsUI, File file, String fileName) {
		this.crpsUI = crpsUI;
		this.crps = (Siccom) crpsUI.state;

		this.fileName = fileName;
		
		parameterFile = new GUIParameterFile(file, true);
		
		readTableEntries(parameterFile);
		// Create table using the DefaultTableModel
		model = new DefaultTableModel(data, columnNames);
		table = new JTable(model);
		table = new JTable(model) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return (columnIndex == 0) ? true : false;
			}
		};
		
		this.setLayout(new BorderLayout());
		
		
		JScrollPane scrollPane = new JScrollPane(table);
		this.add(scrollPane, BorderLayout.CENTER);
		JPanel pnlButtons = new JPanel();
		this.add(pnlButtons, BorderLayout.SOUTH);

		// Reset button
		btnReset = new JButton("Reset");
		pnlButtons.add(btnReset);
		btnReset.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				reset = true;
				readTableEntries(parameterFile);
				write();
				readTableEntries(parameterFile);
				table.revalidate();
				table.repaint();
			}

		});

		// Save table to file button
		btnSave = new JButton("Save");
		pnlButtons.add(btnSave);
		btnSave.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Write out the table data
				// extract column names
				write();
				table.revalidate();
				table.repaint();

			}
		});

		this.setVisible(true);
	}

	/**
	 * Writes the entries from the file into the table
	 */
	public void write() {
		// Finish editing the current cell before saving
		TableCellEditor tce = null;
		if (table.isEditing()) {
			tce = table.getCellEditor();
		}
		if (tce != null) {
			tce.stopCellEditing();

		}

		int rows = table.getRowCount();
		int columns = table.getColumnCount();

		// Write out the Column Headers
		TableColumnModel header = table.getColumnModel();

		try {
			String fileName = Main.getLocalFileName(parameterFile.getFileName());
			file = new FileOutputStream(fileName+"_mod");
		} catch (FileNotFoundException e1) {
			JOptionPane.showMessageDialog(pFrame, e1, "Warning",
					JOptionPane.WARNING_MESSAGE);
		}
		output = new BufferedWriter(new OutputStreamWriter(file));
		try {
			for (int k = 0; k < columns; k++) {
				TableColumn column = header.getColumn(k);
				String value = (String) column.getHeaderValue();

				output.write(value);
				output.write("|");

			}

			output.newLine();

			for (int j = 0; j < rows; j++) {
				for (int k = 0; k < columns; k++) {
					String value = (String) table.getValueAt(j, k);

					output.write(value);
					output.write("|");
				}
				output.newLine();
			}
			output.close();
		} catch (IOException e1) {
			JOptionPane.showMessageDialog(pFrame, e1, "Warning",
					JOptionPane.WARNING_MESSAGE);
		}
		
	}
	
	/**
	 * Reads the table entries of the parameter file and puts them into the table of the panel
	 * 
	 * @param pf the parameter file from which the table entries are read
	 */
	private void readTableEntries(GUIParameterFile pf) {
		columnNames = new Vector<String>();
		data = new Vector<Vector<String>>();
		columnNames.addElement(pf.getHeaderValue());
		columnNames.addElement(pf.getHeaderName());
		columnNames.addElement(pf.getHeaderComment());

		Vector<String> orderedKeys = pf.getOrderedKeys();
		ListIterator<String> li = orderedKeys.listIterator();
		while (li.hasNext()) {
			Vector<String> row = new Vector<String>(3);
			String key = li.next();
			row.addElement(pf.getValue(key));
			row.addElement(key);
			row.addElement(pf.getComment(key));

			row.trimToSize();
			data.addElement(row);
		}
	}

}


