/*
 * myCBR License 1.1
 *
 * Copyright (c) 2008
 * Thomas Roth-Berghofer, Armin Stahl & Deutsches Forschungszentrum f&uuml;r K&uuml;nstliche Intelligenz DFKI GmbH
 * Further contributors: myCBR Team (see http://mycbr-project.net/contact.html for further information 
 * about the myCBR Team). 
 * All rights reserved.
 *
 * myCBR is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 2 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  USA
 *
 * Since myCBR uses some modules, you should be aware of their licenses for
 * which you should have received a copy along with this program, too.
 * 
 * endOfLic */
package de.dfki.mycbr;

import java.awt.Frame;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import org.jdom.Attribute;
import org.jdom.DataConversionException;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.filter.ElementFilter;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import de.dfki.mycbr.explanation.ConceptExplanationProvider;
import de.dfki.mycbr.explanation.ExplanationManager;
import de.dfki.mycbr.explanation.SMExplanationContainer;
import de.dfki.mycbr.model.casebase.CaseInstance;
import de.dfki.mycbr.model.similaritymeasures.*;
import de.dfki.mycbr.model.vocabulary.*;
import de.dfki.mycbr.modelprovider.ModelProvider;
import de.dfki.mycbr.modelprovider.ModelProviderProtege;
import de.dfki.mycbr.retrieval.RetrievalEngine;
import de.dfki.mycbr.retrieval.ui.RetrievalContainer;
import de.dfki.mycbr.retrieval.ui.RetrievalWidget;
import edu.stanford.smi.protege.widget.AbstractTabWidget;

/**
 * 
 * Central class for the cbr project. Provides access to the projects casebase,
 * similarity measures and the vocabulary.
 * 
 * @author myCBR Team
 */
public class CBRProject implements Serializable {

	private static final long serialVersionUID = 1L;

	// the current version
	// for the latest stable release see mycbr-project.net
	public final static String myCBR_VERSION = "2.6.6";

	private final static Logger log = Logger.getLogger(CBRProject.class
			.getName());

	private static CBRProject instance = null;

	/** maps model instance names (String) to its MMF (MMF_Standard). */
	private HashMap<String, MMF_Standard> mmfHolder = new HashMap<String, MMF_Standard>();

	/** maps ModelCls names to its IMF */
	protected HashMap<String, IMF_Standard> modelClsToImf = new HashMap<String, IMF_Standard>();

	private String projectDir;
	private String projectName;

	private ProjectOptions projectOptions = new ProjectOptions();
	private String versionHandlerMessage;
	private String projectVersion = "0.1";
	private String projectAuthorName = "unknown author";

	// For loading instance
	private SAXBuilder saxBuilder;
	private static boolean initMyCbrProjectFromScratch = false;
	private Collection<ClsDataRaw> rawData;
	private Document docSMF = null;
	private String versionPO = null;
	private String versionSMF = null;
	private String versionCB = null;
	boolean updateSuccess = true;
	private VersionHandler versionHandler;

	/**
	 * Construction without project files. This means, that there are no cases
	 * at startup and the project does not have a name. No similarity measure
	 * will be initialized. ModelCls, ModelSlots and CaseInstances are provided
	 * by the model provider.
	 * 
	 */
	private CBRProject() {
		// initialize logger
		try {

			// get logging properties
			LogManager.getLogManager().readConfiguration(
					getClass().getResourceAsStream("logging.properties"));

		} catch (Exception e) {
			log.log(Level.INFO, "Cannot initialize logger!", e);
			System.err.println("Cannot initialize logger!");
		}

		// initialize special value handler
		initSpecialValueHandler(null);

		// initialize model provider
		// with empty raw data because no caseInstances are provided yet
		log.fine("now initialize model provider");
		ModelProvider.getInstance().init(null);

		// the project hasn't got a name yet, because
		// the name can only be provided by the name of XML files
		log.fine("unnamed CBR project [" + projectName
				+ "] initialized successfully.");

		initSMFsForAllModelInstances();

	}

	public static void resetInstance() {
		instance = null;
		ConceptExplanationProvider.resetInstance();
		ExplanationManager.resetInstance();
		SMFContainer.resetInstance();
		SpecialValueHandler.resetInstance();
		ModelProvider.resetInstance();
		RetrievalContainer.resetInstance();
		ExplanationOptions.resetInstance();
		RetrievalEngine.resetInstance();
		RetrievalWidget.resetInstance();
	}
	
