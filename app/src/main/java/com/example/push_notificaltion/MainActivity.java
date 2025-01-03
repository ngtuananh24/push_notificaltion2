package com.example.push_notificaltion;

import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.NotificationCompat;

import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.IMqttMessageListener;

public class MainActivity extends AppCompatActivity {

    private static final String CHANNEL_ID = "notification_channel";
    private static final String MQTT_BROKER_URL = "ssl://f03f9ea0745245ce996d7f35c388d455.s1.eu.hivemq.cloud:8883"; // Broker với SSL
    private static final String MQTT_TOPIC = "test/topic";  // Thay đổi với topic của bạn
    private static final String MQTT_USERNAME = "ngtuananh24";  // Tên đăng nhập
    private static final String MQTT_PASSWORD = "Anh2407@";  // Mật khẩu

    Button btnNotify;
    private MqttClient mqttClient;
    private TextView txtNotification;  // TextView để hiển thị thông báo

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        createNotificationChannel();

        // Khởi tạo TextView
        txtNotification = findViewById(R.id.txtNotification);

        //btnNotify = findViewById(R.id.btnNotify);

        // Kết nối MQTT và nhận thông báo khi có thông điệp
        connectToMqttBroker();

//        btnNotify.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                sendNotification("Tiêu đề thông báo", "Thông báo đã được kích hoạt.");
//            }
//        });
    }

    // Tạo kênh thông báo (Android 8.0 trở lên)
    private void createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            String channelName = "Thông báo với rung";
            String channelDescription = "Kênh thông báo dùng để hiển thị thông báo có rung.";
            int importance = NotificationManager.IMPORTANCE_DEFAULT;

            NotificationChannel channel = new NotificationChannel(CHANNEL_ID, channelName, importance);
            channel.setDescription(channelDescription);
            channel.enableVibration(true);
            channel.setVibrationPattern(new long[]{0, 500, 100, 300});

            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            if (notificationManager != null) {
                notificationManager.createNotificationChannel(channel);
            }
        }
    }

    // Hàm gửi thông báo
    private void sendNotification(String title, String content) {
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
                .setSmallIcon(R.drawable.ic_launcher_foreground)
                .setContentTitle(title)
                .setContentText(content)
                .setPriority(NotificationCompat.PRIORITY_DEFAULT)
                .setVibrate(new long[]{0, 500, 100, 300}); // Mẫu rung

        NotificationManager notificationManager =
                (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        if (notificationManager != null) {
            notificationManager.notify(1, builder.build());
        }
    }

    // Kết nối đến broker MQTT và đăng ký nhận thông báo
    private void connectToMqttBroker() {
        try {
            mqttClient = new MqttClient(MQTT_BROKER_URL, MqttClient.generateClientId(), null);

            MqttConnectOptions options = new MqttConnectOptions();
            options.setCleanSession(true);
            options.setUserName(MQTT_USERNAME);
            options.setPassword(MQTT_PASSWORD.toCharArray());

            mqttClient.connect(options);
            mqttClient.subscribe(MQTT_TOPIC, 1, new IMqttMessageListener() {
                @Override
                public void messageArrived(String topic, MqttMessage message) throws Exception {
                    String messageContent = new String(message.getPayload());

                    // Cập nhật nội dung thông báo vào TextView
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            txtNotification.setText(messageContent);
                        }
                    });

                    sendNotification("Thông báo từ MQTT", messageContent);
                }
            });

            System.out.println("Connected to MQTT broker and subscribed to topic: " + MQTT_TOPIC);
        } catch (MqttException e) {
            e.printStackTrace();
        }
    }
    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mqttClient != null && mqttClient.isConnected()) {
            try {
                mqttClient.disconnect();
            } catch (MqttException e) {
                e.printStackTrace();
            }
        }
    }
}
