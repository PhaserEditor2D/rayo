package rayo.core;

import static java.lang.System.out;
import static javax.swing.JOptionPane.showMessageDialog;

import java.net.ServerSocket;
import java.nio.file.Path;

import org.eclipse.jetty.server.Handler;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.server.ServerConnector;
import org.eclipse.jetty.server.handler.HandlerList;
import org.eclipse.jetty.server.handler.ResourceHandler;

public class WebServer {
	private static Server _server;

	public static void start(Path directory) {
		// TODO: discover port
		if (_server != null) {
			try {
				out.println("Stopping server");
				_server.stop();
			} catch (Exception e) {
				e.printStackTrace();
				showMessageDialog(null, e);
			}
		}

		String path = directory.toString();

		int port = 0;

		int p = 1_982;
		while (port == 0 && p < 2_000) {
			try (ServerSocket server = new ServerSocket(p)) {
				port = p;
			} catch (Exception e) {
				p++;
			}
		}

		out.println("Serving " + path + ":" + port);
		_server = new Server(port);
		_server.setAttribute("useFileMappedBuffer", "false");

		// resources
		ResourceHandler resourceHandler = new ResourceHandler();
		resourceHandler.setMinMemoryMappedContentLength(-1);
		resourceHandler.setDirectoriesListed(true);
		resourceHandler.setWelcomeFiles(new String[] { "index.html" });
		// set the folder to serve
		resourceHandler.setResourceBase(path);

		Handler[] handlers = { resourceHandler };

		// collection
		HandlerList handlerList = new HandlerList();
		handlerList.setHandlers(handlers);
		_server.setHandler(handlerList);

		// start server
		try {
			_server.start();
			// _server.join();
		} catch (Exception e) {
			e.printStackTrace();
			showMessageDialog(null, e);
		}

	}

	@SuppressWarnings("resource")
	public synchronized static int getServerPort() {
		if (_server == null || _server.getConnectors().length == 0) {
			return 0;
		}
		ServerConnector connector = (ServerConnector) _server.getConnectors()[0];
		return connector.getLocalPort();
	}
}
