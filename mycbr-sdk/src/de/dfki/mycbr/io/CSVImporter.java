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

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import de.dfki.mycbr.core.ICaseBase;
import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Attribute;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.casebase.MultipleAttribute;
import de.dfki.mycbr.core.model.AttributeDesc;
import de.dfki.mycbr.core.model.BooleanDesc;
import de.dfki.mycbr.core.model.Concept;
import de.dfki.mycbr.core.model.FloatDesc;
import de.dfki.mycbr.core.model.IntegerDesc;
import de.dfki.mycbr.core.model.StringDesc;
import de.dfki.mycbr.core.model.SymbolDesc;
import de.dfki.mycbr.util.Pair;

/**
 * Imports instances from CSV files.
 * Please call the following methods in the given order:
 * csvImporter.readData();
 * csvImporter.checkData();
 * csvImporter.addMissingValues();
 * csvImporter.addMissingDescriptions();
 * csvImporter.doImport();
 * 
 * @author myCBR Team
 * 
 */
public class CSVImporter {

	private CSVImportError error;
	private String separator = ";";
	private String separatorMultiple = ",";
	private int explanation = -1;
	private static final String EXP_LABEL = "[@expl]";
	public static final String CB_CSV_IMPORT = "CB_csvImport";
	
	private boolean createMissingDescs = true; // will skip columns that are
												// unknown if set to false
	private boolean changeDescOnMissingValues = true; // will set values to
														// special value if they
														// do not fit!
	private String specialAttForMissingValues = Project.UNDEFINED_SPECIAL_ATTRIBUTE;
	private int symbolThreshold = 30;

	private HashMap<Integer, ICaseBase.Meta> col2Meta = new HashMap<Integer, ICaseBase.Meta>();
	
	private ArrayList<String[]> entries = new ArrayList<String[]>();
	private HashSet<String> attributeDescsName = new HashSet<String>();
	private HashMap<AttributeDesc, Integer> attributeDescs = new HashMap<AttributeDesc, Integer>();
	
	private HashMap<Integer, AttributeDesc> indices = new HashMap<Integer,AttributeDesc>();
	
	private Vector<Pair<String, Integer>> descsToBeCreated = new Vector<Pair<String, Integer>>();
	private Vector<Integer> indicesToBeSkipped = new Vector<Integer>();
	
	private Concept concept;
	private File file;

	private HashSet<Pair<String, AttributeDesc>> invalidValues = new HashSet<Pair<String, AttributeDesc>>();

	private String[] header;
	int counter;
	String pattern = ""; // TODO
	ICaseBase cb; // TODO
	Thread t;
	private int totalCaseCount = 0;
	private int currentCaseCount = 0;
	
	public CSVImporter(String f, Concept concept) {
		this.file = new File(f);
		if (!this.file.exists()) {
			error = CSVImportError.FileNotFound;
			error.setProblem(file.getAbsolutePath());
			System.err.println("File " +f + " not found!");
		}
		this.concept = concept;
	}

	public void reset() {
		error = null;
		col2Meta.clear();
		attributeDescs.clear();
		attributeDescsName.clear();
		indices.clear();
		descsToBeCreated.clear();
		indicesToBeSkipped.clear();
		invalidValues.clear();
		counter = 0;
		entries.clear();
		totalCaseCount = 0;
		currentCaseCount = 0;
		explanation = -1;
	}
	
	public void readData() {
		reset();
		// we parse by line
		BufferedReader br;
		try {
			br = new BufferedReader(new FileReader(file));

			// first read table header
			String line = br.readLine();
			header = line.split(separator);
			
			line = br.readLine();				
			while (line != null) {
				if (!line.isEmpty()) {
					entries.add(line.split(separator, -1));
					totalCaseCount++;
				}
				line = br.readLine();
			}
			br.close();

		} catch (FileNotFoundException e) {
			System.out.println("file not found!");
			error = CSVImportError.FileNotFound;
			error.setProblem(file.getAbsolutePath());
		} catch (IOException e) {
			System.out.println("parse error!");
			error = CSVImportError.ParsingError;
			error.setProblem(file.getAbsolutePath());
		}
	}

	
	/**
	 * Adds a mapping entry for meta tag of a case
	 * to the given column in the underlying data set.
	 * 
	 * When importing the current data, the columns
	 * that are mapped to, will be used as the associated
	 * meta data in the case object
	 * 
	 * Will override data, if more than one column is mapped
	 * to the same meta tag. Is assumed to be called after {@link #readData()}
	 * and before {@link #checkData()}
	 * 
	 * @param col the column which holds the meta information
	 * @param meta the meta type
	 */
	public void setMeta(Integer col, ICaseBase.Meta meta) {
		col2Meta.put(col, meta);
	}
	
