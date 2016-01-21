package rayo.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import rayo.ui.IEditor;
import rayo.ui.Workbench;
import rayo.ui.editors.AbstractTextEditor;

public abstract class TextEditorAction extends AbstractAction {
	private static final long serialVersionUID = 1L;

	public TextEditorAction(String name) {
		super(name);
		Workbench.getInstance().addValidator(() -> {
			IEditor editor = Workbench.getInstance().getActiveEditor();
			setEnabled(validate(editor));
		});
	}

	protected boolean validate(IEditor editor) {
		return editor != null && editor instanceof AbstractTextEditor && validateCustom((AbstractTextEditor) editor);
	}

	@SuppressWarnings({ "static-method", "unused" })
	protected boolean validateCustom(AbstractTextEditor editor) {
		return true;
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		AbstractTextEditor textEditor = (AbstractTextEditor) Workbench.getInstance().getActiveEditor();
		actionPefformed(textEditor);
	}

	protected abstract void actionPefformed(AbstractTextEditor editor);
}
