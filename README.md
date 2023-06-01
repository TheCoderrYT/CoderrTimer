# CoderrTimer
Ein Plugin, dass Stoppuhren für jeden Spieler einzeln oder auch anpassbare Gruppenstoppuhren bereitstellt.
## Installation
Klicke auf die `CoderrTimer.jar`-Datei und lade sie dir herunter. 
Anschließend die Datei in den Plugins-Ordner deines Spigot Servers ziehen und fertig.
## Bedienung
`/timer [start|stop|remove] [Spielername,Spielername|id] [Weltname]`<br>
Es kann für jeden Spieler nur eine Stoppuhr gleichzeitig laufen, weitere Stoppuhren müssen pausiert sein.
### Übersicht aller Stoppuhren
Durch den Command `/timer` ohne Argumente kann eine Übersicht aller Stoppuhren des Servers aufgerufen werden. 
Fettgedruckte Zeilen sagen aus, dass der angegebene Timer aktuell als aktiv markiert ist. 
### Stoppuhr für sich selbst
Durch den Command `/timer start` wird eine Stoppuhr gestartet, welche nur für dich selbst läuft. 
Durch `/timer stop` kann diese Stoppuhr gestoppt und durch `/timer remove` gelöscht werden. 
### Gruppenstoppuhren erstellen
Um einer Gruppe von Spieler einen Timer zu erstellen, benutze den Command `/timer start Spielername,Spielername`.
Die Spielernamen müssen durch Kommas getrennt sein, allerdings darf kein Leerzeichen zwischen den Namen stehen.
Die Spieler müssen zudem alle online sein und keine aktiven Stoppuhren besitzen.<br>
Um die Stoppuhr zu stoppen kann derselbe Command wie beim Starten verwendet werden, nur mit dem Parameter `stop`,
oder es wird die Id der Stoppuhr angegeben `/timer stop [id]`. Das Löschen einer Gruppenstoppuhr funktioniert wie das Stoppen.
### Weltenbezogene Stoppuhr
Um eine weltenbezogene Stoppuhr zu erstellen, kann der dritte Parameter verwendet werden. 
Als dritter Parameter kann der Name einer Welt angegeben werden, welche derzeit auf dem Server geladen ist. 
Es können sowohl Einzel-, als auch Gruppenstoppuhren weltenbezogen erstellt werden. 
Um eine weltenbezogene Stoppuhr für eine einzelne Person zu erstellen, 
muss der Spielername als zweites Argument angegeben werden, ohne jegliche Kommas. `/timer start [Spielername] [Weltname]`<br>
Bei weltenbezogenen Stoppuhren läuft die Zeit nur, wenn sich einer der Spieler in der hinterlegten Welt befindet, 
dennoch werden sie in der Übersicht als aktiv angezeigt. 