	/**
	 * Construction with project files. This means that similarity measures will
	 * be loaded from the project files. ModelCls, ModelSlots and CaseInstances
	 * are provided by the model provider. NOTE: The name of the project is the
	 * string before the file suffixes. E.g.: "example_CBR_SMF.XML" -->
	 * "example"
	 * 
	 * @param projectName
	 *            String the name of the project.
	 * @param projectDir
	 *            String the absolute path to the projects home directory.
	 * @throws Exception
	 *             if the myCBR project could not be initialized properly. E.g.
	 *             file not found, XML parse exception, etc.
	 */
	private CBRProject(String projectName, String projectDir) throws Exception {

		this.projectName = projectName;
		this.projectDir = projectDir;
		try {
			// initialize logger
			try {

				LogManager.getLogManager().readConfiguration(
						getClass().getResourceAsStream("logging.properties"));
				System.out.println("CBRProject.logger config = ["
						+ getClass().getResource("logging.properties")
								.toExternalForm() + "].");

			} catch (Exception e) {
				String msg = "Cannot initialize logger!";
				log.log(Level.INFO, msg, e);
				System.err.println(msg);
			}
			log.info("MyCBR: start loading [" + projectName
					+ "] in directory [" + projectDir + "]...");
			versionHandler = new VersionHandler();

			saxBuilder = new SAXBuilder();

			// start loading SMFs
			File smfunctionsFile = getProjectFile_SMFunctions();

			if (!smfunctionsFile.exists()) {
				log.warning("FILE NOT FOUND (similarity measures): "
						+ smfunctionsFile.getAbsolutePath()
						+ "\nInit Similarity measures...");
			} else {

				try {

					docSMF = saxBuilder.build(smfunctionsFile);
					versionSMF = Helper.decode(docSMF.getRootElement()
							.getAttributeValue(XMLConstants.XML_ATT_VERSION));
					if (!myCBR_VERSION.equals(versionSMF)) {
						// in case myCBR has a other version number than the xml
						// file
						// try to update the file to the new version
						updateSuccess &= versionHandler.transferSMFdata(
								versionSMF, docSMF);
					}

					if (docSMF.getRootElement().getAttribute(
							XMLConstants.XML_ATT_PROJECT_VERSION) != null) {
						setProjectVersion(Helper.decode(docSMF.getRootElement()
								.getAttributeValue(
										XMLConstants.XML_ATT_PROJECT_VERSION)));
					}

					if (docSMF.getRootElement().getAttribute(
							XMLConstants.XML_ATT_PROJECT_AUTHOR) != null) {
						setProjectAuthorName(Helper.decode(docSMF
								.getRootElement().getAttributeValue(
										XMLConstants.XML_ATT_PROJECT_AUTHOR)));
					}

				} catch (Throwable e) {
					log.info("Could not load project file ["
							+ smfunctionsFile.getAbsolutePath()
							+ "]. Fatal error");
					log.log(Level.INFO, e.getMessage(), e);
					return;
				}

			}
			// initialize special value handler
			initSpecialValueHandler(docSMF);

			// load case base (raw data)
			// raw data will be passed to model provider which will then create
			// the (final) CaseInstance objects.
			rawData = new ArrayList<ClsDataRaw>();
			File casebaseFile = getProjectFile_Instances();
			log.fine("project file [" + casebaseFile.getAbsolutePath()
					+ "] exists: [" + casebaseFile.exists() + "]");

			if (casebaseFile.exists()) {
				try {
					Document docCB = saxBuilder.build(casebaseFile);
					versionCB = Helper.decode(docCB.getRootElement()
							.getAttributeValue(XMLConstants.XML_ATT_VERSION));

					if (!myCBR_VERSION.equals(versionCB)) {
						// in case myCBR has a higher version number than the
						// xml file
						// try to update the file to the new version
						updateSuccess &= versionHandler.transferCasebaseData(
								versionCB, docCB);
					}

					if (docCB.getRootElement().getAttribute(
							XMLConstants.XML_ATT_PROJECT_VERSION) != null) {
						String value = Helper.decode(docCB.getRootElement()
								.getAttributeValue(
										XMLConstants.XML_ATT_PROJECT_VERSION));
						if (!getProjectVersion().equals(value))
							value = "[" + value + "/" + getProjectVersion()
									+ "]";
						setProjectVersion(value);
					}

					if (docCB.getRootElement().getAttribute(
							XMLConstants.XML_ATT_PROJECT_AUTHOR) != null) {
						String author = Helper.decode(docCB.getRootElement()
								.getAttributeValue(
										XMLConstants.XML_ATT_PROJECT_AUTHOR));

						if (!getProjectAuthorName().equals(author)) {
							author = author + " & " + getProjectAuthorName();
						}

						setProjectAuthorName(author);
					}

					// load the cases specified by the given file
					rawData = loadCasebaseRawData(docCB);

				} catch (Exception e) {
					String msg = "Error occurred reading XML Document";
					log.log(Level.SEVERE, msg, e);
					throw new Exception(msg, e);
				}

			} else {
				log.warning("FILE NOT FOUND (case base): "
						+ casebaseFile.getAbsolutePath()
						+ "\nNo case base file found for this project");
				if (!(ModelProvider.getInstance() instanceof ModelProviderProtege)
						&& !smfunctionsFile.exists()) {
					throw new Exception(
							"myCBR project not found! project name = "
									+ projectName + ", directory = "
									+ projectDir);
				}
			}

			// initialize model provider
			log.fine("now initialize model provider");
			ModelProvider.getInstance().init(rawData);

			// load SMFs
			if (!initMyCbrProjectFromScratch) {
				loadSMFs(docSMF);
			}

			initSMFsForAllModelInstances();

		} catch (Exception e) {
			e.printStackTrace();
		}

	}

	@SuppressWarnings("unchecked")
	private void initialize() {

		ConceptExplanationProvider.initInstance();
		
		if (CBRProject.initMyCbrProjectFromScratch) {
			return;
		}
		
		try {

			File explanationsFile = getProjectFile_Explanations();

			if (explanationsFile.exists()) {
				try {
					Document docExp = saxBuilder.build(explanationsFile);

					// load conceptual explanations
					ConceptExplanationProvider.getInstance().load(
							docExp.getRootElement());

					// load similarity measure explanation
					for (Iterator it = docExp.getRootElement().getDescendants(
							new ElementFilter(
									XMLConstants.XML_TAG_SMEXPLANATION)); it
							.hasNext();) {
						Element element = (Element) it.next();
						String instName = Helper
								.decode(element
										.getAttributeValue(XMLConstants.XML_ATT_MODEL_INSTNAME));
						String smfName = Helper
								.decode(element
										.getAttributeValue(XMLConstants.XML_ATT_SMFNAME));
						SMFHolder holder = SMFContainer.getInstance()
								.getSMFHolderForModelInstance(
										ModelProvider.getInstance()
												.getModelInstance(instName));
						AbstractSMFunction smf = holder
								.getCertainSMFunctionFromHolder(smfName);
						if (smf == null)
							continue;
						smf.initExplanationContainer(element);
					}

				} catch (Throwable e) {
					log.warning("Could not load explanations file.");
					log.log(Level.WARNING, e.getMessage(), e);
				}
			}

			// options must be loaded after initializing explanation manager!
			// load Project Options (if available)
			File optionsFile = getProjectFile_Options();
			if (optionsFile.exists()) {
				try {
					Document docPO = saxBuilder.build(optionsFile);
					versionPO = Helper.decode(docPO.getRootElement()
							.getAttributeValue(XMLConstants.XML_ATT_VERSION));
					if (!myCBR_VERSION.equals(versionPO)) {
						// in case myCBR has a higher version number than the
						// xml file
						// try to update the file to the new version
						updateSuccess &= versionHandler
								.transferProjectOptionsData(versionPO, docPO);
					}
					// now load options
					projectOptions.load(docPO.getRootElement());
					if (!myCBR_VERSION.equals(versionPO)) {
						ProjectOptions.setHasChanged();
					}

				} catch (Throwable e) {
					log
							.info("Could not load options file. Set default options");
					log.log(Level.INFO, e.getMessage(), e);
					projectOptions = new ProjectOptions();
				}

			} else {
				// version is okay
				versionPO = myCBR_VERSION;
			}

			log.info("MyCBR: [" + projectName + "] ...finished loading.");

			// find lowest version of all the project files
			String lowestVer = null;
			List<String> allVersions = Arrays.asList(versionCB, versionSMF,
					versionPO);
	
			if (!allVersions.contains(null)) {
				Collections.sort(allVersions);
				lowestVer = allVersions.get(0);
			}
	
			// do update work
			if (!myCBR_VERSION.equals(lowestVer)) {
				updateSuccess &= versionHandler.doUpdateWork(lowestVer);
				versionHandlerMessage = versionHandler.getComments();
				setHasChanged();
			}
	
			// inform user when update work was not successful
			if (!updateSuccess) {
				log
						.warning("COULD NOT UPDATE THE PROJECT FILES TO THE CURRENT VERSION OF MYCBR! ("
								+ myCBR_VERSION
								+ ")\nErrors may occur! Please contact the authors of MyCBR if you encounter serious problems.\nhttp://www.mycbr-project.net");
			}

		} catch (Exception e) {
			String msg = "COULD NOT LOAD MYCBR PROJECT! Directory = "
					+ projectDir + ", Project Name = " + projectName;
			log.severe(msg);
			// throw new Exception(msg, e);
		}
	}

	/**
	 * First checks consistency for data shared by myCBR and Protege. Then
	 * initializes IMF for all model classes. Is called by the constructors.
	 */
	private void initSMFsForAllModelInstances() {

		log.info("init SMFs for this project ");

		for (SMFHolder holder : SMFContainer.getInstance().values()) {
			// check consistency for data shared by myCBR and Protege
			holder.checkConsistency(null);
		}

		for (ModelCls cls : getAllModelCls()) {
			if (!modelClsToImf.containsKey(cls.getName())) {
				initIMF(cls);
				updateIMFMap(getIMFforModelCls(cls.getName()));
			}
		}
	}

