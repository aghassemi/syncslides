// Copyright 2015 The Vanadium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.

package io.v.syncslides;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import io.v.syncslides.db.DB;
import io.v.syncslides.model.Session;
import io.v.v23.verror.VException;

/**
 * Handles multiple views of a presentation: list of slides, fullscreen slide,
 * navigate through the deck with notes.
 */
public class PresentationActivity extends AppCompatActivity {
    private static final String TAG = "PresentationActivity";

    public static final String SESSION_ID_KEY = "session_id_key";

    private String mSessionId;
    private Session mSession;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Immediately initialize V23, possibly sending user to the
        // AccountManager to get blessings.
        try {
            V23.Singleton.get().init(getApplicationContext(), this);
        } catch (InitException e) {
            // TODO(kash): Start a to-be-written SettingsActivity that makes it possible
            // to wipe the state of syncbase and/or blessings.
            handleError("Failed to init", e);
        }
        setContentView(R.layout.activity_presentation);

        if (savedInstanceState == null) {
            mSessionId = getIntent().getStringExtra(SESSION_ID_KEY);
        } else {
            mSessionId = savedInstanceState.getString(SESSION_ID_KEY);
        }
        if (savedInstanceState != null) {
            // Let the framework take care of inflating the right fragment.
            return;
        }
        DB db = DB.Singleton.get();
        try {
            mSession = db.getSession(mSessionId);
        } catch (VException e) {
            handleError("Failed to load state", e);
            finish();
        }
        showSlideList();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        try {
            if (V23.Singleton.get().onActivityResult(
                    getApplicationContext(), requestCode, resultCode, data)) {
                return;
            }
        } catch (InitException e) {
            // TODO(kash): Start a to-be-written SettingsActivity that makes it possible
            // to wipe the state of syncbase and/or blessings.
            handleError("Failed onActivityResult initialization", e);
        }
        // Any other activity results would be handled here.
    }

    @Override
    protected void onSaveInstanceState(Bundle b) {
        super.onSaveInstanceState(b);
        b.putString(SESSION_ID_KEY, mSessionId);
    }

    /**
     * Set the system UI to be immersive or not.
     */
    public void setUiImmersive(boolean immersive) {
        if (immersive) {
            getSupportActionBar().hide();
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION
                            | View.SYSTEM_UI_FLAG_FULLSCREEN
                            | View.SYSTEM_UI_FLAG_IMMERSIVE_STICKY);
        } else {
            getSupportActionBar().show();
            // See the comment at the top of fragment_slide_list.xml for why we don't simply
            // use View.SYSTEM_UI_FLAG_VISIBLE.
            getWindow().getDecorView().setSystemUiVisibility(
                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                            | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN);
        }
    }

    /**
     * Shows the slide list, where users can see the slides in a presentation
     * and click on one to browse the deck, or press the play FAB to start
     * presenting.
     */
    public void showSlideList() {
        SlideListFragment fragment = SlideListFragment.newInstance(mSessionId);
        getSupportFragmentManager().beginTransaction().replace(R.id.fragment, fragment).commit();
    }

    private void handleError(String msg, Throwable throwable) {
        Log.e(TAG, msg + ": " + Log.getStackTraceString(throwable));
        Toast.makeText(getApplicationContext(), msg, Toast.LENGTH_SHORT).show();
    }
}