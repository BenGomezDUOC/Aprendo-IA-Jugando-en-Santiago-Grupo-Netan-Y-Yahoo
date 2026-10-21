import os
import shutil
import imageio_ffmpeg

raw_dir = r"C:/mari/Aprendo-IA-Jugando-en-Santiago-Grupo-Netan-Y-Yahoo/app/src/main/res/raw"
backup_dir = os.path.join(raw_dir, "backup")
os.makedirs(backup_dir, exist_ok=True)

ffmpeg_exe = imageio_ffmpeg.get_ffmpeg_exe()

videos = [
    "video_barrio_bellavista.mp4",
    "video_cerro_san_cristobal.mp4",
    "video_cerro_santa_lucia.mp4",
    "video_palacio_de_la_moneda.mp4",
    "video_parque_forestal.mp4",
    "video_plaza_armas.mp4"
]

for v in videos:
    src = os.path.join(raw_dir, v)
    backup_path = os.path.join(backup_dir, v)

    if os.path.exists(src):
        # 1. Backup original if not already backed up
        if not os.path.exists(backup_path):
            shutil.copy2(src, backup_path)
            print(f"Backed up: {v}")

        temp_out = os.path.join(raw_dir, "temp_" + v)

        # 2. Transcode command: 1280x720, H.264 Main Profile level 3.1, 30 FPS, AAC audio
        cmd = [
            ffmpeg_exe, "-y", "-i", src,
            "-vf", "scale=1280:720:force_original_aspect_ratio=decrease,pad=1280:720:(ow-iw)/2:(oh-ih)/2",
            "-c:v", "libx264", "-profile:v", "main", "-level", "3.1",
            "-r", "30",
            "-c:a", "aac", "-b:a", "128k",
            "-preset", "medium", "-crf", "23",
            temp_out
        ]

        print(f"Transcoding {v}...")
        res = os.system(" ".join([f'"{c}"' if " " in c else c for c in cmd]))

        if res == 0:
            os.replace(temp_out, src)
            print(f"Successfully transcoded and replaced: {v}")
        else:
            print(f"Error transcoding {v}")
            if os.path.exists(temp_out):
                os.remove(temp_out)
    else:
        print(f"File not found: {src}")

print("All transcoding tasks completed.")
