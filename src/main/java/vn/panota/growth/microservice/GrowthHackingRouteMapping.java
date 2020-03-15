package vn.panota.growth.microservice;

import einvoice.api.exception.UnauthorizedException;
import io.vertx.core.AsyncResult;
import io.vertx.core.Handler;
import io.vertx.core.Vertx;
import io.vertx.core.http.HttpMethod;
import io.vertx.core.http.HttpServerResponse;
import io.vertx.core.logging.Logger;
import io.vertx.core.logging.LoggerFactory;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import io.vertx.ext.web.handler.CorsHandler;
import vn.panota.base.util.HttpUtil;
import vn.panota.growth.service.GrowthPackageService;

import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

public class GrowthHackingRouteMapping {
    private static final Logger LOGGER = LoggerFactory.getLogger(GrowthHackingRouteMapping.class);

    private Vertx vertx;
    private GrowthPackageService growthPackageService;
    private BodyHandler bodyHandler;

    public GrowthHackingRouteMapping(Vertx vertx) {
        this.vertx = vertx;
        growthPackageService = GrowthPackageService._initLoad(vertx);
        this.bodyHandler = BodyHandler.create();
    }

    public void mappingRoutes(Router router, Handler<AsyncResult<Void>> handler) {
        LOGGER.info("Start to load mapping routes");
        vertx.executeBlocking(event -> {
            try {
                Set<HttpMethod> allowedMethods = new HashSet<>(Arrays.asList(HttpMethod.GET,
                        HttpMethod.POST, HttpMethod.PUT, HttpMethod.OPTIONS, HttpMethod.DELETE));
                Set<String> allowedHeaders = new HashSet<>(Arrays.asList("Access-Control-Allow-Origin", "Access-Control-Allow-Method",
                        "X-PINGARUNER", "Content-Type", "x-requested-with", "Authorization", "accept", "origin"));
                CorsHandler corsHandler = CorsHandler.create("*")
                        .allowedHeaders(allowedHeaders)
                        .allowedMethods(allowedMethods);
                router.route("/api/*")
                        .handler(corsHandler)
                        .failureHandler(context -> {
                            HttpServerResponse response = context.response();
                            Throwable failure = context.failure();
                            if (failure instanceof ClassCastException) {
                                HttpUtil.CUSTOM_ERROR_RESPONSE(response, "Cannot parse the param");
                            } else if (failure instanceof UnauthorizedException) {
                                HttpUtil.UNAUTHORISED_REQUEST_RESPONSE(response);
                            } else {
                                LOGGER.error("Something is wrong", failure);
                                HttpUtil.CUSTOM_ERROR_RESPONSE(response, "Something is wrong");
                            }
                        });

//                router.get("/api/*").handler(this::parseParam);
                router.post("/api/*").handler(bodyHandler);
                router.put("/api/*").handler(bodyHandler);

                this.mapGrowthPackageServiceRoutes(router);

                LOGGER.info("LOAD MAPPING ROUTES SUCCESSFULLY");
                event.complete();
            } catch (Exception ex) {
                event.fail(ex);
            }
        }, handler);
    }

    private void mapGrowthPackageServiceRoutes(Router router) {

    }
}
