package rayo.ui.editors.js;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import rayo.core.RayoFile;
import rayo.ui.editors.AbstractTextEditor;

public class JSonEditor extends AbstractTextEditor {
	private static final long serialVersionUID = 1L;

	public JSonEditor(RayoFile file) {
		super(file);
	}

	@Override
	protected void initTextArea(RSyntaxTextArea textArea) {
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_JSON);
		textArea.setCodeFoldingEnabled(true);
		textArea.setMarkOccurrences(true);
		textArea.setUseFocusableTips(true);
	}
}
