package rayo.ui.actions;

import static javax.swing.JOptionPane.NO_OPTION;
import static javax.swing.JOptionPane.YES_NO_OPTION;
import static javax.swing.JOptionPane.showConfirmDialog;

import java.awt.event.ActionEvent;
import java.nio.file.Path;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import rayo.core.Project;
import rayo.ui.Workbench;

public class ReloadAction extends AbstractAction {
	private static final long serialVersionUID = 1L;

	public ReloadAction() {
		super("Reload");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("shift F5"));
		Workbench.getInstance().addValidator(new Runnable() {

			@Override
			public void run() {
				setEnabled(Project.getInstance().getProjectFolder() != null);
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		if (showConfirmDialog(null, "Do you want to reload the project?", "Reload", YES_NO_OPTION) == NO_OPTION) {
			return;
		}

		Path root = Project.getInstance().getProjectFolder().getFilePath();
		Workbench.getInstance().openProjectFolder(root);
	}

}
