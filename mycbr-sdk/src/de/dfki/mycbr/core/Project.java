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

package de.dfki.mycbr.core;

import java.io.File;
import java.io.FileNotFoundException;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Observable;
import java.util.Vector;

import de.dfki.mycbr.core.action.RenameAction;
import de.dfki.mycbr.core.casebase.Attribute;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.casebase.MultipleAttribute;
import de.dfki.mycbr.core.casebase.SpecialAttribute;
import de.dfki.mycbr.core.casebase.SymbolAttribute;
import de.dfki.mycbr.core.explanation.ExplanationManager;
import de.dfki.mycbr.core.model.AttributeDesc;
import de.dfki.mycbr.core.model.Concept;
import de.dfki.mycbr.core.model.ConceptDesc;
import de.dfki.mycbr.core.model.SpecialDesc;
import de.dfki.mycbr.core.model.SymbolDesc;
import de.dfki.mycbr.core.similarity.AmalgamationFct;
import de.dfki.mycbr.core.similarity.ISimFct;
import de.dfki.mycbr.core.similarity.Similarity;
import de.dfki.mycbr.core.similarity.SpecialFct;
import de.dfki.mycbr.core.similarity.SymbolFct;
import de.dfki.mycbr.core.similarity.TaxonomyFct;
import de.dfki.mycbr.core.similarity.config.MultipleConfig;
import de.dfki.mycbr.core.similarity.config.TaxonomyConfig;
import de.dfki.mycbr.io.MyCBRImporter;
import de.dfki.mycbr.io.XMLExporter;
import de.dfki.mycbr.io.XMLImporter;

/**
 * A Project consists of a vocabulary and (possibly several) case bases. The
 * vocabulary builds the model and consists of a concept hierarchy, the
 * attribute descriptions of each concept and the corresponding similarity
 * measures. It can be exported/imported via objects of classes that implement
 * IImporter/ IExporter.
 *
 * @author myCBR Team
 *
 */
public final class Project extends Concept {

    /**
     * Maps the case bases for this project to their name.
     */
    private HashMap<String, ICaseBase> caseBases;

    /**
     * predefined constants for special values. Should not be edited.
     */
    public static final String UNDEFINED_SPECIAL_ATTRIBUTE = "_undefined_";
    /**
     * predefined constants for special values. Should not be edited.
     */
    public static final String UNKNOWN_SPECIAL_VALUE = "_unknown_";
    /**
     * predefined constants for special values. Should not be edited.
     */
    public static final String NO_SPECIAL_VALUE = "_others_";

    /**
     * predefined constants for special values. Should not be edited.
     */
    private String projectPath = ""; // path without filename

    /**
     * predefined constants for special values. Should not be edited.
     */
    private String projectAuthor = "";
    private String extension = ".prj";
    
    /**
     * predefined constants for special values. Should not be edited.
     */
    private SpecialDesc specialValueDesc;

    /**
     * predefined constants for special values. Should not be edited.
     */
    private SpecialFct specialValueFct; // active special value fct

    /**
     * predefined constants for special values. Should not be edited.
     */
    private SymbolDesc inheritanceDesc;

    /**
     * predefined constants for special values. Should not be edited.
     */
    private TaxonomyFct inheritanceFct; // active inheritance function

    /**
     * predefined constants for special values. Should not be edited.
     */
    private ExplanationManager explanationManager;

    /**
     * predefined constants for special values. Should not be edited.
     */
    public static final String DEFAULT_FCT_NAME = "default function";
    
    /**
     * predefined constants for special values. Should not be edited.
     */
    public static final String DEFAULT_FCT_NAME_TMP = "_default function TMP!_";
    
    /**
     * predefined constants for special values. Should not be edited.
     */
    public static final String ID_DEFAULT = "Project";

    /**
     *
     */
    private XMLImporter importer;

