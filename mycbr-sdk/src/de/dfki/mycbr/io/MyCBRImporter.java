/*
 * myCBR License 3.0
 * 
 * Copyright (c) 2006-2015, by German Research Center for Artificial Intelligence (DFKI GmbH), Germany
 * 
 * Project Website: http://www.mycbr-project.net/
 * 
 * This library is free software; you can redistribute it and/or modify 
 * it under the terms of the GNU Lesser General Public License as published by 
 * the Free Software Foundation; either version 3 of the License, or 
 * (at your option) any later version.
 * 
 * This library is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. 
 * See the GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this library; if not, write to the Free Software Foundation, Inc., 
 * 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
 * 
 * Oracle and Java are registered trademarks of Oracle and/or its affiliates. 
 * Other names may be trademarks of their respective owners.
 * 
 * endOfLic */

package de.dfki.mycbr.io;

import java.io.File;
import java.text.ParseException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.dfki.mycbr.core.ICaseBase;
import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Attribute;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.casebase.MultipleAttribute;
import de.dfki.mycbr.core.casebase.SpecialAttribute;
import de.dfki.mycbr.core.casebase.SymbolAttribute;
import de.dfki.mycbr.core.model.AttributeDesc;
import de.dfki.mycbr.core.model.BooleanDesc;
import de.dfki.mycbr.core.model.Concept;
import de.dfki.mycbr.core.model.ConceptDesc;
import de.dfki.mycbr.core.model.DoubleDesc;
import de.dfki.mycbr.core.model.FloatDesc;
import de.dfki.mycbr.core.model.IntegerDesc;
import de.dfki.mycbr.core.model.SimpleAttDesc;
import de.dfki.mycbr.core.model.StringDesc;
import de.dfki.mycbr.core.model.SymbolDesc;
import de.dfki.mycbr.core.similarity.AdvancedFloatFct;
import de.dfki.mycbr.core.similarity.AdvancedIntegerFct;
import de.dfki.mycbr.core.similarity.AmalgamationFct;
import de.dfki.mycbr.core.similarity.FloatFct;
import de.dfki.mycbr.core.similarity.ISimFct;
import de.dfki.mycbr.core.similarity.IntegerFct;
import de.dfki.mycbr.core.similarity.OrderedSymbolFct;
import de.dfki.mycbr.core.similarity.Similarity;
import de.dfki.mycbr.core.similarity.StringFct;
import de.dfki.mycbr.core.similarity.SymbolFct;
import de.dfki.mycbr.core.similarity.TaxonomyFct;
import de.dfki.mycbr.core.similarity.TaxonomyNode;
import de.dfki.mycbr.core.similarity.config.AmalgamationConfig;
import de.dfki.mycbr.core.similarity.config.MultipleConfig;
import de.dfki.mycbr.core.similarity.config.MultipleConfig.MainType;
import de.dfki.mycbr.core.similarity.config.MultipleConfig.Reuse;
import de.dfki.mycbr.core.similarity.config.MultipleConfig.Type;
import de.dfki.mycbr.core.similarity.config.DistanceConfig;
import de.dfki.mycbr.core.similarity.config.NumberConfig;
import de.dfki.mycbr.core.similarity.config.StringConfig;
import de.dfki.mycbr.core.similarity.config.TaxonomyConfig;
import de.dfki.mycbr.util.Pair;

/**
 * Imports myCBR files version 2.6.4 or higher. First tries to import the
 * concepts from the inheritance similarity then imports the attributes from
 * _CASEBASE.XML file. Then imports similarity functions and the case base.
 * 
 * @author myCBR Team
 * 
 */
public class MyCBRImporter extends DefaultHandler {

    private String path;
    private ICaseBase cb;
    private boolean checkedVersion;
    private Project prj;

    private Concept currentConceptDescription;
    private SymbolAttribute currentInheritanceNode;
    private TaxonomyFct currentInheritanceFct;

    /**
     * Initializes this with the given project, path and name. Adds a new case
     * base to this project.
     * 
     * @param project
     *            the project whose model should be imported
     * @throws Exception
     */
    public MyCBRImporter(Project project) throws Exception {
        this.path = project.getPath() + project.getName();
        int index = this.path.lastIndexOf(File.separator);
        String name = this.path;
        if (index != -1) {
            name = name.substring(index + 1, name.length());
        }
        project.setName(name);
        this.prj = project;
        cb = prj.createDefaultCB("CaseBase" + project.getCaseBases().size());
    }

