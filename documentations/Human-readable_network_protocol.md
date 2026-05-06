# PhantomHunt - Netzwerkprotokoll-Spezifikation

Dieses Dokument beschreibt das textbasierte Netzwerkprotokoll, das für die Kommunikation zwischen Client und Server verwendet wird.

## 1. Aufbau der Nachrichten

Alle Nachrichten werden als Text im UTF-8-Format über das Netzwerk verschickt und enden immer mit einem Zeilenumbruch.

Das Protokoll trennt jede eingehende Nachricht nur am ersten Leerzeichen. Dadurch besteht eine Nachricht aus bis zu zwei Teilen:

- **COMMAND (Befehl):** Das erste Wort der Nachricht. Es wird immer großgeschrieben, z. B. `WHISPER` oder `PING`.
- **PAYLOAD (Inhalt/Argumente):** Alles, was nach dem ersten Leerzeichen kommt. Dieser Teil ist optional und kann weitere Leerzeichen enthalten.
- **Aufbau:** `<COMMAND> <PAYLOAD>`

Wenn ein Befehl keine Argumente braucht, besteht die Nachricht nur aus dem `<COMMAND>`.

## 2. Status- & Verbindungsbefehle

### PING
- **Richtung:** Server -> Client
- **Beschreibung:** Der Server sendet regelmäßig einen `PING`, um zu prüfen, ob der Client noch erreichbar ist.
- **Payload:** Keiner.
- **Beispiel:** `PING`

### PONG
- **Richtung:** Client -> Server
- **Beschreibung:** Antwort des Clients auf `PING`. Bleibt ein `PONG` zu lange aus, trennt der Server die Verbindung.
- **Payload:** Keiner.
- **Beispiel:** `PONG`

### LOGOUT
- **Richtung:** Client -> Server
- **Beschreibung:** Der Client verlässt den Server komplett. Der Server bestätigt dies, schließt die Verbindung und entfernt den Client aus der Registry.
- **Payload:** Keiner.
- **Beispiel:**
  - **Client:** `LOGOUT`
  - **Server-Antwort:** `UNICOM Okay, Bye.`

## 3. Identitäts- & Systembefehle

### NICK
- **Richtung:** Client -> Server
- **Beschreibung:** Setzt oder ändert den Nickname. Dieser Befehl wird beim Verbindungsaufbau automatisch gesendet. Leerzeichen im Namen werden serverseitig durch Unterstriche ersetzt.
- **Payload:** Der gewünschte Name.
- **Beispiele:**
  - **Client:** `NICK PhantomHunter`
  - **Server-Antwort (normale Zuweisung):** `WELCOME PhantomHunter`
  - **Server-Antwort (tatsächliche Namensänderung):** `CLEARED NICK PhantomHunter`
  - **Server-Antwort (Name schon vergeben):** `REJECT Name was taken. You are now: PhantomHunter1`

### WELCOME
- **Richtung:** Server -> Client
- **Beschreibung:** Teilt dem Client den aktuell zugewiesenen Namen mit.
- **Payload:** Der zugewiesene Name.
- **Beispiel:** `WELCOME PhantomHunter`

### CLEARED
- **Richtung:** Server -> Client
- **Beschreibung:** Bestätigt eine erfolgreiche systemrelevante Aktion. Aktuell wird `CLEARED` für Nickname-Änderungen verwendet.
- **Payload:** Kontext der bestätigten Aktion.
- **Beispiel:** `CLEARED NICK PhantomHunter`

### REJECT
- **Richtung:** Server -> Client
- **Beschreibung:** Meldet einen Fehler oder lehnt einen Befehl ab.
- **Payload:** Die genaue Fehlermeldung.
- **Beispiele:**
  - `REJECT Name was taken. You are now: Player1`
  - `REJECT User not found: Player2`
  - `REJECT Unsupported command: FOO`

### INFO
- **Richtung:** Server -> Client
- **Beschreibung:** Allgemeine Server-Information oder Systemmeldung.
- **Payload:** Die Informationsnachricht.
- **Beispiele:**
  - `INFO Welcome to the Server: PhantomHunter`
  - `INFO Alice: changed nickname to -> Alice1`
  - `INFO __HUMAN_CAUGHT__`

### PLAYERS
- **Richtung:** Server -> Client
- **Beschreibung:** Übermittelt die Liste aller aktuell verbundenen Spielernamen.
- **Payload:** `<Player1> <Player2> ...`
- **Beispiel:** `PLAYERS Alice Bob Charlie Dana`

