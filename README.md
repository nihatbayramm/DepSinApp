# 🆘 Depsin – Afet Anında Yerel Ağ Tabanlı Yardım Uygulaması

<p align="center">
  <img src="https://img.shields.io/badge/AFET--MODU-AKTİF-red?style=for-the-badge">
  <img src="https://img.shields.io/badge/OFFLINE--READY-YES-black?style=for-the-badge">
  <img src="https://img.shields.io/badge/LOCAL--NETWORK-HOTSPOT-blue?style=for-the-badge">
</p>

<p align="center">
<img src="https://media.giphy.com/media/26tn33aiTi1jkl6H6/giphy.gif" style="width:50%; height:100;">
</p>

---

## 📌 Proje Özeti

**Depsin**, afet anlarında **internet veya mobil şebeke olmadan**,  
cihazların **yerel ağ (hotspot)** üzerinden birbirleriyle iletişim kurmasını sağlayan,  
**acil yardım odaklı** bir mobil uygulamadır.

Uygulama; yardım isteyenler ile yardım edebilecek kişileri  
**en hızlı ve basit şekilde** aynı ağ üzerinde buluşturmayı hedefler.

---

## 🚀 Temel Özellikler

- 📡 **Hotspot tabanlı cihazlar arası iletişim (offline çalışır)**  
- 🆘 **“Yardım Al” / “Yardım Et” butonları ile hızlı aksiyon**  
- 💬 **Sunucuya bağlı cihazlar arasında mesajlaşma**  
- 🗃️ **SQLite ile cihaz içi (yerel) veri saklama**  
- ⚡ **Düşük gecikme – hızlı veri iletimi**

---

## 🎬 İletişim & Akış Mantığı

<p align="center">
  <img src="https://media.giphy.com/media/3o7btPCcdNniyf0ArS/giphy.gif" width="300">
</p>

<p align="center">
  <i>Yerel ağ tabanlı cihazlar arası iletişim</i>
</p>


- Bir cihaz **hotspot (yerel ağ)** oluşturur  
- Diğer cihazlar bu ağa bağlanır  
- Sunucu, aynı ağdaki cihazlardan gelen istekleri dinler  
- Yardım çağrıları ve mesajlar **anlık olarak** ağ içinde yayılır  

Bu yapı sayesinde:
- İnternet bağımlılığı ortadan kalkar  
- Afet koşullarında iletişim devam eder  
- Sistem hızlı ve kararlı çalışır  

---

## 🧪 Test Ortamı

📌 Uygulama şu anda **kontrollü test senaryosu** ile çalıştırılmaktadır:

- 💻 **Sunucu**: Geliştirici bilgisayarında aktif  
- 📶 **Ağ**: Bilgisayar üzerinden oluşturulan hotspot  
- 📱 **Mobil cihazlar**: Bu ağa bağlanarak sunucu ile iletişim kurar  

✅ Bu yöntem, **internetin tamamen kesildiği** senaryoları simüle etmek için kullanılır.  
🔜 İlerleyen aşamada sunucu, **sabit IP veya bulut altyapısına** taşınacaktır.

---

## 📱 Afet Senaryosu

1. Bir kullanıcı hotspot açar  
2. Diğer kullanıcılar bu ağa bağlanır  
3. Yardım çağrıları gönderilir  
4. Aynı ağdaki cihazlara anında iletilir  
5. Yerel veritabanında kayıt altına alınır  

Amaç: **en kısa sürede doğru kişiye ulaşmak**

---

## ⚙️ Kullanılan Teknolojiler

- 📱 Android (Java / Kotlin)
- 🌐 Lokal ağ (HTTP / Socket)
- 🗃️ SQLite (yerel veritabanı)
- 🔐 Planlanan: Şifreli mesajlaşma
- 🤖 Planlanan: AI destekli yardım önceliklendirme

---

## 🛠 Kurulum

```bash
git clone https://github.com/nihatbayramm/DepSinApp.git
cd DepSinApp

```
## Developper :
Server - Backend : @nihatbayramm
Frontend : @bilgeberfin , @selmaduzme
