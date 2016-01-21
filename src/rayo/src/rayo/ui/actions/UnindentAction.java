package rayo.ui.actions;

import javax.swing.KeyStroke;

import rayo.ui.editors.AbstractTextEditor;

public class UnindentAction extends TextEditorAction {
	private static final long serialVersionUID = 1L;

	public UnindentAction() {
		super("Unindent");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("shift TAB"));
	}

	@Override
	protected void actionPefformed(AbstractTextEditor editor) {
		editor.getTextArea().getActionMap().get("RSTA.DecreaseIndentAction").actionPerformed(null);
	}

}
