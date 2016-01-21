package rayo.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.Icon;
import javax.swing.tree.TreeSelectionModel;

import rayo.ui.Workbench;

public abstract class SelectedFileAction extends AbstractAction {

	private static final long serialVersionUID = 1L;

	public SelectedFileAction() {
		super();
		registerValidationListener();
	}

	public SelectedFileAction(String name, Icon icon) {
		super(name, icon);
		registerValidationListener();
	}

	public SelectedFileAction(String name) {
		super(name);
		registerValidationListener();
	}

	protected void registerValidationListener() {
		Workbench.getInstance().addValidator(new Runnable() {

			@Override
			public void run() {
				TreeSelectionModel selection = Workbench.getInstance().getProjectView().getSelectionModel();
				setEnabled(isEnabledForSelection(selection));
			}
		});
	}

	@SuppressWarnings({ "static-method" })
	protected boolean isEnabledForSelection(TreeSelectionModel selection) {
		return selection.getSelectionCount() > 0;
	}

	@Override
	public final void actionPerformed(ActionEvent e) {
		actionPerformed(Workbench.getInstance().getProjectView().getSelectionModel());
	}

	protected abstract void actionPerformed(TreeSelectionModel selection);

}
