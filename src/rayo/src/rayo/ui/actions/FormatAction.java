package rayo.ui.actions;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;

import java.util.HashMap;
import java.util.Map;

import javax.swing.KeyStroke;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import rayo.core.Beautify;
import rayo.core.Project;
import rayo.ui.IEditorStack;
import rayo.ui.IWorkbench;
import rayo.ui.Workbench;
import rayo.ui.editors.AbstractTextEditor;
import rayo.ui.editors.html.CssEditor;
import rayo.ui.editors.html.HtmlEditor;

public class FormatAction extends TextEditorAction {

	public FormatAction() {
		super("Format");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control shift F"));
	}

	private static final long serialVersionUID = 1L;

	@Override
	protected void actionPefformed(AbstractTextEditor editor) {
		Map<Object, Object> options = new HashMap<>();

		IWorkbench wb = Workbench.getInstance();
		IEditorStack editorStack = wb.getEditorStack();
		options.put("indent_with_tabs", editorStack.isTabsEmulated() ? Boolean.FALSE : Boolean.TRUE);
		options.put("indent_size", Integer.valueOf(editorStack.getTabSize()));

		RSyntaxTextArea textArea = editor.getTextArea();
		String source = textArea.getText();
		String lang = Beautify.JS_LANG;
		if (editor instanceof HtmlEditor) {
			lang = Beautify.HTML_LANG;
		} else if (editor instanceof CssEditor) {
			lang = Beautify.CSS_LANG;
		}
		long t = currentTimeMillis();
		source = Beautify.beautify(lang, source, options);
		out.println("Format time " + (currentTimeMillis() - t) + "ms");
		int i = editor.getCaretPosition();
		textArea.beginAtomicEdit();
		textArea.setText(source);
		textArea.endAtomicEdit();
		editor.setCaretPosition(Math.min(i, source.length()));

		Project.getInstance().getTernManager().updateFile(editor.getFile());
		wb.refreshProblems();
	}

}
