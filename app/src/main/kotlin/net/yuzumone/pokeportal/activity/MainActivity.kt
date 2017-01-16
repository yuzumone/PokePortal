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

package net.yuzumone.pokeportal.activity

import android.location.Location
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.util.Log
import com.getbase.floatingactionbutton.FloatingActionButton
import com.getbase.floatingactionbutton.FloatingActionsMenu
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.*
import io.realm.Realm
import io.realm.RealmConfiguration
import net.yuzumone.pokeportal.R
import net.yuzumone.pokeportal.data.Gym
import net.yuzumone.pokeportal.data.PokeStop
import net.yuzumone.pokeportal.fragment.CreatePortalFragment
import net.yuzumone.pokeportal.fragment.DeletePortalFragment
import net.yuzumone.pokeportal.fragment.LocationFragment
import net.yuzumone.pokeportal.listener.OnCreatePortal
import net.yuzumone.pokeportal.listener.OnDeletePortal
import net.yuzumone.pokeportal.listener.OnLocation
import net.yuzumone.pokeportal.util.ResourceUtil
import org.json.JSONArray
import org.json.JSONObject

class MainActivity : AppCompatActivity(), OnLocation, OnMapReadyCallback, OnCreatePortal, OnDeletePortal {

    protected val TAG = "MainActivity"
    lateinit private var mLocation: LatLng
    lateinit private var mActionsMenu: FloatingActionsMenu
    private var mGoogleMap: GoogleMap? = null
    private var mDeleteMarker: Marker? = null

    override fun getLocation(location: Location) {
        Log.d(TAG, location.toString())
        mLocation = LatLng(location.latitude, location.longitude)
        val fragment = SupportMapFragment()
        fragment.getMapAsync(this)
        supportFragmentManager.beginTransaction().add(R.id.content, fragment).commit()
        initializeActionsMenu()
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.let {
            mGoogleMap = it
            val cameraPosition = CameraPosition
                    .builder()
                    .target(mLocation)
                    .zoom(15f)
                    .build()
            it.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            it.setOnMarkerClickListener { marker ->
                mDeleteMarker = marker
                val fragment = DeletePortalFragment.newInstance(marker.title, marker.snippet)
                fragment.show(supportFragmentManager, "delete")
                true
            }
            loadPortal(it)
        }
    }

    fun loadPortal(googleMap: GoogleMap) {
        val pokeStopIcon = BitmapDescriptorFactory
                .fromBitmap(ResourceUtil.getBitmap(this, R.drawable.ic_marker_poke_stop))
        val gymIcon = BitmapDescriptorFactory
                .fromBitmap(ResourceUtil.getBitmap(this, R.drawable.ic_marker_gym))
        Realm.getDefaultInstance().use { realm ->
            realm.where(PokeStop::class.java).findAll().forEach {
                val markerOption = MarkerOptions()
                        .title(it.name)
                        .position(LatLng(it.latitude, it.longitude))
                        .icon(pokeStopIcon)
                        .anchor(0.5f, 0.5f)
                        .snippet(it.uuid)
                googleMap.addMarker(markerOption)
            }
            realm.where(Gym::class.java).findAll().forEach {
                val markerOption = MarkerOptions()
                        .title(it.name)
                        .position(LatLng(it.latitude, it.longitude))
                        .icon(gymIcon)
                        .anchor(0.5f, 0.5f)
                        .snippet(it.uuid)
                googleMap.addMarker(markerOption)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mActionsMenu = findViewById(R.id.actions_menu) as FloatingActionsMenu
        val fragment = LocationFragment()
        fragment.show(supportFragmentManager, "location")

        val realmConfig = RealmConfiguration.Builder(this).build()
        Realm.setDefaultConfiguration(realmConfig)
    }

    fun initializeActionsMenu() {
        val pokeStopMenu = FloatingActionButton(baseContext)
        pokeStopMenu.setIcon(R.drawable.ic_menu_poke_stop)
        pokeStopMenu.title = "PokeStop"
        pokeStopMenu.size = FloatingActionButton.SIZE_MINI
        pokeStopMenu.setOnClickListener {
            val fragment = CreatePortalFragment.newPokeStopInstance(mLocation)
            fragment.show(supportFragmentManager, "poke_stop")
            mActionsMenu.collapse()
        }
        mActionsMenu.addButton(pokeStopMenu)
        val gymMenu = FloatingActionButton(baseContext)
        gymMenu.setIcon(R.drawable.ic_menu_gym)
        gymMenu.title = "Gym"
        gymMenu.size = FloatingActionButton.SIZE_MINI
        gymMenu.setOnClickListener {
            val fragment = CreatePortalFragment.newGymInstance(mLocation)
            fragment.show(supportFragmentManager, "gym")
            mActionsMenu.collapse()
        }
        mActionsMenu.addButton(gymMenu)
    }

    override fun createGym(gym: Gym) {
        mGoogleMap?.let {
            val gymIcon = BitmapDescriptorFactory
                    .fromBitmap(ResourceUtil.getBitmap(this, R.drawable.ic_marker_gym))
            val markerOption = MarkerOptions()
                    .title(gym.name)
                    .position(LatLng(gym.latitude, gym.longitude))
                    .icon(gymIcon)
                    .anchor(0.5f, 0.5f)
                    .snippet(gym.uuid)
            it.addMarker(markerOption)
        }
    }

    override fun createPokeStop(pokeStop: PokeStop) {
        mGoogleMap?.let {
            val pokeStopIcon = BitmapDescriptorFactory
                    .fromBitmap(ResourceUtil.getBitmap(this, R.drawable.ic_marker_poke_stop))
            val markerOption = MarkerOptions()
                    .title(pokeStop.name)
                    .position(LatLng(pokeStop.latitude, pokeStop.longitude))
                    .icon(pokeStopIcon)
                    .anchor(0.5f, 0.5f)
                    .snippet(pokeStop.uuid)
            it.addMarker(markerOption)
        }
    }

    override fun deletePortal(uuid: String) {
        mDeleteMarker?.let { it.remove() }
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction {
                realm.where(PokeStop::class.java)
                        .equalTo("uuid", uuid).findAll().clear()
                realm.where(Gym::class.java)
                        .equalTo("uuid", uuid).findAll().clear()
            }
        }
    }

    private fun createJSON() : String {
        val json = JSONObject()
        val stopJson = JSONArray()
        val gymJson = JSONArray()
        Realm.getDefaultInstance().use { realm ->
            realm.where(PokeStop::class.java).findAll().forEach {
                val stop = JSONObject()
                stop.put("name", it.name)
                stop.put("latitude", it.latitude)
                stop.put("longitude", it.longitude)
                stop.put("uuid", it.uuid)
                stopJson.put(stop)
            }
            realm.where(Gym::class.java).findAll().forEach {
                val gym = JSONObject()
                gym.put("name", it.name)
                gym.put("latitude", it.latitude)
                gym.put("longitude", it.longitude)
                gym.put("uuid", it.uuid)
                gymJson.put(gym)
            }
        }
        json.put("stop", stopJson)
        json.put("gym", gymJson)
        return json.toString()
    }

}
