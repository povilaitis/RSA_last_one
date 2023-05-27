import javax.swing.*;
import java.awt.*;
import java.io.*;
import java.net.*;
import java.security.*;

public class Verification extends JFrame {
    private JTextArea resultArea;

    public Verification() {
        setTitle("Verification");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setLayout(new BorderLayout());

        resultArea = new JTextArea();
        resultArea.setEditable(false);
        JScrollPane scrollPane = new JScrollPane(resultArea);

        add(scrollPane, BorderLayout.CENTER);
        pack();
        setLocationRelativeTo(null);
        setSize(250, 150);

        setVisible(true);
    }

    public static void verification() throws Exception {
        Verification verification = new Verification();
        ServerSocket serverSocket = new ServerSocket(8080);
        verification.resultArea.append("Waiting for connection...\n");

        Socket socket = serverSocket.accept();
        verification.resultArea.append("Connected to server.\n");

        ObjectInputStream ois = new ObjectInputStream(socket.getInputStream());
        String text = (String) ois.readObject();
        byte[] signature = (byte[]) ois.readObject();
        PublicKey publicKey = (PublicKey) ois.readObject();
        ois.close();
        socket.close();

        Signature sig = Signature.getInstance("SHA256withRSA");
        sig.initVerify(publicKey);
        sig.update(text.getBytes());
        boolean isValid = sig.verify(signature);

        if (isValid) {
            verification.resultArea.append("The digital signature is valid. Good job :)\n");
            verification.resultArea.append("The message: " + text + "\n");
        } else {
            verification.resultArea.append("The digital signature is invalid. Please check :(\n");
        }
    }
}