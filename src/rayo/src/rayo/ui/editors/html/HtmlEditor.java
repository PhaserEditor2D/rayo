package rayo.ui.editors.html;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.SyntaxConstants;

import rayo.core.RayoFile;
import rayo.ui.editors.AbstractTextEditor;

public class HtmlEditor extends AbstractTextEditor {

	private static final long serialVersionUID = 1L;

	public HtmlEditor(RayoFile file) {
		super(file);
	}

	@Override
	protected void initTextArea(RSyntaxTextArea textArea) {
		textArea.setSyntaxEditingStyle(SyntaxConstants.SYNTAX_STYLE_HTML);
		textArea.setCodeFoldingEnabled(true);
		textArea.setMarkOccurrences(true);
		textArea.setUseFocusableTips(true);
	}

}
