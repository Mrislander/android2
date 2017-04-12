package com.elitise.firmwareupdate;

import com.elitise.firmwareupdate.ui.camera.GraphicOverlay;
import com.google.android.gms.vision.MultiProcessor;
import com.google.android.gms.vision.Tracker;
import com.google.android.gms.vision.barcode.Barcode;

/**
 * Created by Andy.Xiang on 10/31/2016.
 */

class BarcodeTrackerFactory implements MultiProcessor.Factory<Barcode> {
    private GraphicOverlay<BarcodeGraphic> mGraphicOverlay;
    private BarcodeCaptureActivity bActivity;
    BarcodeTrackerFactory(GraphicOverlay<BarcodeGraphic> barcodeGraphicOverlay,BarcodeCaptureActivity activity) {
        mGraphicOverlay = barcodeGraphicOverlay;
        bActivity = activity;
    }

    @Override
    public Tracker<Barcode> create(Barcode barcode) {
        BarcodeGraphic graphic = new BarcodeGraphic(mGraphicOverlay);
        return new BarcodeGraphicTracker(mGraphicOverlay, graphic,bActivity);
    }

}
