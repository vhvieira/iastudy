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
package de.dfki.mycbr.model.similaritymeasures.smftypes;

import java.awt.Frame;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.jdom.Element;
import org.jdom.JDOMException;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel;
import de.dfki.mycbr.model.similaritymeasures.smftypes.ui.SMFPanel_External;
import de.dfki.mycbr.model.vocabulary.ModelInstance;
import de.dfki.mycbr.model.vocabulary.ModelSlot;
import de.dfki.mycbr.retrieval.Explanation;

/**
 * 
 * @author myCBR Team
 * Implements the "external" similiarity measure.
 *
 */
public class SMF_External extends AbstractSMFunction {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(SMF_External.class.getName());
	
	private static final String XML_ATT_IS_BINARY = "isBinary";
	private static final String XML_ATT_USE_CMD_LINE_ARGS = "useCommandLineArgs";
	private static final String XML_ATT_COMMAND = "command";
	private static final String XML_ATT_WORKING_DIR = "workingDir";
	
	private boolean isBinary = false;
	private boolean useCmdLineArgs = true;
	private String command = "";
	private String workingDir = "";
	
	private ModelSlot slot;
	
	private Process process = null; // here we store the reference to the 
	// external process. The process is started when the retrieval starts
	// and finishes when the retrieval ends. Communication is done over the
	// standard input and output streams.

	public SMF_External(ModelInstance inst, Element smfElement) throws JDOMException {
		super(inst, smfElement);
		slot = (ModelSlot) inst;
		isBinary = smfElement.getAttribute(XML_ATT_IS_BINARY).getBooleanValue();
		useCmdLineArgs = smfElement.getAttribute(XML_ATT_USE_CMD_LINE_ARGS).getBooleanValue();
		command = Helper.decode(smfElement.getAttribute(XML_ATT_COMMAND).getValue());
		workingDir = Helper.decode(smfElement.getAttributeValue(XML_ATT_WORKING_DIR));
	}
	
	public SMF_External(ModelInstance inst, String smfName) throws Exception {
		super(inst, smfName);
		slot = (ModelSlot) inst;
	}
	
	public static String getSMFunctionTypeName_static() {
		return "External";
	}

	public void toXML(Element xmlElement) {
		xmlElement.setAttribute(XML_ATT_IS_BINARY, Boolean.toString(isBinary));
		xmlElement.setAttribute(XML_ATT_USE_CMD_LINE_ARGS, 
				Boolean.toString(useCmdLineArgs));
		xmlElement.setAttribute(XML_ATT_COMMAND, Helper.encode(command));
		xmlElement.setAttribute(XML_ATT_WORKING_DIR, Helper.encode(workingDir));
	}

	/**
	 * copies this similarity function object.
	 */
	public AbstractSMFunction copy() {
		SMF_External newSmf = null;
		try {
			newSmf = new SMF_External(inst, smfName);
			// copy things
			newSmf.setBinary(isBinary);
			newSmf.setCommand(command);
			newSmf.setUseCmdLineArgs(useCmdLineArgs);
			newSmf.setHasChanged(false);
		} catch (Exception e) {
			log.log(Level.SEVERE, "error while copying", e);
		}
		return newSmf;
	}

	/**
	 * returns a new editor panel.
	 */
	protected SMFPanel createSMFPanel()	{
		// initialize a new editor panel representing this smfunction object.
		return new SMFPanel_External(this);
	}

	/**
	 * Check consistency to Slot. 
	 * If something doesn't match prompt user for some options and 
	 * make it consistent.
	 * @return boolean isConsistent
	 */
	public boolean checkConsistency(Frame parent, boolean quiet) {
		// Luckily this cannot become inconsistent; at least I cannot see
		// any reasons.
		return true;
	}
	
	public ValueType getValueType() {
		return slot.getValueType();
	}

	public ModelSlot getSlot() {
		return slot;
	}
	
	// commented out before 20.10.2008
//	private double getResult(Process p) throws IOException {
//		StringBuilder b = new StringBuilder();
//		InputStream in = p.getInputStream();
//		while (true) {
//		  int c = in.read();
//		  // -1 means EOF, 13 is CR, 10 is LF, both may be used as
//		  // line endings
//		  if (c == -1 || c == 13 || c == 10) break;
//		  b.append((byte) c);
//		}
//		return Double.parseDouble(b.toString());
//	}

	private ArrayList<String> cmd = null;
	
