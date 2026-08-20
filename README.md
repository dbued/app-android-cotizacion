# app-android-cotizacion

Aplicación Android nativa para crear, gestionar y compartir cotizaciones de productos o servicios. Diseñada para freelancers, comerciantes y pequeños negocios.

## Funcionalidades

### Pantalla principal - Lista de cotizaciones
- Lista de todas las cotizaciones ordenadas por fecha de creación
- Cada tarjeta muestra: precio, fecha, descripción y foto miniatura
- Botón flotante "Nueva Cotizacion" para crear cotizaciones
- Deslizar hacia la izquierda para eliminar con opción de deshacer
- Mantener presionado para confirmar eliminación
- Estado vacío cuando no hay cotizaciones registradas
- Menú de opciones para exportar a CSV o Excel

### Crear / Editar cotización
- Formulario con campos de precio y descripción con validación en tiempo real
- Captura de foto desde la cámara del dispositivo (permiso solicitado en runtime)
- Vista previa de la foto capturada
- Modo edición: pre-carga los datos existentes y permite actualizarlos
- Snackbar de confirmación al guardar

### Detalle de cotización
- Vista ampliada con foto, precio, descripción y fecha/hora
- Botón **Editar**: navega al formulario con los datos cargados
- Botón **Eliminar**: confirma y borra la cotización
- Botón **Compartir**: envía la cotización por texto (y foto si existe) a otras apps

### Exportación
- **CSV**: genera archivo `.csv` con columnas ID, Descripción, Precio, Fecha, Hora y Foto
- **Excel**: genera archivo `.xls` estilizado con colores alternados, precios en verde y encabezados formateados
- Ambos archivos se guardan en la carpeta Downloads y se comparten automáticamente

## Arquitectura

- **Patrón**: MVVM (ViewModel + StateFlow + Repository)
- **Navegación**: Jetpack Navigation Component con grafo de fragmentos
- **Base de datos**: Room (SQLite) con soporte reactivo vía Flow
- **Persistencia**: base de datos local `quotations_db` y fotos en almacenamiento interno
- **UI**: Material Design 3 con soporte completo de modo oscuro
- **100% offline**: sin llamadas a red ni servicios externos

## Stack tecnológico

| Componente | Librería |
|---|---|
| Lenguaje | Kotlin |
| Base de datos | Room 2.7.2 |
| Navegación | Jetpack Navigation 2.8.5 |
| UI | Material Design 3 1.12.0 |
| Exportación Excel | Apache POI 5.2.5 |
| Asincronía | Coroutines 1.9.0 |
| SDK mínimo | 29 (Android 10) |
| SDK objetivo | 35 (Android 15) |

## Estructura del proyecto

```
app/src/main/java/com/example/radiomodern/
├── data/
│   ├── model/Quotation.kt          # Entidad Room
│   ├── database/AppDatabase.kt     # Base de datos singleton
│   ├── database/QuotationDao.kt    # Operaciones CRUD
│   └── repository/QuotationRepository.kt
├── ui/
│   ├── list/                        # Pantalla principal
│   ├── add/                         # Formulario crear/editar
│   └── detail/                      # Vista de detalle
└── util/
    ├── CsvExporter.kt              # Exportación CSV
    └── ExcelExporter.kt            # Exportación Excel
```
