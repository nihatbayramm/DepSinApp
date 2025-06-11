# 🆘 Depsin - Afet Anında Yardım Uygulaması

**Depsin**, afet anlarında internet veya mobil şebeke olmadan, cihazların lokal bir ağa bağlanarak iletişim kurmasını sağlayan, hayat kurtarmayı hedefleyen bir mobil yardım uygulamasıdır.

## 🚀 Özellikler

- 📡 **Hotspot üzerinden cihazlar arası bağlantı (yerel ağ iletişimi)**  
- 🆘 **"Yardım Al" ve "Yardım Et" butonları ile hızlı yardım çağrısı**  
- 💬 **Sunucuya bağlı cihazlar arasında mesajlaşma**  
- 🗃️ **SQLite veritabanı ile yerel veri saklama**

## 🧪 Test Ortamı

📌 Uygulama şu anda **test amaçlı** olarak aşağıdaki şekilde çalıştırılmaktadır:

- 💻 Sunucu: Geliştirici bilgisayarında çalışmakta  
- 📶 Ağ: Bilgisayar tarafından oluşturulan **hotspot** ağı  
- 📱 Mobil cihazlar bu ağa bağlanarak sunucu ile iletişim kurmakta

✅ Bu yöntem, internet olmayan senaryolarda uygulamanın mantığını test etmek için kullanılmaktadır.  
🔜 İleride sunucu, gerçek bir IP adresine veya bulut sistemine taşınacaktır.

## 📱 Kullanım Senaryosu

Afet durumlarında:
- Bir cihaz hotspot (taşınabilir erişim noktası) oluşturur.
- Diğer cihazlar bu ağa bağlanır.
- Yardım çağrıları ve mesajlar bu ağ üzerinden sunucuya iletilir.

## ⚙️ Teknolojiler

- 📱 Android (Java/Kotlin)
- 🌐 Lokal ağ üzerinden HTTP/Socket bağlantısı
- 🗃️ SQLite (yerel veritabanı)
- 🔐 Gelecek: Şifreli mesajlaşma
- 🤖 Planlanan: AI ile mesaj önceliklendirme

## 🛠 Kurulum

1. Projeyi klonla:
   ```bash
   git clone https://github.com/nihatbayramm/DepSinApp.git
   cd DepSinApp
