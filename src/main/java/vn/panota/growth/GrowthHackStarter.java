package vn.panota.growth;

import io.vertx.core.DeploymentOptions;
import io.vertx.core.Promise;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import vn.panota.base.DeployHelpers;
import vn.panota.base.LocalCacheInitializer;
import vn.panota.base.RunMain;
import vn.panota.base.connector.HttpClientConnector;
import vn.panota.base.connector.HttpServerConnector;
import vn.panota.base.connector.MongodbConnector;
import vn.panota.growth.microservice.GrowthHackingServiceMapping;

import java.util.Arrays;

public class GrowthHackStarter extends RunMain {

    private static Logger LOGGER = LoggerFactory.getLogger(GrowthHackStarter.class);

    public GrowthHackStarter(DeploymentOptions deploymentOptions) {
        super(deploymentOptions);
    }

    @Override
    public void start(Promise<Void> promise) {
//        JsonObject config = Cmd.empty()
//                .put("HTTP_SERVER_CONFIG", "./config/http-servers-config.json");
//        DeploymentOptions deploymentOptions = new DeploymentOptions().setConfig(config);
        DeployHelpers.deployMultiple(vertx, Arrays.asList(
                MongodbConnector.class, HttpServerConnector.class, HttpClientConnector.class,
                LocalCacheInitializer.class, GrowthHackingServiceMapping.class), deploymentOptions, result -> {
            if (result.succeeded()) {
                promise.complete();
                LOGGER.info("Deploy services successfully");
            } else {
                promise.fail(result.cause());
                LOGGER.error("Deploy services failed. About quitting...");
            }
        });
    }
}
