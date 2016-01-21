package rayo.ui.views.project;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import rayo.core.Project;
import rayo.core.RayoFile;
import rayo.ui.IEditor;
import rayo.ui.IMainMenu;
import rayo.ui.IProjectView;
import rayo.ui.IWorkbench;
import rayo.ui.Workbench;
import rayo.ui.actions.OpenFolderAction;
import rayo.ui.actions.OpenInWebBrowserAction;
import rayo.ui.actions.OpenSavedFolderAction;
import rayo.ui.editors.EditorList;
import rayo.ui.widgets.RayoPopupMenu;

@SuppressWarnings("synthetic-access")
public class ProjectView extends JPanel implements IProjectView {

	private static final long serialVersionUID = 1L;

	private ProjectJTree _projectTree;
	private JButton _projectButton;

	private EditorList _editorList;

	public ProjectView() {
		setLayout(new BorderLayout());

		createWorkingFilesPanel();
		createFilesPanel();

		_projectTree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() > 1) {
					openSelectedFile();
				}
			}
		});

		_projectTree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					openSelectedFile();
				}
			}
		});
		_projectTree.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				//
			}

			@Override
			public void focusGained(FocusEvent e) {
				Workbench.getInstance().setActiveState(IProjectView.ACTIVE_STATE);
			}
		});
	}

	private void createWorkingFilesPanel() {
		add(_editorList = new EditorList(), BorderLayout.NORTH);
	}

	private void createFilesPanel() {
		_projectTree = new ProjectJTree();
		add(new ProjectPanel(), BorderLayout.CENTER);
	}

	class ProjectPanel extends JPanel {
		private static final long serialVersionUID = 1L;

		public ProjectPanel() {
			setLayout(new BorderLayout());
			JPanel topPanel = new JPanel(new FlowLayout(FlowLayout.LEFT));
			topPanel.setBackground(Color.white);
			_projectButton = new JButton("Open Folder");
			_projectButton.setHorizontalTextPosition(SwingConstants.LEFT);
			_projectButton.addActionListener(new ActionListener() {

				@Override
				public void actionPerformed(ActionEvent e) {
					if (Project.getInstance().getProjectFolder() == null) {
						new OpenFolderAction().actionPerformed(e);
					} else {
						createPopupMenu();
					}
				}
			});
			topPanel.add(_projectButton);
			add(topPanel, BorderLayout.NORTH);
			JScrollPane scrollPane = new JScrollPane(_projectTree);
			scrollPane.setBorder(BorderFactory.createEmptyBorder());
			add(scrollPane, BorderLayout.CENTER);
		}
	}

	@Override
	public void openFolder(Path newPath) {
		IWorkbench wb = Workbench.getInstance();
		Project project = Project.getInstance();
		project.setPath(newPath, new Runnable() {

			@Override
			public void run() {
				wb.refreshProblems();
				wb.refreshFiles();
			}
		});
		_projectTree.setModel(project.getProjectFolder());
		_projectButton.setText(newPath.getFileName().toString());
	}

	private void createPopupMenu() {
		JPopupMenu menu = new RayoPopupMenu();
		IMainMenu mainMenu = Workbench.getInstance().getMainMenu();

		menu.add(new OpenFolderAction());
		menu.addSeparator();

		{
			List<Path> list = Project.getInstance().getRecentFolders();
			RayoFile projectFolder = Project.getInstance().getProjectFolder();
			if (projectFolder != null) {
				Path current = projectFolder.getFilePath();
				for (Path path : list) {
					if (!path.equals(current)) {
						menu.add(new OpenSavedFolderAction(path));
					}
				}
			}
			if (list.size() > 1) {
				menu.addSeparator();
			}
		}

		menu.add(new OpenInWebBrowserAction());
		menu.add(mainMenu.getOpenLocationAction());
		menu.addSeparator();

		menu.add(mainMenu.getRefreshAction());
		menu.add(mainMenu.getReloadAction());

		menu.show(_projectButton, 0, _projectButton.getHeight());
	}

	@Override
	public void fileWasRemoved(RayoFile file) {
		_projectTree.fileWasRemoved(file);
	}

	@Override
	public void fileWasChanged(RayoFile file) {
		_projectTree.getModel().nodeChanged(file);
		_editorList.repaint();
	}

	@Override
	public TreeSelectionModel getSelectionModel() {
		return _projectTree.getSelectionModel();
	}

	public void createActions() {
		_projectTree.createActions();
	}

	public void refresh() {
		_projectTree.setModel(Project.getInstance().getProjectFolder());
	}

	public void expandNode(RayoFile file) {
		TreeNode[] pathToRoot = _projectTree.getModel().getPathToRoot(file);
		_projectTree.expandPath(new TreePath(pathToRoot));
	}

	public List<RayoFile> getExpandedNodes() {
		List<RayoFile> list = new ArrayList<>();
		Project.getInstance().getProjectFolder().walk(p -> {
			TreeNode[] pathToRoot = _projectTree.getModel().getPathToRoot(p);
			if (_projectTree.isExpanded(new TreePath(pathToRoot))) {
				list.add(p);
			}
		});
		return list;
	}

	public void refreshProblems() {
		_projectTree.repaint();
		_editorList.repaint();
	}

	public void selectEditingFile() {
		IEditor editor = Workbench.getInstance().getActiveEditor();
		if (editor != null) {
			_projectTree.setSelectionPath(new TreePath(_projectTree.getModel().getPathToRoot(editor.getFile())));
		}
	}

	@Override
	public void reveal(RayoFile file) {
		_projectTree.setSelectionPath(new TreePath(_projectTree.getModel().getPathToRoot(file)));
	}

	private void openSelectedFile() {
		TreePath selPath = _projectTree.getSelectionPath();
		if (selPath != null) {
			RayoFile selected = (RayoFile) selPath.getLastPathComponent();
			if (selected.isFile()) {
				Workbench.getInstance().openFile(selected);
			}
		}
	}

	@Override
	public boolean isActive() {
		return _projectTree.hasFocus();
	}

	@Override
	public void copy() {
		_projectTree.copy();
	}

	@Override
	public void paste() {
		_projectTree.paste();
	}
}
