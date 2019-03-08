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
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Vector;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;

import org.apache.log4j.Logger;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;

import de.dfki.mycbr.core.ICaseBase;
import de.dfki.mycbr.core.Project;
import de.dfki.mycbr.core.casebase.Attribute;
import de.dfki.mycbr.core.casebase.Instance;
import de.dfki.mycbr.core.casebase.MultipleAttribute;
import de.dfki.mycbr.core.model.AttributeDesc;
import de.dfki.mycbr.core.model.Concept;
import de.dfki.mycbr.core.model.DoubleDesc;
import de.dfki.mycbr.core.model.FloatDesc;
import de.dfki.mycbr.core.model.IntegerDesc;
import de.dfki.mycbr.core.model.SymbolDesc;
import de.dfki.mycbr.util.Database;
import de.dfki.mycbr.util.Mapping;
import de.dfki.mycbr.util.Pair;

/**
 * class to import a casebase from a postgres database
 * @author myCBR Team, Pascal Reuss
 *
 */
public class DataBaseImporter {


	private HashMap<Integer, Database> dbases;
	private Database db;
	
	private Concept concept;
	private Project project;
	
	private String seperatorMultiple = ",";
	
	private Connection con;
	private Statement pstmt;
	private ResultSet result;
	private String tablename;
	private String dbmapping;
	
	private int totalCaseCount;
	private int currentCaseCount;
	
	private int counter;
	
	private DataBaseImporterError error;
	
	private ICaseBase cb;
	
	private ArrayList<Mapping> AttributeDescName = new ArrayList<Mapping>();
	private HashMap<AttributeDesc, String> AttributesDescs = new HashMap<AttributeDesc, String>();
	private HashMap<String, AttributeDesc> Indicies = new HashMap<String, AttributeDesc>();
	private Vector<String> columnsToSkip = new Vector<String>();
	
	private HashSet<Pair<String, AttributeDesc>> invalidValues = new HashSet<Pair<String, AttributeDesc>>();
	
	private static Logger logger = Logger.getLogger(DataBaseImporter.class);
	
	Thread t;
	
	/**
	 * Constructor for DataBaseImporter
	 * @param id of the database to use for the CaseBase 
	 * @param project used for the CaseBase
	 * @param concept used for the CaseBase
	 */
	public DataBaseImporter(int id, Project project, Concept concept, String dbmapping) {
		this.dbases = new HashMap<Integer, Database>();
		this.concept = concept;
		this.project = project;
		this.dbmapping = dbmapping;
		
		readMetaDataFromXML();		
		connectToDatabase(id);
	}	
	
	/**
	 * Establish a connection to a given postgreSQL Database
	 * @return true if successful, false if connection cannot be established
	 */
	public boolean connectToDatabase(int id) {
		
		db = dbases.get(id);
		
		boolean success = false;
		
		try {
			Class.forName(db.getDriver());
			
			this.con = DriverManager.getConnection(db.getURL(), db.getUser(), db.getPwd());
			
			this.pstmt = this.con.createStatement(ResultSet.TYPE_SCROLL_SENSITIVE, 
					ResultSet.CONCUR_READ_ONLY);
	
			success = true;
			
		} catch (ClassNotFoundException ce) {
			error = DataBaseImporterError.DataBaseConnect;
			logger.error(ce.getLocalizedMessage());
			//			System.out.println(ce.getLocalizedMessage());
		} catch (SQLException se) {
			error = DataBaseImporterError.DataBaseConnect;
			System.out.println(se.getLocalizedMessage());
		}
		
		return success;
	}
	
	
	
