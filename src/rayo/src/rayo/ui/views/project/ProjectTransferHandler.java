package rayo.ui.views.project;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import rayo.core.CoreUtils;
import rayo.core.Project;
import rayo.core.RayoFile;

// taken from
// http://www.coderanch.com/t/346509/GUI/java/JTree-drag-drop-tree-Java
class ProjectTransferHandler extends TransferHandler {
	private static final long serialVersionUID = 1L;

	DataFlavor nodesFlavor;
	DataFlavor[] flavors = new DataFlavor[1];
	RayoFile[] _nodesToRemove;

	public ProjectTransferHandler() {
		try {
			String mimeType = DataFlavor.javaJVMLocalObjectMimeType + ";class=\"" + RayoFile[].class.getName() + "\"";
			nodesFlavor = new DataFlavor(mimeType);
			flavors[0] = nodesFlavor;
		} catch (ClassNotFoundException e) {
			System.out.println("ClassNotFound: " + e.getMessage());
		}
	}

	public DataFlavor getNodesFlavor() {
		return nodesFlavor;
	}

	@Override
	public boolean canImport(TransferHandler.TransferSupport support) {
		if (!support.isDrop()) {
			return false;
		}
		support.setShowDropLocation(true);
		if (!support.isDataFlavorSupported(nodesFlavor)) {
			return false;
		}
		// Do not allow a drop on the drag source selections.
		JTree.DropLocation dropLocation = (JTree.DropLocation) support.getDropLocation();
		TreePath dest = dropLocation.getPath();
		JTree tree = (JTree) support.getComponent();

		// Arian: do not allow to move if there are ancestors in the selection
		if (!isValidSelection(tree)) {
			return false;
		}

		return canImport(tree, dest);
	}

	@SuppressWarnings("static-method")
	public boolean canImport(JTree tree, TreePath dest) {
		if (dest == null) {
			return false;
		}

		int dropRow = tree.getRowForPath(dest);
		int[] selRows = tree.getSelectionRows();
		for (int i = 0; i < selRows.length; i++) {
			if (selRows[i] == dropRow) {
				return false;
			}
		}

		RayoFile target = (RayoFile) dest.getLastPathComponent();
		TreePath path = tree.getPathForRow(selRows[0]);
		RayoFile firstNode = (RayoFile) path.getLastPathComponent();
		RayoFile test = target;
		do {
			if (test == firstNode) {
				return false;
			}
			test = test.getParent();
		} while (test != null);
		return true;
	}

	public static boolean isValidSelection(JTree tree) {
		int[] sel = tree.getSelectionRows();

		RayoFile[] nodes = new RayoFile[sel.length];
		for (int i = 0; i < sel.length; i++) {
			nodes[i] = (RayoFile) tree.getPathForRow(sel[i]).getLastPathComponent();
		}

		return isValidSelection(nodes);
	}

	public static boolean isValidSelection(RayoFile[] sel) {
		for (int i = 0; i < sel.length - 1; i++) {
			for (int j = i + 1; j < sel.length; j++) {
				RayoFile a = sel[i];
				RayoFile b = sel[j];
				if (a.isAncestor(b) || b.isAncestor(a)) {
					return false;
				}
			}
		}
		return true;
	}

	@Override
	protected Transferable createTransferable(JComponent c) {
		JTree tree = (JTree) c;
		TreePath[] paths = tree.getSelectionPaths();
		if (paths != null) {
			// Make up a node array of copies for transfer and
			// another for/of the nodes that will be removed in
			// exportDone after a successful drop.
			List<RayoFile> copies = new ArrayList<>();
			List<RayoFile> toRemove = new ArrayList<>();
			RayoFile node = (RayoFile) paths[0].getLastPathComponent();
			copies.add(node);
			toRemove.add(node);
			for (int i = 1; i < paths.length; i++) {
				RayoFile next = (RayoFile) paths[i].getLastPathComponent();
				// Do not allow higher level nodes to be added to list.
				if (next.getLevel() < node.getLevel()) {
					break;
				} else if (next.getLevel() > node.getLevel()) { // child node
					node.add(next);
					// node already contains child
				} else { // sibling
					copies.add(next);
					toRemove.add(next);
				}
			}
			RayoFile[] nodes = copies.toArray(new RayoFile[copies.size()]);
			_nodesToRemove = toRemove.toArray(new RayoFile[toRemove.size()]);
			return new NodesTransferable(nodes);
		}
		return null;
	}

