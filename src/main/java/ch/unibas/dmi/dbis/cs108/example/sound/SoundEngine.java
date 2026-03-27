package ch.unibas.dmi.dbis.cs108.example.sound;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.stb.STBVorbis;
import org.lwjgl.system.MemoryStack;
import org.lwjgl.system.MemoryUtil;

import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.IntBuffer;
import java.nio.ShortBuffer;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.lwjgl.openal.AL10.*;
import static org.lwjgl.openal.ALC10.*;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * A sound engine powered by LWJGL and OpenAL for playing audio.
 * This engine must be initialized before use and shut down on application exit.
 * It is designed to load and play .ogg audio files.
 */
public final class SoundEngine {

    private static final Logger LOGGER = LogManager.getLogger(SoundEngine.class);

    private long device;
    private long context;

    private final Map<String, Integer> soundBufferMap = new HashMap<>();
    private final List<Integer> activeSources = new ArrayList<>();

    /**
     * Initializes the OpenAL sound engine. This must be called before any other methods.
     * It sets up the audio device and context needed for sound playback.
     */
    public void init() {
        // Open the default audio device.
        device = alcOpenDevice((ByteBuffer) null);
        if (device == NULL) {
            throw new IllegalStateException("Failed to open the default OpenAL device.");
        }

        // Create the OpenAL context.
        ALCCapabilities alcCapabilities = ALC.createCapabilities(device);
        context = alcCreateContext(device, (IntBuffer) null);
        if (context == NULL) {
            throw new IllegalStateException("Failed to create an OpenAL context.");
        }

        // Make the context current and create capabilities.
        alcMakeContextCurrent(context);
        AL.createCapabilities(alcCapabilities);

        LOGGER.info("OpenAL sound engine initialized successfully.");
    }

    /**
     * Loads a sound from a resource path and prepares it for playback.
     * This implementation uses STB to decode .ogg Vorbis files.
     *
     * @param name The friendly name to associate with the sound (e.g., "jump").
     * @param path The resource path to the sound file (e.g., "/sounds/jump.ogg").
     */
    public void loadSound(String name, String path) {
        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Read the entire audio file into a byte buffer.
            ByteBuffer resourceBuffer = resourceToByteBuffer(path);

            IntBuffer channels = stack.mallocInt(1);
            IntBuffer sampleRate = stack.mallocInt(1);

            // Decode the .ogg file into raw audio data (PCM).
            ShortBuffer rawAudioBuffer = STBVorbis.stb_vorbis_decode_memory(resourceBuffer, channels, sampleRate);
            if (rawAudioBuffer == null) {
                LOGGER.error("Failed to decode Vorbis audio from: {}", path);
                return;
            }

            // Determine the OpenAL format based on the number of channels.
            int format = (channels.get(0) == 1) ? AL_FORMAT_MONO16 : AL_FORMAT_STEREO16;

            // Create an OpenAL buffer and upload the audio data.
            int bufferPointer = alGenBuffers();
            alBufferData(bufferPointer, format, rawAudioBuffer, sampleRate.get(0));

            // Free the decoded audio buffer, as it's now on the audio device.
            MemoryUtil.memFree(rawAudioBuffer);

            // Cache the buffer ID using the friendly name.
            soundBufferMap.put(name, bufferPointer);
            LOGGER.info("Loaded sound '{}' from path '{}'", name, path);

        } catch (IOException e) {
            LOGGER.error("Could not find or read sound file at path: {}", path, e);
        }
    }

    /**
     * Plays a loaded sound.
     *
     * @param name The friendly name of the sound to play.
     * @return The OpenAL source ID for the playing sound, which can be used to stop it. Returns -1 on failure.
     */
    public int playSound(String name) {
        Integer bufferId = soundBufferMap.get(name);
        if (bufferId == null) {
            LOGGER.warn("Attempted to play a sound that was not loaded: '{}'", name);
            return -1;
        }

        // Create a new audio source (a playback channel).
        int sourcePointer = alGenSources();
        // Attach the sound buffer to the source.
        alSourcei(sourcePointer, AL_BUFFER, bufferId);
        // Play the sound.
        alSourcePlay(sourcePointer);

        // Keep track of the source so it can be cleaned up later.
        activeSources.add(sourcePointer);
        LOGGER.info("Playing sound '{}' on source ID {}", name, sourcePointer);
        return sourcePointer;
    }

    /**
     * Stops a playing sound and releases its source.
     *
     * @param sourcePointer The source ID of the sound to stop (returned by playSound).
     */
    public void stopSound(int sourcePointer) {
        if (activeSources.contains(sourcePointer)) {
            alSourceStop(sourcePointer);
            alDeleteSources(sourcePointer);
            activeSources.remove(Integer.valueOf(sourcePointer));
            LOGGER.info("Stopped and deleted audio source ID {}", sourcePointer);
        }
    }

    /**
     * Shuts down the sound engine and cleans up all resources.
     * This must be called when the application is closing to prevent memory leaks.
     */
    public void shutdown() {
        // Stop and delete all currently active sources.
        for (int source : activeSources) {
            alSourceStop(source);
            alDeleteSources(source);
        }
        activeSources.clear();

        // Delete all sound buffers that were loaded.
        for (int buffer : soundBufferMap.values()) {
            alDeleteBuffers(buffer);
        }
        soundBufferMap.clear();

        // Destroy the OpenAL context and close the audio device.
        if (context != NULL) {
            alcDestroyContext(context);
        }
        if (device != NULL) {
            alcCloseDevice(device);
        }

        LOGGER.info("OpenAL sound engine has been shut down.");
    }

    /**
     * Helper method to read a resource file into a direct ByteBuffer.
     */
    private ByteBuffer resourceToByteBuffer(String resource) throws IOException {
        InputStream source = SoundEngine.class.getResourceAsStream(resource);
        if (source == null) {
            throw new IOException("Resource not found: " + resource);
        }
        byte[] bytes = source.readAllBytes();
        ByteBuffer buffer = ByteBuffer.allocateDirect(bytes.length);
        buffer.put(bytes).flip();
        return buffer;
    }
}
