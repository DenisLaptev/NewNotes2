package ua.a5.newnotes.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Bundle;
import android.provider.CalendarContract;
import android.support.annotation.NonNull;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TabLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.view.ViewPager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.getbase.floatingactionbutton.FloatingActionsMenu;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.common.api.Scope;

import java.util.GregorianCalendar;
import java.util.Set;

import ua.a5.newnotes.R;
import ua.a5.newnotes.adapter.tabsFragmentAdapters.EventsTabsFragmentAdapter;
import ua.a5.newnotes.interfaces.ServerAuthCodeCallbacks;
import ua.a5.newnotes.util.IabHelper;
import ua.a5.newnotes.util.IabResult;
import ua.a5.newnotes.util.Inventory;
import ua.a5.newnotes.util.Purchase;


import static ua.a5.newnotes.activities.NotesActivity.mGoogleApiClient;
import static ua.a5.newnotes.utils.utils_spannable_string.UtilsDates.getCurrentDay;
import static ua.a5.newnotes.utils.utils_spannable_string.UtilsDates.getCurrentMonth;
import static ua.a5.newnotes.utils.utils_spannable_string.UtilsDates.getCurrentYear;

public class EventsActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        ServerAuthCodeCallbacks {
    private static final int LAYOUT = R.layout.activity_events;

    private Toolbar toolbarEvents;
    private DrawerLayout drawerLayoutEvents;
    private ViewPager viewPagerEvents;
    private TabLayout tabLayoutEvents;

    EventsTabsFragmentAdapter adapterEvents;





    //переменная указывает, есть ли соединение с интернетом или нет.
    boolean isOnLine = false;

    //For billing.
    String base64EncodedPublicKey;
    IabHelper mHelper;
    private static final String TAG = "billing";
    static final String ITEM_SKU_PREMIUM = "upgrade";
    static final int REQUEST_CODE_INT = 10001;


    // Does the user have the premium upgrade?
    public static boolean mIsPremium = false;
    //For billing.

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        setTheme(R.style.AppDefault);
        super.onCreate(savedInstanceState);
        setContentView(LAYOUT);



        base64EncodedPublicKey = getString(R.string.billing_pubkey);

        //For billing.
        mHelper = new IabHelper(this, base64EncodedPublicKey);

        mHelper.startSetup(new IabHelper.OnIabSetupFinishedListener() {
            public void onIabSetupFinished(IabResult result) {
                if (!result.isSuccess()) {
                    Log.d(TAG, "In-app Billing setup failed: " + result);
                } else {
                    Log.d(TAG, "In-app Billing is set up OK");
                }
            }
        });
        //For billing.

        //Initializing the Google Mobile Ads SDK
        MobileAds.initialize(getApplicationContext(), getString(R.string.admob_app_id));
        //Initializing the Google Mobile Ads SDK




