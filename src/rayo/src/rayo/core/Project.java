package rayo.core;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JOptionPane;

import org.json.JSONArray;
import org.json.JSONObject;

import rayo.ui.IWorkbench;
import rayo.ui.Workbench;

public class Project {
	private static final Project _global = new Project();

	public static Project getInstance() {
		return _global;
	}

	private RayoFile _root;
	private TernManager _ternManager;
	private JSONObject _config;
	private JSONArray _foldersConfig;

	private Project() {
		_ternManager = new TernManager();

		JSONObject globalConfig = getWorkbench().getConfig();
		if (!globalConfig.has("rayo.project")) {
			JSONObject obj = new JSONObject();
			obj.put("folders", new JSONArray());
			obj.put("lastFolder", (String) null);
			obj.put("rayo.project", false);
			globalConfig.put("rayo.project", obj);
		}
		_config = globalConfig.getJSONObject("rayo.project");
		_foldersConfig = _config.getJSONArray("folders");
	}

	public Path loadInitialFolder() {
		String foldername = _config.optString("lastFolder", null);
		if (foldername != null) {
			Path path = Paths.get(foldername);
			if (Files.exists(path)) {
				return path;
			}
			showMessageDialog(null, "Folder '" + path.toString() + "' not found.", "Open", ERROR_MESSAGE);
		}
		return null;
	}

	public TernManager getTernManager() {
		return _ternManager;
	}

	public void setPath(Path path, Runnable finished) {
		try {
			_root = new RayoFile(null, path);
			_root.computeExclusionState();
			
			_config.put("lastFolder", path.toAbsolutePath().toString());
			if (!getRecentFolders().contains(path)) {
				_foldersConfig.put(path.toString());
			}
			getWorkbench().saveConfig();

			// submit all files to tern
			new Thread(new Runnable() {

				@SuppressWarnings("synthetic-access")
				@Override
				public void run() {
					try {
						getTernManager().startTernServer(Paths.get("."), path);
						getProjectFolder().walk(Project.this::maybeSubmitFile);
					} catch (IOException e) {
						e.printStackTrace();
						JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
					}
					
					if (finished != null) {
						finished.run();
					}
				}
			}).start();

		} catch (Exception e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
		}
	}

	private void maybeSubmitFile(RayoFile file) {
		try {
			getTernManager().updateFile(file);
		} catch (Exception e) {
			e.printStackTrace();
			showMessageDialog(null, e, "Error", ERROR_MESSAGE);
		}
	}

	private static IWorkbench getWorkbench() {
		return Workbench.getInstance();
	}

	public RayoFile getProjectFolder() {
		return _root;
	}

	public Path getParent(Path child) {
		return child.equals(_root.getFilePath()) ? null : child.getParent();
	}

	public List<Path> getRecentFolders() {
		List<Path> list = new ArrayList<>();
		JSONArray array = _config.getJSONArray("folders");
		for (int i = 0; i < array.length(); i++) {
			list.add(Paths.get(array.getString(i)));
		}
		return list;
	}

	public RayoFile findFile(String fileId) {
		if (fileId == null) {
			return null;
		}
		return _root.findFile(fileId);
	}

	public boolean refresh() {
		return _root.refresh();
	}
}
