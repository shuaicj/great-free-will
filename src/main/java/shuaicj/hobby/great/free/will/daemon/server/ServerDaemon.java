package shuaicj.hobby.great.free.will.daemon.server;

import javax.annotation.PostConstruct;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Netty entry of server daemon.
 *
 * @author shuaicj 2017/09/28
 */
@Component
@Profile("server")
@Slf4j
public class ServerDaemon {

    @Value("${server.daemon.port}") private int port;
    @Autowired private ApplicationContext appCtx;

    @PostConstruct
    public void start() {
        new Thread(() -> {
            logger.info("ServerDaemon started on port: {}", port);
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .handler(new LoggingHandler(LogLevel.DEBUG))
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline().addLast(
                                        new LoggingHandler(LogLevel.DEBUG),
                                        appCtx.getBean(ServerDaemonTunnelDecoder.class),
                                        appCtx.getBean(ServerDaemonTunnelEncoder.class),
                                        appCtx.getBean(ServerDaemonTunnelHandler.class)
                                );
                            }
                        })
                        .option(ChannelOption.SO_BACKLOG, 128)
                        .childOption(ChannelOption.SO_KEEPALIVE, true)
                        .bind(port).sync().channel().closeFuture().sync();
            } catch (InterruptedException e) {
                logger.error("shit happens", e);
            } finally {
                bossGroup.shutdownGracefully();
                workerGroup.shutdownGracefully();
            }
        }).start();
    }
}
