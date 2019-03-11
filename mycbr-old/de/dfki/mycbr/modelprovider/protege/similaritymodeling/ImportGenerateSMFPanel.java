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

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.util.Collection;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.JTextPane;

import de.dfki.mycbr.Helper;

//private static final Logger log = Logger.getLogger( ImportGenerateSMFPanel.class.getName());
public class ImportGenerateSMFPanel extends JPanel {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private JLabel laDescription = null;
	private JButton buMore = null;
	private JPanel paMore = null;
	private JButton buOK = null;
	private JButton buCancel = null;
	private JTextPane txtDescriptionRelAbs = null;
	private JRadioButton rbAbsolute = null;
	private JTextField txtAbsolute = null;
	private JRadioButton rbRelative = null;
	private JTextField txtRelative = null;
	
	private boolean flagHasBeenConfirmed = false;
	private ButtonGroup buttongroup= new ButtonGroup();
	private JLabel laSize = null;
	private JLabel laSizeTotal = null;
	private int totalAmount;
	private int threshold;
	
	/**
	 * This is the default constructor
	 */
	public ImportGenerateSMFPanel() {
		super();
		initialize();
		customInit();
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public ImportGenerateSMFPanel(Collection missingAttributes, int totalAmount) {
		this();
		this.totalAmount = totalAmount;
		laDescription.setText(String.format(Messages.getString("Attributes_are_unknows_generate_them"), missingAttributes)); //$NON-NLS-1$
		laSizeTotal.setText(String.format(Messages.getString("Total_amount_of_imports"), totalAmount)); //$NON-NLS-1$
		refreshThreshold();
//		revalidate();
	}

	private void refreshThreshold() {
		if (rbAbsolute.isSelected()) {
			threshold = Helper.parseInt(txtAbsolute.getText());
			if (threshold < 0) {
				threshold = 0;
			}
		} else {
			threshold = Helper.parseInt(txtRelative.getText());
			if (threshold < 0) {
				threshold = 0;
			}
			if (threshold > 100) {
				threshold=100;
			} else {
				threshold = (int)((((double)threshold)/100) * totalAmount);
			}
		}
		
		laSize.setText(Messages.getString("Threshold") + threshold); //$NON-NLS-1$
	}

	private void customInit() {
		buOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				flagHasBeenConfirmed = true;
				((Window)getTopLevelAncestor()).dispose();
			}
		});
		buCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				((Window) getTopLevelAncestor()).dispose();
			}
		});
		buMore.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				paMore.setVisible(!paMore.isVisible());
