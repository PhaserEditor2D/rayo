package rayo.ui;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static rayo.ui.widgets.WidgetUtils.createTabbedPane;
import static rayo.ui.widgets.WidgetUtils.createVertSplitPane;

import java.awt.BorderLayout;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.imageio.ImageIO;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSplitPane;
import javax.swing.JTabbedPane;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import javafx.application.Application;
import phasereditor.chains.core.ChainsModel;
import phasereditor.inspect.core.jsdoc.PhaserJSDoc;
import rayo.core.Beautify;
import rayo.core.Project;
import rayo.core.RayoFile;
import rayo.core.TernManager;
import rayo.core.WebServer;
import rayo.ui.editors.js.autocomplete.JavaScriptCompletionProvider;
import rayo.ui.views.chains.ChainsView;
import rayo.ui.views.jsdoc.JSDocView;
import rayo.ui.views.problems.ProblemsView;
import rayo.ui.views.project.ProjectView;
import rayo.ui.widgets.WidgetUtils;

@SuppressWarnings("synthetic-access")
public class Workbench extends JFrame implements IWorkbench {
	private static final long serialVersionUID = 1L;

	public static final String VERSION = "0.9.9";
	public static final String RAYO_AND_VERSION = "Rayo v" + VERSION;

	private ProjectView _projectView;

	private MainMenu _mainMenu;

	private JTabbedPane _bottomTabbedPane;

	private JSDocView _docView;

	private JSONObject _config;

	private EditorStack _editorStack;

	private static Workbench _instance;
	private List<Runnable> _validators;

	private ProblemsView _problemsView;

	private JPanel _statusPanel;

	private ChainsView _chainsView;

	private List<Runnable> _readySignal;

	protected boolean _ready;

	JSplitPane _vertSplit;

	private JSplitPane _horizSplit;

	private String _activeState;

