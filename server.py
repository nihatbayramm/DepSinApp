import sqlite3
from flask import Flask, request, jsonify
from flask_cors import CORS
import time
import os
from contextlib import contextmanager

app = Flask(__name__)
CORS(app)  # Tüm kaynaklara izin verir; daha sıkı kontrol için: CORS(app, resources={r"/*": {"origins": "http://192.168.60.*"}})

# Veritabanı başlatma
DB_PATH = "mesajlar.db"

@contextmanager
def get_db_connection():
    conn = None
    try:
        conn = sqlite3.connect(DB_PATH)
        conn.row_factory = sqlite3.Row  # Sütun isimleriyle erişim için
        yield conn
    finally:
        if conn:
            conn.close()

def init_db():
    with get_db_connection() as conn:
        c = conn.cursor()
        
        # Mesajlar tablosu
        c.execute('''CREATE TABLE IF NOT EXISTS mesajlar (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            gonderen TEXT NOT NULL,
            alici TEXT NOT NULL,
            mesaj TEXT NOT NULL,
            zaman TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
            okundu INTEGER DEFAULT 0
        )''')
        
        # Kullanıcılar tablosu
        c.execute('''CREATE TABLE IF NOT EXISTS kullanicilar (
            id INTEGER PRIMARY KEY AUTOINCREMENT,
            kullanici_adi TEXT UNIQUE NOT NULL,
            son_giris TIMESTAMP DEFAULT CURRENT_TIMESTAMP
        )''')
        
        conn.commit()

# Veritabanını başlat
init_db()

@app.route('/mesajlar', methods=['GET'])
def mesajlari_getir():
    try:
        last_id = request.args.get('last_id', type=int)
        kullanici = request.args.get('kullanici', '')
        
        if not kullanici:
            return jsonify({"hata": "Kullanıcı adı gerekli"}), 400
        
        with get_db_connection() as conn:
            c = conn.cursor()
            if last_id:
                c.execute("""
                    SELECT id, gonderen, alici, mesaj, zaman, okundu 
                    FROM mesajlar 
                    WHERE id > ? AND (gonderen = ? OR alici = ?)
                    ORDER BY id ASC
                """, (last_id, kullanici, kullanici))
            else:
                c.execute("""
                    SELECT id, gonderen, alici, mesaj, zaman, okundu 
                    FROM mesajlar 
                    WHERE gonderen = ? OR alici = ?
                    ORDER BY id ASC LIMIT 50
                """, (kullanici, kullanici))
            
            mesajlar = [{
                "id": row["id"],
                "gonderen": row["gonderen"],
                "alici": row["alici"],
                "mesaj": row["mesaj"],
                "zaman": row["zaman"],
                "okundu": bool(row["okundu"])
            } for row in c.fetchall()]
            
            return jsonify(mesajlar), 200
    except Exception as e:
        return jsonify({"hata": f"Veritabanı hatası: {str(e)}"}), 500

@app.route('/gonder', methods=['POST'])
def mesaj_gonder():
    try:
        veri = request.get_json()
        if not veri:
            return jsonify({"hata": "Geçersiz JSON verisi"}), 400

        gonderen = veri.get("gonderen", "").strip()
        alici = veri.get("alici", "").strip()
        mesaj = veri.get("mesaj", "").strip()

        if not gonderen or not alici or not mesaj:
            return jsonify({"hata": "Eksik veri: gonderen, alici ve mesaj gerekli"}), 400

        mesaj_objesi = {
            "gonderen": gonderen,
            "alici": alici,
            "mesaj": mesaj,
            "zaman": time.strftime("%Y-%m-%d %H:%M:%S"),
            "okundu": 0
        }

        with get_db_connection() as conn:
            c = conn.cursor()
            # Mesajı kaydet
            c.execute("""
                INSERT INTO mesajlar (gonderen, alici, mesaj, zaman, okundu) 
                VALUES (?, ?, ?, ?, ?)
            """, (
                mesaj_objesi["gonderen"],
                mesaj_objesi["alici"],
                mesaj_objesi["mesaj"],
                mesaj_objesi["zaman"],
                mesaj_objesi["okundu"]
            ))
            
            # Kullanıcıları güncelle
            c.execute("""
                INSERT OR REPLACE INTO kullanicilar (kullanici_adi, son_giris)
                VALUES (?, CURRENT_TIMESTAMP)
            """, (gonderen,))
            
            c.execute("""
                INSERT OR REPLACE INTO kullanicilar (kullanici_adi, son_giris)
                VALUES (?, CURRENT_TIMESTAMP)
            """, (alici,))
            
            conn.commit()

        return jsonify({"durum": "Mesaj alındı", "mesaj": mesaj_objesi}), 200
    except ValueError as ve:
        return jsonify({"hata": f"JSON parse hatası: {str(ve)}"}), 400
    except Exception as e:
        return jsonify({"hata": f"Sunucu hatası: {str(e)}"}), 500

@app.route('/okundu', methods=['POST'])
def mesaj_okundu():
    try:
        veri = request.get_json()
        if not veri:
            return jsonify({"hata": "Geçersiz JSON verisi"}), 400

        mesaj_id = veri.get("mesaj_id")
        if not mesaj_id:
            return jsonify({"hata": "Mesaj ID gerekli"}), 400

        with get_db_connection() as conn:
            c = conn.cursor()
            c.execute("UPDATE mesajlar SET okundu = 1 WHERE id = ?", (mesaj_id,))
            conn.commit()

        return jsonify({"durum": "Mesaj okundu olarak işaretlendi"}), 200
    except Exception as e:
        return jsonify({"hata": f"Sunucu hatası: {str(e)}"}), 500

@app.route('/')
def anasayfa():
    return "📡 Depsin Sunucusu Çalışıyor", 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True) 