//				revalidate();
//				repaint();
			}
		});
		paMore.setVisible(false);
		
		rbAbsolute.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshThreshold();
				checkEnabled();
			}
		});
		rbRelative.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				refreshThreshold();
				checkEnabled();
			}
		});
		txtAbsolute.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				refreshThreshold();
			};
		});
		txtRelative.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				refreshThreshold();
			};
		});
		
		buttongroup.add(rbAbsolute);
		buttongroup.add(rbRelative);
		checkEnabled();
	}

	protected void checkEnabled() {
		txtAbsolute.setEnabled(rbAbsolute.isSelected());
		txtRelative.setEnabled(rbRelative.isSelected());
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
		gridBagConstraints4.gridx = 1;
		gridBagConstraints4.insets = new java.awt.Insets(5,5,5,5);
		gridBagConstraints4.gridy = 3;
		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
		gridBagConstraints3.gridx = 0;
		gridBagConstraints3.insets = new java.awt.Insets(5,5,5,0);
		gridBagConstraints3.anchor = java.awt.GridBagConstraints.EAST;
		gridBagConstraints3.weightx = 1.0D;
		gridBagConstraints3.gridy = 3;
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridx = 0;
		gridBagConstraints2.gridwidth = 2;
		gridBagConstraints2.weightx = 0.0D;
		gridBagConstraints2.weighty = 10.0D;
		gridBagConstraints2.fill = java.awt.GridBagConstraints.BOTH;
		gridBagConstraints2.gridy = 2;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 1;
		gridBagConstraints1.insets = new java.awt.Insets(5,5,5,5);
		gridBagConstraints1.weighty = 1.0D;
		gridBagConstraints1.anchor = java.awt.GridBagConstraints.NORTHEAST;
		gridBagConstraints1.gridy = 1;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.gridwidth = 2;
		gridBagConstraints.anchor = java.awt.GridBagConstraints.WEST;
		gridBagConstraints.insets = new java.awt.Insets(5,5,0,5);
		gridBagConstraints.weightx = 1.0D;
		gridBagConstraints.fill = java.awt.GridBagConstraints.HORIZONTAL;
		gridBagConstraints.gridy = 0;
		laDescription = new JLabel();
		laDescription.setText("JLabel"); //$NON-NLS-1$
		this.setLayout(new GridBagLayout());
		this.setSize(300, 314);
		this.add(laDescription, gridBagConstraints);
		this.add(getBuMore(), gridBagConstraints1);
		this.add(getPaMore(), gridBagConstraints2);
		this.add(getBuOK(), gridBagConstraints3);
		this.add(getBuCancel(), gridBagConstraints4);
	}

	/**
	 * This method initializes buMore	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBuMore() {
		if (buMore == null) {
			buMore = new JButton();
			buMore.setText(Messages.getString("Options")); //$NON-NLS-1$
		}
		return buMore;
	}

	/**
	 * This method initializes paMore	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPaMore() {
		if (paMore == null) {
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 1;
			gridBagConstraints11.insets = new java.awt.Insets(5,5,5,5);
			gridBagConstraints11.anchor = java.awt.GridBagConstraints.EAST;
			gridBagConstraints11.gridy = 4;
			laSizeTotal = new JLabel();
			laSizeTotal.setText("JLabel"); //$NON-NLS-1$
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 1;
			gridBagConstraints10.anchor = java.awt.GridBagConstraints.EAST;
			gridBagConstraints10.insets = new java.awt.Insets(5,5,0,5);
			gridBagConstraints10.gridy = 3;
			laSize = new JLabel();
			laSize.setText("JLabel"); //$NON-NLS-1$
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints9.gridy = 2;
			gridBagConstraints9.weightx = 1.0;
			gridBagConstraints9.insets = new java.awt.Insets(5,5,5,5);
			gridBagConstraints9.anchor = java.awt.GridBagConstraints.NORTH;
			gridBagConstraints9.gridx = 1;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.gridx = 0;
			gridBagConstraints8.insets = new java.awt.Insets(5,5,5,5);
			gridBagConstraints8.weighty = 1.0D;
			gridBagConstraints8.anchor = java.awt.GridBagConstraints.NORTH;
			gridBagConstraints8.gridy = 2;
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints7.gridy = 1;
			gridBagConstraints7.weightx = 1.0;
			gridBagConstraints7.insets = new java.awt.Insets(5,5,5,5);
			gridBagConstraints7.gridx = 1;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.gridx = 0;
			gridBagConstraints6.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints6.insets = new java.awt.Insets(5,5,5,5);
			gridBagConstraints6.gridy = 1;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints5.gridy = 0;
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.weighty = 0.0D;
			gridBagConstraints5.gridwidth = 2;
			gridBagConstraints5.gridx = 0;
			paMore = new JPanel();
			paMore.setLayout(new GridBagLayout());
			paMore.add(getTxtDescriptionRelAbs(), gridBagConstraints5);
			paMore.add(getRbAbsolute(), gridBagConstraints6);
			paMore.add(getTxtAbsolute(), gridBagConstraints7);
			paMore.add(getRbRelative(), gridBagConstraints8);
			paMore.add(getTxtRelative(), gridBagConstraints9);
			paMore.add(laSize, gridBagConstraints10);
			paMore.add(laSizeTotal, gridBagConstraints11);
		}
		return paMore;
	}

	/**
	 * This method initializes buOK	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBuOK() {
		if (buOK == null) {
			buOK = new JButton();
			buOK.setText(Messages.getString("Ok")); //$NON-NLS-1$
		}
		return buOK;
	}

	/**
	 * This method initializes buCancel	
	 * 	
	 * @return javax.swing.JButton	
	 */
	private JButton getBuCancel() {
		if (buCancel == null) {
			buCancel = new JButton();
			buCancel.setText(Messages.getString("Cancel")); //$NON-NLS-1$
		}
		return buCancel;
	}

	/**
	 * This method initializes txtDescriptionRelAbs	
	 * 	
	 * @return javax.swing.JTextPane	
	 */
	private JTextPane getTxtDescriptionRelAbs() {
		if (txtDescriptionRelAbs == null) {
			txtDescriptionRelAbs = new JTextPane();
			txtDescriptionRelAbs.setText(Messages.getString("Threshold_to_destinguish_between_symbol_and_string")); //$NON-NLS-1$
		}
		return txtDescriptionRelAbs;
	}

	/**
	 * This method initializes rbAbsolute	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbAbsolute() {
		if (rbAbsolute == null) {
			rbAbsolute = new JRadioButton();
			rbAbsolute.setText(Messages.getString("Absolute_threshold")); //$NON-NLS-1$
			rbAbsolute.setSelected(true);
		}
		return rbAbsolute;
	}

	/**
	 * This method initializes txtAbsolute	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getTxtAbsolute() {
		if (txtAbsolute == null) {
			txtAbsolute = new JTextField();
			txtAbsolute.setText("30"); //$NON-NLS-1$
		}
		return txtAbsolute;
	}

	/**
	 * This method initializes rbRelative	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbRelative() {
		if (rbRelative == null) {
			rbRelative = new JRadioButton();
			rbRelative.setText(Messages.getString("Relative_threshold")); //$NON-NLS-1$
		}
		return rbRelative;
	}

	/**
	 * This method initializes txtRelative	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getTxtRelative() {
		if (txtRelative == null) {
			txtRelative = new JTextField();
			txtRelative.setText("25"); //$NON-NLS-1$
		}
		return txtRelative;
	}

	public boolean hasBeenConfirmed() {
		return flagHasBeenConfirmed;
	}

	public int getThreshold() {
		return threshold;
	}

}  //  @jve:decl-index=0:visual-constraint="10,10"
