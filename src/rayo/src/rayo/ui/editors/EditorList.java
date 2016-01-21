package rayo.ui.editors;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JLabel;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;

import rayo.core.RayoFile;
import rayo.ui.IEditor;
import rayo.ui.IWorkbench;
import rayo.ui.Icons;
import rayo.ui.Workbench;

@SuppressWarnings("synthetic-access")
public class EditorList extends JTable {

	private static final long serialVersionUID = 1L;
	private EditorListModel _model;
	protected int _hoverRow;

	public EditorList() {
		_model = new EditorListModel();
		setModel(_model);
		DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
			private static final long serialVersionUID = 1L;
			private boolean _selected;
			private boolean _hover;

			@Override
			public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
					boolean hasFocus, int row, int column) {
				IEditor editor = (IEditor) value;
				IEditor active = Workbench.getInstance().getActiveEditor();
				_selected = editor == active;
				_hover = _hoverRow == row;
				JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, _selected, hasFocus, row,
						column);
				RayoFile file = editor.getFile();
				String fname = file.getFilePath().getFileName().toString();
				label.setText((file.isModified() ? "*" : "") + fname);
				label.setIcon(Icons.getFileIcon(file));
				return label;
			}

			Color _dark = new Color(0, 0, 0, 100);

			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				if (_hover) {
					((Graphics2D) g).setRenderingHint(RenderingHints.KEY_ANTIALIASING,
							RenderingHints.VALUE_ANTIALIAS_ON);
					int s = 10;
					int x = getWidth() - s - 10;
					int y = getHeight() / 2 - s / 2;
					g.setColor(_selected ? getSelectionForeground() : _dark);
					g.drawLine(x, y, x + s, y + s);
					g.drawLine(x + s, y, x, y + s);
				}
			}
		};
		setDefaultRenderer(Object.class, renderer);
		_model.addTableModelListener(new TableModelListener() {

			@Override
			public void tableChanged(TableModelEvent e) {
				changed();
			}
		});
		addMouseListener(new MouseAdapter() {
			@Override
			public void mouseReleased(MouseEvent e) {
				clicked(e);
			}
		});
		addMouseMotionListener(new MouseAdapter() {
			@Override
			public void mouseMoved(MouseEvent e) {
				_hoverRow = rowAtPoint(e.getPoint());
				repaint();
			}

			@Override
			public void mouseExited(MouseEvent e) {
				_hoverRow = -1;
				repaint();
			}
		});
		setSelectionModel(createNullSelectionModel());
		// Color color = UIManager.getColor("menu");
		// color = new Color(color.getRGB());
		// setBackground(color);
	}

	private void clicked(MouseEvent e) {
		int row = rowAtPoint(e.getPoint());
		if (row != -1) {
			IEditor editor = (IEditor) _model.getValueAt(row, 0);
			IWorkbench wb = Workbench.getInstance();
			if (getWidth() - e.getX() < 20) {
				wb.closeFile(editor.getFile());
			} else {
				wb.openFile(editor.getFile());
				repaint();
			}
		}
	}

	private void changed() {
		getParent().validate();
	}

	private static ListSelectionModel createNullSelectionModel() {
		return new ListSelectionModel() {

			@Override
			public void setValueIsAdjusting(boolean valueIsAdjusting) {
				// nothing
			}

			@Override
			public void setSelectionMode(int selectionMode) {
				// nothing
			}

			@Override
			public void setSelectionInterval(int index0, int index1) {
				// nothing
			}

			@Override
			public void setLeadSelectionIndex(int index) {
				// nothing
			}

			@Override
			public void setAnchorSelectionIndex(int index) {
				// nothing
			}

			@Override
			public void removeSelectionInterval(int index0, int index1) {
				// nothing
			}

			@Override
			public void removeListSelectionListener(ListSelectionListener x) {
				// nothing
			}

			@Override
			public void removeIndexInterval(int index0, int index1) {
				// nothing
			}

			@Override
			public boolean isSelectionEmpty() {
				return false;
			}

			@Override
			public boolean isSelectedIndex(int index) {
				return false;
			}

			@Override
			public void insertIndexInterval(int index, int length, boolean before) {
				// nothing
			}

			@Override
			public boolean getValueIsAdjusting() {
				return false;
			}

			@Override
			public int getSelectionMode() {
				return 0;
			}

			@Override
			public int getMinSelectionIndex() {
				return -1;
			}

			@Override
			public int getMaxSelectionIndex() {
				return -1;
			}

			@Override
			public int getLeadSelectionIndex() {
				return -1;
			}

			@Override
			public int getAnchorSelectionIndex() {
				return -1;
			}

			@Override
			public void clearSelection() {
				// nothing
			}

			@Override
			public void addSelectionInterval(int index0, int index1) {
				// nothing
			}

			@Override
			public void addListSelectionListener(ListSelectionListener x) {
				// nothing
			}
		};
	}
}
