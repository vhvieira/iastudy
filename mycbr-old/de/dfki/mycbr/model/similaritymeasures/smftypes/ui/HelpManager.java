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
package de.dfki.mycbr.model.similaritymeasures.smftypes.ui;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextPane;
/**
 * 
 * @author myCBR Team
 */
public class HelpManager extends JDialog {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger ( HelpManager.class.getName ( ) );
	
	// keys
	public static final String KEY_SIMILARITY_MODE 					= "SIMILARITY_MODE"; //$NON-NLS-1$
	public static final String KEY_SYMMETRY 						= "SYMMETRY"; //$NON-NLS-1$
	public static final String KEY_OVERRIDE_SIMILARITIES			= "OVERRIDE_SIMILARITIES"; //$NON-NLS-1$
	public static final String KEY_INTEGER_ADVANCED_ASP 			= "INTEGER_ADVANCED_ASP"; //$NON-NLS-1$
	public static final String KEY_INTEGER_ADVANCED_BSP 			= "INTEGER_ADVANCED_BSP"; //$NON-NLS-1$

	public static final String KEY_INTEGER_STANDARD_STEP 			= "INTEGER_STANDARD_STEP"; //$NON-NLS-1$
	public static final String KEY_INTEGER_STANDARD_POLYNOMIAL 		= "INTEGER_STANDARD_POLYNOMIAL"; //$NON-NLS-1$
	public static final String KEY_INTEGER_STANDARD_SMOOTH 			= "INTEGER_STANDARD_SMOOTH"; //$NON-NLS-1$
	public static final String KEY_INTEGER_STANDARD_CONST 			= "INTEGER_STANDARD_CONST"; //$NON-NLS-1$
	
	public static final String KEY_SYMBOL_TAXONOMY_INNER_NODES 		= "SYMBOL_TAXONOMY_INNER_NODES"; //$NON-NLS-1$
	public static final String KEY_SYMBOL_TAXONOMY_INNER_SEMANTIC	="SYMBOL_TAXONOMY_INNER_SEMANTIC"; //$NON-NLS-1$
	public static final String KEY_SYMBOL_TAXONOMY_UNCERTAIN 		= "SYMBOL_TAXONOMY_UNCERTAIN"; //$NON-NLS-1$
	
	private static final String HELP_FILENAME = "helptxt.cfg"; //$NON-NLS-1$

	private JEditorPane jEditorPane = null;
	private JPanel paButtons = null;
	private JButton buOK = null;
	private JTextPane helpTextPane = null;
	private JScrollPane scrollpane = null;
	private static HashMap<String, String> helpContent = null;
	//private String helpKey;
	private static HelpManager instance;
	
	/**
	 * Default constructor. Sets frame as owner of this.
	 * 
	 * @param frame owner of this
	 * @param helpKey specifies which help text to display 
	 */
	private HelpManager( ) {
		super();
		String imageLocation = "logo_mycbr.png"; //$NON-NLS-1$
		//this.setIcon(java.awt.Toolkit.getDefaultToolkit().getImage(this.getClass().getResource(imageLocation)));
		((java.awt.Frame)getOwner()).setIconImage(java.awt.Toolkit.getDefaultToolkit().getImage(this.getClass().getResource(imageLocation)));
		this.setAlwaysOnTop(true);
		initialize();
		
		setSize(600, 500);
	}
	
	public static HelpManager getInstance(String helpKey) {
		
		if (instance == null) {
			instance = new HelpManager( );			
		} 

		// get the text for specified for helpKey
		instance.setText((String)helpContent.get(helpKey));
		
		return instance;
	}
	
	/**
	 * Initializes the help content by loading the help file
	 * and mapping each help key with its help text.
	 * 
	 * @throws IOException 
	 */
	private static void initHelp() throws IOException {
		helpContent = new HashMap<String, String>();
		BufferedReader br = new BufferedReader(new InputStreamReader(HelpManager.class.getResourceAsStream(HELP_FILENAME)));
		
		String line = null;
		String currentKey = null;
		StringBuffer sb = new StringBuffer();
		while ((line = br.readLine()) != null) {	
			if (line.startsWith(":")) { //$NON-NLS-1$
				// put old help into map
				if (currentKey != null) {
					helpContent.put(currentKey, sb.toString());
				}
				
				// and init new key
				currentKey = line.substring(1);
				sb = new StringBuffer();
				continue;
			}
			// add to help text
			sb.append(line);
			sb.append("\n"); //$NON-NLS-1$
			
		}
		
		// store the last key
		helpContent.put(currentKey, sb.toString());
		
		br.close();
	}
	
