package rayo.ui.editors.html;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import rayo.core.RayoFile;
import rayo.ui.editors.AbstractTextEditor;

public class CssEditor extends AbstractTextEditor {
	private static final long serialVersionUID = 1L;

	public CssEditor(RayoFile file) {
		super(file);
	}

	@Override
	protected void initTextArea(RSyntaxTextArea textArea) {
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_CSS);
		textArea.setCodeFoldingEnabled(true);
		textArea.setMarkOccurrences(true);
		textArea.setUseFocusableTips(true);
	}
}
