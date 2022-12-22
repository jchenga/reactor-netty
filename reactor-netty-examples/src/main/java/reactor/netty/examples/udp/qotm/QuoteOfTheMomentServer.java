package reactor.netty.examples.udp.qotm;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.socket.DatagramPacket;
import reactor.core.publisher.Flux;
import reactor.netty.Connection;
import reactor.netty.udp.UdpServer;

import java.nio.charset.StandardCharsets;
import java.util.Random;

public class QuoteOfTheMomentServer {
	private static final Random random = new Random();
	private static final String[] quotes = {
			"Where there is love there is life.",
			"First they ignore you, then they laugh at you, then they fight you, then you win.",
			"Be the change you want to see in the world.",
			"The weak can never forgive. Forgiveness is the attribute of the strong.",
	};

	private static String nextQuote() {
		int quoteId;
		synchronized (random) {
			quoteId = random.nextInt(quotes.length);
		}
		return quotes[quoteId];

	}

	public static void main(String[] args) {

		UdpServer server = UdpServer.create()
				.host("localhost")
				.port(8081)
				.wiretap(true)
				.handle((in, out) -> {
					Flux<Object> inFlux = in.receiveObject()
							.filter(obj -> {
								if (obj instanceof DatagramPacket) {
									DatagramPacket packet = (DatagramPacket) obj;
									String content = packet.content().toString(StandardCharsets.UTF_8);
									return "QOTM".equalsIgnoreCase(content);
								}
								return false;
							})
							.map(obj -> {
								DatagramPacket packet = (DatagramPacket) obj;
								String nextQuote = nextQuote();
								ByteBuf byteBuf = Unpooled.copiedBuffer("QOTM: " + nextQuote, StandardCharsets.UTF_8);
								return new DatagramPacket(byteBuf, packet.sender());
							});

					return out.sendObject(inFlux);
				});
//				.handle((in, out) -> {
//					Flux<Object> inFlux = in.receiveObject()
//							.filter(obj -> {})
//							.map(obj -> {
//
//								if (obj instanceof DatagramPacket) {
//									DatagramPacket packet = (DatagramPacket) obj;
//									String content = packet.content().toString(StandardCharsets.UTF_8);
//									if ("QOTM".equalsIgnoreCase(content)) {
//										String nextQuote = nextQuote();
//										ByteBuf byteBuf = Unpooled.copiedBuffer("QOTM: " + nextQuote, StandardCharsets.UTF_8);
//										return new DatagramPacket(byteBuf, packet.sender());
//									}
//								}
//							});
//					return out.sendObject(inFlux);
//					//return in.receive().then();
//				});


		Connection conn = server.bindNow();
		conn.onDispose().block();
	}
}
