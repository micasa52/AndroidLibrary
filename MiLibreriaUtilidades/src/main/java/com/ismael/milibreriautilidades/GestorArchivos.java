// Dentro de MiLibreriaUtilidades > java > com.tudominio.milibreriautilidades
// Archivo: GestorArchivos.java
package com.ismael.milibreriautilidades;

import android.app.Activity;
import android.content.Context;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.text.style.BulletSpan;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.Objects;

import androidx.core.content.FileProvider;

/**************************************************************************************************
 * Fecha: 09-06-2025
 * Autor: Ismael Galán Fernández
 * Comentario:
 * Clase que contiene la gestión de escritura lectura de archivos en:
 * 1. Memoria interna del ámbito de la aplicación mediante File para SDK < Q
 * 2. Memoria interna del ámbito de la aplicación para SDK >= Q
 * 3. Memoria externa del ámbito de la aplicación mediante File para SDK < Q
 * 4. Memoria externa del ámbito de la aplicación para SDK >= Q
 * 5. Memoria externa fuera del ámbito de la aplicación
 *
 * ************************************************************************************************/
public class GestorArchivos {
    private static final String TAG_INFO = "TAG_GestorArchivos";

    private static String msn = "";

    /**
     * Guarda texto en un archivo dentro del almacenamiento interno específico de la app.
     * "data/data/com.ismael.librerias/files/" "/data/user/0/com.ismael.libreras/files/MiArchivo.txt"
     * @param activity      Contexto de la aplicación.
     * @param nombreArchivo Nombre del archivo a crear/sobrescribir.
     * @param subCarpeta    Nombre de la subcarpeta a crear/acceder dentro del ámbito de la app.
     * @param contenido     Texto a guardar.
     * @return true si se guardó correctamente, false en caso contrario.
     */
    public static boolean guardarTextoInterno(Activity activity, String subCarpeta, String nombreArchivo, String contenido) {
        String msn = "";
        if (activity == null || nombreArchivo == null || nombreArchivo.isEmpty() || subCarpeta == null ||
                subCarpeta.isEmpty() || contenido == null || contenido.isEmpty()) {
            Log.e(TAG_INFO, "Parámetros inválidos para guardarTextoInterno.");
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            FileOutputStream fos = null;
            try {

                // Creamos la subcarpeta
                File carpetaFile = activity.getFilesDir();                              // Dirección del ámbito interno de la app
//                File carpetaFile = new File(activity.getFilesDir(), subCarpeta);      // Carpeta creada dentro del ámbito interno de la app
                boolean okExists = carpetaFile.exists();
                if (!okExists)
//            okExists = carpetaFile.mkdir();      // Vale solo para crear una carpeta de cada vez
                    okExists = carpetaFile.mkdirs();        // Vale para crear una o varias carpetas consecutivas

                if (okExists) {
                    // Context.MODE_PRIVATE: Si el archivo ya existe, se sobreescribe.
                    // Context.MODE_APPEND: Si el archivo ya existe, se añade la final.
//            fos = context.openFileOutput(nombreArchivo, Context.MODE_PRIVATE);    // Archivo directo en el ámbito interno de la app sin carpetas
                    String dirArchivo = carpetaFile + File.separator + nombreArchivo;
                    fos = new FileOutputStream(dirArchivo);                                 // Archivo dentro de carpetas en el interno ámbito de la app
                    fos.write(contenido.getBytes(StandardCharsets.UTF_8));
                    Log.d(TAG_INFO, "Archivo guardado en: " + activity.getApplicationContext().getFilesDir().getAbsolutePath() + "/" + nombreArchivo);
                    // Considera no mostrar Toasts directamente desde una librería,
                    // es mejor devolver un resultado y que la app decida cómo notificar al usuario.
                    // Toast.makeText(context, "Texto guardado desde librería", Toast.LENGTH_SHORT).show();
                    return true;
                }
            } catch (IOException e) {
                Log.e(TAG_INFO, "Error al guardar el archivo: " + nombreArchivo, e);
                return false;
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        Log.e(TAG_INFO, "Error al cerrar FileOutputStream", e);
                    }
                }
            }
        } else {
            // Si VERSION.SDK_INT < VERSION_CODES.Q
            // Este File = Activity.getApplicationContext().getFilesDir() abre el archivo en la zona privada e interna de la app
            File carpetaFile = new File(activity.getApplicationContext().getFilesDir(), subCarpeta);
//         File carpetaFile = activity.getApplicationContext().getFilesDir();
            boolean okCarpeta = false;
            okCarpeta = carpetaFile.exists();

            if (!okCarpeta)
                okCarpeta = carpetaFile.mkdirs();

            if (okCarpeta) {
                FileOutputStream fos = null;
                try {
                    // Context.MODE_PRIVATE: Si el archivo ya existe, se sobreescribe.
                    // Context.MODE_APPEND: Si el archivo ya existe, se añade la final.
//                    fos = activity.openFileOutput(nombreArchivo, Context.MODE_PRIVATE);         // Context.openFileOutput abre el archivo en la zona privada interna de la app
                    fos = new FileOutputStream(new File(carpetaFile, nombreArchivo));           // Abre el archivo en la carpeta creada dentro de la zona privada interna de la app
                    byte[] bytes = contenido.getBytes(StandardCharsets.UTF_8);
                    fos.write(bytes); // Usar UTF-8 es buena práctica

                    msn = "Texto guardado en: " + activity.getApplicationContext().getFilesDir() + "/" + nombreArchivo;
                    Toast.makeText(activity.getApplicationContext(), msn, Toast.LENGTH_SHORT).show();
                    Log.i(TAG_INFO, msn);

                    // Opcional: Limpiar el EditText después de guardar
                    // txtDocumento.setText("");
                    return true;
                } catch (IOException ioe) {
                    msn = "Error al guardar el archivo: " + nombreArchivo;
                    Log.e(TAG_INFO, msn, ioe);
                    Toast.makeText(activity.getApplicationContext(), msn, Toast.LENGTH_SHORT).show();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException ioe) {
                            msn = "Error al cerrar FileOutputStream";
                            Log.e(TAG_INFO, msn, ioe);
                        }
                    }
                }
            }
        }
        return false;
    }

    /**
     * Lee texto de un archivo dentro del almacenamiento interno específico de la app.
     *
     * @param context       Contexto de la aplicación.
     * @param nombreArchivo Nombre del archivo a leer.
     * @param subCarpeta    Nombre de la subCarpeta a acceder dentro del ámbito de la app.
     * @return texto si se leyó correctamente, "" en caso contrario.
     */
    public static String leerTextoInterno(Context context, String subCarpeta, String nombreArchivo) {
        String texto = "";
        if (context == null || nombreArchivo == null || nombreArchivo.isEmpty() ||
                subCarpeta == null || subCarpeta.isEmpty()) {
            Log.e(TAG_INFO, "Parámetros inválidos para guardarTextoInterno.");
            return "";
        }

        FileInputStream fis = null;
        try {
            // Creamo la subcarpeta
            File carpetaFile = new File(context.getFilesDir(), subCarpeta);
            boolean okExists = carpetaFile.exists();

            if (okExists) {

//                fis = context.openFileInput(nombreArchivo);                       // Desde la carpeta directamente del ámbito interno de la app
                fis = new FileInputStream(new File(carpetaFile, nombreArchivo));    // Desde la/s carpeta/s creada/s en el ámbito interno de la app

                // Para versiones iguales o superiores a la
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    byte[] arrayText = fis.readAllBytes();

                    Log.d(TAG_INFO, "Archivo leido desde: " + context.getFilesDir().getAbsolutePath() + "/" + nombreArchivo);
                    // Considera no mostrar Toasts directamente desde una librería,
                    // es mejor devolver un resultado y que la app decida cómo notificar al usuario.
                    // Toast.makeText(context, "Texto guardado desde librería", Toast.LENGTH_SHORT).show();

                    texto = new String(arrayText, StandardCharsets.UTF_8);
                    return texto;
                } else
                // Para versiones inferiores a la Q
                if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
                    ByteArrayOutputStream baos = new ByteArrayOutputStream();
                    byte[] arrayText = new byte[1024];   // buffer para leer trozos de 1Kb
                    int n = -1;

                    while ((n = fis.read(arrayText)) != -1) {
                        baos.write(arrayText, 0, n);
                    }

                    // Convierte los bytes acumulados en ByteArrayOutputStream a un String
                    return baos.toString(StandardCharsets.UTF_8.name());
                }
            } else {
                String msn = "No existe la carpeta pedida";
                return "Error: " + msn;
            }
        } catch (IOException e) {
            String msn = "Error al guardar el archivo: " + nombreArchivo;
            Log.e(TAG_INFO, msn, e);
            return "Error: " + msn;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    Log.e(TAG_INFO, "Error al cerrar FileOutputStream", e);
                }
            }
        }
        String msn = "Error impreciso";
        return "Error: " + msn;
    }

    /**
     * Guarda texto en un archivo dentro del almacenamiento externo específico de la app.
     * "emulated/0/Android/data/com.ismael.librerias/files/"  "/storage/emulated/0/Android/data/com.ismael.libreras/files/Download/MiCarpeta"
     * @param activity      Contexto de la aplicación.
     * @param subCarpeta    Nombre de la subcarpeta a crear/acceder dentro del ámbito de la app.
     * @param nombreArchivo Nombre del archivo a crear/sobrescribir.
     * @param contenido     Texto a guardar.
     * @return true si se guardó correctamente, false en caso contrario.
     */
    public static boolean guardarTextoExterno(Activity activity, String subCarpeta, String nombreArchivo, String contenido) {
        String msn = "";
        if (activity == null || nombreArchivo == null || nombreArchivo.isEmpty() || subCarpeta == null ||
                subCarpeta.isEmpty() || contenido == null || contenido.isEmpty()) {
            Log.i(TAG_INFO, "Parámetros inválidos para guardarTextoInterno.");
            return false;
        }

        FileOutputStream fos = null;

        try {
            // Creamos la subcarpeta
//                File carpetaFile = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            File carpetaFile = new File(activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), subCarpeta);
            boolean okExists = carpetaFile.exists();
            if (!okExists)
//            okExists = carpetaFile.mkdir();      // Vale solo para crear una carpeta de cada vez
                okExists = carpetaFile.mkdirs();        // Vale para crear una o varias carpetas consecutivas

            if (okExists) {
                // Context.MODE_PRIVATE: Si el archivo ya existe, se sobreescribe.
                // Context.MODE_APPEND: Si el archivo ya existe, se añade la final.
//            fos = context.openFileOutput(nombreArchivo, Context.MODE_PRIVATE);    // Archivo directo en el ámbito interno de la app sin carpetas
                String dirArchivo = carpetaFile + File.separator + nombreArchivo;
                fos = new FileOutputStream(dirArchivo);                                 // Archivo dentro de carpetas en el interno ámbito de la app
                fos.write(contenido.getBytes(StandardCharsets.UTF_8));
                Log.i(TAG_INFO, "Archivo guardado en: " + activity.getApplicationContext().getFilesDir().getAbsolutePath() + "/" + nombreArchivo);
                // Considera no mostrar Toasts directamente desde una librería,
                // es mejor devolver un resultado y que la app decida cómo notificar al usuario.
                // Toast.makeText(context, "Texto guardado desde librería", Toast.LENGTH_SHORT).show();
                return true;
            }
        } catch (IOException e) {
            Log.e(TAG_INFO, "Error al guardar el archivo: " + nombreArchivo, e);
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    Log.e(TAG_INFO, "Error al cerrar FileOutputStream", e);
                }
            }
        }

        return false;
    }

    /**
     * Lee texto de un archivo dentro del almacenamiento externo del ámbito de la app.
     *
     * @param context       Contexto de la aplicación.
     * @param nombreArchivo Nombre del archivo a leer.
     * @param subCarpeta    Nombre de la subCarpeta a acceder dentro del ámbito de la app.
     * @return texto si se leyó correctamente, "" en caso contrario.
     */
    public static String leerTextoExterno(Context context, String subCarpeta, String nombreArchivo) {
        if (context == null || nombreArchivo == null || nombreArchivo.isEmpty() ||
                subCarpeta == null || subCarpeta.isEmpty()) {
            msn = "Parámetros inválidos para guardarTextoInterno.";
            Log.i(TAG_INFO, msn);
            return "";
        }

        String texto = "";
        FileInputStream fis = null;
        try {
            // Creamo la subcarpeta
//            File carpetaFile = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            File carpetaFile = new File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), subCarpeta);
            boolean okExists = carpetaFile.exists();

            if (okExists) {
//                fis = context.openFileInput(nombreArchivo);                       // Desde la carpeta directamente del ámbito interno de la app
                fis = new FileInputStream(new File(carpetaFile,nombreArchivo));   // Desde la/s carpeta/s creada/s en el ámbito interno de la app
                // Para versiones iguales o superiores a la
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    byte[] arrayText = fis.readAllBytes();

                    Log.d(TAG_INFO, "Archivo leido desde: " + context.getFilesDir().getAbsolutePath() + "/" + nombreArchivo);
                    // Considera no mostrar Toasts directamente desde una librería,
                    // es mejor devolver un resultado y que la app decida cómo notificar al usuario.
                    // Toast.makeText(context, "Texto guardado desde librería", Toast.LENGTH_SHORT).show();

                    texto = new String(arrayText, StandardCharsets.UTF_8);
                    return texto;
                }else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){
                    FileReader fr = null;
                    try{
                        fr = new FileReader(new File(carpetaFile,nombreArchivo));
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] arrayText = new byte[1024];   // buffer para leer trozos de 1Kb
                        int n = -1;

                        while ((n = fis.read(arrayText)) != -1) {
                            baos.write(arrayText, 0, n);
                        }

                        // Convierte los bytes acumulados en ByteArrayOutputStream a un String
                        texto = baos.toString(StandardCharsets.UTF_8.name());
                        return texto;
                    }catch (IOException ioe){
                        msn = "Error al guardar el archivo: "+ nombreArchivo;
                        Log.e(TAG_INFO, msn, ioe);
                        return "Error: " + msn;
                    }finally {
                        if(fr != null){
                            fr.close();
                        }
                    }
                }
            }
        } catch (IOException e) {
            msn = "Error al guardar el archivo: " + nombreArchivo;
            Log.e(TAG_INFO, msn, e);
            return "Error: " + msn;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    Log.e(TAG_INFO, "Error al cerrar FileOutputStream", e);
                }
            }
        }
        return msn;
    }

    /**
     * Guarda texto en un archivo dentro del almacenamiento externo específico fuera del ámbito de la app.
     * "Environment.getExternalStorageDirectory()"  -
     * @param activity      Contexto de la aplicación.
     * @param subCarpeta    Nombre de la subcarpeta a crear/acceder dentro del ámbito de la app.
     * @param nombreArchivo Nombre del archivo a crear/sobrescribir.
     * @param contenido     Texto a guardar.
     * @return true si se guardó correctamente, false en caso contrario.
     */
    public static boolean guardarTextoExternoFueraAmbito(Activity activity, String subCarpeta, String nombreArchivo, String contenido) {
        if (activity == null || nombreArchivo == null || nombreArchivo.isEmpty() || subCarpeta == null ||
                subCarpeta.isEmpty() || contenido == null || contenido.isEmpty()) {
            Log.e(TAG_INFO, "Parámetros inválidos para guardarTextoExterno.");
            return false;
        }

        FileOutputStream fos = null;

        try {
            // Creamos la subcarpeta
            File carpetaFile = new File(Environment.getExternalStorageDirectory(), subCarpeta);
            boolean okExists = carpetaFile.exists();
            if (!okExists)
//            okExists = carpetaFile.mkdir();      // Vale solo para crear una carpeta de cada vez
                okExists = carpetaFile.mkdirs();        // Vale para crear una o varias carpetas consecutivas

            if (okExists) {
                // Context.MODE_PRIVATE: Si el archivo ya existe, se sobreescribe.
                // Context.MODE_APPEND: Si el archivo ya existe, se añade la final.
//            fos = context.openFileOutput(nombreArchivo, Context.MODE_PRIVATE);    // Archivo directo en el ámbito interno de la app sin carpetas
                String dirArchivo = carpetaFile + File.separator + nombreArchivo;
                fos = new FileOutputStream(dirArchivo);                                 // Archivo dentro de carpetas en el interno ámbito de la app
                fos.write(contenido.getBytes(StandardCharsets.UTF_8));

                msn = "Archivo guardado en: " + activity.getApplicationContext().getFilesDir().getAbsolutePath() + "/" + nombreArchivo;
                Log.i(TAG_INFO, msn);
                // Considera no mostrar Toasts directamente desde una librería,
                // es mejor devolver un resultado y que la app decida cómo notificar al usuario.
                // Toast.makeText(context, "Texto guardado desde librería", Toast.LENGTH_SHORT).show();
                return true;
            }
        } catch (IOException e) {
            msn = "Error al guardar el archivo: " + nombreArchivo;
            Log.e(TAG_INFO, msn, e);
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    msn = "Error al cerrar FileOutputStream";
                    Log.e(TAG_INFO, msn, e);
                }
            }
        }

        return false;
    }

    /**
     * Guarda una imágen en un archivo dentro del almacenamiento interno específico de la app.
     *
     * @param bitmap        Imagen a guardar.
     * @param nombreCarpeta Nombre de la carpeta a crear.
     * @param nombreArchivo Nombre del archivo a crear/sobrescribir.
     * @return true si se guardó correctamente, false en caso contrario.
     */
                nombreArchivo == null || nombreArchivo.isEmpty()) {
            Log.i(TAG_INFO, "Error de parámetros. No validos");
            return false;
        }


        return false;
    }

    // public static boolean guardarBitmapEnInterno(Context context, Bitmap bitmap, String nombre) { ... }

    // ################ GESTION DE IMAGENES ####################
    /**
     * Obtiene un File con la dirección del nombreArchivo en una subcarpeta en la zona externa del ámbito de la app
     * activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
     *
     * @param activity       Contexto de la aplicación.
     * @param subCarpeta Nombre de la carpeta a crear.
     * @param nombreArchivo Nombre del archivo a crear/sobrescribir.
     * @return true si se guardó correctamente, false en caso contrario.
     */
    public static File obtenerFilesDirExterno(Activity activity, String subCarpeta, String nombreArchivo) throws IOException{
        if(nombreArchivo == null || nombreArchivo.isEmpty()) {
            // Crea un nombre de archivo de imagen único
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            nombreArchivo = "JPEG_" + timeStamp + "_";
        }

        File dirCarpeta = new File(activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES), subCarpeta);
        // ############### ESTO NO ESTÁ PERMITIDO HACERLO ##############
