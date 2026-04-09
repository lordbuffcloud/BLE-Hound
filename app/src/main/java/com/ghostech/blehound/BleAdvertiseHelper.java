package com.ghostech.blehound;

import android.bluetooth.le.AdvertiseData;

/**
 * Java helper to work around Kotlin resolution issues with
 * AdvertiseData.Builder.addManufacturerSpecificData()
 */
public class BleAdvertiseHelper {
    public static AdvertiseData buildManufacturerData(int companyId, byte[] data) {
        return new AdvertiseData.Builder()
                .setIncludeDeviceName(false)
                .setIncludeTxPowerLevel(false)
                .addManufacturerSpecificData(companyId, data)
                .build();
    }
}
