package vn.panota.growth.microservice;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Future;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import vn.panota.base.annotation.Starter;
import vn.panota.base.connector.HttpServerConnector;
import vn.panota.base.option.Order;
import vn.panota.growth.GrowthConstants;
import vn.panota.growth.clustering.EventConsumer;
import vn.panota.growth.clustering.EventProducer;
import vn.panota.growth.dao.mongodb.GrowthHackPackageDAO;
import vn.panota.growth.dao.mongodb.UserEventTrackingDAO;

@Starter(Order.LOWEST)
public class GrowthHackingServiceMapping extends AbstractVerticle {

    private static Logger LOGGER = LoggerFactory.getLogger(GrowthHackingServiceMapping.class);

    @Override
    public void start(Future<Void> handler) throws Exception {
        LOGGER.info("Start to run UserManagementStarter...");
        Router router = HttpServerConnector.getRouter();

        UserEventTrackingDAO._initialize(vertx, GrowthConstants.GROWTH_HACK_SERVICE_MONGODB, GrowthConstants.MongoCollection.USER_EVENT);
        GrowthHackPackageDAO._initialize(vertx, GrowthConstants.GROWTH_HACK_SERVICE_MONGODB, GrowthConstants.MongoCollection.GROWTH_HACK_PACKAGE);
        UserEventTrackingDAO._initialize(vertx, GrowthConstants.GROWTH_HACK_SERVICE_MONGODB, GrowthConstants.MongoCollection.GROWTH_HACK_REF_CODE);

        EventProducer._initialize(vertx);
        EventConsumer._initialize(vertx);

        new GrowthHackingRouteMapping(vertx).mappingRoutes(router, ar -> {
            if (ar.succeeded()) handler.handle(Future.succeededFuture());
            else handler.handle(Future.failedFuture(ar.cause()));
        });
    }
}
