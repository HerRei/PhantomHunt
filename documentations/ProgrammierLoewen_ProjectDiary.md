### Projekt Tagebuch ###



## 2026-02-20 — Hermes ##
**Dauer:** ~6h
**Ziel:** Einrichten einer Einheitlichen Arbeitsumgebung
**Was gemacht:**
- IntelliJ IDEA eingerichtet:
    - JDK installiert/ausgewählt (Java 25)
    - Style Check und Plugins dazu festgelegt
    - IntelliJ kennengelernt

- YouTrack aufgesetzt:
    - Neue YouTrack Cloud Instanz erstellt für unser Team
    - Projekt in unserer Instanz angelegt und Team gebaut
    - Kanban Board erstellt und gantt angefangen zu bauen für die Issues

- Erste Issuel angelegt: Setup, Dokumentation, Präsentation
- GitHub eingerichtet:
- Zugriff aufs UniBasel-Repo geprüft

**Nächstes**
- Konzept und Präsentation schreiben
- Erster Code schreiben (sinnvoll)


## 026-02-21 — Team-Sitzung ##
**Dauer:** ~2h

**Ziel:** Projektinitialisierung, Rollenverteilung und Konzept-Finalisierung
**Was gemacht**
- Wissensaustausch & Tool-Einführung:
    - Review der Vorarbeiten von Hermes (Stand 20.02.)
    - Einführung in die YouTrack-Umgebung und Festlegung der Arbeitsweise im Team
- Projekt-Definition:
    - Finalisierung des Spielkonzepts (Ziele, Mechaniken, Umfang)
- Festlegung des Spielnamens: ‘Phantom Hunt’ und des Gruppennamens: ‘ProgrammierLöwen’
- Organisation:
    - Besprechung und Gliederung der anstehenden Aufgaben
    - Zuteilung der Arbeitspakete an die Teammitglieder
**Probleme / Schwierigkeiten**
- **Erstellung des Projektplans:** Die geforderte Detailtiefe ist herausfordernd, da der Plan eine verbindliche Arbeitsgrundlage darstellt.
- **Mangelnde Erfahrungswerte:** Da das Team zum ersten Mal in dieser Konstellation zusammenarbeitet, fällt die Einschätzung vom Zeitaufwand und potenziellen technischen Hürden schwer.

**Nächstes**
Individuelle Vertiefung: Jeder arbeitet an seiner zugeteilten Aufgabe und verschafft sich einen detaillierten Überblick über das Gesamtprojekt.
Technische Integration: Einarbeitung in das Versionsverwaltungsprogramm Git.


## 26-02-23 — Jan ##
**Dauer:** ~45min
**Rapportstruktur festgelegt:**
- Vorlage für die wöchentlichen Berichte erstellt, um den Fortschritt und die investierten Stunden pro Teammitglied sauber zu dokumentieren.


## 2026-02-25 — Silas und Ismail ##
**Dauer:** ~3h
**Ziel:** Detaillierte Projektplanung und Zeitmanagement
**Was gemacht:**
- Projekt-Timeline erstellt:
    - Festlegung der internen Deadlines basierend auf den offiziellen Meilensteinen (MS1 bis MS6).
    - Einplanung von Pufferzeiten vor den Abgaben, um auf unvorhergesehene technische Hürden reagieren zu können.
- Gantt-Chart finalisiert:
    - Visualisierung der Arbeitspakete in YouTrack.
    - Abhängigkeiten zwischen den Aufgaben definiert (z. B. "Server-Basis-Logik" muss vor "Client-Anbindung" stehen).
    - Zuweisung der Verantwortlichkeiten: Wer arbeitet an der Spielmechanik, wer am Netzwerk-Layer?


## 2026-04-03 — Team-Sitzung ##
**Dauer:** ~4h
**Ziel:** Vorbereitung der ersten Präsentation und Strukturierung der Dokumentation
**Was gemacht:**
- Präsentation (MS1) vorbereitet:
    - Erstellung der Folien für die Projektvorstellung.
    - Inhaltlicher Fokus: Spielidee "Phantom Hunt", Architekturdiagramm (Client/Server-Trennung) und Risikomanagement.
    - Vorbereitung einer kurzen Demo-Skizze (Mockups), wie das UI in JavaFX später aussehen könnte.


