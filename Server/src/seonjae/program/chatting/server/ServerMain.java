package seonjae.program.chatting.server;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;

public class ServerMain extends JFrame {

    private static final String HOST = "localhost";
    private static final int PORT = 5757, MAX_USERS = 10;

    private static HashMap<String, User> users = new HashMap<String, User>();

    private static ServerSocket serverSocket = null;
    private static JTextArea textArea;

    public ServerMain() {
        setTitle("Server Console");

        setSize(600, 420);

        setResizable(false);

        setLocationRelativeTo(null);

        setDefaultCloseOperation(EXIT_ON_CLOSE);

        setLayout(null);

        JScrollPane pane1 = new JScrollPane();
        pane1.setBounds(0, 0, 595, 350);
        add(pane1);

        textArea = new JTextArea();
        textArea.setEditable(false);
        textArea.setFont(new Font("나눔고딕", Font.PLAIN, 12));
        textArea.setForeground(new Color(175, 175, 175));
        textArea.setBackground(new Color(0, 0, 0));

        pane1.setViewportView(textArea);

        JTextField text = new JTextField();
        text.setBounds(10, 358, 472, 26);
        text.setFont(new Font("나눔고딕", Font.PLAIN, 12));
        text.addKeyListener(new KeyAdapter() {
            public void keyPressed(KeyEvent e) {
                if (e.getKeyCode() == KeyEvent.VK_ENTER) {
                    if (text.getText().isEmpty()) {
                        consoleMessage("메세지를 입력하여 주세요!");
                    } else {
                        message("Server : " + text.getText());
                        text.setText("");
                    }
                }
            }
        });
        JButton button = new JButton("COMMAND");
        button.setBounds(480, 358, 100, 25);
        button.setFont(new Font("나눔고딕", Font.PLAIN, 12));
        button.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                if (text.getText().isEmpty()) {
                    consoleMessage("메세지를 입력하여 주세요!");
                } else {
                    message("Server : " + text.getText());
                    text.setText("");
                }
            }
        });
        add(button);
        add(text);
    }

    public static void main(String[] args) {
        new ServerMain().setVisible(true);
        consoleMessage("서버가 실행됩니다.");
        try {
            serverSocket = new ServerSocket(PORT, MAX_USERS, InetAddress.getByName(HOST));

            consoleMessage("서버가 실행되었습니다.");
            while (!serverSocket.isClosed()) {
                Socket userSocket = serverSocket.accept();

                User user = new User(userSocket);

                users.put(user.getNickName(), user);

                user.start();
                message(user.getNickName() + "유저가 연결했습니다.");
            }
        } catch (Exception e) {
            consoleMessage("서버에 오류가 발생했습니다!!");
        } finally {
            try { if (serverSocket != null) serverSocket.close(); } catch (Exception e) {}
        }
    }

    public static void message(Object message) {
        consoleMessage(message);
        sendMessage(message);
    }
    public static void consoleMessage(Object message) {
        textArea.append(" [" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + message.toString() + "\n");
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
    public static void sendMessage(Object message) {
        for (User user : users.values()) user.sendMessage(message);
    }


    public static class User extends Thread {

        private String nickname;
        private Socket socket;
        private static DataInputStream in = null;
        private static DataOutputStream out = null;

        public String getNickName() {
            return nickname;
        }

        public User(Socket socket) {
            this.socket = socket;
            nickname = socket.getInetAddress().getHostAddress();
            nickname = nickname.substring(0, nickname.lastIndexOf(".")) + ".***";
            try {
                in = new DataInputStream(socket.getInputStream());
                out = new DataOutputStream(socket.getOutputStream());
            } catch (Exception e) {
                try { if (socket != null) socket.close(); } catch (Exception e1) {}
                try { if (in != null) in.close(); } catch (Exception e1) {}
                try { if (out != null) out.close(); } catch (Exception e1) {}
                consoleMessage(nickname + "유저와 연결하던 도중 오류가 발생했습니다!");
            }
        }

        public void run() {
            try {
                while (!serverSocket.isClosed() && !socket.isClosed()) {
                    String inputMessage = in.readUTF();
                    message(nickname + " : " + inputMessage);
                }
            } catch (Exception e) {
                disconnect();
            }
        }

        public void sendMessage(Object message) {
            try {
                out.writeUTF(message.toString());
            } catch (Exception e) {
                disconnect();
            }
        }
        public void disconnect() {
            try {
                try { if (socket != null) socket.close(); } catch (Exception e1) {}
                try { if (in != null) in.close(); } catch (Exception e1) {}
                try { if (out != null) out.close(); } catch (Exception e1) {}
                users.remove(nickname);
                message(nickname + " : 유저가 나갔습니다.");
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
