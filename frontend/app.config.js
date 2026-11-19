const GOOGLE_CLIENT_ID_SUFFIX = ".apps.googleusercontent.com";

module.exports = ({ config }) => {
  const iosClientId = process.env.EXPO_PUBLIC_GOOGLE_IOS_CLIENT_ID;
  const plugins = (config.plugins || []).filter((plugin) => {
    const pluginName = Array.isArray(plugin) ? plugin[0] : plugin;
    return pluginName !== "@react-native-google-signin/google-signin";
  });

  if (iosClientId) {
    if (!iosClientId.endsWith(GOOGLE_CLIENT_ID_SUFFIX)) {
      throw new Error("EXPO_PUBLIC_GOOGLE_IOS_CLIENT_ID is not a valid Google client ID.");
    }
    plugins.push([
      "@react-native-google-signin/google-signin",
      {
        iosUrlScheme: `com.googleusercontent.apps.${iosClientId.slice(
          0,
          -GOOGLE_CLIENT_ID_SUFFIX.length,
        )}`,
      },
    ]);
  }

  return { ...config, plugins };
};
