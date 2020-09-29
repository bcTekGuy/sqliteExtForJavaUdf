package app.sqliteextforjavaudf;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.widget.TextView;

import java.io.File;
import java.io.IOException;

public class MainActivity extends AppCompatActivity {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    String sLibDir=this.getApplicationInfo().nativeLibraryDir;
    File libDirectory = new File(this.getApplicationInfo().nativeLibraryDir);
    String files[] = libDirectory.list();

    String sFileNameDatabase = null;
    try {
      sFileNameDatabase = this.getDatabasePath("AA.db3").getCanonicalPath();
      if (sFileNameDatabase != null) {
        String sTestRestults=SimpleCustomFncts.testCustomFncts(sFileNameDatabase);
        TextView tv = findViewById(R.id.testResults);
        tv.setText(sTestRestults);
      }
    } catch (IOException e) {}
  }

}