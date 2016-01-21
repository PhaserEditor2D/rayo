package rayo.ui.actions;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.event.ActionEvent;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import rayo.core.Project;
import rayo.core.RayoFile;
import rayo.core.TernManager;
import rayo.ui.views.project.ProjectJTree;

public class ImportFilesAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	private static JFileChooser _chooser;
	private RayoFile _parent;
	private ProjectJTree _tree;

	public ImportFilesAction(ProjectJTree tree, RayoFile parent) {
		super("Import Files...");
		_parent = parent == null ? Project.getInstance().getProjectFolder() : parent;
		_tree = tree;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (_chooser == null) {
			_chooser = new JFileChooser();
			_chooser.setFileSelectionMode(JFileChooser.FILES_ONLY);
			_chooser.setMultiSelectionEnabled(true);
		}

		if (_chooser.showOpenDialog(null) == JFileChooser.APPROVE_OPTION) {
			File[] files = _chooser.getSelectedFiles();
			for (File file : files) {
				importFile(file);
			}
		}
	}

	private void importFile(File file) {
		Path src = file.toPath();

		if (_parent.isFile()) {
			_parent = _parent.getParent();
		}

		Path dst = _parent.getFilePath().resolve(src.getFileName());

		if (Files.exists(dst)) {
			showMessageDialog(null, String.format("The file %s already exist in this location.", src.getFileName()),
					"Import", INFORMATION_MESSAGE);
			return;
		}

		try {
			Files.copy(src, dst);
			RayoFile newFile = _parent.createFile(dst.getFileName().toString(), null);
			newFile.computeExclusionState();

			_tree.getModel().nodesWereInserted(_parent, new int[] { _parent.getIndex(newFile) });
			TreeNode[] pathToRoot = _tree.getModel().getPathToRoot(newFile);
			_tree.setSelectionPath(new TreePath(pathToRoot));

			TernManager tern = Project.getInstance().getTernManager();
			if (!tern.checkDisposedAndShowMessage()) {
				tern.updateFile(newFile);
			}
		} catch (IOException e) {
			e.printStackTrace();
			showMessageDialog(null, e, "Error", ERROR_MESSAGE);
		}
	}

}
