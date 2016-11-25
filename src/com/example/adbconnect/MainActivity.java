
package com.example.adbconnect;

import java.io.BufferedReader;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.DecimalFormat;
import java.text.NumberFormat;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

/**
 * �ֻ��ˣ�����ˣ���PC�ˣ��ͻ��ˣ�ͨ��USB��������ͨ��
 * ��Ҫ�� ͨ��adb forword tcp:(pc �˿�) tcp:(�ֻ��˿�) ���������ֻ��ϵĶ˿�ӳ�䵽PC�ϵĶ˿�
 * ���� adb forword tcp:8080 tcp:8090
 * ���ֻ��ˣ�����ˣ���8090�˿ڰ󶨵�PC������8080�˿�
 * 
 * �ֻ��ˣ�����ˣ����Ǽ�������8090�˿����շ�����
 * PC��ͨ��8080�շ�����
 * 
 * �˳�����Ҫ�ֻ���serversocket��PC�˳�������socket��ͨ�ţ�PC��������AdbPC
 */
public class MainActivity extends Activity {
	// ���������˿ں�
	private final int SERVER_PORT = 8090;
	private TextView textView;
	private String content = "";
	DataInputStream dis;
	 FileOutputStream fos;
	 ServerSocket serverTwoSocket;
	 boolean flag =true;
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		textView = (TextView)findViewById(R.id.tv);
		
		// ��һ���µ��߳��������ͻ������Ӽ���������Ϣ�ʹ���Ӧ��վ
		new Thread() {
			public void run() {
				startServer();//������Ϣ
			}
		}.start();
		new Thread() {
			public void run() {
				startTwoServer();//�����ļ�
			}
		}.start();
		
	}
	private void startTwoServer(){
		try {
			 serverTwoSocket = new ServerSocket(8000);
			 while(true){
                 Socket client = serverTwoSocket.accept();
                  dis = new DataInputStream(client.getInputStream());
                 //�ļ����ͳ���
                 String fileName = dis.readUTF();
                 long fileLength = dis.readLong();
                  fos = new FileOutputStream(new File("/mnt/sdcard/" + fileName));
                   
                 byte[] sendBytes =new byte[1024];
                 float transLen =0;
                 System.out.println("----��ʼ�����ļ�<" + fileName +">,�ļ���СΪ<" + fileLength +">----");
                 
                 while(flag){
                     int read =0;
                     read = dis.read(sendBytes);
                     if(read == -1){
                         break;
                     }
                     transLen += read;
                   
                     DecimalFormat decimalFormat=new DecimalFormat("0.00");//���췽�����ַ���ʽ�������С������2λ,����0����.
                     String p=decimalFormat.format(100*transLen/fileLength);//format ���ص����ַ���
                   
                     Message msg = mHandler.obtainMessage();
                     msg.obj="�ļ��Ѿ����أ�"+p+"%";
                     mHandler.sendMessage(msg);
                     fos.write(sendBytes,0, read);
                     fos.flush();
                 }
                 Message msg = mHandler.obtainMessage();
                 msg.obj="�ļ��Ѿ����أ�"+"�ɹ�";
                 mHandler.sendMessage(msg);
                 client.close();
                 dis.close();
                 fos.close();
             }
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		finally {
            if(dis !=null)
				try {
					dis.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            if(fos !=null)
				try {
					fos.close();
				} catch (IOException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
            try {
				serverTwoSocket.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
        }
	}
	private void startServer() {
		try {
			//ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
			ServerSocket serverSocket = new ServerSocket(SERVER_PORT);
			// ѭ�������ͻ�����������
			while (true) {
				final Socket client = serverSocket.accept();
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Log.e("hehheh", "��������:");
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(MainActivity.this, "�������ӣ���", 0).show();
								}
							});
							// �ȴ��ͻ��˷��ʹ���վ����Ϣ
							BufferedReader in = new BufferedReader(new InputStreamReader(client.getInputStream(),"GB2312"));
							String str;
							
							while((str = in.readLine()) != null){
								System.out.println(str);
								content = str+"\n";
								runOnUiThread(new Runnable() {
									@Override
									public void run() {
										Toast.makeText(MainActivity.this, content, 1).show();
									}
								});
							}
							//mHandler.sendMessage(mHandler.obtainMessage());
							//	openUrl(str);
						} catch (Exception e) {
							e.printStackTrace();
						} finally {
							try {
								client.close();
							} catch (IOException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
						}
						try {
							Thread.sleep(500);
						} catch (InterruptedException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
				}).start();
				
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
	}

	private void openUrl(String url) {
		Uri uri = Uri.parse(url);
		Intent intent = new Intent(Intent.ACTION_VIEW, uri);
		startActivity(intent);
	}
	
	public Handler mHandler = new Handler() {
		public void handleMessage(Message msg) {
			super.handleMessage(msg);
			textView.setText((CharSequence) msg.obj);
		}
	};
	protected void onDestroy() {
		flag = false;
	};
}
