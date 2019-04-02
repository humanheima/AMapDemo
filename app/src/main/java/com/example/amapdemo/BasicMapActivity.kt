package com.example.amapdemo

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.util.Log
import com.amap.api.location.AMapLocation
import com.amap.api.location.AMapLocationClient
import com.amap.api.location.AMapLocationClientOption
import com.amap.api.location.AMapLocationListener
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.LocationSource
import com.amap.api.maps.Projection
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.LatLng
import com.amap.api.maps.model.Marker
import com.amap.api.maps.model.MarkerOptions
import kotlinx.android.synthetic.main.activity_basic_map.*

/**
 * Crete by dumingwei on 2019/3/12
 * Desc: 显示定位蓝点，并移动到地图中心
 *
 */
class BasicMapActivity : BaseActivity(), LocationSource, AMapLocationListener {


    private val TAG = "BasicMapActivity"

    companion object {

        fun launch(context: Context) {
            val intent = Intent(context, BasicMapActivity::class.java)
            context.startActivity(intent)
        }
    }


    private lateinit var aMap: AMap
    private val latlng = LatLng(39.761, 116.434)

    private var useMoveToLocationWithMapMode = true
    //自定义定位小蓝点的Marker
    private var locationMarker: Marker? = null
    //定位相关
    private var mLocationClient: AMapLocationClient? = null
    private var mLocationOption: AMapLocationClientOption? = null
    //位置改变回调函数
    private var mListener: LocationSource.OnLocationChangedListener? = null

    //坐标和经纬度转换工具
    private var projection: Projection? = null

    private var myCancelCallback = MyCancelCallback()


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basic_map)
        mapView.onCreate(savedInstanceState)
        aMap = mapView.map
        aMap.isMyLocationEnabled = true
        initLocation()


    }

    private fun initLocation() {
        //初始化定位
        aMap.setLocationSource(this)
        aMap.isMyLocationEnabled = true
    }

    override fun deactivate() {
        mListener = null
        if (mLocationClient != null) {
            mLocationClient?.stopLocation()
            mLocationClient?.onDestroy()
        }
        mLocationClient = null
    }

    override fun activate(listener: LocationSource.OnLocationChangedListener?) {
        mListener = listener
        if (mLocationClient == null) {
            mLocationClient = AMapLocationClient(this)
            mLocationClient?.setLocationListener(this)
            mLocationOption = AMapLocationClientOption()
            mLocationOption?.locationMode = AMapLocationClientOption.AMapLocationMode.Hight_Accuracy
            mLocationOption?.interval = 2000L

            mLocationClient?.setLocationOption(mLocationOption)
            mLocationClient?.startLocation()
        }
    }

    override fun onLocationChanged(amapLocation: AMapLocation?) {
        if (mListener != null && amapLocation != null) {
            if (amapLocation.errorCode == 0) {
                val latLng = LatLng(amapLocation.latitude, amapLocation.longitude)
                //展示自定义定位小蓝点
                if (locationMarker == null) {
                    //首次定位,添加mark
                    locationMarker = addMark(latLng)
                    aMap.moveCamera(CameraUpdateFactory.newLatLngZoom(latLng, 18F))
                } else {
                    if (useMoveToLocationWithMapMode) {
                        //二次以后定位，使用sdk中没有的模式，让地图和小蓝点一起移动到中心点（类似导航锁车时的效果）
                        startMoveLocationAndMap(latLng)
                    } else {
                        startChangeLocation(latLng)
                    }

                }


            } else {
                val errText = "定位失败," + amapLocation.errorCode + ": " + amapLocation.errorInfo
                Log.e("AmapErr", errText)
            }
        }
    }


    /**
     * 同时修改自定义定位小蓝点和地图的位置
     * @param latLng
     */
    private fun startMoveLocationAndMap(latLng: LatLng) {
        //将小蓝点提取到屏幕上
        if (projection == null) {
            projection = aMap.projection
        }
        if (locationMarker != null && projection != null) {
            val markerLocation = locationMarker?.position
            val screenPosition = aMap.projection.toScreenLocation(markerLocation)
            locationMarker?.setPositionByPixels(screenPosition.x, screenPosition.y)

        }

        //移动地图，移动结束后，将小蓝点放到放到地图上
        myCancelCallback.targetLatlng = latLng
        //动画移动的时间，最好不要比定位间隔长，如果定位间隔2000ms 动画移动时间最好小于2000ms，可以使用1000ms
        //如果超过了，需要在myCancelCallback中进行处理被打断的情况
        aMap.animateCamera(CameraUpdateFactory.changeLatLng(latLng), 1000, myCancelCallback)
    }

    /**
     * 修改自定义定位小蓝点的位置
     * @param latLng
     */
    private fun startChangeLocation(latLng: LatLng) {

        if (locationMarker != null) {
            val curLatlng = locationMarker?.position
            if (curLatlng == null || curLatlng != latLng) {
                locationMarker?.position = latLng
            }
        }
    }

    private fun addMark(latlng: LatLng): Marker {
        val markerOptions = MarkerOptions().icon(BitmapDescriptorFactory
                .defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                .position(latlng)
                .draggable(true)
        return aMap.addMarker(markerOptions)

    }

    override fun onResume() {
        super.onResume()
        mapView.onResume()
    }

    override fun onPause() {
        super.onPause()
        mapView.onPause()
    }

    override fun onSaveInstanceState(outState: Bundle?) {
        super.onSaveInstanceState(outState)
        mapView.onSaveInstanceState(outState)
    }


    override fun onDestroy() {
        super.onDestroy()
        mapView.onDestroy()
        mLocationClient?.onDestroy()
    }

    /**
     * 监控地图动画移动情况，如果结束或者被打断，都需要执行相应的操作
     */
    internal inner class MyCancelCallback : AMap.CancelableCallback {

        var targetLatlng: LatLng? = null


        override
        fun onFinish() {
            if (locationMarker != null && targetLatlng != null) {
                locationMarker?.position = targetLatlng
            }
        }

        override
        fun onCancel() {
            if (locationMarker != null && targetLatlng != null) {
                locationMarker?.position = targetLatlng
            }
        }
    }
}
