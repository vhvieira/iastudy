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

import java.awt.BorderLayout;
import java.awt.event.ComponentEvent;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.File;
import java.net.URL;
import java.util.Collection;
import java.util.Collections;
import java.util.Vector;
import java.util.logging.ConsoleHandler;
import java.util.logging.Level;
import java.util.logging.LogRecord;
import java.util.logging.Logger;
import java.util.logging.SimpleFormatter;

import javax.swing.Action;
import javax.swing.FocusManager;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;

import de.dfki.mycbr.CBRProject;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.modelprovider.ModelProvider;
import de.dfki.mycbr.modelprovider.protege.ModelClsProtege;
import de.dfki.mycbr.modelprovider.protege.MyCBRMenu;
import edu.stanford.smi.protege.event.FrameEvent;
import edu.stanford.smi.protege.event.FrameListener;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Instance;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.resource.ResourceKey;
import edu.stanford.smi.protege.ui.ClsesPanel;
import edu.stanford.smi.protege.ui.HeaderComponent;
import edu.stanford.smi.protege.ui.ParentChildNode;
import edu.stanford.smi.protege.ui.SlotSubslotRoot;
import edu.stanford.smi.protege.ui.SubclassPane;
import edu.stanford.smi.protege.ui.SubslotPane;
import edu.stanford.smi.protege.util.CreateAction;
import edu.stanford.smi.protege.util.SelectableTree;
import edu.stanford.smi.protege.util.SelectionEvent;

/**
 * @author myCBR Team
 *
 * The main component of the CbrTool Plug in 'Similarity Modeling'. 
 */
