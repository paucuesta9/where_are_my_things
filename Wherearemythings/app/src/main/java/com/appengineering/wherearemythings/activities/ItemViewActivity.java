package com.appengineering.wherearemythings.activities;

import android.content.DialogInterface;
import android.content.Intent;
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

import com.appengineering.wherearemythings.R;
import com.appengineering.wherearemythings.models.Item;
import com.squareup.picasso.Picasso;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmList;

public class ItemViewActivity extends AppCompatActivity {

    private Realm realm;

    private int itemId;
    private Item item;

    private ImageView imageViewPhoto;
    private TextView textViewName;
    private TextView textViewDescription;
    private TextView textViewPlace;
    private TextView textViewQuantityNum;
    private Button buttonEdit;
    private Button buttonDelete;

    static final int REQUEST_TAKE_PHOTO = 1;
    String mCurrentPhotoPath;
    private int photoType;

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
        photoType = item.getPhotoType();

        if (item.getPhotoType() == 1) {
            Picasso.with(this).load("file:///" + item.getPhoto()).placeholder(R.drawable.placeholder).into(imageViewPhoto);
        } else if (item.getPhotoType() == 2){
            Picasso.with(this).load(item.getPhoto()).placeholder(R.drawable.placeholder).into(imageViewPhoto);
        }

        buttonEdit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertForEditingItem(getString(R.string.edit_item), getString(R.string.edit_item_message), item);
            }
        });

        buttonDelete.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                deleteItem(item);
            }
        });

    }

    private void editItem(String newName, String newDescription, String newPhoto, String newPlace, int newQuantityNum, int newPhotoType, Item item) {
        realm.beginTransaction();
        item.setName(newName);
        item.setDescription(newDescription);
        item.setPhoto(newPhoto);
        item.setPlace(newPlace);
        item.setQuantity(newQuantityNum);
        item.setPhotoType(newPhotoType);
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

    private void showAlertForEditingItem(String title, String message, final Item item) {

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
        final EditText inputPhoto = (EditText) viewInflated.findViewById(R.id.editTextPhotoWebItem);

        inputName.setText(item.getName());
        inputDescription.setText(item.getDescription());
        inputPlace.setText(item.getPlace());
        inputQuantity.setText(String.valueOf(item.getQuantity()));

        mCurrentPhotoPath = item.getPhoto();
        if (item.getPhotoType() == 2) {
            inputPhoto.setText(mCurrentPhotoPath);
        }

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeTakePhoto();
                photoType = 1;
                inputPhoto.setText("");
            }
        });

        builder.setPositiveButton(getString(R.string.save), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String itemName = inputName.getText().toString().trim();
                String itemDescription = inputDescription.getText().toString().trim();
                String itemPlace = inputPlace.getText().toString().trim();
                int itemQuantity = Integer.parseInt(inputQuantity.getText().toString());
                String itemPhoto = mCurrentPhotoPath;
                if (inputPhoto.getText().toString().trim().length() > 0){
                    mCurrentPhotoPath = inputPhoto.getText().toString().trim();
                    photoType = 2;
                } else {
                    photoType = 1;
                }
                if (itemName.length() == 0 && itemQuantity == 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.item_edit_name_required), Toast.LENGTH_LONG).show();
                } else {
                    editItem(itemName, itemDescription, itemPhoto, itemPlace, itemQuantity, photoType, item);
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
                Uri photoURI = FileProvider.getUriForFile(this, "com.appengineering.wherearemythings.fileprovider", photoFile);
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
