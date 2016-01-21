package rayo.ui.views.project;

import java.awt.event.MouseEvent;
import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.Arrays;
import java.util.LinkedList;

import javax.swing.tree.TreePath;

import rayo.core.RayoFile;
import rayo.ui.IMainMenu;
import rayo.ui.Workbench;
import rayo.ui.actions.ImportFilesAction;
import rayo.ui.actions.NewFolderAction;
import rayo.ui.actions.NewTemplFileAction;
import rayo.ui.widgets.RayoMenu;
import rayo.ui.widgets.RayoMenuItem;
import rayo.ui.widgets.RayoPopupMenu;

public class ProjectPopupMenu extends RayoPopupMenu {
	private static final long serialVersionUID = 1L;
	private ProjectJTree _tree;

	public ProjectPopupMenu(ProjectJTree tree, MouseEvent e) {
		_tree = tree;

		RayoFile parent = null;
		int x = e.getX();
		int y = e.getY();
		TreePath path = _tree.getPathForLocation(x, y);
		if (path != null) {
			if (!_tree.isPathSelected(path)) {
				_tree.setSelectionPath(path);
			}
			parent = (RayoFile) path.getLastPathComponent();
		}

		RayoMenu newMenu = new RayoMenu("New...");
		createTemplatesMenuItems(newMenu, parent);
		add(newMenu);
		add(new RayoMenuItem(new NewFolderAction(tree, parent)));
		add(new RayoMenuItem(new ImportFilesAction(tree, parent)));

		addSeparator();

		IMainMenu mainMenu = Workbench.getInstance().getMainMenu();

		add(mainMenu.getOpenInBrowserAction());
		add(mainMenu.getOpenLocationAction());

		addSeparator();
		{
			boolean enabled = tree.getSelectionCount() > 0;
			add(tree.getActionMap().get("copy")).setEnabled(enabled);
			add(tree.getActionMap().get("paste")).setEnabled(enabled);
		}
		addSeparator();

		add(mainMenu.getRenameFileAction());
		add(mainMenu.getDeleteFileAction());
	}

	private void createTemplatesMenuItems(RayoMenu newMenu, RayoFile parent) {
		Path templFolder = Paths.get("templates");
		if (Files.exists(templFolder)) {

			try {
				Files.walkFileTree(templFolder, new SimpleFileVisitor<Path>() {
					private LinkedList<RayoMenu> _stack = new LinkedList<>(Arrays.asList(newMenu));

					@Override
					public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
						if (dir.equals(templFolder)) {
							return FileVisitResult.CONTINUE;
						}

						RayoMenu current = _stack.peek();
						if (dir.getFileName().toString().startsWith("group_")) {
							if (current.getMenuComponentCount() > 0) {
								current.addSeparator();
							}
							_stack.push(current);
						} else {
							RayoMenu dirMenu = new RayoMenu(dir.getFileName().toString());
							current.add(dirMenu);
							_stack.push(dirMenu);
						}
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult postVisitDirectory(Path dir, IOException exc) throws IOException {
						_stack.pop();
						return FileVisitResult.CONTINUE;
					}

					@Override
					public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
						NewTemplFileAction action = new NewTemplFileAction(file, getTree(), parent);
						RayoMenuItem item = new RayoMenuItem(action);
						_stack.peek().add(item);
						return FileVisitResult.CONTINUE;
					}
				});

			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}

	public ProjectJTree getTree() {
		return _tree;
	}

}
