package io.kodokojo.service.zookeeper;

import io.kodokojo.config.ZookeeperConfig;
import io.kodokojo.model.BootstrapStackData;
import io.kodokojo.service.ConfigurationStore;
import io.kodokojo.service.ssl.SSLKeyPair;
import javaslang.control.Try;
import org.apache.zookeeper.*;
import org.apache.zookeeper.data.Stat;

import java.io.IOException;

import static java.util.Objects.requireNonNull;

public class ZookeeperConfigurationStore implements ConfigurationStore, Watcher {

    private static final String KODOKOJO_TCP_PORT = "/kodokojo/tcpports";

    private static final String KODOKOJO_PORT_INDEX = "/kodokojo/portIndex";

    private final ZooKeeper zooKeeper;

    public ZookeeperConfigurationStore(ZookeeperConfig zookeeperConfig) {
        requireNonNull(zookeeperConfig, "zookeeperConfig must be defined.");
        try {
            zooKeeper = new ZooKeeper(zookeeperConfig.url(), 2000, this);
            zooKeeper.exists(KODOKOJO_TCP_PORT, this);
        } catch (IOException | InterruptedException | KeeperException e) {
            throw new RuntimeException("Unable to connect to Zookeeper on url " + zookeeperConfig.url() + ".", e);
        }
    }

    @Override
    public boolean storeBootstrapStackData(BootstrapStackData bootstrapStackData) {
        requireNonNull(bootstrapStackData, "bootstrapStackData must be defined.");
        return Try.of(() -> {
            String path = KODOKOJO_TCP_PORT + "/" + bootstrapStackData.getProjectName();
            Stat stat = zooKeeper.exists(path, this);
            if (stat == null) {
                return generateNewPortIndex().andThenTry(port -> {
                    zooKeeper.create(path, port.toString().getBytes(), ZooDefs.Ids.OPEN_ACL_UNSAFE, CreateMode.PERSISTENT);
                });
            }
            return true;
        }).isSuccess();
    }

    @Override
    public boolean storeSSLKeys(String projectName, String brickTypeName, SSLKeyPair sslKeyPair) {
        return true;
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
