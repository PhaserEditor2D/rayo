package rayo.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import rayo.core.Project;
import rayo.core.RayoJSDocQuery;
import rayo.core.TernManager;
import rayo.ui.IDocView;
import rayo.ui.Workbench;

public class OpenInJSDocAction extends ActiveEditorAction {

	private static final long serialVersionUID = 1L;

	public OpenInJSDocAction() {
		super("Open In JSDoc");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F4"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		TernManager tern = Project.getInstance().getTernManager();
		if (tern.checkDisposedAndShowMessage()) {
			return;
		}

		RayoJSDocQuery rquery = new RayoJSDocQuery(Workbench.getInstance().getActiveEditor());
		tern.queryJSDoc(rquery);
		IDocView docview = Workbench.getInstance().getDocView();
		docview.display(rquery);
	}
}