package reactor.netty.examples.tcp.uptime;

import io.netty.handler.timeout.IdleStateHandler;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpClient;

public final class UptimeClient {
	static final String HOST = System.getProperty("host", "127.0.0.1");
	static final int PORT = Integer.parseInt(System.getProperty("port", "8080"));
	static final int RECONNECT_DELAY = Integer.parseInt(System.getProperty("reconnectDelay", "5"));
	static final int READ_TIMEOUT = Integer.parseInt(System.getProperty("readTimeout", "10"));

	private static final UptimeClientHandler handler = new UptimeClientHandler();

	public static void main(String[] args) throws InterruptedException {

		TcpClient client = TcpClient.create().host(HOST)
				.doOnChannelInit((observer, channel, remoteAddress) -> {
					System.err.println("OnChannelInit");
					channel.pipeline().addFirst(handler);
					channel.pipeline().addFirst(new IdleStateHandler(READ_TIMEOUT, 0, 0));
				})
				.doOnConnect(config -> System.err.println("OnConnect"))
				.doOnConnected(conn -> System.err.println("UptimeClient - OnConnected"))
				.doAfterResolve((conn, sock) -> System.err.println("AfterResolve"))
				.doOnDisconnected(conn -> {
					System.err.println("UptimeClient - OnDisconnected");
				})
				.doOnResolve(conn -> System.err.println("OnDisconnected"))
				.doOnResolve(conn -> System.err.println("OnResolve"))
				.doOnResolveError((conn, t) -> System.err.println("OnResolveError"))
				.port(PORT);
		
		Connection conn = client.connectNow();
		Mono<Void> dispose = conn.onDispose();
		dispose.block();

	}


}
