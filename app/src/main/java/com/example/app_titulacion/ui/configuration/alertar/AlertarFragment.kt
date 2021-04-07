package com.example.app_titulacion.ui.configuration.alertar

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.Manifest.permission.ACCESS_FINE_LOCATION
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.location.Location
import android.location.LocationManager
import android.os.Looper
import android.provider.Settings
import android.telephony.SmsManager
import android.util.Log
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import com.example.app_titulacion.R
import com.example.app_titulacion.data.model.Contact
import com.example.app_titulacion.databinding.FragmentAlertarBinding
import com.example.app_titulacion.utils.Constants
import com.example.app_titulacion.utils.Status
import com.example.app_titulacion.utils.showToast
import com.google.android.gms.location.*
import com.google.firebase.firestore.FirebaseFirestore
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class AlertarFragment : Fragment(R.layout.fragment_alertar) {

    private val TAG = "Aletar Fragment"

    private var _binding: FragmentAlertarBinding? = null
    private val binding get() = _binding!!
    private lateinit var sharedPreferences: SharedPreferences
    private val notificacionViewModel: AlertarViewModel by viewModels()

    private val db = FirebaseFirestore.getInstance()


    private var mFusedLocationProviderClient: FusedLocationProviderClient? = null
    private val INTERVAL: Long = 2000
    private val FASTEST_INTERVAL: Long = 1000
    lateinit var mLastLocation: Location
    private lateinit var mLocationRequest: LocationRequest
    private val REQUEST_PERMISSION_LOCATION = 10


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentAlertarBinding.inflate(inflater, container, false)

        return binding.root


    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        sharedPreferences =
            this.requireActivity().getSharedPreferences(Constants.APP_PREF, Context.MODE_PRIVATE)

        val email: String = sharedPreferences.getString(Constants.APP_EMAIL, "").toString()

        mLocationRequest = LocationRequest()

        val locationManager =
            activity?.getSystemService(Context.LOCATION_SERVICE) as LocationManager

        if (!locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
            buildAlertMessageNoGps()
        }

        if (checkPermissionForLocation(this.requireContext())) {
            startLocationUpdates()
        }


        with(binding) {

            acosoSexualButton.setOnClickListener() {
                notificacionViewModel.doSendNotificationAcosoSexual(
                    email,
                    tvLatitud.text.toString(),
                    tvLongitud.text.toString()
                )
                mandarSms(email, tvLatitud.text.toString(), tvLongitud.text.toString())
            }
            agrecionVerbalButton.setOnClickListener() {
                notificacionViewModel.doSendNotificationAgresionVerbal(
                    email,
                    tvLatitud.text.toString(),
                    tvLongitud.text.toString()
                )
                mandarSms(email, tvLatitud.text.toString(), tvLongitud.text.toString())
            }
            agrecionFisicaButton.setOnClickListener() {
                notificacionViewModel.doSendNotificationAgresionFisica(
                    email,
                    tvLatitud.text.toString(),
                    tvLongitud.text.toString()
                )
                mandarSms(email, tvLatitud.text.toString(), tvLongitud.text.toString())
            }
        }

        subscribe()

    }

    private fun mandarMail() {
        //TODO ENVIAR CORREO
    }

    private fun mandarSms(email: String, latitud: String, longitud: String) {
        if (checkPermissionsSms()) {
            listadeContactosConfianza(
                email,
                latitud,
                longitud
            )
        }
    }

    private fun subscribe() {

        notificacionViewModel.sendNotificationAcosoSexual.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    Log.d(TAG, "LOADING")
                }
                Status.SUCCESS -> {
                    Log.d(TAG, "SUCCESS")
                    val gson = Gson()
                    Log.d(TAG, gson.toJson(it.data))
                    showToast(getString(R.string.msjCorrecto))
                }
                Status.ERROR -> {
                    Log.d(TAG, "ERROR ${it.message!!}")
                }
            }
        }

        notificacionViewModel.sendNotificationAgresionVerbal.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    Log.d(TAG, "LOADING")
                }
                Status.SUCCESS -> {
                    Log.d(TAG, "SUCCESS")
                    val gson = Gson()
                    Log.d(TAG, gson.toJson(it.data))
                    showToast(getString(R.string.msjCorrecto))
                }
                Status.ERROR -> {
                    Log.d(TAG, "ERROR ${it.message!!}")
                }
            }
        }

        notificacionViewModel.sendNotificationAgresionFisica.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    Log.d(TAG, "LOADING")
                }
                Status.SUCCESS -> {
                    Log.d(TAG, "SUCCESS")
                    val gson = Gson()
                    Log.d(TAG, gson.toJson(it.data))
                    showToast(getString(R.string.msjCorrecto))
                }
                Status.ERROR -> {
                    Log.d(TAG, "ERROR ${it.message!!}")
                }
            }
        }
    }


    // region Gps
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        //SOLO GPS
        if (requestCode == REQUEST_PERMISSION_LOCATION) {
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Agregar el método startlocationUpdate () más tarde en lugar de Toast
                Toast.makeText(this.requireContext(), "Permiso concedido.", Toast.LENGTH_SHORT)
                    .show()
            }
        }

        //SOLO ENVIAR SMS

        if (requestCode == 777) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                //Todo_ bien SMS
            } else {
                Toast.makeText(this.requireContext(), "Permisos rechazados.", Toast.LENGTH_SHORT)
                    .show()
            }
        }
    }

    fun checkPermissionForLocation(context: Context): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {

            if (context.checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) ==
                PackageManager.PERMISSION_GRANTED
            ) {
                true
            } else {
                // Mostrar la solicitud de permiso
                ActivityCompat.requestPermissions(
                    this.requireActivity(), arrayOf(Manifest.permission.ACCESS_FINE_LOCATION),
                    REQUEST_PERMISSION_LOCATION
                )
                false
            }
        } else {
            true
        }
    }

    private fun buildAlertMessageNoGps() {

        val builder = AlertDialog.Builder(this.requireContext())
        builder.setMessage("Tu GPS parece estar desactivado, ¿Quieres activarlo?")
            .setCancelable(false)
            .setPositiveButton("Sí") { dialog, id ->
                startActivityForResult(
                    Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)
                    , 11
                )
            }
            .setNegativeButton("No") { dialog, id ->
                dialog.cancel()
//                finish()
            }
        val alert: AlertDialog = builder.create()
        alert.show()

    }

    private val mLocationCallback = object : LocationCallback() {
        override fun onLocationResult(locationResult: LocationResult) {
            // do work here
            locationResult.lastLocation
            onLocationChanged(locationResult.lastLocation)
        }
    }

    fun onLocationChanged(location: Location) {
        //Se ha determinado la nueva ubicación
        with(binding) {

            mLastLocation = location
            if (mLastLocation.latitude.toString() == "") {

            } else {
                tvLatitud.text = "" + mLastLocation.latitude
                tvLongitud.text = "" + mLastLocation.longitude
            }

        }
    }

    private fun startLocationUpdates() {

        //Cree la solicitud de ubicación para comenzar a recibir actualizaciones

//        mLocationRequest = LocationRequest()
        mLocationRequest = LocationRequest()
        mLocationRequest!!.priority = LocationRequest.PRIORITY_HIGH_ACCURACY
        mLocationRequest!!.setInterval(INTERVAL)
        mLocationRequest!!.setFastestInterval(FASTEST_INTERVAL)

        // Crear objeto LocationSettingsRequest usando la solicitud de ubicación

        val builder = LocationSettingsRequest.Builder()
        builder.addLocationRequest(mLocationRequest!!)
        val locationSettingsRequest = builder.build()

        val settingsClient = LocationServices.getSettingsClient(this.requireActivity())
        settingsClient.checkLocationSettings(locationSettingsRequest)

        mFusedLocationProviderClient =
            LocationServices.getFusedLocationProviderClient(this.requireActivity())
        // El nuevo SDK de API de Google v11 usa getFusedLocationProviderClient (this)
        if (ActivityCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.ACCESS_FINE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(
                this.requireActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION
            ) != PackageManager.PERMISSION_GRANTED
        ) {

            return
        }
        mFusedLocationProviderClient!!.requestLocationUpdates(
            mLocationRequest, mLocationCallback,
            Looper.myLooper()
        )
    }

    private fun stoplocationUpdates() {
        mFusedLocationProviderClient!!.removeLocationUpdates(mLocationCallback)
    }

