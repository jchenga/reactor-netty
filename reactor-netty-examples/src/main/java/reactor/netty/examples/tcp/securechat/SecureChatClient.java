package reactor.netty.examples.tcp.securechat;

import java.util.Scanner;

import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.ssl.util.InsecureTrustManagerFactory;
import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.tcp.TcpClient;
import reactor.netty.tcp.TcpSslContextSpec;

public class SecureChatClient {

	private static final String HOST = System.getProperty("host", "127.0.0.1");

	private static final int PORT = Integer.parseInt(System.getProperty("port", "8992"));

	private static final boolean WIRETAP = System.getProperty("wiretap") != null;

	public static void main(String[] args) {
		TcpSslContextSpec tcpSslContextSpec = TcpSslContextSpec.forClient()
				.configure(builder -> builder.trustManager(InsecureTrustManagerFactory.INSTANCE));

		TcpClient client = TcpClient.create()
				.host(HOST)
				.port(PORT)
				.wiretap(WIRETAP)
				.doOnConnected(connection -> {
					connection.addHandlerLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
				}).secure(spec -> spec.sslContext(tcpSslContextSpec));

		Connection conn = client.connectNow();
		conn.inbound()
				.receive()
				.asString()
				.doOnNext(msg -> System.out.println(msg))
				.subscribe();

		Scanner scanner = new Scanner(System.in);
		while (scanner.hasNext()) {
			String text = scanner.nextLine();
			conn.outbound().sendString(Mono.just(text + "\r\n"))
					.then()
					.subscribe();

			if ("bye".equalsIgnoreCase(text))
				break;
		}

		conn.onDispose().block();

	}
}
