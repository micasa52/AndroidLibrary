package com.ismael.librerias;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

import androidx.core.content.FileProvider;

public class GestorArchivos {
    private static final String TAG_CLASE = "librerias.TAG_GestorArchivos";

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
    public static boolean guardarTexto(Activity activity, String subCarpeta, String nombreArchivo, String contenido, int memo, String tipoArchivo) {
        if (activity == null || nombreArchivo == null || nombreArchivo.isEmpty() ||
                contenido == null || contenido.isEmpty() || memo == 0 || tipoArchivo == null || tipoArchivo.isEmpty()) {
            msn = "Parámetros inválidos para guardarTextoAmbito.";
            Log.e(TAG_CLASE, msn);
            return false;
        }
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            FileOutputStream fos = null;
            try {
                // Creamos la subcarpeta
                File carpetaRaiz = obtenerCarpetaArchivos(activity, memo, tipoArchivo);
                File carpetaFile = new File(carpetaRaiz, subCarpeta);      // Dirección del ámbito interno de la app
//                File carpetaFile = new File(activity.getFilesDir(), subCarpeta);                    // Carpeta creada dentro del ámbito interno de la app
                boolean okExists = carpetaFile.exists();
                if (!okExists)
//            okExists = carpetaFile.mkdir();      // Vale solo para crear una carpeta de cada vez
                    okExists = carpetaFile.mkdirs();        // Vale para crear una o varias carpetas consecutivas

                if (okExists) {
                    // Context.MODE_PRIVATE: Si el archivo ya existe, se sobreescribe.
                    // Context.MODE_APPEND: Si el archivo ya existe, se añade la final.
//                    fos = context.openFileOutput(nombreArchivo, Context.MODE_PRIVATE);    // Archivo directo en el ámbito interno de la app sin carpetas
                    File dirArchivo = new File(carpetaFile, nombreArchivo);                 // Archivo dentro de carpetas
                    fos = activity.getApplicationContext().openFileOutput(dirArchivo.getPath(), Context.MODE_PRIVATE);
                    fos.write(contenido.getBytes(StandardCharsets.UTF_8));

                    msn = "Archivo guardado en: " + dirArchivo;
                    Log.d(TAG_CLASE, msn);
                    // Considera no mostrar Toasts directamente desde una librería,
                    // es mejor devolver un resultado y que la app decida cómo notificar al usuario.
                    // Toast.makeText(context, "Texto guardado desde librería", Toast.LENGTH_SHORT).show();
                    return true;
                }
            } catch (IOException e) {
                msn = "Error al guardar el archivo: " + nombreArchivo;
                Log.e(TAG_CLASE, msn, e);
                return false;
            } finally {
                if (fos != null) {
                    try {
                        fos.close();
                    } catch (IOException e) {
                        msn = "Error al cerrar FileOutputStream";
                        Log.e(TAG_CLASE, msn, e);
                    }
                }
            }
        } else {
            // Si VERSION.SDK_INT < VERSION_CODES.Q
            // Este File = Activity.getApplicationContext().getFilesDir() abre el archivo en la zona privada e interna de la app
            File carpetaRaiz = obtenerCarpetaArchivos(activity, memo, tipoArchivo);
            File carpetaFile = new File(carpetaRaiz, subCarpeta);
//         File carpetaFile = activity.getApplicationContext().getFilesDir();
            boolean okCarpeta = carpetaFile.exists();

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
                    Log.i(TAG_CLASE, msn);

                    // Opcional: Limpiar el EditText después de guardar
                    // txtDocumento.setText("");
                    return true;
                } catch (IOException ioe) {
                    msn = "Error al guardar el archivo: " + nombreArchivo;
                    Log.e(TAG_CLASE, msn, ioe);
                    Toast.makeText(activity.getApplicationContext(), msn, Toast.LENGTH_SHORT).show();
                } finally {
                    if (fos != null) {
                        try {
                            fos.close();
                        } catch (IOException ioe) {
                            msn = "Error al cerrar FileOutputStream";
                            Log.e(TAG_CLASE, msn, ioe);
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
     * @param activity       Contexto de la aplicación.
     * @param subCarpeta    Nombre de la subCarpeta a acceder dentro del ámbito de la app.
     * @param nombreArchivo Nombre del archivo a leer.
     * @param memo           Modo de acceso a las diferentes memorias del dispositivo.
     * @param tipoArchivo    Tipo de archivo a guardar.
     * @return texto si se leyó correctamente, "mensaje de error" en caso contrario.
     */
    public static String leerTextoInterno(Activity activity, String subCarpeta, String nombreArchivo, int memo, String tipoArchivo) {
        String texto = "";
        if (activity == null || nombreArchivo == null || nombreArchivo.isEmpty() ||
                subCarpeta == null || subCarpeta.isEmpty()) {
            msn = "Parámetros inválidos para guardarTextoInterno.";
            Log.e(TAG_CLASE, msn);
            return "Error: " + msn;
        }

        FileInputStream fis = null;
        try {
            // Creamo la subcarpeta
            File carpetaRaiz = obtenerCarpetaArchivos(activity, memo, tipoArchivo);
            File archivoFile = new File(new File(carpetaRaiz, subCarpeta), nombreArchivo);
            boolean okExists = archivoFile.exists();

            if (okExists) {
//                fis = context.openFileInput(nombreArchivo);          // Desde la carpeta directamente del ámbito interno de la app
                fis = new FileInputStream(archivoFile);              // Desde la/s carpeta/s creada/s en el ámbito interno de la app

                // Para versiones iguales o superiores a la
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    byte[] arrayText = fis.readAllBytes();

                    msn = "Archivo leido desde: " + archivoFile.getPath();
                    Log.d(TAG_CLASE, msn);
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

                        msn = "Archivo leido desde: " + archivoFile.getPath();
                        // Convierte los bytes acumulados en ByteArrayOutputStream a un String
                        texto = baos.toString(StandardCharsets.UTF_8.name());
                        return texto;
                    }
            } else {
                msn = "No existe la carpeta pedida";
                return "Error: " + msn;
            }
        } catch (IOException e) {
            msn = "Error al guardar el archivo: " + nombreArchivo;
            Log.e(TAG_CLASE, msn, e);
            return "Error: " + msn;
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    msn = "Error al cerrar FileOutputStream";
                    Log.e(TAG_CLASE, msn, e);
                }
            }
        }
        msn = "Error impreciso";
        return "Error: " + msn;
    }

