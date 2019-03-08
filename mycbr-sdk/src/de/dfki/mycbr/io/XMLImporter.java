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

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import de.dfki.mycbr.core.DefaultCaseBase;
import de.dfki.mycbr.core.ICaseBase;
import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Attribute;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.casebase.MultipleAttribute;
import de.dfki.mycbr.core.casebase.SymbolAttribute;
import de.dfki.mycbr.core.explanation.ConceptExplanation;
import de.dfki.mycbr.core.explanation.Explainable;
import de.dfki.mycbr.core.explanation.IExplainable;
import de.dfki.mycbr.core.model.AttributeDesc;
import de.dfki.mycbr.core.model.BooleanDesc;
import de.dfki.mycbr.core.model.Concept;
import de.dfki.mycbr.core.model.ConceptDesc;
import de.dfki.mycbr.core.model.DateDesc;
import de.dfki.mycbr.core.model.DescriptionEnum;
import de.dfki.mycbr.core.model.DoubleDesc;
import de.dfki.mycbr.core.model.FloatDesc;
import de.dfki.mycbr.core.model.IntegerDesc;
import de.dfki.mycbr.core.model.IntervalDesc;
import de.dfki.mycbr.core.model.SimpleAttDesc;
import de.dfki.mycbr.core.model.SpecialDesc;
import de.dfki.mycbr.core.model.StringDesc;
import de.dfki.mycbr.core.model.SymbolDesc;
import de.dfki.mycbr.core.similarity.AdvancedDoubleFct;
import de.dfki.mycbr.core.similarity.AdvancedFloatFct;
import de.dfki.mycbr.core.similarity.AdvancedIntegerFct;
import de.dfki.mycbr.core.similarity.AmalgamationFct;
import de.dfki.mycbr.core.similarity.DoubleFct;
import de.dfki.mycbr.core.similarity.FloatFct;
import de.dfki.mycbr.core.similarity.FunctionEnum;
import de.dfki.mycbr.core.similarity.ISimFct;
import de.dfki.mycbr.core.similarity.IntegerFct;
import de.dfki.mycbr.core.similarity.OrderedSymbolFct;
import de.dfki.mycbr.core.similarity.Similarity;
import de.dfki.mycbr.core.similarity.SpecialFct;
import de.dfki.mycbr.core.similarity.StringFct;
import de.dfki.mycbr.core.similarity.SymbolFct;
import de.dfki.mycbr.core.similarity.TaxonomyFct;
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
 * Imports myCBR's internal XML files. A myCBR project
 * is a zip-file that contains several XML files (x.myCBR, x.cb, x.expl, x.config)
 * 
 * @author myCBR Team
 *
 */
public final class XMLImporter {

    /**
     *
     */
    private Project prj;

    /**
     *
     */
    private String path;

    /**
     *
     */
    private Concept currentConcept;

    /**
     *
     */
    private ZipFile zipfile;

    /**
     *
     */
    private int currentCaseCount = 0;

    /**
     *
     */
    private int totalCaseCount;

    /**
     *
     */
    private Thread t;

    /**
     *
     */
    private static final int BUFFER_SIZE = 1024;
    private Concept currentSuperConcept;

    private String myCB = "";
    private String myCBR = "";
    private String myExp = "";
        
    /**
     * 
     */
    public static final String TMP_PART_OF_CONCEPT = "TMP_PART_OF_CONCEPT";
    
    private HashMap<ConceptDesc, String> partOfMap = new HashMap<ConceptDesc, String>();
    private LinkedList<Object> tmpFunctions = new LinkedList<Object>();
    
    /**
     * Initializes this with the given project, path and name. Adds a new case
     * base to this project.
     *
     * @param p
     *            the project whose model should be imported
     * @throws Exception if something goes wrong during parsing of document
     */
    public XMLImporter(final Project p) throws Exception {
        System.out.println("Loading file " + p.getPath() + p.getName()
                + p.getExtension());
        this.prj = p;
        zipfile = new ZipFile(prj.getPath() + prj.getName() + p.getExtension());
        this.path = prj.getPath() + prj.getName();
    }

    /**
     * Imports the model from the specified XML files to the given
     * project. The model contains the concepts hierarchy structure,
     * the concepts attributes and the corresponding similarity functions.
     */
    public void doImport() {
    	
    	// we have to add a temporary concept 
    	// because it might happen that an attribute description 
    	// points to a concept (as a part of relation)
    	// that has not been imported yet.
    	// we point to the temporary concept and adapt it when
    	// all concepts have been imported
    	try {
			prj.createTopConcept(TMP_PART_OF_CONCEPT);
		} catch (Exception e) {
			e.printStackTrace();
		}
		currentSuperConcept = prj; // this is the concept the new concepts will be added to
		
		// import model 
		importModel();
        
		// import cases
        importCases();
        
    }

