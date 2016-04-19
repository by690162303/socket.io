package com.example.socketio_client;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.TextView;

import com.example.socketio_client.Entity.Custom;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.FileInputStream;
import java.io.FileOutputStream;

public class MainActivity extends Activity implements OnClickListener {
	private TextView tv;
	private EditText td;
	private String mAdress;
	private static final String ADDRES_NAME = "Adress.json";
	private Custom cs;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		tv = (TextView) this.findViewById(R.id.adress_textview);
		tv.setOnClickListener(this);
		cs = jsonToPet(readerToFile());
		if(cs!=null){
			mAdress = jsonToPet(readerToFile()).getUseradress();
		}

		tv.setText(mAdress);
		//启动程序发送广播启动服务
        Intent intent = new Intent("SOCKET.IO");
        sendBroadcast(intent);
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		AlertDialog.Builder builde = new AlertDialog.Builder(this);
		td = new EditText(this);
		td.setText(mAdress);
		builde.setTitle(R.string.input_adress).setView(td).setPositiveButton(R.string.queding, new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				// TODO Auto-generated method stub
				tv.setText(td.getText().toString());
//				CrashReport.testJavaCrash();
//				int i= 3/0;
				writeToFile();
				dialog.cancel();
			}
		}).setNegativeButton(R.string.quexiao, null).setCancelable(false).show();
	}

	/**
	 * 读取已经存储的地址信息
	 * @return
	 */
	public String readerToFile() {
		String str = null;
		try {
			FileInputStream fin = openFileInput("Adress.json");
			int length = fin.available();
			byte[] buffer = new byte[length];
			fin.read(buffer);
			str = new String(buffer);
			fin.close();
		} catch (Exception e) {
			return null;
		}
		return str;

	}

	/**
	 * 写文件操作
	 *
	 */
	public void writeToFile() {
		try {
			FileOutputStream fout = this.openFileOutput(ADDRES_NAME, MODE_PRIVATE);// 设置权限
			byte[] bytes = createJson().getBytes();
			fout.write(bytes);
			fout.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
	}

	public String createJson() {
		String str = null;
		JSONObject object = new JSONObject();// 创建一个总的对象，这个对象对整个json串
		try {
			JSONArray jsonarray = new JSONArray();// json数组，里面包含的内容为pet的所有对象
			JSONObject jsonObj = new JSONObject();
			jsonObj.put("useradress", td.getText().toString());// 向pet对象里面添加值
			// 把每个数据当作一对象添加到数组里
			jsonarray.put(jsonObj);// 向json数组里面添加pet对象
			object.put("Custom", jsonarray);// 向总对象里面添加包含pet的数组
			str = object.toString();// 生成返回字符串
		} catch (JSONException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return str;
	}
	public Custom jsonToPet(String json){
		if(json==null){
			return null;
		}
		Custom pet=new Custom();//准备返回的pet对象  
		try {
			JSONObject jsonObject=new JSONObject(json);//我们需要把json串看成一个大的对象
			JSONArray jsonArray=jsonObject.getJSONArray("Custom");//这里获取的是装载有所有pet对象的数组
			JSONObject jsonpet = jsonArray.getJSONObject(0);//获取这个数组中第一个pet对象
			String adress=jsonpet.getString("useradress");
			pet.setUseradress(adress);
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return pet;
	}
}
