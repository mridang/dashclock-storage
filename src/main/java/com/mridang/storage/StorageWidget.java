package com.mridang.storage;

import java.io.File;

import org.acra.ACRA;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.Log;

import com.google.android.apps.dashclock.api.ExtensionData;

/*
 * This class is the main class that provides the widget
 */
public class StorageWidget extends ImprovedExtension {

	/*
	 * (non-Javadoc)
	 * @see com.mridang.battery.ImprovedExtension#getIntents()
	 */
	@Override
	protected IntentFilter getIntents() {
		return null;
	}

	/*
	 * (non-Javadoc)
	 * @see com.mridang.battery.ImprovedExtension#getTag()
	 */
	@Override
	protected String getTag() {
		return getClass().getSimpleName();
	}

	/*
	 * (non-Javadoc)
	 * @see com.mridang.battery.ImprovedExtension#getUris()
	 */
	@Override
	protected String[] getUris() {
		return null;
	}

	/*
	 * @see
	 * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
	 * (int)
	 */
	@Override
	protected void onUpdateData(int arg0) {

		Log.d(getTag(), "Calculating the amount of available storage on device");
		ExtensionData edtInformation = new ExtensionData();
		setUpdateWhenScreenOn(false);

		try {

			File filExternal = Environment.getExternalStorageDirectory();
			StatFs sfsExternal = new StatFs(filExternal.getAbsolutePath());
			Long lngBlize = (long) sfsExternal.getBlockSize();
			Long lngAblock = (long) sfsExternal.getAvailableBlocks();
			Long lngTblock = (long) sfsExternal.getBlockCount();
			Long lngTotal = lngTblock * lngBlize;
			Long lngFree = lngAblock * lngBlize;

			if (lngTotal > 0L) {

				String strTotal = Formatter.formatFileSize(getApplicationContext(), lngTotal);
				String strFree = Formatter.formatFileSize(getApplicationContext(), lngFree);
				Integer intPercent = (int) (0.5d + (double) lngFree * 100 / (double) lngTotal);
				Log.v(getTag(), String.format("%s of %s free on the storage", strFree, strTotal));

				if (Environment.isExternalStorageEmulated()) {
					edtInformation.expandedBody(getString(R.string.internal, strFree, strTotal));
				} else {
					edtInformation.expandedBody(getString(R.string.external, strFree, strTotal));
				}

				edtInformation.visible(true);
				edtInformation.expandedTitle(getString(R.string.available_space, intPercent));
				edtInformation.status(intPercent.toString());
				edtInformation.clickIntent(new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS));

			}

		} catch (Exception e) {
			edtInformation.visible(false);
			Log.e(getTag(), "Encountered an error", e);
			ACRA.getErrorReporter().handleSilentException(e);
		}

		edtInformation.icon(R.drawable.ic_dashclock);
		doUpdate(edtInformation);

	}

	/*
	 * (non-Javadoc)
	 * @see com.mridang.alarmer.ImprovedExtension#onReceiveIntent(android.content.Context, android.content.Intent)
	 */
	@Override
	protected void onReceiveIntent(Context ctxContext, Intent ittIntent) {
		onUpdateData(UPDATE_REASON_MANUAL);
	}

}