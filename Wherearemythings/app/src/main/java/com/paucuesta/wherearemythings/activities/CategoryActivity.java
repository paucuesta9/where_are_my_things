package com.paucuesta.wherearemythings.activities;

import android.app.SearchManager;
import android.content.ContentValues;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.database.Cursor;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AbsListView;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.SearchView;
import android.widget.Toast;

import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.paucuesta.wherearemythings.R;
import com.paucuesta.wherearemythings.adapters.CategoryAdapter;
import com.paucuesta.wherearemythings.adapters.ItemAdapter;
import com.paucuesta.wherearemythings.models.Category;
import com.paucuesta.wherearemythings.models.Item;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmList;
import io.realm.RealmResults;

public class CategoryActivity extends AppCompatActivity implements RealmChangeListener<RealmResults<Category>>, AdapterView.OnItemClickListener {

    //Realm
    private Realm realm;

    //UI
    private FloatingActionButton fab;

    private ListView listView;
    private ListView listView2;

    private CategoryAdapter adapter;
    private ItemAdapter itemadapter;

    private RealmResults<Category> categories;
    private RealmResults<Item> items;

    private Category category;
    private Item item;

    private MaterialSearchView searchView;

    static final int REQUEST_TAKE_PHOTO = 1;
    String mCurrentPhotoPath;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);

        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Db realm
        Realm.init(this);
        realm = Realm.getDefaultInstance();
        categories = realm.where(Category.class).findAll();
        categories.addChangeListener(this);

        adapter = new CategoryAdapter(this, categories, R.layout.list_view_category_item);
        itemadapter = new ItemAdapter(this, items, R.layout.list_view_item_item);
        listView = (ListView) findViewById(R.id.listViewCategory);
        listView2 = (ListView) findViewById(R.id.listViewCategoryItem);
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
        listView2.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent(CategoryActivity.this, ItemViewActivity.class);
                intent.putExtra("id", items.get(position).getId());
                startActivity(intent);
            }
        });

        fab = (FloatingActionButton) findViewById(R.id.fabAddCategory);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertForCreatingBoard("Add new category", "Type a name and take a photo for your new category");
            }
        });

        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setEllipsize(true);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                categories = realm.where(Category.class).equalTo("name", query).findAll();
                items = realm.where(Item.class).equalTo("name", query).findAll();

                adapter = new CategoryAdapter(CategoryActivity.this, categories, R.layout.list_view_category_item);
                itemadapter = new ItemAdapter(CategoryActivity.this, items, R.layout.list_view_item_item);

                listView.setAdapter(adapter);
                listView2.setAdapter(itemadapter);

                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                categories = realm.where(Category.class).equalTo("name", newText).findAll();
                items = realm.where(Item.class).equalTo("name", newText).findAll();

                adapter = new CategoryAdapter(CategoryActivity.this, categories, R.layout.list_view_category_item);
                itemadapter = new ItemAdapter(CategoryActivity.this, items, R.layout.list_view_item_item);

                listView.setAdapter(adapter);
                listView2.setAdapter(itemadapter);
                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            public void onSearchViewShown() {
                //Do some magic
            }

            @Override
            public void onSearchViewClosed() {
                categories = realm.where(Category.class).findAll();
                categories.addChangeListener(CategoryActivity.this);
                adapter = new CategoryAdapter(CategoryActivity.this, categories, R.layout.list_view_category_item);
                listView = (ListView) findViewById(R.id.listViewCategory);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(CategoryActivity.this);
            }
        });

        registerForContextMenu(listView);
    }

    //CRUB Actions
    private void createNewCategory(String categoryName, String categoryPhoto) {
        realm.beginTransaction();
        Category category = new Category(categoryName, categoryPhoto);
        realm.copyToRealm(category);
        realm.commitTransaction();
    }

    private void editCategory(String newName, String newPhoto, Category category) {
        realm.beginTransaction();
        category.setName(newName);
        category.setPhoto(newPhoto);
        realm.copyToRealmOrUpdate(category);
        realm.commitTransaction();
    }

    private void deleteCategory(Category category) {
        realm.beginTransaction();
        category.getItems().deleteAllFromRealm();
        category.deleteFromRealm();
        realm.commitTransaction();
    }

    private void deleteAll() {
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
    }

    //Dialogs
    private void showAlertForCreatingBoard(String title, String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (title != null) builder.setTitle(title);
        if (message != null) builder.setMessage(message);

        final View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_create_category, null);
        builder.setView(viewInflated);

        final EditText inputName = (EditText) viewInflated.findViewById(R.id.editTextNewCategoryName);
        final Button btnTakePhoto = (Button) viewInflated.findViewById(R.id.btnNewCategoryTakePhoto);
        mCurrentPhotoPath = null;

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeTakePhoto();
            }
        });

        builder.setPositiveButton("Add", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String categoryName = inputName.getText().toString().trim();
                if (categoryName.length() > 0) {
                        createNewCategory(categoryName, mCurrentPhotoPath);
                } else {
                    Toast.makeText(getApplicationContext(), "The name is required to create a new Category", Toast.LENGTH_LONG).show();
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }



    private void showAlertForEditingBoard(String title, String message, final Category category) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (title != null) builder.setTitle(title);
        if (message != null) builder.setMessage(message);

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_create_category, null);
        builder.setView(viewInflated);

        final EditText input = (EditText) viewInflated.findViewById(R.id.editTextNewCategoryName);
        final Button btnTakePhoto = (Button) viewInflated.findViewById(R.id.btnNewCategoryTakePhoto);

        input.setText(category.getName());
        mCurrentPhotoPath = category.getPhoto();

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeTakePhoto();
            }
        });


        builder.setPositiveButton("Save", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String categoryName = input.getText().toString().trim();
                String categoryPhoto = mCurrentPhotoPath;
                if (categoryName.length() == 0) {
                    Toast.makeText(getApplicationContext(), "The name or the description is required to edit the current Category", Toast.LENGTH_LONG).show();
                } else {
                    editCategory(categoryName, categoryPhoto, category);
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    //Events


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_activity, menu);
        MenuItem item = menu.findItem(R.id.action_search);
        searchView.setMenuItem(item);

        return true;
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
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenu.ContextMenuInfo menuInfo) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) menuInfo;
        menu.setHeaderTitle(categories.get(info.position).getName());
        getMenuInflater().inflate(R.menu.context_menu_board_activity, menu);
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        AdapterView.AdapterContextMenuInfo info = (AdapterView.AdapterContextMenuInfo) item.getMenuInfo();
        switch (item.getItemId()) {
            case R.id.deleteCategory:
                deleteCategory(categories.get(info.position));
                return true;
            case R.id.editCategory:
                showAlertForEditingBoard("Edit Category", "Change the name of the Category", categories.get(info.position));
                return true;
            default:
                return super.onContextItemSelected(item);
        }

    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Intent intent = new Intent(CategoryActivity.this, ItemActivity.class);
        intent.putExtra("id", categories.get(position).getId());
        startActivity(intent);
    }

    @Override
    public void onChange(RealmResults<Category> categories) {
        adapter.notifyDataSetChanged();
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

    private ArrayList<Category> filter(RealmResults<Category> categories, String text) {
        ArrayList<Category> filterList = new ArrayList<>();
        try {
            for (Category category: categories) {
                String category2 = category.getName();

                if (category2.contains(text)) {
                    filterList.add(category);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return filterList;
    }
}