    /**
     * Imports the model from the specified XML files to the given
     * project. The model contains the concepts hierarchy structure,
     * the c's attributes and the corresponding similarity functions. 
     */
    public void doImport() {

        // first check version of the files
        try {
            SAXParserFactory factory = SAXParserFactory.newInstance();
            SAXParser saxParser = factory.newSAXParser();

            // first parse SMF file to get concepts out of inheritance
            // function
            saxParser.parse("file:" + File.separator + File.separator + File.separator + path + "_CBR_SMF.XML", this);

        } catch (Exception e) {
            e.printStackTrace();
        }

        // then start to import
        ConceptImporter conceptImporter = new ConceptImporter();
        conceptImporter.doImport();
        AttributeImporter attImporter = new AttributeImporter();
        attImporter.doImport();
        FunctionImporter functionImporter = new FunctionImporter();
        functionImporter.doImport();

        // remove default function if another function already contained
        cleanDefaultFcts();

    }

    /**
	 * For each concept description there has to be at least one 
     * similarity function. When creating a concept description,
     * the constructor therefore creates a default function.
     * These functions have to be removed from each concept description
     * after importing all similarity functions.
     * 
	 */
    private void cleanDefaultFcts() {
    	for (Concept c: prj.getAllSubConcepts().values()) {
    		if (c.getAvailableAmalgamFcts().size()>1) {
    			c.deleteAmalgamFct(c.getFct(Project.DEFAULT_FCT_NAME));
    			
    			for (AmalgamationFct f: c.getAvailableAmalgamFcts()) {
    				for (AttributeDesc d: c.getAllAttributeDescs().values()) {
    					if (d instanceof ConceptDesc) {
    						f.setActiveFct(d, ((ConceptDesc)d).getConcept().getActiveAmalgamFct());
    					} else {
    						if (((ISimFct)f.getActiveFct(d)).getName().equals(Project.DEFAULT_FCT_NAME)
    								) {
    							for (ISimFct localFct: ((SimpleAttDesc)d).getSimFcts()) {
    								if (!localFct.getName().equals(Project.DEFAULT_FCT_NAME)) {
    									f.setActiveFct(d, localFct);
    									break;
    								}
    							}
    						}
    					}
    				}
    			}
    		}
    		for (AttributeDesc d: c.getAttributeDescs().values()) {
    			if (d instanceof SimpleAttDesc) {
    				SimpleAttDesc sa = (SimpleAttDesc)d;
    				if (sa.getSimFcts().size()>1) {
    					sa.deleteSimFct(sa.getFct(Project.DEFAULT_FCT_NAME));
    	    		}
    			}
    		}
    	}
    }

    /**
     * Checks whether the given myCBR files have the correct version. Version
     * 2.6.4 or higher is supported.
     * 
     * @param version
     *            the version of the XML file
     * @return true if version is greater than or equal 2.6.4, false otherwise
     */
    private boolean checkVersion(String version) {

        String[] parts = version.split("\\.");

        if (parts.length == 3) {
            if (Integer.parseInt(parts[0]) > 2) {
                return true;
            } else if (Integer.parseInt(parts[0]) == 2) {
                if (Integer.parseInt(parts[1]) > 6) {
                    return true;
                } else if (Integer.parseInt(parts[1]) == 6) {
                    return Integer.parseInt(parts[2]) >= 4;
                }
            }
        }
        return false;
    }

    public void startElement(String namespaceURI, String localName,
            String qName, Attributes atts) throws SAXException {

        // check whether the files have correct version
        if ("Document".equals(qName)) {
            String version = atts.getValue("MyCBR_version");
            if (version == null) {
                throw new SAXException(
                        "Could not determine version of myCBR files!");
            }
            checkedVersion = checkVersion(version);

            if (!checkedVersion) {
                throw new SAXException(
                        "Version of myCBR files must be 2.6.4 or higher!");
            }
        }

    }

    // ///////////////////////////////////////////////////////////////
    // /////////////////// Concept Importer //////////////////////////
    // ///////////////////////////////////////////////////////////////

    /**
     * Imports concepts and inheritance structure.
     */
    private class ConceptImporter extends DefaultHandler {

        private boolean importingInhMeasure = false;

