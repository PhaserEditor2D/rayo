package rayo.ui;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.CardLayout;
import java.awt.Desktop;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CopyOnWriteArrayList;

import javax.swing.JPanel;
import javax.swing.plaf.metal.MetalLookAndFeel;

import rayo.core.RayoFile;

public class EditorStack extends JPanel implements IEditorStack {

	private static final long serialVersionUID = 1L;
	private static int ID = 0;

	private CardLayout _layout;
	private Map<RayoFile, String> _fileIdMap;
	private Map<RayoFile, IEditor> _fileEditorMap;
	private List<IEditor> _editors;
	private IEditor _activeEditor;
	private boolean _useSpaces;
	private int _spacesCount;
	private List<IEditorStackListener> _listeners;
	private Map<IEditor, Long> _editorUsedMap;

	public EditorStack() {
		_fileIdMap = new HashMap<>();
		_fileEditorMap = new HashMap<>();
		_editors = new ArrayList<>();
		_listeners = new CopyOnWriteArrayList<>();
		_editorUsedMap = new HashMap<>();

		_layout = new CardLayout();
		setLayout(_layout);

		setOpaque(true);
		setBackground(MetalLookAndFeel.getControlShadow());

		_spacesCount = 4;
		_useSpaces = false;
	}

	@Override
	public void addListener(IEditorStackListener l) {
		_listeners.add(l);
	}

	@Override
	public void removeListener(IEditorStackListener l) {
		_listeners.remove(l);
	}

	@Override
	public int getTabSize() {
		return _spacesCount;
	}

	@Override
	public void setTabSize(int spacesCount) {
		_spacesCount = spacesCount;
		updateEditorsStatusBar();
	}

	@Override
	public boolean isTabsEmulated() {
		return _useSpaces;
	}

	@Override
	public void setTabsEmulated(boolean useSpaces) {
		_useSpaces = useSpaces;
		updateEditorsStatusBar();
	}

	@Override
	public void updateEditorsStatusBar() {
		for (IEditor editor : _fileEditorMap.values()) {
			editor.updateStatusBar();
		}
	}

	@Override
	public IEditor getEditorFor(RayoFile file) {
		IEditor editor = _fileEditorMap.get(file);
		return editor;
	}

	@Override
	public IEditor openFile(RayoFile file) {
		if (EditorFactory.isBinaryFile(file.getFilePath())) {
			try {
				Desktop.getDesktop().open(file.getFilePath().toFile());
			} catch (IOException e) {
				e.printStackTrace();
				showMessageDialog(null, e, "Open File", ERROR_MESSAGE);
			}
			return null;
		}

		IEditor editor;
		String id;
		if (_fileIdMap.containsKey(file)) {
			id = _fileIdMap.get(file);
			editor = _fileEditorMap.get(file);
		} else {
			editor = EditorFactory.createEditorForFile(file);
			editor.setCaretPosition(file.getLastCaretPosition());
			id = "editor" + ID++;
			_fileIdMap.put(file, id);
			_fileEditorMap.put(file, editor);
			_editors.add(editor);
			add(editor.getComponent());
			_layout.addLayoutComponent(editor.getComponent(), id);
		}
		_editorUsedMap.put(editor, Long.valueOf(currentTimeMillis()));

		_layout.show(this, id);
		editor.refreshProblems();
		editor.focus();
		_activeEditor = editor;

		fireStackChanged();

		return editor;
	}

	private void fireStackChanged() {
		for (IEditorStackListener l : _listeners) {
			l.editorStackChanged();
		}
	}

	@Override
	public void closeEditor(RayoFile file) {
		out.println("Closing " + file.getFilePath());
		List<IEditor> list = getEditors();
		for (IEditor editor : list) {
			if (editor.getFile() == file) {
				_fileEditorMap.remove(file);
				_fileIdMap.remove(file);
				_editors.remove(editor);
				_editorUsedMap.remove(editor);

				doCloseRoutine(editor);

				remove(editor.getComponent());
				validate();

				if (editor == _activeEditor) {
					_activeEditor = null;
				}
				break;
			}
		}

		if (!_editors.isEmpty()) {
			IEditor nextEditor = _editors.get(0);
			Long nextTime = _editorUsedMap.get(nextEditor);
			for (IEditor editor : _editors) {
				Long time = _editorUsedMap.get(editor);
				if (time.compareTo(nextTime) > 0) {
					nextEditor = editor;
					nextTime = time;
				}
			}
			openFile(nextEditor.getFile());
		}

		fireStackChanged();
	}

	private static void doCloseRoutine(IEditor editor) {
		editor.getFile().setLastCaretPosition(editor.getCaretPosition());
		editor.closed();
	}

	@Override
	public void closeAll() {
		removeAll();
		Collection<IEditor> editors = _fileEditorMap.values();
		_fileEditorMap.clear();
		_fileIdMap.clear();
		_activeEditor = null;
		for (IEditor editor : editors) {
			doCloseRoutine(editor);
		}
	}

	@Override
	public IEditor getActiveEditor() {
		return _activeEditor;
	}

	@Override
	public List<IEditor> getEditors() {
		return new ArrayList<>(_editors);
	}

	public void refreshProblems() {
		if (_activeEditor != null) {
			_activeEditor.refreshProblems();
		}
	}

}
