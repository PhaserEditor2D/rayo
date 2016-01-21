package rayo.ui.views.problems;

import java.nio.file.Path;
import java.util.List;

import javax.swing.table.AbstractTableModel;

import rayo.core.Problem;
import rayo.core.Project;
import rayo.core.RayoFile;

public class ProblemsTableModel extends AbstractTableModel {
	private static final long serialVersionUID = 1L;
	private List<Problem> _problems;

	public ProblemsTableModel(List<Problem> problems) {
		super();
		_problems = problems;
	}

	public List<Problem> getProblems() {
		return _problems;
	}

	@Override
	public int getRowCount() {
		return _problems.size();
	}

	@Override
	public int getColumnCount() {
		return 4;
	}

	@Override
	public Object getValueAt(int rowIndex, int columnIndex) {
		Problem p = _problems.get(rowIndex);
		switch (columnIndex) {
		case 0:
			return p;
		case 1:
			RayoFile file = p.getFile();
			return Project.getInstance().getProjectFolder().getFilePath().relativize(file.getFilePath());
		case 2:
			return " line " + p.getLine();
		case 3:
			return p.getId();
		default:
			return "";
		}
	}

	@Override
	public Class<?> getColumnClass(int columnIndex) {
		switch (columnIndex) {
		case 0:
			return Problem.class;
		case 1:
			return Path.class;
		default:
			break;
		}
		return super.getColumnClass(columnIndex);
	}

	@Override
	public String getColumnName(int column) {
		switch (column) {
		case 0:
			return "Description";
		case 1:
			return "File";
		case 2:
			return "Line";
		case 3:
			return "Id";
		default:
			return "";
		}
	}

}
