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
package de.dfki.mycbr.retrieval.ui;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.util.ArrayList;
import java.util.Collection;
import java.util.EventObject;
import java.util.Vector;

import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.event.CellEditorListener;
import javax.swing.event.ChangeEvent;
import javax.swing.table.TableCellEditor;

import de.dfki.mycbr.model.vocabulary.SpecialValueHandler;

public class MultipleCellEditor implements TableCellEditor, CellEditorListener
{
	//
	// editing modes
	//
	private static final int EDITING_MODE_NOTHING 		= 0;
	private static final int EDITING_MODE_ADD 			= 1;
	private static final int EDITING_MODE_REMOVE 		= 2;
	
	// 
	// GUI elements
	//
	private JPanel paWrapper 	= new JPanel(new BorderLayout());
	private JPanel paInit 		= new JPanel(new GridBagLayout());
	private JLabel label 		= new JLabel();
	private JButton buAdd 		= new JButton("+");
	private JButton buRemove 	= new JButton("-");
	private Component editorComponent;
	private JPanel paRemove 	= new JPanel(new GridBagLayout());
	private JComboBox cbRemove	= new JComboBox();
	private JButton buDoAdd		= new JButton("+");
	
	private JPanel paAdd	 	= new JPanel(new GridBagLayout());
	private JButton buDoRemove	= new JButton("-");

	// internal editor (ADD)
	private DefaultCellEditor cellEditor;
	
	// contact to the whole model
	private RetrievalWidget retrievalWidget;
	
	// to fix too-fast-selection problem
	private boolean buttonsActive = false;
	private MouseListener stayCoolMouseListener = new MouseAdapter()
	{
		public void mouseReleased(MouseEvent e)
		{
			buttonsActive = true;
		}
	};

	//
	// configuration
	//
	private boolean isMultiple = false;
	@SuppressWarnings("unchecked")
	private ArrayList listeners = new ArrayList();
	private int editingMode = EDITING_MODE_NOTHING;
	@SuppressWarnings("unchecked")
	private Collection values;

