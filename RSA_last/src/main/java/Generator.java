import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.*;
import java.net.*;
import java.security.*;

public class Generator extends JFrame {

    private JTextArea message;

    public Generator() {
        setTitle("Message and key generator");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        JPanel inputPanel = new JPanel(new BorderLayout());
        inputPanel.setPreferredSize(new Dimension(250, 100));

        message = new JTextArea();
        message.setLineWrap(true);
        message.setWrapStyleWord(true);
        JScrollPane scrollPane = new JScrollPane(message);
        inputPanel.add(scrollPane, BorderLayout.CENTER);

        JButton sendButton = new JButton("Send");
        sendButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                sendMessage();
            }
        });

        inputPanel.add(sendButton, BorderLayout.SOUTH);
        add(inputPanel, BorderLayout.EAST);

        pack();
        setLocationRelativeTo(null);
    }

    public void sendMessage() {
        String text = message.getText();

        if (text.isEmpty()) {
            JOptionPane.showMessageDialog(this, "Please enter a message");
            return;
        }

        try {
            KeyPair keyPair = generateKeyPair();
            PublicKey publicKey = keyPair.getPublic();

            byte[] signature = signMessage(text, keyPair.getPrivate());

            Socket socket = new Socket("localhost", 8080);
            ObjectOutputStream oos = new ObjectOutputStream(socket.getOutputStream());
            oos.writeObject(text);
            oos.writeObject(signature);
            oos.writeObject(publicKey);
            oos.close();
            socket.close();

            JOptionPane.showMessageDialog(this, "Message sent, go check for verification :)");
            message.setText("");
        } catch (Exception e) {
            JOptionPane.showMessageDialog(this, "Error sending message, check code");
        }
    }

    private KeyPair generateKeyPair() throws NoSuchAlgorithmException {
        KeyPairGenerator keyGen = KeyPairGenerator.getInstance("RSA");
        keyGen.initialize(2048);
        return keyGen.generateKeyPair();
    }

    private byte[] signMessage(String text, PrivateKey privateKey) throws NoSuchAlgorithmException,
            InvalidKeyException, SignatureException {
        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initSign(privateKey);
        sig.update(text.getBytes());
        return sig.sign();
    }

    public static void main(String[] args) {
        Thread receiverThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Verification.verification();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        Thread senderThread = new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Generator sender = new Generator();
                    sender.setVisible(true);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
        });

        receiverThread.start();
        senderThread.start();
    }
}