	public static synchronized CBRProject getInstance(AbstractTabWidget widget) {
		if (instance == null) {
			// if CBR project already open: get into it.
			String projDir = (widget.getProject().getProjectDirectoryURI() == null ? null
					: new File(widget.getProject().getProjectDirectoryURI())
							.getAbsolutePath());
			String projectName = widget.getProject().getName();
			try {
				
				ModelProvider.initInstance(widget.getProject());
				SMFContainer.initInstance();
				SpecialValueHandler.initInstance();
				ExplanationManager.initInstance();
				ExplanationOptions.initInstance();
				RetrievalEngine.initInstance();
				
				openProject(projectName, projDir);
				
				
			} catch (Exception e) {
				log.log(Level.SEVERE, "Failed to load project", e); //$NON-NLS-1$
			}
			VersionUpdateDialog.showDialogMaybe((Frame) widget
					.getTopLevelAncestor());
			log.fine("successfully opened project."); //$NON-NLS-1$			
		}
		return instance;
	}

	/**
	 * Gets instance of CBRProject. It is assumed that a tab first called
	 * {@link #getInstance(AbstractTabWidget)} or the stand alone version called
	 * {@link #getInstance(File)} first.
	 * 
	 * @return instance of CBRProject, null, if #getInstance(AbstractTabWidget)
	 */
	public static synchronized CBRProject getInstance() {
		return instance;
	}

	/**
	 * Is used by stand alone version to create project
	 * 
	 * @param projFile
	 * @return CBRProject created from file, if not created before.
	 */
	public static synchronized CBRProject getInstance(File projFile) {
		if (instance == null) {
			String projectName = projFile
					.getName()
					.replaceAll(XMLConstants.FILENAME_SUFFIX_SMF, "").replaceAll(XMLConstants.FILENAME_SUFFIX_CASEBASE, ""); //$NON-NLS-1$ //$NON-NLS-2$
			String projDir = projFile.getParent();

			//
			// get cbr project
			//
			try {
				ModelProvider.initInstance(null);
				SMFContainer.initInstance();
				SpecialValueHandler.initInstance();
				ExplanationManager.initInstance();
				ExplanationOptions.initInstance();
				RetrievalEngine.initInstance();
				openProject(projectName, projDir);
			} catch (Exception e) {
				log.log(Level.SEVERE, "Failed to load project", e); //$NON-NLS-1$
			}
			log.info("successfully opened project."); //$NON-NLS-1$
		}
		return instance;
	}

	/**
	 * Getter for all multi-measure functions (MMF).
	 * 
	 * @see de.dfki.mycbr.model.similaritymeasures.Holder
	 * @return Collection of MMF_Standard objects.
	 */
	public Collection<MMF_Standard> getMmfHolders() {
		return mmfHolder.values();
	}

	/**
	 * returns the smfunction holder for the given instance. if there doesnt
	 * exist a holder for this inst yet, a new one will be created.
	 * 
	 * @param inst
	 * @return SMFHolder all similarity functions defined for this model
	 *         instance (slot/cls) or null if inst==null.
	 */
	public MMF_Standard getMMFforModelInstance(ModelInstance inst) {
		if (inst == null)
			return null;

		MMF_Standard mmf = getMMFforModelInstance(inst.getName());
		return mmf;
	}

	private MMF_Standard getMMFforModelInstance(String instName) {
		String key = instName;

		MMF_Standard mmf = (MMF_Standard) mmfHolder.get(key);
		if (mmf == null) {
			mmf = new MMF_Standard(ModelProvider.getInstance()
					.getModelInstance(instName),
					AbstractSMFunction.DEFAULT_SMF_NAME);
			mmfHolder.put(key, mmf);
		}

		return mmf;
	}

	/**
	 * Getter for all classes (ModelCls) of the vocabulary.
	 * 
	 * @return Collection of all ModelCls objects used in this project.
	 */
	public Collection<ModelCls> getAllModelCls() {
		Collection<ModelCls> allModelCls = ModelProvider.getInstance()
				.getAllModelCls();

		Collection<ModelCls> finalAllModelCls = new ArrayList<ModelCls>();
		for (Iterator<ModelCls> it = allModelCls.iterator(); it.hasNext();) {
			ModelCls cls = it.next();
			if (cls == null || cls.getName().startsWith(":"))
				continue;
			finalAllModelCls.add(cls);
		}
		return finalAllModelCls;
	}

	/**
	 * Returns a new File specifying the information used as training data
	 * 
	 * @return the file specifying the training data
	 */
	public File getProjectFile_Training_DB() {
		return new File(projectDir, projectName
				+ XMLConstants.FILENAME_SUFFIX_TRAINING_DB);
	}

