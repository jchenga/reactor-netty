package reactor.netty.examples.udp.qotm;

import reactor.core.publisher.Mono;
import reactor.netty.Connection;
import reactor.netty.udp.UdpClient;

import java.nio.charset.StandardCharsets;
import java.time.Duration;

public class QuoteOfTheMomentClient {
	public static void main(String[] args) {
		UdpClient client = UdpClient.create()
				.host("localhost")
				.port(8081)
				.wiretap(true)
				.handle((in, out) -> in.receive().asString(StandardCharsets.UTF_8)
						.doOnNext(text -> {
									System.out.println(text);
									in.withConnection(conn -> conn.disposeNow(Duration.ofSeconds(5)));
								}
						).then());
		Connection conn = client.connectNow();
		conn.outbound().sendString(Mono.just("QOTM1")).then().subscribe();
		conn.onDispose().block();
	}
}
