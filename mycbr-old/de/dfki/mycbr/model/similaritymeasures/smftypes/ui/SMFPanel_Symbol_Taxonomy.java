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

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Cursor;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.StringSelection;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Map;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ButtonGroup;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JToolTip;
import javax.swing.JTree;
import javax.swing.ToolTipManager;
import javax.swing.TransferHandler;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import de.dfki.mycbr.ValueAcceptorImpl;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Symbol_Taxonomy;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.Widget_Symmetry.SymmetryModeListener;
import de.dfki.mycbr.retrieval.ui.MultiLineTooltip;

/**
 * 
 * @author myCBR Team
 */
public class SMFPanel_Symbol_Taxonomy extends SMFPanel implements ActionListener, SymmetryModeListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(SMFPanel_Symbol_Taxonomy.class.getName());

	private SMF_Symbol_Taxonomy smf;
	
	Widget_Symmetry paSymmetry = null;
	
	ButtonGroup buttonGroupInnerNodes = new ButtonGroup();
	ButtonGroup buttonGroupSemanticInner = new ButtonGroup();
	ButtonGroup buttonGroupSemanticUncertain = new ButtonGroup();
	ButtonGroup buttonGroupInnerNodes2 = new ButtonGroup();
	ButtonGroup buttonGroupSemanticInner2 = new ButtonGroup();
	ButtonGroup buttonGroupSemanticUncertain2 = new ButtonGroup();
	
	private JPanel paContent = null;
	private JPanel paSettings = null;
	private JPanel paTaxonomy = null;
	private JLabel laQuery = null;
	private JLabel laCase = null;
	private JScrollPane scrollpane = null;
	private JTree tree = null;
	private JRadioButton rbInnerNodesYes = null;
	private JRadioButton rbInnerNodesNo = null;
	private JLabel txtInnerNodes = null;
	private JPanel spacer1 = null;
	private JLabel txtSemanticInner = null;
	private JRadioButton rbAnyValue = null;
	private JRadioButton rbUncertain = null;
	private JPanel spacer2 = null;
	private JRadioButton rbPessimistic = null;
	private JRadioButton rbOptimistic = null;
	private JRadioButton rbAverage = null;
	private JLabel txtSemanticUncertain = null;
	private JPanel spacer3 = null;
	private JRadioButton rbInnerNodesYes2 = null;
	private JRadioButton rbInnerNodesNo2 = null;
	private JRadioButton rbAnyValue2 = null;
	private JRadioButton rbUncertain2 = null;
	private JRadioButton rbPessimistic2 = null;
	private JRadioButton rbOptimistic2 = null;
	private JRadioButton rbAverage2 = null;
	
	private Collection<JRadioButton> allButtons = new ArrayList<JRadioButton>();
	
	private JPopupMenu popupMenu = null;
	private JMenuItem miConfirmTaxonomy = null;
	private JMenuItem miEditNode = null;
	
	private HelpButton buHelpInnerNodes = new HelpButton(HelpManager.KEY_SYMBOL_TAXONOMY_INNER_NODES);
	private HelpButton buHelpAnyValue = new HelpButton(HelpManager.KEY_SYMBOL_TAXONOMY_INNER_SEMANTIC);
	private HelpButton buHelpUncertainty = new HelpButton(HelpManager.KEY_SYMBOL_TAXONOMY_UNCERTAIN);

	public SMFPanel_Symbol_Taxonomy(SMF_Symbol_Taxonomy smf) {
		super(smf);
		this.smf = smf;

		log.fine("initialize SMFPanel_Symbol_Taxonomy."); //$NON-NLS-1$
		initialize();
		
		customInit();
	}
	
	private void customInit() {
		// add symmetry mode listeners
		paSymmetry.addSymmetryModeListener(this);
		
		buttonGroupInnerNodes.add(rbInnerNodesYes);
		buttonGroupInnerNodes.add(rbInnerNodesNo);
		
		buttonGroupSemanticInner.add(rbAnyValue);
		buttonGroupSemanticInner.add(rbUncertain);
		
		buttonGroupSemanticUncertain.add(rbPessimistic);
		buttonGroupSemanticUncertain.add(rbOptimistic);
		buttonGroupSemanticUncertain.add(rbAverage);

		buttonGroupInnerNodes2.add(rbInnerNodesYes2);
		buttonGroupInnerNodes2.add(rbInnerNodesNo2);
		
		buttonGroupSemanticInner2.add(rbAnyValue2);
		buttonGroupSemanticInner2.add(rbUncertain2);
		
		buttonGroupSemanticUncertain2.add(rbPessimistic2);
		buttonGroupSemanticUncertain2.add(rbOptimistic2);
		buttonGroupSemanticUncertain2.add(rbAverage2);
		
		allButtons.add(rbInnerNodesYes);
		allButtons.add(rbInnerNodesNo);
		allButtons.add(rbAnyValue);
		allButtons.add(rbUncertain);
		allButtons.add(rbPessimistic);
		allButtons.add(rbOptimistic);
		allButtons.add(rbAverage);

		allButtons.add(rbInnerNodesYes2);
		allButtons.add(rbInnerNodesNo2);
		allButtons.add(rbAnyValue2);
		allButtons.add(rbUncertain2);
		allButtons.add(rbPessimistic2);
		allButtons.add(rbOptimistic2);
		allButtons.add(rbAverage2);
		

		rbInnerNodesYes.setName	(SMF_Symbol_Taxonomy.KEY_QUERY + "," + SMF_Symbol_Taxonomy.KEY_HAS_INNER_VALUES); //$NON-NLS-1$
		rbInnerNodesNo.setName	(SMF_Symbol_Taxonomy.KEY_QUERY + "," + SMF_Symbol_Taxonomy.KEY_HAS_INNER_VALUES); //$NON-NLS-1$
		rbAnyValue.setName		(SMF_Symbol_Taxonomy.KEY_QUERY + "," + SMF_Symbol_Taxonomy.KEY_INNER_SEMANTIC); //$NON-NLS-1$
		rbUncertain.setName		(SMF_Symbol_Taxonomy.KEY_QUERY + "," + SMF_Symbol_Taxonomy.KEY_INNER_SEMANTIC); //$NON-NLS-1$
		rbPessimistic.setName	(SMF_Symbol_Taxonomy.KEY_QUERY + "," + SMF_Symbol_Taxonomy.KEY_UNCERTAIN); //$NON-NLS-1$
		rbOptimistic.setName	(SMF_Symbol_Taxonomy.KEY_QUERY + "," + SMF_Symbol_Taxonomy.KEY_UNCERTAIN); //$NON-NLS-1$
		rbAverage.setName		(SMF_Symbol_Taxonomy.KEY_QUERY + "," + SMF_Symbol_Taxonomy.KEY_UNCERTAIN); //$NON-NLS-1$

		rbInnerNodesYes2.setName(SMF_Symbol_Taxonomy.KEY_CASE + "," + SMF_Symbol_Taxonomy.KEY_HAS_INNER_VALUES); //$NON-NLS-1$
		rbInnerNodesNo2.setName	(SMF_Symbol_Taxonomy.KEY_CASE + "," + SMF_Symbol_Taxonomy.KEY_HAS_INNER_VALUES); //$NON-NLS-1$
		rbAnyValue2.setName		(SMF_Symbol_Taxonomy.KEY_CASE + "," + SMF_Symbol_Taxonomy.KEY_INNER_SEMANTIC); //$NON-NLS-1$
		rbUncertain2.setName	(SMF_Symbol_Taxonomy.KEY_CASE + "," + SMF_Symbol_Taxonomy.KEY_INNER_SEMANTIC); //$NON-NLS-1$
		rbPessimistic2.setName	(SMF_Symbol_Taxonomy.KEY_CASE + "," + SMF_Symbol_Taxonomy.KEY_UNCERTAIN); //$NON-NLS-1$
		rbOptimistic2.setName	(SMF_Symbol_Taxonomy.KEY_CASE + "," + SMF_Symbol_Taxonomy.KEY_UNCERTAIN); //$NON-NLS-1$
		rbAverage2.setName		(SMF_Symbol_Taxonomy.KEY_CASE + "," + SMF_Symbol_Taxonomy.KEY_UNCERTAIN); //$NON-NLS-1$
		
		
		rbInnerNodesYes.addActionListener(this);
		rbInnerNodesNo.addActionListener(this);
		rbAnyValue.addActionListener(this);
		rbUncertain.addActionListener(this);
		rbPessimistic.addActionListener(this);
		rbOptimistic.addActionListener(this);
		rbAverage.addActionListener(this);
		
		rbInnerNodesYes2.addActionListener(this);
		rbInnerNodesNo2.addActionListener(this);
		rbAnyValue2.addActionListener(this);
		rbUncertain2.addActionListener(this);
		rbPessimistic2.addActionListener(this);
		rbOptimistic2.addActionListener(this);
		rbAverage2.addActionListener(this);

		
		//
		// configure tree
		//

		refreshTreeModel();
		
        tree.getSelectionModel().setSelectionMode(TreeSelectionModel.DISCONTIGUOUS_TREE_SELECTION);
        tree.setDragEnabled(true);

        tree.setEditable(false);
        
        
        //
        // renderer for critical symbols. They need a different color.
        //
        tree.setCellRenderer(new DefaultTreeCellRenderer() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded, boolean leaf, int row, boolean hasFocus) {
				JLabel label = (JLabel)super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row, hasFocus);

				DefaultMutableTreeNode treeNode = (DefaultMutableTreeNode) value;
				String symbol = (String) treeNode.getUserObject();

				Color fgColor = (smf.isCriticalSymbol(symbol) ? Color.red : Color.black);
				label.setForeground(fgColor);

				if (!treeNode.isLeaf() && treeNode!=smf.getTaxonomy()) {
					Dimension size = label.getPreferredSize();

					String simTxt = " [" + smf.getSimilarityValue(symbol) + "]"; //$NON-NLS-1$ //$NON-NLS-2$
					String txt = symbol + simTxt;
					
					// commented out before 20.10.2008
//					int width = size.width + label.getFontMetrics(label.getFont()).charsWidth("[XXXX]".toCharArray(), 0, 5);
//					int width = label.getIconTextGap()+ label.getIcon().getIconWidth()*2 + label.getFontMetrics(label.getFont()).charsWidth(txt.toCharArray(), 0, txt.length()-1);
//					int width = label.getPreferredSize().width + label.getFontMetrics(label.getFont()).charsWidth(simTxt.toCharArray(), 0, simTxt.length()-1);
					
					int width = 10000;//label.getPreferredSize().width *2;
					label.setText(txt);
					label.setPreferredSize(new Dimension(width, size.height));
					label.revalidate();
				}
				
				// commented out before 20.10.2008
//				Dimension size = label.getPreferredSize();
//				log.info("label size = "+size);
//				label.setSize(new Dimension(size.width+20, size.height));
//				label.setPreferredSize(new Dimension(size.width+20, size.height));

				return label;
			}
		});
        
        
        //
        // drag & drop (improved)
        //
        
                
        //
        // drag & drop
        //
     // commented out before 20.10.2008
