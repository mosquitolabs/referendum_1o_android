package com.referendum.uoctubre.main;

import android.annotation.SuppressLint;
import android.app.SearchManager;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.widget.BottomNavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AlertDialog;
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
import com.referendum.uoctubre.fragments.WebFragment;
import com.referendum.uoctubre.utils.StringsManager;

public class MainActivity extends BaseActivity implements BottomNavigationView.OnNavigationItemSelectedListener {

    public static final String TAB_TWITTER = "twitter";
    public static final String TAB_VOTE = "vote";
    public static final String TAB_SHARE = "share";
    public static final String TAB_WEB = "web";
    private BottomNavigationView mNavigationView;
    private Fragment mFragment;
    private SearchView searchView;
    private String mCurrentScreen;

    private final static int INFORMAT_FRAGMENT = 0;
    private final static int VOTA_FRAGMENT = 1;
    private final static int COMPARTEIX_FRAGMENT = 2;
    private final static int WEB_FRAGMENT = 3;

    @SuppressLint("RestrictedApi")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getWindow().setBackgroundDrawableResource(R.color.white);
        setStatusBarColor(R.color.colorPrimaryDark);

        mNavigationView = findViewById(R.id.navigation);
        mNavigationView.setOnNavigationItemSelectedListener(this);

        for (int i = 0; i < mNavigationView.getMenu().size(); i++) {
            MenuItem menuItem = mNavigationView.getMenu().getItem(i);
            switch (menuItem.getItemId()) {
                case R.id.navigation_informat:
                    menuItem.setTitle(StringsManager.getString("menu_info"));
                    break;
                case R.id.navigation_web:
                    menuItem.setTitle(StringsManager.getString("menu_web"));
                    break;
                case R.id.navigation_vota:
                    menuItem.setTitle(StringsManager.getString("menu_vote"));
                    break;
                case R.id.navigation_comparteix:
                    menuItem.setTitle(StringsManager.getString("menu_share_tab"));
                    break;
            }
        }

        if (savedInstanceState == null) {
            showFirstFragment();
        } else {
            mCurrentScreen = savedInstanceState.getString("currentScreen", TAB_TWITTER);
            switch (mCurrentScreen) {
                case TAB_TWITTER:
                    getSupportActionBar().setTitle(StringsManager.getString("title_twitter"));
                    mFragment = getSupportFragmentManager().findFragmentByTag(InfoFragment.TAG);
                    mNavigationView.getMenu().getItem(INFORMAT_FRAGMENT).setChecked(true);
                    break;
                case TAB_WEB:
                    getSupportActionBar().setTitle(StringsManager.getString("title_web"));
                    mFragment = getSupportFragmentManager().findFragmentByTag(WebFragment.TAG);
                    mNavigationView.getMenu().getItem(WEB_FRAGMENT).setChecked(true);
                    break;
                case TAB_VOTE:
                    getSupportActionBar().setTitle(StringsManager.getString("title_vote"));
                    mFragment = getSupportFragmentManager().findFragmentByTag(VoteFragment.TAG);
                    mNavigationView.getMenu().getItem(VOTA_FRAGMENT).setChecked(true);
                    break;
                default:
                    getSupportActionBar().setTitle(StringsManager.getString("title_share"));
                    mFragment = getSupportFragmentManager().findFragmentByTag(ShareFragment.TAG);
                    mNavigationView.getMenu().getItem(COMPARTEIX_FRAGMENT).setChecked(true);
                    break;
            }
            supportInvalidateOptionsMenu();
        }

