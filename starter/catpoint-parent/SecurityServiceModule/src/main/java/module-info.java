module SecurityServiceModule {
    requires java.desktop;
    requires java.prefs;
    requires com.google.common;
    requires com.google.gson;
    requires ImageServiceModule;
    requires miglayout;
    opens cpm.udacity.catpoint.security.data to com.google.gson;

}