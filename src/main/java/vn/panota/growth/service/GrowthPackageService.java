package vn.panota.growth.service;

import io.vertx.core.*;
import io.vertx.core.http.HttpServerRequest;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.json.JsonObject;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.auth.User;
import io.vertx.ext.web.RoutingContext;
import vn.panota.base.CustomHttpHeaders;
import vn.panota.base.bridge.mongodb.Cmd;
import vn.panota.base.service.AbstractDataService;
import vn.panota.base.util.HttpUtil;
import vn.panota.base.util.StringUtil;
import vn.panota.growth.GrowthConstants;
import vn.panota.growth.clustering.EventProducer;
import vn.panota.growth.dao.mongodb.GrowthHackPackageDAO;

import java.util.ArrayList;
import java.util.List;

public class GrowthPackageService extends AbstractDataService {

    private static final Logger LOGGER = LoggerFactory.getLogger(GrowthPackageService.class);

    private static GrowthPackageService userService = null;

    public synchronized static GrowthPackageService _initLoad(Vertx vertx) {
        if (null == userService) userService = new GrowthPackageService(vertx);
        return userService;
    }

    private GrowthHackPackageDAO growthHackPackageDAO;
    private EventProducer eventProducer;

    private GrowthPackageService(Vertx vertx) {
        super(vertx);
        this.growthHackPackageDAO = GrowthHackPackageDAO._load();
        this.eventProducer = EventProducer._load();
    }

    public void handleSendInvitationViaEmail(RoutingContext context) {
        HttpServerRequest request = context.request();
        request.params();
    }

    public void handleAcceptPackage(RoutingContext context) {
        HttpServerRequest request = context.request();
        User user = context.user();
        String ghpId = request.getParam(GrowthConstants.RequestParams.GROWTH_HACKING_PACKAGE_ID);
    }

    public void handleDirectlyAcceptPackage(RoutingContext context) {
        HttpServerRequest request = context.request();
        String ghpId = request.getParam(GrowthConstants.RequestParams.GROWTH_HACKING_PACKAGE_ID);
    }

    public void handleAcceptNewsLetter(RoutingContext context) {
        HttpServerRequest request = context.request();
        HttpServerResponse response = context.response();
        MultiMap params = request.params();
        String tenantId = request.getHeader(CustomHttpHeaders.X_TENANT_ID);
        List<String> emailList = params.getAll("email");
        List<Future> createFutList = new ArrayList();
        for (String email : emailList) {
            Promise<JsonObject> fut = Promise.promise();
            createFutList.add(fut.future());
//            this.growthPackageDAO.createNewsLetterRegistration(tenantId, email, fut);
        }
        CompositeFuture.all(createFutList).setHandler(ar -> {
            if (ar.succeeded()) HttpUtil.OK_RESPONSE(response);
            else HttpUtil.CUSTOM_ERROR_RESPONSE(response, ar.cause().getMessage());
        });
    }

    public void handleCreatePackage(RoutingContext context) {
        HttpServerResponse response = context.response();
        HttpServerRequest request = context.request();
        String tenantId = request.getHeader(CustomHttpHeaders.X_TENANT_ID);
        User user = context.user();
        String userId = user.principal().getString("uid");
        JsonObject body = context.getBodyAsJson();
        JsonObject data = Cmd.empty()
                .put("tenant_id", tenantId)
                .put("user_id", Cmd.oid(userId))
                .put("name", body.getString("name"))
                .put("rule", body.getJsonObject("rule"))
                .put("value", body.getJsonObject("value"))
                .put("status", 0);
        if (body.containsKey("description")) data.put("description", body.getString("description"));
        this.growthHackPackageDAO.createNewPackage(data, ar -> {
            if (ar.succeeded()) {
                String packageId = ar.result().getString("_id");
                HttpUtil.OK_RESPONSE(response);
                this.eventProducer.transferUserLog(tenantId, "growth_hack", userId, Cmd.empty().put("action", "createNew").put("package_id", Cmd.oid(packageId)));
            } else HttpUtil.CUSTOM_ERROR_RESPONSE(response, ar.cause().getMessage());
        });
    }

    public void handleUpdatePackageStatus(RoutingContext context) {
        HttpServerResponse response = context.response();
        HttpServerRequest request = context.request();
        String tenantId = request.getHeader(CustomHttpHeaders.X_TENANT_ID);
        User user = context.user();
        String userId = user.principal().getString("uid");
        String packageId = request.getParam("pkg_id");
        int status = StringUtil.safeParseInt(request.getParam("stt"));
        this.growthHackPackageDAO.updatePackageStatus(packageId, status, ar -> {
            if (ar.succeeded()) {
                HttpUtil.OK_RESPONSE(response);
                this.eventProducer.transferUserLog(tenantId, "growth_hack", userId, Cmd.empty().put("action", "updateStatus").put("package_id", Cmd.oid(packageId)));
            } else HttpUtil.CUSTOM_ERROR_RESPONSE(response, ar.cause().getMessage());
        });
    }

    public void handleEditPackage(RoutingContext context) {
        HttpServerResponse response = context.response();
        HttpServerRequest request = context.request();
        String tenantId = request.getHeader(CustomHttpHeaders.X_TENANT_ID);
        String packageId = request.getParam("pkg_id");
        User user = context.user();
        String userId = user.principal().getString("uid");
        JsonObject body = context.getBodyAsJson();
        JsonObject data = Cmd.empty()
                .put("user_id", Cmd.oid(userId))
                .put("name", body.getString("name"))
                .put("rule", body.getJsonObject("rule"))
                .put("value", body.getJsonObject("value"));
        this.growthHackPackageDAO.editPackage(packageId, data, ar -> {
            if (ar.succeeded()) {
                HttpUtil.OK_RESPONSE(response);
                this.eventProducer.transferUserLog(tenantId, "growth_hack", userId, Cmd.empty().put("action", "editPackage").put("package_id", Cmd.oid(packageId)));
            } else HttpUtil.CUSTOM_ERROR_RESPONSE(response, ar.cause().getMessage());
        });
    }

    public void handleFetchPackages(RoutingContext context) {

    }
}
