# Simple TCP/IP chatroom written in Java swing
Questo progetto implementa una semplice chatroom in Java, composta da un server e un'interfaccia grafica per il client.

## Dipendenze
**Java Development Kit (JDK) 8 o versioni successive**

## Server
Il server è responsabile della gestione delle connessioni dei client e della comunicazione all'interno dei gruppi, con la loro gestione e i comandi utilizzabili dal client.

## Utilizzo
Compilare il codice sorgente del server.

>*javac Server.java*

Avviare il server.

>*java Server*

Il server si metterà in ascolto sulla porta 5555 per le connessioni dei client.

## Funzionalità

1. Creazione di nuovi gruppi e unione a gruppi esistenti.
2. Comunicazione all'interno dei gruppi.
3. Cambio del nickname degli utenti.
4. Chiusura e gestione delle connessioni.

## Client
Il client fornisce un'interfaccia grafica per interagire con il server e partecipare alle chat e interagirci.

## Utilizzo
Compilare il codice sorgente del client.

>*javac Client.java*

Avviare l'applicazione client.

>*java Client*

Eseguire la propria scelta e successivamente inserire un username per entrare dentro ad un gruppo.

## Funzionalità

1. Connessione al server tramite socket TCP.
2. Invio e ricezione di messaggi testuali.
3. Creazione e unione a gruppi.
4. Visualizzazione dei gruppi attivi.
5. Cambio del nickname degli utenti.
6. Uscita dalla chat.


## Estensioni future
- Implementare funzionalità di autenticazione degli utenti.
- Aggiungere supporto per allegati e messaggi multimediali.
- Migliorare l'interfaccia utente per una gestione più intuitiva dei gruppi.
- Aggiungere funzionalità di crittografia per la sicurezza delle comunicazioni.