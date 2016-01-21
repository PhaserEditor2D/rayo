package rayo.ui.editors.js.outline;

import static rayo.ui.widgets.WidgetUtils.CLASS_ICON;
import static rayo.ui.widgets.WidgetUtils.METHOD_ICON;
import static rayo.ui.widgets.WidgetUtils.VARIABLE_ICON;
import static rayo.ui.widgets.WidgetUtils.getCodeFont;
import static rayo.ui.widgets.WidgetUtils.getHtmlColor;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.BorderFactory;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JTree;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreePath;

import org.fife.ui.rsyntaxtextarea.RSyntaxUtilities;

import rayo.ui.IEditor;
import rayo.ui.Workbench;
import rayo.ui.widgets.ToolWindow;
import tern.server.protocol.outline.JSNode;

@SuppressWarnings("synthetic-access")
public class OutlineWindow extends ToolWindow {

	private static final long serialVersionUID = 1L;
	private JTree _tree;
	private JTextField _searchField;
	private DefaultMutableTreeNode _defaultTreeRoot;
	private String _lastSearch;

	public OutlineWindow(JFrame owner) {
		super(owner);
		setModal(true);
		setDefaultCloseOperation(HIDE_ON_CLOSE);
	}

	@Override
	protected Component createContent() {
		_searchField = new JTextField();
		_searchField.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_DOWN || e.getKeyCode() == KeyEvent.VK_UP) {
					_tree.requestFocus();
					if (_tree.getSelectionCount() == 0) {
						TreePath path = _tree.getPathForRow(0);
						_tree.setSelectionPath(path);
					}
				} else {
					searchChanged();
				}
			}
		});
		_searchField.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				gotoSelectedElement();
			}
		});
		_searchField.setPreferredSize(new Dimension(10, 30));

		JPanel topPanel = new JPanel(new BorderLayout());
		topPanel.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
		topPanel.add(_searchField, BorderLayout.CENTER);

		_tree = new JTree();
		_tree.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 5));
		_tree.setRootVisible(false);
		_tree.setShowsRootHandles(true);
		_tree.addMouseListener(new MouseAdapter() {
			@Override
			public void mouseClicked(MouseEvent e) {
				if (e.getClickCount() == 2) {
					gotoSelectedElement();
				}
			}
		});
		_tree.addKeyListener(new KeyAdapter() {
			@Override
			public void keyReleased(KeyEvent e) {
				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
					gotoSelectedElement();
				}
			}
		});
		DefaultTreeCellRenderer renderer = new DefaultTreeCellRenderer() {
			private static final long serialVersionUID = 1L;
			private String _fgSelColor = getHtmlColor(getTextSelectionColor());
			private String _bgSelColor = getHtmlColor(getBackgroundSelectionColor());

			@Override
			public Component getTreeCellRendererComponent(JTree tree, Object value, boolean sel, boolean expanded,
					boolean leaf, int row, boolean hasFocus2) {
				JLabel label = (JLabel) super.getTreeCellRendererComponent(tree, value, sel, expanded, leaf, row,
						hasFocus2);
				label.setIcon(VARIABLE_ICON);
				Object userObj = ((DefaultMutableTreeNode) value).getUserObject();
				if (userObj != null && userObj instanceof JSNode) {
					JSNode node = (JSNode) userObj;
					String type = node.getType();
					String typeText = type == null ? "" : ": " + type;
					String name = node.getName() == null || node.getName().equals("<anonymous>") ? "?" : node.getName();
					String labelText = "<html>" + escape(name + typeText);
					if (_lastSearch != null && _lastSearch.length() > 0) {
						int searchLen = _lastSearch.length();
						int a = name.toLowerCase().indexOf(_lastSearch);
						if (a >= 0) {
							int b = a + searchLen;
							labelText = "<html>" + escape(name.substring(0, a)) + "<span style='background-color:"
									+ _bgSelColor + ";color:" + _fgSelColor + "'>" + escape(name.substring(a, b))
									+ "</span>" + escape(name.substring(b) + typeText);
						}
					}
					label.setText(labelText);
					if (node.isFunction()) {
						label.setIcon(METHOD_ICON);
					} else if (node.isClass()) {
						label.setIcon(CLASS_ICON);
					}
				}
				return label;
			}
		};
		renderer.setFont(getCodeFont());
		_tree.setCellRenderer(renderer);

		JPanel mainPanel = new JPanel(new BorderLayout());
		mainPanel.add(topPanel, BorderLayout.NORTH);
		JScrollPane scrollPane = new JScrollPane(_tree);
		scrollPane.setBorder(BorderFactory.createEmptyBorder());
		mainPanel.add(scrollPane, BorderLayout.CENTER);
		return mainPanel;
	}

	protected static String escape(String plain) {
		return RSyntaxUtilities.escapeForHtml(plain, "", false);
	}

	protected void gotoSelectedElement() {
		TreePath path = _tree.getSelectionModel().getSelectionPath();
		if (path == null) {
			path = _tree.getPathForRow(0);
		}
		gotoPath(path);
	}

	private void gotoPath(TreePath path) {
		JSNode node = (JSNode) ((DefaultMutableTreeNode) path.getLastPathComponent()).getUserObject();
		IEditor editor = Workbench.getInstance().getActiveEditor();
		if (node.getStart() != null) {
			editor.setCaretPosition(node.getStart().intValue());
		}
		setVisible(false);
	}

	protected void searchChanged() {
		String q = _searchField.getText().toLowerCase().trim();

		if (_lastSearch != null && q.equals(_lastSearch)) {
			return;
		}

		_lastSearch = q;
		if (q.length() == 0) {
			_tree.setModel(new DefaultTreeModel(_defaultTreeRoot));
		} else {
			DefaultMutableTreeNode newRoot = new DefaultMutableTreeNode(_defaultTreeRoot.getUserObject());
			filter(_defaultTreeRoot, q, newRoot);
			_tree.setModel(new DefaultTreeModel(newRoot));
		}
		_tree.setRootVisible(false);
		expandTree();
	}

	private void filter(DefaultMutableTreeNode tree, String q, DefaultMutableTreeNode newParent) {
		DefaultMutableTreeNode newParent2 = newParent;
		JSNode node = (JSNode) tree.getUserObject();
		String name = node.getName();
		name = name == null ? "?" : name.toLowerCase();
		if (!tree.isRoot() && name.contains(q)) {
			DefaultMutableTreeNode newChild = new DefaultMutableTreeNode(node);
			newParent2.add(newChild);
			newParent2 = newChild;
		}

		for (int i = 0; i < tree.getChildCount(); i++) {
			DefaultMutableTreeNode child = (DefaultMutableTreeNode) tree.getChildAt(i);
			filter(child, q, newParent2);
		}
	}

	public void display(JSNode root) {
		Window owner = getOwner();
		int w = owner.getWidth() / 2;
		int h = owner.getHeight() / 2;
		setSize(Math.max(300, Math.min(400, w)), Math.max(300, Math.min(400, h)));
		int x = owner.getLocation().x;
		int y = owner.getLocation().y;
		setLocation(x + owner.getWidth() / 2, y + owner.getHeight() / 3 - getHeight() / 2);

		if (root.getChildren().isEmpty() && root.getStart() == null && root.getEnd() == null) {
			return;
		}

		_defaultTreeRoot = new DefaultMutableTreeNode(null);
		DefaultMutableTreeNode selected = walk(root, _defaultTreeRoot,
				Workbench.getInstance().getActiveEditor().getCaretPosition());
		DefaultTreeModel model = new DefaultTreeModel(_defaultTreeRoot);
		_tree.setModel(model);
		expandTree();
		if (selected != null) {
			_tree.setSelectionPath(new TreePath(selected.getPath()));
		}
		_searchField.setText("");
		_searchField.requestFocus();
		setVisible(true);
	}

	private void expandTree() {
		for (int i = 0; i < _tree.getRowCount(); i++) {
			_tree.expandRow(i);
		}
	}

	private DefaultMutableTreeNode walk(JSNode node, DefaultMutableTreeNode tree, int caretPosition) {
		DefaultMutableTreeNode selected = null;
		tree.setUserObject(node);

		if (node.getStart() != null) {
			long start = node.getStart().longValue();
			long end = node.getEnd().longValue();
			if (caretPosition >= start && caretPosition <= end) {
				selected = tree;
			}
		}

		for (JSNode child : node.getChildren()) {
			DefaultMutableTreeNode treeNode = new DefaultMutableTreeNode(null);
			DefaultMutableTreeNode selected2 = walk(child, treeNode, caretPosition);
			if (selected2 != null) {
				selected = selected2;
			}
			tree.add(treeNode);
		}
		return selected;
	}
}
