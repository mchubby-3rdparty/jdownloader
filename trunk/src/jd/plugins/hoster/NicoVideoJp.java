//jDownloader - Downloadmanager
//Copyright (C) 2009  JD-Team support@jdownloader.org
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

package jd.plugins.hoster;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.concurrent.atomic.AtomicInteger;

import jd.PluginWrapper;
import jd.config.ConfigContainer;
import jd.config.ConfigEntry;
import jd.config.SubConfiguration;
import jd.nutils.encoding.Encoding;
import jd.parser.Regex;
import jd.plugins.Account;
import jd.plugins.Account.AccountType;
import jd.plugins.AccountInfo;
import jd.plugins.DownloadLink;
import jd.plugins.DownloadLink.AvailableStatus;
import jd.plugins.HostPlugin;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;
import jd.utils.locale.JDL;

@HostPlugin(revision = "$Revision$", interfaceVersion = 2, names = { "nicovideo.jp" }, urls = { "http://(www\\.)?nicovideo\\.jp/watch/(sm|so|nm)?\\d+" }, flags = { 2 })
public class NicoVideoJp extends PluginForHost {

    private static final String  MAINPAGE                     = "http://www.nicovideo.jp/";

    private static final String  ONLYREGISTEREDUSERTEXT       = "Only downloadable for registered users";
    private static final String  CUSTOM_DATE                  = "CUSTOM_DATE";
    private static final String  CUSTOM_FILENAME              = "CUSTOM_FILENAME";
    private static final String  TYPE_SO                      = "http://(www\\.)?nicovideo\\.jp/watch/so\\d+";
    private static final String  TYPE_WATCH                   = "http://(www\\.)?nicovideo\\.jp/watch/\\d+";
    private static final String  default_extension            = ".flv";

    private static final String  NOCHUNKS                     = "NOCHUNKS";
    private static final String  AVOID_ECONOMY_MODE           = "AVOID_ECONOMY_MODE";

    private static final int     ACCOUNT_FREE_MAXDOWNLOADS    = 2;
    private static final int     economy_active_wait_minutes  = 30;
    /* note: CAN NOT be negative or zero! (ie. -1 or 0) Otherwise math sections fail. .:. use [1-20] */
    private static AtomicInteger totalMaxSimultanFreeDownload = new AtomicInteger(ACCOUNT_FREE_MAXDOWNLOADS);
    /* don't touch the following! */
    private static AtomicInteger maxPremium                   = new AtomicInteger(1);

    public NicoVideoJp(PluginWrapper wrapper) {
        super(wrapper);
        this.enablePremium("https://secure.nicovideo.jp/secure/register");
        setConfigElements();
    }

