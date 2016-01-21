package rayo.ui.editors.js.autocomplete;

import org.fife.ui.autocomplete.AutoCompletion;
import org.fife.ui.autocomplete.CompletionProvider;

public class RayoAutoCompletion extends AutoCompletion {

	public RayoAutoCompletion(CompletionProvider provider) {
		super(provider);
		setChoicesWindowSize(600, 400);
		setDescriptionWindowSize(400, 400);
		setAutoActivationEnabled(true);
		setAutoActivationDelay(0);
		setAutoCompleteSingleChoices(false);
		setShowDescWindow(true);
		setParameterAssistanceEnabled(true);
	}

}
