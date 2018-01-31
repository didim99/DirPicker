package com.didim99.satools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * directory picker Activity
 * Created by didim99 on 26.01.18.
 */

public class DirPickerActivity extends AppCompatActivity {
  private static final String LOG_TAG = "My_log_DirPicker";
  private Context appContext;
  private TextView textPath;
  private Button btnGo;
  private int selectedId = -1;
  private String path;

  private final String FS_ROOT = "/";
  private final String ATTR_NAME = "name";
  private final String ATTR_IS_DIR = "isDir";
  private final String KEY_DIR_PICKER_LAST_PATH = "dirPicker_lastPath";

  private ArrayList<Map<String, Object>> arrayDir = new ArrayList<>();
  private ListDirAdapter adapter;

  protected void onCreate(Bundle savedInstanceState) {
    Log.d(LOG_TAG, "DirPickerActivity starting...");
    super.onCreate(savedInstanceState);
    setContentView(R.layout.dir_picker);
    appContext = getApplicationContext();

    //Adapter and view configuration
    adapter = new ListDirAdapter(this, arrayDir);
    textPath = findViewById(R.id.textPath);
    btnGo = findViewById(R.id.dirPicker_go);
    RecyclerView listDir = findViewById(R.id.list_dir);
    listDir.setLayoutManager(new LinearLayoutManager(this));
    listDir.setHasFixedSize(true);
    listDir.setAdapter(adapter);

    //loading path from saved instance or from SharedPreferences
    path = (String) getLastCustomNonConfigurationInstance();
    if (path == null)
      path = PreferenceManager.getDefaultSharedPreferences(appContext)
        .getString(KEY_DIR_PICKER_LAST_PATH, FS_ROOT);
    // Checking access to file system root directory
    if (path.equals(FS_ROOT) && !isDirOpened(path)) {
      Toast.makeText(appContext,
        R.string.dirPicker_fsRootUnreadable, Toast.LENGTH_LONG).show();
      path = Environment.getExternalStorageDirectory().getPath() + "/";
      //save new start path in SharedPreferences
      PreferenceManager.getDefaultSharedPreferences(appContext).edit()
        .putString(KEY_DIR_PICKER_LAST_PATH, path).apply();
    }
    updateListDir();

    Log.d(LOG_TAG, "DirPickerActivity started");
  }

  @Override
  public Object onRetainCustomNonConfigurationInstance() {
    return path;
  }

  public void onClickBack (View view) {
    if (path.equals(FS_ROOT)) {
      setResult(Activity.RESULT_CANCELED, new Intent());
      finish();
    } else {
      path = (new File(path)).getParent();
      if (!path.equals(FS_ROOT))
        path += "/";
      updateListDir();
    }
  }

  public void onClickGo (View view) {
    //save last picked path in SharedPreferences
    PreferenceManager.getDefaultSharedPreferences(appContext).edit()
      .putString(KEY_DIR_PICKER_LAST_PATH, path).apply();
    Intent intent = new Intent();
    intent.setData(Uri.parse("file://" + path));
    setResult(Activity.RESULT_OK, intent);
    finish();
  }

  private void updateListDir () {
    if (selectedId != -1)
      path = path + arrayDir.get(selectedId).get(ATTR_NAME) + "/";
    selectedId = -1;
    arrayDir.clear();
    Log.d(LOG_TAG, "Curr path: " + path);

    ArrayList<Map<String, Object>> arrayFiles = new ArrayList<>();
    File[] files = new File(path).listFiles();

    boolean dirOpened = files != null;
    btnGo.setEnabled(dirOpened);
    if (dirOpened) {
      Arrays.sort(files);
      Map<String, Object> m;
      for (File file : files) {
        boolean isDir = file.isDirectory();
        m = new HashMap<>();
        m.put(ATTR_NAME, file.getName());
        m.put(ATTR_IS_DIR, isDir);
        if (isDir)
          arrayDir.add(m);
        else
          arrayFiles.add(m);
      }
      arrayDir.addAll(arrayFiles);
    } else {
      Toast.makeText(appContext,
        R.string.dirPicker_dirUnreadable, Toast.LENGTH_LONG).show();
    }

    adapter.notifyDataSetChanged();
    textPath.setText(path);
  }

  private boolean isDirOpened(String dirName) {
    try {
      File[] files = new File(dirName).listFiles();
      for (File file : files) {}
      return true;
    } catch (Exception e) {
      return false;
    }
  }

  class ListDirAdapter extends RecyclerView.Adapter<ListDirAdapter.ViewHolder> {

    private LayoutInflater inflater;
    private List<Map<String, Object>> files;

    ListDirAdapter (Context context, List<Map<String, Object>> files) {
      this.inflater = LayoutInflater.from(context);
      this.files = files;
    }

    @Override
    public ListDirAdapter.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
      View view = inflater.inflate(R.layout.dir_picker_item, parent, false);
      return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(final ListDirAdapter.ViewHolder holder, int position) {
      Map<String, Object> file = files.get(position);
      boolean isDir = (boolean) file.get(ATTR_IS_DIR);

      //Enable top divider for first item
      if (position == 0)
        holder.topDivider.setVisibility(ImageView.VISIBLE);
      else
        holder.topDivider.setVisibility(ImageView.INVISIBLE);

      holder.name.setText((String) file.get(ATTR_NAME));
      if (isDir) {
        holder.folderIcon.setVisibility(ImageView.VISIBLE);
        holder.name.setTextColor(getResources().getColor(R.color.dirPicker_colorTextActive));
        holder.name.setOnClickListener(
          new View.OnClickListener() {
            @Override
            public void onClick(View view) {
              selectedId = holder.getAdapterPosition();
              updateListDir();
            }
          }
        );
      } else {
        holder.folderIcon.setVisibility(ImageView.INVISIBLE);
        holder.name.setTextColor(getResources().getColor(R.color.dirPicker_colorTextInactive));
        holder.name.setOnClickListener(null);
      }
    }

    @Override
    public int getItemCount() {
      return files.size();
    }

    class ViewHolder extends RecyclerView.ViewHolder {
      final TextView name;
      final ImageView topDivider;
      final ImageView folderIcon;
      ViewHolder(View view) {
        super(view);
        topDivider = view.findViewById(R.id.ivTopDivider);
        folderIcon = view.findViewById(R.id.ivFolderIcon);
        name = view.findViewById(R.id.tvFileName);
      }
    }
  }
}