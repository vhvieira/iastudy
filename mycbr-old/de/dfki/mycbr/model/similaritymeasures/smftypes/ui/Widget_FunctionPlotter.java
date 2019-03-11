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
package de.dfki.mycbr.model.similaritymeasures.smftypes.ui;

import java.awt.Color;
import java.awt.Cursor;
import java.awt.Graphics;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;

import javax.swing.FocusManager;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.model.similaritymeasures.HasChangedListenerSMF;


/**
 * @author myCBR Team
 * 
 */
public class Widget_FunctionPlotter extends javax.swing.JPanel implements HasChangedListenerSMF {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public static final double[] DEFAULT_LABEL_POSITIONS = {-1.0, -0.75, -0.5, -0.25, 0.0, 0.25, 0.5, 0.75, 1.0}; 
	
	/**
	 * The mapping from pixel to xValue of your similarity function
	 * has to be done in this interface. 
	 */
	public interface Plotable {
		/**
		 * This will be called to tell you the amount of pixels
		 * used to display your function (~ WIDTH of the used panel).
		 * Later on, the similarity value for each pixel will be
		 * gathered by calling getSimilarityForPixel().
		 * @param gRange amount of p
		 */
		public void setGraphicalRange(int gRange);
		
		/**
		 * Map the pixel (which is element of [0,gRange] proportionally
		 * to the range of the x values of your similarity function.
		 * Then return its similarity value.
		 * 
		 * @param pix the pixel (graphical position in the panel)
		 * @return Double similarity value element of [0,1]
		 */
		public double getSimilarityForPixel(int pix);

		/**
		 * Get the highlighted points along the x-axis. 
		 * the returned values are elements of [0,gRange].
		 * @return values in the range [0,gRange] to highlight.
		 */
		public int[] getHighlightedSpots();
		
		/**
		 * For graph editing (e.g. by DnD, double click, etc), a certain spot may be selected.
		 * Its selection will be shown visually.  
		 * @return a value in the range [0,gRange] or -1 if nothing is selected. 
		 */
		public int getSelectedSpot();

		public boolean contains(double x, double y);
		
		public double getXValueForPixel(int pix);

		/**
		 * 
		 * @param xObject Object calculated by getXValueForPixel method
		 * @param simVal
		 */
		public void doubleClickedMouseAt(double xObject, double simVal);

		public void clickedMouseAt(double xObject, double simVal);
		
		/**
		 * NOTE:
		 * draggedObject is null when dragging begins. So you have to determine an object that is close to the 
		 * given xObject and simVal. Once it is determined, adopt its values to the given xObject and simVal, and
		 * return the identified dragged object.
		 * This returned object will be given back to you as 'draggedObject' parameter in the next call.
		 * 
		 * @param draggedObject Object dragged object
		 * @param xObject
		 * @param simVal
		 * @return
		 */
		public Object dragMouseAt(Object draggedObject, double xObject, double simVal);

		/**
		 * The returned double[] describes the relative label positions at the X-axis 
		 * where a (numeric) label should appear.
		 * The function plotter then calls getXValueForPixel() for every (value*gRange)
		 * and displays it.
		 * 
		 * If null is returned, the default labeling will be made
		 * which corresponds to the values {-1.0, -0.75, -0.5, -0.25, 0.0, 0.25, 0.5, 0.75, 1.0}
		 *  
		 * @return null or double[] contains values in interval [-1.0,+1.0]
		 */
		public double[] getLabelPositions();
	
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

	/** the adapter to an object to be plotted. */
	private Plotable plotable;
	
	private int gRange = -1;
	
	private int[] xPoints;
	private int[] yPoints;
	
	private Object draggedObject = null;
	
	/** This is used for label painting (the numbers under the x-axis). We need to check for paint conflicts. */ 
	private int lastEndX = 0;
	private int lastEndY = 0;

