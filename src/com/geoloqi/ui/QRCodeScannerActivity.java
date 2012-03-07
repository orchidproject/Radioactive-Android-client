package com.geoloqi.ui;

import com.geoloqi.mapattack.R;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

public class QRCodeScannerActivity extends Activity {

	private final static String QRTAG = "QRCODE_TAG";
	private static final int DIALOG_QRCODE_RETURN = 0;
	private String mDialogReturn = "";

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		IntentIntegrator integrator = new IntentIntegrator(this);
		integrator.initiateScan(IntentIntegrator.QR_CODE_TYPES);
	}


	
}
