package rayo.ui.actions;

import javax.swing.KeyStroke;

import rayo.ui.editors.AbstractTextEditor;

public class RedoAction extends TextEditorAction {
	private static final long serialVersionUID = 1L;

	public RedoAction() {
		super("Redo");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control Y"));
	}

	@Override
	protected boolean validateCustom(AbstractTextEditor editor) {
		return editor.getTextArea().canRedo();
	}

	@Override
	protected void actionPefformed(AbstractTextEditor editor) {
		editor.getTextArea().redoLastAction();
	}

}
