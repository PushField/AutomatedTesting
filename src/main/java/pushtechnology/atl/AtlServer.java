package pushtechnology.atl;

import java.lang.management.ManagementFactory;
import java.util.*;
import java.io.IOException;
import java.io.BufferedReader;
import java.io.InputStreamReader;

import com.pushtechnology.diffusion.api.config.*;
import com.pushtechnology.diffusion.api.server.DiffusionServer;
import com.pushtechnology.diffusion.comms.connector.Connector;
import com.pushtechnology.diffusion.server.classloader.ApplicationClassLoader;
import org.apache.commons.cli.UnrecognizedOptionException;

import javax.management.*;
import java.rmi.registry.*;

import org.apache.commons.lang3.ArrayUtils;

/*
Create a new server with only 1 connector - all defaults
*/
public class AtlServer {

    String className = "pushtechnology.atl.ServerWrap";
    String separator = System.getProperty("file.separator");
    String classpath = System.getProperty("java.class.path");
    String javapath  = System.getProperty("java.home") + separator + "bin" + separator + "java";
    String serverName = "";
    String objectName = "";
    int    listenPort = 0;
    int    killPort   = 0;
    int    rmiPort    = 0;
    String jmxUrl     = "";

    MBeanServer mbs = null;
    Process process = null;

    /*
    Create a new server with only 1 connector - predefined listen port and kill port
    */
    public AtlServer(int iListenPort, int iKillPort, String sServerName, int iRmiPort, String sUrlStub, MBeanServer mMbs) throws IOException {
        try {
            serverName = sServerName;
            listenPort = iListenPort;
            killPort   = iKillPort;
            rmiPort    = iRmiPort;
            jmxUrl     = sUrlStub;
            mbs        = mMbs;

            objectName = "pushtechnology.atl:type=ServerWrap,name=" + serverName;

            System.out.println("AtlServer: Running ServerWrap as a process...");
            final ProcessBuilder pb = new ProcessBuilder(javapath, "-cp", classpath, className, "-listenPort",Integer.toString(listenPort), "-killPort",Integer.toString(killPort), "-serverName",serverName, "-rmiPort", Integer.toString(rmiPort), "-jmxUrl", jmxUrl);
            pb.environment().put("JAVA_OPTS","-Dlog4j2.formatMsgNoLookups=true"); //Mitigation for CVE-2021-44228
            pb.redirectErrorStream(true);
            process = pb.start();
            //final int processStatus = process.waitFor();
            System.out.println("AtlServer: [Done] - " + process.toString());

            String line = null;
            final BufferedReader reader =
                    new BufferedReader(new InputStreamReader (process.getInputStream()));
            while((line = reader.readLine()) != null) {
                System.out.println("Line = " + line);
            }
        }
        catch (NullPointerException npe) {
            System.out.println("**Error** Couldn't create command line - " + npe);
        }
    }

    /*
    Start the server
    */
    public void create()
    {
        try {
            ObjectName name = new ObjectName(objectName);
            //  Invoke operation
            Integer i = 0;
            Object[]  opParams = new Object[]{ serverName, listenPort, killPort };
            String[]  opSig = new String[]{ serverName.getClass().getName(), i.getClass().getName(), i.getClass().getName() };

            //System.out.println("AtlServer.create: Creating the server on name \"" + name + "\".... with Params=\"" + opParams.toString() + "\" and sig=\"" + opSig.toString() + "\" and a value of \"" + opSig[0].toString() + "\"");
            mbs.invoke(name, "create", opParams, opSig);
            //System.out.println("AtlServer.create:  Server create() has been invoked.");
        }
        catch(NullPointerException npe) {
            System.out.println("AtlServer.create: **NPE** Couldn't create MBean Objectname: " + npe.getLocalizedMessage());
        }
        catch (MalformedObjectNameException mal) {
            System.out.println("AtlServer.create: **Error** Couldn't create MBean Objectname: " + mal.getLocalizedMessage());
        }
        catch(InstanceNotFoundException instance) {
            System.out.println("AtlServer.create: **Error** Couldn't find instance: " + instance.getMessage());
        }
        catch(MBeanException mbean) {
            System.out.println("AtlServer.create: **Error** MBean threw an exception: " + mbean.getLocalizedMessage());
        }
        catch(ReflectionException reflex) {
            System.out.println("AtlServer.create: **Error** Creating server threw a Reflection exception: " + reflex.getLocalizedMessage());
        }
        catch(IllegalArgumentException illegalArg) {
            System.out.println("AtlServer.create: **Error** Creating server threw an IllegalArgument exception: " + illegalArg.getLocalizedMessage());
        }
        catch(RuntimeMBeanException runtime) {
            System.out.println("AtlServer.create: **Error** Creating server threw a RuntimeMbean exception: " + runtime.getLocalizedMessage());
            System.exit(1);
        }
    }

    /*
    Start the server
    */
    public void start()
    {
        try {
            ObjectName name = new ObjectName(objectName);
            //  Invoke operation
            System.out.println("AtlServer.start: Starting the server on name \"" + name + "\"....");
            mbs.invoke(name, "start", null, null);
            System.out.println("AtlServer.start:  Server start() has been invoked.");
        }
        catch (MalformedObjectNameException mal) {
            System.out.println("AtlServer.start: **Error** Couldn't create MBean Objectname: " + mal.getLocalizedMessage());
        }
        catch(InstanceNotFoundException instance) {
            System.out.println("AtlServer.start: **Error** Couldn't find instance: " + instance.getMessage());
        }
        catch(MBeanException mbean) {
            System.out.println("AtlServer.start: **Error** MBean threw an exception: " + mbean.getLocalizedMessage());
        }
        catch(ReflectionException reflex) {
            System.out.println("AtlServer.start: **Error** Creating server threw a Reflection exception: " + reflex.getLocalizedMessage());
        }
    }

    /*
    Stop the server
    */
    public void stop()
    {
        try {
            ObjectName name = new ObjectName(objectName);
            //  Invoke operation
            System.out.println("AtlServer.stop: Stopping the server on name \"" + name + "\"....");
            mbs.invoke(name, "stop", null, null);
            System.out.println("AtlServer.stop:  Server stop() has been invoked.");
        }
        catch (MalformedObjectNameException mal) {
            System.out.println("AtlServer.stop: **Error** Couldn't create MBean Objectname: " + mal.getLocalizedMessage());
        }
        catch(InstanceNotFoundException instance) {
            System.out.println("AtlServer.stop: **Error** Couldn't find instance: " + instance.getMessage());
        }
        catch(MBeanException mbean) {
            System.out.println("AtlServer.stop: **Error** MBean threw an exception: " + mbean.getLocalizedMessage());
        }
        catch(ReflectionException reflex) {
            System.out.println("AtlServer.stop: **Error** Creating server threw a Reflection exception: " + reflex.getLocalizedMessage());
        }
    }

    public void restart()
    {
        stop();
        start();
    }

}