	/**
	 * Saves all data. There are several project files: one for instances, one
	 * for smf functions, one for options, one for training data.
	 */
	public synchronized void save(String projectName, String projectDir) {
		log.info("start saving...");

		if (!projectDir.equals(this.projectDir)
				|| !projectName.equals(this.projectName)) {
			log.fine("Set project name [" + this.projectName + "] to ["
					+ projectName + "]\nSet project dir [" + this.projectDir
					+ "] to [" + projectDir + "]\n");

			this.projectDir = projectDir;
			this.projectName = projectName;
		}

		XMLOutputter xmlOutputter = new XMLOutputter();
		xmlOutputter.setFormat(Format.getPrettyFormat());

		try {
			// save all similarity measure functions (SMF)
			File file = getProjectFile_SMFunctions();
			PrintStream out = new PrintStream(file, "UTF8");

			String docHeader = String
					.format("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
							+ "<Document %s=\"%s\" %s=\"%s\" %s=\"%s\">",
							XMLConstants.XML_ATT_VERSION, myCBR_VERSION,
							XMLConstants.XML_ATT_PROJECT_VERSION,
							getProjectVersion(),
							XMLConstants.XML_ATT_PROJECT_AUTHOR,
							getProjectAuthorName());
			out.println(docHeader);

			// serialize SpecialValueHandler
			Element outElement = new Element(
					SpecialValueHandler.XML_TAG_SPECIAL_VALUE_HANDLER);
			SpecialValueHandler.getInstance().toXML(outElement);
			xmlOutputter.output(outElement, out);
			out.println();

			for (String key : new HashSet<String>(SMFContainer.getInstance()
					.keySet())) {
				SMFHolder holder = (SMFHolder) SMFContainer.getInstance().get(
						key);

				if (holder.size() == 0)
					continue;
				out.println(String.format("<%s>", XMLConstants.XML_TAG_HOLDER));
				for (Iterator<String> itHolder = holder.keySet().iterator(); itHolder
						.hasNext();) {
					String smfName = itHolder.next();
					AbstractSMFunction smf = (AbstractSMFunction) holder
							.get(smfName);
					System.out.println(String.format("Save SMF %s for slot %s",
							smfName, smf.getModelInstance().getName()));

					Element element = smf.initXMLElement();
					smf.toXML(element);

					// is current function active?
					element.setAttribute(XMLConstants.XML_ATT_ACTIVE, Boolean
							.toString((holder.getActiveSMF() == smf)));
					element.setAttribute(XMLConstants.XML_ATT_SIMMODE, Helper
							.encode(smf.getSMFunctionTypeName()));

					xmlOutputter.output(element, out);
					out.println();
				}

				// check for MMF (multiple values)
				AbstractSMFunction activeSMF = holder.getActiveSMF();
				if (activeSMF != null
						&& activeSMF.getModelInstance() instanceof ModelSlot) {
					ModelSlot slot = (ModelSlot) activeSMF.getModelInstance();
					if (slot.isMultiple()) {
						// serialize MMF
						MMF_Standard mmf = (MMF_Standard) mmfHolder.get(key);
						if (mmf != null) {
							Element mmfElement = new Element(
									XMLConstants.XML_TAG_MULTIMEASURE);
							Element smfElement = mmf.initXMLElement();
							mmf.toXML(smfElement);
							mmfElement.addContent(smfElement);
							xmlOutputter.output(mmfElement, out);
							out.println();
						}
					}
				}

				out
						.println(String.format("</%s>",
								XMLConstants.XML_TAG_HOLDER));

				// remove hasChanged flag.
				holder.setHasChanged(false);
			}

			// store all inheritance measure functions
			HashSet<IMF_Standard> alreadySavedIMF = new HashSet<IMF_Standard>();
			for (Iterator<String> it = modelClsToImf.keySet().iterator(); it
					.hasNext();) {
				String key = it.next();
				IMF_Standard imf = (IMF_Standard) modelClsToImf.get(key);
				if (alreadySavedIMF.contains(imf))
					continue;
				imf.checkConsistency(false);
				Element imfElement = imf.toXML();
				xmlOutputter.output(imfElement, out);
				out.println();
				alreadySavedIMF.add(imf);
			}

			out.println("</Document>");
			out.close();

			// and save case base (case instances)
			file = getProjectFile_Instances();
			out = new PrintStream(file, "UTF8");
			out.println(docHeader);

			for (Iterator<String> it = SMFContainer.getInstance().keySet()
					.iterator(); it.hasNext();) {
				String instName = it.next();
				ModelInstance inst = ModelProvider.getInstance()
						.getModelInstance(instName);

				// model instance must be a class
				if (!(inst instanceof ModelCls)) {
					continue;
				}
				convertInstancesOf((ModelCls) inst, out);
			}

			out.println("</Document>");
			out.close();

			// project options
			if (projectOptions.hasChanged()) {
				// save project options,too. (Otherwise, default values are
				// set.)
				file = getProjectFile_Options();
				out = new PrintStream(file, "UTF8");

				Document optdoc = new Document();

				Element optroot = new Element(XMLConstants.XML_TAG_DOCUMENT);
				optroot.setAttribute(XMLConstants.XML_ATT_VERSION,
						myCBR_VERSION);
				optdoc.setRootElement(optroot);

				projectOptions.toXML(optroot);

				xmlOutputter.output(optdoc, out);
				out.close();
			}

			// explanations
			file = getProjectFile_Explanations();
			out = new PrintStream(file, "UTF8");
			Document expdoc = new Document();
			Element root = new Element(XMLConstants.XML_TAG_DOCUMENT);
			root.setAttribute(XMLConstants.XML_ATT_VERSION, myCBR_VERSION);
			expdoc.setRootElement(root);

			// save conceptual explanations
			ConceptExplanationProvider.getInstance().toXML(root);

			// save similarity measure explanation
			for (SMFHolder holder : SMFContainer.getInstance().values()) {
				for (AbstractSMFunction smf : holder.values()) {
					SMExplanationContainer expContainer = smf
							.getExplanationContainer();
					if (expContainer == null)
						continue;
					Element element = new Element(
							XMLConstants.XML_TAG_SMEXPLANATION);
					element.setAttribute(XMLConstants.XML_ATT_MODEL_INSTNAME,
							Helper.encode(smf.getModelInstanceName()));
					element.setAttribute(XMLConstants.XML_ATT_SMFNAME, Helper
							.encode(smf.getSmfName()));
					expContainer.toXML(element);
					root.addContent(element);
				}
			}

			xmlOutputter.output(expdoc, out);
			out.close();

			if (root.getChildren().size() == 0)
				file.delete();

			log.info("...finished saving.");

		} catch (Exception e) {
			e.printStackTrace();
			log.severe(e.toString());
			log.severe("Saving failed.");
		}

	}

