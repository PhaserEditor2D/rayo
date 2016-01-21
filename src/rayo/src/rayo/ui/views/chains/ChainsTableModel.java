package rayo.ui.views.chains;

import java.util.List;

import javax.swing.table.AbstractTableModel;

import phasereditor.chains.core.Match;

public class ChainsTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;

	private List<Match> _chains;

	private List<Match> _examples;

	public ChainsTableModel(List<Match> list, List<Match> examplesMatches) {
		super();
		_chains = list;
		_examples = examplesMatches;
	}

	@Override
	public int getRowCount() {
		return _chains.size() + _examples.size();
	}

	@Override
	public int getColumnCount() {
		return 1;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		if (rowIndex < _chains.size()) {
			return _chains.get(rowIndex);
		}
		return _examples.get(rowIndex - _chains.size());
	}

}
