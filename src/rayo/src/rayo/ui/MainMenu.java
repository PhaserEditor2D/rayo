package rayo.ui;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.ImageIcon;
import javax.swing.JCheckBoxMenuItem;
import javax.swing.JFrame;
import javax.swing.JMenu;
import javax.swing.JMenuBar;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import rayo.ui.actions.CloseFileAction;
import rayo.ui.actions.DeleteFileAction;
import rayo.ui.actions.DeleteLineAction;
import rayo.ui.actions.FindTextAction;
import rayo.ui.actions.FormatAction;
import rayo.ui.actions.GoToLineAction;
import rayo.ui.actions.GotoDefinitionAction;
import rayo.ui.actions.LineMoveAction;
import rayo.ui.actions.MultipleCopyAction;
import rayo.ui.actions.MultipleCutAction;
import rayo.ui.actions.MultiplePasteAction;
import rayo.ui.actions.OpenInJSDocAction;
import rayo.ui.actions.OpenInWebBrowserAction;
import rayo.ui.actions.OpenLocationAction;
import rayo.ui.actions.OutlineAction;
import rayo.ui.actions.RedoAction;
import rayo.ui.actions.RefreshAction;
import rayo.ui.actions.ReloadAction;
import rayo.ui.actions.RenameFileAction;
import rayo.ui.actions.RenameSymbolAction;
import rayo.ui.actions.ReplaceTextAction;
import rayo.ui.actions.SaveAction;
import rayo.ui.actions.SelectAllAction;
import rayo.ui.actions.ShowInFileTreeAction;
import rayo.ui.actions.UndoAction;
import rayo.ui.actions.UnindentAction;
import rayo.ui.widgets.RayoMenu;

public class MainMenu extends JMenuBar implements IMainMenu {

	private static final long serialVersionUID = 1L;
	private OutlineAction _outlineAction;
	private GotoDefinitionAction _gotoDefAction;
	private OpenInJSDocAction _showDoc;
	private SaveAction _saveAction;
	private CloseFileAction _closeAction;
	private RenameFileAction _renameFileAction;
	private DeleteFileAction _deleteFileAction;
	private RefreshAction _refreshAction;
	private ReloadAction _reloadAction;
	private OpenLocationAction _openLoactionAction;
	private OpenInWebBrowserAction _openInBrowserAction;

	public MainMenu() {
		createActions();

		add(buildFileMenu());
		add(buildEditMenu());
		add(buildNavigateMenu());
		add(buildWindowMenu());
		add(buildHelp());
	}