    /**
     * Default constructor. Initializes the containers for top concepts and case
     * bases
     *
     * @throws Exception never
     */
    public Project() throws Exception {
        super(ID_DEFAULT, null, null);
        initSVDesc();
        subConcepts = new HashMap<String, Concept>();
        caseBases = new HashMap<String, ICaseBase>();

        explanationManager = new ExplanationManager();
        this.prj = this;
        initFcts();
    }

    /**
     * Constructor for project from file.
     * Either loads a _CBR_SMF.XML file or a .zip or .prj file.
     * Initializes the containers for top concepts
     * and case bases
     *
     * @param path the path to load the project from
     * @throws Exception never
     */
    public Project(final String path) throws Exception {
        super(ID_DEFAULT, null, null);
        initSVDesc();
        int begin = 0;
        if (path.lastIndexOf(File.separator) != -1) {
            begin = path.lastIndexOf(File.separator) + 1;
            projectPath = path.substring(0, begin);
        } // else projectPath is ""

        // set project name
        if (path.lastIndexOf("_CBR_SMF.XML") != -1) {
            ID = path.substring(begin, path.lastIndexOf("_CBR_SMF.XML"));
            extension = ".prj";
        } else if (path.lastIndexOf(".zip") != -1) {
            ID = path.substring(begin, path.lastIndexOf(".zip"));
            extension = ".zip";
        } else if (path.lastIndexOf(".prj") != -1) {
            ID = path.substring(begin, path.lastIndexOf(".prj"));
            extension = ".prj";
        } else {
            throw new Exception("File extension unknown. Can only import *.zip, *.prj "
                    + "and *_CBR_SMF.XML files");
        }
        System.out.println(path);
        if (!new File(path).exists()) {
            throw new FileNotFoundException();
        }
        subConcepts = new HashMap<String, Concept>();
        caseBases = new HashMap<String, ICaseBase>();
        explanationManager = new ExplanationManager();
        this.prj = this;
        initFcts();
        if (path.endsWith("_CBR_SMF.XML")) { // try importing old myCBR files...
        	MyCBRImporter myCBRImporter = new MyCBRImporter(this);
            myCBRImporter.doImport();
        } else { // try loading new project
            load();
        }
    }

    /**
     * Initializes this project's special value description.
     * Uses three predefined special values that can be used 
     * as corresponding values for every attribute description 
     * known.
     */
    private final void initSVDesc() {

        HashSet<String> svs = new HashSet<String>();
        svs.add(Project.UNDEFINED_SPECIAL_ATTRIBUTE);
        svs.add(Project.UNKNOWN_SPECIAL_VALUE);
        svs.add(Project.NO_SPECIAL_VALUE);
        try {
            this.specialValueDesc = new SpecialDesc(this, svs);
		} catch (Exception e) {
			e.printStackTrace();
		}	
    }
    
    /**
     * Loads the project from the given file.
     */
    private void load() {
        try {
            importer = new XMLImporter(this);
            importer.doImport();
        } catch (Exception e) {
            e.printStackTrace();
        }

    }
    
    /**
     * Checks whether the import of this project is still running.
     * @return true, if importer is still running
     */
    public boolean isImporting() {
    	if (importer==null) {
    		return false;
    	}
        return importer.isImporting();
    }

    /**
     * The total number of cases that have to be imported
     * is given by this project's .myCBR file.
     * 
     * @return the number of cases to be imported
     */
    public int getTotalNumberOfCases() {
    	if (importer==null) {
    		return 0;
    	}
        return importer.getTotalNumberOfCases();
    }

    /**
     * Returns the number of cases that have been imported so far.
     * @return the number of cases imported so far
     */
    public int getCurrentNumberOfCases() {
    	if (importer==null) {
    		return 0;
    	}
        return importer.getCurrentNumberOfCases();
    }

    /**
     * Loads the project from the given file.
     */
    public void save() {
        if (projectPath != null && ID != null) {
            System.out.println("Save project to file " + projectPath + ID
                    + extension);
            XMLExporter.save(this, projectPath + ID + extension);
        }
    }