	public Widget_FunctionPlotter(Plotable plotable) {
		super();
		this.plotable = plotable;
		
		this.addMouseMotionListener(new MouseMotionAdapter() {
			public void mouseMoved(MouseEvent e) {
				int x = e.getX()- POS_X_MIN;
				if (x>=0 && x<=gRange) {
					String simValStr = Helper.getSimilarityStr(Widget_FunctionPlotter.this.plotable.getSimilarityForPixel(x));
					String numVal = Helper.getSimilarityStr(Widget_FunctionPlotter.this.plotable.getXValueForPixel(x));
					setToolTipText("X[" + numVal + "], Y[" + simValStr + "]");
					
					if (Widget_FunctionPlotter.this.plotable.contains(e.getX(), e.getY())) {
						Widget_FunctionPlotter.this.setCursor (new Cursor (Cursor.HAND_CURSOR));
					} else {
						Widget_FunctionPlotter.this.setCursor (new Cursor (Cursor.DEFAULT_CURSOR));
					}
				} else {
					Widget_FunctionPlotter.this.setCursor (new Cursor (Cursor.DEFAULT_CURSOR));
				}
				
			}
			
			public void mouseDragged(MouseEvent e) {
				int x = e.getX() - POS_X_MIN;
				int y = e.getY();
				
				if (x >= 0 && x <= gRange && y >= Y_ONE && y <= Y_ZERO) {
					double simVal = ((double) (Y_ZERO - y)) / ((double) (Y_ZERO - Y_ONE));
					draggedObject = Widget_FunctionPlotter.this.plotable.dragMouseAt(draggedObject, Widget_FunctionPlotter.this.plotable.getXValueForPixel(x), simVal);
				} else {
					//
					// increase tolerance at border
					//
					if (x < 0) 		x = Math.min(x+MARGIN, 0);
					if (x > gRange) x = Math.max(x-MARGIN, gRange);
					if (y < Y_ONE) 	y = Math.min(y+MARGIN, Y_ONE);
					if (y > Y_ZERO) y = Math.max(y-MARGIN, Y_ZERO);
					if (x >= 0 && x <= gRange && y >= Y_ONE && y <= Y_ZERO) {
						double simVal = ((double) (Y_ZERO - y)) / ((double) (Y_ZERO - Y_ONE));
						draggedObject = Widget_FunctionPlotter.this.plotable.dragMouseAt(draggedObject, Widget_FunctionPlotter.this.plotable.getXValueForPixel(x), simVal);
					}
				}
			}
		});
		
		this.addMouseListener(new MouseAdapter() {
			public void mouseClicked(MouseEvent e) {
				int x = e.getX() - POS_X_MIN;
				int y = e.getY();
				if (x >= 0 && x <= gRange && y >= Y_ONE && y <= Y_ZERO) {
					double simVal = ((double) (Y_ZERO - y)) / ((double) (Y_ZERO - Y_ONE));
					
					double xVal = Widget_FunctionPlotter.this.plotable.getXValueForPixel(x);
					if (e.getClickCount()>=2) {
						Widget_FunctionPlotter.this.plotable.doubleClickedMouseAt(xVal, simVal);
					} else {
						Widget_FunctionPlotter.this.plotable.clickedMouseAt(xVal, simVal);
					}
				}
			}
			
			public void mouseReleased(MouseEvent e) {
				draggedObject=null;
			}
			
			public void mousePressed(MouseEvent arg0) {
				FocusManager.getCurrentKeyboardFocusManager().clearGlobalFocusOwner();
			}
		});
	}

	public void paint(Graphics g) {
		super.paint(g);
		if (plotable == null) {
			return;
		}
		
		this.g = g;

		int newY_ZERO = getSize().height - MARGIN * 2;
		Y_ONE = Math.min(MARGIN, newY_ZERO - 2 * MARGIN);

		POS_X_MIN = MARGIN * 2;
		POS_X_MAX = getSize().width - MARGIN * 2;

		// define graphical range
		int newGRange = POS_X_MAX - POS_X_MIN;
		if (newGRange <= 0) {
			return;
		}
		if (newGRange != gRange || newY_ZERO != Y_ZERO) {
			// set the current graphical range to plotable.
			this.gRange = newGRange;
			this.Y_ZERO = newY_ZERO;
			
			plotable.setGraphicalRange(gRange);
			int tmp = gRange + 3;
			xPoints = new int[tmp];
			yPoints = new int[tmp];
			xPoints[0] = POS_X_MIN;
			yPoints[0] = Y_ZERO;
			xPoints[tmp - 1] = POS_X_MAX;
			yPoints[tmp - 1] = Y_ZERO;
		}


		drawHeaders();
		drawOrdinate();
		drawAbscissa();

		drawGraph();
	}

	// y
	private void drawOrdinate() {
		int lx = MARGIN;
		int w = getSize().width - MARGIN * 2;
		int uy = Y_ZERO;
		int h = LINE_THICKNESS;

		g.setColor(Color.blue);

		g.fillRect(lx + LINE_THICKNESS, uy, w - LINE_THICKNESS * 2, h);

		g.fillPolygon(new int[] { lx + ARROW_SIZE, lx, lx + ARROW_SIZE }, new int[] { uy + ARROW_SIZE, uy, uy - ARROW_SIZE }, 3);
		g.fillPolygon(new int[] { lx + w - ARROW_SIZE, lx + w, lx + w - ARROW_SIZE}, new int[] { uy + ARROW_SIZE, uy, uy - ARROW_SIZE }, 3);

		
		int xPos = lx + w/2;
		int yStep = (Y_ZERO - Y_ONE)/4;
		
		lastEndY = 0;
		
		String label = "1.0";
		drawLabelY(label.toCharArray(), xPos, Y_ONE);

		label = "0.75";
		drawLabelY(label.toCharArray(), xPos, Y_ONE+yStep);
		
		label = "0.5";
		drawLabelY(label.toCharArray(), xPos, Y_ZERO-2*yStep);

		label = "0.25";
		drawLabelY(label.toCharArray(), xPos, Y_ZERO-yStep);
	}

