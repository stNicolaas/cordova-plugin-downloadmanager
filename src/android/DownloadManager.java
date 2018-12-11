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
        callbackContext.success("NOT DOWNLOADING");
      }
    }else{
      callbackContext.success("NOT DOWNLOADING");
    }
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
      request.setNotificationVisibility(android.app.DownloadManager.Request.VISIBILITY_VISIBLE); //VISIBILITY_HIDDEN
      long downloadReference = downloadManager.enqueue(request);
      callbackContext.success(Long.toString(downloadReference));
    } else {
      callbackContext.error("Expected one non-empty string argument.");
    }
  }
}