## 2026-03-04 - Team-Sitzung ##
**Dauer:** ~2h
**Ziel:** Finalisierung und Besrechung der individeuell erarbeiteten Dokumente. Pushen aller finalisierten Dokumente.
**Was gemacht:**
- Beschrechung und Fortschritt breefing:
    - Jede Person Präsentiert kurz, kanpp und klar deren Fortschritte.
    - Falls Niemand Anmerkungen/Verbesserungsvorschläge hatte, wurden diese Dokumente commitet.
- Verbesserung der Präsentation:
    - Slide für Slide durchgesprochen und angepasst
**Probleme / Schwierigkeiten**
- **Visualisierung des Gantt-Charts:** Das Gantt-Chart sinnvoll zu visualisieren, hat uns Probleme bereitet.
- **Minimizierung der Aufgaben Ueberschnitte:** Viele aufgaben die wir uns verteilt haben, hatten in gewissen bereichen Abhaengigkeiten voneinander. So gab es Aufgaben, die sich ueberschneiden.


## 2026-03-09 - Ismail ## 
**Dauer:** ~2h
**Ziel:** Pipeline mit GitLab CI/CD zum bauen, testen & deployen des Java-Projekts mit Gradle.
**Was gemacht:** 
- Pipeline-Basis: .gitlab-ci.yml erstellt, damit das Projekt bei jedem Push automatisch gebaut wird.
- Image: Die Pipeline läuft in einem Container mit Java 25 auf einem kleinen Alpine-Linux.


## 2026-03-09 - Silas ## 
**Dauer:** ~5h
**Ziel:** Ping Pong, sowie Client-Seite implementieren.
**Was gemacht:**
- Client-Seite: ClientTcp.java und ServerHandler.java hinzugefügt welches ermöglicht zu Server verbinden, Packete zu empfangen, sie zu handhaben und Packete zu verschicken.
- Ping Pong: Server pingt clients jede 15 Sekunden an und kickt sie falls sie kein Pong zurücksenden.

## 2026-03-12 - Silas ## 
**Dauer:** ~4h
**Ziel:** Main Methode implementieren.
**Was gemacht:**
- Infos sammeln: Ich habe geschaut wie man am besten eine Main Methode strukturiert, da ich es zuerst falsch gemacht habe.
- Main: Main.java startet den Server oder erstellt Client, wie in den Anforderungen beschrieben.

## 2026-03-13 — Ismail ##
**Dauer:** ~2.5h
**Ziel:** Dokumentation des Netzwerkprotokolls
**Was gemacht:**
- Dokumentation des Protokolls.
- Befehle strukturiert aufgelistet und ihre Funktion erklärt.
- Beispiele zu den Befehlen hinzugefügt.

## 2026-03-09 & 2026-03-13 — Team-Sitzung ##
**Dauer:** ~2h
**Ziel:** Vorbereitung und Kontrolle für die Abgabe von Meilenstein 2
**Was gemacht:**
- Gemeinsame Besprechung des aktuellen Entwicklungsstands im Hinblick auf die MS2-Abgabe.
- Check-up aller Projektanforderungen für Meilenstein 2
- Identifikation noch fehlender oder unvollständig umgesetzter Punkte.
- Die verbleibenden Aufgaben wurden auf die Teammitglieder aufgeteilt, um die Deadline einzuhalten.

## 2026-03-11 - 2026-03-14 - Jan ##
**Dauer:** ~12h
**Ziel:** Erstellen einer ersten UI, Verbindung aufbauen zwischen UI und Server/ Frontend&Backend, UI - Nickname Eingaben, UI - GlobalChat, UI - Wisper Function, UI Change Nickname, UI Nickname vorschlag und korrekte verwendung der Nicknames.
**Was gemacht:**
- Erstellung unteschiedlicher Scens
- Erstellen der Logic und handhabung hinter den Eingaben
- Sicherstellen und Aufbau, der kommunikation zwischen CLient, server und UI
- Einfügen eines Global Chats
- Einfügen eines Wisper Chats
- Akutallisieren der Frontend Java Docs

