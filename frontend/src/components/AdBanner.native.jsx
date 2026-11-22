import React from "react";
import { Platform, View } from "react-native";
import {
  BannerAd,
  BannerAdSize,
  TestIds,
} from "react-native-google-mobile-ads";

const productionUnitId = "ca-app-pub-1541774679229499/9346479655";
const useTestAds =
  __DEV__ || process.env.EXPO_PUBLIC_ADMOB_USE_TEST_ADS === "true";

export function AdBanner() {
  if (Platform.OS !== "android") return null;

  return (
    <View
      style={{
        alignItems: "center",
        justifyContent: "center",
        paddingVertical: 4,
      }}
    >
      <BannerAd
        unitId={useTestAds ? TestIds.BANNER : productionUnitId}
        size={BannerAdSize.LARGE_ANCHORED_ADAPTIVE_BANNER}
        onAdFailedToLoad={(error) => {
          console.error("Não foi possível carregar o banner do AdMob.", error);
        }}
      />
    </View>
  );
}
