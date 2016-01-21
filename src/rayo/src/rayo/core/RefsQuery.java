package rayo.core;

import java.util.HashMap;
import java.util.Map;

import rayo.ui.IEditor;
import tern.server.protocol.refs.ITernRefCollector;

public class RefsQuery extends RayoQuery implements ITernRefCollector {

	private Map<String, ChangeSet> _result;
	private int _countRefs;

	public RefsQuery(IEditor editor) {
		super(editor);
		_result = new HashMap<>();
	}

	public Map<String, ChangeSet> getResult() {
		return _result;
	}

	@Override
	public void setDefinition(String file, Long start, Long end) {
		ChangeSet set;
		if (_result.containsKey(file)) {
			set = _result.get(file);
		} else {
			_result.put(file, set = new ChangeSet(file));
		}
		set.getChanges().add(new Change(start.longValue(), end.longValue()));
		_countRefs++;
	}

	public int getCountRefs() {
		return _countRefs;
	}

	public int getCountFiles() {
		return _result.size();
	}
}