	private SpecialValueHandler specialValueHandler;

	
	public MultipleCellEditor(RetrievalWidget retrievalWidget, DefaultCellEditor _cellEditor)
	{
		super();
		this.cellEditor = _cellEditor;
		this.retrievalWidget = retrievalWidget;
		this.specialValueHandler =SpecialValueHandler.getInstance();
		
		//
		// init GUI
		//
		paInit.add(label, 			new GridBagConstraints(0,0, 1,1, 1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		paInit.add(buAdd, 			new GridBagConstraints(2,0, 1,1, 0.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		paInit.add(buRemove, 		new GridBagConstraints(1,0, 1,1, 0.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
	
		paRemove.add(cbRemove, 		new GridBagConstraints(0,0, 1,1, 1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		paRemove.add(buDoRemove, 	new GridBagConstraints(1,0, 1,1, 0.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		
		paAdd.add(buDoAdd, 			new GridBagConstraints(1,0, 1,1, 0.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
	
		//
		// add button & combobox behavior
		//
		buAdd.addMouseListener(stayCoolMouseListener);
		buRemove.addMouseListener(stayCoolMouseListener);
		
		buAdd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				if (!buttonsActive) return;

				//log.info("ADD VALUE");
				
				paWrapper.removeAll();
				editorComponent.setVisible(true);
				paAdd.add(editorComponent, new GridBagConstraints(0,0, 1,1, 1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
				paWrapper.add(paAdd, BorderLayout.CENTER);
				paWrapper.revalidate();
				paWrapper.repaint();
				
				// change editing mode
				setMode(EDITING_MODE_ADD);

				if (editorComponent instanceof JComboBox)
				{
					JComboBox cb = (JComboBox) editorComponent;
					cb.setLocation(paAdd.getLocation());
					cb.setSize(paWrapper.getWidth(), paWrapper.getHeight());
					cb.setPopupVisible(true);
				}
			}
		});
		buDoAdd.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				cellEditor.stopCellEditing();
			}
		});
		
		buRemove.addActionListener(new ActionListener()
		{
			@SuppressWarnings("unchecked")
			public void actionPerformed(ActionEvent e)
			{
				if (!buttonsActive || values.size()==0) return;

				//log.info("REMOVE VALUE");
				cbRemove.setModel(new DefaultComboBoxModel(new Vector(values)));
				
				paWrapper.removeAll();
				paWrapper.add(paRemove, BorderLayout.CENTER);
				paWrapper.revalidate();
				paWrapper.repaint();
				
				// change editing mode
				setMode(EDITING_MODE_REMOVE);

				cbRemove.setLocation(paRemove.getLocation());
				cbRemove.setSize(paWrapper.getWidth(), paWrapper.getHeight());
				cbRemove.setPopupVisible(true);
			}
		});
		
		cbRemove.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				doRemove();
			}
		});
		buDoRemove.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				doRemove();
			}
		});
	}
	
	protected void doRemove()
	{
		//
		// REMOVE behavior
		//
		//log.info("REMOVE VALUE");
		Object o = cbRemove.getSelectedItem();
		values.remove(o);
		stopCellEditing();
	}

	protected void setMode(int newMode)
	{
		//log.info("CHANGE EDITING MODE ["+newMode+"]");
		editingMode = newMode;
	}

	
	private void init(int row)
	{
		if (editorComponent!=null) editorComponent.setVisible(false);
		// pre-configuration
		setMode(EDITING_MODE_NOTHING);
		isMultiple = retrievalWidget.isMultiple(row);
		buttonsActive = false;
		paWrapper.removeAll();
		paWrapper.add(paInit, BorderLayout.CENTER);
	}

	private void fireStopEvent()
	{
		ChangeEvent event = new ChangeEvent(this);
		for (int i=0; i<listeners.size(); i++)
		{
			CellEditorListener cel = (CellEditorListener) listeners.get(i);
			cel.editingStopped(event);
		}
	}

	private void fireCancelEvent()
	{
		ChangeEvent event = new ChangeEvent(this);
		for (int i=0; i<listeners.size(); i++)
		{
			CellEditorListener cel = (CellEditorListener) listeners.get(i);
			cel.editingCanceled(event);
		}
	}

	
	// interface TableCellEditor

	@SuppressWarnings("unchecked")
	public Component getTableCellEditorComponent(JTable table, Object value, boolean isSelected, int row, int column)
	{
		//log.info("INIT (once per edit)");
		
		init(row);
		editorComponent = cellEditor.getTableCellEditorComponent(table, value, isSelected, row, column);
		
		if (!isMultiple)
		{
			// non-multiple behavior
			paWrapper.removeAll();
			paWrapper.setVisible(false);
			editorComponent.setVisible(true);
			return editorComponent;
		}

		//
		// prepare for multiple attributes
		//
		paWrapper.setVisible(true);
		cellEditor.addCellEditorListener(this);
		label.setText(""+value);
		this.values = new ArrayList();
//		if (value!=null && !Helper.isSpecialValue(value)) values.addAll((Collection) value);
		if (value!=null && !specialValueHandler.isSpecialValue(value)) values.addAll((Collection) value);
		return paWrapper;
	}

	
	
	@SuppressWarnings("unchecked")
	public void addCellEditorListener(CellEditorListener cel)
	{
		if (!isMultiple)
		{
			cellEditor.addCellEditorListener(cel);
			return;
		}
		listeners.add(cel);
	}

	public void removeCellEditorListener(CellEditorListener cel)
	{
		if (!isMultiple)
		{
			cellEditor.removeCellEditorListener(cel);
			return;
		}
		listeners.remove(cel);
	}

	public Object getCellEditorValue()
	{
		if (!isMultiple)
		{
			// non-multiple behavior
			return cellEditor.getCellEditorValue();
		}
		
		//
		// prepare multiple values
		//
		if (values.size()==0) return SpecialValueHandler.SPECIAL_VALUE_UNDEFINED;
		if (values.size() == 1)
		{
			Object value = values.iterator().next();
//			if (Helper.isSpecialValue(value)) return value;
			if (specialValueHandler.isSpecialValue(value)) return value;
		}
		return values;
	}

	public boolean isCellEditable(EventObject arg0)
	{
		return cellEditor.isCellEditable(arg0);
	}

	public boolean shouldSelectCell(EventObject arg0)
	{
		return cellEditor.shouldSelectCell(arg0);
	}

	public void cancelCellEditing()
	{
		//log.info("CANCEL EDIT");
		if (editingMode == EDITING_MODE_ADD)
		{
			cellEditor.removeCellEditorListener(this);
			cellEditor.cancelCellEditing();
			return;
		}
		if (cellEditor!=null) cellEditor.cancelCellEditing();
	}

	public boolean stopCellEditing()
	{
		//log.info("STOP EDIT");
		if (editingMode == EDITING_MODE_ADD)
		{
			cellEditor.removeCellEditorListener(this);
			return cellEditor.stopCellEditing();
		}
		if (cellEditor!=null) cellEditor.stopCellEditing();
		return true;
	}
	

	// end interface
	
	
	// interface CellEditorListener

	public void editingCanceled(ChangeEvent e)
	{
		//
		// ADD behavior
		// NOTE:
		// this method will be called by internalCellEditor - but never from cbRemove
		//
		//log.info("EDITING CANCELED");
		cellEditor.removeCellEditorListener(this);
		fireCancelEvent();
	}

	@SuppressWarnings("unchecked")
	public void editingStopped(ChangeEvent e)
	{
		//
		// ADD behavior
		// NOTE:
		// this method will be called by internalCellEditor - but never from cbRemove
		//
		//log.info("EDITING STOPPED");
		if (editingMode == EDITING_MODE_ADD)
		{
			Object value = cellEditor.getCellEditorValue();
			if (value!=null && !values.contains(value))
			{
				if (values.size()==0 || !specialValueHandler.isSpecialValue(value)) values.add(value);
			}
		}
		
		cellEditor.removeCellEditorListener(this);
		fireStopEvent();
	}

	// end interface

}
