//    jDownloader - Downloadmanager
//    Copyright (C) 2009  JD-Team support@jdownloader.org
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

package jd.plugins.host;

import jd.PluginWrapper;
import jd.plugins.DownloadLink;
import jd.plugins.LinkStatus;
import jd.plugins.PluginException;
import jd.plugins.PluginForHost;

public class MyuploadDK extends PluginForHost {

    public MyuploadDK(PluginWrapper wrapper) {
        super(wrapper);
    }

    public String getAGBLink() {
        return "http://www.myupload.dk/rules/";
    }

    public boolean getFileInformation(DownloadLink parameter) throws Exception {
        this.setBrowserExclusive();
        br.setCookie("http://www.myupload.dk", "lang", "en");
        br.getPage(parameter.getDownloadURL());
        String filename = br.getRegex("<h2>(.*?)</h2>").getMatch(0);
        if (filename == null) throw new PluginException(LinkStatus.ERROR_FILE_NOT_FOUND);
        parameter.setName(filename);
        return true;
    }

    public void handleFree(DownloadLink link) throws Exception {
        getFileInformation(link);
        String url = br.getRegex("to download <a href='(/download/.*?)'>.*?</a><br />").getMatch(0);
        if (url == null) throw new PluginException(LinkStatus.ERROR_PLUGIN_DEFEKT);
        br.setFollowRedirects(true);
        br.setDebug(true);
        dl = br.openDownload(link, url);
        dl.startDownload();
    }

    public void reset() {
        // TODO Auto-generated method stub

    }

    public void reset_downloadlink(DownloadLink link) {
        // TODO Auto-generated method stub

    }

    public String getVersion() {
        return getVersion("$Revision$");

    }

    public int getMaxSimultanFreeDownloadNum() {
        return 20;
    }

}