	/**
	 * Serializes all instances of class cls in XML format.
	 * 
	 * @param cls
	 *            ModelCls whose case instances are to serialize.
	 * @param out
	 *            OutputStream to write to.
	 * @return Element XML element.
	 * @throws IOException
	 *             if something's wrong when writing to out
	 */
	@SuppressWarnings("unchecked")
	private void convertInstancesOf(ModelCls cls, PrintStream out)
			throws IOException {

		XMLOutputter xmlOutputter = new XMLOutputter();
		xmlOutputter.setFormat(Format.getPrettyFormat());
		String header = String.format("<%s class=\"%s\">",
				XMLConstants.XML_TAG_INSTANCES_FOR_CLASS, Helper.encode(cls
						.getName()));
		out.println(header);

		// get all slots that belong to the given cls.
		ArrayList<ModelSlot> slots = new ArrayList<ModelSlot>(cls.listSlots());

		// generate prototype
		// (contains model infos like valuetypes, min/max/allowed values, super
		// cls, etc)
		Element prototypeElement = new Element(XMLConstants.XML_TAG_PROTOTYPE);
		// Comment comment = new Comment("defines value types for slots and
		// super cls. Important for standalone application and consistency
		// check.");
		// prototypeElement.addContent(comment);

		// super cls info.
		if (cls.getSuperCls() != null) {
			prototypeElement.setAttribute(XMLConstants.XML_ATT_SUPERCLASS,
					Helper.encode(cls.getSuperCls().getName()));
		}

		// slots value types
		for (Iterator<ModelSlot> slotIt = slots.iterator(); slotIt.hasNext();) {
			ModelSlot slot = slotIt.next();

			// filter 'weird' slots
			if (slot.getValueType() == null)
				continue;

			Element valElement = new Element(XMLConstants.XML_TAG_SLOTVALUE);
			valElement.setAttribute(XMLConstants.XML_ATT_SLOT, Helper
					.encode(slot.getName()));

			ValueType valType = slot.getValueType();
			valElement.setAttribute(XMLConstants.XML_ATT_VALUE, Helper
					.encode(slot.getValueType().toString()));

			if (slot.getMinimumValue() != null) {
				valElement.setAttribute(XMLConstants.XML_ATT_MIN_VALUE, Helper
						.toRightType(valType, slot.getMinimumValue())
						.toString());
			}
			if (slot.getMaximumValue() != null) {
				valElement.setAttribute(XMLConstants.XML_ATT_MAX_VALUE, Helper
						.toRightType(valType, slot.getMaximumValue())
						.toString());
			}
			if (slot.isMultiple()) {
				valElement.setAttribute(XMLConstants.XML_ATT_MULTIPLE, Boolean
						.toString(slot.isMultiple()));
			}
			if (slot.getAllowedValues() != null
					&& slot.getAllowedValues().size() != 0) {
				StringBuffer allowedValues = new StringBuffer();
				Iterator valIt = slot.getAllowedValues().iterator();

				if (slot.getValueType() == ValueType.INSTANCE) {
					Collection<String> c = new ArrayList<String>();
					while (valIt.hasNext()) {
						ModelCls tmpCls = (ModelCls) valIt.next();
						c.add(tmpCls.getName());
					}
					valIt = c.iterator();
				}

				if (valIt.hasNext()) {
					allowedValues.append((String) valIt.next());
				}
				while (valIt.hasNext()) {
					String value = (String) valIt.next();
					allowedValues.append(";" + value);
				}
				valElement.setAttribute(XMLConstants.XML_ATT_ALLOWED_VALUES,
						Helper.encode(allowedValues.toString()));
			}

			prototypeElement.addContent(valElement);
		}

		xmlOutputter.output(prototypeElement, out);
		out.println();

		// TODO
		boolean isDBconnected = false;
		if (!isDBconnected) {

			// convert data for all case instances.
			Collection<CaseInstance> c = cls.getDirectCaseInstances();
			for (Iterator<CaseInstance> it = c.iterator(); it.hasNext();) {
				CaseInstance currentInst = it.next();

				// store only direct cases
				if (currentInst.getModelCls() != cls)
					continue;

				Element instElement = new Element(
						XMLConstants.XML_TAG_CASE_INSTANCE);
				instElement.setAttribute(XMLConstants.XML_ATT_MODEL_INSTNAME,
						Helper.encode(currentInst.getName()));

				for (Iterator<ModelSlot> slotIt = slots.iterator(); slotIt
						.hasNext();) {
					ModelSlot slot = slotIt.next();

					// filter 'weird' slots
					if (slot.getValueType() == null)
						continue;

					Object value = currentInst.getSlotValue(slot);
					if (value instanceof CaseInstance)
						value = ((CaseInstance) value).getName();
					if (!slot.isMultiple()
							|| value == null
							|| SpecialValueHandler.getInstance()
									.isSpecialValue(value)) {
						Element valElement = new Element(
								XMLConstants.XML_TAG_SLOTVALUE);
						valElement.setAttribute(XMLConstants.XML_ATT_SLOT,
								Helper.encode(slot.getName()));
						valElement.setAttribute(XMLConstants.XML_ATT_VALUE,
								Helper.encode(value.toString()));
						instElement.addContent(valElement);
					} else {
						// multiple... process collection
						for (Iterator valuesIt = ((Collection) value)
								.iterator(); valuesIt.hasNext();) {
							Element valElement = new Element(
									XMLConstants.XML_TAG_SLOTVALUE);
							valElement.setAttribute(XMLConstants.XML_ATT_SLOT,
									Helper.encode(slot.getName()));
							valElement.setAttribute(XMLConstants.XML_ATT_VALUE,
									Helper.encode(valuesIt.next().toString()));
							instElement.addContent(valElement);
						}
					}
				}

				// result.addContent(instElement);
				xmlOutputter.output(instElement, out);
				out.println();

			}
		}
		out.println(String.format("</%s>",
				XMLConstants.XML_TAG_INSTANCES_FOR_CLASS));
	}

	@SuppressWarnings("unchecked")
	private ClsDataRaw readRawDataFrom(Element clsElement)
			throws DataConversionException {
		// store String representations of special values
		HashMap<String, SpecialValue> specialValuesStr = new HashMap<String, SpecialValue>();
		for (Iterator<SpecialValue> it = SpecialValueHandler.getInstance()
				.getAllSpecialValues().iterator(); it.hasNext();) {
			SpecialValue sv = it.next();
			specialValuesStr.put(sv.toString(), sv);
		}

		// read prototype
		Element prototypeElement = (Element) clsElement.getDescendants(
				new ElementFilter(XMLConstants.XML_TAG_PROTOTYPE)).next();

		// read cls and super cls entry
		String clsName = Helper.decode(clsElement
				.getAttributeValue(XMLConstants.XML_ATT_CLASSNAME));
		String superCls = (Helper.decode(prototypeElement
				.getAttributeValue(XMLConstants.XML_ATT_SUPERCLASS)) != null ? Helper
				.decode(prototypeElement
						.getAttributeValue(XMLConstants.XML_ATT_SUPERCLASS))
				: null);

		// maps slot names to slot data raw
		HashMap<String, SlotDataRaw> slotnamesToSlotDataRaw = new HashMap<String, SlotDataRaw>();
		for (Iterator slotIt = prototypeElement
				.getDescendants(new ElementFilter(
						XMLConstants.XML_TAG_SLOTVALUE)); slotIt.hasNext();) {
			Element slotElement = (Element) slotIt.next();

			String slotName = Helper.decode(slotElement
					.getAttributeValue(XMLConstants.XML_ATT_SLOT));

			// get its value type
			ValueType valType = ValueType.getValueType(Helper
					.decode(slotElement
							.getAttributeValue(XMLConstants.XML_ATT_VALUE)));

			// get its min/max values
			Number minVal = null;
			Number maxVal = null;
			boolean multiple = false;
			if ((valType == ValueType.FLOAT) || (valType == ValueType.INTEGER)) {
				if (slotElement.getAttribute(XMLConstants.XML_ATT_MIN_VALUE) != null)
					minVal = (Number) valType.newInstance(slotElement
							.getAttributeValue(XMLConstants.XML_ATT_MIN_VALUE));
				if (slotElement.getAttribute(XMLConstants.XML_ATT_MAX_VALUE) != null)
					maxVal = (Number) valType.newInstance(slotElement
							.getAttributeValue(XMLConstants.XML_ATT_MAX_VALUE));
			}

			if (slotElement.getAttribute(XMLConstants.XML_ATT_MULTIPLE) != null)
				multiple = slotElement.getAttribute(
						XMLConstants.XML_ATT_MULTIPLE).getBooleanValue();

			// get its allowed values
			Collection<String> allowedValues = null;
			if (slotElement.getAttribute(XMLConstants.XML_ATT_ALLOWED_VALUES) != null) {
				String[] allowedValStr = slotElement.getAttributeValue(
						XMLConstants.XML_ATT_ALLOWED_VALUES).split(";");
				allowedValues = new ArrayList<String>();
				for (int i = 0; i < allowedValStr.length; i++) {
					allowedValues.add(Helper.decode(allowedValStr[i]));
				}
			}

			SlotDataRaw slotDataRaw = new SlotDataRawImpl(slotName, valType,
					allowedValues, minVal, maxVal, multiple);
			slotnamesToSlotDataRaw.put(slotName, slotDataRaw);
		}

		// read case instances
		Collection<CaseDataRawImpl> caseInstances = new ArrayList<CaseDataRawImpl>();
		for (Iterator caseIt = clsElement.getDescendants(new ElementFilter(
				XMLConstants.XML_TAG_CASE_INSTANCE)); caseIt.hasNext();) {
			Element caseElement = (Element) caseIt.next();

			String caseName = Helper.decode(caseElement
					.getAttributeValue(XMLConstants.XML_ATT_MODEL_INSTNAME));
			CaseDataRawImpl caseInstanceDataSet = new CaseDataRawImpl(caseName,
					clsName);
			for (Iterator slotIt = caseElement
					.getDescendants(new ElementFilter(
							XMLConstants.XML_TAG_SLOTVALUE)); slotIt.hasNext();) {
				Element slotElement = (Element) slotIt.next();
				// read string attributes.
				String slotName = Helper.decode(slotElement
						.getAttributeValue(XMLConstants.XML_ATT_SLOT));
				String slotValStr = Helper.decode(slotElement
						.getAttributeValue(XMLConstants.XML_ATT_VALUE));

				// now get its appropriate value type and instantiate its java
				// object.
				log.fine(String.format(
						"Case [%s]: Read value for slot [%s] = %s", caseName,
						slotName, slotValStr));
				ValueType valueType = ((SlotDataRaw) slotnamesToSlotDataRaw
						.get(slotName)).getValueType();
				Object value = null;

				// distinguish between regular and special values
				if (specialValuesStr.containsKey(slotValStr)) {
					value = specialValuesStr.get(slotValStr);
				} else {
					// regular value
					if (valueType == ValueType.INSTANCE) {
						// temporary.
						// This value must be transformed by the modelProvider
						// to the corresponding case instance object
						value = slotValStr;
					} else {
						// convert String to the appropriate value type
						// (Integer, Double, ...)
						value = valueType.newInstance(slotValStr);
					}
				}

				// set value in case instance data set.
				SlotDataRaw sdr = (SlotDataRaw) slotnamesToSlotDataRaw
						.get(slotName);
				if (sdr.isMultiple()) {
					if (SpecialValueHandler.getInstance().isSpecialValue(value)) {
						caseInstanceDataSet.put(slotName, value);
					} else {
						Collection c = (Collection) caseInstanceDataSet
								.get(slotName);
						if (c == null) {
							c = new ArrayList<Object>();
						}
						c.add(value);
						caseInstanceDataSet.put(slotName, c);
					}
				} else {
					caseInstanceDataSet.put(slotName, value);
				}
			}

			caseInstances.add(caseInstanceDataSet);

		}
		ClsDataRaw clsDataRaw = new ClsDataRaw(clsName, superCls,
				slotnamesToSlotDataRaw, caseInstances);

		return clsDataRaw;
	}

