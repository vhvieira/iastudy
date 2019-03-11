/*
 * myCBR License 1.1
 *
 * Copyright (c) 2008
 * Thomas Roth-Berghofer, Armin Stahl & Deutsches Forschungszentrum f&uuml;r K&uuml;nstliche Intelligenz DFKI GmbH
 * Further contributors: myCBR Team (see http://mycbr-project.net/contact.html for further information 
 * about the myCBR Team). 
 * All rights reserved.
 *
 * myCBR is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Since myCBR uses some modules, you should be aware of their licenses for
 * which you should have received a copy along with this program, too.
 * 
 * endOfLic */
package de.dfki.mycbr.modelprovider.protege.explanation;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Arrays;
import java.util.Vector;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.ScrollPaneConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.explanation.ConceptExplanationProvider;
import de.dfki.mycbr.explanation.ConceptExplanationScheme;
import de.dfki.mycbr.explanation.ExplainableConcept;
import de.dfki.mycbr.explanation.KnowledgeSource;
import de.dfki.mycbr.modelprovider.protege.ModelClsProtege;
import de.dfki.mycbr.modelprovider.protege.ModelSlotProtege;
import edu.stanford.smi.protege.util.ComponentFactory;

public class ConceptExplanationSchemePanel extends JPanel implements ActionListener, ListSelectionListener, KeyListener {
	
	private static final long serialVersionUID = 1L;

	private static final Vector<Object> TABLE_HEADER = new Vector<Object>(Arrays.asList(new String[]{Messages.getString("Name"), Messages.getString("Url_pattern")}));  //$NON-NLS-1$ //$NON-NLS-2$
	
	private JTextArea txtDescription;
	private JTextArea txtDoc;
	private JTable taSources;
	private JLabel laDescription;
	private JLabel laDoc;
	private JLabel laSeeAlso;
	private JButton buNew;
	private JButton buDelete;
	private ConceptExplanationProvider conceptExpProvider;
	private ConceptExplanationScheme currentScheme;
	private DefaultMutableTreeNode currentNode;
	private JCheckBox cbInherit;

