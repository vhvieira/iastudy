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
import java.awt.Dimension;
import java.awt.Rectangle;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.util.Vector;

import javax.swing.JPanel;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableModel;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.retrieval.AssessedInstance;
import de.dfki.mycbr.retrieval.RetrievalResults;

public class RankingPanel extends JPanel
{
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final String NAME_RANKING_PANEL = "rankingPanel";
	
	private JTable taResults = new JTable();
	private RetrievalContainer retrievalContainer;

	public RankingPanel()
	{
		setName(NAME_RANKING_PANEL);
		setLayout(new BorderLayout());
		add(taResults, BorderLayout.CENTER);

		MouseListener[] ml = taResults.getMouseListeners();
		for (int i=0; i<ml.length; i++) taResults.removeMouseListener(ml[i]);
		MouseMotionListener[] mml = taResults.getMouseMotionListeners();
		for (int i=0; i<mml.length; i++) taResults.removeMouseMotionListener(mml[i]);
		// not used right now:
		taResults.addMouseListener(new MouseAdapter()
		{
			public void mouseClicked(MouseEvent e)
			{
				if (retrievalContainer!=null)
				{
					int selectedRank = taResults.rowAtPoint(e.getPoint());
					retrievalContainer.getRetrievalWidget().setDisplayFirstIndex(selectedRank);
				}
			}
		});
	}
	
	public void displayRetrievalResults(RetrievalResults results)
	{
		Vector<AssessedInstance> ranking 		= results.getRanking();
		
		Vector<String> header = new Vector<String>();
		header.add("Rank");
		header.add("Case ID");
		header.add("Sim");
		
		Vector<Vector<Object>> rows = new Vector<Vector<Object>>();
		int rowCnt = ranking.size();
		
		for (int i=0; i<rowCnt; i++)
		{
			Vector<Object> row = new Vector<Object>();
			AssessedInstance as = (AssessedInstance)ranking.get(i);
			
			row.add(new Integer(i+1));
			row.add(as.inst.getName());
			row.add(Helper.getSimilarityStr(as.similarity));
			
			rows.add(row);
		}
		
		DefaultTableModel dtm = new DefaultTableModel(rows, header)
		{
			/**
			 * 
			 */
			private static final long serialVersionUID = 1L;

			public boolean isCellEditable(int arg0, int arg1)
			{
				return false;
			}
		};

		
		taResults.setModel(dtm);
		String maxStr = "0"+rowCnt;
		taResults.getColumnModel().getColumn(0).setMaxWidth(getFontMetrics(getFont()).charsWidth(maxStr.toCharArray(), 0, maxStr.length()));
		
	}
	
	@Override
	public void setBounds(int x, int y, int width, int height)
	{
		// TODO Auto-generated method stub
		super.setBounds(x, y, width, height);
	}
	
	@Override
	public void setBounds(Rectangle r)
	{
		// TODO Auto-generated method stub
		super.setBounds(r);
	}
	
	@Override
	public void setSize(Dimension d)
	{
		// TODO Auto-generated method stub
		super.setSize(d);
	}
	
	@Override
	public void setSize(int width, int height)
	{
		// TODO Auto-generated method stub
		super.setSize(width, height);
	}
	
	public void setSelectionIndices(final int[] indices)
	{
		SwingUtilities.invokeLater(
			new Runnable() {
				public void run() {
					taResults.clearSelection();
					for (int i=0; i<indices.length; i++)
					{
						taResults.changeSelection(indices[i], 0, true, false); 
					}
				}
			}
		);
	}


	public RetrievalContainer getRetrievalContainer()
	{
		return retrievalContainer;
	}


	public void setRetrievalContainer(RetrievalContainer retrievalContainer)
	{
		this.retrievalContainer = retrievalContainer;
	}
	
}