//        tree.addKeyListener(new KeyAdapter()
//		{
//			public void keyPressed(KeyEvent e)
//			{
//				if (e.getKeyCode()== KeyEvent.VK_ESCAPE && currentlyDraggedNodes!=null)
//				{
//					// cancel drag&drop
//					draggingAborted = true;
//					stopDragging();
//				}
//			}
//		});
//        
//        tree.addMouseMotionListener(new MouseMotionAdapter()
//		{
//			public void mouseDragged(MouseEvent e)
//			{
////				log.info("hey! mouse is pressing right button ["+e.getModifiersEx()+"]: "+(e.getModifiersEx()==MouseEvent.BUTTON3_DOWN_MASK));
//				if (e.getModifiersEx()==MouseEvent.BUTTON3_DOWN_MASK) return;
//
//				// if dragging has been canceled (e.g. by 'ESCAPE'), ignore dragging...
//				if (draggingAborted || !draggingEnabled) return;
//				
//				Rectangle rect = new Rectangle(e.getX(), e.getY(), 20, 20);
//				tree.scrollRectToVisible(rect);
//				// show position in tree while dragging ... more beautiful
//				if (currentlyDraggedNodes != null)
//				{
//					TreePath currentPath = tree.getClosestPathForLocation(e.getX(), e.getY());
//					tree.setSelectionPath(currentPath);
//					return;
//				}
//				log.fine("init currentlyDraggedNodes");
//
//				currentlyDraggedNodes = tree.getSelectionPaths();
//				if (currentlyDraggedNodes==null) return;
//				tree.setCursor(new Cursor(Cursor.HAND_CURSOR));
//			}
//			
//		});
//        
//		tree.addMouseListener(new MouseAdapter()
//		{
//			public void mouseClicked(MouseEvent e)
//			{
//				if (e.getButton()!=MouseEvent.BUTTON1) return;
//
//				if (e.getClickCount()>=2)
//				{
//					TreePath parentPath = tree.getPathForLocation(e.getX(), e.getY());
//
//					if (parentPath==null || parentPath.getPathCount()==0) return;
//					DefaultMutableTreeNode node = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
//					
//					if (node.isLeaf()) return;
//					
//					editNode(node);
//				}
//			}
//			
//			public void mousePressed(MouseEvent e)
//			{
//				// select item with right mouse button, too
//				if (e.getButton()==MouseEvent.BUTTON3)
//				{
//					tree.setSelectionPath(tree.getPathForLocation(e.getX(), e.getY()));
//				}
//				
//			}
//			
//			public void mouseReleased(MouseEvent e)
//			{
//				// if dragging has been aborted, reset flag.
//				draggingAborted= false;
//				if (e.getButton()!=MouseEvent.BUTTON1) return;
//				
//				if (currentlyDraggedNodes==null || !draggingEnabled) return;
//				
//				TreePath parentPath = tree.getClosestPathForLocation(e.getX(), e.getY());
//				if (parentPath == null)
//				{
//					stopDragging();
//					return;
//				}
//				log.fine("drop selected nodes here!");
//				
//				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
//				if (parent == null) return;
//				
////				double minSim = 1;
//				
//				log.fine("parent is not null. currentlyDraggedNodes.length==["+currentlyDraggedNodes.length+"]");
//				for (int i=0; i<currentlyDraggedNodes.length; i++)
//				{
//					TreePath path = currentlyDraggedNodes[i];
//					
//					DefaultMutableTreeNode node = (DefaultMutableTreeNode) path.getLastPathComponent();
//
//					
//					//tmp
//					TreeNode oldParent = node.getParent();
//					if (oldParent==null) continue;
//					int index = oldParent.getIndex(node);
//					
//					
//					// reorder nodes
//					if (!smf.moveNode(node, parent)) continue;
//					
////					minSim = Math.min(minSim, smf.getSimilarityValue((String)node.getUserObject()).doubleValue());
//
//					// notify listeners
//					((DefaultTreeModel)tree.getModel()).nodesWereInserted(parent, new int[]{0});
//					((DefaultTreeModel)tree.getModel()).nodesWereRemoved(oldParent, new int[]{index}, new Object[]{node});
//				
//					tree.expandPath(parentPath);
//					tree.expandPath(parentPath.pathByAddingChild(node));
//				}
//				
//				// clear
//				stopDragging();
//
//				// next two lines have been moved to smf.moveNode()
////				log.fine("grampa "+grampa+","+grampaSim+"   minsim "+minSim);
////				if (grampa!=null) smf.setSimilarityValue((String) parent.getUserObject(), new Double(Helper.roundSimValue((grampaSim+minSim)/2)));
//				
//				// refresh
//				revalidate();
//				repaint();
//			}
//		});

        
        tree.setShowsRootHandles(true);
		tree.addTreeExpansionListener(new TreeExpansionListener() {
			@SuppressWarnings("unchecked") //$NON-NLS-1$
			public void treeExpanded(TreeExpansionEvent e) {
				log.fine("Expand this one : [" + e.getPath() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
				for (Enumeration en = ((DefaultMutableTreeNode) e.getPath().getLastPathComponent()).children(); en.hasMoreElements();) {
					DefaultMutableTreeNode child = (DefaultMutableTreeNode) en.nextElement();
					tree.expandPath(e.getPath().pathByAddingChild(child));
				}
			}

			public void treeCollapsed(TreeExpansionEvent arg0) {
				// nothing to do
			}
		});
		
		tree.setComponentPopupMenu(getPopupMenu());
		tree.collapseRow(0);
		tree.expandRow(0);
        
        tree.setAutoscrolls(true);
        tree.setToggleClickCount(Integer.MAX_VALUE);
        
		//
		// set symmetry mode
		//
		paSymmetry.setSymmetrySelection(smf.isSymmetryMode());
		
		// and load GUI configuration
        refresh();
	}

	/**
	 * Stops dragging.
	 */
	protected void stopDragging() {
		tree.setCursor(Cursor.getDefaultCursor());
	}


	/**
	 * Edit node.
	 * @param node DefaultMutableTreeNode the node to edit.
	 */
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	protected void editNode(DefaultMutableTreeNode node) {
		log.fine("edit node [" + node.getUserObject() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		String symbol = (String) node.getUserObject();

		String result = JOptionPane.showInputDialog(this, Messages.getString("Enter_new_similarity_value"), Double.toString(smf.getSimilarityValue(symbol))); //$NON-NLS-1$ //$NON-NLS-2$
		
		log.fine("set new similarity value for symbol node [" + symbol + "]: [" + result + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		if (!ValueAcceptorImpl.getValueAcceptor_SimilarityValue().accept(result)) {
			return;
		}
		Double simVal = new Double(result);
		
		//
		// now check parent value
		//
		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
		if (parent!=null && smf.getSimilarityValue((String)parent.getUserObject()).doubleValue()>simVal.doubleValue()) {
			// similarity value of this node is smaller than similarity value of parent node
			JOptionPane.showMessageDialog(this, 
					String.format(Messages.getString("The_value__is_smaler_than__from_parent"), simVal, smf.getSimilarityValue((String)parent.getUserObject()))); //$NON-NLS-1$
			return;
		}
		
		//
		// now check childrens values
		//
		for (Enumeration en = node.children(); en.hasMoreElements();) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) en.nextElement();
			if (child.isLeaf()) continue;
			
			if (smf.getSimilarityValue((String)child.getUserObject()).doubleValue()<simVal.doubleValue()) {
				// similarity value of this node is greater than similarity value of child node
				JOptionPane.showMessageDialog(this, 
						String.format(Messages.getString("The_value__is_greater_than__from_child"), simVal, smf.getSimilarityValue((String)child.getUserObject()))); //$NON-NLS-1$
				return;
			}
		}
		
		smf.setSimilarityValue(symbol, simVal);
		
		revalidate();
		repaint();

	}


	public void actionPerformed(ActionEvent e) {
		if (e != null) {
			JRadioButton source = (JRadioButton)e.getSource();
			String[] keys = source.getName().split(","); //$NON-NLS-1$
			smf.configureInnerNodes(keys[0], keys[1], source.getText());
		}
		refresh();
		revalidate();
		repaint();
	}
	
	
	public void refreshTreeModel() {
		// init tree model
		DefaultMutableTreeNode root = smf.getTaxonomy();
		tree.setModel(new DefaultTreeModel(root, true));
		
		// expand every row
		for (int row=0; row < tree.getRowCount(); row++) {
			tree.expandRow(row);
			row++;
		}
	}

	public void refresh() {
		//
		// read data from smf and configure buttons.
		//
		for (Iterator<JRadioButton> it = allButtons.iterator(); it.hasNext();) {
			JRadioButton bu = it.next();
			String[] args = bu.getName().split(",");  //$NON-NLS-1$
			String value = smf.getConfigurationInnerNodes(args[0], args[1]);

			bu.setSelected(bu.getText().equals(value));
		}
		
		//
		// display / hide several components depending on configuration
		//
		boolean innerNodesYes = rbInnerNodesYes.isSelected(); {
			rbAnyValue.setEnabled(innerNodesYes);
			rbUncertain.setEnabled(innerNodesYes);
			txtSemanticInner.setEnabled(innerNodesYes);
		}

		boolean uncertain = rbUncertain.isSelected() && innerNodesYes; {
			rbPessimistic.setEnabled(uncertain);
			rbOptimistic.setEnabled(uncertain);
			rbAverage.setEnabled(uncertain);
			txtSemanticUncertain.setEnabled(uncertain);
		}
		
		boolean sym = smf.isSymmetryMode(); {
			laCase.setEnabled(!sym);
			laQuery.setEnabled(!sym);
			
			rbInnerNodesYes2.setEnabled(!sym);
			rbInnerNodesNo2.setEnabled(!sym);
			
			boolean innerNodesYes2 = rbInnerNodesYes2.isSelected(); {
				rbAnyValue2.setEnabled(!sym && innerNodesYes2);
				rbUncertain2.setEnabled(!sym && innerNodesYes2);
				txtSemanticInner.setEnabled(!sym && innerNodesYes2 || innerNodesYes);
			}

			boolean uncertain2 = rbUncertain2.isSelected() && innerNodesYes2; {
				rbPessimistic2.setEnabled(!sym && uncertain2);
				rbOptimistic2.setEnabled(!sym && uncertain2);
				rbAverage2.setEnabled(!sym && uncertain2);
				txtSemanticUncertain.setEnabled(!sym && uncertain2 || uncertain);
			}
		}
			
	}


	// ###########################
	//
	// GUI 
	// 
	// ###########################
	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private void initialize() {
		this.setLayout(new BorderLayout());
		this.setSize(474, 301);
		this.add(getPaSymmetry(), java.awt.BorderLayout.NORTH);
		this.add(getPaContent(), java.awt.BorderLayout.CENTER);
	}

	/**
	 * This method initializes paSymmetry	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPaSymmetry() {
		if (paSymmetry == null) {
			paSymmetry = new Widget_Symmetry();
		}
		return paSymmetry;
	}

	/**
	 * This method initializes paContent	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPaContent() {
		if (paContent == null) {
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 1;
			gridBagConstraints1.weightx = 6.0D;
			gridBagConstraints1.weighty = 1.0D;
			gridBagConstraints1.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints1.gridy = 0;
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints.weightx = 1.0D;
			gridBagConstraints.weighty = 1.0D;
			gridBagConstraints.gridy = 0;
			paContent = new JPanel();
			paContent.setLayout(new GridBagLayout());
			paContent.add(getPaSettings(), gridBagConstraints);
			paContent.add(getPaTaxonomy(), gridBagConstraints1);
		}
		return paContent;
	}

	/**
	 * This method initializes paSettings	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	public JPanel getPaSettings() {
		if (paSettings == null) {
			GridBagConstraints gridBagConstraints23 = new GridBagConstraints();
			gridBagConstraints23.gridx = 2;
			gridBagConstraints23.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints23.insets = new java.awt.Insets(0,5,5,5);
			gridBagConstraints23.gridy = 10;
			GridBagConstraints gridBagConstraints22 = new GridBagConstraints();
			gridBagConstraints22.gridx = 2;
			gridBagConstraints22.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints22.insets = new java.awt.Insets(0,5,0,5);
			gridBagConstraints22.gridy = 9;
			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 2;
			gridBagConstraints21.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints21.insets = new java.awt.Insets(5,5,0,5);
			gridBagConstraints21.gridy = 8;
			GridBagConstraints gridBagConstraints20 = new GridBagConstraints();
			gridBagConstraints20.gridx = 2;
			gridBagConstraints20.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints20.insets = new java.awt.Insets(0,5,5,5);
			gridBagConstraints20.gridy = 6;
			GridBagConstraints gridBagConstraints19 = new GridBagConstraints();
			gridBagConstraints19.gridx = 2;
			gridBagConstraints19.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints19.insets = new java.awt.Insets(5,5,0,5);
			gridBagConstraints19.gridy = 5;
			GridBagConstraints gridBagConstraints18 = new GridBagConstraints();
			gridBagConstraints18.gridx = 2;
			gridBagConstraints18.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints18.insets = new java.awt.Insets(0,5,5,5);
			gridBagConstraints18.gridy = 3;
			GridBagConstraints gridBagConstraints17 = new GridBagConstraints();
			gridBagConstraints17.gridx = 2;
			gridBagConstraints17.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints17.insets = new java.awt.Insets(5,5,0,5);
			gridBagConstraints17.gridy = 2;
			GridBagConstraints gridBagConstraints16 = new GridBagConstraints();
			gridBagConstraints16.gridx = 0;
			gridBagConstraints16.weighty = 1.0D;
			gridBagConstraints16.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints16.gridy = 11;
			GridBagConstraints gridBagConstraints15 = new GridBagConstraints();
			gridBagConstraints15.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints15.gridy = 8;
			gridBagConstraints15.weightx = 1.0;
			gridBagConstraints15.weighty = 0.0D;
			gridBagConstraints15.gridheight = 3;
			gridBagConstraints15.insets = new java.awt.Insets(5,5,5,5);
			gridBagConstraints15.gridx = 0;
			GridBagConstraints gridBagConstraints14 = new GridBagConstraints();
			gridBagConstraints14.gridx = 1;
			gridBagConstraints14.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints14.insets = new java.awt.Insets(0,5,5,5);
			gridBagConstraints14.gridy = 10;
			GridBagConstraints gridBagConstraints13 = new GridBagConstraints();
			gridBagConstraints13.gridx = 1;
			gridBagConstraints13.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints13.insets = new java.awt.Insets(0,5,0,5);
			gridBagConstraints13.gridy = 9;
			GridBagConstraints gridBagConstraints12 = new GridBagConstraints();
			gridBagConstraints12.gridx = 1;
			gridBagConstraints12.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints12.insets = new java.awt.Insets(5,5,0,5);
			gridBagConstraints12.gridy = 8;
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.gridx = 0;
			gridBagConstraints11.gridy = 7;
			GridBagConstraints gridBagConstraints10 = new GridBagConstraints();
			gridBagConstraints10.gridx = 1;
			gridBagConstraints10.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints10.insets = new java.awt.Insets(0,5,5,5);
			gridBagConstraints10.gridy = 6;
			GridBagConstraints gridBagConstraints9 = new GridBagConstraints();
			gridBagConstraints9.gridx = 1;
			gridBagConstraints9.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints9.insets = new java.awt.Insets(5,5,0,5);
			gridBagConstraints9.gridy = 5;
			GridBagConstraints gridBagConstraints8 = new GridBagConstraints();
			gridBagConstraints8.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints8.gridy = 5;
			gridBagConstraints8.weightx = 1.0;
			gridBagConstraints8.weighty = 0.0D;
			gridBagConstraints8.insets = new java.awt.Insets(5,5,5,5);
			gridBagConstraints8.gridheight = 2;
			gridBagConstraints8.gridx = 0;
			GridBagConstraints gridBagConstraints7 = new GridBagConstraints();
			gridBagConstraints7.gridx = 0;
			gridBagConstraints7.gridy = 4;
			GridBagConstraints gridBagConstraints6 = new GridBagConstraints();
			gridBagConstraints6.fill = java.awt.GridBagConstraints.BOTH;
			gridBagConstraints6.gridy = 2;
			gridBagConstraints6.weightx = 1.0;
			gridBagConstraints6.weighty = 0.0D;
			gridBagConstraints6.gridheight = 2;
			gridBagConstraints6.insets = new java.awt.Insets(5,5,5,5);
			gridBagConstraints6.gridx = 0;
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.gridx = 1;
			gridBagConstraints5.insets = new java.awt.Insets(0,5,5,5);
			gridBagConstraints5.anchor = java.awt.GridBagConstraints.NORTHWEST;
			gridBagConstraints5.gridy = 3;
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.gridx = 1;
			gridBagConstraints4.insets = new java.awt.Insets(5,5,0,5);
			gridBagConstraints4.anchor = java.awt.GridBagConstraints.WEST;
			gridBagConstraints4.gridy = 2;
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 2;
			gridBagConstraints3.insets = new java.awt.Insets(5,5,5,5);
			gridBagConstraints3.weightx = 0;//1.0D;
			gridBagConstraints3.anchor = java.awt.GridBagConstraints.SOUTH;
			gridBagConstraints3.gridy = 1;
			laCase = new JLabel();
			laCase.setText(Messages.getString("Case")); //$NON-NLS-1$
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 1;
			gridBagConstraints2.insets = new java.awt.Insets(5,5,5,5);
			gridBagConstraints2.weightx = 0;//1.0D;
			gridBagConstraints2.anchor = java.awt.GridBagConstraints.SOUTH;
			gridBagConstraints2.weighty = 0;//1.0D;
			gridBagConstraints2.gridy = 1;
			laQuery = new JLabel();
			laQuery.setText(Messages.getString("Query")); //$NON-NLS-1$
			paSettings = new JPanel();
			paSettings.setLayout(new GridBagLayout());
			paSettings.setEnabled(false);
			paSettings.add(getSpacer1(), gridBagConstraints7);
			paSettings.add(getSpacer2(), gridBagConstraints11);
			paSettings.add(getSpacer3(), gridBagConstraints16);
			
			JPanel p1 = new JPanel(new GridBagLayout());
			JPanel p2 = new JPanel(new GridBagLayout());
			JPanel p3 = new JPanel(new GridBagLayout());
			
			JPanel p11 = new JPanel(new GridBagLayout());
			JPanel p12 = new JPanel(new GridBagLayout());
			JPanel p21 = new JPanel(new GridBagLayout());
			JPanel p22 = new JPanel(new GridBagLayout());
			JPanel p31 = new JPanel(new GridBagLayout());
			JPanel p32 = new JPanel(new GridBagLayout());
			
			p11.add(getRbInnerNodesNo(), 		new GridBagConstraints(1,0, 1,1, 1d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,0,5), 0,0));
			p11.add(getRbInnerNodesYes(), 		new GridBagConstraints(1,1, 1,1, 1d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,0,5), 0,0));
			p12.add(getRbInnerNodesNo2(), 		new GridBagConstraints(2,0, 1,1, 1d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,0,5), 0,0));
			p12.add(getRbInnerNodesYes2(), 		new GridBagConstraints(2,1, 1,1, 1d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,0,5), 0,0));
			
			p21.add(getRbAnyValue(), 			new GridBagConstraints(1,0, 1,1, 1d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,0,5), 0,0));
			p21.add(getRbUncertain(), 			new GridBagConstraints(1,1, 1,1, 1d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,0,5), 0,0));
			p22.add(getRbAnyValue2(), 			new GridBagConstraints(2,0, 1,1, 1d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,0,5), 0,0));
			p22.add(getRbUncertain2(), 			new GridBagConstraints(2,1, 1,1, 1d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,0,5), 0,0));
			
			p31.add(getRbPessimistic(), 		new GridBagConstraints(1,0, 1,1, 0d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,0,5), 0,0));
			p31.add(getRbOptimistic(), 			new GridBagConstraints(1,1, 1,1, 1d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,0,5), 0,0));
			p31.add(getRbAverage(), 			new GridBagConstraints(1,2, 1,1, 1d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,0,5), 0,0));
			p32.add(getRbPessimistic2(),		new GridBagConstraints(2,0, 1,1, 1d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,0,5), 0,0));
			p32.add(getRbOptimistic2(), 		new GridBagConstraints(2,1, 1,1, 1d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,0,5), 0,0));
			p32.add(getRbAverage2(), 			new GridBagConstraints(2,2, 1,1, 1d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,5,0,5), 0,0));
			
			p11.setBorder(BorderFactory.createTitledBorder(Messages.getString("Query"))); //$NON-NLS-1$
			p12.setBorder(BorderFactory.createTitledBorder(Messages.getString("Case"))); //$NON-NLS-1$
			p21.setBorder(BorderFactory.createTitledBorder(Messages.getString("Query"))); //$NON-NLS-1$
			p22.setBorder(BorderFactory.createTitledBorder(Messages.getString("Case"))); //$NON-NLS-1$
			p31.setBorder(BorderFactory.createTitledBorder(Messages.getString("Query"))); //$NON-NLS-1$
			p32.setBorder(BorderFactory.createTitledBorder(Messages.getString("Case"))); //$NON-NLS-1$

			Dimension dim = p31.getPreferredSize();
			p11.setMinimumSize(new Dimension (dim.width, p11.getPreferredSize().height));
			p21.setMinimumSize(new Dimension (dim.width, p21.getPreferredSize().height));
			p31.setMinimumSize(new Dimension (dim.width, p31.getPreferredSize().height));
			p12.setMinimumSize(new Dimension (dim.width, p12.getPreferredSize().height));
			p22.setMinimumSize(new Dimension (dim.width, p22.getPreferredSize().height));
			p32.setMinimumSize(new Dimension (dim.width, p32.getPreferredSize().height));

			p11.setPreferredSize(new Dimension (dim.width, p11.getPreferredSize().height));
			p21.setPreferredSize(new Dimension (dim.width, p21.getPreferredSize().height));
			p31.setPreferredSize(new Dimension (dim.width, p31.getPreferredSize().height));
			p12.setPreferredSize(new Dimension (dim.width, p12.getPreferredSize().height));
			p22.setPreferredSize(new Dimension (dim.width, p22.getPreferredSize().height));
			p32.setPreferredSize(new Dimension (dim.width, p32.getPreferredSize().height));

			
			p1.add(getTxtInnerNodes(), 			new GridBagConstraints(0,0, 1,2, 1d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,15,5,15), 0,0));
			p1.add(p11,			 				new GridBagConstraints(1,0, 1,1, 0d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
			p1.add(p12,			 				new GridBagConstraints(2,0, 1,1, 0d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,0), 0,0));
			p1.add(buHelpInnerNodes, 			new GridBagConstraints(3,0, 1,2, 0d,0d, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,15,5,15), 0,0));
			
			p2.add(getTxtSemanticInner(),		new GridBagConstraints(0,0, 1,2, 1d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,15,5,15), 0,0));
			p2.add(p21,			 				new GridBagConstraints(1,0, 1,1, 0d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
			p2.add(p22,			 				new GridBagConstraints(2,0, 1,1, 0d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,0), 0,0));
			p2.add(buHelpAnyValue, 				new GridBagConstraints(3,0, 1,2, 0d,0d, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,15,5,15), 0,0));
			
			p3.add(getTxtSemanticUncertain(),	new GridBagConstraints(0,0, 1,3, 1d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,15,5,15), 0,0));
			p3.add(p31,			 				new GridBagConstraints(1,0, 1,1, 0d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0,0));
			p3.add(p32,			 				new GridBagConstraints(2,0, 1,1, 0d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,0), 0,0));
			p3.add(buHelpUncertainty, 			new GridBagConstraints(3,0, 1,3, 0d,0d, GridBagConstraints.CENTER, GridBagConstraints.NONE, new Insets(5,15,5,15), 0,0));
			
			paSettings.add(p1,	new GridBagConstraints(0,0, 4,1, 1d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
			paSettings.add(p2,	new GridBagConstraints(0,1, 4,1, 1d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
			paSettings.add(p3,	new GridBagConstraints(0,2, 4,1, 1d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
			
		}
		return paSettings;
	}

	/**
	 * This method initializes paTaxonomy	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getPaTaxonomy() {
		if (paTaxonomy == null) {
			paTaxonomy = new JPanel();
			paTaxonomy.setLayout(new BorderLayout());
			paTaxonomy.add(getScrollpane(), java.awt.BorderLayout.CENTER);
		}
		return paTaxonomy;
	}

	/**
	 * This method initializes scrollpane	
	 * 	
	 * @return javax.swing.JScrollPane	
	 */
	private JScrollPane getScrollpane() {
		if (scrollpane == null) {
			scrollpane = new JScrollPane();
			scrollpane.setViewportView(getTree());
		}
		return scrollpane;
	}

	/**
	 * This method initializes tree	
	 * 	
	 * @return javax.swing.JTree	
	 */
	public JTree getTree() {
		if (tree == null) {
			//
			// configure Tooltip text.
			//
			tree = new JTree() {
				/**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				public JToolTip createToolTip() {
					JToolTip toolTip = new MultiLineTooltip();
					return toolTip;
				}

				@SuppressWarnings("unchecked") //$NON-NLS-1$
				public String getToolTipText(MouseEvent e) {
					TreePath currentPath = tree.getPathForLocation(e.getX(), e.getY());
					if (currentPath == null) {
						return null;
					}

					DefaultMutableTreeNode node = (DefaultMutableTreeNode) currentPath.getLastPathComponent();
					if (node == null) {
						return null;
					}

					String symbol = (String) node.getUserObject();
						
					StringBuffer sb = new StringBuffer();
					Map criticalSimilarities_q_cb = smf.getCriticalSimilarities_QueryToCase();
					if (criticalSimilarities_q_cb.size() > 0) {
						Collection<Object> c = (Collection<Object>) criticalSimilarities_q_cb.get(symbol);
						if (c != null && c.size() > 0) {
							sb.append(String.format(Messages.getString("__as_query_value_critical_to__"), symbol, c.toString())); //$NON-NLS-1$
						}
					}
					Map<Object, Object> criticalSimilarities_cb_q = smf.getCriticalSimilarities_CaseToQuery();
					if (criticalSimilarities_cb_q.size() > 0) {
						Collection<Object> c = (Collection<Object>) criticalSimilarities_cb_q.get(symbol);
						if (c != null && c.size() > 0) {
							sb.append(String.format(Messages.getString("__as_case_value_critical_to__"), symbol, c.toString())); //$NON-NLS-1$
						}
					}
					
					return (sb.length() == 0 ? null : sb.toString());
				}
				
			};
			ToolTipManager.sharedInstance().registerComponent(tree);
			tree.setAutoscrolls(true);

			
			tree.setTransferHandler(new TransferHandler() {
			    /**
				 * 
				 */
				private static final long serialVersionUID = 1L;

				protected Transferable createTransferable(JComponent c) {
			    	int[] rows = tree.getSelectionRows();
			    	String str = ""; //$NON-NLS-1$
			    	for (int i=0; i<rows.length-1; i++) {
			    		str += Integer.toString(rows[i]) + ","; //$NON-NLS-2$
			    	}
			    	str += rows[rows.length-1];
			        return new StringSelection(str);
			    }
				
			    public int getSourceActions(JComponent c) {
			        return COPY_OR_MOVE;
			    }
			 				
			    public boolean importData(JComponent c, Transferable t) {
			        if (canImport(c, t.getTransferDataFlavors())) {
			            try {
			                String str = (String)t.getTransferData(DataFlavor.stringFlavor);
			                
			                if (tree.getSelectionPath() == null) {
			                	return false;
			                }
			                TreePath selectionPath = tree.getSelectionPath();
			                DefaultMutableTreeNode parent = (DefaultMutableTreeNode) selectionPath.getLastPathComponent();
			                
			                String rowsStr[] = str.split(","); //$NON-NLS-1$
//			                int[] rows = new int[rowsStr.length];
			                TreePath[] tp = new TreePath[rowsStr.length];
			                for (int i=0; i<rowsStr.length; i++) {
			                	int row = Integer.parseInt(rowsStr[i]);
			                	tp[i] = tree.getPathForRow(row);
			                }
			                
			                for (int i=0; i<tp.length; i++) {
			                	DefaultMutableTreeNode movedNode = (DefaultMutableTreeNode) tp[i].getLastPathComponent();
			                	DefaultMutableTreeNode oldParent = (DefaultMutableTreeNode) movedNode.getParent();
			                	int index = oldParent.getIndex(movedNode);

			                	if (!smf.moveNode(movedNode, parent)) continue;
			                	
			                	log.fine("move [" + movedNode.getUserObject() + "] to [" + parent.getUserObject() + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			                	((DefaultTreeModel)tree.getModel()).nodesWereInserted(parent, new int[]{0});
								((DefaultTreeModel)tree.getModel()).nodesWereRemoved(oldParent, new int[]{index}, new Object[]{movedNode});
								tree.expandPath(selectionPath);
								tree.expandPath(selectionPath.pathByAddingChild(movedNode));
			                }

//			                tree.expandPath(selectionPath);
			                
//			                importString(c, str);
							revalidate();
							repaint();
							
			                return true;
			            } catch (UnsupportedFlavorException ufe) {
			            } catch (IOException ioe) {
			            }
			        }

			        return false;
			    }

			    @Override
				public boolean canImport(JComponent comp, DataFlavor[] df) {
					return comp == tree;
				}
				
			});

			// commented out before 20.10.2008
// // remove all mouse listeners
//			MouseListener[] ml = tree.getMouseListeners();
//			for (int i=0; i<ml.length; i++)
//			{
//				tree.removeMouseListener(ml[i]);
//			}
		}
		return tree;
	}

	private JPopupMenu getPopupMenu() {
		if (popupMenu == null) {
			popupMenu = new JPopupMenu();
			
			miEditNode = new JMenuItem(Messages.getString("Edit_similarity")); //$NON-NLS-1$
			miEditNode.setEnabled(false);
			miEditNode.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					TreePath parentPath = tree.getSelectionPath();

					if (parentPath==null || parentPath.getPathCount()==0) {
						return;
					}
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
					
					if (node.isLeaf()) return;
					
					editNode(node);
				}
			});
			
			miConfirmTaxonomy = new JMenuItem(Messages.getString("Confirm_taxonomy")); //$NON-NLS-1$
			miConfirmTaxonomy.setEnabled(false);
			miConfirmTaxonomy.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent arg0) {
					TreePath parentPath = tree.getSelectionPath();
					if (parentPath==null || parentPath.getPathCount()==0) {
						return;
					}
					DefaultMutableTreeNode node = (DefaultMutableTreeNode) parentPath.getLastPathComponent();
					smf.removeCriticalSymbol((String) node.getUserObject());
					// recalculate table now!
					smf.getTable();
					tree.revalidate();
					tree.repaint();
				}
			});
			
			// commented out before 20.10.2008
