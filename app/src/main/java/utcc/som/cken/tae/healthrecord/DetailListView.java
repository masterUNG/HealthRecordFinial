package utcc.som.cken.tae.healthrecord;

import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.StrictMode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.impl.client.DefaultHttpClient;
import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;

public class DetailListView extends AppCompatActivity {

    //Explicit
    private String TAG = "Health", userNameString;
    private UserTABLE objUserTABLE;
    private RecordTABLE objRecordTABLE;
    private TextView showNameTextView;
    private ListView dateListView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail_list_view);

        //Bind Widget
        bindWidget();

        objUserTABLE = new UserTABLE(this);
        objRecordTABLE = new RecordTABLE(this);

        //Synchronize mySQL
        synchronizeMySQL();

        //Show Name
        showName();

        //Create ListView
        createListView();

    }   // Main Method

    private void createListView() {

        //Get Value from SQLite
        SQLiteDatabase objSqLiteDatabase = openOrCreateDatabase("Health.db", MODE_PRIVATE, null);
        final Cursor objCursor = objSqLiteDatabase.rawQuery("SELECT * FROM recordTABLE WHERE NameUser = " + "'" + userNameString + "'", null);

        objCursor.moveToFirst();
        final String[] strDate = new String[objCursor.getCount()];
        final String[] strSleep = new String[objCursor.getCount()];
        final String[] strBreakfast = new String[objCursor.getCount()];
        final String[] strLunch = new String[objCursor.getCount()];
        final String[] strDinner = new String[objCursor.getCount()];
        final String[] strTypeExercise = new String[objCursor.getCount()];
        final String[] strTimeExercise = new String[objCursor.getCount()];
        final String[] strDrinkWater = new String[objCursor.getCount()];
        final String[] strWeight = new String[objCursor.getCount()];

        for (int i=0;i<objCursor.getCount();i++) {

            strDate[i] = objCursor.getString(objCursor.getColumnIndex("Date"));
            strSleep[i] = objCursor.getString(objCursor.getColumnIndex("Sleep"));
            strBreakfast[i] = objCursor.getString(objCursor.getColumnIndex("Breakfast"));
            strLunch[i] = objCursor.getString(objCursor.getColumnIndex("Lunch"));
            strDinner[i] = objCursor.getString(objCursor.getColumnIndex("Dinner"));
            strTypeExercise[i] = objCursor.getString(objCursor.getColumnIndex("TypeExercise"));
            strTimeExercise[i] = objCursor.getString(objCursor.getColumnIndex("TimeExercise"));
            strDrinkWater[i] = objCursor.getString(objCursor.getColumnIndex("DrinkWater"));
            strWeight[i] = objCursor.getString(objCursor.getColumnIndex("Weight"));

            objCursor.moveToNext();
        }   // for

        objCursor.close();

        MyAdapter objMyAdapter = new MyAdapter(DetailListView.this, strDate);
        dateListView.setAdapter(objMyAdapter);

        dateListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {

                Intent objIntent = new Intent(DetailListView.this, DetailActivity.class);
                objIntent.putExtra("Date", strDate[i]);
                objIntent.putExtra("Sleep", strSleep[i]);
                objIntent.putExtra("Breakfast", strBreakfast[i]);
                objIntent.putExtra("Lunch", strLunch[i]);
                objIntent.putExtra("Dinner", strDinner[i]);
                objIntent.putExtra("TypeExercise", strTypeExercise[i]);
                objIntent.putExtra("TimeExercise", strTimeExercise[i]);
                objIntent.putExtra("DrinkWater", strDrinkWater[i]);
                objIntent.putExtra("Weight", strWeight[i]);
                startActivity(objIntent);

            }   // event
        });

    }   // createListView

    private void bindWidget() {
        showNameTextView = (TextView) findViewById(R.id.txtNameDetail);
        dateListView = (ListView) findViewById(R.id.listView);
    }

    private void showName() {
        userNameString = getIntent().getStringExtra("NameUser");
        showNameTextView.setText(userNameString);
    }

    private void synchronizeMySQL() {

        SQLiteDatabase objSqLiteDatabase = openOrCreateDatabase
                ("Health.db", MODE_PRIVATE, null);
        objSqLiteDatabase.delete("userTABLE", null, null);
        objSqLiteDatabase.delete("recordTABLE", null, null);


        //Setup Policy
        StrictMode.ThreadPolicy myPolicy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(myPolicy);


        // Sync ข้อมูลจาก Table โดยค่อยๆทำทีละ Table
        int intTimes = 0;    // จำนวนครั้ง
        while (intTimes <= 1) {


            // Constant
            InputStream objInputStream = null;  // โหลดไปใช้ไป
            String strJSON = null;  // จะเปลี่ยน Input Stream ให้เป็น String
            String strUrlUser = "http://swiftcodingthai.com/tae/get_data_user_tae.php";   // URL ของไฟล์ JSON ตาราง User
            String strUrlRecord = "http://swiftcodingthai.com/tae/get_data_record_tae.php";
            HttpPost objHttpPost = null;   // ประกาศตัวแปรไว้

            // ข้อที่ 1. Create InputStream   ทำให้มันโหลดแบบ Streaming ให้ได้ก่อน
            try {   // สิ่งที่เสี่ยงต่อการ Error ใส่ในนี้

                HttpClient objHttpClient = new DefaultHttpClient();
                switch (intTimes) {
                    case 0:
                        objHttpPost = new HttpPost(strUrlUser);
                        break;
                    default:
                        objHttpPost = new HttpPost(strUrlRecord);
                        break;
                }

                HttpResponse objHttpResponse = objHttpClient.execute(objHttpPost);
                HttpEntity objHttpEntity = objHttpResponse.getEntity();
                objInputStream = objHttpEntity.getContent();


            } catch (Exception e) { // ถ้า Error จะเข้ามาในนี้

                Log.d(TAG, "InputStream ==>" + e.toString());

            }


            // ข้อที่ 2. Create strJSON     เปลี่ยนสิ่งที่เรา Streaming มาให้เป็น String
            try {

                BufferedReader objBufferedReader = new BufferedReader(new InputStreamReader(objInputStream, "UTF-8"));
                StringBuilder objStringBuilder = new StringBuilder();   // ตัวที่ทำหน้าที่รวม
                String strLine = null;  // ตัวแปรที่รับตัวที่ถูกตัดมา

                while ((strLine = objBufferedReader.readLine()) != null) {  // ถ้า strLine ว่างเปล่า ก็ออกจาก Loop

                    objStringBuilder.append(strLine);   // มีหน้าที่คอยผูก String ไปเรื่อย ๆ


                }   // While Loop
                objInputStream.close();                 // ถ้าหมด ก็ไม่ต้องโหลดต่อ
                strJSON = objStringBuilder.toString();


            } catch (Exception e) {

                Log.d(TAG, "strJSON ==> " + e.toString());

            }


            // ข้้อที่ 3. Update SQLite     เอา strJSON ที่ได้มา มาใส่ใน SQLite
            try {

                final JSONArray objJsonArray = new JSONArray(strJSON);

                for (int i = 0; i < objJsonArray.length(); i++) {

                    JSONObject object = objJsonArray.getJSONObject(i);  // เอา i มาแทนค่าตำแหน่งของ Array

                    switch (intTimes) {

                        // สำหรับ UserTABLE
                        // ได้ String 8 ตัวสำหรับใส่ใน DB แล้ว
                        case 0:
                            String strUser = object.getString("User");  // User เป็น Key ใน JSON
                            String strPassword = object.getString("Password");
                            String strName = object.getString("Name");
                            String strAge = object.getString("Age");
                            String strSex = object.getString("Sex");
                            String strWeight = object.getString("Weight");
                            String strHeight = object.getString("Height");
                            String strEmail = object.getString("Email");

                            objUserTABLE.addNewUser(strUser, strPassword, strName, strAge, strSex, strWeight, strHeight, strEmail);
                            break;
                        default:
                            String strDate = object.getString("Date");
                            String strSleep = object.getString("Sleep");
                            String strBreakfast = object.getString("Breakfast");
                            String strLunch = object.getString("Lunch");
                            String strDinner = object.getString("Dinner");
                            String strTypeExercise = object.getString("TypeExercise");
                            String strTimeExercise = object.getString("TimeExercise");
                            String strDrinkWater = object.getString("DrinkWater");
                            String strWeightRecord = object.getString("Weight");
                            String strNameUser = object.getString("NameUser");

                            objRecordTABLE.addNewRecord(strDate, strSleep, strBreakfast, strLunch, strDinner, strTypeExercise, strTimeExercise, strDrinkWater, strWeightRecord, strNameUser);
                            break;

                    }

                }   // วิ่งวนตามจำนวน แถวใน JSON

            } catch (Exception e) {

                Log.d(TAG, "Update Error ==> " + e.toString());

            }

            intTimes += 1;  // บวกทีละ 1

        }   // while


    }   // synchronize

}   // Main Class