	/**
	 * Removes the meta tag of the given column.
	 * 
	 * Is assumed to be called after {@link #readData()}
	 * and before {@link #checkData()}
	 * 
	 * @param col the column that should not refer to meta
	 *  information afterwards
	 */
	public void removeMeta(int col) {
		col2Meta.remove(col);
	}
	
	/**
	 * after calling this, all values in the table fit the description
	 */
	public void addMissingValues() {
		
		if (error == CSVImportError.ValueMissing) {
			error = null;

			for (Pair<String, AttributeDesc> entry : invalidValues) {
				AttributeDesc d = entry.getSecond();
				String value = entry.getFirst();

				if (!changeDescOnMissingValues) {
					// set value undefined in given table
					System.err.println("The following value is not defined: " + value);
					setValueUndefined(value, d);
					break;
				}

				if (d instanceof IntegerDesc) {
					Integer v;
					try {
						v = Integer.parseInt(value);
					} catch (NumberFormatException e) {
						setValueUndefined(value, d);
						continue;
					}
					IntegerDesc intDesc = (IntegerDesc) d;
					if (intDesc.getMin() > v) {
						intDesc.setMin(v);
					} else if (intDesc.getMax() < v) {
						intDesc.setMax(v);
					}

				} else if (d instanceof FloatDesc) {
					Float v;
					try {
						v = Float.parseFloat(value);
					} catch (NumberFormatException e) {
						setValueUndefined(value, d);
						continue;
					}
					FloatDesc floatDesc = (FloatDesc) d;
					if (floatDesc.getMin() > v) {
						floatDesc.setMin(v);
					} else if (floatDesc.getMax() < v) {
						floatDesc.setMax(v);
					}

				} else if (d instanceof BooleanDesc) { // includes BooleanDesc
					// value is neither true nor false!
					setValueUndefined(value, d);

				} else if (d instanceof SymbolDesc) { // includes BooleanDesc

					SymbolDesc symbolDesc = (SymbolDesc) d;
					symbolDesc.addSymbol(value);
				}

			}
		}
	}

	/**
	 * @param value
	 * @param d
	 */
	private void setValueUndefined(String value, AttributeDesc d) {
		int column = attributeDescs.get(d);
	
		if (!d.isMultiple()) {
			for (int i=0; i<entries.size(); i++) {	
				if (entries.get(i)[column].equals(value)) {
					entries.get(i)[column] = specialAttForMissingValues;
				}
			}
		} else {
			// if multiple attribute contains not allowed value
			// use special value for the whole set
			for (int i=0; i<entries.size(); i++) {	
				if (entries.get(i)[column].indexOf(value)!=-1) {
					entries.get(i)[column] = specialAttForMissingValues;
				}
			}
		}
	}

