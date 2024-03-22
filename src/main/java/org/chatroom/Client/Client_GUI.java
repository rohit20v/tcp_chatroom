package org.chatroom.Client;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.util.concurrent.atomic.AtomicReference;

public class Client_GUI extends JFrame {
    private static String host = "localhost";
    private static int port = 5555;
    private JPanel Form;
    private JPanel chatOptions;
    private JPanel chatArea;
    private JLabel groupName;
    private JTextArea msgArea;
    private JTextField msgTxt;
    private JButton sendBtn;
    private JButton leaveBtn;
    private JLabel statusLbl;
    private JTextField usernameTxt;
    private JButton usernameBtn;
    private JTextField createGrpNameTxt;
    private JButton askCreateBtn;
    private JButton askJoinBtn;
    private JTextField joinGrpNameTxt;
    private JButton createBtn;
    private JButton joinBtn;
    private JTextField createGrpCodeTxt;
    private JTextField joinGrpCodeTxt;
    private JButton showGrpsBtn;
    private JLabel gc_name;
    private JRadioButton prvtBtn;
    private JTextField renameTxt;
    private JButton renameBtn;
    private BufferedReader reader;
    private PrintWriter writer;
    private Socket socket;
    private String username;
    private boolean inGroup = false;
    AtomicReference<String> receivedMessage = new AtomicReference<>("");


    public Client_GUI() {
        SwingUtilities.invokeLater(() -> {

            setContentPane(Form);
            swingStyle();
            msgArea.setEditable(false);
            msgTxt.setEnabled(false);
            sendBtn.setEnabled(false);
            usernameTxt.setEnabled(false);
            usernameBtn.setEnabled(false);
            renameTxt.setEnabled(false);
            renameBtn.setEnabled(false);
            leaveBtn.setEnabled(false);

            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(888, 690);
            setVisible(true);

            setLocationRelativeTo(null);


            createReader_Writer();

            startCreatingGroup();
            startJoiningGroup();
//
//            createGroup();
//            joinGroup();

            showGroups();

            setUsername();

            sendMsg();

            readMsg();

            leaveChat();

        });

        // Thread connessione
        new Thread(() -> {
            createSocket();

        }).start();
    }

    private void swingStyle() {
        Form.setBorder(new EmptyBorder(10, 10, 10, 10));
        Form.setBackground(Color.decode("#0F1035"));
        chatOptions.setBorder(new EmptyBorder(20, 10, 10, 10));
        chatArea.setBorder(new EmptyBorder(10, 10, 10, 10));
//        msgArea.setBackground(Color.decode("#FFFFF"));

        createBtn.setMinimumSize(new Dimension(111, 20));
        joinBtn.setMinimumSize(new Dimension(111, 20));
        prvtBtn.setOpaque(false);
    }

    public void createSocket() {
        do {
            try {
                this.socket = new Socket("localhost", 5555);
                System.out.println("Server up");
                updateStatus(Color.GREEN, "Connected");
                break;
            } catch (Exception e) {
                System.out.println("Server down");
                updateStatus(Color.red, "Server is down");
            }
            try {
                Thread.sleep(2000);
            } catch (InterruptedException e) {
                throw new RuntimeException(e);
            }
        } while (true);
    }

    private void updateStatus(Color green, String Connected) {
        SwingUtilities.invokeLater(() -> {
            this.statusLbl.setForeground(green);
            this.statusLbl.setText(Connected);
        });
    }

    private void createReader_Writer() {
        if (this.socket != null) {
            try {
                this.reader = new BufferedReader(new InputStreamReader(this.socket.getInputStream()));
                this.writer = new PrintWriter(this.socket.getOutputStream(), true);
            } catch (Exception e) {
                System.out.println("Error:\n" + e.getMessage());
                updateStatus(Color.red, "Server is down");
            }
        } else this.statusLbl.setText("Server is down");
    }

    private void setUsername() {
        usernameBtn.addActionListener(e -> {
            username = usernameTxt.getText();
            username = username.replace(" ", "_").trim();
            if (!username.isEmpty()) {
                System.out.println(username);
                writer.println(username.toUpperCase());
                this.msgTxt.setEnabled(true);
                this.sendBtn.setEnabled(true);
                this.usernameBtn.setEnabled(false);
                this.usernameTxt.setEnabled(false);
            }
        });
    }

    private void sendMsg() {
        sendBtn.addActionListener(e -> {
            String msgToSend = msgTxt.getText().trim();
            if (!msgToSend.isEmpty()) {
                writer.println(msgToSend);
            }
            msgTxt.setText("");
        });
    }