### MENU_MUSIC
- **Richtung:** Server -> Client
- **Beschreibung:** Synchronisiert die Menü-Musik aller verbundenen Clients. Der Client spielt die Musik nur in Menü-Szenen ab und stoppt sie in Lobby, Spiel und Endscreen.
- **Payload:** `<SoundEffectName> <offsetMillis>`
- **Beispiel:** `MENU_MUSIC WINTER_AT_THE_GATE 42150`

## 4. Chat-Befehle

### UNICOM
- **Richtung:** Client <-> Server
- **Beschreibung:** Globaler Chat. Der Client sendet eine Nachricht an den Server, und der Server sendet diese Nachricht an alle verbundenen Clients zurück. Dabei wird der Name des Absenders vorangestellt.
- **Payload:** Die Chat-Nachricht.
- **Beispiel:**
  - **Client -> Server:** `UNICOM Hello everyone!`
  - **Server -> Alle Clients:** `UNICOM Player1: Hello everyone!`

### WHISPER
- **Richtung:** Client <-> Server
- **Beschreibung:** Private Nachricht an genau einen Spieler.
- **Payload:** Clientseitig `<Recipient> <Message>`, serverseitig formatierter Nachrichtentext.
- **Beispiele:**
  - **Client -> Server:** `WHISPER PhantomHunter Watch out, behind you!`
  - **Server -> Empfänger:** `WHISPER [Whisper from Player1]: Watch out, behind you!`
  - **Server -> Sender:** `WHISPER [You → PhantomHunter]: Watch out, behind you!`
  - **Fehlerfall:** `REJECT User not found: PhantomHunter`

### YAP
- **Richtung:** Client <-> Server
- **Beschreibung:** Lobby-Chat. Die Nachricht wird nur an Spieler und Zuschauer derselben Lobby weitergeleitet.
- **Payload:** Die Chat-Nachricht.
- **Beispiel:**
  - **Client -> Server:** `YAP Let's start the game!`
  - **Server -> Lobby:** `YAP Player1: Let's start the game!`

## 5. Lobby- & Spielbefehle

**Hinweis:** In der aktuellen Implementierung sind Lobby-Name und Lobby-ID identisch. Beim Erstellen einer Lobby entfernt der Server Leerzeichen sowie `:` und `;` aus dem Namen und macht ihn bei Bedarf eindeutig.

### MKL
- **Richtung:** Client -> Server
- **Beschreibung:** Erstellt eine neue Lobby.
- **Payload:** Gewünschter Lobby-Name.
- **Beispiel:** `MKL My awesome lobby`

### CHECKIN
- **Richtung:** Client -> Server
- **Beschreibung:** Tritt einer bestehenden wartenden Lobby bei.
- **Payload:** Die Lobby-ID.
- **Beispiele:**
  - **Client:** `CHECKIN lobby1`
  - **Server-Antwort (Erfolg):** `LOBBY_INFO lobby1 Player1 Player2`
  - **Server-Antwort (Fehler):** `REJECT Lobby not found or has already started: lobby1`

### SPEC
- **Richtung:** Client -> Server
- **Beschreibung:** Tritt einer bestehenden Lobby als Zuschauer bei.
- **Payload:** Die Lobby-ID.
- **Beispiele:**
  - **Client:** `SPEC lobby1`
  - **Server-Antwort (Fehler):** `REJECT Lobby not found: lobby1`

### LOGOUT_LOBBY
- **Richtung:** Client -> Server
- **Beschreibung:** Verlässt die aktuelle Lobby, ohne die Serververbindung zu beenden.
- **Payload:** Keiner.
- **Beispiel:** `LOGOUT_LOBBY`

### LIST_LOBBY
- **Richtung:** Client <-> Server
- **Beschreibung:** Der Client fordert die aktuelle Lobby-Übersicht an. Der Server antwortet mit allen wartenden und laufenden Lobbies.
- **Payload:** Serverseitig `<waitingLobby1 (players/4)>:<waitingLobby2 (players/4)>;<runningLobby1 (players/4)>`
- **Beispiel:**
  - **Client -> Server:** `LIST_LOBBY`
  - **Server -> Client:** `LIST_LOBBY lobby1 (2/4):lobby2 (1/4);lobby3 (4/4)`

### LOBBY_INFO
- **Richtung:** Server -> Client
- **Beschreibung:** Sendet den aktuellen Zustand einer Lobby an ihre Spieler.
- **Payload:** `<LobbyId> <Player1> <Player2> ...`
- **Beispiel:** `LOBBY_INFO lobby1 Player1 Player2 Player3`

### START
- **Richtung:** Client -> Server
- **Beschreibung:** Startet das Spiel in der aktuellen Lobby. Der Befehl darf nur vom Host gesendet werden und die Lobby muss genau vier Spieler enthalten.
- **Payload:** Keiner.
- **Beispiel:** `START`

