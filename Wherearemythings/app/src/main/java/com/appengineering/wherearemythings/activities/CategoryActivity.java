package com.appengineering.wherearemythings.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.provider.MediaStore;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.FileProvider;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.ContextMenu;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.Toast;

import com.miguelcatalan.materialsearchview.MaterialSearchView;
import com.appengineering.wherearemythings.R;
import com.appengineering.wherearemythings.adapters.CategoryAdapter;
import com.appengineering.wherearemythings.adapters.ItemAdapter;
import com.appengineering.wherearemythings.models.Category;
import com.appengineering.wherearemythings.models.Item;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import io.realm.Case;
import io.realm.Realm;
import io.realm.RealmChangeListener;
import io.realm.RealmResults;

public class CategoryActivity extends AppCompatActivity implements RealmChangeListener<RealmResults<Category>>, AdapterView.OnItemClickListener {

    //Realm
    private Realm realm;

    //UI
    private FloatingActionButton fab;
    private LinearLayout linearLayout;
    private ListView listView;
    private ListView listView2;

    //Adapters
    private CategoryAdapter adapter;
    private ItemAdapter itemadapter;

    // Lists
    private RealmResults<Category> categories;
    private RealmResults<Item> items;

    //SearchView
    private MaterialSearchView searchView;

    //Photos
    static final int REQUEST_TAKE_PHOTO = 1;
    String mCurrentPhotoPath;
    private int photoType;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_category);

        //Setting up the ToolBar
        Toolbar myToolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(myToolbar);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        //Db realm
        Realm.init(this);
        realm = Realm.getDefaultInstance();
        categories = realm.where(Category.class).findAll();
        categories.addChangeListener(this);

        //Adapter
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

        linearLayout = (LinearLayout) findViewById(R.id.linearLayoutAddNewCategory);
        if (categories.size() == 0) {
            linearLayout.setVisibility(View.VISIBLE);
        } else {
            linearLayout.setVisibility(View.INVISIBLE);
        }

        //FloatingActionButton
        fab = (FloatingActionButton) findViewById(R.id.fabAddCategory);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showAlertForCreatingCategory(getString(R.string.add_category), getString(R.string.add_category_message));
            }
        });

        //SearchView
        searchView = (MaterialSearchView) findViewById(R.id.search_view);
        searchView.setEllipsize(true);
        searchView.setOnQueryTextListener(new MaterialSearchView.OnQueryTextListener() {
            //When the users click the tick to search
            @Override
            public boolean onQueryTextSubmit(String query) {
                listView2.setVisibility(View.VISIBLE);

                categories = realm.where(Category.class).contains("name", query, Case.INSENSITIVE).findAll();
                items = realm.where(Item.class).contains("name", query, Case.INSENSITIVE).findAll();

                adapter = new CategoryAdapter(CategoryActivity.this, categories, R.layout.list_view_category_item);
                itemadapter = new ItemAdapter(CategoryActivity.this, items, R.layout.list_view_item_item);

                listView.setAdapter(adapter);
                listView2.setAdapter(itemadapter);

                return false;
            }
            //While the user is writting
            @Override
            public boolean onQueryTextChange(String newText) {
                if (newText.length() > 0) {
                    listView.setVisibility(View.VISIBLE);
                    listView2.setVisibility(View.VISIBLE);
                    categories = realm.where(Category.class).contains("name", newText, Case.INSENSITIVE).findAll();
                    items = realm.where(Item.class).contains("name", newText, Case.INSENSITIVE).findAll();

                    adapter = new CategoryAdapter(CategoryActivity.this, categories, R.layout.list_view_category_item);
                    itemadapter = new ItemAdapter(CategoryActivity.this, items, R.layout.list_view_item_item);

                    listView.setAdapter(adapter);
                    listView2.setAdapter(itemadapter);
                } else {
                    listView.setVisibility(View.INVISIBLE);
                    listView2.setVisibility(View.INVISIBLE);
                }

                return false;
            }
        });

        searchView.setOnSearchViewListener(new MaterialSearchView.SearchViewListener() {
            @Override
            //When the user clicks the search button
            public void onSearchViewShown() {
                //Do some magic
                listView.setVisibility(View.INVISIBLE);
                listView2.setVisibility(View.INVISIBLE);
            }
            //when the user closes the searchView
            @Override
            public void onSearchViewClosed() {
                categories = realm.where(Category.class).findAll();
                categories.addChangeListener(CategoryActivity.this);
                adapter = new CategoryAdapter(CategoryActivity.this, categories, R.layout.list_view_category_item);
                listView = (ListView) findViewById(R.id.listViewCategory);
                listView.setAdapter(adapter);
                listView.setOnItemClickListener(CategoryActivity.this);
                listView.setVisibility(View.VISIBLE);
                listView2.setVisibility(View.INVISIBLE);
            }
        });

        registerForContextMenu(listView);
    }

    @Override
    public void onBackPressed() {
        if (searchView.isSearchOpen()) {
            searchView.closeSearch();
            categories = realm.where(Category.class).findAll();
            categories.addChangeListener(CategoryActivity.this);
            adapter = new CategoryAdapter(CategoryActivity.this, categories, R.layout.list_view_category_item);
            listView = (ListView) findViewById(R.id.listViewCategory);
            listView.setAdapter(adapter);
            listView.setOnItemClickListener(CategoryActivity.this);
            listView.setVisibility(View.VISIBLE);
            listView2.setVisibility(View.INVISIBLE);
        } else {
            super.onBackPressed();
        }
    }

    //CRUB Actions
    private void createNewCategory(String categoryName, String categoryPhoto, int categoryPhotoType) {
        realm.beginTransaction();
        Category category = new Category(categoryName, categoryPhoto, categoryPhotoType);
        realm.copyToRealm(category);
        realm.commitTransaction();
        linearLayout.setVisibility(View.INVISIBLE);
    }

    private void editCategory(String newName, String newPhoto, int categoryPhotoType, Category category) {
        realm.beginTransaction();
        category.setName(newName);
        category.setPhoto(newPhoto);
        category.setPhotoType(categoryPhotoType);
        realm.copyToRealmOrUpdate(category);
        realm.commitTransaction();
    }

    private void deleteCategory(Category category) {
        realm.beginTransaction();
        category.getItems().deleteAllFromRealm();
        category.deleteFromRealm();
        realm.commitTransaction();
        if (categories.size() == 0) {
            linearLayout.setVisibility(View.VISIBLE);
        } else {
            linearLayout.setVisibility(View.INVISIBLE);
        }
    }

    private void deleteAll() {
        realm.beginTransaction();
        realm.deleteAll();
        realm.commitTransaction();
        linearLayout.setVisibility(View.VISIBLE);
    }

    //Dialogs
    private void showAlertForCreatingCategory(String title, String message) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (title != null) builder.setTitle(title);
        if (message != null) builder.setMessage(message);

        final View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_create_category, null);
        builder.setView(viewInflated);

        final EditText inputName = (EditText) viewInflated.findViewById(R.id.editTextNewCategoryName);
        final Button btnTakePhoto = (Button) viewInflated.findViewById(R.id.btnNewCategoryTakePhoto);
        final EditText inputPhoto = (EditText) viewInflated.findViewById(R.id.editTextPhotoWeb);
        mCurrentPhotoPath = null;

        btnTakePhoto.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                activeTakePhoto();
                photoType = 1;
            }
        });

        builder.setPositiveButton(getString(R.string.add), new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String categoryName = inputName.getText().toString().trim();
                if (mCurrentPhotoPath == null) {
                    if (inputPhoto.getText().toString().trim().length() > 0){
                        mCurrentPhotoPath = inputPhoto.getText().toString().trim();
                        photoType = 2;
                    } else {
                        photoType = 1;
                    }
                }
                if (categoryName.length() > 0) {
                        createNewCategory(categoryName, mCurrentPhotoPath, photoType);
                } else {
                    Toast.makeText(getApplicationContext(), getString(R.string.category_create_name_required), Toast.LENGTH_LONG).show();
                }
            }
        });

        AlertDialog dialog = builder.create();
        dialog.show();
    }

    private void showAlertForEditingCategory(String title, String message, final Category category) {

        AlertDialog.Builder builder = new AlertDialog.Builder(this);

        if (title != null) builder.setTitle(title);
        if (message != null) builder.setMessage(message);

        View viewInflated = LayoutInflater.from(this).inflate(R.layout.dialog_create_category, null);
        builder.setView(viewInflated);

        final EditText input = (EditText) viewInflated.findViewById(R.id.editTextNewCategoryName);
        final Button btnTakePhoto = (Button) viewInflated.findViewById(R.id.btnNewCategoryTakePhoto);
        final EditText inputPhoto = (EditText) viewInflated.findViewById(R.id.editTextPhotoWeb);

        input.setText(category.getName());
        mCurrentPhotoPath = category.getPhoto();
        if (category.getPhotoType() == 2) {
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
                String categoryName = input.getText().toString().trim();
                String categoryPhoto = mCurrentPhotoPath;
                if (inputPhoto.getText().toString().trim().length() > 0){
                    mCurrentPhotoPath = inputPhoto.getText().toString().trim();
                    photoType = 2;
                } else {
                    photoType = 1;
                }
                if (categoryName.length() == 0) {
                    Toast.makeText(getApplicationContext(), getString(R.string.category_edit_name_required), Toast.LENGTH_LONG).show();
                } else {
                    editCategory(categoryName, categoryPhoto, photoType, category);
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
                showAlertForEditingCategory(getString(R.string.edit_category), getString(R.string.edit_category_message), categories.get(info.position));
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
