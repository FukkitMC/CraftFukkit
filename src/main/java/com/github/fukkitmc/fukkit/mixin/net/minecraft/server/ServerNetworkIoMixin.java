package com.github.fukkitmc.fukkit.mixin.net.minecraft.server;

import com.github.fukkitmc.fukkit.access.net.minecraft.server.ServerNetworkIoAccess;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import net.minecraft.server.ServerNetworkIo;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;
import java.util.List;

@Implements(@Interface(iface = ServerNetworkIoAccess.class, prefix = "fukkit$"))
@Mixin(ServerNetworkIo.class)
public abstract class ServerNetworkIoMixin {
	@Shadow @Final private List<ChannelFuture> channels;

	@Redirect(method = "bind", at = @At(value = "INVOKE", target = "Lio/netty/bootstrap/ServerBootstrap;bind()Lio/netty/channel/ChannelFuture;", remap = false))
	public ChannelFuture bind(ServerBootstrap bootstrap) {
		return bootstrap.option(ChannelOption.AUTO_READ, false).bind();
	}

	public void fukkit$acceptConnections() {
		synchronized (this.channels) {
			for (ChannelFuture channel : this.channels) {
				channel.channel().config().setAutoRead(true);
			}
		}
	}
}
