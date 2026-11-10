# Quilate — Sistema de Ventas y Análisis de Clientes para Joyería

Aplicación de escritorio en **JavaFX** con persistencia mediante **Hibernate ORM (JPA)** y **PostgreSQL**.
Registra piezas por código de barras, ventas con control de stock, descuentos con límite de rentabilidad,
pedidos personalizados con cupo semanal y reportes de ventas y clientes.

## Tecnologías

| Herramienta | Versión |
|---|---|
| Java (JDK) | 21 |
| JavaFX | 21.0.5 |
| Hibernate ORM | 6.6.4.Final |
| PostgreSQL / driver JDBC | 15+ / 42.7.4 |
| Maven | 3.9 (incluido con `mvnw`) |
| IDE | IntelliJ IDEA |

## Estructura del proyecto

```
src/main/java/com/quilate/
├── Launcher.java        ← punto de entrada (ejecutar este)
├── MainApp.java         ← aplicación JavaFX
├── model/               ← entidades @Entity
├── dao/                 ← GenericDAO + un DAO por entidad (CRUD y consultas HQL)
├── service/             ← reglas de negocio y validaciones
├── controller/          ← controladores de las vistas FXML
├── exception/           ← ReglaNegocioException
└── util/                ← JPAUtil (EntityManagerFactory), AlertUtil
src/main/resources/
├── META-INF/persistence.xml
├── fxml/                ← vistas
└── css/styles.css
database/                ← scripts SQL de la base de datos
```

## Instalación

### 1. Preparar la base de datos

1. Instalar PostgreSQL y abrir pgAdmin o `psql`.
2. Crear la base de datos:
   ```sql
   CREATE DATABASE quilate;
   ```
3. Ejecutar el script de tablas de la carpeta `database/` sobre la base `quilate`.

### 2. Configurar la conexión (sin subir credenciales)

Copiar el archivo de ejemplo y poner **tus** datos locales:

```bash
cp db.properties.example db.properties
```

```properties
jakarta.persistence.jdbc.url=jdbc:postgresql://localhost:5432/quilate
jakarta.persistence.jdbc.user=postgres
jakarta.persistence.jdbc.password=tu_contraseña
hibernate.hbm2ddl.auto=validate
```

`db.properties` está en `.gitignore` y **nunca se sube a GitHub**.
También se pueden usar las variables de entorno `DB_URL`, `DB_USER` y `DB_PASSWORD`.

### 3. Ejecutar

**Desde IntelliJ IDEA:** `File → Open` → seleccionar la carpeta del proyecto (se importa como Maven) →
abrir `Launcher.java` → **Run**.

**Desde la terminal:**

```bash
./mvnw javafx:run
```

En Windows: `mvnw.cmd javafx:run`.

La barra inferior de la ventana indica si la conexión con PostgreSQL fue exitosa.

## Reparto del trabajo

| Integrante | Entidades | Extra |
|---|---|---|
| 1 | Categoria, Pieza, Cliente | Configuración de Hibernate y conexión, búsqueda por código de barras, validación de stock |
| 2 | Empleado, Venta, DetalleVenta | Consulta HQL 1 mostrada en la aplicación |
| 3 | Descuento, PedidoPersonalizado | Consulta HQL 2, README y `.gitignore` |

## Flujo de trabajo con Git

1. `main` siempre debe compilar. No se hace push directo a `main`.
2. Crear una rama por funcionalidad:
   ```bash
   git checkout main
   git pull
   git checkout -b feat/entidad-pieza
   ```
3. Commits pequeños y descriptivos: `feat: agregar entidad Pieza y su DAO`, `fix: validar stock negativo`.
4. Subir la rama y abrir un **Pull Request** hacia `main`.
5. Otro integrante revisa, comenta y aprueba antes de fusionar.
6. Después de cada merge: `git pull` en `main` y comprobar que compila y arranca.
