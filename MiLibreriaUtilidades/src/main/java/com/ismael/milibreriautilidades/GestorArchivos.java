// Dentro de MiLibreriaUtilidades > java > com.tudominio.milibreriautilidades
// Archivo: GestorArchivos.java
package com.ismael.milibreriautilidades;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentResolver;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.Log;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;

import androidx.annotation.RequiresApi;
import androidx.core.content.FileProvider;
/** ### DIFERENETES POSIBILIDADES DE GUARDAR ARCHIVOS EN LAS DIFERENTES MEMORIAS DEL DISPOSITIVO ###
 * 1. Mejor Atribución y Control: El sistema quiere que los archivos creados por una aplicación estén
 * claramente asociados a ella y que la aplicación no tenga acceso indiscriminado a t0do el
 * almacenamiento externo.
 * 2. Protección de la Privacidad del Usuario: Limita la capacidad de las aplicaciones para escanear
 * y modificar archivos arbitrarios en el almacenamiento externo sin el conocimiento explícito del
 * usuario.
 * 3. Menos Desorden: Reduce el desorden de archivos huérfanos en el almacenamiento compartido cuando
 * las aplicaciones se desinstalan. ¿Dónde pueden las aplicaciones guardar archivos de texto (y otros
 * archivos) en Android Q+?
 * 4. Almacenamiento Específico de la Aplicación (App-Specific Storage):
 *      . Almacenamiento Interno: getFilesDir(), getCacheDir(). Estos siempre han sido privados para
 *          la aplicación.
 *      . Almacenamiento Externo Específico de la Aplicación: getExternalFilesDir(null),
 *          getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), getExternalCacheDir().
 *              . Los archivos guardados aquí son eliminados cuando la aplicación se desinstala.
 *              . No se requiere ningún permiso para leer/escribir en estos directorios.
 *              . Otras aplicaciones no pueden acceder directamente a estos archivos (a menos que
 *                  uses FileProvider para compartirlos explícitamente).
 *              . Este es el lugar preferido para la mayoría de los archivos que tu aplicación
 *                  necesita para funcionar y que no están destinados a ser directamente gestionados
 *                  por el usuario fuera de tu app.
 * 5. Colecciones de Medios Compartidos (MediaStore):
 *  . Para archivos multimedia como imágenes, videos y audio, o archivos de descarga (como documentos
 *      PDF, TXT, etc., que el usuario espera encontrar en la carpeta "Downloads").
 *  . Debes usar la API MediaStore para crear entradas y obtener un OutputStream para escribir el
 *      contenido.
 *  . Requiere el permiso WRITE_EXTERNAL_STORAGE (o el más granular ACCESS_MEDIA_LOCATION para la
 *      ubicación de los medios) solo hasta Android 9 (API 28). A partir de Android 10 (Q), para
 *      contribuir a las colecciones de medios de tu propia app, generalmente no necesitas
 *      WRITE_EXTERNAL_STORAGE si el archivo es creado por tu app. Si quieres modificar archivos de
 *      otras apps a través de MediaStore, necesitarás permisos y posiblemente el consentimiento del
 *      usuario.
 *  . Para guardar un archivo de texto que quieres que el usuario vea en su carpeta de "Downloads":
 *
 * 6. Storage Access Framework (SAF):
 *  . Si quieres que el usuario elija una ubicación específica (incluyendo la raíz o una carpeta
 *      personalizada en la raíz) para guardar un archivo, o para abrir un documento que tu
 *      aplicación no creó, debes usar el Storage Access Framework.
 *  . Implica usar Intents como ACTION_CREATE_DOCUMENT (para guardar) o ACTION_OPEN_DOCUMENT
 *      (para abrir).
 *  . Esto presenta un selector de archivos del sistema al usuario, y tu aplicación recibe un
 *      Uri al archivo seleccionado, al cual se le otorgan permisos temporales.
 *  . Ejemplo para guardar un archivo de texto usando SAF:
 *
 * 7. Excepción: MANAGE_EXTERNAL_STORAGE (Acceso a Todos los Archivos)
 *  . Existe un permiso especial MANAGE_EXTERNAL_STORAGE (introducido en Android 11, API 30) que
 *      otorga un acceso amplio al almacenamiento externo, similar al comportamiento antiguo de
 *      WRITE_EXTERNAL_STORAGE.
 *  . Sin embargo, Google Play tiene políticas muy estrictas sobre qué aplicaciones pueden solicitar
 *      y usar este permiso. Solo se permite para casos de uso muy específicos (como administradores
 *      de archivos, aplicaciones de copia de seguridad/restauración) .  La mayoría de las aplicaciones
 *      no calificarán y serán rechazadas si solicitan este permiso sin una justificación válida. En
 *      resumen para >= Q:
 *  . No puedes usar new File("/sdcard/mi_carpeta_raiz/mi_archivo.txt").createNewFile() y esperar que
 *      funcione para la mayoría de las aplicaciones.
 *  . Usa almacenamiento específico de la aplicación para archivos privados de tu app.
 *  . Usa MediaStore para colecciones multimedia comunes (como Downloads).
 *  . Usa Storage Access Framework (SAF) si necesitas que el usuario elija una ubicación o interactúe
 *      con archivos fuera del control directo de tu app.
 *  . Evita MANAGE_EXTERNAL_STORAGE a menos que tu aplicación tenga un caso de uso principal que lo
 *      requiera y cumpla con las políticas de Google Play.*/

