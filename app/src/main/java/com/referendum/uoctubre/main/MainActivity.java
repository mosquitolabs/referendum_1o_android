package com.referendum.uoctubre.main;

import android.app.SearchManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.SearchView;
import android.text.InputType;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.widget.EditText;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.remoteconfig.FirebaseRemoteConfig;
import com.referendum.uoctubre.R;
import com.referendum.uoctubre.fragments.InfoFragment;
import com.referendum.uoctubre.fragments.ShareFragment;
import com.referendum.uoctubre.fragments.VoteFragment;

public class MainActivity extends BaseActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    public static final String TAB_TWITTER = "twitter";
    public static final String TAB_VOTE = "vote";
    public static final String TAB_SHARE = "share";
    private BottomNavigationView mNavigationView;
    private Fragment mFragment;
    private SearchView searchView;
    private String mCurrentScreen;

    private final static int INFORMAT_FRAGMENT = 0;
    private final static int VOTA_FRAGMENT = 1;
    private final static int COMPARTEIX_FRAGMENT = 2;
    public static final int LOCATION_PERMISSION_ID = 1001;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setBackgroundDrawableResource(R.color.white);
        setStatusBarColor(R.color.colorPrimaryDark);

        mNavigationView = findViewById(R.id.navigation);
        mNavigationView.setOnNavigationItemSelectedListener(this);

        if (savedInstanceState == null) {
            showFirstFragment();
        } else {
            mCurrentScreen = savedInstanceState.getString("currentScreen", TAB_TWITTER);
            switch (mCurrentScreen) {
                case TAB_TWITTER:
                    getSupportActionBar().setTitle(R.string.title_twitter);
                    mFragment = getSupportFragmentManager().findFragmentByTag(InfoFragment.TAG);
                    mNavigationView.getMenu().getItem(INFORMAT_FRAGMENT).setChecked(true);
                    break;
                case TAB_VOTE:
                    getSupportActionBar().setTitle(R.string.title_vote);
                    mFragment = getSupportFragmentManager().findFragmentByTag(VoteFragment.TAG);
                    mNavigationView.getMenu().getItem(VOTA_FRAGMENT).setChecked(true);
                    break;
                default:
                    getSupportActionBar().setTitle(R.string.title_share);
                    mFragment = getSupportFragmentManager().findFragmentByTag(ShareFragment.TAG);
                    mNavigationView.getMenu().getItem(COMPARTEIX_FRAGMENT).setChecked(true);
                    break;
            }
            supportInvalidateOptionsMenu();
        }

        fetchRemoteConfig();
    }

    private void fetchRemoteConfig() {
        final FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
        firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
        firebaseRemoteConfig.fetch(3600) //1h expiration time
                .addOnCompleteListener(this, new OnCompleteListener<Void>() {
                    @Override
                    public void onComplete(@NonNull Task<Void> task) {
                        if (task.isSuccessful()) {
                            firebaseRemoteConfig.activateFetched();
                        }
                    }
                });
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putString("currentScreen", mCurrentScreen);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater menuInflater = getMenuInflater();
        menuInflater.inflate(R.menu.menu_main, menu);

        if (mFragment instanceof VoteFragment) {
            menu.findItem(R.id.action_search).setVisible(true);
            menu.findItem(R.id.action_share).setVisible(false);

            final MenuItem searchItem = menu.findItem(R.id.action_search);

            SearchManager searchManager = (SearchManager) MainActivity.this.getSystemService(Context.SEARCH_SERVICE);

            if (searchItem != null) {
                searchView = (SearchView) searchItem.getActionView();
                searchView.setQueryHint(getString(R.string.search_text));
                searchView.setSearchableInfo(searchManager.getSearchableInfo(MainActivity.this.getComponentName()));
                searchView.setInputType(InputType.TYPE_CLASS_NUMBER);

                final EditText searchEditText = searchView.findViewById(android.support.v7.appcompat.R.id.search_src_text);
                searchEditText.setHintTextColor(ContextCompat.getColor(this, R.color.white_transparent));
                searchView.setIconified(true);
                searchView.setMaxWidth(Integer.MAX_VALUE);
                searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                    @Override
                    public boolean onQueryTextSubmit(String query) {
                        if (mFragment instanceof VoteFragment) {
                            int codiPostal;
                            try {
                                codiPostal = Integer.valueOf(query);
                            } catch (NumberFormatException e) {
                                codiPostal = 0;
                            }
                            ((VoteFragment) mFragment).searchStation(codiPostal);
                            searchView.setQuery("", false);
                            searchView.clearFocus();
                            searchView.setIconified(true);
                        }
                        return true;
                    }

                    @Override
                    public boolean onQueryTextChange(String newText) {
                        return false;
                    }
                });
            }
        } else if (mFragment instanceof ShareFragment) {
            menu.findItem(R.id.action_search).setVisible(false);
            menu.findItem(R.id.action_share).setVisible(true);
        } else {
            menu.findItem(R.id.action_search).setVisible(false);
            menu.findItem(R.id.action_share).setVisible(false);
        }
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_share:
                //Allow changing the share message via Firebase
                FirebaseRemoteConfig firebaseRemoteConfig = FirebaseRemoteConfig.getInstance();
                firebaseRemoteConfig.setDefaults(R.xml.remote_config_defaults);
                String shareBody = firebaseRemoteConfig.getString(Constants.FIREBASE_CONFIG_SHARE_APP_MESSAGEREQUIRED);
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, "1-O Referèndum");
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, "Selecciona on Compartir"));
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (itemId == R.id.navigation_informat) {
            showFragment(InfoFragment.TAG);
            getSupportActionBar().setTitle(R.string.title_twitter);
            mCurrentScreen = TAB_TWITTER;
        } else if (itemId == R.id.navigation_vota) {
            showFragment(VoteFragment.TAG);
            getSupportActionBar().setTitle(R.string.title_vote);
            mCurrentScreen = TAB_VOTE;
        } else if (itemId == R.id.navigation_comparteix) {
            showFragment(ShareFragment.TAG);
            getSupportActionBar().setTitle(R.string.title_share);
            mCurrentScreen = TAB_SHARE;
        }
        return true;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        mFragment.onActivityResult(requestCode, resultCode, data);
    }

    public void showFragment(String tag) {
        Log.v("TAG", "fragment: " + tag);
        Fragment f = getSupportFragmentManager().findFragmentByTag(tag);
        Fragment previousFragment = mFragment;
        if (f == null) {
            if (tag.equalsIgnoreCase(ShareFragment.TAG)) {
                mFragment = ShareFragment.newInstance();
            } else if (tag.equalsIgnoreCase(InfoFragment.TAG)) {
                mFragment = InfoFragment.newInstance();
            } else if (tag.equalsIgnoreCase(VoteFragment.TAG)) {
                mFragment = VoteFragment.newInstance();
            }
        } else {
            mFragment = f;
        }
        if (mFragment != previousFragment) {
            showFragment(mFragment, tag, previousFragment);
            supportInvalidateOptionsMenu();
        }
    }

    private void showFragment(Fragment fragment, String tag, Fragment previousFragment) {
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        if (previousFragment != null && !previousFragment.isHidden()) {
            fragmentTransaction.hide(previousFragment);
        }
        if (fragment.isHidden() || fragment.isAdded()) {
            fragmentTransaction.show(fragment);
        } else {
            fragmentTransaction.add(R.id.container, fragment, tag);
        }
        fragmentTransaction.commit();
    }

    public void showFirstFragment() {
        mNavigationView.getMenu().getItem(INFORMAT_FRAGMENT).setChecked(true);
        showFragment(InfoFragment.TAG);
        getSupportActionBar().setTitle(R.string.title_twitter);
        mCurrentScreen = TAB_TWITTER;
    }
}
