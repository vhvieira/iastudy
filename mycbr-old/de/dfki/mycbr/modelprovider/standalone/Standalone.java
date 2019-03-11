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
package de.dfki.mycbr.modelprovider.standalone;

import java.awt.Dimension;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.io.File;

import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JMenuItem;
import javax.swing.filechooser.FileFilter;

import de.dfki.mycbr.CBRProject;
import de.dfki.mycbr.XMLConstants;
import de.dfki.mycbr.modelprovider.protege.similaritymodeling.ImportInstancesDialog;

/**
 * 
 * @author myCBR Team
 */
public class Standalone implements ActionListener {
	private static JFrame frame;
	private static CBRProject cbrProject;
	
	private static File selectProjectFile() {
		//
		// choose project
		// 
		JFileChooser fc = new JFileChooser();
		fc.setFileFilter(new FileFilter() {
			public boolean accept(File f) {
				if (f.isDirectory()) {
					return true;
				}
				return f.getName().toUpperCase().endsWith(XMLConstants.FILENAME_SUFFIX_SMF);
			}

			public String getDescription() {
				return Messages.getString("Mycbr_files"); //$NON-NLS-1$
			}
		});
		fc.setFileSelectionMode(JFileChooser.FILES_ONLY);
		fc.showOpenDialog(null);

		return fc.getSelectedFile();

	}

	public static RetrievalFrame createRetrievalFrame() {
		RetrievalFrame frame = new RetrievalFrame();
		Dimension screensize = Toolkit.getDefaultToolkit().getScreenSize();
		frame.setSize((int)screensize.getWidth()-20, (int)screensize.getHeight()-100);
		frame.setLocation((screensize.width - frame.getWidth()) / 2, 20);
		return frame;
	}

	protected static JMenuItem menuSave;
	protected static JMenuItem menuCSVImporter;
	protected static JMenuItem menuQuit;
	
	public void actionPerformed(ActionEvent e) {
		if (e.getSource() == menuSave) {
			cbrProject.save(cbrProject.getProjectName(), cbrProject.getProjectDir());
		} else if (e.getSource() == menuQuit) {
			frame.dispose();
		} else if (e.getSource() == menuCSVImporter) {
			ImportInstancesDialog iid = new ImportInstancesDialog(frame, "", false,  //$NON-NLS-1$
					null);
			iid.setVisible(true);
		}
	}

	protected static JMenuBar buildMenu(JFrame frame) {
		Standalone dummy = new Standalone(); // we have no this object :-(
		JMenuBar menuBar = new JMenuBar();
		JMenu menuFile = new JMenu(Messages.getString("File")); //$NON-NLS-1$
		menuSave = new JMenuItem(Messages.getString("Save_project"), KeyEvent.VK_S); //$NON-NLS-1$
		menuSave.addActionListener(dummy);
		menuFile.add(menuSave);
		menuCSVImporter = new JMenuItem(Messages.getString("Import_instances_from_csv"), KeyEvent.VK_I); //$NON-NLS-1$
		menuCSVImporter.addActionListener(dummy);
		menuFile.add(menuCSVImporter);
		menuQuit = new JMenuItem(Messages.getString("Quit"), KeyEvent.VK_Q); //$NON-NLS-1$
		menuQuit.addActionListener(dummy);
		menuFile.add(menuQuit);
		menuBar.add(menuFile);
		return menuBar;
	}
	
	public static void main(String[] args)
	{
		File projFile = null;
		if (args.length>0)
		{
			// first argument is a project file name
			projFile = new File(args[0]);
		}
		else
		{
			projFile = selectProjectFile();
		}
		
		if (projFile==null || !projFile.exists())
		{
			System.err.println("Sorry. Project file not found : ["+(projFile==null?null:projFile.getAbsolutePath())+"]"); //$NON-NLS-1$ //$NON-NLS-2$
			System.exit(-1);
		}
		
		cbrProject = CBRProject.getInstance(projFile);
		frame = createRetrievalFrame();
		
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setJMenuBar(buildMenu(frame));
		frame.setVisible(true);
		
	}

}
