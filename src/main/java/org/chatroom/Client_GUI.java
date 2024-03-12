package org.chatroom;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;


public class Client_GUI extends JFrame {
    private JPanel Form;
    private JPanel chatOptions;
    private JPanel chatArea;
    private JLabel groupName;
    private JTextArea msgsArea;
    private JTextField msgTxt;
    private JButton sendBtn;
    private JButton leaveBtn;
    private JLabel statusLbl;
    private JTextField usernameTxt;
    private JButton usernameBtn;
    private BufferedReader reader;
    private PrintWriter writer;
    private Socket socket;
    private String username;

    public Client_GUI() {
        setContentPane(Form);
        Form.setBorder(new EmptyBorder(10, 10, 10, 10));
        Form.setBackground(Color.lightGray);
        chatOptions.setBorder(new EmptyBorder(10, 10, 10, 10));
        chatArea.setBorder(new EmptyBorder(10, 10, 10, 10));
        msgsArea.setBackground(Color.white);
        msgsArea.setEditable(false);
        msgsArea.setEnabled(false);
        msgTxt.setEnabled(false);
        sendBtn.setEnabled(false);
        usernameBtn.setEnabled(false);
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(420, 569);
        setVisible(true);

        setLocationRelativeTo(null);
        createSocket();
        createReader_Writer();

        JOptionPane.showMessageDialog(this, "Enter username");

        setUsername();

        sendMsg();

        readMsg();

        leaveChat();
    }

    public void createSocket() {
        try {
            this.socket = new Socket("localhost", 5555);
            this.statusLbl.setForeground(Color.GREEN);
            this.statusLbl.setText("Connected");
            usernameBtn.setEnabled(true);

        } catch (Exception e) {
            this.statusLbl.setForeground(Color.red);
            this.statusLbl.setText("Error creating socket");
        }
    }

    public void createReader_Writer() {
        if (this.socket != null) {
            try {
                this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                this.writer = new PrintWriter(this.socket.getOutputStream(), true);
            } catch (Exception e) {
                System.out.println("Error:\n" + e.getMessage());
            }
        } else this.statusLbl.setText("Server is down");
    }

    public void setUsername() {
        usernameBtn.addActionListener(e -> {
            username = usernameTxt.getText();
            username = username.replace(" ", "_").trim();
            if (!username.isEmpty()) {
                System.out.println(username);
                writer.println(username.toUpperCase());
                this.msgsArea.setEnabled(true);
                this.msgTxt.setEnabled(true);
                this.sendBtn.setEnabled(true);
                this.usernameBtn.setEnabled(false);
                this.usernameTxt.setEnabled(false);
            }
        });
    }

    public void sendMsg() {
        sendBtn.addActionListener(e -> {
            String msgToSend = msgTxt.getText().trim();
            if (!msgToSend.isEmpty()) {
                writer.println(msgToSend);
            }
            msgTxt.setText("");
        });
    }

    public void readMsg() {
        new Thread(() -> {
            try {
                while (true) {
                    String receivedMessage = reader.readLine();
                    if (receivedMessage != null && !receivedMessage.isEmpty()) {
                        if (receivedMessage.endsWith("jpg")) {
                            System.out.println("Got " + receivedMessage.substring(7));
                            getImage(receivedMessage);
                        } else msgsArea.append(receivedMessage + "\n");
                    }
                }
            } catch (Exception e) {
                System.err.println("Server is down");
//                e.printStackTrace();
            }
        }).start();
    }

    public void getImage(String imgPath) {
        try {
            File imageFile = new File(imgPath.substring(imgPath.indexOf(" ")).trim());
            if (imageFile.exists()) {
                System.out.println("EXISTS");
                BufferedImage image = ImageIO.read(imageFile);
                ImageIcon icon = new ImageIcon(new ImageIcon(image).getImage().getScaledInstance(242, 269, Image.SCALE_DEFAULT));
                JOptionPane.showMessageDialog(this, new JLabel(icon));
            } else System.out.println("Doesnt exist");
        } catch (IOException e) {
            System.err.println("Error reading image: " + e.getMessage());
        }
    }
    public void leaveChat() {
        leaveBtn.addActionListener(e -> {
            dispose();
            SwingUtilities.invokeLater(Client_GUI::new);
        });
    }
}


