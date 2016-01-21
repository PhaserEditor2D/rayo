package rayo.ui.views.project;

import java.awt.Color;
import java.awt.Component;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.Transferable;
import java.awt.event.ActionEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.HashMap;
import java.util.Map;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.DropMode;
import javax.swing.JTree;
import javax.swing.KeyStroke;
import javax.swing.TransferHandler;
import javax.swing.UIManager;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import rayo.core.Project;
import rayo.core.RayoFile;
import rayo.ui.Icons;
import rayo.ui.Workbench;
import rayo.ui.actions.RenameFileAction;

public class ProjectJTree extends JTree {
	private static final long serialVersionUID = 1L;
	protected static final Color EXCLUDED_COLOR = Color.lightGray;
	private int _transferAction;

	public ProjectJTree() {
		setRootVisible(false);
		setShowsRootHandles(true);

		getActionMap().put("cut", new AbstractAction("Cut") {
			private static final long serialVersionUID = 1L;

			{
				putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control X"));
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				toClipboard(TransferHandler.MOVE);
			}
		});

		getActionMap().put("copy", new AbstractAction("Copy") {
			private static final long serialVersionUID = 1L;

			{
				putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control C"));
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				toClipboard(TransferHandler.COPY);
			}
		});

		getActionMap().put("paste", new AbstractAction("Paste") {

			private static final long serialVersionUID = 1L;

			{
				putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control V"));
			}

			@Override
			public void actionPerformed(ActionEvent e) {
				paste();
			}
		});

		initDnD();

		super.setModel(new DefaultTreeModel(null));

		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
					boolean leaf, int row, boolean hasFocus1) {

				DefaultTreeCellRenderer label = (DefaultTreeCellRenderer) super.getTreeCellRendererComponent(tree,
						value, sel, expanded, leaf, row, hasFocus1);

				if (value instanceof RayoFile) {
					RayoFile file = (RayoFile) value;
					String name = file.getFilePath().getFileName().toString();

					if (!sel) {
						if (file.isExcluded() && name.endsWith(".js")) {
							label.setForeground(EXCLUDED_COLOR);
						} else if (name.indexOf('.') == 0) {
							label.setForeground(Color.gray);
						}
					}

					if (file.isFolder()) {
						label.setIcon(getOpenIcon());
					}

					label.setIcon(Icons.getFileIcon(file));

					label.setText(name);
				}
				return label;
			}
		};
		setCellRenderer(renderer);

		addMouseListener(new MouseAdapter() {

			@Override
			public void mouseReleased(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					showPopup(e);
				}
			}
		});
	}

	private void initDnD() {
		setDragEnabled(true);
		setDropMode(DropMode.ON);
		setTransferHandler(createTransferHandler());
	}

	private static TransferHandler createTransferHandler() {
		return new ProjectTransferHandler();
	}

	public void createActions() {
		// override the F2 key
		RenameFileAction action = Workbench.getInstance().getMainMenu().getRenameFileAction();
		getActionMap().put("rayo.renameFile", action);
		getInputMap().put((KeyStroke) action.getValue(Action.ACCELERATOR_KEY), "rayo.renameFile");
	}

	public void setModel(RayoFile projectFolder) {
		super.setModel(new DefaultTreeModel(projectFolder));
	}

	@Override
	public DefaultTreeModel getModel() {
		return (DefaultTreeModel) super.getModel();
	}

	protected void showPopup(MouseEvent e) {
		if (Project.getInstance().getProjectFolder() != null) {
			ProjectPopupMenu popup = new ProjectPopupMenu(this, e);
			popup.show(this, e.getX(), e.getY());
		}
	}

	public void fileWasRemoved(RayoFile file) {
		RayoFile parent = file.getParent();
		int index = parent.getIndex(file);
		if (index != -1) {
			getModel().nodesWereRemoved(parent, new int[] { index }, new Object[] { file });
		}
	}

	public void toClipboard(int transferAction) {
		if (!ProjectTransferHandler.isValidSelection(ProjectJTree.this)) {
			UIManager.getLookAndFeel().provideErrorFeedback(ProjectJTree.this);
			return;
		}

		_transferAction = transferAction;

		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		getTransferHandler().exportToClipboard(this, clipboard, transferAction);
	}

	public void copy() {
		toClipboard(TransferHandler.COPY);
	}

	public void paste() {
		ProjectTransferHandler transferHandler = (ProjectTransferHandler) getTransferHandler();

		Clipboard clipboard = Toolkit.getDefaultToolkit().getSystemClipboard();
		Transferable contents = clipboard.getContents(null);
		TreePath dest = getSelectionPath();
		if (!transferHandler.importData(this, contents, dest, _transferAction)) {
			UIManager.getLookAndFeel().provideErrorFeedback(this);
		}

		// next pastes are copies, even if the first action was a cut
		_transferAction = TransferHandler.COPY;
	}

	public void refresh() {
		Map<Object, Boolean> map = new HashMap<>();
		RayoFile root = Project.getInstance().getProjectFolder();
		root.sort();
		root.walk(f -> {
			TreeNode[] pathToRoot = getModel().getPathToRoot(f);
			boolean expanded = isExpanded(new TreePath(pathToRoot));
			map.put(f, Boolean.valueOf(expanded));
		});
		setModel(root);
		root.walk(f -> {
			TreeNode[] pathToRoot = getModel().getPathToRoot(f);
			TreePath path = new TreePath(pathToRoot);
			boolean expanded = map.get(f).booleanValue();
			setExpandedState(path, expanded);
		});
	}

}
