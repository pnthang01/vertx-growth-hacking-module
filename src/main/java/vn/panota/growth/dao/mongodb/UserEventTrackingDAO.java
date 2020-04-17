package vn.panota.growth.dao.mongodb;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import vn.panota.base.dao.AbstractMongodbDAO;

public class UserEventTrackingDAO extends AbstractMongodbDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserEventTrackingDAO.class);

    private static UserEventTrackingDAO _instance;

    public static UserEventTrackingDAO _initialize(Vertx vertx, String conName, String collection) {
        if (null == _instance) {
            _instance = new UserEventTrackingDAO(vertx, conName, collection);
            _instance.init();
        }
        return _instance;
    }

    public static UserEventTrackingDAO _load() {
        if (null == _instance) throw new NullPointerException("UserEventTrackingDAO is not initialized yet");
        return _instance;
    }


    private UserEventTrackingDAO(Vertx vertx, String conName, String collection) {
        super(vertx, conName, collection);
    }

    @Override
    public <T extends AbstractMongodbDAO> T init() {
        return null;
    }

    @Override
    public void setupCollection(Handler<AsyncResult<Void>> handler) {

    }

    public void insertNewEvent(JsonObject inputData, Handler<AsyncResult<JsonObject>> handler) {
        this.createOne(inputData, ar -> {
            if(ar.succeeded()) handler.handle(Future.succeededFuture(ar.result()));
            else this.handleException("newEvent", ar.cause(), handler);
        });
    }

    private void handleException(String action, Throwable cause, Handler handler) {
        String message = null;
        switch (cause.getClass().getName()) {
            default:
                message = String.format("error.%s.%s.unknown", module, action);
        }
        LOGGER.error(message, cause);
        handler.handle(Future.failedFuture(message));
    }
}
