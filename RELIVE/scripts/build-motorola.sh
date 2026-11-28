#!/usr/bin/env bash
set -Eeuo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
cd "$ROOT"

echo "=== RELIVE — préparation et compilation Motorola ==="
echo "Projet : $ROOT"

if ! command -v java >/dev/null 2>&1; then
  echo "ERREUR : Java est introuvable. Installe un JDK 21 puis relance."
  exit 10
fi

JAVA_VERSION="$(java -version 2>&1 | awk -F '[".]' '/version/ {print $2; exit}')"
if [[ -z "$JAVA_VERSION" || "$JAVA_VERSION" -lt 21 ]]; then
  echo "ERREUR : ce projet compile en Java 21. JDK détecté : ${JAVA_VERSION:-inconnu}."
  exit 11
fi

if [[ -x "$ROOT/gradlew" ]]; then
  GRADLE="$ROOT/gradlew"
elif command -v gradle >/dev/null 2>&1; then
  echo "Wrapper absent : génération isolée de Gradle Wrapper 9.6.0..."
  TMP_WRAPPER_DIR="$(mktemp -d)"
  (
    cd "$TMP_WRAPPER_DIR"
    gradle wrapper --gradle-version 9.6.0
  )
  cp "$TMP_WRAPPER_DIR/gradlew" "$ROOT/gradlew"
  cp "$TMP_WRAPPER_DIR/gradlew.bat" "$ROOT/gradlew.bat"
  mkdir -p "$ROOT/gradle/wrapper"
  cp "$TMP_WRAPPER_DIR/gradle/wrapper/"* "$ROOT/gradle/wrapper/"
  rm -rf "$TMP_WRAPPER_DIR"
  chmod +x "$ROOT/gradlew"
  GRADLE="$ROOT/gradlew"
else
  echo "ERREUR : Gradle est introuvable et le dépôt ne contient pas encore son Wrapper."
  echo "Dans Termux, installe d'abord Gradle et JDK 21, puis relance ce script."
  exit 12
fi

echo
"$GRADLE" --version

echo
echo "=== Tests unitaires + APK debug ==="
"$GRADLE" --no-daemon :app:testDebugUnitTest :app:assembleDebug

APK="$ROOT/app/build/outputs/apk/debug/app-debug.apk"
if [[ ! -s "$APK" ]]; then
  echo "ERREUR : Gradle a terminé sans produire l'APK attendu."
  exit 13
fi

DEST="/storage/emulated/0/Download/RELIVE-MOTOROLA-debug.apk"
if mkdir -p "/storage/emulated/0/Download" 2>/dev/null && cp "$APK" "$DEST" 2>/dev/null; then
  echo
  echo "SUCCÈS : APK copié ici : $DEST"
else
  echo
  echo "SUCCÈS : APK généré ici : $APK"
  echo "Copie-le dans Download depuis ton gestionnaire de fichiers."
fi

echo "Taille APK : $(wc -c < "$APK") octets"
echo "Tests unitaires et compilation terminés."
