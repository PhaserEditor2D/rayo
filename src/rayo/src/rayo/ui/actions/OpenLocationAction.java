package rayo.ui.actions;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.tree.TreeSelectionModel;

import rayo.core.Project;
import rayo.core.RayoFile;
import rayo.ui.Workbench;

public class OpenLocationAction extends AbstractAction {
	private static final long serialVersionUID = 1L;

	public OpenLocationAction() {
		super("Open Location");
		Workbench.getInstance().addValidator(new Runnable() {

			@Override
			public void run() {
				setEnabled(Project.getInstance().getProjectFolder() != null);
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		TreeSelectionModel selection = Workbench.getInstance().getProjectView().getSelectionModel();

		RayoFile selFile;
		if (selection.getSelectionCount() == 0) {
			selFile = Project.getInstance().getProjectFolder();
		} else {
			selFile = (RayoFile) selection.getSelectionPath().getLastPathComponent();
		}

		File file = selFile.getFilePath().toFile();

		try {
			Desktop.getDesktop().open((file.isDirectory() ? file : file.getParentFile()));
		} catch (IOException e1) {
			showMessageDialog(null, e1, "Open Location", ERROR_MESSAGE);
			e1.printStackTrace();
		}
	}

}
