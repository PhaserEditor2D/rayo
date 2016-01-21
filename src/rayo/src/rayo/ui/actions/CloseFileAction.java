package rayo.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import rayo.ui.Workbench;

public class CloseFileAction extends ActiveEditorAction {
	private static final long serialVersionUID = 1L;

	public CloseFileAction() {
		super("Close");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control W"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		Workbench.getInstance().closeEditingFile();
	}

}
