package org.chatroom.Client;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.image.BufferedImage;
import java.io.*;
import java.net.Socket;
import java.net.URL;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;

public class Client_GUI extends JFrame {
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
    private JTextField grpNameTxt;
    private JButton askCreateBtn;
    private JButton askJoinBtn;
    private JButton createBtn;
    private JButton joinBtn;
    private JTextField grpCodeTxt;
    private JButton showGrpsBtn;
    private JLabel gc_name;
    private JRadioButton pvtBtn;
    private JTextField renameTxt;
    private JButton renameBtn;
    private JScrollPane scrollPane;
    private BufferedReader reader;
    private PrintWriter writer;
    private Socket socket;
    private String username;
    private AtomicReference<String> receivedMessage = new AtomicReference<>("");

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
            pvtBtn.setEnabled(false);

            setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
            setSize(888, 690);
            setVisible(true);
            setLocationRelativeTo(null);

            startCreatingGroup();
            startJoiningGroup();

            showGroups();

            setUsername();

            sendMsg();

            leaveChat();

            renameBtn();
        });

        new Thread(this::createSocket).start();

    }

    private void createSocket() {
        do {
            try {
                this.socket = new Socket("localhost", 5555);
                System.out.println("Server up");
                updateStatus(Color.decode("#3a86ff"), "Connected");

                createReader_Writer();
                readMsg();
                break;
            } catch (Exception e) {
                System.out.println("Server down");
                updateStatus(Color.red, "Server is down");
                shutdownClient();
                try {
                    Thread.sleep(2000);
                } catch (InterruptedException ex) {
                    //ignore
                }
            }
        } while (true);
    }

    private void shutdownClient() {
        try {
            if (this.socket != null) {
                socket.close();
                reader.close();
                writer.close();
            }
        } catch (Exception e) {
            System.out.println("Server is closed");
        }
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
                updateStatus(Color.decode("#3a86ff"), "Connected");
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

                if (username.length() > 16){
                    JOptionPane.showMessageDialog(null, "Username too long", "Invalid Username", JOptionPane.ERROR_MESSAGE);
                }
                else if(username.contains("/")){
                    JOptionPane.showMessageDialog(null, "Username mustn't contain '/'", "Invalid Username", JOptionPane.ERROR_MESSAGE);
                }
                else {
                    System.out.println(username);
                    writer.println(username.toUpperCase());
                    this.msgTxt.setEnabled(true);
                    this.sendBtn.setEnabled(true);
                    this.usernameBtn.setEnabled(false);
                    this.usernameTxt.setEnabled(false);
                    this.renameBtn.setEnabled(true);
                    this.renameTxt.setEnabled(true);
                    this.leaveBtn.setEnabled(true);
                }
            }
        });
    }

    private void sendMsg() {
        sendBtn.addActionListener(e -> {
            String msgToSend = msgTxt.getText().trim();
            if (!msgToSend.isEmpty()) {
                if (msgToSend.startsWith("/cat")) {
                    try {
                        ImageIcon icon = fetchCat();
                        ImageIcon resizedIcon = new ImageIcon(Objects.requireNonNull(icon).getImage().getScaledInstance(120, 120, Image.SCALE_SMOOTH));
                        JOptionPane.showMessageDialog(null, resizedIcon, "", JOptionPane.PLAIN_MESSAGE);
                    } catch (Exception ex) {
                        JOptionPane.showMessageDialog(null, "Error fetching data from the CATAAS API", "API Error", JOptionPane.ERROR_MESSAGE);

                    }
                } else {
                    writer.println(msgToSend);
                }
            }
            msgTxt.setText("");
        });
    }

    private void readMsg() {
        try {
            while (true) {
                receivedMessage.set(reader.readLine());
                if (receivedMessage.get() != null && !receivedMessage.get().isEmpty()) {
                    if (!receivedMessage.get().endsWith("momento."))
                        msgArea.append(receivedMessage.get() + "\n");
                    else
                        JOptionPane.showMessageDialog(this, receivedMessage, "Groups", JOptionPane.ERROR_MESSAGE);
                }
            }
        } catch (Exception e) {
            updateStatus(Color.red, "Server is down");
        }
    }

    private void startCreatingGroup() {
        askCreateBtn.addActionListener(e -> {
            if (this.writer == null) {
                JOptionPane.showMessageDialog(this, "Server is offline", "", JOptionPane.ERROR_MESSAGE);
            } else {
                writer.println("1");
                askJoinBtn.setEnabled(false);
                askCreateBtn.setEnabled(false);
                grpNameTxt.setEnabled(true);
                grpCodeTxt.setEnabled(true);
                createBtn.setEnabled(true);
                showGrpsBtn.setEnabled(false);
                pvtBtn.setEnabled(true);
                validateGroup();
            }
        });
    }


    private void validateGroup() {
        createBtn.addActionListener(e -> {
            if (createBtn.isEnabled()) {
                if (!grpCodeTxt.getText().isEmpty() && !grpNameTxt.getText().isEmpty()) {
                    if (grpCodeTxt.getText().length() > 10 || grpNameTxt.getText().length() > 10)
                        JOptionPane.showMessageDialog(null, "Group name and code mustn't be that long", "", JOptionPane.ERROR_MESSAGE);
                    else {
                        createGroup();
                    }
                }
            }
        });
    }

    private void createGroup() {
        this.writer.println(grpNameTxt.getText());
        this.writer.println(grpCodeTxt.getText());
        this.writer.println(pvtBtn.isSelected());
        groupName.setText(grpNameTxt.getText());
        grpNameTxt.setText("");
        grpCodeTxt.setText("");
        new Thread(() -> {
            if (!receivedMessage.get().endsWith("LEAVE")) {
                grpNameTxt.setEnabled(false);
                grpCodeTxt.setEnabled(false);
                createBtn.setEnabled(false);
                pvtBtn.setEnabled(false);
                JOptionPane.showMessageDialog(this, "Enter username", "", JOptionPane.INFORMATION_MESSAGE);
                usernameBtn.setEnabled(true);
                leaveBtn.setEnabled(false);
                usernameTxt.setEnabled(true);
            } else {
                createBtn.setEnabled(false);
                pvtBtn.setEnabled(false);
                leaveBtn.setEnabled(true);
            }
        }).start();
    }

    private void startJoiningGroup() {
        askJoinBtn.addActionListener(e -> {
            if (this.writer == null) {
                JOptionPane.showMessageDialog(this, "Server is offline", "", JOptionPane.ERROR_MESSAGE);
            } else {
                this.writer.println("2");
                new Thread(() -> {
                    String msg;
                    msg = receivedMessage.get();
                    if (!msg.equals("Non ci sono gruppi disponibili. Devi crearne uno nuovo.")) {
                        askCreateBtn.setEnabled(false);
                        askJoinBtn.setEnabled(false);
                        grpNameTxt.setEnabled(true);
                        grpCodeTxt.setEnabled(true);
                        joinBtn.setEnabled(true);
                        receivedMessage.set("");
                        pvtBtn.setEnabled(false);

                        joinGroup();
                        showGrpsBtn.setEnabled(false);
                    }
                }).start();
            }
        });
    }

    private void joinGroup() {
        joinBtn.addActionListener(e -> {
            if (!grpCodeTxt.getText().isEmpty() && !grpNameTxt.getText().isEmpty()) {
                this.writer.println(grpNameTxt.getText());
                this.writer.println(grpCodeTxt.getText());
                new Thread(this::verifyJoining).start();
            }
        });
    }

    private void verifyJoining() {
        if (!receivedMessage.get().endsWith("LEAVE!")) {
            groupName.setText(grpNameTxt.getText());
            grpNameTxt.setText("");
            grpCodeTxt.setText("");
            grpNameTxt.setEnabled(false);
            grpCodeTxt.setEnabled(false);
            joinBtn.setEnabled(false);
            usernameBtn.setEnabled(true);
            leaveBtn.setEnabled(false);
            usernameTxt.setEnabled(true);
        } else {
            joinBtn.setEnabled(false);
            usernameBtn.setEnabled(false);
            leaveBtn.setEnabled(true);
            usernameTxt.setEnabled(false);
        }
    }

    private void showGroups() {
        showGrpsBtn.addActionListener(e -> {
            if (this.writer != null) this.writer.println("3");
            else JOptionPane.showMessageDialog(this, "Server is offline", "Groups", JOptionPane.ERROR_MESSAGE);
        });
    }

    private void renameBtn() {
        renameBtn.addActionListener(e -> {
            String newName = renameTxt.getText().trim();
            if (!newName.isEmpty()) {
                writer.println("/nome " + newName);
            }
            renameTxt.setText("");
        });
    }

    private void leaveChat() {
        leaveBtn.addActionListener(e -> {
            this.writer.println("/quit");
            dispose();
            SwingUtilities.invokeLater(Client_GUI::new);
        });
    }

    private void buttonHoverFx() {
        askCreateBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (askCreateBtn.isEnabled()) {
                    super.mouseEntered(e);
                    askCreateBtn.setBackground(Color.decode("#184e77"));
                    askCreateBtn.setForeground(Color.decode("#72EFDD"));
                }

            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (askCreateBtn.isEnabled()) {
                    super.mouseEntered(e);
                    askCreateBtn.setBackground(Color.decode("#72EFDD"));
                    askCreateBtn.setForeground(Color.decode("#184e77"));
                }
            }
        });
        askJoinBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (askJoinBtn.isEnabled()) {
                    super.mouseEntered(e);
                    askJoinBtn.setBackground(Color.decode("#184e77"));
                    askJoinBtn.setForeground(Color.decode("#72EFDD"));
                }

            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (askJoinBtn.isEnabled()) {
                    super.mouseEntered(e);
                    askJoinBtn.setBackground(Color.decode("#72EFDD"));
                    askJoinBtn.setForeground(Color.decode("#184e77"));
                }
            }
        });
        leaveBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (leaveBtn.isEnabled()) {
                    super.mouseEntered(e);
                    leaveBtn.setBackground(Color.decode("#ef476f"));
                    leaveBtn.setForeground(Color.decode("#edf2fb"));
                }
            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (leaveBtn.isEnabled()) {
                    super.mouseExited(e);
                    leaveBtn.setBackground(Color.decode("#DCF2F1"));
                    leaveBtn.setForeground(Color.decode("#ef476f"));
                }
            }
        });
        createBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (createBtn.isEnabled()) {
                    super.mouseEntered(e);
                    createBtn.setBackground(Color.decode("#0077b6"));
                    createBtn.setForeground(Color.white);
                }

            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (createBtn.isEnabled()) {
                    super.mouseEntered(e);
                    createBtn.setBackground(Color.decode("#90E0EF"));
                    createBtn.setForeground(Color.decode("#0077b6"));
                }
            }
        });
        joinBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (joinBtn.isEnabled()) {
                    super.mouseEntered(e);
                    joinBtn.setBackground(Color.decode("#0077b6"));
                    joinBtn.setForeground(Color.white);
                }

            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (joinBtn.isEnabled()) {
                    super.mouseEntered(e);
                    joinBtn.setBackground(Color.decode("#90E0EF"));
                    joinBtn.setForeground(Color.decode("#0077b6"));
                }
            }
        });
        usernameBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (usernameBtn.isEnabled()) {
                    super.mouseEntered(e);
                    usernameBtn.setBackground(Color.decode("#0077b6"));
                    usernameBtn.setForeground(Color.white);
                }

            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (usernameBtn.isEnabled()) {
                    super.mouseEntered(e);
                    usernameBtn.setBackground(Color.decode("#90E0EF"));
                    usernameBtn.setForeground(Color.decode("#0077b6"));
                }
            }
        });
        renameBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (renameBtn.isEnabled()) {
                    super.mouseEntered(e);
                    renameBtn.setBackground(Color.decode("#0077b6"));
                    renameBtn.setForeground(Color.white);
                }

            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (renameBtn.isEnabled()) {
                    super.mouseEntered(e);
                    renameBtn.setBackground(Color.decode("#90E0EF"));
                    renameBtn.setForeground(Color.decode("#0077b6"));
                }
            }
        });
        sendBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (sendBtn.isEnabled()) {
                    super.mouseEntered(e);
                    sendBtn.setBackground(Color.decode("#70e000"));
                    sendBtn.setForeground(Color.white);
                }

            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (sendBtn.isEnabled()) {
                    super.mouseEntered(e);
                    sendBtn.setBackground(Color.decode("#57cc99"));
                    sendBtn.setForeground(Color.black);
                }
            }
        });
        showGrpsBtn.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseEntered(MouseEvent e) {
                if (showGrpsBtn.isEnabled()) {
                    super.mouseEntered(e);
                    showGrpsBtn.setBackground(Color.decode("#caffbf"));
                    showGrpsBtn.setForeground(Color.black);
                }

            }

            @Override
            public void mouseExited(MouseEvent e) {
                if (showGrpsBtn.isEnabled()) {
                    super.mouseEntered(e);
                    showGrpsBtn.setBackground(Color.decode("#2B2D42"));
                    showGrpsBtn.setForeground(Color.white);
                }
            }
        });
    }

    private void swingStyle() {
        Form.setBorder(new EmptyBorder(10, 10, 10, 10));
        Form.setBackground(Color.decode("#0F1035"));

        chatOptions.setBorder(new EmptyBorder(20, 10, 10, 10));
        chatArea.setBorder(new EmptyBorder(10, 10, 10, 10));


        msgArea.setLineWrap(true);
        msgArea.setBorder(new EmptyBorder(5, 5, 5, 5));

        setRoundedCornerUI(sendBtn, Color.decode("#57cc99"));
        setRoundedCornerUI(askCreateBtn, Color.cyan);
        setRoundedCornerUI(askJoinBtn, Color.cyan);
        setRoundedCornerUI(createBtn, Color.blue);
        setRoundedCornerUI(joinBtn, Color.blue);
        setRoundedCornerUI(usernameBtn, Color.blue);
        setRoundedCornerUI(renameBtn, Color.blue);
        setRoundedCornerUI(leaveBtn, Color.red);
        setRoundedCornerUI(chatOptions, Color.red);

        renderLogo();
        buttonHoverFx();
    }

    private void renderLogo() {
        BufferedImage img = loadImage("src/main/resources/mt_room.png");
        BufferedImage resizedImg = resizeImage(Objects.requireNonNull(img), 300, 150);
        displayImage(resizedImg);
    }

    public static BufferedImage loadImage(String filePath) {
        try {
            return ImageIO.read(new java.io.File(filePath));
        } catch (Exception e) {
            System.out.println("Image not found: ");
            return null;
        }
    }

    public static BufferedImage resizeImage(BufferedImage originalImage, int width, int height) {
        BufferedImage resizedImage = new BufferedImage(width, height, originalImage.getType());
        Graphics2D g = resizedImage.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BICUBIC);
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();
        return resizedImage;
    }

    public void displayImage(BufferedImage image) {
        ImageIcon icon = new ImageIcon(image);
        gc_name.setIcon(icon);
    }

    private static ImageIcon fetchCat() {
        try {
            URL imageUrl = new URL("https://cataas.com/cat");
            Image image = ImageIO.read(imageUrl);
            return new ImageIcon(image);
        } catch (IOException e) {
            System.out.println("Error fetching image");
            return null;
        }
    }

    private static void setRoundedCornerUI(JComponent component, Color c) {
        component.setBorder(BorderFactory.createEmptyBorder(10, 15, 10, 15));
        component.setBorder(BorderFactory.createCompoundBorder(
                BorderFactory.createLineBorder(c, 0),
                new RoundedBorder(8)));
    }
}


