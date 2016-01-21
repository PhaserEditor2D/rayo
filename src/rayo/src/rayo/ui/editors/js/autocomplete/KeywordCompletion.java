package rayo.ui.editors.js.autocomplete;

import org.fife.ui.autocomplete.BasicCompletion;
import org.fife.ui.autocomplete.CompletionProvider;

public class KeywordCompletion extends BasicCompletion {

	public KeywordCompletion(CompletionProvider provider, String keyword) {
		super(provider, keyword);
		setRelevance(ICompletionConstants.KEYWORD_RELEVANCE);
	}

	@Override
	public String getSummary() {
		return null;
	}
}
