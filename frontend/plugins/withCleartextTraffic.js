const { withAndroidManifest } = require("@expo/config-plugins");

/**
 * Guarantees that development APKs can reach a backend served over HTTP on
 * the local network. This is intentionally explicit because the generated
 * AndroidManifest is the source of truth used by Android at runtime.
 */
module.exports = function withCleartextTraffic(config) {
  return withAndroidManifest(config, (config) => {
    const application = config.modResults.manifest.application?.[0];

    if (!application) {
      throw new Error("AndroidManifest não possui a tag <application>.");
    }

    application.$ = application.$ || {};
    application.$["android:usesCleartextTraffic"] = "true";
    return config;
  });
};
