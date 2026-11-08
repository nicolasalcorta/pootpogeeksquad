# Trabajo Práctico Obligatorio - POO - GeekSquad

Proyecto del TPO de la segunda parte de la materia Programación Orientada a Objetos. Elegimos la **propuesta B: "Clínica Veterinaria"**. Aplicamos lo visto en la cursada: relaciones entre clases, interfaces, herencia, UML y los principios SOLID y GRASP.

### El grupo está conformado por:
- Alcorta, Nicolas Damian    — LU: 1209397
- Motroni, Bruno             — LU: 1208661
- Novelli, Tomas Ignacio     — LU: 1164878

[Informe del trabajo](https://docs.google.com/document/d/1EuUUv2O3vi98p8nJWFUZkK3Lhp79A5bARw5ROojmTrQ/edit?usp=sharing)

## Descripción del sistema

Aplicación de escritorio en Java con interfaz gráfica Swing para gestionar una clínica veterinaria. Permite:

- **Pacientes:** registrar dueños y mascotas (perros, gatos, aves y conejos) y transferir una mascota a otro dueño.
- **Turnos:** asignar turnos según el horario de cada veterinario, sin superposiciones; reprogramar, cancelar (con cargo si se cancela con menos de 2 horas de anticipación) y registrar ausencias. Un dueño con 3 ausencias seguidas debe pagar una seña. Las urgencias se asignan al veterinario de guardia y corren los turnos que se superponen, avisando a sus dueños.
- **Consultas e historia clínica:** el veterinario registra la consulta, el diagnóstico, los tratamientos y las vacunas aplicadas, la confirma y puede agregar notas rectificatorias. Cada mascota tiene su historia clínica.
- **Vacunas:** catálogo de vacunas por especie y edad mínima, y recordatorios de los refuerzos próximos a vencer.
- **Stock:** productos con lotes y fecha de vencimiento. Se descuenta primero del lote que vence antes y hay alerta de stock bajo.
- **Facturación:** facturas de consultas, señas y cancelaciones, con ajuste según el medio de pago (efectivo −10 %, débito, crédito en cuotas +8 %) y 15 % de descuento con plan de salud. Las facturas se anulan con nota de crédito.

Cada usuario ve las pantallas de su rol:

| Rol | Pestañas |
| --- | --- |
| Administrador | Turnos, Pacientes, Vacunas, Facturación, Stock |
| Recepcionista | Turnos, Pacientes, Vacunas, Facturación |
| Veterinario | Turnos, Pacientes, Consultas, Vacunas |

Solo el administrador puede anular facturas y cargar productos y lotes.

### Organización del código

El código está en `TPOficial/src`, organizado en capas (MVC):

| Paquete | Contenido |
| --- | --- |
| `modelo` | Clases del dominio: personas, mascotas, turnos, historia clínica, stock, facturación |
| `excepciones` | Excepciones de negocio (checked), todas heredan de `ReglaNegocioException` |
| `controlador` | Los gestores (controladores): turnos, pacientes, consultas, vacunas, facturación, stock y personal |
| `repositorio` | Interfaces de repositorio con implementaciones en archivos `.txt` y en memoria |
| `notificacion` | Interfaz `Notificador` (aviso en ventana o por consola) |
| `vista` | Ventanas Swing y el punto de entrada `vista.Principal` |

Los datos se guardan en archivos `.txt` dentro de la carpeta `datos/`, que el programa crea solo.

## Requisitos

- **JDK 21 o superior** (el proyecto de Eclipse está configurado para Java 21; probado con JDK 25). No hace falta ninguna librería externa: Swing viene incluido en el JDK.
- Opcional: IntelliJ IDEA o Eclipse.

## Instrucciones de ejecución

### Con IntelliJ IDEA

1. Abrir la carpeta del repositorio.
2. Si los archivos no aparecen como clases, hacer clic derecho en `TPOficial/src` → **Mark Directory as → Sources Root**.
3. En **File → Project Structure → Project**, elegir un SDK de Java 21 o superior.
4. Ejecutar la clase `vista.Principal` (`TPOficial/src/vista/Principal.java`).

### Con Eclipse

1. **File → Import → Existing Projects into Workspace** y elegir la carpeta `TPOficial`.
2. Ejecutar `vista.Principal` con **Run As → Java Application**.

### Desde la consola

Desde la carpeta `TPOficial`:

```bash
javac -encoding UTF-8 -d out src/module-info.java src/*/*.java
java -cp out vista.Principal
```

### Primer uso

La primera vez, la carpeta `datos/` no existe y el sistema carga datos de prueba para poder usarlo enseguida:

| Usuario | Rol |
| --- | --- |
| Laura Gómez | Administrador |
| Martín Ruiz | Recepcionista |
| Sofía Pérez | Veterinario (lunes a viernes de 9 a 18) |
| Diego Fernández | Veterinario de guardia (todos los días) |

También se cargan dos productos (amoxicilina y vacuna antirrábica) con sus lotes, y la vacuna antirrábica en el catálogo.

Al iniciar, se elige el usuario en la ventana de ingreso. Con **Cambiar usuario** se vuelve a esa ventana. Para empezar de nuevo con los datos de prueba, borrar la carpeta `datos/`.

Recorrido sugerido para probarlo:

1. Como **recepcionista**: registrar un dueño y una mascota, y asignarle un turno con Sofía Pérez.
2. Como **Sofía Pérez**: en Consultas, registrar la consulta del turno, agregar un tratamiento o una vacuna y confirmarla.
3. Como **recepcionista**: en Facturación, emitir la factura de la consulta.
4. Como **administradora**: anular la factura, o cargar un producto y un lote en Stock.
