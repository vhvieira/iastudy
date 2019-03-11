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

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.geom.GeneralPath;
import java.util.Collections;
import java.util.Iterator;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.JPanel;
import javax.swing.JSplitPane;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.model.similaritymeasures.HasChangedListenerSMF;
import de.dfki.mycbr.model.similaritymeasures.smftypes.Abstract_SMF_Number;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Number_Advanced;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.Widget_FunctionPlotter.Plotable;

/**
 * @author myCBR Team
 *
 */
public class SMFPanel_Number_Advanced extends SMFPanel implements HasChangedListenerSMF {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(SMFPanel_Number_Advanced.class.getName());

	public static int DRAGGING_MODE_X_ALLOWED	= 1;
	public static int DRAGGING_MODE_Y_ALLOWED	= 2;
	
	private class DraggedObject {
		Double value;
		int draggingMode;
		
		public final void setValue(Double value) {
			this.value = value;
		}

		public DraggedObject(Double value, int draggingMode) {
			this.value = value;
			this.draggingMode = draggingMode;
		}

		public final int getDraggingMode() {
			return draggingMode;
		}

		public final Double getValue() {
			return value;
		}
	}
	
	JPanel paSplitPane = new JPanel();

	Widget_Symmetry paSymmetry = new Widget_Symmetry();
	Widget_DiffOrQuotient paDiffOrQuotient = new Widget_DiffOrQuotient();

	private SMF_Number_Advanced smf;

	BorderLayout borderLayout1 = new BorderLayout();

	JSplitPane splitpane = new JSplitPane();

	BorderLayout borderLayout2 = new BorderLayout();

	private Number_Advanced_Widget paAdvanced;
	
	private Widget_FunctionPlotter plotter;
	
	private int selectedSpot = -1;
	
