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
package de.dfki.mycbr.retrieval;

import java.io.Serializable;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Vector;

import javax.swing.tree.TreeNode;

import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.vocabulary.ModelInstance;

/**
 * @author myCBR Team
 * 
 *         TODO To change the template for this generated type comment go to
 *         Window - Preferences - Java - Code Style - Code Templates
 */
public class Explanation implements TreeNode, Serializable, Comparable<Object> {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private StringBuffer sb = new StringBuffer();

	Explanation parentExp = null;

	Object query;
	Object cbInstance;
	double similarity;
	double weight;
	// String aspect;
	AbstractSMFunction smf;
	Vector<Explanation> children = new Vector<Explanation>();
	HashMap<ModelInstance, Explanation> instToChildren = new HashMap<ModelInstance, Explanation>();

	private ModelInstance inst;

	public Explanation(ModelInstance inst, Object query, Object cbInstance,
			AbstractSMFunction usedSmf, double sim) {
		this(inst, query, cbInstance, usedSmf);
		this.similarity = sim;
	}

	public Explanation(ModelInstance inst, Object query, Object cbInstance,
			AbstractSMFunction usedSmf) {
		this.inst = inst;
		this.query = query;
		this.cbInstance = cbInstance;
		this.smf = usedSmf;
	}

	public void addExplanationChild(ModelInstance inst, Explanation exp) {
		exp.setParent(this);
		children.add(exp);
		instToChildren.put(inst, exp);
	}

	public Explanation getLocalExplanation(ModelInstance inst) {
		return (Explanation) instToChildren.get(inst);
	}

	private void setParent(Explanation parent) {
		this.parentExp = parent;
	}

	/**
	 * Add an explaining comment.
	 * 
	 * @param provenance
	 *            the component/attribute/thing whatever, which provided this
	 *            comment.
	 * @param comment
	 *            some explaining text
	 */
	public void addComment(String provenance, String comment) {
		sb.append("[ details ] " + comment + "\n");
	}

	public double getSimilarity() {
		return similarity;
	}

	public double getWeight() {
		return weight;
	}

	public void setSimilarity(double similarity) {
		this.similarity = similarity;
	}

	public void setWeight(double weight) {
		this.weight = weight;
	}

	public Object getCbInstance() {
		return cbInstance;
	}

	public Object getQuery() {
		return query;
	}

	public String getComments() {
		return sb.toString();
	}

	public String toString() {
		StringBuffer allSb = new StringBuffer();
		for (Enumeration<Explanation> en = children(); en.hasMoreElements();) {
			TreeNode node = (TreeNode) en.nextElement();
			allSb.append(" -- " + node.toString() + "\n");
		}
		allSb.append(sb);
		return allSb.toString();
	}

	// implementation of the interface TreeNode

	public int getChildCount() {
		return children.size();
	}

	public boolean getAllowsChildren() {
		return false;
	}

	public boolean isLeaf() {
		return children.size() == 0;
	}

	public Enumeration<Explanation> children() {
		return children.elements();
	}

	public TreeNode getParent() {
		return parentExp;
	}

	public TreeNode getChildAt(int index) {
		return (Explanation) children.get(index);
	}

	public int getIndex(TreeNode exp) {
		return children.indexOf(exp);
	}

	public final AbstractSMFunction getSmf() {
		return smf;
	}

	public ModelInstance getModelInstance() {
		return inst;
	}

	public Vector<Explanation> getChildren() {
		return children;
	}

	public int compareTo(Object o) {
		return Double.compare(similarity, ((Explanation) o).similarity);
	}

}
