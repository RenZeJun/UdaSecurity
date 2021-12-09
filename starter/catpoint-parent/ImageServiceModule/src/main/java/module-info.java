module ImageServiceModule {
    exports com.udacity.catpoint.image.service to com.udacity.catpoint.security.application, SecurityServiceModule;
    requires java.desktop;
    requires java.prefs;
    requires software.amazon.awssdk.services.rekognition;
    requires software.amazon.awssdk.auth;
    requires software.amazon.awssdk.core;
    requires software.amazon.awssdk.regions;
    requires org.slf4j;
}