    /**
     * Guarda texto en un archivo dentro del almacenamiento externo específico de la app.
     * "emulated/0/Android/data/com.ismael.librerias/files/"  "/storage/emulated/0/Android/data/com.ismael.libreras/files/Download/MiCarpeta"
     * @param activity      Contexto de la aplicación.
     * @param subCarpeta    Nombre de la subcarpeta a crear/acceder dentro del ámbito de la app.
     * @param nombreArchivo Nombre del archivo a crear/sobrescribir.
     * @param contenido     Texto a guardar.
     * @param memo           Modo de acceso a las diferentes memorias del dispositivo.
     * @param tipoArchivo    Tipo de archivo a guardar.
     * @return true si se guardó correctamente, false en caso contrario.
     */
    public static boolean guardarTextoExterno(Activity activity, String subCarpeta, String nombreArchivo, String contenido, int memo, String tipoArchivo) {
        if (activity == null || nombreArchivo == null || nombreArchivo.isEmpty() || subCarpeta == null ||
                subCarpeta.isEmpty() || contenido == null || contenido.isEmpty()) {
            msn ="Parámetros inválidos para guardarTextoInterno.";
            Log.i(TAG_CLASE, msn);
            return false;
        }

        FileOutputStream fos = null;

        try {
            // Creamos la subcarpeta
//                File carpetaFile = activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            File capertaRaiz = obtenerCarpetaArchivos(activity, memo, tipoArchivo);
            File carpetaFile = new File(capertaRaiz, subCarpeta);
            boolean okExists = carpetaFile.exists();
            if (!okExists)
//            okExists = carpetaFile.mkdir();      // Vale solo para crear una carpeta de cada vez
                okExists = carpetaFile.mkdirs();        // Vale para crear una o varias carpetas consecutivas

            if (okExists) {
                // Context.MODE_PRIVATE: Si el archivo ya existe, se sobreescribe.
                // Context.MODE_APPEND: Si el archivo ya existe, se añade la final.
//            fos = context.openFileOutput(nombreArchivo, Context.MODE_PRIVATE);    // Archivo directo en el ámbito interno de la app sin carpetas
                String dirArchivo = carpetaFile.getPath() + File.separator + nombreArchivo;
                fos = new FileOutputStream(dirArchivo);                                 // Archivo dentro de carpetas en el interno ámbito de la app
                fos.write(contenido.getBytes(StandardCharsets.UTF_8));
                msn = "Archivo guardado en: " + dirArchivo;
                Log.i(TAG_CLASE, msn);
                // Considera no mostrar Toasts directamente desde una librería,
                // es mejor devolver un resultado y que la app decida cómo notificar al usuario.
                // Toast.makeText(context, "Texto guardado desde librería", Toast.LENGTH_SHORT).show();
                return true;
            }
        } catch (IOException e) {
            msn = "Error al guardar el archivo: " + nombreArchivo;
            Log.e(TAG_CLASE, msn, e);
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    msn = "Error al cerrar FileOutputStream";
                    Log.e(TAG_CLASE, msn, e);
                }
            }
        }

        return false;
    }

    /**
     * Lee texto de un archivo dentro del almacenamiento externo del ámbito de la app.
     *
     * @param activity       Contexto de la aplicación.
     * @param nombreArchivo Nombre del archivo a leer.
     * @param subCarpeta    Nombre de la subCarpeta a acceder dentro del ámbito de la app.
     * @param memo           Modo de acceso a las diferentes memorias del dispositivo.
     * @param tipoArchivo    Tipo de archivo a leer.
     * @return texto si se leyó correctamente, "mensaje de error" en caso contrario.
     */
    public static String leerTextoExterno(Activity activity, String subCarpeta, String nombreArchivo, int memo, String tipoArchivo) {
        if (activity == null || nombreArchivo == null || nombreArchivo.isEmpty()) {
            msn = "Parámetros inválidos para guardarTextoInterno.";
            Log.i(TAG_CLASE, msn);
            return "Error: " + msn;
        }

        String texto = "";
        FileInputStream fis = null;
        File archivoFile = null;
        try {
            // Comprobamos si existe el archivo
//            File carpetaFile = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
            File carpetaRaiz = obtenerCarpetaArchivos(activity, memo, tipoArchivo);
            archivoFile = new File(new File(carpetaRaiz, subCarpeta), nombreArchivo);
            boolean okExists = archivoFile.exists();

            if (okExists) {
                fis = new FileInputStream(archivoFile);
                // Para versiones iguales o superiores a la Tiramisu
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                    byte[] arrayText = fis.readAllBytes();

                    msn = "Archivo leido desde: " + archivoFile.getPath();
                    Log.i(TAG_CLASE, msn);
                    // Considera no mostrar Toasts directamente desde una librería,
                    // es mejor devolver un resultado y que la app decida cómo notificar al usuario.
                    // Toast.makeText(context, "Texto guardado desde librería", Toast.LENGTH_SHORT).show();

                    texto = new String(arrayText, StandardCharsets.UTF_8);
                    return texto;
                }else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU){
                    FileReader fr = null;
                    try{
                        fr = new FileReader(archivoFile);
                        ByteArrayOutputStream baos = new ByteArrayOutputStream();
                        byte[] arrayText = new byte[1024];   // buffer para leer trozos de 1Kb
                        int n = -1;

                        while ((n = fis.read(arrayText)) != -1) {
                            baos.write(arrayText, 0, n);
                        }

                        msn = "Archivo leido desde: " + archivoFile.getPath();
                        Log.i(TAG_CLASE, msn);
                        // Convierte los bytes acumulados en ByteArrayOutputStream a un String
                        texto = baos.toString(StandardCharsets.UTF_8.name());
                        return texto;
                    }catch (IOException ioe){
                        msn = "Error al guardar el archivo: "+ archivoFile.getPath();
                        Log.e(TAG_CLASE, msn, ioe);
                    }finally {
                        if(fr != null){
                            try {
                                fr.close();
                            } catch (IOException ioe) {
                                msn = "Error al cerrar FileReader";
                                Log.e(TAG_CLASE, msn, ioe);
                            }
                        }
                    }
                }
            }else{
                msn = "No existe el archivo: " + archivoFile.getPath();
            }
        } catch (IOException ioe) {
            msn = "Error al guardar el archivo: " + archivoFile.getPath();
            Log.e(TAG_CLASE, msn, ioe);
        } finally {
            if (fis != null) {
                try {
                    fis.close();
                } catch (IOException e) {
                    msn = "Error al cerrar FileOutputStream";
                    Log.e(TAG_CLASE, msn, e);
                }
            }
        }
        return "Error: " + msn;
    }

    /**
     * Guarda texto en un archivo dentro del almacenamiento externo específico fuera del ámbito de la app.
     * "Environment.getExternalStorageDirectory()"  -
     * @param activity      Contexto de la aplicación.
     * @param subCarpeta    Nombre de la subcarpeta a crear/acceder dentro del ámbito de la app.
     * @param nombreArchivo Nombre del archivo a crear/sobrescribir.
     * @param contenido     Texto a guardar.
     * @param memo           Modo de acceso a las diferentes memorias del dispositivo.
     * @param tipoArchivo    Tipo de archivo a leer.
     * @return true si se guardó correctamente, false en caso contrario.
     */
    public static boolean guardarTextoExternoFueraAmbito(Activity activity, String subCarpeta, String nombreArchivo, String contenido, int memo, String tipoArchivo) {
        if (activity == null || nombreArchivo == null || nombreArchivo.isEmpty() || subCarpeta == null ||
                subCarpeta.isEmpty() || contenido == null || contenido.isEmpty()) {
            msn = "Parámetros inválidos para guardarTextoExterno.";
            Log.e(TAG_CLASE, msn);
            return false;
        }

        FileOutputStream fos = null;
        Uri carpetaUri = null;
        try {
            // Creamos la subcarpeta
            File carpetaRaiz = obtenerCarpetaArchivos(activity, memo, tipoArchivo);
            File carpetaFile = new File(carpetaRaiz, subCarpeta);
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q){
                carpetaUri = obtenerDirUri(activity,carpetaFile);
            }else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){

            }
            boolean okExists = carpetaFile.exists();
            if (!okExists)