    /**
     * IMPORTANT: The site has a "normal" and "economy" mode. Normal mode = Higher video quality - mp4 streams. Economy mode = lower quality
     * - flv streams. Premium users are ALWAYS in the normal mode.
     */
    @SuppressWarnings("deprecation")
    @Override
    public AvailableStatus requestFileInformation(final DownloadLink link) throws IOException, PluginException, ParseException {
        link.setProperty("extension", default_extension);
        this.setBrowserExclusive();
        br.setCustomCharset("utf-8");
        br.setFollowRedirects(true);
        br.getPage(link.getDownloadURL());
        if (br.containsHTML("<title>ニコニコ動画　ログインフォーム</title>") || br.getURL().contains("/secure/") || br.getURL().contains("login_form?")) {
            throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        }
        if (br.containsHTML("this video inappropriate.<")) {
            String watch = br.getRegex("harmful_link\" href=\"([^<>\"]*?)\">Watch this video</a>").getMatch(0);
            br.getPage(watch);
        }
        String filename = br.getRegex("<meta property=\"og:title\" content=\"([^<>\"]*?)\"").getMatch(0);
        if (filename == null) {
            filename = br.getRegex("<h1 itemprop=\"name\">([^<>\"]*?)</h1>").getMatch(0);
        }
        if (filename == null) {
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        filename = Encoding.htmlDecode(filename).trim();

        link.setProperty("plainfilename", filename);
        final String date = br.getRegex("property=\"video:release_date\" content=\"(\\d{4}\\-\\d{2}\\-\\d{2}T\\d{2}:\\d{2}\\+\\d{4})\"").getMatch(0);
        if (date != null) {
            link.setProperty("originaldate", date);
        }
        final String channel = br.getRegex("Uploader: <strong itemprop=\"name\">([^<>\"]*?)</strong>").getMatch(0);
        if (channel != null) {
            link.setProperty("channel", channel);
        }

        filename = getFormattedFilename(link);
        link.setName(filename);
        link.getLinkStatus().setStatusText(JDL.L("plugins.hoster.nicovideojp.only4registered", ONLYREGISTEREDUSERTEXT));

        return AvailableStatus.TRUE;
    }

    @SuppressWarnings("deprecation")
    @Override
    public AccountInfo fetchAccountInfo(final Account account) throws Exception {
        final AccountInfo ai = new AccountInfo();
        if (!account.getUser().matches(".+@.+\\..+")) {
            if ("de".equalsIgnoreCase(System.getProperty("user.language"))) {
                throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nBitte gib deine E-Mail Adresse ins Benutzername Feld ein!", PluginException.VALUE_ID_PREMIUM_DISABLE);
            } else {
                throw new PluginException(LinkStatus.ERROR_PREMIUM, "\r\nPlease enter your e-mail adress in the username field!", PluginException.VALUE_ID_PREMIUM_DISABLE);
            }
        }
        try {
            login(account);
        } catch (PluginException e) {
            account.setValid(false);
            return ai;
        }
        try {
            account.setType(AccountType.FREE);
            // account.setMaxSimultanDownloads(ACCOUNT_FREE_MAXDOWNLOADS);
        } catch (final Throwable e) {
            /* Not available in old 0.9.581 Stable */
            account.setProperty("free", true);
        }
        ai.setUnlimitedTraffic();
        /* TODO: Implement premium support */
        ai.setStatus("Registered User");
        return ai;
    }

    @Override
    public String getAGBLink() {
        return "http://info.nicovideo.jp/base/rule.html";
    }

    @Override
    public int getMaxSimultanFreeDownloadNum() {
        return -1;
    }

    @Override
    public int getMaxSimultanPremiumDownloadNum() {
        /* workaround for free/premium issue on stable 09581 */
        return maxPremium.get();
    }

    @Override
    public void handleFree(final DownloadLink downloadLink) throws Exception, PluginException {
        requestFileInformation(downloadLink);
        try {
            throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_ONLY);
        } catch (final Throwable e) {
            if (e instanceof PluginException) {
                throw (PluginException) e;
            }
        }
        throw new PluginException(LinkStatus.ERROR_FATAL, ONLYREGISTEREDUSERTEXT);
    }

