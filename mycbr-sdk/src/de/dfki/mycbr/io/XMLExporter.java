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
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import de.dfki.mycbr.core.ICaseBase;
import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Attribute;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.casebase.SimpleAttribute;
import de.dfki.mycbr.core.casebase.SymbolAttribute;
import de.dfki.mycbr.core.explanation.ConceptExplanation;
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
import de.dfki.mycbr.core.model.StringDesc;
import de.dfki.mycbr.core.model.SymbolDesc;
import de.dfki.mycbr.core.similarity.AdvancedDoubleFct;
import de.dfki.mycbr.core.similarity.AdvancedFloatFct;
import de.dfki.mycbr.core.similarity.AdvancedIntegerFct;
import de.dfki.mycbr.core.similarity.AmalgamationFct;
import de.dfki.mycbr.core.similarity.DateFct;
import de.dfki.mycbr.core.similarity.DoubleFct;
import de.dfki.mycbr.core.similarity.FloatFct;
import de.dfki.mycbr.core.similarity.FunctionEnum;
import de.dfki.mycbr.core.similarity.ISimFct;
import de.dfki.mycbr.core.similarity.IntegerFct;
import de.dfki.mycbr.core.similarity.IntervalFct;
import de.dfki.mycbr.core.similarity.OrderedSymbolFct;
import de.dfki.mycbr.core.similarity.Similarity;
import de.dfki.mycbr.core.similarity.StringFct;
import de.dfki.mycbr.core.similarity.SymbolFct;
import de.dfki.mycbr.core.similarity.TaxonomyFct;
import de.dfki.mycbr.core.similarity.TaxonomyNode;

/**
 * Exports the given project using myCBR's internal XML format.
 * 
 * @author myCBR Team
 * 
 */
public class XMLExporter {

	public static final String PROJECT_FILE_EXTENSION = ".myCBR";
	public static final String CB_FILE_EXTENSION = ".myCB";

