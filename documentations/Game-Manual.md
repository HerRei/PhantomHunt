# PhantomHunt – Offizielles Spielhandbuch
Willkommen bei PhantomHunt! Du stehst kurz davor, ein verfluchtes Schloss zu betreten. In diesem strategischen 2D-Multiplayer-Spiel ist Teamwork genauso wichtig wie der pure Überlebensinstinkt. Wirst du dem Wahnsinn entkommen oder die Eindringlinge als Geist in die Enge treiben?

## Das Spielprinzip (3 vs. 1)
PhantomHunt ist asymmetrisch. Das bedeutet: Die Teams sind nicht gleich gross und haben völlig unterschiedliche Aufgaben!
Es spielen immer 3 Phantome gegen 1 Menschen.

*Der Mensch:* Wurde im Schloss eingesperrt. Dein Ziel ist es, den Geistern so lange wie möglich auszuweichen. Jede Sekunde des Überlebens ist entscheidend!

*Die Geister:* Euer Schloss wurde betreten. Eure Aufgabe ist es, als Team zusammenzuarbeiten, den Menschen in die Enge zu treiben und ihn zu erschrecken (zu fangen).

**Runden und Rollentausch**
Damit das Spiel absolut fair bleibt, besteht ein komplettes Match immer aus genau 4 Runden.
Nach jeder Runde wechseln die Rollen automatisch. So ist sichergestellt, dass jeder der vier Spieler am Ende genau einmal den flüchtenden Menschen und dreimal einen jagenden Geist gespielt hat.

## Installation und Spielstart
Da PhantomHunt ein Multiplayer-Spiel ist, muss ein Spieler den Server hosten und alle Spieler als Clients beitreten.

**Vorbereitung (Für alle Spieler)**
Öffne ein Terminal im Hauptverzeichnis des Spiels (\Gruppe-2).

Kompiliere das Spiel, indem du folgenden Befehl eingibst:
*./gradlew jar*

Nach erfolgreichem Build findest du das fertige Spiel im Ordner build/libs/.

**Ein Spiel hosten (Server)**
Ein Spieler muss die Spielwelt verwalten. Öffne das Terminal und starte den Server mit Angabe eines Ports (z.B. 2222):
Gib diese Zeiele jetzt ein und drücke Enter.
*java -jar build/libs/phantom-hunt.jar server 2222*
Teile den anderen Spielern nun deine IP-Adresse und diesen Port mit. Die findest du unter Einstellungen > Netzwerk und Internet.

**Einem Server beitreten (Client)**
Um mitzuspielen, startest du das Spiel im Client-Modus und gibst die IP-Adresse sowie den Port des Servers an. Beispiel:
*java -jar build/libs/phantom-hunt.jar client 192.168.1.9:2222*

**Home-Menü**
Sobald du das Spiel über das Terminal gestartet hast, gelangst du in den Home-Screen.

*Dein Profil:* Als Erstes kannst du dir einen eigenen Spielernamen (Nickname) aussuchen. Dir wird auch standardmässig einer zugeteilt.

*Global Chat & Flüstern:* Tausche dich im globalen Chat mit allen Spielern auf dem Server aus. Willst du Taktiken geheim besprechen? Dann nutze die "Whisper"-Funktion, um private Nachrichten an einzelne Mitspieler zu senden.

*Lobby-System:* Um ein Match zu starten, müssen sich die Spieler in einer Lobby sammeln.

*Lobby erstellen:* Du kannst eine eigene Lobby eröffnen. Du bist dann der "Host" dieser Lobby. Oben auf deinem Bildschirm wird nun eine eindeutige Lobby-ID angezeigt. Teile diese ID mit deinen Mitspielern!

*Lobby beitreten:* Wenn ein Freund bereits eine Lobby erstellt hat, gib einfach seine Lobby-ID ein, um beizutreten.

*Spielstart:* Sobald sich genügend Spieler (insgesamt 4) in der Lobby eingefunden haben, kann der Host den Start-Button drücken und die erste Runde einläuten.

## Steuerung und Spielmechanik
Damit ihr im Schloss überlebt oder erfolgreich jagt, müsst ihr eure Figur sicher durch die Gänge manövrieren.

**Bewegung:**
Egal ob du als Mensch oder als Geist spielst, du steuerst deine Spielfigur mit den klassischen Bewegungstasten:

*W – Nach oben*

*A – Nach links*

*S – Nach unten*

*D – Nach rechts*

**Die Jagd (Fangen & Erschrecken):**
Es gibt keine spezielle Taste für einen Angriff. Die Geister fangen den Menschen durch geschicktes Positionieren:

Um den Menschen zu erschrecken, muss ein Geist auf genau dasselbe Feld (die gleiche Position) laufen, auf dem der Mensch gerade steht.

Sobald sich der Mensch und ein Geist auf derselben Stelle befinden, ist der Mensch gefangen! Die aktuelle Runde endet sofort zugunsten der Geister und die Punkte werden verteilt.

## Power-Up:
Jede Runde erscheint zufällig auf der Karte eine Fähigkeit. Dann wird der Spiess umgedreht!

**Sichtbare Verwundbarkeit (Glitch-Effekt):** 
Sobald der Mensch dieses Power-Up einsammelt, bleibt die Schwäche der Phantome nicht verborgen. Solange der Mensch die Fähigkeit aktiv hat, wird die Form der Geister instabil. Sie flackern hektisch zwischen ihrer normalen Gestalt und einer verzerrten, "geglitchten" Form hin und her.

**Geister verjagen:**
Wenn der Mensch auf dasselbe Feld wie ein Geist trifft, scheidet der Geist dadurch nicht aus, wird aber magisch an einer andere, weit entfernten Position im Schloss teleportiert und der Mensch erhält 10 Punkt.

## Punkte und Siegbedingungen
Am Ende der 4 Runden gewinnt nicht das Team, sondern der Einzelspieler mit den meisten Punkten. Punkte sammelst du wie folgt:

**Wenn du als Mensch spielst:**

Du erhältst kontinuierlich 1 Punkt für jede Sekunde, die du überlebst und nicht von den Geistern gefangen wirst.

Schaffst du es, die komplette Rundenzeit zu überleben, erhältst du einen massiven +50 Punkte-Bonus!

**Wenn du als Geist spielst:**

Ihr müsst euch absprechen! Ihr erhaltet je 10 Punkte, wenn ihr den Menschen erfolgreich aufspürt und fangt. Der Fänger erhält zusätzlich noch einen +10 Punkte-Bonus.

Wer am Ende der vierten Runde ganz oben auf dem Scoreboard steht, hat die Partie gewonnen!

![PhantomHunt Screenshot](./images/Phantom-Hunt_game.png)