/**************************************************************************************************
 * Fecha: 09-06-2025
 * Autor: Ismael Galán Fernández
 * Comentario:
 * Clase que contiene la gestión de escritura lectura de archivos en:
 * 1. Memoria interna del ámbito de la aplicación mediante File para SDK < Q
 * 2. Memoria interna del ámbito de la aplicación para SDK >= Q
 * 5. Memoria interna fuera del ámbito de la aplicación
 * 3. Memoria externa del ámbito de la aplicación mediante File para SDK < Q
 * 4. Memoria externa del ámbito de la aplicación para SDK >= Q
 * 5. Memoria externa fuera del ámbito de la aplicación
 *
 * ************************************************************************************************/
public class GestorArchivos {
    private static final String TAG_CLASE = "TAG_GestorArchivos";
    // Tag para los tipos de parámetros
    private static final String TAG_RELATIVE_PATH = "relative_path";
    private static final String TAG_MIME_TYPE = "mime_type";
    private static final String TAG_URI = "uri";
    private static final String TAG_DIRECTORIO_RELATIVO = "directorio_relativo";
    private static final String TAG_DIRECTORIO_BUSQUEDA = "directorio_busqueda";

    // Modos Tipos Memoria
    public final static int MEMO_INT_AMBITO_FILES = 1;          // Para acceder a la memoria interna del ámbito files de la app
    public final static int MEMO_INT_NO_AMBITO = 2;             // Para acceder a la memoria interna fuera del ámbito de la app
    public final static int MEMO_INT_AMBITO_CACHE = 3;          // Para acceder a la memoria interna del ambito caché de la app
    public final static int MEMO_INT_AMBITO_RAIZ = 4;           // Para acceder a la memoria interna del ámbito raiz de la app
    public final static int MEMO_EXT_AMBITO_FILES = 10;         // Para acceder a la memoria externa del ámbito files de la app
    public final static int MEMO_EXT_NO_AMBITO = 20;            // Para acceder a la memoria externa fuera del ámbito de la app
    public final static int MEMO_EXT_AMBITO_CACHE = 30;         // Para acceder a la memoria externa del ámbito caché de la app
//    public final static int MEMO_EXT_AMBITO_RAIZ = 40;          // Para acceder a la memoria externa del ámbito raiz de la app
    // Tipos de archivos
    public final static String TIPO_IMAGEN = "imagen";          // Si el tipo de archivo es para guardar en la carpeta PICTURES del ambito de la app
    public final static String TIPO_TEXTO = "texto";            // Si el tipo de archivo es para guardar en la carpeta DOCUMENTS del ambito de la app
    public final static String TIPO_DOWLOADS = "downloads";     // Si el tipo de archivo es para guardar en la carpeta DOWNLOADS del ambito de la app
    public final static String TIPO_VIDEO = "video";            // Si el tipo de archivo es para guardar en la carpeta MOVIES del ambito de la app
    public final static String TIPO_AUDIO = "musica";           // Si el tipo de archivo es para guardar en la carpeta MUSIC del ambito de la app
    public final static String TIPO_PRIVATE= "private";         // Si el tipo de archivo es para guardar en la carpeta raiz del ámbito de la app
    public final static String TIPO_NULO = "";                  // Si el tipo de archivo es para guardar en la carpeta raiz

//    public final static String TIPO_VERSION_Q = "versionQ";     // Si el tipo de archivo es para guardar en la carpeta raiz

    private static String msn = "";

