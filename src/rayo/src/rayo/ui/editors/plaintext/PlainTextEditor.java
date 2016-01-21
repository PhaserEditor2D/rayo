package rayo.ui.editors.plaintext;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import rayo.core.RayoFile;
import rayo.ui.editors.AbstractTextEditor;

public class PlainTextEditor extends AbstractTextEditor {

	private static final long serialVersionUID = 1L;

	public PlainTextEditor(RayoFile file) {
		super(file);
	}

	@Override
	protected void initTextArea(RSyntaxTextArea textArea) {
		// nothing
	}

}
