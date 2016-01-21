package rayo.ui.actions;

import javax.swing.AbstractAction;
import javax.swing.Icon;

import rayo.ui.IEditor;
import rayo.ui.Workbench;

public abstract class ActiveEditorAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	public ActiveEditorAction() {
		super();
		init();
	}

	public ActiveEditorAction(String name, Icon icon) {
		super(name, icon);
		init();
	}

	public ActiveEditorAction(String name) {
		super(name);
		init();
	}

	private void init() {
		Workbench.getInstance().addValidator(new Runnable() {

			@Override
			public void run() {
				IEditor activeEditor = Workbench.getInstance().getActiveEditor();
				if (activeEditor == null) {
					setEnabled(false);
				} else {
					setEnabled(isEnabledForEditor(activeEditor));
				}
			}
		});
	}

	@SuppressWarnings({ "static-method", "unused" })
	protected boolean isEnabledForEditor(IEditor editor) {
		return true;
	}

}
