package testing;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.*;

import org.apache.log4j.Logger;

public class RelayTest
{
    private static final Logger LOGGER = Logger.getLogger(RelayTest.class);

    public static void main(String[] args) throws Exception
    {
        TestClusterClientBase.setupLog4J();
        Thread.currentThread().setName("host-process");
        var clusterName = "TestCluster";
        
        List<Properties> propsList = new ArrayList<>();

        final var nodeCount = 30;
        for (int i = 0; i < nodeCount; i++)
        {
            var properties = getCommonProps(clusterName, "nyc_node_" + i);
            fillSiteNYCProps(properties);
            propsList.add(properties);

            var properties2 = getCommonProps(clusterName, "lon_node_" + i);
            fillSiteLONProps(properties2);
            propsList.add(properties2);
        }

        var threads = Executors.newCachedThreadPool();
        for (Properties properties : propsList)
        {
            // without the sleep things are messed up instantly
            TimeUnit.SECONDS.sleep(1);
            threads.submit(() -> {
                try
                {
                    new TestClusterClientBase().performWithProps(properties);
                }
                catch(Throwable e)
                {
                    LOGGER.error("Throwable from node '" + properties.getProperty(TestClusterClientBase.PROP_INFINISPAN_NODE_NAME) + "': " + e.getMessage(), e);
                }
            });
        }
    }

    private static Properties getCommonProps(String pClusterName, String pNodeName)
    {
        Properties properties = new Properties();
        properties.setProperty("infinispan.cluster.name", pClusterName);
        properties.setProperty(TestClusterClientBase.PROP_INFINISPAN_NODE_NAME, pNodeName);
        properties.setProperty("jgroups.bind.address", "localhost");
        properties.setProperty(TestClusterClientBase.PROP_INFINISPAN_CONFIG_BASE, "conf/infinispan.default.crosssite.xml");
        // see infinispan.transports.default.xml
        properties.setProperty("infinispan.transport.stack.name", "udp_multicast");

        return properties;
    }

    private static void fillSiteNYCProps(Properties pProperties)
    {
        pProperties.setProperty("jgroups.site.name", "NYC");
        pProperties.setProperty("jgroups.bus.bind.address", "localhost");
        pProperties.setProperty("jgroups.bus.bind.port", "11111");
        pProperties.setProperty("jgroups.bus.tcpping.initial_hosts", "localhost[11112]");
        pProperties.setProperty("jgroups.mcast_addr", "228.6.7.8");
    }
    
    private static void fillSiteLONProps(Properties pProperties)
    {
        pProperties.setProperty("jgroups.site.name", "LON");
        pProperties.setProperty("jgroups.bus.bind.address", "localhost");
        pProperties.setProperty("jgroups.bus.bind.port", "11112");
        pProperties.setProperty("jgroups.bus.tcpping.initial_hosts", "localhost[11111]");
        pProperties.setProperty("jgroups.mcast_addr", "228.6.7.9");
    }
}
