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

package de.dfki.mycbr.util;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * class to represent a database for the DB importer
 * @author myCBR Team, Pascal Reuss
 *
 */
public class Database {
	
	private int id;
	private String host;
	private String port;
	private String user;
	private String pwd;
	private String dbname;
	private String dbtype;
	private String driver;
	
	private HashMap<String, HashMap<String, Object>> mappings;
	
	/**
	 * Constructor
	 * @param params Array with database parameters
	 * (0) id, (1) host, (2) port, (3) user, (4) pwd, (5) dbname, (6) dbtype, (7) driver
	 */
	public Database(String[] params) {
		this.id = Integer.parseInt(params[0]);
		this.host = params[1];
		this.port = params[2];
		this.user = params[3];
		this.pwd = params[4];
		this.dbname = params[5];
		this.dbtype = params[6];
		this.driver = params[7];
		this.mappings = new HashMap<String, HashMap<String, Object>>();
	}
	
	public Database() {
		this.mappings = new HashMap<String, HashMap<String, Object>>();
	}

	public String getHost() {
		return host;
	}

	public void setHost(String host) {
		this.host = host;
	}

	public String getPort() {
		return port;
	}

	public void setPort(String port) {
		this.port = port;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPwd() {
		return pwd;
	}

	public void setPwd(String pwd) {
		this.pwd = pwd;
	}

	public String getDbname() {
		return dbname;
	}

	public void setDbname(String dbname) {
		this.dbname = dbname;
	}

	public String getDbtype() {
		return dbtype;
	}

	public void setDbtype(String dbtype) {
		this.dbtype = dbtype;
	}

	public String getDriver() {
		return driver;
	}

	public void setDriver(String driver) {
		this.driver = driver;
	}
	
	public HashMap<String, HashMap<String, Object>> getMapping() {
		return this.mappings;
	}
	
	public void setMappings(HashMap<String, HashMap<String, Object>> mappings) {
		this.mappings = mappings;
	}
	
	public String getURL() {
		return "jdbc:" + this.dbtype + "://" + this.host + ":" + this.port + "/" + this.dbname;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public int getId() {
		return this.id;
	}
	
	public String getConcept(String table) {
		return (String) mappings.get(table).get("concept");
	}
	
	@SuppressWarnings("unchecked")
	public ArrayList<Mapping> getMappings(String table) {
		return (ArrayList<Mapping>) mappings.get(table).get("mapping");
	}

}
