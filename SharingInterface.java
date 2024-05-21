import java.rmi.Remote;
import java.rmi.RemoteException;

public interface SharingInterface extends Remote {
    byte[] captureScreenshot() throws RemoteException;
    void receiveMousePosition(int x, int y,int z) throws RemoteException;
    void registerClient(String clientId) throws RemoteException;
   // void receiveScreenshot(byte[] imageData)  throws RemoteException;
    void sendMousePosition(int x, int y,int z) throws RemoteException;
   // void sendScreenshotToClient(String serverId) throws RemoteException;
   void receiveKeyPress(int keyCode, int eventType) throws RemoteException;
   byte[] downloadFile(String filePath) throws RemoteException;
}
