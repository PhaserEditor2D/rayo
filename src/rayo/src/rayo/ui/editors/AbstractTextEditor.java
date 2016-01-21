package rayo.ui.editors;

import static java.lang.System.out;
import static rayo.ui.widgets.WidgetUtils.getCodeFont;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.ActionEvent;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.IOException;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.AbstractButton;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.KeyStroke;
import javax.swing.UIManager;
import javax.swing.border.AbstractBorder;
import javax.swing.event.CaretEvent;
import javax.swing.event.CaretListener;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.plaf.metal.MetalLookAndFeel;
import javax.swing.text.BadLocationException;

import org.fife.rsta.ui.CollapsibleSectionPanel;
import org.fife.rsta.ui.search.FindToolBar;
import org.fife.rsta.ui.search.ReplaceToolBar;
import org.fife.rsta.ui.search.SearchEvent;
import org.fife.rsta.ui.search.SearchListener;
import org.fife.ui.rsyntaxtextarea.ErrorStrip;
import org.fife.ui.rsyntaxtextarea.RSyntaxTextArea;
import org.fife.ui.rsyntaxtextarea.Theme;
import org.fife.ui.rtextarea.Gutter;
import org.fife.ui.rtextarea.RTextScrollPane;
import org.fife.ui.rtextarea.SearchContext;
import org.fife.ui.rtextarea.SearchEngine;
import org.fife.ui.rtextarea.SearchResult;

import rayo.core.Problem;
import rayo.core.Project;
import rayo.core.RayoFile;
import rayo.core.TernManager;
import rayo.ui.IEditor;
import rayo.ui.IEditorStack;
import rayo.ui.Workbench;

@SuppressWarnings("synthetic-access")
public abstract class AbstractTextEditor extends JPanel implements IEditor, DocumentListener, SearchListener {
	private static final long serialVersionUID = 1L;

	public static final String ACTIVE_STATE = "abstractTextEditor";

	private RayoFile _file;
	private RSyntaxTextArea _textArea;
	private RTextScrollPane _textAreaContainer;

	private JLabel _statusLinesLabel;

	private JPanel _statusPanel;

	private JButton _tabsSizeButton;

	private JButton _tabsModeButton;

	private JPanel _statusToolbar;

	private CollapsibleSectionPanel _collapsiblePanel;

	private FindToolBar _findToolBar;

	private ReplaceToolBar _replaceToolBar;

	private Action _findAction;

	private Action _replaceAction;

	public AbstractTextEditor(RayoFile file) {
		_file = file;

		setLayout(new BorderLayout());

		// create text component
		String text = file.getContent();
		_file.setEditingContent(text);
		_textArea = new RSyntaxTextArea(text);
		_textArea.discardAllEdits();
		_textArea.putClientProperty("rayo.editor", this);
		// we add the document listener at the end of the method.

		Theme th;
		try {
//			th = Theme.load(Theme.class.getResourceAsStream("/rayo/ui/editors/themes/rayo.xml"));
			th = Theme.load(Theme.class.getResourceAsStream("/org/fife/ui/rsyntaxtextarea/themes/dark.xml"));
			th.apply(_textArea);
		} catch (IOException e) {
			e.printStackTrace();
		}

		_textArea.setFont(getCodeFont());

		IEditorStack editorStack = Workbench.getInstance().getEditorStack();
		_textArea.setTabsEmulated(editorStack.isTabsEmulated());
		_textArea.setTabSize(editorStack.getTabSize());

		initTextArea(_textArea);

		_textAreaContainer = new RTextScrollPane(_textArea);
		_collapsiblePanel = new CollapsibleSectionPanel();
		_collapsiblePanel.add(_textAreaContainer);

		ErrorStrip errorStrip = new ErrorStrip(_textArea);
		add(BorderLayout.EAST, errorStrip);

		Gutter gutter = _textAreaContainer.getGutter();
		gutter.setBookmarkingEnabled(true);
		add(_collapsiblePanel, BorderLayout.CENTER);

		initActions();

		_textArea.addCaretListener(new CaretListener() {

			@Override
			public void caretUpdate(CaretEvent e) {
				updateCaretStatus();
			}
		});

		{
			Font font = getFont().deriveFont(10f);
			_statusLinesLabel = new JLabel();
			_statusLinesLabel.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 0));
			_statusLinesLabel.setFont(font);

