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

package net.yuzumone.pokeportal

import android.location.Location
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.MapFragment
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.MarkerOptions
import net.yuzumone.pokeportal.listener.OnLocation

class MainActivity : AppCompatActivity(), OnLocation, OnMapReadyCallback {

    protected val TAG = "MainActivity"
    lateinit private var mLocation: Location

    override fun getLocation(location: Location) {
        Log.d(TAG, location.toString())
        mLocation = location
        val fragment = MapFragment()
        fragment.getMapAsync(this)
        fragmentManager.beginTransaction().add(android.R.id.content, fragment).commit()
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        val position = LatLng(mLocation.latitude, mLocation.longitude)
        googleMap?.addMarker(MarkerOptions().position(position))
        val cameraPosition = CameraPosition
                .builder()
                .target(position)
                .zoom(15f)
                .build()
        googleMap?.animateCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val fragment = LocationFragment()
        fragment.show(supportFragmentManager, "location")
    }
}
