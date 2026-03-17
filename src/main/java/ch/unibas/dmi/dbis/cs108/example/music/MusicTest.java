package ch.unibas.dmi.dbis.cs108.example.music; // Passe das an deinen genauen Pfad an!

public class MusicTest {
    public static void main(String[] args) {
        try {
            // 1. Module starten
            // Der Pfad zeigt direkt in deinen resources Ordner
            MusicAI ai = new MusicAI("src/main/resources/music_brain.onnx");
            AudioEngine audio = new AudioEngine();

            // 2. Ein musikalischer Prompt (z.B. C-Dur Arpeggio: C4, E4, G4)
            // 60 = C4, 64 = E4, 67 = G4
            long[] melodie = {60, 64, 67};

            System.out.println("KI denkt nach...");

            // 3. KI befragen
            long generierteNote = ai.generiereNaechsteNote(melodie);
            System.out.println("Die KI hat Note ID " + generierteNote + " generiert!");

            // 4. KI-Output bereinigen (Falls die Token-ID über 127 liegt, fangen wir das ab)
            int midiNote = (int) generierteNote;
            if (midiNote > 127 || midiNote < 0) {
                System.out.println("Note out of bounds, setze Fallback auf C4 (60)");
                midiNote = 60;
            }

            // 5. Ton spielen!
            audio.playNote(midiNote, 100);

            // 6. Das Programm 3 Sekunden warten lassen, damit du den Ton hören kannst
            Thread.sleep(3000);

            // 7. Aufräumen
            audio.close();
            System.out.println("Test erfolgreich beendet.");

        } catch (Exception e) {
            System.err.println("Es gab einen Fehler im Musik-Test!");
            e.printStackTrace();
        }
    }
}