	/**
	 * Initializes this
	 * 
	 * @return void
	 */
	private void initialize() {

		this.setSize(374, 304);
		this.setTitle(Messages.getString("Info")); //$NON-NLS-1$
		this.getContentPane().add(getJEditorPane());
		this.requestFocus();
		buOK.requestFocusInWindow();
		buOK.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				dispose();
			}
		});
		
		if (helpContent == null) {
			try {
				initHelp();
			} catch (Exception e) {
				e.printStackTrace();
				log.log(Level.SEVERE, "could not load [" + HELP_FILENAME + "]", e); //$NON-NLS-1$ //$NON-NLS-2$
			}
		}
		
	}
	
	/**
	 * Initializes jContentPane to display the 
	 * button and the main content.
	 * 
	 * @return javax.swing.JEditorPane main container of this
	 */
	private JEditorPane getJEditorPane() {
		if (jEditorPane == null) {
			jEditorPane = new JEditorPane();
			jEditorPane.setLayout(new BorderLayout());
			jEditorPane.add(getPaButtons(), java.awt.BorderLayout.SOUTH);
			jEditorPane.add(getScrollPane(), java.awt.BorderLayout.CENTER);
		}
		return jEditorPane;
	}

	/**
	 * Initializes scrollpane to display the 
	 * component containing the help text.
	 * 
	 * @return javax.swing.JScrollPane containing the text pane
	 */
	private JScrollPane getScrollPane() {
		if (scrollpane == null) {
			scrollpane = new JScrollPane(getHelpTextPane());
		}
		return scrollpane;
	}

	/**
	 * Initializes paButtons.	
	 * 	
	 * @return javax.swing.JPanel containt the ok button
	 */
	private JPanel getPaButtons() {
		if (paButtons == null) {
			GridBagConstraints gridBagConstraints = new GridBagConstraints();
			gridBagConstraints.insets = new java.awt.Insets(5,5,5,5);
			paButtons = new JPanel();
			paButtons.setLayout(new GridBagLayout());
			paButtons.add(getBuOK(), gridBagConstraints);
		}
		return paButtons;
	}

	/**
	 * Initializes buOK	
	 * 	
	 * @return javax.swing.JButton	OK button
	 */
	private JButton getBuOK() {
		if (buOK == null) {
			buOK = new JButton();
			buOK.setText(Messages.getString("OK")); //$NON-NLS-1$
		}
		return buOK;
	}

	/**
	 * Initializes the pane containing the help text.	
	 * 	
	 * @return javax.swing.JTextPane pane containing the help text	
	 */
	private JTextPane getHelpTextPane() {
		if (helpTextPane == null) {
			helpTextPane = new JTextPane();
			helpTextPane.setEditable(false);
			helpTextPane.setContentType("text/html"); //$NON-NLS-1$
		}
		return helpTextPane;
	}

	/**
	 * Fills the text pane with the current help text
	 * 
	 * @param text help text to be displayed
	 */
	public void setText(String text) {
		// get URL of help icon, style sheet and separator for help window
		URL help = HelpManager.class.getResource("help1.png"); //$NON-NLS-1$
		URL styleSheet = HelpManager.class.getResource("style.css"); //$NON-NLS-1$
		URL background = HelpManager.class.getResource("background.png");  //$NON-NLS-1$
		
		// set URL of line image for separating headline from text 
		String line = getClass().getResource("line.png").toString();  //$NON-NLS-1$
		text = text.replaceAll("<LINE>", line); //$NON-NLS-1$
		
		helpTextPane.setText("<html><head>"   + //$NON-NLS-1$
				"<link rel=STYLESHEET TYPE=\"text/css\" HREF=\""  //$NON-NLS-1$
				+ styleSheet + "\"></head><body background=\""  //$NON-NLS-1$
				+ background + "\" ><table align=\"center\" padding=\"0\" cellspacing=\"0\"><tr><td align=\"left\" width=\"10%\"><img src=\""  //$NON-NLS-1$
				+ help + "\" ></td>" + text); //$NON-NLS-1$

	}
} 
