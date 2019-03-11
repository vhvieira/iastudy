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
import java.awt.Color;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import edu.stanford.smi.protege.model.Cls;
import edu.stanford.smi.protege.util.ComponentFactory;

/**
 * @author myCBR Team
 *
 * Represents the right side in the protege editor. Provides a header and import instances button.
 */
public class MainPanel extends javax.swing.JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(MainPanel.class.getName());
	
	private static final boolean GENERATE_BUTTON_ACTIVE = false;

	BorderLayout borderLayout1 = new BorderLayout();

	BorderLayout borderLayout2 = new BorderLayout();

	JPanel paContent = new JPanel();

	JPanel paTop = new JPanel();

	NoTabbedPane tabber = new NoTabbedPane();

	JPanel paHeader = new JPanel();

	GridBagLayout gridBagLayout1 = new GridBagLayout();

	// buttons
	JButton buGenerate 	= new JButton();

	
	public MainPanel() {
		try {
			jbInit();
			customInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void customInit() {
		// GENERATE button is useful for our own purposes only.
		// so we don't display it in official release.
		buGenerate.setVisible(GENERATE_BUTTON_ACTIVE);

		createHeaderPanel();

		// maximize/minimize view
		paHeader.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() >= 2) {
					MyCbr_Similarities_Tab.instance().toggleMaximizeSize();
				}
			}
		});

		// buttons
		buGenerate.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				log.fine("pressed 'Generate instances'.");

				Cls currentCls = MyCbr_Similarities_Tab.instance().getCurrentCls();
				if (currentCls == null) {
					log.fine("currentCls is null... return.");
					JOptionPane.showMessageDialog(MyCbr_Similarities_Tab.instance(), "Please specify a Class first.");
					return;
				}

				GeneratorDialog gd = new GeneratorDialog((Frame)getTopLevelAncestor(), currentCls);
				Helper.centerWindow(gd);
				gd.setVisible(true);
				
			}
		});

	}
	
	void jbInit() throws Exception {
		paTop.setLayout(gridBagLayout1);
		this.setLayout(borderLayout1);
		paContent.setLayout(borderLayout2);
		buGenerate.setText("Generate Instances");
		this.add(paContent, BorderLayout.CENTER);
		this.add(paTop, BorderLayout.NORTH);
		paTop.add(buGenerate, new GridBagConstraints(1, 0, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE,
				new Insets(5, 5, 5, 5), 0, 0));
		paTop.add(paHeader, new GridBagConstraints(0, 0, 3, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 390, 0));
		paContent.add(tabber, BorderLayout.CENTER);
	}

	public void select(ModelInstance inst) {
		// pass through to tabber
		tabber.select(inst);
	}

	public void refresh() {
		// pass through to tabber
		tabber.refresh();
	}

	/**
	 Protege and myCBR share common data. Because of redundancies inconsistencies may occure.
	 This method is used to check for this inconsistency to everything that is currently displayed.
	 */
	public void checkConsistency() {
		tabber.checkConsistency();
	}

	private void createHeaderPanel() {
		String editorLabel = "SIMILARITY MEASURE EDITOR";

		paHeader.setLayout(new BorderLayout());
		JPanel titlePanel = new JPanel(new BorderLayout());
		JLabel titleLabel = ComponentFactory.createTitleFontLabel(editorLabel.toUpperCase());
		titleLabel.setForeground(Color.white);
		titleLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 2));
		titlePanel.add(titleLabel);
		paHeader.add(titlePanel, BorderLayout.NORTH);
		titlePanel.setBackground(Helper.COLOR_RED_MYCBR);
	}

	/**
	 * Closes the currently active tab
	 * @return true if tab has been closed.
	 */
	public boolean closeActiveTab() {
		// pass through
		return tabber.closeHolderPanel();
	}

}
