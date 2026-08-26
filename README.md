# LogicaMate v1.0.0

**El Templo de los Patrones** — una aventura de exploración lógica para niños y niñas de 8 a 12 años.

Android nativo · Kotlin · Jetpack Compose · Material 3 · Room · MVVM. 100% offline, sin cuentas, sin anuncios, sin datos personales.

## ⚠️ Estado de compilación

**No verificado.** Este proyecto se generó en un entorno sin Android SDK, sin compilador de Kotlin y sin acceso de red. Ningún `./gradlew` se ejecutó realmente. Ver [`docs/BUILD_REPORT.md`](docs/BUILD_REPORT.md) para el detalle completo y honesto de qué se verificó de verdad (el contenido semilla y el esquema SQL sí se ejecutaron y verificaron; la compilación Android, no).

## Qué es

10 cámaras, cada una con su propia mecánica de manipulación (arrastrar, ordenar, clasificar, construir, investigar — nunca solo opción múltiple): Entrada, Galería de Patrones, Pasadizo de Secuencias, Sala de Analogías, Sala de Clasificación, Sala de Matrices, Sala de Relaciones, Sala de Deducción, Taller Constructor, y la Cámara Maestra (mezcla de todas, desbloqueada al completarlas).

135 desafíos semilla, generados y auto-verificados matemáticamente uno a uno (`tools/generate_seed_content.py`). Progresión real mediante la **Llave Lógica** (8 fragmentos), una colección de 24 **Tesoros de la Lógica**, 8 insignias, XP, nivel y racha diaria — todo derivado de acciones reales, nunca de tiempo transcurrido o apertura de la app.

Ver [`docs/MEMORIA_DESCRIPTIVA.md`](docs/MEMORIA_DESCRIPTIVA.md) para el detalle completo del diseño pedagógico y de producto.

## Estructura del repositorio

```
app/                    Módulo Android (Kotlin + Compose)
  src/main/              domain/ (motores puros) · data/ (Room) · seed/ (contenido) · ui/ (Compose)
  src/test/               134 tests JVM (JUnit4 + Robolectric)
  src/androidTest/        1 test instrumentado (Compose UI)
database/                schema.sql + sample_data.sql (verificados contra SQLite real)
docs/                    Memoria, manuales, base de datos, reporte de compilación (+ PDFs en docs/pdf/)
tools/                    generate_seed_content.py, generate_vector_assets.py
.github/workflows/       build.yml (CI: tests + lint + APK al hacer push)
```

## Compilar

```bash
git clone <este-repositorio>
cd LogicaMate
./gradlew clean
./gradlew testDebugUnitTest
./gradlew lintDebug
./gradlew assembleDebug
```

Requiere Android Studio (o Gradle 8.7 + JDK 17 + Android SDK con `compileSdk 34`) instalado localmente — el `gradle-wrapper.jar` no está incluido en este ZIP (ver `docs/BUILD_REPORT.md`); ejecuta `gradle wrapper --gradle-version 8.7` una vez con un Gradle local, o usa el workflow de GitHub Actions incluido, que no depende de él.

## Documentación

- [`docs/MEMORIA_DESCRIPTIVA.md`](docs/MEMORIA_DESCRIPTIVA.md) — qué es, diseño, contenido, simplificaciones documentadas.
- [`docs/MANUAL_USUARIO.md`](docs/MANUAL_USUARIO.md) — cómo jugar.
- [`docs/MANUAL_TECNICO.md`](docs/MANUAL_TECNICO.md) — arquitectura, motores, base de datos, pruebas.
- [`docs/BASE_DE_DATOS.md`](docs/BASE_DE_DATOS.md) — esquema, diagrama ER.
- [`docs/BUILD_REPORT.md`](docs/BUILD_REPORT.md) — estado real de compilación y pruebas.
