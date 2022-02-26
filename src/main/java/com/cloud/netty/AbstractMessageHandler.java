package com.cloud.netty;

import com.cloud.model.AbstractMessage;
import com.cloud.model.FileMessage;
import com.cloud.model.FileRequest;
import com.cloud.model.FilesList;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class AbstractMessageHandler extends SimpleChannelInboundHandler<AbstractMessage> {

    private Path currentPath;

    public AbstractMessageHandler(){
        currentPath = Paths.get("serverFiles");
        //currentPath = Paths.get(System.getProperty("user.home"));
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        ctx.writeAndFlush(new FilesList(currentPath));
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, AbstractMessage message) throws Exception {
        log.debug("Received: {}", message);

        switch (message.getMessageType()){
            case FILE_REQUEST:
                FileRequest req = (FileRequest) message;
                ctx.writeAndFlush(new FileMessage(currentPath.resolve(req.getFileName())));
                break;
            case FILE:
                FileMessage fileMessage = (FileMessage) message;
                Files.write(currentPath.resolve(fileMessage.getFileName()),
                        fileMessage.getBytes());
                ctx.writeAndFlush(new FilesList(currentPath));
                break;
        }
    }
}
