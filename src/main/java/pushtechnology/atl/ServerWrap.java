package pushtechnology.atl;

import com.pushtechnology.diffusion.api.APIException;
import com.pushtechnology.diffusion.api.config.ConfigException;
import com.pushtechnology.diffusion.api.config.ConnectorConfig;
import com.pushtechnology.diffusion.api.server.DiffusionServer;
import com.pushtechnology.diffusion.server.classloader.ApplicationClassLoader;
import com.pushtechnology.repackaged.picocontainer.PicoLifecycleException;
import org.apache.commons.cli.*;
import org.apache.commons.lang3.ObjectUtils;

import javax.management.MBeanServer;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.util.*;
import javax.management.*;
import javax.management.remote.*;
import java.util.concurrent.TimeUnit;

public class ServerWrap implements ServerWrapMBean {

    //Instance variables
    private DiffusionServer server;
    private final String connectorToKeep = "High Volume Connector";

    private int listenPort      = 0;
    private int killPort        = 0;
    private String serverName   = "";
    private int rmiPort         = 0;
    private String jmxURL       = "";

    public static void main(String[] args) throws Exception {

        try {
            ServerWrap instance = new ServerWrap();
            instance.run(args);
        }
        catch(Exception e) {
            System.out.println("ServerWrap: Unable to run ServerWrap instance");
            e.printStackTrace();
        }
    }

    //******************************************************************
    /*
        Run - to run the instance
     */

    public void run(String[] args) throws Exception {
        System.out.println("ServerWrap.run():  ServerName=\"" + serverName + "\".   Processing options");
        if(processCommandLine(args)) {
            System.out.println("ServerWrap.run(): Processed commandline.   Ready to create the server... ServerName="+serverName);
        }
        else{
            System.out.println("ServerWrap.run(): Error processing commandline.");
        }
        System.out.println("ServerWrap.run():  ServerName=\"" + serverName + "\" with hash " +  this.hashCode());
        ObjectName name = new ObjectName("pushtechnology.atl:type=ServerWrap,name="+serverName);

        System.out.println("ServerWrap.run(): Attempting to connect to - " + jmxURL);
        JMXServiceURL url = new JMXServiceURL(jmxURL);
        JMXConnector  jmxc = JMXConnectorFactory.connect(url,  null);

        //Create MBeans
        final MBeanServerConnection mbsc = jmxc.getMBeanServerConnection();

        System.out.println("ServerWrap.run(): Creating MBean...");
        //printMBeans(mbsc);
        try {
            mbsc.createMBean(ServerWrap.class.getName(), ObjectName.getInstance("pushtechnology.atl:type=ServerWrap,name="+serverName));
            System.out.println("ServerWrap.run(): MBean has been created!");
        }
        catch(InstanceAlreadyExistsException exists) {
            System.out.println("ServerWrap.run(): No need to recreate MBean - already exists!");
        }
    }

    /*
    Print all MBeans on a connection
     */
    private void printMBeans(MBeanServerConnection mbsc) {
        // Print MBeans
        try {
            Set<ObjectInstance> instances = mbsc.queryMBeans(null, null);
            Iterator<ObjectInstance> iterator = instances.iterator();

            while (iterator.hasNext()) {
                ObjectInstance instance = iterator.next();
                System.out.println("Class Name:\t" + instance.getClassName());
                System.out.println("Object Name:\t" + instance.getObjectName());
                System.out.println("****************************************");
            }
        }catch(IOException ioex) {
            System.out.println("ServerWrap:  Encountered a problem - " + ioex.getMessage());
        }
    }
    //******************************************************************
    /*
    Process CommandLine arguments
     */

    private boolean processCommandLine(String[] args) {
        Options options = new Options();
        Option optListenPort = Option.builder("listenPort")
                .argName("port" )
                .hasArg(true)
                .desc("Port for connector to listen on" )
                .build();

        Option optKillPort = Option.builder("killPort")
                .argName("port" )
                .hasArg(true)
                .desc("Port to listen on to kill the server" )
                .build();

        Option optServerName = Option.builder("serverName")
                .argName("name" )
                .hasArg(true)
                .desc("Name for the new server" )
                .build();

        Option optRmiPort = Option.builder("rmiPort")
                .argName("rmi" )
                .hasArg(true)
                .desc("RMI port for IPC" )
                .build();

        Option optJmxURL = Option.builder("jmxUrl")
                .argName("jmx" )
                .hasArg(true)
                .desc("JMX URL for IPC" )
                .build();

        options.addOption(optListenPort);
        options.addOption(optKillPort);
        options.addOption(optServerName);
        options.addOption(optRmiPort);
        options.addOption(optJmxURL);

        CommandLineParser parser = new GnuParser();
        CommandLine cmd = null;
        boolean retval = false;

        try {
            cmd = parser.parse(options, args);
        }
        catch(Exception ex) {
            System.out.println("ServerWrap.processCommandLine(): Exception parsing command line - " + ex.getLocalizedMessage());;
            return retval;
        }
        // have the right arguments been passed?
        if(cmd.hasOption("listenPort")) {
            listenPort = Integer.parseInt(cmd.getOptionValue( "listenPort" ));
        }
        if(cmd.hasOption("killPort")) {
            killPort = Integer.parseInt(cmd.getOptionValue( "killPort" ));
        }
        if(cmd.hasOption("serverName")) {
            serverName = cmd.getOptionValue( "serverName" );
        }
        if(cmd.hasOption("rmiPort")) {
            rmiPort = Integer.parseInt(cmd.getOptionValue( "rmiPort" ));
        }
        if(cmd.hasOption("jmxUrl")) {
            jmxURL = cmd.getOptionValue( "jmxUrl" );
        }

        //If all 3 passed, then return true;
        if(listenPort == 0 || killPort == 0 || serverName.equals("") || rmiPort == 0 || jmxURL.equals(""))
        {
            System.out.println("** Usage Error **");
            HelpFormatter formatter = new HelpFormatter();
            formatter.printHelp("ant", options);
        }
        else {
            retval = true;
        }
        return retval;
    }