    /**
     * Initializes this project's functions.
     * That is, the special value function used to calculate
     * the similarity between any value and a special value or between
     * two special values and the inheritance function that is used
     * to calculate similarity values between two concepts known for this
     * project.
     */
    private void initFcts() {

        specialValueFct = new SpecialFct(prj, prj.getSpecialValueDesc(),
                Project.DEFAULT_FCT_NAME);
        specialValueDesc.addFct(specialValueFct);

        try {
            inheritanceDesc = new SymbolDesc(prj, "inheritanceDesc",
                    new HashSet<String>());
        } catch (Exception e1) {
            System.err.println("exception while initializing inheritance desc");
        }
        LinkedList<SymbolAttribute> l = new LinkedList<SymbolAttribute>();
        initInheritanceDesc(prj, l);

        inheritanceFct = inheritanceDesc
                .addTaxonomyFct(Project.DEFAULT_FCT_NAME, false);

        try {
            inheritanceFct.setCaseConfig(TaxonomyConfig.INNER_NODES_ANY);
            inheritanceFct.setQueryConfig(TaxonomyConfig.INNER_NODES_ANY);
            inheritanceFct.updateTable();
        } catch (Exception e) {
            System.err.println("exception while initializing inheritance fct");
        }
        
    }

    /**
     * Tries to create a default case base for this project. Throws an exception
     * if another case base with that name already exists.
     *
     * @param name the name of the new case base
     * @return new default case base for this project.
     * @throws Exception if there is a case base with that name
     */
    public DefaultCaseBase createDefaultCB(final String name)
                                                             throws Exception {
        DefaultCaseBase newCasebase = new DefaultCaseBase(this, name);
        caseBases.put(name, newCasebase);
        setChanged();
        notifyObservers();
        return newCasebase;
    }
    
    /**
     * Checks whether there is a case base with the given name.
     * @param name the name of the case base
     * @return true if there is a case base with the given name
     */
    public boolean hasCB(final String name) {
        return caseBases.containsKey(name);
    }

    /**
     * Returns the case base associated with the given name.
     * Null if there is no case base with that name.
     *
     * @param name the name of the case base to be returned
     * @return case base with name name or null if there is none
     */
    public ICaseBase getCB(final String name) {
        return caseBases.get(name);
    }

    /**
     * Removes the case base specified by name from this project.
     *
     * @param name
     *            the unique name of the case base to be removed
     * @return the removed case base, null if none removed
     */
    public ICaseBase deleteCaseBase(final String name) {
       ICaseBase deletedCB = caseBases.remove(name);
       if (deletedCB != null) {
           setChanged();
           notifyObservers();
       }
       return deletedCB;
    }

    /**
     * Gets the case bases for this project.
     *
     * @return the case bases for this project
     */
    public HashMap<String, ICaseBase> getCaseBases() {
        return caseBases;
    }

    /**
     * Adds a new top c with the specified name to this project. Returns null if
     * there is a c with this name contained in this project's c hierarchy.
     *
     * @param id
     *            the name for the new c
     * @return new c attribute if name is unique, else null
     * @throws Exception if there is another concept with name id
     */
    public Concept createTopConcept(final String id) throws Exception {
        Concept c = new Concept(id, this, this);
        subConcepts.put(id, c);
        setChanged();
        notifyObservers();
        return c;
    }

    /**
     * Returns the c description with the given name. Searches for a description
     * in the whole model.
     *
     * @param id
     *            the ID of the c description to be returned
     * @return c description with the given name, null if there is none
     */
    public Concept getConceptByID(final String id) {
        Concept res = null;
        // first search in sub concepts ...
        if (subConcepts.containsKey(id)) {
            return subConcepts.get(id);
        }
        // ... then recursively in sub concepts
        for (Concept sub : subConcepts.values()) {
            res = getConceptByID(sub, id);
            if (res != null) {
                break;
            }
        }
        return res;
    }

