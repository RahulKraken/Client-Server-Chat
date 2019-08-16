package com.kraken;

import com.sun.xml.internal.ws.util.StringUtils;
import sun.misc.GThreadHelper;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.EOFException;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.InetAddress;
import java.net.Socket;

public class Client extends JFrame {

    private JTextField userText;
    private JTextArea chatWindow;
    private ObjectOutputStream output;
    private ObjectInputStream input;
    private String message = "";
    private String serverIP;
    private Socket connection;

    // constructor
    public Client(String host) {
        super("Kraken's awesome chat client");
        serverIP = host;
        userText = new JTextField();
        userText.setEditable(false);
        userText.addActionListener(e -> {
            sendData(e.getActionCommand());
            userText.setText("");
        });
        add(userText, BorderLayout.NORTH);
        chatWindow = new JTextArea();
        add(new JScrollPane(chatWindow), BorderLayout.CENTER);
        setSize(300, 300);
        setVisible(true);
    }

    // run the client
    public void startRunning() {
        try {
            connectToServer();
            setupStreams();
            whileChatting();
        } catch (EOFException e) {
            showMessage("\n Client terminated the connection");
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            closeCrap();
        }
    }

    // connect to server
    private void connectToServer() throws IOException {
        showMessage("\nAttempting connection...\n");
        connection = new Socket(InetAddress.getByName(serverIP), 8080);
        showMessage("Connected to: " + connection.getInetAddress().getHostName());
    }

    // setup Streams
    private void setupStreams() throws IOException {
        output = new ObjectOutputStream(connection.getOutputStream());
        output.flush();
        input = new ObjectInputStream(connection.getInputStream());
        showMessage("\nStreams are not setup!");
    }

    // user can chat
    private void whileChatting() throws IOException {
        ableToType(true);
        do {
            try {
                message = (String) input.readObject();
                showMessage(message);
            } catch (ClassNotFoundException e) {
                showMessage("\nI DON'T KNOW WTF WAS RECEIVED");
            }
        } while (!message.equals("SERVER - END"));
    }

    // close crap down
    private void closeCrap() {
        showMessage("\nClosing crap down");
        ableToType(false);
        try {
            output.close();
            input.close();
            connection.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    // send data to server
    private void sendData(String message) {
        try {
            output.writeObject("CLIENT - " + message);
            output.flush();
            showMessage("CLIENT - " + message);
        } catch (IOException e) {
            chatWindow.append("\nSomething went wrong while sending msg.");
        }
    }

    // show message in chat window
    private void showMessage(String message) {
        SwingUtilities.invokeLater(() -> chatWindow.append("\n" + message));
    }

    // let user type
    private void ableToType(boolean status) {
        SwingUtilities.invokeLater(() -> userText.setEditable(status));
    }
}
