package rayo.ui.actions;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.showInputDialog;
import static javax.swing.JOptionPane.showMessageDialog;
import static rayo.core.CoreUtils.findNextFileName;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import rayo.core.RayoFile;
import rayo.ui.views.project.ProjectJTree;

public class NewFolderAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	private ProjectJTree _tree;
	private RayoFile _contextFile;

	public NewFolderAction(ProjectJTree tree, RayoFile contextFile) {
		super("New Folder");
		_tree = tree;
		_contextFile = contextFile;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		String name = (String) showInputDialog(null, "Enter the folder name", "New Folder", QUESTION_MESSAGE, null,
				null, "Folder");

		if (name == null) {
			return;
		}

		if (_contextFile == null) {
			_contextFile = (RayoFile) _tree.getModel().getRoot();
		}
		if (_contextFile.isFile()) {
			_contextFile = _contextFile.getParent();
		}

		try {
			name = findNextFileName(_contextFile.getFilePath(), name);
			RayoFile newFolder = _contextFile.createFolder(name);
			_tree.getModel().nodesWereInserted(_contextFile, new int[] { _contextFile.getIndex(newFolder) });
			TreeNode[] pathToRoot = _tree.getModel().getPathToRoot(newFolder);
			_tree.setSelectionPath(new TreePath(pathToRoot));
		} catch (IOException e1) {
			e1.printStackTrace();
			showMessageDialog(null, e1.getMessage(), "New Folder", ERROR_MESSAGE);
		}
	}

	@SuppressWarnings("static-method")
	protected String createInitialContent() {
		return "//new file";
	}

}
