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

package jd.gui.skins.jdgui.settings.panels.downloadandnetwork;

import javax.swing.JTabbedPane;

import jd.config.ConfigContainer;
import jd.config.ConfigEntry;
import jd.config.ConfigGroup;
import jd.config.Configuration;
import jd.config.SubConfiguration;
import jd.gui.skins.jdgui.GUIUtils;
import jd.gui.skins.jdgui.JDGuiConstants;
import jd.gui.skins.jdgui.settings.ConfigPanel;
import jd.gui.skins.jdgui.settings.GUIConfigEntry;
import jd.utils.JDTheme;
import jd.utils.JDUtilities;
import jd.utils.locale.JDL;

public class General extends ConfigPanel {
    private static final String JDL_PREFIX = "jd.gui.skins.jdgui.settings.panels.downloadandnetwork.General.";

      public String getBreadcrum() {     return JDL.L(this.getClass().getName()+".breadcrum", this.getClass().getSimpleName()); }   public static String getTitle(){
        return JDL.L(JDL_PREFIX + "download.title", "Download & Network");
     }
    private static final long serialVersionUID = 3383448498625377495L;

    private Configuration configuration;

    private SubConfiguration config;

    public General(Configuration configuration) {
        super();
        this.configuration = configuration;
        config = SubConfiguration.getConfig("DOWNLOAD");
        initPanel();
        load();
    }

    private ConfigContainer setupContainer() {
        ConfigContainer container = new ConfigContainer();
        ConfigEntry ce;
        ConfigEntry conditionEntry;

        /* DESTINATION PATH */
        container.setGroup(new ConfigGroup(JDL.L("gui.config.general.downloadDirectory", "Download directory"), JDTheme.II("gui.images.package_opened", 32, 32)));
        container.addEntry(ce = new ConfigEntry(ConfigContainer.TYPE_BROWSEFOLDER, JDUtilities.getConfiguration(), Configuration.PARAM_DOWNLOAD_DIRECTORY, ""));

        ce.setDefaultValue(JDUtilities.getResourceFile("downloads").getAbsolutePath());

        container.addEntry(ce = new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, JDUtilities.getConfiguration(), Configuration.PARAM_USE_PACKETNAME_AS_SUBFOLDER, JDL
                .L("gui.config.general.createSubFolders", "Wenn möglich Unterordner mit Paketname erstellen")));
        ce.setDefaultValue(false);

