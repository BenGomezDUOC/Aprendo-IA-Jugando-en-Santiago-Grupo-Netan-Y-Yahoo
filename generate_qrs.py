import os
import qrcode

artifacts_dir = r"C:\Users\castr\AppData\Local\Google\AndroidStudio2026.2.1\projects\aprendo-ia-jugando-en-Santiago-Grupo-Netan-Y-Yahoo.de25fb44\.artifacts\69842ac0-bc67-4260-a726-fbdcfd314906"
os.makedirs(artifacts_dir, exist_ok=True)

qr_data = {
    "qr_plaza_armas.png": "QR_PLAZA_ARMAS",
    "qr_santa_lucia.png": "QR_SANTA_LUCIA",
    "qr_san_cristobal.png": "QR_SAN_CRISTOBAL",
    "qr_la_moneda.png": "QR_LA_MONEDA",
    "qr_parque_forestal.png": "QR_PARQUE_FORESTAL",
    "qr_bellavista.png": "QR_BELLAVISTA"
}

for filename, text in qr_data.items():
    filepath = os.path.join(artifacts_dir, filename)
    img = qrcode.make(text)
    img.save(filepath)
    print(f"Generated: {filepath}")

print("All 6 QR codes generated successfully.")
