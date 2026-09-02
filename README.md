# PushUp 1v1 — v1.1

Application Android de duel de pompes en mode **hot-seat** : deux joueurs passent l'un après l'autre devant la caméra du même téléphone.

## Fonctionnalités
- 30 secondes par joueur.
- Comptage automatique par ML Kit Pose Detection.
- Détection basée sur l'angle du coude, avec lissage, seuils haut/bas et délai anti-double-comptage.
- Caméra frontale + analyse en arrière-plan pour garder l'interface fluide.
- Résultat, revanche et retour à l'accueil.
- Nettoyage correct de la caméra et du détecteur lors de la navigation.
- GitHub Actions compile automatiquement l'APK à chaque `push` sur `main`.

## Conseils de détection
Place le téléphone de façon à voir le corps entier, idéalement de profil. L'épaule, le coude et le poignet doivent rester visibles. Une pompe est validée lorsque le coude passe en position basse puis revient en position haute.

## Compiler
Avec Android Studio : ouvre le dossier `pushup1v1` et lance **Build > Build APK(s)**.

Avec GitHub : pousse le projet sur une branche `main`. Le workflow `.github/workflows/build.yml` utilise Gradle 8.7 et dépose `app-debug.apk` dans les artifacts du run.
