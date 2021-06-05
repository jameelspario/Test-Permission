package com.spario.testpermission;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class PermissionManager {

    public static String[] PERMISSIONS = {
            android.Manifest.permission.READ_CONTACTS,
            android.Manifest.permission.WRITE_CONTACTS,
            android.Manifest.permission.WRITE_EXTERNAL_STORAGE,
            android.Manifest.permission.READ_SMS,
            android.Manifest.permission.CAMERA
    };

    private OnPermissionGranted listener1;
    private OnPermissionDenied listener2;

    public static final int REQUEST_ID_MULTIPLE_PERMISSIONS = 1;
    private Activity context;

    private List<String> listPermissionsNeeded = new ArrayList<>();

    public PermissionManager(Activity context) {
        this.context = context;
    }

    public void setListener(OnPermissionGranted listener1, OnPermissionDenied listener2){
        this.listener1 = listener1;
        this.listener2 = listener2;
    }

    public boolean hasPermissions(String... permissions) {
        if (context != null && permissions != null) {
            int result;
            for (String permission : permissions) {
                result = ContextCompat.checkSelfPermission(context, permission);
                if (result != PackageManager.PERMISSION_GRANTED) {
                    listPermissionsNeeded.add(permission);
                }
            }

            if(!listPermissionsNeeded.isEmpty()){
                ActivityCompat.requestPermissions(context, listPermissionsNeeded.toArray(new String[listPermissionsNeeded.size()]),REQUEST_ID_MULTIPLE_PERMISSIONS);
                return false;
            }
        }

        return true;
    }

    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_ID_MULTIPLE_PERMISSIONS: {
                Map<String, Integer> perms = new HashMap<>();

                for (String permission : permissions) {
                    perms.put(permission, PackageManager.PERMISSION_GRANTED);
                }

                if (grantResults.length > 0) {
                    for (int i = 0; i < permissions.length; i++)
                        perms.put(permissions[i], grantResults[i]);

                    boolean allPermissionsGranted = true;
                    for (String permission1 : permissions) {
                        allPermissionsGranted = allPermissionsGranted && (perms.get(permission1) == PackageManager.PERMISSION_GRANTED);
                    }

                    if (allPermissionsGranted) {
                        Log.d("TAG", "onRequestPermissionsResult: all permissions granted");
                        if(listener1!=null) listener1.permissionGranted();
                    } else {
                        for (String permission2 : perms.keySet())
                            if (perms.get(permission2) == PackageManager.PERMISSION_GRANTED)
                                perms.remove(permission2);

                        StringBuilder message = new StringBuilder("The app has not been granted permissions:\n\n");
                        for (String permission : perms.keySet()) {
                            message.append(permission);
                            message.append("\n");
                        }
                        message.append("\nHence, it cannot function properly." +
                                "\nPlease consider granting it this permission in phone Settings.");

                        if(listener2!=null) listener2.permissionDenied(message.toString());

                    }
                }
            }
        }
    }

    public interface OnPermissionDenied{
        void permissionDenied(String message);
    }

    public interface OnPermissionGranted{
        void permissionGranted();
    }

    public void dialog(String message){
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setTitle("Permission Required")
                .setMessage(message)
                .setPositiveButton(android.R.string.ok, (dialog, id) -> dialog.cancel());
        AlertDialog alert = builder.create();
        alert.show();
    }



}
