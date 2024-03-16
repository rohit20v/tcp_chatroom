package org.chatroom.Client;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;


public class Client_GUI extends JFrame {
    private String host = "localhost";
    private int port = 5555;
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
    private BufferedReader reader;
    private PrintWriter writer;
    private final Socket socket;
    private String username;
    private String groups;

    public Client_GUI() {
        setContentPane(Form);
        Form.setBorder(new EmptyBorder(10, 10, 10, 10));
        Form.setBackground(Color.decode("#0F1035"));
        chatOptions.setBorder(new EmptyBorder(20, 10, 10, 10));
        chatArea.setBorder(new EmptyBorder(10, 10, 10, 10));
//        msgArea.setBackground(Color.decode("#FFFFF"));
        msgArea.setEditable(false);
        msgTxt.setEnabled(false);
        sendBtn.setEnabled(false);
        usernameBtn.setEnabled(false);

        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(888, 690);
        setVisible(true);

        setLocationRelativeTo(null);

        this.socket = createSocket();
        if (socket != null && socket.isConnected()) {
            createReader_Writer();

            startCreatingGroup();
            startJoiningGroup();

            createGroup();
            joinGroup();

            showGroups();

            setUsername();

            sendMsg();

            readMsg();

            leaveChat();

        }

    }

    public Socket createSocket() {
        Socket clientSocket = null;
        try {
            clientSocket = new Socket("localhost", 5555);
            this.statusLbl.setForeground(Color.GREEN);
            this.statusLbl.setText("Connected");

        } catch (Exception e) {
            this.statusLbl.setForeground(Color.red);
            this.statusLbl.setText("Server is down");
        }
        return clientSocket;
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
                        if (receivedMessage.endsWith("jpg") || receivedMessage.endsWith("png")) {
                            System.out.println("Got " + receivedMessage);
                            getImage(receivedMessage);
                        } else msgArea.append(receivedMessage + "\n");
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

    public void startCreatingGroup() {
        askCreateBtn.addActionListener(e -> {
            writer.println("1");
            askJoinBtn.setEnabled(false);
            askCreateBtn.setEnabled(false);
            createGrpNameTxt.setEnabled(true);
            createGrpCodeTxt.setEnabled(true);
            createBtn.setEnabled(true);
        });
    }

    public void createGroup() {
        createBtn.addActionListener(e -> {
            new Thread(() -> {
                if (createBtn.isEnabled()) {
                    if (!createGrpCodeTxt.getText().isEmpty() && !createGrpNameTxt.getText().isEmpty()) {
                        this.writer.println(createGrpNameTxt.getText());
                        this.writer.println(createGrpCodeTxt.getText());
                        createGrpNameTxt.setText("");
                        createGrpCodeTxt.setText("");
                        createGrpNameTxt.setEnabled(false);
                        createGrpCodeTxt.setEnabled(false);
                        createBtn.setEnabled(false);
                        JOptionPane.showMessageDialog(this, "Enter username");
                        usernameBtn.setEnabled(true);


                    }
                }
            }).start();
        });
    }

    public void startJoiningGroup() {
        askJoinBtn.addActionListener(e -> {
            this.writer.println("2");
            askCreateBtn.setEnabled(false);
            askJoinBtn.setEnabled(false);
            joinGrpNameTxt.setEnabled(true);
            joinGrpCodeTxt.setEnabled(true);
            joinBtn.setEnabled(true);
        });
    }

    public void joinGroup() {
        joinBtn.addActionListener(e -> {
            new Thread(() -> {
                if (joinBtn.isEnabled()) {
                    if (!joinGrpCodeTxt.getText().isEmpty() && !joinGrpNameTxt.getText().isEmpty()) {
                        this.writer.println(joinGrpNameTxt.getText());
                        this.writer.println(joinGrpCodeTxt.getText());
                        joinGrpNameTxt.setText("");
                        joinGrpCodeTxt.setText("");
                        joinGrpNameTxt.setEnabled(false);
                        joinGrpCodeTxt.setEnabled(false);
                        joinBtn.setEnabled(false);
                        usernameBtn.setEnabled(true);
                    }
                }
            }).start();
        });
    }


    public void showGroups() {
        showGrpsBtn.addActionListener(e -> {
            this.writer.println("3");
            new Thread(() -> {
                try {
                    groups = this.reader.readLine();
                    JOptionPane.showMessageDialog(null, groups);
                } catch (Exception ex) {
                    System.err.println(ex.getMessage() + "\nLine: 175 ");
                }
            }).start();
        });
    }

    public void leaveChat() {
        //TODO: broadcastToGroup(groupName, nickname + " ha lasciato il gruppo");
        leaveBtn.addActionListener(e -> {
            this.writer.println("/quit");
            dispose();
            SwingUtilities.invokeLater(Client_GUI::new);
        });
    }
}


