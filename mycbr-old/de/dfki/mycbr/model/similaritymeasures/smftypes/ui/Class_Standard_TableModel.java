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

import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.JOptionPane;
import javax.swing.table.AbstractTableModel;

import de.dfki.mycbr.CBRProject;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.SMFContainer;
import de.dfki.mycbr.model.similaritymeasures.SMFHolder;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Class_Standard;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Class_Standard.SlotAmalgamation;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.modelprovider.ModelProvider;

/**
 * @author myCBR Team
 * 
 */
public class Class_Standard_TableModel extends AbstractTableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(Class_Standard_TableModel.class.getName());

	SMF_Class_Standard smf = null;

	public Class_Standard_TableModel(SMF_Class_Standard smf) {
		this.smf=smf;
	}

	public int getColumnCount() {
		return 5;
	}

	public int getRowCount() {
		if (smf == null) {
			return 0;
		}
		return smf.getSlotList().size();
	}

	public Object getValueAt(int y, int x) {
		if (smf == null)
			return null;

		ArrayList<SlotAmalgamation> sl = smf.getSlotList();
		SlotAmalgamation sa = sl.get(y);

		switch (x) {
			case 0:
				return sa.getSlot();//.getName();
			case 1:
				return new Boolean(sa.isEnabled());
			case 2:
				return new Double(sa.getWeight());
			case 3:
				String locSMFname = smf.getLocalSMF(sa.getSlot());
				return locSMFname;
			case 4:
				return sa.getComment();
        }
        return null;
    }


	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public Class getColumnClass(int x) {
		if (x==1) return Boolean.class;
		if (x==2) return Double.class;
		if (x==3) return AbstractSMFunction.class;
		if (x==4) return String.class;
		return super.getColumnClass(x);
	}

	public void setValueAt(Object obj, int y, int x) {
		if (x!=3 &&  (smf == null || obj==null)) {
			return;
		}
		
		String val = (obj == null ? null : obj.toString());

		ArrayList<SlotAmalgamation> sl = smf.getSlotList();
		SlotAmalgamation sa = (SlotAmalgamation) sl.get(y);
//        if (sa.isInherit()) return;

		switch (x) {
			case 1: {
				boolean value = Boolean.valueOf(val).booleanValue();
				if (value == true) {
					// check if smfunction is available
					log.fine("check: smfunction for slot ["+sa.getSlot().getName()+"] available?"); //$NON-NLS-1$ //$NON-NLS-2$
					ModelSlot slot = (ModelSlot) ModelProvider.getInstance().getModelInstance(sa.getSlot().getName());
					SMFHolder holder = CBRProject.getInstance().getSpecialSMFHolderForOOComposition(slot);
					if (holder == null) {
						holder = SMFContainer.getInstance().getSMFHolderForModelInstance(slot);
					}
					if (holder == null || holder.getActiveSMF()==null) {
						JOptionPane.showMessageDialog(null, String.format(Messages.getString("No_similarity_measure_defined_for"), sa.getSlot().getName()), Messages.getString("Consistency_check"), //$NON-NLS-1$ //$NON-NLS-2$
								JOptionPane.INFORMATION_MESSAGE);
						break;
					}
				}
				sa.setEnabled(value);
				break;
			}
			case 2:
				sa.setWeight(Double.parseDouble(val));
				break;
			case 3:
				smf.setLocalSMF(sa.getSlot(), val);
				break;
			case 4:
				sa.setComment(val);
				break;
		}

	}

	public boolean isCellEditable(int y, int x) {
		ArrayList<SlotAmalgamation> sl = smf.getSlotList();
		SlotAmalgamation sa = (SlotAmalgamation) sl.get(y);
		if (sa.isInherit()) {
			return false;
		}
		return (x>0);
	}
}

/**
 * $Log$
 */