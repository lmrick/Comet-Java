package com.cometproject.server.api;

import com.cometproject.api.config.Configuration;
import com.cometproject.api.utilities.process.Initializable;
import com.cometproject.server.api.routes.*;
import com.cometproject.server.api.transformers.JsonTransformer;
import org.apache.log4j.Logger;
import spark.Spark;
import java.util.Arrays;

public class APIManager implements Initializable {
    private static final Logger log = Logger.getLogger(APIManager.class.getName());
    private static final String[] configProperties = new String[]{
            "comet.api.enabled",
            "comet.api.port",
            "comet.api.token"
    };
    private static APIManager apiManagerInstance;
    private boolean enabled;
    private int port;
    private String authToken;
    private JsonTransformer jsonTransformer;

    public APIManager() {

    }

    public static APIManager getInstance() {
        if (apiManagerInstance == null)
            apiManagerInstance = new APIManager();

        return apiManagerInstance;
    }

    @Override
    public void initialize() {
        this.initializeConfiguration();
        this.initializeSpark();
        this.initializeRouting();
    }

    private void initializeConfiguration() {
        boolean allPropertiesAvailable = Arrays.stream(configProperties)
            .allMatch(configProperty -> {
                boolean containsKey = Configuration.currentConfig().containsKey(configProperty);
                if (!containsKey) {
                log.warn("API configuration property not available: " + configProperty + ", API is disabled");
                }
                return containsKey;
            });

        if (!allPropertiesAvailable) {
            this.enabled = false;
            return;
        }

        this.enabled = Configuration.currentConfig().getProperty("comet.api.enabled").equals("true");
        this.port = Integer.parseInt(Configuration.currentConfig().getProperty("comet.api.port"));
        this.authToken = Configuration.currentConfig().getProperty("comet.api.token");
    }

    private void initializeSpark() {
        if (!this.enabled) return;
        Spark.port(this.port);
        this.jsonTransformer = new JsonTransformer();
    }

    private void initializeRouting() {
        if (!this.enabled)
            return;

        Spark.before((request, response) -> {
            boolean authenticated = request.headers("authToken") != null && request.headers("authToken").equals(this.authToken);

            if (!authenticated) {
                log.error("Unauthenticated request from: " + request.ip() + "; " + request.contextPath());
                response.type("application/json");
                Spark.halt("{\"error\":\"Invalid authentication token\"}");
            }
        });

        Spark.get("/", (request, response) -> {
            Spark.halt(404);
            return "Invalid request, if you believe you received this in error, please contact the server administrator!";
        });

        Spark.get("/player/:id/reload", PlayerRoutes::reloadPlayerData, jsonTransformer);
        Spark.get("/player/:id/disconnect", PlayerRoutes::disconnect, jsonTransformer);
        Spark.post("/player/:id/alert", PlayerRoutes::alert, jsonTransformer);
        Spark.get("/player/:id/badge/:badge", PlayerRoutes::giveBadge, jsonTransformer);
        Spark.post("/player/:id/gift", RewardRoutes::gift, jsonTransformer);

        Spark.get("/rooms/active/all", RoomRoutes::getAllActiveRooms, jsonTransformer);
        Spark.get("/room/:id/:action", RoomRoutes::roomAction, jsonTransformer);

        Spark.get("/system/status", SystemRoutes::status, jsonTransformer);
        Spark.get("/system/shutdown", SystemRoutes::shutdown, jsonTransformer);
        Spark.get("/system/reload/:type", SystemRoutes::reload, jsonTransformer);
        Spark.post("/camera/purchase", PhotoRoutes::purchase, jsonTransformer);
        Spark.get("/camera/purchase", PhotoRoutes::purchase, jsonTransformer);
    }

}
