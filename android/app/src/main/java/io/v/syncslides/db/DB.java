// Copyright 2015 The Vanadium Authors. All rights reserved.
// Use of this source code is governed by a BSD-style
// license that can be found in the LICENSE file.

package io.v.syncslides.db;

import android.content.Context;

import io.v.syncslides.InitException;
import io.v.syncslides.model.Deck;
import io.v.syncslides.model.DynamicList;

/**
 * Provides high-level methods for getting and setting the state of SyncSlides.
 * It is an interface instead of a concrete class to make testing easier.
 */
public interface DB {
    class Singleton {
        private static volatile DB instance;

        public static DB get() {
            DB result = instance;
            if (instance == null) {
                synchronized (Singleton.class) {
                    result = instance;
                    if (result == null) {
                        instance = result = new SyncbaseDB();
                    }
                }
            }
            return result;
        }
    }

    /**
     * Perform initialization steps.
     */
    void init(Context context) throws InitException;
}