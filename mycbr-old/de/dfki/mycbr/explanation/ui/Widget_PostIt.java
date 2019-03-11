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
package de.dfki.mycbr.explanation.ui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.Timer;
import java.util.TimerTask;

import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.SwingConstants;
import javax.swing.border.EtchedBorder;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.VerticalLabelUI;

public class Widget_PostIt extends JPanel {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final int VISIBILITY_STATE_CLOSED 	= 0;
	public static final int VISIBILITY_STATE_OPEN_HALF = 1;
	public static final int VISIBILITY_STATE_OPEN_FULL = 2;
	
	//
	// GUI
	//
	private JPanel sidePanel 		= new JPanel(new GridBagLayout());
	private JPanel sidePanelMax 	= new JPanel(new BorderLayout());
	private JPanel sidePanelStep 	= new JPanel(new BorderLayout());
	private JLabel laSideLabel = new JLabel(Messages.getString("Explanations")); //$NON-NLS-1$
	private JLabel labelMax = new JLabel("|<"); //$NON-NLS-1$
	private JLabel labelStep = new JLabel("<"); //$NON-NLS-1$
	
	// inner component
	private JComponent tabbedPane;
	private Dimension originalPreferredSize;
	private Dimension closedPreferredSize;

	// current visibility state
	static int visibilityState = VISIBILITY_STATE_CLOSED;
//	int visibilityState = VISIBILITY_STATE_CLOSED;

	private static boolean lastIsVisible = false;
	private static boolean startHighlightingNextOpportunity = false;;
	
	// commented out before 20.10.2008
//	public Widget_PostIt(List<JComponent> innerComponents)
//	{
//		this.tabbedPane = new JTabbedPane();
//		for (JComponent comp: innerComponents)
//		{
//			tabbedPane.add(comp.getName(), comp);
//			
//		}
//		
//		this.originalPreferredSize = tabbedPane.getPreferredSize();
//		this.closedPreferredSize = new Dimension(0, originalPreferredSize.height);
//		this.laSideLabel.setDoubleBuffered(true);
//		setLayout(new BorderLayout());
//		customInit();
//	}

	public Widget_PostIt(JComponent innerComponent) {
//		this(Arrays.asList(new JComponent[]{innerComponent}));
		this.tabbedPane = innerComponent;
		
		this.originalPreferredSize = tabbedPane.getPreferredSize();
		this.closedPreferredSize = new Dimension(0, originalPreferredSize.height);
		this.laSideLabel.setDoubleBuffered(true);
		setLayout(new BorderLayout());
		customInit();
	}