	@Override
	protected void exportDone(JComponent source, Transferable data, int action) {
		// the node re-location is performed in the importData.
	}

	@Override
	public int getSourceActions(JComponent c) {
		return COPY_OR_MOVE;
	}

	@Override
	public boolean importData(TransferHandler.TransferSupport support) {
		if (!canImport(support)) {
			return false;
		}
		JTree tree = (JTree) support.getComponent();
		Transferable transferable = support.getTransferable();
		TreePath dest = ((JTree.DropLocation) support.getDropLocation()).getPath();

		return importData(tree, transferable, dest, MOVE);
	}

	public boolean importData(JTree tree, Transferable transferable, TreePath dest, int action) {
		// Extract transfer data.
		RayoFile[] nodes = null;
		try {
			nodes = (RayoFile[]) transferable.getTransferData(nodesFlavor);
		} catch (UnsupportedFlavorException ufe) {
			System.out.println("UnsupportedFlavor: " + ufe.getMessage());
			return false;
		} catch (java.io.IOException ioe) {
			System.out.println("I/O error: " + ioe.getMessage());
			return false;
		}
		// Get drop location info.
		RayoFile parent = (RayoFile) dest.getLastPathComponent();
		// this is the case when the file is drop into other file (not a
		// directory)
		if (parent.isFile()) {
			parent = parent.getParent();
		}
		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();
		// Add data to model.
		for (int i = 0; i < nodes.length; i++) {
			RayoFile node = nodes[i];
			try {
				Path srcPath = node.getFilePath();

				if (action == COPY) {
					copy(tree, parent, node);
				} else {
					// do a move
					Path moveToPath = parent.getFilePath();

					String result = CoreUtils.moveTree(srcPath.toFile(), moveToPath.toFile());

					if (result != null) {
						if (result.length() == 0) {
							return false;
						}
						UIManager.getLookAndFeel().provideErrorFeedback(null);
						showMessageDialog(null, result, "Move", INFORMATION_MESSAGE);
						return false;
					}

					node.getParent().remove(node);
					parent.add(node);

					((ProjectJTree) tree).refresh();

					tree.expandPath(new TreePath(model.getPathToRoot(node.getParent())));
					tree.setSelectionPath(new TreePath(model.getPathToRoot(node)));
				}
			} catch (IOException e) {
				e.printStackTrace();
				showMessageDialog(null, e, "Move", ERROR_MESSAGE);
			}
		}
		return true;
	}

	private static boolean copy(JTree tree, RayoFile dstFolder, RayoFile file) throws IOException {
		Path srcPath = file.getFilePath();

		if (srcPath.equals(dstFolder.getFilePath())) {
			if (Files.isDirectory(srcPath)) {
				showMessageDialog(null, "Cannot move a folder into the same place", "Copy", ERROR_MESSAGE);
				return false;
			}
		}

		DefaultTreeModel model = (DefaultTreeModel) tree.getModel();

		String result = CoreUtils.copyTree(srcPath.toFile(), dstFolder.getFilePath().toFile());

		if (result != null) {
			if (result.length() == 0) {
				return false;
			}
			UIManager.getLookAndFeel().provideErrorFeedback(null);
			showMessageDialog(null, result, "Move", INFORMATION_MESSAGE);
			return false;
		}

		// TODO: when override it creates a new node, it should not happen

		RayoFile newFile = dstFolder.createFile(srcPath.getFileName().toString(), null);

		newFile.computeExclusionState();
		Project.getInstance().getTernManager().updateFile(newFile);

		((ProjectJTree) tree).refresh();
		tree.expandPath(new TreePath(model.getPathToRoot(newFile.getParent())));
		tree.setSelectionPath(new TreePath(model.getPathToRoot(newFile)));

		return true;
	}

	public class NodesTransferable implements Transferable {
		RayoFile[] _nodes;

		public NodesTransferable(RayoFile[] nodes) {
			this._nodes = nodes;
		}

		@Override
		public Object getTransferData(DataFlavor flavor) throws UnsupportedFlavorException {
			if (!isDataFlavorSupported(flavor))
				throw new UnsupportedFlavorException(flavor);
			return _nodes;
		}

		@Override
		public DataFlavor[] getTransferDataFlavors() {
			return flavors;
		}

		@Override
		public boolean isDataFlavorSupported(DataFlavor flavor) {
			return nodesFlavor.equals(flavor);
		}
	}
}