## 2026-03-27 - Einführung von Vera in Team
**Dauer:** ~1h
**Ziele:** Einführen Vera in YouTrack, Code, Aufgabenverteilung
**Was gemacht:**
- Vera als neues Mitglied in die YouTrack Umgebung eingeführt
    In YouTrack:
    - Einführung in Agileboard
    - Einführung in Gantt
- Kurzes Brefing über Code/Repo Struktur gegeben
- Erneute Besprechung der momentanen Aufgabenverteilung und neuverteilung der Aufgaben

## 2026-05-15 — Hermes ##
**Dauer:** ~6h
**Ziel:** Erweiterung der Server-Lobby-Funktionalität.
**Was gemacht:**
- **Sound-Engine:** Das Implementieren einer SoundEngine mit hilfe eines Youtube Tutorials, bei dem mit JLWGL und OpenAL gearbeitet wird.
- **Whitespace in Usernames:** Die Logik zur Verarbeitung von Username um Mehrdeutikeiten in Whisper und MKL zu beheben.
- **Lobby-Management:** Es wurden bei Lobbies ab jetzt festgehalten, in welchem Zustand sie sind und man kann diese Jeweils abfragen. Dass sollte vorallem die Arbeit im frontend später erleichtern.
- **Zuschauer-Modus implementiert:** Mit spec kann man einer Lobby als Spectator beitreten. Die spectators werden ausserhalb der Spieler getrackt und auch schön in die Lobbys aufgenommen und dann wieder Rausgenommen umd die Datenstruktur konsisten zu halten.
    
**Nächstes:**
- Die GUI and diese Funktionen anbinden.
- Weitere Tests die danne erggeben, ob die Mehrdeutigkeit wirklich behoben wurde. evt. erstellen von Unit-tests und Äquivalenzklassenf um all das auf nicht Ambiguative Art zu testen. 

## 2026-03-28 - Ismail ##
**Dauer:** ~0.5h
**Ziel:** Build-Prozess anpassen.
**Was gemacht:**
build.gradle aktualisiert, um nur noch ein einziges JAR-File anstelle von separaten Dateien zu bauen.


## 2026-03-29 — Vera ##
**Dauer:** ~6h
**Ziel:** Einarbeitung in die bestehende Code-Basis.
**Was gemacht:**
- **Code-Review:** Den Ablauf zwischen Netzwerk-Logik und der Benutzeroberfläche Schritt für Schritt nachvollzogen.
- **Protokoll-Check:** Verstanden, wie die Packet-Klasse Daten in Strings umwandelt und wie die Command-Enums für die Kommunikation genutzt werden.
- **Projekt-Setup:** Die lokale Entwicklungsumgebung eingerichtet. 

**Nächstes:**
- Konkrete Schwachstellen in der Fehlerbehandlung und beim Threadin gim Client beheben.


## 2026-03-28 - 2026-03-29 - Jan ##
**Dauer:** ~1d 
**Ziel:** GameState soweit vorzubereiten, dass das Frontend begonnen werden kann.
**Was gemacht:**
- **Planung-GameState:** Planung, Recherche und skzzieren des geplanten und GameState. 
- **Round-Management:** Implementierung der Klassen des GameState verantwortlich für (Rundenwechsel, Countdoun, Rollenwechsel, ...).
- **Score-Management:** Implementierung der Klassen des GameState zusätzlich verantowrtlich für (verteilung der Punkte)
- **Player-Management:** Implementierung der Klassen des GameState zusätzlich verantwortlich für das PlayerHandling (Authority for 1 Match: Roles, Positions, Speed, State, Score)

**Nächstes:**
- Da die Implementierung der Klasse sehr "sloppy" ausgefallen ist, muss die Klasse GameState nochmals aufgeräumt werden. Womögliche unterteilung der Klasse in viele Unterklassen.
- Dennoch kann nun mit der Implementierung des Frontends gestarted werden

**Nächstes:**

