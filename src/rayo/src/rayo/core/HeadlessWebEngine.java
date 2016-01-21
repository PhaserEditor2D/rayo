package rayo.core;

import static java.lang.System.currentTimeMillis;
import static java.lang.System.out;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;

import javafx.application.Application;
import javafx.scene.web.WebEngine;
import javafx.stage.Stage;
import netscape.javascript.JSObject;

public class HeadlessWebEngine extends Application {
	@Override
	public void start(Stage stage) throws IOException {
		final WebEngine engine = new WebEngine();
		String libsrc = new String(Files.readAllBytes(Paths.get("src/resources/beautify.js")));
		engine.executeScript(libsrc);

		JSObject js_beautify = (JSObject) engine.executeScript("window");
		String src = new String(Files.readAllBytes(Paths.get("file.js")));
		long t = currentTimeMillis();
		Object formatted = js_beautify.call("js_beautify", src);
		t = currentTimeMillis() - t;
		out.println(formatted);
		out.println(t);

		// obj = engine.executeScript("js_beautify('function pepe(a, i){};')");
		// out.println(obj);
		// stage.setScene(new Scene(docString, 800, 600));
		// stage.show();
	}

	public static void main(String[] args) {
		launch(args);
	}
}