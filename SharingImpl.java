import java.rmi.server.UnicastRemoteObject;
import javax.imageio.ImageIO;
import java.awt.event.MouseEvent;
import java.awt.AWTException;
import java.awt.Robot;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.rmi.RemoteException;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
public class SharingImpl extends UnicastRemoteObject implements SharingInterface{
 
    private Robot robot;
    private String serverId;
    BufferedImage screenshot;
    byte[] imageInByte;
    boolean isConnected = true;


    public SharingImpl(String serverId) throws RemoteException, AWTException {
        super();
        robot = new Robot();
        this.serverId = serverId;
    }

  @Override
    public byte[] captureScreenshot() throws RemoteException {
        if(isConnected){
            try {
                Toolkit toolkit = Toolkit.getDefaultToolkit();
                Rectangle screenSize = new Rectangle(toolkit.getScreenSize());
                screenshot = robot.createScreenCapture(screenSize);
                ByteArrayOutputStream baos = new ByteArrayOutputStream();
                ImageIO.write(screenshot, "png", baos);
                baos.flush();
                byte[] imageInByte = baos.toByteArray();
                baos.close();
                return imageInByte;
            } catch (IOException e) {
                throw new RemoteException("Failed to capture screenshot", e);
            }

        }else{
            System.out.println("Invalid registration attempt: Client ID does not match Server ID.");
            return imageInByte;
        }
        
   
}


@Override
public void receiveMousePosition(int x, int y, int eventType) throws RemoteException {
    robot.mouseMove(x, y);
    switch (eventType) {
        case MouseEvent.MOUSE_MOVED:
            // Handle mouse movement event
            System.out.println("Mouse moved to (" + x + ", " + y + ")");
            break;
        case MouseEvent.MOUSE_CLICKED:
            // Handle mouse click event
            System.out.println("Mouse clicked at (" + x + ", " + y + ")");
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            break;
        case MouseEvent.MOUSE_PRESSED:
            // Handle mouse press event
            System.out.println("Mouse pressed at (" + x + ", " + y + ")");
            robot.mousePress(InputEvent.BUTTON1_DOWN_MASK);
            break;
        case MouseEvent.MOUSE_RELEASED:
            // Handle mouse release event
            System.out.println("Mouse released at (" + x + ", " + y + ")");
            robot.mouseRelease(InputEvent.BUTTON1_DOWN_MASK);
            break;
        case MouseEvent.MOUSE_ENTERED:
            // Handle mouse enter event
            System.out.println("Mouse entered at (" + x + ", " + y + ")");
            robot.mouseMove(x, y);
            break;
        case MouseEvent.MOUSE_EXITED:
            // Handle mouse exit event
            System.out.println("Mouse exited at (" + x + ", " + y + ")");
            robot.mouseMove(x, y);
            break;
        default:
            // Handle other types of mouse events if necessary
            System.out.println("Unknown mouse event type: " + eventType);
            break;
    }
}

    @Override
    public void registerClient(String serverId) throws RemoteException {
        if (serverId.equals(this.serverId)) {
            isConnected = true;
        } else {
            isConnected = false;
        }
    }
   
  
  @Override
  public void sendMousePosition(int x, int y, int eventType) throws RemoteException {
      receiveMousePosition(x, y, eventType);
  }

    @Override
    public byte[] downloadFile(String filePath) throws RemoteException {
        try (FileInputStream fis = new FileInputStream(filePath);
             ByteArrayOutputStream baos = new ByteArrayOutputStream()) {
            byte[] buffer = new byte[1024];
            int bytesRead;
            while ((bytesRead = fis.read(buffer)) != -1) {
                baos.write(buffer, 0, bytesRead);
            }
            return baos.toByteArray();
        } catch (IOException e) {
            throw new RemoteException("Failed to download file", e);
        }
    }
  @Override
public void receiveKeyPress(int keyCode, int eventType) throws RemoteException {
    switch (eventType) {
        case KeyEvent.KEY_PRESSED:
            System.out.println("Key pressed: " + KeyEvent.getKeyText(keyCode));
            robot.keyPress(keyCode);
            break;
        case KeyEvent.KEY_RELEASED:
            System.out.println("Key released: " + KeyEvent.getKeyText(keyCode));
            robot.keyRelease(keyCode);
            break;
        default:
            System.out.println("Unknown key event type: " + eventType);
            break;
    }
}


}

