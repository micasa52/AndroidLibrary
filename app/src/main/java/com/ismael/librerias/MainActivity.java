package com.ismael.librerias;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.ismael.milibreriautilidades.GestorArchivos;
import com.ismael.milibreriautilidades.GestorPermisos;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {
    private final String TAG_INFO = "TAG_MainActivity";
//    private final String TAG_CONTENIDO = "CONTENIDO";
//    private final String TAG_NOMBRE_ARCHIVO = "ARCHIVO";

    private EditText txt_texto;
    private TextView lbl_texto;
    private Button btn_guardar, btn_leer, btn_foto;
    private ImageView imageView;

    private final String SUB_CARPETA = "MiAppFotos";
//    private final String SUB_CARPETA = "";
    private final String NOMBRE_ARCHIVO_FOTO = "MiArchivoImagenDePrueba.jpg";
    private final String NOMBRE_ARCHIVO_DOCUMENTO = "MiArchivoDocumento.txt";

    private File rutaArchivoActual;
    private Uri uriImagenArchivoCamara;

    private String msn = "";
    private String contenido = "";

    private MainActivity mainActivity = this;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_main);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });
        crearControles();
        addListeners();

        permisos();
        permisoAccesoAbsoluto();
    }

    private void permisoAccesoAbsoluto() {
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            // Solicitar permisos para almacenamiento externo
            if (!Environment.isExternalStorageManager()) {
                Intent intent =new  Intent(Settings.ACTION_MANAGE_APP_ALL_FILES_ACCESS_PERMISSION);
//                intent.setData(Uri.fromParts("package", getPackageName(), null));
                intent.setData(Uri.parse("package:" + getApplicationContext().getPackageName()));

                manageStoragePermissionLauncher.launch(intent); // Para Android 11 (API 30)
            }else{
                msn = "Permiso de acceso a todo el almacenamiento concedido";
            }
        }
    }

    private void permisos() {
        GestorPermisos.obtenerPermisoEscritura(this);
        msn = GestorPermisos.getMsn();
        GestorPermisos.obtenerPermisoLectura(this);
        msn += "\n" + GestorPermisos.getMsn();
        GestorPermisos.obtenerPermisoCamara(this);
        msn += "\n" + GestorPermisos.getMsn();
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R){
            Intent intent = GestorPermisos.obtenerTotalAccessPermisionIntent(this);
            if(intent != null)
                manageStoragePermissionLauncher.launch(intent);
            else{
                msn += "\nPermiso de acceso a todo el almacenamiento no completado";
            }
        }
        Log.i(TAG_INFO, msn);
    }

    private void crearControles() {
        txt_texto = findViewById(R.id.txt_texto);
        lbl_texto = findViewById(R.id.lbl_texto);
        btn_guardar = findViewById(R.id.btn_guardar);
        btn_leer = findViewById(R.id.btn_leer);
        btn_foto = findViewById(R.id.btn_foto);
        imageView = findViewById(R.id.imageView);
    }

    private void addListeners() {
        btn_guardar.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                accion_btn_guardar();
//                accion_btn_guardar2();
                accion_btn_guardar3();
            }
        });

        btn_leer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accion_btn_leer();
            }
        });

        btn_foto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                accion_btn_foto();