        container.addEntry(ce = new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, JDUtilities.getConfiguration(), Configuration.PARAM_CREATE_SUBFOLDER_BEFORE_DOWNLOAD, JDL
                .L("gui.config.general.createSubFoldersbefore", "Create sub-folders after adding links")));
        ce.setDefaultValue(false);
        ce.setEnabled(JDUtilities.getConfiguration().getBooleanProperty(Configuration.PARAM_USE_PACKETNAME_AS_SUBFOLDER, false));
        /* control */

        container.setGroup(new ConfigGroup(JDL.L("gui.config.download.download.tab", "Downloadsteuerung"), JDTheme.II("gui.images.downloadorder", 32, 32)));

        container.addEntry(ce = new ConfigEntry(ConfigContainer.TYPE_SPINNER, config, Configuration.PARAM_DOWNLOAD_MAX_SIMULTAN, JDL.L("gui.config.download.simultan_downloads",
                                                                                                                                       "Maximale gleichzeitige Downloads"), 1, 20));
        ce.setDefaultValue(2);
        ce.setStep(1);

        container.addEntry(ce = new ConfigEntry(ConfigContainer.TYPE_SPINNER, config, Configuration.PARAM_DOWNLOAD_MAX_SIMULTAN_PER_HOST, JDL
                .L("gui.config.download.simultan_downloads_per_host", "Maximum of simultaneous downloads per host (0 = no limit)"), 0, 20));
        ce.setDefaultValue(0);
        ce.setStep(1);

        container.addEntry(conditionEntry = new ConfigEntry(ConfigContainer.TYPE_SPINNER, config, Configuration.PARAM_DOWNLOAD_MAX_CHUNKS, JDL.L("gui.config.download.chunks",
                                                                                                                                                 "Anzahl der Verbindungen/Datei(Chunkload)"), 1, 20));
        conditionEntry.setDefaultValue(2);
        conditionEntry.setStep(1);

        // container.addEntry(ce = new ConfigEntry(ConfigContainer.TYPE_SPINNER,
        // config, PluginForHost.PARAM_MAX_RETRIES,
        // JDLocale.L("gui.config.download.retries",
        // "Max. Neuversuche bei vorrübergehenden Hosterproblemen"), 0, 20));
        // ce.setDefaultValue(3);
        // ce.setStep(1);

        String[] removeDownloads = new String[] {
                JDL.L("gui.config.general.toDoWithDownloads.immediate", "immediately"),
                JDL.L("gui.config.general.toDoWithDownloads.atStart", "at startup"),
                JDL.L("gui.config.general.toDoWithDownloads.packageReady", "when package is ready"),
                JDL.L("gui.config.general.toDoWithDownloads.never", "never")
        };
        container.addEntry(ce = new ConfigEntry(ConfigContainer.TYPE_COMBOBOX_INDEX, JDUtilities.getConfiguration(), Configuration.PARAM_FINISHED_DOWNLOADS_ACTION, removeDownloads, JDL
                .L("gui.config.general.toDoWithDownloads", "Remove finished downloads ...")));
        ce.setDefaultValue(removeDownloads[3]);

        String[] fileExists = new String[] {
                JDL.L("system.download.triggerfileexists.overwrite", "Datei überschreiben"), JDL.L("system.download.triggerfileexists.skip", "Link überspringen")
        };
        container.addEntry(ce = new ConfigEntry(ConfigContainer.TYPE_COMBOBOX_INDEX, config, Configuration.PARAM_FILE_EXISTS, fileExists, JDL.L("system.download.triggerfileexists",
                                                                                                                                                "Wenn eine Datei schon vorhanden ist:")));
        ce.setDefaultValue(fileExists[1]);

        container.addEntry(ce = new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, SubConfiguration.getConfig(GUIUtils.getConfig()), JDGuiConstants.PARAM_START_DOWNLOADS_AFTER_START, JDL
                .L("gui.config.download.startDownloadsOnStartUp", "Download beim Programmstart beginnen")));
        ce.setDefaultValue(false);

        container.addEntry(ce = new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, SubConfiguration.getConfig("DOWNLOAD"), "PARAM_DOWNLOAD_AUTORESUME_ON_RECONNECT", JDL
                .L("gui.config.download.autoresume", "Let Reconnects interrupt resumeable downloads")).setDefaultValue(true));

        container.addEntry(ce = new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, SubConfiguration.getConfig("DOWNLOAD"), "PARAM_DOWNLOAD_PREFER_RECONNECT", JDL
                .L("gui.config.download.preferreconnect", "Do not start new links if reconnect requested")).setDefaultValue(true));

        container.addEntry(conditionEntry = new ConfigEntry(ConfigContainer.TYPE_SPINNER, SubConfiguration.getConfig("DOWNLOAD"), Configuration.PARAM_DOWNLOAD_PAUSE_SPEED, JDL
                .L("gui.config.download.pausespeed", "Speed of pause in kb/s"), 10, 500));
        conditionEntry.setDefaultValue(10);
        conditionEntry.setStep(10);
        return container;
    }

    @Override
    public void initPanel() {
        ConfigContainer container = setupContainer();

        for (ConfigEntry cfgEntry : container.getEntries()) {
            GUIConfigEntry ce = new GUIConfigEntry(cfgEntry);
            if (ce != null) addGUIConfigEntry(ce);
        }

        JTabbedPane tabbed = new JTabbedPane();
        tabbed.add(getBreadcrum(), panel);

        this.add(tabbed);
    }

    @Override
    public void load() {
        loadConfigEntries();
    }

    @Override
    public void save() {
        saveConfigEntries();

    }
}
