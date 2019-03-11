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
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JPanel;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;

import de.dfki.mycbr.retrieval.AssessedInstance;
import de.dfki.mycbr.retrieval.RetrievalResults;

public class ResultsPanel extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String NAME_SIM_VS_DIV_PANEL = "simVsDivPanel";

	private RetrievalContainer retrievalContainer;

	private RetrievalResults results;

	private JTree tree;

	private RetrievalWidget retrievalWidget;

	public ResultsPanel() {
		setName(NAME_SIM_VS_DIV_PANEL);
		setLayout(new BorderLayout());

		tree = new JTree();
		tree.setRootVisible(false);
		tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				int[] rows = tree.getSelectionRows();
				if (rows == null)
					return;
				int[] ranks = new int[rows.length];
				for (int i = 0; i < rows.length; i++) {
					Object obj = ((DefaultMutableTreeNode) tree.getPathForRow(
							rows[i]).getLastPathComponent()).getUserObject();
					if (!(obj instanceof AssessedInstance))
						continue;
					AssessedInstance ai = (AssessedInstance) obj;
					ranks[i] = results.getRanking().indexOf(ai);
				}
				retrievalWidget.setDisplayedRanks(ranks);
			}
		});

		add(tree, BorderLayout.CENTER);
	}

	public void displayRetrievalResults(RetrievalResults results) {
		this.results = results;
	}

	public void setSelectionIndices(int[] indices) {
		// TODO Auto-generated method stub
	}

	public RetrievalContainer getRetrievalContainer() {
		return retrievalContainer;
	}

	public void setRetrievalContainer(RetrievalContainer retrievalContainer) {
		this.retrievalContainer = retrievalContainer;
		retrievalWidget = retrievalContainer.getRetrievalWidget();
	}

}