//            okExists = carpetaFile.mkdir();      // Vale solo para crear una carpeta de cada vez
                okExists = carpetaFile.mkdirs();        // Vale para crear una o varias carpetas consecutivas

            if (okExists) {
                // Context.MODE_PRIVATE: Si el archivo ya existe, se sobreescribe.
                // Context.MODE_APPEND: Si el archivo ya existe, se añade la final.
//                fos = context.openFileOutput(nombreArchivo, Context.MODE_PRIVATE);      // Archivo directo en el ámbito interno de la app sin carpetas
                String dirArchivo = carpetaFile.getPath() + File.separator + nombreArchivo;
                fos = new FileOutputStream(dirArchivo);                                 // Archivo dentro de carpetas en el interno ámbito de la app
                fos.write(contenido.getBytes(StandardCharsets.UTF_8));

                msn = "Archivo guardado en: " + dirArchivo;
                Log.i(TAG_CLASE, msn);
                // Considera no mostrar Toasts directamente desde una librería,
                // es mejor devolver un resultado y que la app decida cómo notificar al usuario.
                // Toast.makeText(context, "Texto guardado desde librería", Toast.LENGTH_SHORT).show();
                return true;
            }else
                msn = "No existe o no se puede crear el directorio: " + carpetaFile.getPath();
        } catch (IOException e) {
            msn = "Error al guardar el archivo: " + nombreArchivo;
            Log.e(TAG_CLASE, msn, e);
            return false;
        } finally {
            if (fos != null) {
                try {
                    fos.close();
                } catch (IOException e) {
                    msn = "Error al cerrar FileOutputStream";
                    Log.e(TAG_CLASE, msn, e);
                }
            }
        }
        return false;
    }

    /**
     * Obtiene una Uri con la dirección del nombreArchivo en una subcarpeta en la zona de memoria
     * determinada por el fileFoto
     * Esa carpeta tiene que tenerla creada en archivo "file_paths.xml" para que pueda ser creada y usada mediante
     * el fileprovider designado en el archivo "AndroidMaifest.xml"
     *
     * @param activity Contexto de la aplicación.
     * @param fileArchivo objeto File con la dirección del archivos imagen guardado.
     * @return true si se guardó correctamente, false en caso contrario.
     */
    public static Uri obtenerDirUri(Activity activity, File fileArchivo) throws IOException{
        // Continuar solo si el File fue creado exitosamente
        if (fileArchivo != null) {
            // Obtener el URI para el archivo usando FileProvider
            // La autoridad debe coincidir con la declarada en el AndroidManifest.xml
            // y con el aplicationId
            String autoridad = activity.getApplicationContext().getPackageName() + ".fileprovider";
            Uri uriArchivo = FileProvider.getUriForFile(activity.getApplicationContext(), autoridad, fileArchivo);

            return uriArchivo;
        }

        return null;
    }
    /**
     * Obtiene un File con la dirección del nombreArchivo en una subcarpeta en la memoriq externa en el ámbito de la app
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
        if (!okCarpeta) {
            okCarpeta = dirCarpeta.mkdirs();
        }

        // Si nombreArchivo llega nulo o vacío, se genera el nombre con el timeStamp
        File imagenFile = null;
        // Aquí se añade al nombre un número aleatorio para hacer archivos con nombres diferentes
        if (okCarpeta && (!nombreArchivo.contains("JPEG") && !nombreArchivo.contains("jpg") && !nombreArchivo.contains("png"))) {
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

    private static File obtenerCarpetaArchivos(Activity activity, int memo, String tipoArchivo) {
        return null;
    }
}
