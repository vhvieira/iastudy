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
import java.awt.CardLayout;
import java.awt.Color;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.retrieval.RetrievalResultListener;
import de.dfki.mycbr.retrieval.RetrievalResults;

/**
 * 
 * @author myCBR Team
 */
public class RetrievalResultsPanel extends JPanel implements
		RetrievalResultListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger
			.getLogger(RetrievalResultsPanel.class.getName());

	private static final String LABEL = "QUERY RESULTS";

	private JLabel laTimeStart = null;
	private JLabel laFinish = null;
	private JLabel laDuration = null;
	private JLabel jLabel = null;
	private JTextField txtTimeStart = null;
	private JTextField txtTimeFinish = null;
	private JTextField txtDuration = null;

	private JPanel paAdditional;

	private RetrievalResults results;

	private JPanel paHeader = null;
	private RetrievalContainer retrievalContainer;

	private RankingPanel paRanking;

	private JPanel paContent;

	private CardLayout cardLayout = new CardLayout();

	private JScrollPane scrollpane;

	public RetrievalResultsPanel() {
		paRanking = new RankingPanel();

		initialize();
		checkVisibilityState();

	}

	/**
	 * Will be called by retrieval engine when it's finished.
	 */
	public synchronized void setRetrievalResults(RetrievalResults results) {
		this.results = results;
		if (results == null) {
			return;
		}

		log.fine("show query results!");
		paRanking.displayRetrievalResults(results);

		long duration = results.getTimeFinish().getTime()
				- results.getTimeStart().getTime();

		SimpleDateFormat dateFormat = new SimpleDateFormat();
		dateFormat.applyPattern("h:mm:ss");

		double dur = ((double) duration) / 1000;
		DecimalFormat decFormat = new DecimalFormat("###,###,##0.000");

		txtTimeStart.setText(dateFormat.format(results.getTimeStart()));
		txtTimeFinish.setText(dateFormat.format(results.getTimeFinish()));
		txtDuration.setText("" + decFormat.format(dur) + " sec");

		checkVisibilityState();

		retrievalContainer.revalidate();
		retrievalContainer.doLayout();

	}

	private void checkVisibilityState() {
		setVisible(results != null);
	}

	public void setSelectionIndices(int[] indices) {
		paRanking.setSelectionIndices(indices);
	}

	private void initialize() {
		setLayout(new BorderLayout());

		add(getPaHeader(LABEL), BorderLayout.NORTH);

		paContent = new JPanel(cardLayout);
		scrollpane = new JScrollPane(paRanking);
		paContent.add(scrollpane, paRanking.getName());

		add(paContent, BorderLayout.CENTER);

		cardLayout.show(paContent, paRanking.getName());

		add(getPaAdditional(), BorderLayout.SOUTH);

	}

	/**
	 * This method initializes this
	 * 
	 * @return void
	 */
	private JPanel getPaAdditional() {
		if (paAdditional == null) {
			paAdditional = new JPanel();

			GridBagConstraints gridBagConstraints21 = new GridBagConstraints();
			gridBagConstraints21.gridx = 1;
			gridBagConstraints21.anchor = java.awt.GridBagConstraints.NORTH;
			gridBagConstraints21.weighty = 1.0D;
			gridBagConstraints21.gridwidth = 2;
			gridBagConstraints21.gridy = 3;
			gridBagConstraints21.fill = GridBagConstraints.BOTH;
			gridBagConstraints21.insets = new Insets(0, 0, 0, 5);
			GridBagConstraints gridBagConstraints11 = new GridBagConstraints();
			gridBagConstraints11.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints11.gridy = 2;
			gridBagConstraints11.weightx = 1.0;
			gridBagConstraints11.gridx = 2;
			gridBagConstraints11.insets = new Insets(0, 0, 0, 5);
			GridBagConstraints gridBagConstraints5 = new GridBagConstraints();
			gridBagConstraints5.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints5.gridy = 1;
			gridBagConstraints5.weightx = 1.0;
			gridBagConstraints5.gridx = 2;
			gridBagConstraints5.insets = new Insets(0, 0, 0, 5);
			GridBagConstraints gridBagConstraints4 = new GridBagConstraints();
			gridBagConstraints4.fill = java.awt.GridBagConstraints.HORIZONTAL;
			gridBagConstraints4.gridy = 0;
			gridBagConstraints4.weightx = 1.0;
			gridBagConstraints4.anchor = java.awt.GridBagConstraints.SOUTH;
			gridBagConstraints4.gridx = 2;
			gridBagConstraints4.insets = new Insets(5, 0, 0, 5);
			GridBagConstraints gridBagConstraints3 = new GridBagConstraints();
			gridBagConstraints3.gridx = 1;
			gridBagConstraints3.gridy = 0;
			gridBagConstraints3.insets = new Insets(5, 5, 0, 5);
			jLabel = new JLabel();
			jLabel.setText("");
			GridBagConstraints gridBagConstraints2 = new GridBagConstraints();
			gridBagConstraints2.gridx = 0;
			gridBagConstraints2.anchor = java.awt.GridBagConstraints.EAST;
			gridBagConstraints2.gridy = 2;
			gridBagConstraints2.insets = new Insets(0, 5, 0, 0);
			laDuration = new JLabel();
			laDuration.setText("Duration:");
			GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
			gridBagConstraints1.gridx = 0;
			gridBagConstraints1.anchor = java.awt.GridBagConstraints.EAST;
			gridBagConstraints1.gridy = 1;
			gridBagConstraints1.insets = new Insets(0, 5, 0, 0);
			laFinish = new JLabel();
			laFinish.setText("Finish:");
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.gridx = 0;
			gridBagConstraints.anchor = java.awt.GridBagConstraints.SOUTHEAST;
			gridBagConstraints.weighty = 1.0D;
			gridBagConstraints.gridy = 0;
			gridBagConstraints.insets = new Insets(5, 5, 0, 0);
			laTimeStart = new JLabel();
			laTimeStart.setText("Start:");
			paAdditional.setLayout(new GridBagLayout());
			paAdditional.setSize(100, 200);
			paAdditional.add(laTimeStart, gridBagConstraints);
			paAdditional.add(laFinish, gridBagConstraints1);
			paAdditional.add(laDuration, gridBagConstraints2);
			paAdditional.add(getTxtTimeStart(), gridBagConstraints4);
			paAdditional.add(getJTextField(), gridBagConstraints5);
			paAdditional.add(getJTextField2(), gridBagConstraints11);
		}
		return paAdditional;

	}

	/**
	 * This method initializes txtTimeStart	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getTxtTimeStart() {
		if (txtTimeStart == null) {
			txtTimeStart = new JTextField();
			txtTimeStart.addFocusListener(Helper.focusListener);
			txtTimeStart.setEditable(false);
		}
		return txtTimeStart;
	}

	/**
	 * This method initializes jTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField() {
		if (txtTimeFinish == null) {
			txtTimeFinish = new JTextField();
			txtTimeFinish.addFocusListener(Helper.focusListener);
			txtTimeFinish.setEditable(false);
		}
		return txtTimeFinish;
	}

	/**
	 * This method initializes jTextField	
	 * 	
	 * @return javax.swing.JTextField	
	 */
	private JTextField getJTextField2() {
		if (txtDuration == null) {
			txtDuration = new JTextField();
			txtDuration.addFocusListener(Helper.focusListener);
			txtDuration.setEditable(false);
		}
		return txtDuration;
	}

	public void setRetrievalState(double percentage) {
		// nothing to do
	}

	/**
	 * This method initializes paHeader	
	 * 	
	 * @return javax.swing.JPanel	
	 */
	protected JPanel getPaHeader(String labeltxt) {
		if (paHeader == null) {
			paHeader = new JPanel();
			paHeader.setLayout(new BorderLayout());

			JPanel titlePanel = new JPanel(new BorderLayout());
			titlePanel.setBackground(Helper.COLOR_RED_MYCBR);
			JLabel titleLabel = new JLabel(labeltxt.toUpperCase());
			titleLabel.setForeground(Color.white);
			titleLabel.setBorder(BorderFactory.createEmptyBorder(2, 4, 2, 2));
			titlePanel.add(titleLabel);
			paHeader.add(titlePanel, BorderLayout.CENTER);

		}
		return paHeader;
	}

	/**
	 * Set RetrievalContainer. This container will be informed when some selections have changed.
	 * @param retrievalContainer
	 */
	public void setRetrievalContainer(RetrievalContainer retrievalContainer) {
		this.retrievalContainer = retrievalContainer;
		paRanking.setRetrievalContainer(retrievalContainer);
	}

}
