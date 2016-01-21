package rayo.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import rayo.core.Project;
import rayo.ui.Workbench;

public class RefreshAction extends AbstractAction {
	private static final long serialVersionUID = 1L;

	public RefreshAction() {
		super("Refresh");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F5"));
		Workbench.getInstance().addValidator(new Runnable() {

			@Override
			public void run() {
				setEnabled(Project.getInstance().getProjectFolder() != null);
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		new Thread(new Runnable() {

			@Override
			public void run() {
				Workbench.getInstance().refreshFiles();
			}
		}).start();
	}

}
