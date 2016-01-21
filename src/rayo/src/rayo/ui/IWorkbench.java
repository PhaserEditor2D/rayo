package rayo.ui;

import java.nio.file.Path;

import javax.swing.JFrame;

import org.json.JSONObject;

import rayo.core.RayoFile;

public interface IWorkbench {
	String BOTTOM_PANEL = "bottom";
	String LEFT_PANEL = "left";

	public void togglePanel(String panelId);

	public IEditor getActiveEditor();

	public boolean closeEditingFile();

	/**
	 * Like in {@link #closeFile(RayoFile, boolean)} but <code>force</code> is
	 * set to <code>true</code>.
	 * 
	 * @param file
	 *            The file to close.
	 */
	public boolean closeFile(RayoFile file);

	/**
	 * Close the file (remove from working pane and close the editor) and maybe
	 * ask for confirmation.
	 * 
	 * @param file
	 *            The file to close.
	 * @param force
	 *            If set to <code>true</code> the file is closed without ask for
	 *            confirmation, so if the file was modified, the modifications
	 *            are lost.
	 */
	public boolean closeFile(RayoFile file, boolean force);

	public IEditor openFile(RayoFile file);

	public IDocView getDocView();

	public JFrame getWindow();

	public IProjectView getProjectView();

	public IProblemView getProblemsView();

	public void saveConfig();

	public JSONObject getConfig();

	IMainMenu getMainMenu();

	IEditorStack getEditorStack();

	void addValidator(Runnable validator);

	void openProjectFolder(Path folder);

	public void refreshProblems();

	public void refreshFiles();

	void executeOnReady(Runnable action);

	public String getActiveState();

	public void setActiveState(String state);
}
