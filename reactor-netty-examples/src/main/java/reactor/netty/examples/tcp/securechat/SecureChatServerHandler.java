package reactor.netty.examples.tcp.securechat;

import io.netty.channel.Channel;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;

@ChannelHandler.Sharable
public class SecureChatServerHandler extends SimpleChannelInboundHandler<String> {
	private final ChannelGroup group;

	public SecureChatServerHandler(ChannelGroup group) {
		this.group = group;
	}

	@Override
	protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
		Channel current = ctx.channel();
		for (Channel channel : group) {
			// broadcast to other channels in the group
			if (channel != null) {
				channel.writeAndFlush("[" + current.remoteAddress() + "]" + msg + '\n');
			}
		}
	}
}
