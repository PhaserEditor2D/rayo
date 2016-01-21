package rayo.core;

import static javax.swing.JOptionPane.CANCEL_OPTION;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.NO_OPTION;
import static javax.swing.JOptionPane.YES_NO_CANCEL_OPTION;
import static javax.swing.JOptionPane.YES_OPTION;
import static javax.swing.JOptionPane.showMessageDialog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.Vector;
import java.util.function.Consumer;

import javax.swing.JOptionPane;
import javax.swing.tree.MutableTreeNode;
import javax.swing.tree.TreeNode;

import rayo.ui.IWorkbench;
import rayo.ui.Workbench;

public class RayoFile implements MutableTreeNode {
	private Path _path;
	private Vector<RayoFile> _children;
	private RayoFile _parent;
	private boolean _modified;
	private String _editingContent;
	private String _id;
	private List<Problem> _problems;
	private int _lastCaretPosition;
	private ExcludeRule _excludeRules;
	private boolean _excluded;

	public RayoFile(RayoFile parent, Path path) {
		_parent = parent;
		_path = path;
		_children = new Vector<>();
		_id = UUID.randomUUID().toString();
		File pfile = path.toFile();
		File[] list = pfile.listFiles();
		if (list != null) {
			for (File file : list) {
				_children.add(new RayoFile(this, file.toPath()));
			}
			sort();
		}
		_problems = new ArrayList<>();
		_lastCaretPosition = 0;

		if (isFolder()) {
			Path path2 = path.resolve(ExcludeRule.EXCLUDE_FILENAME);
			if (Files.exists(path2)) {
				_excludeRules = ExcludeRule.create(path2);
			}
		}
	}

	private void collectRules(List<ExcludeRule> rules) {
		if (_excludeRules != null) {
			rules.add(_excludeRules);
		}

		if (_parent != null) {
			_parent.collectRules(rules);
		}
	}

	public void computeExclusionState() {
		Path relpath = getRelativePath();
		_excluded = false;

		List<ExcludeRule> rules = new ArrayList<>();
		collectRules(rules);

		for (ExcludeRule rule : rules) {
			if (rule.isExcluded(relpath)) {
				_excluded = true;
				break;
			}
		}

		for (RayoFile child : _children) {
			child.computeExclusionState();
		}
	}

	public boolean isExcluded() {
		return _excluded;
	}

	public int getLastCaretPosition() {
		return _lastCaretPosition;
	}

	public void setLastCaretPosition(int lastCaretPosition) {
		_lastCaretPosition = lastCaretPosition;
	}

	public List<Problem> getProblems() {
		return _problems;
	}

	public void setProblems(List<Problem> problems) {
		_problems = problems;
	}

	public String getId() {
		return _id;
	}

	public RayoFile createFile(String name, String content) throws IOException {
		RayoFile newFile = new RayoFile(this, _path.resolve(name));
		Path path = newFile.getFilePath();

		if (content != null) {
			Files.createFile(path);
			Files.write(path, content.getBytes());
		}

		_children.add(newFile);
		
		sort();

		return newFile;
	}

	public RayoFile createFolder(String name) throws IOException {
		RayoFile newFolder = new RayoFile(this, _path.resolve(name));
		Path path = newFolder.getFilePath();

		Files.createDirectories(path);

		_children.add(newFolder);
		
		sort();

		return newFolder;
	}

	public void sort() {
		for (RayoFile child : _children) {
			child.sort();
		}

		_children.sort((a, b) -> {
			int x = a.isFolder() ? 0 : 1;
			int y = b.isFolder() ? 0 : 1;
			if (x == y) {
				return a.getFilePath().compareTo(b.getFilePath());
			}
			return Integer.compare(x, y);
		});
	}

	public boolean isModified() {
		return _modified;
	}

	public void setModified(boolean modified) {
		_modified = modified;
		getWorkbench().getProjectView().fileWasChanged(this);
	}

