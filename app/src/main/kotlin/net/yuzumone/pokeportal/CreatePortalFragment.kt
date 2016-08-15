package net.yuzumone.pokeportal

import android.app.Dialog
import android.location.Location
import android.os.Bundle
import android.support.v4.app.DialogFragment
import android.support.v7.widget.Toolbar
import android.view.ViewGroup
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import com.google.android.gms.maps.CameraUpdateFactory
import com.google.android.gms.maps.GoogleMap
import com.google.android.gms.maps.OnMapReadyCallback
import com.google.android.gms.maps.SupportMapFragment
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.android.gms.maps.model.Marker
import com.google.android.gms.maps.model.MarkerOptions

class CreatePortalFragment : DialogFragment(), OnMapReadyCallback {

    lateinit private var mDialog: Dialog
    lateinit private var mToolbar: Toolbar
    lateinit private var mEditText: EditText
    lateinit private var mLocation: Location
    private var mPortalLocation: LatLng? = null

    /*
     * true: PokeStop
     * false: Gym
     */
    private var mPortalType: Boolean = true

    companion object {
        val ARG_LOCATION = "location"
        val ARG_TYPE = "bool"
        fun newPokeStopInstance(location: Location): CreatePortalFragment {
            val fragment = CreatePortalFragment()
            val args = Bundle()
            args.putParcelable(ARG_LOCATION, location)
            args.putBoolean(ARG_TYPE, true)
            fragment.arguments = args
            return fragment
        }
        fun newGymInstance(location: Location): CreatePortalFragment {
            val fragment = CreatePortalFragment()
            val args = Bundle()
            args.putParcelable(ARG_LOCATION, location)
            args.putBoolean(ARG_TYPE, false)
            fragment.arguments = args
            return fragment
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mLocation = arguments.getParcelable(ARG_LOCATION)
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
        val position = LatLng(mLocation.latitude, mLocation.longitude)
        googleMap?.addMarker(MarkerOptions().position(position).draggable(true))
        val cameraPosition = CameraPosition
                .builder()
                .target(position)
                .zoom(15f)
                .build()
        googleMap?.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition))
        googleMap?.setOnMarkerDragListener(object : GoogleMap.OnMarkerDragListener {
            override fun onMarkerDragEnd(marker: Marker?) {
                mPortalLocation = marker!!.position
            }

            override fun onMarkerDrag(marker: Marker?) {

            }

            override fun onMarkerDragStart(marker: Marker?) {

            }
        })
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

    fun savePortal() {
        // ToDo
        dismiss()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val fragment = fragmentManager.findFragmentById(R.id.map) as SupportMapFragment
        fragmentManager.beginTransaction().remove(fragment).commit()
    }
}