	public SMFPanel_Number_Advanced(SMF_Number_Advanced smf) {
		super(smf);
		this.smf = smf;
		log.fine("initialize SMFPanel_Integer_Advanced");
		paAdvanced = new Number_Advanced_Widget(smf);
		plotter = new Widget_FunctionPlotter(new Plotable() {
			private int gRange = 0;
			//private Integer intZero = new Integer(0);
			private double[] labelPositions = null;
			
			public void updateLabelPositions() {
				if (getSmf().getDiffOrQuotientMode() == Abstract_SMF_Number.MODE_QUOTIENT) {
					labelPositions = null;
					return;
				}
				
				double diff = getSmf().getDiff();
				labelPositions = new double[Widget_FunctionPlotter.DEFAULT_LABEL_POSITIONS.length];
				for (int i=0; i<labelPositions.length; i++) {
					double xVal = Widget_FunctionPlotter.DEFAULT_LABEL_POSITIONS[i] * diff;
					if (getSmf().isIntegerMode()) {
						xVal = (double) ((int) xVal);
					}
					labelPositions[i] = ((double) xVal) / diff;
				}
			}
			
			public void setGraphicalRange(int gRange) {
				this.gRange = gRange;
				updateLabelPositions();
			}

			public double getSimilarityForPixel(int pix) {
				//
				// The challenge is to create valid queries:
				// --  qVal and cVal must be in the range [minVal, maxVal]    --
				// --  and "all" possible combinations of q and c must occur  -- 
				// Solution: 
				// Start with q = minVal, c = maxVal
				// Increase q up to maxVal while keeping c constant.
				// Then keep q constant and decrease c down to minVal.
				//
				SMF_Number_Advanced smf = getSmf();
				double rel = (double) pix / (double) gRange;
				double diff = smf.getDiff();
				double c = smf.roundIfIntegerAndDiffMode(smf.getMinValue() + Math.min(diff, 2*rel*diff)); 
				double q = smf.roundIfIntegerAndDiffMode(smf.getMaxValue() - Math.max(0, -diff+2*rel*diff));
				
				double simVal = smf.getSimilarityBetween(q,c, null);
				return simVal;
			}

			public int[] getHighlightedSpots() {
				SMF_Number_Advanced smf = getSmf();

				double diff = smf.getDiff();
				int[] spots = new int[smf.getSamplingPoints().keySet().size()-1];
				
				if (smf.getDiffOrQuotientMode() == Abstract_SMF_Number.MODE_DIFFERENCE) {
					int index = 0;
					for (Iterator<Double> it = smf.getSamplingPoints().keySet().iterator(); it.hasNext();) {
						double i = it.next();
						if (i == 0) {
							continue;
						}
						spots[index++] = (int) ((gRange/2)+((i*gRange)/(diff*2)));
					}
				} else if (smf.getDiffOrQuotientMode() == Abstract_SMF_Number.MODE_QUOTIENT)  {
					int index = 0;
					for (Iterator<Double> it = smf.getSamplingPoints().keySet().iterator(); it.hasNext();) {
						double d = it.next();
						if (d == 1d) {
							continue;
						}
						spots[index++] = (int) (gRange+(smf.getDifferenceForQuotient(d)*gRange/diff))/2;
					}
				}

				return spots;
			}

			public double getXValueForPixel(int pix) {
				SMF_Number_Advanced smf = getSmf();
				double diff = smf.getDiff();
				double d = ((2 * diff * pix) / gRange) - diff;
				if (smf.getDiffOrQuotientMode() == Abstract_SMF_Number.MODE_DIFFERENCE) {
					return smf.roundIfIntegerMode(d);
				}
				return smf.getQuotientForDifference(d);
			}
			
			public void doubleClickedMouseAt(double xValue, double simVal) {
				log.fine("set value at x[" + xValue + "], simVal[" + simVal + "]");
				if (getSmf().getDiffOrQuotientMode() == Abstract_SMF_Number.MODE_DIFFERENCE) {
					xValue = getSmf().roundIfIntegerAndDiffMode(xValue); 
				}
				
				xValue = Helper.parseDouble(Helper.formatDoubleAsString(xValue));
				getSmf().setSamplingPoint(xValue, Helper.parseDouble(Helper.formatDoubleAsString(simVal)));

				double diff = getSmf().getDiff();
				selectedSpot = (int) ((xValue +diff) * (double) gRange / (2*diff));
				paAdvanced.selectSamplingPoint(xValue);
			}

			public void clickedMouseAt(double xObject, double simVal) {
				double x = (double)xObject;
				double diff = getSmf().getDiff();
				selectedSpot = (int) ((x +diff) * (double) gRange / (2*diff));

				Object selectedObject = getSamplingPointKeyAt(x, simVal);
				if (selectedObject == null) {
					selectedObject = getSamplingPointKeyAt(x);
				}
				if (selectedObject == null) {
					return;
				}

				paAdvanced.selectSamplingPoint((Double)selectedObject);
			}

			private Double getSamplingPointKeyAt(double x, double simVal) {
				double xMargin = Math.abs(getXValueForPixel(10)-getXValueForPixel(0));
				if (getSmf().getDiffOrQuotientMode()==Abstract_SMF_Number.MODE_QUOTIENT) xMargin = x/10;
				log.fine("looking for drag object in area of x[" + x + "], y[" + simVal + "]. xMargin[" + xMargin + "]");
				
				Double result = null;
				for (Iterator<Double> it = getSmf().getSamplingPoints().keySet().iterator(); it.hasNext();) {
					double currentSP = it.next();
					if (Math.abs(currentSP-x) > xMargin) {
						continue;
					}
					
					double sim = getSmf().getSamplingPoints().get(currentSP);
					if (Math.abs(sim-simVal) < 0.1) {
						if (result==null || Math.abs(result-x) > Math.abs(currentSP-x)) {
							result = currentSP;
						}
					}
				}
				return result;
			}
			
			private Double getSamplingPointKeyAt(double x) {
				double xMargin = Math.abs(getXValueForPixel(10)-getXValueForPixel(0));
				if (getSmf().getDiffOrQuotientMode() == Abstract_SMF_Number.MODE_QUOTIENT) {
					xMargin = x/10;
				}
				log.fine("looking for drag object in area of x[" + x + "], and dont care about y-value. xMargin[" + xMargin + "]");
				
				for (Iterator<Double> it = getSmf().getSamplingPoints().keySet().iterator(); it.hasNext();) {
					Double currentSP = it.next().doubleValue();
					if (Math.abs(currentSP)!=getSmf().getDiff() && currentSP!=0d && Math.abs(currentSP-x)<=xMargin) {
						return currentSP;
					}
				}
				return null;
			}
			
			public Object dragMouseAt(Object draggedObject, double xObject, double simVal) {
				SMF_Number_Advanced smf = getSmf();
				DraggedObject dO = (DraggedObject) draggedObject;
				Double xVal = smf.roundIfIntegerAndDiffMode(xObject);
//				if (smf.isIntegerMode() && smf.getDiffOrQuotientMode()==Abstract_SMF_Number.MODE_QUOTIENT) xVal=smf.getQuotientForDifference(new Long(Math.round(smf.getDifferenceForQuotient(xVal))).doubleValue()); 
				
				if (dO == null) {
					Double draggedObjectValue = getSamplingPointKeyAt(xVal, simVal);
					double diff = smf.getDiff();
					
					if (draggedObjectValue != null) {
						int draggingMode = DRAGGING_MODE_Y_ALLOWED;
						
						double tmp = draggedObjectValue;
						if (   		   (smf.getDiffOrQuotientMode() == Abstract_SMF_Number.MODE_QUOTIENT && tmp!=1d && tmp!=smf.getMaxValue()/smf.getMinValue() && tmp!=smf.getMinValue()/smf.getMaxValue())
									|| (smf.getDiffOrQuotientMode() == Abstract_SMF_Number.MODE_DIFFERENCE && tmp!=0d && Math.abs(tmp)!=diff)) {
							draggingMode |= DRAGGING_MODE_X_ALLOWED;
						}
						dO = new DraggedObject(draggedObjectValue, draggingMode);
					} else {
						// look for a sampling point to move without changing its y-value.
						draggedObjectValue = getSamplingPointKeyAt(xVal);
						if (draggedObjectValue != null) {
							double tmp = draggedObjectValue;
							if (   		(smf.getDiffOrQuotientMode() == Abstract_SMF_Number.MODE_QUOTIENT && tmp!=1d && tmp!=smf.getMaxValue()/smf.getMinValue() && tmp!=smf.getMinValue()/smf.getMaxValue())
								  	 || (smf.getDiffOrQuotientMode() == Abstract_SMF_Number.MODE_DIFFERENCE && tmp!=0d && Math.abs(tmp)!=diff)) {
								dO = new DraggedObject(draggedObjectValue, DRAGGING_MODE_X_ALLOWED);
							}
						}
					}
				}
				
				if (dO == null) return null;

				// check if another sampling point lies between new xVal and old xVal. do nothing in this case.
				Vector<Double> v = new Vector<Double>(smf.getSamplingPoints().keySet());
				if (v.indexOf(xVal)>=0 && (dO.getDraggingMode()&DRAGGING_MODE_X_ALLOWED)>0) {
					return dO;
				}
				v.add(xVal);
				Collections.sort(v);
				log.fine("index of xVal = [" + v.indexOf(xVal) + "], dO=[" + v.indexOf(dO.getValue()) + "]");
				int indexXVal = v.indexOf(xVal);
				int indexDO = v.indexOf(dO.getValue());
				if (Math.abs(indexXVal - indexDO)>1) {
					return dO;
				}

				if ((dO.getDraggingMode()&DRAGGING_MODE_X_ALLOWED) == 0) {
					// dragging x is NOT allowed
					xVal = dO.getValue();
				}
				if ((dO.getDraggingMode()&DRAGGING_MODE_Y_ALLOWED) == 0) {
					// dragging y is NOT allowed
					simVal = smf.getSamplingPoints().get(dO.getValue());
				}
				smf.removeSamplingPoint(dO.getValue());
				dO.setValue(xVal);
				smf.setSamplingPoint(dO.getValue(), Helper.parseDouble( Helper.formatDoubleAsString(simVal)));
				paAdvanced.selectSamplingPoint(xVal);
				
				return dO;
			}

			public double[] getLabelPositions() {
				return labelPositions;
			}

			public int getSelectedSpot() {
				return selectedSpot;
			}

			public boolean contains(double x, double y) {
				int[] xPoints = plotter.getXValues();
				int[] yPoints = plotter.getYValues();
					
				GeneralPath polyline = new GeneralPath(GeneralPath.WIND_EVEN_ODD, xPoints.length);
				polyline.moveTo (xPoints[0], yPoints[0]);
					
				for (int index = 1; index < xPoints.length; index++) {
					polyline.lineTo(xPoints[index],	yPoints[index]);
				}
					
				for (int index = xPoints.length-1; index > 0; index--) {
					polyline.lineTo(xPoints[index],new Float(yPoints[index]+1));
				}
					
				return polyline.contains(x,y);
				
			}

		});
		smf.addHasChangedListener(plotter, true);
		smf.addHasChangedListener(this, true);
		
		jbInit();
		customInit();
		
		paSymmetry.setSymmetrySelection(smf.isSymmetryMode());
		paDiffOrQuotient.setDiffOrQuotientMode(smf.getDiffOrQuotientMode());
		
		// only allow Quotient Mode if minimumValue > 0
		//paDiffOrQuotient.setVisible(true);
		paDiffOrQuotient.setEnabled(smf.checkQuotientModeAllowed());
		
	}