### GAME_SETTINGS
- **Richtung:** Client -> Server
- **Beschreibung:** Sendet die Spielregeln der aktuellen Lobby an den Server. Der Befehl darf nur vom Host gesendet werden und wird vor `START` verwendet. Der Server speichert diese Werte in der Lobby und verwendet sie beim Spielstart.
- **Payload:** `<totalRounds> <roundDurationMillis> <playerRadius> <moveSpeedPerSecond> <humanPointsPerSecond> <humanRoundWinBonus> <phantomCatchBonus> <humanCatchBonus> <humanAbilitys> <phantomRoundWinBonus>`
- **Beispiele:**
  - **Client -> Server:** `GAME_SETTINGS 4 50000 6.0 100.0 1 50 10 10 3 10`
  - **Server-Antwort (Erfolg):** `CLEARED Game settings updated.`
  - **Server-Antwort (Fehler):** `REJECT Only the lobby host can change game settings.`

### GAME_START
- **Richtung:** Server -> Client
- **Beschreibung:** Signalisiert den Start eines Spiels und den Wechsel in die Spielszene.
- **Payload:** Keiner.
- **Beispiel:** `GAME_START`

### INPUT
- **Richtung:** Client -> Server
- **Beschreibung:** Sendet den aktuellen Bewegungsinput eines Spielers.
- **Payload:** `<vertical> <horizontal>` mit Werten aus `-1`, `0`, `1`.
- **Beispiel:** `INPUT -1 0`

### GSU (Game State Update)
- **Richtung:** Server -> Client
- **Beschreibung:** Sendet den aktuellen Spielzustand an alle Teilnehmer der Lobby.
- **Payload:** `<currentRound> <timeRemainingMs> <playerData> <abilityX> <abilityY> <abilityVisible>`
- **Format von `playerData`:** `<Name>:<Role>:<X>:<Y>:<Score>;<Name>:<Role>:<X>:<Y>:<Score>;...`
- **Beispiel:** `GSU 1 24987 Alice:HUMAN:10.50:20.25:12;Bob:PHANTOM:30.00:15.75:8;Charlie:PHANTOM:18.00:22.50:8;Dana:PHANTOM:11.20:40.10:8 512.0 384.0 true`

### ABILITY
- **Richtung:** Client <-> Server
- **Beschreibung:** Der Client sendet `ABILITY` ohne Payload, um seine Fähigkeit zu aktivieren. Der Server informiert anschließend alle Teilnehmer über Start und Ende des Effekts.
- **Payload:** Clientseitig keiner, serverseitig `START` oder `END`.
- **Beispiele:**
  - **Client -> Server:** `ABILITY`
  - **Server -> Lobby:** `ABILITY START`
  - **Server -> Lobby:** `ABILITY END`

### WISDOM
- **Richtung:** Client <-> Server
- **Beschreibung:** Öffnet, beansprucht oder bricht den täglichen Wisdom-Bonus ab. Der Server antwortet mit dem aktuellen Status des Bonus.
- **Payload:** Clientseitig `START`, `CLAIM` oder `CANCEL`; serverseitig `STARTED`, `ACTIVE`, `CLAIMED`, `CANCELED` oder `TOO_EARLY <remainingSeconds>`.
- **Beispiele:**
  - **Client -> Server:** `WISDOM START`
  - **Server -> Client:** `WISDOM STARTED`
  - **Client -> Server:** `WISDOM CLAIM`
  - **Server -> Client:** `WISDOM CLAIMED`
  - **Server -> Client:** `WISDOM TOO_EARLY 8`

### GAME_FINISH
- **Richtung:** Client <-> Server
- **Beschreibung:** Der Server signalisiert damit das Ende eines Matches. Der Client kann denselben Befehl später erneut senden, um eine bereits beendete Lobby zurück in den Lobby-Zustand zu versetzen.
- **Payload:** Keiner.
- **Beispiele:**
  - **Server -> Client:** `GAME_FINISH`
  - **Client -> Server:** `GAME_FINISH`

### SHOW_HIGHSCORE
- **Richtung:** Client <-> Server
- **Beschreibung:** Der Client fordert die Highscore-Liste an. Der Server antwortet mit bis zu zehn Einträgen.
- **Payload:** Serverseitig `1. <Name>: <Score>|2. <Name>: <Score>|...`
- **Beispiel:**
  - **Client -> Server:** `SHOW_HIGHSCORE`
  - **Server -> Client:** `SHOW_HIGHSCORE 1. Alice: 42|2. Bob: 35|3. Charlie: 18|`
