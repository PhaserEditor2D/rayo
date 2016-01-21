package rayo.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import rayo.ui.IEditor;
import rayo.ui.Workbench;

public class ShowInFileTreeAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	public ShowInFileTreeAction() {
		super("Show In File Tree");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control T"));
		Workbench.getInstance().addValidator(new Runnable() {

			@Override
			public void run() {
				setEnabled(Workbench.getInstance().getActiveEditor() != null);
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		IEditor editor = Workbench.getInstance().getActiveEditor();
		Workbench.getInstance().getProjectView().reveal(editor.getFile());
	}

}