public class MyCbr_Similarities_Tab extends edu.stanford.smi.protege.widget.AbstractTabWidget implements edu.stanford.smi.protege.util.SelectionListener, java.awt.event.ComponentListener, FrameListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	//private final static Logger log = Logger.getLogger(MyCbr_Similarities_Tab.class.getName());
	
	public static final String 		TAB_LABEL = Messages.getString("Similarity_measure_editor"); //$NON-NLS-1$

	public static ImageIcon 		LOGO_MYCBR_BIG = null;
	public static ImageIcon 		ICON_MYCBR = null;
	{
		try {
			ICON_MYCBR 		= new ImageIcon(MyCbr_Similarities_Tab.class.getResource("logo_mycbr.png")); //$NON-NLS-1$
			LOGO_MYCBR_BIG 	= new ImageIcon(MyCbr_Similarities_Tab.class.getResource("logo_mycbr_big.png")); //$NON-NLS-1$
		} catch (Throwable ex) {
			// ignore
		}
	}

	/**
	 * static instance of the plugin main component
	 */
	private static MyCbr_Similarities_Tab instance = null;

	private JSplitPane splitpaneLeftRight = new JSplitPane();

	private JSplitPane splitpaneUpDown = new JSplitPane(JSplitPane.VERTICAL_SPLIT);

	private BorderLayout borderLayout1 = new BorderLayout();

	private SubslotPane paSlots;
	private ClsesPanel paClasses;

	private JPanel paContentLeft = new JPanel();

	private MainPanel paMain = new MainPanel();
	
	private Cls currentCls;
	private Slot currentSlot;
	private Instance selectedInstance;
	

	public MyCbr_Similarities_Tab() {
		addComponentListener(this);
	}


	/**
	 * Initializes the whole plug in. This is called during Protege startup.
	 * Inherited from AbstractTabWidget.
	 * 
	 */
	public void initialize() {

		CBRProject.getInstance(this);
		setLabel(TAB_LABEL);
		if (ICON_MYCBR != null) {
			setIcon(ICON_MYCBR);
		}

		// static instance
		instance = this;

		//
		// NOTE: somehow, the notification about a deletion of this instance will NOT be submitted.
		// So, I have to work around this problem. There is a checking during the save() method.
		//
		if (getProject().getKnowledgeBase() == null) {
		//	log.info("NOTE: COULD NOT ADD FRAMELISTENER TO PROJECT [" + getProject().getName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
		} else {
			getProject().getKnowledgeBase().addFrameListener(this);
//			getProject().getKnowledgeBase().addKnowledgeBaseListener(this);
		}

		//
		addExceptionPopupLogger();			
		
		this.setLayout(borderLayout1);
		splitpaneLeftRight.setContinuousLayout(true);
		this.add(splitpaneLeftRight, BorderLayout.CENTER);
		splitpaneUpDown.setContinuousLayout(true);

		paClasses = new ClsesPanel(getProject()) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			protected void createPanes() {
				super.createPanes();
				JTree tree = (JTree) _subclassPane.getSelectable();
				tree.getModel().addTreeModelListener(new TreeModelListener(){

					public void treeNodesChanged(TreeModelEvent arg0) {
					}

					public void treeNodesInserted(TreeModelEvent e) {
						if (e.getChildren().length>0 && e.getChildren()[0] instanceof ParentChildNode) {
							// select new class (if it was the first one)
							// this is just for convenience
//							Collection allModelCls = cbrProject.getAllModelCls();
//							if (allModelCls.size()!=1) return;
							Cls cls = (Cls) ((ParentChildNode) e.getChildren()[0]).getUserObject();
							setSelectedCls(cls);
						}
					}

					public void treeNodesRemoved(TreeModelEvent arg0) {
					}

					public void treeStructureChanged(TreeModelEvent e) {
					}
				});
				tree.addMouseListener(new MouseAdapter() {
					public void mousePressed(MouseEvent e) {
						selectCls();
					}
				});
			}
			
			@SuppressWarnings("unchecked") //$NON-NLS-1$
			protected void selectCls() {
				Collection selectionCol = paClasses.getSelection();
				if (selectionCol==null || selectionCol.size()==0) {
					return;
				}
				Instance selectedObj = (Instance) selectionCol.iterator().next();

				// this is why we do all the effort:
				// we want to notify our listeners even when selection didn't change
				// but has been triggered again.
				if (currentCls == selectedObj) {
					notifySelectionListeners();
				}
			}
			
			protected HeaderComponent createClsBrowserHeader() {
				HeaderComponent hc = super.createClsBrowserHeader();
//				hc.setColor(Colors.getSlotColor());
				return hc;
			}
		};
		paClasses.addSelectionListener(this);
		if (!paClasses.getSelection().isEmpty()) {
			currentCls = (Cls) paClasses.getSelection().iterator().next(); 
		}
		
		// paSlots = new SlotsPanel(getProject(), this);
		paSlots = new SubslotPane(getProject()) {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			protected Action getCreateAction() {
				//
				// Since the SlotBrowser of this tab lists only those slots of the selected class of paClasses
				// the creation of a slot implies that its domain is the currently selected class.
				// So, here we set the domain of the newly created slot.
				//
		        return new CreateAction(ResourceKey.SLOT_CREATE) {
		            /**
					 * 
					 */
					private static final long serialVersionUID = 1L;

					public void onCreate() {
		                Slot slot = getProject().getKnowledgeBase().createSlot(null);
		                if (currentCls != null) {
		                	currentCls.addDirectTemplateSlot(slot);
		                }
		                paClasses.notifySelectionListeners();
		            }
		        };
			}
		};
		
		JTree internalTreeSlots = (JTree) paSlots.getSelectable();
		internalTreeSlots.addMouseListener(new MouseAdapter() {
			@SuppressWarnings("unchecked") //$NON-NLS-1$
			public void mousePressed(MouseEvent e) {
				Collection selectionCol = paSlots.getSelection();
				if (selectionCol == null || selectionCol.size() == 0) {
					return;
				}
				Instance selectedObj = (Instance) selectionCol.iterator().next();

				// this is why we do all the effort:
				// we want to notify our listeners even when selection didn't
				// change but has been triggered again.
				//
				if (currentSlot == selectedObj) {
					paSlots.notifySelectionListeners();
				}
			}
		});
		internalTreeSlots.getModel().addTreeModelListener(new TreeModelListener() {
			public void treeNodesInserted(TreeModelEvent arg0) {
				//
				// if a slot has been created somewhere else
				// it may have been added to this tree model.
				// But want only those slots listed here that belong to the selected class
				//
				paClasses.notifySelectionListeners();
			}
			public void treeNodesChanged(TreeModelEvent arg0) {
			}
			public void treeNodesRemoved(TreeModelEvent arg0) {
			}
			public void treeStructureChanged(TreeModelEvent arg0) {
			}
		});
		
		paSlots.addSelectionListener(this);
		if (!paSlots.getSelection().isEmpty()) {
			currentSlot = (Slot) paSlots.getSelection().iterator().next(); 
		}

		splitpaneLeftRight.add(paContentLeft, JSplitPane.LEFT);
		splitpaneLeftRight.add(paMain, JSplitPane.RIGHT);
		splitpaneLeftRight.setDividerLocation(230);

		paContentLeft.setLayout(new BorderLayout());
		paContentLeft.add(splitpaneUpDown, BorderLayout.CENTER);

		splitpaneUpDown.add(paClasses, JSplitPane.TOP);
		splitpaneUpDown.add(paSlots, JSplitPane.BOTTOM);
		splitpaneUpDown.setDividerLocation(230);

		// icon
		URL url = getClass().getResource("../../../../img/cbr_icon.PNG"); //$NON-NLS-1$
		if (url != null) {
			Icon icon = new ImageIcon(url);
			setIcon(icon);
		}

		addFocusListener(new FocusAdapter() {
			public void focusGained(FocusEvent arg0) {
				refresh();
			}
		});

		refresh();
