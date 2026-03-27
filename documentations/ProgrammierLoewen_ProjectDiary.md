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

## 2026-03-11 - 2026-03-14 - Implementierung GUI ##
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