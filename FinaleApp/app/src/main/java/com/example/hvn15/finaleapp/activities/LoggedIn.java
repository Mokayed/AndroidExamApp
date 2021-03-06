package com.example.hvn15.finaleapp.activities;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Vibrator;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.ActivityCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.example.hvn15.finaleapp.customerFragments.AccountFragment;
import com.example.hvn15.finaleapp.customerFragments.DiscountListFragment;
import com.example.hvn15.finaleapp.customerFragments.MapFragment;
import com.example.hvn15.finaleapp.objectClasses.Person;
import com.example.hvn15.finaleapp.R;
import com.example.hvn15.finaleapp.adapters.SectionsStatePagerAdapter;
import com.example.hvn15.finaleapp.objectClasses.Shop;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;


public class LoggedIn extends AppCompatActivity {

    private DateFormat dateFormat = new SimpleDateFormat("dd/MM/yyyy HH:mm");
    public String userName;
    public ArrayList<Shop> discountsFromFirebase = new ArrayList<>();
    public ArrayList<Person> users = new ArrayList<>();
    public HashMap<String, ArrayList<Shop>> adminDiscountsMap = new HashMap<>();
    private DatabaseReference allDiscountsFirebase;
    private ViewPager mViewPager;
    public Location location;
    public Vibrator vibrator;
    private SeekBar seekBarDiscount;
    public SeekBar seekbarDistance;
    private EditText filterWithName;
    private EditText filterWithCategory;
    private TextView seekbarNumber;
    private TextView seekbarNumber2;
    private DiscountListFragment discountListFragment;
    private MapFragment mapFragment;
    private ListView listView;
    public DatabaseReference maximumDistanceFirebase;
    public int discountNumOnSeekbar = 25 / 5;
    public int distanceNumOnSeekbar = 10;
    public int maxKm;
    private Button popUpBtn;

    @Override

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_logged_in);

        seekbarNumber = findViewById(R.id.seekbarNumber);
        popUpBtn = findViewById(R.id.popUpButn);
        popUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(LoggedIn.this, Pop.class));
            }
        });
        seekbarNumber2 = findViewById(R.id.seekbarNumber2);
        filterWithName = findViewById(R.id.name);
        filterWithCategory = findViewById(R.id.category);
        DiscountListFragment f1 = new DiscountListFragment(); //Instantiate fragments
        discountListFragment = f1;
        MapFragment f2 = new MapFragment();
        mapFragment = f2;
        listView = findViewById(R.id.listview);
        vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
        // Get a reference to the current User, and takes the max distance of the user.
        maximumDistanceFirebase = FirebaseDatabase.getInstance().getReference().child("users").child(getIntent().getExtras().getString("userName")).child("maximumDistance");

        filterWithCategory.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {

            }
            // After the categoryTextInput is changed, Categories will be sorted by the input on the list and map.
            @Override
            public void afterTextChanged(Editable editable) {
                discountListFragment.filterCategory(editable.toString());
                mapFragment.sortByCategory(editable.toString(), adminDiscountsMap);
            }
        });
        //Sort the list and the map, by the name of the store (Shop,exmpel: hm1)
        filterWithName.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void onTextChanged(CharSequence charSequence, int i, int i1, int i2) {
            }
            @Override
            public void afterTextChanged(Editable editable) {
                discountListFragment.filterName(editable.toString());
                mapFragment.sortByName(editable.toString(), adminDiscountsMap);
            }

        });

        mViewPager = findViewById(R.id.container_admin);
        //Setup the pager
        setupViewPager(mViewPager);
        seekBarDiscount = findViewById(R.id.discountSeekbar);
        seekbarDistance = findViewById(R.id.seekBar);
        seekbarDistance.setMax(mapFragment.maxKm - 1);
        seekbarDistance.setProgress(distanceNumOnSeekbar);
        seekBarDiscount.incrementProgressBy(5);
        seekBarDiscount.setMax(99 / 5);
        seekBarDiscount.setProgress(discountNumOnSeekbar);
        seekBarDiscount.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {

            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                //Divided by 5 because we want the 5,10,15,20%
                discountNumOnSeekbar = i / 5;
                discountNumOnSeekbar = (i * 5) + 5;
                seekbarNumber.setText("" + discountNumOnSeekbar);
                discountListFragment.filterDiscount(discountsFromFirebase, discountNumOnSeekbar); // note: its
                mapFragment.sortByDiscount(discountNumOnSeekbar, adminDiscountsMap);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }
        });
