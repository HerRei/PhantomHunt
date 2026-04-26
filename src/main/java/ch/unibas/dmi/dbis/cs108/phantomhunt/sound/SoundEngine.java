package ch.unibas.dmi.dbis.cs108.phantomhunt.sound;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.BufferUtils;
import org.lwjgl.openal.AL;
import org.lwjgl.openal.ALC;
import org.lwjgl.openal.ALCCapabilities;
import org.lwjgl.openal.ALCapabilities;

import javax.sound.sampled.AudioFormat;
import javax.sound.sampled.AudioInputStream;
import javax.sound.sampled.AudioSystem;
import javax.sound.sampled.UnsupportedAudioFileException;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.net.URL;
import java.nio.ByteBuffer;
import java.util.EnumMap;
import java.util.Map;

import static org.lwjgl.openal.ALC10.ALC_DEFAULT_DEVICE_SPECIFIER;
import static org.lwjgl.openal.ALC10.alcCloseDevice;
import static org.lwjgl.openal.ALC10.alcCreateContext;
import static org.lwjgl.openal.ALC10.alcDestroyContext;
import static org.lwjgl.openal.ALC10.alcGetString;
import static org.lwjgl.openal.ALC10.alcMakeContextCurrent;
import static org.lwjgl.openal.ALC10.alcOpenDevice;
import static org.lwjgl.openal.AL10.AL_BUFFER;
import static org.lwjgl.openal.AL10.AL_FALSE;
import static org.lwjgl.openal.AL10.AL_FORMAT_MONO16;
import static org.lwjgl.openal.AL10.AL_FORMAT_STEREO16;
import static org.lwjgl.openal.AL10.AL_GAIN;
import static org.lwjgl.openal.AL10.AL_LOOPING;
import static org.lwjgl.openal.AL10.AL_PLAYING;
import static org.lwjgl.openal.AL10.AL_SOURCE_STATE;
import static org.lwjgl.openal.AL10.AL_TRUE;
import static org.lwjgl.openal.AL10.alBufferData;
import static org.lwjgl.openal.AL10.alDeleteBuffers;
import static org.lwjgl.openal.AL10.alDeleteSources;
import static org.lwjgl.openal.AL10.alGenBuffers;
import static org.lwjgl.openal.AL10.alGenSources;
import static org.lwjgl.openal.AL10.alGetSourcei;
import static org.lwjgl.openal.AL10.alSourcePlay;
import static org.lwjgl.openal.AL10.alSourceRewind;
import static org.lwjgl.openal.AL10.alSourceStop;
import static org.lwjgl.openal.AL10.alSourcef;
import static org.lwjgl.openal.AL10.alSourcei;
import static org.lwjgl.system.MemoryUtil.NULL;

/**
 * A sound engine powered by LWJGL and OpenAL for playing audio. Modeled after the "GamesWithGabe"
 * OpenAL 2D Game Engine tutorial from YouTube It has to be disclosed that this is pretty much a 1:1
 * copy.
 */
public final class SoundEngine {

  private static final Logger LOGGER = LogManager.getLogger(SoundEngine.class);

  private long audioDevice;
  private long audioContext;
  private boolean initialized;
  private final Map<SoundEffect, Integer> bufferIds = new EnumMap<>(SoundEffect.class);
  private final Map<SoundEffect, Integer> sourceIds = new EnumMap<>(SoundEffect.class);

  /**
   * Initializes the OpenAL audio device and context. Must be called before loading or playing any
   * sounds.
   *
   * @throws IllegalStateException if OpenAL is unsupported or fails to initialize.
   */
  public synchronized void init() {
    if (initialized) {
      return;
    }

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
      throw new IllegalStateException("OpenAL is not supported on this system.");
    }

