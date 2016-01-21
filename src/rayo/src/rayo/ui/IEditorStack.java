package rayo.ui;

import java.util.List;

import rayo.core.RayoFile;

public interface IEditorStack {

	IEditor getActiveEditor();

	List<IEditor> getEditors();

	IEditor openFile(RayoFile file);

	void closeEditor(RayoFile file);

	IEditor getEditorFor(RayoFile file);

	void closeAll();

	int getTabSize();

	void setTabSize(int spacesCount);

	boolean isTabsEmulated();

	void setTabsEmulated(boolean useSpaces);

	void updateEditorsStatusBar();

	void addListener(IEditorStackListener l);

	void removeListener(IEditorStackListener l);

}
