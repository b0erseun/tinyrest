
import com.tinyrest.core.server.servlets.ServerActivityMonitor;
import com.tinyrest.core.server.servlets.ServerActivityMonitorImpl;
import com.tinyrest.core.server.servlets.ServerMonitorException;

/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Sam
 */
public class TestClass {
    
    public static void main(String[] args) throws ServerMonitorException {
        ServerActivityMonitor sam = new ServerActivityMonitorImpl(2);
        
        sam.startCall(null);
        
        sam.endCall(null);
        
        sam.startCall(null);
        sam.startCall(null);
        sam.startCall(null);
        sam.startCall(null);

        sam.endCall(null);
        
        sam.startCall(null);
    }
    
}
