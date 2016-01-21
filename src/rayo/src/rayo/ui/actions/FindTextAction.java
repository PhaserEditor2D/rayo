package rayo.ui.actions;

import javax.swing.KeyStroke;

import rayo.ui.editors.AbstractTextEditor;

public class FindTextAction extends TextEditorAction {
	private static final long serialVersionUID = 1L;

	public FindTextAction() {
		super("Find");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control F"));
	}

	@Override
	protected void actionPefformed(AbstractTextEditor editor) {
		editor.getFindAction().actionPerformed(null);
	}

}
