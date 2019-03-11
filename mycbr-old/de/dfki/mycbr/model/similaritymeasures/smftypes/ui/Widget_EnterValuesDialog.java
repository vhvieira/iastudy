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

import java.awt.Dimension;
import java.awt.Frame;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import de.dfki.mycbr.Helper;
import de.dfki.mycbr.ValueAcceptor;
import de.dfki.mycbr.ValueAcceptorImpl;
import de.dfki.mycbr.ValueType;
import de.dfki.mycbr.model.similaritymeasures.smftypes.SMF_Number_Advanced;

/**
 * @author myCBR Team
 *
 */
public class Widget_EnterValuesDialog extends JDialog implements KeyListener, ActionListener {
	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private final static Logger log = Logger.getLogger(Widget_EnterValuesDialog.class.getName());
	
	JPanel panel1 = new JPanel();

	JLabel laMessage = new JLabel();

	JScrollPane jScrollPane1 = new JScrollPane();

	JPanel paValues = new JPanel();

	GridBagLayout gridBagLayout1 = new GridBagLayout();

	GridBagLayout gridBagLayout2 = new GridBagLayout();

	Map<String,String> keyValueMap;
	Map<String,ValueAcceptor> keyAcceptorMap;

	HashMap<JTextField, String> txtPaneKeyMap = new HashMap<JTextField, String>();

	JButton buOk = new JButton();

	JButton buCancel = new JButton();

	public Widget_EnterValuesDialog(Frame frame, SMF_Number_Advanced smf) {
		super(frame, Messages.getString("Enter_new_sample_point"), true);
		String[] keys = new String[] { "Distance", "Similarity" };
		String[] initialValues = new String[] { "0", "1.0" };
		ValueAcceptor[] acceptors;
		
		if(smf.getValueType() == ValueType.FLOAT) {
			acceptors = new ValueAcceptor[] { ValueAcceptorImpl.getValueAcceptor_Float(), ValueAcceptorImpl.getValueAcceptor_SimilarityValue() }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
		} else {
			acceptors = new ValueAcceptor[] { ValueAcceptorImpl.getValueAcceptor_Integer(), ValueAcceptorImpl.getValueAcceptor_SimilarityValue() }; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$	
		}
		
		try {
			jbInit();
			laMessage.setText(Messages.getString("Enter_new_sample_point"));
			keyValueMap = new HashMap<String, String>();
			keyAcceptorMap = new HashMap<String, ValueAcceptor>();
			for (int i=0; i<keys.length; i++) {
				JLabel laKey = new JLabel();
				JTextField txtValue = new JTextField();
				
				txtValue.addKeyListener(this);
				txtValue.addActionListener(this);

				laKey.setText(keys[i]);
				txtValue.setText(initialValues[i]);
				laKey.setAlignmentX(JLabel.RIGHT_ALIGNMENT);
				
				paValues.add(laKey, new GridBagConstraints(0, i, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
				paValues.add(txtValue, new GridBagConstraints(1, i, 1, 1, 1.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(5, 5, 5, 5), 0, 0));

				txtPaneKeyMap.put(txtValue, keys[i]);
				keyValueMap.put(keys[i], initialValues[i]);
				keyAcceptorMap.put(keys[i], acceptors[i]);
			}
			customInit();
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}

	private void customInit() {
		// buttons
		buCancel.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// discard and close
				keyValueMap = null;
				dispose();
			}
		});

		buOk.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				confirmAndClose();
			}
		});
		
		
		keyReleased(null);
		setSize(400, 200);
		Helper.centerWindow(this);
	}

	protected void confirmAndClose() {
		if (!checkValidity()) {
			return;
		}
		dispose();
	}

	/**
	 * 
	 * @return true if input data is valid
	 */
	private boolean checkValidity() {
		// copy all data from the text fields and check validity.
		for (Iterator<Entry<JTextField, String>> it=txtPaneKeyMap.entrySet().iterator(); it.hasNext();) {
			Entry<JTextField, String>  entry = it.next();
			String txt = entry.getKey().getText();
			String key = entry.getValue();
			keyValueMap.put(key, txt);
			ValueAcceptor va = (ValueAcceptor)keyAcceptorMap.get(key);
			if (!va.accept(txt)) {
				log.fine("[" + key + "] doesnt allow [" + txt + "]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
				return false;
			}
		}
		return true;
	}

	private void jbInit() throws Exception {
		panel1.setLayout(gridBagLayout2);
		laMessage.setText(Messages.getString("Message")); //$NON-NLS-1$
		paValues.setLayout(gridBagLayout1);
		buOk.setMaximumSize(new Dimension(75, 23));
		buOk.setMinimumSize(new Dimension(75, 23));
		buOk.setPreferredSize(new Dimension(75, 23));
		buOk.setText(Messages.getString("OK")); //$NON-NLS-1$
		buCancel.setMaximumSize(new Dimension(75, 23));
		buCancel.setMinimumSize(new Dimension(75, 23));
		buCancel.setPreferredSize(new Dimension(75, 23));
		buCancel.setText(Messages.getString("Cancel")); //$NON-NLS-1$
		getContentPane().add(panel1);
		panel1.add(laMessage, new GridBagConstraints(0, 0, 2, 1, 0.0, 0.0, GridBagConstraints.WEST, GridBagConstraints.HORIZONTAL, new Insets(0, 5, 5, 5), 0, 0));
		panel1.add(jScrollPane1, new GridBagConstraints(0, 1, 2, 1, 1.0, 1.0, GridBagConstraints.CENTER, GridBagConstraints.BOTH, new Insets(0, 0, 0, 0), 0, 0));
		panel1.add(buOk, new GridBagConstraints(0, 2, 1, 1, 1.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		panel1.add(buCancel, new GridBagConstraints(1, 2, 1, 1, 0.0, 0.0, GridBagConstraints.EAST, GridBagConstraints.NONE, new Insets(5, 5, 5, 5), 0, 0));
		jScrollPane1.getViewport().add(paValues, null);
	}

	public Map<String, String> getResultMap() {
		return keyValueMap;
	}

	public void keyPressed(KeyEvent e) {
	}

	public void keyReleased(KeyEvent e) {
		if (!checkValidity()) {
			buOk.setEnabled(false);
			return;
		}
		buOk.setEnabled(true);
	}

	public void keyTyped(KeyEvent e) {
	}

	public void actionPerformed(ActionEvent e) {
		confirmAndClose();
	}
}