	/**
	 * Checks for consistent data.
	 * 
	 * Assumes that all meta data tags have been added first
	 */
	public void checkData() {
		if (error == null) {
			// header contains duplicates?
			for (int i = 0; i < header.length; i++) {
				if (col2Meta.containsKey(i)) {
					continue;
				} else if (header[i].equals(EXP_LABEL)) {
					explanation = i;
					continue;
				}
				if (!attributeDescsName.add(header[i])) {
					error = CSVImportError.DuplicateDescName;
					error.setProblem(header[i]);
					return;
				}
				AttributeDesc desc = concept.getAttributeDesc(header[i]);
				if (desc == null) {
					// desc has to be created later on, when we know its
					// type
					Pair<String, Integer> toBeAdded = new Pair<String, Integer>(
							header[i], i);
					descsToBeCreated.add(toBeAdded);
					if (!createMissingDescs) {
						indicesToBeSkipped.add(i);
					}
				} else {
					attributeDescs.put(desc, i);
					indices.put(i, desc);
				}
			}
			
			for (Map.Entry<AttributeDesc,Integer> entry : attributeDescs
					.entrySet()) {
				AttributeDesc d = entry.getKey();
				Integer column = entry.getValue();

				
				if (d instanceof IntegerDesc) {
					// check if all values are integer or special values
					// and if they fit the range
					for (String[] row : entries) {
						
						if (concept.getProject()
								.isSpecialAttribute(row[column])) {
							continue;
						}
						Integer v;
						String[] currentValue = row[column].split(separatorMultiple);
						if (currentValue.length>1) {
							d.setMultiple(true);
						}
						for (String s: currentValue) {
							try {
								v = Integer.parseInt(s);
							} catch (NumberFormatException e) {
								invalidValues.add(new Pair<String, AttributeDesc>(
										s, d));
								continue;
							}
							IntegerDesc intDesc = (IntegerDesc) d;
							if (intDesc.getMin() > v || intDesc.getMax() < v) {
								invalidValues.add(new Pair<String, AttributeDesc>(
										s, d));
							}
						}
					}
				} else if (d instanceof FloatDesc) {
					// check if all values are float or special values
					// and if they fit the range
					for (String[] row : entries) {

						if (concept.getProject()
								.isSpecialAttribute(row[column])) {
							continue;
						}
						Float v;
						String[] currentValue = row[column].split(separatorMultiple);
						if (currentValue.length>1) {
							d.setMultiple(true);
						}
						for (String s: currentValue) {
							try {
								v = Float.parseFloat(s);
							} catch (NumberFormatException e) {
								invalidValues.add(new Pair<String, AttributeDesc>(
										s, d));
								continue;
							}
							FloatDesc floatDesc = (FloatDesc) d;
							if (floatDesc.getMin() > v || floatDesc.getMax() < v) {
								invalidValues.add(new Pair<String, AttributeDesc>(
										s, d));
							}
						}
					}
				} else if (d instanceof SymbolDesc) { // includes BooleanDesc
					// check if all values occurring are either allowed values
					// or special values
					for (String[] row : entries) {

						if (concept.getProject()
								.isSpecialAttribute(row[column])) {
							continue;
						}
						SymbolDesc symbolDesc = (SymbolDesc) d;
						String[] currentValue = row[column].split(separatorMultiple);
						if (currentValue.length>1) {
							d.setMultiple(true);
						}
						for (String s: currentValue) {
							if (!symbolDesc.isAllowedValue(s)) {
								invalidValues.add(new Pair<String, AttributeDesc>(
										s, d));
							}
						}
					}
				}

			}
			if (invalidValues.size() != 0) {
				error = CSVImportError.ValueMissing;
			}
		}
	}

