/**
 MyCBR License 1.1

 Copyright (c) 2008
 Thomas Roth-Berghofer, Armin Stahl & Deutsches Forschungszentrum f&uuml;r K&uuml;nstliche Intelligenz DFKI GmbH
 Further contributors: myCBR Team (see http://mycbr-project.net/contact.html for further information 
 about the mycbr Team). 
 All rights reserved.

 MyCBR is free software; you can redistribute it and/or modify
 it under the terms of the GNU General Public License as published by
 the Free Software Foundation; either version 2 of the License, or
 (at your option) any later version.

 This program is distributed in the hope that it will be useful,
 but WITHOUT ANY WARRANTY; without even the implied warranty of
 MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 GNU General Public License for more details.

 You should have received a copy of the GNU General Public License
 along with this program; if not, write to the Free Software
 Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA

 Since MyCBR uses some modules, you should be aware of their licenses for
 which you should have received a copy along with this program, too.
 
 endOfLic**/
package de.dfki.mycbr.modelprovider.protege.similaritymodeling;

import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Vector;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.filechooser.FileFilter;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableCellRenderer;

import de.dfki.mycbr.CBRProject;
import de.dfki.mycbr.Helper;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.SpecialValueHandler;
import de.dfki.mycbr.modelprovider.ModelProvider;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Project;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.model.ValueType;
import edu.stanford.smi.protege.ui.ClsesPanel;
import edu.stanford.smi.protege.util.SelectionEvent;
import edu.stanford.smi.protege.util.SelectionListener;

/**
 * @author myCBR Team
 *
 * GUI for instances import.
 */