	/**
	 * Reads the Data from the table of a given database
	 * @param table the name of the table to read the data from
	 * @param filtercolumns an array with the names of the columns for a where clause
	 * @param filtervalues an array with the values of the columns for a where clause
	 * @return true if data can be imported, false if not
	 */
	public boolean readData(String table, String[] filtercolumns, String[] filtervalues) {
		
		boolean success = false;
		// Use Conceptname as table name
		if (table == null) {
			this.tablename = this.concept.getName();
		} else {
			this.tablename = table;
		}
		
		String SQL = "SELECT * FROM " + this.tablename + " WHERE TRUE";
		
		
		if (filtercolumns != null && filtervalues != null) {
			for (int i = 0; i < filtercolumns.length; i++) {
				SQL += " AND " + filtercolumns[i] + " = '" + filtervalues[i] + "'";
			}
		}

		try {
			
			// Gets the resultset
			result = this.pstmt.executeQuery(SQL);
			//counting the rows
			while (result.next()) {
				totalCaseCount++;
			}

			result.beforeFirst();
			
		} catch (SQLException se) {
			error = DataBaseImporterError.DataBaseConnect;
			System.out.println("ERROR");
			se.printStackTrace();
		}
		
		return success;
	}
	
	/**
	 * Method to get the columnNames of the table from which the data is read
	 */
	public void readMetaDataFromDatabase(String table) {
		
		if (table == null) {
			this.tablename = this.concept.getName();
		} else {
			this.tablename = table;
		}
		
		String SQL = "SELECT column_name FROM information_schema.COLUMNS WHERE table_name = " + this.tablename;
		
		try {
			ResultSet metaresult = pstmt.executeQuery(SQL);
			metaresult.beforeFirst();
			
			while (metaresult.next()) {
				String column = result.getString("column_name");
				Mapping mapping = new Mapping(column, column);
				AttributeDescName.add(mapping);
			}
			
		} catch (SQLException se) {
			error = DataBaseImporterError.DataBaseConnect;
			System.out.println(se.getLocalizedMessage());
		}
	}
	