	/**
	 * Load all smfunctions.
	 * 
	 * @param docSMF
	 *            XML Document describing the SMFs.
	 */
	@SuppressWarnings("unchecked")
	private void loadSMFs(Document docSMF) {
		try {
			// clear old holders
			SMFContainer.getInstance().clear();

			// read XML document (Similarity Measure Functions)
			// load SMFs
			for (Iterator fctIt = docSMF.getDescendants(new ElementFilter(
					XMLConstants.XML_TAG_SMFUNCTION)); fctIt.hasNext();) {
				try {
					// get next similarity function
					Element smfElement = (Element) fctIt.next();

					// according slot
					Attribute att = smfElement
							.getAttribute(XMLConstants.XML_ATT_MODEL_INSTNAME);
					String instname = Helper.decode(att.getValue());

					// log.fine("get model instance ["+instname+"]");
					ModelInstance instance = ModelProvider.getInstance()
							.getModelInstance(instname);
					if (instance == null) {
						log.fine("ModelInstance [" + instname
								+ "] not available for some reason.");
						continue;
					}
					log.fine("loading smfunction for model instance ["
							+ instname + "]");

					SMFHolder holder;
					AbstractSMFunction smf;
					if (smfElement.getParentElement().getName().equals(
							XMLConstants.XML_TAG_MULTIMEASURE)) {
						// multi measure
						smf = SMFunctionFactory.loadMMFunction(smfElement,
								instance);
						mmfHolder.put(instname, (MMF_Standard) smf);
					} else {
						// common smf
						smf = SMFunctionFactory.loadSMFunction(smfElement,
								instance);
						holder = SMFContainer.getInstance()
								.getSMFHolderForModelInstance(instance);
						holder.put(smf.getSmfName(), smf);

						// is active?
						att = smfElement
								.getAttribute(XMLConstants.XML_ATT_ACTIVE);
						boolean isActive = att.getBooleanValue();

						if (isActive || holder.size() == 1) {
							holder.initActiveSMF(smf);
						}
					}

				} catch (Exception e) {
					log.log(Level.SEVERE, "Could not load smfunction.", e);
				}

			}

			// load IMFs
			for (Iterator fctIt = docSMF.getDescendants(new ElementFilter(
					XMLConstants.XML_TAG_INHERITANCEMEASURE)); fctIt.hasNext();) {
				// get next IMF element
				Element element = (Element) fctIt.next();

				String topClsName = Helper.decode(element
						.getAttributeValue(XMLConstants.XML_ATT_TOPCLS));
				ModelCls instance = (ModelCls) ModelProvider.getInstance()
						.getModelInstance(topClsName);

				// inheritance measure
				IMF_Standard imf = SMFunctionFactory.loadIMFunction(element,
						instance);
				modelClsToImf.put(instance.getName(), imf);
				updateIMFMap(imf);
			}

		} catch (Exception e) {
			e.printStackTrace();
			log.log(Level.SEVERE, e.getMessage(), e);
		}

	}

	/**
	 * Load the casebase from XML document. The CaseDataRaw objects will be
	 * passed to the modelprovider which will then build the CaseInstance
	 * objects.
	 * 
	 * @param docCB
	 *            XML Document containing the casebase.
	 * @return Collection of CaseDataRawImpl objects.
	 */
	@SuppressWarnings("unchecked")
	private Collection<ClsDataRaw> loadCasebaseRawData(Document docCB) {
		Collection<ClsDataRaw> rawDataSets = new ArrayList<ClsDataRaw>();

		for (Iterator clsIt = docCB.getDescendants(new ElementFilter(
				XMLConstants.XML_TAG_INSTANCES_FOR_CLASS)); clsIt.hasNext();) {
			try {
				// get xml element for class
				Element instForClsElement = (Element) clsIt.next();
				// and read all its case instances
				rawDataSets.add(readRawDataFrom(instForClsElement));
			} catch (Exception e) {
				e.printStackTrace();
				log.log(Level.SEVERE, "loading failed!", e);
			}
		}

		return rawDataSets;
	}

	private File getProjectFile_SMFunctions() {
		return new File(projectDir, projectName
				+ XMLConstants.FILENAME_SUFFIX_SMF);
	}

	private File getProjectFile_Instances() {
		return new File(projectDir, projectName
				+ XMLConstants.FILENAME_SUFFIX_CASEBASE);
	}

	private File getProjectFile_Options() {
		return new File(projectDir, projectName
				+ XMLConstants.FILENAME_SUFFIX_OPTIONS);
	}

	private File getProjectFile_Explanations() {
		return new File(projectDir, projectName
				+ XMLConstants.FILENAME_SUFFIX_EXPLANATIONS);
	}

	public String getProjectName() {
		return projectName;
	}

	public String getProjectDir() {
		return projectDir;
	}

	/**
	 * 
	 * @param caseDataRaw
	 * @return
	 */
	public boolean confirmCaseNew(CaseDataRaw caseDataRaw) {
		log.info("New Case [" + caseDataRaw.getCaseName() + "] for class ["
				+ caseDataRaw.getTypeName() + "]");

		boolean result = ModelProvider.getInstance()
				.confirmCaseNew(caseDataRaw);
		if (!result) {
			log.warning("Could not write in modelProvider ["
					+ ModelProvider.getInstance().getClass().getName() + "]");
		}

		return result;
	}

