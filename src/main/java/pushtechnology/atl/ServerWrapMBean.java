package pushtechnology.atl;

public interface ServerWrapMBean {

    //Get the server name
    public String getName();

    //Create the server
    public void create(String sServerName, Integer iListenPort, Integer iKillPort);

    //Start the server
    public void start();

    //Stop the server
    public void stop();
}
