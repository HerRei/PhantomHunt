package ch.unibas.dmi.dbis.cs108.example.music;

import ai.onnxruntime.OnnxTensor;
import ai.onnxruntime.OrtEnvironment;
import ai.onnxruntime.OrtException;
import ai.onnxruntime.OrtProvider;
import ai.onnxruntime.OrtSession;

import java.util.Collections;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class MusicAI implements AutoCloseable {
    private static final String INPUT_NAME = "input_ids";

    private static final int MIN_NOTE = 40;
    private static final int MAX_NOTE = 90;
    private static final int REPETITION_MEMORY = 6;
    private static final float REPETITION_PENALTY = 8.0f;
    private static final float TEMPERATURE = 1.1f;
    private static final long FALLBACK_NOTE = 60L;

    private final OrtEnvironment env;
    private final OrtSession session;
    private final boolean coreMlAktiv;

    public MusicAI(String modelPath) throws OrtException {
        this.env = OrtEnvironment.getEnvironment();

        System.out.println("ORT-Version: " + env.getVersion());
        EnumSet<OrtProvider> providers = OrtEnvironment.getAvailableProviders();
        System.out.println("Verfügbare Provider: " + providers);

        OrtSession builtSession = null;
        boolean usingCoreML = false;

        if (providers.contains(OrtProvider.CORE_ML)) {
            try {
                OrtSession.SessionOptions coreMlOpts = new OrtSession.SessionOptions();
                configureCommonOptions(coreMlOpts);

                Map<String, String> coreMlOptions = new HashMap<>();
                coreMlOptions.put("ModelFormat", "MLProgram");
                coreMlOptions.put("MLComputeUnits", "ALL");
                coreMlOptions.put("RequireStaticInputShapes", "1");
                coreMlOptions.put("EnableOnSubgraphs", "0");

                coreMlOpts.addCoreML(coreMlOptions);
                coreMlOpts.addCPU(true);

                System.out.println("🍎 Versuche CoreML-Session zu bauen ...");
                builtSession = env.createSession(modelPath, coreMlOpts);
                usingCoreML = true;
                System.out.println("✅ CoreML-Session erfolgreich geladen.");
            } catch (OrtException e) {
                System.out.println("⚠️ CoreML-Session fehlgeschlagen, falle auf CPU zurück.");
                System.out.println("Grund: " + e.getMessage());
            }
        }

        if (builtSession == null) {
            OrtSession.SessionOptions cpuOpts = new OrtSession.SessionOptions();
            configureCommonOptions(cpuOpts);
            cpuOpts.addCPU(true);

            System.out.println("🖥️ Baue CPU-Session ...");
            builtSession = env.createSession(modelPath, cpuOpts);
            usingCoreML = false;
            System.out.println("✅ CPU-Session erfolgreich geladen.");
        }

        this.session = builtSession;
        this.coreMlAktiv = usingCoreML;
    }

    private void configureCommonOptions(OrtSession.SessionOptions opts) throws OrtException {
        opts.setOptimizationLevel(OrtSession.SessionOptions.OptLevel.ALL_OPT);
        opts.setCPUArenaAllocator(true);
        opts.setMemoryPatternOptimization(true);
        opts.setInterOpNumThreads(1);

        int cores = Math.max(1, Runtime.getRuntime().availableProcessors());
        opts.setIntraOpNumThreads(Math.max(1, cores / 2));
    }

    public float[] holeLogits(long[] promptNotes) throws OrtException {
        if (promptNotes == null || promptNotes.length == 0) {
            float[] dummy = new float[128];
            dummy[(int) FALLBACK_NOTE] = 1.0f;
            return dummy;
        }

        long[][] inputData = {promptNotes};

        try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, inputData)) {
            Map<String, OnnxTensor> inputs = Collections.singletonMap(INPUT_NAME, inputTensor);

            try (OrtSession.Result results = session.run(inputs)) {
                Object output = results.get(0).getValue();

                if (!(output instanceof float[][][] logits)) {
                    throw new OrtException("Unerwarteter Output-Typ: " + output.getClass().getName());
                }

                if (logits.length == 0 || logits[0].length == 0) {
                    float[] dummy = new float[128];
                    dummy[(int) FALLBACK_NOTE] = 1.0f;
                    return dummy;
                }

                int lastIndex = Math.min(promptNotes.length - 1, logits[0].length - 1);
                return logits[0][lastIndex].clone();
            }
        }
    }

    public long generiereNaechsteNote(long[] promptNotes) throws OrtException {
        float[] logits = holeLogits(promptNotes);
        return sampleAusLogits(logits, promptNotes, MIN_NOTE, MAX_NOTE, TEMPERATURE, REPETITION_PENALTY);
    }

    public long sampleAusLogits(
            float[] logits,
            long[] history,
            int minValue,
            int maxValue,
            float temperature,
            float repetitionPenalty
    ) {
        if (logits == null || logits.length == 0) {
            return FALLBACK_NOTE;
        }

        int lower = Math.max(0, minValue);
        int upper = Math.min(maxValue, logits.length - 1);

        if (upper < lower) {
            return FALLBACK_NOTE;
        }

        if (history != null && repetitionPenalty > 0f) {
            int memory = Math.min(REPETITION_MEMORY, history.length);
            for (int i = 1; i <= memory; i++) {
                int past = (int) history[history.length - i];
                if (past >= lower && past <= upper) {
                    logits[past] -= repetitionPenalty;
                }
            }
        }

        float maxLogit = -Float.MAX_VALUE;
        for (int i = lower; i <= upper; i++) {
            if (logits[i] > maxLogit) {
                maxLogit = logits[i];
            }
        }

        double sum = 0.0;
        double[] probs = new double[logits.length];

        for (int i = lower; i <= upper; i++) {
            probs[i] = Math.exp((logits[i] - maxLogit) / Math.max(0.05f, temperature));
            sum += probs[i];
        }

        if (sum <= 0.0 || Double.isNaN(sum) || Double.isInfinite(sum)) {
            return FALLBACK_NOTE;
        }

        double r = Math.random() * sum;
        double running = 0.0;

        for (int i = lower; i <= upper; i++) {
            running += probs[i];
            if (running >= r) {
                return i;
            }
        }

        return FALLBACK_NOTE;
    }

    public int waehleIndex(float[] logits, long[] history, int optionCount, float temperature) {
        if (optionCount <= 1) return 0;

        long sample = sampleAusLogits(
                logits,
                history,
                MIN_NOTE,
                MAX_NOTE,
                temperature,
                REPETITION_PENALTY / 2.0f
        );

        return Math.floorMod((int) sample, optionCount);
    }

    public boolean isCoreMlAktiv() {
        return coreMlAktiv;
    }

    @Override
    public void close() throws OrtException {
        if (session != null) session.close();
        if (env != null) env.close();
    }
}