package developer.moein.simplemlkit;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.FirebaseApp;
import com.google.firebase.ml.vision.FirebaseVision;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcode;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetector;
import com.google.firebase.ml.vision.barcode.FirebaseVisionBarcodeDetectorOptions;
import com.google.firebase.ml.vision.common.FirebaseVisionImage;
import net.alhazmy13.mediapicker.Image.ImagePicker;

import java.util.List;
//TODO (4) start coding
public class MainActivity extends AppCompatActivity {

    FirebaseVisionBarcodeDetectorOptions options;
    FirebaseVisionBarcodeDetector detector;
    Button choose;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //initialize your app:
        FirebaseApp.initializeApp(this);

        options = new FirebaseVisionBarcodeDetectorOptions.Builder()
                .setBarcodeFormats(FirebaseVisionBarcode.FORMAT_ALL_FORMATS)
                .build();
        detector = FirebaseVision.getInstance()
                .getVisionBarcodeDetector(options);
        choose = findViewById(R.id.chooser);

        choose.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                //start image picture:
                new ImagePicker.Builder(MainActivity.this)
                        .mode(ImagePicker.Mode.CAMERA_AND_GALLERY)
                        .compressLevel(ImagePicker.ComperesLevel.MEDIUM)
                        .directory(ImagePicker.Directory.DEFAULT)
                        .scale(600, 600)
                        .allowMultipleImages(false)
                        .enableDebuggingMode(true)
                        .build();
            }
        });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        //Getting selected image:
        if (requestCode == ImagePicker.IMAGE_PICKER_REQUEST_CODE && resultCode == RESULT_OK) {
            List<String> mPaths = (List<String>) data.getSerializableExtra(ImagePicker.EXTRA_IMAGE_PATH);
            //Create a bitmap from file path:
            Bitmap bitmap = BitmapFactory.decodeFile(mPaths.get(0));

            SetBitmap(bitmap);
        }
    }

    private void SetBitmap(Bitmap bitmap){
        //Create an FireBaseVisionImage from bitmap
        FirebaseVisionImage image = FirebaseVisionImage.fromBitmap(bitmap);
        //Getting the info
        GetBarcode(image);
    }

    /*
    Credit: MoeinDeveloper
    Mon,Jul 16
    Google Machine Learning kit known as MLKit!
    Barcode scanner part!
     */
    private void GetBarcode(FirebaseVisionImage image){
        //Send request to the server and get the information
        Task<List<FirebaseVisionBarcode>> result= detector.detectInImage(image)
                .addOnSuccessListener(new OnSuccessListener<List<FirebaseVisionBarcode>>() {
                    @Override
                    public void onSuccess(List<FirebaseVisionBarcode> barcodes) {
                        for (FirebaseVisionBarcode barcode : barcodes){
                            Log.d("type", barcode.getDisplayValue());
                            //Get the type
                            int type = barcode.getValueType();
                            //Parsing types:
                            switch (type){
                                case FirebaseVisionBarcode.TYPE_URL:
                                    //open url using intent
                                    Intent view = new Intent(Intent.ACTION_VIEW);
                                    view.setData(Uri.parse(barcode.getDisplayValue()));
                                    startActivity(view);
                                    break;
                                    default:
                                        Toast.makeText(MainActivity.this, barcode.getDisplayValue(), Toast.LENGTH_SHORT).show();
                                        break;
                            }
                        }
                    }
                }).addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(MainActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
    }

}
