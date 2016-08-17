/*
 * Copyright (C) 2016 yuzumone
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
 *
 */

package net.yuzumone.pokeportal.fragment

import android.Manifest
import android.app.Dialog
import android.app.ProgressDialog
import android.content.Context
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v4.content.ContextCompat
import android.util.Log
import com.google.android.gms.common.ConnectionResult
import com.google.android.gms.common.api.GoogleApiClient
import com.google.android.gms.location.LocationListener
import com.google.android.gms.location.LocationRequest
import com.google.android.gms.location.LocationServices
import net.yuzumone.pokeportal.listener.OnLocation

class LocationFragment : DialogFragment(),
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        LocationListener {

    protected val TAG = "LocationFragment"
    private val REQUEST_LOCATION = 1
    lateinit private var mListener: OnLocation
    lateinit private var mProgress: ProgressDialog
    lateinit private var mGoogleApiClient: GoogleApiClient

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context !is OnLocation) {
            throw ClassCastException("Don't implement Listener.")
        }
        mListener = context
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mProgress = ProgressDialog(activity)
        mProgress.setMessage("Scanning location")
        mProgress.setProgressStyle(ProgressDialog.STYLE_SPINNER)

        buildGoogleApiClient()
        checkPermission()
        return mProgress
    }

    fun checkPermission() {
        if (ContextCompat.checkSelfPermission(activity,
                Manifest.permission.ACCESS_FINE_LOCATION) !== PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf<String>(Manifest.permission.ACCESS_FINE_LOCATION,
                    Manifest.permission.ACCESS_COARSE_LOCATION), REQUEST_LOCATION)
        } else {
            mGoogleApiClient.connect()
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == REQUEST_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                mGoogleApiClient.connect()
            }
        } else {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        }
    }

    @Synchronized protected fun buildGoogleApiClient() {
        mGoogleApiClient = GoogleApiClient.Builder(activity)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build()
    }

    override fun onConnected(connectionHint: Bundle?) {
        Log.d(TAG, "onConnected")
        val location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient)
        if (location != null) {
            mListener.getLocation(location)
            dismiss()
        } else {
            startLocationUpdates()
        }
    }

    protected fun startLocationUpdates() {
        val request = LocationRequest
                .create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10000)
                .setFastestInterval(2000)
        LocationServices.FusedLocationApi.requestLocationUpdates(mGoogleApiClient, request, this)
    }

    override fun onLocationChanged(location: Location?) {
        Log.d(TAG, "onLocationChanged")
        if (location != null) {
            mListener.getLocation(location)
            LocationServices.FusedLocationApi.removeLocationUpdates(mGoogleApiClient, this)
            dismiss()
        }
    }

    override fun onConnectionSuspended(cause: Int) {
        Log.d(TAG, "onConnectionSuspended")
    }

    override fun onConnectionFailed(ConnectionResult: ConnectionResult) {
        Log.d(TAG, "onConnectionFailed")
    }

    override fun onStop() {
        super.onStop()
        mGoogleApiClient.disconnect()
    }
}