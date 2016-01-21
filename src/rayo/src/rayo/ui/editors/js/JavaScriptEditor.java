package rayo.ui.editors.js;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JPanel;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import rayo.core.RayoFile;
import rayo.ui.Workbench;
import rayo.ui.editors.AbstractTextEditor;
import rayo.ui.editors.js.autocomplete.JavaScriptCompletionProvider;
import rayo.ui.editors.js.autocomplete.RayoAutoCompletion;
import rayo.ui.editors.js.autocomplete.RayoCompletionProvider;

public class JavaScriptEditor extends AbstractTextEditor {

	private static final long serialVersionUID = 1L;
	private JButton _complOnLettersButton;

	public JavaScriptEditor(RayoFile file) {
		super(file);
	}

	@Override
	protected void createStatusToolbar(JPanel toolbar) {
		_complOnLettersButton = new JButton();
		AbstractAction a = new AbstractAction("COL Off") {
			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				JavaScriptCompletionProvider.getInstance().toggleAutoActivateOnLetters();
				Workbench.getInstance().getEditorStack().updateEditorsStatusBar();
			}
		};
		_complOnLettersButton.setAction(a);
		_complOnLettersButton.setToolTipText("Auto complete on letters.");
		toolbar.add(_complOnLettersButton);

		super.createStatusToolbar(toolbar);
	}

	@Override
	public void updateStatusBar() {
		super.updateStatusBar();

		_complOnLettersButton
				.setText(JavaScriptCompletionProvider.getInstance().isAutoActivateOnLetters() ? "COL On" : "COL Off");
	}

	@Override
	protected void initTextArea(RSyntaxTextArea textArea) {
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JAVASCRIPT);

		if (getFile().isExcluded()) {
			return;
		}

		textArea.setAutoIndentEnabled(true);
		textArea.setAntiAliasingEnabled(true);
		textArea.setCodeFoldingEnabled(true);
		textArea.setMarkOccurrences(true);
		textArea.setUseFocusableTips(true);

		RayoCompletionProvider provider = new RayoCompletionProvider();
		textArea.setToolTipSupplier(provider);
		AutoCompletion ac = new RayoAutoCompletion(provider);
		ac.install(textArea);
	}

}
