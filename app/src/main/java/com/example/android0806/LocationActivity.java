package com.example.android0806;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.pedro.library.AutoPermissions;
import com.pedro.library.AutoPermissionsListener;

public class LocationActivity extends AppCompatActivity implements AutoPermissionsListener {

    //디자인 한 뷰를 저장하기 위한 변수
    private TextView lblLocation;
    private Button btnLocation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        //뷰 찾아오기
        lblLocation = findViewById(R.id.lblLocation);
        btnLocation = findViewById(R.id.btnLocation);

        btnLocation.setOnClickListener(
                new Button.OnClickListener(){
                    public void onClick(View view){
                        startLocationService();
                    }
                });


        AutoPermissions.Companion.loadAllPermissions(this, 101);

    }

    //Activity의 메소드로 권한 요청을 설정했을 때 호출되는 메소드
    @Override
    //requestCode는 권한 요청 할 때 구분하기 위해서 부여한 번호
    //permissions는 요청한 권한의 배열
    //grantResults는 요청한 권한의 허용 여부 배열
    public void onRequestPermissionsResult(
            int requestCode,
            String permissions[],
            int [] grantResults){
        super.onRequestPermissionsResult(
                requestCode, permissions, grantResults);
        //AutoPermissions의 메소드를 호출하도록 설정
        AutoPermissions.Companion.parsePermissions(
                this, requestCode,
                permissions, this);
    }

    //권한 사용을 거부했을 때 호출되는 메소드
    @Override
    public void onDenied(int requestCode, String[] permissions){
        Toast.makeText(this, "권한 사용을 거부함",
                Toast.LENGTH_LONG).show();
    }

    //권한 사용을 허용했을 때 호출되는 메소드
    @Override
    public void onGranted(int requestCode, String[] permissions){
        Toast.makeText(this, "권한 사용을 허용함",
                Toast.LENGTH_LONG).show();
    }

    //위치정보가 갱신될 때 호출될 리스너 객체
    class GPSListener implements LocationListener {

        //위치정보가 변경되면 호출되는 메소드
        @Override
        public void onLocationChanged(
                @NonNull Location location) {
            //위도와 경도 가져오기
            double latitude = location.getLatitude();
            double longitude = location.getLongitude();

            //출력
            String msg =
                    String.format("위도:%.6f 경도:%.6f",
                            latitude, longitude);
            lblLocation.setText(msg);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(@NonNull String provider) {

        }

        @Override
        public void onProviderDisabled(@NonNull String provider) {

        }
    }

    //버튼을 눌렀을 때 호출될 메소드
    private void startLocationService(){
        //위치정보 사용 객체를 생성
        LocationManager manager =
                (LocationManager)getSystemService(
                        Context.LOCATION_SERVICE);
        try{
            //위치정보 제공자를 설정 : 동적 권한 설정이 되어야 함
            //이 코드를 부르는 곳에서 설정
            Location location =
                    manager.getLastKnownLocation(
                            LocationManager.GPS_PROVIDER);
            if(location != null){
                //위도와 경도 가져오기
                double latitude = location.getLatitude();
                double longitude = location.getLongitude();

                //출력
                String msg =
                        String.format("위도:%.6f 경도:%.6f",
                                latitude, longitude);
                lblLocation.setText(msg);
            }

            //리스너를 생성
            GPSListener gpsListener = new GPSListener();
            //위치정보가 갱신될 때 gpsListener의 메소드를 호출하도록 설정
            //첫번째 매개변수는 위치 정보 갱신을 위한 정보 제공자를 설정
            //두번째 매개변수는 위치 정보를 측정할 시간 단위
            //세번째 매개변수는 위치 정보를 측정할 거리 단위
            //네번째 매개변수가 호출될 리스너
            manager.requestLocationUpdates(
                    LocationManager.GPS_PROVIDER,
                    10000, 10, gpsListener);

        }catch(Exception e){
            Log.e("위치 정보 사용 실패", e.getMessage());
        }
    }
}