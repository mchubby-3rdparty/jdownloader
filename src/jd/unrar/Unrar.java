package jd.unrar;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Unrar {
    public Config config;
    private HashMap<File, String> files;
    public HashMap<String, Integer> passwordlist;
    public String standardPassword = null;
    private static final String allOk = "(?s).*[\\s]+All OK[\\s].*";
    /**
     * Konstruktor zum entpacken aller Rar-Archive im angegebenen Ordner,
     * Passwoerter werden aus der PasswortListe entnommen fals vorhanden
     * 
     * @param path
     */
    public Unrar(String path) {
        this(new File(path));
    }
    /**
     * Konstruktor zum entpacken aller Rar-Archive im angegebenen Ordner,
     * Passwoerter werden aus der PasswortListe entnommen fals vorhanden
     * 
     * @param path
     */
    public Unrar(File path) {
        this(path.listFiles(), null);
    }
    /**
     * Konstruktor zum entpacken einer bestimmten Datei wenn das Passwort aus
     * der PasswortListe geholt werden soll oder kein Passwort benoetigt wird
     * einfach null als Password setzen
     * 
     * @param file
     * @param password
     */
    public Unrar(String file, String password) {
        this(new File[]{new File(file)}, password);
    }
    /**
     * Konstruktor zum entpacken einer bestimmten Datei wenn das Passwort aus
     * der PasswortListe geholt werden soll oder kein Passwort benoetigt wird
     * einfach null als Password setzen
     * 
     * @param file
     * @param password
     */
    public Unrar(File file, String password) {
        this(new File[]{file}, password);
    }
    /**
     * Konstruktor zum entpacken bestimmter Dateien wenn die Passwoerter aus der
     * PasswortListe geholt werden sollen oder keine Passwoerter benoetigt
     * werden einfach null als Password setzen
     * 
     * @param files
     * @param password
     */
    @SuppressWarnings("unchecked")
    public Unrar(File[] files, String password) {
        HashMap<File, String> filelist = new HashMap<File, String>();
        for (int i = 0; i < files.length; i++) {
            filelist.put(files[i], password);
        }
        this.files = filelist;
        loadObjects();
    }
    /**
     * Konstruktor zum entpacken bestimmter Dateien, Passwoerter werden aus
     * derconfigfile PasswortListe entnommen fals vorhanden
     * 
     * @param files
     * @param password
     */
    public Unrar(File[] files) {
        this(files, null);
    }
    /**
     * Konstruktor zum entpacken bestimmter Dateien mit verschiedenen
     * Passwoertern in der HashMap steht fuer key die Datei und fuer value das
     * Passwort, wenn das Passwort aus der PasswortListe geholt werden soll oder
     * kein Passwort benoetigt wird einfach null als key bzw. Password setzen
     * 
     * @param files
     */
    public Unrar(HashMap<File, String> files) {
        this.files = files;
        loadObjects();
    }
    @SuppressWarnings("unchecked")
    private void loadObjects() {
        File configfile = new File(Config.unraritHome, "config.cfg");
        if (configfile.exists()) {
            this.config = (Config) Utilities.loadObject(configfile, false);
            if (config.passwordList.exists())
                this.passwordlist = (HashMap<String, Integer>) Utilities.loadObject(config.passwordList, true);
        } else {
            this.config = new Config();
            if (!Config.unraritHome.exists())
                Config.unraritHome.mkdir();
            saveConfig();
            if (config.passwordList.exists())
                this.passwordlist = (HashMap<String, Integer>) Utilities.loadObject(config.passwordList, true);
            else {
                this.passwordlist = new HashMap<String, Integer>();
                savePasswordList();
            }
        }
    }
    private void savePasswordList() {
        Utilities.saveObject(this.passwordlist, config.passwordList, true);
    }
    public void saveConfig() {
        Utilities.saveObject(this.config, new File(Config.unraritHome, "config.cfg"), false);
    }

    /**
     * der unrar command kann hier fest gesetzt werden fals nicht gesetzt
     * versucht Unrarit das Unrarprogramm an Standartorten zu finden der unrar
     * command kann z.b.: durch textfeld.settext(Unrarit.autoGetUnrarCommand());
     * in ein Textfeld gelesen werden
     */
    public void setUnrarCommand(String unrarCommand) {
        config.unrar = unrarCommand;
        saveConfig();
    }
    public String getUnrarCommand() {
        if (config.unrar == null) {
            config.unrar = autoGetUnrarCommand();
            if (config.unrar == null)
                logger("Can't find unrar command");
            saveConfig();
            return config.unrar;
        } else {
            return config.unrar;
        }

    }
    /**
     * Versucht den Programmpfad von unrar bzw unrar.exe zu finden
     * 
     * @return
     */
    public static String autoGetUnrarCommand() {

        String programm = null;
        String OS = System.getProperty("os.name").toLowerCase();
        if ((OS.indexOf("nt") > -1) || (OS.indexOf("windows") > -1)) {
            try {
                File unrarexe = new File(new File(System.getenv("ProgramFiles"), "Winrar"), "unrar.exe");
                if (unrarexe.isFile())
                    programm = unrarexe.getAbsolutePath();
            } catch (Throwable e) {
            }
        } else {
            try {
                String[] charset = System.getenv("PATH").split(":");
                String Programmname = "unrar";
                for (int i = 0; i < charset.length; i++) {
                    File fi = new File(charset[i], Programmname);
                    if (fi.isFile()) {
                        programm = fi.getAbsolutePath();
                        break;
                    }
                }
            } catch (Throwable e) {
            }
        }
        return programm;
    }
    private void logger(Object message) {
        System.out.println("" + message);
    }

    public void addToPasswordlist(String password) {
        if (!passwordlist.containsKey(password)) {
            passwordlist.put(password, 1);
            passwordlist = (HashMap<String, Integer>) sortByValue(passwordlist);
            savePasswordList();
        }
    }
    public void addToPasswordlist(File passwords) {
        try {
            String thisLine;
            FileInputStream fin = new FileInputStream((File) passwords);
            BufferedReader myInput = new BufferedReader(new InputStreamReader(fin));
            while ((thisLine = myInput.readLine()) != null) {
                if (!passwordlist.containsKey(thisLine))
                    passwordlist.put(thisLine, 1);
            }
        } catch (IOException e) {
            logger(e);
        }
        passwordlist = (HashMap<String, Integer>) sortByValue(passwordlist);
        savePasswordList();
    }
    @SuppressWarnings("unchecked")
    private static Map sortByValue(Map map) {
        List list = new LinkedList(map.entrySet());
        Collections.sort(list, new Comparator() {
            public int compare(Object o1, Object o2) {
                return ((Comparable) ((Map.Entry) (o2)).getValue()).compareTo(((Map.Entry) (o1)).getValue());
            }
        });
        // logger.info(list);
        Map result = new LinkedHashMap();
        for (Iterator it = list.iterator(); it.hasNext();) {
            Map.Entry entry = (Map.Entry) it.next();
            result.put(entry.getKey(), entry.getValue());
        }
        return result;
    }
    private void reorderPasswordList(String password) {
        passwordlist.put(password, passwordlist.get(password) + 1);
        passwordlist = (HashMap<String, Integer>) sortByValue(passwordlist);
    }
    private void passwordlist(File file, String password) {
        logger("Extracting " + file.getName());
        if (password != null) {
            int z = checkarchiv(file, password);
            if (z > 1) {
                if (z == 2)
                    logger("Password incorect");
                return;
            }
            String str = execprozess(file, password);
            if (str.matches(allOk)) {
                logger("All OK");
                addToPasswordlist(password);
                return;
            } else {
                logger(str);
                return;
            }
        } else if (standardPassword != null) {
            int z = checkarchiv(file, standardPassword);
            if (z > 1) {
                if (z == 2)
                    logger("Password incorect");
                return;
            }
            if (execprozess(file, standardPassword).matches(allOk)) {
                logger("All OK");
                addToPasswordlist(standardPassword);
                return;
            } else {
                logger("Can't extract " + file.getName());
                return;
            }
        } else {

            int z = checkarchiv(file, "");
            if (z == 3)
                return;
            if (z == 1) {
                if (execprozess(file, "").matches(allOk))
                    logger("All OK");
                else
                    logger("Can't extract " + file.getName());
                return;
            } else {
                long time = -System.currentTimeMillis();
                for (Map.Entry<String, Integer> entry : passwordlist.entrySet()) {
                    
                    password = entry.getKey();
                    z = checkarchiv(file, password);
                    if (z == 3)
                        return;
                    if (z == 1) {
                        logger("Password "+password+" found in " + (time + System.currentTimeMillis()) / 1000 + " sec");
                        if (execprozess(file, password).matches(allOk))
                        {
                            logger("All OK");
                            reorderPasswordList(password);
                        }
                        else
                            logger("Can't extract " + file.getName());
                        return;
                    }
                }
                logger("Can't extract " + file.getName() + "  (it seems like the password isn't in the list?)");

            }
        }
    }
    private String startInputListener(Process p, File parent) {
        InputStreamReader ipsr = new InputStreamReader(p.getErrorStream());
        StringBuffer buff = new StringBuffer();
        char seperator = System.getProperty("line.separator").charAt(0);
        try {
            int temp;
            do {
                temp = ipsr.read();
                buff.append((char) temp);
                if (((char) temp) == seperator && buff.indexOf(" (password incorrect ?)") != -1) {
                    p.destroy();
                }
            } while ((temp != -1));
        } catch (Exception e) {
            Pattern pattern = Pattern.compile("Extracting  (.*)", Pattern.CASE_INSENSITIVE);
            Matcher matcher = pattern.matcher(buff);
            while (matcher.find()) {
                File delfile = new File(parent, matcher.group(1));
                if (delfile.isFile() && delfile.length() == 0)
                    delfile.delete();

            }
        }
        return buff.toString();
    }
    private void closeEvent() {
        savePasswordList();
    }

    private int checkarchiv(File file, String password) {
        try {
            String ext = ((password != "") ? " -p" + password : " -p-") + " -ierr t -sl"+config.maxFilesize+" ";
            Process p = Runtime.getRuntime().exec(config.unrar + ext + file.getName(), null, file.getParentFile());
            String str = startInputListener(p, file.getParentFile());
            if (str.indexOf(" (password incorrect ?)") != -1)
                return 2;
            else if (str.indexOf("Cannot find volume") != -1) {
                Pattern pattern = Pattern.compile("(Cannot find volume .*)", Pattern.CASE_INSENSITIVE);
                Matcher matcher = pattern.matcher(str);
                while (matcher.find()) {
                    logger(matcher.group());
                }
                return 3;
            } else if(str.indexOf("No files to extract") != -1 || str.matches(allOk))
                return 1;
            else
            {
                logger("unknown error");
                logger(str);
                return 3;
            }

        } catch (IOException e) {
        }
        return 3;

    }
    private String execprozess(File file, String password) {

        try {
            String ext = ((password != "") ? " -p" + password : " -p-") + ((config.overwriteFiles) ? " -o+" : " -o-") + " -ierr x ";
            Process p = Runtime.getRuntime().exec(config.unrar + ext + file.getName(), null, file.getParentFile());
            String str = startInputListener(p, file.getParentFile());
            if (config.autoDelete) {
                if (str.matches(allOk)) {
                    Pattern pattern = Pattern.compile("Extracting from (.*)");
                    Matcher matcher = pattern.matcher(str);
                    while (matcher.find()) {
                        File delfile = new File(file.getParentFile(), matcher.group(1));
                        if (!delfile.isFile() || !delfile.delete())
                            logger("Can't delete " + delfile.getName());
                    }
                }
            }
            return str;

        } catch (IOException e) {
        }
        return null;

    }
    /**
     * Startet den Entpackungsprozess
     */
    public void unrar() {
        config.unrar = getUnrarCommand();
        if (config.unrar == null) {
            return;
        }
        for (Map.Entry<File, String> entry : files.entrySet()) {
            File file = entry.getKey();
            if (file.isFile()) {
                String name = file.getName();
                if (name.matches(".*part[0]*[1].rar$"))
                    passwordlist(entry.getKey(), entry.getValue());
                else if (!name.matches(".*part[0-9]+.rar$") && name.matches(".*rar$"))
                    passwordlist(entry.getKey(), entry.getValue());
            } else
                logger(file.getName() + " isn't a file");

        }
        closeEvent();

    }

}