    @SuppressWarnings("deprecation")
    @Override
    public void handlePremium(final DownloadLink link, final Account account) throws Exception {
        requestFileInformation(link);
        login(account);
        br.setFollowRedirects(true);
        // Important, without accessing the link we cannot get the downloadurl!
        br.getPage(link.getDownloadURL());
        if (Encoding.htmlDecode(br.toString()).contains("closed=1\\&done=true")) {
            throw new PluginException(LinkStatus.ERROR_TEMPORARILY_UNAVAILABLE, "Server error", 10 * 60 * 1000l);
        }
        if (link.getDownloadURL().matches(TYPE_SO) || link.getDownloadURL().matches(TYPE_WATCH)) {
            final String linkid = new Regex(br.getURL(), "(\\d+)$").getMatch(0);
            br.postPage("http://flapi.nicovideo.jp/api/getflv", "v=" + linkid);
        } else {
            final String vid = new Regex(br.getURL(), "((sm|nm)\\d+)$").getMatch(0);
            br.postPage("http://flapi.nicovideo.jp/api/getflv", "v=" + vid);
        }
        String dllink = new Regex(Encoding.htmlDecode(br.toString()), "\\&url=(http://.*?)\\&").getMatch(0);
        if (dllink == null) {
            dllink = new Regex(Encoding.htmlDecode(br.toString()), "(http://smile-com\\d+\\.nicovideo\\.jp/smile\\?v=[0-9\\.]+)").getMatch(0);
        }
        if (dllink == null) {
            logger.warning("Final downloadlink (String is \"dllink\") regex didn't match!");
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }

        int maxChunks = 0;
        if (link.getBooleanProperty(NOCHUNKS, false)) {
            maxChunks = 1;
        }

        dl = jd.plugins.BrowserAdapter.openDownload(br, link, dllink, true, maxChunks);
        if (dl.getConnection().getContentType().contains("html")) {
            logger.warning("The final dllink seems not to be a file!");
            br.followConnection();
            throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFECT);
        }
        final String contenttype = dl.getConnection().getContentType();
        if (contenttype.equals("video/mp4")) {
            link.setProperty("extension", ".mp4");
        } else {
            /* Check if the user allows lower quality .flv files. */
            if (this.getPluginConfig().getBooleanProperty(AVOID_ECONOMY_MODE, false)) {
                throw new PluginException(LinkStatus.ERROR_HOSTER_TEMPORARILY_UNAVAILABLE, "Site is currently in economy mode", economy_active_wait_minutes * 60 * 1000l);
            }
            link.setProperty("extension", ".flv");
        }
        /* Now that we got the final extension of the file we can set the final filename. */
        final String final_filename = getFormattedFilename(link);
        link.setFinalFileName(final_filename);
        try {
            /* add a download slot */
            controlPremium(+1);
            if (!this.dl.startDownload()) {
                try {
                    if (dl.externalDownloadStop()) {
                        return;
                    }
                } catch (final Throwable e) {
                }
                /* unknown error, we disable multiple chunks */
                if (link.getBooleanProperty(NicoVideoJp.NOCHUNKS, false) == false) {
                    link.setProperty(NicoVideoJp.NOCHUNKS, Boolean.valueOf(true));
                    throw new PluginException(LinkStatus.ERROR_RETRY);
                }
            }
        } catch (final PluginException e) {
            // New V2 errorhandling
            /* unknown error, we disable multiple chunks */
            if (e.getLinkStatus() != LinkStatus.ERROR_RETRY && link.getBooleanProperty(NicoVideoJp.NOCHUNKS, false) == false) {
                link.setProperty(NicoVideoJp.NOCHUNKS, Boolean.valueOf(true));
                throw new PluginException(LinkStatus.ERROR_RETRY);
            }
            throw e;
        } finally {
            /* remove download slot */
            controlPremium(-1);
        }
    }

    private void login(final Account account) throws Exception {
        this.setBrowserExclusive();
        br.setFollowRedirects(false);
        br.postPage("https://secure.nicovideo.jp/secure/login?site=niconico", "next_url=&mail=" + Encoding.urlEncode(account.getUser()) + "&password=" + Encoding.urlEncode(account.getPass()));
        if (br.getCookie(MAINPAGE, "user_session") == null) {
            throw new PluginException(LinkStatus.ERROR_PREMIUM, PluginException.VALUE_ID_PREMIUM_DISABLE);
        }
    }

    @SuppressWarnings("deprecation")
    private String getFormattedFilename(final DownloadLink downloadLink) throws ParseException {
        final String extension = downloadLink.getStringProperty("extension", default_extension);
        final String videoid = new Regex(downloadLink.getDownloadURL(), "/watch/(.+)").getMatch(0);
        String videoName = downloadLink.getStringProperty("plainfilename", null);
        final SubConfiguration cfg = SubConfiguration.getConfig("nicovideo.jp");
        String formattedFilename = cfg.getStringProperty(CUSTOM_FILENAME, defaultCustomFilename);
        if (formattedFilename == null || formattedFilename.equals("")) {
            formattedFilename = defaultCustomFilename;
        }
        if (!formattedFilename.contains("*videoname") || !formattedFilename.contains("*ext*")) {
            formattedFilename = defaultCustomFilename;
        }

        String date = downloadLink.getStringProperty("originaldate", null);
        final String channelName = downloadLink.getStringProperty("channel", null);

        String formattedDate = null;
        if (date != null && formattedFilename.contains("*date*")) {
            date = date.replace("T", ":");
            final String userDefinedDateFormat = cfg.getStringProperty(CUSTOM_DATE, "dd.MM.yyyy_HH-mm-ss");
            // 2009-08-30T22:49+0900
            SimpleDateFormat formatter = new SimpleDateFormat("yyyy-MM-dd:HH:mm+ssss");
            Date dateStr = formatter.parse(date);

            formattedDate = formatter.format(dateStr);
            Date theDate = formatter.parse(formattedDate);

            if (userDefinedDateFormat != null) {
                try {
                    formatter = new SimpleDateFormat(userDefinedDateFormat);
                    formattedDate = formatter.format(theDate);
                } catch (Exception e) {
                    // prevent user error killing plugin.
                    formattedDate = "";
                }
            }
            if (formattedDate != null) {
                formattedFilename = formattedFilename.replace("*date*", formattedDate);
            } else {
                formattedFilename = formattedFilename.replace("*date*", "");
            }
        }
        formattedFilename = formattedFilename.replace("*videoid*", videoid);
        if (formattedFilename.contains("*channelname*")) {
            if (channelName != null) {
                formattedFilename = formattedFilename.replace("*channelname*", channelName);
            } else {
                formattedFilename = formattedFilename.replace("*channelname*", "");
            }
        }
        formattedFilename = formattedFilename.replace("*ext*", extension);
        // Insert filename at the end to prevent errors with tags
        formattedFilename = formattedFilename.replace("*videoname*", videoName);

        return formattedFilename;
    }

    /**
     * Prevents more than one free download from starting at a given time. One step prior to dl.startDownload(), it adds a slot to maxFree
     * which allows the next singleton download to start, or at least try.
     *
     * This is needed because xfileshare(website) only throws errors after a final dllink starts transferring or at a given step within pre
     * download sequence. But this template(XfileSharingProBasic) allows multiple slots(when available) to commence the download sequence,
     * this.setstartintival does not resolve this issue. Which results in x(20) captcha events all at once and only allows one download to
     * start. This prevents wasting peoples time and effort on captcha solving and|or wasting captcha trading credits. Users will experience
     * minimal harm to downloading as slots are freed up soon as current download begins.
     *
     * @param controlFree
     *            (+1|-1)
     */
    public synchronized void controlPremium(final int num) {
        logger.info("maxFree was = " + maxPremium.get());
        maxPremium.set(Math.min(Math.max(1, maxPremium.addAndGet(num)), totalMaxSimultanFreeDownload.get()));
        logger.info("maxFree now = " + maxPremium.get());
    }

    @Override
    public String getDescription() {
        return "JDownloader's nicovideo.jp plugin helps downloading videoclips. JDownloader provides settings for the filenames.";
    }

    private final static String defaultCustomFilename = "*videoname**ext*";

    private void setConfigElements() {
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_LABEL, "Customize the filename properties"));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_TEXTFIELD, getPluginConfig(), CUSTOM_DATE, JDL.L("plugins.hoster.nicovideojp.customdate", "Define how the date should look.")).setDefaultValue("dd.MM.yyyy_HH-mm-ss"));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_LABEL, "Customize the filename! Example: '*channelname*_*date*_*videoname**ext*'"));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_TEXTFIELD, getPluginConfig(), CUSTOM_FILENAME, JDL.L("plugins.hoster.nicovideojp.customfilename", "Define how the filenames should look:")).setDefaultValue(defaultCustomFilename));
        final StringBuilder sb = new StringBuilder();
        sb.append("Explanation of the available tags:\r\n");
        sb.append("*channelname* = name of the channel/uploader\r\n");
        sb.append("*date* = date when the video was posted - appears in the user-defined format above\r\n");
        sb.append("*videoname* = name of the video without extension\r\n");
        sb.append("*videoid* = ID of the video e.g. 'sm12345678'\r\n");
        sb.append("*ext* = the extension of the file, in this case usually '.flv'");
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_LABEL, sb.toString()));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_SEPARATOR));
        getConfig().addEntry(new ConfigEntry(ConfigContainer.TYPE_CHECKBOX, getPluginConfig(), NicoVideoJp.AVOID_ECONOMY_MODE, JDL.L("plugins.hoster.MicoVideoJp.AvoidEconomy mode", "Avoid economy mode - only download higher quality .mp4 videos?\r\n<html><b>Important: The default extension of all filenames is " + default_extension + ". It will be corrected once the downloads start if either this setting is active or the nicovideo site is in normal (NOT economy) mode!\r\nIf this setting is active and nicovideo is in economy mode, JDownloader will wait " + economy_active_wait_minutes + " minutes and try again afterwards.</b></html>")).setDefaultValue(false));
    }

    @Override
    public void reset() {
    }

    @Override
    public void resetDownloadlink(DownloadLink link) {
    }

}