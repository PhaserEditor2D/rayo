package rayo.core;

import static java.lang.System.out;
import static javax.swing.JOptionPane.ERROR_MESSAGE;
import static javax.swing.JOptionPane.INFORMATION_MESSAGE;
import static javax.swing.JOptionPane.showMessageDialog;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import javax.swing.JOptionPane;

import com.eclipsesource.json.JsonObject;

import rayo.ui.Workbench;
import tern.EcmaVersion;
import tern.ITernProject;
import tern.TernException;
import tern.TernResourcesManager;
import tern.repository.ITernRepository;
import tern.repository.TernRepository;
import tern.server.BasicTernPlugin;
import tern.server.DefaultResponseHandler;
import tern.server.IResponseHandler;
import tern.server.ITernPlugin;
import tern.server.ITernServer;
import tern.server.TernDef;
import tern.server.TernModuleInfo;
import tern.server.TernPlugin;
import tern.server.nodejs.NodejsTernServer;
import tern.server.nodejs.process.NodejsProcessManager;
import tern.server.nodejs.process.PrintNodejsProcessListener;
import tern.server.protocol.TernDoc;
import tern.server.protocol.TernQuery;
import tern.server.protocol.completions.ITernCompletionCollector;
import tern.server.protocol.completions.TernCompletionsQuery;
import tern.server.protocol.definition.ITernDefinitionCollector;
import tern.server.protocol.definition.TernDefinitionQuery;
import tern.server.protocol.lint.TernLintQuery;
import tern.server.protocol.outline.JSNodeRoot;
import tern.server.protocol.outline.TernOutlineCollector;
import tern.server.protocol.outline.TernOutlineQuery;
import tern.server.protocol.refs.TernRefsQuery;

public class TernManager {
	/**
	 * 100K
	 */
	private static final long JS_SIZE_TO_AUTO_SUMBIT = 100 * 1_024;

	private ITernProject _ternProject;
	private ITernServer _ternServer;

	public TernManager() {

	}

	public synchronized void startTernServer(Path ternRepoBaseDir, Path projectDir) throws IOException {
		if (_ternServer != null) {
			_ternServer.dispose();
		}
		out.println("Repo dir: " + ternRepoBaseDir.toAbsolutePath().normalize());
		ITernRepository repository = new TernRepository("ternjs", ternRepoBaseDir.toFile());
		_ternProject = TernResourcesManager.getTernProject(projectDir.toFile());
		_ternProject.setRepository(repository);
		_ternProject.setEcmaVersion(EcmaVersion.ES5);
		_ternProject.addLib(TernDef.browser);
		_ternProject.addLib(TernDef.ecma5);
		_ternProject.addPlugin(TernPlugin.doc_comment);
		_ternProject.addPlugin(TernPlugin.outline);
		_ternProject.addPlugin(TernPlugin.eslint);
		_ternProject.addPlugin(plugin("phaser"));

		// configure plug-ins
		JsonObject json = _ternProject.getPlugins();
		{
			JsonObject options = (JsonObject) json.get("doc_comment");
			options.set("strong", true);

		}
		{
			JsonObject options = (JsonObject) json.get("eslint");
			JsonObject config = new JsonObject();
			options.set("config", config);

			{
				JsonObject section = new JsonObject();
				section.set("browser", true);
				config.set("env", section);
			}

			{
				JsonObject section = new JsonObject();
				section.set("Phaser", true);
				config.set("globals", section);
			}

			{
				JsonObject section = new JsonObject();
				section.set("no-unused-vars", 2);
				config.set("rules", section);
			}

		}
		out.println("Plugins configuration:" + json);
		//
		if (!_ternProject.getTernProjectFile().exists()) {
			_ternProject.save();
		}

		try {
			NodejsProcessManager.getInstance().init(new File(ternRepoBaseDir.toFile(), "node_modules/tern"));
			NodejsTernServer nodeServer = new NodejsTernServer(_ternProject);
			_ternServer = nodeServer;
			nodeServer.setPersistent(true);
			
			// disable logging
			// nodeServer.addInterceptor(LoggingInterceptor.getInstance());
			
			nodeServer.addProcessListener(PrintNodejsProcessListener.getInstance());
			_ternServer.addFile("rayo_preload_server.js", "");
		} catch (Exception e) {
			e.printStackTrace();
			showMessageDialog(null, e, "Tern", ERROR_MESSAGE);
		}

	}

	public ITernServer getTernServer() {
		return _ternServer;
	}

	public ITernProject getTernProject() {
		return _ternProject;
	}

	private static ITernPlugin plugin(String name) {
		return new BasicTernPlugin(new TernModuleInfo(name), null);
	}

	public boolean checkDisposedAndShowMessage() {
		if (isDisposed()) {
			showMessageDialog(null,
					"<html>The Tern server is down.<br>Please check any project configuration and reload the project (<code>SHIFT+F5</code>) to restart the connection.",
					"Tern Server", ERROR_MESSAGE);
			return true;
		}
		return false;
	}

	public boolean isDisposed() {
		return _ternServer.isDisposed();
	}