    /**
     * Returns the c description with the given name. Searches for a description
     * in the sub concepts of the given c.
     *
     * @param c
     *            the description whose sub concepts we scan through
     * @param id
     *            the name of the c description to be returned
     * @return c description with the given name, null if there is none
     */
    private Concept getConceptByID(final Concept c, final String id) {
        Concept res = null;
        // first search in sub concepts ...
        if (c.getSubConcepts().containsKey(id)) {
            return c.getSubConcepts().get(id);
        }
        // ... then recursively in sub concepts
        for (Concept sub : c.getSubConcepts().values()) {
            res = getConceptByID(sub, id);
            if (res != null) {
                break;
            }
        }
        return res;
    }

    /**
     * Checks whether there is a c description in the current inheritance
     * hierarchy which has the specified name.
     *
     * @param id
     *            the name to be checked
     * @return true, if there is a description with this name, false otherwise
     */
    public Boolean hasConceptWithID(final String id) {

        if (getAllSubConcepts().get(id) != null) {
            return true;
        }
        return false;
    }

    /**
     * Returns a list of attribute descriptions with the given name.
     *
     * @param name
     *            the name of the attribute description's to be returned
     * @return list of attribute description with the given name
     */
    public LinkedList<AttributeDesc> getAttDescsByName(final String name) {
        LinkedList<AttributeDesc> res = new LinkedList<AttributeDesc>();
        // look in every sub c...
        for (Concept sub : subConcepts.values()) {
            // ... whether it has a description with this name ...
            getAttDescsByName(sub, res, name);
            // ... and look in all sub concepts recursively
            searchAttInSubConcepts(sub, res, name);
        }
        return res;
    }

    /**
     * Iterate over sub concepts recursively.
     * Calls
     * {@link #getAttDescsByName(Concept, List, String)} for each c description
     * in the inheritance hierarchy to find attribute description with the given
     * name
     *
     * @param desc
     *            the c whose sub concepts are browsed
     * @param list
     *            the list to append attributes with the given name
     * @param name
     *            the name of attribute descriptions to be returned
     */
    private void searchAttInSubConcepts(final Concept desc,
            final List<AttributeDesc> list,
            final String name) {
        // look in every sub c...
        for (Concept sub : desc.getSubConcepts().values()) {
            // ... whether it has a description with this name ...
            getAttDescsByName(sub, list, name);
            // ... and look in all sub concepts recursively
            for (Concept sub2 : sub.getSubConcepts().values()) {
                searchAttInSubConcepts(sub2, list, name);
            }
        }
    }

    /**
     * Adds attribute descriptions of desc with the given name to list, if they
     * are not c descriptions themselves. Concept descriptions as attributes
     * appear as c descriptions in inheritance hierarchy and are browsed by
     * searchAttInSubConcepts.
     *
     * @param c
     *            the concept whose sub concepts are browsed
     * @param list
     *            the list to append attributes with the given name
     * @param name
     *            the name of attribute descriptions to be returned
     */
    private void getAttDescsByName(final Concept c,
            final List<AttributeDesc> list,
            final String name) {
        if (c.getAttributeDescs().containsKey(name)) {
            AttributeDesc attDesc = c.getAttributeDescs().get(name);
            if (!(attDesc instanceof ConceptDesc)) {
                list.add(attDesc);
            }
        }
    }

    /**
     * Sets the project's path to the specified path.
     *
     * @param path
     *            the project path to set
     */
    public void setPath(final String path) {
        this.projectPath = path;
        setChanged();
        notifyObservers();
    }

    /**
     * Gets the path of this project.
     *
     * @return the project path
     */
    public String getPath() {
        return projectPath;
    }

    /**
     * Returns the zip file's extension.
     * Either .zip or .prj.
     * @return the zip file's extension
     */
    public String getExtension() {
    	return extension;
    }
    
    /**
     * Sets the author of this project.
     *
     * @param author
     *            the project author to set
     */
    public void setAuthor(final String author) {
        this.projectAuthor = author;
        setChanged();
        notifyObservers();
    }

