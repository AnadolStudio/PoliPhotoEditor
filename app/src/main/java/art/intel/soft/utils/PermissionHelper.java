package art.intel.soft.utils;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.provider.Settings;
import android.view.View;

import com.google.android.material.snackbar.BaseTransientBottomBar;
import com.google.android.material.snackbar.Snackbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import art.intel.soft.R;

public class PermissionHelper {
    public static final int REQUEST_STORAGE_PERMISSION = 1;
    @SuppressLint("InlinedApi")
    public static final String[] WRITE_GALLERY_PERMISSION = new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                                                                         Manifest.permission.READ_MEDIA_IMAGES};
    @SuppressLint("InlinedApi")
    public static final String[] READ_GALLERY_PERMISSION = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE,
                                                                        Manifest.permission.READ_MEDIA_IMAGES};
    private static final String TAG = PermissionHelper.class.getName();

    public static void requestPermission(AppCompatActivity activity, String permission, int requestCode) {
        ActivityCompat.requestPermissions(activity, new String[]{permission}, requestCode);
    }

    public static void requestPermission(AppCompatActivity activity, String[] permissions, int requestCode) {
        ActivityCompat.requestPermissions(activity, permissions, requestCode);
    }

    public static boolean hasPermission(AppCompatActivity activity, String permission) {
        return ContextCompat.checkSelfPermission(activity, permission) == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean hasPermission(AppCompatActivity activity, String[] permissions) {
        for (String p : permissions) {
            if (!hasPermission(activity, p)) {
                return false;
            }
        }
        return true;
    }

    public static void showSettingsSnackbar(AppCompatActivity activity, View rootView) {
        Snackbar snackbar = Snackbar.make(
                rootView,
                activity.getText(R.string.gallery_error_miss_permission),
                BaseTransientBottomBar.LENGTH_INDEFINITE);
        snackbar.setAction(R.string.gallery_snack_bar_settings, (v -> startAppSettingsActivity(activity)));
        snackbar.show();
    }

    public static void startAppSettingsActivity(AppCompatActivity activity) {
        Intent intent = new Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", activity.getPackageName(), null);
        intent.setData(uri);
        activity.startActivity(intent);
    }
}