//			miInsertInnerNode = new JMenuItem("Insert Inner Node");
//			miInsertInnerNode.addActionListener(new ActionListener()
//			{
//				public void actionPerformed(ActionEvent e)
//				{
//					TreePath tp = tree.getSelectionPath();
//					
//					DefaultMutableTreeNode selectedNode = null;
//					if (tp!=null) selectedNode = (DefaultMutableTreeNode) tp.getLastPathComponent();
//					
//					if (selectedNode==null) selectedNode=smf.getTaxonomy();
//					
//					String newSymbol = null;
//					boolean repeatInput = true;
//
//					while (repeatInput)
//					{
//						repeatInput=false;
//						newSymbol = JOptionPane.showInputDialog((Component)e.getSource(), "Enter name of inner symbol:", "Create new inner node", JOptionPane.OK_CANCEL_OPTION);
//						if (newSymbol==null) return;
//						if (((ModelSlot)smf.getModelInstance()).getAllowedValues().contains(newSymbol))
//						{
//							repeatInput=true;
//							JOptionPane.showMessageDialog((Component)e.getSource(), "This value already exists: ["+newSymbol+"]", "Cannot accept new symbol", JOptionPane.ERROR_MESSAGE);
//						}
//						else if (newSymbol.equals(""))
//						{
//							repeatInput=true;
//							JOptionPane.showMessageDialog((Component)e.getSource(), "This value is not allowed: ["+newSymbol+"]", "Cannot accept new symbol", JOptionPane.ERROR_MESSAGE);
//						}
//					}
//
//					// insert new symbol value in ModelSlot
//					smf.insertNewSymbol(newSymbol);
//					// then perform a consistency check.
////					MyCbrTab.instance().checkConsistency();
//					
//					log.fine("insert new symbol["+newSymbol+"] at ["+selectedNode.getUserObject()+"]");
//					
//
//					if (selectedNode==smf.getTaxonomy())
//					{
//						
//						Symbol_Sim_Tupel newTupel = smf.new Symbol_Sim_Tupel(newSymbol, 1);//((Symbol_Sim_Tupel)selectedNode.getUserObject()).getSimVal());
//						
//						DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newTupel);
//						selectedNode.insert(newNode, 0);
//
//						((DefaultTreeModel)tree.getModel()).nodeStructureChanged(selectedNode);
//					}
//					else
//					{
//						
//						DefaultMutableTreeNode parentNode = (DefaultMutableTreeNode) selectedNode.getParent(); 
//						
////						Symbol_Sim_Tupel newTupel = smf.new Symbol_Sim_Tupel(newSymbol, ((Symbol_Sim_Tupel)parentNode.getUserObject()).getSimVal());
//						Symbol_Sim_Tupel newTupel = smf.new Symbol_Sim_Tupel(newSymbol, ((Symbol_Sim_Tupel)selectedNode.getUserObject()).getSimVal());
//						
//						DefaultMutableTreeNode newNode = new DefaultMutableTreeNode(newTupel);
//
//						int index = parentNode.getIndex(selectedNode);
//						selectedNode.removeFromParent();
////						((DefaultTreeModel)tree.getModel()).nodesWereRemoved(parentNode, new int[]{});
//						
//						parentNode.insert(newNode, index);
//						newNode.insert(selectedNode, 0);
//						
//						((DefaultTreeModel)tree.getModel()).nodeStructureChanged(parentNode);
////						((DefaultTreeModel)tree.getModel()).nodesWereInserted(parent, new int[]{0});
//					}
//					
//					tree.collapseRow(0);
//					tree.expandRow(0);
//					
//				}
//			});
//
//			miRemoveNode = new JMenuItem("Remove Node");
//			miRemoveNode.addActionListener(new ActionListener()
//			{
//				public void actionPerformed(ActionEvent e)
//				{
//					TreePath tp = tree.getSelectionPath();
//					
//					DefaultMutableTreeNode selectedNode = null;
//					if (tp!=null) selectedNode = (DefaultMutableTreeNode) tp.getLastPathComponent();
//
//					if (selectedNode==null || selectedNode == smf.getTaxonomy()) return;
//					
//					smf.removeOldSymbol(((Symbol_Sim_Tupel)selectedNode.getUserObject()).getSymbol());
//					
//					DefaultMutableTreeNode parent = (DefaultMutableTreeNode)selectedNode.getParent();
//					int index = parent.getIndex(selectedNode);
//					selectedNode.removeFromParent();
//					
//					ArrayList al = new ArrayList();
//					for (Enumeration en=selectedNode.children(); en.hasMoreElements();)
//					{
//						al.add(en.nextElement());
//					}
//					for (Iterator it=al.iterator(); it.hasNext();)
//					{
//						DefaultMutableTreeNode child = (DefaultMutableTreeNode) it.next();
//						log.fine("put child ["+child.getUserObject()+"] to new parent ["+parent.getUserObject()+"]");
//						child.removeFromParent();
//						parent.insert(child, index++);
//					}
//					
//					((DefaultTreeModel)tree.getModel()).nodeStructureChanged(selectedNode);
//					tree.collapseRow(0);
//					tree.expandRow(0);
//					
//				}
//			});

					
			
			popupMenu.addPopupMenuListener(new PopupMenuListener() {

				public void popupMenuWillBecomeVisible(PopupMenuEvent e) {
					if (tree.getSelectionPath()==null || tree.getSelectionPath().getPathCount()==0) {
						miConfirmTaxonomy.setEnabled(false);
						miEditNode.setEnabled(false);
						return;
					}
					DefaultMutableTreeNode node = (DefaultMutableTreeNode)tree.getSelectionPath().getLastPathComponent();
					miConfirmTaxonomy.setEnabled(smf.isCriticalSymbol((String)node.getUserObject()));
					miEditNode.setEnabled(!node.isLeaf());
				}

				public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
					// nothing
				}

				public void popupMenuCanceled(PopupMenuEvent arg0) {
					// nothing
				}
			});

			popupMenu.add(miEditNode);
			popupMenu.add(miConfirmTaxonomy);
