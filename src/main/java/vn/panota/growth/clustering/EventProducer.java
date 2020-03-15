package vn.panota.growth.clustering;

import io.vertx.core.Vertx;
import io.vertx.core.eventbus.DeliveryOptions;
import io.vertx.core.eventbus.EventBus;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import org.bson.types.ObjectId;
import vn.panota.base.CustomHttpHeaders;

public class EventProducer {

    private static final Logger LOGGER = LoggerFactory.getLogger(EventProducer.class);

    private static EventProducer _instance;

    public static EventProducer _initialize(Vertx vertx) {
        if (null == _instance) _instance = new EventProducer(vertx);
        return _instance;
    }

    public static EventProducer _load() {
        if (null == _instance) throw new NullPointerException("EventProducer is not initialized yet");
        return _instance;
    }

    private Vertx vertx;
    private EventBus eventBus;

    private EventProducer(Vertx vertx) {
        this.vertx = vertx;
        this.eventBus = vertx.eventBus();
    }

    public void transferUserLog(String tenantId, String type, String userId, JsonObject message) {
        DeliveryOptions deliveryOptions = new DeliveryOptions()
                .addHeader(CustomHttpHeaders.X_TENANT_ID, tenantId)
                .addHeader("type", type)
                .addHeader("user_id", userId)
                .addHeader(CustomHttpHeaders.LOG_ID, ObjectId.get().toString());
        this.eventBus.publish("log.user.insert", message, deliveryOptions);
    }

}
