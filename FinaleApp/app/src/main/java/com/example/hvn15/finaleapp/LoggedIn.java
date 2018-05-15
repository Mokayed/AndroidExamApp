package com.example.hvn15.finaleapp;

import android.content.Context;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.FragmentManager;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.HashMap;

public class
LoggedIn extends AppCompatActivity {

    private static final String TAG = "LoggedIn";
    public ArrayList<Shop> shopList = new ArrayList<>();
    public ArrayList<Person> users = new ArrayList<>();
    public HashMap<String, ArrayList<Shop>> test1 = new HashMap<>();
    private DatabaseReference database;
    private SectionsStatePagerAdapter mSectionsStatePagerAdapter;
    private ViewPager mViewPager;
    public String hej;
    private static final int LOCATION_REQUEST_CODE = 101;
    private Button btnNavSecondActivity;
    public Location location;
    public Vibrator vibrator;
    private SeekBar seekBar;
    private TextView seekbarNumber;
    private Fragment1 fragment1;
    private ListView listView;
    int progress = 25/5;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);
        //getting the toolbar
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        seekbarNumber = (TextView) findViewById(R.id.seekbarNumber);
        Fragment1 f1 = new Fragment1();
        fragment1 = f1;
        listView = (ListView) findViewById(R.id.listview);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);


        //setting the title
        toolbar.setTitle("My Toolbar");

        //placing toolbar in place of actionbar
        setSupportActionBar(toolbar);
        mViewPager = (ViewPager) findViewById(R.id.container_admin);
        //Setup the pager
        setupViewPager(mViewPager);
        seekBar = (SeekBar) findViewById(R.id.seekBar2);
        seekBar.incrementProgressBy(5);
        seekBar.setMax(99/5);
        seekBar.setProgress(progress);
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                progress = i/5;
                progress = (i*5) + 5;
                seekbarNumber.setText(""+progress);

                /*getSupportFragmentManager()
                        .beginTransaction()
                        .add(R.id.container_admin, new Fragment1())
                        .commit();


                FragmentManager fm = getSupportFragmentManager();

                Fragment1 awesome = (Fragment1)
                        getSupportFragmentManager()
                                .findFragmentById(R.id.container_admin);

                ListView lw = findViewById(R.id.listview);
                awesome.updateList(progress, lw, shopList);*/
                fragment1.updateList(shopList, progress);

            }

                @Override
                public void onStartTrackingTouch(SeekBar seekBar) {

                }

                @Override
                public void onStopTrackingTouch(SeekBar seekBar) {

                }
                });

                mSectionsStatePagerAdapter = new SectionsStatePagerAdapter(getSupportFragmentManager());


        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return;
        }
        location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Log.d("loggedinTest", location.toString());


        Log.d(TAG, "onCreate: Started.");
        Bundle extras = getIntent().getExtras();
        hej = extras.getString("hello");
        database = FirebaseDatabase.getInstance().getReference().child("data");
        users = (ArrayList<Person>) getIntent().getSerializableExtra("users");




        database.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {

                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String id = child.getKey();
                    ArrayList<Shop> discountsBelongingToShop = new ArrayList<>();
                    Log.d("idTest", id);
                    for (DataSnapshot child2 : child.getChildren()) {
                        //loop gennnem tilbud her
                        // Log.d(TAG, "test" + child2.child("discount").getValue().toString());
                        Shop shop = new Shop(child2.child("category").getValue().toString(),
                                child2.child("date").getValue().toString(),
                                child2.child("description").getValue().toString(),
                                child2.child("discount").getValue().toString(),
                                child2.child("period").getValue().toString(),
                                child2.child("price_after").getValue().toString(),
                                child2.child("price_before").getValue().toString(),
                                child2.child("title").getValue().toString());
                        shopList.add(shop);
                        discountsBelongingToShop.add(shop);
                    }
                    test1.put(id, discountsBelongingToShop);
                }
                Log.d("hash", test1.toString());
                fragment1.updateList(shopList, 0); //makes a default homescreen for the CustomAdapter list
            }

            @Override
            public void onCancelled(DatabaseError databaseError) {

            }
        });

        Log.d(TAG, hej.toString());

        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
        //btnNavSecondActivity = (Button) findViewById(R.id.btnNavSecondActivity);

        bottomNavigationView.setOnNavigationItemSelectedListener(new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.action_list:
                        setViewPager(0);
                        break;
                    case R.id.action_map:
                        setViewPager(1);
                        break;
                    case R.id.action_account:
                        setViewPager(2);
                        break;
                }
                return true;
            }
        });


    }

    @Override
    //calling the top toolbar menu options
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

    // when you select an option from the toolbar  menu
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        switch (item.getItemId()) {
            case R.id.menuAbout:
                Toast.makeText(this, "You clicked about", Toast.LENGTH_SHORT).show();
                break;

            case R.id.menuSettings:
                Toast.makeText(this, "You clicked settings", Toast.LENGTH_SHORT).show();
                break;

            case R.id.menuLogout:
                Toast.makeText(this, "You clicked logout", Toast.LENGTH_SHORT).show();
                break;

        }
        return true;
    }

    private void setupViewPager(ViewPager viewPager) {
        SectionsStatePagerAdapter adapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new Fragment1(), "Fragment1");
        adapter.addFragment(new Fragment2(), "Fragment2");
        adapter.addFragment(new Fragment3(), "Fragment3");
        viewPager.setAdapter(adapter);
    }

    public void setViewPager(int fragmentNumber) {
        mViewPager.setCurrentItem(fragmentNumber);
    }


}