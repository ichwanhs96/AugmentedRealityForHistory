package informatika.com.augmentedrealityforhistory.activities;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import informatika.com.augmentedrealityforhistory.R;

public class MainActivity extends AppCompatActivity{
    //button
    private Button button;
    private Button buttonListView;
    private Button buttonLogin;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        button = (Button) findViewById(R.id.button);
        buttonListView = (Button) findViewById(R.id.buttonListView);
        buttonLogin = (Button) findViewById(R.id.buttonLogin);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextOverlayActivity();
            }
        });

        buttonListView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextMainMenuActivity();
            }
        });

        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                nextLoginActivity();
            }
        });
    }

    public void nextOverlayActivity() {
        Intent intent = new Intent(this, OverlayActivity.class);
        startActivity(intent);
    }

    public void nextListViewActivity() {
        Intent intent = new Intent(this, ListHistoryActivity.class);
        startActivity(intent);
    }

    public void nextMainMenuActivity(){
        Intent intent = new Intent(this, MainMenuActivity.class);
        startActivity(intent);
    }

    public void nextLoginActivity(){
        Intent intent = new Intent(this, LoginActivity.class);
        startActivity(intent);
    }
}