	public ConceptExplanationSchemePanel(ConceptExplanationProvider conceptExpProvider) {
		this.conceptExpProvider = conceptExpProvider;
		setLayout(new GridBagLayout());
		add(createHeaderPanel(),	new GridBagConstraints(0,0, 2,1, 1.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		
		JPanel paInheritance = new JPanel(new GridBagLayout());
		cbInherit = new JCheckBox();
		paInheritance.add(cbInherit, new GridBagConstraints(0,0, 1,1, 1.0,0.0, GridBagConstraints.WEST, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
		add(paInheritance,	new GridBagConstraints(0,1, 2,1, 1.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		
		
		JPanel paScheme = new JPanel();
		paScheme.setLayout(new GridBagLayout());

		laDescription = 	new JLabel(Messages.getString("Short_description")); //$NON-NLS-1$
		txtDescription = 	new JTextArea();
		txtDescription.setLineWrap(true);
		txtDescription.setWrapStyleWord(true);
		paScheme.add(laDescription,		new GridBagConstraints(0,2, 1,1, 0.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
		paScheme.add(new JScrollPane(txtDescription, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER),	new GridBagConstraints(0,3, 1,1, 1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
		
		laDoc = 	new JLabel("Documentation (read-only):"); //$NON-NLS-1$
		txtDoc = 	new JTextArea();
		txtDoc.setLineWrap(true);
		txtDoc.setWrapStyleWord(true);
		txtDoc.setEditable(false);
		paScheme.add(laDoc,		new GridBagConstraints(0,4, 1,1, 0.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
		paScheme.add(new JScrollPane(txtDoc, ScrollPaneConstants.VERTICAL_SCROLLBAR_AS_NEEDED, ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER),	new GridBagConstraints(0,5, 1,1, 1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
		
		laSeeAlso = 		new JLabel(Messages.getString("further_sources")); //$NON-NLS-1$
		taSources = 		new JTable();
		taSources.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		paScheme.add(laSeeAlso,		new GridBagConstraints(0,6, 1,1, 0.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
		paScheme.add(new JScrollPane(taSources),		new GridBagConstraints(0,7, 1,1, 1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));

		JPanel paButtons = new JPanel(new GridBagLayout());
		buNew 		= new JButton(Messages.getString("New")); //$NON-NLS-1$
		buDelete 	= new JButton(Messages.getString("Delete")); //$NON-NLS-1$
		paButtons.add(buNew, 	new GridBagConstraints(0,0, 1,1, 1.0,0.0, GridBagConstraints.EAST,   GridBagConstraints.NONE, new Insets(5,5,5,5), 0,0));
		paButtons.add(buDelete,	new GridBagConstraints(1,0, 1,1, 0.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,0,5,5), 0,0));
		paScheme.add(paButtons,	new GridBagConstraints(0,10, 1,1, 1.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));

		buNew.addActionListener(this);
		buDelete.addActionListener(this);
		taSources.getSelectionModel().addListSelectionListener(this);
		cbInherit.addActionListener(this);
		
		txtDescription.addKeyListener(this);
		add(paScheme,	new GridBagConstraints(0,2, 1,1, 1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		
	}

	public void setSelectedNode(DefaultMutableTreeNode node) {
		currentNode = node;
		currentScheme = conceptExpProvider.getDirectExplanationScheme(node);

		cbInherit.setVisible(false);
		if (currentScheme == null) {
			setEditable(false);
			cbInherit.setSelected(false);
			if (node == null)  {
				cbInherit.setText(Messages.getString("Override_explanation_scheme")); //$NON-NLS-1$
			} else {
				ConceptExplanationScheme superScheme = conceptExpProvider.getResponsibleExplanationScheme(node);
				cbInherit.setText(String.format(Messages.getString("Override_explanation_scheme_currently_using..."), superScheme.getExplainableConcept().getUserObject())); //$NON-NLS-1$
			}
			txtDescription.setText(""); //$NON-NLS-1$
		} else {
			setEditable(true);
			cbInherit.setSelected(true);
			cbInherit.setText(Messages.getString("Override_explanation_scheme")); //$NON-NLS-1$
			txtDescription.setText(currentScheme.getDescription());
			txtDoc.setText("");
			if (node.getUserObject() instanceof ExplainableConcept) {
				Object obj = ((ExplainableConcept)node.getUserObject()).getUserObject();
				if (obj instanceof ModelClsProtege) {
					ModelClsProtege mcp = (ModelClsProtege)obj;
					txtDoc.setText(mcp.getProtegeInstance().getDocumentation().toString());
				} else if (obj instanceof ModelSlotProtege) {
					ModelSlotProtege msp = (ModelSlotProtege)obj;
					txtDoc.setText(msp.getProtegeInstance().getDocumentation().toString());	
				}	
			}
		}

		updateList();
		updateButtons();
	}

	private KnowledgeSource getSelectedKnowledgeSource() {
		int rowIndex = taSources.getSelectedRow();
		if (rowIndex < 0) {
			return null;
		}
		KnowledgeSource ks = (KnowledgeSource) taSources.getValueAt(rowIndex, 0);
		return ks;
	}

	private void setEditable(boolean editable) {
		editable = true;
		txtDescription.setEnabled(editable);
		laDescription.setEnabled(editable);
		laSeeAlso.setEnabled(editable);
		taSources.setEnabled(editable);
		buDelete.setEnabled(editable);
		buNew.setEnabled(editable);
	}

	private JPanel createHeaderPanel() {
		String editorLabel = Messages.getString("Explanation_scheme_editor"); //$NON-NLS-1$
		JPanel paHeader = new JPanel(new BorderLayout());
		JPanel titlePanel = new JPanel(new BorderLayout());
		JLabel titleLabel = ComponentFactory.createTitleFontLabel(editorLabel.toUpperCase());
		titleLabel.setForeground(Color.white);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 2));
		titlePanel.add(titleLabel);
		paHeader.add(titlePanel, BorderLayout.NORTH);
		titlePanel.setBackground(Helper.COLOR_RED_MYCBR);
		return paHeader;
	}

	public void actionPerformed(ActionEvent e) {
		Object button = e.getSource();
		if (currentScheme==null && button!=cbInherit) {
			return;
		}
		if (button == buNew) {
			KnowledgeSource source = new KnowledgeSource();
			source.setTitle(Messages.getString("New_knowledge_source")); //$NON-NLS-1$
			
			currentScheme.getSources().add(source);
			updateList();
			taSources.changeSelection(currentScheme.getSources().indexOf(source), 0, false, false);
		} else if (button == buDelete) {
			Object selectedSource = getSelectedKnowledgeSource();
			if (selectedSource == null) {
				return;
			}
			currentScheme.getSources().remove(selectedSource);
			updateList();
		} else if (button == cbInherit) {
			if (cbInherit.isSelected()) {
				ExplainableConcept concept = (ExplainableConcept) currentNode.getUserObject();
				ConceptExplanationScheme scheme = new ConceptExplanationScheme(concept);
				conceptExpProvider.setDirectExplanationScheme(currentNode, scheme);
			} else {
				conceptExpProvider.removeDirectExplanationScheme(currentNode);
			}
			setSelectedNode(currentNode);
			updateList();
		}
	}

	private void updateList() {
		int selectedRow = taSources.getSelectedRow();
		Object selectedSource = getSelectedKnowledgeSource();
		
		Vector<KnowledgeSource> vector = new Vector<KnowledgeSource>();
		if (currentScheme != null) {
			vector.addAll(currentScheme.getSources());
		}
		
		Vector<Vector<Object>> data = new Vector<Vector<Object>>();
		for (KnowledgeSource ks: vector) {
			Vector<Object> row = new Vector<Object>();
			row.add(ks);
			row.add(ks.getPattern());
			data.add(row);
		}
		taSources.setModel(new DefaultTableModel(data, TABLE_HEADER) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void setValueAt(Object value, int row, int column) {
				KnowledgeSource ks = (KnowledgeSource) getValueAt(row, 0);
				String valStr = (String) value;
				conceptExpProvider.getProject().setHasChanged();
				switch (column) {
					case 0: ks.setTitle(valStr); break;
					case 1: ks.setPattern(valStr); break;
				}
			}
			
			@Override
			public Object getValueAt(int row, int column) {
				if (column == 0) {
					return super.getValueAt(row, column);
				}
				KnowledgeSource ks = (KnowledgeSource) getValueAt(row, 0);
				switch (column) {
					case 0: return ks.getTitle();
					case 1: return ks.getPattern();
				}
				return null;
			}
		});
		
//		liSources.setListData(vector);
		
		selectedRow = vector.indexOf(selectedSource);
		taSources.changeSelection(selectedRow, 0, false, false);
	}

	public void valueChanged(ListSelectionEvent e) {
		updateButtons();
	}

	private void updateButtons() {
		KnowledgeSource selectedSource = getSelectedKnowledgeSource();
		buNew.setEnabled(currentScheme != null);
		buDelete.setEnabled(currentScheme!=null && selectedSource!=null);
	}

	public void repaintList() {
		taSources.repaint();
	}

	public void keyReleased(KeyEvent e) {
		Object txtField = e.getSource();
		if (txtField == txtDescription) {
			currentScheme.setDescription(txtDescription.getText());
			conceptExpProvider.getProject().setHasChanged();
		}
	}
	
	// do nothing
	public void keyPressed(KeyEvent arg0) {}
	public void keyTyped(KeyEvent arg0) {}

}