        /**
         * Imports concepts from project's XML file *_CBR_SMF.XML
         */
        public void doImport() {
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();

                // first parse SMF file to get concepts out of inheritance
                // function
                saxParser.parse("file:" + File.separator + File.separator + File.separator + path + "_CBR_SMF.XML", this);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void startElement(String namespaceURI, String localName,
                String qName, Attributes atts) throws SAXException {

            // import concepts
            if ("InheritanceMeasure".equals(qName)) {
                if (("_IMF_INTERNAL_SMF_".equals(atts.getValue("smfname")))
                        && ("_FAKESLOT_IMF_".equals(atts
                                .getValue("model_instname")))) {
                    String name = atts.getValue("topCls");
                    try {
                        // add top c
                        currentConceptDescription = prj.createTopConcept(name);
                    } catch (Exception e) {
                        e.printStackTrace();
                        return;
                    }
                    currentInheritanceFct = prj.getInhFct();
                    currentInheritanceFct.setSymmetric(false);
                    currentInheritanceNode = (SymbolAttribute) currentInheritanceFct
                            .getDesc().getAttribute(name);
                }
                importingInhMeasure = true;
            } else if (importingInhMeasure) {
                if ("RootSymbol".equals(qName)) {
                    // set node similarity
                    currentInheritanceFct.setNodeSimilarity(
                            currentInheritanceNode, Similarity.get(Double
                                    .parseDouble(atts.getValue("simVal"))));
                } else if ("ChildSymbol".equals(qName)) {
                    try {
                        currentConceptDescription = new Concept(atts
                                .getValue("symbol"), prj,
                                currentConceptDescription);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    currentInheritanceNode = (SymbolAttribute) currentInheritanceFct
                            .getDesc().getAttribute(atts.getValue("symbol"));
                    currentInheritanceFct.setNodeSimilarity(
                            currentInheritanceNode, Similarity.get(Double
                                    .parseDouble(atts.getValue("simVal"))));
                } else if ("InnerNodesConfig".equals(qName)) {
                    TaxonomyConfig config = TaxonomyConfig.NO_INNERNODES;

                    if (!"no".equals(atts.getValue("has_inner_values"))) {
                        config = TaxonomyConfig.INNER_NODES_ANY;
                        if (!"any_value"
                                .equals(atts.getValue("inner_semantic"))) {
                            config = TaxonomyConfig.INNER_NODES_OPTIMISTIC;
                            if ("average".equals(atts.getValue("uncertain"))) {
                                config = TaxonomyConfig.INNER_NODES_AVERAGE;
                            } else if ("pessimistic".equals(atts
                                    .getValue("uncertain"))) {
                                config = TaxonomyConfig.INNER_NODES_PESSIMISTIC;
                            }
                        }
                    }
                    try {
                        if ("query".equals(atts.getValue("scope"))) {
                            currentInheritanceFct.setQueryConfig(config);
                        } else {
                            currentInheritanceFct.setCaseConfig(config);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }

        }

        public void endElement(String namespaceURI, String localName,
                String qName) {
            if ("InheritanceMeasure".equals(qName)) {
                if (currentInheritanceFct.getCaseConfig() == currentInheritanceFct
                        .getQueryConfig()) {
                    currentInheritanceFct.setSymmetric(true);
                }
                currentInheritanceFct = null;
                currentConceptDescription = null;
                currentInheritanceNode = null;
                importingInhMeasure = false;
            } else if ("ChildSymbol".equals(qName)) {
                if (importingInhMeasure) {
                    currentConceptDescription = currentConceptDescription
                            .getSuperConcept();
                    if (currentConceptDescription != null) {
                        currentInheritanceNode = (SymbolAttribute) currentInheritanceFct
                                .getDesc().getAttribute(
                                        currentConceptDescription.getName());
                    }
                }
            }
        }

    }

    // ///////////////////////////////////////////////////////////////
    // ////////////////// SimpleAttribute Importer /////////////////////////
    // ///////////////////////////////////////////////////////////////

    /**
     * Imports the attributes
     */
    private class AttributeImporter extends DefaultHandler {

        private boolean importingAtts;
        private Concept desc;
        private Instance currentCase;
        private HashMap<Instance, Pair<ConceptDesc, String>> references = new HashMap<Instance, Pair<ConceptDesc, String>>();

        /**
         * Imports attributes from project's XML file *_CBR_CASEBASE.XML
         */
        public void doImport() {
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();

                File f = new File(path + "_CBR_CASEBASE.XML");
                if (f.exists()) { // it might be that there is no case base to
                                  // be imported!
                    // parse case file to add attributes and cases
                    saxParser.parse(f, this);

                    // now set references to instances
                    for (Map.Entry<Instance, Pair<ConceptDesc, String>> entry : references
                            .entrySet()) {
                        ConceptDesc cDesc = entry.getValue().getFirst();
                        Instance i = prj.getInstance(entry.getValue()
                                .getSecond());

                        entry.getKey().addAttribute(cDesc, i);
                    }
                } else {
                	System.err.println("Could not find file " + path + "_CBR_CASEBASE.XML");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        private String conceptName = "";

        @SuppressWarnings("unchecked")
        public void startElement(String namespaceURI, String localName,
                String qName, Attributes atts) throws SAXException {

            // import attribute descriptions
            if ("Prototype".equals(qName)) {
                importingAtts = true;
            } else if ("Instances_for_Class".equals(qName)) {
                desc = prj.getConceptByID(atts.getValue("class"));
                conceptName = atts.getValue("class");
            }

            if ("slotvalue".equals(qName)) {

                if (importingAtts) {
                    if (desc == null) {
                        System.err
                                .println("Error during import: concept with name \""
                                        + conceptName + "\" unknown");
                        return;
                    }
                    String type = atts.getValue("value");
                    AttributeDesc newDesc = null;
                    try {
                    	if (desc.hasAttributeDesc(atts.getValue("slot"))) {
                    		return; // attribute has been inherited
                    	}
                        if ("Integer".equals(type)) {
                            Integer min = Integer.parseInt(atts
                                    .getValue("minval"));
                            Integer max = Integer.parseInt(atts
                                    .getValue("maxval"));
                            newDesc = new IntegerDesc(desc, atts
                                    .getValue("slot"), min, max);
                        } else if ("Float".equals(type)) {
                            Float min = Float.parseFloat(atts
                                    .getValue("minval"));
                            Float max = Float.parseFloat(atts
                                    .getValue("maxval"));
                            newDesc = new FloatDesc(desc,
                                    atts.getValue("slot"), min, max);
                        } else if ("Double".equals(type)) {
                        	Double min = Double.parseDouble(atts
                                    .getValue("minval"));
                        	Double max = Double.parseDouble(atts
                                    .getValue("maxval"));
                            newDesc = new DoubleDesc(desc,
                                    atts.getValue("slot"), min, max);
                        } else if ("Symbol".equals(type)) {
                            HashSet<String> info = new HashSet<String>(Arrays
                                    .asList(atts.getValue("allowed_values")
                                            .split(";")));
                            newDesc = new SymbolDesc(desc, atts
                                    .getValue("slot"), info);
                        } else if ("Boolean".equals(type)) {
                            newDesc = new BooleanDesc(desc, atts
                                    .getValue("slot"));
                        } else if ("Instance".equals(type)) {
                            try {
                                newDesc = new ConceptDesc(desc, atts
                                        .getValue("slot"), prj
                                        .getConceptByID(atts
                                                .getValue("allowed_values")));
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        } else if ("String".equals(type)) {
                            newDesc = new StringDesc(desc, atts
                                    .getValue("slot"));
                        }
                        if (newDesc != null) {
                            String isMultiple = atts.getValue("multiple");
                            if ("true".equals(isMultiple)) {
                                newDesc.setMultiple(true);
                            }
                        }

                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else { // importing cases
                    String valueAsString = atts.getValue("value");
                    Object value = valueAsString;

                    AttributeDesc attDesc = currentCase.getConcept()
                            .getAttributeDescs().get(atts.getValue("slot"));
                    if (attDesc==null) {
                    	return; // TODO
                    }
                    if (!prj.isSpecialAttribute(valueAsString)) {
	                    if (attDesc instanceof FloatDesc) {
	                        value = Float.parseFloat(valueAsString);
	                    } else if (attDesc instanceof IntegerDesc) {
	                        value = Integer.parseInt(valueAsString);
	                    } else if (attDesc instanceof BooleanDesc) {
	                        value = Boolean.parseBoolean(valueAsString);
	                    }
                    }
                    // if desc is multiple we have to add multiple attribute
                    // with list of all values
                    // only if value is not a special value
                    if (attDesc.isMultiple() && !valueAsString.startsWith("_")) {
                        MultipleAttribute<AttributeDesc> att = null;
                        // get current list of values
                        Attribute a = currentCase.getAttForDesc(attDesc);
                        if (a instanceof SpecialAttribute) {
                            LinkedList<Attribute> attList = new LinkedList<Attribute>();
                            att = new MultipleAttribute<AttributeDesc>(attDesc,
                                    attList);
                        } else if (a instanceof MultipleAttribute<?>) {
                            att = (MultipleAttribute<AttributeDesc>) a;
                        }
                        // and add value to list
                        try {
                            att.addValue(attDesc.getAttribute(value));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                        value = att;
                    } else if ((attDesc instanceof ConceptDesc)
                            && (!valueAsString.startsWith("_"))) {
                        // found instance reference
                        // has to be set later on, because
                        // referenced case might not have been imported yet!
                        Pair<ConceptDesc, String> p = new Pair<ConceptDesc, String>(
                                (ConceptDesc) attDesc, valueAsString);
                        references.put(currentCase, p);
                        return;
                    }
                    try {
                        currentCase.addAttribute(attDesc, value);
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            } else { // found new case
                if ("Instance".equals(qName)) {
                    if (desc == null) {
                        System.err
                                .println("Error during import: concept with name \""
                                        + conceptName + "\" unknown");
                        return;
                    }
                    Instance i;
					try {
						String name = atts.getValue("model_instname");
						i = desc.addInstance(name);
						cb.addCase(i);
						currentCase = i;
                    } catch (Exception e) {
                       	e.printStackTrace();
                    }
                }
            }

        }

        public void endElement(String namespaceURI, String localName,
                String qName) {
            if ("Prototype".equals(qName)) {
                importingAtts = false;
            }
        }

    }

    // ///////////////////////////////////////////////////////////////
    // ////////////////// Function Importer //////////////////////////
    // ///////////////////////////////////////////////////////////////

    /**
     * Importer for similarity functions
     */
    private class FunctionImporter extends DefaultHandler {

        private AttributeDesc desc;
        private Concept c;

        private Object fct;
        private String currentSymbol;
        private boolean importingTaxonomy = false;
        private boolean importingTable = false;
        private boolean importingSV = false;

        private Object currentNode;

        /**
         * Imports functions from project's XML file *_CBR_SMF.XML
         */
        public void doImport() {
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();

                // parse smf file once again to import other smf
                saxParser.parse("file:" + File.separator + File.separator + File.separator + path + "_CBR_SMF.XML", this);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        public void startElement(String namespaceURI, String localName,
                String qName, Attributes atts) throws SAXException {

            // import functions
            if ("SMFunction".equals(qName)
                    && !("NoType".equals(atts.getValue("type")))) {
                String slotName = atts.getValue("model_instname");
                String type = atts.getValue("type");
                String simMode = atts.getValue("simMode");
                String smfname = atts.getValue("smfname");
                List<AttributeDesc> attDescs = prj.getAttDescsByName(slotName);
                if ("Script".equals(simMode)) {
                    System.err
                            .println("Given similarity function cannot be imported: script");
                } else if ("External".equals(simMode)) {
                    System.err
                            .println("Given similarity function cannot be imported: external");
                } else if (attDescs.size() >= 1) {
                    desc = attDescs.get(0);
                    if ("Symbol".equals(type)) {
                        if ("Table".equals(simMode)) {
                            fct = ((SymbolDesc) desc).addSymbolFct(smfname,
                                    true);

                            importingTable = true;
                        } else if ("Taxonomy".equals(simMode)) {
                            fct = ((SymbolDesc) desc).addTaxonomyFct(smfname,
                                    true);
                            importingTaxonomy = true;
                            currentNode = desc;
                        } else if ("Ordered".equals(simMode)) {
                            boolean isCyclic = Boolean.parseBoolean(atts
                                    .getValue("isCyclic"));
                            OrderedSymbolFct f = ((SymbolDesc) desc)
                                    .addOrderedSymbolFct(smfname, true);

                            f.setCyclic(isCyclic);
                            f.setSymmetric(false);
                            // attribute distLastFirst follows from given
                            // indexes?
                            fct = f;

                        }
                    } else if ("Integer".equals(type)) {
                        if ("Advanced".equals(simMode)) {
                            fct = ((IntegerDesc) desc).addAdvancedIntegerFct(
                                    smfname, true);
                            if ("0".equals(atts.getValue("modeDiffOrQuotient"))) {
                            	((AdvancedIntegerFct) fct).setDistanceFct(DistanceConfig.DIFFERENCE);
                            } else if ("1".equals(atts.getValue("modeDiffOrQuotient"))) {
                            	((AdvancedIntegerFct) fct).setDistanceFct(DistanceConfig.QUOTIENT);
                            } else {
                            	((AdvancedIntegerFct) fct).setDistanceFct(DistanceConfig.valueOf(atts.getValue("modeDiffOrQuotient")));	
                            }
                            
                        } else if ("Standard".equals(simMode)) {
                            fct = ((IntegerDesc) desc).addIntegerFct(smfname,
                                    true);
                            if ("0".equals(atts.getValue("modeDiffOrQuotient"))) {
                            	((IntegerFct) fct).setDistanceFct(DistanceConfig.DIFFERENCE);
                            } else if ("1".equals(atts.getValue("modeDiffOrQuotient"))) {
                            	((IntegerFct) fct).setDistanceFct(DistanceConfig.QUOTIENT);
                            } else {
                            	((IntegerFct) fct).setDistanceFct(DistanceConfig.valueOf(atts.getValue("modeDiffOrQuotient")));	
                            }
                            ((ISimFct) fct).setSymmetric(false);
                        }
                    } else if ("Float".equals(type)) {
                        if ("Advanced".equals(simMode)) {
                            fct = ((FloatDesc) desc).addAdvancedFloatFct(
                                    smfname, true);
                            if ("0".equals(atts.getValue("modeDiffOrQuotient"))) {
                            	((AdvancedFloatFct) fct).setDistanceFct(DistanceConfig.DIFFERENCE);
                            } else if ("1".equals(atts.getValue("modeDiffOrQuotient"))) {
                            	((AdvancedFloatFct) fct).setDistanceFct(DistanceConfig.QUOTIENT);
                            } else {
                            	((AdvancedFloatFct) fct).setDistanceFct(DistanceConfig.valueOf(atts.getValue("modeDiffOrQuotient")));	
                            }
                        } else if ("Standard".equals(simMode)) {
                            fct = ((FloatDesc) desc).addFloatFct(smfname, true);
                            if ("0".equals(atts.getValue("modeDiffOrQuotient"))) {
                            	((FloatFct) fct).setDistanceFct(DistanceConfig.DIFFERENCE);
                            } else if ("1".equals(atts.getValue("modeDiffOrQuotient"))) {
                            	((FloatFct) fct).setDistanceFct(DistanceConfig.QUOTIENT);
                            } else {
                            	((FloatFct) fct).setDistanceFct(DistanceConfig.valueOf(atts.getValue("modeDiffOrQuotient")));	
                            }
                            ((ISimFct) fct).setSymmetric(false);
                        }
                    } else if ("String".equals(type)) {
                        StringDesc stringDesc = (StringDesc) desc;

                        if ("Standard".equals(simMode)) {
                            if ("exact_match".equals(atts.getValue("mode"))) {

                                fct = stringDesc.addStringFct(
                                        StringConfig.EQUALITY, smfname, true);

                            } else if ("trigram".equals(atts.getValue("mode"))) {

                                fct = stringDesc.addStringFct(
                                        StringConfig.NGRAM, smfname, true);
                            } else {
                                System.err
                                        .println("Given similarity function cannot be imported: "
                                                + atts.getValue("mode"));
                            }
                        } else if ("Word-Based".equals(simMode)) {
                            System.err
                                    .println("Given similarity function cannot be imported: word-based");
                        } else if ("Character-Based".equals(simMode)) {
                            if ("ngram".equals(atts.getValue("mode"))) {
                                fct = stringDesc.addStringFct(
                                        StringConfig.NGRAM, smfname, true);
                                StringFct sFct = (StringFct) fct;
                                if ("false".equals(atts
                                        .getValue("case_sensitive"))) {
                                    sFct.setCaseSensitive(false);
                                }
                                sFct.setN(Integer.parseInt(atts
                                        .getValue("ngram_value")));
                            } else {
                                System.err
                                        .println("Given similarity function cannot be imported: "
                                                + atts.getValue("mode"));
                            }
                        }
                    } else if ("Boolean".equals(type)) {
                        BooleanDesc boolDesc = (BooleanDesc) desc;
                        fct = boolDesc.addBooleanFct(smfname, true);
                    }
                    desc.getOwner().getActiveAmalgamFct().setActiveFct(desc,
                            fct);

                } else if (prj.getConceptByID(slotName) != null) {
                    c = prj.getConceptByID(slotName);
                    if ("weighted_sum".equals(atts.getValue("amalgamation"))) {
                        fct = c.addAmalgamationFct(
                                AmalgamationConfig.WEIGHTED_SUM, smfname, false);
                    } else if ("euclidean"
                            .equals(atts.getValue("amalgamation"))) {
                        fct = c.addAmalgamationFct(
                                AmalgamationConfig.EUCLIDEAN, smfname, false);
                    } else if ("minimum".equals(atts.getValue("amalgamation"))) {
                        fct = c.addAmalgamationFct(AmalgamationConfig.MINIMUM,
                                smfname, false);
                    } else if ("maximum".equals(atts.getValue("amalgamation"))) {
                        fct = c.addAmalgamationFct(AmalgamationConfig.MAXIMUM,
                                smfname, false);
                    }
                    if ("true".equals(atts.getValue("active"))) {
                    	c.setActiveAmalgamFct((AmalgamationFct) fct);
                    }
                } else {
                    System.err.println("Could not import function " + smfname
                            + " because name not unique or attribute \""
                            + slotName + "\" unknown!");

                }
                if (fct != null && fct instanceof ISimFct) {
                    ((ISimFct) fct).setSymmetric(false);
                }

            } else if ("MAP".equals(qName)) {
                OrderedSymbolFct f = (OrderedSymbolFct) fct;
                try {
                    f.setOrderIndexOf(atts.getValue("symbol"), Integer
                            .parseInt(atts.getValue("integer")));
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if ("InternalSMF".equals(qName)) {
                String smfname = atts.getValue("smfname");
                if (!"SpecialValuesSMF".equals(smfname)) {
                    fct = ((OrderedSymbolFct) fct).getInternalFunction();
                    IntegerFct f = (IntegerFct) fct;
                    f.setSymmetric(false);
                    if ("0".equals(atts.getValue("modeDiffOrQuotient"))) {
                    	f.setDistanceFct(DistanceConfig.DIFFERENCE);
                    } else if ("1".equals(atts.getValue("modeDiffOrQuotient"))) {
                    	f.setDistanceFct(DistanceConfig.QUOTIENT);
                    } else {
                    	f.setDistanceFct(DistanceConfig.valueOf(atts.getValue("modeDiffOrQuotient")));	
                    }
                } else {
                    importingTable = true;
                    importingSV = true;
                    fct = prj.getSpecialFct();
                }
            } else if ("QuerySymbol".equals(qName)) {
                if (importingTable) {
                    currentSymbol = atts.getValue("symbol"); // for importing
                    if ("Non-Special Value".equals(currentSymbol)) {
                        currentSymbol = Project.NO_SPECIAL_VALUE;
                    } else if (importingSV) {
                        currentSymbol = "_".concat(currentSymbol).concat("_");
                    }
                    // symbol
                } // table
            } else if ("CBSymbol".equals(qName)) {
                if (importingTable) {
                    // for importing symbol table
                    String cbSymbol = atts.getValue("symbol");

                    if ("Non-Special Value".equals(cbSymbol)) {
                        cbSymbol = Project.NO_SPECIAL_VALUE;
                    } else if (importingSV) {
                        cbSymbol = "_".concat(cbSymbol).concat("_");
                    }

                    Similarity sim = Similarity.get(Double.parseDouble(atts
                            .getValue("sim")));
                    ((SymbolFct) fct).setSimilarity(currentSymbol, cbSymbol,
                            sim);
                }
            } else if ("SamplingPoint".equals(qName)) {
                // for importing sampling points
                Double xValue = Double.parseDouble(atts.getValue("xValue"));
                Similarity yValue = Similarity.get(Double.parseDouble(atts
                        .getValue("yValue")));
                if (fct instanceof AdvancedFloatFct) {
                    ((AdvancedFloatFct) fct).addAdditionalPoint(xValue, yValue);
                } else {
                    ((AdvancedIntegerFct) fct).addAdditionalPoint(xValue,
                            yValue);
                }

            } else if ("Slot".equals(qName) && (fct instanceof AmalgamationFct)) {
                AmalgamationFct f = (AmalgamationFct) fct;
                    f.setWeight(c.getAllAttributeDescs().get(
                            atts.getValue("slotname")), Float.parseFloat(atts
                            .getValue("weight")));
                
                f.setActive(c.getAllAttributeDescs().get(
                        atts.getValue("slotname")), Boolean.parseBoolean(atts
                        .getValue("enabled")));

            } else if ("LeftSide".equals(qName)) {
                if (fct instanceof IntegerFct) {
                    IntegerFct f = (IntegerFct) fct;
                    String mode = atts.getValue("fctMode");
                    if ("polinomial".equals(mode)) {
                        f.setFunctionTypeL(NumberConfig.POLYNOMIAL_WITH);
                    } else if ("step".equals(mode)) {
                        f.setFunctionTypeL(NumberConfig.STEP_AT);
                    } else if ("smooth_step".equals(mode)) {
                        f.setFunctionTypeL(NumberConfig.SMOOTH_STEP_AT);
                    } // else by default constant
                    f.setFunctionParameterL(Float.parseFloat(atts
                            .getValue(mode)));
                } else {
                    FloatFct f = (FloatFct) fct;
                    String mode = atts.getValue("fctMode");
                    if ("polinomial".equals(mode)) {
                        f.setFunctionTypeL(NumberConfig.POLYNOMIAL_WITH);
                    } else if ("step".equals(mode)) {
                        f.setFunctionTypeL(NumberConfig.STEP_AT);
                    } else if ("smooth_step".equals(mode)) {
                        f.setFunctionTypeL(NumberConfig.SMOOTH_STEP_AT);
                    } // else by default constant
                    f.setFunctionParameterL(Float.parseFloat(atts
                            .getValue(mode)));
                }
            } else if ("RightSide".equals(qName)) {
                if (fct instanceof IntegerFct) {
                    IntegerFct f = ((IntegerFct) fct);
                    String mode = atts.getValue("fctMode");
                    if ("polinomial".equals(mode)) {
                        f.setFunctionTypeR(NumberConfig.POLYNOMIAL_WITH);
                    } else if ("step".equals(mode)) {
                        f.setFunctionTypeR(NumberConfig.STEP_AT);
                    } else if ("smooth_step".equals(mode)) {
                        f.setFunctionTypeR(NumberConfig.SMOOTH_STEP_AT);
                    } // else by default constant
                    f.setFunctionParameterR(Float.parseFloat(atts
                            .getValue(mode)));
                } else {
                    FloatFct f = ((FloatFct) fct);
                    String mode = atts.getValue("fctMode");
                    if ("polinomial".equals(mode)) {
                        f.setFunctionTypeR(NumberConfig.POLYNOMIAL_WITH);
                    } else if ("step".equals(mode)) {
                        f.setFunctionTypeR(NumberConfig.STEP_AT);
                    } else if ("smooth_step".equals(mode)) {
                        f.setFunctionTypeR(NumberConfig.SMOOTH_STEP_AT);
                    } // else by default constant
                    f.setFunctionParameterR(Float.parseFloat(atts
                            .getValue(mode)));
                }
            } else if ("RootSymbol".equals(qName)) {
                if (importingTaxonomy) {
                    TaxonomyFct f = (TaxonomyFct) fct;
                    currentNode = f.getDesc().getAttribute(
                            atts.getValue("symbol"));
                    try {
                        f.setParent((SymbolAttribute) currentNode, f.getTopSymbol());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    f.setNodeSimilarity((TaxonomyNode)currentNode, Similarity.get(Double
                            .parseDouble(atts.getValue("simVal"))));
                }
            } else if ("ChildSymbol".equals(qName)) {
                if (importingTaxonomy) {
                    TaxonomyFct f = (TaxonomyFct) fct;
                    SymbolAttribute tmpNode = (SymbolAttribute) f.getDesc()
                            .getAttribute(atts.getValue("symbol"));

                    try {
                        f.setParent((TaxonomyNode)tmpNode, (TaxonomyNode)currentNode);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    currentNode = tmpNode;

                    f.setNodeSimilarity((TaxonomyNode)currentNode, Similarity.get(Double
                            .parseDouble(atts.getValue("simVal"))));
                }
            } else if ("InnerNodesConfig".equals(qName)) {
                if (importingTaxonomy) {
                    TaxonomyFct f = (TaxonomyFct) fct;
                    TaxonomyConfig config = TaxonomyConfig.NO_INNERNODES;

                    if (!"no".equals(atts.getValue("has_inner_values"))) {
                        config = TaxonomyConfig.INNER_NODES_ANY;
                        if (!"any_value"
                                .equals(atts.getValue("inner_semantic"))) {
                            config = TaxonomyConfig.INNER_NODES_OPTIMISTIC;
                            if ("average".equals(atts.getValue("uncertain"))) {
                                config = TaxonomyConfig.INNER_NODES_AVERAGE;
                            } else if ("pessimistic".equals(atts
                                    .getValue("uncertain"))) {
                                config = TaxonomyConfig.INNER_NODES_PESSIMISTIC;
                            }
                        }
                    }
                    try {
                        if ("query".equals(atts.getValue("scope"))) {
                            f.setQueryConfig(config);
                        } else {
                            f.setCaseConfig(config);
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            } else if ("NoType".equals(atts.getValue("type"))) {
                String slotname = atts.getValue("smfname");
                if (prj.getAttDescsByName(slotname).size() == 0) {
                    return;
                }
                // multiple measure fct only allowed for symbol desc
                fct = ((SymbolDesc) desc).addSymbolFct(slotname, true);
                SymbolFct f = (SymbolFct) fct;
                f.setSymmetric(false);
                MainType mt = null;
                Reuse r = null;
                Type t = null;

                String ONEORMULTI_MODE = atts.getValue("oneOrMulti");
                String SINGLESIM_MODE = atts.getValue("singleSim");
                String MULTISELECTION_MODE = atts.getValue("multiSelection");
                String REUSE_MODE = atts.getValue("reuse");
                String NOMATCH_MODE = atts.getValue("noMatch");
                String TOTAL_MODE = atts.getValue("totalFct");

                if (ONEORMULTI_MODE.equals("10")) { // ONE
                    if (SINGLESIM_MODE.equals("30")) { // BEST
                        mt = MainType.BEST_MATCH;
                    } else { // WORST
                        mt = MainType.WORST_MATCH;
                    }
                    r = Reuse.NONE;
                    t = Type.NONE;
                } else { // Multi
                    if (MULTISELECTION_MODE.equals("20")) { // by query
                        mt = MainType.PARTNER_QUERY;
                    } else if (MULTISELECTION_MODE.equals("21")) { // by case
                        mt = MainType.PARTNER_CASE;
                    } else {
                        mt = MainType.PARTNER_MAX;
                    }

                    if (REUSE_MODE.equals("40")) {
                        r = Reuse.REUSE;
                    } else {
                        if (NOMATCH_MODE.equals("45")) {
                            r = Reuse.ZERO_SIM;
                        } else {
                            r = Reuse.IGNORE;
                        }
                    }

                    if (TOTAL_MODE.equals("50")) {
                        t = Type.AVG;
                    } else if (TOTAL_MODE.equals("51")) {
                        t = Type.MAX;
                    } else {
                        t = Type.MIN;
                    }
                }
                f.setMultipleConfig(new MultipleConfig(mt, r, t));
            } else if ("SpecialValue".equals(qName)) {
                String sv = "_".concat(atts.getValue("Label").concat("_"));
                prj.addSpecialValue(sv);
            }
        }

        public void endElement(String namespaceURI, String localName,
                String qName) {
            if ("SMFunction".equals(qName)) {
                importingTaxonomy = false;
                importingTable = false;

                if (fct instanceof TaxonomyFct) {
                    TaxonomyFct f = (TaxonomyFct) fct;
                    if (f.getCaseConfig() == f.getQueryConfig()) {
                        f.setSymmetric(true);
                    }
                    currentNode = null;
                }
            } else if ("ChildSymbol".equals(qName)) {
                if (importingTaxonomy) {
                    TaxonomyFct f = (TaxonomyFct) fct;
                    currentNode = f.getParent((TaxonomyNode)currentNode);
                }
            } else if ("SpecialValueHandler".equals(qName)) {
                importingSV = false;
                importingTable = false;
            }
        }
    }

}