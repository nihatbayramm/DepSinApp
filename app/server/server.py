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
        yield conn
    finally:
        if conn:
            conn.close()

def init_db():
    if not os.path.exists(DB_PATH):
        with get_db_connection() as conn:
            c = conn.cursor()
            c.execute('''CREATE TABLE IF NOT EXISTS mesajlar
                         (id INTEGER PRIMARY KEY AUTOINCREMENT,
                          gonderen TEXT NOT NULL,
                          mesaj TEXT NOT NULL,
                          zaman TEXT NOT NULL)''')
            conn.commit()

init_db()

@app.route('/connect', methods=['GET'])
def connect():
    return jsonify({"status": "connected"}), 200

@app.route('/heartbeat', methods=['GET'])
def heartbeat():
    return jsonify({"status": "alive"}), 200

@app.route('/mesajlar', methods=['GET'])
def mesajlari_getir():
    try:
        last_id = request.args.get('last_id', type=int)
        with get_db_connection() as conn:
            c = conn.cursor()
            if last_id:
                c.execute("SELECT id, gonderen, mesaj, zaman FROM mesajlar WHERE id > ? ORDER BY id ASC", (last_id,))
            else:
                c.execute("SELECT id, gonderen, mesaj, zaman FROM mesajlar ORDER BY id ASC LIMIT 50")
            mesajlar = [{"id": row[0], "gonderen": row[1], "mesaj": row[2], "zaman": row[3]} for row in c.fetchall()]
            return jsonify(mesajlar), 200
    except Exception as e:
        return jsonify({"hata": f"Veritabanı hatası: {str(e)}"}), 500

@app.route('/gonder', methods=['POST'])
def mesaj_gonder():
    try:
        veri = request.form  # JSON yerine form verisi kullanıyoruz
        if not veri:
            return jsonify({"hata": "Geçersiz veri"}), 400

        gonderen = veri.get("gonderen", "").strip()
        mesaj = veri.get("mesaj", "").strip()

        if not gonderen or not mesaj:
            return jsonify({"hata": "Eksik veri: gonderen ve mesaj gerekli"}), 400

        mesaj_objesi = {
            "gonderen": gonderen,
            "mesaj": mesaj,
            "zaman": time.strftime("%Y-%m-%d %H:%M:%S")
        }

        with get_db_connection() as conn:
            c = conn.cursor()
            c.execute("INSERT INTO mesajlar (gonderen, mesaj, zaman) VALUES (?, ?, ?)",
                      (mesaj_objesi["gonderen"], mesaj_objesi["mesaj"], mesaj_objesi["zaman"]))
            conn.commit()

        return jsonify({"durum": "Mesaj alındı", "mesaj": mesaj_objesi}), 200
    except Exception as e:
        print(f"Hata: {str(e)}")  # Hata ayıklama için
        return jsonify({"hata": f"Sunucu hatası: {str(e)}"}), 500

@app.route('/')
def anasayfa():
    return "📡 Depsin Sunucusu Çalışıyor", 200

if __name__ == '__main__':
    app.run(host='0.0.0.0', port=5000, debug=True)