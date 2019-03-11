/*
 * MyCBR License 1.1
 *
 * Copyright (c) 2008
 * Thomas Roth-Berghofer, Armin Stahl & Deutsches Forschungszentrum f&uuml;r K&uuml;nstliche Intelligenz DFKI GmbH
 * Further contributors: myCBR Team (see http://mycbr-project.net/contact.html for further information 
 * about the myCBR Team). 
 * All rights reserved.
 *
 * MyCBR is free software; you can redistribute it and/or modify
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
 * Since MyCBR uses some modules, you should be aware of their licenses for
 * which you should have received a copy along with this program, too.
 *
 * endOfLic */
package de.dfki.mycbr.retrieval.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.Point;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.MouseMotionListener;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.ButtonModel;
import javax.swing.DefaultCellEditor;
import javax.swing.DefaultComboBoxModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.JToolBar;
import javax.swing.JToolTip;
import javax.swing.JTree;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.ToolTipManager;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.JTableHeader;
import javax.swing.table.TableCellEditor;
import javax.swing.table.TableCellRenderer;
import javax.swing.table.TableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import de.dfki.mycbr.*;
import de.dfki.mycbr.ProjectOptions.OptionsListener;
import de.dfki.mycbr.explanation.*;
import de.dfki.mycbr.model.casebase.CaseInstance;
import de.dfki.mycbr.model.similaritymeasures.AbstractClassSM;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.SMFContainer;
import de.dfki.mycbr.model.similaritymeasures.SimMap;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Class_Standard;
import de.dfki.mycbr.model.vocabulary.*;
import de.dfki.mycbr.modelprovider.protege.ModelClsProtege;
import de.dfki.mycbr.modelprovider.protege.ModelSlotProtege;
import de.dfki.mycbr.modelprovider.protege.similaritymodeling.MyCbr_Similarities_Tab;
import de.dfki.mycbr.retrieval.*;
/**
 * This class generates the widget for the retrieval results containing the tool
 * bar, the tree view for slots, the table for the query and the retrieved cases
 * and the panels for displaying the explanation's "see also" information.
 * 
 * @author myCBR Team
 * 
 */
