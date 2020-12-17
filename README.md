# HORECA-registration-system

## De volgorde voor het starten van het systeem staat hieronder beschreven:
1.  RegistrarGUI        (1 maal)
2.  MatchingServiceGUI  (1 maal)
3.  MixingProxyGUI      (1 maal)
4.  BarGUI              (x maal)
5.  CustomerGUI         (x maal)
6.  DoctorGUI           (1 maal)

## Bezoek brengen aan een bar (Mixing proxy al opgestart)
1. BarGUI starten, business nummer ingeven en op ENTER drukken
2. BarGUI: Mothley hashes knop drukken en vervolgens de bar openen knop drukken
3. U kopieert de datastring uit de onderste textfield
4. CustomerGUI starten
5. Datastring gaan plakken in het bovenste textField en op ENTER drukken
6. Vervolgens kan er een bezoek gebracht worden aan de CF door op de "bezoek bar" knop te drukken
7. Verlaat men de bar drukt men eerst op "verlaat bar"
8. Tot slot gaat men de Mixing Proxy gaan flushen door op de knop "Flush" te drukken in de MixingProxyGUI

## Bezoek brengen aan de dokter
1. DokterGUI opstarten, telefoonnr van de patient ingeven en op "get logs" drukken.
2. Men controleert of de Mixing Proxy geen capsules meer in zijn queue heeft. Zo ja drukt men op de flush knop.
3. De logs kunnen gecontroleerd worden vooraleer men ze naar de matching service stuurt op de knop "To matching"
