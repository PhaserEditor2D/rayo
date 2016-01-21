package rayo.ui.editors;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import rayo.ui.IEditor;
import rayo.ui.IEditorStack;
import rayo.ui.IEditorStackListener;
import rayo.ui.Workbench;

public class EditorListModel extends AbstractTableModel implements IEditorStackListener {

	private static final long serialVersionUID = 1L;

	private List<IEditor> _editors;

	private static IEditorStack getEditorStack() {
		return Workbench.getInstance().getEditorStack();
	}

	public EditorListModel() {
		_editors = getEditorStack().getEditors();
		getEditorStack().addListener(this);
	}

	@Override
	public int getRowCount() {
		return _editors.size();
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		return _editors.get(rowIndex);
	}

	@Override
	public void editorStackChanged() {
		_editors = getEditorStack().getEditors();
		fireTableStructureChanged();
	}

}
