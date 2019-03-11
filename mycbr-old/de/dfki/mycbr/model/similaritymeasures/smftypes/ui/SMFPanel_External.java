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
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.File;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextField;

import de.dfki.mycbr.model.similaritymeasures.AbstractSMFunction;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_External;
import de.dfki.mycbr.modelprovider.protege.similaritymodeling.InstanceGenerator;

/**
 * Panel for using an external script as similarity measure function.
 * You can choose an executable file and a working directory.
 * A panel for testing the script is included, too.
 * 
 * @author myCBR Team
 *
 */
public class SMFPanel_External extends SMFPanel implements ActionListener, KeyListener {

	private static final long serialVersionUID = 1L;

	private JTextField tfCommand;
	private JTextField tfWorkingDir;
	private JButton buTest; 
	private JButton buOpen;
	
	private SMF_External smf;
	private SMFPanel_SMF_Test testPanel;	
		
	public SMFPanel_External(AbstractSMFunction smf) {
		super(smf);
		this.smf = (SMF_External) smf;
		testPanel = new SMFPanel_SMF_Test(this.smf);
		initialize();
	}
	
	private void initialize() {
		
		this.setLayout(new BorderLayout());
		
		JPanel panel = new JPanel(new GridBagLayout());

		tfCommand 		= new JTextField(smf.getCommand());
		tfWorkingDir 	= new JTextField(smf.getWorkingDir());
	
		tfCommand.addKeyListener(this);
		tfWorkingDir.addKeyListener(this);
		
		JLabel commandLabel = new JLabel(Messages.getString("Command")); //$NON-NLS-1$
		JLabel dirLabel = new JLabel(Messages.getString("Working_directory")); //$NON-NLS-1$
		
		// layout components according to grid bag layout
		GridBagConstraints gridBagConstraints1 = new GridBagConstraints();
		gridBagConstraints1.fill = GridBagConstraints.HORIZONTAL;
		gridBagConstraints1.gridy = 0;
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.gridwidth = 3;
		gridBagConstraints1.insets = new Insets( 5, 5, 5, 5 );
		gridBagConstraints1.anchor = GridBagConstraints.FIRST_LINE_START;
		panel.add( commandLabel, gridBagConstraints1 );
		
		gridBagConstraints1.gridy = 1;
		gridBagConstraints1.gridx = 0;
		panel.add( dirLabel, gridBagConstraints1);
		
		gridBagConstraints1.gridy = 0;
		gridBagConstraints1.gridx = 3;
		gridBagConstraints1.gridwidth = 3;
		gridBagConstraints1.weightx = 0.5;
		panel.add(tfCommand, gridBagConstraints1);
		
		gridBagConstraints1.gridy = 1;
		gridBagConstraints1.gridx = 3;
		gridBagConstraints1.weightx = 0.5;
		panel.add(tfWorkingDir, gridBagConstraints1);	
		
		gridBagConstraints1.gridy = 2;
		gridBagConstraints1.gridx = 0;
		gridBagConstraints1.weightx = 0.0;
		gridBagConstraints1.gridwidth = 1;
		panel.add(getBuOpen(), gridBagConstraints1);
		
		gridBagConstraints1.gridx = 1;
		panel.add(getBuTest(), gridBagConstraints1);
		
		// add empty label for filling the rest of the panel
		// so that the input fields do not get too wide
		gridBagConstraints1.gridy = 0;
		gridBagConstraints1.gridx = 8;
		gridBagConstraints1.weightx = 1.0;
		panel.add(new JLabel(" "), gridBagConstraints1); //$NON-NLS-1$
		
		panel.setBorder(BorderFactory.createEmptyBorder(20, 0, 20, 0));
		this.add(panel, BorderLayout.NORTH);
		this.add(testPanel, BorderLayout.WEST);
	}
 	
	private JButton getBuOpen() {
		if (buOpen == null) {
			buOpen = new JButton(Messages.getString("Select")); //$NON-NLS-1$
			buOpen.setToolTipText(Messages.getString("Select_the_executable")); //$NON-NLS-1$
			buOpen.addActionListener(this);
		}
		return buOpen;
	}
	
	private JButton getBuTest() {
		if (buTest == null) {
			buTest = new JButton(Messages.getString("Test")); //$NON-NLS-1$
			buTest.setToolTipText(Messages.getString("Test_the_external_command")); //$NON-NLS-1$
			buTest.setEnabled( false );
			buTest.addActionListener( this );
		}
		return buTest;
	}

	public void actionPerformed(ActionEvent event) {
		if (event.getSource().equals(buOpen)) {
			JFileChooser chooser = new JFileChooser();
			chooser.setMultiSelectionEnabled(false);
			if (chooser.showOpenDialog(SMFPanel_External.this) == JFileChooser.APPROVE_OPTION) {
				File f = chooser.getSelectedFile();
				String exe = f.getAbsolutePath(); // f.getName();
				
				if (exe.contains(" ")) { //$NON-NLS-1$
					exe = "\"" + f + "\""; // do proper quoting //$NON-NLS-1$ //$NON-NLS-2$
				}
		  	
				String path = f.getParent();
				if (path == null) {
					path = ""; //$NON-NLS-1$
				}
				
				tfCommand.setText(exe);
				tfWorkingDir.setText(path);
				smf.setCommand(tfCommand.getText());
				smf.setWorkingDir(tfWorkingDir.getText());
				buTest.setEnabled(!smf.getCommand().equals("")); //$NON-NLS-1$
			}	
		  
		} else if (event.getSource().equals(buTest)) {
			double result = 0.0;
			Object val = InstanceGenerator.generateSlotValue(smf.getSlot());
			Object val2 = InstanceGenerator.generateSlotValue(smf.getSlot());
			try {
				// get any valid value for checking:
				smf.startRetrieval();
				result = smf.checkSimilarityBetween(val, val2);
				smf.finishRetrieval();
			} catch (Exception e) {
				JOptionPane.showMessageDialog(SMFPanel_External.this, 
						e.getMessage(), Messages.getString("Error"), //$NON-NLS-1$
						JOptionPane.ERROR_MESSAGE);
				return;
			}
			// else: no exception: everything worked: 
			JOptionPane.showMessageDialog(SMFPanel_External.this, 
					"It seems to work: For (" + val.toString() + ", "  //$NON-NLS-1$ //$NON-NLS-2$
					+ val2.toString() + ") the result is: " + result, "Success", //$NON-NLS-1$ //$NON-NLS-2$
					JOptionPane.INFORMATION_MESSAGE);
			JOptionPane.showMessageDialog(SMFPanel_External.this, 
					String.format(Messages.getString("It_seems_to_work_for(_,_)_result_is_"), val.toString(), val2.toString(), result), //$NON-NLS-1$
					Messages.getString("Success"), JOptionPane.INFORMATION_MESSAGE); //$NON-NLS-1$
		}
	}

	public void keyReleased(KeyEvent e) {
		smf.setCommand(tfCommand.getText());
		smf.setWorkingDir(tfWorkingDir.getText());
		buTest.setEnabled(!smf.getCommand().equals("")); //$NON-NLS-1$
	}
	
	public void keyPressed(KeyEvent e) {}
	public void keyTyped(KeyEvent e) {}
}