	public synchronized void queryCompletions(RayoCompletionsQuery rquery, ITernCompletionCollector collector) {
		String filename = rquery.getFilename();
		TernCompletionsQuery query = new TernCompletionsQuery(filename, Integer.valueOf(rquery.getOffset()));
		query.setDocs(true);
		query.setTypes(true);
		query.setGuess(false);
		query.setCaseInsensitive(false);
		query.setOrigins(false);
		query.setUrls(false);
		query.set("sort", false);
		query.set("includeKeywords", true);
		TernDoc doc = new TernDoc(query);
		doc.addFile(filename, rquery.getContent(), null, null);
		try {
			_ternServer.request(doc, collector);
		} catch (TernException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "Tern", INFORMATION_MESSAGE);
		}
	}

	public synchronized JSNodeRoot queryOutline(RayoQuery rquery) {
		String filename = rquery.getFilename();
		TernOutlineQuery query = new TernOutlineQuery(filename);
		TernDoc doc = new TernDoc(query);
		doc.addFile(filename, rquery.getContent(), null, null);
		try {
			TernOutlineCollector collector = new TernOutlineCollector();
			_ternServer.request(doc, collector);
			JSNodeRoot root = collector.getRoot();
			return root;
		} catch (TernException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "Tern Error", INFORMATION_MESSAGE);
			return null;
		}
	}

	public synchronized void queryDefinition(RayoQuery rquery, ITernDefinitionCollector collector) {
		int pos = rquery.getOffset();
		String filename = rquery.getFilename();
		TernDefinitionQuery query = new TernDefinitionQuery(filename, Integer.valueOf(pos));
		TernDoc doc = new TernDoc(query);
		doc.addFile(filename, rquery.getContent(), null, null);
		try {
			_ternServer.request(doc, collector);
		} catch (TernException e) {
			e.printStackTrace();
			JOptionPane.showMessageDialog(null, e.getMessage(), "Tern", INFORMATION_MESSAGE);
		}
	}

	public synchronized void queryJSDoc(RayoJSDocQuery rquery) {
		String filename = rquery.getFilename();
		int pos = rquery.getOffset();
		TernQuery query = new TernQuery("documentation");
		query.set("file", filename);
		query.set("end", pos);
		TernDoc doc = new TernDoc(query);
		doc.addFile(filename, rquery.getContent(), null, null);
		DefaultResponseHandler handler = new DefaultResponseHandler(false);
		try {
			_ternServer.request(doc, handler);
			JsonObject data = (JsonObject) handler.getData();
			rquery.resultDoc = data.getString("doc", null);
			rquery.resultType = data.getString("type", null);
		} catch (TernException e) {
			// do nothing
		}
	}

	public synchronized void queryLint(LintQuery rquery) {
		try {
			TernLintQuery query = TernLintQuery.create(TernPlugin.eslint, false);
			query.setFile(rquery.getFilename());
			query.setLineNumber(true);
			query.setUseLinterAsSuffix(false);
			TernDoc doc = new TernDoc(query);
			_ternServer.request(doc, rquery);
		} catch (TernException e) {
			e.printStackTrace();
			showMessageDialog(null, e, "ESLint", ERROR_MESSAGE);
		}
	}

	public synchronized void queryRefs(RefsQuery rquery) {
		TernRefsQuery query = new TernRefsQuery(rquery.getFilename(), Integer.valueOf(rquery.getOffset()));
		TernDoc doc = new TernDoc(query);
		doc.addFile(rquery.getFilename(), rquery.getContent(), null, null);
		try {
			_ternServer.request(doc, rquery);
		} catch (TernException e) {
			e.printStackTrace();
			showMessageDialog(null, e.getMessage(), "Rename", INFORMATION_MESSAGE);
		}
	}

	public synchronized void deleteFile(RayoFile file) {
		TernDoc doc = new TernDoc();
		doc.delFile(file.getId());
		_ternServer.request(doc, new IResponseHandler() {

			@Override
			public void onSuccess(Object data, String dataAsJsonString) {
				// nothing
			}

			@SuppressWarnings("synthetic-access")
			@Override
			public void onError(String error, Throwable t) {
				_ternProject.handleException(t);
				showMessageDialog(null, error, "Tern", ERROR_MESSAGE);
			}

			@Override
			public boolean isDataAsJsonString() {
				return false;
			}
		});
	}

	public synchronized void updateFile(RayoFile file) {
		if (_ternServer.isDisposed()) {
			return;
		}

		if (file.isFolder()) {
			return;
		}

		if (file.isExcluded()) {
			return;
		}

		Path path = file.getFilePath();
		String name = path.getFileName().toString();
		if (!name.endsWith(".js")) {
			return;
		}

		// do not auto-submit files above 100k
		try {
			if (Files.size(path) > JS_SIZE_TO_AUTO_SUMBIT) {
				out.println("Avoid " + path + " exceed allowed the size.");
				return;
			}
		} catch (IOException e) {
			e.printStackTrace();
			showMessageDialog(null, e, "Tern", ERROR_MESSAGE);
			return;
		}

		_ternServer.addFile(file.getId(), file.getContent());
		queryLint(new LintQuery(file));
		Workbench.getInstance().refreshProblems();
	}
}
