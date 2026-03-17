package ch.unibas.dmi.dbis.cs108.example.music; // Passe das an deinen genauen Pfad an!

import ai.onnxruntime.*;
import java.util.Collections;

public class MusicAI {
    private OrtEnvironment env;
    private OrtSession session;

    public MusicAI(String modelPath) throws OrtException {
        env = OrtEnvironment.getEnvironment();
        // Lade das Modell (die .data Datei zieht er sich automatisch dazu!)
        session = env.createSession(modelPath, new OrtSession.SessionOptions());
        System.out.println("🧠 ONNX-Modell geladen und scharf!");
    }

    public long generiereNaechsteNote(long[] promptNotes) throws OrtException {
        // Bereite das 2D-Array vor [BatchSize=1][SequenceLength]
        long[][] inputData = { promptNotes };

        try (OnnxTensor inputTensor = OnnxTensor.createTensor(env, inputData)) {
            // "input_ids" ist der Input-Name unseres Transformers
            var inputs = Collections.singletonMap("input_ids", inputTensor);

            try (OrtSession.Result results = session.run(inputs)) {
                // Das Output-Format ist [Batch][Sequence][VocabSize]
                float[][][] logits = (float[][][]) results.get(0).getValue();

                // Wir wollen nur die Vorhersage für die letzte Note in der Reihe
                float[] vorhersage = logits[0][promptNotes.length - 1];

                return findeBesteNote(vorhersage);
            }
        }
    }

    private long findeBesteNote(float[] probabilities) {
        int besterIndex = 0;
        float hoechsterWert = -Float.MAX_VALUE;
        for (int i = 0; i < probabilities.length; i++) {
            if (probabilities[i] > hoechsterWert) {
                hoechsterWert = probabilities[i];
                besterIndex = i;
            }
        }
        return besterIndex;
    }
}