package jd;

import java.awt.Toolkit;
import java.io.File;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;

import jd.config.Configuration;
import jd.controlling.JDController;
import jd.controlling.interaction.Interaction;
import jd.gui.UIInterface;
import jd.gui.skins.simple.SimpleGUI;
import jd.plugins.Plugin;
import jd.plugins.PluginForDecrypt;
import jd.plugins.PluginForHost;
import jd.plugins.PluginForSearch;
import jd.utils.JDUtilities;

/**
 * $Revision: 242 $ $Author: coalado $ Start der Applikation
 * 
 * @author astaldo
 */


// TODO Links speichern / laden / editieren(nochmal laden, Passwort merken)
// TODO Klänge wiedergeben
// TODO HTTPPost Klasse untersuchen
// TODO TelnetReconnect (später)
// TODO WebInterface
// TODO GUI Auswahl
public class Main {
    /**
     * @param args
     */
    public static void main(String args[]) {
        Main main = new Main();
        main.go();
    }

    private void go() {
        Logger logger = Plugin.getLogger();
        loadImages();

        File fileInput = null;

        try {

            fileInput = JDUtilities.getResourceFile(JDUtilities.CONFIG_PATH);
        }
        catch (RuntimeException e) {
            e.printStackTrace();
        }
        try {
            logger.finer("Load config from: " + fileInput + " (" + JDUtilities.CONFIG_PATH + ")");
            if (fileInput != null && fileInput.exists()) {

                Object obj = JDUtilities.loadObject(null, fileInput, Configuration.saveAsXML);
                if (obj instanceof Configuration) {
                    Configuration configuration = (Configuration) obj;
                    JDUtilities.setConfiguration(configuration);
                    Plugin.getLogger().setLevel((Level) configuration.getProperty(Configuration.PARAM_LOGGER_LEVEL, Level.FINER));
                }
                else {
                    logger.severe("Configuration error: " + obj);
                    logger.severe("Konfigurationskonflikt. Lade Default einstellungen");
                    JDUtilities.getConfiguration().setDefaultValues();
                }

            }
            else {
                logger.warning("no configuration loaded");
                logger.severe("Konfigurationskonflikt. Lade Default einstellungen");
                JDUtilities.getConfiguration().setDefaultValues();
            }
        }
        catch (Exception e) {
            logger.severe("Konfigurationskonflikt. Lade Default einstellungen");
            JDUtilities.getConfiguration().setDefaultValues();
        }

        logger.info("Lade Plugins");
        JDUtilities.loadPlugins();
        logger.info("Lade GUI");
        UIInterface uiInterface = new SimpleGUI();

        logger.info("Erstelle Controller");
        JDController controller = new JDController();
        controller.setUiInterface(uiInterface);
        logger.info("Lade Queue");
        if(!controller.initDownloadLinks()){
           
      
          File links = JDUtilities.getResourceFile("links.dat");
          if (links.exists()) {
              File newFile = new File(links.getAbsolutePath() + ".bup");
              newFile.delete();
              links.renameTo(newFile);
              uiInterface.showMessageDialog("Linkliste inkompatibel. \r\nBackup angelegt: "+newFile+" Liste geleert!");

          }
         
        }
   
        logger.info("Registriere Plugins");
        Iterator<PluginForHost> iteratorHost = JDUtilities.getPluginsForHost().iterator();
        while (iteratorHost.hasNext()) {
            iteratorHost.next().addPluginListener(controller);
        }
        Iterator<PluginForDecrypt> iteratorDecrypt = JDUtilities.getPluginsForDecrypt().iterator();
        while (iteratorDecrypt.hasNext()) {
            iteratorDecrypt.next().addPluginListener(controller);
        }
        Iterator<PluginForSearch> iteratorSearch = JDUtilities.getPluginsForSearch().iterator();
        while (iteratorSearch.hasNext()) {
            iteratorSearch.next().addPluginListener(controller);
        }

        Interaction.handleInteraction(Interaction.INTERACTION_APPSTART, false);
    }

    /**
     * Die Bilder werden aus der JAR Datei nachgeladen
     */
    private void loadImages() {
        ClassLoader cl = getClass().getClassLoader();
        Toolkit toolkit = Toolkit.getDefaultToolkit();
        JDUtilities.addImage("add", toolkit.getImage(cl.getResource("img/add.png")));
        JDUtilities.addImage("configuration", toolkit.getImage(cl.getResource("img/configuration.png")));
        JDUtilities.addImage("delete", toolkit.getImage(cl.getResource("img/delete.png")));
        JDUtilities.addImage("dnd", toolkit.getImage(cl.getResource("img/dnd.png")));
        JDUtilities.addImage("dnd_big", toolkit.getImage(cl.getResource("img/dnd_big.png")));
        JDUtilities.addImage("dnd_big_filled", toolkit.getImage(cl.getResource("img/dnd_big_filled.png")));
        JDUtilities.addImage("down", toolkit.getImage(cl.getResource("img/down.png")));
        JDUtilities.addImage("exit", toolkit.getImage(cl.getResource("img/shutdown.png")));
        JDUtilities.addImage("led_empty", toolkit.getImage(cl.getResource("img/led_empty.gif")));
        JDUtilities.addImage("led_green", toolkit.getImage(cl.getResource("img/led_green.gif")));
        JDUtilities.addImage("load", toolkit.getImage(cl.getResource("img/load.png")));
        JDUtilities.addImage("log", toolkit.getImage(cl.getResource("img/log.png")));
        JDUtilities.addImage("mind", toolkit.getImage(cl.getResource("img/mind.png")));
        JDUtilities.addImage("reconnect", toolkit.getImage(cl.getResource("img/reconnect.png")));
        JDUtilities.addImage("save", toolkit.getImage(cl.getResource("img/save.png")));
        JDUtilities.addImage("start", toolkit.getImage(cl.getResource("img/start.png")));
        JDUtilities.addImage("stop", toolkit.getImage(cl.getResource("img/stop.png")));
        JDUtilities.addImage("up", toolkit.getImage(cl.getResource("img/up.png")));
        JDUtilities.addImage("update", toolkit.getImage(cl.getResource("img/update.png")));
        JDUtilities.addImage("search", toolkit.getImage(cl.getResource("img/search.png")));
    }
}
