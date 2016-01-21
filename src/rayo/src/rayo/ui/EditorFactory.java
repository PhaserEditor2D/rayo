package rayo.ui;

import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;

import rayo.core.RayoFile;
import rayo.ui.editors.html.CssEditor;
import rayo.ui.editors.html.HtmlEditor;
import rayo.ui.editors.js.JSonEditor;
import rayo.ui.editors.js.JavaScriptEditor;
import rayo.ui.editors.plaintext.PlainTextEditor;

public class EditorFactory {
	public static IEditor createEditorForFile(RayoFile file) {
		// hard code editor resolution
		Path path = file.getFilePath();

		String fname = path.getFileName().toString().toLowerCase();
		if (fname.endsWith(".js")) {
			return new JavaScriptEditor(file);
		}

		if (fname.endsWith(".html") || fname.endsWith(".htm")) {
			return new HtmlEditor(file);
		}

		if (fname.endsWith(".css")) {
			return new CssEditor(file);
		}

		if (fname.endsWith(".json") || fname.endsWith(".tern-project") || fname.endsWith(".jshintrc")) {
			return new JSonEditor(file);
		}

		return new PlainTextEditor(file);
	}

	public static boolean isBinaryFile(Path path) {
		String fname = path.getFileName().toString().toLowerCase();
		if (fname.endsWith(".js") || fname.endsWith(".html") || fname.endsWith(".htm") || fname.endsWith(".css")
				|| fname.endsWith(".json") || fname.endsWith(".tern-project") || fname.endsWith(".jshintrc")) {
			return false;
		}

		// taken from
		// http://stackoverflow.com/questions/620993/determining-binary-text-file-type-in-java
		try (InputStream in = Files.newInputStream(path);) {
			int size = in.available();
			if (size > 1024)
				size = 1024;
			byte[] data = new byte[size];
			in.read(data);

			int ascii = 0;
			int other = 0;

			for (int i = 0; i < data.length; i++) {
				byte b = data[i];
				if (b < 0x09)
					return true;

				if (b == 0x09 || b == 0x0A || b == 0x0C || b == 0x0D)
					ascii++;
				else if (b >= 0x20 && b <= 0x7E)
					ascii++;
				else
					other++;
			}

			if (other == 0)
				return false;

			return (double) other / (ascii + other) > 0.95;
		} catch (Exception e) {
			return true;
		}
	}

}
