
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
 * 手机端（服务端）与PC端（客户端）通过USB数据线来通信
 * 主要是 通过adb forword tcp:(pc 端口) tcp:(手机端口) 命令来将手机上的端口映射到PC上的端口
 * 例如 adb forword tcp:8080 tcp:8090
 * 把手机端（服务端）的8090端口绑定到PC本机的8080端口
 * 
 * 手机端（服务端）还是继续监听8090端口来收发数据
 * PC端通过8080收发数据
 * 
 * 此程序需要手机端serversocket、PC端程序来用socket来通信，PC端例子是AdbPC
 */
public class MainActivity extends Activity {
	// 定义侦听端口号
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
		
		// 开一个新的线程来侦听客户端连接及发来的信息和打开相应网站
		new Thread() {
			public void run() {
				startServer();//传输消息
			}
		}.start();
		new Thread() {
			public void run() {
				startTwoServer();//传输文件
			}
		}.start();
		
	}
	private void startTwoServer(){
		try {
			 serverTwoSocket = new ServerSocket(8000);
			 while(true){
                 Socket client = serverTwoSocket.accept();
                  dis = new DataInputStream(client.getInputStream());
                 //文件名和长度
                 String fileName = dis.readUTF();
                 long fileLength = dis.readLong();
                  fos = new FileOutputStream(new File("/mnt/sdcard/" + fileName));
                   
                 byte[] sendBytes =new byte[1024];
                 float transLen =0;
                 System.out.println("----开始接收文件<" + fileName +">,文件大小为<" + fileLength +">----");
                 
                 while(flag){
                     int read =0;
                     read = dis.read(sendBytes);
                     if(read == -1){
                         break;
                     }
                     transLen += read;
                   
                     DecimalFormat decimalFormat=new DecimalFormat("0.00");//构造方法的字符格式这里如果小数不足2位,会以0补足.
                     String p=decimalFormat.format(100*transLen/fileLength);//format 返回的是字符串
                   
                     Message msg = mHandler.obtainMessage();
                     msg.obj="文件已经下载："+p+"%";
                     mHandler.sendMessage(msg);
                     fos.write(sendBytes,0, read);
                     fos.flush();
                 }
                 Message msg = mHandler.obtainMessage();
                 msg.obj="文件已经下载："+"成功";
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
			// 循环侦听客户端连接请求
			while (true) {
				final Socket client = serverSocket.accept();
				new Thread(new Runnable() {
					@Override
					public void run() {
						try {
							Log.e("hehheh", "有人来访:");
							runOnUiThread(new Runnable() {
								@Override
								public void run() {
									Toast.makeText(MainActivity.this, "有人连接！！", 0).show();
								}
							});
							// 等待客户端发送打开网站的消息
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
