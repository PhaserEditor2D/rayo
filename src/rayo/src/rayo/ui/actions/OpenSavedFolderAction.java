package rayo.ui.actions;

import java.awt.event.ActionEvent;
import java.nio.file.Path;

import javax.swing.AbstractAction;

import rayo.ui.Workbench;

public class OpenSavedFolderAction extends AbstractAction {
	private static final long serialVersionUID = 1L;

	private Path _folder;

	public OpenSavedFolderAction(Path folder) {
		super("Open '" + folder.getFileName().toString() + "'");
		_folder = folder;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Workbench.getInstance().openProjectFolder(_folder);
	}

}