    /**
     * Guarda el texto a un archivo dentro del almacenamiento indicado por @memo y dentro de la subCarpeta
     * indicado por @tipoArchivo.
     * PARA LAS VERSIONES SUPERIORES A Q
     *
     * @param activity       Contexto de la aplicación.
     * @param subCarpeta    Nombre de la subCarpeta a crear dentro de la seleccion de memoria y tipo
     *                      de archivo.
     * @param nombreArchivo Nombre del archivo a leer.
     * @param memo           Modo de acceso a las diferentes memorias del dispositivo.
     * @param tipoArchivo    Tipo de archivo a guardar. Y subcarpeta a crear según el tipo de archivo.
     * @return true si se guardó correctamente, false en caso contrario.
     */
    public static boolean guardarMayoresQ(Activity activity, String subCarpeta, String nombreArchivo, String contenido, int memo, String tipoArchivo) {
        if (activity == null || nombreArchivo == null || nombreArchivo.isEmpty() ||
                contenido == null || contenido.isEmpty() || memo == 0 || tipoArchivo == null) {
            msn = "Parámetros inválidos para guardarTextoAmbito.";
            Log.e(TAG_CLASE, msn);
            return false;
        }

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
                fos = new FileOutputStream(dirArchivo, false);
//                fos = activity.getApplicationContext().openFileOutput(nombreArchivo, Context.MODE_PRIVATE);       // Esto siempre graba en el ámbito privado de la app, files
                fos.write(contenido.getBytes(StandardCharsets.UTF_8));

                msn = "Archivo guardado en: " + dirArchivo;
                Log.d(TAG_CLASE, msn);
                // Considera no mostrar Toasts directamente desde una librería,
                // es mejor devolver un resultado y que la app decida cómo notificar al usuario.
                // Toast.makeText(context, "Texto guardado desde librería", Toast.LENGTH_SHORT).show();
                return true;
            }else{
                msn = "Error al tener acceso a la carpeta: " + subCarpeta;
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
     * Guarda el texto a un archivo dentro del almacenamiento indicado por @memo y dentro de la subCarpeta
     * indicado por @tipoArchivo.
     * PARA LAS VERSIONES INFERIORES A Q
     *
     * @param activity       Contexto de la aplicación.
     * @param subCarpeta    Nombre de la subCarpeta a crear dentro de la seleccion de memoria y tipo
     *                      de archivo.
     * @param nombreArchivo Nombre del archivo a leer.
     * @param memo           Modo de acceso a las diferentes memorias del dispositivo.
     * @param tipoArchivo    Tipo de archivo a guardar. Y subcarpeta a crear según el tipo de archivo.
     * @return true si se guardó correctamente, false en caso contrario.
     */
    public static boolean guardarMenoresQ(Activity activity, String subCarpeta, String nombreArchivo, String contenido, int memo, String tipoArchivo) {
        if (activity == null || nombreArchivo == null || nombreArchivo.isEmpty() ||
                contenido == null || contenido.isEmpty() || memo == 0 || tipoArchivo == null) {
            msn = "Parámetros inválidos para guardarTextoAmbito.";
            Log.e(TAG_CLASE, msn);
            return false;
        }

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

                msn = "Texto guardado en: " + carpetaFile + "/" + nombreArchivo;
                Log.i(TAG_CLASE, msn);

                // Opcional: Limpiar el EditText después de guardar
                // txtDocumento.setText("");
                return true;
            } catch (IOException ioe) {
                msn = "Error al guardar el archivo: " + nombreArchivo;
                Log.e(TAG_CLASE, msn, ioe);
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
        }else {
            msn = "Error al crear la carpeta: " + subCarpeta;
            Log.e(TAG_CLASE, msn);
            return false;
        }

        return false;
    }

