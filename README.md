## XÂY DỰNG CHƯƠNG TRÌNH NHẬN THÔNG BÁO CHO ĐIỆN THOẠI
#### Author: Nguyễn Tuấn Anh
#### Class: K57KMT
#### ID student: K215480106003
--------------
![image](https://github.com/user-attachments/assets/68ce3b42-17a9-4ff0-9a2b-0147ba539ab6)

## Demo

https://github.com/user-attachments/assets/7ea54594-9795-45e3-ac94-6f333f16539a

### 1. Xây dựng một server gửi thống báo về điện thoại: [Link](https://github.com/ngtuananh24/API)

- Chuỗi json: {"title": "Tiêu đề", "content": "Nội dung thông báo"}
- Lưu lịch sử những thông báo đã gửi vào MySQL

![Screenshot 2025-02-28 194416](https://github.com/user-attachments/assets/5f02cf7e-9076-438f-b2eb-d8187ab44c02)

 ![image](https://github.com/user-attachments/assets/75ac11d5-f6ce-4180-a039-d01e32841082)

![image](https://github.com/user-attachments/assets/f1799570-312f-429d-833e-30ab10004796)

### 2. Máy nhận thông báo
Thiết bị đã nhận được thông báo online từ sever thông báo rung và hiển thị trực tiếp nội dung trên app

![image](https://github.com/user-attachments/assets/4c503b13-0434-4bda-87fd-8b9f1f205e90)

### 3. Máy gửi thông báo (More: Kết nối FireBase)
Mô tả: Máy sẽ đóng vai trò như máy giao viên giúp chọn được người nhận, tiêu đề, nội dung và lưu lịch sử các thông báo đã gửi
[Project máy gửi thông báo](https://github.com/ngtuananh24/gui_thong_bao)

![image](https://github.com/user-attachments/assets/b273fb10-9b90-40f7-91e9-d0dfa631a7be)

![image](https://github.com/user-attachments/assets/baa2224f-be0d-4068-8776-0268030269c8)


# Tạo Thông Báo trên điện thoại Android

Chương trình này gồm hai phần: **Máy chủ (Server)** và **Ứng dụng Android**. Mục tiêu của hệ thống là giám sát và gửi thông báo đến ứng dụng Android qua MQTT khi có dữ liệu mới được gửi từ máy chủ. Dưới đây là cách xây dựng hệ thống này, từ cấu hình máy chủ đến xử lý thông báo trên ứng dụng Android.

## 1. **Cấu hình Máy chủ (Server) với Flask và MySQL**

### 1.1. **Cài đặt Thư viện**

Máy chủ được xây dựng bằng Flask (web framework Python) và sử dụng MySQL để lưu trữ dữ liệu. Đồng thời, MQTT được sử dụng để giao tiếp với các thiết bị.

Các thư viện cần cài đặt:

- `Flask`: Để tạo API web.
- `mysql-connector-python`: Để kết nối với MySQL.
- `paho-mqtt`: Để giao tiếp với MQTT broker.

```bash
pip install Flask mysql-connector-python paho-mqtt
```

### 1.2. **Cấu hình MQTT**

Máy chủ sử dụng một broker MQTT để gửi các thông báo dưới dạng JSON. Thông tin cấu hình broker bao gồm:

- **MQTT_BROKER**: Địa chỉ broker MQTT.
- **MQTT_PORT**: Cổng kết nối (8883 cho kết nối an toàn).
- **MQTT_USERNAME và MQTT_PASSWORD**: Thông tin đăng nhập vào broker.

```python
MQTT_BROKER = "f03f9ea0745245ce996d7f35c388d455.s1.eu.hivemq.cloud"
MQTT_PORT = 8883
MQTT_USERNAME = "ngtuananh24"
MQTT_PASSWORD = "Anh2407@"
MQTT_TOPIC = "tb"
```

### 1.3. **Gửi Dữ liệu qua MQTT**

Máy chủ gửi thông tin dưới dạng JSON và thực hiện kết nối MQTT để gửi dữ liệu.

```python
def send_mqtt(title, content):
    client = mqtt.Client()
    client.username_pw_set(MQTT_USERNAME, MQTT_PASSWORD)
    client.tls_set(tls_version=mqtt.ssl.PROTOCOL_TLS)
    client.connect(MQTT_BROKER, MQTT_PORT, 60)

    # Tạo chuỗi JSON
    message = json.dumps({"title": title, "content": content}, ensure_ascii=False)

    # Gửi MQTT
    client.publish(MQTT_TOPIC, message)
    print("Gửi thành công")
    client.disconnect()
```
### 1.4. **Lưu Dữ liệu vào MySQL**

Sau khi gửi thông báo qua MQTT, dữ liệu sẽ được lưu vào MySQL để lưu trữ lịch sử.

```python
def save_to_mysql(title, content):
    conn = None
    cursor = None
    try:
        conn = mysql.connector.connect(**DB_CONFIG)
        cursor = conn.cursor()
        sql = "INSERT INTO lichsu (title, content) VALUES (%s, %s)"
        cursor.execute(sql, (title, content))
        conn.commit()
        print("Dữ liệu đã lưu vào MySQL")
    except Exception as e:
        print("Lỗi lưu MySQL:", e)
    finally:
        if cursor is not None:
            cursor.close()
        if conn is not None:
            conn.close()
```

### 1.5. **API Flask**

Chương trình cung cấp các route API cho:
- Gửi thông báo qua MQTT: `/send`
- Lấy lịch sử thông báo từ MySQL: `/history`

```python
@app.route("/send", methods=["POST"])
def send():
    title = request.args.get("title")
    content = request.args.get("content")
    if title and content:
        send_mqtt(title, content)
        return jsonify({"message": "Thông báo đã gửi thành công!"})
    return jsonify({"message": "Thiếu dữ liệu!"})
```

## 2. **Ứng Dụng Android**

Ứng dụng Android kết nối tới MQTT broker để nhận thông báo và hiển thị nó qua giao diện người dùng. 

### 2.1. **Cài đặt Thư viện**

Ứng dụng Android sử dụng `paho-mqtt` để kết nối tới MQTT broker và nhận thông báo.

### 2.2. **Kết Nối tới MQTT Broker**

Ứng dụng Android tạo một kết nối MQTT tới broker với các thông tin cấu hình như sau:

```java
private static final String BROKER_URL = "tcp://192.168.0.104:1883";
private static final String CLIENT_ID = "AndroidClient-" + System.currentTimeMillis();
private static final String TOPIC = "tb";
```

### 2.3. **Nhận Thông Báo và Xử Lý JSON**

Khi ứng dụng Android nhận được thông báo từ broker, dữ liệu JSON sẽ được phân tích và hiển thị trong ứng dụng.

```java
@Override
public void messageArrived(String topic, MqttMessage message) {
    String payload = new String(message.getPayload());

    // Phân tích JSON và hiển thị thông báo
    processJsonMessage(payload);
}
```

### 2.4. **Hiển Thị Thông Báo**

Khi nhận được thông báo, ứng dụng Android sẽ hiển thị một thông báo (notification) và cập nhật UI với dữ liệu từ JSON.

```java
private void showNotification(String title, String content) {
    NotificationCompat.Builder builder = new NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setVibrate(new long[]{0, 500, 200, 500}) // Mẫu rung
            .setAutoCancel(true);

    NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
    notificationManager.notify(NOTIFICATION_ID, builder.build());
}
```

## 3. **Nguyên lý Hoạt động**

### 3.1. **Máy chủ (Server)**

- Máy chủ nhận thông tin từ người dùng (tiêu đề và nội dung).
- Dữ liệu sẽ được gửi qua MQTT tới broker và lưu vào MySQL.
- Máy chủ cung cấp API để gửi dữ liệu và xem lịch sử thông báo.

### 3.2. **Ứng dụng Android**

- Ứng dụng Android kết nối tới MQTT broker để nhận thông báo.
- Khi nhận được tin nhắn, ứng dụng sẽ phân tích dữ liệu JSON và hiển thị thông báo.
- Ứng dụng sẽ rung và hiển thị thông báo trên giao diện người dùng.

## 4. **Kết luận**

Hệ thống này giúp theo dõi và gửi thông báo giữa máy chủ và ứng dụng Android thông qua MQTT. Dữ liệu được lưu trữ trong MySQL để dễ dàng tra cứu lịch sử. Các tính năng như rung và thông báo giúp người dùng dễ dàng nhận diện thông tin quan trọng.
- Tích Hợp Nhiều Loại Cảm Biến và Điều Khiển
- Ứng Dụng Trong Quản Lý Bãi Đỗ Xe
- Ứng Dụng Trong Các Hệ Thống Cảnh Báo
- ....


