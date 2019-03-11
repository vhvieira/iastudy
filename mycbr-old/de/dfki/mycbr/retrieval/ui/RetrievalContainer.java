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
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import de.dfki.mycbr.retrieval.RetrievalEngine;
import de.dfki.mycbr.retrieval.RetrievalResults;

public class RetrievalContainer extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private RetrievalWidget retrievalWidget;
	private RetrievalResultsPanel paRetrievalResults;

	private JSplitPane splitter;
	private static RetrievalContainer instance;

	private RetrievalContainer() {
		setLayout(new BorderLayout());

		retrievalWidget = RetrievalWidget.initInstance();
		paRetrievalResults = new RetrievalResultsPanel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			@Override
			public void setVisible(boolean arg0) {
				super.setVisible(arg0);
				if (splitter != null) {
					if (splitter.getDividerLocation() > splitter
							.getMaximumDividerLocation()) {
						splitter.setDividerLocation(splitter
								.getMaximumDividerLocation());
					}
				}
			}
		};
		retrievalWidget.setRetrievalContainer(this);
		paRetrievalResults.setRetrievalContainer(this);

		JPanel panel = new JPanel(new GridBagLayout());
		splitter = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
		splitter.setLeftComponent(retrievalWidget);
		splitter.setRightComponent(paRetrievalResults);
		panel.add(splitter, new GridBagConstraints(0, 0, 1, 1, 1.0, 1.0,
				GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(
						0, 0, 0, 0), 0, 0));
		add(panel, BorderLayout.CENTER);

	}

	public static synchronized RetrievalContainer getInstance() {
		return instance;
	}

	public static RetrievalContainer initInstance() {
		instance = new RetrievalContainer();	
		return instance;
	}
	public static void resetInstance() {
		instance = null;
	}
	
	public void setSelectionIndices(int[] ranksOfCases) {
		paRetrievalResults.setSelectionIndices(ranksOfCases);
	}

	public de.dfki.mycbr.retrieval.RetrievalEngine getRetrievalEngine() {
		de.dfki.mycbr.retrieval.RetrievalEngine re = RetrievalEngine.getInstance();
		re.addRetrievalResultListener(paRetrievalResults);
		re.addRetrievalResultListener(retrievalWidget);
		return re;
	}

	public void setRetrievalResults(RetrievalResults results) {
		paRetrievalResults.setRetrievalResults(results);
		retrievalWidget.setRetrievalResults(results);
	}

	public void setRetrievalState(double percentage) {
		paRetrievalResults.setRetrievalState(percentage);
		retrievalWidget.setRetrievalState(percentage);
	}

	public RetrievalWidget getRetrievalWidget() {
		return retrievalWidget;
	}

	public RetrievalResultsPanel getPaRetrievalResults() {
		return paRetrievalResults;
	}

}