//		
//		addAncestorListener(new AncestorListener() {
//			boolean done = false;
//			
//			public void ancestorAdded(AncestorEvent e) {
//				if (done) {
//					return;
//				}
//				Container c = e.getAncestor();
//				if (((JComponent) c).getTopLevelAncestor() instanceof Window) {
//					//log.fine("Window ancester added. Now add windowAdapter to this"); //$NON-NLS-1$
//					done = true;
//					((Window)((JComponent) c).getTopLevelAncestor()).addWindowListener(new WindowAdapter() {
//						private long lastTime = -1;
//						public void windowActivated(WindowEvent arg0) {
//							long now = System.currentTimeMillis();
//							if ((now-lastTime) >= 5000) {
//								checkConsistency();
//								lastTime = System.currentTimeMillis();
//							}
//						}
//						
//					});
//				}
//			}
//
//			public void ancestorRemoved(AncestorEvent arg0) {}
//			public void ancestorMoved(AncestorEvent arg0) {}
//		});
		
		
		// add menu entry
        MyCBRMenu.createInstance(getProject());
		MyCBRMenu.addMenuTo(getMainWindowMenuBar());
		
		// set current cls
		Collection<ModelCls> allModelCls = CBRProject.getInstance().getAllModelCls();
		Cls cls = (allModelCls.size() == 0 ? null : ((ModelClsProtege)allModelCls.iterator().next()).getProtegeCls());
		if (cls != null) {
			((SubclassPane) paClasses.getSelectable()).setSelectedCls(cls);
		}
		setCurrentCls(cls);
		
	}

	/**
	 * This will be called when the user double clicks either a class from the
	 * ClassesPanel or a Slot from the SlotsPanel. Then we want the right panel
	 * (MainPanel) to show our CBR data for it.
	 */
	@SuppressWarnings("unchecked") //$NON-NLS-1$
	public void selectionChanged(SelectionEvent event) {
		Collection selection = event.getSelectable().getSelection();
		//log.fine("SELECTION CHANGED " + selection); //$NON-NLS-1$
		if (selection.isEmpty()) {
			return;
		}
		
		select((Instance) selection.iterator().next());
	}

	private void select(Instance inst) {
		// get selected instance
		selectedInstance = inst;
		ModelInstance tmp = null;
		if (inst != null) {
			tmp = ModelProvider.getInstance().getModelInstance(inst.getName());
		}

		paMain.select(tmp);
		if (tmp != null) {
			if (inst instanceof Cls) {
				setCurrentCls((Cls)inst);
			} else {
				setCurrentSlot((Slot)inst);
			}
			
			// update GUI
			refresh();
		}
	}


	/**
	 * Updates the GUI. It calls all containing components to refresh (as
	 * needed).
	 */
	public void refresh() {
		//log.fine("refreshing cbrToolTab"); //$NON-NLS-1$
		paMain.refresh();
	}


	public static MyCbr_Similarities_Tab instance() {
		if (instance == null) {
			//log.fine("cbrtab instance is null !"); //$NON-NLS-1$
		}
		return instance;
	}

	/**
	 * Closes the currently active tab
	 * 
	 * @return true if tab has been closed.
	 */
	public boolean closeActiveTab() {
		return paMain.closeActiveTab();
	}

	// do nothing
	public void componentHidden(ComponentEvent e) {}
	public void componentMoved(ComponentEvent e) {}
	public void componentResized(ComponentEvent e) {}
	
	public void componentShown(ComponentEvent e) {
		checkConsistency();
	}

	public void checkConsistency() {
		if (CBRProject.getInstance() == null) {
			return;
		}
			
		if (selectedInstance!=null && selectedInstance.isDeleted()) {

			if (selectedInstance==currentCls) {
				setCurrentCls(null);
			}
			if (selectedInstance==currentSlot) {
				setCurrentSlot(null);
			}
			select(null);
			return;
		}
		
		if (selectedInstance instanceof Slot) {
			if (currentCls != null && !currentCls.getDirectTemplateSlots().contains(selectedInstance)) {
				setCurrentSlot(null);
				setCurrentCls(currentCls);
				select(null);
			}
		}

		if (paMain!=null) {
			paMain.checkConsistency();
		}

	}

	
	@Override
	public void dispose() {
		super.dispose();
		MyCBRMenu.removeMenuFrom(getMainWindowMenuBar());
	}

	/**
	 * Implemented from Protege fw.
	 * Protege calls this method before save().
	 * Checks all opened smfunctions whether they have been changed or not. 
	 */
	public boolean canSave() {
		return true;
	}

	/**
	 * Saves all data.
	 * Method canSave() has been called before this. So we dont have to check
	 * for problems that might occure.
	 */
	public void save() {
			FocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
			
			String projectDir = new File(getProject().getProjectDirectoryURI()).getAbsolutePath();
			String projectName = getProject().getName();
			CBRProject.getInstance().save(projectName, projectDir);
		
	}

	/**
	 * Maximizes/Collapses editor component.
	 */
	public void toggleMaximizeSize() {
		if (!paContentLeft.isVisible()) {
			paContentLeft.setVisible(true);
			splitpaneLeftRight.setDividerLocation((int)paContentLeft.getSize().getWidth());
			return;
		}
		paContentLeft.setVisible(false);
	}

	@SuppressWarnings("unchecked") //$NON-NLS-1$
	private void setCurrentCls(Cls cls) {
		currentCls = cls;
		if (cls != null) {
			SelectableTree tree = (SelectableTree) paSlots.getSelectable();
			Vector slots = new Vector(cls.getVisibleTemplateSlots());
			Collections.sort(slots);
			tree.setRoot(new SlotSubslotRoot(getKnowledgeBase(), slots));
			tree.revalidate();
		}
		try {
			//
			// Modify some part of the slotsPanel
			// this code may be a problem, if the implementation of protege changes in future.
			// Probably, this piece of code remains safe.
			//
			HeaderComponent headerComp = (HeaderComponent)paSlots.getComponent(0);
			headerComp.setComponentLabel(Messages.getString("For_class")); //$NON-NLS-1$
			String clsName = (cls==null? "": cls.getName()); //$NON-NLS-1$
			((JLabel) headerComp.getComponent()).setText(clsName);
		} catch (Throwable t) {
			//log.log(Level.WARNING, "Could not update protege component. Reason: Working on implementation -- not interface.", t); //$NON-NLS-1$
		}
	}
	
	public Cls getCurrentCls() {
		return currentCls;
	}

	public Slot getCurrentSlot() {
		return currentSlot;
	}

	private void setCurrentSlot(Slot currentSlot) {
		this.currentSlot = currentSlot;
	}
	
	private void addExceptionPopupLogger() {
		ConsoleHandler myHandler = new ConsoleHandler() {
			private SimpleFormatter formatter = new SimpleFormatter();

			public void publish(LogRecord logrec) {
				if (logrec.getLevel() != Level.SEVERE) {// && logrec.getLevel()!=Level.SEVERE) 
					return;
				}
				JOptionPane.showMessageDialog(instance(), formatter.format(logrec), String.format(Messages.getString("Logger"), logrec.getLevel()), JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
			}
			
		};
		myHandler.setLevel(Level.INFO);
		Logger thisLogger = Logger.getLogger(""); //$NON-NLS-1$
		thisLogger.setLevel(Level.INFO);
		thisLogger.addHandler(myHandler);
	}
	
	public void nameChanged(FrameEvent event) {
		// tell cbr project
		try {
			paMain.select(ModelProvider.getInstance().getModelInstance(event.getFrame().getName()));
		} catch (Throwable e) {
			e.printStackTrace();
		}
	}

	// do nothing
	public void browserTextChanged(FrameEvent event) {}
	public void ownFacetAdded(FrameEvent event) {}
	public void ownFacetRemoved(FrameEvent event) {}
	public void ownFacetValueChanged(FrameEvent event) {}
	public void ownSlotAdded(FrameEvent event) {}
	public void ownSlotRemoved(FrameEvent event) {}
	public void ownSlotValueChanged(FrameEvent event) {}
	public void visibilityChanged(FrameEvent event) {}
	public void deleted(FrameEvent arg0) {}

	@Override
	public boolean canClose() {
		return true;
	}
	
	@Override
	public void close() {
		CBRProject.resetInstance();
		closeActiveTab();
	}
}
