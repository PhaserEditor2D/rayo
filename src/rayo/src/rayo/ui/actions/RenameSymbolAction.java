package rayo.ui.actions;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.QUESTION_MESSAGE;
import static javax.swing.JOptionPane.showInputDialog;
import static javax.swing.JOptionPane.showMessageDialog;

import java.io.IOException;
import java.nio.file.Files;
import java.util.Map;
import java.util.Map.Entry;

import javax.swing.KeyStroke;

import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;

import rayo.core.Change;
import rayo.core.ChangeSet;
import rayo.core.Project;
import rayo.core.RayoFile;
import rayo.core.RefsQuery;
import rayo.core.TernManager;
import rayo.ui.IEditorStack;
import rayo.ui.Workbench;
import rayo.ui.editors.AbstractTextEditor;
import rayo.ui.editors.js.JavaScriptEditor;

public class RenameSymbolAction extends TextEditorAction {
	private static final long serialVersionUID = 1L;

	public RenameSymbolAction() {
		super("Symbol Rename");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("alt shift R"));
	}

	@Override
	protected boolean validateCustom(AbstractTextEditor editor) {
		return editor instanceof JavaScriptEditor;
	}

	@Override
	protected void actionPefformed(AbstractTextEditor editor) {
		RefsQuery query = new RefsQuery(editor);
		Project project = Project.getInstance();
		IEditorStack stack = Workbench.getInstance().getEditorStack();

		TernManager tern = project.getTernManager();
		if (tern.checkDisposedAndShowMessage()) {
			return;
		}

		tern.queryRefs(query);
		if (query.getCountRefs() == 0) {
			return;
		}

		Map<String, ChangeSet> result = query.getResult();

		String newName;
		{
			ChangeSet set = result.get(editor.getFile().getId());
			Change first = set.getChanges().get(0);
			String currentName = editor.getContent().substring((int) first.getStart(), (int) first.getEnd());
			String message = "<html>Enter the new name<br><i style='color:gray'><small>Found " + query.getCountRefs()
					+ " references in " + query.getCountFiles() + " files</i></small>";
			newName = (String) showInputDialog(null, message, "Rename", QUESTION_MESSAGE, null, null, currentName);
			if (newName == null || newName.equals(currentName)) {
				return;
			}
		}

		for (Entry<String, ChangeSet> entry : result.entrySet()) {
			RayoFile file = Project.getInstance().findFile(entry.getKey());
			AbstractTextEditor editor2 = (AbstractTextEditor) stack.getEditorFor(file);

			String master;

			if (editor2 == null) {
				master = file.getContent();
			} else {
				master = editor2.getContent();
			}

			StringBuilder sb = new StringBuilder();

			int start = 0;

			for (Change change : entry.getValue().getChanges()) {
				String s = master.substring(start, (int) change.getStart());
				sb.append(s);
				sb.append(newName);
				start = (int) change.getEnd();
			}
			sb.append(master.substring(start));

			if (editor2 == null) {
				try {
					Files.write(file.getFilePath(), sb.toString().getBytes());
				} catch (IOException e) {
					e.printStackTrace();
					showMessageDialog(null, e, "Rename", ERROR_MESSAGE);
				}
			} else {
				RSyntaxTextArea textArea = editor2.getTextArea();
				int pos = textArea.getCaretPosition();
				textArea.beginAtomicEdit();
				textArea.setText(sb.toString());
				textArea.endAtomicEdit();
				textArea.setCaretPosition(pos);
				file.setEditingContent(sb.toString());
			}
			tern.updateFile(file);
		}
	}
}
