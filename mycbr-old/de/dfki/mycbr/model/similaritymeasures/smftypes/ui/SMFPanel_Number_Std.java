/** MyCBR License 1.1

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
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.model.similaritymeasures.HasChangedListenerSMF;
import de.dfki.mycbr.model.similaritymeasures.smftypes.Abstract_SMF_Number;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Number_Std;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.Widget_FunctionPlotter.Plotable;

/**
 * @author myCBR Team
 *
 */
public class SMFPanel_Number_Std extends SMFPanel implements HasChangedListenerSMF {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;


	private final static Logger log = Logger.getLogger(SMFPanel_Number_Std.class.getName());
	

	JPanel paSplitPane = new JPanel();

	Widget_Symmetry paSymmetry = new Widget_Symmetry();
	Widget_DiffOrQuotient paDiffOrQuotient = new Widget_DiffOrQuotient();

	private SMF_Number_Std smf;

	BorderLayout borderLayout1 = new BorderLayout();

	JSplitPane splitpane = new JSplitPane();

	BorderLayout borderLayout2 = new BorderLayout();

	private Number_Standard_Widget paStandard;
	
	private Widget_FunctionPlotter plotter;
	
	public SMFPanel_Number_Std(SMF_Number_Std smf) {
		super(smf);
		this.smf = smf;
		log.fine("initialize SMFPanel_Integer_Standard.");
		paStandard = new Number_Standard_Widget(smf);
		paSymmetry.setSymmetrySelection(smf.isSymmetryMode());
		paDiffOrQuotient.setDiffOrQuotientMode(smf.getDiffOrQuotientMode());
		
		// only allow Quotient Mode if minimumValue > 0
		//paDiffOrQuotient.setVisible(true);
		paDiffOrQuotient.setEnabled(smf.checkQuotientModeAllowed());
		plotter = new Widget_FunctionPlotter(new Plotable() {
			private int gRange;
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
				SMF_Number_Std smf = getSmf();
				double rel = (double) pix / (double) gRange;
				double diff = smf.getDiff();
				double c = smf.roundIfIntegerAndDiffMode(smf.getMinValue() + Math.min(diff, 2*rel*diff)); 
				double q = smf.roundIfIntegerAndDiffMode(smf.getMaxValue() - Math.max(0, -diff+2*rel*diff));
				
				double simVal = smf.getSimilarityBetween(q,c, null);
				return simVal;
				
			}

			public int[] getHighlightedSpots() {
				return null;
			}

			public double getXValueForPixel(int pix) {
				SMF_Number_Std smf = getSmf();
				double diff = smf.getDiff();
				double d = ((2 * diff * pix) / gRange) - diff;
				if (smf.getDiffOrQuotientMode() == Abstract_SMF_Number.MODE_DIFFERENCE) {
					return smf.roundIfIntegerMode(d);
				}
				return smf.getQuotientForDifference(d);
			}

			public void doubleClickedMouseAt(double xValue, double simVal) {
				log.fine("set value at x[" + xValue + "], simVal[" + simVal + "]");
				SMF_Number_Std smf = getSmf();
				boolean isLeftSide = smf.isSymmetryMode() || (xValue<=0 && smf.getDiffOrQuotientMode()==Abstract_SMF_Number.MODE_DIFFERENCE) || (xValue<=1d && smf.getDiffOrQuotientMode()==Abstract_SMF_Number.MODE_QUOTIENT);
				int key = (isLeftSide ? smf.getCurrentFctLeft() : smf.getCurrentFctRight());
				
				if (key == SMF_Number_Std.FCT_STEP) {
					double value = (double) Math.abs(xValue);
					smf.setValue(key, value, isLeftSide);
				} else if (key == SMF_Number_Std.FCT_POLYNOMIAL) {
					double tmp = (getSmf().getDiffOrQuotientMode() == Abstract_SMF_Number.MODE_QUOTIENT ? smf.getDifferenceForQuotient(xValue) : xValue);
					double ratio = ((double) 1) - ((double) Math.abs(tmp)) 
												/ ((double) getSmf().getDiff());
					double power = Math.abs(Math.log(simVal) / Math.log(ratio));
					power = Helper.roundDouble(power, 2);
					smf.setValue(key, power, isLeftSide);
				} else if (key == SMF_Number_Std.FCT_SMOOTH_STEP) {
					double value = (double) Math.abs(xValue);
					smf.setValue(key, value, isLeftSide);
				} else if (key == SMF_Number_Std.FCT_CONST) {
					smf.setValue(key, Helper.parseDouble(Helper.formatDoubleAsString(simVal)), isLeftSide);
				}		
			}

			public void clickedMouseAt(double xObject, double simVal) {
				// no idea for a feature
			}

			private boolean drag_leftSide;

			public Object dragMouseAt(Object draggedObject, double xObject, double simVal) {
				double xVal = getSmf().roundIfIntegerAndDiffMode(xObject);
				
				double currentSimVal;
				double simMatchDist  = 0.1;
				double xValMatchDist; 
				boolean isLeftSide;
				if (getSmf().getDiffOrQuotientMode() == Abstract_SMF_Number.MODE_QUOTIENT) {
					xValMatchDist = (draggedObject == null ? xVal/10d : 0);
					currentSimVal = getSmf().getSimilarityBetween(1d, xVal, null);
					isLeftSide = (xObject<=1d);
				} else {
					xValMatchDist = (draggedObject == null ? Math.abs(getXValueForPixel(10)-getXValueForPixel(0)) : 0);
					currentSimVal = getSmf().getSimilarityBetween(0d, xVal, null);
					isLeftSide = (xObject <= 0d);
				}
				xVal = Math.abs(xVal);

				if (draggedObject == null) {
					drag_leftSide = isLeftSide;
					int currentFct = (isLeftSide? getSmf().getCurrentFctLeft(): getSmf().getCurrentFctRight());
					double[] usedMap = (isLeftSide? getSmf().getFctValsLeft(): getSmf().getFctValsRight());
					
					// check if mouse is in a close area, otherwise return null
					if (currentFct == SMF_Number_Std.FCT_CONST || currentFct == SMF_Number_Std.FCT_POLYNOMIAL) {
						if (Math.abs(simVal - currentSimVal) > simMatchDist) {
							return null; // no hit
						}
					} else if (currentFct == SMF_Number_Std.FCT_SMOOTH_STEP || currentFct == SMF_Number_Std.FCT_STEP) {
						double currentXVal = usedMap[currentFct];
						if (Math.abs(xVal - currentXVal) > xValMatchDist) {
							return null; // no hit
						}
					} else {
						// unknown fct
						return null;
					}
					draggedObject = SMF_Number_Std.fctToString(currentFct);
				}
				
				//
				// now start dragging work
				//
				if (drag_leftSide != isLeftSide) {
					return draggedObject;
				}
				if (draggedObject.equals(SMF_Number_Std.FCT_CONST_STR)) {
					getSmf().setValue(SMF_Number_Std.stringToFct((String)draggedObject),
							Helper.parseDouble( Helper.formatDoubleAsString(simVal)), isLeftSide);
				} else if (draggedObject.equals(SMF_Number_Std.FCT_POLYNOMIAL_STR)) {
					double power = -1;
					try {
						double tmp = (getSmf().getDiffOrQuotientMode()==Abstract_SMF_Number.MODE_QUOTIENT? getSmf().getDifferenceForQuotient(xVal): xVal);
						double ratio = ((double) 1) - ((double) Math.abs(tmp)) / ((double) getSmf().getDiff());
						power = Math.abs(Math.log(simVal) / Math.log(ratio));
						power = Helper.roundDouble(power, 2);
					} catch (Throwable e) {
						return null;
					}
					if (power == Double.POSITIVE_INFINITY) {
						return null;
					}
					getSmf().setValue(SMF_Number_Std.stringToFct((String) draggedObject), power, isLeftSide);
				} else if (draggedObject.equals(SMF_Number_Std.FCT_SMOOTH_STEP_STR) || draggedObject.equals(SMF_Number_Std.FCT_STEP_STR)) {
					getSmf().setValue(SMF_Number_Std.stringToFct((String) draggedObject), xVal, isLeftSide);
				}

				return draggedObject;
			}

			public double[] getLabelPositions() {
				return labelPositions;
			}

			public int getSelectedSpot() {
				return -1;
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

		try {
			jbInit();
			customInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void customInit() {
		splitpane.setOrientation(JSplitPane.VERTICAL_SPLIT);
		splitpane.setContinuousLayout(true);

		splitpane.setTopComponent(paStandard);
		splitpane.setBottomComponent(new JScrollPane(plotter));
		splitpane.setDividerLocation(0.5);
		splitpane.setResizeWeight(0);
		
		// add smf as symmetry listener
		paSymmetry.addSymmetryModeListener(smf);
		paDiffOrQuotient.addDiffOrQuotientModeListener(smf);
		smf.addHasChangedListener(this, true);
		smf.addHasChangedListener(plotter, true);
		
		// fill with data
		paStandard.refresh();
	}

	void jbInit() throws Exception {
		this.setLayout(new GridBagLayout());
		paSplitPane.setLayout(borderLayout2);
		paSplitPane.setBorder(BorderFactory.createEmptyBorder());
		this.add(paSplitPane, 		new GridBagConstraints(0,1, 2,1, 1.0,1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		this.add(paSymmetry, 		new GridBagConstraints(0,0, 1,1, 1.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		this.add(paDiffOrQuotient, 	new GridBagConstraints(1,0, 1,1, 1.0,0.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0,0,0,0), 0,0));
		paSplitPane.add(splitpane, BorderLayout.CENTER);
	}

	private SMF_Number_Std getSmf() {
		return smf;
	}
	
	public void smfHasChanged(boolean hasChanged) {
		if (hasChanged) {
			paStandard.refresh();
			paDiffOrQuotient.setDiffOrQuotientMode(getSmf().getDiffOrQuotientMode());
			paDiffOrQuotient.setEnabled(smf.checkQuotientModeAllowed());
		}
	}

}
