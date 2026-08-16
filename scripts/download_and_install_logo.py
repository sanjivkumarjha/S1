import os
import sys
import requests
from PIL import Image

def download_file_from_google_drive(file_id, destination):
    print(f"Downloading file ID {file_id} from Google Drive...")
    URL = "https://docs.google.com/uc?export=download"

    session = requests.Session()
    response = session.get(URL, params={'id': file_id, 'confirm': 't'}, stream=True)
    token = get_confirm_token(response)

    if token:
        params = {'id': file_id, 'confirm': token}
        response = session.get(URL, params=params, stream=True)

    save_response_content(response, destination)

def get_confirm_token(response):
    for key, value in response.cookies.items():
        if key.startswith('download_warning'):
            return value
    return None

def save_response_content(response, destination):
    CHUNK_SIZE = 32768
    with open(destination, 'wb') as f:
        for chunk in response.iter_content(CHUNK_SIZE):
            if chunk:  # filter out keep-alive new chunks
                f.write(chunk)

def main():
    file_id = "1PmlUdJ8o3Fkk6Vy5zuEKPcltiBKDbBVz"
    temp_destination = "temp_app_logo"

    try:
        download_file_from_google_drive(file_id, temp_destination)
        print("Download complete. Validating file...")

        # Read first few bytes to check if it's an HTML page
        with open(temp_destination, 'rb') as f:
            header = f.read(100)
            if b"<!DOCTYPE html>" in header or b"<html" in header or b"google.com" in header:
                print("Error: The downloaded content appears to be an HTML page, not an image.", file=sys.stderr)
                sys.exit(1)

        # Open image with PIL to verify and prepare for saving
        img = Image.open(temp_destination)
        print(f"Successfully opened image: {img.format}, size: {img.size}")

        # Ensure target directories exist
        res_dir = "app/src/main/res"
        
        # Paths to save PNG files
        target_png_paths = [
            os.path.join(res_dir, "drawable/ic_launcher.png"),
        ]

        mipmaps = ["hdpi", "mdpi", "xhdpi", "xxhdpi", "xxxhdpi"]
        for mip in mipmaps:
            target_png_paths.append(os.path.join(res_dir, f"mipmap-{mip}/ic_launcher.png"))
            target_png_paths.append(os.path.join(res_dir, f"mipmap-{mip}/ic_launcher_round.png"))

        # Save to each path
        for path in target_png_paths:
            os.makedirs(os.path.dirname(path), exist_ok=True)
            img.save(path, "PNG")
            print(f"Saved logo to: {path}")

        # Delete any existing .webp launcher files to avoid conflicts
        for mip in mipmaps:
            for name in ["ic_launcher.webp", "ic_launcher_round.webp"]:
                webp_path = os.path.join(res_dir, f"mipmap-{mip}/{name}")
                if os.path.exists(webp_path):
                    os.remove(webp_path)
                    print(f"Removed legacy webp icon: {webp_path}")

        # Clean up temp file
        if os.path.exists(temp_destination):
            os.remove(temp_destination)

        print("Logo installation completed successfully!")

    except Exception as e:
        print(f"Exception occurred during download or validation: {e}", file=sys.stderr)
        if os.path.exists(temp_destination):
            os.remove(temp_destination)
        sys.exit(1)

if __name__ == "__main__":
    main()
