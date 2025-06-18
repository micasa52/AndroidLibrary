package com.ismael.milibreriautilidades;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.widget.Toast;

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
 *
 * TODO Es aconsejable implementar en la activity el método para el control de la no aceptación:
 * @Override public void onRequestPermissionsResult(
 *      int requestCode,
 *      @NonNull String[] permissions,
 *      @NonNull int[] grantResults)
 *
 * ************************************************************************************************/

public class GestorPermisos {
    private final String TAG_INFO = "TAG_GestorPermisos";

    public static int REQUEST_CAMERA_PERMISSION = 100;
    public static int REQUEST_LECTURA_PERMISSION = 200;
    public static int REQUEST_ESCRITURA_PERMISSION = 300;

    /**
     * Pide el permiso de uso de la cámara de fotos
     * @param activity Activity para obtener el Contexto de la aplicación.
     * @return no devuelve nada (de momento)
     */
    public static void obtenerPermisoCamara(Activity activity){
        String msn = "";
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
            Toast.makeText(activity.getApplicationContext(), msn, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(activity.getApplicationContext(), msn, Toast.LENGTH_SHORT).show();
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
                Toast.makeText(activity.getApplicationContext(), msn, Toast.LENGTH_SHORT).show();
            }
        }
    }
}
