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
import java.awt.Component;
import java.awt.Graphics;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.util.Iterator;
import java.util.Map.Entry;

import javax.swing.ButtonGroup;
import javax.swing.FocusManager;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.JToggleButton;
import javax.swing.JToolTip;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.explanation.ExplanationManager;
import de.dfki.mycbr.explanation.SlotStatistic;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.vocabulary.ModelCls;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.retrieval.ui.MultiLineTooltip;

public class Widget_Explanation_Num extends JPanel implements ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private JPanel paGraph;
	private JPanel paTools;
	private JPanel paQuery;

	private JSlider sliderFa;
	private JSlider sliderQ;
	private JLabel laQuery = new JLabel(Messages.getString("Query_value")); //$NON-NLS-1$
	private JTextField txtQuery = new JTextField();
	private JToggleButton buDiffs = new JToggleButton(Messages.getString("Diff")); //$NON-NLS-1$
	private JToggleButton buOccs  = new JToggleButton(Messages.getString("Occ")); //$NON-NLS-1$
	private ButtonGroup buttongroupToggle = new ButtonGroup();
	private JLabel laHeader;

	private AbstractSMFunction smf;
	private ModelSlot slot;
	private ModelCls cls;

	private double queryValue = 0;
	
	
	public Widget_Explanation_Num(AbstractSMFunction smf) {
		super();
		this.smf = smf;
		this.cls = Helper.getDomainCls((ModelSlot) smf.getModelInstance());
		this.slot = (ModelSlot) smf.getModelInstance();
		setName(Messages.getString("Explanatory_design_support")); //$NON-NLS-1$
		isInt = (smf.getValueType() == ValueType.INTEGER);

		setLayout(new GridBagLayout());
		
		paGraph = new JPanel() {
			/**
			 * 
			 */
			private static final long serialVersionUID = -396057848537931769L;

			public JToolTip createToolTip() {
				return new MultiLineTooltip();
			}
			
			public void paint(Graphics g) {
				super.paint(g);
				paintExplanationGraph(g);
			}
		};
		paGraph.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				int x = e.getX()- POS_X_MIN;
				if (x >= 0 && x <= gRange) {
					double slotX = x * slotRange / gRange;
					slotX = (isInt? Math.round(slotX): Helper.roundDouble(slotX, 2));
					
					int length = Y_ZERO - Y_ONE;
					long val2 = Math.round(((Y_ZERO - yPoints[x+1])*clsUsage) / (((double)length) *sliderFactor));

					
					if (!showDiffs) {
						String numVal = Integer.toString((int)Math.round(slot.getMinimumValue().intValue() + (int) slotX));
						paGraph.setToolTipText(String.format(Messages.getString("Occurrences_in_casebases"), numVal, val2)); //$NON-NLS-1$
					} else {
						double diff = slotX*2 - slotRange;
						double caseval = slotX = queryValue - slotX;
						paGraph.setToolTipText(String.format(Messages.getString("Differences_in_casebases"), diff, caseval, val2)); //$NON-NLS-1$
					}
				}
			}
			
			public void mouseDragged(MouseEvent e) {
				// no idea
			}
		});
		
		paGraph.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e){}
			public void mouseReleased(MouseEvent e){}
			
			public void mousePressed(MouseEvent arg0) {
				FocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
			}
		});

		paTools = new JPanel(new GridBagLayout());
		sliderFa = new JSlider(1, 100);
		sliderFa.setValue((int)(sliderFactor*10));
		sliderFa.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				sliderFactor = ((double) sliderFa.getValue())/10d;
				repaint();
			}
		});
		sliderFa.setOrientation(JSlider.VERTICAL);

		
		sliderQ = new JSlider(-100, 100);
		sliderQ.setValue(0);
		sliderQ.addChangeListener(new ChangeListener() {
			public void stateChanged(ChangeEvent e) {
				int value = sliderQ.getValue();
				double hd = (double) slotRange/2;
				double val = hd + ( (hd * ((double)value)/100));
				if (isInt) val = Math.round(val);
				txtQuery.setText(Helper.formatDoubleAsString(slot.getMinimumValue().doubleValue() + val));
				
				refreshQuery();
				repaint();
			}
		});
		
		paQuery = new JPanel(new GridBagLayout());
		paQuery.add(sliderQ, 		new GridBagConstraints(0,0, 1,1, 1d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		paQuery.add(laQuery, 		new GridBagConstraints(1,0, 1,1, 0d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		paQuery.add(txtQuery, 		new GridBagConstraints(2,0, 1,1, 0d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		txtQuery.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent arg0) {
				refreshQuery();
				txtQuery.setToolTipText(txtQuery.getText());
				repaint();
			}
		});
		
		paTools.add(new StandardExplanationPanel(),	new GridBagConstraints(0,0, 3,1, 0d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));
		paTools.add(getPaHeader(), 	new GridBagConstraints(0,1, 1,1, 1d,1d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5,5,5,5), 0, 0));
		paTools.add(buOccs, 		new GridBagConstraints(1,1, 1,1, 0d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 0), 0, 0));
		paTools.add(buDiffs, 		new GridBagConstraints(2,1, 1,1, 0d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 0, 5, 5), 0, 0));
		paTools.add(paQuery, 		new GridBagConstraints(0,2, 3,1, 0d,0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(5, 5, 5, 5), 0, 0));

		buDiffs.setToolTipText(Messages.getString("Show_differences")); //$NON-NLS-1$
		buOccs.setToolTipText(Messages.getString("Show_occurences")); //$NON-NLS-1$
		buttongroupToggle.add(buDiffs);
		buttongroupToggle.add(buOccs);
		buDiffs.addActionListener(this);
		buOccs.addActionListener(this);
		buOccs.setSelected(true);
		
		add(paTools, 	new GridBagConstraints(1, 0, 1, 1, 1d, 0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		add(sliderFa, 	new GridBagConstraints(0, 1, 1, 1, 0d, 0d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(MARGIN, MARGIN, MARGIN*2, 0), 0, 0));
		add(paGraph, 	new GridBagConstraints(1, 1, 1, 1, 1d, 1d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));


		// init query value
		queryValue = (slot.getMaximumValue().doubleValue() - slot.getMinimumValue().doubleValue()) / 2;
		if (isInt) queryValue = Math.round(queryValue);
		txtQuery.setText(Helper.formatDoubleAsString(slot.getMinimumValue().doubleValue()+queryValue));
		txtQuery.setToolTipText(txtQuery.getText());

		updateButtons();
		txtQuery.addFocusListener(Helper.focusListener);
	}

	
	protected void refreshQuery() {
		double value = Helper.parseDouble(txtQuery.getText());
		if (value == Double.MIN_VALUE) return;
		
		queryValue = value;
		if (isInt) {
			queryValue = Math.round(queryValue);
		}
	}

	public void updateButtons() {
		this.showDiffs = buDiffs.isSelected();
		if (showDiffs) {
			// display diffs
			laHeader.setText(Messages.getString("Casebase_distribution_of_differences")); //$NON-NLS-1$
		} else {
			// display occurrences
			laHeader.setText(Messages.getString("Casebase_distribution_of_values")); //$NON-NLS-1$
		}
		sliderQ.setEnabled(showDiffs);
		laQuery.setEnabled(showDiffs);
		txtQuery.setEnabled(showDiffs);
		txtQuery.setToolTipText((showDiffs? txtQuery.getText() : null));
		
		repaint();
	}

	private Component getPaHeader() {
		JPanel paHeader = new JPanel(new BorderLayout());
		paHeader.add(getHeaderLabel(), BorderLayout.CENTER);
		return paHeader;
	}

	private JLabel getHeaderLabel() {
		if (laHeader == null) {
			laHeader = new JLabel();
			laHeader.setHorizontalAlignment(SwingConstants.CENTER);
		}
		return laHeader;
	}

	/** general line thickness */
	private static final int LINE_THICKNESS = 2;

	/** a certain unit for graphical margins */
	private static final int MARGIN = 15;
	
	private static final int ARROW_SIZE = 6;
	
	private int POS_X_MIN;
	private int POS_X_MAX;

	/** defines the y coordinate in the panel for a ZERO similarity value. */
	private int Y_ZERO;
	
	/** defines the y coordinate in the panel for a ONE similarity value. */
	private int Y_ONE; 

	/** just caching for programming convenience. */
	private Graphics g;

	private int gRange = -1;
	
	private int[] xPoints;
	private int[] yPoints;
	
	/** This is used for label painting (the numbers under the x-axis). We need to check for paint conflicts. */ 
	private int lastEndX = 0;
	private int lastEndY = 0;
	private double slotRange;
	private double newFactor;
	private double sliderFactor = 1d;
	private SlotStatistic stat;
	private boolean isInt;
	private long clsUsage;

	private boolean showDiffs;

	public void paintExplanationGraph(Graphics g) {
		// long startTime = System.currentTimeMillis();
		this.g = g;
		clsUsage = ExplanationManager.getInstance().getClsStatistic(cls);

		int newY_ZERO = paGraph.getSize().height - MARGIN * 2;
		Y_ONE = Math.min(MARGIN, newY_ZERO - 2 * MARGIN);

		POS_X_MIN = MARGIN * 2;
		POS_X_MAX = paGraph.getSize().width - MARGIN * 2;

		// define graphical range
		int newGRange = POS_X_MAX - POS_X_MIN;
		if (newGRange<=0) return;
		if (newGRange != gRange || newY_ZERO != Y_ZERO) {
			// set the current graphical range to plotable.
			this.gRange = newGRange;
			this.Y_ZERO = newY_ZERO;
			int tmp = gRange + 3;
			xPoints = new int[tmp];
			yPoints = new int[tmp];
			xPoints[0] = POS_X_MIN;
			yPoints[0] = Y_ZERO;
			xPoints[tmp - 1] = POS_X_MAX;
			yPoints[tmp - 1] = Y_ZERO;
		}
		slotRange = slot.getMaximumValue().doubleValue() - slot.getMinimumValue().doubleValue();
		newFactor = ((double)slotRange)/ ((double)gRange);
		if (showDiffs) {
			newFactor *= 2;
		}
		stat = ExplanationManager.getInstance().getSlotStatistic(slot);
		isInt = (smf.getValueType() == ValueType.INTEGER);

		drawOrdinate();
		drawAbscissa();
		
		if (showDiffs) {
			drawHeaders();
		}
	
		drawGraph();
	}

	private void drawHeaders() {
		Color tmpColor = g.getColor();
		g.setColor(Color.DARK_GRAY);
		String headerLeft  = "query < case"; //$NON-NLS-1$
		String headerRight = "case < query"; //$NON-NLS-1$
		g.drawChars(headerLeft.toCharArray(), 0, headerLeft.length(), POS_X_MIN+MARGIN, MARGIN);
		g.drawChars(headerRight.toCharArray(), 0, headerRight.length(), POS_X_MAX-MARGIN-headerRight.length()*8, MARGIN);
		
		g.setColor(tmpColor);
	}

	private void drawGraph() {		
		//
		// now faster way
		//
		Iterator< Entry< Object, Integer >> it = stat.entryset().iterator();
		Entry< Object, Integer > currentEntry = (it.hasNext()?  it.next() : null);
		double currentKeyVal = (currentEntry!=null? ((Number)currentEntry.getKey()).doubleValue() : Double.MAX_VALUE);
		double oldSlotX = Double.MAX_VALUE;
		double amount = 0;
		double relApp = 0;
		double minVal = slot.getMinimumValue().doubleValue();
		
		if (!showDiffs) {
			// show occurrences
			for (int x = 0; x <= gRange; x++) {
				double slotX = minVal + (x * slotRange) / gRange;
				if (isInt) {
					slotX = Math.round(slotX);
				}
	
				if (slotX != oldSlotX) {
					amount = 0;
					while (currentKeyVal <= slotX) {
						amount += ((Number) currentEntry.getValue()).doubleValue();
						currentEntry = (it.hasNext() ?  it.next() : null);
						currentKeyVal = (currentEntry != null ? ((Number)currentEntry.getKey()).doubleValue() : Double.MAX_VALUE);
					}
					relApp = amount / (double)clsUsage;
					relApp *= sliderFactor;
				}
				oldSlotX=slotX;
	
				int pixX = POS_X_MIN + x;
				int pixY = valToPixY(relApp);
				int xTmp = x+1;
				xPoints[xTmp]=pixX;
				yPoints[xTmp]=pixY;
				
			}
		
		} else {
			for (int x = gRange; x >= 0; x--) {
				// calculate y-value for all pixels along this range
				// double simVal = plotable.getSimilarityForPixel(x);
				double slotX = (x * slotRange) / gRange;

				// transform x onto diff range (= [-maxdiff, +maxdiff] )
				slotX = slotX*2 - slotRange;
				
				// now find corresponding case value
				slotX = queryValue - slotX;
				
				if (isInt) {
					slotX = Math.round(slotX);
				}
	
				if (slotX!=oldSlotX) {
					amount = 0;
					while (currentKeyVal <= slotX) {
						amount+= ((Number) currentEntry.getValue()).doubleValue();
						currentEntry = (it.hasNext()? it.next() : null);
						currentKeyVal = (currentEntry!=null? ((Number)currentEntry.getKey()).doubleValue() : Double.MAX_VALUE);
					}
					relApp = amount / (double)clsUsage;
					relApp *= sliderFactor;
				} else {
					// amount & relApp keep the same
				}
				oldSlotX=slotX;
	
				int pixX = POS_X_MIN + x;
				int pixY = valToPixY(relApp);
				int xTmp = x+1;
				xPoints[xTmp] = pixX;
				yPoints[xTmp] = pixY;	
			}
		}
		
		Color tmpColor = g.getColor();
		g.setColor(Color.RED);
		g.drawPolyline(xPoints, yPoints, gRange+3);
		g.setColor(tmpColor);
		
	}

	private int valToPixY(double simVal) {
		int result = 0;

		int length = Y_ZERO - Y_ONE;
		result = Y_ZERO - new Long(Math.round(simVal * length)).intValue();

		return result;
	}

	// x
	private void drawAbscissa() {
		int lx = MARGIN;
		int w = paGraph.getSize().width - MARGIN * 2;
		int uy = Y_ZERO;
		int h = LINE_THICKNESS;

		g.setColor(Color.blue);

		g.fillRect(lx + LINE_THICKNESS, uy, w - LINE_THICKNESS * 2, h);

		g.fillPolygon(new int[] { lx + ARROW_SIZE, lx, lx + ARROW_SIZE }, new int[] { uy + ARROW_SIZE, uy, uy - ARROW_SIZE }, 3);
		g.fillPolygon(new int[] { lx + w - ARROW_SIZE, lx + w, lx + w - ARROW_SIZE}, new int[] { uy + ARROW_SIZE, uy, uy - ARROW_SIZE }, 3);

		
		double tmp = slot.getMinimumValue().doubleValue();
		double step = slotRange / 8;
		if (showDiffs) {
			tmp = -slotRange;
			step = (slotRange*2) / 8;
		}
		String label  = Helper.formatDoubleAsString( tmp );
		drawLabelX(label.toCharArray(), 0, label.length(), POS_X_MIN, Y_ZERO+MARGIN);

		tmp += step;
		label  = Helper.formatDoubleAsString(tmp);
		drawLabelX(label.toCharArray(), 0, label.length(), POS_X_MIN+(gRange/8), Y_ZERO+MARGIN);
		
		tmp += step;
		label  = Helper.formatDoubleAsString(tmp);
		drawLabelX(label.toCharArray(), 0, label.length(), POS_X_MIN+(gRange/4), Y_ZERO+MARGIN);

		tmp += step;
		label  = Helper.formatDoubleAsString(tmp);
		drawLabelX(label.toCharArray(), 0, label.length(), POS_X_MIN+((3*gRange)/8), Y_ZERO+MARGIN);

		
		tmp += step;
		label  = Helper.formatDoubleAsString(tmp);
		drawLabelX(label.toCharArray(), 0, label.length(), POS_X_MIN+((gRange)/2), Y_ZERO+MARGIN);
		

		tmp += step;
		label  = Helper.formatDoubleAsString(tmp);
		drawLabelX(label.toCharArray(), 0, label.length(), POS_X_MIN+(5*gRange/8), Y_ZERO+MARGIN);
		
		tmp += step;
		label  = Helper.formatDoubleAsString(tmp);
		drawLabelX(label.toCharArray(), 0, label.length(), POS_X_MIN+((3*gRange)/4), Y_ZERO+MARGIN);

		tmp += step;
		label  = Helper.formatDoubleAsString(tmp);
		drawLabelX(label.toCharArray(), 0, label.length(), POS_X_MIN+((7*gRange)/8), Y_ZERO+MARGIN);

//		label = ""+plotable.getXValueForPixel(gRange);
//		label  = renderNumber(slot.getMaximumValue().doubleValue());
		tmp += step;
		label  = Helper.formatDoubleAsString(tmp);
		drawLabelX(label.toCharArray(), 0, label.length(), POS_X_MAX, Y_ZERO+MARGIN);

	}

	// y
	private void drawOrdinate() {
//		int lx = POS_X_MIN;//+ (POS_X_MAX-POS_X_MIN)/2;
		int lx = (showDiffs? (POS_X_MIN+ (POS_X_MAX-POS_X_MIN)/2) : POS_X_MIN);
		
		int w = LINE_THICKNESS;
		int uy = Y_ONE;
		int h = Y_ZERO - Y_ONE +LINE_THICKNESS;
		
		// reset lastEnd.
		lastEndX = 0;

		g.setColor(Color.blue);
		g.fillRect(lx - LINE_THICKNESS / 2, uy + LINE_THICKNESS, w, h - LINE_THICKNESS * 2);
		g.fillPolygon(new int[] { lx - ARROW_SIZE, lx, lx + ARROW_SIZE}, new int[] { uy + ARROW_SIZE, uy, uy + ARROW_SIZE}, 3);

		
//		int xPos = (POS_X_MIN+POS_X_MAX)/2 + POS_X_MIN;
//		int xPos = POS_X_MIN;//lx + w/2;
		int xPos = lx;
		int yStep = (Y_ZERO - Y_ONE)/4;
		
		lastEndY = 0;
		
//		double fac = expManager.getCaseBaseSize() / sliderFactor;
		double fac = clsUsage / sliderFactor;
		
		Color tmpColor = g.getColor();
		g.setColor(Color.DARK_GRAY);

		String label = Helper.formatDoubleAsString((int)(1.0d * fac));
		drawLabelY(label.toCharArray(), 0, label.length(), xPos, Y_ONE);

		label = Helper.formatDoubleAsString((int)(0.75d * fac));
		drawLabelY(label.toCharArray(), 0, label.length(), xPos, Y_ONE+yStep);
		
		label = Helper.formatDoubleAsString((int)(0.5d * fac));
		drawLabelY(label.toCharArray(), 0, label.length(), xPos, Y_ZERO-2*yStep);

		label = Helper.formatDoubleAsString((int)(0.25d * fac));
		drawLabelY(label.toCharArray(), 0, label.length(), xPos, Y_ZERO-yStep);
		
		g.setColor(tmpColor);

	}
	

	private void drawLabelX(char[] txt, int iStart, int iEnd, int x, int y) {
		int txtLength = g.getFontMetrics().charsWidth(txt, iStart, iEnd);
		int xLabel = x - ((int) (txtLength/2));
		if (xLabel > lastEndX) {
			g.drawChars(txt, iStart, iEnd, xLabel, y);
			g.drawLine(x, Y_ZERO, x, Y_ZERO +3);
			
			lastEndX = xLabel + txtLength + MARGIN;
		}
	}


	private void drawLabelY(char[] txt, int iStart, int iEnd, int x, int y) {
		int txtHeight = g.getFontMetrics().getHeight();
		int yLabel = y + ((int) (txtHeight/2));
		if (yLabel > lastEndY) {
			g.drawChars(txt, iStart, iEnd, x+MARGIN/2, yLabel);
			g.drawLine(x-3, y, x, y);
			
			lastEndY = yLabel + txtHeight;// + MARGIN;
		}
	}

	public void smfHasChanged(boolean hasChanged) {
		repaint();
	}


	public void actionPerformed(ActionEvent e) {
		updateButtons();
	}
	
}