    /**
     * Gets the author of this project.
     *
     * @return the project author's name
     */
    public String getAuthor() {
        return projectAuthor;
    }

    /**
     * @return the specialValueDesc
     */
    public SpecialDesc getSpecialValueDesc() {
        return specialValueDesc;
    }

    /**
     *
     * @param obj the string which is checked
     * @return true if obj is a special value
     */
    public boolean isSpecialAttribute(final String obj) {
    	if (obj == null) {
    		return false;
    	}
    	if (obj.equals(Project.NO_SPECIAL_VALUE)
    			|| obj.equals(Project.UNDEFINED_SPECIAL_ATTRIBUTE)
    			|| obj.equals(Project.UNKNOWN_SPECIAL_VALUE)) {
    		return true;
    	}
        return specialValueDesc.isAllowedValue(obj);
    }

    /**
     * @return the explanationManager
     */
    public ExplanationManager getExplanationManager() {
        return explanationManager;
    }

    /**
     * @param value the special value to be added
     */
    public void addSpecialValue(final String value) {
        specialValueDesc.addSymbol(value);
    }

    /**
     * @param name the name of the instance to be returned
     * @return the instance with name name, null if there is none
     */
    @Override
    public Instance getInstance(final String name) {
        Instance i = null;
        for (Concept c : getAllSubConcepts().values()) {
            i = c.getInstance(name);
            if (i != null) {
                break;
            }
        }
        return i;
    }

    /**
     * Initializes the inheritance description for this project. Adds a symbol
     * for each sub c of the given description to the inheritance description
     *
     * @param prj
     *            the description whose sub concepts should be added
     * @param l
     *            the list where the concepts should be added
     */
    private void initInheritanceDesc(
            final Project prj, final LinkedList<SymbolAttribute> l) {
        SymbolAttribute att = null;
        for (String name : prj.getSubConcepts().keySet()) {
            att = inheritanceDesc.addSymbol(name);
            l.add(att);
        }
    }

    /**
     * Returns the inheritance function of this c. The inheritance function
     * describes relations of concepts known for this model. These are then used
     * to compare object oriented cases for different concepts.
     *
     * @return inheritance function of this project.
     */
    public TaxonomyFct getInhFct() {
        return inheritanceFct;
    }

    /**
     * Changes the active special value function.
     * Assumes that f is already a function defined for 
     * the special value description. To add another special
     * function call {@link SpecialDesc#addSpecialFct(String, boolean)} 
     * on {@link #getSpecialValueDesc()}
     *  
     * @param f the new special value function
     */
    public void setInhFct(SpecialFct f) {
        specialValueFct = f;
        setChanged();
        notifyObservers();
    }

    /**
     * Creates a new InheritanceFct for myCBR's model.
     *
     * @param name the name of the new inheritance function
     * @return the new inheritance function with name name
     */
    public TaxonomyFct addInhFct(final String name) {

        List<SymbolAttribute> l = new LinkedList<SymbolAttribute>();
        TaxonomyFct f = new TaxonomyFct(prj, inheritanceDesc, l, name);
        inheritanceDesc.addFct(f);
        setChanged();
        notifyObservers();
        return f;
    }

    /**
     * Returns the special function of this project.
     * The special function computes similarities of special 
     * attributes to any sort of attribute
     *
     * @return the special function of this project
     */
    public SymbolFct getSpecialFct() {
        return specialValueFct;
    }
        
    /**
     * Changes the active special function.
     * Assumes that f is already a function defined for 
     * the special description. To add another special
     * function call {@link SpecialDesc#addSpecialFct(String, boolean)} 
     * on {@link #getSpecialValueDesc()}
     *  
     * @param f the new special value function
     */
    public void setSpecialValueFct(SpecialFct f) {
        specialValueFct = f;
        setChanged();
        notifyObservers();
    }

