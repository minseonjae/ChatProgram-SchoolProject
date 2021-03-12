package seonjae.program.chatting.client;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.io.*;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.Date;

public class ClientMain extends JFrame {

    private static final String HOST = "localhost";
    private static final int PORT = 5757;

    private static JTextArea textArea;

    private static Socket socket;
    private static DataInputStream in = null;
    private static DataOutputStream out = null;

    public ClientMain() {
        setTitle("Client Console");

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
                        sendMessage(text.getText());
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
                    sendMessage(text.getText());
                    text.setText("");
                }
            }
        });
        add(button);
        add(text);
    }

    public static void main(String[] args) {
        new ClientMain().setVisible(true);
        consoleMessage("서버에 연결을 시작합니다.");
        try {
            socket = new Socket();
            socket.connect(new InetSocketAddress(HOST, PORT));

            in = new DataInputStream(socket.getInputStream());
            out = new DataOutputStream(socket.getOutputStream());

            consoleMessage("서버와 연결되었습니다.");
            while (socket.isConnected()) {
                String inputMessage = in.readUTF();
                consoleMessage(inputMessage);
            }
        } catch (Exception e) {
            consoleMessage("서버에 연결하던 도중 오류가 발생했습니다!");
        } finally {
            try { if (socket != null) socket.close(); } catch (Exception e1) {}
            try { if (in != null) in.close(); } catch (Exception e1) {}
            try { if (out != null) out.close(); } catch (Exception e1) {}
        }
    }

    public static void consoleMessage(Object message) {
        textArea.append(" [" + new SimpleDateFormat("HH:mm:ss").format(new Date()) + "] " + message.toString() + "\n");
        textArea.setCaretPosition(textArea.getDocument().getLength());
    }
    public static void sendMessage(Object message) {
        try {
            out.writeUTF(message.toString());
        } catch (Exception e) {
            consoleMessage("메세지를 보내는 도중 오류가 발생했습니다!");
        }
    }
}
