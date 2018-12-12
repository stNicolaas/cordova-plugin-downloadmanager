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

public class DownloadManager extends CordovaPlugin {

  @Override
  public boolean execute(String action, JSONArray args, CallbackContext callbackContext) throws JSONException {
    if (action.equals("download")) {
      String message = args.getString(0);
      this.startDownload(message, callbackContext);
      return true;
    }
    if (action.equals("status")) {
      long reference = Long.parseLong(args.getString(0));
      this.status(reference, callbackContext);
      return true;
    }
    if (action.equals("storage_available")) {
      boolean available = this.isExternalStorageAvailable();
      if (available){
        callbackContext.success("kwaai");
      } else {
        callbackContext.error("vok");
      }
      return true;
    }
    if (action.equals("test")) {
      this.test(callbackContext);
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

  private void test(CallbackContext callbackContext) {
    String[] array = this.getExtSdCardDataPaths(this.cordova.getActivity().getApplicationContext());
    callbackContext.success(String.join(",", array));
  }

  private String[] getExtSdCardDataPaths(Context context) {
      List<String> paths = new ArrayList<String>();
      for (File file : context.getExternalFilesDirs("external")) {
          if (file != null) {
              int index = file.getAbsolutePath().lastIndexOf("/Android/data");
              if (index >= 0) {
                  String path = file.getAbsolutePath().substring(0, index);
                  try {
                      path = new File(path).getCanonicalPath();
                  } catch (IOException e) {
                      // Keep non-canonical path.
                  }
                  paths.add(path);
              }
          }
      }
      if (paths.isEmpty()) paths.add("/storage/sdcard1");
      return paths.toArray(new String[0]);
  }

  private void startDownload(String message, CallbackContext callbackContext) {
    if (message != null && message.length() > 0) {
      String filename = message.substring(message.lastIndexOf("/")+1, message.length());
      try {
        filename = URLDecoder.decode(filename,"UTF-8");
      } catch (UnsupportedEncodingException e) {
        callbackContext.error("Error in converting filename");
      }
      android.app.DownloadManager downloadManager = (android.app.DownloadManager) cordova.getActivity().getApplicationContext().getSystemService(Context.DOWNLOAD_SERVICE);
      Uri Download_Uri = Uri.parse(message);
      android.app.DownloadManager.Request request = new android.app.DownloadManager.Request(Download_Uri);
      request.setAllowedNetworkTypes(android.app.DownloadManager.Request.NETWORK_WIFI | android.app.DownloadManager.Request.NETWORK_MOBILE);
      request.setAllowedOverRoaming(false);
      request.setTitle(filename);
      request.setDescription(filename);
      request.setDestinationInExternalFilesDir(cordova.getActivity().getApplicationContext(), "", filename);
      request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED); 
      // VISIBILITY_VISIBLE | VISIBILITY_HIDDEN | VISIBILITY_VISIBLE_NOTIFY_COMPLETED
      long downloadReference = downloadManager.enqueue(request);
      callbackContext.success(Long.toString(downloadReference));
    } else {
      callbackContext.error("Expected one non-empty string argument.");
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
