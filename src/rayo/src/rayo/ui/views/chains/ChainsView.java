package rayo.ui.views.chains;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;
import static rayo.ui.widgets.WidgetUtils.getCodeFont;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Font;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseEvent;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;

import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.table.DefaultTableCellRenderer;

import phasereditor.chains.core.ChainItem;
import phasereditor.chains.core.ChainsModel;
import phasereditor.chains.core.Line;
import phasereditor.chains.core.Match;
import phasereditor.inspect.core.jsdoc.PhaserMethod;
import rayo.ui.Icons;
import rayo.ui.Workbench;

public class ChainsView extends JPanel {
	private static final long serialVersionUID = 1L;

	private JTable _table;

	private JTextField _searchText;

	private Object _query;

	public ChainsView() {
		setLayout(new BorderLayout());

		Font font = getCodeFont();
		{
			_table = new JTable() {
				private static final long serialVersionUID = 1L;

				@Override
				protected void processMouseEvent(MouseEvent e) {
					super.processMouseEvent(e);

					if (e.getID() == MouseEvent.MOUSE_CLICKED && e.getClickCount() == 2) {
						rowClicked();
					}
				}
			};

			Color color = _table.getSelectionBackground();
			String bg = "rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ")";

			color = _table.getSelectionForeground();
			String fg = "rgb(" + color.getRed() + "," + color.getGreen() + "," + color.getBlue() + ")";

			DefaultTableCellRenderer renderer = new DefaultTableCellRenderer() {
				private static final long serialVersionUID = 1L;

				@Override
				public Component getTableCellRendererComponent(JTable table, Object value, boolean isSelected,
						boolean hasFocus, int row, int column) {
					JLabel label = (JLabel) super.getTableCellRendererComponent(table, value, isSelected, hasFocus, row,
							column);

					Match match = (Match) value;
					String text = "";
					if (match.item instanceof ChainItem) {
						ChainItem item = (ChainItem) match.item;
						String chain = item.getDisplay();
						text = getLabelText(match, chain, item.getDepth() > 1);
						if (chain.startsWith("new ")) {
							label.setIcon(Icons.getPhaserMemberIcon(PhaserMethod.class));
						} else {
							label.setIcon(Icons.getPhaserMemberIcon(item.getPhaserMember()));
						}
					} else if (match.item instanceof Line) {
						Line line = (Line) match.item;
						text = getLabelText(match, line.text, false);
						text += "<font style='color:gray'>[" + line.filename + " - " + line.linenum + "]</font>";
						label.setIcon(Icons.getFileIcon());
					} else {
						String line = (String) match.item;
						text = getLabelText(match, line, false);
						label.setIcon(Icons.getFileIcon());
					}
					label.setText(text);
					return label;
				}

				private String getLabelText(Match match, String line, boolean italic) {
					StringBuilder sb = new StringBuilder();

					String line2 = line.replace("|", " | ");

					int a = match.start;
					int b = a + match.length;

					sb.append("<html>");
					if (italic) {
						sb.append("<i>");
					}
					sb.append(line2.substring(0, a));
					sb.append("<font style='background-color:" + bg + ";color:" + fg + "'>");
					sb.append(line2.substring(a, b) + "</font>");
					sb.append(line2.substring(b));

					return sb.toString();
				}
			};
			_table.setTableHeader(null);
			_table.setFont(font);
			_table.setDefaultRenderer(Object.class, renderer);
		}
		_searchText = new JTextField();
		_searchText.setFont(font);
		_searchText.setText("game.load.");
		_searchText.addKeyListener(new KeyAdapter() {
			@SuppressWarnings("synthetic-access")
			@Override
			public void keyReleased(KeyEvent e) {
				updateFromText();
			}
		});
		add(_searchText, BorderLayout.NORTH);
		add(new JScrollPane(_table), BorderLayout.CENTER);

		Workbench.getInstance().executeOnReady(this::modelIsReady);
	}

	protected void rowClicked() {
		int row = _table.getSelectedRow();
		if (row == -1) {
			return;
		}
		Match match = (Match) _table.getModel().getValueAt(row, 0);
		Object item = match.item;
		if (item instanceof ChainItem) {
			Workbench.getInstance().getDocView().display(((ChainItem) item).getPhaserMember());
		} else if (item instanceof Line) {
			Line line = (Line) item;
			String path = line.filename;
			openUrl(path, line.linenum);

		} else if (item instanceof String) {
			openUrl((String) item, 0);
		}
	}

	private static void openUrl(String path, int linenum) {
		try {
			String path2 = path.replace("+", " ").replace(" ", "%20");
			if (path2.indexOf("labs") != 0) {
				path2 = "examples/" + path2;
			}
			String href = "https://github.com/photonstorm/phaser-examples/blob/master/" + path2 + "#L" + linenum;
			Desktop.getDesktop().browse(new URI(href));
		} catch (IOException | URISyntaxException e) {
			e.printStackTrace();
			showMessageDialog(null, e, "Browser", ERROR_MESSAGE);
		}
	}

	private void updateFromText() {
		String query = _searchText.getText();

		if (_query != null && query.equals(_query)) {
			return;
		}
		_query = query;

		ChainsModel model = ChainsModel.getInstance();
		List<Match> chainsMathes = model.searchChains(query, 100);
		List<Match> examplesMatches = model.searchExamples(query, 100);
		_table.setModel(new ChainsTableModel(chainsMathes, examplesMatches));
	}

	private void modelIsReady() {
		updateFromText();
	}
}