public class ImportInstancesDialog extends JDialog implements ActionListener, SelectionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(ImportInstancesDialog.class.getName());
	
	private static final String FILENAME_TMP_PROPOSED_VALUETYPES = "tmpProposedValueTypes.properties"; //$NON-NLS-1$

	JPanel paContent = new JPanel();

	JLabel jLabel1 = new JLabel();

	JTextField txtFile = new JTextField();

	JButton buSelect = new JButton();

	JButton buImport = new JButton();

	JScrollPane scrollpane = null;

	JTable table = new JTable();

	JButton buClose = new JButton();

	JCheckBox cbBuildSMFsOnly = new JCheckBox(Messages.getString("Build_slots_and_smf_dont_import_cases"), false); //$NON-NLS-1$

	JCheckBox cbSeperator = new JCheckBox(Messages.getString("Change_main_seperator"), false); //$NON-NLS-1$

	JTextField txtSeperator = new JTextField(CSVImport.getSeperator());

	JCheckBox cbInternalSeperator = new JCheckBox(Messages.getString("Use_internal_seperator"), false); //$NON-NLS-1$
	JCheckBox cbCheckExistingSlots = new JCheckBox(Messages.getString("Check_configuration_of_existing_slots"), false); //$NON-NLS-1$

	JTextField txtInternalSeperator = new JTextField(CSVImport.getInternalSeperator());

	GridBagLayout gridBagLayout1 = new GridBagLayout();

	JButton buChoose = new JButton();

	JPanel paMainClass = new JPanel();

	JProgressBar progressBar = new JProgressBar();

	JLabel jLabel2 = new JLabel();

	private ClsesPanel paCls;

	GridBagLayout gridBagLayout2 = new GridBagLayout();

	Cls selectedCls = null;
	ModelCls modelCls = null;

	private Project protegeProject;

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private Vector nameIndices = null;

	private Vector<ErrorMessageCSV> errorMessages;

	private Vector<String> header = new Vector<String>();

	public ImportInstancesDialog(Frame frame, String title, boolean modal, Project protProject) {
		super(frame, title, modal);
		try {
			this.protegeProject = protProject;

			if (protProject != null) {
				paCls = new ClsesPanel(protProject);
				paCls.addSelectionListener(this);
			} else {
				modelCls = CBRProject.getInstance().getAllModelCls().iterator().next();
			}

			table.getDefaultEditor(String.class).addCellEditorListener(new CellEditorListener() {
				@SuppressWarnings("unchecked") //$NON-NLS-1$
				public void editingStopped(ChangeEvent e) {
					int row = table.getSelectedRow();
					DefaultTableModel tableModel = ((DefaultTableModel) table.getModel());
					Vector tableData = tableModel.getDataVector();
					Vector rowData = (Vector) tableData.get(row);
					ErrorMessageCSV em = CSVImport.checkImportInstance(modelCls, rowData, header);
					if (errorMessages!=null && errorMessages.size()>row) {
						errorMessages.set(row, em);
					}
					table.repaint();
				}

				public void editingCanceled(ChangeEvent e) {
					// nothing to do
				}
			});
			
			
			TableCellRenderer tcr = new DefaultTableCellRenderer() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;
				Color defColor = new JLabel().getBackground();
				JButton buAdaptModel = new JButton(Messages.getString("+")); //$NON-NLS-1$
				boolean initialized = false;
				
				JPanel panel = new JPanel();
				
				{
					panel.setLayout(new GridBagLayout());
					
					panel.add(buAdaptModel, new GridBagConstraints(1,0, 1,1, 0d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
				}
				
				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected, boolean hasFocus, int row, int column) {
					Component comp = super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row, column);
					
					if (!initialized) {
						panel.add(comp, new GridBagConstraints(0,0, 1,1, 1d,1d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
						initialized = true;
					}
					
					comp.setBackground(defColor);
					buAdaptModel.setVisible(false);
					if (errorMessages!=null && row<errorMessages.size()) {
						ErrorMessageCSV msg = errorMessages.get(row);
						if (msg!=null && msg.getAttribute()!=null) {
							int index = header.indexOf(msg.getAttribute());
							if (column == index) {
								comp.setBackground(Color.red);
								buAdaptModel.setVisible(true);
							}
						}
					}
					return panel;
				}
			};
			table.addMouseListener(new MouseAdapter() {
				int buttonWidth = new JButton(Messages.getString("+")).getPreferredSize().width; //$NON-NLS-1$
				@SuppressWarnings("unchecked") //$NON-NLS-1$
				@Override
				public void mouseClicked(MouseEvent e) {
					try {
						int col = table.columnAtPoint(e.getPoint());
						int col2 = table.columnAtPoint(new Point(e.getPoint().x + buttonWidth, e.getPoint().y));
						if (col2 == col) {
							return;
						}
						int row = table.rowAtPoint(e.getPoint());
						if (errorMessages != null && row < errorMessages.size()) {
							ErrorMessageCSV msg = errorMessages.get(row);
							if (msg!=null && msg.getAttribute() != null) {
								int index = header.indexOf(msg.getAttribute());
								if (col == index) {
									//
									// change ModelSlot
									//
									String entry = (String) table.getValueAt(row, col);
									Slot slot = protegeProject.getKnowledgeBase().getSlot(header.elementAt(index));
									Collection<String> values = new ArrayList<String>();
									if (slot.getAllowsMultipleValues()) {
										String[] split = entry.split(CSVImport.getInternalSeperator());
										for (int i=0; i<split.length; i++) {
											values.add(split[i]);
										}
									} else {
										values.add(entry);
									}
									for (String valStr : values) {
										if (slot.getValueType().toString().equals(ValueType.INTEGER.toString())) {
											int min = slot.getMinimumValue().intValue();
											int max = slot.getMaximumValue().intValue();
											float val = Integer.parseInt(valStr);
											if (val < min) {
												slot.setMinimumValue(val);
											}
											if (val > max) {
												slot.setMaximumValue(val);
											}
										} else if (slot.getValueType().toString().equals(ValueType.FLOAT.toString())) {
											float min = slot.getMinimumValue().floatValue();
											float max = slot.getMaximumValue().floatValue();
											float val = Float.parseFloat(valStr);
											if (val < min) {
												slot.setMinimumValue(val);
											}
											if (val > max) {
												slot.setMaximumValue(val);
											}
										} else if (slot.getValueType().toString().equals(ValueType.SYMBOL.toString())) {
											Collection av = new Vector(slot.getAllowedValues());
											if (!av.contains(valStr)) {
												av.add(valStr);
											}
											slot.setAllowedValues(av);
										}
									}
									DefaultTableModel tableModel = ((DefaultTableModel) table.getModel());
									Vector tableData = tableModel.getDataVector();
									
									for (int i=0; i<tableData.size(); i++) {
										Vector rowData = (Vector) tableData.get(i);
										ErrorMessageCSV em = CSVImport.checkImportInstance(modelCls, rowData, header);
										errorMessages.set(i, em);
									}
									table.repaint();
								}
							}
						}
					} catch (Throwable ex) {
						log.log(Level.FINE, Messages.getString("Cound_not_change_slot"), ex); //$NON-NLS-1$
					}
				}
			});
			table.addMouseMotionListener(new MouseMotionAdapter() {
				@Override
				public void mouseMoved(MouseEvent e) {
					int col = table.columnAtPoint(e.getPoint());
					int row = table.rowAtPoint(e.getPoint());
					String txt = null;
					if (errorMessages != null && row < errorMessages.size()) {
						ErrorMessageCSV msg = errorMessages.get(row);
						if (msg != null && msg.getAttribute() != null) {
							int index = header.indexOf(msg.getAttribute());
							if (col == index) {
								txt = msg.getMessage();
							}
						}
					}
					table.setToolTipText(txt);
				}
			});
			table.setDefaultRenderer(Object.class, tcr);
			table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
			txtFile.addFocusListener(Helper.focusListener);
			txtInternalSeperator.addFocusListener(Helper.focusListener);
			txtSeperator.addFocusListener(Helper.focusListener);
			
			jbInit();
			customInit();

			pack();

			setSize(600, 650);
			Helper.centerWindow(this);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	/**
	 *  
	 */
	private void customInit() {
		buImport.setEnabled(false);

		buSelect.addActionListener(this);
		buClose.addActionListener(this);
		buImport.addActionListener(this);
		buChoose.addActionListener(this);

		KeyListener kl = new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				JTextField txtFld = (JTextField) e.getSource();
				String txt = txtFld.getText();
				if (txt.length() == 0) {
					return;
				}
				
				// commented out before 20.10.2008
//				if (txt.length() > 1)
//					txtFld.setText(txt.substring(0, 1));
				
				if (txtFld == txtSeperator) {
					CSVImport.setSeperator(txt);
				}
				if (txtFld == txtInternalSeperator) {
					CSVImport.setInternalSeperator(txt);
				}
				File file = new File(txtFile.getText());
				if (!file.exists()) {
					return;
				}
				loadTable(file);
			}
		};
		txtSeperator.addKeyListener(kl);
		txtInternalSeperator.addKeyListener(kl);

		txtSeperator.setEnabled(cbSeperator.isSelected());
		txtInternalSeperator.setEnabled(cbInternalSeperator.isSelected());
		cbSeperator.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				txtSeperator.setEnabled(cbSeperator.isSelected());
			}
		});
		cbInternalSeperator.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				txtInternalSeperator.setEnabled(cbInternalSeperator.isSelected());
			}
		});

	}

	private void jbInit() throws Exception {
		paContent.setLayout(gridBagLayout1);
		jLabel1.setText(Messages.getString("Select_file_to_import(cvs)")); //$NON-NLS-1$
		txtFile.setText(""); //$NON-NLS-1$
		buSelect.setMaximumSize(new Dimension(90, 23));
		buSelect.setMinimumSize(new Dimension(90, 23));
		buSelect.setPreferredSize(new Dimension(90, 23));
		buChoose.setMaximumSize(new Dimension(90, 23));
		buChoose.setMinimumSize(new Dimension(90, 23));
		buChoose.setPreferredSize(new Dimension(90, 23));
		buSelect.setText(Messages.getString("Select")); //$NON-NLS-1$
		this.setTitle(Messages.getString("Importer")); //$NON-NLS-1$
		buImport.setMaximumSize(new Dimension(90, 23));
		buImport.setMinimumSize(new Dimension(90, 23));
		buImport.setPreferredSize(new Dimension(90, 23));
		buImport.setMargin(new Insets(2, 14, 2, 14));
		buImport.setText(Messages.getString("Import")); //$NON-NLS-1$
		buClose.setMaximumSize(new Dimension(90, 23));
		buClose.setMinimumSize(new Dimension(90, 23));
		buClose.setPreferredSize(new Dimension(90, 23));
		buClose.setText(Messages.getString("Close")); //$NON-NLS-1$
		buChoose.setText(Messages.getString("Choose")); //$NON-NLS-1$
		paMainClass.setBorder(BorderFactory.createLoweredBevelBorder());
		paMainClass.setLayout(gridBagLayout2);
		jLabel2.setText(Messages.getString("Choose_main_class")); //$NON-NLS-1$
		if (paCls != null) {
			paCls.setBorder(BorderFactory.createLoweredBevelBorder());
		}
		//        cbPrompt.setText("prompt for error handling");
		getContentPane().add(paContent);
		paContent.add(jLabel1, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 10, 5, 5), 0, 0));
		paContent.add(txtFile, new GridBagConstraints(0, 1, 2, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));
		paContent.add(buSelect, new GridBagConstraints(3, 1, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

		scrollpane = new JScrollPane(table);
		paContent.add(scrollpane, new GridBagConstraints(0, 2, 4, 1, 1.0, 3.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 0, 5), 0, 0));

		paContent.add(createOptionsPanel(), new GridBagConstraints(0, 4, 4, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

		paContent.add(buClose, new GridBagConstraints(1, 8, 2, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		paContent.add(buImport, new GridBagConstraints(3, 8, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		paContent.add(buChoose, new GridBagConstraints(2, 1, 1, 1, 0.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 0, 5, 0), 0, 0));
		paContent.add(paMainClass, new GridBagConstraints(0, 3, 4, 1, 0.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		paMainClass.add(jLabel2, new GridBagConstraints(0, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(10, 5, 5, 5), 0, 0));
		if (paCls != null) {
			paMainClass.add(paCls, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		}
		paContent.add(progressBar, new GridBagConstraints(0, 8, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 0), 0, 0));
	}

	private JPanel createOptionsPanel() {
		JPanel paOptions = new JPanel(new GridBagLayout());

		Dimension minSize = new Dimension(50, txtSeperator.getPreferredSize().height);
		txtSeperator.setMinimumSize(minSize);
		txtInternalSeperator.setMinimumSize(minSize);

		paOptions.add(cbSeperator, 			new GridBagConstraints(0, 1, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		paOptions.add(txtSeperator, 		new GridBagConstraints(1, 1, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

		paOptions.add(cbInternalSeperator, 	new GridBagConstraints(0, 2, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		paOptions.add(txtInternalSeperator, new GridBagConstraints(1, 2, 1, 1, 1.0, 0.0, GridBagConstraints.CENTER, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

		paOptions.add(cbBuildSMFsOnly, 		new GridBagConstraints(0, 4, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		paOptions.add(cbCheckExistingSlots, new GridBagConstraints(0, 5, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

		return paOptions;
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public void actionPerformed(ActionEvent e) {
		Object source = e.getSource();

		if (source == buChoose) {
			// maybe we can start selection from a certain directory.
			File file = null;
			if (!"".equals(txtFile.getText()) || CBRProject.getInstance().getProjectDir() == null) { //$NON-NLS-1$
				file = new File(txtFile.getText());
			} else {
				file = new File(CBRProject.getInstance().getProjectDir());
			}
			JFileChooser fc = new JFileChooser(file);
			fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			fc.setDialogType(JFileChooser.OPEN_DIALOG);
			fc.setFileFilter(new FileFilter() {
				public boolean accept(File file) {
					return file.toString().toUpperCase().endsWith(".CSV") || file.isDirectory(); //$NON-NLS-1$
				}

				public String getDescription() {
					return Messages.getString("Comma_seperated_value"); //$NON-NLS-1$
				}
			});
			if (fc.showOpenDialog(this) == JFileChooser.APPROVE_OPTION) {
				txtFile.setText(fc.getSelectedFile().toString());
				loadTable(fc.getSelectedFile());
			}
		} else if (source == buSelect) {
			File file = new File(txtFile.getText());
			loadTable(file);
		} else if (source == buClose) {
			dispose();
		} else if (source == buImport) {
			if (selectedCls == null && modelCls == null) {
				// might never happen
				System.err.println("selected main class is null!"); //$NON-NLS-1$
				return;
			}

			boolean buildSMFsOnly = cbBuildSMFsOnly.isSelected();

			DefaultTableModel tableModel = ((DefaultTableModel) table.getModel());
			boolean checkExistingSlots = cbCheckExistingSlots.isSelected();

			// get header
			Collection configureAttributes = new ArrayList();
			Vector<String> newHeader = new Vector<String>();
			for (int i = 0; i < tableModel.getColumnCount(); i++) {
				String slotName = tableModel.getColumnName(i);
				newHeader.add(slotName);

				// log.info("find slot "+slotName+" in "+protProject.getName());
				if (protegeProject != null) {
					Slot slot = protegeProject.getKnowledgeBase().getSlot(slotName);
					if (slot == null || checkExistingSlots) {
						// Attribute not found. Want to create one?
	//					log.fine("Attribute not found! " + slotName);
						configureAttributes.add(slotName);
					}
				}
			}
			if (configureAttributes.size() > 0) {
				JDialog dialog = new JDialog(this, true);
				dialog.setTitle(Messages.getString("Create_slots")); //$NON-NLS-1$
				CreateSlotsPanel csp = new CreateSlotsPanel(configureAttributes, newHeader, tableModel.getDataVector(), SpecialValueHandler.getInstance(), cbInternalSeperator.isSelected(), selectedCls, protegeProject);
				dialog.getContentPane().add(csp);
				dialog.setSize(600, 600);
				Helper.centerWindow(dialog);
				
				csp.proposeValueTypes();
				Map<String, ValueType> proposedValueTypes = new HashMap<String, ValueType>();
				BufferedReader br = null;
				File fileTmpProposedValueTypes = null;
				try {
					fileTmpProposedValueTypes = new File(FILENAME_TMP_PROPOSED_VALUETYPES);
					br = new BufferedReader(new FileReader(fileTmpProposedValueTypes));
					String line = br.readLine();
					if (fileTmpProposedValueTypes.exists() && txtFile.getText().equals(line)) {
						line = br.readLine();
						while (line != null) {
							String[] split = line.split(";"); //$NON-NLS-1$
							proposedValueTypes.put(split[0], ValueType.valueOf(split[1]));
							line = br.readLine();
						}
					}
					br.close();
				} catch (Exception exc) {
					log.log(Level.FINE, "could not read temporary file for proposed value types (not critical):\n", exc); //$NON-NLS-1$
					proposedValueTypes.clear();
				}
				if (!proposedValueTypes.isEmpty()) {
					csp.setProposedValueTypes(proposedValueTypes);
				}

				// open the dialog
				dialog.setVisible(true);
				if (!csp.hasBeenConfirmed()) {
					return;
				}
				
				// save preferences
				proposedValueTypes = csp.getProposedValueTypes();
				try {
					if (fileTmpProposedValueTypes != null) {
						PrintStream out = new PrintStream(fileTmpProposedValueTypes);
						out.println(txtFile.getText());
						for (Entry entry : proposedValueTypes.entrySet()) {
							out.println(entry.getKey().toString() + ";" + entry.getValue()); //$NON-NLS-1$ //$NON-NLS-2$
						}
					}
				} catch (FileNotFoundException e2) {
					e2.printStackTrace();
				}
				
				// commented out before 20.10.2008
//				log.fine("create initial similarity measures, now");
//				for (Iterator it = configureAttributes.iterator(); it.hasNext();)
//				{
//					String newAttName = (String) it.next();
//					ModelInstance modelInstance = mycbrProject.getModelInstanceByName(newAttName);
//					String newSMFname = newAttName.replaceAll(" ", "_") + "_smf";
//					try
//					{
//						mycbrProject.newSMF(modelInstance, newSMFname);
//					} catch (Exception e1)
//					{
//						log.log(Level.INFO, "Could not initialize smf [" + newSMFname + "]", e);
//					}
//				}
//				// init class smf if not available.
//				try
//				{
//					ModelCls cls = (ModelCls) mycbrProject.getModelInstanceByName(selectedCls.getName());
//					SMFHolder clsHolder = mycbrProject.getSMFHolderForModelInstance(cls);
//					String newSMFname = cls.getName() + "_smf";
//					if (clsHolder.getActiveSMF() == null)
//					{
//						try
//						{
//							mycbrProject.newSMF(cls, newSMFname);
//						} catch (Exception e1)
//						{
//							log.log(Level.INFO, "Could not initialize smf [" + newSMFname + "]", e);
//						}
//					}
//				} catch (Throwable t)
//				{
//					log.log(Level.INFO, String.format("Could not initilialize similarity measure for Class [%s]", selectedCls.getName()), t);
//					JOptionPane.showMessageDialog(this, "Slots and similarity measure successfully initialized.", "Note", JOptionPane.INFORMATION_MESSAGE);
//				}
//
			}

			if (buildSMFsOnly) {
				// return now
				JOptionPane.showMessageDialog(this, Messages.getString("Successfully_done"), Messages.getString("Done"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
				dispose();
				return;
			}

			// get default name indices
			if (nameIndices == null) {
				ImportInstancesNameChooser nameGenerator = new ImportInstancesNameChooser(this, true, newHeader, tableModel.getDataVector());
				Helper.centerWindow(nameGenerator);
				nameGenerator.setVisible(true);
				nameIndices = nameGenerator.getNameIndices();
			}
			log.fine("selected indices are [" + nameIndices + "] and name composes like [" + generateName(nameIndices, newHeader) + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$

			// store the instances (the rows) which
			// could not be imported in an extra vector
			Vector criticalData = new Vector();
			Vector<ErrorMessageCSV> newErrorMessages = new Vector<ErrorMessageCSV>();

			Vector tableData = tableModel.getDataVector();
			int tableSize = tableData.size();

			progressBar.setVisible(true);
			progressBar.setStringPainted(true);
			int step = tableSize / 100;
			int cnt = 0;

			String name = null;
			for (int i = 0; i < tableSize; i++) {
				Vector row = (Vector) tableData.get(i);

				name = generateName(nameIndices, row);
//				String errorMsg = CSVImport.checkImportInstance(cbrProject, selectedCls, row, newHeader);
				ErrorMessageCSV errorMsg = CSVImport.checkImportInstance(modelCls, row, newHeader);
				if (errorMsg != null) {
					criticalData.add(row);
					newErrorMessages.add(errorMsg);
				} else {
					// no problems occured -> just import
					if (name != null && protegeProject != null) {
						while (protegeProject.getKnowledgeBase().getInstance(name) != null) {
							log.fine("[" + name + "] already exists. Try another one..."); //$NON-NLS-1$ //$NON-NLS-2$
							name = name + "_" + i; //$NON-NLS-1$
						}
					}

					// log.info("import no. "+i);
					CSVImport.importInstance(modelCls, row, newHeader, name);
				}
				cnt++;
				if (cnt >= step) {
					cnt = 0;
					int percentage = (int) ((i * 100) / tableSize);
					progressBar.setValue(percentage);
					progressBar.setString(name);
					progressBar.paintImmediately(progressBar.getVisibleRect());
				}

			}
			progressBar.setValue(100);
			progressBar.setString(name);

			tableModel.setDataVector(criticalData, newHeader);
			updateButtons();

			this.errorMessages = newErrorMessages;
			repaint();
			header = newHeader;
			
			if (criticalData.size() > 0) {
				JOptionPane.showMessageDialog(this, Messages.getString("Could_not_import_some_rows"), Messages.getString("Note"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				JOptionPane.showMessageDialog(this, Messages.getString("Successfully_done"), Messages.getString("Done"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
				dispose();
			}

		}

	}
	

	/**
	 * @param file
	 */
	private void loadTable(File file) {
		try {
			ArrayList<String[]> convData = new ArrayList<String[]>();
			String[] header = CSVImport.readTable(file, convData);
			if (header == null) {
				return;
			}

			// our table data must be a new array: remove first row (=newHeader)
			try {
				String[][] data = new String[convData.size()][header.length];
				for (int i = 0; i < data.length; i++) {
					for (int j = 0; j < data[0].length; j++) {
						data[i][j] = convData.get(i)[j];
					}
				}
				// set data & header
				table.setModel(new DefaultTableModel(data, header));
				table.doLayout();
			} catch (Throwable t) {
				// rethrow
				throw new Exception(Messages.getString("Amount_of_colums_is_not_constant"), t); //$NON-NLS-1$
			}

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(this, String.format(Messages.getString("Loading_failed"), e.toString()), Messages.getString("Error"), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$ //$NON-NLS-2$
			return;
		}
		updateButtons();
	}

	public void selectionChanged(SelectionEvent event) {
		if (paCls != null) {
			selectedCls = (Cls) paCls.getSelection().iterator().next();
			modelCls = (ModelCls) ModelProvider.getInstance().getModelInstance(selectedCls.getName());
		}
		updateButtons();
	}

	private void updateButtons() {
		if ((selectedCls != null || modelCls != null) && (table.getColumnCount() > 0 && table.getRowCount() > 0)) {
			buImport.setEnabled(true);
		} else {
			buImport.setEnabled(false);
		}
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public static String generateName(Vector nameIndices, Vector row) {
		// Protege generates a name for the null value. However, this is wrong
		// behaviour for the standalone version.
		if (nameIndices.size() == 0) {
			return null;
		}
		String result = (String) row.get(((Integer) nameIndices.get(0)).intValue());
		if (nameIndices.size() > 0) {
			for (int i = 1; i < nameIndices.size(); i++) {
				result += "_" + (String) row.get(((Integer) nameIndices.get(i)).intValue()); //$NON-NLS-1$
			}
		}
		return result;
	}

}