	private static IWorkbench getWorkbench() {
		return Workbench.getInstance();
	}

	public void setEditingContent(String content) {
		_editingContent = content;
	}

	public String getContent() {
		if (_editingContent != null) {
			return _editingContent;
		}
		String fileContent;
		try {
			byte[] bytes = Files.readAllBytes(_path);
			fileContent = new String(bytes);
			// TODO: I don't know why.
			fileContent = fileContent.replace("\r\n", "\n");
			return fileContent;
		} catch (IOException e) {
			e.printStackTrace();
			showMessageDialog(null, e.getMessage(), "Error", ERROR_MESSAGE);
			return "";
		}
	}

	public void save() {
		if (!_modified) {
			return;
		}

		try {
			Files.write(_path, _editingContent.getBytes());
			setModified(false);
			getWorkbench().getProjectView().fileWasChanged(this);
		} catch (Exception e) {
			e.printStackTrace();
			showMessageDialog(null, e.getMessage(), "Error", ERROR_MESSAGE);
		}
	}

	public Path getFilePath() {
		return _path;
	}

	public void rename(String filename) throws IOException {
		Path newPath = _path.getParent().resolve(filename);
		if (Files.exists(newPath)) {
			showMessageDialog(null, String.format("File %s already exists.", newPath.getFileName().toString()),
					"Rename", INFORMATION_MESSAGE);
			return;
		}
		Files.move(_path, newPath, StandardCopyOption.REPLACE_EXISTING);
		_path = newPath;

		for (RayoFile file : _children) {
			file.reparentPath();
		}
	}

	public void reparentPath() {
		_path = _parent._path.resolve(_path.getFileName());
		for (RayoFile file : _children) {
			file.reparentPath();
		}
	}

	public boolean isFolder() {
		return Files.isDirectory(_path);
	}

	public boolean isFile() {
		return !isFolder();
	}

	@Override
	public TreeNode getChildAt(int childIndex) {
		return _children.get(childIndex);
	}

	@Override
	public int getChildCount() {
		return _children.size();
	}

	@Override
	public RayoFile getParent() {
		return _parent;
	}

	public RayoFile getRootFile() {
		if (_parent == null) {
			return this;
		}

		return _parent.getRootFile();
	}

	public Path getRelativePath() {
		return getRootFile().getFilePath().relativize(_path);
	}

	@Override
	public int getIndex(TreeNode node) {
		return _children.indexOf(node);
	}

	@Override
	public boolean getAllowsChildren() {
		return Files.isDirectory(_path);
	}

	@Override
	public boolean isLeaf() {
		return _children.isEmpty();
	}

	@Override
	public Enumeration children() {
		return _children.elements();
	}

	public RayoFile[] getChildren() {
		return _children.toArray(new RayoFile[_children.size()]);
	}

	/**
	 * @deprecated This should be moved to the editor interface.
	 * @param force
	 * @return
	 */
	@Deprecated
	public boolean closing(boolean force) {
		if (!force) {
			if (_modified) {
				int answer = JOptionPane.showConfirmDialog(null,
						String.format("'%s' has been modified. Save changes?", _path.getFileName()), "Save File",
						YES_NO_CANCEL_OPTION);
				switch (answer) {
				case YES_OPTION:
					save();
					return true;
				case CANCEL_OPTION:
					return false;
				case NO_OPTION:
					break;
				default:
					return false;
				}
			}
		}
		// discard editing state
		_modified = false;
		_editingContent = null;
		return true;
	}

	/**
	 * @deprecated This should be moved to the editor interface.
	 * @return
	 */
	@Deprecated
	public boolean askForSaveBeforeShutdown() {
		if (!_modified) {
			return true;
		}

		int answer = JOptionPane.showConfirmDialog(null,
				String.format("'%s' has been modified. Save changes?", _path.getFileName()), "Save File",
				YES_NO_CANCEL_OPTION);
		switch (answer) {
		case YES_OPTION:
			save();
			return true;
		case CANCEL_OPTION:
			return false;
		default:
			break;
		}
		return true;
	}

