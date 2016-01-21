package rayo.ui.actions;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.showInputDialog;
import static javax.swing.JOptionPane.showMessageDialog;

import java.io.IOException;

import javax.swing.KeyStroke;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import rayo.core.RayoFile;
import rayo.ui.IProjectView;
import rayo.ui.IWorkbench;
import rayo.ui.Workbench;

public class RenameFileAction extends SelectedFileAction {

	private static final long serialVersionUID = 1L;

	public RenameFileAction() {
		super("Rename");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F2"));
	}

	@Override
	protected void actionPerformed(TreeSelectionModel selection) {
		try {
			IWorkbench wb = Workbench.getInstance();
			IProjectView view = wb.getProjectView();
			for (TreePath treePath : selection.getSelectionPaths()) {

				RayoFile file = (RayoFile) treePath.getLastPathComponent();
				String filename = file.getFilePath().getFileName().toString();
				String newName = (String) showInputDialog(null, "Enter the new name", "Rename", QUESTION_MESSAGE, null,
						null, filename);

				if (newName == null || newName.trim().length() == 0) {
					return;
				}

				file.rename(newName);

				view.fileWasChanged(file);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			showMessageDialog(null, e1, "Delete File", ERROR_MESSAGE);
		}
	}
}
