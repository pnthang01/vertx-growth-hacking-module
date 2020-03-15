package vn.panota.growth.dao.mongodb;

import io.vertx.core.*;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.mongo.IndexOptions;
import vn.panota.base.bridge.mongodb.Cmd;
import vn.panota.base.bridge.mongodb.FieldUpdate;
import vn.panota.base.dao.AbstractMongodbDAO;

import java.util.List;

public class GrowthHackRefCodeDAO extends AbstractMongodbDAO {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrowthHackRefCodeDAO.class);

    private static GrowthHackRefCodeDAO _instance;

    public static GrowthHackRefCodeDAO _initialize(Vertx vertx, String conName, String collection) {
        if (null == _instance) {
            _instance = new GrowthHackRefCodeDAO(vertx, conName, collection);
            _instance.init();
        }
        return _instance;
    }

    public static GrowthHackRefCodeDAO _load() {
        if (null == _instance) throw new NullPointerException("GrowthHackRefCodeDAO is not initialized yet");
        return _instance;
    }

    private GrowthHackRefCodeDAO(Vertx vertx, String conName, String collection) {
        super(vertx, conName, collection);
    }

    @Override
    public <T extends AbstractMongodbDAO> T init() {
        return null;
    }

    @Override
    public void setupCollection(Handler<AsyncResult<Void>> handler) {
        mongoClient.getCollections(ar -> {
            if (ar.succeeded()) {
                List<String> result = ar.result();
                if (!result.contains(this.collection)) {
                    Promise<Void> firstChain = Promise.promise();
                    this.mongoClient.createCollection(this.collection, firstChain);
                    firstChain.future().compose(voidAr -> {
                        Promise<Void> secondChain = Promise.promise();
                        this.mongoClient.createIndexWithOptions(this.collection, Cmd.empty().put("user_id", 1).put("package_id", 1),
                                new IndexOptions().unique(true), secondChain);
                        return secondChain.future();
                    }).compose(voidAr -> {
                        Promise<Void> thirdChain = Promise.promise();
                        this.mongoClient.createIndexWithOptions(this.collection, Cmd.empty().put("ref_code", 1),
                                new IndexOptions().unique(true), thirdChain);
                        return thirdChain.future();
                    }).setHandler(handler);
                } else handler.handle(Future.succeededFuture());
            } else handler.handle(Future.failedFuture(ar.cause()));
        });
    }

    public void createNewReferenceCode(String userId, String packageId, String refCode, Handler<AsyncResult<JsonObject>> handler) {
        JsonObject data = Cmd.empty()
                .put("user_id", Cmd.oid(userId))
                .put("package_id", Cmd.oid(packageId))
                .put("ref_code", refCode)
                .put("achieve_count", 0);
        this.createOne(data, ar -> {
            if (ar.succeeded()) handler.handle(Future.succeededFuture(ar.result()));
            else this.handleException("newRefCode", ar.cause(), handler);
        });
    }

    public void getByReferenceCode(String refCode, Handler<AsyncResult<JsonObject>> handler) {
        this.findOne(Cmd.empty().put("ref_code", refCode), ar -> {
            if (ar.succeeded()) handler.handle(Future.succeededFuture(ar.result()));
            else this.handleException("getRefCode", ar.cause(), handler);
        });
    }

    public void updateAchieveCount(String refCode, Handler<AsyncResult<Void>> handler) {
        this.editOne(Cmd.empty().put("ref_code", refCode), FieldUpdate.inc(Cmd.empty().put("achieve_count",1 )), null, ar -> {
            if(ar.succeeded()) handler.handle(Future.succeededFuture());
            else this.handleException("updateAchieveCount", ar.cause(), handler);
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