	/**
	 * Saves the given project to the given path
	 * @param prj the project to be saved
	 * @param path the path that should be used
	 */
	public static void save(Project prj, String path) {
		try {
			String extension = path.lastIndexOf(".prj")!=-1 ? ".prj" : ".zip";
			
			OutputStream fout = new FileOutputStream(new File(path.replace(
					extension, ".myCBR")));
			OutputStream bout = new BufferedOutputStream(fout);
			OutputStreamWriter out = new OutputStreamWriter(bout, "8859_1");

			out.write("<?xml version=\"1.0\" ");
			out.write("encoding=\"ISO-8859-1\"?>\r\n");
			out.write("<Project name=\"" + encode(prj.getName())
					+ "\" author=\"" + encode(prj.getAuthor())
					+ "\" >\r\n");

			// save special value description
			out.write("<svs>\r\n");
			for (String svEntry : prj.getSpecialValueDesc().getAllowedValues()) {
				out.write("  <sv name=\"" + encode(svEntry) + "\"/>\r\n");
			}
			// save special value function
			for (ISimFct fct : prj.getSpecialValueDesc().getSimFcts()) {
				if (fct instanceof SymbolFct) {
					SymbolFct f = (SymbolFct) fct;
					out.write("<fct name=\"" + encode(f.getName())
							+ "\" type=\"" + FunctionEnum.Symbol + "\""
							+ " mt=\"" + f.getMultipleConfig().getMainType()
							+ "\" " + "r=\"" + f.getMultipleConfig().getReuse()
							+ "\" " + "t=\"" + f.getMultipleConfig().getType()
							+ "\" " + "symm=\"" + f.isSymmetric() + "\">\r\n");
					for (SymbolAttribute a : f.getDesc().getSymbolAttributes()) {
						out.write("<qsym name=\"" + a.getValue() + "\">\r\n");
						for (SymbolAttribute a2 : f.getDesc()
								.getSymbolAttributes()) {
							Similarity sim = f.calculateSimilarity(a, a2);
							if (!a2.equals(a)) {
								if (sim.getValue() != 0.0) {
									out.write("<csym name=\"" + a2.getValue()
											+ "\" sim=\"" + sim + "\"/>\r\n");
								}
							} else if (sim.getRoundedValue() != 1.0) {
								out.write("<csym name=\"" + a2.getValue()
										+ "\" sim=\"" + sim + "\"/>\r\n");
							}
						}
						out.write("</qsym>\r\n");
					}
					out.write("</fct>\r\n");
				}
			}
			out.write("</svs>\r\n");

			// save model
			out.write("<model>\r\n");
			for (Concept c : prj.getSubConcepts().values()) {
				saveConcept(out, c, "  ");
			}
			out.write("</model>\r\n");

			// save inheritance description
			out.write("<hierarchy>\r\n");
			saveTaxonomy(out, prj.getInhFct(), "  ");
			out.write("</hierarchy>\r\n");
			for (ICaseBase cb: prj.getCaseBases().values()) {
				out.write("<cases no=\"" + cb.getCases().size()
						+ "\" cb=\"" + cb.getName() + "\"/>\r\n");
			}
			out.write("</Project>\r\n");

			out.flush();
			out.close();

			// **********************************************************
			// ******************* WRITE CB FILE ************************
			// **********************************************************
			File cbFile = new File(path.replace(extension, ".myCB"));
			fout = new FileOutputStream(cbFile);
			bout = new BufferedOutputStream(fout);
			out = new OutputStreamWriter(bout, "8859_1");

			out.write("<?xml version=\"1.0\" ");
			out.write("encoding=\"ISO-8859-1\"?>\r\n");
			out.write("<Project name=\"" + encode(prj.getName())
					+ "\" author=\"" + encode(prj.getAuthor())
					+ "\" >\r\n");


			// save instances
			for (Concept c : prj.getAllSubConcepts().values()) {
				out.write("<instances name=\"" + encode(c.getName()) + "\">\r\n");
				saveInstances(out, c);
				out.write("</instances>\r\n");
			}
			
			for (ICaseBase cb: prj.getCaseBases().values()) {
				out.write("<cb name=\"" + encode(cb.getName()) + "\">\r\n");
				for (Instance i: cb.getCases()) {
					out.write("     <case name=\"" + encode(i.getName()) + "\" />\r\n");	
				}
				out.write("</cb>\r\n");
				
			}
			

			out.write("</Project>\r\n");

			out.flush();
			out.close();

			// save explanations
			File expFile = new File(path.replace(extension, ".myExp"));
			fout = new FileOutputStream(expFile);
			bout = new BufferedOutputStream(fout);
			out = new OutputStreamWriter(bout, "8859_1");

			out.write("<?xml version=\"1.0\" ");
			out.write("encoding=\"ISO-8859-1\"?>\r\n");
			out.write("<Project name=\"" + encode(prj.getName())
					+ "\" author=\"" + encode(prj.getAuthor())
					+ "\" >\r\n");

			for (ConceptExplanation e : prj.getExplanationManager()
					.getExplanations()) {
				if (e.getExplainable() instanceof Concept) {
					out.write("<exp obj=\""
							+ encode(e.getExplainable().getName())
							+ "\" type=\"" + e.getExplainable().getExpType()
							+ "\" >\r\n");
				} else if (e.getExplainable() instanceof AttributeDesc) {
					out.write("<exp obj=\""
							+ encode(e.getExplainable().getName())
							+ "\" c=\""
							+ encode(((AttributeDesc) e.getExplainable())
									.getOwner().getName()) + "\" type=\""
							+ e.getExplainable().getExpType() + "\" >\r\n");
				} else if (e.getExplainable() instanceof SimpleAttribute) {
					out.write("<exp obj=\""
							+ encode(e.getExplainable().getName())
							+ "\" c=\""
							+ encode(((SimpleAttribute) e.getExplainable())
									.getAttributeDesc().getOwner().getName())
							+ "\" desc=\""
							+ encode(((SimpleAttribute) e.getExplainable())
									.getAttributeDesc().getName())
							+ "\" type=\"" + e.getExplainable().getExpType()
							+ "\" >\r\n");
				} else if (e.getExplainable() instanceof Instance) {
					out.write("<exp obj=\""
							+ encode(e.getExplainable().getName())
							+ "\" c=\""
							+ encode(((Instance) e.getExplainable())
									.getConcept().getName()) + "\" type=\""
							+ e.getExplainable().getExpType() + "\" >\r\n");
				}
				out.write("  <desc>\r\n");
				out.write(encode(e.getDescription()));
				out.write("  </desc>\r\n");
				for (String url : e.getLinks()) {
					out.write("  <link url=\"" + url + "\" />\r\n");
				}
				out.write("</exp>\r\n");
			}
			out.write("</Project>\r\n");

			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		zip(path);
	}

	/**
	 * Creates one zip file x.prj containing the exported files. 
	 * @param path the path that was used to save the project
	 */
	private static void zip(String path) {
		String extension = path.endsWith(".zip") ? ".zip" : ".prj";
		new File(path).delete(); // delete old zip file

		// generate new zip file
		FileOutputStream dest = null;
		try {
			dest = new FileOutputStream(path);
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		ZipOutputStream out = new ZipOutputStream(
				new BufferedOutputStream(dest));
		File f1 = new File(path.replace(extension, ".myCBR"));
		File f2 = new File(path.replace(extension, ".myCB"));
		File f3 = new File(path.replace(extension, ".myExp"));

		addFile(f1, out);
		addFile(f2, out);
		addFile(f3, out);

		new File(path.replace(extension, ".myCBR")).delete();
		new File(path.replace(extension, ".myCB")).delete();
		new File(path.replace(extension, ".myExp")).delete();
		try {
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Adds the given file to the zip file that has to be created
	 * @param f the file that should be added to the zip file 
	 */
	private static void addFile(File f, ZipOutputStream out) {
		try {
			byte data[] = new byte[2048];
			FileInputStream in = new FileInputStream(f);
			int slash = f.getName().lastIndexOf(File.separator);
			String name;

			// Add ZIP entry to output stream.
			name = f.getName().substring(slash + 1);
			out.putNextEntry(new ZipEntry(name));

			// Transfer bytes from the file to the ZIP file
			int len;
			while ((len = in.read(data)) > 0) {
				out.write(data, 0, len);
			}

			// Complete the entry
			in.close();
			out.closeEntry();

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}

	}

	/**
	 * Writes the instances' XML representation to the given output.
	 *   
	 * @param out the output stream
	 * @param c the concept whose instances should be exported
	 */
	private static void saveInstances(OutputStreamWriter out, Concept c) {

		try {
			for (Instance i : c.getDirectInstances()) {
				out.write("  <instance id=\"" + encode(i.getName())
						+ "\" >\r\n");
				for (Map.Entry<AttributeDesc, Attribute> entry : i
						.getAttributes().entrySet()) {
					String value = entry.getValue().getValueAsString();
					if (value != Project.UNDEFINED_SPECIAL_ATTRIBUTE) { // only write attribute value, if it is not _undefined_
																	// because this is the default value and will be set automatically
																	// if no value is given
						out.write("    <att name=\""
								+ encode(entry.getKey().getName())
								+ "\" value=\"" + encode(value) + "\" />\r\n");
					}
				}
				out.write("  </instance>\r\n");
			}

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Writes the given concept's XML representation to the given
	 * output.
	 * 
	 * @param out the output stream
	 * @param c the concept to be exported
	 * @param string tab for indentation
	 */
	private static void saveConcept(OutputStreamWriter out, Concept c,
			String tab) {
		try {
			out.write(tab + "<concept name=\"" + encode(c.getName()) + "\">\r\n");
			for (AttributeDesc a : c.getAttributeDescs().values()) {
				saveDesc(out, a, tab + "  ");
			}
			for (Concept sub : c.getSubConcepts().values()) {
				saveConcept(out, sub, tab + "  ");
			}
			for (AmalgamationFct f : c.getAvailableAmalgamFcts()) {
				saveAmalgam(out, f, tab + "  ", c.getActiveAmalgamFct().equals(
						f));
			}
			out.write(tab + "</concept>\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Writes the given amalgamation function's XML representation to the
	 * given output.
	 * 
	 * @param out the given output stream
	 * @param f the amalgamation function to be exported
	 * @param tab tab for indentation
	 * @param active flag for marking the amalgamation function active
	 */
	private static void saveAmalgam(OutputStreamWriter out, AmalgamationFct f,
			String tab, boolean active) {
		try {
			out.write(tab + "<amalgam name=\"" + encode(f.getName())
					+ "\" type=\"" + f.getType() + "\" active=\"" + active
					+ "\" >\r\n");
			for (AttributeDesc d : f.getConcept().getAllAttributeDescs()
					.values()) {
				String localFctName = "default";
				if (d instanceof ConceptDesc) {
					localFctName = ((AmalgamationFct) f.getActiveFct(d))
							.getName();
				} else {
					if (f.getActiveFct(d) != null) {
						localFctName = ((ISimFct) f.getActiveFct(d)).getName();
					}
				}
				out.write(tab + "  <entry name=\"" + encode(d.getName())
						+ "\" active=\"" + f.isActive(d) + "\" " + "fct=\""
						+ localFctName + "\" weight=\"" + f.getWeight(d)
						+ "\"/>\r\n");
			}
			out.write(tab + "</amalgam>\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Writes the given attribute description's XML representation
	 * to the given output.
	 * 
	 * @param out the given output
	 * @param a the attribute description the be exported
	 * @param tab tab for indentation
	 */
	private static void saveDesc(OutputStreamWriter out, AttributeDesc a,
			String tab) {
		try {
			if (a instanceof SimpleAttDesc) {
				SimpleAttDesc s = (SimpleAttDesc) a;
				// save desc
				if (a instanceof IntegerDesc) {
					IntegerDesc d = (IntegerDesc) a;
					out.write(tab + "<desc name=\"" + encode(a.getName())
							+ "\" type=\"" + DescriptionEnum.Integer
							+ "\" min=\"" + d.getMin() + "\" max=\""
							+ d.getMax() + "\" mult=\"" + d.isMultiple()
							+ "\" >\r\n");
				} else if (a instanceof FloatDesc) {
					FloatDesc d = (FloatDesc) a;
					out.write(tab + "<desc name=\"" + encode(a.getName())
							+ "\" type=\"" + DescriptionEnum.Float
							+ "\" min=\"" + d.getMin() + "\" max=\""
							+ d.getMax() + "\" mult=\"" + d.isMultiple()
							+ "\" >\r\n");
				} else if (a instanceof DoubleDesc) {
					DoubleDesc d = (DoubleDesc) a;
					out.write(tab + "<desc name=\"" + encode(a.getName())
							+ "\" type=\"" + DescriptionEnum.Double
							+ "\" min=\"" + d.getMin() + "\" max=\""
							+ d.getMax() + "\" mult=\"" + d.isMultiple()
							+ "\" >\r\n");
				} else if (a instanceof BooleanDesc) {
					out.write(tab + "<desc name=\"" + encode(a.getName())
							+ "\" type=\"" + DescriptionEnum.Boolean
							+ "\" mult=\"" + a.isMultiple() + "\" >\r\n");
				} else if (a instanceof SymbolDesc) {
					SymbolDesc d = (SymbolDesc) a;
					out.write(tab + "<desc name=\"" + encode(a.getName())
							+ "\" type=\"" + DescriptionEnum.Symbol
							+ "\" mult=\"" + d.isMultiple() + "\" >\r\n");
					for (String symb : d.getAllowedValues()) {
						out.write(tab + "  <symbol value=\"" + encode(symb)
								+ "\" />\r\n");
					}
				} else if (a instanceof StringDesc) {
					out.write(tab + "<desc name=\"" + encode(a.getName())
							+ "\" type=\"" + DescriptionEnum.String
							+ "\" mult=\"" + a.isMultiple() + "\" >\r\n");
				} else if (a instanceof IntervalDesc) {
					IntervalDesc d = (IntervalDesc) a;
					out.write(tab + "<desc name=\"" + encode(a.getName())
							+ "\" type=\"" + DescriptionEnum.Interval
							+ "\" min=\"" + d.getMin() + "\" max=\""
							+ d.getMax() + "\" mult=\"" + d.isMultiple()
							+ "\" >\r\n");
				} else if (a instanceof DateDesc) {
					DateDesc d = (DateDesc) a;
					SimpleDateFormat df = d.getFormat();
					out.write(tab + "<desc name=\"" + encode(a.getName())
							+ "\" type=\"" + DescriptionEnum.Date + "\" format=\"" + df.toPattern() + "\" min=\""
							+ df.format(d.getMinDate()) + "\" max=\"" + df.format(d.getMaxDate())
							+ "\" mult=\"" + d.isMultiple() + "\" >\r\n");
				}
				// save fcts
				for (ISimFct f : s.getSimFcts()) {
					saveFct(out, f, tab.concat("  "));
				}
				out.write(tab + "</desc>\r\n");
			} else { // ConceptDesc
				ConceptDesc d = (ConceptDesc) a;
				out.write(tab + "<desc name=\"" + encode(a.getName())
						+ "\" type=\"" + DescriptionEnum.Concept + "\" concept=\""
						+ encode(d.getConcept().getName()) + "\" mult=\""
						+ d.isMultiple() + "\" >\r\n");
				// save fcts
				for (AmalgamationFct f : d.getConcept()
						.getAvailableAmalgamFcts()) {
					out.write(tab + "  <mc fct=\"" + encode(f.getName())
							+ "\" mt=\"" + d.getMultipleConfig(f).getMainType()
							+ "\" " + "r=\""
							+ d.getMultipleConfig(f).getReuse() + "\" "
							+ "t=\"" + d.getMultipleConfig(f).getType()
							+ "\" />\r\n");
				}
				out.write(tab + "</desc>\r\n");
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Writes the given similarity function's XML representation
	 *  to the given output.
	 *  
	 * @param out the given output
	 * @param f the similarity function to be exported
	 * @param tab tab for indentation
	 */
	private static void saveFct(OutputStreamWriter out, ISimFct f, String tab) {
		try {
			if (f instanceof AdvancedFloatFct) {
				AdvancedFloatFct fct = (AdvancedFloatFct) f;
				out.write(tab + "<fct name=\"" + encode(f.getName())
						+ "\" type=\"" + FunctionEnum.AdvancedFloat
						+ "\" mode=\"" + fct.getDistanceFct() + "\" symm=\""
						+ fct.isSymmetric() + "\" mt=\""
						+ f.getMultipleConfig().getMainType() + "\" " + "r=\""
						+ f.getMultipleConfig().getReuse() + "\" " + "t=\""
						+ f.getMultipleConfig().getType() + "\" maxForQuotient=\"" + fct.getMaxForQuotient()+ "\" >\r\n");
				for (Map.Entry<Double, Similarity> p : fct
						.getAdditionalPoints().entrySet()) {
					out.write(tab + "  <point x=\"" + p.getKey() + "\" y=\""
							+ p.getValue().getRoundedValue() + "\" />\r\n");
				}
				out.write(tab + "</fct>\r\n");
			} else if (f instanceof AdvancedDoubleFct) {
				AdvancedDoubleFct fct = (AdvancedDoubleFct) f;
				out.write(tab + "<fct name=\"" + encode(f.getName())
						+ "\" type=\"" + FunctionEnum.AdvancedDouble
						+ "\" mode=\"" + fct.getDistanceFct() + "\" symm=\""
						+ fct.isSymmetric() + "\" mt=\""
						+ f.getMultipleConfig().getMainType() + "\" " + "r=\""
						+ f.getMultipleConfig().getReuse() + "\" " + "t=\""
						+ f.getMultipleConfig().getType() + "\" maxForQuotient=\"" + fct.getMaxForQuotient()+ "\" >\r\n");
				for (Map.Entry<Double, Similarity> p : fct
						.getAdditionalPoints().entrySet()) {
					out.write(tab + "  <point x=\"" + p.getKey() + "\" y=\""
							+ p.getValue().getRoundedValue() + "\" />\r\n");
				}
				out.write(tab + "</fct>\r\n");
			} else if (f instanceof AdvancedIntegerFct) {
				AdvancedIntegerFct fct = (AdvancedIntegerFct) f;
				out.write(tab + "<fct name=\"" + encode(f.getName())
						+ "\" type=\"" + FunctionEnum.AdvancedInteger
						+ "\" mode=\"" + fct.getDistanceFct() + "\" symm=\""
						+ fct.isSymmetric() + "\" mt=\""
						+ f.getMultipleConfig().getMainType() + "\" " + "r=\""
						+ f.getMultipleConfig().getReuse() + "\" " + "t=\""
						+ f.getMultipleConfig().getType() + "\">\r\n");
				for (Map.Entry<Double, Similarity> p : fct
						.getAdditionalPoints().entrySet()) {
					out.write(tab + "  <point x=\"" + p.getKey() + "\" y=\""
							+ p.getValue().getRoundedValue() + "\" />\r\n");
				}
				out.write(tab + "</fct>\r\n");
			} else if (f instanceof DateFct) {
				out.write(tab + "<fct name=\"" + encode(f.getName())
						+ "\" type=\"" + FunctionEnum.Date + "\" symm=\""
						+ f.isSymmetric() + "\" mt=\""
						+ f.getMultipleConfig().getMainType() + "\" " + "r=\""
						+ f.getMultipleConfig().getReuse() + "\" " + "t=\""
						+ f.getMultipleConfig().getType() + "\" />\r\n");
			} else if (f instanceof FloatFct) {
				FloatFct fct = (FloatFct) f;
				out.write(tab + "<fct name=\"" + encode(f.getName())
						+ "\" type=\"" + FunctionEnum.Float + "\" ltype=\""
						+ fct.getFunctionTypeL() + "\" lparam=\""
						+ fct.getFunctionParameterL() + "\" rtype=\""
						+ fct.getFunctionTypeR() + "\" rparam=\""
						+ fct.getFunctionParameterR() + "\" mode=\""
						+ fct.getDistanceFct() + "\" symm=\"" + fct.isSymmetric()
						+ "\" mt=\"" + f.getMultipleConfig().getMainType()
						+ "\" " + "r=\"" + f.getMultipleConfig().getReuse()
						+ "\" " + "t=\"" + f.getMultipleConfig().getType()
						+ "\" maxForQuotient=\"" + fct.getMaxForQuotient()+ "\" />\r\n");
			}  else if (f instanceof DoubleFct) {
				DoubleFct fct = (DoubleFct) f;
				out.write(tab + "<fct name=\"" + encode(f.getName())
						+ "\" type=\"" + FunctionEnum.Double + "\" ltype=\""
						+ fct.getFunctionTypeL() + "\" lparam=\""
						+ fct.getFunctionParameterL() + "\" rtype=\""
						+ fct.getFunctionTypeR() + "\" rparam=\""
						+ fct.getFunctionParameterR() + "\" mode=\""
						+ fct.getDistanceFct() + "\" symm=\"" + fct.isSymmetric()
						+ "\" mt=\"" + f.getMultipleConfig().getMainType()
						+ "\" " + "r=\"" + f.getMultipleConfig().getReuse()
						+ "\" " + "t=\"" + f.getMultipleConfig().getType()
						+ "\" maxForQuotient=\"" + fct.getMaxForQuotient()+ "\" />\r\n");
			} else if (f instanceof IntegerFct) {
				IntegerFct fct = (IntegerFct) f;
				out.write(tab + "<fct name=\"" + encode(f.getName())
						+ "\" type=\"" + FunctionEnum.Integer + "\" ltype=\""
						+ fct.getFunctionTypeL() + "\" lparam=\""
						+ fct.getFunctionParameterL() + "\" rtype=\""
						+ fct.getFunctionTypeR() + "\" rparam=\""
						+ fct.getFunctionParameterR() + "\" mode=\""
						+ fct.getDistanceFct() + "\" symm=\"" + fct.isSymmetric()
						+ "\" mt=\"" + f.getMultipleConfig().getMainType()
						+ "\" " + "r=\"" + f.getMultipleConfig().getReuse()
						+ "\" " + "t=\"" + f.getMultipleConfig().getType()
						+ "\" />\r\n");
			} else if (f instanceof IntervalFct) {
				out.write(tab + "<fct name=\"" + encode(f.getName())
						+ "\" type=\"" + FunctionEnum.Interval + "\" symm=\""
						+ f.isSymmetric() + "\" mt=\""
						+ f.getMultipleConfig().getMainType() + "\" " + "r=\""
						+ f.getMultipleConfig().getReuse() + "\" " + "t=\""
						+ f.getMultipleConfig().getType() + "\" />\r\n");
			} else if (f instanceof OrderedSymbolFct) {
				OrderedSymbolFct fct = (OrderedSymbolFct) f;
				out.write("<fct name=\"" + encode(f.getName()) + "\" type=\""
						+ FunctionEnum.OrderedSymbol + "\"" + " mt=\""
						+ f.getMultipleConfig().getMainType() + "\" " + "r=\""
						+ f.getMultipleConfig().getReuse() + "\" " + "t=\""
						+ f.getMultipleConfig().getType() + "\" " + "symm=\""
						+ f.isSymmetric() + "\"" + " cyclic=\""
						+ fct.isCyclic() + "\" >\r\n");
				for (SymbolAttribute a : fct.getDesc().getSymbolAttributes()) {
					out.write("<qsym name=\"" + a.getValue() + "\">\r\n");
					for (SymbolAttribute a2 : fct.getDesc()
							.getSymbolAttributes()) {
						if (!a2.equals(a)) {
							Similarity sim = f.calculateSimilarity(a, a2);
							if (sim.getValue() != 0.0) {
								out.write("<csym name=\"" + a2.getValue()
										+ "\" sim=\"" + sim + "\"/>\r\n");
							}
						}
					}
					out.write("</qsym>\r\n");
				}
				IntegerFct intFct = fct.getInternalFunction();
				out.write(tab + "<intfct name=\"" + encode(intFct.getName())
						+ "\" type=\"" + FunctionEnum.Integer + "\" ltype=\""
						+ intFct.getFunctionTypeL() + "\" lparam=\""
						+ intFct.getFunctionParameterL() + "\" rtype=\""
						+ intFct.getFunctionTypeR() + "\" rparam=\""
						+ intFct.getFunctionParameterR() + "\" mode=\""
						+ intFct.getDistanceFct() + "\" symm=\""
						+ intFct.isSymmetric() + "\"  />\r\n");
				for (Map.Entry<SymbolAttribute, Integer> entry : fct.getOrder()
						.entrySet()) {
					out.write(tab + "  <order name=\""
							+ encode(entry.getKey().getValueAsString())
							+ "\" index=\"" + entry.getValue() + "\" />\r\n");
				}
				out.write("</fct>\r\n");
			} else if (f instanceof TaxonomyFct) {

				TaxonomyFct fct = (TaxonomyFct) f;
				saveTaxonomy(out, fct, tab);
			} else if (f instanceof StringFct) {
				StringFct fct = (StringFct) f;
				out.write(tab + "<fct name=\"" + encode(f.getName())
						+ "\" type=\"" + FunctionEnum.String + "\" symm=\""
						+ f.isSymmetric() + "\" config=\"" + fct.getConfig()
						+ "\" n=\"" + fct.getN() + "\" mt=\""
						+ f.getMultipleConfig().getMainType() + "\" " + "r=\""
						+ f.getMultipleConfig().getReuse() + "\" " + "t=\""
						+ f.getMultipleConfig().getType() + "\" />\r\n");
			} else if (f instanceof SymbolFct) {
				SymbolFct fct = (SymbolFct) f;
				out.write("<fct name=\"" + encode(f.getName()) + "\" type=\""
						+ FunctionEnum.Symbol + "\"" + " mt=\""
						+ f.getMultipleConfig().getMainType() + "\" " + "r=\""
						+ f.getMultipleConfig().getReuse() + "\" " + "t=\""
						+ f.getMultipleConfig().getType() + "\" " + "symm=\""
						+ f.isSymmetric() + "\">\r\n");
				for (SymbolAttribute a : fct.getDesc().getSymbolAttributes()) {
					out.write("<qsym name=\"" + a.getValue() + "\">\r\n");
					for (SymbolAttribute a2 : fct.getDesc()
							.getSymbolAttributes()) {
						Similarity sim = f.calculateSimilarity(a, a2);
						if (!a2.equals(a)) {
							if (sim.getValue() != 0.0) {
								out.write("<csym name=\"" + a2.getValue()
										+ "\" sim=\"" + sim + "\"/>\r\n");
							}
						} else if (sim.getRoundedValue() != 1.0) {
							out.write("<csym name=\"" + a2.getValue()
									+ "\" sim=\"" + sim + "\"/>\r\n");
						}
					}
					out.write("</qsym>\r\n");
				}
				out.write("</fct>\r\n");
			}
		} catch (IOException e) {
			e.printStackTrace();
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	/**
	 * Writes the XML representation of the given taxonomy function to the given output. 
	 * @param out the given output
	 * @param fct the function to be exported
	 * @param tab tab for indentation
	 */
	private static void saveTaxonomy(OutputStreamWriter out, TaxonomyFct fct,
			String tab) {
		try {
			out.write(tab
					+ "<fct name=\""
					+ encode(fct.getName())
					+ "\" type=\""
					+ FunctionEnum.Taxonomy
					+ "\" mt=\""
						+ fct.getMultipleConfig().getMainType() + "\" " + "r=\""
						+ fct.getMultipleConfig().getReuse() + "\" " + "t=\""
						+ fct.getMultipleConfig().getType() + "\" qconfig=\""
					+ fct.getQueryConfig()
					+ "\" cconfig=\""
					+ fct.getCaseConfig()
					+ "\" top=\""
					+ fct.getTopSymbol()
					+ "\" sim=\""
					+ fct.getSimilarity(
							fct.getTopSymbol()) + "\" symm=\""
					+ fct.isSymmetric() + "\" >\r\n");

			for (Map.Entry<TaxonomyNode, TaxonomyNode> entry : fct
					.entrySet()) {
				out.write(tab + "  <node name=\""
						+ encode(entry.getKey().toString()) + "\" sim=\""
						+ fct.getSimilarity(entry.getKey())
						+ "\" parent=\"" + entry.getValue() + "\" />\r\n");
			}
			out.write(tab + "</fct>\r\n");
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Encodes the given string as XML valid
	 * @param in the string to be encoded
	 * @return the encoded string
	 */
	private static String encode(String in) {
	    if (in == null) {
	        return null;
	    }
	    boolean anyCharactersProtected = false;

	    StringBuffer stringBuffer = new StringBuffer();
	    for (int i = 0; i < in.length(); i++) {
	        char ch = in.charAt(i);

	        boolean controlCharacter = ch < 32;
	        boolean unicodeButNotAscii = ch > 126;
	        boolean characterWithSpecialMeaningInXML = ch == '<' || ch == '&' || ch == '>' || ch == '"';

	        if (characterWithSpecialMeaningInXML || unicodeButNotAscii || controlCharacter) {
	            stringBuffer.append("&#" + (int) ch + ";");
	            anyCharactersProtected = true;
	        } else {
	            stringBuffer.append(ch);
	        }
	    }
	    if (anyCharactersProtected == false) {
	        return in;
	    }

	    return stringBuffer.toString();
	}
}
