package rayo.ui.actions;

import javax.swing.KeyStroke;

import rayo.ui.editors.AbstractTextEditor;

public class ReplaceTextAction extends TextEditorAction {

	public ReplaceTextAction() {
		super("Replace");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control H"));
	}

	private static final long serialVersionUID = 1L;

	@Override
	protected void actionPefformed(AbstractTextEditor editor) {
		editor.getReplaceAction().actionPerformed(null);
	}

}
