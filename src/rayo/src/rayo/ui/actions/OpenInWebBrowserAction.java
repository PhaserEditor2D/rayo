package rayo.ui.actions;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Desktop;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

import rayo.core.Project;
import rayo.core.WebServer;
import rayo.ui.Workbench;

public class OpenInWebBrowserAction extends AbstractAction {
	private static final long serialVersionUID = 1L;

	public OpenInWebBrowserAction() {
		super("Open In Web Browser");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F9"));
		Workbench.getInstance().addValidator(new Runnable() {
			
			@Override
			public void run() {
				setEnabled(Project.getInstance().getProjectFolder() != null);
			}
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		try {
			Desktop.getDesktop().browse(new URL("http://localhost:" + WebServer.getServerPort()).toURI());
		} catch (IOException | URISyntaxException e1) {
			e1.printStackTrace();
			showMessageDialog(null, e, "Error", ERROR_MESSAGE);
		}
	}
}
