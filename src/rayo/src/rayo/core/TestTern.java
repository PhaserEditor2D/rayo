package rayo.core;

import static java.lang.System.out;

import java.io.File;

import tern.EcmaVersion;
import tern.ITernProject;
import tern.TernException;
import tern.TernResourcesManager;
import tern.repository.ITernRepository;
import tern.repository.TernRepository;
import tern.server.BasicTernPlugin;
import tern.server.ITernServer;
import tern.server.TernModuleInfo;
import tern.server.TernPlugin;
import tern.server.nodejs.NodejsTernServer;
import tern.server.nodejs.process.NodejsProcessManager;
import tern.server.protocol.IJSONObjectHelper;
import tern.server.protocol.TernDoc;
import tern.server.protocol.completions.ITernCompletionCollector;
import tern.server.protocol.completions.TernCompletionProposalRec;
import tern.server.protocol.completions.TernCompletionsQuery;

public class TestTern {
	public static void main(String[] args) throws TernException {
		File ternRepoBaseDir = new File(".");
		out.println("Repo dir: " + ternRepoBaseDir.toPath().toAbsolutePath().normalize());
		ITernRepository repository = new TernRepository("ternjs", ternRepoBaseDir);
		File projectDir = new File(".");
		ITernProject project = TernResourcesManager.getTernProject(projectDir);
		project.setEcmaVersion(EcmaVersion.ES5);
		project.setRepository(repository);
		project.addPlugin(TernPlugin.outline);
		project.addPlugin(new BasicTernPlugin(new TernModuleInfo("phaser"), null));
		NodejsProcessManager.getInstance().init(new File(ternRepoBaseDir, "node_modules/tern"));
		ITernServer server = new NodejsTernServer(project);
		// server.addFile("main.js", "Phaser.Game.");
		String text = "var a=[];a.";
		text = "var c = new MyConstructor();c.";
		text = "''.to";
		server.addFile("main.js", text);
		TernCompletionsQuery query = new TernCompletionsQuery("main.js", Integer.valueOf(text.length()));
		query.setDocs(true);
		query.setTypes(true);
		TernDoc doc = new TernDoc(query);
		server.request(doc, new ITernCompletionCollector() {

			@Override
			public void addProposal(TernCompletionProposalRec proposal, Object completion,
					IJSONObjectHelper jsonManager) {
				out.println(proposal.start + " " + proposal.end);
				out.println(proposal.name + ":" + proposal.type + " - " + proposal.doc);

			}
		});

		// TernDoc doc = new TernDoc(new TernOutlineQuery("main.js"));
		// TernOutlineCollector collector = new TernOutlineCollector();
		// server.request(doc, collector);
		// JSNodeRoot root = collector.getRoot();
		// System.err.println(root.getChildren().get(0).getName() + ":" +
		// root.getChildren().get(0).getType());
	}
}