//			popupMenu.add(miInsertInnerNode);
//			popupMenu.add(miRemoveNode);
			
		}
		return popupMenu;
	}

	
	/**
	 * This method initializes rbInnerNodesYes	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbInnerNodesYes() {
		if (rbInnerNodesYes == null) {
			rbInnerNodesYes = new JRadioButton();
			rbInnerNodesYes.setText(SMF_Symbol_Taxonomy.VALUE_YES);
		}
		return rbInnerNodesYes;
	}

	/**
	 * This method initializes rbInnerNodesNo	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbInnerNodesNo() {
		if (rbInnerNodesNo == null) {
			rbInnerNodesNo = new JRadioButton();
//			rbInnerNodesNo.setSelected(true);
			rbInnerNodesNo.setText(SMF_Symbol_Taxonomy.VALUE_NO);
		}
		return rbInnerNodesNo;
	}

	/**
	 * This method initializes txtInnerNodes	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JComponent getTxtInnerNodes() {
		if (txtInnerNodes == null) {
//			txtInnerNodes = new JTextArea();
			txtInnerNodes = new JLabel();
			txtInnerNodes.setText(Messages.getString("<html>Inner_nodes_as_value")); //$NON-NLS-1$
			txtInnerNodes.setPreferredSize(new java.awt.Dimension(65,32));
//			txtInnerNodes.setEditable(false);
		}
		return txtInnerNodes;
	}

	/**
	 * This method initializes spacer1	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getSpacer1() {
		if (spacer1 == null) {
			spacer1 = new JPanel();
		}
		return spacer1;
	}

	/**
	 * This method initializes txtSemanticInner	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JComponent getTxtSemanticInner() {
		if (txtSemanticInner == null) {
//			txtSemanticInner = new JTextArea();
			txtSemanticInner = new JLabel();
//			txtSemanticInner.setLineWrap(false);
			txtSemanticInner.setPreferredSize(new java.awt.Dimension(65,32));
			txtSemanticInner.setText(Messages.getString("<html>Semantic_of_inner_nodes")); //$NON-NLS-1$
		}
		return txtSemanticInner;
	}

	/**
	 * This method initializes rbAnyValue	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbAnyValue() {
		if (rbAnyValue == null) {
			rbAnyValue = new JRadioButton();
//			rbAnyValue.setSelected(true);
			rbAnyValue.setText(SMF_Symbol_Taxonomy.VALUE_ANY_VALUE);
		}
		return rbAnyValue;
	}

	/**
	 * This method initializes rbUncertain	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbUncertain() {
		if (rbUncertain == null) {
			rbUncertain = new JRadioButton();
			rbUncertain.setText(SMF_Symbol_Taxonomy.VALUE_UNCERTAIN);
		}
		return rbUncertain;
	}

	/**
	 * This method initializes spacer2	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getSpacer2() {
		if (spacer2 == null) {
			spacer2 = new JPanel();
		}
		return spacer2;
	}

	/**
	 * This method initializes rbPessimistic	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbPessimistic() {
		if (rbPessimistic == null) {
			rbPessimistic = new JRadioButton();
//			rbPessimistic.setSelected(true);
			rbPessimistic.setText(SMF_Symbol_Taxonomy.VALUE_PESSIMISTIC);
		}
		return rbPessimistic;
	}

	/**
	 * This method initializes rbOptimistic	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbOptimistic() {
		if (rbOptimistic == null) {
			rbOptimistic = new JRadioButton();
			rbOptimistic.setText(SMF_Symbol_Taxonomy.VALUE_OPTIMISTIC);
		}
		return rbOptimistic;
	}

	/**
	 * This method initializes rbAverage	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbAverage() {
		if (rbAverage == null) {
			rbAverage = new JRadioButton();
			rbAverage.setText(SMF_Symbol_Taxonomy.VALUE_AVERAGE);
		}
		return rbAverage;
	}

	/**
	 * This method initializes txtSemanticUncertain	
	 * 	
	 * @return javax.swing.JTextArea	
	 */
	private JComponent getTxtSemanticUncertain() {
		if (txtSemanticUncertain == null) {
//			txtSemanticUncertain = new JTextArea();
			txtSemanticUncertain = new JLabel();
			txtSemanticUncertain.setText(Messages.getString("<html>Semantic_of_uncertain")); //$NON-NLS-1$
		}
		return txtSemanticUncertain;
	}

	/**
	 * This method initializes spacer3	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	private JPanel getSpacer3() {
		if (spacer3 == null) {
			spacer3 = new JPanel();
		}
		return spacer3;
	}

	/**
	 * This method initializes jRadioButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbInnerNodesYes2() {
		if (rbInnerNodesYes2 == null) {
			rbInnerNodesYes2 = new JRadioButton();
			rbInnerNodesYes2.setText(SMF_Symbol_Taxonomy.VALUE_YES);
		}
		return rbInnerNodesYes2;
	}

	/**
	 * This method initializes jRadioButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbInnerNodesNo2() {
		if (rbInnerNodesNo2 == null) {
			rbInnerNodesNo2 = new JRadioButton();
//			rbInnerNodesNo2.setSelected(true);
			rbInnerNodesNo2.setText(SMF_Symbol_Taxonomy.VALUE_NO);
		}
		return rbInnerNodesNo2;
	}

	/**
	 * This method initializes jRadioButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbAnyValue2() {
		if (rbAnyValue2 == null) {
			rbAnyValue2 = new JRadioButton();
//			rbAnyValue2.setSelected(true);
			rbAnyValue2.setText(SMF_Symbol_Taxonomy.VALUE_ANY_VALUE);
		}
		return rbAnyValue2;
	}

	/**
	 * This method initializes jRadioButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbUncertain2() {
		if (rbUncertain2 == null) {
			rbUncertain2 = new JRadioButton();
			rbUncertain2.setText(SMF_Symbol_Taxonomy.VALUE_UNCERTAIN);
		}
		return rbUncertain2;
	}

	/**
	 * This method initializes jRadioButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbPessimistic2() {
		if (rbPessimistic2 == null) {
			rbPessimistic2 = new JRadioButton();
//			rbPessimistic2.setSelected(true);
			rbPessimistic2.setText(SMF_Symbol_Taxonomy.VALUE_PESSIMISTIC);
		}
		return rbPessimistic2;
	}

	/**
	 * This method initializes jRadioButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbOptimistic2() {
		if (rbOptimistic2 == null) {
			rbOptimistic2 = new JRadioButton();
			rbOptimistic2.setText(SMF_Symbol_Taxonomy.VALUE_OPTIMISTIC);
		}
		return rbOptimistic2;
	}

	/**
	 * This method initializes jRadioButton	
	 * 	
	 * @return javax.swing.JRadioButton	
	 */
	private JRadioButton getRbAverage2() {
		if (rbAverage2 == null) {
			rbAverage2 = new JRadioButton();
			rbAverage2.setText(SMF_Symbol_Taxonomy.VALUE_AVERAGE);
		}
		return rbAverage2;
	}

	/**
	 * Switch symmetryMode.
	 * @see de.dfki.mycbr.model.similaritymeasures.smftypes.ui.Widget_Symmetry.SymmetryModeListener
	 */
	public boolean setSymmetryMode(boolean symmetryMode) {
		boolean applied = smf.setSymmetryMode(symmetryMode);
		
		if (applied) {
			refresh();
			revalidate();
			repaint();
		}
		
		return applied;
	}

	public void setDraggingEnabled(boolean draggingEnabled) {
	}

}
