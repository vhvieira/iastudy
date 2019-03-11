package de.dfki.mycbr.retrieval.ui;

import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.util.Enumeration;
import java.util.HashSet;

import javax.swing.ButtonModel;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.SwingConstants;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TreeExpansionEvent;
import javax.swing.event.TreeExpansionListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.model.vocabulary.ModelSlot;

/**
 * Represents the slots underlying tree in the retrieval widget
 * 
 * @author myCBR
 *
 */
public class SlotTreeView extends JTree {

	private static Icon ICON_TREE_CLOSED 			= null;
	private static Icon ICON_TREE_OPENED 			= null;
	private static Icon ICON_TREE_LEAF 	 			= null;
	
	public static Icon ICON_SORT_HORIZ				= null;
	
	private int preferredTreeCellWidth = 10;
	private int iconWidth = 20;
	private HashSet<ModelInstance> expandedNodes = new HashSet<ModelInstance>();
	
	{
		try
		{
			ICON_TREE_CLOSED 			= new ImageIcon(RetrievalWidget.class.getResource("plus.png"));
			ICON_TREE_OPENED 			= new ImageIcon(RetrievalWidget.class.getResource("minus.png"));
			ICON_TREE_LEAF   			= new ImageIcon(RetrievalWidget.class.getResource("leaf.png"));
			
			ICON_SORT_HORIZ		 		= new ImageIcon(RetrievalWidget.class.getResource("arrowHoriz.png"));


		}
		catch (Throwable ex){ }
	}
	
	private static final long serialVersionUID = 1L;
	private DefaultMutableTreeNode currentRoot;
	private JTable table;
	
	public SlotTreeView(DefaultMutableTreeNode currentRoot, JTable table) {
		this.currentRoot = currentRoot;
		getSelectionModel().setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		this.table = table;
		setShowsRootHandles(false);
		
		setCellRenderer(getTreeCellRenderer());
		addTreeExpansionListener(new TreeExpansionListener()
		{
			public void treeCollapsed(TreeExpansionEvent e) {
				SlotTreeView.this.findPreferredTreeCellWidth( );
				expandedNodes.remove((ModelInstance)((DefaultMutableTreeNode) e.getPath().getLastPathComponent()).getUserObject());
				SlotTreeView.this.table.tableChanged(new TableModelEvent(SlotTreeView.this.table.getModel(), TableModelEvent.HEADER_ROW));
			}

			public void treeExpanded(TreeExpansionEvent e) {
				SlotTreeView.this.findPreferredTreeCellWidth( );
				expandedNodes.add((ModelInstance)((DefaultMutableTreeNode) e.getPath().getLastPathComponent()).getUserObject());
				SlotTreeView.this.table.tableChanged(new TableModelEvent(SlotTreeView.this.table.getModel(), TableModelEvent.HEADER_ROW));				
			}
		});
	
	}

	public TreeCellRenderer getTreeCellRenderer()
	{
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer()
		{
			private static final long serialVersionUID = 1L;
			JPanel panel = new JPanel(new GridBagLayout());
			JLabel lab = new JLabel();
			JLabel laIcon = new JLabel(ICON_SORT_HORIZ);
			int lastWidth = -1;
			
			JButton button = new JButton(">");
			ButtonModel buttonModel;
			
			{
				panel.add(lab, 		new GridBagConstraints(0,0, 1,1, 0.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
				panel.add(button, 	new GridBagConstraints(1,0, 1,1, 1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
				
				panel.add(laIcon, 	new GridBagConstraints(2,0, 1,1, 0.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
				laIcon.setVisible(false);
				
				lab.setBackground(getBackground());
				lab.setForeground(getForeground());
				button.setHorizontalAlignment(SwingConstants.LEFT);
				buttonModel = button.getModel();
			}
			

			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean selected, 
			                                              boolean expanded, boolean leaf, int row, boolean hasFocus) {
				DefaultMutableTreeNode node = (DefaultMutableTreeNode) value;
				TreeNode[] path = node.getPath();
				
				JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, selected, expanded, leaf, row, hasFocus);
				label.setToolTipText(label.getText());
				lab.setIcon(label.getIcon());
				lab.setToolTipText(label.getText());
				iconWidth = lab.getIcon().getIconWidth()+lab.getIconTextGap()+0;
				int currentWidth = preferredTreeCellWidth - ((path.length-2) * iconWidth);
				if (currentWidth!=lastWidth)
				{
					panel.setPreferredSize(new Dimension(currentWidth, panel.getPreferredSize().height));
					lastWidth = currentWidth;
				}
				
				button.setText(label.getText());
				buttonModel.setArmed(selected);
				laIcon.setVisible(selected);
				buttonModel.setPressed(selected);
				return panel;
			}
		};
		if (ICON_TREE_CLOSED!=null) renderer.setClosedIcon(ICON_TREE_CLOSED);
		if (ICON_TREE_OPENED!=null) renderer.setOpenIcon(ICON_TREE_OPENED);
		if (ICON_TREE_LEAF  !=null) renderer.setLeafIcon(ICON_TREE_LEAF);
		
		return renderer;
	}
	
	@SuppressWarnings("unchecked")
	public void findPreferredTreeCellWidth() {
		
		JLabel label = new JLabel();
		int max = -1;
		if (currentRoot==null) {
			return;
		}
		for (Enumeration< DefaultMutableTreeNode > en=currentRoot.breadthFirstEnumeration(); en.hasMoreElements();)
		{
			DefaultMutableTreeNode node = en.nextElement();
			
			DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
			if (parent != null && isCollapsed(new TreePath(parent.getPath()))) continue;
			
			Object o = node.getUserObject();
			if (!(o instanceof ModelSlot)) continue;
			
			String name = ((ModelInstance) o).getName();
			label.setText(name);
			int width = label.getPreferredSize().width + (iconWidth * (node.getPath().length-2));
			if (max<width) max = width;
		}
		preferredTreeCellWidth = max + 150;
		setCellRenderer( getTreeCellRenderer() );
	}
	
	public boolean isMultiple(int row) {
		Object o = ((DefaultMutableTreeNode) getPathForRow(row).getLastPathComponent()).getUserObject();
		if (!(o instanceof ModelSlot)) return false;
		return ((ModelSlot) o).isMultiple();
	}

	public void setNewRoot(DefaultMutableTreeNode newRoot) {
		currentRoot = newRoot;
		int tmpSelectedIndex = ((getSelectionRows()==null || getSelectionRows().length==0)? -1: getSelectionRows()[0]);
		setModel(new DefaultTreeModel(currentRoot));
		setSelectionRow(tmpSelectedIndex);
		setRootVisible(false);
		
		// expand all tree nodes that have been expanded before.
		for (int row=0; row<getRowCount(); row++)
		{
			TreePath path = getPathForRow(row);
			ModelInstance mi = (ModelInstance) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
			if (expandedNodes.contains(mi))
			{
				expandPath(path);
			}
		}
	}
	
	public void expandTreeNodes(HashSet< Object > expandedSlots)
	{
		for (int i=0; i<getRowCount(); i++)
		{
			Object o = ((DefaultMutableTreeNode) getPathForRow(i).getLastPathComponent()).getUserObject();
			if (expandedSlots.contains(o)) expandRow(i);
		}
	}
	
	public int getIconWidth() {
		return iconWidth;
	}

}