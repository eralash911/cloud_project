package com.cloud.firstServer;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class Handler implements Runnable {

    private Path currentDir;
    private DataOutputStream os;
    private DataInputStream is;
    private static final int BUFFER_SIZE =8192;
    private  byte[] buffer;


    public Handler(Socket socket) throws IOException {
        is = new DataInputStream(socket.getInputStream());
        os = new DataOutputStream(socket.getOutputStream());
        currentDir = Paths.get("serverFiles");
        System.out.println("Client accepted.....");
        sendServerFiles();
        buffer = new byte[BUFFER_SIZE];

    }

    private List<String>getFileNames() throws IOException {
        return Files.list(currentDir)
                .map(p->p.getFileName().toString())
                .collect(Collectors.toList());
    }

    private  void sendServerFiles() throws IOException {
        os.writeUTF("#list#");
        List<String> names = getFileNames();
        os.writeInt(names.size());
        for (String name :
                names) {
            os.writeUTF(name);
        }
        os.flush();
    }
    @Override
    public void run() {
        while (true) {
            try {
                String command = is.readUTF();
                System.out.println("received command " + command);
                    if(command.equals("#upload")){
                        String fileName = is.readUTF();
                        long size = is.readLong();
                        try(FileOutputStream fos = new FileOutputStream(currentDir.resolve(fileName).toFile())){
                            for (int i = 0; i < (size + BUFFER_SIZE - 1)/BUFFER_SIZE; i++) {
                                int read = is.read(buffer);

                                fos.write(buffer, 0,read);
                            }
                        }
                        sendServerFiles();
                    }

            } catch (Exception e) {
                e.printStackTrace();
            }

        }
    }

}