## 2026-04-01 — Vera ##
**Dauer:** ~4h
**Ziel:** Refactoring der Core-Klassen und Stabilisierung der GUI.
**Was gemacht:**
- **Sichere Datenstrukturen:** Die Packet-Klasse so umgebaut, dass sie bei fehlenden Argumenten eine leere Liste statt null speichert. Das verhindert spätere Abstürze beim Zugriff auf Daten.
- **Thread-Fixes:** Den ServerHandler und die HubScene so angepasst, dass Netzwerk-Updates die GUI nicht mehr zum Abstürzen bringen (Platform.runLater). Den Schreibzugriff auf den Socket durch synchronized abgesichert.
- **Logik-Korrekturen:** Einen kritischen Fehler beim Decodieren von Nachrichten behoben (richtiges Exception-Handling). Ausserdem die Start-Szene korrigiert, damit man im Hub landet.
- **Chat-Erweiterung:** Das Flüstern-System in der GUI gefixt, indem ich ein dynamisches Eingabefeld für den Empfänger eingebaut habe

**Nächstes:**
- Die neuen Funktionen für den Zuschauer-Modus (Spectator) an die GUI anbinden.
- Prüfen, ob die Logik für Leerzeichen in Usernamen bei allen Befehlen (Whisper, MKL) einwandfrei funktioniert.

## 2026-04-05 - Ismail ##
**dauer:** ~11h
**Ziel:** Spielkarte implementieren und anzeigen.
**Was gemacht:**
- Die Blöcke für die Map gezeichnet und diese in eine Map angeordnet.
- Kollisionskarte gezeichnet.
- GameScene implementiert, um die Karte visuell anzuzeigen.
- Map-Loading im GameModel zentralisiert (lädt ab sofort die visuelle Karte und die Kollisionskarte).
- isWalkable()-Methode für zukünftige Bewegungsabfragen via Kollisionskarte in der GameScene bereitgestellt.
- Skalierung der Karte hinzugefügt, damit sie sich an die Bildschirmgröße der User anpasst.

## 2026-04-08 bis 2026-04-09 — Vera ##
**Dauer:** ~7h
**Ziel:** Grundlage für das QA-Konzept erarbeiten und erste Ausformulierung erstellen.
**Was gemacht:**
- Mind Map erstellt, um alle wichtigen Aspekte des QA-Konzepts zu sammeln und zu strukturieren.
- Inhalte in ein schriftliches Konzept überführt.
- Intensive Auseinandersetzung mit den einzelnen Themen, um ein besseres Verständnis zu entwickeln.
- Erste Struktur und Formulierungen ausgearbeitet, sodass das Konzept nachvollziehbar aufgebaut ist.

**Nächstes:**
- Das Konzept weiter vertiefen und zusätzliche Aspekte ergänzen.

## 2026-04-10 - Ismail ##
**Dauer:** ~10h
**Ziel:**
- Serverseitiges Laden der Karte vorbereiten.
- Lobby-System für Client und Server implementieren.
**Was gemacht:**
- Utility-Klasse MapLoader hinzugefügt, um das Map-Bild einzulesen.
- Konvertierung des Map-Bildes in ein 2D-Array auf der Serverseite umgesetzt.
- Funktionierendes Lobby-System erstellt (Lobbys erstellen, beitreten, Teilnehmer-Updates anzeigen).
- LOBBY_INFO Command in Command.java hinzugefügt, damit der Server Lobby-Updates broadcasten kann.
- Neue JavaFX Szene LobbyScene als Warteraum erstellt (Start-Button ist nur für den Host sichtbar).
- CreateLobbyScene (sendet Command.MKL) und JoinLobbyScene (sendet Command.CHECKIN) angebunden.
- broadcastLobbyInfo() in Lobby.java und LobbyHandler integriert, um alle Clients bei Änderungen zu informieren.
- handleLobbyInfo im ServerHandler implementiert, um eingehende Updates auf Client-Seite zu verarbeiten.

## 2026-04-11 — Vera ##
**Dauer:** ~2.5h
**Ziel:** QA-Konzept erweitern (Advanced-Inhalte).
**Was gemacht:**
- Fortgeschrittene Themen in das Konzept integriert und bestehende Inhalte ergänzt.
- Einzelne Abschnitte präzisiert und weiter ausgearbeitet.

