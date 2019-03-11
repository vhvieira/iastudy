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
package de.dfki.mycbr.modelprovider.protege.retrieval;

import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.FocusManager;
import javax.swing.ImageIcon;
import javax.swing.SwingUtilities;

import de.dfki.mycbr.CBRProject;
import de.dfki.mycbr.modelprovider.protege.MyCBRMenu;
import de.dfki.mycbr.modelprovider.protege.similaritymodeling.MyCbr_Similarities_Tab;
import de.dfki.mycbr.retrieval.ui.RetrievalContainer;
import edu.stanford.smi.protege.event.KnowledgeBaseEvent;
import edu.stanford.smi.protege.event.KnowledgeBaseListener;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.model.Slot;
import edu.stanford.smi.protege.widget.AbstractTabWidget;

/**
 * 
 * @author myCBR Team
 */
public class MyCbr_Retrieval_Tab extends AbstractTabWidget implements KnowledgeBaseListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger ( MyCbr_Retrieval_Tab.class.getName ( ) );
	
	public static final String TAB_LABEL = Messages.getString("Cbr_retrieval");   //$NON-NLS-1$

	public static ImageIcon 		ICON_MYCBR = null; 
	{
		try {
			ICON_MYCBR = new ImageIcon(MyCbr_Similarities_Tab.class.getResource("logo_mycbr.png")); //$NON-NLS-1$
		} catch (Throwable ex) {}
	}

	private RetrievalContainer retrievalContainer;

	
	public void initialize() {
		try {
			CBRProject.getInstance(this);
			setLabel(TAB_LABEL);
			if (ICON_MYCBR != null) {
				setIcon(ICON_MYCBR);
			}
			
			retrievalContainer = RetrievalContainer.initInstance();
			add(retrievalContainer);
			
			if (getProject().getKnowledgeBase() == null) {
				log.fine("NOTE: COULD NOT ADD KnowledgeBaseListener TO PROJECT [" + getProject().getName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				log.fine("add KnowledgeBaseListener to project [" + getProject().getName() + "]"); //$NON-NLS-1$ //$NON-NLS-2$
				getProject().getKnowledgeBase().addKnowledgeBaseListener(this);
			}

			// add menu entry
	        MyCBRMenu.createInstance(getProject());
			MyCBRMenu.addMenuTo(getMainWindowMenuBar());
		} catch (Throwable e) {
			e.printStackTrace();
			log.log(Level.SEVERE, "Cannot initialize Cbr retrieval tab.", e); //$NON-NLS-1$
		}

	}

	private void refresh() {
		retrievalContainer.getRetrievalWidget().updateAvailableClasses();
		retrievalContainer.getRetrievalWidget().updateAvailableSlots();
	}

	
	@Override
	public void dispose() {
		super.dispose();
		MyCBRMenu.removeMenuFrom(getMainWindowMenuBar());
	}

	//
	// implementation of knowledge base listener
	//
	public void clsCreated(KnowledgeBaseEvent event) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				
				refresh();
			}
		});
	}
	
	public void clsDeleted(KnowledgeBaseEvent event) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				refresh();
			}
		});
	}
	
	public void instanceCreated(KnowledgeBaseEvent event) {
		if (event.getFrame() instanceof Slot || event.getFrame() instanceof Cls) {
			refresh();
		}
	}
	
	public void slotDeleted(KnowledgeBaseEvent event) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				refresh();
			}
		});
	}
	
	public void slotCreated(KnowledgeBaseEvent event) {
		SwingUtilities.invokeLater(new Runnable() {
			public void run() {
				refresh();
			}
		});
	}

	public void instanceDeleted(KnowledgeBaseEvent event) {
		if (event.getFrame() instanceof Slot || event.getFrame() instanceof Cls) {
			refresh();
		}
	}
	
	public void frameNameChanged(KnowledgeBaseEvent event) {
		if (event.getFrame() instanceof Slot || event.getFrame() instanceof Cls) {
			refresh();
		}
	}

	public void defaultClsMetaClsChanged(KnowledgeBaseEvent event) {
	}
	
	public void defaultFacetMetaClsChanged(KnowledgeBaseEvent event) {
	}
	
	public void defaultSlotMetaClsChanged(KnowledgeBaseEvent event) {
	}
	
	public void facetCreated(KnowledgeBaseEvent event) {
	}
	
	public void facetDeleted(KnowledgeBaseEvent event) {
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
	 * for problems that might occur.
	 */
	public void save() {
			FocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
			
			String projectDir = new File(getProject().getProjectDirectoryURI()).getAbsolutePath();
			String projectName = getProject().getName();
			CBRProject.getInstance().save(projectName, projectDir);
		
	}

	@Override
	public boolean canClose() {
		return true;
	}
	
	@Override
	public void close() {
		CBRProject.resetInstance();
	}
}