    private void readMsg() {
        new Thread(() -> {
            try {
                while (true) {
                    receivedMessage.set(reader.readLine());
                    if (receivedMessage.get() != null && !receivedMessage.get().isEmpty()) {
                        if (receivedMessage.get().endsWith("jpg") || receivedMessage.get().endsWith("png")) {
                            System.out.println("Got " + receivedMessage);
                            getImage(receivedMessage.get());
                        } else {
                            System.out.println("Got " + receivedMessage);
                            msgArea.append(receivedMessage + "\n");
                        }
//                        else {
//                            JOptionPane.showMessageDialog(null, receivedMessage);
//                        }
                    }
                }
            } catch (Exception e) {
                System.err.println("Server is down");
//                e.printStackTrace();
                updateStatus(Color.red, "Server is down");
            }
        }).start();
    }

    private void getImage(String imgPath) {
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
            updateStatus(Color.red, "Server is down");

        }
    }

    private void startCreatingGroup() {
        askCreateBtn.addActionListener(e -> {
            writer.println("1");
            askJoinBtn.setEnabled(false);
            askCreateBtn.setEnabled(false);
            createGrpNameTxt.setEnabled(true);
            createGrpCodeTxt.setEnabled(true);
            createBtn.setEnabled(true);
            showGrpsBtn.setEnabled(false);
            createGroup();
        });
    }

    private void createGroup() {
        createBtn.addActionListener(e -> {
            if (createBtn.isEnabled()) {
                if (!createGrpCodeTxt.getText().isEmpty() && !createGrpNameTxt.getText().isEmpty()) {
                    this.writer.println(createGrpNameTxt.getText());
                    this.writer.println(createGrpCodeTxt.getText());
                    groupName.setText(createGrpNameTxt.getText());
                    createGrpNameTxt.setText("");
                    createGrpCodeTxt.setText("");
                    createGrpNameTxt.setEnabled(false);
                    createGrpCodeTxt.setEnabled(false);
                    createBtn.setEnabled(false);
                    JOptionPane.showMessageDialog(this, "Enter username", "", JOptionPane.PLAIN_MESSAGE);
                    usernameBtn.setEnabled(true);
                    leaveBtn.setEnabled(true);
                    usernameTxt.setEnabled(true);
                }
            }
        });
    }

    private void startJoiningGroup() {
        askJoinBtn.addActionListener(e -> {
            if (this.writer == null) {
                System.err.println("Server is not online");
            } else {
                this.writer.println("2");
                new Thread(() -> {
                    String msg = "";
                    try {
                        msg = receivedMessage.get();
                    } catch (Exception ex) {
                        System.out.println(ex.getMessage());
                        ;
                    }
                    if (msg.equals("Inserisci il nome e la password del gruppo")) {
                        askCreateBtn.setEnabled(false);
                        askJoinBtn.setEnabled(false);
                        joinGrpNameTxt.setEnabled(true);
                        joinGrpCodeTxt.setEnabled(true);
                        joinBtn.setEnabled(true);
                        joinGroup();
                        showGrpsBtn.setEnabled(false);
                    }
                }).start();
            }
        });
    }

    private void joinGroup() {
        joinBtn.addActionListener(e -> {
            System.out.println("In join");
            if (joinBtn.isEnabled()) {
                if (!joinGrpCodeTxt.getText().isEmpty() && !joinGrpNameTxt.getText().isEmpty()) {
                    this.writer.println(joinGrpNameTxt.getText());
                    this.writer.println(joinGrpCodeTxt.getText());
                    groupName.setText(joinGrpNameTxt.getText());
                    joinGrpNameTxt.setText("");
                    joinGrpCodeTxt.setText("");
                    joinGrpNameTxt.setEnabled(false);
                    joinGrpCodeTxt.setEnabled(false);
                    joinBtn.setEnabled(false);
                    usernameBtn.setEnabled(true);
                    leaveBtn.setEnabled(true);
                    usernameTxt.setEnabled(true);
                }
            }
        });
    }


    private void showGroups() {
        showGrpsBtn.addActionListener(e -> {
            this.writer.println("3");
            new Thread(() -> {
                try {
//                    JOptionPane.showMessageDialog(this, this.reader.readLine());
                } catch (Exception ex) {
                    System.err.println(ex.getMessage() + "\nLine: 175 ");
                    updateStatus(Color.red, "Server is down");

                }
            }).start();
        });
    }

    private void leaveChat() {
        leaveBtn.addActionListener(e -> {
            this.writer.println("/quit");
            dispose();
            SwingUtilities.invokeLater(Client_GUI::new);
        });
    }
}


