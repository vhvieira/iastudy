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
package de.dfki.mycbr.modelprovider.protege;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.table.DefaultTableModel;

import org.jdom.Document;
import org.jdom.Element;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import de.dfki.mycbr.CBRProject;
import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ProjectOptions;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.SMFContainer;
import de.dfki.mycbr.model.similaritymeasures.SMFHolder;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SpecialValuesConfigurator;
import de.dfki.mycbr.modelprovider.protege.similaritymodeling.GeneratorDialog;
import de.dfki.mycbr.modelprovider.protege.similaritymodeling.ImportInstancesDialog;
import de.dfki.mycbr.modelprovider.protege.similaritymodeling.MyCbr_Similarities_Tab;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Project;

public class MyCBRMenu extends JMenu {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static final Logger log = Logger.getLogger(MyCBRMenu.class.getName());

	private static final String ABOUT_TEXT = String.format(Messages.getString("About_text"),CBRProject.myCBR_VERSION); //$NON-NLS-1$
	
	private static int openCounter = 0;

	private static MyCBRMenu instance;

	private static JMenuBar menubar;

	private JMenuItem miImportCSV;

	private JMenuItem miExportSMF;

	private JMenuItem miAbout;

	private JMenuItem miSpecialValues;
	private JMenuItem miOptions;
	private JMenuItem miGenerate;

	private Project protProject;
	private DefaultTableModel dtm;
	private ArrayList<SMFHolder> allSMFs;
	
	public Project getProtProject() {
		return protProject;
	}

