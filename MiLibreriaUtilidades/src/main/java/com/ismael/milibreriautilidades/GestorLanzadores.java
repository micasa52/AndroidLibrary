package com.ismael.milibreriautilidades;

import android.app.Activity;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
/**************************************************************************************************
 * Fecha: 10-06-2025
 * Autor: Ismael Galán Fernández
 * Comentario:
 * Clase que contiene el proceso lanzamiento de:
 * 1. CÁMARA
 * 2. ESCAMER DE CÓDIGOS DE BARRAS Y QR
 * 3. SELECTOR DE IMÁGENES
 * 4. SELECTOR DE ARCHIVOS
 * ************************************************************************************************/
public class GestorLanzadores {
    private final static String TAG_INFO = "TAG_GestorLanzadores";

    private static Bitmap bitmap;

    public static void larzarCamara(Activity activity, String subCarpeta, String nombreImagen){
        /*
        Toast.makeText(activity, "Foto guardada en: " + rutaArchivoActual, Toast.LENGTH_SHORT).show();
        Log.i(TAG_INFO, "Foto guardada exitosamente en: " + uriImagenArchivo.toString());
        Log.i(TAG_INFO, "Ruta absoluta del archivo: " + rutaArchivoActual);

        // Mostrar la imagen en el ImageView
        // Puedes cargarla directamente desde la ruta o el URI
        bitmap = BitmapFactory.decodeFile(rutaArchivoActual);

        if (bitmap != null) {
            // Antes de llamar a setImageBitmap:
            // int targetW = imageView.getWidth(); // Obtener después de que el layout esté medido
            // int targetH = imageView.getHeight();
            // Si targetW o targetH son 0, usa dimensiones predeterminadas o las de la pantalla.
            // Por ahora, un ejemplo con valores fijos:
            float displayWidth = 0.5f; // Un tamaño razonable para la vista previa
            float displayHeight = 0.5f;
            Bitmap scaledBitmap = scaleBitmap(bitmap, displayWidth, displayHeight);
            if (scaledBitmap != null) {
                Log.i(TAG_INFO, "Bitmap dimensiones: " + scaledBitmap.getWidth() + "x" + scaledBitmap.getHeight());
                imageView.setImageBitmap(scaledBitmap);
                Log.i(TAG_INFO, "Bitmap colocado en el imageView");
            } else {
                imageView.setImageBitmap(bitmap); // Fallback al original si el escalado falla
            }
        } else {
            // Aternativamente, si el decodeFile falla por alguna razón (raro si se guardó bien)
            // imageView.setImageURI(uriImagenArchivo);
            Log.i(TAG_INFO, "No se pudo decodificar el bitmap desde la ruta.");
        }

         */
    }

    // ###################### Setters y Getters #########################
    public void setBitmap(Bitmap bmp){
        this.bitmap = bmp;
    }

    public Bitmap getBitmap(){
        return this.bitmap;
    }
}
