package jd.plugins.decrypter;

import java.util.ArrayList;

import jd.PluginWrapper;
import jd.controlling.ProgressController;
import jd.parser.Regex;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "oboom.com" }, urls = { "https?://(www\\.)?oboom\\.com/(#share/[a-f0-9\\-]+|#?folder/[A-Z0-9]+)" }, flags = { 0 })
public class OBoomCom extends antiDDoSForDecrypt {

    private final String APPID  = "43340D9C23";
    private final String wwwURL = "https://www.oboom.com/1.0/";
    private final String apiURL = "https://api.oboom.com/1/";

    public OBoomCom(PluginWrapper wrapper) {
        super(wrapper);
    }

    @Override
    public ArrayList<DownloadLink> decryptIt(CryptedLink parameter, ProgressController progress) throws Exception {
        final ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        getPage(wwwURL + "guestsession?source=" + APPID);
        final String uid = new Regex(parameter.toString(), "(share|folder)/([A-Z0-9\\-]+)").getMatch(1);
        String guestSession = br.getRegex("200,.*?\"(.*?)\"").getMatch(0);
        if (guestSession == null || uid == null) {
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        String name = null;
        if (parameter.toString().contains("share/")) {
            getPage(wwwURL + guestSession + "/share?share=" + uid);
            String files = br.getRegex("\"files\":\\[(.*?)\\]").getMatch(0);
            name = br.getRegex("\"name\":\"(.*?)\"").getMatch(0);
            if (name != null && "undefined".equals(name)) {
                name = null;
            }
            if (files != null) {
                String fileIDs[] = new Regex(files, "\"(.*?)\"").getColumn(0);
                for (String fileID : fileIDs) {
                    decryptedLinks.add(createDownloadlink("https://www.oboom.com/#" + fileID));
                }
            }
        } else if (parameter.toString().contains("folder/")) {
            getPage(apiURL + "ls?item=" + uid + "&token=" + guestSession);
            if (br.getHttpConnection().getResponseCode() == 404) {
                final DownloadLink offline = createDownloadlink("directhttp://" + parameter);
                offline.setFinalFileName(uid);
                offline.setAvailable(false);
                offline.setProperty("offline", true);
                decryptedLinks.add(offline);
                return decryptedLinks;
            }
            name = br.getRegex("\"name\":\"(.*?)\"").getMatch(0);
            final String jsontext = br.getRegex(",\\[(.+)").getMatch(0);
            String[] items = jsontext.split("\\},\\{");
            if (items != null && items.length != 0) {
                for (final String f : items) {
                    final String fname = getJson(f, "name");
                    final String fsize = getJson(f, "size");
                    final String fuid = getJson(f, "id");
                    final String type = getJson(f, "type");
                    final String state = getJson(f, "state");
                    if (fuid != null && "file".equalsIgnoreCase(type)) {
                        DownloadLink dl = createDownloadlink("https://www.oboom.com/#" + fuid);
                        if ("online".equalsIgnoreCase(state)) {
                            dl.setAvailable(true);
                        } else {
                            dl.setAvailable(false);
                        }
                        if (fname != null) {
                            dl.setName(fname);
                        }
                        if (fsize != null) {
                            dl.setDownloadSize(Long.parseLong(fsize));
                        }
                        final String linkID = getHost() + "://" + fuid;
                        try {
                            dl.setLinkID(linkID);
                        } catch (final Throwable e) {
                            dl.setProperty("LINKDUPEID", linkID);
                        }
                        decryptedLinks.add(dl);
                    } else if (fuid != null && "folder".equalsIgnoreCase(type)) {
                        final DownloadLink dl = createDownloadlink("https://www.oboom.com/folder/" + fuid);
                        decryptedLinks.add(dl);
                    } else if (fuid != null && !"file".equalsIgnoreCase(type)) {
                        // sub folders maybe possible also ??
                        return null; // should get users reporting issue!
                    }
                }
            }
        }
        if (name != null) {
            FilePackage fp = FilePackage.getInstance();
            fp.setName(name);
            fp.addLinks(decryptedLinks);
        }
        return decryptedLinks;
    }

}
