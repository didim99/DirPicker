# DirPicker
Simple directory picker Activity

# Getting started

### Dependencies
Add in your `build.gradle`:
```Groovy
android {
  ...
  defaultConfig {
    ...
    vectorDrawables.useSupportLibrary = true
  }
  ...
}
...
dependencies {
  ...
  implementation 'com.android.support:appcompat-v7:26+'
  implementation 'com.android.support:recyclerview-v7:26+'
  compile 'com.android.support:support-vector-drawable:26+'
}
```

### Resources
[`/res`](https://github.com/didim99/DirPicker/tree/master/res) directory contains all necessary resources. Colors optimized for default (dark) `AppCompat` theme.

# Using

To start `DirPicker` use:
```Java
public class MainActivity extends AppCompatActivity {
...
  private static final int REQUEST_CHOOSE_DIR = 1;
...
  void choosePath () {
    Intent intent = new Intent(this, DirPickerActivity.class);
    startActivityForResult(intent, REQUEST_CHOOSE_DIR);
  }
...
}
```

To get choosed path use:
```Java
@Override
protected void onActivityResult(int requestCode, int resultCode, Intent i) {
  if (requestCode == REQUEST_CHOOSE_DIR && resultCode == RESULT_OK) {
    Uri data = i.getData();
    if (data != null) {
      String extPath = data.getPath();
    }
  }
}
```