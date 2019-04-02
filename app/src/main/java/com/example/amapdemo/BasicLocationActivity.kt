package com.example.amapdemo

import android.content.Context
import android.content.Intent
import android.graphics.Color
import android.location.Location
import android.os.Bundle
import android.util.Log
import com.amap.api.maps.AMap
import com.amap.api.maps.CameraUpdate
import com.amap.api.maps.CameraUpdateFactory
import com.amap.api.maps.model.BitmapDescriptorFactory
import com.amap.api.maps.model.MyLocationStyle
import kotlinx.android.synthetic.main.activity_basic_map.*


/**
 * Crete by dumingwei on 2019/3/12
 * Desc:
 * 1. 显示定位蓝点，并移动到地图中心
 * 2. 改变地图缩放级别
 * 3. 改变地图默认显示区域
 *
 */
class BasicLocationActivity : BaseActivity(), AMap.OnMyLocationChangeListener {


    /**
     * 在定位成功以后，改变定位模式为
     *
     *MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER
     */
    var changeStyle = true

    /**
     * 获取经纬度信息
     */
    override fun onMyLocationChange(location: Location?) {
        if (changeStyle) {
            changeStyle = false
            Log.d(TAG, "onMyLocationChange: change style")
            setLocationStyle(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER)
            //设置地图缩放级别
            val mCameraUpdate: CameraUpdate = CameraUpdateFactory.zoomTo(18F)
            aMap.moveCamera(mCameraUpdate)
        }
        Log.d(TAG, "onMyLocationChange:${location?.latitude},${location?.longitude}")
    }


    private val TAG = "BasicLocationActivity"

    companion object {

        val FILL_COLOR = Color.argb(0, 0, 0, 0)
        val STROKE_COLOR = Color.argb(0, 0, 0, 0)


        fun launch(context: Context) {
            val intent = Intent(context, BasicLocationActivity::class.java)
            context.startActivity(intent)
        }
    }

    private lateinit var aMap: AMap


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_basic_map)
        mapView.onCreate(savedInstanceState)
        aMap = mapView.map
        initMap()
    }

    private fun initMap() {
        //设置缩放按钮是否可见
        aMap.uiSettings?.isZoomControlsEnabled = false
        //设置旋转手势是否可用
        aMap.uiSettings?.isRotateGesturesEnabled = false

        //监听位置改变信息
        aMap.setOnMyLocationChangeListener(this)

        aMap.isMyLocationEnabled = true// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
        setLocationStyle()

    }

    /**
     * 如果不传递参数，默认连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。
     */
    private fun setLocationStyle(type: Int = MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE) {
        val myLocationStyle = MyLocationStyle()
        myLocationStyle.myLocationType(type)
        myLocationStyle.myLocationIcon(BitmapDescriptorFactory.fromResource(R.drawable.ic_gps_point))
        myLocationStyle.showMyLocation(true)
        //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
        myLocationStyle.interval(4000L)
        myLocationStyle.strokeColor(STROKE_COLOR)
        myLocationStyle.radiusFillColor(FILL_COLOR)
        aMap.myLocationStyle = myLocationStyle//设置定位蓝点的Style
        //aMap.getUiSettings().setMyLocationButtonEnabled(true);设置默认定位按钮是否显示，非必需设置。
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
    }
}