    /**
     * Calculates the similarity of the given attributes by calling the
     * calculateSimilarity method of the appropriate similarity function. Should
     * be used to calculate similarity of special values.
     * 
     * @param att1 the query's special value
     * @param att2 the case's special value
     * @return Similarity of the given attributes
     * @throws Exception
     */
    public Similarity calculateSpecialSimilarity(final Attribute att1,
            final Attribute att2) {
        if ((att1 instanceof SpecialAttribute)
                && (att2 instanceof SpecialAttribute)) {
            return specialValueFct.calculateSimilarity(
                    (SpecialAttribute) att1, (SpecialAttribute) att2);
        } else if (att1 instanceof SpecialAttribute) {
            return specialValueFct.calculateSimilarity(
                    (SpecialAttribute) att1, (SpecialAttribute)prj.getSpecialValueDesc()
                            .getAttribute(Project.NO_SPECIAL_VALUE));
        } else if (att2 instanceof SpecialAttribute) {
            return specialValueFct.calculateSimilarity((SpecialAttribute)prj
                    .getSpecialValueDesc().getAttribute(
                            Project.NO_SPECIAL_VALUE),
                    (SpecialAttribute) att2);
        }
        return Similarity.INVALID_SIM;

    }

    /**
     * Calculates the similarity of the given <code>MultipleAttribute</code> objects
     * based on the given similarity function.
     * 
     * @param simFct the similarity function to compare simple attributes
     * @param att1 the query's multiple value
     * @param att2 the case's multiple value
     * @return the similarity of att1 to att2
     */
    public Similarity calculateMultipleAttributeSimilarity(final Object simFct,
            final MultipleAttribute<?> att1, final MultipleAttribute<?> att2) {
        if (simFct == null || att1 == null || att2 == null) {
            return Similarity.INVALID_SIM;
        }
        Similarity result = Similarity.INVALID_SIM;
        if (att1.getAttributeDesc().isMultiple()) {
            if (att1.getAttributeDesc() instanceof ConceptDesc
                    && simFct instanceof AmalgamationFct) {
                try {
                    MultipleConfig mc = ((ConceptDesc) att1.getAttributeDesc())
                            .getMultipleConfig((AmalgamationFct) simFct);
                    result = mc.calculateSimilarity((AmalgamationFct) simFct,
                            att1, att2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else if (((ISimFct) simFct).getMultipleConfig() != null) {
                try {
                    result = ((ISimFct) simFct).getMultipleConfig()
                            .calculateSimilarity((ISimFct) simFct, att1, att2);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                System.err.println("no multiple config set for function: "
                        + ((ISimFct) simFct).getName());
            }
        } else {
            System.err.println("Error. Found multiple attribute instance "
                    + "for description which is not multiple!");
        }
        return result;
    }

    /**
     * Returns the <code>SpecialAttribute</code> object corresponding to
     * the given string.
     * 
     * @param obj the value for the special value attribute to be returned
     * @return the attribute with value obj or null, if there is none
     * @see SpecialDesc#getAttribute(String)
     */
    public Attribute getSpecialAttribute(final String obj) {
        return specialValueDesc.getAttribute(obj);
    }

    /**
     * Cleans all instances known for concept c. Since each case is an instance,
     * the cases are clean, too.
     *
     * @param c the concept whose instances should be cleaned
     * @param desc the cases are cleaned wrt the attribute description
     */
    public void cleanInstances(final Concept c, final AttributeDesc desc) {
        Collection<Instance> currentInstances = c.getDirectInstances();
        if (currentInstances != null) {
            for (Instance i : currentInstances) {
                i.clean(desc);
            }
        }
        for (Concept sub : c.getSubConcepts().values()) {
            cleanInstances(sub, desc);
        }
    }

    @Override
    public Concept removeSubConcept(final String name) {
        Concept deletedConcept = subConcepts.remove(name);
        if (deletedConcept!= null) {
            setChanged();
            notifyObservers();
        }
        return deletedConcept;
    }

    @Override
    public Concept getSuperConcept() {
        return null;
    }

    @Override
    public void setSuperConcept(final Concept c, final boolean isNEW) {
    }

    @Override
    public HashMap<String, AttributeDesc> getAttributeDescs() {
        return new HashMap<String, AttributeDesc>();
    }

    @Override
    public HashMap<String, AttributeDesc> getAllAttributeDescs() {
        return new HashMap<String, AttributeDesc>();
    }

    @Override
    public void addAttributeDesc(final AttributeDesc desc) throws Exception {
    }

    @Override
    public boolean removeAttributeDesc(final String name) {
        return false;
    }

    @Override
    public boolean canOverride(final Concept c) {
        return false;
    }

    @Override
    public Project getProject() {
        return this;
    }

    @Override
    public void addPartOfRelation(final ConceptDesc desc) {
    }

    @Override
    public Vector<ConceptDesc> getPartOfRelations() {
        return new Vector<ConceptDesc>();
    }

    @Override
    public void deletePartOfRelation(final ConceptDesc desc) {
    }

    @Override
    public void delete() {
    }

    // ////////////////////////////////////////////////////
    // ////////////////// HELPER METHODS //////////////////
    // ////////////////////////////////////////////////////

    /**
     * For displaying local similarities. Should not be called for further
     * calculations
     *
     * @param a the query attribute
     * @param b the case attribute
     * @param desc the attribute description underlying the given attributes
     * @return the similarity of the given attributes
     * @throws Exception if something goes wrong during similarity computations
     */
    public Similarity getLocalSimilarity(final Attribute a,
            final Attribute b, final AttributeDesc desc) throws Exception {
        Similarity sim = Similarity.INVALID_SIM;
        Object f = null;
        if (desc != null) {
            f = desc.getOwner().getActiveAmalgamFct().getActiveFct(desc);
        }

        if (a instanceof Instance && b instanceof Instance) {
            Instance i = (Instance) a;
            Instance j = (Instance) b;
            sim = i.getConcept().getActiveAmalgamFct()
                    .calculateSimilarity(i, j);
        } else if (f != null && f instanceof ISimFct) {
            sim = ((ISimFct) f).calculateSimilarity(a, b);
        }
        return sim;
    }

	/**
	 * Used to speed up the import of case bases if the number of cases
	 * is known.
	 * 
	 * @param cbName the name of the default case base to be created
	 * @param currentCBCount the number of expected cases
	 * @throws Exception
	 *             if there is another case base with that name
	 */
	public DefaultCaseBase createDefaultCB(String cbName, int currentCBCount)
			throws Exception {
		DefaultCaseBase newCasebase = new DefaultCaseBase(this, cbName,
				currentCBCount);
		caseBases.put(cbName, newCasebase);
		setChanged();
		notifyObservers();
		return newCasebase;
	}

	@Override
	public void update(Observable o, Object arg) {
		if (o instanceof ICaseBase && arg instanceof RenameAction) {
			// case base renamed
			String oldName = ((RenameAction)arg).getOldName();
			ICaseBase cb = caseBases.get(oldName);
			if (cb!=null && cb.equals(o)) {
				caseBases.remove(oldName);
				caseBases.put(cb.getName(), cb);
				setChanged();
				notifyObservers(arg);
			}
		} else {
			super.update(o, arg);
		}
	}

	/**
	 * Removes the case with the given name from each case base.
	 * 
	 * @param name the cases' name
	 */
	public void removeCase(String name) {
		for (ICaseBase cb: getCaseBases().values()) {
			cb.removeCase(name);
		}
	}
	
	/**
	 * Returns all instances that are known for this project.
	 * 
	 * @return a collection of all instances known for this project.
	 */
	public Collection<Instance> getAllInstances() {
		Collection<Instance> allInstForConcept = new LinkedList<Instance>();

	    for (Concept sub : getSubConcepts().values()) {
	    	allInstForConcept.addAll(sub.getAllInstances());
	    }
	    return allInstForConcept;
	}
}