//        File dirCarpeta = new File(Environment.DIRECTORY_PICTURES, "BonolotoImagenes");

        // Asegurese de que el directorio exista (getExternalFilesDir lo crea si es necesario en al mayoría de los casos)
        boolean okCarpeta = dirCarpeta.exists();
        if (dirCarpeta != null && !okCarpeta) {
            okCarpeta = dirCarpeta.mkdirs();
        }

        // Si nombreArchivo llega nulo o vacío, se genera el nombre con el timeStamp
        File imagenFile = null;
        // Aquí se añade al nombre un número aleatorio para hacer archivos con nombres diferentes
        if (okCarpeta && nombreArchivo.contains("JPEG")) {
            imagenFile = File.createTempFile(
                    nombreArchivo,       // prefijo
                    ".jpg",             // sufijo
                    dirCarpeta          // directorio
            );
        }else if(okCarpeta){
            imagenFile = new File(dirCarpeta, nombreArchivo + ".jpg");
        }

        return imagenFile;
    }

    /**
     * Obtiene un File con la dirección del nombreArchivo en una subcarpeta en la zona externa del ámbito de la app
     * Environment.getExternalStorageDirectory()
     *
     * @param activity       Contexto de la aplicación.
     * @param subCarpeta Nombre de la carpeta a crear.
     * @param nombreArchivo Nombre del archivo a crear/sobrescribir.
     * @return true si se guardó correctamente, false en caso contrario.
     */
    public static File obtenerDirExterno(Activity activity, String subCarpeta, String nombreArchivo) throws IOException{
        if(nombreArchivo == null || nombreArchivo.isEmpty()) {
            // Crea un nombre de archivo de imagen único
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            nombreArchivo = "JPEG_" + timeStamp + "_";
        }

        File dirCarpeta = new File(Environment.getExternalStorageDirectory(), subCarpeta);
        // ############### ESTO NO ESTÁ PERMITIDO HACERLO ##############
//        File dirCarpeta = new File(Environment.DIRECTORY_PICTURES, "BonolotoImagenes");

        // Asegurese de que el directorio exista (getExternalFilesDir lo crea si es necesario en al mayoría de los casos)
        boolean okCarpeta = dirCarpeta.exists();
        if (dirCarpeta != null && !okCarpeta) {
            okCarpeta = dirCarpeta.mkdirs();
        }

        // Si nombreArchivo llega nulo o vacío, se genera el nombre con el timeStamp
        File imagenFile = null;
        // Aquí se añade al nombre un número aleatorio para hacer archivos con nombres diferentes
        if (okCarpeta && nombreArchivo.contains("JPEG")) {
            imagenFile = File.createTempFile(
                    nombreArchivo,       // prefijo
                    ".jpg",             // sufijo
                    dirCarpeta          // directorio
            );
        }else if(okCarpeta){
            imagenFile = new File(dirCarpeta, nombreArchivo + ".jpg");
        }

        return imagenFile;
    }

    /**
     * Obtiene una Uri con la dirección del nombreArchivo en una subcarpeta en la zona externa del ámbito de la app
     * Esa carpeta tiene que tenerla creada en archivo "file_paths.xml" para que pueda ser creada y usada mediante
     * el fileprovider designado en el archivo "AndroidMaifest.xml"
     *
     * @param activity Contexto de la aplicación.
     * @param fileFoto objeto File con la dirección del archivos imagen guardado.
     * @return true si se guardó correctamente, false en caso contrario.
     */
    public static Uri obtenerUriExterna(Activity activity, File fileFoto) throws IOException{
        String msn = "";

        // Continuar solo si el File fue creado exitosamente
        if (fileFoto != null) {
            // Obtener el URI para el archivo usando FileProvider
            // La autoridad debe coincidir con la declarada en el AndroidManifest.xml
            // y con el aplicationId
            String autoridad = activity.getApplicationContext().getPackageName() + ".fileprovider";
            Uri uriImagenArchivo = FileProvider.getUriForFile(Objects.requireNonNull(activity.getApplicationContext()), autoridad, fileFoto);

            return uriImagenArchivo;
        }

        return null;
    }

    /**
     * Cambia de tamaño una foto Bitmap para mostrarlar en el imageView
     *
     * @param originalImage Imagen de tamaño real.
     * @param ratioWidth ratio de multiplicación del ancho de la imagen.
     * @param ratioHeight ratio de multiplicación de la altura de la imagen.
     * @return Bitmap nuevo de la imagen escalada a otro tamaño. y null si no recibe imagen.
     */
    // Ejemplo de cómo escalar un bitmap (simplificado)
    public static Bitmap scaleBitmap(Bitmap originalImage, float ratioWidth, float ratioHeight) {
        if (originalImage == null) return null;

        int targetWidth = Math.round(originalImage.getWidth() * ratioWidth);
        int targetHeight = Math.round(originalImage.getHeight() * ratioHeight);
        return Bitmap.createScaledBitmap(originalImage, targetWidth, targetHeight, true);
    }

    // ############################ Setters y Getters #############################
    public void setMsn(String mensaje){
        msn = mensaje;
    }

        return msn;
    }
}
