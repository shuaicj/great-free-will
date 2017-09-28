package shuaicj.hobby.great.free.will.daemon.client;

import javax.annotation.PostConstruct;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LoggingHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

/**
 * Netty entry of client daemon.
 *
 * @author shuaicj 2017/09/28
 */
@Component
@Profile("client")
@Slf4j
public class ClientDaemon {

    @Value("${client.daemon.port}") private int port;
    @Autowired private ApplicationContext appCtx;

    @PostConstruct
    public void start() {
        new Thread(() -> {
            logger.info("ClientDaemon started on port: {}", port);
            EventLoopGroup bossGroup = new NioEventLoopGroup(1);
            EventLoopGroup workerGroup = new NioEventLoopGroup();
            try {
                ServerBootstrap b = new ServerBootstrap();
                b.group(bossGroup, workerGroup)
                        .channel(NioServerSocketChannel.class)
                        .handler(appCtx.getBean(LoggingHandler.class))
                        .childHandler(new ChannelInitializer<SocketChannel>() {
                            @Override
                            protected void initChannel(SocketChannel ch) throws Exception {
                                ch.pipeline().addLast(
                                        appCtx.getBean(LoggingHandler.class),
                                        appCtx.getBean(ClientDaemonDecoder.class),
                                        appCtx.getBean(ClientDaemonEncoder.class)
                                );
                            }
                        })
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