	/**
	 * Method to get the mapping for columns and attributes from an xml file
	 * 
	 */
	public void readMetaDataFromXML() {
		System.out.println("Reading XML");
		try {
			File dmap = new File(dbmapping);
			
			DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
			DocumentBuilder builder = factory.newDocumentBuilder();
			Document doc = builder.parse(dmap);
			
			doc.getDocumentElement().normalize();
			//Getting database nodes
			NodeList databases = doc.getElementsByTagName("database");
			
			//Running through all databases
			for(int i = 0; i < databases.getLength(); i++) {
				Database db = new Database();
				HashMap<String, HashMap<String, Object>> mappings = new HashMap<String, HashMap<String, Object>>();
				
				Node database = databases.item(i);
				
				NodeList nodes = database.getChildNodes();
				String name = "";
				String concept = "";
				
				for (int j = 0; j < nodes.getLength(); j++) {
				
					Node column = nodes.item(j);
					
					if (column.getNodeType() == Node.ELEMENT_NODE) {
						
						//Getting the id
						if (column.getNodeName().equals("id")) {
							Element id = (Element) column;
							
							NodeList idElementList = id.getChildNodes();
							
							db.setId(Integer.parseInt(idElementList.item(0).getNodeValue()));
						//Getting connection parameters
						} else if (column.getNodeName().equals("connection")) {
							Element connElement = (Element) column;
							
							NodeList hostElementList = connElement.getElementsByTagName("host");
							Element host = (Element) hostElementList.item(0);
							NodeList hostElements = host.getChildNodes();
							db.setHost(hostElements.item(0).getNodeValue().trim());
							
							NodeList portElementList = connElement.getElementsByTagName("port");
							Element port = (Element) portElementList.item(0);
							NodeList portElements = port.getChildNodes();
							db.setPort(portElements.item(0).getNodeValue().trim());
							
							NodeList userElementList = connElement.getElementsByTagName("user");
							Element user = (Element) userElementList.item(0);
							NodeList userElements = user.getChildNodes();
							db.setUser(userElements.item(0).getNodeValue().trim());
							
							NodeList pwdElementList = connElement.getElementsByTagName("pass");
							Element pwd = (Element) pwdElementList.item(0);
							NodeList pwdElements = pwd.getChildNodes();
							db.setPwd(pwdElements.item(0).getNodeValue().trim());
							
							NodeList dbnameElementList = connElement.getElementsByTagName("dbname");
							Element dbname = (Element) dbnameElementList.item(0);
							NodeList dbnameElements = dbname.getChildNodes();
							db.setDbname(dbnameElements.item(0).getNodeValue().trim());
							
							NodeList driverElementList = connElement.getElementsByTagName("driver");
							Element driver = (Element) driverElementList.item(0);
							NodeList driverElements = driver.getChildNodes();
							db.setDriver(driverElements.item(0).getNodeValue().trim());
							
							NodeList dbtypeElementList = connElement.getElementsByTagName("dbtype");
							Element dbtype = (Element) dbtypeElementList.item(0);
							NodeList dbtypeElements = dbtype.getChildNodes();
							db.setDbtype(dbtypeElements.item(0).getNodeValue().trim());
	
						
						//Getting table mappings
						} else if (column.getNodeName().equals("table")) {
							HashMap<String, Object> tabledata = new HashMap<String, Object>();
							ArrayList<Mapping> map = new ArrayList<Mapping>();
							NodeList tableinf = column.getChildNodes();
							
							for (int l = 0; l < tableinf.getLength(); l++) {
								
								Node node = tableinf.item(l);
								if (node.getNodeType() == Node.ELEMENT_NODE) {
									
									if (node.getNodeName().equals("name")) {
										
										Element nameElement = (Element) node;
										
										NodeList nameElementList = nameElement.getChildNodes();
										
										name = nameElementList.item(0).getNodeValue().trim();
										
									} else if (node.getNodeName().equals("concept")) {
										
										Element conceptElement = (Element) node;
										
										NodeList conceptElementList = conceptElement.getChildNodes();
										
										concept = conceptElementList.item(0).getNodeValue().trim();
									
									} else if (node.getNodeName().equals("column")) {
										
										Element columnElement = (Element) node;
												
										NodeList sourcelist = columnElement.getElementsByTagName("source");
										NodeList targetlist = columnElement.getElementsByTagName("target");
												
										Element source = (Element) sourcelist.item(0);
										Element target = (Element) targetlist.item(0);
												
										NodeList sourceElementList = source.getChildNodes();
										NodeList targetElementList = target.getChildNodes();
												
										Mapping mapping = new Mapping(sourceElementList.item(0).getNodeValue().trim(),
												targetElementList.item(0).getNodeValue().trim());
												
										map.add(mapping);
									}	
								}			
							}
							tabledata.put("concept", concept);
							tabledata.put("mapping", map);
							mappings.put(name, tabledata);
						}
					}
				}
				db.setMappings(mappings);
				this.dbases.put(db.getId(), db);
			}	

		} catch (SAXParseException err) {
	        System.out.println ("** Parsing error" + ", line " 
	             + err.getLineNumber () + ", uri " + err.getSystemId ());
	        System.out.println(" " + err.getMessage ());	
			
		} catch (ParserConfigurationException pce) {
			pce.printStackTrace();
		} catch (IOException ie) {
			ie.printStackTrace();
		} catch (SAXException se) {
			se.printStackTrace();
		}
		
	}
	
	/**
	 * method to add values from the table to an attributes allowed values
	 * Only for used 
	 */
	public void addMissingValues() {
		//If missing values found
		if (error == DataBaseImporterError.ValueMissing) {
			error = null;
			
			for (Pair<String, AttributeDesc> entry : invalidValues) {
				
				AttributeDesc desc = entry.getSecond();
				String value = entry.getFirst();
				
				if (desc instanceof IntegerDesc) {
					Integer v;
					try {
						v = Integer.parseInt(value);
					} catch (NumberFormatException ne) {
						//ToDo
						continue;
					}
					
					IntegerDesc intDesc = (IntegerDesc) desc;
					if (intDesc.getMin() > v) {
						intDesc.setMin(v);
					} else if (intDesc.getMax() < v) {
						intDesc.setMax(v);
					}
					
				} else if (desc instanceof FloatDesc) {
					Float v;
					try {
						v = Float.parseFloat(value);
					} catch (NumberFormatException ne) {
						//ToDo
						continue;
					}
					
					FloatDesc floatdesc = (FloatDesc) desc;
					if (floatdesc.getMin() > v) {
						floatdesc.setMin(v);
					} else if (floatdesc.getMax() < v) {
						floatdesc.setMax(v);
					}
					
				} else if (desc instanceof SymbolDesc) {
					SymbolDesc symdesc = (SymbolDesc) desc;
					symdesc.addSymbol(value);
				}
			}
			
			
		}
	}
	