    initialized = true;
    LOGGER.info("OpenAL sound engine initialized successfully.");
  }

  /**
   * Loads a sound effect into memory and assigns it an OpenAL source ID.
   *
   * @param effect the sound effect to load.
   */
  public synchronized void loadSound(SoundEffect effect) {
    ensureInitialized();
    if (sourceIds.containsKey(effect)) {
      return;
    }

    int bufferId = createBuffer(effect);
    int sourceId = createSource(effect, bufferId);

    bufferIds.put(effect, bufferId);
    sourceIds.put(effect, sourceId);
    LOGGER.info("Loaded sound '{}'", effect.name());
  }

  /**
   * Plays the specified sound effect. If it is already playing, it will be restarted.
   *
   * @param effect the sound effect to play.
   */
  public void playSound(SoundEffect effect) {
    Integer sourceId = getSourceId(effect);
    if (sourceId == null) {
      return;
    }

    int state = alGetSourcei(sourceId, AL_SOURCE_STATE);
    if (state == AL_PLAYING) {
      return;
    }

    alSourceRewind(sourceId);
    alSourcePlay(sourceId);
  }

  /**
   * Stops the specified sound effect if it is currently playing.
   *
   * @param effect the sound effect to stop.
   */
  public void stopSound(SoundEffect effect) {
    Integer sourceId = getSourceId(effect);
    if (sourceId == null) {
      return;
    }

    alSourceStop(sourceId);
  }

  /** Stops all currently playing sounds. */
  public synchronized void stopAll() {
    for (Integer sourceId : sourceIds.values()) {
      alSourceStop(sourceId);
    }
  }

  /**
   * Cleans up all OpenAL resources, buffers, and destroys the audio context. Should be called when
   * the application is closing.
   */
  public synchronized void shutdown() {
    for (Integer sourceId : sourceIds.values()) {
      alDeleteSources(sourceId);
    }
    for (Integer bufferId : bufferIds.values()) {
      alDeleteBuffers(bufferId);
    }
    sourceIds.clear();
    bufferIds.clear();

    if (audioContext != NULL) {
      alcDestroyContext(audioContext);
    }
    if (audioDevice != NULL) {
      alcCloseDevice(audioDevice);
    }

    audioContext = NULL;
    audioDevice = NULL;
    initialized = false;
    LOGGER.info("OpenAL sound engine has been shut down.");
  }

  private void ensureInitialized() {
    if (!initialized) {
      throw new IllegalStateException("SoundEngine.init() must be called first.");
    }
  }

  private Integer getSourceId(SoundEffect effect) {
    Integer sourceId = sourceIds.get(effect);
    if (sourceId == null) {
      LOGGER.warn("Attempted to access a sound that was not loaded: '{}'", effect.name());
    }
    return sourceId;
  }

  private int createBuffer(SoundEffect effect) {
    URL resource = SoundEngine.class.getResource(effect.resourcePath());
    if (resource == null) {
      throw new IllegalStateException("Missing sound resource: " + effect.resourcePath());
    }

    try (AudioInputStream inputStream = AudioSystem.getAudioInputStream(resource)) {
      AudioFormat baseFormat = inputStream.getFormat();
      AudioFormat pcmFormat =
          new AudioFormat(
              AudioFormat.Encoding.PCM_SIGNED,
              baseFormat.getSampleRate(),
              16,
              baseFormat.getChannels(),
              baseFormat.getChannels() * 2,
              baseFormat.getSampleRate(),
              false);

      try (AudioInputStream pcmStream = AudioSystem.getAudioInputStream(pcmFormat, inputStream);
          ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
        byte[] chunk = new byte[8192];
        int bytesRead;
        while ((bytesRead = pcmStream.read(chunk)) != -1) {
          outputStream.write(chunk, 0, bytesRead);
        }

        byte[] pcmBytes = outputStream.toByteArray();
        ByteBuffer pcmBuffer = BufferUtils.createByteBuffer(pcmBytes.length);
        pcmBuffer.put(pcmBytes);
        pcmBuffer.flip();

        int bufferId = alGenBuffers();
        alBufferData(
            bufferId,
            resolveFormat(pcmFormat.getChannels()),
            pcmBuffer,
            (int) pcmFormat.getSampleRate());
        return bufferId;
      }
    } catch (UnsupportedAudioFileException | IOException | IllegalArgumentException e) {
      throw new IllegalStateException(
          "Could not load sound resource '" + effect.resourcePath() + "'", e);
    }
  }

  private int createSource(SoundEffect effect, int bufferId) {
    int sourceId = alGenSources();
    alSourcei(sourceId, AL_BUFFER, bufferId);
    alSourcei(sourceId, AL_LOOPING, effect.loops() ? AL_TRUE : AL_FALSE);
    alSourcef(sourceId, AL_GAIN, effect.gain());
    return sourceId;
  }

  private int resolveFormat(int channels) {
    return switch (channels) {
      case 1 -> AL_FORMAT_MONO16;
      case 2 -> AL_FORMAT_STEREO16;
      default -> throw new IllegalStateException("Unsupported channel count: " + channels);
    };
  }
}
