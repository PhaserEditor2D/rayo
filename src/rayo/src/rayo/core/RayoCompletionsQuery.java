package rayo.core;

import org.fife.ui.autocomplete.CompletionProvider;

public class RayoCompletionsQuery extends RayoQuery {

	public CompletionProvider _provider;

	public RayoCompletionsQuery(RayoFile file, int offset, CompletionProvider provider) {
		super(file, offset);
		_provider = provider;
	}
}