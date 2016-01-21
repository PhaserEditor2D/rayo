package rayo.ui.views.problems;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.UIManager;
import javax.swing.table.DefaultTableCellRenderer;

import rayo.core.Problem;
import rayo.core.Project;
import rayo.core.RayoFile;
import rayo.ui.IEditor;
import rayo.ui.IProblemView;
import rayo.ui.Workbench;

public class ProblemsView extends JPanel implements IProblemView {

	private static final long serialVersionUID = 1L;
	private JTable _table;
	private ProblemsTableModel _model;

	public ProblemsView() {
		setLayout(new BorderLayout());
		_table = new JTable();
		_table.setAutoCreateRowSorter(true);
		_table.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		_table.setModel(_model = new ProblemsTableModel(Collections.emptyList()));
		_table.setDefaultRenderer(Problem.class, new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
						column);
				Problem p = (Problem) value;
				label.setIcon(p.getSeverity().getIcon());
				label.setText(p.getMessage());
				return label;
			}

		});
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer();
		renderer.setIcon(UIManager.getIcon("Tree.leafIcon"));
		_table.setDefaultRenderer(Path.class, renderer);
		_table.addMouseListener(new MouseAdapter() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					openProblem();
				}
			}
		});
		add(new JScrollPane(_table), BorderLayout.CENTER);
	}

	public synchronized void refresh() {
		List<Problem> list = new ArrayList<>();
		RayoFile root = Project.getInstance().getProjectFolder();
		root.walk(file -> {
			list.addAll(file.getProblems());
		});

		list.sort((a, b) -> {
			return a.getSeverity().compareTo(b.getSeverity());
		});

		_table.setModel(_model = new ProblemsTableModel(list));
	}

	private void openProblem() {
		int row = _table.getSelectionModel().getMinSelectionIndex();

		if (row == -1) {
			return;
		}

		Problem p = _model.getProblems().get(row);
		RayoFile file = p.getFile();
		IEditor editor = Workbench.getInstance().openFile(file);
		editor.setCaretPosition(p.getFrom());
	}
}
