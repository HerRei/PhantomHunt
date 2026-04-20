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
### WELCOME
-   **Richtung:** Server -> Client
-   **Beschreibung:** Begrüsst einen neu verbundenen Client.
-   **Payload:** Der Name des Clients.
-   **Beispiel:** WELCOME PhantomHunter
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
### INFO
-   **Richtung:** Server -> Client
-   **Beschreibung:** Sendet eine allgemeine Information an den Client.
-   **Payload:** Die Informationsnachricht.
-   **Beispiel:** INFO This is an informational message.
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
### YAP
-   **Richtung:** Client -> Server
-   **Beschreibung:** Sendet eine Nachricht, die nur für die Lobby bestimmt ist.
-   **Payload:** Die Chat-Nachricht.
-   **Beispiel:** YAP Let's start the game!

## 5. Lobby- und Spielbefehle
Befehle zur Verwaltung von Spiellobbys und dem Spielablauf.
### MKL
-   **Richtung:** Client -> Server
-   **Beschreibung:** Erstellt eine neue Lobby.
-   **Payload:** Der Name der Lobby.
-   **Beispiel:** MKL My awesome lobby
### SPEC
-   **Richtung:** Client -> Server
-   **Beschreibung:** Lässt den Client einer bestimmten Lobby als Zuschauer beitreten.
-   **Payload:** Die ID der Lobby.
-   **Beispiel:** SPEC 12345
### START
-   **Richtung:** Client -> Server
-   **Beschreibung:** Startet das Spiel in der aktuellen Lobby. Der Befehl kann nur vom Host der Lobby ausgeführt werden.
-   **Payload:** Die ID der Lobby.
-   **Beispiel:** START lobby1
### LOBBY_INFO
-   **Richtung:** Server -> Client
-   **Beschreibung:** Sendet Informationen über eine Lobby an den Client, einschließlich der Lobby-ID und der Liste der Spieler.
-   **Payload:** <LobbyId> <Player1> <Player2> ...
-   **Beispiel:** LOBBY_INFO lobby1 Player1 Player2 Player3
### GAME_START
-   **Richtung:** Server -> Client
-   **Beschreibung:** Signalisiert dem Client, dass das Spiel gestartet wurde und er zur Spielszene wechseln soll.
-   **Beispiel:** GAME_START
### GSU (Game State Update)
-   **Richtung:** Server -> Client
-   **Beschreibung:** Sendet den aktuellen Spielzustand an die Clients. Dies beinhaltet die aktuelle Runde, die verbleibende Zeit und die Zustände der Spieler.
-   **Payload:** <currentRound> <timeRemaining> <playerData>
-   **playerData Format:** <Name>:<Role>:<X>:<Y>:<Score>;<Name>:<Role>:<X>:<Y>:<Score>;...
-   **Beispiel:** GSU 1 25 Name1:HUMAN:10.5:20.2:100;Name2:PHANTOM:30.1:40.3:50
### LIST_LOBBY
-   **Richtung:** Server <-> Client
-   **Beschreibung:** Sendet die aktuellen Lobbies an den client und Client schickt Anfrage dafür.
-   **Payload:** <waitingLobbies>:<playingLobbies>
-   **Beispiel:** LIST_LOBBY lobby1:lobby2;lobby3
### ABILITY
-   **Richtung:** Server <-> Client
-   **Beschreibung:** Client schickt Anfrage für ability, Server sendet darauf, falls geht start und ende der Zeit.
-   **Payload:** (start/end)
-   **Beispiel:** ABILITY START
