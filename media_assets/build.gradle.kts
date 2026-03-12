plugins {
    id("com.android.asset-pack")
}

assetPack {
    packName.set("media_assets")
    dynamicDelivery {
        deliveryType = "install-time"
    }
}
