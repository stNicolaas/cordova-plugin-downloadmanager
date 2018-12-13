package downloadmanager;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import org.apache.cordova.CordovaPlugin;
import org.apache.cordova.CallbackContext;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import android.database.Cursor;
import java.io.File;
import java.util.ArrayList;
import java.io.IOException;
import java.util.List;

public class DownloadManager extends CordovaPlugin {

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    if (action.equals("download")) {
      String title = args.getString(0);
      String download_url = args.getString(1);
      String destination_url = args.getString(2);
      this.startDownload(title, download_url, destination_url, callbackContext);
      return true;
    }
    if (action.equals("status")) {
      long reference = Long.parseLong(args.getString(0));
      this.status(reference, callbackContext);
      return true;
    }
    return false;
  }

  private void status(long reference, CallbackContext callbackContext) {
    android.app.DownloadManager downloadManager = (android.app.DownloadManager) cordova.getActivity().getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
    android.app.DownloadManager.Query query = new android.app.DownloadManager.Query();
    query.setFilterById(reference);
    Cursor cursor = downloadManager.query(query);
    if (cursor.moveToFirst()) {
      int columnIndex = cursor.getColumnIndex(android.app.DownloadManager.COLUMN_STATUS);
      int status = cursor.getInt(columnIndex);
      if (status == android.app.DownloadManager.STATUS_RUNNING){
        callbackContext.success("DOWNLOADING");
      } else {
        callbackContext.success("NOT DOWNLOADING: " + Integer.toString(status));
      }
    }else{
      callbackContext.success("NOT DOWNLOADING: NOT FOUND");
    }
  }

  private void startDownload(String title, String download_url, String destination_url, CallbackContext callbackContext) {
    try {
      if (download_url != null && download_url.length() > 0) {
        android.app.DownloadManager downloadManager = (android.app.DownloadManager) cordova.getActivity().getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
        Uri uri = Uri.parse(download_url);
        android.app.DownloadManager.Request request = new android.app.DownloadManager.Request(uri);
        request.setAllowedNetworkTypes(android.app.DownloadManager.Request.NETWORK_WIFI | android.app.DownloadManager.Request.NETWORK_MOBILE);
        request.setAllowedOverRoaming(false);
        request.setTitle(title);
        // request.setDescription(title);
        request.setDestinationUri(uri);
        request.setDestinationInExternalFilesDir(cordova.getActivity().getApplicationContext(), null, destination_url);
        request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); 
        // VISIBILITY_VISIBLE | VISIBILITY_HIDDEN | VISIBILITY_VISIBLE_NOTIFY_COMPLETED
        long downloadReference = downloadManager.enqueue(request);
        callbackContext.success(Long.toString(downloadReference));
      } else {
        callbackContext.error("Expected one non-empty string argument.");
      }
    } catch (Exception e) {
      callbackContext.error(e.getMessage());
    }
  }
  
  private boolean isExternalStorageAvailable() {
    String state = Environment.getExternalStorageState();
    boolean mExternalStorageAvailable = false;
    boolean mExternalStorageWriteable = false;
    if (Environment.MEDIA_MOUNTED.equals(state)) {
      mExternalStorageAvailable = mExternalStorageWriteable = true;
    } else if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
      mExternalStorageAvailable = true;
      mExternalStorageWriteable = false;
    } else {
      mExternalStorageAvailable = mExternalStorageWriteable = false;
    }
    if (mExternalStorageAvailable == true && mExternalStorageWriteable == true) {
      return true;
    } else {
      return false;
    }
  }
}