    //******************************************************************
    /*
    Create the server
     */
    public void create(String sServerName, Integer iListenPort, Integer iKillPort) {
        serverName = sServerName;
        listenPort = iListenPort;
        killPort  = iKillPort;

        System.out.println("ServerWrap.create():  ServerName=\"" + serverName + "\"");
        try {
            System.out.println("ServerWrap.create(): Creating new server..." +  this.hashCode());
            server = new DiffusionServer();
            System.out.println("ServerWrap.create():Server created - " + server.toString() + "| now setting serverName - \"" + serverName + "\"... and a port of " + listenPort);
            server.getConfig().setServerName(serverName);
            System.out.println("ServerWrap.create(): Trimming connector list to 1 per server");
            trimConnectors(listenPort);
        }
        catch(ConfigException cfgEx) {
            System.out.print("ServerWrap.create():ConfigException: Error on creating a server - ");
            System.out.println(cfgEx.getLocalizedMessage());
        }
        catch(NullPointerException npe) {
            System.out.print("ServerWrap.create():NullPointerException: Error on creating a server - ");
            System.out.println(npe.getLocalizedMessage());
        }
        catch(Exception catchallException) {
            System.out.print("ServerWrap.create():Exception: Error on creating a server - ");
            System.out.println(catchallException.getLocalizedMessage());
        }
    }

    //******************************************************************
    /*
    Start the server
     */
    public void start() {
        System.out.println("ServerWrap.start():  ServerName=\"" + serverName + "\": " + this.hashCode());
        try {
            if(server == null) {
                System.out.println("ServerWrap.start():  Server was never created.  Sorry, cannot continue.");
                System.exit(1);
            }
            else
            if(!server.isStarted()) {
                System.out.println("ServerWrap.start():  Attempting to start server... Statistics = " + server.getConfig().getStatistics().getClientStatistics().toString());
                server.start();
            }
            else {
                System.out.println("ServerWrap.start():  Server is already started!");
            }
        }
        catch(APIException apiException) {
            System.out.println("ServerWrap.start(): Unable to start server - " + apiException.getLocalizedMessage());
        }
        catch(NullPointerException npe) {
            System.out.println("ServerWrap.start():  Sorry - server couldn't be started!");
            //System.exit(0);
        }
        catch(PicoLifecycleException pico) {
            System.out.println("ServerWrap.start():  Sorry - server couldn't be started! Pico exception - " + pico.getMessage());
            //System.exit(0);
        }
        catch(IllegalArgumentException ill) {
            System.out.println("ServerWrap.start():  Sorry - server couldn't be started!! IllegalArg exception - " + ill.getMessage());
            //System.exit(0);
        }
        catch(IllegalStateException illstate) {
            System.out.println("ServerWrap.start():  Sorry - server couldn't be started!! Illegal State Exception - " + illstate.getLocalizedMessage());
            //System.exit(0);
        }
    }

    //******************************************************************
    /*
    Stop the server
     */
    public void stop() {
        try {
            System.out.println("I will now stop server - " + serverName + ":" + this.hashCode() + ".");
            if(!server.isStopped()) {
                server.stop();
            }
        }
        catch(Exception ex) {
        System.out.println("ServerWrap.stop(): Unable to stop server - " + ex.getLocalizedMessage());
        }
    }

    //******************************************************************
    /*
    Get the server name
    */
    public String getName() {
        return server.getConfig().getServerName();
    }

    //******************************************************************
    /*
    Trim Connectors - No need for 2 connectors - keep 1 (that named in variable connectorToKeep)
    */
    private void trimConnectors(int port) {

        for (int i=0; i<server.getConfig().getConnectors().size();i++)
        {
            ConnectorConfig cfg = server.getConfig().getConnectors().get(i);
            if (cfg.getName().equals(connectorToKeep)) {
                try {
                    //System.out.println("ServerWrap.trimConnectors(): Setting port to be " + port + " on server - " + cfg.getName());
                    cfg.setPort(port);
                }
                catch(ConfigException configException) {
                    System.out.println("ServerWrap.trimConnectors(): Couldn't set port " + port + " on server - " + configException.getLocalizedMessage());
                }
                catch(NoSuchElementException nosuch) {
                    System.out.println("ServerWrap.trimConnectors(): Unable to set port - " + nosuch.getMessage());
                }
            }
            else {
                try {
                    //System.out.println("ServerWrap.trimConnectors(): Disabling Connector " + cfg.getName() + " (required=false)");
                    server.getConfig().getConnector(cfg.getName()).setRequired(false);
                }
                catch(Exception e) {
                    System.out.println("Error setting setRequired(false) on " + cfg.getName() + ": " + e.getLocalizedMessage());
                }
            }
        }
    }
}
