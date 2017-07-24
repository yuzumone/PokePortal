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
import android.nfc.NdefMessage
import android.nfc.NdefRecord
import android.nfc.NfcAdapter
import android.nfc.NfcEvent
import android.os.Bundle
import android.support.v7.app.AppCompatActivity
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
import java.nio.charset.Charset

class MainActivity : AppCompatActivity(), OnLocation, OnMapReadyCallback, OnCreatePortal,
        OnDeletePortal, NfcAdapter.CreateNdefMessageCallback {

    private lateinit var location: LatLng
    private lateinit var actionsMenu: FloatingActionsMenu
    private lateinit var googleMap: GoogleMap
    private lateinit var deleteMarker: Marker
    private lateinit var nfcAdapter: NfcAdapter

    override fun getLocation(location: Location) {
        this.location = LatLng(location.latitude, location.longitude)
        val fragment = SupportMapFragment()
        fragment.getMapAsync(this)
        supportFragmentManager.beginTransaction().add(R.id.content, fragment).commit()
        initializeActionsMenu()
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.let { map ->
            this.googleMap = map
            val cameraPosition = CameraPosition
                    .builder()
                    .target(location)
                    .zoom(15f)
                    .build()
            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            map.setOnMarkerClickListener { marker ->
                deleteMarker = marker
                val fragment = DeletePortalFragment.newInstance(marker.title, marker.snippet)
                fragment.show(supportFragmentManager, "delete")
                true
            }
            loadPortal(map)
        }
    }

    private fun loadPortal(googleMap: GoogleMap) {
        val pokeStopIcon = BitmapDescriptorFactory
                .fromBitmap(ResourceUtil.getBitmap(this, R.drawable.ic_marker_poke_stop))
        val gymIcon = BitmapDescriptorFactory
                .fromBitmap(ResourceUtil.getBitmap(this, R.drawable.ic_marker_gym))
        Realm.getDefaultInstance().use { realm ->
            realm.where(PokeStop::class.java).findAll().forEach { stop ->
                val markerOption = MarkerOptions()
                        .title(stop.name)
                        .position(LatLng(stop.latitude, stop.longitude))
                        .icon(pokeStopIcon)
                        .anchor(0.5f, 0.5f)
                        .snippet(stop.uuid)
                googleMap.addMarker(markerOption)
            }
            realm.where(Gym::class.java).findAll().forEach { gym ->
                val markerOption = MarkerOptions()
                        .title(gym.name)
                        .position(LatLng(gym.latitude, gym.longitude))
                        .icon(gymIcon)
                        .anchor(0.5f, 0.5f)
                        .snippet(gym.uuid)
                googleMap.addMarker(markerOption)
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        actionsMenu = findViewById(R.id.actions_menu) as FloatingActionsMenu
        val fragment = LocationFragment()
        fragment.show(supportFragmentManager, "location")

        val realmConfig = RealmConfiguration.Builder(this).build()
        Realm.setDefaultConfiguration(realmConfig)

        nfcAdapter = NfcAdapter.getDefaultAdapter(this)
        nfcAdapter.setNdefPushMessageCallback(this, this)
    }

    public override fun onResume() {
        super.onResume()
        if (NfcAdapter.ACTION_NDEF_DISCOVERED == intent.action) {
            val rawMsgs = intent.getParcelableArrayExtra(NfcAdapter.EXTRA_NDEF_MESSAGES)
            val msg = rawMsgs[0] as NdefMessage
            val json = msg.records[0].payload.toString(Charset.forName("UTF-8"))
            storePortal(json)
        }
    }

    private fun initializeActionsMenu() {
        val pokeStopMenu = FloatingActionButton(baseContext)
        pokeStopMenu.apply {
            setIcon(R.drawable.ic_menu_poke_stop)
            title = "PokeStop"
            size = FloatingActionButton.SIZE_MINI
            setOnClickListener {
                val fragment = CreatePortalFragment.newPokeStopInstance(location)
                fragment.show(supportFragmentManager, "poke_stop")
                actionsMenu.collapse()
            }
        }
        actionsMenu.addButton(pokeStopMenu)
        val gymMenu = FloatingActionButton(baseContext)
        gymMenu.apply {
            setIcon(R.drawable.ic_menu_gym)
            title = "Gym"
            size = FloatingActionButton.SIZE_MINI
            setOnClickListener {
                val fragment = CreatePortalFragment.newGymInstance(location)
                fragment.show(supportFragmentManager, "gym")
                actionsMenu.collapse()
            }
        }
        actionsMenu.addButton(gymMenu)
    }

    override fun createGym(gym: Gym) {
        val gymIcon = BitmapDescriptorFactory
                .fromBitmap(ResourceUtil.getBitmap(this, R.drawable.ic_marker_gym))
        val markerOption = MarkerOptions()
                .title(gym.name)
                .position(LatLng(gym.latitude, gym.longitude))
                .icon(gymIcon)
                .anchor(0.5f, 0.5f)
                .snippet(gym.uuid)
        googleMap.addMarker(markerOption)
    }

    override fun createPokeStop(pokeStop: PokeStop) {
        val pokeStopIcon = BitmapDescriptorFactory
                .fromBitmap(ResourceUtil.getBitmap(this, R.drawable.ic_marker_poke_stop))
        val markerOption = MarkerOptions()
                .title(pokeStop.name)
                .position(LatLng(pokeStop.latitude, pokeStop.longitude))
                .icon(pokeStopIcon)
                .anchor(0.5f, 0.5f)
                .snippet(pokeStop.uuid)
        googleMap.addMarker(markerOption)
    }

    override fun deletePortal(uuid: String) {
        deleteMarker.remove()
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

    private fun storePortal(json: String) {
        val jsonObject = JSONObject(json)
        val stops = jsonObject.getJSONArray("stop")
        val gyms = jsonObject.getJSONArray("gym")
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction {
                for (i in 0..stops.length() - 1)
                    realm.createObjectFromJson(PokeStop::class.java, stops.getJSONObject(i))
                for (i in 0..gyms.length() - 1)
                    realm.createObjectFromJson(Gym::class.java, gyms.getJSONObject(i))
            }
        }
    }

    override fun createNdefMessage(event: NfcEvent?): NdefMessage {
        return NdefMessage(
                arrayOf(createMimeRecord(
                        "application/net.yuzumone.pokeportal", createJSON().toByteArray()))
        )
    }

    private fun createMimeRecord(mimeType: String, payload: ByteArray): NdefRecord {
        val mimeBytes = mimeType.toByteArray(Charset.forName("UTF-8"))
        val mimeRecord = NdefRecord(
                NdefRecord.TNF_MIME_MEDIA, mimeBytes, ByteArray(0), payload)
        return mimeRecord
    }
}
