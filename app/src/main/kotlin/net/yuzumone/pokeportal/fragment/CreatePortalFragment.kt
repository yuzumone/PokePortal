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

    lateinit private var mListener: OnCreatePortal
    lateinit private var mDialog: Dialog
    lateinit private var mToolbar: Toolbar
    lateinit private var mEditText: EditText
    lateinit private var mPortalLocation: LatLng

    /*
     * true: PokeStop
     * false: Gym
     */
    private var mPortalType: Boolean = true

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
        mListener = context
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPortalLocation = arguments.getParcelable(ARG_LOCATION)
        mPortalType = arguments.getBoolean(ARG_TYPE)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        mDialog = Dialog(activity)
        mDialog.window.requestFeature(Window.FEATURE_NO_TITLE)
        mDialog.setContentView(R.layout.fragment_create_portal)
        mDialog.window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_RESIZE)
        mDialog.window.setLayout(ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.MATCH_PARENT)
        mToolbar = mDialog.findViewById(R.id.toolbar) as Toolbar
        mEditText = mDialog.findViewById(R.id.edit_name) as EditText

        return mDialog
    }

    override fun onMapReady(googleMap: GoogleMap?) {
        googleMap?.let {
            it.addMarker(MarkerOptions().position(mPortalLocation).draggable(true))
            val cameraPosition = CameraPosition
                    .builder()
                    .target(mPortalLocation)
                    .zoom(15f)
                    .build()
            it.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
            it.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
                override fun onMarkerDragEnd(marker: Marker?) {
                    marker?.let {
                        mPortalLocation = it.position
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

        mToolbar.setNavigationIcon(R.drawable.ic_action_back)
        mToolbar.setNavigationOnClickListener { dismiss() }
        mToolbar.inflateMenu(R.menu.menu_create_portal)
        mToolbar.setOnMenuItemClickListener { item ->
            when (item?.itemId) {
                R.id.menu_save -> savePortal()
            }
            false
        }
        if (mPortalType) {
            mToolbar.title = "PokeStop"
            mEditText.hint = "PokeStop Name"
        } else {
            mToolbar.title = "Gym"
            mEditText.hint = "Gym Name"
        }
    }

    private fun savePortal() {
        Realm.getDefaultInstance().use { realm ->
            realm.executeTransaction {
                if (mPortalType) {
                    val pokeStop = realm.createObject(PokeStop::class.java)
                    pokeStop.uuid = UUID.randomUUID().toString()
                    pokeStop.name = mEditText.text.toString()
                    pokeStop.latitude = mPortalLocation.latitude
                    pokeStop.longitude = mPortalLocation.longitude
                    mListener.createPokeStop(pokeStop)
                    Toast.makeText(activity, "Create ${pokeStop.name}", Toast.LENGTH_LONG).show()
                } else {
                    val gym = realm.createObject(Gym::class.java)
                    gym.uuid = UUID.randomUUID().toString()
                    gym.name = mEditText.text.toString()
                    gym.latitude = mPortalLocation.latitude
                    gym.longitude = mPortalLocation.longitude
                    mListener.createGym(gym)
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