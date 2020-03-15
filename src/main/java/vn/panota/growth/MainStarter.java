package vn.panota.growth;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.DeploymentOptions;
import io.vertx.core.Vertx;
import io.vertx.core.VertxOptions;
import io.vertx.core.eventbus.EventBusOptions;
import io.vertx.core.http.ClientAuth;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.core.net.JksOptions;
import io.vertx.core.spi.cluster.ClusterManager;
import io.vertx.spi.cluster.zookeeper.ZookeeperClusterManager;
import vn.panota.base.RunMain;
import vn.panota.base.bridge.mongodb.Cmd;
import vn.panota.growth.service.GrowthPackageService;

import java.lang.reflect.Constructor;

public class MainStarter extends AbstractVerticle {

    private static final Logger LOGGER = LoggerFactory.getLogger(MainStarter.class);

    public MainStarter() {
    }

    public static void main(String[] args) {
        System.setProperty("is_cluster", "true");
        System.setProperty("starter_class", "vn.panota.growth.GrowthHackStarter");
        try {
            boolean isCluster = Boolean.valueOf(System.getProperty("is_cluster", "false"));
            String starterClass = System.getProperty("starter_class");
            Class<RunMain> aClass = (Class<RunMain>) Class.forName(starterClass);
            Constructor<RunMain> constructor = aClass.getConstructor(DeploymentOptions.class);
            DeploymentOptions deploymentOptions = (new DeploymentOptions()).setConfig(Cmd.empty().put("config_type", "one"));
            RunMain runMain = (RunMain)constructor.newInstance(deploymentOptions);
            if (isCluster) {
//                System.setProperty("vertx.zookeeper.config", "./config/zookeeper-conf.json");
                ClusterManager mgr = new ZookeeperClusterManager();
                JsonObject eventBusCfg = ((ZookeeperClusterManager)mgr).getConfig().getJsonObject("event_bus", Cmd.empty());
                EventBusOptions eventBusOptions = (new EventBusOptions()).setSsl(true).setKeyStoreOptions((new JksOptions()).setPath("./config/keystore.jks").setPassword("123tyghvn456")).setTrustStoreOptions((new JksOptions()).setPath("./config/keystore.jks").setPassword("123tyghvn456")).setClientAuth(ClientAuth.REQUIRED);
                if (eventBusCfg.containsKey("public_host") && eventBusCfg.containsKey("public_port")) {
                    eventBusOptions.setClusterPublicHost(eventBusCfg.getString("public_host")).setClusterPublicPort(eventBusCfg.getInteger("public_port"));
                } else {
                    eventBusOptions.setHost(eventBusCfg.getString("host", "localhost")).setPort(eventBusCfg.getInteger("port", 11910));
                }

                VertxOptions vertxOptions = (new VertxOptions()).setClusterManager(mgr).setEventBusOptions(eventBusOptions);
                Vertx.clusteredVertx(vertxOptions, (clusterAr) -> {
                    if (clusterAr.succeeded()) {
                        Vertx vertx = (Vertx)clusterAr.result();
                        vertx.deployVerticle(runMain, deploymentOptions, (deployAr) -> {
                            if (deployAr.succeeded()) {
                                LOGGER.info("------------------ ALL CLUSTERED DEPLOYMENTS ARE SUCCESSFULLY ---------------------");
                            } else {
                                LOGGER.error("------------------ ALL CLUSTERED DEPLOYMENTS ARE FAILED ---------------------", deployAr.cause());
                                System.exit(0);
                            }

                        });
                    } else {
                        LOGGER.error("Could not start application as cluster", clusterAr.cause());
                        System.exit(0);
                    }
                });
            } else {
                Vertx vertx = Vertx.vertx();
                vertx.deployVerticle(runMain, deploymentOptions, (deployAr) -> {
                    if (deployAr.succeeded()) {
                        LOGGER.info("------------------ ALL NON-CLUSTERED DEPLOYMENTS ARE SUCCESSFULLY ---------------------");
                    } else {
                        LOGGER.error("------------------ ALL NON-CLUSTERED DEPLOYMENTS ARE FAILED ---------------------", deployAr.cause());
                        System.exit(0);
                    }

                });
            }
        } catch (Exception e) {
            LOGGER.error("Error when starting MainClass", e);
            System.exit(0);
        }

    }
}
