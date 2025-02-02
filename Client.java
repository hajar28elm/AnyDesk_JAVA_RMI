import java.awt.*;
import java.awt.event.*;
import java.awt.geom.AffineTransform;
import java.awt.image.AffineTransformOp;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.rmi.Naming;
import java.rmi.NotBoundException;
import java.rmi.RemoteException;

import javax.imageio.ImageIO;
import javax.swing.*;

public class Client extends JFrame implements MouseListener,KeyListener, MouseMotionListener{

    private SharingInterface server;
    private JLabel screenshotLabel;
    private JPanel panel;

    public Client() throws RemoteException, MalformedURLException, NotBoundException {
        super("Received Screenshot");
        String serverId = JOptionPane.showInputDialog(null, "Enter Sender ID:", "Sender ID Input", JOptionPane.PLAIN_MESSAGE);

        if (serverId != null && !serverId.trim().isEmpty()) {
            String url = "rmi://localhost/Server";
            server = (SharingInterface) Naming.lookup(url);
            server.registerClient(serverId);

            byte[] screenshotData = server.captureScreenshot();
            if (screenshotData == null) {
                throw new RemoteException("Failed to capture screenshot: null data received.");
            }

            BufferedImage resizedScreenshot = receiveScreenshot(screenshotData);
            screenshotLabel = new JLabel(new ImageIcon(resizedScreenshot));

            panel = new JPanel();
            panel.add(screenshotLabel);
            panel.addMouseListener(this);

            setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
            getContentPane().add(panel);

            pack();
            setLocationRelativeTo(null);
            setVisible(true);

            // Start the thread to continuously fetch screenshots
            new Thread(() -> {
                while (true) {
                    try {
                        byte[] updatedScreenshotData = server.captureScreenshot();
                        if (updatedScreenshotData != null) {
                            BufferedImage updatedScreenshot = receiveScreenshot(updatedScreenshotData);
                            screenshotLabel.setIcon(new ImageIcon(updatedScreenshot));
                            revalidate();
                            repaint();
                        }
                        Thread.sleep(1000 / 30); // 30 frames per second
                    } catch (RemoteException | InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            // Add button to request file download
            JButton downloadButton = new JButton("Download File");
            downloadButton.addActionListener(e -> selectAndDownloadFile());
            panel.add(downloadButton);

        } else {
            System.out.println("Sender ID cannot be empty. Exiting...");
            System.exit(0); // Exit the application if no sender ID is provided
        }
    }
     private void selectAndDownloadFile() {
        JFileChooser fileChooser = new JFileChooser();
        int returnValue = fileChooser.showOpenDialog(this);
        if (returnValue == JFileChooser.APPROVE_OPTION) {
            File selectedFile = fileChooser.getSelectedFile();
            try {
                byte[] fileData = server.downloadFile(selectedFile.getAbsolutePath());
                saveFileToDisk(fileData, selectedFile.getName());
            } catch (RemoteException e) {
                JOptionPane.showMessageDialog(this, "Failed to download file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
            }
        }
    }
    private void saveFileToDisk(byte[] fileData, String fileName) {
        try (FileOutputStream fos = new FileOutputStream(new File(System.getProperty("user.home"), fileName))) {
            fos.write(fileData);
            JOptionPane.showMessageDialog(this, "File downloaded successfully.", "Success", JOptionPane.INFORMATION_MESSAGE);
        } catch (IOException e) {
            JOptionPane.showMessageDialog(this, "Failed to save file: " + e.getMessage(), "Error", JOptionPane.ERROR_MESSAGE);
        }
    }
    public BufferedImage receiveScreenshot(byte[] imageData) throws RemoteException {
        try {
            InputStream in = new ByteArrayInputStream(imageData);
            BufferedImage screenshot = ImageIO.read(in);

            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] screens = ge.getScreenDevices();
            Rectangle screenBounds = new Rectangle();
            for (GraphicsDevice screen : screens) {
                java.awt.GraphicsConfiguration config = screen.getDefaultConfiguration();
                screenBounds = screenBounds.union(config.getBounds());
            }
            int screenWidth = screenBounds.width;
            int screenHeight = screenBounds.height;

            double scaleX = (double) screenWidth / screenshot.getWidth();
            double scaleY = (double) screenHeight / screenshot.getHeight();
            double scale = Math.min(scaleX, scaleY);

            AffineTransform tx = AffineTransform.getScaleInstance(scale, scale);
            AffineTransformOp op = new AffineTransformOp(tx, AffineTransformOp.TYPE_BILINEAR);
            BufferedImage resizedScreenshot = op.filter(screenshot, null);
            return resizedScreenshot;
        } catch (IOException e) {
            throw new RemoteException("Failed to process received screenshot", e);
        }
    }

    @Override
    public void mouseClicked(MouseEvent e) {
        try {
            server.receiveMousePosition(e.getX(), e.getY(), MouseEvent.MOUSE_CLICKED);
        } catch (RemoteException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void mousePressed(MouseEvent e) {
    }

    @Override
    public void mouseReleased(MouseEvent e) {
    }

    @Override
    public void mouseEntered(MouseEvent e) {
        try {
            server.receiveMousePosition(e.getX(), e.getY(), MouseEvent.MOUSE_MOVED);
        } catch (RemoteException e1) {
            e1.printStackTrace();
        }
    }

    @Override
    public void mouseExited(MouseEvent e) {
    }
    @Override
    public void keyPressed(KeyEvent e) {
        try {
            server.receiveKeyPress(e.getKeyCode(), KeyEvent.KEY_PRESSED);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void keyReleased(KeyEvent e) {
        try {
            server.receiveKeyPress(e.getKeyCode(), KeyEvent.KEY_RELEASED);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void keyTyped(KeyEvent e) {
        // Typically, we don't need to handle keyTyped for this use case
    }

    @Override
    public void mouseDragged(MouseEvent e) {
        try {
            server.receiveMousePosition(e.getX(), e.getY(), MouseEvent.MOUSE_DRAGGED);
        } catch (RemoteException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void mouseMoved(MouseEvent e) {
        // TODO Auto-generated method stub
       // throw new UnsupportedOperationException("Unimplemented method 'mouseMoved'");
    }
}
