package rayo.core;

public class Change {
	private long _start;
	private long _end;
	private String _text;

	public Change(long start, long end) {
		this(start, end, null);
	}

	public Change(long start, long end, String text) {
		_start = start;
		_end = end;
		_text = text;
	}

	public long getStart() {
		return _start;
	}

	public void setStart(long start) {
		_start = start;
	}

	public long getEnd() {
		return _end;
	}

	public void setEnd(long end) {
		_end = end;
	}

	public String getText() {
		return _text;
	}

	public void setText(String text) {
		_text = text;
	}

	public long getLength() {
		return _end - _start;
	}

}
