package rayo.core;

import rayo.ui.IEditor;

public class RayoQuery {
	private int _offset;
	private RayoFile _file;

	public RayoQuery(RayoFile file, int offset) {
		_file = file;
		this.setOffset(offset);
	}

	public RayoQuery(IEditor editor) {
		this(editor.getFile(), editor.getCaretPosition());
	}

	public int getOffset() {
		return _offset;
	}

	public void setOffset(int offset) {
		this._offset = offset;
	}

	public String getFilename() {
		return _file.getId();
	}

	public String getContent() {
		return _file.getContent();
	}
	
	public RayoFile getFile() {
		return _file;
	}
}