	/**
	 * 
	 * @param caseInstance
	 * @param caseDataRaw
	 * @return
	 */
	public boolean confirmCaseChanges(CaseInstance caseInstance,
			CaseDataRaw caseDataRaw) {
		log.info("Change Case [" + caseInstance.getName() + "] : ["
				+ caseDataRaw.getTypeName() + "]");

		boolean successful = ModelProvider.getInstance().confirmCaseChanges(
				caseInstance, caseDataRaw);
		if (!successful) {
			log.warning("Could not write in modelProvider ["
					+ ModelProvider.getInstance().getClass().getName() + "]");
		}

		return successful;
	}

	/**
	 * 
	 * @param caseInstance
	 * @return
	 */
	public boolean confirmCaseDeletion(CaseInstance caseInstance) {
		log.info("Delete Case [" + caseInstance.getName() + "]");

		boolean successful = ModelProvider.getInstance().confirmCaseDeletion(
				caseInstance);
		if (!successful) {
			log.warning("Could not write in modelProvider ["
					+ ModelProvider.getInstance().getClass().getName() + "]");
		}

		return successful;
	}

	public boolean changeModelSlot_addNewSymbol(ModelSlot modelSlot,
			String newSymbol) {
		log.info("add new symbol for ModelSlot [" + modelSlot.getName()
				+ "]: [" + newSymbol + "]");

		boolean result = ModelProvider.getInstance()
				.changeModelSlot_addNewSymbol(modelSlot, newSymbol);

		log.info("adding new symbol was: ["
				+ (result ? "successful" : "NOT SUCCESSFUL") + "].");
		return result;

	}

	public boolean changeModelSlot_removeOldSymbol(ModelSlot modelSlot,
			String oldSymbol) {
		log.info("add new symbol for ModelSlot [" + modelSlot.getName()
				+ "]: [" + oldSymbol + "]");

		boolean result = ModelProvider.getInstance()
				.changeModelSlot_removeOldSymbol(modelSlot, oldSymbol);

		log.info("adding new symbol was: ["
				+ (result ? "successful" : "NOT SUCCESSFUL") + "].");
		return result;
	}

	/**
	 * Returns a CBR project. If this project (identified by projectName and
	 * projectDir) has already been opened the appropriate instance will be
	 * returned. For this comparison we do not care about the concrete
	 * ModelProvider instance. If there is no open project we construct one.
	 * 
	 * @param projectName
	 *            String name of the project
	 * @param projectDir
	 *            String directory of the project files
	 * @return CBRProject the demanded project
	 * @throws Exception
	 *             if the myCBR project could not initialized properly. E.g.
	 *             file not found, XML parse exception, etc.
	 */
	private static synchronized void openProject(String projectName,
			String projectDir) throws Exception {

		if (projectName == null) {
			// this project has not been saved, yet. And it does not even have a
			// name.
			if (instance == null) {
				log
						.fine("initialize unnamed instance instance. project doesnt have a name nor a directory, yet.");
				CBRProject.initMyCbrProjectFromScratch = true;
				instance = new CBRProject();
				instance.initialize();
				return;
			}
		}
		if (projectDir == null) {
			projectDir = new File(projectName).getParent();
		}
		instance = new CBRProject(projectName, projectDir);
		instance.initialize();
	}

	public void renameModelInstance(String oldName, String newName) {
		log.fine("rename [" + oldName + "] to [" + newName + "]");

		ModelProvider.getInstance().renameModelInstance(oldName, newName);

		SMFHolder tmp = (SMFHolder) SMFContainer.getInstance().get(oldName);
		if (tmp != null)
			tmp.renameSMFs(oldName, newName);
		SMFContainer.getInstance().remove(oldName);
		SMFContainer.getInstance().put(newName, tmp);

		// check consistency for IMF
		ModelInstance instance = ModelProvider.getInstance().getModelInstance(
				newName);
		if (instance instanceof ModelCls) {
			modelClsToImf.put(newName, modelClsToImf.get(oldName));
			IMF_Standard imf = getIMFforModelCls(instance.getName());
			imf.renamedCls(oldName, newName);
			updateIMFMap(imf);
			modelClsToImf.remove(oldName);
		}
	}

	public AbstractSMFunction checkWrapMMF(AbstractSMFunction smf) {
		if (!(smf.getModelInstance() instanceof ModelSlot)) {
			return smf;
		}
		ModelSlot slot = (ModelSlot) smf.getModelInstance();
		if (slot.isMultiple()) {
			MMF_Standard mmf = getMMFforModelInstance(slot);
			if (mmf == null) {
				log.fine("Create Default multi measure for [" + slot.getName()
						+ "].");
				mmf = SMFunctionFactory.createNewMMFunction(slot,
						AbstractSMFunction.DEFAULT_SMF_NAME);
				mmfHolder.put(slot.getName(), mmf);
			}
			mmf.setInternalSMF(smf);
			smf = mmf;
		}
		return smf;
	}

	public SimMap getActiveSMFs(ModelCls cls) {
		AbstractClassSM smfC = (AbstractClassSM) SMFContainer.getInstance()
				.getSMFHolderForModelInstance(cls).getActiveSMF();
		if (smfC == null) {
			log.info("No global similarity measure found for [" + cls + "]");
			return null;
		}
		SimMap activeSMFs = smfC.getSimMap();
		return activeSMFs;
	}

	public SMFHolder getSpecialSMFHolderForOOComposition(ModelSlot slot) {
		if (slot.getValueType() == ValueType.INSTANCE) {
			// find corresponding cls
			if (slot.getAllowedValues().size() < 0)
				return null;
			ModelCls rangeCls = (ModelCls) slot.getAllowedValues().iterator()
					.next();
			return SMFContainer.getInstance().getSMFHolderForModelInstance(
					rangeCls);
		}
		return null;
	}

	public void setHasChanged() {
		log.fine("Project has changed.");
		ModelProvider.getInstance().setHasChanged();
	}

	public ProjectOptions getProjectOptions() {
		return projectOptions;
	}

	/**
	 * initializes SpecialValueHandler. if there is nothing to load from a XML
	 * document, pass null.
	 * 
	 * @param docSMF
	 *            Document the XML document containing the serialization of the
	 *            SVH. May be null.
	 */
	private void initSpecialValueHandler(Document docSMF) {
		// initialize SpecialValueHandler
		try {
			if (docSMF != null) {
				// load SpecialValueHandler
				Element svhElement = docSMF.getRootElement().getChild(
						SpecialValueHandler.XML_TAG_SPECIAL_VALUE_HANDLER);
				if (svhElement != null)
					SpecialValueHandler.getInstance().load(svhElement);
			}
		} catch (Exception e) {
			log.log(Level.SEVERE, "Error in SpecialValueHandler", e);
			e.printStackTrace();
		}
	}

	public IMF_Standard getIMFforModelCls(String clsName) {
		return (IMF_Standard) modelClsToImf.get(clsName);
	}

