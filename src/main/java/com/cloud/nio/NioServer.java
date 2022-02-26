package com.cloud.nio;

import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Set;

@Slf4j
public class NioServer {

    private Selector selector;
    private ServerSocketChannel socketChannel;
    private int cnt;
    private String name;
    private ByteBuffer buffer;

    public NioServer() throws IOException {
        buffer = ByteBuffer.allocate(4);
        cnt = 1;
        selector = Selector.open();
        socketChannel = ServerSocketChannel.open();
        socketChannel.configureBlocking(false);
        socketChannel.register(selector, SelectionKey.OP_ACCEPT);
        socketChannel.bind(new InetSocketAddress(8489));
       log.debug("server started!!!!!!");

        while (socketChannel.isOpen()){
            selector.select();
            Set<SelectionKey> keySet = selector.selectedKeys();
            Iterator<SelectionKey> iterator = keySet.iterator();
            while (iterator.hasNext()){
               SelectionKey selectionKey =  iterator.next();
               if(selectionKey.isAcceptable()){
                   handleAccept();
               }
               if(selectionKey.isReadable()){
                   handleRead(selectionKey);
               }
               iterator.remove();
            }
        }
    }

    private void handleRead(SelectionKey selectionKey) throws IOException {
        if(selectionKey.isValid()) {
            SocketChannel socketChannel = (SocketChannel) selectionKey.channel();
            StringBuilder stringBuilder = new StringBuilder();
            while (true){
                int read = socketChannel.read(buffer);
                if(read == -1){
                    socketChannel.close();
                    log.debug("CLient disconected");
                    cnt--;
                    return;
                }
                if (read == 0){
                    break;
                }
                buffer.flip();
                while (buffer.hasRemaining()){
                    stringBuilder.append((char) buffer.get());
                }
                buffer.clear();
            }
            String result = selectionKey.attachment() + " : " + stringBuilder.toString() ;
            log.debug("received: {}", result);
            for (SelectionKey key : selector.selectedKeys()) {
                if(selectionKey .isValid() && selectionKey.channel() instanceof SocketChannel){
                    ((SocketChannel) selectionKey.channel()).write(ByteBuffer.wrap(result.getBytes(StandardCharsets.UTF_8)));
                }

            }

        }
    }

    private void handleAccept() throws IOException {
        name = "User" + cnt;
        cnt ++;
        SocketChannel channel = socketChannel.accept();
        channel.configureBlocking(false);
        channel.register(selector, SelectionKey.OP_READ, name);
        channel.write(ByteBuffer.wrap(("Hello " + name).getBytes(StandardCharsets.UTF_8)));
        log.debug("client {} accepted!!!!!!!!", name);
    }


    public static void main(String[] args) throws IOException {
        new NioServer();
    }
}
