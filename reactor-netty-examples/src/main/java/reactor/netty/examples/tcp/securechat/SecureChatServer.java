package reactor.netty.examples.tcp.securechat;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.security.cert.CertificateException;

import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.DelimiterBasedFrameDecoder;
import io.netty.handler.codec.Delimiters;
import io.netty.handler.ssl.util.SelfSignedCertificate;
import io.netty.util.concurrent.GlobalEventExecutor;
import reactor.core.publisher.Flux;
import reactor.netty.tcp.TcpServer;
import reactor.netty.tcp.TcpSslContextSpec;

public class SecureChatServer {

	private static final int PORT = Integer.parseInt(System.getProperty("port", "8992"));

	private static final boolean WIRETAP = System.getProperty("wiretap") != null;

	public static final void main(String[] args) throws UnknownHostException, CertificateException {
		ChannelGroup channelGroup = new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
		SecureChatServerHandler broadcastHandler = new SecureChatServerHandler(channelGroup);
		String hostname = InetAddress.getLocalHost().getHostName();
		TcpServer server = TcpServer.create()
				.port(PORT)
				.wiretap(WIRETAP)
				.doOnConnection(connection -> {
					connection.addHandlerLast(new DelimiterBasedFrameDecoder(8192, Delimiters.lineDelimiter()));
					connection.addHandlerLast(broadcastHandler);
				}).channelGroup(channelGroup)
				.handle((in, out) -> {
					// original example send a message that includes information of cipher suite used
					// by the SslHandler. How do we retrieve that information in Reactor Netty?
					Flux<String> welcomeFlux = Flux.just("Welcome to " + hostname + " secure chat service!\n");
					Flux<String> flux = in.receive()
							.asString()
							.takeUntil("bye"::equalsIgnoreCase)
							.map(msg -> "[you] " + msg);

					return out.sendString(Flux.concat(welcomeFlux, flux));

				});

		SelfSignedCertificate ssc = new SelfSignedCertificate();
		server = server.secure(spec -> spec.sslContext(TcpSslContextSpec.forServer(ssc.certificate(), ssc.privateKey())));
		server.bindNow().onDispose().block();
	}
}
