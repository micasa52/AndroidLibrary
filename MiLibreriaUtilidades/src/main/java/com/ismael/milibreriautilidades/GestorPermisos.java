package com.ismael.milibreriautilidades;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;

import androidx.activity.result.ActivityResult;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**************************************************************************************************
 * Fecha: 10-06-2025
 * Autor: Ismael Galán Fernández
 * Comentario:
 * Clase que contiene la obtención de los permisos de:
 * 1. CAMARA -> Hace falta el permiso en el Manifest.xml
 * 2. LECTURA -> No hace falta para versiones superiores o iguales a Q
 * 3. ESCIRUTRA -> No hace falta para versiones superiores o iguales a Q
 * 4. ACCESO TOTAL A ARCHIVOS -> Solo para versiones superiores o iguales a R
 *
 * TODO Es aconsejable implementar en la activity el método para el control de la no aceptación:
 * @Override public void onRequestPermissionsResult(
 *      int requestCode,
 *      @NonNull String[] permissions,
 *      @NonNull int[] grantResults)
 *
 * ************************************************************************************************/

public class GestorPermisos {
    private static final String TAG_INFO = "TAG_GestorPermisos";

    public static int REQUEST_CAMERA_PERMISSION = 100;
    public static int REQUEST_LECTURA_PERMISSION = 200;
    public static int REQUEST_ESCRITURA_PERMISSION = 300;

    private static String msn = "";

    /**
     * Pide el permiso de uso de la cámara de fotos
     * @param activity Activity para obtener el Contexto de la aplicación.
     * @return no devuelve nada (de momento)
     */
    public static void obtenerPermisoCamara(Activity activity){
        // Permiso de Cámara
        if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), android.Manifest.permission.CAMERA)
                != PackageManager.PERMISSION_GRANTED) {
            // Permiso no concedido, solicitarlo
            ActivityCompat.requestPermissions(activity,
                    new String[]{android.Manifest.permission.CAMERA},
                    REQUEST_CAMERA_PERMISSION);
        } else {
            // Permiso ya concedido, proceder a lanzar la cámara
            msn = "Permiso de Cámara concedido";
            Log.i(TAG_INFO, msn);
        }
    }

    /**
     * Pide el permiso de lectura de archivos (solo para versiones menores de Q)
     * @param activity Activity para obtener el Contexto de la aplicación.
     * @return no devuelve nada (de momento)
     */
    public static void obtenerPermisoLectura(Activity activity) {
        String msn = "";
        // Permiso de Lectura
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permiso no concedido, solicitarlo
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.READ_EXTERNAL_STORAGE},
                        REQUEST_LECTURA_PERMISSION);
            } else {
                // Permiso ya concedido para la lectura de archivos
                msn = "Permiso de Lectura concedido";
                Log.i(TAG_INFO, msn);
            }
        }
    }

    /**
     * Pide el permiso de escritura de archivos (solo para versiones menores de Q)
     * @param activity Activity para obtener el Contexto de la aplicación.
     * @return no devuelve nada (de momento)
     */
    public static void obtenerPermisoEscritura(Activity activity){
        String msn = "";
        // Permiso de Escritura
        if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(activity.getApplicationContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                // Permiso no concedido, solicitarlo
                ActivityCompat.requestPermissions(activity,
                        new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE},
                        REQUEST_ESCRITURA_PERMISSION);
            } else {
                // Permiso ya concedido para la Escritura de archivos
                msn = "Permiso de Escritura concedido";
                Log.i(TAG_INFO, msn);
            }
        }
    }

    /**
     * Pide el permiso de acceso total a los archivos (solo para versiones mayores de R)
     * @param result ActivityResult para obtener el resultCode del lanzamiento del permiso.
     * Se puede recoger un mensaje mediante el getter "getMsn()".
     */
    // Para solicitar el permiso MANAGE_EXTERNAL_STORAGE (a partir de Android 11)
    public static void comprobarPermisoEscritura(ActivityResult result) {
        int resultCode = result.getResultCode();
        if(resultCode == Activity.RESULT_OK) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                if (Environment.isExternalStorageManager()) {
                    // Permiso otorgado
                    msn = "Permiso de acceso a todo el almacenamiento concedido";
                    Log.i(TAG_INFO, msn);
                } else {
                    // Permiso denegado
                    msn = "Permiso de acceso a todo el almacenamiento denegado";
                    Log.i(TAG_INFO, msn);
                }
            }
        }else{
            msn = "Permiso de acceso a todo el almacenamiento denegado";
            Log.i(TAG_INFO, msn);
        }
    }

    /**
     * Pide el Intent que lanzará la ventana de activación del permiso de acceso total a los archivos
     * (solo para versiones mayores de R)
     * @param activity Activity para obtener el Contexto de la aplicación.
     * @return Intent para lanzar el la ventana de activación del permiso
     *      Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION
     * Se puede recoger un mensaje mediante el getter "getMsn()".
     */
    @RequiresApi(api = Build.VERSION_CODES.R)
    public static Intent obtenerTotalAccessPermisionIntent(Activity activity) {
        Intent intent = null;
        // Solicitar permisos para almacenamiento externo
        if (!Environment.isExternalStorageManager()) {
            intent =new  Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
            intent.setData(Uri.fromParts("package", activity.getPackageName(), null));
        }else{
            msn = "Permiso de acceso a todo el almacenamiento concedido";
            Log.i(TAG_INFO, msn);
        }
        return intent;
    }

    // ####################### Setters y Getters #######################
    public static String getMsn() {
        return msn;
    }
}
