package pe.edu.usat.ecorutapac.util;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class ImageCompressor {

    public interface CompressionCallback {
        void onSuccess(File compressedFile);
        void onError(Exception e);
    }

    private static final ExecutorService executor = Executors.newFixedThreadPool(2);

    /**
     * Comprime de manera asíncrona una imagen desde una Uri y la guarda en el cache interno.
     */
    public static void comprimirAsync(Context context, Uri imageUri, CompressionCallback callback) {
        executor.execute(() -> {
            try {
                File outputFile = comprimirSincrono(context, imageUri, 1280, 80);
                if (outputFile != null && outputFile.exists()) {
                    callback.onSuccess(outputFile);
                } else {
                    callback.onError(new Exception("No se pudo comprimir la imagen"));
                }
            } catch (Exception e) {
                callback.onError(e);
            }
        });
    }

    public static File comprimirSincrono(Context context, Uri imageUri, int maxDimension, int quality) throws Exception {
        InputStream input = context.getContentResolver().openInputStream(imageUri);
        if (input == null) return null;

        // Decodificar solo los límites
        BitmapFactory.Options boundsOptions = new BitmapFactory.Options();
        boundsOptions.inJustDecodeBounds = true;
        BitmapFactory.decodeStream(input, null, boundsOptions);
        input.close();

        int originalWidth = boundsOptions.outWidth;
        int originalHeight = boundsOptions.outHeight;

        int scaleFactor = 1;
        while ((originalWidth / scaleFactor) > maxDimension || (originalHeight / scaleFactor) > maxDimension) {
            scaleFactor *= 2;
        }

        BitmapFactory.Options decodeOptions = new BitmapFactory.Options();
        decodeOptions.inSampleSize = scaleFactor;

        InputStream inputActual = context.getContentResolver().openInputStream(imageUri);
        Bitmap bitmap = BitmapFactory.decodeStream(inputActual, null, decodeOptions);
        if (inputActual != null) inputActual.close();

        if (bitmap == null) return null;

        // Guardar archivo comprimido en cache
        File storageDir = new File(context.getCacheDir(), "evidencias");
        if (!storageDir.exists()) {
            storageDir.mkdirs();
        }

        File destinationFile = new File(storageDir, "EVIDENCIA_" + UUID.randomUUID().toString() + ".jpg");
        FileOutputStream fos = new FileOutputStream(destinationFile);
        bitmap.compress(Bitmap.CompressFormat.JPEG, quality, fos);
        fos.flush();
        fos.close();
        bitmap.recycle();

        return destinationFile;
    }
}