//                accion_btn_foto2();
            }
        });
    }

    // 1º mét0do de accion del btn_guardar
    private void accion_btn_guardar() {
        contenido = txt_texto.getText().toString();
        boolean guardado = false;

        if(contenido != null && !contenido.isEmpty()) {
//            guardado = GestorArchivos.guardarTextoExternoFueraAmbito(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO, contenido, GestorArchivos.MODO_INTERNO_AMBITO, GestorArchivos.TIPO_TEXTO);
//            guardado = GestorArchivos.guardarTextoExternoFueraAmbito(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO, contenido, GestorArchivos.MODO_INTERNO_CACHE, GestorArchivos.TIPO_TEXTO);
//            guardado = GestorArchivos.guardarTextoExternoFueraAmbito(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO, contenido, GestorArchivos.MODO_EXTERNO_AMBITO, GestorArchivos.TIPO_TEXTO);
//            guardado = GestorArchivos.guardarTextoExternoFueraAmbito(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO, contenido, GestorArchivos.MODO_EXTERNO_CACHE, GestorArchivos.TIPO_TEXTO);
//            guardado = GestorArchivos.guardarTextoExternoFueraAmbito(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO, contenido, GestorArchivos.MODO_EXTERNO_NO_AMBITO, GestorArchivos.TIPO_TEXTO);
//            guardado = GestorArchivos.saveTextToDowloads(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO, contenido);
            guardado = GestorArchivos.saveContentToUri(this, SUB_CARPETA, NOMBRE_ARCHIVO_FOTO, contenido, GestorArchivos.TIPO_IMAGEN);
            if (guardado) {
                msn = GestorArchivos.getMsn();
//                txt_texto.setText("");
            } else
                msn = GestorArchivos.getMsn();

            Toast.makeText(this, msn, Toast.LENGTH_LONG).show();
        } else {
            msn = "No se ha podido guardar el texto";
            Toast.makeText(this, msn, Toast.LENGTH_LONG).show();
        }
    }

    // 2º mét0do de accion del btn_guardar 
    private void accion_btn_guardar2() {
        contenido = txt_texto.getText().toString();
        boolean guardado = false;

        if(contenido != null && !contenido.isEmpty()){
            Intent contenidoIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            contenidoIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contenidoIntent.setType("text/plain");  // Tipo MIME para archivos de texto
            contenidoIntent.putExtra(Intent.EXTRA_TITLE, NOMBRE_ARCHIVO_DOCUMENTO); // EXTRA_TITLE para especificar el nombre del archivo

            // Opcional: Especificar un directorio inicial (puede que no todos los selectores lo respeten)
            Uri documentsUri = Uri.EMPTY; // Por ejemplo, directorio de descargas

            contenidoIntent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, documentsUri);

            /* Lanza el explorador para que puedas seleccionar un archivo.
             * El nombre del archivo se especifica en el intent con EXTRA_TITLE.*/
            crearDocumentoLauncher.launch(contenidoIntent);
        }
    }

    // 3º mét0do de acción del btn_guardar
    private void accion_btn_guardar3() {
        contenido = txt_texto.getText().toString();
        boolean okGuardar = false;

        if (contenido != null && !contenido.isEmpty()) {
//            okGuardar = GestorArchivos.guardarTexto(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO, contenido, GestorArchivos.MEMO_INT_AMBITO_FILES, GestorArchivos.TIPO_PRIVATE);
//            okGuardar = GestorArchivos.guardarTexto(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO, contenido, GestorArchivos.MEMO_INT_AMBITO_CACHE, GestorArchivos.TIPO_AUDIO);
//            okGuardar = GestorArchivos.guardarTexto(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO, contenido, GestorArchivos.MEMO_INT_AMBITO_RAIZ, GestorArchivos.TIPO_TEXTO);
//            okGuardar = GestorArchivos.guardarTexto(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO, contenido, GestorArchivos.MEMO_INT_NO_AMBITO, GestorArchivos.TIPO_NULO); // Para guardar un archivo en la memoria interna fuera del ambito de la app
//            okGuardar = GestorArchivos.guardarTexto(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO, contenido, GestorArchivos.MEMO_EXT_AMBITO_FILES, GestorArchivos.TIPO_TEXTO);
//            okGuardar = GestorArchivos.guardarTexto(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO, contenido, GestorArchivos.MEMO_EXT_AMBITO_CACHE, GestorArchivos.TIPO_TEXTO);
//            okGuardar = GestorArchivos.guardarTexto(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO, contenido, GestorArchivos.MEMO_EXT_AMBITO_RAIZ, GestorArchivos.TIPO_TEXTO);
//            okGuardar = GestorArchivos.guardarTexto(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO, contenido, GestorArchivos.MEMO_EXT_NO_AMBITO, GestorArchivos.TIPO_PRIVATE);

            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                okGuardar = GestorArchivos.guardarMayoresQ(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO, contenido, GestorArchivos.MEMO_EXT_NO_AMBITO, GestorArchivos.TIPO_TEXTO);
            }else{
                okGuardar = GestorArchivos.guardarMenoresQ(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO, contenido, GestorArchivos.MEMO_EXT_NO_AMBITO, GestorArchivos.TIPO_TEXTO);
            }
            String msn = GestorArchivos.getMsn();
            Toast.makeText(getApplicationContext(), msn, Toast.LENGTH_LONG).show();
        }else {
            String msn = "No hay texto para guardar";
            Toast.makeText(getApplicationContext(), msn, Toast.LENGTH_LONG).show();
        }
    }
    
    // Mét0do de accion del btn_leer
    private void accion_btn_leer() {
        String texto = "";

//        String texto = GestorArchivos.leerTextoExterno(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO, GestorArchivos.MEMO_INT_AMBITO_FILES, GestorArchivos.TIPO_PRIVATE);
//        String texto = GestorArchivos.leerTextoExterno(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO, GestorArchivos.MEMO_INT_AMBITO_CACHE, GestorArchivos.TIPO_AUDIO);
//        String texto = GestorArchivos.leerTextoExterno(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO, GestorArchivos.MEMO_INT_AMBITO_RAIZ, GestorArchivos.TIPO_TEXTO);
//        String texto = GestorArchivos.leerTextoExterno(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO, GestorArchivos.MEMO_INT_NO_AMBITO, GestorArchivos.TIPO_NULO); // Para leer un archivo en la memoria interna fuera del ambito de la app
//        String texto = GestorArchivos.leerTextoExterno(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO, GestorArchivos.MEMO_EXT_AMBITO_FILES, GestorArchivos.TIPO_TEXTO);
//        String texto = GestorArchivos.leerTextoExterno(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO, GestorArchivos.MEMO_EXT_AMBITO_CACHE, GestorArchivos.TIPO_TEXTO);
//        String texto = GestorArchivos.leerTextoExterno(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO, GestorArchivos.MEMO_EXT_AMBITO_RAIZ, GestorArchivos.TIPO_TEXTO);
//        String texto = GestorArchivos.leerTextoExterno(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO, GestorArchivos.MEMO_EXT_NO_AMBITO, GestorArchivos.TIPO_PRIVATE);
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
            texto = GestorArchivos.leerMayoresTIRAMISU(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO, GestorArchivos.MEMO_EXT_NO_AMBITO, GestorArchivos.TIPO_TEXTO);
        else
            texto = GestorArchivos.leerMenoresTIRAMISU(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO, GestorArchivos.MEMO_EXT_NO_AMBITO, GestorArchivos.TIPO_TEXTO);

        if (texto != null && !texto.isEmpty() && !texto.contains("Error:")) {
            Toast.makeText(this, "El texto ha sido leído correctamente", Toast.LENGTH_LONG).show();
            lbl_texto.setText(texto);
        } else if (texto.contains("Error"))
            Toast.makeText(this, texto, Toast.LENGTH_LONG).show();
    }
    
    // Mét0do de accion del btn_foto
    private void accion_btn_foto(){
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Asegúrate de que hay una actividad de cámara para manejar el intent
        if (pictureIntent.resolveActivity(getPackageManager()) != null){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                guardarImagenMedianteURI();
            }else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                medianteIntent(pictureIntent);
            }
        }else{
            Toast.makeText(this, "No se encontró aplicación de cámara.", Toast.LENGTH_SHORT).show();
        }
    }

    private void accion_btn_foto2(){
        Intent takePictureIntent = GestorArchivos.obtenerFotoIntentLauncher(this, SUB_CARPETA, NOMBRE_ARCHIVO_FOTO, GestorArchivos.MEMO_EXT_NO_AMBITO, GestorArchivos.TIPO_NULO);
        if (takePictureIntent != null) {
            camaraIntentLauncher.launch(takePictureIntent);
        }else{
            Toast.makeText(this, "No se encontró aplicación de cámara.", Toast.LENGTH_SHORT).show();
        }

    }

    private void guardarImagenMedianteURI() {
        try {
            rutaArchivoActual = GestorArchivos.obtenerArchivoDir(this, SUB_CARPETA, NOMBRE_ARCHIVO_FOTO, GestorArchivos.MEMO_INT_AMBITO_FILES, GestorArchivos.TIPO_IMAGEN);
            uriImagenArchivoCamara = GestorArchivos.obtenerDirUri(this, rutaArchivoActual);
            Log.i(TAG_INFO, "La footo será guardada en: " + uriImagenArchivoCamara.toString());

            // Pasar el URI a la cámara
            camaraUriLauncher.launch(uriImagenArchivoCamara);

        } catch (IOException ioe) {
            // Error durante la creación del archivo
            msn = "Error al crear el archivo de imagen";
            Log.e(TAG_INFO, msn);
            Toast.makeText(this.getApplicationContext(), msn, Toast.LENGTH_SHORT).show();
        }
    }

    private void medianteIntent(Intent camaraIntent) {
//        rutaArchivoActual = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), SUB_CARPETA + "/" + NOMBRE_ARCHIVO_FOTO);
//        rutaArchivoActual = GestorArchivos.obtenerDirExterno(this, SUB_CARPETA, NOMBRE_ARCHIVO_FOTO, GestorArchivos.MEMO_EXT_NO_AMBITO, GestorArchivos.TIPO_IMAGEN);
//        rutaArchivoActual = GestorArchivos.obtenerFilesDirExterno(this, SUB_CARPETA, NOMBRE_ARCHIVO_FOTO);
        rutaArchivoActual = GestorArchivos.obtenerArchivoDir(this, SUB_CARPETA, NOMBRE_ARCHIVO_FOTO, GestorArchivos.MEMO_INT_AMBITO_FILES, GestorArchivos.TIPO_IMAGEN);
        if (rutaArchivoActual != null) {
            // Para Android N (API 24) y superior, DEBES usar FileProvider.
            // Para < N, puedes usar Uri.fromFile().
            // Esta lógica es para < Q, así que FileProvider es relevante si targetSdk >= 24.
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                // Reemplaza "com.tudominio.nombredeapp.fileprovider" con tu autoridad real
                uriImagenArchivoCamara = FileProvider.getUriForFile(this,
                        "com.ismael.librerias.fileprovider", // TU AUTORIDAD DE FILEPROVIDER
                        rutaArchivoActual);
            } else {
                uriImagenArchivoCamara = Uri.fromFile(rutaArchivoActual);
            }

            camaraIntent.putExtra(MediaStore.EXTRA_OUTPUT, uriImagenArchivoCamara);
            // Otorgar permisos de URI a la app de cámara si es necesario (especialmente para FileProvider)
            // Esto se hace automáticamente por el sistema para ACTION_IMAGE_CAPTURE si el URI es de FileProvider
            // y el FileProvider está configurado con grantUriPermissions="true".
            // takePictureIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

            Log.i(TAG_INFO, "Lanzando cámara. URI: " + uriImagenArchivoCamara + ", Ruta: " + rutaArchivoActual.getPath());
            camaraIntentLauncher.launch(camaraIntent);
        }else {
            msn = GestorArchivos.getMsn();
            Toast.makeText(this, msn, Toast.LENGTH_LONG).show();
        }
    }

    private Bitmap accionRequestCamaraMenorQ() {
        Bitmap bitmap = GestorArchivos.obtenerBitmapDeFile(this, SUB_CARPETA, NOMBRE_ARCHIVO_FOTO, GestorArchivos.MEMO_INT_AMBITO_FILES, GestorArchivos.TIPO_IMAGEN);
        if(bitmap != null) {
            File origen = GestorArchivos.obtenerArchivoDir(this, SUB_CARPETA, NOMBRE_ARCHIVO_FOTO, GestorArchivos.MEMO_INT_AMBITO_FILES, GestorArchivos.TIPO_IMAGEN);
            File destino = GestorArchivos.obtenerArchivoDir(this, SUB_CARPETA, NOMBRE_ARCHIVO_FOTO, GestorArchivos.MEMO_EXT_NO_AMBITO, GestorArchivos.TIPO_NULO);

            boolean okCopiar = GestorArchivos.copiarImagenDeFileAFile(this, origen, destino);
            if(!okCopiar) {
                msn = GestorArchivos.getMsn();
                Toast.makeText(this, msn, Toast.LENGTH_LONG).show();
                return null;
            }

            bitmap = BitmapFactory.decodeFile(destino.getPath());
            bitmap = GestorArchivos.scaleBitmap(bitmap, 0.5f, 0.5f);
        }
        msn = GestorArchivos.getMsn();
        Toast.makeText(this, msn, Toast.LENGTH_LONG).show();

        return bitmap;
    }



    // Lanzador de la cámara mediante un Intent que contiene el Uri del Provider donde se guardará la imagen sacada con la cámara
    private final ActivityResultLauncher<Intent> camaraIntentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result ->{
                int resultCode = result.getResultCode();
                if(resultCode == RESULT_OK){
                    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
                        if (rutaArchivoActual != null && uriImagenArchivoCamara != null) {
                            Log.i(TAG_INFO, "Foto guardada en:" + rutaArchivoActual);
                            // uriImagenArchivoCamara podría ser un content:// URI de FileProvider
                            // o un file:// URI en versiones muy antiguas.
                            // Para mostrarla, es más seguro usar la rutaArchivoActual si la tienes,
                            // o el uriImagenArchivoCamara.
                            imageView.setImageURI(Uri.fromFile(rutaArchivoActual)); // O directamente uriImagenArchivoCamara
                            lbl_texto.setText(rutaArchivoActual.getPath());
                        } else {
                            Log.i(TAG_INFO, "RESULT_OK pero URI o ruta son nulos.");
                            Toast.makeText(this, "Error: Datos de imagen nulos.", Toast.LENGTH_SHORT).show();
                        }
                    }else
                    if(Build.VERSION.SDK_INT < Build.VERSION_CODES.R){
                        Bitmap bitmap = accionRequestCamaraMenorQ();
                        if(bitmap != null) {
                            imageView.setImageBitmap(bitmap);
                        }
                        Toast.makeText(this, msn, Toast.LENGTH_SHORT).show();
                    }
                }else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    Log.i(TAG_INFO, "Captura cancelada.");
                    Toast.makeText(this, "Captura cancelada.", Toast.LENGTH_SHORT).show();
                    // Opcionalmente, eliminar el archivo si se creó
                    if (rutaArchivoActual != null) {
                        File f = rutaArchivoActual;
                        f.delete();
                    }
                } else {
                    Log.i(TAG_INFO, "Captura fallida, resultCode: " + result.getResultCode());
                }
                // Limpiar para la próxima
                uriImagenArchivoCamara = null;
                rutaArchivoActual = null;
            });

    // Lanzador para la cámara
    private final ActivityResultLauncher<Uri> camaraUriLauncher = registerForActivityResult(new ActivityResultContracts.TakePicture(),
            exitoso ->{
                if(exitoso){
                    // Mostrar la imagen en el ImageView
                    // Puedes cargarla directamente desde la ruta o el URI
                    Bitmap bitmap = BitmapFactory.decodeFile(rutaArchivoActual.getPath());

                    if (bitmap != null) {
                        // Antes de llamar a setImageBitmap:
                        File destino = GestorArchivos.obtenerArchivoDir(this, SUB_CARPETA, NOMBRE_ARCHIVO_FOTO, GestorArchivos.MEMO_EXT_NO_AMBITO, GestorArchivos.TIPO_NULO);
                        boolean okCopiar = GestorArchivos.copiarImagenDeFileAFile(this, rutaArchivoActual, destino);
                        if(okCopiar) {
                            bitmap = BitmapFactory.decodeFile(destino.getPath());
                            float displayWidth = 0.5f; // Un tamaño razonable para la vista previa
                            float displayHeight = 0.5f;
                            Bitmap scaledBitmap = GestorArchivos.scaleBitmap(bitmap, displayWidth, displayHeight);
                            if (scaledBitmap != null) {
                                Log.i(TAG_INFO, "Bitmap dimensiones: " + scaledBitmap.getWidth() + "x" + scaledBitmap.getHeight());
                                imageView.setImageBitmap(scaledBitmap);
                                lbl_texto.setText(rutaArchivoActual.getPath());
                                Log.i(TAG_INFO, "Bitmap colocado en el imageView");
                            } else {
                                imageView.setImageBitmap(bitmap); // Fallback al original si el escalado falla
                                lbl_texto.setText(rutaArchivoActual.getPath());
                            }
                        }
                    } else {
                        // Aternativamente, si el decodeFile falla por alguna razón (raro si se guardó bien)
                        // imageView.setImageURI(uriImagenArchivo);
                        Log.i(TAG_INFO, "No se pudo decodificar el bitmap desde la ruta.");
                    }
                    Toast.makeText(this, "Foto guardada en: " + rutaArchivoActual, Toast.LENGTH_SHORT).show();
                }
            }
    );

    // Lanzador del explorador de archivos para seleccionar donde guardar texto en un archivo en la memoria externa fuera del ámbito de la app
    private final ActivityResultLauncher<Intent> crearDocumentoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result ->{
                int resultCode = result.getResultCode();
                Intent dataIntent = result.getData();
                if(resultCode == Activity.RESULT_OK && dataIntent != null){
                    Uri uri = result.getData().getData();
                    if(uri != null){
                        GestorArchivos.escribirTextoEnUriExterno(this, uri, contenido);
                        msn = GestorArchivos.getMsn();
                        Log.i(TAG_INFO, msn);
                        if(msn != null && !msn.isEmpty()) {
                            Toast.makeText(this.getApplicationContext(), msn, Toast.LENGTH_LONG).show();
                        }
                    }else{
                        msn = "No se pudo obtener el URI.";
                        Toast.makeText(this.getApplicationContext(), msn, Toast.LENGTH_LONG).show();
                    }
                }else{
                    msn = "Guardado cancelado.";
                    Toast.makeText(this.getApplicationContext(), msn, Toast.LENGTH_LONG).show();
                }
            });


    /**
     * Estos tres métodos son el mismo planteandos de diferentes formas:
     * 1. forma antigua con un mét0do Override que se llama al obtener un resultado en la petición
     *    del launcher
     * 2. forma un poco más moderna con una función lambda que se llama al obtener un resultado en
     *    la petición del launcher
     * 3. forma más moderna que sustituye la función lambda por un mét0do de instancia. Sólo es
     *    valido si el mét0do no devuelve nada*/
    /*
    private final ActivityResultLauncher<Intent> manageStoragePermissionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
                @Override
                public void onActivityResult(ActivityResult result) {
                    GestorPermisos.obtenerPermisoEscritura(result);
                    msn = GestorPermisos.getMsn();
                    Toast.makeText(this, msn, Toast.LENGTH_LONG).show();
                }
            });
    */

    private final ActivityResultLauncher<Intent> manageStoragePermissionLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result ->{
        GestorPermisos.comprobarPermisoEscritura(result);
        msn = GestorPermisos.getMsn();
        Toast.makeText(this, msn, Toast.LENGTH_LONG).show();
            });

    /*
    private final ActivityResultLauncher<Intent> manageStoragePermissionLauncher = registerForActivityResult(
            new ActivityResultContracts.StartActivityForResult(),
            GestorPermisos::obtenerPermisoEscritura);
    */
}