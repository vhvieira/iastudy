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
package de.dfki.mycbr.model.similaritymeasures.smftypes.ui;

import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
import java.util.Iterator;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;

/**
 * @author myCBR Team
 *
 */
public class Widget_Symmetry extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(Widget_Symmetry.class.getName());
	
	public interface SymmetryModeListener {
		/**
		 * Will be called when the user presses either the button 'asymmetric' or 'symmetric'
		 * @param symmetryMode boolean false = asymmetric, true = symmetric.
		 * @return boolean true if listener could apply this mode.
		 */
		public boolean setSymmetryMode(boolean symmetryMode);
	}
	
	
	JLabel laSymmetry = new JLabel();

	ButtonGroup buttongroup = new ButtonGroup();

	JRadioButton rbSymmetric = new JRadioButton();

	JRadioButton rbAsymmetric = new JRadioButton();

	HelpButton buHelp = new HelpButton(HelpManager.KEY_SYMMETRY);
	
	HashSet<SymmetryModeListener> listeners = new HashSet<SymmetryModeListener>();

	public Widget_Symmetry() {
		try {
			jbInit();
			customInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void customInit() {
		rbAsymmetric.addActionListener(this);
		rbSymmetric.addActionListener(this);
	}

	void jbInit() throws Exception {
		laSymmetry.setRequestFocusEnabled(true);
		laSymmetry.setText(Messages.getString("Symmetry")); //$NON-NLS-1$
		this.setBorder(null);
		this.setMinimumSize(new Dimension(156, 37));
		this.setLayout( new GridBagLayout() );
		rbSymmetric.setSelected(true);
		rbSymmetric.setText(Messages.getString("Symmetric")); //$NON-NLS-1$
		rbAsymmetric.setText(Messages.getString("Asymmetric")); //$NON-NLS-1$
		this.add(rbAsymmetric, new GridBagConstraints(2, 0, 1, 2, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 0), 0, 0));
		this.add(laSymmetry, new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		this.add(rbSymmetric, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 0), 0, 0));
		this.add(buHelp, new GridBagConstraints(3, 0, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 40, 0));
		buttongroup.add(rbAsymmetric);
		buttongroup.add(rbSymmetric);
	}

	public void setSymmetrySelection(boolean s) {
		log.fine("set symmetry to [" + s + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		if (s) {
			rbAsymmetric.setSelected(false);
			rbSymmetric.setSelected(true);
		} else {
			rbAsymmetric.setSelected(true);
			rbSymmetric.setSelected(false);
		}
	}

	public void actionPerformed(ActionEvent e) {
		boolean sym = true;
		if (e.getSource() == rbAsymmetric) {
			sym = false;
		}

		log.fine("set symmetry mode [" + sym + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		
		boolean appliedAll = true;
		for (Iterator<SymmetryModeListener> it = listeners.iterator(); it.hasNext();) {
			appliedAll &= (it.next()).setSymmetryMode(sym);
		}
		
		if (!appliedAll) {
			log.fine("Could not apply symmetry mode (sym=[" + sym + "]) to all listeners.. undo."); //$NON-NLS-1$ //$NON-NLS-2$
			setSymmetrySelection(!sym);

			appliedAll=true;
			for (Iterator<SymmetryModeListener> it = listeners.iterator(); it.hasNext();) {
				appliedAll &= (it.next()).setSymmetryMode(!sym);
			}
			log.fine("Undo was possible : [" + appliedAll + "]."); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	public void addSymmetryModeListener(SymmetryModeListener sml) {
		listeners.add(sml);
	}
}