	private void customInit() {
		// init GUI
		laSideLabel.setUI(new VerticalLabelUI(false));
		labelMax.setHorizontalAlignment(SwingConstants.CENTER);
		labelStep.setHorizontalAlignment(SwingConstants.CENTER);
		
		sidePanelMax.add(labelMax, BorderLayout.SOUTH);
		sidePanelStep.add(labelStep, BorderLayout.NORTH);
		
		sidePanel.add(sidePanelMax, new GridBagConstraints(0,1, 1,1, 1d, 1d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		sidePanel.add(sidePanelStep, new GridBagConstraints(0,2, 1,1, 1d, 0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		sidePanel.add(laSideLabel, new GridBagConstraints(0,3, 1,1, 1d, 0d, GridBagConstraints.NORTHWEST, GridBagConstraints.NONE, new Insets(15,0,15,0), 0,0));
		sidePanel.setBorder(new EtchedBorder());

		
		// configure buttons
		labelStep.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				switch (visibilityState) {
					case VISIBILITY_STATE_CLOSED	: visibilityState=VISIBILITY_STATE_OPEN_HALF; break; 
					case VISIBILITY_STATE_OPEN_HALF	: visibilityState=VISIBILITY_STATE_OPEN_FULL; break; 
					case VISIBILITY_STATE_OPEN_FULL	: visibilityState=VISIBILITY_STATE_OPEN_HALF; break; 
				}
				checkVisibilities();
			}
		});
		labelMax.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				switch (visibilityState) {
					case VISIBILITY_STATE_CLOSED	: visibilityState=VISIBILITY_STATE_OPEN_FULL; break; 
					case VISIBILITY_STATE_OPEN_FULL	: visibilityState=VISIBILITY_STATE_CLOSED; break; 
					case VISIBILITY_STATE_OPEN_HALF	: visibilityState=VISIBILITY_STATE_CLOSED; break; 
				}
				checkVisibilities();
			}		
		});
		
		// now add components to this
		add(sidePanel, BorderLayout.WEST);
		add(tabbedPane, BorderLayout.CENTER);

		checkVisibilities();
	}

	protected void checkVisibilities() {
		try {
			if (visibilityState == VISIBILITY_STATE_CLOSED) {
				labelMax.setText("|<"); //$NON-NLS-1$
				labelStep.setText(".<"); //$NON-NLS-1$
				tabbedPane.setVisible(false);
				
				tabbedPane.setPreferredSize(closedPreferredSize);
			} else if (visibilityState == VISIBILITY_STATE_OPEN_HALF) {
				labelMax.setText(">"); //$NON-NLS-1$
				labelStep.setText("<"); //$NON-NLS-1$
				tabbedPane.setVisible(true);
				
				tabbedPane.setPreferredSize(originalPreferredSize);	
			} else {
				labelMax.setText(">|"); //$NON-NLS-1$
				labelStep.setText(">."); //$NON-NLS-1$
				tabbedPane.setVisible(true);

				Dimension newSize = new Dimension(getParent().getSize().width-sidePanel.getPreferredSize().width, originalPreferredSize.height);
				tabbedPane.setPreferredSize(newSize);
			}
			if (tabbedPane.getParent() != null) {
				((JPanel) tabbedPane.getParent()).revalidate();
			}
				
		} catch (Throwable t) {
			t.printStackTrace();
		}
		
	}

	
	@Override
	public void setVisible(boolean flag) {
		super.setVisible(flag);
		if (!lastIsVisible && flag) startHighlightingNextOpportunity = true;
		lastIsVisible = flag;
	}

	@Override
	public void paint(Graphics g) {
		super.paint(g);
		
		if (startHighlightingNextOpportunity) {
			//
			// start HIGHLIGHTING - Timer
			// (flashes up the label for the explanation panel)
			//
			TimerTask timerTask = new TimerTask() {
				final Color baseCol = sidePanel.getBackground();
				final Color highlightCol = Helper.COLOR_RED_MYCBR;
				final double step = Math.PI / 10d;
				double progress = 0d;

				@Override
				public void run() {
					try {
						progress += step;
						double rel = Math.sin(progress);

						int r = arrangeValue(baseCol.getRed()	+ (int) (rel * (highlightCol.getRed() 	- baseCol.getRed())));
						int g = arrangeValue(baseCol.getGreen() + (int) (rel * (highlightCol.getGreen() - baseCol.getGreen())));
						int b = arrangeValue(baseCol.getBlue()	+ (int) (rel * (highlightCol.getBlue()	- baseCol.getBlue())));
						Color col = new Color(r, g, b);
						sidePanel.setBackground(col);
						sidePanelMax.setBackground(col);
						sidePanelStep.setBackground(col);
						sidePanel.repaint();

						if (progress > Math.PI) {
							sidePanel.setBackground(baseCol);
							sidePanelMax.setBackground(baseCol);
							sidePanelStep.setBackground(baseCol);
							startHighlightingNextOpportunity = false;
							cancel();
						}
					} catch (RuntimeException e) {
						e.printStackTrace();
					}
				}
				
				private int arrangeValue(int v) {
					if (v > 255) {
						return 255;
					}
					if (v < 0) {
						return 0;
					}
					return v;
				}
			};
			Timer timer = new Timer();
			timer.schedule(timerTask, 100, 100);
		}
	}
	
}
