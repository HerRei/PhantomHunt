package ch.unibas.dmi.dbis.cs108.example.sound;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.libc.LibCStdlib;

import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.stb.STBVorbis.stb_vorbis_decode_filename;
import static org.lwjgl.system.MemoryStack.stackPush;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * A sound engine powered by LWJGL and OpenAL for playing audio.
 * Modeled after the "GamesWithGabe" OpenAL 2D Game Engine tutorial from YoutTube
 * It has to be disclosed that this is pretty much a 1:1 copy
 */
public final class SoundEngine {

    private static final Logger LOGGER = LogManager.getLogger(SoundEngine.class);

    private long audioDevice;
    private long audioContext;

    // Acts as the "AssetPool" from the video
    private final Map<String, Sound> sounds = new HashMap<>();

    /**
     * Initializes the OpenAL sound engine context and capabilities.
     */
    public void init() {
        String defaultDeviceName = alcGetString(0, ALC_DEFAULT_DEVICE_SPECIFIER);
        audioDevice = alcOpenDevice(defaultDeviceName);
        if (audioDevice == NULL) {
            throw new IllegalStateException("Failed to open the default OpenAL device.");
        }

        int[] attributes = {0};
        audioContext = alcCreateContext(audioDevice, attributes);
        if (audioContext == NULL) {
            throw new IllegalStateException("Failed to create an OpenAL context.");
        }

        alcMakeContextCurrent(audioContext);

        ALCCapabilities alcCapabilities = ALC.createCapabilities(audioDevice);
        ALCapabilities alCapabilities = AL.createCapabilities(alcCapabilities);

        if (!alCapabilities.OpenAL10) {
            LOGGER.error("Audio library not supported.");
            throw new IllegalStateException("OpenAL is not supported on this system.");
        }

        LOGGER.info("OpenAL sound engine initialized successfully.");
    }

    /**
     * Loads a sound from an absolute file path and adds it to the engine's asset pool.
     */
    public void loadSound(String name, String filepath, boolean loops) {
        if (sounds.containsKey(name)) {
            return; // Already loaded
        }
        
        Sound sound = new Sound(filepath, loops);
        sounds.put(name, sound);
        LOGGER.info("Loaded sound '{}' from path '{}'", name, filepath);
    }

    /**
     * Retrieves a sound by name and plays it.
     */
    public void playSound(String name) {
        Sound sound = sounds.get(name);
        if (sound != null) {
            sound.play();
        } else {
            LOGGER.warn("Attempted to play a sound that was not loaded: '{}'", name);
        }
    }

    /**
     * Retrieves a sound by name and stops it.
     */
    public void stopSound(String name) {
        Sound sound = sounds.get(name);
        if (sound != null) {
            sound.stop();
        }
    }

    public Collection<Sound> getAllSounds() {
        return this.sounds.values();
    }

    /**
     * Shuts down the sound engine and cleans up all resources.
     */
    public void shutdown() {
        for (Sound sound : sounds.values()) {
            sound.delete();
        }
        sounds.clear();

        if (audioContext != NULL) {
            alcDestroyContext(audioContext);
        }
        if (audioDevice != NULL) {
            alcCloseDevice(audioDevice);
        }

        LOGGER.info("OpenAL sound engine has been shut down.");
    }


    public static class Sound {
        private int bufferId;
        private int sourceId;
        private String filepath;
        private boolean isPlaying = false;

        /**
         * Loads and buffers the audio data into OpenAL via STB Vorbis.
         */
        public Sound(String filepath, boolean loops) {
            this.filepath = filepath;

            try (MemoryStack stack = stackPush()) {
                IntBuffer channelsBuffer = stack.mallocInt(1);
                IntBuffer sampleRateBuffer = stack.mallocInt(1);

                ShortBuffer rawAudioBuffer = stb_vorbis_decode_filename(filepath, channelsBuffer, sampleRateBuffer);
                if (rawAudioBuffer == null) {
                    LOGGER.error("Could not load sound '{}'", filepath);
                    return;
                }

                int channels = channelsBuffer.get(0);
                int sampleRate = sampleRateBuffer.get(0);

                int format = -1;
                if (channels == 1) {
                    format = AL_FORMAT_MONO16;
                } else if (channels == 2) {
                    format = AL_FORMAT_STEREO16;
                }

                bufferId = alGenBuffers();
                alBufferData(bufferId, format, rawAudioBuffer, sampleRate);

                sourceId = alGenSources();

                alSourcei(sourceId, AL_BUFFER, bufferId);
                alSourcei(sourceId, AL_LOOPING, loops ? 1 : 0);
                alSourcei(sourceId, AL_POSITION, 0);
                alSourcef(sourceId, AL_GAIN, 0.3f);

                // Free STB raw audio buffer
                LibCStdlib.free(rawAudioBuffer); 
            }
        }

        /**
         * Plays the sound, resetting the cursor if it was previously stopped.
         */
        public void play() {
            int state = alGetSourcei(sourceId, AL_SOURCE_STATE);
            if (state == AL_STOPPED) {
                isPlaying = false;
                alSourcei(sourceId, AL_POSITION, 0);
            }

            if (!isPlaying) {
                alSourcePlay(sourceId);
                isPlaying = true;
            }
        }

        public void stop() {
            if (isPlaying) {
                alSourceStop(sourceId);
                isPlaying = false;
            }
        }

        public void delete() {
            alDeleteSources(sourceId);
            alDeleteBuffers(bufferId);
        }

        public boolean isPlaying() {
            int state = alGetSourcei(sourceId, AL_SOURCE_STATE);
            if (state == AL_STOPPED) {
                isPlaying = false;
            }
            return isPlaying;
        }

        public String getFilepath() {
            return filepath;
        }
    }
}
