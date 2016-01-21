package rayo.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import rayo.ui.IProjectView;
import rayo.ui.Workbench;
import rayo.ui.editors.AbstractTextEditor;

public class MultipleCopyAction extends MultipleAction {
	private static final long serialVersionUID = 1L;

	public MultipleCopyAction() {
		super("Copy", new StateAction(AbstractTextEditor.ACTIVE_STATE) {

			@Override
			public void actionPerformed(ActionEvent e) {
				((AbstractTextEditor) Workbench.getInstance().getActiveEditor()).getTextArea().copy();
			}
		}, new StateAction(IProjectView.ACTIVE_STATE) {

			@Override
			public void actionPerformed(ActionEvent e) {
				Workbench.getInstance().getProjectView().copy();
			}
		});
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control C"));
	}

}
