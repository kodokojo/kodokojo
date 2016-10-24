package io.kodokojo.service.zookeeper;

import io.kodokojo.config.ZookeeperConfig;
import io.kodokojo.service.BootstrapConfigurationProvider;
import javaslang.control.Try;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

public class ZookeeperBootstrapConfigurationProvider implements BootstrapConfigurationProvider, Watcher {

    private static final Logger LOGGER = LoggerFactory.getLogger(ZookeeperBootstrapConfigurationProvider.class);

    public static final String KODOKOJO_TCP_PORT = "/kodokojo/tcpports";

    public  static final String KODOKOJO_PORT_INDEX = "/kodokojo/portIndex";

    private final ZooKeeper zooKeeper;


    public ZookeeperBootstrapConfigurationProvider(ZookeeperConfig zookeeperConfig) {
        requireNonNull(zookeeperConfig, "zookeeperConfig must be defined.");
        try {
            zooKeeper = new ZooKeeper(zookeeperConfig.url(), 2000, this);
            zooKeeper.exists(KODOKOJO_TCP_PORT, this);
        } catch (IOException | InterruptedException | KeeperException e) {
            throw new RuntimeException("Unable to connect to Zookeeper on url " + zookeeperConfig.url() + ".", e);
        }
    }


    @Override
    public int provideTcpPortEntrypoint(String projectName, String stackName) {
        String path = KODOKOJO_TCP_PORT + "/" + projectName;
        Try<Integer> aTry = Try.of(() -> {
            Stat stat = zooKeeper.exists(path, this);
            if (stat == null) {
                return generateNewPortIndex().andThenTry(port -> {
                    zooKeeper.create(path, port.toString().getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("Generate Tcp port {} on zookeeper path '{}'.", path, path);
                    }
                }).getOrElse(-1);
            } else {
                byte[] data = zooKeeper.getData(path, this, stat);
                Integer res = Integer.valueOf(new String(data));
                return res;
            }
        });
        return aTry.getOrElse(-1);
    }

    @Override
    public void process(WatchedEvent event) {
        //  Nothing to do.
    }

    protected final Try<Integer> generateNewPortIndex() {
        return Try.of(() -> {
            Stat stat = zooKeeper.exists(KODOKOJO_PORT_INDEX, this);
            Integer port = 1;
            if (stat == null) {
                zooKeeper.create(KODOKOJO_PORT_INDEX, port.toString().getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
            } else {
                byte[] data = zooKeeper.getData(KODOKOJO_PORT_INDEX, this, stat);
                int version = stat.getVersion();
                port = Integer.parseInt(new String(data));
                port++;
                zooKeeper.setData(KODOKOJO_PORT_INDEX, port.toString().getBytes(), version);

            }
            return port;
        });
    }
}
