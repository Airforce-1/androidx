/*
 * Copyright (C) 2017 The Android Open Source Project
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
package android.arch.background.workmanager.impl.constraints;

import android.arch.background.workmanager.Constraints;
import android.arch.background.workmanager.impl.constraints.controllers.BatteryChargingController;
import android.arch.background.workmanager.impl.constraints.controllers.BatteryNotLowController;
import android.arch.background.workmanager.impl.constraints.controllers.ConstraintController;
import android.arch.background.workmanager.impl.constraints.controllers.NetworkConnectedController;
import android.arch.background.workmanager.impl.constraints.controllers.NetworkMeteredController;
import android.arch.background.workmanager.impl.constraints.controllers.NetworkNotRoamingController;
import android.arch.background.workmanager.impl.constraints.controllers.NetworkUnmeteredController;
import android.arch.background.workmanager.impl.constraints.controllers.StorageNotLowController;
import android.arch.background.workmanager.impl.logger.Logger;
import android.arch.background.workmanager.impl.model.WorkSpec;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.annotation.VisibleForTesting;

import java.util.ArrayList;
import java.util.List;

/**
 * Tracks {@link WorkSpec}s and their {@link Constraints}, and notifies a
 * {@link WorkConstraintsCallback} when all of their constraints are met or not met.
 */

public class WorkConstraintsTracker implements ConstraintController.OnConstraintUpdatedCallback {

    private static final String TAG = "WorkConstraintsTracker";

    private final WorkConstraintsCallback mCallback;
    private final ConstraintController[] mConstraintControllers;

    public WorkConstraintsTracker(Context context, WorkConstraintsCallback callback) {
        Context appContext = context.getApplicationContext();
        mCallback = callback;
        mConstraintControllers = new ConstraintController[] {
                new BatteryChargingController(appContext, this),
                new BatteryNotLowController(appContext, this),
                new StorageNotLowController(appContext, this),
                new NetworkConnectedController(appContext, this),
                new NetworkUnmeteredController(appContext, this),
                new NetworkNotRoamingController(appContext, this),
                new NetworkMeteredController(appContext, this)
        };
    }

    @VisibleForTesting
    WorkConstraintsTracker(WorkConstraintsCallback callback, ConstraintController[] controllers) {
        mCallback = callback;
        mConstraintControllers = controllers;
    }

    /**
     * Replaces the list of tracked {@link WorkSpec}s to monitor if their constraints are met.
     *
     * @param workSpecs A list of {@link WorkSpec}s to monitor constraints for
     */
    public void replace(@NonNull List<WorkSpec> workSpecs) {
        for (ConstraintController controller : mConstraintControllers) {
            controller.replace(workSpecs);
        }
    }

    /**
     * Resets and clears all tracked {@link WorkSpec}s.
     */
    public void reset() {
        for (ConstraintController controller : mConstraintControllers) {
            controller.reset();
        }
    }

    private boolean areAllConstraintsMet(@NonNull String workSpecId) {
        for (ConstraintController constraintController : mConstraintControllers) {
            if (constraintController.isWorkSpecConstrained(workSpecId)) {
                Logger.debug(TAG, "Work %s constrained by %s", workSpecId,
                        constraintController.getClass().getSimpleName());
                return false;
            }
        }
        return true;
    }

    @Override
    public void onConstraintMet(@NonNull List<String> workSpecIds) {
        List<String> unconstrainedWorkSpecIds = new ArrayList<>();
        for (String workSpecId : workSpecIds) {
            if (areAllConstraintsMet(workSpecId)) {
                Logger.debug(TAG, "Constraints met for %s", workSpecId);
                unconstrainedWorkSpecIds.add(workSpecId);
            }
        }
        mCallback.onAllConstraintsMet(unconstrainedWorkSpecIds);
    }

    @Override
    public void onConstraintNotMet(@NonNull List<String> workSpecIds) {
        mCallback.onAllConstraintsNotMet(workSpecIds);
    }
}
