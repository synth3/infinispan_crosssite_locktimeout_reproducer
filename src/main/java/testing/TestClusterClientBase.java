package testing;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Properties;
import java.util.concurrent.ThreadLocalRandom;

import org.apache.log4j.ConsoleAppender;
import org.apache.log4j.FileAppender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.infinispan.Cache;
import org.infinispan.commons.util.StringPropertyReplacer;
import org.infinispan.configuration.parsing.ConfigurationBuilderHolder;
import org.infinispan.configuration.parsing.ParserRegistry;
import org.infinispan.configuration.parsing.URLXMLResourceResolver;
import org.infinispan.manager.DefaultCacheManager;


public class TestClusterClientBase
{
    private static final Logger LOGGER = Logger.getLogger(TestClusterClientBase.class);

    public static final String PROP_INFINISPAN_NODE_NAME = "infinispan.node.name";
    public static final String PROP_INFINISPAN_CONFIG_BASE = "infinispan.config.base";


    public void performWithProps(Properties pProperties) throws IOException, InterruptedException
    {
        final var nodeName = pProperties.getProperty(PROP_INFINISPAN_NODE_NAME);
        Thread.currentThread().setName(nodeName);

        final ConfigurationBuilderHolder configBuilderHolder;
        final var infinispanConfigPath = Paths.get(pProperties.getProperty(PROP_INFINISPAN_CONFIG_BASE));
        try (var infinispanXmlIs = Files.newInputStream(infinispanConfigPath)) {
            // Enable import of infinispan config xmls
            var xmlResResolver = new URLXMLResourceResolver(infinispanConfigPath.getParent().toUri().toURL())
            {
                @Override
                public java.net.URL resolveResource(String href) throws IOException
                {
                    // enable usage of properties in the href attribute too
                    return super.resolveResource(StringPropertyReplacer.replaceProperties(href, pProperties));
                }
            };
            // Use the ParserRegistry for being able to resolve properties that are
            // referenced in the
            // configuration xml utilizing properties set in our service configuration
            // ...by default only system properties are used for resolving property values
            configBuilderHolder = new ParserRegistry(Thread.currentThread().getContextClassLoader(), false, pProperties)
                    .parse(infinispanXmlIs, xmlResResolver);
        }
        
        try (DefaultCacheManager infinispanCacheManager = new DefaultCacheManager(configBuilderHolder, true))
        {
            Cache<String, String> cacheTestDistributed = infinispanCacheManager.getCache("distributed-cache");
            while (true)
            {
                Thread.sleep(5000 + ThreadLocalRandom.current().nextInt(-2000, 3000));
                try
                {
                    var currentValDist = cacheTestDistributed.get("test");
                    LOGGER.info("Current test value (distributed): " + currentValDist);
                    cacheTestDistributed.put("test", nodeName);
                } catch (Exception e) {
                    LOGGER.error("Cache access exception!", e);
                }
            }
        }
    }

    static void setupLog4J() throws IOException
    {
        var layout = new PatternLayout("[%d{ISO8601}] %-5p [%t] %m%n");
        Logger.getRootLogger().getLoggerRepository().resetConfiguration();
        ConsoleAppender console = new ConsoleAppender();
        console.setLayout(layout);
        console.setThreshold(Level.WARN);
        console.activateOptions();
        Logger.getRootLogger().addAppender(console);

        var file = new FileAppender(layout, "log/the.log", false);
        file.setThreshold(Level.INFO);
        file.activateOptions();
        Logger.getRootLogger().addAppender(file);
    }
}
