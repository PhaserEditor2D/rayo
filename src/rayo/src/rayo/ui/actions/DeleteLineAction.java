package rayo.ui.actions;

import javax.swing.KeyStroke;

import rayo.ui.editors.AbstractTextEditor;

public class DeleteLineAction extends TextEditorAction {
	private static final long serialVersionUID = 1L;

	public DeleteLineAction() {
		super("Delete Line");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control D"));
	}

	@Override
	protected void actionPefformed(AbstractTextEditor editor) {
		editor.getTextArea().getActionMap().get("RTA.DeleteLineAction").actionPerformed(null);
	}

}
