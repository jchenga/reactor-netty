/*
 * Copyright (c) 2011-2022 VMware, Inc. or its affiliates, All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *   https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package reactor.netty.http.websocket;

import java.nio.charset.Charset;
import java.util.function.BiFunction;
import java.util.function.Function;

import io.netty5.buffer.api.Buffer;
import io.netty5.buffer.api.BufferAllocator;
import io.netty5.handler.codec.http.websocketx.BinaryWebSocketFrame;
import io.netty5.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty5.handler.codec.http.websocketx.WebSocketFrame;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.netty.NettyOutbound;
import reactor.util.annotation.Nullable;

/**
 * A websocket framed outbound
 *
 * @author Stephane Maldini
 * @author Simon Baslé
 * @since 0.6
 */
public interface WebsocketOutbound extends NettyOutbound {

	/**
	 * Returns the websocket subprotocol negotiated by the client and server during
	 * the websocket handshake, or null if none was requested.
	 *
	 * @return the subprotocol, or null
	 */
	@Nullable
	String selectedSubprotocol();

	@Override
	NettyOutbound send(Publisher<? extends Buffer> dataStream);

	/**
	 * Prepare to send a close frame on subscribe then close the underlying channel
	 *
	 * @return a {@link Mono} fulfilled when the send succeeded or failed, immediately
	 * completed if already closed
	 */
	Mono<Void> sendClose();

	/**
	 * Prepare to send a close frame on subscribe then close the underlying channel
	 *
	 * @param rsv
	 *            reserved bits used for protocol extensions
	 *
	 * @return a {@link Mono} fulfilled when the send succeeded or failed, immediately
	 * completed if already closed
	 */
	Mono<Void> sendClose(int rsv);

	/**
	 * Prepare to send a close frame on subscribe then close the underlying channel
	 *
	 * @param statusCode
	 *            Integer status code as per <a href="https://tools.ietf.org/html/rfc6455#section-7.4">RFC 6455#section-7.4</a>. For
	 *            example, <tt>1000</tt> indicates normal closure.
	 * @param reasonText
	 *            Reason text. Set to null if no text.
	 *
	 * @return a {@link Mono} fulfilled when the send succeeded or failed, immediately
	 * completed if already closed
	 * @throws IllegalArgumentException when the status code MUST NOT be set as a status code in a
	 * Close control frame.
	 * Consider checking <a href="https://datatracker.ietf.org/doc/html/rfc6455#section-7.4.1">RFC 6455#section-7.4</a>
	 * for a complete list of the close status codes.
	 */
	Mono<Void> sendClose(int statusCode, @Nullable String reasonText);

	/**
	 * Prepare to send a close frame on subscribe then close the underlying channel
	 *
	 * @param rsv
	 *            reserved bits used for protocol extensions
	 * @param statusCode
	 *            Integer status code as per <a href="https://tools.ietf.org/html/rfc6455#section-7.4">RFC 6455#section-7.4</a>. For
	 *            example, <tt>1000</tt> indicates normal closure.
	 * @param reasonText
	 *            Reason text. Set to null if no text.
	 *
	 * @return a {@link Mono} fulfilled when the send succeeded or failed, immediately
	 * completed if already closed
	 * @throws IllegalArgumentException when the status code MUST NOT be set as a status code in a
	 * Close control frame.
	 * Consider checking <a href="https://datatracker.ietf.org/doc/html/rfc6455#section-7.4.1">RFC 6455#section-7.4</a>
	 * for a complete list of the close status codes.
	 */
	Mono<Void> sendClose(int rsv, int statusCode, @Nullable String reasonText);

	@Override
	default NettyOutbound sendString(Publisher<? extends String> dataStream, Charset charset) {
		return sendObject(Flux.from(dataStream)
		                      .map(str -> stringToWebsocketFrame.apply(alloc(), str)));
	}

	BiFunction<? super BufferAllocator, ? super String, ? extends WebSocketFrame> stringToWebsocketFrame  =
			TextWebSocketFrame::new;
	Function<? super Buffer, ? extends WebSocketFrame> bytebufToWebsocketFrame =
			BinaryWebSocketFrame::new;
}
