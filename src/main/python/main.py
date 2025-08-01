import socket
import sys
import webrtcvad
import wave
import io
import speech_recognition as sr


vad = webrtcvad.Vad()
vad.set_mode(3)

chunk_size = 4096
frame_duration_ms = 30
sample_rate = 48_000
channels = 1
sample_width = 2
frame_size = int(sample_rate * frame_duration_ms / 1000) * sample_width
max_silence_frames = 30 # 10 * 30ms = 300ms

recognizer = sr.Recognizer()

def raw_pcm_to_wav_bytes(raw_pcm: bytes) -> io.BytesIO:
    wav_io = io.BytesIO()
    with wave.open(wav_io, 'wb') as wav_file:
        wav_file.setnchannels(channels)
        wav_file.setsampwidth(sample_width)
        wav_file.setframerate(sample_rate)
        wav_file.writeframes(raw_pcm)
    wav_io.seek(0)
    return wav_io

def run(port: int, language: str):
    print("Running...")

    with socket.socket(socket.AF_INET, socket.SOCK_STREAM) as sock:
        print(f"Connecting to 127.0.0.1:{port}...")
        sock.connect(("127.0.0.1", port))

        print("Connected.")
        sock.sendall("#Connected\n".encode())

        pcm_buffer = bytearray()
        voice_buffer = bytearray()
        speaking = False
        silence_counter = 0

        while True:
            chunk = sock.recv(chunk_size)
            if not chunk:
                break

            pcm_buffer.extend(chunk)

            # if we have enough bytes
            while len(pcm_buffer) >= frame_size:
                frame = pcm_buffer[:frame_size]
                pcm_buffer = pcm_buffer[frame_size:]

                is_speech = vad.is_speech(frame, sample_rate=sample_rate)

                if is_speech:
                    if not speaking:
                        print("Speaking...")
                        speaking = True
                    voice_buffer.extend(frame)
                    silence_counter = 0
                elif speaking:
                    silence_counter += 1
                    voice_buffer.extend(frame)

                    if silence_counter >= max_silence_frames:
                        print("Recorded phrase")

                        try:
                            wav_audio = raw_pcm_to_wav_bytes(voice_buffer)
                            with sr.AudioFile(wav_audio) as audio:
                                voice = recognizer.record(audio)
                                # noinspection PyUnresolvedReferences
                                text = recognizer.recognize_google(voice, language=language)
                                print(text)
                                sock.sendall((text + "\n").encode("utf-8"))
                        except Exception as e:
                            print(e)

                        speaking = False
                        silence_counter = 0
                        voice_buffer = bytearray()


if __name__ == "__main__":
    print("Starting stt...")
    run(int(sys.argv[1]), sys.argv[2])