	public void modelInstanceHasBeenCreated(String name) {
		log.fine("ModelInstance [" + name + "] has been created");
		ModelInstance newInstance = ModelProvider.getInstance()
				.getModelInstance(name);

		if (newInstance instanceof ModelCls) {
			initIMF((ModelCls) newInstance);
		} else {
			// newInstance is a ModelSlot
			for (Iterator<ModelCls> it = getAllModelCls().iterator(); it
					.hasNext();) {
				ModelCls cls = (ModelCls) it.next();
				if (cls.listSlots().contains(newInstance)) {
					// check consistency (quiet)
					for (Iterator<AbstractSMFunction> smfIt = SMFContainer
							.getInstance().getSMFHolderForModelInstance(cls)
							.values().iterator(); it.hasNext();) {
						AbstractSMFunction smf = smfIt.next();
						if (smf == null)
							continue;
						smf.checkConsistency(null, true);
					}
				}
			}
		}

		// init similarity measure (SMF)
		try {
			SMFContainer.getInstance()
					.getSMFHolderForModelInstance(newInstance)
					.checkConsistency(null);
		} catch (Exception e) {
			log
					.log(Level.FINE,
							"Could not initialize SMF. No problem here.", e);
		}
	}

	private void initIMF(ModelCls cls) {
		// init inheritance measure (IMF)
		IMF_Standard imf = null;

		// does the cls have any ancestors?
		boolean areThereAncestors = false;

		if (cls.getSuperCls() != null) {
			areThereAncestors = true;
		}

		// in case the current cls does not have ancestors
		// check whether it is the super cls of any other cls
		if (!areThereAncestors) {
			for (Iterator<ModelCls> it = getAllModelCls().iterator(); it
					.hasNext();) {
				ModelCls cls2 = it.next();
				if (cls2.getSuperCls() == cls) {
					areThereAncestors = true;
					break;
				}
			}
		}

		// if the given cls has ancestors or is a super cls of
		// any cls itself try to integrate new model cls
		// else create new imf
		if (areThereAncestors) {
			// find highest model cls by inheritance
			imf = getIMFforModelCls(Helper
					.findHighestModelClsByInheritance(cls).getName());
			if (imf != null) {
				imf.integrateNewModelCls(cls);
			} else {
				imf = SMFunctionFactory.createNewIMFunction(cls);
			}
		} else {
			// create new IMF
			imf = SMFunctionFactory.createNewIMFunction(cls);
		}
		modelClsToImf.put(cls.getName(), imf);
	}

	public void modelClsHasBeenDeleted(String name) {
		ModelInstance deletedInstance = ModelProvider.getInstance()
				.getModelInstance(name);
		if (deletedInstance == null)
			return;
		log.fine("delete modelInstance [" + deletedInstance + "] in instance.");

		// delete all smfs
		SMFContainer.getInstance()
				.getSMFHolderForModelInstance(deletedInstance).clear();

		if (deletedInstance instanceof ModelCls) {
			IMF_Standard imf = getIMFforModelCls(name);
			imf.checkConsistency(true);
			modelClsToImf.remove(name);
		}
	}

	public void checkConsistencyForSlot(ModelSlot slot) {
		log.fine("Value type of slot [" + slot + "] changed");
		SMFHolder holder = SMFContainer.getInstance()
				.getSMFHolderForModelInstance(slot);
		holder.checkConsistency(null);
	}

	public AbstractSMFunction newSMF(ModelInstance modelInstance,
			String newSmfName) throws Exception {

		if (SMFunctionFactory.getSMFClassesForInstance(modelInstance) == null)
			return null;

		log.fine("init new SMF for [" + modelInstance.getName()
				+ "]. smf name = [" + newSmfName + "]");
		AbstractSMFunction smf = null;

		SMFHolder holder = SMFContainer.getInstance()
				.getSMFHolderForModelInstance(modelInstance);

		if (!Helper.checkSMFName(newSmfName) || holder.containsKey(newSmfName)) {
			log.info("Name of new SMF is invalid [" + newSmfName + "]");
			return null;
		}
		smf = SMFunctionFactory.createNewSMFunction(modelInstance, newSmfName);
		holder.confirmSMFunction(smf);

		return smf;
	}

	public void _subClsAdded(ModelCls superCls, ModelCls subCls) {
		log.fine("SubCls [" + subCls + "] added to superCls [" + superCls
				+ "].");

		IMF_Standard imfSubCls = getIMFforModelCls(subCls.getName());
		IMF_Standard imfSuperCls = getIMFforModelCls((superCls == null ? null
				: superCls.getName()));

		if (imfSuperCls == null) {
			if (superCls.getName().startsWith(":")) {
				// this class stands for itself
				log.fine("split");
				IMF_Standard imfSplit = imfSubCls.split(subCls);
				updateIMFMap(imfSplit);
				return;
			} else {
				log
						.fine("this here can never happen! (At least, it should not)");
			}
		}
		if (imfSubCls == imfSuperCls) {
			log.fine("still the same IMF.");
			imfSubCls.checkConsistency(true);
			updateIMFMap(imfSuperCls);
		} else {
			log.fine("split and merge");
			IMF_Standard imfSplit = imfSubCls.split(subCls);
			imfSuperCls.merge(superCls, imfSplit);
			updateIMFMap(imfSuperCls);
		}
	}

	/**
	 * All affected ModelCls that are registered in the inheritance measure
	 * function (IMF) will be mapped to this IMF. This means, the given IMF is
	 * the right inheritance measure for the affected ModelCls.
	 * 
	 * @param imf
	 *            IMF responsible for all ModelCls that are registered in IMF
	 */
	private void updateIMFMap(IMF_Standard imf) {
		for (Iterator<ModelCls> it = imf.getAffectedCls().iterator(); it
				.hasNext();) {
			ModelCls cls = it.next();
			modelClsToImf.put(cls.getName(), imf);
		}
	}

	/**
	 * myCBR is supposed to be backward compatible. If a project which has an
	 * older version is opened, myCBR checks for compatibility updates. The
	 * information given by these updates is provided by this method. If nothing
	 * has to be updated, null will be returned. NOTE: The version handler
	 * message is very important, since it may contain information on e.g.
	 * deleted similarity measures.
	 * 
	 * @return String message concerning version control. (null means: no
	 *         updates done)
	 */
	public String getVersionHandlerMessage() {
		return versionHandlerMessage;
	}

	/**
	 * Gets the current version of the project.
	 * 
	 * @return the current projects version
	 */
	public String getProjectVersion() {
		return projectVersion;
	}

	/**
	 * Sets the current projects version to projectVersion
	 * 
	 * @param projectVersion
	 *            specifying the version the current project should have
	 */
	public void setProjectVersion(String projectVersion) {
		this.projectVersion = projectVersion;
	}

	/**
	 * Gets the author name for the current project
	 * 
	 * @return the author of this project
	 */
	public String getProjectAuthorName() {
		return projectAuthorName;
	}

	/**
	 * Sets the projects author to authorName. If the specified name is null or
	 * empty, the name of the author will be "unknown author"
	 * 
	 * @param authorName
	 *            the name of the author
	 */
	public void setProjectAuthorName(String authorName) {
		if (authorName == null || authorName.equals("")) {
			authorName = "unknown author";
		}
		this.projectAuthorName = authorName;
	}

}