	/**
	 * method to check if the read data is valid
	 */
	public void checkData() {
		
		for (Mapping map : db.getMappings(tablename)) {
			
			AttributeDesc desc = concept.getAttributeDesc(map.getTarget());
			HashMap<String, Concept> con = concept.getAllSubConcepts();
			for (String s : con.keySet()) {
				Concept c = project.getConceptByID(s);
				desc = c.getAttributeDesc(map.getTarget());
			}
			//TODO Rekursiver Aufruf

			//Check if the columns from the database are part of the case structure
			if (desc == null) {
				//if not part of the structure ingnore
				columnsToSkip.add(map.getSource());
			} else {
				//if part, add to list
				AttributesDescs.put(desc, map.getSource());
				Indicies.put(map.getTarget(), desc);
			}
			
			//Checking what kind of Attribute (Integer, Float, Double, Symbol)
			for (Map.Entry<AttributeDesc, String> entry : AttributesDescs.entrySet()) {
				AttributeDesc d = entry.getKey();
				String col = entry.getValue();
				//Checking if values of IntegerDesc are valid and in range
				if (d instanceof IntegerDesc) {
					
					try {
						while (result.next()) {
							
							Integer v = null;
							int colindex = result.findColumn(col);
							//If multiple values, database colum had to be of type String
							if (result.getMetaData().getColumnClassName(colindex).equals("java.lang.String")) {
								String[] values = result.getString(col).split(seperatorMultiple);
								if (values.length > 1) {
									d.setMultiple(true);
								}
								
								for (String s : values) {
									try {
										v = Integer.parseInt(s.trim());
									} catch (NumberFormatException ne) {
										invalidValues.add(new Pair<String, AttributeDesc> (s.trim(), d));
										continue;
									}
								}
								
								IntegerDesc intDesc = (IntegerDesc) d;
								if (intDesc.getMin() > v || intDesc.getMax() < v) {
									invalidValues.add(new Pair<String, AttributeDesc> (Integer.toString(v),d));
								}
								
							//if only one value it could be an integer column
							} else if (result.getMetaData().getColumnClassName(colindex).equals("java.lang.Integer")) {
								v = result.getInt(col);
								
								IntegerDesc intDesc = (IntegerDesc) d;
								if (intDesc.getMin() > v || intDesc.getMax() < v) {
									invalidValues.add(new Pair<String, AttributeDesc> (Integer.toString(v),d));
								}
							}	
							
						}
						result.beforeFirst();
					} catch (SQLException se) {
						error = DataBaseImporterError.DataBaseConnect;
						System.out.println(se.getLocalizedMessage());
					}
				
				//Checking if values of Float are valid or in range
				} else if (d instanceof FloatDesc) {
					
					try {
						while (result.next()) {
							
							Integer v = null;
							int colindex = result.findColumn(col);
							//If multiple values, database colum had to be of type String
							if (result.getMetaData().getColumnClassName(colindex).equals("java.lang.String")) {
								String[] values = result.getString(col).split(seperatorMultiple);
								if (values.length > 1) {
									d.setMultiple(true);
								}
								
								for (String s : values) {
									try {
										v = Integer.parseInt(s.trim());
									} catch (NumberFormatException ne) {
										invalidValues.add(new Pair<String, AttributeDesc> (s.trim(), d));
										continue;
									}
								}
								
								FloatDesc floatDesc = (FloatDesc) d;
								if (floatDesc.getMin() > v || floatDesc.getMax() < v) {
									invalidValues.add(new Pair<String, AttributeDesc> (Float.toString(v),d));
								}
							
							//if only one value it could be an float column
							} else if (result.getMetaData().getColumnClassName(colindex).equals("java.lang.Float")) {
								v = result.getInt(col);
								
								FloatDesc floatDesc = (FloatDesc) d;
								if (floatDesc.getMin() > v || floatDesc.getMax() < v) {
									invalidValues.add(new Pair<String, AttributeDesc> (Float.toString(v),d));
								}
							}	
							
						}
						result.beforeFirst();
					} catch (SQLException se) {
						error = DataBaseImporterError.DataBaseConnect;
						System.out.println(se.getLocalizedMessage());
					}
					
					//Checking if values of Double are valid or in range
				} else if (d instanceof DoubleDesc) {
					
					try {
						while (result.next()) {
							
							Integer v = null;
							int colindex = result.findColumn(col);
							//If multiple values, database colum had to be of type String
							if (result.getMetaData().getColumnClassName(colindex).equals("java.lang.String")) {
								String[] values = result.getString(col).split(seperatorMultiple);
								if (values.length > 1) {
									d.setMultiple(true);
								}
								
								for (String s : values) {
									try {
										v = Integer.parseInt(s.trim());
									} catch (NumberFormatException ne) {
										invalidValues.add(new Pair<String, AttributeDesc> (s.trim(), d));
										continue;
									}
								}
								
								DoubleDesc doubleDesc = (DoubleDesc) d;
								if (doubleDesc.getMin() > v || doubleDesc.getMax() < v) {
									invalidValues.add(new Pair<String, AttributeDesc> (Double.toString(v),d));
								}
							
							//if only one value it could be an Double column
							} else if (result.getMetaData().getColumnClassName(colindex).equals("java.lang.Double")) {
								v = result.getInt(col);
								
								DoubleDesc doubleDesc = (DoubleDesc) d;
								if (doubleDesc.getMin() > v || doubleDesc.getMax() < v) {
									invalidValues.add(new Pair<String, AttributeDesc> (Double.toString(v),d));
								}
							}	
							
						}
						result.beforeFirst();
					} catch (SQLException se) {
						error = DataBaseImporterError.DataBaseConnect;
						System.out.println(se.getLocalizedMessage());
					}
					
				//Checking if the symbol and boolean values are allowed values
				} else if (d instanceof SymbolDesc) {
					try {
						while (result.next()) {
							
							int colindex = result.findColumn(col);
							if (result.getMetaData().getColumnClassName(colindex).equals("java.lang.String")) {

								SymbolDesc symbDesc = (SymbolDesc) d;

								if (result.getString(col) != null) {
								
									String[] values = result.getString(col).split(seperatorMultiple);
	
									if (values.length > 1) {
										d.setMultiple(true);
									}
										
									for (String s : values) {
										if (!symbDesc.isAllowedValue(s.trim())) {
											s = s.replace(" ", "_");
											if (!symbDesc.isAllowedValue(s.trim())) {
												invalidValues.add(new Pair<String, AttributeDesc> (s.trim(),d));
											}
										}
									}
								}
							} 
						}
						result.beforeFirst();
					} catch (SQLException se) {
						error = DataBaseImporterError.DataBaseConnect;
						se.printStackTrace();
					}
				}
			}
			if (invalidValues.size() != 0) {
				error = DataBaseImporterError.ValueMissing;
			}
		}
		System.out.println("All Data checked");
		
	}
	