	// x
	private void drawAbscissa() {
		int lx = POS_X_MIN+ (POS_X_MAX-POS_X_MIN)/2;
		int w = LINE_THICKNESS;
		int uy = Y_ONE;
		int h = Y_ZERO - Y_ONE +LINE_THICKNESS;
		
		// reset lastEnd.
		lastEndX = 0;

		g.setColor(Color.blue);
		g.fillRect(lx - LINE_THICKNESS / 2, uy + LINE_THICKNESS, w, h - LINE_THICKNESS * 2);
		g.fillPolygon(new int[] { lx - ARROW_SIZE, lx, lx + ARROW_SIZE}, new int[] { uy + ARROW_SIZE, uy, uy + ARROW_SIZE}, 3);


		double[] labelPositions = plotable.getLabelPositions();
		if (labelPositions == null) {
			labelPositions = DEFAULT_LABEL_POSITIONS;
		}

		double posL = ((labelPositions[0]+1d)/2d);
		int pixL = (int) (posL*gRange);
		double lDouble = plotable.getXValueForPixel(pixL);
		int normalizer = ((labelPositions.length+1)/2)-1;
		
		// draw other labels
		double currentDouble = 0;
		String label = "";
		for(int i=0; i<labelPositions.length; i++ ){
			
			double tmp = ((labelPositions[i]+1d)/2d);
			int range = (int) (tmp*gRange);
			currentDouble = lDouble-(lDouble/normalizer*i);
			label  = Helper.getSimilarityStr(currentDouble);
			drawLabelX(label.toCharArray(), POS_X_MIN+range, Y_ZERO+MARGIN);
		
		}
	}

	/**
	 * Draws the graph
	 */
	private void drawGraph() {
		for (int x = 0; x <= gRange; x++) {
			// calculate y-value for all pixels along this range
			double simVal = plotable.getSimilarityForPixel(x); 
			
			if (simVal < 0 || simVal>1) {
				continue;
			}
			xPoints[x + 1] = POS_X_MIN + x;
			yPoints[x + 1] = valToPixY(simVal);
		}
		g.drawPolyline(xPoints, yPoints, gRange+3);
	}

	/**
	 * Draws the header of the graph
	 */
	private void drawHeaders() {
		Color tmpColor = g.getColor();
		g.setColor(Color.DARK_GRAY);
		String headerLeft  = "case < query";
		String headerRight = "case > query";
		
		g.drawChars(headerLeft.toCharArray(), 0, headerLeft.length(), POS_X_MIN+MARGIN, MARGIN);
		g.drawChars(headerRight.toCharArray(), 0, headerRight.length(), POS_X_MAX-MARGIN-headerRight.length()*8, MARGIN);
		
		g.setColor(tmpColor);
	}

	/**
	 * Draws the given label near the x-axis to the specified position
	 * @param txt the label to be drawn
	 * @param x the x coordinate for the label
	 * @param y the y coordinate for the label
	 */
	private void drawLabelX(char[] txt, int x, int y) {
		int txtLength = g.getFontMetrics().charsWidth(txt, 0, txt.length);
		int xLabel = x - ((int) (txtLength/2));
		if (xLabel > lastEndX) {
			g.drawChars(txt, 0, txt.length, xLabel, y); // draw label
			g.drawLine(x, Y_ZERO, x, Y_ZERO -3); // draw tiny line on axis
			
			lastEndX = xLabel + txtLength + MARGIN;
		}
	}

	/**
	 * Draws the given label near the y-axis to the specified position
	 * @param txt the label to be drawn
	 * @param x the x coordinate for the label
	 * @param y the y coordinate for the label
	 */
	private void drawLabelY(char[] txt, int x, int y) {
		int txtHeight = g.getFontMetrics().getHeight();
		int yLabel = y + ((int) (txtHeight/2));
		if (yLabel > lastEndY) {
			g.drawChars(txt, 0, txt.length, x+MARGIN, yLabel); // draw label
			g.drawLine(x-3, y, x +3, y); // draw tiny line on axis
			lastEndY = yLabel + txtHeight;
		}
	}

	/**
	 * Repaints this when the underlying similarity function changes
	 */
	public void smfHasChanged(boolean hasChanged) {
		repaint();
	}	

	/**
	 * Returns an array of x values which are used to
	 * plot the given function
	 * @return x values
	 */
	public int[] getXValues(){
		return xPoints;
	}
	
	/**
	 * Returns an array of y values which are used to
	 * plot the given function
	 * @return y values
	 */
	public int[] getYValues(){
		return yPoints;
	}
	
	/**
	 * Returns the pixel (in y direction) that the given similarity value is associated with
	 * @param simVal the similarity value for which a pixel should be returned
	 * @return the pixel (y coordinate) for the given similarity value
	 */
	private int valToPixY(double simVal) {
		int result = 0;

		int length = Y_ZERO - Y_ONE;
		result = Y_ZERO - new Long(Math.round(simVal * length)).intValue();

		return result;
	}
}
