package rayo.ui.actions;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static rayo.core.CoreUtils.findNextFileName;

import java.awt.event.ActionEvent;
import java.io.IOException;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import rayo.core.Project;
import rayo.core.RayoFile;
import rayo.core.TernManager;
import rayo.ui.Icons;
import rayo.ui.Workbench;
import rayo.ui.views.project.ProjectJTree;

public class NewFileAction extends AbstractAction {

	private static final long serialVersionUID = 1L;
	private ProjectJTree _tree;
	private RayoFile _contextFile;
	private String _nameHint;
	private String _ext;

	public NewFileAction(String name, String ext, ProjectJTree tree, RayoFile contextFile) {
		super(ext.length() == 0 ? name : name + "." + ext, Icons.getFileIcon());
		_nameHint = name;
		_ext = ext;
		_tree = tree;
		_contextFile = contextFile;
	}

	@Override
	public final void actionPerformed(ActionEvent e) {
		if (_contextFile == null) {
			_contextFile = (RayoFile) _tree.getModel().getRoot();
		}
		if (_contextFile.isFile()) {
			_contextFile = _contextFile.getParent();
		}

		String name = getFilename();
		if (name == null) {
			return;
		}

		try {

			RayoFile newFile = _contextFile.createFile(name, createInitialContent(name));
			newFile.computeExclusionState();

			Workbench.getInstance().openFile(newFile);
			_tree.getModel().nodesWereInserted(_contextFile, new int[] { _contextFile.getIndex(newFile) });
			TreeNode[] pathToRoot = _tree.getModel().getPathToRoot(newFile);
			_tree.setSelectionPath(new TreePath(pathToRoot));

			TernManager tern = Project.getInstance().getTernManager();
			if (!tern.checkDisposedAndShowMessage()) {
				tern.updateFile(newFile);
			}
		} catch (IOException e1) {
			e1.printStackTrace();
			showMessageDialog(null, e1, "New File", ERROR_MESSAGE);
		}
	}

	protected String getFilename() {
		String input;
		if (_nameHint.length() == 0) {
			input = "." + _ext;
		} else {
			input = _nameHint;
		}

		String name = JOptionPane.showInputDialog("Enter the file name:", input);
		if (name == null) {
			return null;
		}
		if (_ext.length() > 0 && !name.endsWith("." + _ext)) {
			name += "." + _ext;
		}
		name = findNextFileName(_contextFile.getFilePath(), name);
		return name;
	}

	@SuppressWarnings({ "unused", "static-method" })
	protected String createInitialContent(String name) throws IOException {
		return "//new file " + name;
	}

}
