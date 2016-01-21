package rayo.core;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.Reader;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Map;

import javax.script.Invocable;
import javax.script.ScriptEngine;
import javax.script.ScriptEngineManager;
import javax.script.ScriptException;

import javafx.application.Application;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

public class Beautify extends Application {
	private static JSObject _windowObject;
	public static String JS_LANG = "js";
	public static String CSS_LANG = "css";
	public static String HTML_LANG = "html";

	public static String beautify(String lang, String source, Map<Object, Object> options) {
		return (String) _windowObject.call(lang + "_beautify", source, options);
	}

	@Override
	public void start(Stage primaryStage) throws Exception {
		WebEngine engine = new WebEngine();

		StringBuilder sb = new StringBuilder();
		for (String lib : new String[] { "beautify.js", "beautify-css.js", "beautify-html.js" }) {
			try (BufferedReader reader = new BufferedReader(
					new InputStreamReader(getClass().getResourceAsStream("/resources/" + lib)))) {
				String line;
				while ((line = reader.readLine()) != null) {
					sb.append(line + "\n");
				}
			}
			sb.append("\n");
		}

		engine.executeScript(sb.toString());

		_windowObject = (JSObject) engine.executeScript("window");

		Object mutex = new Object();
		synchronized (mutex) {
			mutex.wait();
		}
	}

	public static void test1() throws ScriptException, IOException, NoSuchMethodException {
		Object global;
		ScriptEngine engine = new ScriptEngineManager().getEngineByExtension("js");

		// the js_beautify function will be added to this var
		engine.eval("var global = {};");

		try (Reader reader = new InputStreamReader(Beautify.class.getResourceAsStream("/resources/beautify.js"),
				Charset.forName("utf-8"))) {
			engine.eval(reader);
			global = engine.get("global");
		}

		String source = new String(Files.readAllBytes(Paths.get("file.js")));
		long t = currentTimeMillis();
		Invocable invoke = (Invocable) engine;
		String formatted = (String) invoke.invokeMethod(global, "js_beautify", source);
		t = currentTimeMillis() - t;
		out.println(formatted);

		out.println(t + "ms");
	}
}
