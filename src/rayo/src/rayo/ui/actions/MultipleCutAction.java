package rayo.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import rayo.ui.Workbench;
import rayo.ui.editors.AbstractTextEditor;

public class MultipleCutAction extends MultipleAction {
	private static final long serialVersionUID = 1L;

	public MultipleCutAction() {
		super("Cut", new StateAction(AbstractTextEditor.ACTIVE_STATE) {

			@Override
			public void actionPerformed(ActionEvent e) {
				((AbstractTextEditor) Workbench.getInstance().getActiveEditor()).getTextArea().cut();
			}
		});
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control X"));
	}

}
