# Pointage NFC

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
2. Dans Pointage NFC : **Importer l'export CSV**. Les travaux (activités STT) et les clients (étiquettes STT)
   sont remplis, les plus utilisés en premier. Range la liste des travaux comme tu veux : les 3 premières lignes
   sont celles de l'appui long.
3. **Écrire le tag NFC**, puis pose le téléphone sur un tag vierge (NTAG213/215). Colle le tag dans le camion.

Les noms doivent être identiques à ceux de STT, ce qui est le cas après un import.

## Compilation

`./gradlew assembleDebug` → `app/build/outputs/apk/debug/app-debug.apk`.
GitHub Actions compile le même APK à chaque push (artefact `pointage-nfc-apk`).
