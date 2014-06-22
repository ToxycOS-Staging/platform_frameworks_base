/*
 * Copyright (C) 2010 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package android.graphics;

import com.android.ide.common.rendering.api.LayoutLog;
import com.android.layoutlib.bridge.Bridge;
import com.android.layoutlib.bridge.impl.DelegateManager;
import com.android.tools.layoutlib.annotations.LayoutlibDelegate;

import android.graphics.PorterDuff.Mode;

import java.awt.AlphaComposite;
import java.awt.Composite;

import static com.android.layoutlib.bridge.impl.PorterDuffUtility.getAlphaCompositeRule;
import static com.android.layoutlib.bridge.impl.PorterDuffUtility.getPorterDuffMode;

/**
 * Delegate implementing the native methods of android.graphics.PorterDuffXfermode
 *
 * Through the layoutlib_create tool, the original native methods of PorterDuffXfermode have been
 * replaced by calls to methods of the same name in this delegate class.
 *
 * This class behaves like the original native implementation, but in Java, keeping previously
 * native data into its own objects and mapping them to int that are sent back and forth between
 * it and the original PorterDuffXfermode class.
 *
 * Because this extends {@link Xfermode_Delegate}, there's no need to use a
 * {@link DelegateManager}, as all the PathEffect classes will be added to the manager owned by
 * {@link Xfermode_Delegate}.
 *
 */
public class PorterDuffXfermode_Delegate extends Xfermode_Delegate {

    // ---- delegate data ----

    private final Mode mMode;

    // ---- Public Helper methods ----

    public Mode getMode() {
        return mMode;
    }

    @Override
    public Composite getComposite(int alpha) {
        return getComposite(mMode, alpha);
    }

    @Override
    public boolean isSupported() {
        return true;
    }

    @Override
    public String getSupportMessage() {
        // no message since isSupported returns true;
        return null;
    }

    public static Composite getComposite(int mode, int alpha) {
        return getComposite(getPorterDuffMode(mode), alpha);
    }

    // ---- native methods ----

    @LayoutlibDelegate
    /*package*/ static long nativeCreateXfermode(int mode) {
        PorterDuffXfermode_Delegate newDelegate = new PorterDuffXfermode_Delegate(mode);
        return sManager.addNewDelegate(newDelegate);
    }

    // ---- Private delegate/helper methods ----

    private PorterDuffXfermode_Delegate(int mode) {
        mMode = getPorterDuffMode(mode);
    }

    private static Composite getComposite(Mode mode, int alpha255) {
        float alpha1 = alpha255 != 0xFF ? alpha255 / 255.f : 1.f;
        int rule = getAlphaCompositeRule(mode);
        if (rule >= 0) {
            return AlphaComposite.getInstance(rule, alpha1);
        }

        Bridge.getLog().fidelityWarning(LayoutLog.TAG_BROKEN,
                String.format("Unsupported PorterDuff Mode: %1$s", mode.name()),
                null, null /*data*/);

        return AlphaComposite.getInstance(AlphaComposite.SRC_OVER, alpha1);
    }
}