        initToolbar();
        initNavigationView();
        initTabs();
    }

    private void initToolbar() {
        toolbarEvents = (Toolbar) findViewById(R.id.toolbar_events);
        toolbarEvents.setTitle(R.string.toolbar_title);
        toolbarEvents.setTitleTextAppearance(this,R.style.toolbar_notesevents_title_style);
        toolbarEvents.setOnMenuItemClickListener(new Toolbar.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {

                    case R.id.btn_calendar_events:

                        //The intent to create a calendar event.
                        Intent calIntent = new Intent(Intent.ACTION_INSERT);
                        calIntent.setType("vnd.android.cursor.item/event");

                        try {
                            GregorianCalendar calDateBegin = new GregorianCalendar(getCurrentYear(), getCurrentMonth(), getCurrentDay());
                            calIntent.putExtra(CalendarContract.EXTRA_EVENT_BEGIN_TIME,
                                    calDateBegin.getTimeInMillis());

                            startActivity(calIntent);
                            //finish();
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        break;
                }
                return false;
            }
        });

        toolbarEvents.inflateMenu(R.menu.menu_events);
    }


    private void initNavigationView() {
        drawerLayoutEvents = (DrawerLayout) findViewById(R.id.drawer_layout_events);
        ActionBarDrawerToggle toggle =
                new ActionBarDrawerToggle(
                        this,
                        drawerLayoutEvents,
                        toolbarEvents,
                        R.string.view_navigation_open,
                        R.string.view_navigation_close
                );
        drawerLayoutEvents.setDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationViewEvents = (NavigationView) findViewById(R.id.navigation_view_events);
        navigationViewEvents.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem menuItem) {
                drawerLayoutEvents.closeDrawers();
                Intent intent;
                switch (menuItem.getItemId()) {

                    case R.id.main_menu_events_notes:
                        intent = new Intent(EventsActivity.this, NotesActivity.class);
                        startActivity(intent);
                        finish();
                        break;

                    case R.id.main_menu_events_events:

                        break;

                    case R.id.main_menu_events_upgrade:

                        isOnLine = isOnline();

                        if (isOnLine == true) {

                            try {
                                mHelper.launchPurchaseFlow(EventsActivity.this, ITEM_SKU_PREMIUM, REQUEST_CODE_INT, mPurchaseFinishedListener, "ITEM_SKU_PREMIUM");
                                //mHelper.launchPurchaseFlow(OptionsMenuActivity.this, ITEM_SKU_TEST, REQUEST_CODE_INT,mPurchaseFinishedListener, "ITEM_SKU_TEST");
                            } catch (Exception e) {
                                e.printStackTrace();
                            }

                        } else {
                            Toast.makeText(getApplicationContext(), R.string.toast_nointernet,
                                    Toast.LENGTH_SHORT).show();
                        }

                        break;

                    case R.id.main_menu_events_moreapps:
                        isOnLine = isOnline();
                        if (isOnLine == true) {
                            String url = "https://play.google.com/store/apps/developer?id=a5.ua&hl=en";
                            Intent i = new Intent(Intent.ACTION_VIEW);
                            i.setData(Uri.parse(url));
                            startActivity(i);

                        } else {
                            Toast.makeText(getApplicationContext(), R.string.toast_nointernet,
                                    Toast.LENGTH_SHORT).show();
                        }
                        break;

                    case R.id.main_menu_events_quit:
                        AlertDialog.Builder builder = new AlertDialog.Builder(EventsActivity.this, R.style.MyAlertDialogStyle);
                        builder.setTitle(R.string.quitdialog_title);
                        builder.setMessage(R.string.quitdialog_message);

                        //positive button.
                        builder.setPositiveButton(R.string.quitdialog_positivebutton, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                finish();
                            }

                        });

                        //negative button.
                        builder.setNegativeButton(R.string.quitdialog_negativebutton, new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {

                            }
                        });
                        builder.show();
                        break;
                }
                return true;
            }
        });
    }


    private void initTabs() {
        viewPagerEvents = (ViewPager) findViewById(R.id.viewpager_events);
        adapterEvents = new EventsTabsFragmentAdapter(this, getSupportFragmentManager());
        viewPagerEvents.setAdapter(adapterEvents);
        viewPagerEvents.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

            }

            @Override
            public void onPageSelected(int position) {
                adapterEvents.getItem(position).update();
            }

            @Override
            public void onPageScrollStateChanged(int state) {

            }
        });

        tabLayoutEvents = (TabLayout) findViewById(R.id.tablayout_events);
        tabLayoutEvents.setupWithViewPager(viewPagerEvents);
    }


    @Override
    public void onBackPressed() {
        //Если открыта шторка или меню FloatingActionsMenu,
        //то по нажатию на onBackPressed(), закрыть их.
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout_events);
        FloatingActionsMenu menuMultipleActions = (FloatingActionsMenu) findViewById(R.id.multiple_actions_events);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else if (menuMultipleActions.isExpanded()) {
            menuMultipleActions.collapse();
        } else {

            AlertDialog.Builder builder = new AlertDialog.Builder(EventsActivity.this, R.style.MyAlertDialogStyle);
            builder.setTitle(R.string.quitdialog_title);
            builder.setMessage(R.string.quitdialog_message);

            //positive button.
            builder.setPositiveButton(R.string.quitdialog_positivebutton, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    finish();
                }

            });

            //negative button.
            builder.setNegativeButton(R.string.quitdialog_negativebutton, new DialogInterface.OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {

                }
            });
            builder.show();
        }
    }













    public boolean isOnline() {
        ConnectivityManager cm =
                (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnected();
    }










    IabHelper.QueryInventoryFinishedListener
            mQueryFinishedListener = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {

            Log.d(TAG, "Query inventory finished.");

            // Is it a failure?
            if (result.isFailure()) {
                // handle error
                return;
            }

            Log.d(TAG, "Query inventory was successful.");


            // Do we have the premium upgrade?
            Purchase premiumPurchase = inventory.getPurchase(ITEM_SKU_PREMIUM);
            mIsPremium = (premiumPurchase != null);
            Log.d("Billing", "User is " + (mIsPremium ? "PREMIUM" : "NOT PREMIUM"));

        }
    };




    //For Leaderboard
    @Override
    public void onConnected(Bundle connectionHint) {
        //Toast.makeText(getApplicationContext(), "connected",
        //        Toast.LENGTH_SHORT).show();
    }


    private ConnectionResult mConnectionResult;

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                connectionResult.startResolutionForResult(this, 0);
            } catch (IntentSender.SendIntentException e) {
                mGoogleApiClient.connect();
            }
        }
        mConnectionResult = connectionResult;
    }


    @Override
    public void onConnectionSuspended(int i) {
    }


    public ServerAuthCodeCallbacks.CheckResult onCheckServerAuthorization(String s, Set<Scope> set) {
        return null;
    }

    public boolean onUploadServerAuthCode(String s, String s1) {
        return false;
    }


    //For billing.

    @Override
    protected void onActivityResult(int requestCode, int responseCode, Intent intent) {
        if (!mHelper.handleActivityResult(requestCode,
                responseCode, intent)) {
            super.onActivityResult(requestCode, responseCode, intent);
        }
        if (requestCode == ConnectionResult.SIGN_IN_REQUIRED && responseCode == RESULT_OK) {
            mConnectionResult = null;
            mGoogleApiClient.connect();
        }

        Log.d("Billing", "onActivityResult(" + requestCode + "," + responseCode + "," + intent + ")");

    }


    IabHelper.OnIabPurchaseFinishedListener mPurchaseFinishedListener
            = new IabHelper.OnIabPurchaseFinishedListener() {
        public void onIabPurchaseFinished(IabResult result, Purchase purchase) {
            if (result.isFailure()) {
                // Handle error
                Log.d(TAG, "Error purchasing: " + result);
                return;
            } else if (purchase.getSku().equals(ITEM_SKU_PREMIUM)) {
                // give user access to premium content and update the UI

                //mIsPremium = true;

                try {
                    //updateUi();
                    consumeItem();
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

        }
    };


    //Этот метод срабатывает, если был успешный заказ на обновление приложения.
    public void consumeItem() throws Exception {
        mHelper.queryInventoryAsync(mGotInventoryListener);
    }

    IabHelper.QueryInventoryFinishedListener mGotInventoryListener
            = new IabHelper.QueryInventoryFinishedListener() {
        public void onQueryInventoryFinished(IabResult result, Inventory inventory) {
            if (result.isFailure()) {
                // Handle failure
            } else {

                // does the user have the premium upgrade?
                mIsPremium = inventory.hasPurchase(ITEM_SKU_PREMIUM);
            }
        }
    };


    IabHelper.OnConsumeFinishedListener mConsumeFinishedListener =
            new IabHelper.OnConsumeFinishedListener() {
                public void onConsumeFinished(Purchase purchase,
                                              IabResult result) {

                    if (result.isSuccess()) {
                        //clickButton.setEnabled(true);
                    } else {
                        // handle error
                    }
                }
            };


    @Override
    public void onDestroy() {
        super.onDestroy();

        if (mHelper != null) try {
            mHelper.dispose();
        } catch (Exception e) {
            e.printStackTrace();
        }
        mHelper = null;
        finish();
    }
}
