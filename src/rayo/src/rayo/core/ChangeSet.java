package rayo.core;

import java.util.ArrayList;
import java.util.List;

public class ChangeSet {
	private String _filename;
	private List<Change> _changes;

	public ChangeSet(String filename) {
		super();
		_filename = filename;
		_changes = new ArrayList<>();
	}

	public String getFilename() {
		return _filename;
	}

	public List<Change> getChanges() {
		return _changes;
	}

}
