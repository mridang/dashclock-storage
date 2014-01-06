package com.mridang.storage;

import java.util.Random;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Environment;
import android.os.StatFs;
import android.provider.Settings;
import android.text.format.Formatter;
import android.util.Log;

import com.bugsense.trace.BugSenseHandler;
import com.google.android.apps.dashclock.api.DashClockExtension;
import com.google.android.apps.dashclock.api.ExtensionData;

/*
 * This class is the main class that provides the widget
 */
public class StorageWidget extends DashClockExtension {

	/*
	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onCreate()
	 */
	public void onCreate() {

		super.onCreate();
		Log.d("StorageWidget", "Created");
		BugSenseHandler.initAndStartSession(this, getString(R.string.bugsense));

	}

	/*
	 * This calculates the amount of external memory on the device
	 * 
	 * @returns   The amount of external memory
	 */
	public Long getExternalTotalMemory() {

		try {

			StatFs sfsExternal = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
			return Long.valueOf(sfsExternal.getBlockCount()) * Long.valueOf(sfsExternal.getBlockSize());

		} catch (Exception e) {
			e.printStackTrace();
			return 0L;
		}

	}

	/*
	 * @see
	 * com.google.android.apps.dashclock.api.DashClockExtension#onUpdateData
	 * (int)
	 */
	@Override
	protected void onUpdateData(int arg0) {

		setUpdateWhenScreenOn(true);

		Log.d("StorageWidget", "Checking device storage");
		ExtensionData edtInformation = new ExtensionData();
		edtInformation.visible(true);

		try {

			Log.v("StorageWidget", "Memory: " + Formatter.formatFileSize(getApplicationContext(), getExternalTotalMemory()));
			if (getExternalTotalMemory() > 0L) {
				edtInformation
				.expandedBody((edtInformation.expandedBody() == null ? ""
						: edtInformation.expandedBody() + "\n")
						+ String.format(getString(Environment.isExternalStorageEmulated() ? R.string.internal : R.string.external),
								Formatter.formatFileSize(
										getApplicationContext(),
										getExternalFreeMemory()),
										Formatter.formatFileSize(
												getApplicationContext(),
												getExternalTotalMemory())));
			}

			edtInformation.status(String.format(getString(R.string.available_space), (int) (0.5d + (double) getExternalFreeMemory() * 100 / (double) getExternalTotalMemory())));
			edtInformation.clickIntent(new Intent(Settings.ACTION_INTERNAL_STORAGE_SETTINGS));

			if (new Random().nextInt(5) == 0) {

				PackageManager mgrPackages = getApplicationContext().getPackageManager();

				try {

					mgrPackages.getPackageInfo("com.mridang.donate", PackageManager.GET_META_DATA);

				} catch (NameNotFoundException e) {

					Integer intExtensions = 0;
				    Intent ittFilter = new Intent("com.google.android.apps.dashclock.Extension");
				    String strPackage;

				    for (ResolveInfo info : mgrPackages.queryIntentServices(ittFilter, 0)) {

				    	strPackage = info.serviceInfo.applicationInfo.packageName;
						intExtensions = intExtensions + (strPackage.startsWith("com.mridang.") ? 1 : 0); 

					}

					if (intExtensions > 1) {

						edtInformation.visible(true);
						edtInformation.clickIntent(new Intent(Intent.ACTION_VIEW).setData(Uri.parse("market://details?id=com.mridang.donate")));
						edtInformation.expandedTitle("Please consider a one time purchase to unlock.");
						edtInformation.expandedBody("Thank you for using " + intExtensions + " extensions of mine. Click this to make a one-time purchase or use just one extension to make this disappear.");
						setUpdateWhenScreenOn(true);

					}

				}

			} else {
				setUpdateWhenScreenOn(false);
			}

		} catch (Exception e) {
			edtInformation.visible(false);
			Log.e("StorageWidget", "Encountered an error", e);
			BugSenseHandler.sendException(e);
		}

		edtInformation.icon(R.drawable.ic_dashclock);
		publishUpdate(edtInformation);
		Log.d("StorageWidget", "Done");

	}

	/*
	 * This calculates the amount of external free memory on the device
	 * 
	 * @returns   The amount of free external memory
	 */
	public Long getExternalFreeMemory() {

		try {

			StatFs sfsExternal = new StatFs(Environment.getExternalStorageDirectory().getAbsolutePath());
			return Long.valueOf(sfsExternal.getAvailableBlocks()) * Long.valueOf(sfsExternal.getBlockSize());

		} catch (Exception e) {
			e.printStackTrace();
			return 0L;
		}

	}

	/*
	 * @see com.google.android.apps.dashclock.api.DashClockExtension#onDestroy()
	 */
	public void onDestroy() {

		super.onDestroy();
		Log.d("StorageWidget", "Destroyed");
		BugSenseHandler.closeSession(this);

	}

}