**Nächstes:**
- Gesamtes Konzept nochmals kritisch durchgehen und überarbeiten.

## 2026-04-12 — Vera ##
**Dauer:** ~3.5h
**Ziel:** Überarbeitung des Konzepts und Javadoc Anpassungen.
**Was gemacht:**
- Das komplette QA-Konzept überarbeitet und verständlicher formuliert.
- Struktur verbessert, damit der Aufbau logischer ist.
- Javadoc überarbeitet und allgemeines Housekeeping im Code durchgeführt (Kommentare verbessert, kleinere Unsauberkeiten behoben).

## 2026-04-13 — Silas & Hermes ##
**Dauer:** ~14h(pro Person)
**Ziel:** GUI implementierung und erstellen prototyps
**Was gemacht:**
- Alle Schnittstellen wurden vereint und ein funktionierender prototyp erstellt.
- Gui Wurde komplett neu erstellt so das man Spiel versteht.

## 2026-04-16 — Silas ##
**Dauer:** ~5h
**Ziel:** Rebuilden und aufräumen von Serverseite und Collision-system.
**Was gemacht:**
- Konzept von wie das movement und Kollisionen funktionieren sollte.


## 2026-04-16 — Silas ##
**Dauer:** ~5h
**Ziel:** Rebuilden und aufräumen von Serverseite und Collision-system.
**Was gemacht:**
- Konzept von wie das movement und Kollisionen funktionieren sollte.

## 2026-04-19 bis 2026-04-26 — Vera ##
**Dauer:** ~25h
**Ziel:** Erweiterung und Verbesserung der Unit Tests.
**Was gemacht:**
- Umfangreiche Unit Tests implementiert und bestehende Tests erweitert.
- Testabdeckung gezielt verbessert und zentrale Komponenten systematisch getestet.
- Struktur und Organisation der Tests überarbeitet.

**Nächstes:**
- Weitere Unit Tests implementieren, um die Abdeckung zu erhöhen.
- QA-Dokumentation vervollständigen und präzisieren.


## 2026-04-19 — Silas ##
**Dauer:** ~4h
**Ziel:** Pacman-movement implementieren und Spawnpunkte random machen.
**Was gemacht:**
- Ich konnte das Ziel mit Schwierigkeiten umsetzen.

## 2026-04-20 — Silas ##
**Dauer:** ~3h
**Ziel:** Ability implementieren
**Was gemacht:**
- Die Ability funktioniert jetzt über "Q" und Geister spawnen beim Fangen an zufälliger Stelle.

## 2026-04-21 — Silas ##
**Dauer:** ~4h
**Ziele:** 
- Lobby sauber verlassen/schliessen nach Spielende. 
- Gewinner muss angezeigt werden im GUI.

**Was gemacht:**
- Ich konnte eine Funktion implementieren welche die Lobby mehr oder weniger sauber schliesst jedoch immer noch nicht komplett sauber.
- Gewinner wird in der Endscene richtig angezeigt

## 2026-04-24 — Silas ##
**Dauer:** ~1h
**Ziel:** Man kann den Highscore im GUI anschauen.
**Was gemacht:**
- Ich konnte das Ziel umsetzen.

## 2026-04-21 - Alle ##
**Dauer:** ~1h
**Ziel:** Debrefing für MS4 & Aufgabenverteilung MS5
**Was gemacht:**
- Wir haben die Resultate des MS4 besprochen. Welche Prozesse gut funktioneirt haben und welche noch zu verbessern sind.
- Wir haben die Aufgaben für den MS5 verteilt.

## 2026-04-19 bis 2026-04-26 — Vera ##
**Dauer:** ~25h
**Ziel:** Erweiterung und Verbesserung der Unit Tests.
**Was gemacht:**
- Umfangreiche Unit Tests implementiert und bestehende Tests erweitert.
- Testabdeckung gezielt verbessert und zentrale Komponenten systematisch getestet.
- Struktur und Organisation der Tests überarbeitet.

**Nächstes:**
- Weitere Unit Tests implementieren, um die Abdeckung zu erhöhen.
- QA-Dokumentation vervollständigen und präzisieren.



