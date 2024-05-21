import java.rmi.registry.Registry;

import javax.swing.JOptionPane;

import java.rmi.registry.LocateRegistry;
import java.net.InetAddress;

public class SenderMain {

    public static void main(String[] args) {
        try {
             String serverId = JOptionPane.showInputDialog(null, "Enter Server ID:", "Server ID Input", JOptionPane.PLAIN_MESSAGE);
             if (serverId != null && !serverId.trim().isEmpty()) {
                 SharingImpl server = new SharingImpl(serverId);
                 Registry registry = LocateRegistry.createRegistry(1099);
                 registry.rebind("Server", server);
                 InetAddress ipAddress = InetAddress.getLocalHost();
                 String serverIpAddress = ipAddress.getHostAddress();
                 System.out.println("Server is running on IP address: " + serverIpAddress);
                 System.out.println("Server is running...");
             } else {
                 System.out.println("Server ID cannot be empty. Exiting...");
             }
         } catch (Exception e) {
             System.err.println("Server exception: " + e.toString());
             e.printStackTrace();
         }
 }
}
