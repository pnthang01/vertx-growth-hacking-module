package vn.panota.growth.dao.mongodb;

import io.vertx.core.AsyncResult;
import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import vn.panota.base.bridge.mongodb.Cmd;
import vn.panota.base.bridge.mongodb.FieldUpdate;
import vn.panota.base.dao.AbstractMongodbDAO;

import java.util.Date;

public class GrowthHackPackageDAO extends AbstractMongodbDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrowthHackPackageDAO.class);

    private static GrowthHackPackageDAO _instance;

    public static GrowthHackPackageDAO _initialize(Vertx vertx, String conName, String collection) {
        if (null == _instance) {
            _instance = new GrowthHackPackageDAO(vertx, conName, collection);
            _instance.init();
        }
        return _instance;
    }

    public static GrowthHackPackageDAO _load() {
        if (null == _instance) throw new NullPointerException("GrowthPackageDAO is not initialized yet");
        return _instance;
    }

    private GrowthHackPackageDAO(Vertx vertx, String conName, String collection) {
        super(vertx, conName, collection, "growthHack");
    }

    @Override
    public <T extends AbstractMongodbDAO> T init() {
        return null;
    }

    @Override
    public void setupCollection(Handler<AsyncResult<Void>> handler) {
    }

    public void createNewPackage(JsonObject data, Handler<AsyncResult<JsonObject>> handler) {
        this.createOne(data, ar -> {
            if (ar.succeeded()) handler.handle(Future.succeededFuture(ar.result()));
            else this.handleException("createPackage", ar.cause(), handler);
        });
    }

    public void updatePackageStatus(String packageId, int status, Handler<AsyncResult<Void>> handler) {
        this.editOne(Cmd.empty().put("_id", Cmd.oid(packageId)), FieldUpdate.set(Cmd.empty().put("status", status)), null, ar -> {
            if (ar.succeeded()) handler.handle(Future.succeededFuture());
            else this.handleException("updatePackageStatus", ar.cause(), handler);
        });
    }

    public void editPackage(String packageId, JsonObject data, Handler<AsyncResult<JsonObject>> handler) {
        JsonObject setData = FieldUpdate.set(Cmd.empty()
                .mergeIn(data)
                .put(LAST_UPDATED, new Date().toInstant()));
        this.editOne(Cmd.empty().put("_id", Cmd.oid(packageId)), setData, null, ar -> {
            if (ar.succeeded()) handler.handle(Future.succeededFuture());
            else this.handleException("editPackage", ar.cause(), handler);
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
