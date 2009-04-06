//    jDownloader - Downloadmanager
//    Copyright (C) 2008  JD-Team support@jdownloader.org
//
//    This program is free software: you can redistribute it and/or modify
//    it under the terms of the GNU General Public License as published by
//    the Free Software Foundation, either version 3 of the License, or
//    (at your option) any later version.
//
//    This program is distributed in the hope that it will be useful,
//    but WITHOUT ANY WARRANTY; without even the implied warranty of
//    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//    GNU General Public License for more details.
//
//    You should have received a copy of the GNU General Public License
//    along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.gui.skins.simple.config;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.logging.Logger;

import javax.swing.ButtonGroup;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.text.PlainDocument;

import jd.config.ConfigContainer;
import jd.config.ConfigEntry;
import jd.gui.skins.simple.components.BrowseFile;
import jd.gui.skins.simple.components.JDTextArea;
import jd.gui.skins.simple.components.JDTextField;
import jd.gui.skins.simple.components.JLinkButton;
import jd.gui.skins.simple.config.panels.PremiumPanel;
import jd.utils.JDUtilities;

/**
 * Diese Klasse fasst ein label / input Paar zusammen und macht das lesen und
 * schreiben einheitlich. Es lassen sich so Dialogelemente Automatisiert
 * einfügen.
 */
public class GUIConfigEntry implements ActionListener, ChangeListener, PropertyChangeListener, DocumentListener {

    private static final long serialVersionUID = -1391952049282528582L;
    private static final String DEBUG = "";

    private static final Object GAPRIGHT = "gapright 0";


    private ConfigEntry configEntry;

    /**
     * Die input Komponente
     */

    private JComponent[] input;
    private JComponent decoration;
    // private Insets insets = new Insets(1, 5, 1, 5);

    // private JComponent left;

    protected Logger logger = JDUtilities.getLogger();

    // private JComponent right;

    // private JComponent total;

