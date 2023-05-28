package com.gachon.mowa.ui.main.phonebook.content

import android.location.Geocoder
import android.util.Log
import android.view.View
import com.gachon.mowa.base.BaseFragment
import com.gachon.mowa.data.remote.welfarecenter.WelfareCenter
import com.gachon.mowa.data.remote.welfarecenter.WelfareCenterService
import com.gachon.mowa.data.remote.welfarecenter.WelfareCenterView
import com.gachon.mowa.databinding.FragmentWelfareCenterBinding
import com.gachon.mowa.util.ApplicationClass.Companion.showToast
import com.gachon.mowa.util.getLatitude
import com.gachon.mowa.util.getLongitude
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

    private var welfareCenters = ArrayList<WelfareCenter>()

    private lateinit var welfareCenterService: WelfareCenterService
    private lateinit var welfareCenterRVAdapter: WelfareCenterRVAdapter

    override fun initAfterBinding() {
        initRecyclerView()
    }

    override fun onResume() {
        super.onResume()

        /* 위치 정보 -> 공공 API -> kakao map API -> 뷰에 반영 */
        // 이때 위치 정보는 MainActivity에서 받아와 저장하도록 함.
        initWelfareCenterService()
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
     * 공공 API 서비스를 호출한다.
     */
    private fun initWelfareCenterService() {
        Log.d(TAG, "initWelfareCenterService/latitude: ${getLatitude()}, longitude: ${getLongitude()}")
        welfareCenterService = WelfareCenterService()

        /* 역지오코딩 (위도, 경도 -> 시군구명) */

        val geoCoder = Geocoder(requireContext(), Locale.getDefault())
        val addresses = geoCoder.getFromLocation(getLatitude(), getLongitude(), 10)
        Log.d(TAG, "initWelfareCenterService/addresses: $addresses")

        if (addresses != null) {
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
