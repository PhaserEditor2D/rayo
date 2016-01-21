package rayo.ui.editors.js.autocomplete;

import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.awt.Point;
import java.util.ArrayList;
import java.util.List;

import javax.swing.text.BadLocationException;
import javax.swing.text.JTextComponent;

import org.fife.ui.autocomplete.AbstractCompletionProvider;
import org.fife.ui.autocomplete.Completion;
import org.fife.ui.autocomplete.ParameterizedCompletion;
import org.json.JSONObject;

import com.eclipsesource.json.JsonObject;

import rayo.core.Project;
import rayo.core.RayoCompletionsQuery;
import rayo.core.TernManager;
import rayo.ui.IEditor;
import tern.server.protocol.IJSONObjectHelper;
import tern.server.protocol.completions.ITernCompletionCollector;
import tern.server.protocol.completions.TernCompletionItem;
import tern.server.protocol.completions.TernCompletionProposalRec;

@SuppressWarnings("synthetic-access")
public class JavaScriptCompletionProvider extends AbstractCompletionProvider {

	private ArrayList<Completion> _completions;
	protected String _prefix;
	private boolean _autoActivateOnLetters;
	private static final JavaScriptCompletionProvider _instance = new JavaScriptCompletionProvider();

	public static JavaScriptCompletionProvider getInstance() {
		return _instance;
	}

	private JavaScriptCompletionProvider() {
		_autoActivateOnLetters = true;
		setAutoActivationRules(_autoActivateOnLetters, ".");
		setParameterizedCompletionParams('(', ",", ')');
		setListCellRenderer(new RayoCompletionCellRenderer());
	}

	public boolean isAutoActivateOnLetters() {
		return _autoActivateOnLetters;
	}

	public void toggleAutoActivateOnLetters() {
		_autoActivateOnLetters = !_autoActivateOnLetters;
		setAutoActivationRules(_autoActivateOnLetters, ".");
	}

	@Override
	public String getAlreadyEnteredText(JTextComponent textComp) {
		computeCompletions(textComp);
		return _prefix == null ? "" : _prefix;
	}

	private void computeCompletions(JTextComponent comp) {
		_completions = new ArrayList<>();
		_prefix = null;

		TernManager tern = Project.getInstance().getTernManager();

		if (tern.isDisposed()) {
			return;
		}

		int offset = comp.getCaretPosition();

		if (offset < 0 || offset > comp.getDocument().getLength()) {
			return;
		}

		IEditor editor = (IEditor) comp.getClientProperty("rayo.editor");

		if (editor == null) {
			return;
		}

		RayoCompletionsQuery args = new RayoCompletionsQuery(editor.getFile(), offset, this);
		tern.queryCompletions(args, new ITernCompletionCollector() {

			@Override
			public void addProposal(TernCompletionProposalRec proposal, Object completion,
					IJSONObjectHelper jsonManager) {

				TernCompletionItem item = new TernCompletionItem(proposal);

				JavaScriptCompletionProvider provider = JavaScriptCompletionProvider.this;

				boolean isKeyworkd = false;
				if (completion instanceof JsonObject) {
					JsonObject obj = (JsonObject) completion;
					isKeyworkd = obj.getBoolean("isKeyword", false);
				}

				if (_prefix == null) {
					try {
						_prefix = comp.getDocument().getText(proposal.start, proposal.end - proposal.start);
					} catch (BadLocationException e) {
						e.printStackTrace();
						showMessageDialog(null, e.getMessage(), "Error", ERROR_MESSAGE);
					}
				}

				if (item.isFunction()) {
					_completions.addAll(RayoFunctionCompletion.computeCompletions(provider, item));
				} else if (item.isProperty()) {
					_completions.add(new RayoPropertyCompletion(provider, item));
				} else if (isKeyworkd) {
					if (_prefix.length() > 0) {
						_completions.add(new KeywordCompletion(provider, item.getName()));
					}
				} else {
					_completions.add(new RayoVarCompletion(provider, item));
				}
			}
		});
	}

	@Override
	public List<Completion> getCompletions(JTextComponent comp) {
		return getCompletionsAtOffset(comp, comp.getCaretPosition());
	}

	@Override
	public List<Completion> getCompletionsAt(JTextComponent comp, Point p) {
		return null;
	}

	@SuppressWarnings("unused")
	private List<Completion> getCompletionsAtOffset(JTextComponent comp, int offset) {
		return _completions;
	}

	@Override
	public List<ParameterizedCompletion> getParameterizedCompletions(JTextComponent tc) {
		// TODO: is this better?
		return new ArrayList<>();
	}

	public void loadState(JSONObject config) {
		boolean col = config.optBoolean("editor.js.completeOnLetters", true);
		if (isAutoActivateOnLetters() != col) {
			toggleAutoActivateOnLetters();
		}
	}

	public void saveState(JSONObject config) {
		config.put("editor.js.completeOnLetters", isAutoActivateOnLetters());
	}
}