    /**
     * Erstellt einen neuen GUIConfigEntry
     * 
     * @param type
     *            TypID z.B. GUIConfigEntry.TYPE_BUTTON
     * @param propertyInstance
     *            Instanz einer propertyklasse (Extends property).
     * @param propertyName
     *            Name der Eigenschaft
     * @param label
     *            Label
     */
    GUIConfigEntry(ConfigEntry cfg) {
        configEntry = cfg;
        cfg.setGuiListener(this);
        this.addPropertyChangeListener(cfg);

        // this.setBorder(BorderFactory.createEtchedBorder());
        input = new JComponent[1];

        switch (configEntry.getType()) {

        case ConfigContainer.TYPE_LINK:

            try {
                input[0] = new JLinkButton(configEntry.getLabel(), new URL(configEntry.getPropertyName()));

            } catch (MalformedURLException e) {
                input[0] = new JLabel(configEntry.getPropertyName());
                e.printStackTrace();
            }
            // addInstantHelpLink();
            input[0].setEnabled(configEntry.isEnabled());

            break;

        case ConfigContainer.TYPE_PASSWORDFIELD:

            decoration = new JLabel(configEntry.getLabel());
            input[0] = new JPasswordField();

            PlainDocument doc = (PlainDocument) ((JPasswordField) input[0]).getDocument();
            doc.addDocumentListener(this);

            input[0].setEnabled(configEntry.isEnabled());
            ((JPasswordField) input[0]).setHorizontalAlignment(SwingConstants.RIGHT);

            break;

        case ConfigContainer.TYPE_TEXTFIELD:

            decoration = new JLabel(configEntry.getLabel());
            input[0] = new JDTextField();

            doc = (PlainDocument) ((JDTextField) input[0]).getDocument();
            doc.addDocumentListener(this);
            input[0].setEnabled(configEntry.isEnabled());
            ((JDTextField) input[0]).setHorizontalAlignment(SwingConstants.RIGHT);

            break;
        case ConfigContainer.TYPE_TEXTAREA:
            decoration = new JLabel(configEntry.getLabel());
            new JScrollPane(input[0] = new JDTextArea());
            input[0].setEnabled(configEntry.isEnabled());
            doc = (PlainDocument) ((JDTextArea) input[0]).getDocument();
            doc.addDocumentListener(this);

            break;
        case ConfigContainer.TYPE_CHECKBOX:

            input[0] = new JCheckBox();
            input[0].setEnabled(configEntry.isEnabled());
            ((JCheckBox) input[0]).addChangeListener(this);

            decoration = new JLabel(configEntry.getLabel());
            break;
        case ConfigContainer.TYPE_BROWSEFILE:
            // logger.info("ADD Browser");

            if (configEntry.getLabel().trim().length() > 0) new JLabel(configEntry.getLabel());
            input[0] = new BrowseFile();
            ((BrowseFile) input[0]).setEnabled(configEntry.isEnabled());

            ((BrowseFile) input[0]).setEditable(true);

            break;
        case ConfigContainer.TYPE_BROWSEFOLDER:
            // logger.info("ADD BrowserFolder");

            if (configEntry.getLabel().trim().length() > 0) decoration = new JLabel(configEntry.getLabel());
            input[0] = new BrowseFile();

            ((BrowseFile) input[0]).setEditable(true);
            ((BrowseFile) input[0]).setEnabled(configEntry.isEnabled());
            ((BrowseFile) input[0]).setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);

            break;
        case ConfigContainer.TYPE_SPINNER:
            // logger.info("ADD Spinner");
            decoration = new JLabel(configEntry.getLabel());
            input[0] = new JSpinner(new SpinnerNumberModel(configEntry.getStart(), configEntry.getStart(), configEntry.getEnd(), configEntry.getStep()));
            input[0].setEnabled(configEntry.isEnabled());
            ((JSpinner) input[0]).addChangeListener(this);
            // ((JSpinner)input[0])

            break;
        case ConfigContainer.TYPE_BUTTON:
            // //logger.info("ADD Button");
            input[0] = new JButton(configEntry.getLabel());
            ((JButton) input[0]).addActionListener(this);
            ((JButton) input[0]).addActionListener(configEntry.getActionListener());
            input[0].setEnabled(configEntry.isEnabled());

            break;
        case ConfigContainer.TYPE_COMBOBOX:
        case ConfigContainer.TYPE_COMBOBOX_INDEX:
            decoration = new JLabel(configEntry.getLabel());
            input[0] = new JComboBox(configEntry.getList());
            ((JComboBox) input[0]).addActionListener(this);
            for (int i = 0; i < configEntry.getList().length; i++) {

                if (configEntry.getList()[i].equals(configEntry.getPropertyInstance().getProperty(configEntry.getPropertyName()))) {
                    ((JComboBox) input[0]).setSelectedIndex(i);

                    break;
                }
            }
            input[0].setEnabled(configEntry.isEnabled());

            break;
        case ConfigContainer.TYPE_RADIOFIELD:

            input = new JComponent[configEntry.getList().length];
            JRadioButton radio;

            ButtonGroup group = new ButtonGroup();

            for (int i = 0; i < configEntry.getList().length; i++) {
                radio = new JRadioButton(configEntry.getList()[i].toString());
                ((JRadioButton) input[0]).addActionListener(this);
                radio.setActionCommand(configEntry.getList()[i].toString());
                input[i] = radio;
                input[i].setEnabled(configEntry.isEnabled());

                group.add(radio);

                Object p = configEntry.getPropertyInstance().getProperty(configEntry.getPropertyName());
                if (p == null) {
                    p = "";
                }

                if (configEntry.getList()[i].toString().equals(p.toString())) {
                    radio.setSelected(true);

                }

            }
            break;
        case ConfigContainer.TYPE_PREMIUMPANEL:

            input[0] = new PremiumPanel(this);
            input[0].setEnabled(configEntry.isEnabled());

        case ConfigContainer.TYPE_LABEL:
            input = new JComponent[0];
            decoration = new JLabel(configEntry.getLabel());
            break;
        case ConfigContainer.TYPE_SEPARATOR:
            // //logger.info("ADD Seperator");
            input = new JComponent[0];
            decoration = new JSeparator(SwingConstants.HORIZONTAL);

            break;

        }
        this.firePropertyChange(getConfigEntry().getPropertyName(), null, getText());
    }

    private void firePropertyChange(String propertyName, Object object, Object text) {
        // TODO Auto-generated method stub

    }

    private void addPropertyChangeListener(ConfigEntry cfg) {
        // TODO Auto-generated method stub

    }

    public JComponent[] getInput() {
        return input;
    }

    public void actionPerformed(ActionEvent e) {
        getConfigEntry().valueChanged(getText());

    }

    public void changedUpdate(DocumentEvent e) {
        getConfigEntry().valueChanged(getText());

    }

    public ConfigEntry getConfigEntry() {
        return configEntry;
    }

    /**
     * Gibt den zusstand der Inputkomponente zurück
     * 
     * @return
     */
    public Object getText() {
        // //logger.info(configEntry.getType()+"_2");
        switch (configEntry.getType()) {
        case ConfigContainer.TYPE_LINK:
            return ((JLinkButton) input[0]).getLinkURL().toString();
        case ConfigContainer.TYPE_PASSWORDFIELD:
            return new String(((JPasswordField) input[0]).getPassword());
        case ConfigContainer.TYPE_TEXTFIELD:
            return ((JDTextField) input[0]).getText();
        case ConfigContainer.TYPE_TEXTAREA:
            return ((JDTextArea) input[0]).getText();
        case ConfigContainer.TYPE_CHECKBOX:
            return ((JCheckBox) input[0]).isSelected();
        case ConfigContainer.TYPE_PREMIUMPANEL:
            return ((PremiumPanel) input[0]).getAccounts();
        case ConfigContainer.TYPE_BUTTON:
            return null;
        case ConfigContainer.TYPE_COMBOBOX:
            return ((JComboBox) input[0]).getSelectedItem();
        case ConfigContainer.TYPE_COMBOBOX_INDEX:
            return ((JComboBox) input[0]).getSelectedIndex();
        case ConfigContainer.TYPE_LABEL:
            return null;
        case ConfigContainer.TYPE_RADIOFIELD:
            JRadioButton radio;
            for (JComponent element : input) {

                radio = (JRadioButton) element;

                if (radio.getSelectedObjects() != null && radio.getSelectedObjects()[0] != null) { return radio.getSelectedObjects()[0]; }
            }
            return null;
        case ConfigContainer.TYPE_SEPARATOR:
            return null;
        case ConfigContainer.TYPE_BROWSEFOLDER:
        case ConfigContainer.TYPE_BROWSEFILE:
            return ((BrowseFile) input[0]).getText();
        case ConfigContainer.TYPE_SPINNER:
            return ((JSpinner) input[0]).getValue();
        }

        return null;
    }

    public void insertUpdate(DocumentEvent e) {
        getConfigEntry().valueChanged(getText());
    }

    public void propertyChange(PropertyChangeEvent evt) {

        if (input[0] == null) { return; }
        // logger.info("New Value "+evt.getNewValue());
        if (getConfigEntry().isConditionalEnabled(evt)) {
            input[0].setEnabled(true);
            for (JComponent i : input) {
                if (i != null) i.setEnabled(true);
            }

            if (decoration != null) decoration.setEnabled(true);

        } else {
            for (JComponent i : input) {
                if (i != null) i.setEnabled(false);
            }
            if (decoration != null) decoration.setEnabled(true);
        }
    }

    public JComponent getDecoration() {
      
        return decoration;
    }

    public void removeUpdate(DocumentEvent e) {
        getConfigEntry().valueChanged(getText());
    }

    public void setConfigEntry(ConfigEntry configEntry) {
        this.configEntry = configEntry;
    }

    /**
     * Setz daten ind ei INput Komponente
     * 
     * @param text
     */
    public void setData(Object text) {
        if (text == null && configEntry.getDefaultValue() != null) {
            text = configEntry.getDefaultValue();
        }
        // //logger.info(configEntry.getDefaultValue()+" - "+text + "
        // "+input.length+" -
        // "+input[0]);
        switch (configEntry.getType()) {
        case ConfigContainer.TYPE_LINK:
            try {
                ((JLinkButton) input[0]).setLinkURL(new URL(text == null ? "" : text.toString()));
            } catch (MalformedURLException e1) {

                e1.printStackTrace();
            }
            break;
        case ConfigContainer.TYPE_PASSWORDFIELD:
            ((JPasswordField) input[0]).setText(text == null ? "" : text.toString());
            break;
        case ConfigContainer.TYPE_TEXTFIELD:
            ((JDTextField) input[0]).setText(text == null ? "" : text.toString());
            break;
        case ConfigContainer.TYPE_TEXTAREA:
            ((JDTextArea) input[0]).setText(text == null ? "" : text.toString());
            break;
        case ConfigContainer.TYPE_PREMIUMPANEL:
            ((PremiumPanel) input[0]).setAccounts(text);
            break;
        case ConfigContainer.TYPE_CHECKBOX:
            if (text == null) {
                text = false;
            }
            try {
                ((JCheckBox) input[0]).setSelected((Boolean) text);
            } catch (Exception e) {
                logger.severe("Falcher Wert: " + text);
                ((JCheckBox) input[0]).setSelected(false);
            }
            break;
        case ConfigContainer.TYPE_BUTTON:
            break;
        case ConfigContainer.TYPE_COMBOBOX:
            ((JComboBox) input[0]).setSelectedItem(text);
            break;
        case ConfigContainer.TYPE_COMBOBOX_INDEX:
            if (text instanceof Integer) {
                ((JComboBox) input[0]).setSelectedIndex((Integer) text);
            } else {
                ((JComboBox) input[0]).setSelectedItem(text);
            }
            break;
        case ConfigContainer.TYPE_LABEL:
            break;
        case ConfigContainer.TYPE_BROWSEFOLDER:
        case ConfigContainer.TYPE_BROWSEFILE:
            ((BrowseFile) input[0]).setText(text == null ? "" : text.toString());
            break;
        case ConfigContainer.TYPE_SPINNER:
            int value = text instanceof Integer ? (Integer) text : Integer.parseInt(text.toString());
            try {

                value = Math.min((Integer) ((SpinnerNumberModel) ((JSpinner) input[0]).getModel()).getMaximum(), value);
                value = Math.max((Integer) ((SpinnerNumberModel) ((JSpinner) input[0]).getModel()).getMinimum(), value);
                ((JSpinner) input[0]).setModel(new SpinnerNumberModel(value, configEntry.getStart(), configEntry.getEnd(), configEntry.getStep()));

            } catch (Exception e) {
                e.printStackTrace();
            }
            break;
        case ConfigContainer.TYPE_RADIOFIELD:
            for (int i = 0; i < configEntry.getList().length; i++) {
                JRadioButton radio = (JRadioButton) input[i];
                if (radio.getActionCommand().equals(text)) {
                    radio.setSelected(true);
                } else {
                    radio.setSelected(false);
                }
            }

        case ConfigContainer.TYPE_SEPARATOR:

            break;
        }
        getConfigEntry().valueChanged(getText());
    }

    public void stateChanged(ChangeEvent e) {
        getConfigEntry().valueChanged(getText());
    }

}
