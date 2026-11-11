# NFC Timer

Appli compagnon de [Simple Time Tracker](https://github.com/Razeeman/Android-SimpleTimeTracker) (STT).
STT garde tout l'historique ; cette appli lui envoie seulement les ordres « tout arrêter » et « démarrer tel travail ».

## Dans le camion

Plaque le téléphone (déverrouillé) sur le tag :

- **Tu le laisses** : vibration à 3 s, 5 s et 7 s. Retire le téléphone après la vibration voulue.
  3 s = 1er travail de la liste, 5 s = 2e, 7 s = 3e.
- **Tu le retires tout de suite** : l'écran affiche tous les travaux, plus « Tout arrêter ».

Ensuite, choix du client (ou « Sans client »). Le travail en cours s'arrête et le nouveau démarre dans STT.

## Installation

1. Dans STT : *Réglages > Exporter en CSV*, sur toute la période.
2. Dans NFC Timer : **Importer l'export CSV**. Les travaux (activités STT) et les clients (étiquettes STT)
   sont remplis, les plus utilisés en premier. Range la liste des travaux comme tu veux : les 3 premières lignes
   sont celles de l'appui long.
3. **Écrire le tag NFC**, puis pose le téléphone sur un tag vierge (NTAG213/215). Colle le tag dans le camion.

Les noms doivent être identiques à ceux de STT, ce qui est le cas après un import.

## Compilation

`./gradlew assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`.
GitHub Actions compile le même APK à chaque push (artefact `nfc-timer-apk`),
et le paquet Play Store signé (artefact `nfc-timer-play-store-aab`) dès que les secrets ci-dessous existent.

## Publier sur Google Play

1. **Secrets GitHub** (*Settings > Secrets and variables > Actions > New repository secret*) :
   `NFC_TIMER_KEYSTORE_BASE64` et `NFC_TIMER_KEYSTORE_PASSWORD`, contenus des deux fichiers `.txt` fournis avec la clé.
   Garde `nfc-timer-upload.jks` et son mot de passe en lieu sûr (hors GitHub) : c'est la clé d'importation.
2. *Actions > NFC Timer > Run workflow*, puis télécharger l'artefact `nfc-timer-play-store-aab` (`app-release.aab`).
3. Play Console > **Créer une application** : nom « NFC Timer », français, Application, Gratuite.
   Accepter la **signature des applications par Google Play** (proposée par défaut).
4. Remplir la fiche et le *Contenu de l'appli* avec `store/fiche-play-store.md`.
   Règles de confidentialité : lien vers `PRIVACY.md` sur GitHub.
5. **Test fermé** (obligatoire pour un compte personnel créé après nov. 2023) : importer l'`.aab`,
   inviter au moins **12 testeurs** qui gardent l'appli installée **14 jours d'affilée**.
   Ensuite seulement : *Tableau de bord > Demander l'accès à la production*.
6. Chaque nouvelle version : relancer le workflow, importer le nouvel `.aab` (le `versionCode` augmente tout seul).

Le `applicationId` `fr.pointage.nfc` ne doit plus jamais changer après la première publication.
