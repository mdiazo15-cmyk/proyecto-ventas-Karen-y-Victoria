# Sistema de Integración y Análisis de Datos de Ventas

Proyecto final de Programación en Java (UMG).
Integrantes: Karen Johana Miguel Ordoñez y María Victoria Díaz Orellana.

## Requisitos
- JDK 21 o superior
- Maven (incluido en NetBeans)
- PostgreSQL [18.3] en localhost
- Dependencias (las descarga Maven): Gson 2.10.1 y driver PostgreSQL 42.7.3

## Configuración de la base de datos
1. En pgAdmin, crear una base de datos vacía llamada `ventas_db`.
2. Copiar `config/db.properties.example` a `config/db.properties` y completar:
   ```
   db.url=jdbc:postgresql://localhost:5432/ventas_db
   db.user=postgres
   db.password=SU_CONTRASEÑA
   ```
3. Las tablas se crean solas al ejecutar el programa (script en `sql/schema.sql`).

## Compilación y ejecución
Desde la raíz del proyecto:
```
mvn clean compile
mvn exec:java
```
También se puede ejecutar con Run en NetBeans. El programa procesa todos los
`.csv`, `.json` y `.xml` de la carpeta `datos/` y genera en `salida/` los archivos
`reporte_analisis.txt` y `errores_procesamiento.txt`.

## Decisiones técnicas
- Los lectores entregan texto crudo y el validador lo convierte a `Venta` o lo rechaza,
  para que un dato malo no rompa la lectura.
- Se corrige (normaliza): espacios sobrantes, mayúsculas/minúsculas, y fecha `dd/MM/yyyy`.
  Se rechaza: campos vacíos, fechas inexistentes, cantidad o precio <= 0 o fuera de rango.
- Duplicados por `id_venta`, detectados entre todos los archivos; se acepta la primera
  aparición y las demás se registran como duplicado.
- Idempotencia: `id_venta` es clave primaria y el DAO consulta antes de insertar.
- Dinero con `BigDecimal`. Los cálculos de los indicadores los hace SQL en la base de datos.
- Un registro o archivo erróneo no detiene el proceso; los errores indican archivo y
  número de registro.
- Porcentaje de rechazo: el reporte muestra dos valores: solo inválidos, e
  inválidos + duplicados. El principal es (inválidos + duplicados) / recibidos,
  porque ambos son registros no aceptados.