public class RetrievalWidget extends JPanel implements RetrievalResultListener,
		OptionsListener {
	private final static Logger log = Logger.getLogger(RetrievalWidget.class
			.getName());
	private static final long serialVersionUID = 1L;

	// define some constants
	private static final int ROW_HEIGHT = 16;
	private static final int COLUMN_INDEX_QUERY = 0;

	// icons
	private static Icon ICON_TREE_CLOSED = null;
	private static Icon ICON_TREE_OPENED = null;
	private static Icon ICON_TREE_LEAF = null;

	public static Icon ICON_SORT_HORIZ = null;
	public static Icon ICON_SORT_VERT_CLEAR = null;
	public static Icon ICON_SORT_VERT_UP = null;
	public static Icon ICON_SORT_VERT_DOWN = null;
	public static Icon ICON_RESET = null;
	public static Icon ICON_START_RETRIEVAL = null;
	public static Icon ICON_CLEAR = null;
	public static Icon ICON_LOAD = null;
	public static Icon ICON_SAVE = null;
	public static Icon ICON_PREVIOUS = null;
	public static Icon ICON_NEXT = null;

	{
		try {
			ICON_TREE_CLOSED = new ImageIcon(RetrievalWidget.class
					.getResource("plus.png"));
			ICON_TREE_OPENED = new ImageIcon(RetrievalWidget.class
					.getResource("minus.png"));
			ICON_TREE_LEAF = new ImageIcon(RetrievalWidget.class
					.getResource("leaf.png"));

			ICON_SORT_HORIZ = new ImageIcon(RetrievalWidget.class
					.getResource("arrowHoriz.png"));
			ICON_SORT_VERT_CLEAR = new ImageIcon(RetrievalWidget.class
					.getResource("arrowsVert.png"));
			ICON_SORT_VERT_UP = new ImageIcon(RetrievalWidget.class
					.getResource("arrowsVertUp.png"));
			ICON_SORT_VERT_DOWN = new ImageIcon(RetrievalWidget.class
					.getResource("arrowsVertDown.png"));

			ICON_RESET = new ImageIcon(RetrievalWidget.class
					.getResource("reset.png"));
			ICON_START_RETRIEVAL = new ImageIcon(RetrievalWidget.class
					.getResource("startRetrieval.png"));
			ICON_CLEAR = new ImageIcon(RetrievalWidget.class
					.getResource("clear.png"));
			ICON_LOAD = new ImageIcon(RetrievalWidget.class
					.getResource("load.png"));
			ICON_SAVE = new ImageIcon(RetrievalWidget.class
					.getResource("save.png"));
			ICON_PREVIOUS = new ImageIcon(RetrievalWidget.class
					.getResource("previous.png"));
			ICON_NEXT = new ImageIcon(RetrievalWidget.class
					.getResource("next.png"));
		} catch (Throwable ex) {
			// ignore
		}
	}

	// define GUI elements
	private JToolBar toolBar;
	private JPanel paHeader;
	private JPanel paContent;
	private JPanel paBottom;
	private JButton buStartRetrieval;
	private JButton buSaveQueryAsCase;
	private JComboBox cbClass;
	private JButton buResetQuery;
	private JButton buQueryFromCase;
	private JButton buPrev;
	private JButton buNext;
	private JButton buReset;
	private JScrollPane scrollPane;
	private SlotTreeView tree;
	private JTable table;
	private JProgressBar progressBar;

	// define model
	private ModelCls currentCls; // @jve:decl-index=0:
	private DefaultQuery currentQuery;
	private DefaultMutableTreeNode currentRoot;

	private RankingTableModel tableModel;
	private TableCellEditor cellEditor;
	private TableCellRenderer cellRenderer;
	private RetrievalContainer retrievalContainer;

	// last retrieval results + its configuration
	private Vector<AssessedInstance> ranking;
	private SimMap lastModelToSMF;
	private RetrievalResults lastRetrievalResults;
	private int displayFirstIndex;
	private int CASES_TO_DISPLAY;
	private int[] ranksOfCases;
	private Integer[] currentBestRank;

	// tree rendering configuration & helpers
	private int preferredTreeCellWidth = 10;
	private int iconWidth = 20;
	private int selectedRank = -1;
	private boolean sortDescendingly = false;

	private boolean columnsSorted = false;
	private int currentIndexForColumnSorting = 0;

	private Comparator<ModelInstance> comparatorAlphanumerical;
	private int MAX_CASES_TO_DISPLAY;
	private ActionListener cbClassActionListener;
	private JTable taFilter;
	private JTable taQueryWeights;
	private HashSet<ModelInstance> expandedNodes = new HashSet<ModelInstance>();
	private JTextArea txtExpConcept;
	private JTextArea txtURLConcept;
	private JPopupMenu popupMenu;
	protected ConceptExplanationScheme currentConcExpScheme;
	protected Object concept;
	private JLabel laExpConceptDescription;
	private JSplitPane splitter;
	private int currentCol = -1;
	private static RetrievalWidget instance;
	private boolean initialized = false;
	
	/**
	 * This is the default constructor
	 */
	private RetrievalWidget() {
		super();
		ProjectOptions options = CBRProject.getInstance().getProjectOptions();
		MAX_CASES_TO_DISPLAY = options.getCasesToDisplay();
		options.addOptionsListener(this);
		initialize();
		customInit();
		taFilter.setVisible(options.isShowFilters());
		taQueryWeights
				.setVisible(options.getWeightMode() != Query.WEIGHT_MODE_CLASS_ONLY);
		initialized = true;
	}

	public static RetrievalWidget getInstance() {
		return instance;
	}
	
	public static RetrievalWidget initInstance() {
		instance = new RetrievalWidget();	
		return instance;
	}
	
	public static void resetInstance() {
		instance = null;
	}
	
	private void customInit() {
		ToolTipManager.sharedInstance().setDismissDelay(10000);

		// conceptual explanations
		MouseMotionAdapter mouseExpConcept = new MouseMotionAdapter() {

			@Override
			public void mouseMoved(MouseEvent e) {
				try {
					ExplanationManager expManager = ExplanationManager
							.getInstance();
					if (!expManager.isEnabled_conceptualExp())
						return;
					Point p = e.getPoint();
					Object source = e.getSource();
	
					if (source == table) {
						int row = table.rowAtPoint(p);
						int col = table.columnAtPoint(p);
						currentCol = col;
						concept = table.getValueAt(row, col);
					} else if (source == tree) {
						TreePath path = tree.getPathForLocation(p.x, p.y);
						if (path != null && path.getLastPathComponent() != null) {
							DefaultMutableTreeNode node = (DefaultMutableTreeNode) path
									.getLastPathComponent();
							concept = node.getUserObject();
						}
					}
					
					if (concept == null) {
						return;	
					}
					
					currentConcExpScheme = null;
					if (concept == null
							|| (currentConcExpScheme = ConceptExplanationProvider
									.getInstance().getResponsibleExplanationScheme(
											concept)) == null) {
						txtExpConcept.setText("");
						txtURLConcept.setText("");
						return;
					}
	
					laExpConceptDescription.setText("\"" + concept + "\" is:");
					txtExpConcept.setText(currentConcExpScheme.getDescription());
	
					if (currentConcExpScheme.getExplainableConcept()
							.getUserObject() instanceof ModelClsProtege) {
						ModelClsProtege mcp = (ModelClsProtege) currentConcExpScheme
								.getExplainableConcept().getUserObject();
						txtDoc.setText(mcp.getProtegeCls().getDocumentation()
								.toString());
					} else if (currentConcExpScheme.getExplainableConcept()
							.getUserObject() instanceof ModelSlotProtege) {
						ModelSlotProtege msp = (ModelSlotProtege) currentConcExpScheme
								.getExplainableConcept().getUserObject();
						txtDoc.setText(msp.getProtegeInstance().getDocumentation()
								.toString());
					} else {
						txtDoc.setText("");
					}
					txtURLConcept.setText(currentConcExpScheme
							.getResolvedKnowledgeSources(concept.toString()).toString());
				} catch (Exception exc) {
					System.err.println("exception in mouse motion listener of retireval widget");
					exc.printStackTrace();
				}
			}
		};

		table.addMouseMotionListener(mouseExpConcept);
		tree.addMouseMotionListener(mouseExpConcept);

		// set combo box model and pre-selected cls
		updateAvailableClasses();
		resetFilterTable();
		resetQueryWeightsTable();
		
		updatePageButtons();
	}

	public synchronized void updateAvailableClasses() {
		Collection<ModelCls> allModelCls = CBRProject.getInstance()
				.getAllModelCls();
		cbClass.setModel(new DefaultComboBoxModel(new Vector<ModelCls>(
				allModelCls)));

		if (currentCls == null || !allModelCls.contains(currentCls)) {
			// no currentCls set.
			// init current cls with top level cls, and create new query
			currentCls = Helper
					.findHighestModelClsByCompositionAndInheritance(allModelCls);
			if (currentCls != null)
				currentQuery = new DefaultQuery(currentCls, CBRProject
						.getInstance().getProjectOptions().getWeightMode());
		}

		if (currentCls != null) {
			// check for slot modifications
			DefaultMutableTreeNode newRoot = new DefaultMutableTreeNode(
					currentCls);
			buildupTreeNodes(newRoot, currentQuery);
			if (currentRoot == null || !compareTrees(currentRoot, newRoot)) {
				// reset table model
				setNewRoot(newRoot);
				findPreferredTreeCellWidth();
				resetRankingTableModel();
			}

		}
		setModelCls(currentCls);
	}

	public synchronized void updateAvailableSlots() {
		setModelCls(currentCls);
		revalidate();
		repaint();
	}

	@SuppressWarnings(value = { "unchecked" })
	private boolean compareTrees(DefaultMutableTreeNode parent1,
			DefaultMutableTreeNode parent2) {
		if (parent1.getUserObject() != parent2.getUserObject())
			return false;

		// collect user objects from parent1
		HashSet<Object> userObjects1 = new HashSet<Object>();
		for (Enumeration<DefaultMutableTreeNode> en = parent1.children(); en
				.hasMoreElements();) {
			Object uo1 = ((DefaultMutableTreeNode) en.nextElement())
					.getUserObject();
			userObjects1.add(uo1);
		}

		// collect user objects from parent2
		HashSet<Object> userObjects2 = new HashSet<Object>();
		for (Enumeration<DefaultMutableTreeNode> en = parent2.children(); en
				.hasMoreElements();) {
			Object uo2 = ((DefaultMutableTreeNode) en.nextElement())
					.getUserObject();
			userObjects2.add(uo2);
		}

		// compare both sets
		if (!userObjects1.equals(userObjects2))
			return false;

		// go into subtrees
		Enumeration<DefaultMutableTreeNode> en2 = parent2.children();
		for (Enumeration<DefaultMutableTreeNode> en = parent1.children(); en
				.hasMoreElements();) {
			DefaultMutableTreeNode child1 = (DefaultMutableTreeNode) en
					.nextElement();
			DefaultMutableTreeNode child2 = (DefaultMutableTreeNode) en2
					.nextElement();
			if (!compareTrees(child1, child2))
				return false;
		}

		return true;
	}

	public void setQuery(DefaultQuery query) {
		DefaultQuery old = currentQuery;
		currentQuery = query;
		// copy weights and filters
		if (old.getModelCls().equals(currentQuery.getModelCls())) {
			currentQuery.setWeights(old.getWeights());
			currentQuery.setFilters(old.getFilters());
		}
		clearSelections();
		clearLastRetrievalData();
		DefaultMutableTreeNode newRoot = new DefaultMutableTreeNode(currentCls);
		buildupTreeNodes(newRoot, currentQuery);
		setNewRoot(newRoot);
		findPreferredTreeCellWidth();
		resetRankingTableModel();
	}

	public void setModelCls(ModelCls cls) {

		clearLastRetrievalData();

		if (cls == null) {
			getPaContent().setVisible(false);
			return;
		}
		getPaContent().setVisible(true);

		// make consistent to combo box
		if (cbClass.getSelectedItem() != cls) {
			cbClass.removeActionListener(getCbClassActionListener());
			cbClass.setSelectedItem(cls);
			cbClass.addActionListener(getCbClassActionListener());
		}

		// set current cls here
		currentCls = cls;
		currentQuery = new DefaultQuery(cls, CBRProject.getInstance()
				.getProjectOptions().getWeightMode());
		DefaultMutableTreeNode newRoot = new DefaultMutableTreeNode(currentCls);
		buildupTreeNodes(newRoot, currentQuery);
		setNewRoot(newRoot);
		findPreferredTreeCellWidth();

		// reset table model
		resetRankingTableModel();
	}

	private void setNewRoot(DefaultMutableTreeNode newRoot) {
		currentRoot = newRoot;
		int tmpSelectedIndex = ((tree.getSelectionRows() == null || tree
				.getSelectionRows().length == 0) ? -1
				: tree.getSelectionRows()[0]);
		tree.setModel(new DefaultTreeModel(currentRoot));
		tree.setSelectionRow(tmpSelectedIndex);
		tree.setRootVisible(false);

		// expand all tree nodes that have been expanded before.
		for (int row = 0; row < tree.getRowCount(); row++) {
			TreePath path = tree.getPathForRow(row);
			ModelInstance mi = (ModelInstance) ((DefaultMutableTreeNode) path
					.getLastPathComponent()).getUserObject();
			if (expandedNodes.contains(mi)) {
				tree.expandPath(path);
			}
		}
	}
	
	private void resetRankingTableModel() {
		selectedRank = -1;

		tableModel = new RankingTableModel(currentRoot, currentQuery,
				new CaseInstance[MAX_CASES_TO_DISPLAY], tree);

		// table.setModel(new TreeTableModelAdapter(tableModel, tree));
		table.setModel(tableModel);
		table.setRowHeight(ROW_HEIGHT);
		tree.setRowHeight(ROW_HEIGHT);
	}

	private void resetFilterTable() {

		TableModel tmFilter = new TableModel() {

			public int getColumnCount() {
				return 1;
			}

			public String getColumnName(int column) {
				return "Filter";
			}

			public Object getValueAt(Object _node, int column) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) _node;
				if (!(node.getUserObject() instanceof ModelSlot))
					return null;
				ModelSlot slot = (ModelSlot) node.getUserObject();

				DefaultQuery q = (DefaultQuery) Helper.getCorrespondingObject(
						node, currentQuery);
				if (q == null)
					return null;
				return q.getFilter(slot);
			}

			public void setValueAt(Object value, Object _node, int column) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) _node;
				if (!(node.getUserObject() instanceof ModelSlot))
					return;
				ModelSlot slot = (ModelSlot) node.getUserObject();

				DefaultQuery q = (DefaultQuery) Helper.getCorrespondingObject(
						node, currentQuery);
				if (q == null)
					return;
				q.setFilter(slot, (Filter) value);
			}

			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public int getRowCount() {
				return tree.getRowCount();
			}

			public Object getValueAt(int rowIndex, int columnIndex) {
				if (isCellEditable(rowIndex, columnIndex)) {
					return  getValueAt(nodeForRow(rowIndex), columnIndex);
				} else {
					return null;
				}
			}

			private Object nodeForRow(int row) {
				TreePath treePath = tree.getPathForRow(row);
				return treePath.getLastPathComponent();
			}

			public Class<?> getColumnClass(int columnIndex) {
				return Object.class;
			}

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				AbstractSMFunction aSMF =  (SMF_Class_Standard)SMFContainer.getInstance().getSMFHolderForModelInstance(RetrievalWidget.getInstance().currentCls).getActiveSMF();
				ModelSlot ms = (ModelSlot)((DefaultMutableTreeNode)nodeForRow(rowIndex)).getUserObject();
				if (aSMF instanceof SMF_Class_Standard) {
					SMF_Class_Standard smfTMP = (SMF_Class_Standard)aSMF;
					if (smfTMP.getSlotAmalgamation(ms)!=null
							&& !smfTMP.getSlotAmalgamation(ms).isEnabled()) {
						return false;
					}
				}
				return true;
			}

			public void addTableModelListener(TableModelListener l) {
			}

			public void removeTableModelListener(TableModelListener l) {
			}

			public void setValueAt(Object value, int rowIndex, int columnIndex) {
				setValueAt(value, nodeForRow(rowIndex), columnIndex);
			}

		};

		taFilter.setModel(tmFilter);
	}

	private void resetQueryWeightsTable() {

		TableModel tmQW = new TableModel() {

			private static final long serialVersionUID = 1L;

			public int getColumnCount() {
				return 1;
			}

			public String getColumnName(int column) {
				return "Query Weights";
			}

			public Object getValueAt(Object _node, int column) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) _node;
				if (!(node.getUserObject() instanceof ModelSlot))
					return null;
				ModelSlot slot = (ModelSlot) node.getUserObject();

				DefaultQuery q = (DefaultQuery) Helper.getCorrespondingObject(
						node, currentQuery);
				if (q == null)
					return null;
				return q.getWeight(slot);
			}

			public void addTableModelListener(TableModelListener l) {
			}

			public Class<?> getColumnClass(int columnIndex) {
				return Object.class;
			}

			public boolean isCellEditable(int rowIndex, int columnIndex) {
				return taFilter.isCellEditable(rowIndex, columnIndex);
			}

			public void removeTableModelListener(TableModelListener l) {
			}

			public void setValueAt(Object value, int rowIndex, int columnIndex) {
				setValueAt(value, nodeForRow(rowIndex), columnIndex);
			}

			public void setValueAt(Object value, Object _node, int column) {
				double weight = Helper.parseDouble(value.toString());
				if (weight < 0)
					return;

				DefaultMutableTreeNode node = (DefaultMutableTreeNode) _node;
				if (!(node.getUserObject() instanceof ModelSlot))
					return;
				ModelSlot slot = (ModelSlot) node.getUserObject();

				DefaultQuery q = (DefaultQuery) Helper.getCorrespondingObject(
						node, currentQuery);
				if (q == null)
					return;
				q.setQueryWeight(slot, weight);
			}

			public int getRowCount() {
				return tree.getRowCount();
			}

			public Object getValueAt(int rowIndex, int columnIndex) {
				if (isCellEditable(rowIndex, columnIndex)) {
					return getValueAt(nodeForRow(rowIndex), columnIndex);
				} else {
					return null;
				}
			}

			private Object nodeForRow(int row) {
				TreePath treePath = tree.getPathForRow(row);
				return treePath.getLastPathComponent();
			}

		};

		taQueryWeights.setModel(tmQW);
	}

	private void buildupTreeNodes(DefaultMutableTreeNode parentNode,
			DefaultQuery query) {
		// sort by names
		TreeSet<ModelSlot> set = new TreeSet<ModelSlot>(
				getComparatorAlphanumerical());
		ModelCls cls = query.getModelCls();
		set.addAll(cls.listSlots());

		// now build up tree
		for (Iterator<ModelSlot> it = set.iterator(); it.hasNext();) {
			ModelSlot slot = it.next();

			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(slot);
			parentNode.add(childNode);

			if (slot.getValueType() == ValueType.INSTANCE) {
				Collection<Object> allowedValues = slot.getAllowedValues();
				Object qVal = query.getSlotValue(slot);
				if (allowedValues.contains(cls)
						|| SpecialValueHandler.getInstance().isSpecialValue(
								qVal))
					continue;
				if (allowedValues.size() >= 1)
					buildupTreeNodes(childNode, (DefaultQuery) qVal);
			}
		}

	}

	/**
	 * Sorts the current ranking table rows by the local similarities of
	 * the given explanation object
	 * @param parentNode
	 * @param query
	 * @param exp
	 */
	private void buildupTreeNodesSorted(DefaultMutableTreeNode parentNode,
			DefaultQuery query, Explanation exp) {
		TreeMap<String, DefaultMutableTreeNode> treeMap = new TreeMap<String, DefaultMutableTreeNode>();
		ModelCls cls = query.getModelCls();
		// find all involved slots
		for (Iterator<ModelSlot> it = cls.listSlots().iterator(); it.hasNext();) {
			ModelSlot slot = it.next();
			Explanation localExp = null;
			if (exp != null) {
				localExp = exp.getLocalExplanation(slot);
			}
			DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(slot);

			double sim = -1;
			String key = sim + slot.getName();
			if (localExp != null) {
				sim = localExp.getSimilarity();
				
				if (localExp.getQuery() == SpecialValueHandler.SPECIAL_VALUE_UNDEFINED) {
					key = "Z" + key;
				}
				String tmp = Double.toString(sim);
				if (tmp.length()<4){
					tmp.concat("0");
				}
				key = (tmp + 0.00) + slot.getName();
			}

			treeMap.put(key, childNode);

			if (slot.getValueType() == ValueType.INSTANCE) {
				Collection<Object> allowedValues = slot.getAllowedValues();
				if (allowedValues.size() >= 1 && !allowedValues.contains(cls)) {
					Object qVal = query.getSlotValue(slot);
					if (localExp != null)
						buildupTreeNodesSorted(childNode, (DefaultQuery) qVal,
								localExp);
					else
						buildupTreeNodes(childNode, (DefaultQuery) qVal);
				}
			}
		}

		// now put all children onto the parent in ascending order
		ArrayList<DefaultMutableTreeNode> nodes = new ArrayList<DefaultMutableTreeNode>(
				treeMap.values());
		if (!sortDescendingly) {
			Collections.reverse(nodes);
		}

		for (Iterator<DefaultMutableTreeNode> it = nodes.iterator(); it
				.hasNext();) {
			DefaultMutableTreeNode childNode = it.next();
			parentNode.add(childNode);
		}
	}

	protected void startRetrieval() {
		// get similarity measures
		SimMap activeSMFs = CBRProject.getInstance().getActiveSMFs(currentCls);
		if (activeSMFs == null || activeSMFs.get(currentCls) == null) {
			String title = "Global similarity measure not found";
			// try to find an SMF for a super Cls
			AbstractClassSM superClsSmf = Helper.findSuperClsSMF(activeSMFs,
					currentCls.getSuperCls());
			if (superClsSmf == null) {
				JOptionPane.showMessageDialog(this,
						"Sorry, there is no global similarity measure for class ["
								+ currentCls.getName() + "], yet.", title,
						JOptionPane.ERROR_MESSAGE);
				return;
			} else {
				int answer = JOptionPane.showConfirmDialog(this,
						"Warning, there is no global similarity measure for ["
								+ currentCls.getName()
								+ "], yet.\nBut there is one for ["
								+ superClsSmf.getModelInstance().getName()
								+ "].\n\nDo you want to use this instead?",
						title, JOptionPane.YES_NO_OPTION,
						JOptionPane.WARNING_MESSAGE);
				if (answer == JOptionPane.NO_OPTION)
					return;
			}

		}
		log.info("Start retrieval.");
		
		// prepare GUI elements
		buStartRetrieval.setEnabled(false);
		buPrev.setEnabled(false);
		buNext.setEnabled(false);
		table.clearSelection();
		tree.clearSelection();

		selectedRank = -1;
		progressBar.setValue(0);
		progressBar.setVisible(true);

		paContent.revalidate();
		paContent.paintImmediately(paContent.getBounds());

		// clear old data
		clearLastRetrievalData();

		// configure retrieval engine
		RetrievalEngine re = (retrievalContainer == null ? RetrievalEngine
				.getInstance()
				: retrievalContainer.getRetrievalEngine());

		re.prepareForRetrieval(currentCls);
		re.addRetrievalResultListener(this);

		// start retrieval, now
		ExplanationManager expManager = ExplanationManager.getInstance();

		boolean useExp = false;

		if (expManager != null) {
			useExp = expManager.isEnabled();
		}

		re.beginRetrieval(new DefaultQuery(currentQuery), activeSMFs, useExp);
		log.fine("Query has been sent..");
	}

	public void setDisplayFirstIndex(int displayFirstIndex) {
		if (displayFirstIndex < 0 || ranking == null
				|| ranking.size() <= displayFirstIndex) {
			log.fine("ignore setting of currentFirstIndex ["
					+ displayFirstIndex + "]");
			return;
		}
		this.displayFirstIndex = displayFirstIndex;

		CASES_TO_DISPLAY = Math.min(ranking.size() - displayFirstIndex,
				MAX_CASES_TO_DISPLAY);
		int[] bestRanks = new int[CASES_TO_DISPLAY];
		for (int i = 0; i < CASES_TO_DISPLAY; i++)
			bestRanks[i] = i + displayFirstIndex;
		setDisplayedRanks(bestRanks);

		updatePageButtons();
	}

	public void setDisplayFirstIndexSorted(int displayFirstIndex) {
		if (displayFirstIndex < 0 || ranking == null
				|| ranking.size() <= displayFirstIndex) {
			log.fine("ignore setting of currentFirstIndex ["
					+ displayFirstIndex + "]");
			return;
		}
		this.displayFirstIndex = displayFirstIndex;

		CASES_TO_DISPLAY = Math.min(currentBestRank.length - displayFirstIndex
				+ 1, MAX_CASES_TO_DISPLAY);

		int[] displayedRanks = new int[CASES_TO_DISPLAY];

		for (int i = 0; i < displayedRanks.length; i++) {
			displayedRanks[i] = this.currentBestRank[i + displayFirstIndex];
		}
		setDisplayedRanks(displayedRanks);

		updatePageButtons();

	}

	public void setDisplayedRanks(int[] ranksOfCases) {
		this.ranksOfCases = ranksOfCases;
		CaseInstance[] caseInstances = new CaseInstance[ranksOfCases.length];
		for (int i = 0; i < caseInstances.length; i++) {
			caseInstances[i] = ((AssessedInstance) ranking.get(ranksOfCases[i])).inst;
		}
		tableModel.setCaseInstances(caseInstances);
		// full table update
		table.tableChanged(new TableModelEvent(table.getModel(),
				TableModelEvent.HEADER_ROW));

		// if this is set, tell it about new selection
		if (retrievalContainer != null) {
			retrievalContainer.setSelectionIndices(ranksOfCases);
		}

	}

	public int[] getDisplayedRanks() {
		return ranksOfCases;
	}

	// sort functions
	protected void sortColumns(DefaultMutableTreeNode node) {
		columnsSorted = true;

		// similarity for this slot has been calculated?
		if (ranking == null
				|| ranking.size() == 0
				|| (SpecialValueHandler.getInstance()
						.isSpecialValue(
								((DefaultQuery) Helper.getCorrespondingObject(
										node,
										(DefaultQuery) lastRetrievalResults
												.getOriginalQuery()))
										.getSlotValue((ModelSlot) node
												.getUserObject())) || (lastModelToSMF != null && lastModelToSMF
						.get((ModelInstance) node.getUserObject()) == null))) {
			tree.clearSelection();
			return;
		}

		Vector<Integer> bestRanksSorted = new Vector<Integer>();
		HashMap<Integer, Double> bestSimsMap = new HashMap<Integer, Double>();

		// add rankings to map
		for (int i = 0; i < ranking.size(); i++) {
			AssessedInstance ai = (AssessedInstance) ranking.get(i);
			Explanation exp = getCorrespondingExplanation(node, ai);
			double sim = (exp == null ? 0 : exp.getSimilarity());

			int index = 0;
			bestSimsMap.put(i, sim);
			Vector<Integer> bestRanksSortedTmp = bestRanksSorted;

			for (Iterator<Integer> it = bestRanksSortedTmp.iterator(); it
					.hasNext();) {
				int currentRank = it.next();
				double currentSim = bestSimsMap.get(currentRank);

				if (sim > currentSim) {
					break;
				}

				index++;
			}
			bestRanksSorted.add(index, i);
		}

		currentBestRank = new Integer[ranking.size()];

		bestRanksSorted.copyInto(currentBestRank);
		setDisplayFirstIndexSorted(currentIndexForColumnSorting);

	}

	/**
	 * Sorts the rows according to the local similarities of the query values
	 * and the case specified by selectedRank
	 * 
	 * @param selectedRank
	 *            the index of the case
	 */
	protected void sortRows(int selectedRank) {
		if (ranking == null || ranking.size() <= selectedRank
				|| selectedRank < 0)
			return;

		if (selectedRank == this.selectedRank) {
			// toggle sortDescendingly
			sortDescendingly = !sortDescendingly;
		}

		// set selected column
		this.selectedRank = selectedRank;

		// find selected CaseInstance (selected row) in current order
		// The purpose is to reselect this row after new sorting
		TreePath path = tree.getSelectionPath();
		ModelSlot selectedSlot = ((path == null || path.getPathCount() == 0) ? null
				: (ModelSlot) ((DefaultMutableTreeNode) path
						.getLastPathComponent()).getUserObject());

		// find expanded tree nodes
		HashSet<Object> expandedSlots = findExpandedTreeNodes();

		// get instance and build new tree that is sorted by the explanation
		AssessedInstance ai = (AssessedInstance) ranking.get(selectedRank);
		DefaultMutableTreeNode newRoot = new DefaultMutableTreeNode(currentCls);
		buildupTreeNodesSorted(newRoot, currentQuery, ai.explanation);
		setNewRoot(newRoot);

		// now expand the right tree nodes again
		expandTreeNodes(expandedSlots);

		// find right selection index for rows
		if (selectedSlot != null) {
			for (int i = 0; i < tree.getRowCount(); i++) {
				if (((DefaultMutableTreeNode) tree.getPathForRow(i)
						.getLastPathComponent()).getUserObject() == selectedSlot) {
					tree.setSelectionRow(i);
					break;
				}
			}
		}
	}

	// interface RetrievalResultsListener
	public synchronized void setRetrievalResults(RetrievalResults results) {
		lastRetrievalResults = results;

		SwingUtilities.invokeLater(new Runnable() {

			public void run() {
				log.info("Finished retrieval.");

				if (lastRetrievalResults != null) {

					ranking = lastRetrievalResults.getRanking();

					lastModelToSMF = lastRetrievalResults.getRetrievalEngine()
							.getSimMap();

					if (!lastRetrievalResults.getOriginalQuery().equals(
							currentQuery)) {
						log
								.info("Query was not submitted by this widget. Show original query...");

						if (!(lastRetrievalResults.getOriginalQuery() instanceof DefaultQuery)) {
							log
									.info("This is not supported, yet. Query is not an instance of class "
											+ DefaultQuery.class.getName());
						} else {
							DefaultQuery oQuery = (DefaultQuery) lastRetrievalResults
									.getOriginalQuery();
							setQuery(oQuery);

						}

					}
				}
				setDisplayFirstIndex(0);
				progressBar.setVisible(false);
				buStartRetrieval.setEnabled(true);
			}
		});
	}

	public void setRetrievalState(double percentage) {
		progressBar.setValue((int) (percentage * 100));
	}

	// end interface

	private HashSet<Object> findExpandedTreeNodes() {
		HashSet<Object> expandedSlots = new HashSet<Object>();
		for (int i = 0; i < tree.getRowCount(); i++) {
			Object o = ((DefaultMutableTreeNode) tree.getPathForRow(i)
					.getLastPathComponent()).getUserObject();
			if (o instanceof ModelSlot && tree.isExpanded(i))
				expandedSlots.add(o);
		}
		return expandedSlots;
	}

	private void expandTreeNodes(HashSet<Object> expandedSlots) {
		for (int i = 0; i < tree.getRowCount(); i++) {
			Object o = ((DefaultMutableTreeNode) tree.getPathForRow(i)
					.getLastPathComponent()).getUserObject();
			if (expandedSlots.contains(o))
				tree.expandRow(i);
		}
	}

	public Explanation getCorrespondingExplanation(DefaultMutableTreeNode node,
			AssessedInstance ai) {
		TreeNode[] path = node.getPath();

		Explanation currentExplanation = ai.explanation;

		for (int i = 1; i < path.length && currentExplanation != null; i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) path[i];

			ModelSlot slot = (ModelSlot) child.getUserObject();
			currentExplanation = currentExplanation.getLocalExplanation(slot);
		}

		return currentExplanation;
	}

	private void clearLastRetrievalData() {
		ranking = null;
		ranksOfCases = null;
		lastRetrievalResults = null;
		lastModelToSMF = null;
		updatePageButtons();
	}

	protected void clearSelections() {
		table.clearSelection();
		tree.clearSelection();
		selectedRank = -1;
	}

	public void updatePageButtons() {
		buNext.setEnabled(ranking != null
				&& displayFirstIndex + MAX_CASES_TO_DISPLAY < ranking.size());
		buPrev.setEnabled(ranking != null && displayFirstIndex > 0);
	}

	@SuppressWarnings("unchecked")
	private void findPreferredTreeCellWidth() {
		JLabel label = new JLabel();
		int max = -1;
		for (Enumeration<DefaultMutableTreeNode> en = currentRoot
				.breadthFirstEnumeration(); en.hasMoreElements();) {
			DefaultMutableTreeNode node = en.nextElement();

			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node
					.getParent();
			if (parent != null
					&& tree.isCollapsed(new TreePath(parent.getPath())))
				continue;

			Object o = node.getUserObject();
			if (!(o instanceof ModelSlot))
				continue;

			String name = ((ModelInstance) o).getName();
			label.setText(name);
			int width = label.getPreferredSize().width
					+ (iconWidth * (node.getPath().length - 2));
			if (max < width)
				max = width;
		}
		preferredTreeCellWidth = max + 50;
		tree.setCellRenderer(getTreeCellRenderer());
	}

	/**
	 * Set RetrievalContainer. This container will be informed when some
	 * selections have changed.
	 * 
	 * @param retrievalContainer
	 */
	public void setRetrievalContainer(RetrievalContainer retrievalContainer) {
		this.retrievalContainer = retrievalContainer;
	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
		gridBagConstraints3.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints3.gridy = 3;
		gridBagConstraints3.gridx = 0;
		gridBagConstraints3.weighty = 0.1d;
		gridBagConstraints3.anchor = GridBagConstraints.SOUTH;
		GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
		gridBagConstraints2.gridx = 0;
		gridBagConstraints2.fill = GridBagConstraints.BOTH;
		gridBagConstraints2.weightx = 1.0D;
		gridBagConstraints2.weighty = 1.0D;
		gridBagConstraints2.gridy = 2;
		gridBagConstraints2.fill = GridBagConstraints.BOTH;
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints1.gridy = 1;
		gridBagConstraints1.weightx = 1d;
		gridBagConstraints1.anchor = GridBagConstraints.NORTH;
		GridBagConstraints gridBagConstraints = new GridBagConstraints();
		gridBagConstraints.gridx = 0;
		gridBagConstraints.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints.weightx = 1.0D;
		gridBagConstraints.gridy = 0;
		this.setSize(490, 427);

		this.setLayout(new BorderLayout());
		this.add(getPaHeader(), BorderLayout.PAGE_START);

		splitter = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
		splitter.addComponentListener(new ComponentAdapter() {
			@Override
			public void componentResized(ComponentEvent e) {
				double ratio = 0.8d;
				splitter.setDividerLocation(ratio);
			}
		});
		splitter.setTopComponent(getPaContent());
		splitter.setBottomComponent(getPaBottom());
		this.add(splitter, BorderLayout.CENTER);
		splitter.setDividerLocation(splitter.getMinimumDividerLocation());

		// if explanations are disabled, the corresponding panels
		// should not be shown
		setConceptExplanationsVisible(ExplanationManager.getInstance().isEnabled_conceptualExp());
		

	}

	public void setConceptExplanationsVisible(boolean visible) {
		splitter.getBottomComponent().setVisible(visible);
	}

	private JToolBar getToolBar() {

		if (toolBar == null) {

			toolBar = new JToolBar("myCBR Retrieval");
			toolBar.setLayout(new BoxLayout(toolBar, BoxLayout.X_AXIS));

			toolBar.add(getCbClass());
			toolBar.addSeparator();

			toolBar.add(getBuStartRetrieval());
			toolBar.addSeparator();

			toolBar.add(getBuQueryFromCase());
			toolBar.add(getBuSaveQueryAsCase());
			toolBar.add(getBuResetQuery());
			toolBar.addSeparator();

			toolBar.add(getBuReset());
			toolBar.addSeparator();

			toolBar.add(Box.createHorizontalGlue());
			toolBar.addSeparator();
			toolBar.add(getBuPrev());
			toolBar.add(getBuNext());
			toolBar.addSeparator();

			toolBar.setRollover(true);
			toolBar.setFloatable(false);
			toolBar.setMargin(new Insets(3, 0, 3, 0));
		}

		return toolBar;
	}

	/**
	 * This method initializes paContent
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getPaContent() {
		if (paContent == null) {
			paContent = new JPanel();

			scrollPane = new JScrollPane(getTable());

			// configure row header
			JPanel paTree = new JPanel(new GridBagLayout());
			paTree.add(getTree(), new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
					GridBagConstraints.CENTER, GridBagConstraints.BOTH,
					new Insets(0, 0, 0, 0), 0, 0));
			paTree.add(getQueryWeightsTable(), new GridBagConstraints(1, 0, 1,
					1, 1.0, 1.0, GridBagConstraints.CENTER,
					GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			paTree.add(getFilterTable(), new GridBagConstraints(2, 0, 1, 1,
					1.0, 1.0, GridBagConstraints.CENTER,
					GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
			paTree.setBorder(BorderFactory.createMatteBorder(0, 1, 1, 1,
					getTable().getGridColor()));
			JPanel paSpacer = new JPanel();
			paSpacer.setBorder(BorderFactory.createMatteBorder(0, 0, 0, 0,
					getTable().getGridColor()));
			JPanel parh = new JPanel(new BorderLayout());
			parh.add(paTree, BorderLayout.NORTH);
			parh.add(paSpacer, BorderLayout.CENTER);
			scrollPane.setRowHeaderView(parh);

			// configure progress bar
			JPanel paProgressBar = new JPanel(new GridBagLayout());
			paProgressBar.add(getProgressBar(), new GridBagConstraints(0, 0, 1,
					1, 1.0, 1.0, GridBagConstraints.NORTHWEST,
					GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
			progressBar.setVisible(false);

			// put everything to the panel
			paContent.setLayout(new BorderLayout());
			paContent.add(scrollPane, BorderLayout.CENTER);
			paContent.add(paProgressBar, BorderLayout.SOUTH);

		}
		return paContent;
	}

	private Component getQueryWeightsTable() {
		if (taQueryWeights == null) {
			taQueryWeights = new JTable();
			taQueryWeights.setToolTipText("Query Weights");
			taQueryWeights.setPreferredSize(new Dimension(new JLabel("=======")
					.getPreferredSize().width, taQueryWeights
					.getPreferredSize().height));
		}
		return taQueryWeights;
	}

	private JTable getFilterTable() {
		if (taFilter == null) {
			taFilter = new JTable();
			taFilter.setToolTipText("Filters");
			taFilter.addMouseMotionListener(new MouseMotionListener() {
				
				public void mouseMoved(MouseEvent e) {
					currentRow = taFilter.rowAtPoint(e.getPoint());
				}
				
				public void mouseDragged(MouseEvent e) {
					
				}
			});

			taFilter.setComponentPopupMenu(getFilterPopupMenu());
			JComboBox cbEditor = new JComboBox(FilterHandler.getFilters());
			taFilter.setDefaultEditor(Object.class, new DefaultCellEditor(
					cbEditor) {
				private static final long serialVersionUID = 1L;

				@Override
				public Component getTableCellEditorComponent(JTable table,
						Object value, boolean arg2, int row, int column) {
					JComboBox cb = (JComboBox) super
							.getTableCellEditorComponent(table, value, arg2,
									row, column);
					ModelSlot slot = (ModelSlot) ((DefaultMutableTreeNode) tree
							.getPathForRow(row).getLastPathComponent())
							.getUserObject();
					AbstractSMFunction aSMF =  (SMF_Class_Standard)SMFContainer.getInstance().getSMFHolderForModelInstance(RetrievalWidget.getInstance().currentCls).getActiveSMF();
					if (aSMF instanceof SMF_Class_Standard) {
						SMF_Class_Standard smfTMP = (SMF_Class_Standard)aSMF;
						if (smfTMP.getSlotAmalgamation(slot)!=null
								&& smfTMP.getSlotAmalgamation(slot).isEnabled()) {
							ValueType vt = slot.getValueType();
		
							Vector<Filter> values = new Vector<Filter>();
							values.add(FilterHandler.FILTER_SIMILAR);
							values.add(FilterHandler.FILTER_EQUAL);
		
							if (vt == ValueType.INTEGER || vt == ValueType.FLOAT) {
								values.add(FilterHandler.FILTER_LESS_THAN);
								values.add(FilterHandler.FILTER_GREATER_THAN);
							}
							cb.setModel(new DefaultComboBoxModel(values));
							cb.setSelectedIndex(values.indexOf(value));
							cb.setEnabled(true);
						
							return cb;
						}
					} 
						
					return null;
						
					
				}
				
			});

			taFilter.setPreferredSize(new Dimension(new JLabel("====")
					.getPreferredSize().width,
					taFilter.getPreferredSize().height));
			
		}
		return taFilter;
	}
	
	JPopupMenu m;
	int currentRow = 0;
	private JPopupMenu getFilterPopupMenu() {
		if (m == null) {
			m = new JPopupMenu();
			JMenuItem setFilterForALL = new JMenuItem("Use filter for each slot");
			setFilterForALL.addActionListener(new ActionListener() {
	
				public void actionPerformed(ActionEvent e) {
					Object value = taFilter.getModel().getValueAt(currentRow, 0);
					setAllFilters(value);
				}				
				
				private void setAllFilters(Object selectedItem) {
				for(int i = 0; i<taFilter.getRowCount(); i++) {
						JComboBox cb = (JComboBox)taFilter.getDefaultEditor(taFilter.getColumnClass(0)).getTableCellEditorComponent(taFilter, "", false, i, 0);
						if (cb != null) {
							for (int j = 0; j < cb.getModel().getSize(); j++) {
								if (cb.getModel().getElementAt(j).equals(selectedItem)) {
									taFilter.setValueAt(selectedItem, i, 0);
									break;
								}
							}
						}
					}
					taFilter.updateUI();
				}
					
			});
			m.add(setFilterForALL);
		}
		return m;
	}

	private JButton getBuReset() {
		if (buReset == null) {
			buReset = new JButton("Reset");
			buReset.setToolTipText("Reset Sorting");
			buReset.setMargin(new Insets(5, 5, 5, 5));
			buReset.setIcon(ICON_RESET);
			buReset.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					clearSelections();
					columnsSorted = false;
					currentIndexForColumnSorting = 0;
					setDisplayFirstIndex(0);

					// find expanded tree nodes
					HashSet<Object> expandedSlots = findExpandedTreeNodes();

					// set new Root
					DefaultMutableTreeNode newRoot = new DefaultMutableTreeNode(
							currentCls);
					buildupTreeNodes(newRoot, currentQuery);
					setNewRoot(newRoot);

					// now expand the right tree nodes again
					expandTreeNodes(expandedSlots);

					// full table update
					table.tableChanged(new TableModelEvent(table.getModel(),
							TableModelEvent.HEADER_ROW));
				}
			});
		}
		return buReset;
	}

	private Component getProgressBar() {
		if (progressBar == null) {
			progressBar = new JProgressBar();
		}
		return progressBar;
	}

	private JTable getTable() {
		if (table == null) {
			// init table
			table = new JTable() {
				private static final long serialVersionUID = 1L;

				public JToolTip createToolTip() {
					JToolTip tt = new MultiLineTooltip();
					return tt;
				}

				@SuppressWarnings("unchecked")
				public String getToolTipText(MouseEvent e) {
					int row = rowAtPoint(e.getPoint());
					int column = columnAtPoint(e.getPoint());

					StringBuffer sb = new StringBuffer();

					if (row < 0 || column < 0)
						return null;

					Object value = table.getValueAt(row, column);
					if (value == null)
						return null;

					DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
							.getPathForRow(row).getLastPathComponent();
					ModelSlot slot = (ModelSlot) node.getUserObject();

					if (column == COLUMN_INDEX_QUERY) {
						// QUERY VALUE
						sb.append("" + slot.getName() + ":\n");

						if (value instanceof Collection) {
							for (Iterator<Object> it = ((Collection<Object>) value)
									.iterator(); it.hasNext();) {
								Object v = it.next();
								sb.append("" + v + "\n");
							}
						} else {
							sb.append("" + value);
						}

					} else {
						// CASE VALUE
						if (ranking != null) {
							AssessedInstance ai = (AssessedInstance) ranking
									.get(ranksOfCases[column - 1]);
							Explanation exp = getCorrespondingExplanation(node,
									ai);
							String simTxt = null;
							String expTxt = "";
							String hintTxt = "";
							if (exp != null) {
								simTxt = Helper.getSimilarityStr(exp.getSimilarity());
								expTxt = "\n" + exp.getComments();
							} else {
								DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) node
										.getParent();
								while (parentNode != null) {
									Explanation superExp = getCorrespondingExplanation(
											parentNode, ai);
									if (superExp != null) {
										hintTxt = "see similarity of super component["
												+ parentNode.getUserObject()
												+ "] : ["
												+ Helper
														.formatDoubleAsString(superExp
																.getSimilarity())
												+ "]\n";
										break;
									}
									parentNode = (DefaultMutableTreeNode) parentNode
											.getParent();
								}
							}
							sb.append(ai.inst.getName() + "\n" + slot.getName()
									+ " = " + value + " => similarity = "
									+ simTxt + "\n");
							sb.append(hintTxt);
							sb.append(expTxt);

							// look for knowledge engineer remarks and
							// additional information.
							if (exp != null) {
								AbstractSMFunction smFunction = exp.getSmf();
								if (smFunction != null) {
									SMExplanationContainer expContainer = smFunction
											.getExplanationContainer();
									if (expContainer != null) {
										DefaultSMExplanation exp2 = expContainer
												.getExplanation(exp.getQuery(),
														exp.getCbInstance());
										if (exp2 != null) {
											sb.append("Rationale: "
													+ exp2.getRationale()
													+ "\n");
											sb.append("Author: "
													+ exp2.getAuthor() + "\n");
										}
									}
								}
							}
						}
					}

					return sb.toString();

				}
			};

			JTableHeader tableHeader = new JTableHeader(table.getColumnModel()) {
				private static final long serialVersionUID = 1L;

				public JToolTip createToolTip() {
					return new MultiLineTooltip();
				}
			};
			tableHeader.setReorderingAllowed(false);
			table.setTableHeader(tableHeader);

			table.setDefaultEditor(Object.class, getTableCellEditor());
			table.setDefaultRenderer(Object.class, getTableCellRenderer());

			int height = ROW_HEIGHT * 3;
			table.getTableHeader().setPreferredSize(
					new Dimension(
							table.getTableHeader().getPreferredSize().width,
							height));
			table.getTableHeader().setDefaultRenderer(
					getHeaderCellRendererColumn());
			table.getTableHeader().addMouseListener(new MouseAdapter() {
				public void mousePressed(MouseEvent e) {
					if (ranksOfCases == null
							|| (table.columnAtPoint(e.getPoint()) == 0) || !ExplanationOptions.getInstance().isExplanationsEnabled()) {
						return;
					}
					sortRows(ranksOfCases[table.columnAtPoint(e.getPoint()) - 1]);
					RetrievalWidget.this.updateUI();
					
				}
				
			});

			table.setComponentPopupMenu(getPopupMenu());
			table.getTableHeader().addMouseMotionListener(
					new MouseMotionAdapter() {
						public void mouseMoved(MouseEvent e) {
							int columnIndex = table.columnAtPoint(e.getPoint()) - 1;
							String text = null;
							if (ranksOfCases != null && columnIndex >= 0
									&& columnIndex < ranksOfCases.length) {
								AssessedInstance ai = (AssessedInstance) ranking
										.get(ranksOfCases[columnIndex]);
								StringBuffer sb = new StringBuffer();

								DefaultMutableTreeNode node = currentRoot;

								Explanation exp = getCorrespondingExplanation(
										node, ai);
								String expTxt = "";
								String hintTxt = "";
								if (exp != null) {
									expTxt = exp.getComments();
								} else {
									DefaultMutableTreeNode p = (DefaultMutableTreeNode) node
											.getParent();
									while (p != null) {
										Explanation superExp = getCorrespondingExplanation(
												p, ai);
										if (superExp != null) {
											hintTxt = "see similarity of super component["
													+ p.getUserObject()
													+ "] : ["
													+ Helper
															.formatDoubleAsString(superExp
																	.getSimilarity())
													+ "]\n";
											break;
										}
										p = (DefaultMutableTreeNode) p
												.getParent();
									}

								}
								sb.append(hintTxt);
								sb.append(expTxt);
								text = sb.toString();
							}
							table.getTableHeader().setToolTipText(text);
						}
					});

		}
		return table;
	}

	private JPopupMenu getPopupMenu() {
		if (popupMenu == null) {
			popupMenu = new JPopupMenu();
			popupMenu.addPopupMenuListener(new PopupMenuListener() {

				public void popupMenuCanceled(PopupMenuEvent e) {
				}

				public void popupMenuWillBecomeInvisible(PopupMenuEvent e) {
				}

				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {

					popupMenu.removeAll();

					if (ranking == null)
						return;

					JMenuItem itemLoadQuery = new JMenuItem("Case as query");
					itemLoadQuery.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							HashSet<Object> expandedSlots = findExpandedTreeNodes();
							if (ranksOfCases.length < currentCol - 1) {
								return;
							}
							AssessedInstance ai = (AssessedInstance) ranking
									.get(ranksOfCases[currentCol - 1]);
							
							setQuery(new DefaultQuery(ai.inst, CBRProject
									.getInstance().getProjectOptions()
									.getWeightMode()));
							expandTreeNodes(expandedSlots);
						}
					});

					popupMenu.add(itemLoadQuery);

					if (currentCol == 0) {
						itemLoadQuery.setEnabled(false);
					}

					if (currentConcExpScheme == null)
						return;

					for (KnowledgeSource ks : currentConcExpScheme.getSources()) {
						JMenuItem item = new JMenuItem("Explain " + concept
								+ " via " + ks.getTitle());
						item.setActionCommand(ks.resolve("" + concept));
						item.addActionListener(new ActionListener() {
							public void actionPerformed(ActionEvent e) {
								String url = e.getActionCommand();
								try {
									Helper.browse(url);
								} catch (IOException e1) {
									log.log(Level.INFO,
											"Could not open browser for URL ["
													+ url + "]", e1);
								}
							}
						});
						popupMenu.add(item);
					}
				}
			});
		}
		return popupMenu;
	}

	private TableCellRenderer getHeaderCellRendererColumn() {
		TableCellRenderer headerRenderer = new TableCellRenderer() {
			JPanel panel = new JPanel(new GridBagLayout());
			JLabel laName = new JLabel();
			JLabel laSim = new JLabel();
			JLabel laRank = new JLabel();
			JLabel laIcon = new JLabel();
			Color defaultColor;
			{
				panel.add(laName, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
				panel.add(laRank, new GridBagConstraints(0, 1, 1, 1, 1.0, 1.0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
				panel.add(laSim, new GridBagConstraints(0, 2, 1, 1, 1.0, 1.0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(0, 0, 0, 0), 0, 0));
				panel.add(laIcon, new GridBagConstraints(1, 0, 1, 3, 0.0, 1.0,
						GridBagConstraints.CENTER, GridBagConstraints.NONE,
						new Insets(5, 5, 5, 5), 0, 0));

				panel.setBorder(BorderFactory.createMatteBorder(0, 0, 1, 1,
						getTable().getGridColor()));
				defaultColor = panel.getBackground();
			}

			public Component getTableCellRendererComponent(JTable table,
					Object ciName, boolean isSelected, boolean hasFocus,
					int row, int column) {
				laName.setText((ciName == null ? "-" : "" + ciName));

				int selectedColumn = -1;
				if (ranksOfCases != null) {
					for (int i = 0; i < ranksOfCases.length; i++) {
						if (ranksOfCases[i] == selectedRank) {
							selectedColumn = i + 1;
							break;
						}
					}
				}

				// set selection
				if (column == selectedColumn && column != COLUMN_INDEX_QUERY) {
					panel.setBackground(table.getSelectionBackground());
					laIcon.setIcon((sortDescendingly ? ICON_SORT_VERT_DOWN
							: ICON_SORT_VERT_UP));
				} else {
					panel.setBackground(defaultColor);
					laIcon.setIcon(ICON_SORT_VERT_CLEAR);

				}

				if (ranking == null || ranksOfCases == null
						|| column > ranksOfCases.length
						|| column == COLUMN_INDEX_QUERY) {
					laSim.setText("");
					laRank.setText("");
					laIcon.setIcon(null);
				} else {
					int rankIndex = ranksOfCases[column - 1];
					AssessedInstance ai = (AssessedInstance) ranking
							.get(rankIndex);
					laSim.setText(Helper.getSimilarityStr(ai.similarity));
					laRank.setText("" + (rankIndex + 1));
				}

				return panel;
			}

		};
		return headerRenderer;
	}

	private TableCellRenderer getTableCellRenderer() {
		if (cellRenderer == null) {
			cellRenderer = new DefaultTableCellRenderer() {
				private static final long serialVersionUID = 1L;
				Color defaultBGColor = new JTable().getBackground();
				JPanel panel = new JPanel(new GridBagLayout());
				JLabel labelExplanationLS = new JLabel(Helper
						.getSimilarityStr(9.99d));
				JLabel labelExplanationWLS = new JLabel(Helper
						.getSimilarityStr(9.99d));
				JPanel paLS = new JPanel(new BorderLayout());
				JPanel paWLS = new JPanel(new BorderLayout());
				boolean isInitialized = false;
				Dimension prefSize = panel.getPreferredSize();

				{
					labelExplanationLS.setMinimumSize(labelExplanationLS
							.getPreferredSize());
					labelExplanationWLS.setMinimumSize(labelExplanationWLS
							.getPreferredSize());
					labelExplanationLS
							.setHorizontalAlignment(SwingConstants.TRAILING);
					paLS.add(labelExplanationLS, BorderLayout.CENTER);
					paWLS.add(labelExplanationWLS, BorderLayout.CENTER);
					paLS.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 1,
							Color.black));
					paWLS.setBorder(BorderFactory.createMatteBorder(0, 1, 0, 1,
							Color.black));
					paLS.setVisible(false);
					paWLS.setVisible(false);
					panel.add(paWLS, new GridBagConstraints(2, 0, 1, 1, 0.0,
							0.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH, new Insets(0, 0, 0, 2), 0,
							0));
					panel.add(paLS, new GridBagConstraints(3, 0, 1, 1, 0.0,
							0.0, GridBagConstraints.CENTER,
							GridBagConstraints.BOTH, new Insets(0, 2, 0, 0), 0,
							0));
				}

				@SuppressWarnings("unchecked")
				public Component getTableCellRendererComponent(JTable table,
						Object value, boolean isSelected, boolean hasFocus,
						int row, int column) {
					Component component = super.getTableCellRendererComponent(
							table, value, isSelected, hasFocus, row, column);
					boolean expVisibleLS = false;
					boolean expVisibleWLS = false;
					if (!isInitialized) {
						panel.add(component, new GridBagConstraints(0, 0, 1, 1,
								1.0, 0.0, GridBagConstraints.CENTER,
								GridBagConstraints.BOTH,
								new Insets(0, 0, 0, 0), 0, 0));
					}

					if (value == null)
						return panel;// comp;
					boolean correctValue = true;

					DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) tree
							.getPathForRow(row).getLastPathComponent();
					ModelSlot slot = (ModelSlot) treeNode.getUserObject();

					// check correctness of value
					ValueType vt = slot.getValueType();
					if (column == COLUMN_INDEX_QUERY && !slot.isMultiple()
							&& slot.getMinimumValue() != null
							&& slot.getMaximumValue() != null) {
						if (SpecialValueHandler.getInstance().isSpecialValue(
								value))
							;
						else if (vt == ValueType.INTEGER
								|| vt == ValueType.FLOAT) {
							try {
								double val = Double.parseDouble(value.toString());
								
								double min = slot.getMinimumValue().doubleValue();
								double max = slot.getMaximumValue().doubleValue();
								correctValue = (val >= min && val <= max);
							} catch (Exception e) {
								
							}
						}
					}

					Color fgColor = (correctValue ? Color.black : Color.red);
					if (value == SpecialValueHandler.SPECIAL_VALUE_UNDEFINED)
						fgColor = Color.GRAY;
					component.setForeground(fgColor);

					// has the original query value changed?
					boolean queryValueChanged = false;
					Vector<AssessedInstance> tmpRanking = ranking;
					double sim = -1;
					if (tmpRanking != null && lastRetrievalResults != null
							&& ranksOfCases != null) {
						if (column == COLUMN_INDEX_QUERY) {
							DefaultQuery query = (DefaultQuery) Helper
									.getCorrespondingObject(treeNode,
											(DefaultQuery) lastRetrievalResults
													.getOriginalQuery());
							if (query != null) {
								Object origQueryValue = query
										.getSlotValue(slot);

								queryValueChanged = ((origQueryValue == null && value != null) || (origQueryValue != null && !origQueryValue
										.equals(value))); // currentQuery.getQueryValue(slot)

								// fix collection compare problem (different
								// classes...)
								if (queryValueChanged && slot.isMultiple()
										&& value != null
										&& origQueryValue != null
										&& value instanceof Collection
										&& origQueryValue instanceof Collection) {
									Collection<Object> vc = (Collection<Object>) value;
									Collection<Object> origc = (Collection<Object>) origQueryValue;
									if (vc.containsAll(origc)
											&& origc.containsAll(vc))
										queryValueChanged = false;
								}
							} else {
								log
										.warning("Could not find corresponding object!");
							}
						} else {
							String expTxtLS = "";
							String expTxtWLS = "";
							ExplanationManager expManager = ExplanationManager
									.getInstance();
							boolean expEnabled = expManager.isEnabled();
							if (column != COLUMN_INDEX_QUERY
									&& tmpRanking != null) {
								if (!(value == SpecialValueHandler.SPECIAL_VALUE_UNDEFINED && CBRProject
										.getInstance().getProjectOptions()
										.isIgnoreUndefinedInRetrieval_Query())) {
									AssessedInstance ai = (AssessedInstance) tmpRanking
											.get(ranksOfCases[column - 1]);
									Explanation exp = getCorrespondingExplanation(
											treeNode, ai);
									if (exp == null) {
										expTxtLS = "?";
										expTxtWLS = "?";
									} else {
										sim = exp.getSimilarity();
										
										// we display weight instead of weighted local sim!
										//double weightedSim = sim * exp.getWeight();
										double weightedSim = exp.getWeight();
										if (!Double.isNaN(sim)) {
											expVisibleLS = expEnabled
													&& expManager
															.isEnabled_retrieval_showLocalSimilarities();
											expVisibleWLS = expEnabled
													&& expManager
															.isEnabled_retrieval_showWeightedLocalSimilarities();
											expTxtLS = Helper
													.getSimilarityStr(sim);
											expTxtWLS = Helper
											.getSimilarityStr(weightedSim);
										}
									}
									labelExplanationLS
											.setPreferredSize(prefSize);
									labelExplanationWLS
											.setPreferredSize(prefSize);
								}
							}
							if (!expManager.isEnabled()) {
								expTxtLS = "";
								expTxtWLS = "";
							}
							labelExplanationLS.setText(expTxtLS);
							
							// Format weighted local similarities 
							// in the same way as similarities
							String txtOLD = expTxtWLS;
							try {
								expTxtWLS = Helper.getSimilarityStr(Double.parseDouble(expTxtWLS));
							} catch (Exception e) {
								expTxtWLS = txtOLD;
							}
							labelExplanationWLS.setText(expTxtWLS);
						}
					}

					// background color
					Color bgColor = defaultBGColor;
					if (column == COLUMN_INDEX_QUERY) {
						// in case the query changed disable the results
						// because they now do not belong to the current query
						// any longer
						if (queryValueChanged) {
							// TODO
						}
					} else if (sim >= 0d
							&& ExplanationOptions.getInstance()
									.isHighlightSimilarity()) {
						float f = (float) sim;
						bgColor = new Color(1f - f / 2, 1f - f / 3, 1f - f / 3);
					}
					
					AbstractSMFunction aSMF =  (SMF_Class_Standard)SMFContainer.getInstance().getSMFHolderForModelInstance(RetrievalWidget.getInstance().currentCls).getActiveSMF();
					
					component.setForeground(Color.BLACK);
					
					if (aSMF instanceof SMF_Class_Standard) {
						SMF_Class_Standard smfTMP = (SMF_Class_Standard)aSMF;
						if (smfTMP.getSlotAmalgamation(slot)!=null
								&& !smfTMP.getSlotAmalgamation(slot).isEnabled()) {
							component.setForeground(Color.GRAY);
						}
					}
					
					component.setBackground(bgColor);

					paLS.setVisible(expVisibleLS);
					paWLS.setVisible(expVisibleWLS);

					return panel;
				}
			};
		}
		return cellRenderer;
	}

	private TableCellEditor getTableCellEditor() {
		if (cellEditor == null) {
			JComboBox editorBox = new JComboBox();
			editorBox.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					JComboBox box = (JComboBox) e.getSource();
					Object item = box.getSelectedItem();
					if (item instanceof ModelCls
							|| item instanceof CaseInstance) {
						setQuery(currentQuery);
						
					}
					revalidate();
					repaint();
				}
			});
			DefaultCellEditor tmpCellEditor = new DefaultCellEditor(editorBox) {
				private static final long serialVersionUID = 1L;

				@SuppressWarnings("unchecked")
				public Component getTableCellEditorComponent(JTable table,
						Object value, boolean isSelected, int row, int col) {
					JComboBox box = (JComboBox) super
							.getTableCellEditorComponent(table, value,
									isSelected, row, col);

					Vector<Object> values = new Vector<Object>();
					values.addAll(SpecialValueHandler.getInstance()
							.getAllSpecialValues());

					ModelSlot slot = (ModelSlot) ((DefaultMutableTreeNode) tree
							.getPathForRow(row).getLastPathComponent())
							.getUserObject();

					if (slot.getValueType() == ValueType.SYMBOL) {
						values.addAll(slot.getAllowedValues());
						if (slot.isMultiple()
								&& value != null
								&& !SpecialValueHandler.getInstance()
										.isSpecialValue(value))
							values.removeAll((Collection<Object>) value);
						box.setEditable(false);

						box.setModel(new DefaultComboBoxModel(values));
						box.setSelectedIndex(values.indexOf(value));
					} else if (slot.getValueType() == ValueType.INSTANCE
							&& slot.getAllowedValues().size() == 1) {
						DefaultQuery query = (DefaultQuery) value;
						ModelCls topCls = (ModelCls) slot.getAllowedValues()
								.iterator().next();
						Vector allSubCls = new Vector();
						allSubCls.add(topCls);
						for (int i = 0; i < allSubCls.size(); i++) {
							ModelCls cls = (ModelCls) allSubCls.get(i);
							allSubCls.addAll(cls.getDirectSubClses());
						}
						allSubCls.addAll(topCls.getDirectCaseInstances());
						box.setEditable(false);
						box.setModel(new DefaultComboBoxModel(allSubCls));
						if (query != null)
							box.setSelectedIndex(allSubCls.indexOf(query
									.getModelCls()));
					} else {
						box.setEditable(true);
						if (!slot.isMultiple() && values.indexOf(value) < 0) {
							values.add(value);
						}
						String emptyStr = "";
						values.add(emptyStr);

						box.setModel(new DefaultComboBoxModel(values));
						box.setSelectedIndex(values.indexOf(value));
						if (SpecialValueHandler.getInstance().isSpecialValue(
								value)
								&& !"".equals(box.getEditor().getItem())) {
							box.setSelectedItem(emptyStr);
						}

					}
					return box;
				}

				@Override
				public Object getCellEditorValue() {
					Object o = super.getCellEditorValue();
					return o;
				}
			};
			cellEditor = new MultipleCellEditor(this, tmpCellEditor);
		}
		return cellEditor;
	}

	private Component getTree() {
		if (tree == null) {
			tree = new SlotTreeView(this.currentRoot, table);
			tree.setComponentPopupMenu(getPopupMenu());
			tree.addMouseListener(getTreeSelectionMouseListener());
			tree.addMouseMotionListener(getTreeMouseMotionListener());

		}
		return tree;
	}

	protected MouseMotionListener getTreeMouseMotionListener() {
		MouseMotionListener mouseListener = new MouseMotionListener() {

			public void mouseDragged(MouseEvent arg0) {
			}

			TreePath path = null;

			public void mouseMoved(MouseEvent e) {
				path = tree.getClosestPathForLocation(e.getX(), e.getY());
				DefaultMutableTreeNode n = (DefaultMutableTreeNode) path
						.getLastPathComponent();
				if (n.getUserObject() instanceof ModelSlotProtege) {

					tree.setToolTipText(((ModelSlotProtege) n.getUserObject())
							.getName());
				}
			}

		};
		return mouseListener;
	}

	protected MouseListener getTreeSelectionMouseListener() {
		MouseListener mouseListener = new MouseListener() {

			public void mouseExited(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			public void mouseEntered(MouseEvent e) {
				try {
					path = tree.getClosestPathForLocation(e.getX(), e.getY());
					DefaultMutableTreeNode n = (DefaultMutableTreeNode) path
							.getLastPathComponent();
					if (n.getUserObject() instanceof ModelSlotProtege) {
	
						tree.setToolTipText(((ModelSlotProtege) n.getUserObject())
								.getName());
					}
				} catch (Exception exc) {
					System.err.println("Exception in mouse motion listener!");
					exc.printStackTrace();
				}

			}

			public void mouseClicked(MouseEvent arg0) {
				// TODO Auto-generated method stub

			}

			TreePath path = null;

			public void mousePressed(MouseEvent e) {
				try {
					path = tree.getClosestPathForLocation(e.getX(), e.getY());
					if (e.getX() <= (path.getPathCount() - 1) * iconWidth) {
						// expand or collapse (toggle)
						if (tree.isExpanded(path))
							tree.collapsePath(path);
						else
							tree.expandPath(path);
					} else {
						// select button
						tree.setSelectionPath(path);
					}
				} catch (Exception exc) {
					System.err.println("Exception in mouse motion listener!");
					exc.printStackTrace();
				}
			}

			public void mouseReleased(MouseEvent e) {
				// only if explanations are used there is a local similarity
				// given by which we can sort!
				if (ExplanationOptions.getInstance().isExplanationsEnabled()) {
					try {
						if (path != null
								&& e.getX() > (path.getPathCount() - 1) * iconWidth) {
							// select button
							tree.setSelectionPath(path);
							// only sort columns in case there is a retrieval result
							if (ranking != null) {
								sortColumns((DefaultMutableTreeNode) path
										.getLastPathComponent());
							}
						}
						path = null;
					} catch (Exception exc) {
						System.err.println("Exception in mouse motion listener!");
						exc.printStackTrace();
					}
				}
			}
		};
		return mouseListener;
	}

	private TreeCellRenderer getTreeCellRenderer() {
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
			private static final long serialVersionUID = 1L;
			JPanel panel = new JPanel(new GridBagLayout());
			JLabel lab = new JLabel();
			JLabel laIcon = new JLabel(ICON_SORT_HORIZ);
			int lastWidth = -1;

			JButton button = new JButton(">");
			ButtonModel buttonModel;
			{
				panel.add(lab, new GridBagConstraints(0, 0, 1, 1, 0.0, 1.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				panel.add(button, new GridBagConstraints(1, 0, 1, 1, 1.0, 1.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));

				panel.add(laIcon, new GridBagConstraints(2, 0, 1, 1, 0.0, 1.0,
						GridBagConstraints.CENTER, GridBagConstraints.BOTH,
						new Insets(0, 0, 0, 0), 0, 0));
				laIcon.setVisible(false);

				lab.setBackground(tree.getBackground());
				lab.setForeground(tree.getForeground());
				button.setHorizontalAlignment(SwingConstants.LEFT);
				buttonModel = button.getModel();
			}

			public Component getTreeCellRendererComponent(JTree tree,
					Object value, boolean selected, boolean expanded,
					boolean leaf, int row, boolean hasFocus) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
				TreeNode[] path = node.getPath();
				
				JLabel label = (JLabel) super.getTreeCellRendererComponent(
						tree, value, selected, expanded, leaf, row, hasFocus);

				lab.setIcon(label.getIcon());
				if (node.getUserObject() instanceof ModelSlotProtege) {
					ModelSlotProtege ms = (ModelSlotProtege)node.getUserObject();
					
					button.setForeground(Color.BLACK);					
					if (initialized) {
						AbstractSMFunction aSMF = (SMF_Class_Standard)SMFContainer.getInstance().getSMFHolderForModelInstance(RetrievalWidget.getInstance().currentCls).getActiveSMF();
	
						if (aSMF instanceof SMF_Class_Standard) {
							SMF_Class_Standard smfTMP = (SMF_Class_Standard)aSMF;
							if (smfTMP.getSlotAmalgamation(ms)!=null
									&& !smfTMP.getSlotAmalgamation(ms).isEnabled()) {
								button.setForeground(Color.GRAY);
							}
						}
					}
					
				} 
				iconWidth = lab.getIcon().getIconWidth() + lab.getIconTextGap()
						+ 0;
				int currentWidth = preferredTreeCellWidth
						- ((path.length - 2) * iconWidth);
				if (currentWidth != lastWidth) {
					panel.setPreferredSize(new Dimension(currentWidth, panel
							.getPreferredSize().height));
					lastWidth = currentWidth;
				}

				button.setText(label.getText());
				buttonModel.setArmed(selected);
				laIcon.setVisible(selected);
				buttonModel.setPressed(selected);
				
				return panel;
			}
		};
		if (ICON_TREE_CLOSED != null)
			renderer.setClosedIcon(ICON_TREE_CLOSED);
		if (ICON_TREE_OPENED != null)
			renderer.setOpenIcon(ICON_TREE_OPENED);
		if (ICON_TREE_LEAF != null)
			renderer.setLeafIcon(ICON_TREE_LEAF);
		return renderer;
	}

	private JTextArea txtDoc;

	
	/**
	 * This method initializes paBottom
	 * 
	 * @return javax.swing.JPanel
	 */
	private JPanel getPaBottom() {
		if (paBottom == null) {
			paBottom = new JPanel(new BorderLayout());
			
			txtExpConcept = new JTextArea();
			txtExpConcept.setLineWrap(true);
			txtExpConcept.setWrapStyleWord(true);
			txtExpConcept.setEditable(false);
			txtExpConcept.setBorder(BorderFactory.createEtchedBorder());
			
			txtDoc = new JTextArea();
			txtDoc.setLineWrap(true);
			txtDoc.setWrapStyleWord(true);
			txtDoc.setEditable(false);
			txtDoc.setBorder(BorderFactory.createEtchedBorder());
			
			txtURLConcept = new JTextArea();
			txtURLConcept.setBorder(BorderFactory.createEtchedBorder());

			Dimension size = new Dimension(350, 150);
			txtExpConcept.setPreferredSize(size);
			txtDoc.setPreferredSize(size);
			txtURLConcept.setPreferredSize(size);
			
			laExpConceptDescription = new JLabel(" ");
			
			Box boxMain = Box.createHorizontalBox();
			
			Box firstBox = Box.createVerticalBox();
			firstBox.add(laExpConceptDescription);
			laExpConceptDescription.setAlignmentY(Component.LEFT_ALIGNMENT);
			laExpConceptDescription.setAlignmentX(Component.LEFT_ALIGNMENT);
			firstBox.add(Box.createVerticalStrut(5));
			txtExpConcept.setAlignmentX(Component.LEFT_ALIGNMENT);
			txtExpConcept.setAlignmentY(Component.LEFT_ALIGNMENT);
			firstBox.add(txtExpConcept);
			firstBox.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
			
			boxMain.add(firstBox);
			boxMain.add(Box.createHorizontalStrut(5));
			
			Box secondBox = Box.createVerticalBox();
			JLabel ld = new JLabel("Documentation:");
			secondBox.add(ld);
			ld.setAlignmentY(Component.LEFT_ALIGNMENT);
			ld.setAlignmentX(Component.LEFT_ALIGNMENT);
			secondBox.add(Box.createVerticalStrut(5));
			txtDoc.setAlignmentX(Component.LEFT_ALIGNMENT);
			txtDoc.setAlignmentY(Component.LEFT_ALIGNMENT);
			secondBox.add(txtDoc);
			secondBox.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
			
			boxMain.add(secondBox);
			boxMain.add(Box.createHorizontalStrut(5));
			
			Box thirdBox = Box.createVerticalBox();
			JLabel lsa = new JLabel("See also:");
			thirdBox.add(lsa);
			lsa.setAlignmentY(Component.LEFT_ALIGNMENT);
			lsa.setAlignmentX(Component.LEFT_ALIGNMENT);
			thirdBox.add(Box.createVerticalStrut(5));
			txtURLConcept.setAlignmentX(Component.LEFT_ALIGNMENT);
			txtURLConcept.setAlignmentY(Component.LEFT_ALIGNMENT);
			
			thirdBox.add(txtURLConcept);
			thirdBox.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
			
			boxMain.add(thirdBox);
			boxMain.add(Box.createHorizontalStrut(5));
			
			paBottom.add(boxMain, BorderLayout.WEST);
			
		}
		return paBottom;
	}

	/**
	 * This method initializes buStartRetrieval
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getBuStartRetrieval() {
		if (buStartRetrieval == null) {
			buStartRetrieval = new JButton();
			buStartRetrieval.setText("Retrieve");
			buStartRetrieval.setToolTipText("Start Retrieval");
			buStartRetrieval.setMargin(new Insets(5, 5, 5, 5));
			buStartRetrieval.setIcon(ICON_START_RETRIEVAL);
			buStartRetrieval.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if (currentCls == null)
						return;
					if (currentCls.getDirectCaseInstances().size() == 0) {
						JOptionPane.showMessageDialog(null,
								"There are no cases to retrieve",
								"Casebase empty",
								JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					startRetrieval();
				}
			});
		}
		return buStartRetrieval;
	}

	private JPanel getPaHeader() {
		if (paHeader == null) {
			paHeader = new JPanel(new BorderLayout());
			String classBrowserLabel = "DETAILS AND QUERY";	
			paHeader.setLayout(new BorderLayout());
			JPanel titlePanel = new JPanel(new BorderLayout());
			titlePanel.setBackground(Helper.COLOR_RED_MYCBR);
			JLabel titleLabel = new JLabel(classBrowserLabel.toUpperCase());
			titleLabel.setForeground(Color.white);
			titleLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 2));
			titlePanel.add(titleLabel);
			paHeader.add(titlePanel, BorderLayout.NORTH);
			paHeader.add(getToolBar(), BorderLayout.CENTER);
		}
		return paHeader;
	}

	/**
	 * This method initializes cbClass
	 * 
	 * @return javax.swing.JComboBox
	 */
	private JComboBox getCbClass() {
		if (cbClass == null) {
			cbClass = new JComboBox();
			cbClass.setSize(new Dimension(90, 22));
			cbClass.setPreferredSize(new Dimension(90, 22));
			cbClass.setMinimumSize(new Dimension(90, 22));
			cbClass.setMaximumSize(new Dimension(90, 22));
			cbClass.addActionListener(getCbClassActionListener());
		}
		return cbClass;
	}

	public ActionListener getCbClassActionListener() {
		if (cbClassActionListener == null) {
			cbClassActionListener = new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					ModelCls cls = (ModelCls) cbClass.getSelectedItem();
					if (cls == currentCls)
						return;
					log.fine("select another class: [" + cls.getName() + "]");
					setModelCls(cls);
				}
			};
		}
		return cbClassActionListener;
	}

	/**
	 * This method initializes buResetQuery
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getBuResetQuery() {
		if (buResetQuery == null) {
			buResetQuery = new JButton();
			buResetQuery.setText("Clear");
			buResetQuery.setToolTipText("Clear Query");
			buResetQuery.setMargin(new Insets(5, 5, 5, 5));
			buResetQuery.setIcon(ICON_CLEAR);
			buResetQuery.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {

					if (currentCls == null)
						return;
					currentQuery = new DefaultQuery(currentCls, CBRProject
							.getInstance().getProjectOptions().getWeightMode());
					clearSelections();
					clearLastRetrievalData();
					resetRankingTableModel();
					
					RetrievalContainer.getInstance().getPaRetrievalResults().setVisible(false);

				}
			});
		}
		return buResetQuery;
	}

	private JList list;

	/**
	 * This method initializes buQueryFromCase
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getBuQueryFromCase() {
		if (buQueryFromCase == null) {
			buQueryFromCase = new JButton();
			buQueryFromCase.setText("Load");
			buQueryFromCase.setMargin(new Insets(5, 5, 5, 5));
			buQueryFromCase.setToolTipText("Load Query From Case");
			buQueryFromCase.setIcon(ICON_LOAD);
			buQueryFromCase.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {

					if (currentCls == null) {
						JOptionPane.showMessageDialog(null,
								"Sorry, no Class selected.");
						return;
					}
					if (currentCls.getDirectCaseInstances().size() == 0) {
						JOptionPane.showMessageDialog(null,
								"There are no cases available",
								"Casebase empty",
								JOptionPane.INFORMATION_MESSAGE);
						return;
					}
					Vector<Object> v = new Vector<Object>(currentCls
							.getDirectCaseInstances());
					list = new JList(v);
					JDialog d = Helper.createDialog(
							(Window) getTopLevelAncestor(), "", true);
					JPanel pa = new JPanel(new BorderLayout());
					pa.add(new JLabel("Please select a case:"),
							BorderLayout.NORTH);
					pa.add(new JScrollPane(list), BorderLayout.CENTER);

					list.addMouseListener(new MouseAdapter() {
						public void mouseClicked(MouseEvent e) {
							if (e.getClickCount() >= 2) {
								HashSet<Object> expandedSlots = findExpandedTreeNodes();

								((JDialog) ((JComponent) e.getSource())
										.getTopLevelAncestor()).dispose();
								CaseInstance ci = (CaseInstance) list
										.getSelectedValue();
								if (ci == null) {
									return;
								}
								setQuery(new DefaultQuery(ci, CBRProject
										.getInstance().getProjectOptions()
										.getWeightMode()));

								expandTreeNodes(expandedSlots);

							}
						}
					});

					JButton _buOk = new JButton("OK");
					_buOk.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent e) {
							HashSet<Object> expandedSlots = findExpandedTreeNodes();

							((JDialog) ((JComponent) e.getSource())
									.getTopLevelAncestor()).dispose();
							CaseInstance ci = (CaseInstance) list
									.getSelectedValue();
							if (ci == null) {
								return;
							}
							setQuery(new DefaultQuery(ci, CBRProject
									.getInstance().getProjectOptions()
									.getWeightMode()));
							expandTreeNodes(expandedSlots);

						}
					});
					pa.add(_buOk, BorderLayout.SOUTH);

					d.getContentPane().add(pa);
					d.setSize(200, 300);
					Helper.centerWindow(d);
					d.setVisible(true);

				}
			});
		}
		return buQueryFromCase;
	}

	/**
	 * This method initializes buSaveQueryAsCase
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getBuSaveQueryAsCase() {
		if (buSaveQueryAsCase == null) {
			buSaveQueryAsCase = new JButton();
			buSaveQueryAsCase.setText("Save");
			buSaveQueryAsCase.setToolTipText("Save Query as Case");
			buSaveQueryAsCase.setMargin(new Insets(5, 5, 5, 5));
			buSaveQueryAsCase.setIcon(ICON_SAVE);
			buSaveQueryAsCase.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					if (currentQuery == null) {
						JOptionPane.showMessageDialog(null,
								"Sorry, no Query specified.");
						return;
					}

					Vector<String> slotsAsVector = new Vector<String>();
					Vector<String> queryValuesAsVector = new Vector<String>();
					Vector<Vector<String>> slotsAsVectorOfVectors = new Vector<Vector<String>>();

					for (Iterator<ModelSlot> it = currentQuery.getModelCls()
							.listSlots().iterator(); it.hasNext();) {
						Vector<String> vectorTmp = new Vector<String>();
						ModelSlot slot = it.next();
						slotsAsVector.add(slot.getName());
						vectorTmp.add(slot.getName());
						queryValuesAsVector.add(currentQuery.getSlotValue(slot)
								.toString());
						slotsAsVectorOfVectors.add(vectorTmp);
					}

					// get unique instance name
					String name = JOptionPane
							.showInputDialog("Enter the instance's name:");
					while ((name != null) // entered a name
							&& ((MyCbr_Similarities_Tab.instance().getProject()
									.getKnowledgeBase().getInstance(name) != null) // an
																					// instance
																					// with
																					// that
																					// name
																					// already
																					// exists
							|| (name.length() == 0)) // entered an empty name
					) {
						name = JOptionPane
								.showInputDialog("Instance with that name already exists or name is empty! Enter another one.");
					}

					// if operation hasn't been canceled add instance
					if (name != null) {
						CaseDataRaw caseDataRaw = new CaseDataRawImpl(name,
								currentQuery.getModelCls().getName());
						for (ModelSlot slot : currentQuery.getModelCls()
								.listSlots()) {
							caseDataRaw.setSlotValue(slot.getName(),
									currentQuery.getSlotValue(slot).toString());
						}
						CBRProject.getInstance().confirmCaseNew(caseDataRaw);
						
					}
				}
			});
		}
		return buSaveQueryAsCase;
	}

	/**
	 * This method initializes buPrev
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getBuPrev() {
		if (buPrev == null) {
			buPrev = new JButton();
			buPrev.setText("Previous");
			buPrev.setToolTipText("Show Previous Rankings");
			buPrev.setMargin(new Insets(5, 5, 5, 5));
			buPrev.setIcon(ICON_PREVIOUS);
			buPrev.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (!columnsSorted) {
						tree.clearSelection();
						setDisplayFirstIndex(displayFirstIndex - 1);
					} else {
						setDisplayFirstIndexSorted(--currentIndexForColumnSorting);
					}
				}
			});
		}
		return buPrev;
	}

	/**
	 * This method initializes buNext
	 * 
	 * @return javax.swing.JButton
	 */
	private JButton getBuNext() {
		if (buNext == null) {
			buNext = new JButton();
			buNext.setText("Next");
			buNext.setToolTipText("Show Next Rankings");
			buNext.setMargin(new Insets(5, 5, 5, 5));
			buNext.setIcon(ICON_NEXT);
			buNext.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					if (!columnsSorted) {
						tree.clearSelection();
						setDisplayFirstIndex(displayFirstIndex + 1);
					} else {
						setDisplayFirstIndexSorted(++currentIndexForColumnSorting);
					}
				}
			});
		}
		return buNext;
	}

	public boolean isMultiple(int row) {
		Object o = ((DefaultMutableTreeNode) tree.getPathForRow(row)
				.getLastPathComponent()).getUserObject();
		if (!(o instanceof ModelSlot))
			return false;
		return ((ModelSlot) o).isMultiple();
	}

	private Comparator<ModelInstance> getComparatorAlphanumerical() {
		if (comparatorAlphanumerical == null) {
			comparatorAlphanumerical = new Comparator<ModelInstance>() {
				public int compare(ModelInstance o1, ModelInstance o2) {
					return ((ModelInstance) o1).getName().compareTo(
							((ModelInstance) o2).getName());
				}
			};
		}
		return comparatorAlphanumerical;
	}

	public void optionsChanged(ProjectOptions options) {
		log.fine("options changed");
		MAX_CASES_TO_DISPLAY = options.getCasesToDisplay();
		taFilter.setVisible(options.isShowFilters());
		taQueryWeights
				.setVisible(options.getWeightMode() != Query.WEIGHT_MODE_CLASS_ONLY);
		currentQuery.setWeightMode(options.getWeightMode());

		if (ranking == null) {
			clearSelections();
			clearLastRetrievalData();
			resetRankingTableModel();
			
		} else {
			setDisplayFirstIndex(0);
		}
		if (options.getExplanationOptions().isExplanationsEnabled()
				&& options.getExplanationOptions()
						.isConceptExplanationsEnabled()) {
			setConceptExplanationsVisible(true);
		} else {
			setConceptExplanationsVisible(false);
		}
	}

	public static void notifyWhenActiveSlotsChanged() {
		if ((RetrievalWidget.getInstance() != null)&&(RetrievalWidget.getInstance().tree != null)) {
			RetrievalWidget.getInstance().tree.updateUI();
		}
	}

}
