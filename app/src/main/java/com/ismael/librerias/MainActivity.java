package com.ismael.librerias;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
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
import java.io.OutputStream;
import java.nio.charset.StandardCharsets;

public class MainActivity extends AppCompatActivity {
    private final String TAG_INFO = "TAG_MainActivity";
    private final String TAG_CONTENIDO = "CONTENIDO";
    private final String TAG_NOMBRE_ARCHIVO = "ARCHIVO";

    private EditText txt_texto;
    private TextView lbl_texto;
    private Button btn_guardar, btn_leer, btn_foto;
    private ImageView imageView;

//    private final String SUB_CARPETA = "MiAppFotos";
    private final String SUB_CARPETA = "MiAppFotos";
    private final String NOMBRE_ARCHIVO_FOTO = "MiArchivoImagenDePrueba";;
    private final String NOMBRE_ARCHIVO_DOCUMENTO = "MiArchivoDocumento.txt";

    private File rutaArchivoActual;
    private Uri uriImagenArchivoCamara;

    private String msn = "";
    private String contenido = "";

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
    }

    private void permisos() {
        GestorPermisos.obtenerPermisoEscritura(this);
        GestorPermisos.obtenerPermisoLectura(this);
        GestorPermisos.obtenerPermisoCamara(this);
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
                accion_btn_guardar2();
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
            }
        });
    }

    private void accion_btn_guardar2() {
        contenido = txt_texto.getText().toString();
        boolean guardado = false;

        if(contenido != null && !contenido.isEmpty()){
            Intent contenidoIntent = new Intent(Intent.ACTION_CREATE_DOCUMENT);
            contenidoIntent.addCategory(Intent.CATEGORY_OPENABLE);
            contenidoIntent.setType("text/plain");  // Tipo MIME para archivos de texto
            contenidoIntent.putExtra(Intent.EXTRA_TITLE, NOMBRE_ARCHIVO_DOCUMENTO); // EXTRA_TITLE para especificar el nombre del archivo

            // Opcional: Especificar un directorio inicial (puede que no todos los selectores lo respeten)
            Uri documentsUri = null; // Por ejemplo, directorio de descargas
            // PARA VERSIONES >= Q
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                documentsUri = MediaStore.Downloads.EXTERNAL_CONTENT_URI;
            }else
            // PARA VERSIONES < Q
            if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                try {
                    documentsUri = GestorArchivos.obtenerDirExterno(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO).toURI();
                } catch (IOException ioe) {
                    ioe.printStackTrace();
                }
            }

            contenidoIntent.putExtra(DocumentsContract.EXTRA_INITIAL_URI, documentsUri);

            /* Lanza el explorador para que puedas seleccionar un archivo.
             * El nombre del archivo se especifica en el intent con EXTRA_TITLE.*/
            crearDocumentoLauncher.launch(contenidoIntent);
        }
    }

    private void accion_btn_guardar() {
        contenido = txt_texto.getText().toString();
        boolean guardado = false;

        if(contenido != null && !contenido.isEmpty()) {
            guardado = GestorArchivos.guardarTextoExternoFueraAmbito(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO, contenido);
            if (guardado) {
                Toast.makeText(this, "El texto se ha guardado correctamente", Toast.LENGTH_LONG).show();
//                txt_texto.setText("");
            } else
                Toast.makeText(this, "El texto no se pudo guardar", Toast.LENGTH_LONG).show();
        } else {
            Toast.makeText(this, "No hay texto para grabar", Toast.LENGTH_LONG).show();
        }
    }

    private void accion_btn_leer() {
        String texto = GestorArchivos.leerTextoExterno(this, SUB_CARPETA, NOMBRE_ARCHIVO_DOCUMENTO);
        if(texto != null && !texto.isEmpty() && !texto.contains("Error:")){
            Toast.makeText(this, "El texto ha sido leído correctamente", Toast.LENGTH_LONG).show();
            lbl_texto.setText(texto);
        }else if(texto.contains("Error"))
            Toast.makeText(this, texto, Toast.LENGTH_LONG).show();

    }

    private void accion_btn_foto(){
        Intent pictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Asegúrate de que hay una actividad de cámara para manejar el intent
        if (pictureIntent.resolveActivity(getPackageManager()) != null){
            if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                medianteURI();
            }else if(Build.VERSION.SDK_INT < Build.VERSION_CODES.Q){
                medienteIntent(pictureIntent);
            }
        }else{
            Toast.makeText(this, "No se encontró aplicación de cámara.", Toast.LENGTH_SHORT).show();
        }
    }

    private void medianteURI() {
        String msn = "";

        try {
            rutaArchivoActual = GestorArchivos.obtenerFilesDirExterno(this, SUB_CARPETA, NOMBRE_ARCHIVO_FOTO);
            uriImagenArchivoCamara = GestorArchivos.obtenerUriExterna(this, rutaArchivoActual);
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

    private void medienteIntent(Intent camaraIntent) {
        String msn = "";
        try {
//        rutaArchivoActual = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES), SUB_CARPETA + "/" + NOMBRE_ARCHIVO_FOTO);
            rutaArchivoActual = GestorArchivos.obtenerDirExterno(this, SUB_CARPETA, NOMBRE_ARCHIVO_FOTO);

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
            }
        }catch (IOException ioe){
            msn = "No se ha podido crear o no se ha encontrado la carpeta.";
            Log.e(TAG_INFO, msn, ioe);
            Toast.makeText(this, msn, Toast.LENGTH_LONG).show();
        }
    }

    // Lanzador de la cámara mediante un Intent que contiene el Uri del Provider donde se guardará la imagen sacada con la cámara
    private final ActivityResultLauncher<Intent> camaraIntentLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result ->{
                int resultCode = result.getResultCode();
                if(resultCode == RESULT_OK){
                    if(rutaArchivoActual != null && uriImagenArchivoCamara != null){
                        Log.i(TAG_INFO, "Foto guardada en:" + rutaArchivoActual);
                        // uriImagenArchivoCamara podría ser un content:// URI de FileProvider
                        // o un file:// URI en versiones muy antiguas.
                        // Para mostrarla, es más seguro usar la rutaArchivoActual si la tienes,
                        // o el uriImagenArchivoCamara.
                        imageView.setImageURI(Uri.fromFile(rutaArchivoActual)); // O directamente uriImagenArchivoCamara
                        lbl_texto.setText(rutaArchivoActual.getPath());
                    }else {
                        Log.i(TAG_INFO, "RESULT_OK pero URI o ruta son nulos.");
                        Toast.makeText(this, "Error: Datos de imagen nulos.", Toast.LENGTH_SHORT).show();
                    }
                }else if (result.getResultCode() == Activity.RESULT_CANCELED) {
                    Log.i(TAG_INFO, "Captura cancelada.");
                    Toast.makeText(this, "Captura cancelada.", Toast.LENGTH_SHORT).show();
                    // Opcionalmente, eliminar el archivo si se creó
                    if (rutaArchivoActual != null) {
                        File f = rutaArchivoActual;
                        if (f.exists() && f.length() == 0) f.delete();
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
                    Bitmap bitmap = null;
                    // Mostrar la imagen en el ImageView
                    // Puedes cargarla directamente desde la ruta o el URI
                    bitmap = BitmapFactory.decodeFile(rutaArchivoActual.getPath());

                    if (bitmap != null) {
                        // Antes de llamar a setImageBitmap:
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
                    } else {
                        // Aternativamente, si el decodeFile falla por alguna razón (raro si se guardó bien)
                        // imageView.setImageURI(uriImagenArchivo);
                        Log.i(TAG_INFO, "No se pudo decodificar el bitmap desde la ruta.");
                    }
                    Toast.makeText(this, "Foto guardada en: " + rutaArchivoActual, Toast.LENGTH_SHORT).show();
                }
            }
    );

    // Lanzador para guardar texto en un archivo en la memoria externa fuera del ámbito de la app
    private final ActivityResultLauncher<Intent> crearDocumentoLauncher = registerForActivityResult(new ActivityResultContracts.StartActivityForResult(),
            result ->{
                int resultCode = result.getResultCode();
                Intent dataIntent = result.getData();
                if(resultCode == Activity.RESULT_OK && dataIntent != null){
                    Uri uri = result.getData().getData();
                    if(uri != null){
                        GestorArchivos.escribirTextoEnUri(this, uri, contenido);
                        msn = GestorArchivos.getMsn();
                        Toast.makeText(this.getApplicationContext(), msn, Toast.LENGTH_LONG).show();

                    }else{
                        msn = "No se pudo obtener el URI.";
                        Toast.makeText(this.getApplicationContext(), msn, Toast.LENGTH_LONG).show();
                    }
                }else{
                    msn = "Guardado cancelado.";
                    Toast.makeText(this.getApplicationContext(), msn, Toast.LENGTH_LONG).show();
                }
            });
}