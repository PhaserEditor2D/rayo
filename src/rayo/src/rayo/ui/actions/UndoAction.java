package rayo.ui.actions;

import javax.swing.KeyStroke;

import rayo.ui.editors.AbstractTextEditor;

public class UndoAction extends TextEditorAction {
	private static final long serialVersionUID = 1L;

	public UndoAction() {
		super("Undo");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Z"));
	}

	@Override
	protected boolean validateCustom(AbstractTextEditor editor) {
		return editor.getTextArea().canUndo();
	}

	@Override
	protected void actionPefformed(AbstractTextEditor editor) {
		editor.getTextArea().undoLastAction();
	}

}
