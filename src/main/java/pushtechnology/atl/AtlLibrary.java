package pushtechnology.atl;

import javax.management.MBeanServer;
import java.rmi.registry.LocateRegistry;
import java.lang.management.ManagementFactory;

import javax.management.remote.*;

public class AtlLibrary {

    private MBeanServer mbs = ManagementFactory.getPlatformMBeanServer();
    private String              urlStub;
    private JMXServiceURL       jmxUrl;
    private JMXConnectorServer  jmxServer;

    public boolean initialize(String server, int rmiPort) {

        try {
            LocateRegistry.createRegistry(rmiPort);
            urlStub = "service:jmx:rmi:///jndi/rmi://" + server + ":" + rmiPort + "/jmxrmi";
            jmxUrl = new JMXServiceURL(urlStub);
            jmxServer =  JMXConnectorServerFactory.newJMXConnectorServer(jmxUrl, null, mbs);
            jmxServer.start();
            return true;
        }
        catch(Exception e) {
            System.out.println("**Error**\nUnable to create Registry.\n\n" + e.getMessage());
            return false;
        }
    }

    /* Accessor for MBS */
    public MBeanServer getMbs () {
        return mbs;
    }

    /* Accessor for URL Stub */
    public String getUrlStub () {
        return urlStub;
    }
}
