package com.gachon.mowa.ui.main.phonebook.content

import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.location.LocationListener
import android.location.LocationManager
import android.util.Log
import android.view.View
import android.widget.Toast
import com.gachon.mowa.base.BaseFragment
import com.gachon.mowa.data.local.AppDatabase
import com.gachon.mowa.data.remote.welfarecenter.WelfareCenter
import com.gachon.mowa.data.remote.welfarecenter.WelfareCenterService
import com.gachon.mowa.data.remote.welfarecenter.WelfareCenterView
import com.gachon.mowa.databinding.FragmentWelfareCenterBinding
import com.gachon.mowa.util.ApplicationClass.Companion.showToast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import net.daum.mf.map.api.MapPOIItem
import net.daum.mf.map.api.MapPoint
import net.daum.mf.map.api.MapView
import java.util.*
import kotlin.collections.ArrayList

class WelfareCenterFragment :
    BaseFragment<FragmentWelfareCenterBinding>(FragmentWelfareCenterBinding::inflate),
    WelfareCenterView {
    companion object {
        const val TAG = "FRAG/WELFARE-CENTER"
    }

    private var latitude: Double = 0.0
    private var longitude: Double = 0.0
    private var welfareCenters = ArrayList<WelfareCenter>()

    private lateinit var locationManager: LocationManager
    private lateinit var gpsListener: GPSListener
    private lateinit var welfareCenterService: WelfareCenterService
    private lateinit var welfareCenterRVAdapter: WelfareCenterRVAdapter

    override fun initAfterBinding() {
        initRecyclerView()
    }

    override fun onResume() {
        super.onResume()

        /* 위치 정보 -> 공공 API -> kakao map API -> 뷰에 반영 */

        startLocationService()
    }

    override fun onPause() {
        super.onPause()
        binding.welfareCenterMapContainerRl.removeAllViews()
    }

    /**
     * RecyclerView 초기화를 해줍니다.
     */
    private fun initRecyclerView() {
        welfareCenterRVAdapter =
            WelfareCenterRVAdapter(
                requireContext(),
                object : WelfareCenterRVAdapter.MyItemClickListener {

                    override fun onItemClick(view: View, itemPosition: Int) {
                        // 아이템(공공기관 전화번호)을 클릭했을 때
                        Log.d(TAG, "initRecyclerView/onItemClick")
                    }
                })

        binding.welfareCenterPublicRv.adapter = welfareCenterRVAdapter
    }

    /**
     * 사용자의 위치 정보를 받아오는 리스너
     */
    private fun startLocationService() {
        locationManager =
            requireActivity().getSystemService(Context.LOCATION_SERVICE) as LocationManager

        try {
            gpsListener = GPSListener()

            // Main thread에서 만들어져야 한다.
            locationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER,
                0,
                0f,
                gpsListener
            )
        } catch (e: SecurityException) {
            e.printStackTrace()
        }
    }

    /**
     * 사용자 위치 정보(위도, 경도)를 받아온다.
     * 위치를 받아오는 것이 성공하면 날씨 API를 받아온다.
     */
    inner class GPSListener : LocationListener {
        override fun onLocationChanged(p0: Location) {
            latitude = p0.latitude
            longitude = p0.longitude

            Log.d(TAG, "GPSListener/onLocationChanged/latitude: $latitude, longitude: $longitude")

            locationManager.removeUpdates(gpsListener)

            initWelfareCenterService()
        }
    }

    /**
     * 공공 API 서비스를 호출한다.
     */
    private fun initWelfareCenterService() {
        Log.d(TAG, "initWelfareCenterService/latitude: $latitude, longitude: $longitude")
        welfareCenterService = WelfareCenterService()

        /* 역지오코딩 (위도, 경도 -> 시군구명) */

        val geoCoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = geoCoder.getFromLocation(latitude, longitude, 10)
        Log.d(TAG, "initWelfareCenterService/addresses: $addresses")

        if (addresses.size != 0) {
            val address = addresses[0]

            Log.d(
                TAG,
                "initWelfareCenterService/${address.adminArea} ${address.locality} ${address.thoroughfare}"
            )

            // API 호출
            welfareCenterService.getWelfareCenters(this@WelfareCenterFragment, address.locality)
        } else {
            Log.d(TAG, "initWelfareCenterService/API 호출 실패")
        }
    }

    /**
     * 공공 API 호출이 성공했을 때
     */
    override fun onGetWelfareCenterSuccess(welfareCenters: List<WelfareCenter>) {
        Log.d(TAG, "onGetWelfareCenterSuccess/welfareCenters: $welfareCenters")
        welfareCenterRVAdapter.addData(welfareCenters)

        this.welfareCenters.clear()
        this.welfareCenters.addAll(welfareCenters)

        initKakaoMapApi()
    }

    /**
     * 공공 API 호출이 실패했을 때
     */
    override fun onGetWelfareCenterFailure(code: Int, message: String) {
        Log.d(TAG, "onGetWelfareCenterFailure/code: $code, message: $message")
        showToast(requireContext(), "복지 시설을 불러오는 데 실패했습니다.")
    }

    /**
     * Kakao Map을 보여줍니다.
     */
    private fun initKakaoMapApi() {
        val mapView = MapView(context)
        binding.welfareCenterMapContainerRl.addView(mapView)

        mapView.apply {
            setZoomLevel(4, true)
        }

        // 지도가 맵의 현재 위치로 이동한다.
        mapView.currentLocationTrackingMode = MapView.CurrentLocationTrackingMode.TrackingModeOnWithoutHeading

        val markers = ArrayList<MapPOIItem>()

        for (welfareCenter in welfareCenters) {
            // 마커를 추가한다.
            val marker = MapPOIItem()

            marker.mapPoint = MapPoint.mapPointWithGeoCoord(
                welfareCenter.latitude,
                welfareCenter.longitude
            )
            marker.itemName = welfareCenter.name
            markers.add(marker)
        }

        // mapView.addPOIItems(markerArr.toArray(new MapPOIItem[markerArr.size()]));
        mapView.addPOIItems(markers.toArray(arrayOfNulls(markers.size)))
    }
}
