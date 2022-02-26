package com.cloud.client;

import com.cloud.model.AbstractMessage;
import com.cloud.model.FileMessage;
import com.cloud.model.FileRequest;
import com.cloud.model.FilesList;
import io.netty.handler.codec.serialization.ObjectDecoderInputStream;
import io.netty.handler.codec.serialization.ObjectEncoderOutputStream;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.Initializable;
import javafx.scene.control.ListView;

import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.File;
import java.io.IOException;
import java.net.Socket;
import java.net.URL;
import java.nio.file.*;
import java.util.ArrayList;
import java.util.List;
import java.util.ResourceBundle;
import java.util.stream.Collectors;


public class Controller implements Initializable {

    private Path baseDir;
    public ListView<String> clientFiles;
    public ListView<String> serverFiles;
    private ObjectDecoderInputStream is;
    private ObjectEncoderOutputStream os;


    private void read () {
        try{
            while(true){
                AbstractMessage msg = (AbstractMessage) is.readObject();
                switch (msg.getMessageType()){
                    case FILE:
                        FileMessage fileMessage = (FileMessage) msg;
                        Files.write(baseDir.resolve(fileMessage.getFileName()),
                                fileMessage.getBytes());
                        Platform.runLater(()->fillClientView(getFileNames()));
                        case FILES_LIST:
                        FilesList file = (FilesList) msg;
                        Platform.runLater(()->
                            fillServerView(file.getFiles()));
                        break;

                }
            }

        }catch (Exception e){
            e.printStackTrace();
        }
    }
    private void fillClientView(List<String> list){
            clientFiles.getItems().clear();
            clientFiles.getItems().addAll(list);
    }

    private void fillServerView(List<String> list){
        serverFiles.getItems().clear();
        serverFiles.getItems().addAll(list);
    }

    private List<String>getFileNames()  {
        try {
            return Files.list(baseDir)
            .map(p -> p.getFileName().toString())
            .collect(Collectors.toList());
        }catch (Exception e){
            return  new ArrayList<>();
        }

    }
    @Override
    public void initialize(URL location, ResourceBundle resources) {
        try{
            baseDir = Paths.get(System.getProperty("user.home"));
            clientFiles.getItems().addAll(getFileNames());
            clientFiles.setOnMouseClicked((e -> {
                if(e.getClickCount() == 2){
                    String file = clientFiles.getSelectionModel().getSelectedItem();
                    Path path = baseDir.resolve(file);
                    if(Files.isDirectory(path)){
                        baseDir = path;
                        fillClientView(getFileNames());
                    }
                }
            }));


            Socket socket = new Socket("localhost", 8780);
            os = new ObjectEncoderOutputStream(socket.getOutputStream());
            is = new ObjectDecoderInputStream(socket.getInputStream());
            Thread thread = new Thread(this::read);
            thread.setDaemon(true);
            thread.start();
        }catch (Exception e){
            e.printStackTrace();
        }
    }

    public void upload(ActionEvent actionEvent) throws IOException {
        String file = clientFiles.getSelectionModel().getSelectedItem().toString();
        Path filePath = baseDir.resolve(file);
        os.writeObject(new FileMessage(filePath));
    }

    public void download(ActionEvent actionEvent) throws IOException {
        String file = serverFiles.getSelectionModel().getSelectedItem().toString();
        os.writeObject(new FileRequest(file));
    }

}
