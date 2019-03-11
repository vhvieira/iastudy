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
package de.dfki.mycbr.explanation;

import java.io.Serializable;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

import org.jdom.Element;
import org.jdom.filter.ElementFilter;

import de.dfki.mycbr.Helper;

public class ConceptExplanationScheme implements Serializable {
	/**
	 * 
	 */
	private static final long serialVersionUID = 7259223985271955804L;
	
	public static final String XML_TAG_EXP_SCHEME 		= "ExplanationScheme";
	public static final String XML_ATT_DESCRIPTION 		= "Description";
	public static final String XML_ATT_CONCEPT_ID 		= "conceptID";
	
	private String description = "";
	private List<KnowledgeSource> sources = new ArrayList<KnowledgeSource>();
	private ExplainableConcept concept;
	
	public ConceptExplanationScheme(ExplainableConcept concept) {
		this.concept = concept;
	}
	
	@SuppressWarnings("unchecked")
	public ConceptExplanationScheme(ExplainableConcept concept, Element element) {
		this.concept = concept;
		description = Helper.decode(element.getAttributeValue(XML_ATT_DESCRIPTION));
		
		for (Iterator it=element.getDescendants(new ElementFilter(KnowledgeSource.XML_TAG_KNOWLEDGE_SOURCE)); it.hasNext();) {
			Element sourceElement = (Element) it.next();
			KnowledgeSource source = new KnowledgeSource(sourceElement);
			sources.add(source);
		}
		
	}
	
	public String getDescription() {
		return description;
	}
	
	public void setDescription(String description) {
		this.description = description;
	}
	
	public List<KnowledgeSource> getSources() {
		return sources;
	}

	public ExplainableConcept getExplainableConcept() {
		return concept;
	}

	public Element toXML() {
		Element element = new Element(XML_TAG_EXP_SCHEME);
		element.setAttribute(XML_ATT_DESCRIPTION, Helper.encode(description));
		element.setAttribute(XML_ATT_CONCEPT_ID, Helper.encode(concept.getID()));
		
		for (KnowledgeSource source : sources) {
			Element sourceElement = source.toXML();
			element.addContent(sourceElement);
		}
		return element;
	}

	public Vector<URL> getResolvedKnowledgeSources(String conceptName) {
		Vector<URL> v = new Vector<URL>();
		for (KnowledgeSource ks : sources) {
			String urlStr = ks.resolve(conceptName);
			try {
				URL url = new URL(urlStr);
				v.add(url);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			}
		}
		return v;
	}
	
}
