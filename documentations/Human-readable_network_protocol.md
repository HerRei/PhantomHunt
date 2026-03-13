# PhantomHunt - Netzwerkprotokoll-Spezifikation
Dieses Dokument beschreibt den textbasierte Netzwerkprotokoll, das für die Kommunikation zwischen Client und Server verwendet wird.

## 1. Aufbau der Nachrichten
Alle Nachrichten werden als Text (im UTF-8 Format) über das Netzwerk verschickt und enden immer mit einem Zeilenumbruch (Enter).
Das Protokoll trennt jede Nachricht beim ersten Leerzeichen. Dadurch besteht jede Nachricht aus bis zu zwei Teilen:
-   **COMMAND (Befehl):** Das allererste Wort der Nachricht. Es wird immer GROSS 
    geschrieben (z. B. WHISPER oder PING).
-   **PAYLOAD (Inhalt/Argumente):** Alles, was nach dem ersten Leerzeichen kommt. Das können 
    Namen oder Chat-Texte sein. Dieser Teil ist optional.
-   **Aufbau:** <COMMAND> <PAYLOAD>
    Wenn ein Befehl keine Argumente braucht, besteht die Nachricht nur aus dem <COMMAND>

## 2. Status- & Verbindungsbefehle
Diese Befehle stellen sicher, dass die Verbindung zwischen Server und Client aktiv bleibt.
### PING
-   **Richtung:** Server -> Client
-   **Beschreibung:** Der Server sendet alle 15 Sekunden einen PING an den Client, um zu
    überprüfen, ob dieser noch online ist.
-   **Beispiel:** PING
### PONG
-   **Richtung:** Client -> Server
-   **Beschreibung:** Die obligatorische Antwort des Clients auf einen PING. Wenn der Server 
    innerhalb von 16 Sekunden keinen PONG erhält, wird der Client vom Server gekickt.
-   **Beispiel:** PONG
### LOGOUT
-   **Richtung:** Client -> Server
-   **Beschreibung:** Der Client teilt dem Server mit, dass er das Spiel verlassen möchte.
    Der Server bestätigt dies mit einer Abschiedsnachricht und schließt die Verbindung (disconnect()).
-   **Beispiel:**
    -   **Client:** LOGOUT
    -   **Server-Antwort:** UNICOM Okay, Bye

## 3. Identitätsbefehle
Befehle, die den Status oder den Namen eines Spielers auf dem Server verwalten.
### NICK
-   **Richtung:** Client -> Server (handleNickChange)
-   **Beschreibung:** Beantragt eine Änderung des Benutzernamens. Wird auch bei der ersten
    Verbindung automatisch gesendet, wobei der System-Benutzername
    (oder ein zufällig generierter Name) verwendet wird.
-   **Payload:** Der gewünschte Name.
-   **Beispiele:**
    -   **Client:** NICK PhantomHunter
    -   **Server-Antwort (Erfolg):** CLEARED NICK PhantomHunter
    -   **Server-Antwort (Fehler/Name vergeben):** REJECT Name was taken. You are now: PhantomHunter1
### CLEARED
-   **Richtung:** Server -> Client
-   **Beschreibung:** Bestätigt dem Client, dass eine systemkritische Aktion (z. B. eine NICK-Änderung) erfolgreich war.
-   **Payload:** Der Kontext der Freigabe (z. B. der zugewiesene Name).
-   **Beispiel:** CLEARED NICK PhantomHunter
### REJECT
-   **Richtung:** Server -> Client
-   **Beschreibung:** Informiert den Client über einen Fehler. Dies geschieht beispielsweise bei ungültigen Eingaben, nicht unterstützten Befehlen, leeren Namen oder wenn ein Chatpartner nicht gefunden werden konnte.
-   **Payload:** Die genaue Fehlermeldung.
-   **Beispiele:** 
    -   **REJECT Name was taken. You are now:** Player1 (Wenn der gewünschte Name bereits besetzt ist)
    -   **REJECT User not found:** Player2 (Wenn bei WHISPER das Ziel nicht online ist)
### CHECKIN
-   **Richtung:** Client -> Server
-   **Beschreibung:** Reserviert für zukünftige Identitätsüberprüfungen (CHECKIN <name>). 
-   **Beispiel:** CHECKIN Player1

## 4. Chat-Befehle
Befehle für die textbasierte Kommunikation zwischen Spielern.
### UNICOM
-   **Richtung:** Client <-> Server
-   **Beschreibung:** Globaler Chat. Der Client sendet eine Nachricht an den Server, und der Server leitet diese Nachricht an alle registrierten Clients weiter. Der Server fügt automatisch den Namen des Absenders voran.
-   **Payload:** Die Chat-Nachricht.
-   **Beispiel:** 
    -   **Client -> Server:** UNICOM Hello everyone!
    -   **Server -> Alle Clients:** UNICOM Player1: Hello everyone!
### WHISPER
-   **Richtung:** Client -> Server
-   **Beschreibung**: Sendet eine private Nachricht an einen bestimmten Spieler. Das erste Wort des Payloads muss der exakte Name des Empfängers sein, gefolgt von einem Leerzeichen und der eigentlichen Nachricht.
-   **Payload:** <Recipient> <Message>
-   **Beispiel Client:** WHISPER PhantomHunter Watch out, behind you!
    -   **Fehlerfall (Server -> Client):** Wenn "PhantomHunter" nicht existiert, antwortet der Server
        mit **REJECT User not found:** PhantomHunter.
