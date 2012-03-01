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

package org.jdownloader.extensions.jdtrayicon;

import javax.swing.JLabel;
import javax.swing.JPanel;

import jd.controlling.downloadcontroller.DownloadInformations;
import jd.controlling.downloadcontroller.DownloadWatchDog;
import jd.gui.swing.components.JWindowTooltip;
import jd.gui.swing.jdgui.components.JDProgressBar;
import jd.nutils.Formatter;
import net.miginfocom.swing.MigLayout;

import org.jdownloader.extensions.jdtrayicon.translate.T;

public class TrayIconTooltip extends JWindowTooltip {

    private static final long          serialVersionUID = -400023413449818691L;

    private JLabel                     lblSpeed;
    private JLabel                     lblDlRunning;
    private JLabel                     lblDlFinished;
    private JLabel                     lblDlTotal;
    private JDProgressBar              prgTotal;
    private JLabel                     lblETA;
    private JLabel                     lblProgress;

    private final DownloadInformations ds;

    public TrayIconTooltip() {
        ds = DownloadInformations.getInstance();
    }

    protected void addContent(JPanel panel) {
        panel.setLayout(new MigLayout("wrap 2", "[fill, grow][fill, grow]"));
        panel.add(new JLabel(T._.plugins_optional_trayIcon_downloads()), "spanx 2");
        panel.add(new JLabel(T._.plugins_optional_trayIcon_dl_running()), "gapleft 10");
        panel.add(lblDlRunning = new JLabel(""));
        panel.add(new JLabel(T._.plugins_optional_trayIcon_dl_finished()), "gapleft 10");
        panel.add(lblDlFinished = new JLabel(""));
        panel.add(new JLabel(T._.plugins_optional_trayIcon_dl_total()), "gapleft 10");
        panel.add(lblDlTotal = new JLabel(""));
        panel.add(new JLabel(T._.plugins_optional_trayIcon_speed()));
        panel.add(lblSpeed = new JLabel(""));
        panel.add(new JLabel(T._.plugins_optional_trayIcon_progress()));
        panel.add(lblProgress = new JLabel(""));
        panel.add(prgTotal = new JDProgressBar(), "spanx 2");
        panel.add(new JLabel(T._.plugins_optional_trayIcon_eta()));
        panel.add(lblETA = new JLabel(""));
    }

    protected void updateContent() {
        ds.updateInformations();

        lblDlRunning.setText(String.valueOf(ds.getRunningDownloads()));
        lblDlFinished.setText(String.valueOf(ds.getFinishedDownloads()));
        lblDlTotal.setText(String.valueOf(ds.getDownloadCount()));
        lblSpeed.setText(Formatter.formatReadable(DownloadWatchDog.getInstance().getConnectionHandler().getSpeed()) + "/s");

        lblProgress.setText(Formatter.formatFilesize(ds.getCurrentDownloadSize(), 0) + " / " + Formatter.formatFilesize(ds.getTotalDownloadSize(), 0));
        prgTotal.setMaximum(ds.getTotalDownloadSize());
        prgTotal.setValue(ds.getCurrentDownloadSize());

        lblETA.setText(Formatter.formatSeconds(ds.getETA()));
    }

}