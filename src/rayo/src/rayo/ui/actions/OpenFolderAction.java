package rayo.ui.actions;

import java.awt.event.ActionEvent;
import java.io.File;
import java.nio.file.Path;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;

import rayo.core.Project;
import rayo.core.RayoFile;
import rayo.ui.IWorkbench;
import rayo.ui.Workbench;

public class OpenFolderAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	public OpenFolderAction() {
		super("Open Folder...");
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		// TODO: use something like the IDEA open dialog.
		JFileChooser chooser = new JFileChooser();
		RayoFile current = Project.getInstance().getProjectFolder();
		if (current != null) {
			Path parent = current.getFilePath().getParent();
			if (parent == null) {
				parent = current.getFilePath();
			}
			chooser.setCurrentDirectory(parent.toFile());
		}
		chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
		if (chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			File dir = chooser.getSelectedFile();
			Path newPath = dir.toPath();
			IWorkbench wb = Workbench.getInstance();
			wb.openProjectFolder(newPath);
		}

	}

}