	private static JMenu buildHelp() {
		JMenu menu = new RayoMenu("Help");
		menu.add(new AbstractAction("Tutorial") {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().browse(new URL("https://phasereditor.boniatillo.com/blog/rayo").toURI());
				} catch (IOException | URISyntaxException e1) {
					e1.printStackTrace();
					showMessageDialog(null, e, "Error", ERROR_MESSAGE);
				}
			}
		});
		menu.add(new AbstractAction("Twitter") {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					Desktop.getDesktop().browse(new URL("https://twitter.com/boniatillo_com").toURI());
				} catch (IOException | URISyntaxException e1) {
					e1.printStackTrace();
					showMessageDialog(null, e, "Error", ERROR_MESSAGE);
				}
			}
		});
		menu.addSeparator();
		menu.add(new AbstractAction("About") {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO: ugly about
				StringBuilder msg = new StringBuilder();
				msg = new StringBuilder();
				msg.append("<html><h1>Rayo</h1>");
				msg.append("<small>Version: 0.9.1 PREVIEW</small>");
				msg.append("<br><br>Copyright Arian Fornaris 2016");
				msg.append("<br><br><h2>Thanks</h2>");
				msg.append("tern.java (https://github.com/angelozerr/tern.java)");
				msg.append("<br>ternjs (https://github.com/ternjs/tern)");
				msg.append("<br>BeautifyJS (https://github.com/beautify-web/js-beautify)");
				msg.append("<br>RSyntax (https://github.com/bobbylight/RSyntaxTextArea)");
				msg.append("<br>Phaser (https://phaser.io)");
				msg.append("<br>Some icons are part of the IntelliJ IDEA Community Edition");
				msg.append(
						"<br><br>Special thanks for Angelo Zerr, author of tern.java,<br>for his great work and excellent support.");

				JOptionPane.showMessageDialog(null, msg, "About", INFORMATION_MESSAGE,
						new ImageIcon(((JFrame) Workbench.getInstance()).getIconImages().get(2)));
			}
		});
		return menu;
	}

	private static JMenu buildEditMenu() {
		JMenu menu = new RayoMenu("Edit");
		menu.add(new UndoAction());
		menu.add(new RedoAction());
		menu.add(new MultipleCutAction());
		menu.add(new MultipleCopyAction());
		menu.add(new MultiplePasteAction());
		menu.addSeparator();
		menu.add(new SelectAllAction());
		menu.addSeparator();
		menu.add(new FormatAction());
		menu.add(new UnindentAction());
		menu.add(new DeleteLineAction());
		menu.add(new LineMoveAction(-1));
		menu.add(new LineMoveAction(1));
		menu.addSeparator();
		menu.add(new FindTextAction());
		menu.add(new ReplaceTextAction());
		menu.addSeparator();
		menu.add(new RenameSymbolAction());

		return menu;
	}

	private static JMenu buildWindowMenu() {
		JMenu menu = new JMenu("Window");
		JCheckBoxMenuItem item = new JCheckBoxMenuItem("Left Panel", true);
		item.setAccelerator(KeyStroke.getKeyStroke("control shift L"));
		menu.add(item).addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				Workbench.getInstance().togglePanel(IWorkbench.LEFT_PANEL);
			}
		});

		item = new JCheckBoxMenuItem("Bottom Panel", true);
		item.setAccelerator(KeyStroke.getKeyStroke("control shift B"));
		menu.add(item).addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {

				Workbench.getInstance().togglePanel(IWorkbench.BOTTOM_PANEL);
			}
		});
		return menu;
	}

	private JMenu buildFileMenu() {
		JMenu menu = new RayoMenu("File");
		menu.add(_closeAction);
		menu.add(_saveAction);
		menu.addSeparator();
		menu.add(_renameFileAction);
		menu.add(_deleteFileAction);
		menu.addSeparator();
		menu.add(_openInBrowserAction);
		menu.add(_openLoactionAction);
		menu.addSeparator();
		menu.add(_refreshAction);
		menu.add(_reloadAction);
		return menu;
	}

	private void createActions() {
		// file actions
		_closeAction = new CloseFileAction();
		_saveAction = new SaveAction();
		_renameFileAction = new RenameFileAction();
		_deleteFileAction = new DeleteFileAction();
		_refreshAction = new RefreshAction();
		_reloadAction = new ReloadAction();
		_openInBrowserAction = new OpenInWebBrowserAction();
		_openLoactionAction = new OpenLocationAction();

		// navigate actions
		_outlineAction = new OutlineAction();
		_gotoDefAction = new GotoDefinitionAction();
		_showDoc = new OpenInJSDocAction();
	}

	private JMenu buildNavigateMenu() {
		JMenu menu = new RayoMenu("Navigate");
		menu.add(new GoToLineAction());
		menu.add(_outlineAction);
		menu.add(_gotoDefAction);
		menu.addSeparator();
		menu.add(_showDoc);
		menu.add(new ShowInFileTreeAction());
		return menu;
	}

	@Override
	public RefreshAction getRefreshAction() {
		return _refreshAction;
	}

	@Override
	public OpenLocationAction getOpenLocationAction() {
		return _openLoactionAction;
	}

	@Override
	public OpenInWebBrowserAction getOpenInBrowserAction() {
		return _openInBrowserAction;
	}

	@Override
	public ReloadAction getReloadAction() {
		return _reloadAction;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rayo.ui.IMainMenu#getDeleteFileAction()
	 */
	@Override
	public DeleteFileAction getDeleteFileAction() {
		return _deleteFileAction;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see rayo.ui.IMainMenu#getRenameFileAction()
	 */
	@Override
	public RenameFileAction getRenameFileAction() {
		return _renameFileAction;
	}
}
