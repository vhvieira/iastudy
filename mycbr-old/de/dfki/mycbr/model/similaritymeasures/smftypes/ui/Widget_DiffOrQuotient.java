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

import de.dfki.mycbr.model.similaritymeasures.smftypes.Abstract_SMF_Number;

/**
 * @author myCBR Team
 *
 */
public class Widget_DiffOrQuotient extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(Widget_DiffOrQuotient.class.getName());
	
	public interface DiffOrQuotientModeListener {
		/**
		 * Will be called when the user presses either the button 'asymmetric' or 'symmetric'
		 * @param mode boolean false = asymmetric, true = symmetric.
		 * @return boolean true if listener could apply this mode.
		 */
		public boolean setDiffOrQuotientMode(int mode);
	}
	
	
	JLabel laMode = new JLabel();

	ButtonGroup buttongroup = new ButtonGroup();

	JRadioButton rbDifference = new JRadioButton();

	JRadioButton rbQuotient = new JRadioButton();

	GridBagLayout gridBagLayout1 = new GridBagLayout();

	HelpButton buHelp = new HelpButton(HelpManager.KEY_SYMMETRY);
	
	HashSet<DiffOrQuotientModeListener> listeners = new HashSet<DiffOrQuotientModeListener>();

	public Widget_DiffOrQuotient() {
		try {
			jbInit();
			customInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void customInit() {
		rbQuotient.addActionListener(this);
		rbDifference.addActionListener(this);
	}

	void jbInit() throws Exception {
		laMode.setRequestFocusEnabled(true);
		laMode.setText(Messages.getString("DistanceFunction")); //$NON-NLS-1$
		this.setBorder(null);
		this.setMinimumSize(new Dimension(156, 37));
//		this.setPreferredSize(new Dimension(156, 37));
		this.setLayout(gridBagLayout1);
		rbDifference.setSelected(true);
		rbDifference.setText(Messages.getString("Difference")); //$NON-NLS-1$
		rbQuotient.setText(Messages.getString("Quotient")); //$NON-NLS-1$
		this.add(rbQuotient, new GridBagConstraints(2, 0, 1, 2, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 0), 0, 0));
		this.add(laMode, new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 15, 5, 15), 0, 0));
		this.add(rbDifference, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.NONE, new Insets(5, 5, 5, 0), 0, 0));
		this.add(buHelp, new GridBagConstraints(3, 0, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 2), 0, 0));
		buttongroup.add(rbQuotient);
		buttongroup.add(rbDifference);
	}

	public void setDiffOrQuotientMode(int mode) {
		log.fine("set quotient mode to [" + mode + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		if (mode == Abstract_SMF_Number.MODE_DIFFERENCE) {
			rbQuotient.setSelected(false);
			rbDifference.setSelected(true);
		} else {
			rbQuotient.setSelected(true);
			rbDifference.setSelected(false);
		}
	}

	public void actionPerformed(ActionEvent e) {
		int mode = Abstract_SMF_Number.MODE_DIFFERENCE;
		if (e.getSource() == rbQuotient) {
			mode = Abstract_SMF_Number.MODE_QUOTIENT;
		}

		boolean appliedAll = true;
		for (Iterator<DiffOrQuotientModeListener> it = listeners.iterator(); it.hasNext();) {
			appliedAll &= it.next().setDiffOrQuotientMode(mode);
		}
		
		if (!appliedAll) {
			log.fine("Could not apply symmetry mode (sym=[" + mode + "]) to all listeners.. undo."); //$NON-NLS-1$ //$NON-NLS-2$
			mode = (mode+1) %2;
			setDiffOrQuotientMode(mode);

			appliedAll=true;
			for (Iterator<DiffOrQuotientModeListener> it = listeners.iterator(); it.hasNext();) {
				appliedAll &= it.next().setDiffOrQuotientMode(mode);
			}
			log.fine("Undo was possible : [" + appliedAll + "]."); //$NON-NLS-1$ //$NON-NLS-2$
		}
	}
	
	public void addDiffOrQuotientModeListener(DiffOrQuotientModeListener listener) {
		listeners.add(listener);
	}
	
	/**
	 * Overrides setEnabled method of JComponent. If the minimum value of a number slot is smaller or equal zero,
	 * the quotient mode will not be useful, so the difference mode will be selected and the radio buttons will be disabled.
	 * 
	 * @param enabled the boolean value to set the mode for both radio buttons
	 * @see javax.swing.JComponent#setEnabled(boolean)
	 */
	public void setEnabled(boolean enabled) {
		rbDifference.setEnabled(enabled);
		rbQuotient.setEnabled(enabled);	
	}
}
