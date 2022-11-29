package reactor.netty.examples.tcp.uptime;

import reactor.netty.tcp.TcpServer;

public class UptimeServer {

	private static final int PORT = Integer.parseInt(System.getProperty("port", "8080"));

	public static void main(String[] args) {


		TcpServer server = TcpServer.create()
				.port(PORT)
				.handle((in, out) -> {
					// Discard the incoming data and release the buffer
					in.receive().subscribe();
					return out.neverComplete();
				});

		server.bindNow().onDispose().block();
	}
}
