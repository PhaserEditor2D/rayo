package rayo.ui.actions;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;

import rayo.ui.Workbench;

public class MultipleAction extends AbstractAction {
	private static final long serialVersionUID = 1L;

	public interface IAction {
		public boolean isActive();

		public void actionPerformed(ActionEvent e);
	}

	protected static abstract class StateAction implements IAction {
		private String _state;

		public StateAction(String state) {
			super();
			_state = state;
		}

		@Override
		public boolean isActive() {
			return Workbench.getInstance().getActiveState() == _state;
		}
	}

	private IAction[] _actions;
	private IAction _activeAction;

	public MultipleAction(String label, IAction... actions) {
		super(label);
		_actions = actions;
		Workbench.getInstance().addValidator(() -> {
			for (IAction action : _actions) {
				if (action.isActive()) {
					setEnabled(true);
					_activeAction = action;
					return;
				}
			}
			setEnabled(false);
		});
	}

	@Override
	public void actionPerformed(ActionEvent e) {
		_activeAction.actionPerformed(e);
	}

}
