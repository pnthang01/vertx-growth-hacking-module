package vn.panota.growth;

public interface GrowthConstants {

    String GROWTH_HACK_SERVICE_MONGODB = "gh_mongodb";

    interface RequestParams {
        String GROWTH_HACKING_PACKAGE_ID = "ghp_id";
    }

    interface MongoCollection {
        String USER_EVENT = "gh_user_event";
        String GROWTH_HACK_PACKAGE = "gh_growth_package";
        String GROWTH_HACK_REF_CODE = "gh_ref_code";

    }

    interface EventBusName {
        String REGISTERED_SUCCESSFULLY = "user.register_successfully";
    }
}
