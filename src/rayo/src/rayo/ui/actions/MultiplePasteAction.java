package rayo.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import rayo.ui.IProjectView;
import rayo.ui.Workbench;
import rayo.ui.editors.AbstractTextEditor;

public class MultiplePasteAction extends MultipleAction {
	private static final long serialVersionUID = 1L;

	public MultiplePasteAction() {
		super("Paste", new StateAction(AbstractTextEditor.ACTIVE_STATE) {

			@Override
			public void actionPerformed(ActionEvent e) {
				((AbstractTextEditor) Workbench.getInstance().getActiveEditor()).getTextArea().paste();
			}
		}, new StateAction(IProjectView.ACTIVE_STATE) {

			@Override
			public void actionPerformed(ActionEvent e) {
				Workbench.getInstance().getProjectView().paste();
			}
		});
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control V"));
	}

}
