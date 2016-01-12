//jDownloader - Downloadmanager
//Copyright (C) 2015  JD-Team support@jdownloader.org
//
//This program is free software: you can redistribute it and/or modify
//it under the terms of the GNU General Public License as published by
//the Free Software Foundation, either version 3 of the License, or
//(at your option) any later version.
//
//This program is distributed in the hope that it will be useful,
//but WITHOUT ANY WARRANTY; without even the implied warranty of
//MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
//GNU General Public License for more details.
//
//You should have received a copy of the GNU General Public License
//along with this program.  If not, see <http://www.gnu.org/licenses/>.

package jd.plugins.decrypter;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.concurrent.atomic.AtomicReference;

import org.appwork.storage.JSonStorage;
import org.appwork.utils.StringUtils;
import org.appwork.utils.formatter.HexFormatter;
import org.jdownloader.captcha.v2.challenge.clickcaptcha.ClickedPoint;
import org.jdownloader.captcha.v2.challenge.keycaptcha.KeyCaptcha;
import org.jdownloader.captcha.v2.challenge.recaptcha.v2.CaptchaHelperCrawlerPluginRecaptchaV2;

import jd.PluginWrapper;
import jd.controlling.ProgressController;
import jd.http.Browser;
import jd.http.URLConnectionAdapter;
import jd.nutils.JDHash;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.parser.html.Form;
import jd.parser.html.InputField;
import jd.plugins.CryptedLink;
import jd.plugins.DecrypterException;
import jd.plugins.DecrypterPlugin;
import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForDecrypt;
import jd.plugins.components.UserAgents;
import jd.utils.JDUtilities;

@DecrypterPlugin(revision = "$Revision$", interfaceVersion = 3, names = { "filecrypt.cc" }, urls = { "https?://(?:www\\.)?filecrypt\\.cc/Container/([A-Z0-9]{10})\\.html" }, flags = { 0 })
public class FileCryptCc extends PluginForDecrypt {

    private static AtomicReference<String> LAST_USED_PASSWORD = new AtomicReference<String>();

    public FileCryptCc(PluginWrapper wrapper) {
        super(wrapper);
    }

    /* NO OVERRIDE!! */
    public boolean hasCaptcha(CryptedLink link, jd.plugins.Account acc) {
        return true;
    }