	public void addMissingDescriptions() {
		// add missing descriptions
		for (Pair<String, Integer> toBeAdded : descsToBeCreated) {
			String name = toBeAdded.getFirst();
			Integer column = toBeAdded.getSecond();

			// boolean isString = true; // string is always possible
			// boolean isSymbol = true; // symbol is always possible -> check
			// for
			boolean isBoolean = true;
			boolean isInteger = true;
			boolean isFloat = true;
			boolean isMultiple = false;
			// boolean isDate = true; TODO
			// boolean isInterval = true; TODO

			Number min = Integer.MAX_VALUE;
			Number max = Integer.MIN_VALUE;
			HashSet<String> values = new HashSet<String>(); // to check how many
															// values there are
			for (String[] row : entries) {

				if (concept.getProject().isSpecialAttribute(row[column])) {
					continue;
				}

				String[] currentValue = row[column].split(separatorMultiple);
				if (currentValue.length>1) {
					isMultiple = true;
				}
				
				for (String s: currentValue) {
					values.add(s);
	
					if (isBoolean && !s.equals("true")
							&& !s.equals("false")) {
						isBoolean = false;
					}
	
					if (isInteger) {
						try {
							Integer v = Integer.parseInt(s);
							if (v > max.intValue()) {
								max = v;
							}
							if (v < min.intValue()) {
								min = v;
							}
						} catch (NumberFormatException e) {
							isInteger = false;
						}
					}
	
					if (isFloat) {
						try {
							Float v = Float.parseFloat(s);
							if (v > max.floatValue()) {
								max = v;
							}
							if (v < min.floatValue()) {
								min = v;
							}
						} catch (NumberFormatException e) {
							isFloat = false;
						}
					}
				}
			}
			if (isInteger) {
				IntegerDesc d;
				try {
					d = new IntegerDesc(concept, name, min.intValue(),
							max.intValue());
					attributeDescs.put(d,column);
					indices.put(column, d);
					if (isMultiple) {
						d.setMultiple(isMultiple);
					}
				} catch (Exception e) {
					System.err
							.println("Error during csv import: Could not create attribute description: "
									+ name);
				}
			} else if (isFloat) {
				FloatDesc d;
				try {
					d = new FloatDesc(concept, name, min.floatValue(),
							max.floatValue());
					attributeDescs.put(d,column);
					indices.put(column, d);
					if (isMultiple) {
						d.setMultiple(isMultiple);
					}
				} catch (Exception e) {
					System.err
							.println("Error during csv import: Could not create attribute description: "
									+ name);
				}
			} else if (isBoolean) {
				SymbolDesc d;
				try {
					d = new BooleanDesc(concept, name);
					attributeDescs.put(d,column);
					indices.put(column, d);
					if (isMultiple) {
						d.setMultiple(isMultiple);
					}
				} catch (Exception e) {
					System.err
							.println("Error during csv import: Could not create attribute description: "
									+ name);
				}
			} else if (values.size() > symbolThreshold) {
				StringDesc d;
				try {
					d = new StringDesc(concept, name);
					attributeDescs.put(d,column);
					indices.put(column, d);
					if (isMultiple) {
						d.setMultiple(isMultiple);
					}
				} catch (Exception e) {
					System.err
							.println("Error during csv import: Could not create attribute description: "
									+ name);
				}
			} else {
				SymbolDesc d;
				try {
					d = new SymbolDesc(concept, name, values);
					attributeDescs.put(d,column);
					indices.put(column, d);
					if (isMultiple) {
						d.setMultiple(isMultiple);
					}
				} catch (Exception e) {
					System.err
							.println("Error during csv import: Could not create attribute description: "
									+ name);
				}
			}
			
		}
	}

