package vn.panota.growth.clustering;

import io.netty.util.internal.StringUtil;
import io.vertx.core.MultiMap;
import io.vertx.core.Vertx;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.eventbus.Message;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import vn.panota.base.CustomHttpHeaders;
import vn.panota.base.bridge.mongodb.Cmd;
import vn.panota.growth.dao.mongodb.UserEventTrackingDAO;

public class EventConsumer {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventConsumer.class);

    private static EventConsumer _instance;

    public static EventConsumer _initialize(Vertx vertx) {
        if (null == _instance) _instance = new EventConsumer(vertx);
        return _instance;
    }

    public static EventConsumer _load() {
        if (null == _instance) throw new NullPointerException("EventConsumer is not initialized yet");
        return _instance;
    }

    private Vertx vertx;
    private EventBus eventBus;
    private UserEventTrackingDAO userEventTrackingDAO;

    private EventConsumer(Vertx vertx) {
        this.vertx = vertx;
        this.eventBus = vertx.eventBus();
        this.userEventTrackingDAO = UserEventTrackingDAO._load();
        this.listenerRegister();
    }

    private void listenerRegister() {
        eventBus.consumer("user.registered_successfully", this::registerSuccessfully);
    }

    private void registerSuccessfully(Message<JsonObject> message) {
        JsonObject data = message.body();
        MultiMap headers = message.headers();
        String tenantId = headers.get(CustomHttpHeaders.X_TENANT_ID);
        String logId = headers.get(CustomHttpHeaders.LOG_ID);
        JsonObject inputData = Cmd.empty()
                .put("action_name", "user.registered")
                .put("acted_user_id", Cmd.oid(data.getString("user_id")))
                .put("ref_code", data.getString("ref_code"));
        if(!StringUtil.isNullOrEmpty(tenantId)) inputData.put("tenant_id", tenantId);
        this.userEventTrackingDAO.insertNewEvent(inputData, ar -> {
            JsonObject object = Cmd.empty().put(CustomHttpHeaders.LOG_ID, logId);
            if (ar.succeeded()) eventBus.publish("log.user.insert_successfully", object);
            else {
                LOGGER.error("Unknown error when insert event " + data + " status.", ar.cause());
                eventBus.publish("log.user.insert_failed", object);
            }
        });
    }
}
