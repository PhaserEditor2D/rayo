package rayo.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import rayo.core.Project;
import rayo.core.RayoQuery;
import rayo.core.TernManager;
import rayo.ui.IEditor;
import rayo.ui.Workbench;
import rayo.ui.editors.js.outline.OutlineWindow;
import tern.server.protocol.outline.JSNodeRoot;

public class OutlineAction extends ActiveEditorAction {

	private static final long serialVersionUID = 1L;
	private OutlineWindow _dlg;

	public OutlineAction() {
		super("Quick Outline");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("control O"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		TernManager tern = Project.getInstance().getTernManager();

		if (tern.checkDisposedAndShowMessage()) {
			return;
		}

		if (_dlg == null) {
			_dlg = new OutlineWindow(Workbench.getInstance().getWindow());
		}

		IEditor editor = Workbench.getInstance().getActiveEditor();
		JSNodeRoot root = tern.queryOutline(new RayoQuery(editor));
		_dlg.display(root);
	}
}
