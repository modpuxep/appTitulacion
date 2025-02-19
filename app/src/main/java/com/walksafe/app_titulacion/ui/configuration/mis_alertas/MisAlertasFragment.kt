package com.walksafe.app_titulacion.ui.configuration.mis_alertas

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.net.Uri
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.walksafe.app_titulacion.R
import com.walksafe.app_titulacion.databinding.FragmentConfigurationAlertasBinding
import com.walksafe.app_titulacion.utils.Constants
import com.walksafe.app_titulacion.utils.Status
import com.walksafe.app_titulacion.utils.showToast
import com.google.gson.Gson
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MisAlertasFragment : Fragment(), MisAlertasAdapter.MisAlertasListener {

    private val TAG = "MisAlertasFragment"

    private var _binding: FragmentConfigurationAlertasBinding? = null
    private val binding get() = _binding!!

    private lateinit var misAlertasAdapter: MisAlertasAdapter

    private lateinit var sharedPreferences: SharedPreferences

    private val listaNotificacionesViewModel: MisAlertasViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentConfigurationAlertasBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        misAlertasAdapter = MisAlertasAdapter(this)

        binding.rvConfigurationAlertas.apply {
            layoutManager = LinearLayoutManager(view?.context, LinearLayoutManager.VERTICAL, false)
            adapter = misAlertasAdapter
        }

        sharedPreferences =
            this.requireActivity().getSharedPreferences(Constants.APP_PREF, Context.MODE_PRIVATE)

        val email: String = sharedPreferences.getString(Constants.APP_EMAIL, "").toString()

        listaNotificacionesViewModel.doGetListNotification(email)


        subscribe()

    }

    private fun subscribe() {
        listaNotificacionesViewModel.getListaNotificaciones.observe(viewLifecycleOwner) {
            when (it.status) {
                Status.LOADING -> {
                    Log.d(TAG, "LOADING")
                    binding.progressBar.visibility = View.VISIBLE
                    binding.rvConfigurationAlertas.visibility = View.GONE
                }
                Status.SUCCESS -> {
                    Log.d(TAG, "SUCCESS")
                    val notificationList = it.data?.data!!
                    misAlertasAdapter.updateList(notificationList)

                    if (notificationList.count() > 0){
                        showToast(getString(R.string.msjCargaCorrecta))
                    }
                    else{
                        showToast(getString(R.string.msjCargaNoData))
                    }

                    binding.progressBar.visibility = View.GONE
                    binding.rvConfigurationAlertas.visibility = View.VISIBLE
                }
                Status.ERROR -> {
                    Log.d(TAG, "ERROR ${it.message!!}")
                    showToast(getString(R.string.msjCargaInCorrecta))
                    binding.progressBar.visibility = View.GONE
                    binding.rvConfigurationAlertas.visibility = View.GONE
                }
            }
        }
    }

    override fun onItemClickSelected(latitud: String, longitud: String) {
        val intent: Intent = Intent(Intent.ACTION_VIEW)
//        intent.setData( Uri.parse("https://www.google.com/maps/@" + latitud + "," + longitud + "z?hl=es"))
        intent.setData( Uri.parse("geo:" + latitud + ","+ longitud + "?z=16&q=" + latitud + "," + longitud + "Ubicación"))
        startActivity(intent)

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}