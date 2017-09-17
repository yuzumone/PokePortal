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

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.Toolbar
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions
import io.realm.Realm
import net.yuzumone.pokeportal.R
import net.yuzumone.pokeportal.data.Gym
import net.yuzumone.pokeportal.data.PokeStop
import net.yuzumone.pokeportal.listener.OnCreatePortal
import java.util.*

class CreatePortalFragment : DialogFragment(), OnMapReadyCallback {

    private lateinit var listener: OnCreatePortal
    private lateinit var toolbar: Toolbar
    private lateinit var editText: EditText
    private lateinit var portalLocation: LatLng

    /*
     * true: PokeStop
     * false: Gym
     */
    private var portalType: Boolean = true

    companion object {
        val ARG_LOCATION = "location"
        val ARG_TYPE = "bool"
        fun newPokeStopInstance(location: LatLng): CreatePortalFragment {
            val fragment = CreatePortalFragment()
            val args = Bundle()
            args.putParcelable(ARG_LOCATION, location)
            args.putBoolean(ARG_TYPE, true)
            fragment.arguments = args
            return fragment
        }
        fun newGymInstance(location: LatLng): CreatePortalFragment {
            val fragment = CreatePortalFragment()
            val args = Bundle()
            args.putParcelable(ARG_LOCATION, location)
            args.putBoolean(ARG_TYPE, false)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onAttach(context: Context?) {
        super.onAttach(context)

        if (context !is OnCreatePortal) {
            throw ClassCastException("Don't implement Listener.")
        }
        listener = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        portalLocation = arguments.getParcelable(ARG_LOCATION)
        portalType = arguments.getBoolean(ARG_TYPE)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = Dialog(activity)
        dialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        dialog.setContentView(R.layout.fragment_create_portal)
        dialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        dialog.window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        toolbar = dialog.findViewById(R.id.toolbar)
        editText = dialog.findViewById(R.id.edit_name)
        return dialog
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.let { map ->
            map.addMarker(MarkerOptions().position(portalLocation).draggable(true))
            val cameraPosition = CameraPosition
                    .builder()
                    .target(portalLocation)
                    .zoom(15f)
                    .build()
            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            map.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
                override fun onMarkerDragEnd(marker: Marker?) {
                    marker?.let { marker ->
                        portalLocation = marker.position
                    }
                }

                override fun onMarkerDrag(marker: Marker?) {

                }

                override fun onMarkerDragStart(marker: Marker?) {

                }
            })
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        val mapFragment = fragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        mapFragment.getMapAsync(this)

        toolbar.setNavigationIcon(R.drawable.ic_action_back)
        toolbar.setNavigationOnClickListener { dismiss() }
        toolbar.inflateMenu(R.menu.menu_create_portal)
        toolbar.setOnMenuItemClickListener { item ->
            when (item?.itemId) {
                R.id.menu_save -> savePortal()
            }
            false
        }
        if (portalType) {
            toolbar.title = "PokeStop"
            editText.hint = "PokeStop Name"
        } else {
            toolbar.title = "Gym"
            editText.hint = "Gym Name"
        }
    }

    private fun savePortal() {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction {
                if (portalType) {
                    val uuid = UUID.randomUUID().toString()
                    val pokeStop = realm.createObject(PokeStop::class.java, uuid)
                    pokeStop.name = editText.text.toString()
                    pokeStop.latitude = portalLocation.latitude
                    pokeStop.longitude = portalLocation.longitude
                    listener.createPokeStop(pokeStop)
                    Toast.makeText(activity, "Create ${pokeStop.name}", Toast.LENGTH_LONG).show()
                } else {
                    val uuid = UUID.randomUUID().toString()
                    val gym = realm.createObject(Gym::class.java, uuid)
                    gym.name = editText.text.toString()
                    gym.latitude = portalLocation.latitude
                    gym.longitude = portalLocation.longitude
                    listener.createGym(gym)
                    Toast.makeText(activity, "Create ${gym.name}", Toast.LENGTH_LONG).show()
                }
            }
        }
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val fragment = fragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        fragmentManager.beginTransaction().remove(fragment).commit()
    }
}