	/**
	 * maps the read data to cases
	 */
	public void doImport() {
		 t = new Thread(new DataBaseThread());
		 t.start();
	}
	
	
	/**
	 * Returns the TableName which should be used for import
	 * @return TableName
	 */
	public String getTableName() {
		return this.tablename;
	}
	
	/**
	 * Sets the tablename which should be used for import
	 * @param tablename
	 */
	public void setTableName(String tablename) {
		this.tablename = tablename;
	}
	
	public int getTotalNumberOfCases() {
		return this.totalCaseCount;
	}
	
	public int getCurrentNumberOfCases() {
		return this.currentCaseCount;
	}
	
	public String getSeperatorMultiple() {
		return this.seperatorMultiple;
	}
	
	public void setSeperatorMultiple(String seperator) {
		this.seperatorMultiple = seperator;
	}
	
	/**
	 * 
	 * @return true when still importing, false otherwisec
	 */
	public boolean isImporting() {
		return t.isAlive();
	}
	
	private class DataBaseThread implements Runnable {

		public void run() {
			for (Pair<String, AttributeDesc> p : invalidValues) {
				System.out.println(p.getSecond().getName() + ": " +p.getFirst());
				error = null;
			}
				
			if (error == null) {
				
				try {
					if (cb == null) {
						cb = concept.getProject().createDefaultCB("CB_" + tablename);
						System.out.println("New case base created: "+cb.getName());
					}
				} catch (Exception e) {
					System.out.println("Fehler");
					e.printStackTrace();
					return;
				}
			
			try {
				result.beforeFirst();
				
				while (result.next()) {
			
					Instance i = new Instance(concept, concept.getName() + Integer.toString(counter++));
					
					for (Mapping map : db.getMappings(tablename)) {

							AttributeDesc desc = Indicies.get(map.getTarget());
							if (desc == null) {
								System.out.println("DESC NULL: " + map.getTarget());
							}
							Integer colindex = result.findColumn(map.getSource());
							
							if (desc.isMultiple()) {
								if (result.getMetaData().getColumnClassName(colindex).equals("java.lang.String")) {
									
									if (result.getString(colindex) != null) {
										
										String[] values = result.getString(colindex).split(seperatorMultiple);
										LinkedList<Attribute> list = new LinkedList<Attribute>();
										for (String s : values) {
					
											//System.out.println(s);
											list.add(desc.getAttribute(s.trim()));
										}
										MultipleAttribute<AttributeDesc> multipleAtt = new MultipleAttribute<AttributeDesc>(desc, list);
										i.addAttribute(desc, multipleAtt);
										
									} else {
										LinkedList<Attribute> list = new LinkedList<Attribute>();
										MultipleAttribute<AttributeDesc> multipleAtt = new MultipleAttribute<AttributeDesc>(desc, list);
										i.addAttribute(desc, multipleAtt);
									}
								} 
							} else {
								if (result.getMetaData().getColumnClassName(colindex).equals("java.lang.String")) {
									i.addAttribute(Indicies.get(map.getTarget()), result.getString(colindex));
									
								} else if (result.getMetaData().getColumnClassName(colindex).equals("java.lang.Integer")) {
									i.addAttribute(Indicies.get(map.getTarget()), result.getInt(colindex));

								} else if (result.getMetaData().getColumnClassName(colindex).equals("java.lang.Float")) {
									i.addAttribute(Indicies.get(map.getTarget()), result.getFloat(colindex));
									
								} else if (result.getMetaData().getColumnClassName(colindex).equals("java.lang.Double")) {
									i.addAttribute(Indicies.get(map.getTarget()), result.getDouble(colindex));
									
								} else if (result.getMetaData().getColumnClassName(colindex).equals("java.lang.Boolean")) {
									i.addAttribute(Indicies.get(map.getTarget()), result.getBoolean(colindex));
									
								}
							}
						}

						cb.addCase(i);
						currentCaseCount++;
					}
				
			} catch (SQLException se) {
				se.printStackTrace();
			} catch (Exception e) {
				e.printStackTrace();
			}
			
			}
		}		
	}
	
	
	public enum DataBaseImporterError {
		DataBaseConnect("Could not connect to database"), ValueMissing("Values are Missing");
		
		private String desc;
		private String problem;
		
		DataBaseImporterError(String desc) {
			this.desc = desc;
		}
		
		public String getProblem() {
			return problem;
		}
		
		public void setProblem(String problem) {
			this.problem = problem;
		}
		
		public String getDesc() {
			return this.desc;
		}
	}
	
}
