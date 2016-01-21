package rayo.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import rayo.ui.IEditor;
import rayo.ui.Workbench;

public class SaveAction extends ActiveEditorAction {

	private static final long serialVersionUID = 1L;

	public SaveAction() {
		super("Save");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control S"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		IEditor editor = Workbench.getInstance().getActiveEditor();
		editor.save();
	}

	@Override
	protected boolean isEnabledForEditor(IEditor editor) {
		return editor.getFile().isModified();
	}

}
