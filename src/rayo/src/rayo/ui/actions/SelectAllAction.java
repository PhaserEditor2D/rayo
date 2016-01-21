package rayo.ui.actions;

import javax.swing.KeyStroke;

import rayo.ui.editors.AbstractTextEditor;

public class SelectAllAction extends TextEditorAction {
	private static final long serialVersionUID = 1L;

	public SelectAllAction() {
		super("Select All");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control A"));
	}

	@Override
	protected void actionPefformed(AbstractTextEditor editor) {
		editor.getTextArea().selectAll();
	}

}
