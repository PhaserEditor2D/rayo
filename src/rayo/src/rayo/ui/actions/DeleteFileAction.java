package rayo.ui.actions;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.io.IOException;

import javax.swing.KeyStroke;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import rayo.core.RayoFile;
import rayo.ui.IProjectView;
import rayo.ui.IWorkbench;
import rayo.ui.Workbench;

public class DeleteFileAction extends SelectedFileAction {

	private static final long serialVersionUID = 1L;

	public DeleteFileAction() {
		super("Delete");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("DELETE"));
	}

	@Override
	protected void actionPerformed(TreeSelectionModel selection) {
		try {
			IProjectView view = Workbench.getInstance().getProjectView();
			for (TreePath treePath : selection.getSelectionPaths()) {
				RayoFile file = (RayoFile) treePath.getLastPathComponent();
				view.fileWasRemoved(file);
				deleteFile(file);
			}
			Workbench.getInstance().refreshProblems();
		} catch (IOException e1) {
			e1.printStackTrace();
			showMessageDialog(null, e1.getMessage(), "Delete File", ERROR_MESSAGE);
		}
	}

	private static void deleteFile(RayoFile file) throws IOException {
		if (!file.exists()) {
			return;
		}

		if (file.isFile()) {
			file.delete();
		} else {
			for (RayoFile child : file.getChildren()) {
				deleteFile(child);
			}
			file.delete();
		}
		IWorkbench workbench = Workbench.getInstance();
		workbench.closeFile(file, true);
	}
}
