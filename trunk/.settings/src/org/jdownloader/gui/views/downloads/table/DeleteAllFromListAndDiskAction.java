package org.jdownloader.gui.views.downloads.table;

import java.awt.event.ActionEvent;

import jd.plugins.DownloadLink;
import jd.plugins.FilePackage;

import org.jdownloader.actions.AppAction;
import org.jdownloader.gui.translate._GUI;
import org.jdownloader.gui.views.SelectionInfo;

public class DeleteAllFromListAndDiskAction extends AppAction {

    private SelectionInfo<FilePackage, DownloadLink> si;

    public DeleteAllFromListAndDiskAction(SelectionInfo<FilePackage, DownloadLink> si) {

        this.si = si;
        setName(_GUI._.DeleteAllAction_DeleteAllAction_object_());
        setIconKey("remove");
    }

    @Override
    public void actionPerformed(ActionEvent e) {

    }

}