	private MyCBRMenu(Project protProject) {
		super(Messages.getString("Mycbr")); //$NON-NLS-1$
		this.protProject = protProject;
		
		miImportCSV = new JMenuItem(Messages.getString("Import_instances_from_CSV")); //$NON-NLS-1$
		miExportSMF = new JMenuItem(Messages.getString("Export_similarity_measure")); //$NON-NLS-1$
		miAbout = new JMenuItem(Messages.getString("About_mycbr")); //$NON-NLS-1$

		miSpecialValues	= new JMenuItem(Messages.getString("Configure_special_values")); //$NON-NLS-1$
		miOptions 		= new JMenuItem(Messages.getString("Options...")); //$NON-NLS-1$
		miGenerate 		= new JMenuItem(Messages.getString("Generate_instances")); //$NON-NLS-1$


		miSpecialValues.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				JDialog d = Helper.createDialog((Window) getTopLevelAncestor(), Messages.getString("Configure_special_values"), true); //$NON-NLS-1$
				d.setSize(800, 500);
				JPanel paSpecialValues = new SpecialValuesConfigurator();
				d.getContentPane().add(paSpecialValues);
				Helper.centerWindow(d);
				d.setVisible(true);
			}
		});

		miOptions.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				ProjectOptions options = CBRProject.getInstance().getProjectOptions();
				
				options.showOptions((Frame)getTopLevelAncestor());
			}
		});
		
		
		miGenerate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				log.fine("pressed 'Generate instances'."); //$NON-NLS-1$

				Cls currentCls = MyCbr_Similarities_Tab.instance().getCurrentCls();
				if (currentCls == null) {
					log.fine("currentCls is null... return."); //$NON-NLS-1$
					JOptionPane.showMessageDialog(MyCbr_Similarities_Tab.instance(), Messages.getString("Specify_a_class_first")); //$NON-NLS-1$
					return;
				}

				GeneratorDialog gd = new GeneratorDialog((Frame)getTopLevelAncestor(), currentCls);
				Helper.centerWindow(gd);
				gd.setVisible(true);
			}
		});

		miImportCSV.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				log.fine("pressed 'Import instances from CSV'."); //$NON-NLS-1$
				ImportInstancesDialog iid = new ImportInstancesDialog((Frame) getTopLevelAncestor(), "", false, getProtProject()); //$NON-NLS-1$
				iid.setVisible(true);
			}
		});

		miExportSMF.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked") //$NON-NLS-1$
			public void actionPerformed(ActionEvent e) {
				try {
					log.fine("pressed 'Export SMFs'."); //$NON-NLS-1$

					//
					// prompt user to choose the smfs to export
					//
					allSMFs = new ArrayList<SMFHolder>(SMFContainer.getInstance().values());
					JDialog d = Helper.createDialog((Window) menubar.getTopLevelAncestor(), Messages.getString("Export_similarity_measures"), true); //$NON-NLS-1$
					d.setTitle(Messages.getString("Select_similarity_measure_for_export")); //$NON-NLS-1$
					JTable table = new JTable();

					Vector<String> header = new Vector<String>(2);
					header.add(Messages.getString("Export_smf")); //$NON-NLS-1$
					header.add(Messages.getString("Attribute")); //$NON-NLS-1$
					header.add(Messages.getString("Smf_name")); //$NON-NLS-1$
					Vector data = new Vector(allSMFs.size());
					for (Iterator it=allSMFs.iterator(); it.hasNext();) {
						SMFHolder h = (SMFHolder) it.next();
						AbstractSMFunction smf = h.getActiveSMF();
						if (smf == null) {
							it.remove();
						}
					}
					for (int i=0; i<allSMFs.size(); i++) {
						SMFHolder h = (SMFHolder) allSMFs.get(i);
						AbstractSMFunction smf = h.getActiveSMF();
						
						Vector<Object> row = new Vector<Object>(2);
						row.add(new Boolean(true));
						row.add(smf.getModelInstanceName());
						row.add(smf.getSmfName());
						data.add(row);
					}

					dtm = new DefaultTableModel(data, header) {
						/**
						 * 
						 */
						private static final long serialVersionUID = 1L;

						public Class getColumnClass(int col) {
							return (col == 0 ? Boolean.class : String.class);
						}
						
						public boolean isCellEditable(int row, int col) {
							return (col == 0);
						}
					};
					table.setModel(dtm);

					JButton buOK = new JButton(Messages.getString("Ok")); //$NON-NLS-1$
					JButton buCancel = new JButton(Messages.getString("Cancel")); //$NON-NLS-1$

					buOK.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {

							
							ArrayList smfsToExport = new ArrayList();
							for (int i=0; i<dtm.getRowCount(); i++) {
								if (((Boolean)dtm.getValueAt(i, 0)).booleanValue()) smfsToExport.add(allSMFs.get(i));
							}
							
							
							//
							// choose output directory
							//
							JFileChooser fc = new JFileChooser();
							fc.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
							//						fc.showOpenDialog(buExportSMF.getParent().getParent());
							fc.showSaveDialog(miExportSMF.getParent().getParent());

							File dir = fc.getSelectedFile();
							if (dir == null) {
								return;
							}

							XMLOutputter xmlOutputter = new XMLOutputter();
							xmlOutputter.setFormat(Format.getPrettyFormat());

							log.info("export smf to [" + dir.getAbsolutePath() + "]"); //$NON-NLS-1$ //$NON-NLS-2$

							for (Iterator it = smfsToExport.iterator(); it.hasNext();) {
								SMFHolder h = (SMFHolder) it.next();

								AbstractSMFunction smf = h.getActiveSMF();

								if (smf != null) {
									// serialize it
									File newFile = new File(dir, smf.getModelInstanceName() + ".XML"); //$NON-NLS-1$

									Document doc = new Document();

									Element smfElement = smf.initXMLElement();
									smf.toXML(smfElement);

									doc.setRootElement(smfElement);

									try {
										Writer out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(newFile), "UTF8")); //$NON-NLS-1$

										out.write(xmlOutputter.outputString(doc));
										out.close();

									} catch (Exception ex) {
										log.log(Level.SEVERE, "could not serialize [" + newFile.getAbsolutePath() + "]", ex); //$NON-NLS-1$ //$NON-NLS-2$
										ex.printStackTrace();
									}

								}
							}

							
							((Window)((JComponent)e.getSource()).getTopLevelAncestor()).dispose();
						}
					});
					buCancel.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							((Window) ((JComponent) e.getSource()).getTopLevelAncestor()).dispose();
						}
					});
					
					JPanel panel = new JPanel(new GridBagLayout());
					panel.add(new JScrollPane(table), new GridBagConstraints(0, 0, 2, 1, 1d, 1d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
					panel.add(buCancel, new GridBagConstraints(0, 1, 1, 1, 1d, 0d, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
					panel.add(buOK, new GridBagConstraints(1, 1, 1, 1, 0d, 0d, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));

					d.getContentPane().add(panel);
					d.setSize(500, 400);
					Helper.centerWindow(d);
					d.setVisible(true);
				} catch (Exception exc) {
					log.log(Level.SEVERE, "lha", exc); //$NON-NLS-1$
				}
			}
		});

		miAbout.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				log.fine("pressed 'About myCBR'."); //$NON-NLS-1$
				JDialog d = new JDialog();
				Dimension prefSize = new Dimension(250,250);
				JPanel p = new JPanel(new BorderLayout());
				JTextPane label = new JTextPane();
				label.addHyperlinkListener(new HyperlinkListener() {
					public void hyperlinkUpdate(HyperlinkEvent e) {
						if (e.getEventType()!= HyperlinkEvent.EventType.ACTIVATED) {
							return;
						}
						log.info("connect to " + e.getURL().toExternalForm()+" event type = "+e.getEventType()); //$NON-NLS-1$ //$NON-NLS-2$
						Runtime runtime = Runtime.getRuntime();
						try {
							runtime.exec("cmd /c start " + e.getURL().toExternalForm()); //$NON-NLS-1$
						} catch (Throwable t) {
							log.info("Sorry... could not start standard browser."); //$NON-NLS-1$
						}
					}
				});
				label.setContentType("text/html"); //$NON-NLS-1$
				label.setText(ABOUT_TEXT);
				label.setEditable(false);
				if (MyCbr_Similarities_Tab.LOGO_MYCBR_BIG != null) {
					JLabel laLogo = new JLabel(MyCbr_Similarities_Tab.LOGO_MYCBR_BIG);
					prefSize = new Dimension(laLogo.getPreferredSize().width, prefSize.height);
					p.add(laLogo, BorderLayout.NORTH);
				}
				p.add(label, BorderLayout.CENTER);
				JButton bu = new JButton(Messages.getString("Ok")); //$NON-NLS-1$
				bu.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent e) {
						((Window)((JComponent)e.getSource()).getTopLevelAncestor()).dispose();
					}
				});
				p.add(bu, BorderLayout.SOUTH);
				d.getContentPane().add(p);
				d.setTitle(Messages.getString("About_mycbr")); //$NON-NLS-1$
				d.setSize(prefSize);
				Helper.centerWindow(d);
				d.setVisible(true);
				
			}
		});

		add(miImportCSV);
		add(miExportSMF);

		addSeparator();
		add(miSpecialValues);
		add(miOptions);
		add(miGenerate);
		
		addSeparator();
		add(miAbout);
	}

	public static void addMenuTo(JMenuBar mainWindowMenuBar) {
		if (openCounter == 0) {
			mainWindowMenuBar.add(instance);
			MyCBRMenu.menubar = mainWindowMenuBar;
		}
		openCounter++;
		log.fine("MENU counter = " + openCounter); //$NON-NLS-1$
	}

	public static void removeMenuFrom(JMenuBar mainWindowMenuBar) {
		if (--openCounter <= 0) {
			if (mainWindowMenuBar!=null && instance!=null) {
				mainWindowMenuBar.remove(instance);
			}
			instance = null;
		}
		log.fine("MENU counter = " + openCounter); //$NON-NLS-1$
	}

	public static void createInstance(Project protProject) {
		if (instance == null) {
			instance = new MyCBRMenu(protProject);
		}
	}

}
