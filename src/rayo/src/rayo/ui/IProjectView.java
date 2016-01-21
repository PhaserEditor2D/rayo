package rayo.ui;

import java.nio.file.Path;

import javax.swing.tree.TreeSelectionModel;

import rayo.core.RayoFile;

public interface IProjectView {

	String ACTIVE_STATE = "project";

	void openFolder(Path folder);

	TreeSelectionModel getSelectionModel();

	/**
	 * @deprecated Use a better way to catch this event
	 * @param file
	 */
	@Deprecated
	void fileWasChanged(RayoFile file);

	/**
	 * @deprecated Use a better way to catch this event
	 * @param file
	 */
	@Deprecated
	void fileWasRemoved(RayoFile file);

	void reveal(RayoFile file);

	boolean isActive();

	void copy();

	void paste();
}
