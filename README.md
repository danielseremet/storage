 În baza diagramei din figura 1 elaborați o aplicație ce gestionează un serviciu de stocare a fișierelor.
Cerințe:
1. Puplicați codul sursă pe https://github.com/
2. Un utilizator poate avea mai multe roluri. Utilizatorul cu rolul Administrator va avea acces la toate
funcționalitățile aplicației. Utilizatorul cu rolul User va avea acces la funcționalitățile de gestiune a
fișierilor personale.
3. Elaborați funcționalitatea de expediere a notificării pe mail la înregistrarea utilizatorului
(Recomandat gmail);
4. Elaborați funcționalitatea de solicitare a spațiului stocare a fișierelor. După înregistrarea
utilizatorului și confirmarea înregistrării, utilizatorul se va autentifica și va solicita spațiul de stocare
a fișierelor. După aprobare de către utilizatorul cu rolul de Administrator, utilizatorul autentificat va
avea acces la funcționalitățile de gestiune a fișierilor personale. La aprobare utilizatorul va fi
notificat pe mail cu un mesaj precum că spațiul solicitat este disponibil pentru a fi utilizat. Prin
aprobare se va înțelege schimbarea statutului “activated” din False în True. Utilizatorul autentificat
va putea încărca fișiere doar în limita spațiului solicitat. În cazul cînd spațiul solicitat a fost epuizat,
utilizatorul va primi un mesaj de alertă. Aplicația trebuie să notifice utilizatorii pe mail cu un mesaj
despre statutul spațiului. Mesajul de notificare trebuie să conțină informația despre spațiul utilizat
și spațiul rămas. La schimbarea statutului “activated” din True în False, utilizatorul trebuie să fie
notificat, precum că spațiul rezervat a fost blocat. După blocarea spațiului rezervat, funcționalitățile
de gestiune a fișierilor personale nu vor fi disponibile.
5. Prin mecanismul de gestiune a fișierilor personale se subînțelege elaborarea următoarelor
funcționalități:
• Încărcare fișier
• Descărcare fișier
• Vizualizare metadate
• Ștergere fișier
6. Fișierele trebuie să fie stocate pe severul de fișiere minio, în baza de date se va stoca doar calea
spre fișier.
7. Aplicația trebuie sa fie înpachetată în docker container și incărcată pe https://hub.docker.com/.
8. De creat un fișier docker-compose.yml cu toate congurările necesare pentru a testa funcționalitățile
aplicației.

Tehnologii:
• PostgreSql
• Minio
• Spring Boot, Spring Framework, Spring MVC, Spring Data, Spring Security
• Liquibase
• Maven, Git, Docker
