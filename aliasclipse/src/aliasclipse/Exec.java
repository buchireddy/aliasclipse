/* -*-mode:java; c-basic-offset:2; indent-tabs-mode:nil -*- */
package aliasclipse;

import java.awt.Container;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.KeyGenerator;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.SecretKey;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JTextField;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelExec;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.UIKeyboardInteractive;
import com.jcraft.jsch.UserInfo;

public class Exec
{
    public static boolean executeSshCommand(String command)
    {
        try {
            JSch jsch = new JSch();
            JSch.setConfig("StrictHostKeyChecking", "no");
            jsch.addIdentity("H:\\.ssh\\id_rsa");

            Properties config = new Properties();
            config.load(new FileInputStream("H:\\.eclipseConfig"));

            String host = config.getProperty("host");
            String user = System.getProperty("user.name");

            InputStream is = new FileInputStream("H:\\.ssh\\known_hosts");
            jsch.setKnownHosts(is);
            Session session = jsch.getSession(user, host, 22);

            // username and password will be given via UserInfo interface.
            UserInfo ui = new MyUserInfo();
            session.setUserInfo(ui);
            String pass = config.getProperty("password");
//            byte[] data = encrypt(pass);

            session.setPassword(config.getProperty("password"));
            session.connect();

            Channel channel = session.openChannel("exec");
            ((ChannelExec) channel).setCommand(command);

            channel.setInputStream(null);
            ((ChannelExec) channel).setErrStream(System.err);

            InputStream in = channel.getInputStream();

            channel.connect();

            byte[] tmp = new byte[1024];
            while (true) {
                while (in.available() > 0) {
                    int i = in.read(tmp, 0, 1024);
                    if (i < 0)
                        break;
                    System.out.print(new String(tmp, 0, i));
                }
                if (channel.isClosed()) {
                    break;
                }
            }
            channel.disconnect();
            session.disconnect();
        } catch (Exception e) {
            System.out.println(e);
            return false;
        }
        
        return true;
    }

    public static class MyUserInfo implements UserInfo, UIKeyboardInteractive
    {
        public String getPassword()
        {
            return passwd;
        }

        public boolean promptYesNo(String str) {
            Object[] options = { "yes", "no" };
            int foo = JOptionPane.showOptionDialog(null, str, "Warning",
                    JOptionPane.DEFAULT_OPTION, JOptionPane.WARNING_MESSAGE,
                    null, options, options[0]);
            return foo == 0;
        }

        String passwd;
        JTextField passwordField = (JTextField) new JPasswordField(20);

        public String getPassphrase() {
            return null;
        }

        public boolean promptPassphrase(String message) {
            return true;
        }

        public boolean promptPassword(String message) {
            Object[] ob = { passwordField };
            int result = JOptionPane.showConfirmDialog(null, ob, message,
                    JOptionPane.OK_CANCEL_OPTION);
            if (result == JOptionPane.OK_OPTION) {
                passwd = passwordField.getText();
                return true;
            } else {
                return false;
            }
        }

        public void showMessage(String message) {
            JOptionPane.showMessageDialog(null, message);
        }

        final GridBagConstraints gbc = new GridBagConstraints(0, 0, 1, 1, 1, 1,
                GridBagConstraints.NORTHWEST, GridBagConstraints.NONE,
                new Insets(0, 0, 0, 0), 0, 0);
        private Container panel;

        public String[] promptKeyboardInteractive(String destination,
                String name, String instruction, String[] prompt, boolean[] echo) {
            panel = new JPanel();
            panel.setLayout(new GridBagLayout());

            gbc.weightx = 1.0;
            gbc.gridwidth = GridBagConstraints.REMAINDER;
            gbc.gridx = 0;
            panel.add(new JLabel(instruction), gbc);
            gbc.gridy++;

            gbc.gridwidth = GridBagConstraints.RELATIVE;

            JTextField[] texts = new JTextField[prompt.length];
            for (int i = 0; i < prompt.length; i++) {
                gbc.fill = GridBagConstraints.NONE;
                gbc.gridx = 0;
                gbc.weightx = 1;
                panel.add(new JLabel(prompt[i]), gbc);

                gbc.gridx = 1;
                gbc.fill = GridBagConstraints.HORIZONTAL;
                gbc.weighty = 1;
                if (echo[i]) {
                    texts[i] = new JTextField(20);
                } else {
                    texts[i] = new JPasswordField(20);
                }
                panel.add(texts[i], gbc);
                gbc.gridy++;
            }

            if (JOptionPane.showConfirmDialog(null, panel, destination + ": "
                    + name, JOptionPane.OK_CANCEL_OPTION,
                    JOptionPane.QUESTION_MESSAGE) == JOptionPane.OK_OPTION) {
                String[] response = new String[prompt.length];
                for (int i = 0; i < prompt.length; i++) {
                    response[i] = texts[i].getText();
                }
                return response;
            } else {
                return null; // cancel
            }
        }
    }

    public static byte[] encrypt(String data) {
        byte[] byteCipherText = null;
        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("RC4");
            SecretKey secretKey = keyGen.generateKey();
            Cipher aesCipher = Cipher.getInstance("RC4");
            aesCipher.init(Cipher.ENCRYPT_MODE, secretKey);

            byte[] byteDataToEncrypt = data.getBytes("UTF-8");

            byteCipherText = aesCipher.doFinal(byteDataToEncrypt);
        } catch (NoSuchAlgorithmException e2) {
            e2.printStackTrace();
        } catch (NoSuchPaddingException e1) {
            e1.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return byteCipherText;
    }

    public static String decrypt(byte[] data) {
        String strDecryptedText = null;

        try {
            KeyGenerator keyGen = KeyGenerator.getInstance("RC4");
            SecretKey secretKey = keyGen.generateKey();
            Cipher aesCipher;

            aesCipher = Cipher.getInstance("RC4");
            // aesCipher.init(Cipher.ENCRYPT_MODE,secretKey);
            aesCipher.init(Cipher.DECRYPT_MODE, secretKey,
                    aesCipher.getParameters());
            byte[] byteDecryptedText = aesCipher.doFinal(data);
            strDecryptedText = new String(byteDecryptedText, "UTF-8");
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (InvalidAlgorithmParameterException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        }

        return strDecryptedText;
    }
    
    public static void main(String[] args)
    {
        executeSshCommand("ls");
    }
}