	public void delete() throws IOException {
		Files.delete(_path);
		removeFromParent();
		Project.getInstance().getTernManager().deleteFile(this);
	}

	public boolean exists() {
		return Files.exists(_path);
	}

	public void walk(Consumer<RayoFile> visitor) {
		visitor.accept(this);
		for (RayoFile file : _children) {
			file.walk(visitor);
		}
	}

	RayoFile findFile(String id) {
		if (_id.equals(id)) {
			return this;
		}

		for (RayoFile child : _children) {
			RayoFile file = child.findFile(id);
			if (file != null) {
				return file;
			}
		}
		return null;
	}

	public RayoFile findFile(Path path) {
		if (_path.equals(path)) {
			return this;
		}
		for (RayoFile file : _children) {
			RayoFile found = file.findFile(path);
			if (found != null) {
				return found;
			}
		}
		return null;
	}

	public boolean refresh() {
		// detect deleted
		if (!Files.exists(_path)) {
			removeFromParent();
			return true;
		}

		// detect new children
		Set<Path> toAdd = new HashSet<>();
		if (Files.isDirectory(_path)) {
			File[] files = _path.toFile().listFiles();
			if (files != null) {
				for (File f : files) {
					toAdd.add(f.toPath());
				}
			}
		}

		boolean changed = false;

		RayoFile[] list = _children.toArray(new RayoFile[_children.size()]);
		for (RayoFile child : list) {
			boolean childChanged = child.refresh();
			changed = changed || childChanged;
			toAdd.remove(child.getFilePath());
		}

		for (Path file : toAdd) {
			_children.add(new RayoFile(this, file));
		}

		changed = changed || !toAdd.isEmpty();

		return changed;
	}

	/**
	 * Returns the number of levels above this node -- the distance from the
	 * root to this node. If this node is the root, returns 0.
	 *
	 * @see #getDepth
	 * @return the number of levels above this node
	 */
	public int getLevel() {
		TreeNode ancestor;
		int levels = 0;

		ancestor = this;
		while ((ancestor = ancestor.getParent()) != null) {
			levels++;
		}

		return levels;
	}

	/**
	 * Returns true if <code>aNode</code> is a child of this node. If
	 * <code>aNode</code> is null, this method returns false.
	 *
	 * @return true if <code>aNode</code> is a child of this node; false if
	 *         <code>aNode</code> is null
	 */
	public boolean isNodeChild(TreeNode aNode) {
		boolean retval;

		if (aNode == null) {
			retval = false;
		} else {
			if (getChildCount() == 0) {
				retval = false;
			} else {
				retval = (aNode.getParent() == this);
			}
		}

		return retval;
	}

	public void add(RayoFile child) {
		_children.add(child);
		child._parent = this;
		child.reparentPath();
	}

	@Override
	public void insert(MutableTreeNode child, int index) {
		_children.insertElementAt((RayoFile) child, index);
	}

	@Override
	public void remove(int index) {
		_children.remove(index);
	}

	@Override
	public void remove(MutableTreeNode node) {
		_children.remove(node);
	}

	@Override
	public void setUserObject(Object object) {
		_path = (Path) object;
	}

	@Override
	public void removeFromParent() {
		if (_parent != null) {
			_parent._children.remove(this);
		}
	}

	@Override
	public void setParent(MutableTreeNode newParent) {
		_parent = (RayoFile) newParent;
		reparentPath();
	}

	@Override
	public String toString() {
		return "RayoFile: " + getRelativePath();
	}

	public boolean isAncestor(RayoFile e) {
		if (e == this) {
			return true;
		}

		if (e == null) {
			return false;
		}

		return isAncestor(e.getParent());
	}
}
