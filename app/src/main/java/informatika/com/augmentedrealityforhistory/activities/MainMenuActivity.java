package informatika.com.augmentedrealityforhistory.activities;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import informatika.com.augmentedrealityforhistory.R;
import informatika.com.augmentedrealityforhistory.fragments.AddContent;
import informatika.com.augmentedrealityforhistory.fragments.AddHistory;
import informatika.com.augmentedrealityforhistory.fragments.AddPoi;
import informatika.com.augmentedrealityforhistory.fragments.ListHistory;

/**
 * Created by USER on 7/22/2016.
 */
public class MainMenuActivity extends AppCompatActivity {
    //Defining Variables
    private Toolbar toolbar;
    private NavigationView navigationView;
    private DrawerLayout drawerLayout;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mainmenu);

        //set main fragment
        ListHistory listHistoryFragment = new ListHistory();
        android.support.v4.app.FragmentTransaction fragmentTransaction = getSupportFragmentManager().beginTransaction();
        fragmentTransaction.replace(R.id.frame, listHistoryFragment);
        fragmentTransaction.commit();

        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Initializing NavigationView
        navigationView = (NavigationView) findViewById(R.id.navigation_view);
        navigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(MenuItem menuItem) {
                //Checking if the item is in checked state or not, if not make it in checked state
                if(menuItem.isChecked()) menuItem.setChecked(false);
                else menuItem.setChecked(true);

                //Closing drawer on item click
                drawerLayout.closeDrawers();

                android.support.v4.app.FragmentTransaction fragmentTransaction;

                //Check to see which item was being clicked and perform appropriate action
                switch (menuItem.getItemId()){
                    case R.id.history:
                        Toast.makeText(getApplicationContext(),"List History Selected", Toast.LENGTH_SHORT).show();
                        ListHistory listHistoryFragment = new ListHistory();
                        fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.frame, listHistoryFragment);
                        fragmentTransaction.commit();
                        return true;
                    case R.id.addHistory:
                        Toast.makeText(getApplicationContext(),"Add History Selected",Toast.LENGTH_SHORT).show();
                        AddHistory addHistoryFragment = new AddHistory();
                        fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.frame, addHistoryFragment);
                        fragmentTransaction.commit();
                        return true;
                    case R.id.addPoi:
                        Toast.makeText(getApplicationContext(),"Add Poi Selected",Toast.LENGTH_SHORT).show();
                        AddPoi addPoiFragment = new AddPoi();
                        fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.frame, addPoiFragment);
                        fragmentTransaction.commit();
                        return true;
                    case R.id.addContent:
                        Toast.makeText(getApplicationContext(),"Add Content Selected",Toast.LENGTH_SHORT).show();
                        AddContent addContentFragment = new AddContent();
                        fragmentTransaction = getSupportFragmentManager().beginTransaction();
                        fragmentTransaction.replace(R.id.frame, addContentFragment);
                        fragmentTransaction.commit();
                        return true;
                    case R.id.logout:
                        Toast.makeText(getApplicationContext(),"Logout Selected",Toast.LENGTH_SHORT).show();
                        return true;
                    default:
                        Toast.makeText(getApplicationContext(),"Somethings Wrong",Toast.LENGTH_SHORT).show();
                        return true;

                }
            }
        });

        // Initializing Drawer Layout and ActionBarToggle
        drawerLayout = (DrawerLayout) findViewById(R.id.drawer);
        ActionBarDrawerToggle actionBarDrawerToggle = new ActionBarDrawerToggle(this,drawerLayout,toolbar,R.string.openDrawer, R.string.closeDrawer){

            @Override
            public void onDrawerClosed(View drawerView) {
                super.onDrawerClosed(drawerView);
            }

            @Override
            public void onDrawerOpened(View drawerView) {
                super.onDrawerOpened(drawerView);
            }
        };

        //Setting the actionbarToggle to drawer layout
        drawerLayout.setDrawerListener(actionBarDrawerToggle);

        //calling sync state is necessay or else your hamburger icon wont show up
        actionBarDrawerToggle.syncState();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.option_menu, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }
}
