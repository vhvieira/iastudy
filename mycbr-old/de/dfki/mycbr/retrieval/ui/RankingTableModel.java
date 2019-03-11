/**
 MyCBR License 1.1

 Copyright (c) 2008
 Thomas Roth-Berghofer, Armin Stahl & Deutsches Forschungszentrum für Künstliche Intelligenz DFKI GmbH
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
package de.dfki.mycbr.retrieval.ui;

import javax.swing.JTree;
import javax.swing.event.TableModelListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.table.AbstractTableModel;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.TreePath;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.casebase.CaseInstance;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.model.vocabulary.SpecialValueHandler;
import de.dfki.mycbr.retrieval.DefaultQuery;

public class RankingTableModel extends AbstractTableModel implements TableModel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private DefaultQuery query;

	private CaseInstance[] caseInstances;
	private JTree tree;

	public RankingTableModel(DefaultMutableTreeNode root, DefaultQuery query,
			CaseInstance[] caseInstances, JTree tree) {
		this.tree = tree;
		this.query = query;
		this.caseInstances = caseInstances;

		tree.addTreeExpansionListener(new TreeExpansionListener() {

			// Don't use fireTableRowsInserted() here;
			// the selection model would get updated twice.
			public void treeExpanded(TreeExpansionEvent event) {
				fireTableDataChanged();
			}

			public void treeCollapsed(TreeExpansionEvent event) {
				fireTableDataChanged();
			}
		});

	}

	public int getColumnCount() {
		return caseInstances.length + 1;
	}

	public String getColumnName(int column) {
		if (column > caseInstances.length)
			return null;
		if (column == 0) {
			return "Query";
		} else {
			CaseInstance ci = caseInstances[column - 1];
			return (ci == null ? null : ci.getName());
		}
	}

	public Object getValueAt(Object _node, int column) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) _node;
		if (!(node.getUserObject() instanceof ModelSlot))
			return null;
		ModelSlot slot = (ModelSlot) node.getUserObject();

		if (column == 0) {
			// QUERY VALUE
			Object o = getCorrespondingObject(node, column);
			if (SpecialValueHandler.getInstance().isSpecialValue(o))
				return o;
			DefaultQuery tmpQuery = (DefaultQuery) o;
			Object value = (tmpQuery == null ? null : tmpQuery
					.getSlotValue(slot));
			if (value instanceof DefaultQuery)
				value = null;
			return value;
		} else if ((column-1 < caseInstances.length) && (column-1 >= 0)) {
			// CASE VALUE
			if (caseInstances[column - 1] == null)
				return null;
			Object o = getCorrespondingObject(node, column);
			if (SpecialValueHandler.getInstance().isSpecialValue(o))
				return o;
			CaseInstance tmpCi = (CaseInstance) o;
			Object value = (tmpCi == null ? null : tmpCi.getSlotValue(slot));
			return value;
		}
		return null;
	}

	private Object getCorrespondingObject(DefaultMutableTreeNode node,
			int column) {
		Object currentObject = null;
		if (column == 0) {
			// QUERY
			currentObject = Helper.getCorrespondingObject(node, query);
		} else {
			// CASE INSTANCE
			currentObject = Helper.getCorrespondingObject(node,
					caseInstances[column - 1]);
		}

		return currentObject;
	}

	public void setValueAt(Object value, Object _node, int column) {
		DefaultMutableTreeNode node = (DefaultMutableTreeNode) _node;
		ModelSlot slot = (ModelSlot) node.getUserObject();

		// find the right type for this value
		Object realValue = null;
		if (value != null) {
			ValueType vt = slot.getValueType();
			if (vt == ValueType.STRING || vt == ValueType.SYMBOL
					|| SpecialValueHandler.getInstance().isSpecialValue(value)
					|| !(value instanceof String)) {
				realValue = value;
			} else {
				realValue = slot.getValueType().newInstance(value.toString());
				if (realValue == null)
					return;
			}
		}

		if (column == 0) {
			// QUERY VALUE
			Object o = getCorrespondingObject(node, column);
			if (SpecialValueHandler.getInstance().isSpecialValue(o))
				return;
			DefaultQuery tmpQuery = (DefaultQuery) o;

			if (realValue instanceof ModelCls) {
				realValue = new DefaultQuery((ModelCls) realValue);
			}
			if (realValue instanceof CaseInstance) {
				realValue = new DefaultQuery((CaseInstance) realValue);
			}

			tmpQuery.setSlotValue(slot, realValue);

		}
	}

	public Object getChild(Object parent, int index) {
		return ((DefaultMutableTreeNode) parent).getChildAt(index);
	}

	public int getChildCount(Object parent) {
		return ((DefaultMutableTreeNode) parent).getChildCount();
	}

	public void setCaseInstances(CaseInstance[] caseInstances) {
		this.caseInstances = caseInstances;
	}

	public CaseInstance[] getCaseInstances() {
		return caseInstances;
	}

	@SuppressWarnings("unchecked")
	public Class getColumnClass(int column) {
		return Object.class;
	}

	/**
	 * By default, make the column with the Tree in it the only editable one.
	 * Making this column editable causes the JTable to forward mouse and
	 * keyboard events in the Tree column to the underlying JTree.
	 */
	public boolean isCellEditable(Object node, int column) {
		return column == 0;
	}

	public int getRowCount() {
		return tree.getRowCount();
	}

	public Object getValueAt(int rowIndex, int columnIndex) {

		return getValueAt(nodeForRow(rowIndex), columnIndex);

	}

	public void addTableModelListener(TableModelListener l) {

	}

	public boolean isCellEditable(int rowIndex, int columnIndex) {
		return isCellEditable(nodeForRow(rowIndex), columnIndex);
	}

	public void removeTableModelListener(TableModelListener l) {
		// TODO Auto-generated method stub
	}

	public void setValueAt(Object value, int rowIndex, int columnIndex) {
		setValueAt(value, nodeForRow(rowIndex), columnIndex);
	}

	private Object nodeForRow(int row) {
		TreePath treePath = tree.getPathForRow(row);
		return treePath.getLastPathComponent();
	}
}
