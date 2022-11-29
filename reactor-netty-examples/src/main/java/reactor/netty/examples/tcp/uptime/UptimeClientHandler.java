package reactor.netty.examples.tcp.uptime;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;


public class UptimeClientHandler extends SimpleChannelInboundHandler<Object> {
	long startTime = -1;

	@Override
	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
		System.err.println("ChannelUnregistered");
	}

	@Override
	public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
		System.err.println("ChannelReadComplete");
		super.channelReadComplete(ctx);
	}

	@Override
	public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
		System.err.println("ChannelRegistered");
		super.channelRegistered(ctx);
	}

	@Override
	public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
		System.err.println("ExceptionCaught");
		super.exceptionCaught(ctx, cause);
	}

	@Override
	public void channelActive(ChannelHandlerContext ctx) throws Exception {
		if (startTime < 0) {
			startTime = System.currentTimeMillis();
		}
		println("Connected to: " + ctx.channel().remoteAddress());
		super.channelActive(ctx);
	}

	@Override
	public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
		if (!(evt instanceof IdleStateEvent)) {
			super.userEventTriggered(ctx, evt);
			return;
		}
		IdleStateEvent e = (IdleStateEvent) evt;
		if (e.state() == IdleState.READER_IDLE) {
			// The connection was OK but there was no traffic for last period.
			println("Disconnecting due to no inbound traffic");
			ctx.close();
		}
	}

	@Override
	public void channelInactive(ChannelHandlerContext ctx) {
		println("Disconnected from: " + ctx.channel().remoteAddress());

	}


//	@Override
//	public void channelUnregistered(ChannelHandlerContext ctx) throws Exception {
//		println("Sleeping for: " + UptimeClient.RECONNECT_DELAY + 's');
//
//
//		ctx.channel().eventLoop().schedule(new Runnable() {
//			@Override
//			public void run() {
//				println("Reconnecting to: " + UptimeClient.HOST + ':' + UptimeClient.PORT);
//			}
//		}, UptimeClient.RECONNECT_DELAY, TimeUnit.SECONDS);
//
//	}


	void println(String msg) {
		if (startTime < 0) {
			System.err.format("[SERVER IS DOWN] %s%n", msg);
		}
		else {
			System.err.format("[UPTIME: %5ds] %s%n", (System.currentTimeMillis() - startTime) / 1000, msg);
		}
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {
		// Discard received data
	}
}
