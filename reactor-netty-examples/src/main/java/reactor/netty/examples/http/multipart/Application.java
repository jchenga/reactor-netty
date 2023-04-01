package reactor.netty.examples.http.multipart;

import reactor.core.publisher.Mono;
import reactor.netty.DisposableServer;
import reactor.netty.http.server.HttpServer;

public class Application {

	public static void main(String[] args) {
		DisposableServer server =
				HttpServer.create()
						.route(routes -> routes.post("/multipart", (request, response) ->
						  response.sendString(request.receiveForm()
								  .flatMap(data -> Mono.just('[' + data.getName() + ']'))))).bindNow();
		server.onDispose().block();
	}
}
