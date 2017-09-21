# Referèndum 1-O

L'1 d'Octubre ja s'acosta i a causa de la censura i la persecució de drets democràtics fonamentals com són la llibertat d'expressió (tancament de pàgines web, requises de cartells...) i la llibertat de correspondència (vulnerant així la privacitat), un grup de professionals de les TIC ens hem vist empesos a crear una aplicació per tal de mantenir informada la població i garantir que tothom pugui exercir el seu dret a decidir.

Funcionalitats:

* Accedeix a tota la informació de Twitter sense necessitat de ser-ne usuari.
* Accedeix a la web oficial del referèndum sense fer servir servidors intermediaris ni VPN.
* Troba el teu col·legi electoral.
* Comparteix un reguitzell de materials gràfics (cartells, díptics...) a favor del dret a decidir.
* Mantén-te informat de qualsevol novetat a través de les notificacions de l'aplicació.

Aquesta aplicació és de codi obert per tal que tothom pugui replicar-la.

## Descàrrega

* Android: https://play.google.com/store/apps/details?id=com.referendum.uoctubre
* iOS: Pròximament estarà disponible.

## Compilació

Per a compilar l'aplicació, us caldrà el següent:
* Crear un compte de Firebase i copiar-ne el fitxer `google-services.json`.
* Canviar l'API key de Google Maps i Fabric a l'`AndroidManifest.xml`.
* Crear una aplicació de Twitter i copiar-ne les claus a `Constants.java`.
* Afegir els següents fitxers a Firebase Storage:
  * `hashtags.json`
  * `imatges.json`
* Canviar les regles a Firebase Storage:
    ```
    service firebase.storage {
      match /b/{bucket}/o {
        match /{allPaths=**} {
          allow read;
          allow write: if request.auth != null;
        }
      }
    }
    ```
Al directori `data` hi trobareu exemples del format de cadascun dels fitxers.

## Llicència

L'aplicació està llicenciada sota la llicència [Apache License 2.0](https://github.com/mosquitolabs/referendum_1o/blob/master/LICENSE).

Si desitges contribuir a millorar l'aplicació, envia'ns els "pull requests" que creguis convenients!