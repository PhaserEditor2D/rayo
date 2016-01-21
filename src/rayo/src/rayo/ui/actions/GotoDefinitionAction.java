package rayo.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.KeyStroke;

import rayo.core.Project;
import rayo.core.RayoFile;
import rayo.core.RayoQuery;
import rayo.core.TernManager;
import rayo.ui.IEditor;
import rayo.ui.IWorkbench;
import rayo.ui.Workbench;
import tern.server.protocol.definition.ITernDefinitionCollector;

public class GotoDefinitionAction extends ActiveEditorAction {

	private static final long serialVersionUID = 1L;

	public GotoDefinitionAction() {
		super("Go To Definition");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("F3"));
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		TernManager tern = Project.getInstance().getTernManager();
		
		if (tern.checkDisposedAndShowMessage()) {
			return;
		}
		
		tern.queryDefinition(new RayoQuery(Workbench.getInstance().getActiveEditor()), new ITernDefinitionCollector() {

			@Override
			public void setDefinition(String filename, Long start, Long end) {
				if (start != null && end != null) {
					RayoFile file = Project.getInstance().findFile(filename);
					if (file != null) {
						IWorkbench wb = Workbench.getInstance();
						IEditor editor = wb.openFile(file);
						editor.setCaretPosition(start.intValue());
					}
				}
			}
		});

	}
}