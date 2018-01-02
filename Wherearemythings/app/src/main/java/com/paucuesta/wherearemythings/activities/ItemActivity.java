package com.paucuesta.wherearemythings.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import com.paucuesta.wherearemythings.R;
import com.paucuesta.wherearemythings.adapters.ItemAdapter;
import com.paucuesta.wherearemythings.models.Category;
import com.paucuesta.wherearemythings.models.Item;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;

public class ItemActivity extends AppCompatActivity implements RealmChangeListener<Category>, AdapterView.OnItemClickListener {

    private ListView listView;
    private FloatingActionButton fab;

    private ItemAdapter adapter;
    private RealmList<Item> items;
    private Realm realm;

    private int categoryId;
    private Category category;


    private Uri mCapturedImageURI;
    private static final int RESULT_LOAD_IMAGE = 2;
    static final int REQUEST_TAKE_PHOTO = 1;

    String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_item);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        realm = Realm.getDefaultInstance();

        if (getIntent().getExtras() != null) {
            categoryId = getIntent().getExtras().getInt("id");
        }

        category = realm.where(Category.class).equalTo("id", categoryId).findFirst();
        category.addChangeListener(this);
        items = category.getItems();

        this.setTitle(category.getName());

        fab = (FloatingActionButton) findViewById(R.id.fabAddItem);
        listView = findViewById(R.id.listViewItem);
        adapter = new ItemAdapter(this, items, R.layout.list_view_item_item);

        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);

        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertForCreatingNote("Add new item", "Type a item for " + category.getName() + ".");
            }
        });
        registerForContextMenu(listView);
    }

    //CRUB Actions
    private void createNewItem(String itemName, String itemDescription, String itemPlace, int itemQuantity, String itemPhoto) {
        realm.beginTransaction();
        Item item = new Item(itemName, itemDescription, itemPlace, itemQuantity, itemPhoto);
        realm.copyToRealm(item);
        category.getItems().add(item);
        realm.commitTransaction();
    }

    private void deleteAll() {
        realm.beginTransaction();
        category.getItems().deleteAllFromRealm();
        realm.commitTransaction();
    }

    //Dialogs
    private void showAlertForCreatingNote(String title, String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (title != null) builder.setTitle(title);
        if (message != null) builder.setMessage(message);

        final View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_create_item, null);
        builder.setView(viewInflated);

        final EditText inputName = (EditText) viewInflated.findViewById(R.id.editTextNewItemName);
        final EditText inputDescription = (EditText) viewInflated.findViewById(R.id.editTextNewItemDescription);
        final Button btnTakePhoto = (Button) viewInflated.findViewById(R.id.btnNewItemTakePhoto);
        final EditText inputPlace = (EditText) viewInflated.findViewById(R.id.editTextNewItemPlace);
        final EditText inputQuantity = (EditText) viewInflated.findViewById(R.id.editTextNewItemQuantity);

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeTakePhoto();
            }
        });

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String itemName = inputName.getText().toString().trim();
                String itemDescription = inputDescription.getText().toString().trim();
                String itemPlace = inputPlace.getText().toString().trim();
                int itemQuantity = Integer.parseInt(inputQuantity.getText().toString());
                if (inputName.length() > 0 && inputPlace.length() > 0 && inputQuantity.length() > 0) {
                    createNewItem(itemName, itemDescription, itemPlace, itemQuantity, mCurrentPhotoPath);
                } else {
                    Toast.makeText(getApplicationContext(), "The inputs are required to create a new Item", Toast.LENGTH_LONG).show();
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity_item, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.deleteAll:
                deleteAll();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public void onChange(Category category) {
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(ItemActivity.this, ItemViewActivity.class);
        intent.putExtra("id", items.get(position).getId());
        startActivity(intent);
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