			_statusPanel = new JPanel(new BorderLayout(0, 0));
			_statusToolbar = new JPanel(new FlowLayout(FlowLayout.LEFT, 0, 0)) {
				private static final long serialVersionUID = 1L;

				@Override
				public Component add(Component comp) {
					AbstractButton btn = (AbstractButton) comp;
					btn.setContentAreaFilled(false);
					btn.setFont(font);
					return super.add(comp);
				}
			};
		}

		createStatusToolbar(_statusToolbar);

		updateCaretStatus();
		updateStatusBar();

		// just at the end I add the document listener
		_textArea.getDocument().addDocumentListener(this);

		// focus listener
		_textArea.addFocusListener(new FocusListener() {

			@Override
			public void focusLost(FocusEvent e) {
				//
			}

			@Override
			public void focusGained(FocusEvent e) {
				Workbench.getInstance().setActiveState(AbstractTextEditor.ACTIVE_STATE);
			}
		});
	}

	private void initActions() {
		_findToolBar = new FindToolBar(this);
		_findAction = _collapsiblePanel.addBottomComponent(KeyStroke.getKeyStroke("control F"), _findToolBar);

		_replaceToolBar = new ReplaceToolBar(this);
		_replaceAction = _collapsiblePanel.addBottomComponent(KeyStroke.getKeyStroke("control H"), _replaceToolBar);
	}

	public Action getReplaceAction() {
		return _replaceAction;
	}

	public Action getFindAction() {
		return _findAction;
	}

	@Override
	public boolean isActive() {
		return _textArea.hasFocus();
	}

	protected void createStatusToolbar(JPanel toolbar) {
		IEditorStack editorStack = Workbench.getInstance().getEditorStack();

		_tabsModeButton = new JButton(new AbstractAction(editorStack.isTabsEmulated() ? "Spaces" : "Tab Size") {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				editorStack.setTabsEmulated(!editorStack.isTabsEmulated());
			}
		});
		_tabsModeButton.setToolTipText("Change tabs emulation mode.");
		toolbar.add(_tabsModeButton);

		_tabsSizeButton = new JButton(new AbstractAction(Integer.toString(_textArea.getTabSize())) {

			private static final long serialVersionUID = 1L;

			@Override
			public void actionPerformed(ActionEvent e) {
				JPopupMenu menu = new JPopupMenu();
				for (int i : new int[] { 2, 4, 8 }) {
					menu.add(new AbstractAction(Integer.toString(i)) {
						private static final long serialVersionUID = 1L;

						@Override
						public void actionPerformed(ActionEvent e2) {
							editorStack.setTabSize(i);
						}
					});
				}
				menu.show(_tabsSizeButton, 0, -80);
			}
		});
		_tabsSizeButton.setToolTipText("Change the size of tab chars.");
		toolbar.add(_tabsSizeButton);

		_statusPanel.add(_statusLinesLabel, BorderLayout.WEST);
		_statusPanel.add(_statusToolbar, BorderLayout.EAST);
	}

	@Override
	public void refreshProblems() {
		Gutter gutter = _textAreaContainer.getGutter();
		gutter.removeAllTrackingIcons();
		List<Problem> list = _file.getProblems();
		for (Problem p : list) {
			try {
				gutter.addOffsetTrackingIcon(p.getFrom(), p.getSeverity().getIcon(), p.getMessage());
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
	}

	@Override
	public void updateStatusBar() {
		IEditorStack editorStack = Workbench.getInstance().getEditorStack();
		_textArea.setTabSize(editorStack.getTabSize());
		_textArea.setTabsEmulated(editorStack.isTabsEmulated());
		_tabsModeButton.setText(editorStack.isTabsEmulated() ? "Spaces" : "Tab Size");
		_tabsSizeButton.setText(Integer.toString(editorStack.getTabSize()));
	}

	@Override
	public JComponent getStatusComponent() {
		return _statusPanel;
	}

	static class ToolBorder extends AbstractBorder {
		private static final long serialVersionUID = 1L;

		@Override
		public void paintBorder(Component c, Graphics g, int x, int y, int width, int height) {
			g.setColor(MetalLookAndFeel.getSeparatorForeground());
			g.drawLine(x, y, x, y + height);
		}

	}

	protected abstract void initTextArea(RSyntaxTextArea textArea);

	protected void updateCaretStatus() {
		int line = _textArea.getCaretLineNumber() + 1;
		int col = _textArea.getCaretOffsetFromLineStart() + 1;
		_statusLinesLabel.setText("<html>Line " + line + ", Column " + col + " <a color='gray'> / "
				+ _textArea.getLineCount() + " Lines</a>");
	}

	@Override
	public void activated() {
		// nothing
	}

	@Override
	public void closed() {
		out.println("Closed javascript editor: " + getFile().getFilePath());
	}

	@Override
	public RayoFile getFile() {
		return _file;
	}

	@Override
	public void focus() {
		_textArea.requestFocus();
	}

	public RSyntaxTextArea getTextArea() {
		return _textArea;
	}

	@Override
	public void setCaretPosition(int position) {
		try {
			_textArea.setCaretPosition(position);
		} catch (IllegalArgumentException e) {
			// nothing
		}
	}

	@Override
	public String getContent() {
		return _textArea.getText();
	}

	public void openText(String text) {
		_textArea.getDocument().removeDocumentListener(this);
		_textArea.setText(text);
		_textArea.discardAllEdits();
		_textArea.setCaretPosition(0);
		_textArea.getDocument().addDocumentListener(this);
	}

	@Override
	public void save() {
		_file.save();

		TernManager tern = Project.getInstance().getTernManager();

		if (tern.checkDisposedAndShowMessage()) {
			return;
		}
		tern.updateFile(_file);

		Workbench.getInstance().refreshProblems();
	}

	@Override
	public int getCaretPosition() {
		return _textArea.getCaretPosition();
	}

	@Override
	public void insertUpdate(DocumentEvent e) {
		// nothing
	}

	@Override
	public void removeUpdate(DocumentEvent e) {
		// nothing
	}

	@Override
	public void changedUpdate(DocumentEvent e) {
		_file.setEditingContent(getContent());

		{
			// TODO: set to modified if it is really different of the
			// underlaying file content. We can keep in memory a hash of the
			// file content and then compare.
			_file.setModified(true);
		}
	}

	@Override
	public JComponent getComponent() {
		return this;
	}

	@Override
	public void searchEvent(SearchEvent e) {
		SearchEvent.Type type = e.getType();
		SearchContext context = e.getSearchContext();
		SearchResult result = null;

		switch (type) {
		default: // Prevent FindBugs warning later
		case MARK_ALL:
			result = SearchEngine.markAll(_textArea, context);
			break;
		case FIND:
			result = SearchEngine.find(_textArea, context);
			if (!result.wasFound()) {
				UIManager.getLookAndFeel().provideErrorFeedback(_textArea);
			}
			break;
		case REPLACE:
			result = SearchEngine.replace(_textArea, context);
			if (!result.wasFound()) {
				UIManager.getLookAndFeel().provideErrorFeedback(_textArea);
			}
			break;
		case REPLACE_ALL:
			result = SearchEngine.replaceAll(_textArea, context);
			JOptionPane.showMessageDialog(null, result.getCount() + " occurrences replaced.");
			break;
		}

		String text = null;
		if (result.wasFound()) {
			text = "Text found; occurrences marked: " + result.getMarkedCount();
		} else if (type == SearchEvent.Type.MARK_ALL) {
			if (result.getMarkedCount() > 0) {
				text = "Occurrences marked: " + result.getMarkedCount();
			} else {
				text = "";
			}
		} else {
			text = "Text not found";
		}
		_statusLinesLabel.setText("<html><small>" + text);
	}

	@Override
	public String getSelectedText() {
		return _textArea.getSelectedText();
	}
}
