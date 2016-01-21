package rayo.ui.actions;

import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.text.BadLocationException;

import org.fife.rsta.ui.GoToDialog;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import rayo.ui.Workbench;
import rayo.ui.editors.AbstractTextEditor;

public class GoToLineAction extends TextEditorAction {

	private static final long serialVersionUID = 1L;

	public GoToLineAction() {
		super("Go To Line");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control L"));
	}

	@Override
	protected void actionPefformed(AbstractTextEditor editor) {
		GoToDialog dialog = new GoToDialog(Workbench.getInstance().getWindow());
		RSyntaxTextArea textArea = editor.getTextArea();
		dialog.setMaxLineNumberAllowed(textArea.getLineCount());
		dialog.setVisible(true);
		int line = dialog.getLineNumber();
		if (line > 0) {
			try {
				textArea.setCaretPosition(textArea.getLineStartOffset(line - 1));
			} catch (BadLocationException ble) { // Never happens
				UIManager.getLookAndFeel().provideErrorFeedback(textArea);
				ble.printStackTrace();
			}
		}

	}

}