//sort map and list by max distance km
        seekbarDistance.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int i, boolean b) {
                distanceNumOnSeekbar = i + 1;
                maximumDistanceFirebase.setValue(distanceNumOnSeekbar);
                seekbarNumber2.setText("" + distanceNumOnSeekbar);
                discountListFragment.checkIfDiscountIsInRadius(distanceNumOnSeekbar, adminDiscountsMap);
                mapFragment.sortByKm(distanceNumOnSeekbar);

            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {

            }

        });



        //if the user accepts to deal his current location or not
        LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;

        }
        // if the current location is accepted by the user, so we can put the location to the user current location
        location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
        Bundle extras = getIntent().getExtras();
        maxKm = extras.getInt("maximumDistance");
        userName = extras.getString("userName");
        allDiscountsFirebase = FirebaseDatabase.getInstance().getReference().child("data");
        users = (ArrayList<Person>) getIntent().getSerializableExtra("users");
        //we add a valueeventlistner to data section in the firebase
        allDiscountsFirebase.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                for (DataSnapshot child : dataSnapshot.getChildren()) {
                    String id = child.getKey();
                    ArrayList<Shop> discountsBelongingToShop = new ArrayList<>();
                    for (DataSnapshot child2 : child.getChildren()) {
                        //we make a check for the period if it is old for the current date, so it well be removed from firebase.
                        try {
                            Date convertedDate = new Date();
                            convertedDate = dateFormat.parse(child2.child("period").getValue().toString());
                            Date parsed = convertedDate;
                            Date dateNow = new Date(System.currentTimeMillis());
                            if (dateNow.compareTo(parsed) != -1) {
                                allDiscountsFirebase.child(child.getKey()).child(child2.getKey()).setValue(null);
                                //If the result is 1, so it up to date.
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
// if the period is accepted, then we add all the discount from the firebase to the shop list.
                        Shop shop = new Shop(child2.child("category").getValue().toString(),
                                child2.child("date").getValue().toString(),
                                child2.child("description").getValue().toString(),
                                child2.child("discount").getValue().toString(),
                                child2.child("period").getValue().toString(),
                                child2.child("price_after").getValue().toString(),
                                child2.child("price_before").getValue().toString(),
                                child2.child("title").getValue().toString(),
                                child2.child("store").getValue().toString());
                        discountsFromFirebase.add(shop);
                        discountsBelongingToShop.add(shop);
                    }
                    adminDiscountsMap.put(id, discountsBelongingToShop);
                }
                discountListFragment.filterDiscount(discountsFromFirebase, 0); //makes a default homescreen for the CustomAdapter list

            }

            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
//a switch which navigate between the navigation button bar
        BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
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
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu, menu);
        return true;
    }

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
//we add the fragments to the adpater, so we can swipe left to right
    private void setupViewPager(ViewPager viewPager) {
        SectionsStatePagerAdapter adapter = new SectionsStatePagerAdapter(getSupportFragmentManager());
        adapter.addFragment(new DiscountListFragment(), "DiscountListFragment");
        adapter.addFragment(new MapFragment(), "MapFragment");
        adapter.addFragment(new AccountFragment(), "AccountFragment");
        viewPager.setAdapter(adapter);
    }

    public void setViewPager(int fragmentNumber) {
        mViewPager.setCurrentItem(fragmentNumber);
    }

    public void setMaxKmOnSeekBar(int newNum) {
        seekbarDistance.setMax(newNum + 6);
    }
}