    public ArrayList<DownloadLink> decryptIt(CryptedLink param, ProgressController progress) throws Exception {
        final ArrayList<DownloadLink> decryptedLinks = new ArrayList<DownloadLink>();
        final String parameter = param.toString();
        br = new Browser();
        String agent = UserAgents.stringUserAgent();
        while (!agent.contains(" Chrome/")) {
            agent = UserAgents.stringUserAgent();
        }
        br.getHeaders().put("User-Agent", agent);
        br.getHeaders().put("Accept-Encoding", "gzip, deflate, sdch");
        br.getHeaders().put("Accept-Language", "en");

        br.setFollowRedirects(true);
        final String uid = new Regex(parameter, this.getSupportedLinks()).getMatch(0);
        // not all captcha types are skipable (recaptchav2 isn't). I tried with new response value - raztoki
        getPage(parameter);
        if (br.getURL().contains("filecrypt.cc/404.html")) {
            try {
                decryptedLinks.add(createOfflinelink(parameter));
            } catch (final Throwable t) {
                logger.info("OfflineLink :" + parameter);
            }
            return decryptedLinks;
        }
        // Separate password and captcha. this is easier for count reasons!
        int counter = -1;
        final int retry = 10;

        ArrayList<String> tries = new ArrayList<String>();
        if (StringUtils.isNotEmpty(LAST_USED_PASSWORD.get())) {
            tries.add(LAST_USED_PASSWORD.get());
        }
        if (!StringUtils.equals(LAST_USED_PASSWORD.get(), param.getDecrypterPassword()) && !StringUtils.isNotEmpty(param.getDecrypterPassword())) {
            tries.add(param.getDecrypterPassword());
        }
        String usedPassword = null;
        while (counter++ < retry && containsPassword()) {
            Form passwordForm = null;
            final Form[] allForms = br.getForms();
            if (allForms != null && allForms.length != 0) {
                for (final Form aForm : allForms) {
                    if (aForm.containsHTML("password")) {
                        passwordForm = aForm;
                        break;
                    }
                }
            }
            /* If there is captcha + password, password comes first, then captcha! */
            if (passwordForm != null) {

                String passCode = null;
                if (tries.size() > 0) {
                    passCode = tries.remove(0);
                }

                // when previous provided password has failed, or not provided we should ask
                if (passCode == null) {
                    passCode = getUserInput("Password?", param);

                }
                usedPassword = passCode;
                passwordForm.put("password", Encoding.urlEncode(passCode));
                submitForm(passwordForm);
            } else {
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT, "Could not find pasword form");
            }
        }
        if (usedPassword != null) {
            LAST_USED_PASSWORD.set(usedPassword);
        }
        if (counter == retry && containsPassword()) {
            throw new DecrypterException(DecrypterException.PASSWORD);
        }
        // captcha time!
        counter = -1;
        while (counter++ < retry && containsCaptcha()) {
            Form captchaForm = null;
            final Form[] allForms = br.getForms();
            if (allForms != null && allForms.length != 0) {
                for (final Form aForm : allForms) {
                    if (aForm.containsHTML("captcha")) {
                        captchaForm = aForm;
                        break;
                    }
                }
            }
            final String captcha = captchaForm != null ? captchaForm.getRegex("(/captcha/[^<>\"]*?)\"").getMatch(0) : null;
            if (captcha != null && captcha.contains("circle.php")) {
                final File file = this.getLocalCaptchaFile();
                br.cloneBrowser().getDownload(file, captcha);
                final ClickedPoint cp = getCaptchaClickedPoint(getHost(), file, param, null, "Click on the open circle");
                captchaForm.put("button.x", String.valueOf(cp.getX()));
                captchaForm.put("button.y", String.valueOf(cp.getY()));
                captchaForm.remove("button");
                submitForm(captchaForm);
            } else if (captchaForm != null && captchaForm.containsHTML("=\"g-recaptcha\"")) {
                final String recaptchaV2Response = new CaptchaHelperCrawlerPluginRecaptchaV2(this, br).getToken();
                captchaForm.put("g-recaptcha-response", Encoding.urlEncode(recaptchaV2Response));
                submitForm(captchaForm);
            } else if (captchaForm != null && captchaForm.containsHTML("solvemedia\\.com/papi/")) {

                final org.jdownloader.captcha.v2.challenge.solvemedia.SolveMedia sm = new org.jdownloader.captcha.v2.challenge.solvemedia.SolveMedia(br);
                File cf = null;
                try {
                    cf = sm.downloadCaptcha(getLocalCaptchaFile());
                } catch (final Exception e) {
                    if (org.jdownloader.captcha.v2.challenge.solvemedia.SolveMedia.FAIL_CAUSE_CKEY_MISSING.equals(e.getMessage())) {
                        throw new PluginException(LinkStatus.ERROR_FATAL, "Host side solvemedia.com captcha error - please contact the " + this.getHost() + " support");
                    }
                    throw e;
                }
                final String code = getCaptchaCode(cf, param);
                if (StringUtils.isEmpty(code)) {
                    if (counter + 1 < retry) {
                        continue;
                    } else {
                        throw new DecrypterException(DecrypterException.CAPTCHA);
                    }
                }
                final String chid = sm.getChallenge(code);
                captchaForm.put("adcopy_response", Encoding.urlEncode(code));
                captchaForm.put("adcopy_challenge", chid);
                submitForm(captchaForm);
            } else if (captchaForm != null && captchaForm.containsHTML("capcode")) {
                final String result = handleCaptchaChallenge(new KeyCaptcha(this, br, createDownloadlink(parameter)).createChallenge(this));
                if (StringUtils.isEmpty(result)) {
                    throw new PluginException(LinkStatus.ERROR_CAPTCHA);
                }
                if ("CANCEL".equals(result)) {
                    throw new PluginException(LinkStatus.ERROR_FATAL);
                }
                captchaForm.put("capcode", result);
                submitForm(captchaForm);
            } else if (captcha != null) {
                // they use recaptcha response field key for non recaptcha.. math sum and text =
                // http://filecrypt.cc/captcha/captcha.php?namespace=container
                // using bismarck original observation, this type is skipable.
                if (counter > 0) {
                    final String code = getCaptchaCode(captcha, param);
                    if (StringUtils.isEmpty(code)) {
                        if (counter + 1 < retry) {
                            continue;
                        } else {
                            throw new DecrypterException(DecrypterException.CAPTCHA);
                        }
                    }
                    captchaForm.put("recaptcha_response_field", Encoding.urlEncode(code));
                } else {
                    captchaForm.put("recaptcha_response_field", "");
                }
                submitForm(captchaForm);
            } else {
                throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT, "Could not find captcha form");
            }
        }
        if (counter == retry && containsCaptcha()) {
            throw new DecrypterException(DecrypterException.CAPTCHA);
        }
        final String fpName = br.getRegex("class=\"status (online|offline) shield\">([^<>\"]*?)<").getMatch(1);

        // mirrors
        String[] mirrors = br.getRegex("\"([^\"]*/Container/" + uid + "\\.html\\?mirror=\\d+)\"").getColumn(0);
        if (mirrors.length < 1) {
            mirrors = new String[1];
            mirrors[0] = parameter + "?mirror=0";
        }
        // first mirror shown should be mirror 0;
        Arrays.sort(mirrors);
        for (String mirror : mirrors) {
            // if 0 we don't need to get new page
            if (!mirror.endsWith("mirror=0")) {
                br.getPage(mirror);
            }
            final ArrayList<DownloadLink> tdl = new ArrayList<DownloadLink>();
            // Use clicknload first as it doesn't rely on JD service.jdownloader.org, which can go down!
            handleCnl2(tdl, parameter);
            if (!tdl.isEmpty()) {
                decryptedLinks.addAll(tdl);
                continue;
            }
            /* Second try DLC, then single links */
            final String dlc_id = br.getRegex("DownloadDLC\\('([^<>\"]*?)'\\)").getMatch(0);
            if (dlc_id != null) {
                logger.info("DLC found - trying to add it");
                tdl.addAll(loadcontainer("http://filecrypt.cc/DLC/" + dlc_id + ".dlc"));
                if (tdl.isEmpty()) {
                    logger.warning("DLC is empty or something is broken!");
                    continue;
                }
                decryptedLinks.addAll(tdl);
            }
        }
        if (!decryptedLinks.isEmpty()) {
            logger.info("DLC successfully added");
            return decryptedLinks;
        }
        // this isn't always shown, see 104061178D - raztoki 20141118
        logger.info("Trying single link handling");
        final String[] links = br.getRegex("openLink\\('([^<>\"]*?)'").getColumn(0);
        if (links == null || links.length == 0) {
            logger.warning("Decrypter broken for link: " + parameter);
            return null;
        }
        br.setFollowRedirects(false);
        for (final String singleLink : links) {
            final Browser br2 = br.cloneBrowser();
            br2.getPage("http://filecrypt.cc/Link/" + singleLink + ".html");
            if (br2.containsHTML("friendlyduck.com/") || br2.containsHTML("filecrypt\\.cc/usenet\\.html") || br2.containsHTML("share-online\\.biz/affiliate")) {
                /* Advertising */
                continue;
            }
            String finallink = null;
            final String first_rd = br2.getRedirectLocation();
            if (first_rd != null && first_rd.contains("filecrypt.cc/")) {
                br2.getPage(first_rd);
                finallink = br2.getRedirectLocation();
            } else if (first_rd != null && !first_rd.contains("filecrypt.cc/")) {
                finallink = first_rd;
            } else {
                final String nextlink = br2.getRegex("\"(https?://(www\\.)?filecrypt\\.cc/index\\.php\\?Action=(G|g)o[^<>\"]*?)\"").getMatch(0);
                if (nextlink != null) {
                    br2.getPage(nextlink);
                    finallink = br2.getRedirectLocation();
                }
            }
            if (finallink != null && finallink.contains("filecrypt.cc/")) {
                logger.warning("Decrypter broken for link: " + parameter + "->" + finallink);
            } else if (finallink != null) {
                decryptedLinks.add(createDownloadlink(finallink));
            }
        }

        if (fpName != null) {
            final FilePackage fp = FilePackage.getInstance();
            fp.setName(Encoding.htmlDecode(fpName.trim()));
            fp.addLinks(decryptedLinks);
        }

        return decryptedLinks;
    }

    private void handleCnl2(final ArrayList<DownloadLink> decryptedLinks, final String parameter) throws Exception {
        final Form[] forms = br.getForms();
        Form CNLPOP = null;
        Form cnl = null;
        for (final Form f : forms) {
            if (f.containsHTML("CNLPOP")) {
                CNLPOP = f;
                break;
            }
        }
        if (CNLPOP != null) {
            final String infos[] = CNLPOP.getRegex("'(.*?)'").getColumn(0);
            cnl = new Form();
            cnl.addInputField(new InputField("crypted", infos[2]));
            cnl.addInputField(new InputField("jk", "function f(){ return \'" + infos[1] + "';}"));
            cnl.addInputField(new InputField("source", null));
        }
        if (cnl == null) {
            for (final Form f : forms) {
                if (f.hasInputFieldByName("jk")) {
                    cnl = f;
                    break;
                }
            }
        }
        if (cnl != null) {
            final HashMap<String, String> infos = new HashMap<String, String>();
            infos.put("crypted", Encoding.urlDecode(cnl.getInputField("crypted").getValue(), false));
            infos.put("jk", Encoding.urlDecode(cnl.getInputField("jk").getValue(), false));
            String source = cnl.getInputField("source").getValue();
            if (StringUtils.isEmpty(source)) {
                source = parameter.toString();
            } else {
                infos.put("source", source);
            }
            infos.put("source", source);
            final String json = JSonStorage.toString(infos);
            final DownloadLink dl = createDownloadlink("http://dummycnl.jdownloader.org/" + HexFormatter.byteArrayToHex(json.getBytes("UTF-8")));
            decryptedLinks.add(dl);
        }
    }

    private final boolean containsCaptcha() {
        return new Regex(cleanHTML, containsCaptcha).matches();
    }

    private final boolean containsPassword() {
        return new Regex(cleanHTML, "class=\"passw\"").matches();
    }

    private final String containsCaptcha = "class=\"safety\">(Sicherheitsabfrage|Security prompt)<";

    private String       cleanHTML       = null;

    private final void cleanUpHTML() {
        String toClean = br.toString();
        ArrayList<String> regexStuff = new ArrayList<String>();
        // generic cleanup
        regexStuff.add("<!(--.*?--)>");
        regexStuff.add("(<\\s*(\\w+)\\s+[^>]*style\\s*=\\s*(\"|')(?:(?:[\\w:;\\s#-]*(visibility\\s*:\\s*hidden;|display\\s*:\\s*none;|font-size\\s*:\\s*0;)[\\w:;\\s#-]*)|font-size\\s*:\\s*0|visibility\\s*:\\s*hidden|display\\s*:\\s*none)\\3[^>]*(>.*?<\\s*/\\2[^>]*>|/\\s*>))");

        for (String aRegex : regexStuff) {
            String results[] = new Regex(toClean, aRegex).getColumn(0);
            if (results != null) {
                for (String result : results) {
                    toClean = toClean.replace(result, "");
                }
            }
        }

        cleanHTML = toClean;
    }

    @SuppressWarnings("deprecation")
    private ArrayList<DownloadLink> loadcontainer(final String theLink) throws IOException, PluginException {
        ArrayList<DownloadLink> links = new ArrayList<DownloadLink>();
        final Browser brc = br.cloneBrowser();
        File file = null;
        URLConnectionAdapter con = null;
        try {
            con = brc.openGetConnection(theLink);
            if (con.getResponseCode() == 200) {
                file = JDUtilities.getResourceFile("tmp/filecryptcc/" + JDHash.getSHA1(theLink) + theLink.substring(theLink.lastIndexOf(".")));
                if (file == null) {
                    return links;
                }
                file.getParentFile().mkdirs();
                file.deleteOnExit();
                brc.downloadConnection(file, con);
                if (file != null && file.exists() && file.length() > 100) {
                    links.addAll(JDUtilities.getController().getContainerLinks(file));
                }
            }
        } catch (Throwable e) {
            e.printStackTrace();
        } finally {
            try {
                con.disconnect();
            } catch (final Throwable e) {
            }
            if (file.exists()) {
                file.delete();
            }

        }
        return links;
    }

    private final void getPage(final String page) throws IOException, PluginException {
        if (page == null) {
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        br.getPage(page);
        cleanUpHTML();
    }

    private final void postPage(final String url, final String post) throws IOException, PluginException {
        if (url == null || post == null) {
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        br.postPage(url, post);
        cleanUpHTML();
    }

    private final void submitForm(final Form form) throws Exception {
        if (form == null) {
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        br.submitForm(form);
        cleanUpHTML();
    }

}