	public void setCaseBase(ICaseBase cb) {
		this.cb = cb;
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see de.dfki.mycbr.io.IImporter#doImport()
	 */
	public void doImport() {
		t = new Thread(new CSVThread());
		t.start();
	}

	/**
	 * @return the error
	 */
	public CSVImportError getError() {
		return error;
	}

	/**
	 * @param separator
	 *            the separator to set
	 */
	public void setSeparator(String separator) {
		this.separator = separator;
	}

	/**
	 * @return the separator
	 */
	public String getSeparator() {
		return separator;
	}

	/**
	 * @return the separatorMultiple
	 */
	public String getSeparatorMultiple() {
		return separatorMultiple;
	}

	/**
	 * @param separatorMultiple the separatorMultiple to set
	 */
	public void setSeparatorMultiple(String separatorMultiple) {
		this.separatorMultiple = separatorMultiple;
	}
	
	/**
	 * @param createMissingDescs
	 *            the createMissingSlots to set
	 */
	public void setCreateMissingDesc(boolean createMissingDescs) {
		this.createMissingDescs = createMissingDescs;
	}

	/**
	 * @return the createMissingSlots
	 */
	public boolean isCreateMissingDescs() {
		return createMissingDescs;
	}

	/**
	 * @param changeDescOnMissingValues
	 *            the changeDescOnMissingValues to set
	 */
	public void setChangeDescOnMissingValues(boolean changeDescOnMissingValues) {
		this.changeDescOnMissingValues = changeDescOnMissingValues;
	}

	/**
	 * @return the changeDescOnMissingValues
	 */
	public boolean isChangeDescOnMissingValues() {
		return changeDescOnMissingValues;
	}

	/**
	 * @param specialAttForMissingValues
	 *            the specialAttForMissingValues to set
	 */
	public void setSpecialAttForMissingValues(String specialAttForMissingValues) {
		this.specialAttForMissingValues = specialAttForMissingValues;
	}

	/**
	 * @return the specialAttForMissingValues
	 */
	public String getSpecialAttForMissingValues() {
		return specialAttForMissingValues;
	}

	public Vector<Integer> getColumnsToBeSkipped() {
		return indicesToBeSkipped;
	}
	
	/**
	 * @return the entries
	 */
	public ArrayList<String[]> getData() {
		return entries;
	}

	/**
	 * @return the attributeDescsName
	 */
	public HashSet<String> getAttributeDescsName() {
		return attributeDescsName;
	}

	/**
	 * @return the symbolThreshold
	 */
	public int getSymbolThreshold() {
		return symbolThreshold;
	}

	/**
	 * @param symbolThreshold
	 *            the symbolThreshold to set
	 */
	public void setSymbolThreshold(int symbolThreshold) {
		this.symbolThreshold = symbolThreshold;
	}

	public Vector<Pair<String, Integer>> getMissingDescs() {
		return descsToBeCreated;
	}

	public HashSet<Pair<String, AttributeDesc>> getInvalidValues() {
		return invalidValues;
	}

	public HashMap<AttributeDesc, Integer> getIndexToDescMap() {
		return attributeDescs;
	}

    public int getTotalNumberOfCases() {
        return totalCaseCount;
    }
    
    /**
    *
    * @return true, when still importing. false otherwise
    */
   public boolean isImporting() {
       return t.isAlive();
   }

   /**
    *
    * @return the number of cases currently imported
    */
   public int getCurrentNumberOfCases() {
       return currentCaseCount;
   }
   
	private class CSVThread implements Runnable {
		public void run() {
			if (error == null) {
				try {
					if (cb == null && !concept.getProject().hasCB("CB_csvImport")) {
						cb = concept.getProject().createDefaultCB("CB_csvImport");	
					} else if (cb == null) {
						cb = concept.getProject().getCB(CB_CSV_IMPORT);
					}
					
				} catch (Exception e) {
					e.printStackTrace();
					return;
				}
				int colToBeSkipped = 0;
				
				for (String[] row: entries) {
					Instance i;
					try {
						counter = concept.getDirectInstances().size();
						String id = concept.getName() + Integer.toString(counter++);
						while(concept.getInstance(id)!=null) {
							id = concept.getName() + Integer.toString(counter++);
						}
						i = new Instance(concept, id);
						//i = concept.addInstance(concept.getName() + Integer.toString(counter++));
						
						for (int j = 0; j<row.length; j++) {
							if (colToBeSkipped < indicesToBeSkipped.size() && indicesToBeSkipped.get(colToBeSkipped).equals(j)) {
								colToBeSkipped++;
								continue;
							}
							if (!col2Meta.containsKey(j) && j!=explanation) {
								AttributeDesc desc = indices.get(j);
								if (desc.isMultiple() && !concept.getProject().isSpecialAttribute(row[j])) {
									String[] values = row[j].split(separatorMultiple);
									LinkedList<Attribute> list = new LinkedList<Attribute>();
									for (String s: values) {
										list.add(desc.getAttribute(s));
									}
									MultipleAttribute<AttributeDesc> multipleAtt = new MultipleAttribute<AttributeDesc>(desc, list);
									i.addAttribute(desc, multipleAtt);
								} else {
									i.addAttribute(indices.get(j), row[j]);
								}
							} else if (j==explanation) {
								concept.getProject().getExplanationManager().explain(i, row[j]);
							}
						}
						cb.addCase(i);
						currentCaseCount++;
						
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
				
			}
		}
	}
	
	/**
	 * Returns the header of the CSV file as a string array.
	 * Contains the names of the attribute descriptions
	 * @return array of names of attribute descriptions in order of their appearance 
	 */
	public String[] getHeader() {
		return header;
	}
	
	public enum CSVImportError {
		FileNotFound("File not found!"), ValueMissing("Values is missing!"), DuplicateDescName(
				"Found a duplicate attribute description name!"), ParsingError(
				"Problem parsing file!");

		private String desc;
		private String problem;

		CSVImportError(String desc) {
			this.desc = desc;
		}

		public String getProblem() {
			return problem;
		}

		public void setProblem(String p) {
			problem = p;
		}

		public String getDesc() {
			return desc;
		}
	}
}