    /**
     * Lee texto de un archivo dentro del almacenamiento indicado por @memo y dentro de la subCarpeta
     * indicado por @tipoArchivo.
     * PARA LAS VERSIONES SUPERIORES A TIRAMISU
     *
     * @param activity       Contexto de la aplicación.
     * @param subCarpeta    Nombre de la subCarpeta a crear dentro de la seleccion de memoria y tipo
     *                      de archivo.
     * @param nombreArchivo Nombre del archivo a leer.
     * @param memo           Modo de acceso a las diferentes memorias del dispositivo.
     * @param tipoArchivo    Tipo de archivo a guardar. Y subcarpeta a crear según el tipo de archivo.
     * @return texto si se leyó correctamente, "mensaje de error" en caso contrario.
     */
    @RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
    public static String leerMayoresTIRAMISU(Activity activity, String subCarpeta, String nombreArchivo, int memo, String tipoArchivo) {
        String texto = "";
        if (activity == null || nombreArchivo == null || nombreArchivo.isEmpty() ||
                memo == 0 || tipoArchivo == null) {
            msn = "Parámetros inválidos para guardarTextoAmbito.";
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

                // Para versiones iguales o superiores a la TIRAMISU (API 33)
                byte[] arrayText = fis.readAllBytes();

                msn = "Archivo leido desde: " + archivoFile.getPath();
                Log.d(TAG_CLASE, msn);
                // Considera no mostrar Toasts directamente desde una librería,
                // es mejor devolver un resultado y que la app decida cómo notificar al usuario.
                // Toast.makeText(context, "Texto guardado desde librería", Toast.LENGTH_SHORT).show();

                texto = new String(arrayText, StandardCharsets.UTF_8);
                return texto;
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
    }

    /**
     * Lee texto de un archivo dentro del almacenamiento indicado por @memo y dentro de la subCarpeta
     * indicado por @tipoArchivo.
     * PARA LAS VERSIONES INFERIORES A TIRAMISU
     *
     * @param activity       Contexto de la aplicación.
     * @param subCarpeta    Nombre de la subCarpeta a crear dentro de la seleccion de memoria y tipo
     *                      de archivo.
     * @param nombreArchivo Nombre del archivo a leer.
     * @param memo           Modo de acceso a las diferentes memorias del dispositivo.
     * @param tipoArchivo    Tipo de archivo a guardar. Y subcarpeta a crear según el tipo de archivo.
     * @return texto si se leyó correctamente, "mensaje de error" en caso contrario.
     */
    public static String leerMenoresTIRAMISU(Activity activity, String subCarpeta, String nombreArchivo, int memo, String tipoArchivo) {
        String texto = "";
        if (activity == null || nombreArchivo == null || nombreArchivo.isEmpty() ||
                memo == 0 || tipoArchivo == null) {
            msn = "Parámetros inválidos para guardarTextoAmbito.";
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

                // Para versiones inferiores a la TIRAMISU
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
            } else {
                msn = "No existe el archivo pedido " + archivoFile;
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
    }

    /**
     * Guarda texto en un archivo dentro del almacenamiento externo específico fuera del ámbito de la app.
     * "Context.getContentResolver()"  - para < Q en MediaStore.Downloads y para > Q en MediaStore.Documents
     * @param activity      Contexto de la aplicación.
     * @param uri           Direccion Uri del archivo a crear/sobrescribir.
     * @param contenido     Texto a guardar.
     * @return true si se guardó correctamente, false en caso contrario.
     */
    public static boolean escribirTextoEnUriExterno(Activity activity, Uri uri, String contenido) {
        try{
            OutputStream os = activity.getApplicationContext().getContentResolver().openOutputStream(uri);
            if(os != null){
                os.write(contenido.getBytes(StandardCharsets.UTF_8));
                msn = "Archivo guardado en: " + uri.toString();

                return true;
            }else{
                msn = "No se pudo abrir el archivo para escritura.";
                return false;
            }
        }catch (IOException ioe){
            msn = "Error al guardar archivo " + ioe.getMessage();
            ioe.printStackTrace();

            return false;
        }
    }

    /**
     * Guarda una imágen en un archivo dentro del almacenamiento interno específico de la app.
     *
     * @param activity      Activity desde la que se hace la llamada.
     * @param bitmap        Imagen a guardar.
     * @param nombreCarpeta Nombre de la carpeta a crear.
     * @param nombreArchivo Nombre del archivo a crear/sobrescribir.
     * @return true si se guardó correctamente, false en caso contrario.
     */
    public static boolean guardarBitmapEnMediaStore(Activity activity, Bitmap bitmap, String nombreCarpeta, String nombreArchivo) {
        if (activity == null || bitmap == null || nombreCarpeta == null || nombreCarpeta.isEmpty() ||
                nombreArchivo == null || nombreArchivo.isEmpty()) {
            Log.i(TAG_CLASE, "Error de parámetros. No validos");
            return false;
        }


        return false;
    }

    /**
     * Obtiene un File con la dirección del nombreArchivo en una subcarpeta en la zona externa del ámbito de la app
     * Environment.getExternalStorageDirectory()
     *
     * @param activity       Contexto de la aplicación.
     * @param subCarpeta Nombre de la carpeta a crear.
     * @param nombreArchivo Nombre del archivo a crear/sobrescribir.
     * @param tipoArchivo Según el tipo de archivos se guardará en una carpeta u otra.
     * @return true si se guardó correctamente, false en caso contrario.
     */
    public static File obtenerArchivoDir(Activity activity, String subCarpeta, String nombreArchivo, int modo, String tipoArchivo){
        if(nombreArchivo == null || nombreArchivo.isEmpty()) {
            // Crea un nombre de archivo de imagen único
            String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(new Date());
            nombreArchivo = "JPEG_" + timeStamp + "_";
        }

        File dirCarpeta = new File(obtenerCarpetaArchivos(activity, modo, tipoArchivo), subCarpeta);
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
            try {
                imagenFile = File.createTempFile(
                        nombreArchivo,       // prefijo
                        ".jpg",             // sufijo
                        dirCarpeta          // directorio
                );
            } catch (IOException e) {
                msn = "Error al crear el archivo: " + nombreArchivo;
            }
        }else if(okCarpeta){
            imagenFile = new File(dirCarpeta, nombreArchivo);
        }else
            msn = "La carpeta no existe o no se puede crear.";

        return imagenFile;
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
     * Devuelve un File con la dirección de la carpeta raiz donde se guardará el archivo
     * Para versiones >= Q
     * Según el tipo de archivos se guardará en una carpeta u otra (Pictures, Documents, Downloads, Audio, Video)
     * pero siempre en los ámbitos privados de la app.
     *
     * Para versiones < Q
     * @param activity Contexto de la aplicación.
     * @param modo Modo de acceso a las diferentes zonas de memoria.
     * @param tipoArchivo Según el tipo de archivos se guardará en una carpeta u otra.
     * @return File con la dirección de la carpeta donde se guardará el archivo.*/
    private static File obtenerCarpetaArchivos(Activity activity, int modo, String tipoArchivo) {
        switch (modo) {
            case MEMO_INT_AMBITO_FILES:
                switch (tipoArchivo) {
                    case TIPO_IMAGEN:
                        return new File(activity.getFilesDir(), Environment.DIRECTORY_PICTURES);
                    case TIPO_TEXTO:
                        return new File(activity.getFilesDir(), Environment.DIRECTORY_DOCUMENTS);
                    case TIPO_DOWLOADS:
                        return new File(activity.getFilesDir(), Environment.DIRECTORY_DOWNLOADS);
                    case TIPO_AUDIO:
                        return new File(activity.getFilesDir(), Environment.DIRECTORY_MUSIC);
                    case TIPO_VIDEO:
                        return new File(activity.getFilesDir(), Environment.DIRECTORY_MOVIES);
                    case TIPO_PRIVATE:
                        return activity.getDir(null, Context.MODE_PRIVATE);
                    case TIPO_NULO:
                        return activity.getDir("", Context.MODE_PRIVATE);
                }

            case MEMO_INT_NO_AMBITO:
                switch (tipoArchivo) {
                    case TIPO_IMAGEN:
                        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES);
                    case TIPO_TEXTO:
                        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS);
                    case TIPO_DOWLOADS:
                        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                    case TIPO_AUDIO:
                        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MUSIC);
                    case TIPO_VIDEO:
                        return Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
                    case TIPO_PRIVATE:
                        return Environment.getExternalStoragePublicDirectory("");
                    case TIPO_NULO:
                        return Environment.getExternalStorageDirectory();
                }

            case MEMO_INT_AMBITO_CACHE:
                switch (tipoArchivo) {
                    case TIPO_IMAGEN:
                        return new File(activity.getCacheDir(), Environment.DIRECTORY_PICTURES);
                    case TIPO_TEXTO:
                        return new File(activity.getCacheDir(), Environment.DIRECTORY_DOCUMENTS);
                    case TIPO_DOWLOADS:
                        return new File(activity.getCacheDir(), Environment.DIRECTORY_DOWNLOADS);
                    case TIPO_AUDIO:
                        return new File(activity.getCacheDir(), Environment.DIRECTORY_MUSIC);
                    case TIPO_VIDEO:
                        return new File(activity.getCacheDir(), Environment.DIRECTORY_MOVIES);
                    case TIPO_NULO:
                        return activity.getCacheDir();
                }

            case MEMO_INT_AMBITO_RAIZ:
                switch (tipoArchivo) {
                    case TIPO_IMAGEN:
                        return activity.getDir(Environment.DIRECTORY_PICTURES, Context.MODE_PRIVATE);
                    case TIPO_TEXTO:
                        return activity.getDir(Environment.DIRECTORY_DOCUMENTS, Context.MODE_PRIVATE);
                    case TIPO_DOWLOADS:
                        return activity.getDir(Environment.DIRECTORY_DOWNLOADS, Context.MODE_PRIVATE);
                    case TIPO_AUDIO:
                        return activity.getDir(Environment.DIRECTORY_MUSIC, Context.MODE_PRIVATE);
                    case TIPO_VIDEO:
                        return activity.getDir(Environment.DIRECTORY_MOVIES, Context.MODE_PRIVATE);
                    case TIPO_PRIVATE:
                        return activity.getDir("", Context.MODE_PRIVATE);
                    case TIPO_NULO:
                        return activity.getDir(null, Context.MODE_PRIVATE);
                }

            case MEMO_EXT_AMBITO_FILES:
                switch (tipoArchivo) {
                    case TIPO_IMAGEN:
                        return activity.getExternalFilesDir(Environment.DIRECTORY_PICTURES);
                    case TIPO_TEXTO:
                        return activity.getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS);
                    case TIPO_DOWLOADS:
                        return activity.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS);
                    case TIPO_AUDIO:
                        return activity.getExternalFilesDir(Environment.DIRECTORY_MUSIC);
                    case TIPO_VIDEO:
                        return activity.getExternalFilesDir(Environment.DIRECTORY_MOVIES);
                    case TIPO_NULO:
                        return activity.getExternalFilesDir(null);
                }

            case MEMO_EXT_AMBITO_CACHE:
                switch (tipoArchivo) {
                    case TIPO_IMAGEN:
                        return new File(activity.getExternalCacheDir(), Environment.DIRECTORY_PICTURES);
                    case TIPO_TEXTO:
                        return new File(activity.getExternalCacheDir(), Environment.DIRECTORY_DOCUMENTS);
                    case TIPO_DOWLOADS:
                        return new File(activity.getExternalCacheDir(), Environment.DIRECTORY_DOWNLOADS);
                    case TIPO_NULO:
                        return activity.getExternalCacheDir();
                }

            case MEMO_EXT_NO_AMBITO:
                if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                    // TODO terminar para memoria externa/SD
                    return Environment.getExternalStorageDirectory();
                }else {
                    // TODO terminar para memoria externa/SD
                    return Environment.getExternalStorageDirectory();
                }
            default:
                return null;
        }
    }

    /**
     * Guarda un contenido en una Uri según el tipo de archivo y zona de memoria
     *
     * @param activity Contexto de la aplicación.
     * @param subCarpeta Carpeta secundaria dentro de la zona de memoria elegida.
     * @param nombreArchivo Nombre del archivo a crear/sobrescribir.
     * @param contenido Contenido del archivo a guardar.
     * @param tipoArchivo Según el tipo de archivos se guardará en una carpeta u otra.
     * @return bolean true si se guardó correctamente, false en caso contrario.*/
    public static boolean saveContentToUri(Activity activity, String subCarpeta, String nombreArchivo, String contenido, String tipoArchivo) {
        if (activity == null || nombreArchivo == null || nombreArchivo.isEmpty() || subCarpeta == null || subCarpeta.isEmpty() ||
                contenido == null || contenido.isEmpty() || tipoArchivo == null || tipoArchivo.isEmpty()) {
            msn = "Parámetros inválidos en " + TAG_CLASE + ".guardarTextoExterno().";
            return false;
        }

        Map<String, Object> parametros = obtenerComponetesUriExterno(tipoArchivo);
        if (!parametros.isEmpty()) {
//            String relativePath = Environment.DIRECTORY_DOWNLOADS;
//            String relativePath = Environment.DIRECTORY_DOCUMENTS;
//            String relativePath = Environment.DIRECTORY_MUSIC;
//            String relativePath = Environment.DIRECTORY_MOVIES;
//            String relativePath = Environment.DIRECTORY_PICTURES;
            String relativePath = (String) parametros.get(TAG_RELATIVE_PATH);
            relativePath += "/" + subCarpeta + "/";


            ContentResolver resolver = activity.getContentResolver();
            ContentValues contentValues = new ContentValues();
            contentValues.put(MediaStore.MediaColumns.DISPLAY_NAME, nombreArchivo);
//            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "text/plain");
//            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "audio/mp3");
//            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "video/mp4");
//            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, "image/jpeg");
            contentValues.put(MediaStore.MediaColumns.MIME_TYPE, (String) parametros.get(TAG_MIME_TYPE));
            contentValues.put(MediaStore.MediaColumns.RELATIVE_PATH, relativePath);
            contentValues.put(MediaStore.MediaColumns.IS_PENDING, 1);

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
//                Uri uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues);                                    // Para la CARPETA Dowload
//                Uri uri = resolver.insert(MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues);           // Para la CARPETA Documents
//                Uri uri = resolver.insert(MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues);     // Para la CARPETA Music
//                Uri uri = resolver.insert(MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues);     // Para la CARPETA Movies
//                Uri uri = resolver.insert(MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY), contentValues);    // Para la CARPETA Documents
                Uri uri = resolver.insert((Uri) Objects.requireNonNull(parametros.get(TAG_URI)), contentValues);    // Para la CARPETA Documents
                /**
                 * Comprobar si un archivo con el nombre especificado ya existe en la colección.
                 *
                 * Obtén el collectionUri para Downloads (esto está bien si es para Q+)
                 * Uri collectionUriForQuery;
                 * if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                 *     collectionUriForQuery = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                 * } else {
                 *     collectionUriForQuery = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
                 * }
                 * NO uses el uri de un insert() previo como collectionUri para una consulta de búsqueda general.
                 *
                 * Uri que obtuviste de un insert() previo (NO LO USES COMO collectionUri para buscar)
                 * Uri uriPrevioDelInsert = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, contentValues); */
//                String directorioRelativoDeseado = Environment.DIRECTORY_DOWNLOADS;
//                String directorioRelativoDeseado = Environment.DIRECTORY_DOCUMENTS;
//                String directorioRelativoDeseado = Environment.DIRECTORY_MUSIC;
//                String directorioRelativoDeseado = Environment.DIRECTORY_MOVIES;
//                String directorioRelativoDeseado = Environment.DIRECTORY_PICTURES;
                String directorioRelativoDeseado = (String) parametros.get(TAG_DIRECTORIO_RELATIVO);
//                Uri coleccionDondeBuscar = MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
//                Uri coleccionDondeBuscar = MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
//                Uri coleccionDondeBuscar = MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
//                Uri coleccionDondeBuscar = MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
//                Uri coleccionDondeBuscar = MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY);
                Uri coleccionDondeBuscar = (Uri) parametros.get(TAG_DIRECTORIO_BUSQUEDA);
                Uri busquedaUri = GestorArchivos.findFileUriByName(activity, nombreArchivo, directorioRelativoDeseado, coleccionDondeBuscar);
                if (busquedaUri == null && uri != null) {
                    busquedaUri = uri;
                }

                try {
                    OutputStream os = resolver.openOutputStream(busquedaUri, "wt");
                    os.write(contenido.getBytes());
                    os.flush();
                    os.close();
                    contentValues.put(MediaStore.MediaColumns.IS_PENDING, 0);
                    resolver.update(busquedaUri, contentValues, null, null);
                    msn = "Archivo guardado en: " + busquedaUri.toString();

                    return true;
                } catch (Exception e) {
                    msn = "Error al guardar el archivo: " + e.getMessage();
                    e.printStackTrace();
                    Log.e(TAG_CLASE, msn);
                    return false;
                }
            } else if (Build.VERSION.SDK_INT < Build.VERSION_CODES.Q) {
                // TODO versión no completada
                msn = "Versión no completada!!!";
            }
        } else {
            msn = "Error de parámetros. No validos";
            return false;
        }

        return false;
    }

    /**
     * Guarda un contenido en una Uri de la memoria externa (no ámbito) según el tipo de archivo
     *
     * @param tipoArchivo Según el tipo de archivos se guardará en una carpeta u otra.
     * @return Map con los parámetros necesarios para guardar el archivo.*/
    @SuppressLint("NewApi")
    private static Map<String, Object> obtenerComponetesUriExterno(String tipoArchivo){
        Map<String, Object> parametros = new HashMap<>();

        switch (tipoArchivo){
            case TIPO_DOWLOADS:
                parametros.put(TAG_RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS);
                parametros.put(TAG_MIME_TYPE, "text/plain");
                parametros.put(TAG_URI, MediaStore.Downloads.EXTERNAL_CONTENT_URI);
                parametros.put(TAG_DIRECTORIO_RELATIVO, Environment.DIRECTORY_DOWNLOADS);
                parametros.put(TAG_DIRECTORIO_BUSQUEDA, MediaStore.Downloads.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY));
                break;
            case TIPO_TEXTO:
                parametros.put(TAG_RELATIVE_PATH, Environment.DIRECTORY_DOCUMENTS);
                parametros.put(TAG_MIME_TYPE, "text/plain");
                parametros.put(TAG_URI, MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY));
                parametros.put(TAG_DIRECTORIO_RELATIVO, Environment.DIRECTORY_DOCUMENTS);
                parametros.put(TAG_DIRECTORIO_BUSQUEDA, MediaStore.Files.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY));
                break;
            case TIPO_AUDIO:
                parametros.put(TAG_RELATIVE_PATH, Environment.DIRECTORY_MUSIC);
                parametros.put(TAG_MIME_TYPE, "audio/mp3");
                parametros.put(TAG_URI, MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY));
                parametros.put(TAG_DIRECTORIO_RELATIVO, Environment.DIRECTORY_MUSIC);
                parametros.put(TAG_DIRECTORIO_BUSQUEDA, MediaStore.Audio.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY));
                break;
            case TIPO_VIDEO:
                parametros.put(TAG_RELATIVE_PATH, Environment.DIRECTORY_MOVIES);
                parametros.put(TAG_MIME_TYPE, "video/mp4");
                parametros.put(TAG_URI, MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY));
                parametros.put(TAG_DIRECTORIO_RELATIVO, Environment.DIRECTORY_MOVIES);
                parametros.put(TAG_DIRECTORIO_BUSQUEDA, MediaStore.Video.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY));
                break;
            case TIPO_IMAGEN:
                parametros.put(TAG_RELATIVE_PATH, Environment.DIRECTORY_PICTURES);
                parametros.put(TAG_MIME_TYPE, "image/jpeg");
                parametros.put(TAG_URI, MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY));
                parametros.put(TAG_DIRECTORIO_RELATIVO, Environment.DIRECTORY_PICTURES);
                parametros.put(TAG_DIRECTORIO_BUSQUEDA, MediaStore.Images.Media.getContentUri(MediaStore.VOLUME_EXTERNAL_PRIMARY));
                break;
            case TIPO_NULO:
                break;
        }
        return parametros;
    }

    /**
     * Comprueba si un archivo con el nombre especificado ya existe en la colección
     * de MediaStore y directorio relativo dados.
     *
     * @param activity Contexto de la aplicación.
     * @param nombreArchivo El nombre del archivo a buscar (ej. "mi_documento.txt").
     * @param relativePath El directorio relativo dentro de la colección de MediaStore
     *                     (ej. Environment.DIRECTORY_DOWNLOADS). Solo relevante para Android Q+.
     * @param collectionUri El Uri de la colección de MediaStore a consultar
     *                      (ej. MediaStore.Downloads.EXTERNAL_CONTENT_URI).
     * @return El Uri del archivo si existe, o null si no existe.
     */
    private static Uri findFileUriByName(Activity activity, String nombreArchivo, String relativePath, Uri collectionUri){
        ContentResolver resolver = activity.getApplicationContext().getContentResolver();
        Uri existingFileUri = null;

        String[] projection = {MediaStore.MediaColumns._ID, MediaStore.MediaColumns.DISPLAY_NAME};
        // Puedes añadir MediaStore.MediaColumns.RELATIVE_PATH si quieres verificarlo en el cursor

        // Cláusula WHERE para bucar por nombre y ruta relativa (solo en Android Q+)
        StringBuilder selectionBuilder = new StringBuilder();
        selectionBuilder.append(MediaStore.MediaColumns.DISPLAY_NAME + " = ?");

        // Usaremos una lista para los argumentos de selección porque el tamaño puede variar
        ArrayList<String> selectionArgsList = new ArrayList<>();
        selectionArgsList.add(nombreArchivo);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            selectionBuilder.append(" AND " + MediaStore.MediaColumns.RELATIVE_PATH + " LIKE ?");
            // Usamos LIKE con % al final porque MediaStore a veces añade una barra al final de relative_path
            // y a veces no, dependiendo de cómo se insertó.
            // También, asegúrate de que relativePath no tenga una barra al inicio si no la esperas.
            String formatedRelativePath = "";
            if(relativePath.endsWith("/"))
                formatedRelativePath = relativePath;
            else
                formatedRelativePath = relativePath + "/";

            // Asegúrate de que no haya un % al inicio si no es intencional para relativePath
            // MediaStore a veces no tiene la barra al final, por eso el % al final es útil.
            selectionArgsList.add(formatedRelativePath + "%");      // Ajustado para que el % sea al final
        }else{
            // Para versiones < Q, la búsqueda por ruta es más compleja y generalmente
            // implica consultar MediaStore.MediaColumns.DATA (ruta absoluta del archivo).
            // Esta función está más orientada a Q+, pero podrías adaptarla.
            // Si collectionUri es MediaStore.Files.getContentUri("external"),
            // podrías intentar buscar por DATA si tienes la ruta completa.
            // Por simplicidad, aquí no se implementa completamente para < Q.
        }

        String selection = selectionBuilder.toString();
        String[] selectionArgs = selectionArgsList.toArray(new String[0]);        // Convertir la lista a un array

        // Es buena idea hacer Log de la consulta para depurar
        Log.i(TAG_CLASE, "Query: " + selection);
        Log.i(TAG_CLASE, "Args: " + Arrays.toString(selectionArgs));
        Log.i(TAG_CLASE, "Collection: " + collectionUri.toString());

        Cursor cursor = null;
        try {
            cursor = resolver.query(collectionUri, projection, selection, selectionArgs, null);
            if (cursor.moveToFirst()) {
                int idColumn = cursor.getColumnIndex(MediaStore.MediaColumns._ID);
                long id = cursor.getInt(idColumn);
                existingFileUri = ContentUris.withAppendedId(collectionUri, id);
                // Opcional: Verificar más estrictamente el relative_path si es necesario
                // if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                //     val relativePathColumn = cursor.getColumnIndex(MediaStore.MediaColumns.RELATIVE_PATH)
                //     if (relativePathColumn != -1) {
                //         val pathInDb = cursor.getString(relativePathColumn)
                //         // Compara pathInDb con tu relativePath esperado de forma más precisa si es necesario
                //     }
                // }
            }
        }catch (Exception e){
            msn = "Error al buscar el archivo: " + e.getMessage();
            Log.e(TAG_CLASE, msn, e);
        }finally {
            if(cursor != null)
                cursor.close();        // Asegúrate de cerrar el cursor
        }
        return existingFileUri;
    }

    // ################ GESTION DE IMAGENES ####################

    /**
     * Cambia de tamaño una foto Bitmap para mostrarlar en el imageView
     *
     *@param activity Contexto de la aplicación.
     * @param subCarpeta Carpeta secundaria dentro de la zona de memoria elegida.
     * @param nombreArchivo Nombre del archivo a crear/sobrescribir.
     * @param memo Zona de memoria elegida.
     * @param tipoArchivo Según el tipo de archivos se guardará en una carpeta u otra.
     * @return Bitmap extraido del File.
     * */
    public static Bitmap obtenerBitmapDeFile(Activity activity, String subCarpeta, String nombreArchivo, int memo, String tipoArchivo){
        File origen = GestorArchivos.obtenerArchivoDir(activity, subCarpeta, nombreArchivo, memo, tipoArchivo);
        Bitmap bitmap = BitmapFactory.decodeFile(origen.getPath());

        return bitmap;
    }

    /**
     * Copia la imagen de un archivo a otro
     *
     * @param activity Contexto de la aplicación.
     * @param fileOrigen Archivo de la imagen original.
     * @param fileDestino Archivo de la imagen destino.
     * @return true si se ha copiado correctamente, false en caso contrario.
     */
    public static boolean copiarImagenDeFileAFile(Activity activity, File fileOrigen, File fileDestino){
        Bitmap bitmap = BitmapFactory.decodeFile(fileOrigen.getPath());
        boolean okGuardar = GestorArchivos.guardarImagen(activity, bitmap, fileDestino);

        return okGuardar;
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

    /**
     * Devuelve un intent para lanzar la cámara enviando el Uri donde se guardará la imagen
     *
     * @param activity Contexto de la aplicación.
     * @param subCarpeta Carpeta secundaria dentro de la zona de memoria elegida.
     * @param nombreArchivo Nombre del archivo a crear/sobrescribir.
     * @param tipoArchivo Según el tipo de archivos se guardará en una carpeta u otra.
     * @return Intent para lanzar la cámara o null si no se puede lanzar porque no hay cámara.*/
    @SuppressLint("QueryPermissionsNeeded")
    public static Intent obtenerFotoIntentLauncher(Activity activity, String subCarpeta, String nombreArchivo, int memo, String tipoArchivo) {
        Intent intent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        if(intent.resolveActivity(activity.getPackageManager()) != null){
            Uri uriImagenArchivoCamara = Uri.fromFile(new File(obtenerCarpetaArchivos(activity, memo, tipoArchivo), subCarpeta + "/" + nombreArchivo));
            intent.putExtra(MediaStore.EXTRA_OUTPUT, uriImagenArchivoCamara);
            return intent;
        }else{
            return null;
        }
    }

    /**
     * Guardca un Bitmap en la dirección determinada por el File
     *
     * @param activity Contexto de la aplicación.
     * @param bitmap Imagen a guardar.
     * @param fileDestino Dirección del archivo donde se guardará la imagen.
     * @return bolean true si se guardó correctamente, false en caso contrario.*/
    public static boolean guardarImagen(Activity activity, Bitmap bitmap, File fileDestino) {
        OutputStream os = null;
        try {
            os = new FileOutputStream(fileDestino);
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, os);
            os.flush();
            os.close();
            msn = "Imagen guardada en: " + fileDestino.getAbsolutePath();
            Log.i(TAG_CLASE, msn);
            return true;
        }catch (IOException ioe){
            msn = "Error al guardar la imagen: " + ioe.getMessage();
            Log.e(TAG_CLASE, msn, ioe);
        }
        return false;
    }

    // ############################ Setters y Getters #############################
    public void setMsn(String mensaje){
        msn = mensaje;
    }

    public static String getMsn(){
        return msn;
    }
}