	private void customInit() {
		splitpane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitpane.setContinuousLayout(true);

		splitpane.setTopComponent(paAdvanced);
//		splitpane.setBottomComponent(plotter);
		JPanel panel = new JPanel(new GridBagLayout());
		panel.add(plotter, new GridBagConstraints(0,0, 1,1, 1d, 1d, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));

		splitpane.setBottomComponent(panel);
		splitpane.setDividerLocation(0.5);
		splitpane.setResizeWeight(0);

		// add smfunction as symmetry listener and diffOrQuotientMode listener
		paSymmetry.addSymmetryModeListener(smf);
		paDiffOrQuotient.addDiffOrQuotientModeListener(smf);
	}

	private void jbInit() {
		this.setLayout(new GridBagLayout());
		paSplitPane.setLayout(borderLayout2);
		this.add(paSplitPane, 		new GridBagConstraints(0,1, 2,1, 1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		this.add(paSymmetry, 		new GridBagConstraints(0,0, 1,1, 1.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		this.add(paDiffOrQuotient, 	new GridBagConstraints(1,0, 1,1, 1.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		paSplitPane.add(splitpane, BorderLayout.CENTER);
	}

	private SMF_Number_Advanced getSmf() {
		return smf;
	}

	public void smfHasChanged(boolean hasChanged) {
		if (hasChanged) {
			paDiffOrQuotient.setDiffOrQuotientMode(getSmf().getDiffOrQuotientMode());
			paDiffOrQuotient.setEnabled(smf.checkQuotientModeAllowed());
		}
	}

}
