package rayo.ui.actions;

import javax.swing.KeyStroke;

import rayo.ui.editors.AbstractTextEditor;

public class LineMoveAction extends TextEditorAction {
	private static final long serialVersionUID = 1L;
	private int _move;

	public LineMoveAction(int move) {
		super(move == 1 ? "Move Line Down" : "Move Line Up");
		putValue(ACCELERATOR_KEY, KeyStroke.getKeyStroke("alt " + (move == -1 ? "UP" : "DOWN")));
		_move = move;
	}

	@Override
	protected void actionPefformed(AbstractTextEditor editor) {
		editor.getTextArea().getActionMap().get(_move == -1 ? "RTA.LineUpAction" : "RTA.LineDownAction")
				.actionPerformed(null);
	}

}