//endregion


    // region Sms

    private fun listadeContactosConfianza(email: String, latitud: String, longitud: String) {

        db.collection(Constants.USER_COL).document(email).collection(Constants.COLEC_CONTACT)
            .addSnapshotListener { querySnapshot, firebaseFirestoreException ->
                if (firebaseFirestoreException != null) {
                    Log.e(TAG, "firebaseFirestoreException", firebaseFirestoreException)
                }

                val list = mutableListOf<Contact>()
                querySnapshot!!.forEach { queryDocumentSnapshot ->
                    list.add(queryDocumentSnapshot.toObject(Contact::class.java))
                }

                //solo se puede enviar con 160 caracteres como maximo
                val urlMaps =
                    "WalkSafe: $email https://www.google.es/maps?q=$latitud,$longitud"

                for (sendCell in list) {
                    sendSMS(urlMaps, sendCell.celular.toString())
                }
                showToast(getString(R.string.msjCorrectoSms))

            }


    }

    private fun sendSMS(
        smsMsj: String,
        cel: String
    ) {

        if (cel.count() > 0) {
            val sms: SmsManager = SmsManager.getDefault()
            sms.sendTextMessage(
                cel,
                null,
                smsMsj,
                null,
                null
            )
        }
    }

    private fun checkPermissionsSms(): Boolean {

        var r: Boolean = false

        if (ContextCompat.checkSelfPermission(
                this.requireContext(),
                Manifest.permission.SEND_SMS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            //permiso no aceptado por el momento
            requestSmsPermission()

        } else {
            r = true
            return r
        }
        return r
    }

    private fun requestSmsPermission() {
        if (ActivityCompat.shouldShowRequestPermissionRationale(
                this.requireActivity(),
                Manifest.permission.SEND_SMS
            )
        ) {
            //El usuaario ya ha rechazado los permisos
            Toast.makeText(this.requireContext(), "Permisos rechazados.", Toast.LENGTH_SHORT).show()
        } else {
            //pedir permiso
            ActivityCompat.requestPermissions(
                this.requireActivity(),
                arrayOf(Manifest.permission.SEND_SMS),
                777
            )
        }
    }


    //endregion


    override fun onDestroyView() {
        stoplocationUpdates()
        super.onDestroyView()
        _binding = null
    }


}