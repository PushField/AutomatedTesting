package pushtechnology.atl;

import java.lang.management.ManagementFactory;
import java.util.ArrayList;
import java.util.Scanner;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.io.IOException;

import com.pushtechnology.diffusion.api.APIException;
import com.pushtechnology.diffusion.api.config.ConnectorConfig;
import com.pushtechnology.diffusion.api.server.DiffusionServer;

import javax.management.*;
import javax.management.remote.*;

/**
 * Test App for creating AtlServers.
 * AtlServers in turn create a DiffusionServer within its own JVM.
 *
 * First create the servers, store in array, wait 20secs.
 * Then start the servers, wait 20secs,
 * Then stop the servers.
 */

public class App
{
    public static void main( String[] args ) throws java.lang.InterruptedException {

        int rmiPort = 1001;
        int delay   = 10;
        System.out.println("Attempting to initialise JMX Server...");
        promptEnterKey();
        AtlLibrary atl = new AtlLibrary();

        if(atl.initialize("localhost", rmiPort)) {

            ArrayList<AtlServer> serverList = new ArrayList<AtlServer>();

            try {

                //Create the servers
                for(int i=0; i<2; i++) {

                    String serverName = "DevOpsServer_"+(i+1);
                    int iListenPort = 1230+i;
                    int iKillPort   = 2340+i;
                    System.out.println("App: Creating wrapper " + serverName + "...");
                    promptEnterKey();
                    AtlServer atlServer = new AtlServer(iListenPort, iKillPort, serverName, rmiPort, atl.getUrlStub(), atl.getMbs());
                    System.out.println("App: Creating server...");
                    promptEnterKey();
                    atlServer.create();
                    serverList.add(atlServer);
                }
                //Start the servers
                System.out.println("App: Servers created.   Now waiting for " + delay + " secs before starting...");
                TimeUnit.SECONDS.sleep(delay);
                for(int i=0;i< serverList.size();i++) {
                    System.out.println("App: Starting server \"" + serverList.get(i).serverName + "\"");
                    promptEnterKey();
                    serverList.get(i).start();
                    System.out.println("App: Server \"" + serverList.get(i).serverName + "\" started.");
                }
                //Stop the servers
                TimeUnit.SECONDS.sleep(delay);
                System.out.println("TEST FINISHED - Now stopping the servers... !!! ");
                for (AtlServer server : serverList) {
                    System.out.print("Stopping " + server.serverName + "...\t");
                    promptEnterKey();
                    server.stop();
                    System.out.println("[DONE]");
                }

            } catch (IOException ioEx) {
                System.out.println("**Error** Couldn't create new Server: " + ioEx);
            }
        }
    }

    public static void promptEnterKey(){
        System.out.println("Press \"ENTER\" to continue...");
        Scanner scanner = new Scanner(System.in);
        scanner.nextLine();
    }
}