    /**
	 * Part-of relations might reference a concept that has not been
	 * imported yet. There is a temporary concept <code>TMP_PART_OF_CONCEPT</code>
	 * that is used as a reference. After importing all concepts, these references have to
	 * be fixed. 
	 */
	private void fixPartOfs() {
		// get the temporary concept
		Concept tmpConcept = prj.getConceptByID(TMP_PART_OF_CONCEPT);
		
		@SuppressWarnings("unchecked")
		// get the list of temporary part-of relations
		Vector<ConceptDesc> list = (Vector<ConceptDesc>) tmpConcept.getPartOfRelations().clone();
		for (ConceptDesc d: list) {
			Concept c = prj.getConceptByID(partOfMap.get(d)); // get original concept
			d.setConcept(c); // fix part-of relation
		}
			
        // delete temporary concept
		tmpConcept.delete();
	}

	/**
     * Extracts the XML-files from the original
     * project's zip-file. 
     */
    public void importModel() {
        Enumeration<? extends ZipEntry> e = zipfile.entries();
        ZipEntry entry;

        while (e.hasMoreElements()) {
            entry = (ZipEntry) e.nextElement();
            String filename = path
            .substring(0, path.lastIndexOf(File.separator))
            + File.separator + entry.getName();
            if (entry.getName().endsWith(".myCB")) {
            	myCB = filename;
            } else if (entry.getName().endsWith(".myCBR")) {
            	myCBR = filename;
            } else if (entry.getName().endsWith(".myExp")) {
            	myExp = filename;
            }
            
            try {
                copyInputStream(zipfile.getInputStream(entry),
                        new BufferedOutputStream(new FileOutputStream(filename)));
            } catch (FileNotFoundException e1) {
                e1.printStackTrace();
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }
        
        // read contents and save them
        // then start to import
        ModelImporter importer = new ModelImporter();
        importer.doImport();

        ExplanationImporter importerExp = new ExplanationImporter();
        importerExp.doImport();
        
        // fix part of relations such that temporary concept no longer necessary
		fixPartOfs();
		
		importer.setUnknownFunctions();
    }

    /**
     * Returns the number of cases that have been imported.
     * @return the total number of cases
     */
    public int getTotalNumberOfCases() {
        return totalCaseCount;
    }

    /**
     * Starts a new thread importing the cases.
     */
    public void importCases() {
        t = new Thread(new CaseImporter());
        t.start();
    }

    /**
     * Returns true, when the import thread is still alive,
     * false otherwise.
     * @return true, when still importing, false otherwise
     */
    public boolean isImporting() {
        return t.isAlive();
    }

    /**
     * Returns the number of cases that have been imported
     * so far.
     * @return the number of cases currently imported
     */
    public int getCurrentNumberOfCases() {
        return currentCaseCount;
    }

    /**
     * Copies the given input stream to the output stream.
     * @param in input stream of file to be copied
     * @param out the output stream of the file to be written
     * @throws IOException if something goes wrong when reading/writing
     */
    public static void copyInputStream(final InputStream in,
            final OutputStream out)
            throws IOException {
        byte[] buffer = new byte[BUFFER_SIZE];
        int len;

        while ((len = in.read(buffer)) >= 0) {
            out.write(buffer, 0, len);
        }
        in.close();
        out.close();
    }

    /**
     * Returns the current project.
     * @return the project this importer belongs to
     */
    public Project getProject() {
        return prj;
    }

    /**
     * For each concept description there has to be at least one 
     * similarity function. When creating a concept description,
     * the constructor therefore creates a default function.
     * These functions have to be removed from each concept description
     * after importing all similarity functions.
     * 
     * @param c the concept whose functions should be cleaned
     */
    private void cleanDefaultFcts(final Concept c) {

    	// clean amalgamation functions
        if (c.getAvailableAmalgamFcts().size() > 1) {
            for (AmalgamationFct amalgam : c
                    .getAvailableAmalgamFcts()) {
                if (Project.DEFAULT_FCT_NAME.equals(amalgam.getName())) {
                    c.deleteAmalgamFct(amalgam);
                    break;
                }
            }
        }
        
        // clean ISimFct objects
        for (AttributeDesc desc : c.getAttributeDescs().values()) {
            if (desc instanceof SimpleAttDesc) {
                SimpleAttDesc sDesc = (SimpleAttDesc) desc;
                for (ISimFct f : sDesc.getSimFcts()) {
                    if (Project.DEFAULT_FCT_NAME.equals(f.getName())) {
                        sDesc.deleteSimFct(f);
                        break;
                    } 
                }
            }
        }

        // continue with subconcepts
        for (Concept conc : c.getSubConcepts().values()) {
            cleanDefaultFcts(conc);
        }
    }

    // ///////////////////////////////////////////////////////////////
    // //////////////////// MODEL Importer ///////////////////////////
    // ///////////////////////////////////////////////////////////////

    /**
     * Imports concepts and inheritance structure.
     */
    private class ModelImporter extends DefaultHandler {

        /**
         *
         */
        private AttributeDesc currentDesc;

        /**
         *
         */
        private ISimFct currentFct;

        /**
         *
         */
        private SymbolAttribute qsym;

        /**
         *
         */
        private AmalgamationFct amalgam;
        
        /**
         * Functions for part of descriptions might not have been imported when loading the amalgamation function
         */
        private HashMap<AmalgamationFct, List<Pair<AttributeDesc, String>>> toBeSet = new HashMap<AmalgamationFct, List<Pair<AttributeDesc,String>>>();
        private HashMap<ConceptDesc, Pair<String, MultipleConfig>> toBeSet_multiple = new HashMap<ConceptDesc, Pair<String, MultipleConfig>>();
        
		private boolean importingSVS;

        /**
         * Imports concepts from project's XML file *.myCBR.
         */
        public void doImport() {
            try {
                SAXParserFactory factory = SAXParserFactory.newInstance();
                SAXParser saxParser = factory.newSAXParser();

                // first parse SMF file to get concepts out of inheritance
                // function
                saxParser.parse("file:" + File.separator + File.separator + File.separator + myCBR, this);

                
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
		 * AmalgamationFct objects might reference other similarity functions
		 * that have not been imported yet. These references have to be fixed.
		 */
		private void setUnknownFunctions() {
			
			for (Map.Entry<ConceptDesc, Pair<String, MultipleConfig>> unknownFcts: toBeSet_multiple.entrySet()) {
				ConceptDesc d = unknownFcts.getKey();
				
				String name = unknownFcts.getValue().getFirst();
				MultipleConfig mc = unknownFcts.getValue().getSecond();
				d.setMultipleConfig(d.getConcept().getFct(name), mc);
			}
			
			for (Map.Entry<AmalgamationFct, List<Pair<AttributeDesc, String>>> unknownFcts: toBeSet.entrySet()) {
				AmalgamationFct amalgam = unknownFcts.getKey();
				for (Pair<AttributeDesc, String> unknownFct: unknownFcts.getValue()) {
					AttributeDesc d = unknownFct.getFirst();
					String name = unknownFct.getSecond();
					Object f = null;
					if (d instanceof ConceptDesc) {
						f = ((ConceptDesc)d).getConcept().getFct(name);
					} else if (d instanceof SimpleAttDesc) {
						f = ((SimpleAttDesc)d).getFct(name);
					}
					if (f==null) {
						System.err.println("Problem importing amalgamation function: \"" + amalgam.getName() + "\". Could not find function \""+ 
								name +"\" for attribute description \""+ d.getName() +"\".");
					} else {
						amalgam.setActiveFct(d, f);
					}
				}
			}
			
			
		}

		/**
         * @param namespaceURI namespace uri
         * @param localName the name of the current element
         * @param qName the qualified name of the current element
         * @param atts the attributes of the current tag
         * @throws SAXException if something goes wrong when parsing the file
         */
        public void startElement(final String namespaceURI,
                final String localName, final String qName,
                final Attributes atts) throws SAXException {
            try {
                if ("Project".equals(qName)) { // import concepts
                    String name = atts.getValue("name");
                    String author = atts.getValue("author");
                    prj.setAuthor(author);
                    prj.setName(name);
                } else if ("svs".equals(qName)) {
                    currentDesc = prj.getSpecialValueDesc();
                    importingSVS = true;
                } else if ("sv".equals(qName)) {
                    String sv = atts.getValue("name");
                    prj.getSpecialValueDesc().addSymbol(sv);
                } else if ("concept".equals(qName)) {
                    String name = atts.getValue("name");
                    currentConcept = new Concept(name,prj,currentSuperConcept);
                    currentSuperConcept = currentConcept;
                } else if ("desc".equals(qName)) {
                    DescriptionEnum type = DescriptionEnum.valueOf(atts
                            .getValue("type"));
                    importDesc(type, atts);
                } else if ("symbol".equals(qName)) { // add allowed values!
                    SymbolDesc d = (SymbolDesc) currentDesc;
                    d.addSymbol(atts.getValue("value"));
                } else if ("fct".equals(qName)) {
                    FunctionEnum type =
                        FunctionEnum.valueOf(atts.getValue("type"));
                    importFct(type, atts);
                    if (atts.getValue("mt") != null
                            && atts.getValue("t") != null
                            && atts.getValue("r") != null) {
                        currentFct.setMultipleConfig(new MultipleConfig(MainType
                            .valueOf(atts.getValue("mt")), Reuse.valueOf(atts
                            .getValue("r")), Type.valueOf(atts.getValue("t"))));
                    }
                    if (atts.getValue("symm") != null) {
                        currentFct.setSymmetric(Boolean.parseBoolean(atts
                                .getValue("symm")));
                    }
                } else if ("qsym".equals(qName)) {
                    qsym = (SymbolAttribute) currentDesc.getAttribute(atts
                                .getValue("name"));
                } else if ("csym".equals(qName)) {
                    SymbolAttribute csym = null;
                    csym = (SymbolAttribute) currentDesc.getAttribute(atts
                                .getValue("name"));
                    Similarity sim = Similarity.get(Double.parseDouble(atts
                            .getValue("sim")));
                    SymbolFct f = (SymbolFct) currentFct;
                    f.setSimilarity(qsym, csym, sim);
                } else if ("node".equals(qName)) {
                    SymbolAttribute currentNode = (SymbolAttribute)currentDesc.getAttribute(atts
                                .getValue("name"));
                    TaxonomyFct fct = ((TaxonomyFct) currentFct);
                    String parentName = atts.getValue("parent");
                    if (((SymbolDesc)fct.getTopSymbol()).getName().equals(parentName)) {
                    	fct.setParent(currentNode, fct.getTopSymbol());
                    } else {
                    	fct.setParent(currentNode, (SymbolAttribute)currentDesc.getAttribute(parentName));
                    }
                    
                    Similarity sim = Similarity.get(Double.parseDouble(atts
                            .getValue("sim")));
                    
                    fct.setNodeSimilarity(currentNode, sim);
                    
                } else if ("point".equals(qName)) {
                    Double x = Double.parseDouble(atts.getValue("x"));
                    Similarity y = Similarity.get(Double.parseDouble(atts
                            .getValue("y")));
                    if (currentFct instanceof AdvancedFloatFct) {
                        ((AdvancedFloatFct) currentFct)
                                                  .addAdditionalPoint(x, y);
                    } else if (currentFct instanceof AdvancedDoubleFct) {
                        ((AdvancedDoubleFct) currentFct)
                        .addAdditionalPoint(x, y);
                    } else { // AdvancedIntegerFct
                        ((AdvancedIntegerFct) currentFct)
                                                  .addAdditionalPoint(x, y);
                    }
                } else if ("intfct".equals(qName)) { // import internal fct of
                    // ordered symbol fct
                    IntegerFct currentIntFct = ((OrderedSymbolFct) currentFct)
                            .getInternalFunction();
                    currentIntFct.setName(atts.getValue("name"));
                    NumberConfig ltype = NumberConfig.valueOf(atts
                            .getValue("ltype"));
                    NumberConfig rtype = NumberConfig.valueOf(atts
                            .getValue("rtype"));
                    double l = Double.parseDouble(atts.getValue("lparam"));
                    double r = Double.parseDouble(atts.getValue("rparam"));
                    currentIntFct.setSymmetric(false);
                    currentIntFct.setFunctionTypeL(ltype);
                    currentIntFct.setFunctionTypeR(rtype);
                    currentIntFct.setFunctionParameterL(l);
                    currentIntFct.setFunctionParameterR(r);
                    if ("0".equals(atts.getValue("mode"))) {
                        currentIntFct.setDistanceFct(DistanceConfig.DIFFERENCE);
                    } else if ("1".equals(atts.getValue("mode"))) {
                        currentIntFct.setDistanceFct(DistanceConfig.QUOTIENT);
                    } else {
                        currentIntFct.setDistanceFct(DistanceConfig.valueOf(atts.getValue("mode")));	
                    }
                } else if ("order".equals(qName)) { // import order of symbols
                    ((OrderedSymbolFct) currentFct).setOrderIndexOf(atts
                                .getValue("name"), Integer.parseInt(atts
                                .getValue("index")));
                } else if ("hierarchy".equals(qName)) {
                    currentDesc = prj.getInhFct().getDesc();
                } else if ("amalgam".equals(qName)) {
                	String name = atts.getValue("name");
                	if (name.equals(Project.DEFAULT_FCT_NAME)) {
                		name = Project.DEFAULT_FCT_NAME_TMP;
                	}
                    amalgam = currentConcept.addAmalgamationFct(
                            AmalgamationConfig.valueOf(atts.getValue("type")),
                            atts.getValue("name"),
                            Boolean.parseBoolean(atts.getValue("active")));
                    if (name.equals(Project.DEFAULT_FCT_NAME_TMP)) {
                    	tmpFunctions.add(amalgam);
                    }
                } else if ("entry".equals(qName)) {
                    AttributeDesc d = currentConcept.getAllAttributeDescs().get(atts
                            .getValue("name"));
                    if (d == null) {
                    	System.err.println("Could not find description with name \"" + atts
                            .getValue("name") + "\" in concept \"" + currentConcept
                            .getName() + "\"");
                    }
                    amalgam.setWeight(d, Double
                            .parseDouble(atts.getValue("weight")));
                    amalgam.setActive(d, Boolean.parseBoolean(atts
                            .getValue("active")));
                    Object f;
                    String name = atts.getValue("fct");
                    if (d instanceof ConceptDesc) {
                        f = ((ConceptDesc) d).getConcept().getFct(name);
                    } else {
                        f = ((SimpleAttDesc) d).getFct(name);
                    }

                    // function might not have been imported yet!
                    if (f == null) {
                    	List<Pair<AttributeDesc, String>> unknownFctList = toBeSet.get(amalgam);
                    	if (unknownFctList==null) {
                    		unknownFctList = new LinkedList<Pair<AttributeDesc,String>>();
                    	}
                    	unknownFctList.add(new Pair<AttributeDesc, String>(d, name));
                    	toBeSet.put(amalgam, unknownFctList);
                    } else {
                    	amalgam.setActiveFct(d, f);
                    }
                } else if ("mc".equals(qName)) {
                    ConceptDesc d = ((ConceptDesc) currentDesc);
                    MultipleConfig mc = new MultipleConfig(MainType.valueOf(atts
                            .getValue("mt")), Reuse.valueOf(atts.getValue("r")),
                            Type.valueOf(atts.getValue("t")));
                    String name = atts.getValue("fct");
                    if (d.getConcept().getFct(name)==null) {
                    	toBeSet_multiple.put(d, new Pair<String, MultipleConfig>(name, mc));
                    } else {
                    	d.setMultipleConfig(
                            d.getConcept().getFct(name), mc);
                    }
                } else if ("cases".equals(qName)) {
                	int currentCBCount = Integer.parseInt(atts.getValue("no"));
                	String cbName = atts.getValue("cb");
                	if (cbName!=null) {
                		prj.createDefaultCB(cbName, currentCBCount);
                	}
                    totalCaseCount += currentCBCount;
                }
            } catch (SAXException e) {
                throw e;
            } catch (Exception e) {
                e.printStackTrace();
                return;
            }
        } // end of method

        /**
         * Imports a function of the given type based on the given attributes.
         * 
         * @param type the type of the fct to be imported
         * @param atts the current attributes of the tag
         * @throws Exception if something goes wrong during sim calculations
         */
        private void importFct(final FunctionEnum type, final Attributes atts)
                                                            throws Exception {

        	String name = atts.getValue("name");
        	if (name.equals(Project.DEFAULT_FCT_NAME)) {
        		name = Project.DEFAULT_FCT_NAME_TMP; // because default fcts will be deleted
        	}
        	
            switch (type) {
            case Integer:
                IntegerFct currentIntFct = ((IntegerDesc) currentDesc)
                        .addIntegerFct(name, false);
                NumberConfig ltype = NumberConfig.valueOf(atts
                        .getValue("ltype"));
                NumberConfig rtype = NumberConfig.valueOf(atts
                        .getValue("rtype"));
                double l = Double.parseDouble(atts.getValue("lparam"));
                double r = Double.parseDouble(atts.getValue("rparam"));
                currentIntFct.setSymmetric(false);
                currentIntFct.setFunctionTypeL(ltype);
                currentIntFct.setFunctionTypeR(rtype);
                currentIntFct.setFunctionParameterL(l);
                currentIntFct.setFunctionParameterR(r);
                if ("0".equals(atts.getValue("mode"))) {
                    currentIntFct.setDistanceFct(DistanceConfig.DIFFERENCE);
                } else if ("1".equals(atts.getValue("mode"))) {
                    currentIntFct.setDistanceFct(DistanceConfig.QUOTIENT);
                } else {
                    currentIntFct.setDistanceFct(DistanceConfig.valueOf(atts.getValue("mode")));	
                }
                currentFct = currentIntFct;
                break;
            case Float:
                FloatFct currentFloatFct = ((FloatDesc) currentDesc)
                        .addFloatFct(name, false);
                if (atts.getValue("maxForQuotient")!=null) {
                	double maxForQuotient = Double.parseDouble(atts.getValue("maxForQuotient"));
                	currentFloatFct.setMaxForQuotient(maxForQuotient);
                }
                ltype = NumberConfig.valueOf(atts.getValue("ltype"));
                rtype = NumberConfig.valueOf(atts.getValue("rtype"));
                l = Double.parseDouble(atts.getValue("lparam"));
                r = Double.parseDouble(atts.getValue("rparam"));
                currentFloatFct.setSymmetric(false);
                currentFloatFct.setFunctionTypeL(ltype);
                currentFloatFct.setFunctionTypeR(rtype);
                currentFloatFct.setFunctionParameterL(l);
                currentFloatFct.setFunctionParameterR(r);
                if ("0".equals(atts.getValue("mode"))) {
                    currentFloatFct.setDistanceFct(DistanceConfig.DIFFERENCE);
                } else if ("1".equals(atts.getValue("mode"))) {
                    currentFloatFct.setDistanceFct(DistanceConfig.QUOTIENT);
                } else {
                    currentFloatFct.setDistanceFct(DistanceConfig.valueOf(atts.getValue("mode")));	
                }
                currentFct = currentFloatFct;
                break;
            case Double:
            	DoubleFct currentDoubleFct = ((DoubleDesc) currentDesc)
                        .addDoubleFct(name, false);
                if (atts.getValue("maxForQuotient")!=null) {
                	double maxForQuotient = Double.parseDouble(atts.getValue("maxForQuotient"));
                	currentDoubleFct.setMaxForQuotient(maxForQuotient);
                }
                ltype = NumberConfig.valueOf(atts.getValue("ltype"));
                rtype = NumberConfig.valueOf(atts.getValue("rtype"));
                l = Double.parseDouble(atts.getValue("lparam"));
                r = Double.parseDouble(atts.getValue("rparam"));
                currentDoubleFct.setSymmetric(false);
                currentDoubleFct.setFunctionTypeL(ltype);
                currentDoubleFct.setFunctionTypeR(rtype);
                currentDoubleFct.setFunctionParameterL(l);
                currentDoubleFct.setFunctionParameterR(r);
                if ("0".equals(atts.getValue("mode"))) {
                    currentDoubleFct.setDistanceFct(DistanceConfig.DIFFERENCE);
                } else if ("1".equals(atts.getValue("mode"))) {
                    currentDoubleFct.setDistanceFct(DistanceConfig.QUOTIENT);
                } else {
                    currentDoubleFct.setDistanceFct(DistanceConfig.valueOf(atts.getValue("mode")));	
                }
                currentFct = currentDoubleFct;
                break;
            case Symbol:
            	if (importingSVS) {
            		currentFct = ((SpecialDesc) currentDesc).addSpecialFct(
            				name, false);
            	} else {
            		currentFct = ((SymbolDesc) currentDesc).addSymbolFct(
            				name, false);
            	}
                break;
            case Interval:
                currentFct = ((IntervalDesc) currentDesc)
                        .addIntervalFct(name, false);
                break;
            case String:
                currentFct = ((StringDesc) currentDesc).addStringFct(
                        StringConfig.valueOf(atts.getValue("config")),
                        name, false);
                int n = Integer.parseInt(atts.getValue("n"));
                ((StringFct) currentFct).setN(n);
                break;
            case AdvancedFloat:
                currentFct = ((FloatDesc) currentDesc)
                        .addAdvancedFloatFct(name,
                                false);
                if (atts.getValue("maxForQuotient")!=null) {
                	double maxForQuotient = Double.parseDouble(atts.getValue("maxForQuotient"));
                	((AdvancedFloatFct) currentFct).setMaxForQuotient(maxForQuotient);
                }
                if ("0".equals(atts.getValue("mode"))) {
                	((AdvancedFloatFct) currentFct).setDistanceFct(DistanceConfig.DIFFERENCE);
                } else if ("1".equals(atts.getValue("mode"))) {
                	((AdvancedFloatFct) currentFct).setDistanceFct(DistanceConfig.QUOTIENT);
                } else {
                	((AdvancedFloatFct) currentFct).setDistanceFct(DistanceConfig.valueOf(atts.getValue("mode")));	
                }
                break;
            case AdvancedDouble:
                currentFct = ((DoubleDesc) currentDesc)
                        .addAdvancedDoubleFct(name,
                                false);
                if (atts.getValue("maxForQuotient")!=null) {
                	double maxForQuotient = Double.parseDouble(atts.getValue("maxForQuotient"));
                	((AdvancedDoubleFct) currentFct).setMaxForQuotient(maxForQuotient);
                }
                if ("0".equals(atts.getValue("mode"))) {
                	((AdvancedDoubleFct) currentFct).setDistanceFct(DistanceConfig.DIFFERENCE);
                } else if ("1".equals(atts.getValue("mode"))) {
                	((AdvancedDoubleFct) currentFct).setDistanceFct(DistanceConfig.QUOTIENT);
                } else {
                	((AdvancedDoubleFct) currentFct).setDistanceFct(DistanceConfig.valueOf(atts.getValue("mode")));	
                }
                break;
            case AdvancedInteger:
                currentFct = ((IntegerDesc) currentDesc)
                        .addAdvancedIntegerFct(name,
                                false);
                if ("0".equals(atts.getValue("mode"))) {
                	((AdvancedIntegerFct) currentFct).setDistanceFct(DistanceConfig.DIFFERENCE);
                } else if ("1".equals(atts.getValue("mode"))) {
                	((AdvancedIntegerFct) currentFct).setDistanceFct(DistanceConfig.QUOTIENT);
                } else {
                	((AdvancedIntegerFct) currentFct).setDistanceFct(DistanceConfig.valueOf(atts.getValue("mode")));	
                }
                break;
            case Date:
                currentFct = ((DateDesc) currentDesc).addDateFct(name, false);
                break;
            case Taxonomy:
                currentFct = ((SymbolDesc) currentDesc).addTaxonomyFct(
                		name, false);
                currentFct.setSymmetric(false);
                ((TaxonomyFct) currentFct)
                        .setQueryConfig(TaxonomyConfig.valueOf(atts
                                .getValue("qconfig")));
                ((TaxonomyFct) currentFct).setCaseConfig(TaxonomyConfig
                        .valueOf(atts.getValue("cconfig")));
                ((TaxonomyFct) currentFct).setNodeSimilarity(
                        (SymbolDesc)currentDesc, Similarity.get(Double
                                .parseDouble(atts.getValue("sim"))));
                if (atts.getValue("qconfig").equals(atts.getValue("cconfig"))) {
                	currentFct.setSymmetric(true);
                }
                break;
            case OrderedSymbol:
                currentFct = ((SymbolDesc) currentDesc)
                        .addOrderedSymbolFct(name,
                                false);
                ((OrderedSymbolFct) currentFct).setCyclic(Boolean
                        .parseBoolean(atts.getValue("cyclic")));
                break;
            default:
                break;
            }
            
            if (name.equals(Project.DEFAULT_FCT_NAME_TMP)) {
            	tmpFunctions.add(currentFct);
            }
        }

        /**
         * Imports a description of the given type based on the
         * given attributes.
         * @param type the type of the desc to be imported
         * @param atts the attributes of the current tag
         */
        private void importDesc(final DescriptionEnum type,
                                final Attributes atts) {
        	try {
                switch (type) {
                case Integer:
                    currentDesc = new IntegerDesc(currentConcept, atts
                            .getValue("name"), Integer.parseInt(atts
                            .getValue("min")), Integer.parseInt(atts
                            .getValue("max")));
                    break;
                case Float:
                    currentDesc = new FloatDesc(currentConcept, atts
                            .getValue("name"), Float.parseFloat(atts
                            .getValue("min")), Float.parseFloat(atts
                            .getValue("max")));
                    break;
                case Double:
                    currentDesc = new DoubleDesc(currentConcept, atts
                            .getValue("name"), Double.parseDouble(atts
                            .getValue("min")), Double.parseDouble(atts
                            .getValue("max")));
                    break;
                case Symbol:
                    currentDesc = new SymbolDesc(currentConcept, atts
                            .getValue("name"), new HashSet<String>());
                    break;
                case Interval:
                    currentDesc = new IntervalDesc(currentConcept, atts
                            .getValue("name"), Integer.parseInt(atts
                            .getValue("min")), Integer.parseInt(atts
                            .getValue("max")));
                    break;
                case String:
                    currentDesc = new StringDesc(currentConcept, atts
                            .getValue("name"));
                    break;
                case Boolean:
                    currentDesc = new BooleanDesc(currentConcept, atts
                            .getValue("name"));
                    break;
                case Concept:
                	Concept c = prj.getConceptByID(atts.getValue("concept"));
                	if (c == null) {
                		// use temporary concept created at the beginning
                		// because original concept has not been imported
                		// yet. Fix this afterwards by fixPartOfs()
                		c = prj.getConceptByID(TMP_PART_OF_CONCEPT);
                	}
                    currentDesc = new ConceptDesc(currentConcept, atts
                            .getValue("name"), c); 
                    partOfMap.put((ConceptDesc)currentDesc, atts.getValue("concept"));
                    break;
                case Date:
                	String format = atts.getValue("format");
                    SimpleDateFormat df = new SimpleDateFormat(format);
                    currentDesc = new DateDesc(currentConcept, atts
                            .getValue("name"), df.parse(atts
                            .getValue("min")), df.parse(atts
                            .getValue("max")), df);
                    break;
                default:
                    break;
                }
                currentDesc.setMultiple(Boolean.parseBoolean(atts
                        .getValue("mult")));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        
        public void endElement(String uri, String localName, String qName)
         													throws SAXException {
        	if (qName.equals("concept")) {
        		currentSuperConcept = currentSuperConcept.getSuperConcept();
        	} else if (qName.equals("svs")) {
        		importingSVS = false;
        	}
        }

    } // end of internal class

    // ///////////////////////////////////////////////////////////////
    // ///////////////////// CASE Importer ///////////////////////////
    // ///////////////////////////////////////////////////////////////

    /**
     * Imports instances and cases.
     */
    private final class CaseImporter extends DefaultHandler
                                                          implements Runnable {

        /**
         *
         */
        private Concept currentConcept;

        /**
         *
         */
        private Instance currentInstance;

        private ICaseBase currentCB;
        
        /**
         * Imports concepts from project's XML file *.myCB.
         */
        public void doImport() {
            try {
                if (new File(myCB).exists()) {
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    SAXParser saxParser = factory.newSAXParser();

                    // first parse SMF file to get concepts out of inheritance
                    // function
                    saxParser.parse("file:" + File.separator + File.separator + File.separator + myCB, this);
                    System.out.println("Imported " + currentCaseCount
                            + " instances.");
                } else {
                	System.err.println(myCB + " does not exist!");
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }


        /**
         * @param namespaceURI namespace uri
         * @param localName the name of the current element
         * @param qName the qualified name of the current element
         * @param atts the attributes of the current tag
         * @throws SAXException if something goes wrong when parsing the file
         */
        public void startElement(final String namespaceURI,
                final String localName,
                final String qName, final Attributes atts) throws SAXException {
            if ("instances".equals(qName)) {
                Concept tmp = prj.getConceptByID(atts.getValue("name"));
                if (tmp != null) {
                    currentConcept = tmp;
                } else {
                    System.err.println("Could not find concept "
                            + atts.getValue("name"));
                }
            } else if ("instance".equals(qName)) {
                try {
					currentInstance = currentConcept.addInstance(atts
					        .getValue("id"));

	                currentCaseCount++;
				} catch (Exception e) {
					e.printStackTrace();
				}

            } else if ("att".equals(qName)) {
                String name = atts.getValue("name");
                AttributeDesc desc = currentConcept.getAttributeDesc(name);
                String value = atts.getValue("value");
                if (desc.isMultiple() && !prj.isSpecialAttribute(value)) {
                    String[] values = value.split(";");
                    LinkedList<Attribute> l = new LinkedList<Attribute>();

                    for (String s : values) {
                        // add corresponding value...
                        try {
                            l.add(desc.getAttribute(s));
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                    MultipleAttribute<?> att =
                        new MultipleAttribute<AttributeDesc>(desc, l);
                    currentInstance.addAttribute(name, att);
                } else {
                    // add corresponding value;
                    try {
                        currentInstance.addAttribute(name, desc
                                .getAttribute(value));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }
            } else if ("case".equals(qName)) {
               if (atts.getValue("cb")!=null) {
            	   ICaseBase cb = prj.getCB(atts.getValue("cb"));
            	   cb.addCase(currentInstance);
               } else {
            	   currentCB.addCase(prj.getInstance(atts.getValue("name")));
               }
            } else if ("cb".equals(qName)) {
                try {
                	DefaultCaseBase cb = null;
                	String cbName = atts.getValue("name");
                	// Case base might have been initialized
                	// in model importer
                	if (prj.getCB(cbName)==null) {
                		cb = prj.createDefaultCB(atts.getValue("name"));
                	
                         String author = atts.getValue("author");
                         if (author != null) {
                         	cb.setAuthor(atts.getValue("author"));
                         }
                         
                         String date = atts.getValue("date");
                         if (date != null) {
                         	cb.setDate(cb.getDateFormat().parse(date));
                         }
                	}
                	currentCB = prj.getCB(cbName);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }

        /*
         * (non-Javadoc)
         * @see java.lang.Runnable#run()
         */
        @Override
        /**
         * Calls {@link CaseImporter#doImport}, and
         * {@link #cleanDefaultFcts(Project)}
         */
        public void run() {
            CaseImporter importerCases = new CaseImporter();
            importerCases.doImport();
            
            // remove default function if another function already contained
            cleanDefaultFcts(prj);
            
            for (ISimFct f: prj.getSpecialValueDesc().getSimFcts()) {
            	if (f.getName().equals(Project.DEFAULT_FCT_NAME)) {
            		prj.getSpecialValueDesc().deleteSimFct(f);
            	}
            }
            
            for (ISimFct f: prj.getInhFct().getDesc().getSimFcts()) {
            	if (f.getName().equals(Project.DEFAULT_FCT_NAME)) {
            		prj.getSpecialValueDesc().deleteSimFct(f);
            	}
            }
            
            for (Object o: tmpFunctions) {
            	if (o instanceof ISimFct) {
            		((ISimFct)o).setName(Project.DEFAULT_FCT_NAME);
            	} else if (o instanceof AmalgamationFct) {
            		((AmalgamationFct)o).setName(Project.DEFAULT_FCT_NAME);
            	}
            }
            prj.setSpecialValueFct((SpecialFct)(prj.getSpecialValueDesc().getSimFcts().get(0)));
        }

    }

    // ///////////////////////////////////////////////////////////////
    // ////////////////// EXPLANATION Importer ///////////////////////
    // ///////////////////////////////////////////////////////////////

    /**
     * Importers concept explanations.
     */
    private final class ExplanationImporter extends DefaultHandler {

        /**
         *
         */
        private IExplainable currentExplainable;

        /**
         *
         */
        private ConceptExplanation currentExp;

        /**
         *
         */
        private boolean readDesc;

        /**
         *
         */
        private String currentDesc;

        /**
         * Imports concepts from project's XML file *.expl.
         */
        public void doImport() {
            try {
                if (new File(myExp).exists()) {
                    SAXParserFactory factory = SAXParserFactory.newInstance();
                    SAXParser saxParser = factory.newSAXParser();

                    // first parse SMF file to get concepts out of inheritance
                    // function
                    saxParser.parse("file:" + File.separator + File.separator + File.separator + myExp, this);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        /**
         * @param namespaceURI namespace uri
         * @param localName the name of the current element
         * @param qName the qualified name of the current element
         * @param atts the attributes of the current tag
         * @throws SAXException if something goes wrong when parsing the file
         */
        public void startElement(final String namespaceURI,
                final String localName,
                final String qName, final Attributes atts) throws SAXException {
            if ("exp".equals(qName)) {
                Explainable type = Explainable.valueOf(atts.getValue("type"));

                switch (type) {
                case Concept:
                    currentExplainable = prj.getConceptByID(atts
                            .getValue("obj"));
                    break;
                case AttributeDesc:
                    currentExplainable = prj.getConceptByID(atts.getValue("c"))
                            .getAttributeDesc(atts.getValue("obj"));
                    break;
                case SimpleAttribute:
                    try {
                        currentExplainable = (IExplainable) prj.getConceptByID(
                                atts.getValue("c")).getAttributeDesc(
                                atts.getValue("desc")).getAttribute(
                                atts.getValue("obj"));
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                    break;
                case Instance:
                    currentExplainable = prj.getConceptByID(atts.getValue("c"))
                            .getInstance(atts.getValue("obj"));
                    break;
                default:
                    break;
                }

            } else if ("desc".equals(qName)) {
                readDesc = true;
            } else if ("link".equals(qName)) {
                currentExp.addLink(atts.getValue("url"));
            }
        }

        /**
         * @param chars the chars to be parsed
         * @param iStart start index
         * @param iLen length of chars to be parsed
         */
        public void characters(final char[] chars,
                final int iStart, final int iLen) {
            if (readDesc) {
                currentDesc = new String(chars, iStart, iLen);
            }
        }


        /**
         * @param namespaceURI namespace uri
         * @param localName the name of the current element
         * @param qName the qualified name of the current element
         */
        public void endElement(final String namespaceURI,
                final String localName,
                final String qName) {
            if ("desc".equals(qName)) {
                readDesc = false;
                currentExp = prj.getExplanationManager().explain(
                        currentExplainable, currentDesc);
                currentDesc = null;
            }
        }

    }

}