	private ArrayList<String> parseCommand(String cmd) {
		ArrayList<String> result = new ArrayList<String>();
		StringBuilder b = new StringBuilder();
		int i = 0;
		while (i < cmd.length()) {
			b.setLength(0); // parse a new word
			if (cmd.charAt(i) == '"') {
				i++; // skip "
				while (i < cmd.length() && cmd.charAt(i) != '"') {
					b.append(cmd.charAt(i));
					i++;
				}
				i++; // skip "
			} else {
				while (i < cmd.length() && cmd.charAt(i) != ' '
								&& cmd.charAt(i) != '\t') {
					b.append(cmd.charAt(i));
					i++;
				}
				i++; // skip whitespace
			}
			if (b.length() > 0) {
				result.add(b.toString());
			}
		}
		return result;
	}
	
	public double checkSimilarityBetween(Object query, Object cb) throws IOException {
		double result = 0.0;
		if (!useCmdLineArgs) {
			if (process != null) {
				OutputStream out = process.getOutputStream();
				BufferedWriter w = new BufferedWriter(new OutputStreamWriter(out));
				w.write(query.toString() + "\n");
				w.write(cb.toString() + "\n");
				w.flush();
			}
		} else {
			// we use command line arguments for passing the data.
			// 'command' may contain arguments too:
			if (cmd == null) {
				cmd = parseCommand(command);
				// reserve space for the query and the case:
				cmd.add(""); 
				cmd.add("");
			}
			cmd.set(cmd.size()-2, query.toString());
			cmd.set(cmd.size()-1, cb.toString());
			
			// commented out before 20.10.2008
//			String[] x = command.split("\\s+");
//			String[] cmd = new String[x.length + 2];
//			for (int i = 0; i < x.length; i++) cmd[i] = x[i];
//			cmd[x.length] = query.toString();
//			cmd[x.length+1] = cb.toString();
			
			
			ProcessBuilder pb = new ProcessBuilder(cmd);
			pb.directory(new File(workingDir));
			process = pb.start(); 
			if (process == null) throw new IOException("cannot create process");
			
			// commented out before 20.10.2008
//			process = Runtime.getRuntime().exec(
//					new String[]{command, query.toString(), cb.toString()},
//					null, new File(workingDir));
			
		}
		if (process != null) {
			String s = new BufferedReader(new InputStreamReader(
					process.getInputStream())).readLine(); 
			if (s != null) {
				result = Double.parseDouble(s);
			} else {
				throw new IOException("process failed");
			}
		}
		return result;
	}
	
	public double getSimilarityBetween(Object query, Object cb, Explanation exp) throws Exception {
		double result = 0.0;
		result = checkSimilarityBetween(query, cb);
		// Explanation
		if (exp != null) {
			Helper.addExplanation(exp, this.inst, query, cb, this, null, result);
		}
		return result;
	}
	
	public void startRetrieval() throws Exception {
		// we override this method here to start the process
		if (!useCmdLineArgs) {
			String[] cmd = command.split("\\s+");
			ProcessBuilder pb = new ProcessBuilder(cmd);
			pb.directory(new File(workingDir));
			process = pb.start();
			//process = Runtime.getRuntime().exec(command, null, new File(workingDir));
			if (process == null) {
				log.severe("cannot create process");
			}
		}
	}

	public void finishRetrieval() throws Exception {
		// we override this method here to kill the process
		if (process != null) {
			process.getOutputStream().write(-1); // put EOF to the subprocess's stdin
		}
	}

	/**
	 * @param isBinary the isBinary to set
	 */
	public void setBinary(boolean isBinary) {
		setHasChanged(isBinary != this.isBinary);
		this.isBinary = isBinary;
	}

	/**
	 * @return the isBinary
	 */
	public boolean isBinary() {
		return isBinary;
	}

	/**
	 * @param useCmdLineArgs the useCmdLineArgs to set
	 */
	public void setUseCmdLineArgs(boolean useCmdLineArgs) {
		setHasChanged(useCmdLineArgs != this.useCmdLineArgs);
		this.useCmdLineArgs = useCmdLineArgs;
	}

	/**
	 * @return the useCmdLineArgs
	 */
	public boolean isUseCmdLineArgs() {
		return useCmdLineArgs;
	}

	/**
	 * @param command the command to set
	 */
	public void setCommand(String command) {
		setHasChanged(true);
		this.command = command;
	}

	/**
	 * @return the command
	 */
	public String getCommand() {
		return command;
	}

	/**
	 * @param workingDir the workingDir to set
	 */
	public void setWorkingDir(String workingDir) {
		setHasChanged(true);
		this.workingDir = workingDir;
	}

	/**
	 * @return the workingDir
	 */
	public String getWorkingDir() {
		return workingDir;
	}
}