        fetchRemoteConfig();
    }

    @Override
    public void onBackPressed() {
        if (mFragment != null && mFragment instanceof WebFragment) {
            if (!((WebFragment) mFragment).onBackPressed()) {
                super.onBackPressed();
            }
        } else {
            super.onBackPressed();
        }
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

        for (int i = 0; i < menu.size(); i++) {
            MenuItem menuItem = menu.getItem(i);
            switch (menuItem.getItemId()) {
                case R.id.action_search:
                    menuItem.setTitle(StringsManager.getString("menu_search"));
                    break;
                case R.id.action_share:
                    menuItem.setTitle(StringsManager.getString("menu_share_app"));
                    break;
                case R.id.action_language:
                    menuItem.setTitle(StringsManager.getString("menu_language"));
                    break;
            }
        }

        if (mFragment instanceof VoteFragment) {
            menu.findItem(R.id.action_search).setVisible(true);
            menu.findItem(R.id.action_share).setVisible(false);
            menu.findItem(R.id.action_language).setVisible(false);

            final MenuItem searchItem = menu.findItem(R.id.action_search);

            SearchManager searchManager = (SearchManager) MainActivity.this.getSystemService(Context.SEARCH_SERVICE);

            if (searchItem != null) {
                searchView = (SearchView) searchItem.getActionView();
                searchView.setQueryHint(StringsManager.getString("search_text"));
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
            menu.findItem(R.id.action_language).setVisible(false);
        } else if (mFragment instanceof WebFragment) {
            menu.findItem(R.id.action_search).setVisible(false);
            menu.findItem(R.id.action_share).setVisible(false);
            menu.findItem(R.id.action_language).setVisible(false);
        } else {
            menu.findItem(R.id.action_search).setVisible(false);
            menu.findItem(R.id.action_share).setVisible(false);
            menu.findItem(R.id.action_language).setVisible(true);
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
                String shareBody = firebaseRemoteConfig.getString(Constants.FIREBASE_CONFIG_SHARE_APP_MESSAGEREQUIRED + "_" + StringsManager.getCurrentLanguage());
                Intent sharingIntent = new Intent(android.content.Intent.ACTION_SEND);
                sharingIntent.setType("text/plain");
                sharingIntent.putExtra(android.content.Intent.EXTRA_SUBJECT, StringsManager.getString("share_subject"));
                sharingIntent.putExtra(android.content.Intent.EXTRA_TEXT, shareBody);
                startActivity(Intent.createChooser(sharingIntent, StringsManager.getString("share_select")));
                return true;
            case R.id.action_language:
                AlertDialog.Builder builder = new AlertDialog.Builder(this);
                builder.setTitle(StringsManager.getString("change_language_title"));

                String[] languages = new String[]{"Català", "Aranés (pròplèuments)", "Castellano", "English"};

                builder.setItems(languages, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String newLang;
                        switch (which) {
                            case 0:
                            default:
                                newLang = "ca";
                                break;
                            case 1:
                                newLang = "oc";
                                break;
                            case 2:
                                newLang = "es";
                                break;
                            case 3:
                                newLang = "en";
                                break;
                        }
                        if (!newLang.equals(StringsManager.getCurrentLanguage())) {
                            StringsManager.setLanguage(newLang);
                            Intent intent = new Intent(MainActivity.this, MainActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
                            startActivity(intent);
                            finish();
                        }
                        dialog.dismiss();
                    }
                });
                builder.show();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int itemId = item.getItemId();
        if (isActivitySafe()) {
            if (itemId == R.id.navigation_informat) {
                showFragment(InfoFragment.TAG);
                getSupportActionBar().setTitle(StringsManager.getString("title_twitter"));
                mCurrentScreen = TAB_TWITTER;
            } else if (itemId == R.id.navigation_vota) {
                showFragment(VoteFragment.TAG);
                getSupportActionBar().setTitle(StringsManager.getString("title_vote"));
                mCurrentScreen = TAB_VOTE;
            } else if (itemId == R.id.navigation_comparteix) {
                showFragment(ShareFragment.TAG);
                getSupportActionBar().setTitle(StringsManager.getString("title_share"));
                mCurrentScreen = TAB_SHARE;
            } else if (itemId == R.id.navigation_web) {
                showFragment(WebFragment.TAG);
                getSupportActionBar().setTitle(StringsManager.getString("title_web"));
                mCurrentScreen = TAB_WEB;
            }
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
            } else if (tag.equalsIgnoreCase(WebFragment.TAG)) {
                mFragment = WebFragment.newInstance();
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
        getSupportActionBar().setTitle(StringsManager.getString("title_twitter"));
        mCurrentScreen = TAB_TWITTER;
    }
}