	public Workbench() {
		super("Rayo");
		_instance = this;
		_validators = new CopyOnWriteArrayList<>();
		_readySignal = new ArrayList<>();

		try {
			setIconImages(Arrays.asList(

			ImageIO.read(getClass().getResourceAsStream("/icons/logo16.png")),

			ImageIO.read(getClass().getResourceAsStream("/icons/logo32.png")),

			ImageIO.read(getClass().getResourceAsStream("/icons/logo48.png")),

			ImageIO.read(getClass().getResourceAsStream("/icons/logo64.png"))));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private void createContent() {
		WidgetUtils.setLaf();

		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setSize(800, 600);
		setExtendedState(MAXIMIZED_BOTH);

		_editorStack = new EditorStack();

		_projectView = new ProjectView();

		_docView = new JSDocView();

		_problemsView = new ProblemsView();

		_chainsView = new ChainsView();

		_bottomTabbedPane = createTabbedPane();
		_bottomTabbedPane.putClientProperty("rayo.paintBorderEdge.left", Boolean.FALSE);
		_bottomTabbedPane.addTab("Problems", _problemsView);
		_bottomTabbedPane.addTab("Chains", _chainsView);
		_bottomTabbedPane.addTab("JSDoc", _docView);

		_mainMenu = new MainMenu();
		setJMenuBar(_mainMenu);

		JPanel editorAndBottomPane = new JPanel(new BorderLayout(0, 0));
		{
			_vertSplit = createVertSplitPane(_editorStack, _bottomTabbedPane);
			_vertSplit.addComponentListener(new ComponentAdapter() {
				@Override
				public void componentResized(ComponentEvent e) {
					_vertSplit.setDividerLocation(0.6);
				}
			});

			_statusPanel = new JPanel(new BorderLayout(0, 0));
			// _statusPanel.setPreferredSize(new Dimension(100, 30));

			editorAndBottomPane.add(_vertSplit, BorderLayout.CENTER);
			editorAndBottomPane.add(_statusPanel, BorderLayout.SOUTH);
		}

		_horizSplit = WidgetUtils.createHorizSplitPane(_projectView, editorAndBottomPane);
		_horizSplit.addComponentListener(new ComponentAdapter() {

			@Override
			public void componentResized(ComponentEvent e) {
				_horizSplit.setDividerLocation(0.2);
			}
		});

		JPanel content = new JPanel(new BorderLayout());
		content.add(_horizSplit, BorderLayout.CENTER);
		setContentPane(content);

		// actions
		_projectView.createActions();
	}

	@Override
	protected void processWindowEvent(WindowEvent e) {
		if (e.getID() == WindowEvent.WINDOW_CLOSING) {
			for (IEditor editor : _editorStack.getEditors()) {
				if (!editor.getFile().askForSaveBeforeShutdown()) {
					return;
				}
			}
			saveState();
		}

		super.processWindowEvent(e);
	}

	private void loadState() {
		RayoFile root = Project.getInstance().getProjectFolder();
		if (root == null) {
			return;
		}

		// open files
		try {
			JSONObject config = getConfig();
			JSONObject state = config.optJSONObject("state");
			Path folder = root.getFilePath();
			JSONObject currentState = state.getJSONObject(folder.toString());

			{
				JSONArray list = currentState.optJSONArray("openFiles");
				for (int i = 0; i < list.length(); i++) {
					try {
						JSONObject obj = list.getJSONObject(i);
						String fname = obj.getString("file");
						int offset = obj.getInt("cursor");
						boolean expanded = obj.optBoolean("expanded", false);
						Path path = Paths.get(fname);
						if (Files.exists(path) && !Files.isDirectory(path)) {
							RayoFile found = root.findFile(path);

							if (found == null) {
								continue;
							}

							found.setLastCaretPosition(offset);

							// TODO: do a lazy open (do not load content)
							openFile(found);

							if (expanded) {
								_projectView.expandNode(found);
							}
						}
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			{
				JSONArray list = currentState.optJSONArray("expandedFiles");
				for (int i = 0; i < list.length(); i++) {
					try {
						String fname = list.getString(i);
						Path path = Paths.get(fname);
						RayoFile found = root.findFile(path);
						_projectView.expandNode(found);
					} catch (Exception e) {
						e.printStackTrace();
					}
				}
			}

			{
				String fname = currentState.optString("activeEditor", null);
				if (fname != null) {
					Path path = Paths.get(fname);
					if (!Files.isDirectory(path)) {
						RayoFile found = root.findFile(path);
						if (found != null) {
							openFile(found);
						}
					}
				}
			}

			// editor stack
			// TODO: move this to a method on EditorStack
			_editorStack.setTabSize(config.optInt("editor.tabSize", 4));
			_editorStack.setTabsEmulated(config.optBoolean("editor.tabsEmulated", false));

			// javascript editors

			JavaScriptCompletionProvider.getInstance().loadState(getConfig());
			_editorStack.updateEditorsStatusBar();

		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void saveState() {
		RayoFile projectFolder = Project.getInstance().getProjectFolder();
		if (projectFolder == null) {
			return;
		}

		JSONObject config = getConfig();
		JSONObject state = config.optJSONObject("state");
		if (state == null) {
			config.put("state", state = new JSONObject());
		}

		Path folder = projectFolder.getFilePath();
		String rootname = folder.toString();
		JSONObject currentState = state.optJSONObject(rootname);

		if (currentState == null) {
			state.put(rootname, currentState = new JSONObject());
		}

		{
			JSONArray list = new JSONArray();
			for (IEditor editor : _editorStack.getEditors()) {
				RayoFile file = editor.getFile();
				JSONObject obj = new JSONObject();
				obj.put("file", file.getFilePath().toString());
				int offset = editor.getCaretPosition();
				obj.put("cursor", offset);
				list.put(obj);
			}
			currentState.put("openFiles", list);
		}

		{
			JSONArray list = new JSONArray();
			for (RayoFile file : _projectView.getExpandedNodes()) {
				list.put(file.getFilePath().toString());
			}
			currentState.put("expandedFiles", list);
		}

		if (getActiveEditor() != null) {
			currentState.put("activeEditor", getActiveEditor().getFile().getFilePath().toString());
		}

		// TODO: move this to a method on EditorStack
		config.put("editor.tabSize", _editorStack.getTabSize());
		config.put("editor.tabsEmulated", _editorStack.isTabsEmulated());

		JavaScriptCompletionProvider.getInstance().saveState(getConfig());

		saveConfig();
	}

	@Override
	public void refreshFiles() {
		Project project = Project.getInstance();
		if (project.refresh()) {
			_projectView.refresh();
		}

		TernManager tern = project.getTernManager();
		if (tern.checkDisposedAndShowMessage()) {
			return;
		}

		RayoFile root = project.getProjectFolder();
		root.walk(file -> tern.updateFile(file));

		refreshProblems();
	}

	@Override
	public void refreshProblems() {
		_problemsView.refresh();
		_editorStack.refreshProblems();
		_projectView.refreshProblems();
	}

	@Override
	public IProjectView getProjectView() {
		return _projectView;
	}

	@Override
	public IProblemView getProblemsView() {
		return _problemsView;
	}

	@Override
	public IDocView getDocView() {
		return _docView;
	}

	@Override
	public IMainMenu getMainMenu() {
		return _mainMenu;
	}

	@Override
	public IEditor getActiveEditor() {
		return getEditorStack().getActiveEditor();
	}

	@Override
	public IEditorStack getEditorStack() {
		return _editorStack;
	}

	@Override
	public void openProjectFolder(Path folder) {
		saveState();

		IEditorStack editorStack = getEditorStack();
		for (IEditor editor : editorStack.getEditors()) {
			if (!closeFile(editor.getFile())) {
				return;
			}
		}

		_projectView.openFolder(folder);
		loadState();

		new Thread(new Runnable() {

			@Override
			public void run() {
				WebServer.start(folder);
			}
		}).start();
	}

	@Override
	public IEditor openFile(RayoFile file) {
		Path root = Project.getInstance().getProjectFolder().getFilePath();
		setTitle(root.relativize(file.getFilePath()) + " (" + root.getFileName() + ")" + " - Rayo");
		IEditor editor = getEditorStack().openFile(file);
		if (editor != null) {
			editor.activated();
			editor.focus();
			JComponent statusComp = editor.getStatusComponent();
			_statusPanel.removeAll();
			if (statusComp != null) {
				_statusPanel.add(statusComp, BorderLayout.CENTER);
			}
			_statusPanel.validate();
		}
		return editor;
	}

	public static IWorkbench getInstance() {
		return _instance;
	}

	@Override
	public boolean closeEditingFile() {
		return closeFile(getActiveEditor().getFile());
	}

	@Override
	public boolean closeFile(RayoFile file, boolean force) {
		if (!file.closing(force)) {
			return false;
		}
		_editorStack.closeEditor(file);
		return true;
	}

	@Override
	public boolean closeFile(RayoFile file) {
		return closeFile(file, false);
	}

	public boolean isReady() {
		return _ready;
	}

	@Override
	public void executeOnReady(Runnable action) {
		synchronized (_readySignal) {
			if (_ready) {
				action.run();
			} else {
				_readySignal.add(action);
			}
		}
	}

	public void start() {
		// load the early process
		new Thread(new Runnable() {

			@SuppressWarnings("unused")
			@Override
			public void run() {
				PhaserJSDoc.getInstance();
				ChainsModel.getInstance();

				synchronized (_readySignal) {
					_ready = true;
					for (Runnable run : _readySignal) {
						run.run();
					}
				}

				// I don't know why the file chooser delays so much the first
				// time it opens in my machine, I guess it has to query all
				// mapped networks and connected devices.
				try {
					Thread.sleep(1000);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				new JFileChooser();
			}
		}).start();
		// --

		loadConfig();

		// load last folder
		Project project = Project.getInstance();
		Path initPath = project.loadInitialFolder();

		createContent();

		if (initPath != null) {
			openProjectFolder(initPath);
			_problemsView.refresh();
		}

		// show workbench
		setVisible(true);

		{
			IEditor editor = getActiveEditor();
			if (editor != null) {
				editor.focus();
			}
		}

		new Thread(new Runnable() {

			@Override
			public void run() {
				while (true) {
					try {
						Thread.sleep(400);
					} catch (InterruptedException e) {
						e.printStackTrace();
					}
					for (Runnable validator : _validators) {
						try {
							validator.run();
						} catch (Exception e) {
							showMessageDialog(null, e, "Error", ERROR_MESSAGE);
						}
					}

				}
			}
		}).start();

		Application.launch(Beautify.class);
	}

	@Override
	public void addValidator(Runnable validator) {
		_validators.add(validator);
	}

	private void loadConfig() {
		String uhome = System.getProperty("user.home");
		Path path = Paths.get(uhome).resolve(".rayo").resolve("config.json");
		if (Files.exists(path)) {
			try (InputStream input = Files.newInputStream(path)) {
				_config = new JSONObject(new JSONTokener(input));
			} catch (Exception e) {
				e.printStackTrace();
				showMessageDialog(null, "Error loading configuration file: " + e.getMessage(), "Error", ERROR_MESSAGE);
			}
		}
		if (_config == null) {
			_config = new JSONObject();
		}
	}

	@Override
	public synchronized void saveConfig() {
		String uhome = System.getProperty("user.home");
		Path path = Paths.get(uhome).resolve(".rayo").resolve("config.json");
		try {
			if (!Files.exists(path)) {
				Files.createDirectories(path.getParent());
				Files.createFile(path);
			}
			Files.write(path, _config.toString(4).getBytes());
		} catch (JSONException | IOException e) {
			e.printStackTrace();
			showMessageDialog(null, "Error saving configuration file: " + e.getMessage(), "Error", ERROR_MESSAGE);
		}
	}

	@Override
	public JSONObject getConfig() {
		return _config;
	}

	@Override
	public JFrame getWindow() {
		return this;
	}

	@SuppressWarnings("boxing")
	@Override
	public void togglePanel(String panelId) {
		String k = "rayo.lastDividerLocation";
		switch (panelId) {
		case BOTTOM_PANEL:
			if (_vertSplit.getRightComponent() == null) {
				_vertSplit.setRightComponent(_bottomTabbedPane);
				_vertSplit.setDividerLocation((int) _vertSplit.getClientProperty(k));
			} else {
				_vertSplit.putClientProperty(k, _editorStack.getHeight());
				_vertSplit.setRightComponent(null);
			}
			break;
		case LEFT_PANEL:
			if (_horizSplit.getLeftComponent() == null) {
				_horizSplit.setLeftComponent(_projectView);
				_horizSplit.setDividerLocation((int) _horizSplit.getClientProperty(k));
			} else {
				_horizSplit.setLeftComponent(null);
				_horizSplit.putClientProperty(k, _projectView.getWidth());
			}

			break;
		default:
			return;
		}
	}

	@Override
	public String getActiveState() {
		return _activeState;
	}

	@Override
	public void setActiveState(String state) {
		_activeState = state;
	}
}
