package dev.trindadedev.blockode.utils;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.Settings;
import androidx.activity.result.ActivityResultLauncher;
import androidx.core.content.ContextCompat;
import java.util.List;

public class PermissionUtil {
  PermissionUtil() {}

  public interface Permission {
    void request();

    boolean check();
  }

  public static final class Storage implements Permission {
    private final Activity activity;
    private final ActivityResultLauncher<Intent> storageAllLauncher;
    private final ActivityResultLauncher<List<String>> storageReadWriteLauncher;

    public Storage(
        final Activity activity,
        final ActivityResultLauncher<Intent> storageAllLauncher,
        final ActivityResultLauncher<List<String>> storageReadWriteLauncher) {
      this.activity = activity;
      this.storageAllLauncher = storageAllLauncher;
      this.storageReadWriteLauncher = storageReadWriteLauncher;
    }

    @Override
    public boolean check() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        return Environment.isExternalStorageManager();
      } else {
        return ContextCompat.checkSelfPermission(
                    activity, Manifest.permission.READ_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED
            && ContextCompat.checkSelfPermission(
                    activity, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                == PackageManager.PERMISSION_GRANTED;
      }
    }

    @Override
    public final void request() {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
        requestStorageAll();
      } else {
        requestStorageReadWrite();
      }
    }

    public final void requestStorageAll() {
      if (!Environment.isExternalStorageManager()) {
        var intent = new Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
        intent.setData(Uri.parse("package:" + activity.getPackageName()));
        storageAllLauncher.launch(intent);
      }
    }

    public final void requestStorageReadWrite() {
      storageReadWriteLauncher.launch(
          List.of(
              Manifest.permission.READ_EXTERNAL_STORAGE,
              Manifest.permission.WRITE_EXTERNAL_STORAGE));
    }
  }
}
