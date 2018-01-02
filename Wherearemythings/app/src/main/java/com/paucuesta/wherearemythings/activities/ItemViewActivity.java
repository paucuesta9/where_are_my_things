package com.paucuesta.wherearemythings.activities;

import android.content.ContentValues;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.paucuesta.wherearemythings.R;
import com.paucuesta.wherearemythings.models.Category;
import com.paucuesta.wherearemythings.models.Item;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;

public class ItemViewActivity extends AppCompatActivity {

    private Realm realm;

    private int itemId;
    private Item item;
    private RealmList<Item> items;

    private ImageView imageViewPhoto;
    private TextView textViewName;
    private TextView textViewDescription;
    private TextView textViewPlace;
    private TextView textViewPlace2;
    private TextView textViewQuantityNum;
    private Button buttonEdit;
    private Button buttonDelete;

    private static final int RESULT_LOAD_IMAGE = 2;
    static final int REQUEST_TAKE_PHOTO = 1;

    String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item_view);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        realm = Realm.getDefaultInstance();

        if (getIntent().getExtras() != null) {
            itemId = getIntent().getExtras().getInt("id");
        }

        item = realm.where(Item.class).equalTo("id", itemId).findFirst();

        setTitleActivity(item.getName());

        imageViewPhoto = (ImageView) findViewById(R.id.imageViewItemActivityPhoto);
        textViewName = (TextView) findViewById(R.id.textViewItemActivityName);
        textViewDescription = (TextView) findViewById(R.id.textViewItemActivityDescription);
        textViewPlace = (TextView) findViewById(R.id.textViewItemActivityPlace);
        textViewQuantityNum = (TextView) findViewById(R.id.textViewItemActivityQuantityNum);
        buttonEdit = (Button) findViewById(R.id.buttonItemActivityEdit);
        buttonDelete = (Button) findViewById(R.id.buttonItemActivityDelete);

        textViewName.setText(item.getName());
        textViewDescription.setText(item.getDescription());
        textViewPlace.setText(item.getPlace());
        textViewQuantityNum.setText(String.valueOf(item.getQuantity()));
        Picasso.with(this).load("file:///"+ item.getPhoto()).placeholder(R.drawable.placeholder).fit().into(imageViewPhoto);

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertForEditingBoard("Edit Item", "Change the name of the Item", item);
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItem(item);
            }
        });

    }

    private void editItem(String newName, String newDescription, String newPhoto, String newPlace, int newQuantityNum, Item item) {
        realm.beginTransaction();
        item.setName(newName);
        item.setDescription(newDescription);
        item.setPhoto(newPhoto);
        item.setPlace(newPlace);
        item.setQuantity(newQuantityNum);
        realm.copyToRealmOrUpdate(item);
        realm.commitTransaction();
    }

    private void deleteItem(Item item) {
        realm.beginTransaction();
        item.deleteFromRealm();
        realm.commitTransaction();
        Intent intent = new Intent(ItemViewActivity.this, CategoryActivity.class);
        startActivity(intent);
    }

    private void showAlertForEditingBoard(String title, String message, final Item item) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (title != null) builder.setTitle(title);
        if (message != null) builder.setMessage(message);

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_create_item, null);
        builder.setView(viewInflated);

        final EditText inputName = (EditText) viewInflated.findViewById(R.id.editTextNewItemName);
        final EditText inputDescription = (EditText) viewInflated.findViewById(R.id.editTextNewItemDescription);
        final Button btnTakePhoto = (Button) viewInflated.findViewById(R.id.btnNewItemTakePhoto);
        final EditText inputPlace = (EditText) viewInflated.findViewById(R.id.editTextNewItemPlace);
        final EditText inputQuantity = (EditText) viewInflated.findViewById(R.id.editTextNewItemQuantity);

        inputName.setText(item.getName());
        inputDescription.setText(item.getDescription());
        inputPlace.setText(item.getPlace());
        inputQuantity.setText(String.valueOf(item.getQuantity()));

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeTakePhoto();
            }
        });

        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String itemName = inputName.getText().toString().trim();
                String itemDescription = inputDescription.getText().toString().trim();
                String itemPhoto = mCurrentPhotoPath;
                String itemPlace = inputPlace.getText().toString().trim();
                int itemQuantity = Integer.parseInt(inputQuantity.getText().toString());
                if (inputName.length() == 0 && inputPlace.length() == 0 && inputQuantity.length() == 0) {
                    Toast.makeText(getApplicationContext(), "The inputs are required to edit the current Item", Toast.LENGTH_LONG).show();
                } else {
                    editItem(itemName, itemDescription, itemPhoto, itemPlace, itemQuantity, item);
                    textViewName.setText(item.getName());
                    textViewDescription.setText(item.getDescription());
                    textViewPlace.setText(item.getPlace());
                    textViewQuantityNum.setText(String.valueOf(item.getQuantity()));
                    setTitleActivity(item.getName());
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void setTitleActivity(String title) {
        this.setTitle(title);
    }

    private void activeTakePhoto() {
        dispatchTakePictureIntent();
    }

    private void dispatchTakePictureIntent() {
        Intent takePictureIntent = new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
        // Ensure that there's a camera activity to handle the intent
        if (takePictureIntent.resolveActivity(getPackageManager()) != null) {
            // Create the File where the photo should go
            File photoFile = null;
            try {
                photoFile = createImageFile();
            } catch (IOException ex) {
                // Error occurred while creating the File
            }
            // Continue only if the File was successfully created
            if (photoFile != null) {
                Uri photoURI = FileProvider.getUriForFile(this, "com.paucuesta.wherearemythings.fileprovider", photoFile);
                takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, photoURI);
                startActivityForResult(takePictureIntent, REQUEST_TAKE_PHOTO);
            }
        }
    }

    private File createImageFile() throws IOException {
        // Create an image file name
        String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
        String imageFileName = "JPEG_" + timeStamp + "_";
        File storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES);
        File image = File.createTempFile(
                imageFileName,  /* prefix */
                ".jpg",         /* suffix */
                storageDir      /* directory */
        );

        // Save a file: path for use with ACTION_VIEW intents
        mCurrentPhotoPath = image.getAbsolutePath();
        return image;
    }
}
